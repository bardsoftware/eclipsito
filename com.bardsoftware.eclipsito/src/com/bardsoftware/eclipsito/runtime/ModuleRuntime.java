// Copyright (C) 2020 BarD Software
package com.bardsoftware.eclipsito.runtime;

import com.bardsoftware.eclipsito.Args;
import com.bardsoftware.eclipsito.ModulesDirectoryProcessor;
import com.bardsoftware.eclipsito.PluginDescriptor;
import com.bardsoftware.eclipsito.update.UpdaterImpl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.stream.Collectors;

import static com.bardsoftware.eclipsito.Launch.LOG;

/**
 * @author dbarashev@bardsoftware.com
 */
public class ModuleRuntime {
  final UpdaterImpl updater;
  final Collection<PluginDescriptor> effectiveRuntime;

  public ModuleRuntime(Collection<PluginDescriptor> effectiveRuntime, UpdaterImpl updater) {
    this.effectiveRuntime = effectiveRuntime;
    this.updater = updater;
  }

  static ModuleRuntime build(Args args) {
    List<File> updateLayerStores = new ArrayList<>();

    // We order layers in descending order of their version numbers.
    SortedMap<String, File> layer2dir = new TreeMap<>(Comparator.reverseOrder());
    getVersionLayerStoreDirs(args.versionDirs).forEach(file -> {
      if (!file.isDirectory()) {
        if (!file.mkdirs()) {
          LOG.warning(String.format("Layer store is no a directory and can't be created: %s", file));
          return;
        }
      }
      if (!file.canRead()) {
        LOG.warning(String.format("Cannot read directory: %s", file));
        return;
      }
      updateLayerStores.add(file);
      layer2dir.putAll(collectLayers(file));
    });

    // De-duplicate plugins. We iterate over layers in descending order from the layer with the greatest version number.
    // Should we meet plugin wih the same id and different version numbers twice, one with greatest number wins.
    Map<DescriptorKey, PluginDescriptor> uniqueDescriptors = new HashMap<>();
    layer2dir.forEach((version, layerDir) -> {
      try {
        ModulesDirectoryProcessor.process(layerDir, args.descriptorPattern)
            .forEach(descriptor -> uniqueDescriptors.putIfAbsent(
                new DescriptorKey(version, descriptor.getId()), descriptor
            ));
      } catch (IOException e) {
        LOG.log(Level.SEVERE, "Failed to process plugin descriptors", e);
        die("");
      }
    });

    try (BareFormatter formatter = new BareFormatter()) {
      LOG.fine("We will run with the following plugins:");
      uniqueDescriptors.forEach((key, descriptor) -> {
        LOG.fine(String.format("%s at %s", key, descriptor.getLocation()));
      });
    }
    return new ModuleRuntime(uniqueDescriptors.values(), new UpdaterImpl(updateLayerStores, layer2dir.keySet()));
  }

  private static List<File> getVersionLayerStoreDirs(String storeSpec) {
    return Arrays.stream(storeSpec.split(File.pathSeparator))
        .map(path -> path.startsWith("~/")
            ? path.replaceFirst("~", System.getProperty("user.home").replace("\\", "/"))
            : path)
        .map(path -> {
          if (!Paths.get(path).isAbsolute()) {
            LOG.fine(String.format("Path `%s` is not absolute. We'll try resolving it relative to user.dir=%s",
                path, System.getProperty("user.dir")));
            File layerStoreDir = new File(System.getProperty("user.dir"), path);
            if (layerStoreDir.isDirectory() && layerStoreDir.canRead()) {
              path = layerStoreDir.getAbsolutePath();
            } else {
              LOG.warning(String.format("Can't resolve path %s as a readable directory. Skipping it.",
                  layerStoreDir.getAbsolutePath()));
            }
          }
          return path.replace("/", File.separator);
        })
        .map(File::new)
        .collect(Collectors.toList());
  }

  private static SortedMap<String, File> collectLayers(File layerStore) {
    TreeMap<String, File> result = new TreeMap<>();
    for (File layer : layerStore.listFiles(f -> f.isDirectory())) {
      String version = getVersionNumber(layer);
      if (version == null) {
        LOG.warning(String.format("Can't find VERSION file in directory=%s. This directory will be skipped", layer.getAbsolutePath()));
        continue;
      }
      result.put(version, layer);
    }
    return result;
  }

  private static String getVersionNumber(File layerDir) {
    File versionFile = new File(layerDir, "VERSION");
    try (BufferedReader br = new BufferedReader(new FileReader(versionFile))) {
      String version = br.readLine();
      assert version != null : "Empty version file";
      return version;
    } catch (IOException e) {
      LOG.severe("No version file found");
    }
    return null;
  }

  private static void die(String message) {
    LOG.severe(message);
    System.exit(1);
  }

  private static class BareFormatter implements AutoCloseable {
    private Map<Handler, Formatter> handler2formatter = new HashMap<>();

    BareFormatter() {
      for (Handler h : LogManager.getLogManager().getLogger("").getHandlers()) {
        handler2formatter.put(h, h.getFormatter());
        h.setFormatter(new Formatter() {
          @Override
          public String format(LogRecord logRecord) {
            return String.format("%s\n", formatMessage(logRecord));
          }
        });
      }
    }

    @Override
    public void close() {
      handler2formatter.forEach(Handler::setFormatter);
    }
  }

  private static class DescriptorKey {
    final String version;
    final String pluginId;

    DescriptorKey(String version, String pluginId) {
      this.version = version;
      this.pluginId = pluginId;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      DescriptorKey that = (DescriptorKey) o;
      return Objects.equals(pluginId, that.pluginId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(pluginId);
    }

    @Override
    public String toString() {
      return String.format("Plugin %s-%s", this.pluginId, this.version);
    }
  }
}
