package org.bardsoftware.impl.eclipsito;

import java.util.logging.Level;

import org.bardsoftware.eclipsito.Boot;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

class InternalReloader implements Runnable {
    private String myThreadName = "InternalRealoader";
    private Thread myThread;
    private boolean isThreadDone = false; // thread completion semaphore
    private int myCheckInterval = 2; // the number of seconds between checks for modified classes 
    private Bundle myBundle;

    InternalReloader(Bundle bundle) {
        myBundle = bundle;
    }

    public int getCheckInterval() {
        return myCheckInterval;
    }

    public void setCheckInterval(int checkInterval) {
        myCheckInterval = checkInterval;
    }

    // background thread that checks for session timeouts and shutdown
    public void run() {
        Boot.LOG.log(Level.FINE, "Background thread started");
        // Loop until the termination semaphore is set
        while (!isThreadDone) {
            sleepForCheckInterval();
            bundleModificationCheck();
        }
        Boot.LOG.log(Level.FINE, "Background thread stopped");
    }

    void threadStart() {
        if (myThread != null) return;
        Boot.LOG.log(Level.FINE, "Starting background thread");
        isThreadDone = false;
        myThreadName = "InternalReloader[" + myBundle.getSymbolicName() + "]";
        myThread = new Thread(this, myThreadName);
        myThread.setDaemon(true);
        myThread.start();
    }

    void threadStop() {
        if (myThread == null) return;
        Boot.LOG.log(Level.FINE, "Stopping background thread");
        isThreadDone = true;
        myThread.interrupt();
        try {
            myThread.join();
        } catch (InterruptedException e) {
            // ignore
        }
        myThread = null;
    }

    private void sleepForCheckInterval() {
        try {
            Thread.sleep(myCheckInterval * 1000L);
        } catch (InterruptedException e) {
        }
    }

    private void bundleModificationCheck() {
        new Thread(){
            public void run() {
                try {
                    myBundle.update();
                } catch (BundleException e) {
                    Boot.LOG.log(Level.WARNING, "Problems while reloading bundle: "+myBundle.getSymbolicName());
                }
            }
        }.start();
    }

}
