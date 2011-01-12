package org.bardsoftware.test.eclipsito.dependencies;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Plugin4class extends TestPlugin implements BundleActivator {
    private static int ourStartNumber;

    public void start(BundleContext context) throws Exception {
        ++ourStartedPluginCounter;
        ourStartNumber = ourStartedPluginCounter;
    }

    public void stop(BundleContext context) throws Exception {
    }

    public static int getStartNumber() {
        return ourStartNumber;
    }

}
