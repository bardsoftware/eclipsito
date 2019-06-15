// Copyright (C) 2019 BarD Software
package com.bardsoftware.eclipsito;

import com.bardsoftware.eclipsito.runtime.Runner;
import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

class Args {
  @Parameter(names = "--descriptor-pattern", description = "Regexp on the plugin descriptor file name")
  String descriptorPattern = "plugin.xml";

  @Parameter(names = "--bundles", description = "The root directory of all versioned bundles")
  String bundleDirs;

  @Parameter(names = "--app", description = "Application to run")
  String app;

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

    System.err.println(args.appArgs);
    SortedMap<String, File> version2bundleDir = new TreeMap<>(Comparator.reverseOrder());
    getBundleDirs(args.bundleDirs).forEach(file -> {
      if (!file.isDirectory()) {
        die(String.format("Not a directory: %s", file));
      }
      if (!file.canRead()) {
        die(String.format("Cannot read directory: %s", file));
      }
      version2bundleDir.putAll(collectVersionedBundles(file));
    });

    SortedMap<String, PluginDescriptor> version2descriptor = new TreeMap<>();
    version2bundleDir.forEach((version, bundleDir) -> {
      try {
        ModulesDirectoryProcessor.process(bundleDir, args.descriptorPattern)
            .forEach(descriptor -> {
              String key = String.format("%s: %s", version, descriptor.getId());
              version2descriptor.putIfAbsent(key, descriptor);
            });
      } catch (IOException e) {
        e.printStackTrace();
      }
    });
    Runner runner = new Runner();
    final PluginDescriptor[] descriptors = version2descriptor.values()
        .toArray(new PluginDescriptor[version2descriptor.size()]);
    runner.run(descriptors, args.app, new String[0]);
  }

  static List<File> getBundleDirs(String bundleDir) {
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


}
