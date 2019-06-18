// Copyright (C) 2019 BarD Software
package com.bardsoftware.eclipsito.update;

import com.grack.nanojson.JsonArray;
import com.grack.nanojson.JsonObject;
import com.grack.nanojson.JsonParser;
import com.grack.nanojson.JsonParserException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author dbarashev@bardsoftware.com
 */
public class Updater {

  private final List<File> versionLayerRoots;

  public Updater(Collection<File> versionLayerRoots) {
    assert versionLayerRoots != null && !versionLayerRoots.isEmpty(): "The list of version layer roots is supposed to be non-empty";
    this.versionLayerRoots = new ArrayList(versionLayerRoots);
  }

  public CompletableFuture<List<VersionLayerInfo>> fetchUpdates(String updateUrl) {
    CompletableFuture<List<VersionLayerInfo>> result = new CompletableFuture<>();
    new Thread(() -> {
      OkHttpClient httpClient = new OkHttpClient();
      Request req = new Request.Builder().url(updateUrl).build();
      try (Response resp = httpClient.newCall(req).execute()) {
        result.complete(parseUpdates(resp.body().string()));
      } catch (IOException | JsonParserException e) {
        result.completeExceptionally(e);
      }
    }).start();
    return result;
  }

  private List<VersionLayerInfo> parseUpdates(String json) throws JsonParserException {
    List<VersionLayerInfo> result = new ArrayList<>();
    JsonArray allUpdates = JsonParser.array().from(json);
    for (int i = 0; i < allUpdates.size(); i++) {
      JsonObject update = allUpdates.getObject(i);
      if (update.has("version") && update.has("url")) {
        result.add(new VersionLayerInfo(
            update.getString("version"),
            update.getString("url"),
            update.getString("description", "")
        ));
      }
    }
    return result;
  }


  public CompletableFuture<File> installUpdate(VersionLayerInfo versionLayer) {

  }
}
