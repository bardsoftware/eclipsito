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
    SortedMap<String, File> version2dir = new TreeMap<>(Comparator.reverseOrder());
    getVersionLayerDirs(args.versionDirs).forEach(file -> {
      if (!file.isDirectory()) {
        die(String.format("Not a directory: %s", file));
      }
      if (!file.canRead()) {
        die(String.format("Cannot read directory: %s", file));
      }
      version2dir.putAll(collectVersionedBundles(file));
    });

    SortedMap<String, PluginDescriptor> version2descriptor = new TreeMap<>();
    version2dir.forEach((version, bundleDir) -> {
      try {
        ModulesDirectoryProcessor.process(bundleDir, args.descriptorPattern)
            .forEach(descriptor -> {
              String key = String.format("%s: %s", version, descriptor.getId());
              version2descriptor.putIfAbsent(key, descriptor);
            });
      } catch (IOException e) {
        LOG.log(Level.SEVERE, "Failed to process plugin descriptors", e);
        die("");
      }
    });
    try (BareFormatter formatter = new BareFormatter()) {
      LOG.info("We will run with the following plugins:");
      version2descriptor.forEach((version, descriptor) -> {
        LOG.info(String.format("%s at %s", version, descriptor.myLocationUrl));
      });
    }
    Updater updater = new Updater(version2dir.values());
    PlatformImpl platform = new PlatformImpl(updater);
    Runner runner = new Runner(platform);
    final PluginDescriptor[] descriptors = version2descriptor.values()
        .toArray(new PluginDescriptor[0]);
    runner.run(descriptors, args.app, args.appArgs.toArray(new String[0]));
  }

  static List<File> getVersionLayerDirs(String bundleDir) {
    return Arrays.stream(bundleDir.split(File.pathSeparator))
        .map(path -> path.startsWith("~/") ? path.replaceFirst("~", System.getProperty("user.home")) : path)
        .map(File::new)
        .collect(Collectors.toList());
  }

  static SortedMap<String, File> collectVersionedBundles(File bundlesDir) {
    TreeMap<String, File> result = new TreeMap<>();
    for (File bundle : bundlesDir.listFiles(f -> f.isDirectory())) {
      String bundleVersion = getVersionNumber(bundle);
      if (bundleVersion == null) {
        LOG.warning(String.format("Can't find VERSION file in directory=%s. This directory will be skipped", bundle.getAbsolutePath()));
        continue;
      }
      result.put(bundleVersion, bundle);
    }
    return result;
  }

  static String getVersionNumber(File modulesDir) {
    File versionFile = new File(modulesDir, "VERSION");
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


}
