// Copyright (C) 2019 BarD Software
package com.bardsoftware.eclipsito.update;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author dbarashev@bardsoftware.com
 */
public interface Updater {
  CompletableFuture<List<UpdateMetadata>> getUpdateMetadata(String updateUrl);
}
