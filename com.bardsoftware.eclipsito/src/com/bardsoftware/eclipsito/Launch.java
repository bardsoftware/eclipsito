// Copyright (C) 2019 BarD Software
package com.bardsoftware.eclipsito;

import com.bardsoftware.eclipsito.runtime.RunLoop;
import com.beust.jcommander.JCommander;

import java.util.Objects;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    args.versionDirs = Objects.requireNonNullElse(args.versionDirs, System.getProperty("versionDirs"));
    args.app = Objects.requireNonNullElse(args.app, System.getProperty("app"));

    var handler = new ConsoleHandler();
    LOG.addHandler(handler);
    handler.setLevel(Level.ALL);
    handler.setFormatter(new java.util.logging.SimpleFormatter());

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

    RunLoop runLoop = new RunLoop(args);
    runLoop.start();
  }


}
