// Copyright (C) 2019 BarD Software
package com.bardsoftware.eclipsito.update;

import com.bardsoftware.eclipsito.Launch;
import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * @author dbarashev@bardsoftware.com
 */
public class UpdaterImpl implements Updater{

  private final List<File> updateLayerStores;
  private final Set<String> installedUpdateVersions;

  public UpdaterImpl(Collection<File> updateLayerStores, Set<String> installedUpdateVersions) {
    assert updateLayerStores != null && !updateLayerStores.isEmpty(): "Empty list of update layer stores";
    this.updateLayerStores = new ArrayList(updateLayerStores);
    this.installedUpdateVersions = installedUpdateVersions;
  }

  public CompletableFuture<List<UpdateMetadata>> getUpdateMetadata(String updateUrl) {
    HttpClient httpClient = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();
    HttpRequest req = HttpRequest.newBuilder().uri(URI.create(updateUrl)).build();
    return httpClient.sendAsync(req, HttpResponse.BodyHandlers.ofString()).thenApply(resp -> {
      try {
        if (resp.statusCode() == 200) {
          return parseUpdates(resp.body())
              .stream()
              .filter(update -> !isInstalled(update))
              .sorted(Comparator.reverseOrder())
              .collect(Collectors.toList());
        } else {
          Launch.LOG.warning(String.format(
              "Received HTTP %d when requesting updates from %s", resp.statusCode(), updateUrl
          ));
          return Collections.<UpdateMetadata>emptyList();
        }
      } catch (JsonParserException e) {
        throw new CompletionException(e);
      }
    }).exceptionally(ex -> {
      Launch.LOG.log(Level.SEVERE, String.format("Failed to fetch updates from %s", updateUrl), ex);
      return Collections.emptyList();
    });
  }

  private List<UpdateMetadata> parseUpdates(String json) throws JsonParserException {
    List<UpdateMetadata> result = new ArrayList<>();
    JsonArray allUpdates = JsonParser.array().from(json);
    for (int i = 0; i < allUpdates.size(); i++) {
      JsonObject update = allUpdates.getObject(i);
      if (update.has("version") && update.has("url")) {
        result.add(new UpdateMetadata(
            update.getString("version"),
            update.getString("url"),
            update.getString("description", ""),
            update.getString("date", ""),
            update.getInt("size", -1)
        ));
      }
    }
    return result;
  }

  public CompletableFuture<File> installUpdate(UpdateMetadata updateMetadata, UpdateProgressMonitor monitor) throws IOException {
    DownloadWorker updateInstaller = new DownloadWorker(getUpdateLayerStore());
    return updateInstaller.downloadUpdate(updateMetadata.url, monitor);
  }

  public Set<String> getInstalledUpdateVersions() {
    return Set.copyOf(installedUpdateVersions);
  }

  private boolean isInstalled(UpdateMetadata update) {
    return this.installedUpdateVersions.contains(update.version);
  }

  private File getUpdateLayerStore() throws IOException {
    return this.updateLayerStores.stream()
        .filter(UpdaterImpl::isWritableDirectory)
        .findFirst().orElseThrow(() -> new IOException("Cannot find writable directory for installing update"));
  }

  private static boolean isWritableDirectory(File dir) {
    // dbarashev: Apparently on some systems a directory may be reported as "canWrite" while actually
    // forbidding creating new directories inside. I observed this on Win 8.1 with C:\Program Files (x86)\GanttProject-2.99
    // So we use Files.isWritable as suggested here: https://bugs.openjdk.java.net/browse/JDK-8148211
    return dir.exists() && dir.isDirectory() && dir.canWrite() && Files.isWritable(dir.toPath());
  }
}
