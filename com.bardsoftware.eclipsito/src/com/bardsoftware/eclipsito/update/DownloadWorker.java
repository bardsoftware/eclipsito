// Copyright (C) 2019 BarD Software
package com.bardsoftware.eclipsito.update;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author dbarashev@bardsoftware.com
 */
public class DownloadWorker {
  static final Logger LOG = Logger.getLogger("Eclipsito");
  private final File myLayerDir;

  private static final int BUFFER_SIZE = 4096;

  DownloadWorker(File layerDir) {
    myLayerDir = layerDir;
  }

  CompletableFuture<File> downloadUpdate(String downloadUrl, UpdateProgressMonitor monitor) throws IOException {
    HttpClient httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();
    HttpRequest req = HttpRequest.newBuilder().uri(URI.create(downloadUrl)).build();
    File tempFile = File.createTempFile("ganttproject-update", "zip");
    return httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofInputStream())
        .thenApply(resp -> {
          if (resp.statusCode() == 200) {
            return downloadZip(resp, tempFile, monitor);
          } else {
            throw new RuntimeException(String.format(
                "Cannot download update from %s. Server responds with %s",
                downloadUrl, String.valueOf(resp.statusCode())
            ));
          }
        })
        .thenApply(this::unzipUpdates);
  }

  private File downloadZip(HttpResponse<InputStream> resp, File outFile, UpdateProgressMonitor monitor) {
    try {
      long fileSize = resp.headers().firstValueAsLong("content-length").orElse(-1);
      LOG.info(String.format("Will download %d bytes and save as file %s", fileSize, outFile.getAbsolutePath()));
      try (InputStream inputStream = resp.body();
           OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outFile))) {

        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead;
        long totalBytesRead = 0;

        while ((bytesRead = inputStream.read(buffer)) != -1) {
          outputStream.write(buffer, 0, bytesRead);
          totalBytesRead += bytesRead;
          int percentCompleted = (int) (totalBytesRead * 100 / fileSize);

          monitor.progress(percentCompleted);
        }
      }
      return outFile;
    } catch (IOException e) {
      throw new CompletionException(e);
    }
  }

  private File unzipUpdates(File updateFile) {
    File folder = myLayerDir;
    if (!folder.exists()) {
      if (!folder.mkdirs()) {
        throw new RuntimeException(String.format(
            "Folder %s does not exist and failed to create", folder.getAbsolutePath()));
      }
    }

    Set<File> oldFiles = new HashSet<>(Arrays.asList(Objects.requireNonNullElse(folder.listFiles(), new File[0])));
    try (ZipInputStream zis = new ZipInputStream(new FileInputStream(updateFile.getAbsolutePath()))) {
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
            throw new CompletionException(String.format(
                "Failed to unzip entry %s", zipEntry.getName()), e);
          }
        }
        zis.closeEntry();
      }

      Set<File> newFiles = new HashSet<>(Arrays.asList(folder.listFiles()));
      newFiles.removeAll(oldFiles);
      switch (newFiles.size()) {
        case 0: throw new RuntimeException(String.format(
            "Invalid update: no new files installed into %s", folder.getAbsolutePath()));
        case 1: return newFiles.stream().findFirst().get();
        default: throw new RuntimeException(String.format(
            "Invalid update: too many new files installed into %s\n%s", folder.getAbsolutePath(), newFiles
        ));
      }
    } catch (IOException e) {
      throw new CompletionException(e);
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
