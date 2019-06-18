// Copyright (C) 2019 BarD Software
package com.bardsoftware.eclipsito.update;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author dbarashev@bardsoftware.com
 */
public class DownloadWorker {
  public static final Logger LOG = Logger.getLogger("Eclipsito");
  private final File myLayerDir;

  interface UIFacade {
    void reportProgress(int progress);
  }
  private static final int BUFFER_SIZE = 4096;
  private String myDownloadURL;
  private UIFacade myUiFacade;

  public DownloadWorker(UIFacade uiFacade, File layerDir, String downloadURL) {
    myUiFacade = uiFacade;
    myDownloadURL = downloadURL;
    myLayerDir = layerDir;
  }

  CompletableFuture<File> downloadUpdate() {
    CompletableFuture<File> result = new CompletableFuture<>();
    new Thread(() -> {
      OkHttpClient httpClient = new OkHttpClient();
      Request req = new Request.Builder().url(myDownloadURL).build();
      try (Response resp = httpClient.newCall(req).execute()) {
        if (resp.code() == 200) {
          File tempFile = File.createTempFile("ganttproject-update", "zip");
          downloadZip(resp, tempFile);
          unzipUpdates(tempFile);
        } else {
          throw new IOException(String.format(
              "Cannot download update from %s. Server responds with %s",
              myDownloadURL, String.valueOf(resp.code())
          ));
        }
      } catch (IOException e) {
        result.completeExceptionally(e);
      }
    }).start();
    return result;
  }

  private void downloadZip(Response resp, File outFile) throws IOException  {
    long fileSize = resp.body().contentLength();
    try (InputStream inputStream = resp.body().byteStream();
         FileOutputStream outputStream = new FileOutputStream(outFile)) {

      byte[] buffer = new byte[BUFFER_SIZE];
      int bytesRead;
      long totalBytesRead = 0;

      while ((bytesRead = inputStream.read(buffer)) != -1) {
        outputStream.write(buffer, 0, bytesRead);
        totalBytesRead += bytesRead;
        int percentCompleted = (int) (totalBytesRead * 100 / fileSize);

        myUiFacade.reportProgress(percentCompleted);
      }
    }
  }

  private void unzipUpdates(File updateFile) throws IOException {
    try (ZipInputStream zis = new ZipInputStream(new FileInputStream(updateFile.getAbsolutePath()))) {

      File folder = myLayerDir;
      if (!folder.exists()) {
        if (!folder.mkdirs()) {
          throw new IOException(String.format(
              "Folder %s does not exist and failed to create", folder.getAbsolutePath()));
        }
      }

      ZipEntry zipEntry;
      while ((zipEntry = zis.getNextEntry()) != null) {
        String fileName = zipEntry.getName();

        if (zipEntry.isDirectory()) {
          File dir = new File(myLayerDir, zipEntry.getName());
          if (!dir.exists()) {
            if (!dir.mkdirs()) {
              throw new IOException(String.format(
                  "Failed to unzip updates. Cannot create folder %s", dir.getAbsolutePath()));
            }
          }
        } else {
          File newFile = new File(myLayerDir, fileName);
          File parent = newFile.getParentFile();
          if (!parent.exists()) {
            if (!parent.mkdirs()) {
              throw new IOException(String.format(
                  "Failed to unzip updates. Cannot create folder %s", parent.getAbsolutePath()));
            }
          }

          try (FileOutputStream fos = new FileOutputStream(newFile)) {
            copy(zis, fos);
          } catch (IOException e) {
            throw new IOException(String.format(
                "Failed to unzip entry %s", zipEntry.getName()), e);
          }
        }
        zis.closeEntry();
      }
    }
  }

  private static long copy(InputStream from, OutputStream to) throws IOException {
    byte[] buf = new byte[4096];
    long total = 0;
    while (true) {
      int r = from.read(buf);
      if (r == -1) {
        break;
      }
      to.write(buf, 0, r);
      total += r;
    }
    return total;
  }
}
