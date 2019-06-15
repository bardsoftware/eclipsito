// Copyright (C) 2019 BarD Software
package com.bardsoftware.eclipsito;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

class Args {
  @Parameter(names = "--plugins", description = "The root directory of all plugin versions")
  String pluginsDir;

  @Parameter(names = "--help", help = true)
  boolean help;
}

/**
 * @author dbarashev@bardsoftware.com
 */
public class Launch {
  public static final Logger LOG = Logger.getLogger("Launcher");

  public static void main(String[] argv) {
    Args args = new Args();
    JCommander parser = JCommander.newBuilder().addObject(args).build();
    parser.parse(argv);
    if (args.help) {
      parser.usage();
      System.exit(0);
    }
    List<File> moduleDirs = getModuleDirs(args.pluginsDir);
    moduleDirs.forEach(file -> {
      if (!file.isDirectory()) {
        die(String.format("Not a directory: %s", file));
      }
      if (!file.canRead()) {
        die(String.format("Cannot read directory: %s", file));
      }
    });

    File highestVersion = getVersionDir(moduleDirs);

  }

  static List<File> getModuleDirs(String pluginsDir) {
    return Arrays.stream(pluginsDir.split(File.pathSeparator))
        .map(path -> path.startsWith("~") ? path.replaceFirst("~", System.getProperty("user.home")) : path)
        .map(File::new)
        .collect(Collectors.toList());
  }

  static File getVersionDir(List<File> modulesDir) {
    if (modulesDir.isEmpty()) {
      die("No module directories found");
    }
    String version = null;
    File versionDir = null;
    for (File dir : modulesDir) {
      String dirVersion = getVersionNumber(dir);
      if (version == null
          || (dirVersion != null && version.compareTo(dirVersion) < 0)) {
        File newVersionDir = new File(dir, dirVersion);
        if (versionDir.exists() && versionDir.isDirectory() && versionDir.canRead()) {
          version = dirVersion;
          versionDir = newVersionDir;
        } else {
          LOG.severe("Cannot read folder with path " + dir.getPath() + File.separator + dirVersion);
        }
      }
    }
    return versionDir;
  }

  static String getVersionNumber(File modulesDir) {
    File versionFile = new File(modulesDir + File.separator + "VERSION");
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
