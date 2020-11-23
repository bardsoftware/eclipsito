// Copyright (C) 2020 BarD Software
package com.bardsoftware.eclipsito.runtime;

import com.bardsoftware.eclipsito.Args;
import com.bardsoftware.eclipsito.Launch;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;

/**
 * @author dbarashev@bardsoftware.com
 */
public class RunLoop {
  private static final ThreadGroup topThreadGroup = new ThreadGroup("TopThreadGroup") {
    public void uncaughtException(Thread t, Throwable e) {
      Launch.LOG.log(Level.WARNING, "[uncaughtException]" + e, e);
    }
  };
  private final Args cmdLine;
  private ExecutorService executor = Executors.newSingleThreadExecutor(r -> new Thread(topThreadGroup, r, "GanttProject Runner"));
  private Runner currentRunner;
  private Future<?> currentRun;

  public RunLoop(Args args) {
    this.cmdLine = args;
    Runtime.getRuntime().addShutdownHook(new ShutdownHook());
  }

  public void start() {
    ModuleRuntime moduleRuntime = ModuleRuntime.build(this.cmdLine);
    PlatformImpl platform = new PlatformImpl(moduleRuntime.updater, () -> {
      RunLoop.this.restart();
    });
    this.currentRun = this.executor.submit(new Runner(
        platform,
        moduleRuntime.effectiveRuntime,
        this.cmdLine.app,
        this.cmdLine.appArgs.toArray(new String[0])
    ));
  }

  private void restart() {
    if (currentRun != null) {
      currentRun.cancel(true);
    }
    start();
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

  public void shutdown() {
    if (currentRunner != null) {
      currentRunner.stop();
    }
    executor.shutdown();
    Launch.LOG.fine("Eclipsito platform is shut down.");
  }


}
