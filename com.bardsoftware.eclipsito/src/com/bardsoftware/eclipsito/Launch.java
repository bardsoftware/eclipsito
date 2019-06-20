// Copyright (C) 2019 BarD Software
package com.bardsoftware.eclipsito;

import com.bardsoftware.eclipsito.runtime.PlatformImpl;
import com.bardsoftware.eclipsito.runtime.Runner;
import com.bardsoftware.eclipsito.update.Updater;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
import java.util.logging.Logger;
import java.util.stream.Collectors;

class Args {
  @Parameter(names = "--descriptor-pattern", description = "Regexp on the plugin descriptor file name")
  String descriptorPattern = "plugin.xml";

  @Parameter(names = "--version-dirs", description = "The list of version layer directories")
  String versionDirs;

  @Parameter(names = "--app", description = "Application to run")
  String app;

  @Parameter(names = { "--verbosity", "-v" }, description = "Logging verbosity level 0+")
  Integer verbosity = 2;

  @Parameter(names = "--help", help = true)
  boolean help;

  @Parameter(description = "Application arguments")
  List<String> appArgs = new ArrayList<>();
}

/**
 * @author dbarashev@bardsoftware.com
 */
public class Launch {
  public static final Logger LOG = Logger.getLogger("Eclipsito");

  public static void main(String[] argv) {
    Args args = new Args();
    JCommander parser = JCommander.newBuilder().addObject(args).build();
    parser.parse(argv);
    if (args.help) {
      parser.usage();
      System.exit(0);
    }

    switch(args.verbosity) {
      case 0: LOG.setLevel(Level.OFF); break;
      case 1: LOG.setLevel(Level.SEVERE); break;
      case 2: LOG.setLevel(Level.WARNING); break;
      case 3: LOG.setLevel(Level.INFO); break;
      case 4: LOG.setLevel(Level.FINE); break;
      case 5: LOG.setLevel(Level.FINER); break;
      case 6: LOG.setLevel(Level.FINEST); break;
      default: LOG.setLevel(Level.ALL);
    }
    List<File> updateLayerStores = new ArrayList<>();

    // We order layers in descending order of their version numbers.
    SortedMap<String, File> layer2dir = new TreeMap<>(Comparator.reverseOrder());
    getVersionLayerStoreDirs(args.versionDirs).forEach(file -> {
      if (!file.isDirectory()) {
        die(String.format("Not a directory: %s", file));
      }
      if (!file.canRead()) {
        die(String.format("Cannot read directory: %s", file));
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
      LOG.info("We will run with the following plugins:");
      uniqueDescriptors.forEach((key, descriptor) -> {
        LOG.info(String.format("%s at %s", key, descriptor.getLocation()));
      });
    }
    Updater updater = new Updater(updateLayerStores, layer2dir.keySet());
    PlatformImpl platform = new PlatformImpl(updater);
    Runner runner = new Runner(platform);
    runner.run(
        uniqueDescriptors.values().toArray(new PluginDescriptor[0]),
        args.app,
        args.appArgs.toArray(new String[0])
    );
  }

  private static List<File> getVersionLayerStoreDirs(String storeSpec) {
    return Arrays.stream(storeSpec.split(File.pathSeparator))
        .map(path -> path.startsWith("~/") ? path.replaceFirst("~", System.getProperty("user.home")) : path)
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
