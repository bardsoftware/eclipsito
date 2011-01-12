package org.bardsoftware.test.eclipsito.dependencies;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Plugin11class implements BundleActivator {
    private static boolean wasStarted = false;

    public void start(BundleContext context) throws Exception {
        System.out.println("[Plugin5class] start warning, plugin should NOT be started!!!");
        wasStarted = true;
    }

    public void stop(BundleContext context) throws Exception {
    }

    public static boolean wasStarted() {
        return wasStarted;
    }

}
