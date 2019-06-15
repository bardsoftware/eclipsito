// Copyright (C) 2019 BarD Software
package com.bardsoftware.eclipsito.runtime;

import com.bardsoftware.eclipsito.Launch;
import com.bardsoftware.eclipsito.PluginDescriptor;

import java.util.Arrays;
import java.util.logging.Level;

/**
 * @author dbarashev@bardsoftware.com
 */
public class Runner {
  private static final ThreadGroup topThreadGroup = new ThreadGroup("TopThreadGroup") {
    public void uncaughtException(Thread t, Throwable e) {
      Launch.LOG.log(Level.WARNING, "[uncaughtException]" + e, e);
    }
  };

  private PlatformImpl myPlatform = new PlatformImpl();

  public void run(PluginDescriptor[] plugins, final String application, final String[] args) {
    if (plugins.length == 0) {
      Launch.LOG.severe("No plugins found");
    }
    Launch.LOG.fine("Command line args: " + Arrays.asList(args));
    myPlatform.setup(plugins);
    new Thread(topThreadGroup, "Start") {
      public void run() {
        myPlatform.start();
        // after all bundles are started up, we can
        // launch an application which could use all supplied bundle services
        if (application != null && application.length() > 0) {
          ApplicationLauncher.launchApplication(application, args);
        }
      }
    }.start();
    Runtime.getRuntime().addShutdownHook(new ShutdownHook());
  }

  public void shutdown() {
    myPlatform.stop();
    Launch.LOG.fine("Eclipsito platform is shut down.");
  }

  private class ShutdownHook extends Thread {
    public ShutdownHook() {
      super(topThreadGroup, "ShutdownHook-Thread");
      setPriority(Thread.MAX_PRIORITY);
    }

    public void run() {
      shutdown();
    }
  }

}
