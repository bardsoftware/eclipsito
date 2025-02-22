// Copyright (C) 2019 BarD Software
package com.bardsoftware.eclipsito.runtime;

import com.bardsoftware.eclipsito.Launch;
import com.bardsoftware.eclipsito.PluginDescriptor;

import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;

/**
 * @author dbarashev@bardsoftware.com
 */
public class Runner implements Runnable {
  private final PlatformImpl myPlatform;
  private final Collection<PluginDescriptor> plugins;
  private final String application;
  private final String[] args;

  public Runner(PlatformImpl platform, Collection<PluginDescriptor> plugins, final String application, final String[] args) {
    myPlatform = platform;
    this.plugins = plugins;
    this.application = application;
    this.args = args;
  }

  public void run() {
    if (plugins.isEmpty()) {
      Launch.LOG.severe("No plugins found");
    }
    Launch.LOG.fine("Command line args: " + Arrays.asList(args));
    myPlatform.setup(plugins.toArray(new PluginDescriptor[0]));
    myPlatform.start();
    Launch.LOG.fine("Launching application="+application);
    // after all bundles are started up, we can
    // launch an application which could use all supplied bundle services
    try {
      if (application != null && application.length() > 0) {
        ApplicationLauncher.launchApplication(application, args);
      }
    } catch (Throwable t) {
      Launch.LOG.log(Level.SEVERE, "Failed to launch the application", t);
    }
  }

  void stop() {
    myPlatform.stop();
  }
}
