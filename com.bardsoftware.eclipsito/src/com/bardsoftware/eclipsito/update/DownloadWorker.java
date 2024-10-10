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

  private static final int BUFFER_SIZE = 4096;
  private final UpdateUnzipper unzipper;
  DownloadWorker(File layerDir) {
    unzipper = new UpdateUnzipper(layerDir);
  }

  CompletableFuture<File> downloadUpdate(
      UpdateMetadata metadata,
      UpdateProgressMonitor monitor,
      UpdateIntegrityChecker integrityChecker) throws IOException {
    String downloadUrl = metadata.url;
    HttpClient httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();
    HttpRequest req = HttpRequest.newBuilder().uri(URI.create(downloadUrl)).build();
    File tempFile = File.createTempFile("ganttproject-update", "zip");
    return httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofInputStream())
        .thenApply(resp -> {
          if (resp.statusCode() == 200) {
            // If the HTTP request has completed successfully, we save the ZIP file and run the
            // integrity check. Otherwise we terminate exceptionally and report the status code.
            File zipFile = downloadZip(resp, tempFile, monitor);
            if (integrityChecker.verify(zipFile)) {
              return zipFile;
            } else {
              throw new RuntimeException(String.format(
                 "Update %s failed to pass the integrity check.", metadata.version
              ));
            }
          } else {
            throw new RuntimeException(String.format(
                "Cannot download update from %s. Server responded with HTTP %s",
                downloadUrl, resp.statusCode()
            ));
          }
        })
        .thenApply(unzipper::unzipUpdates);
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

}
