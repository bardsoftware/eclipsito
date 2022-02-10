// Copyright (C) 2020 BarD Software
package com.bardsoftware.eclipsito;

import com.beust.jcommander.Parameter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dbarashev@bardsoftware.com
 */
public class Args {
  @Parameter(names = "--descriptor-pattern", description = "Regexp on the plugin descriptor file name")
  public
  String descriptorPattern = "plugin.xml";

  @Parameter(names = "--version-dirs", description = "The list of version layer directories")
  public
  String versionDirs;

  @Parameter(names = "--app", description = "Application to run")
  public
  String app;

  @Parameter(names = {"--verbosity", "-v"}, description = "Logging verbosity level 0+")
  Integer verbosity = 4;

  @Parameter(names = "--help", help = true)
  boolean help;

  @Parameter(description = "Application arguments")
  public
  List<String> appArgs = new ArrayList<>();
}
