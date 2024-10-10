/*
 * Copyright 2024 BarD Software s.r.o., Dmitry Barashev.
 *
 * This file is part of GanttProject, an opensource project management tool.
 *
 * GanttProject is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 * GanttProject is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GanttProject.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.bardsoftware.eclipsito.update;

import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletionException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UpdateUnzipper {
    private final File myLayerDir;

    UpdateUnzipper(File layerDir) {
        myLayerDir = layerDir;
    }

    public File unzipUpdates(File updateFile) {
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
