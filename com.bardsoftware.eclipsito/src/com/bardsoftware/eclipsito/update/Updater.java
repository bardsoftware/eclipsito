// Copyright (C) 2019 BarD Software
package com.bardsoftware.eclipsito.update;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author dbarashev@bardsoftware.com
 */
public interface Updater {
  CompletableFuture<List<UpdateMetadata>> getUpdateMetadata(String updateUrl);
  CompletableFuture<File> installUpdate(UpdateMetadata updateMetadata, UpdateProgressMonitor monitor, UpdateIntegrityChecker integrityChecker) throws IOException;
  CompletableFuture<File> installUpdate(File zipfile) throws IOException;
}
