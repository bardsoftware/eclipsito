package org.bardsoftware.test.eclipsito.dependencies;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Plugin6class implements BundleActivator {
    private static boolean wasStarted = false;

    public void start(BundleContext context) throws Exception {
        wasStarted = true;
    }

    public void stop(BundleContext context) throws Exception {
    }

    public static boolean wasStarted() {
        return wasStarted;
    }

}
