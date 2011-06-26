package org.bardsoftware.impl.eclipsito;

import java.net.URI;
import java.net.URL;
import java.security.AllPermission;
import java.security.Policy;
import java.util.logging.Level;

import org.bardsoftware.eclipsito.Boot;
import org.w3c.dom.Document;

public class BootImpl extends Boot {
    private static final String ATTRIBUTE_MODULES_DIRECTORY = "modules-directory";
    private static final String ATTRIBUTE_DESCRIPTOR_FILE_PATTERN = "descriptor-file-pattern";
    private static final String ATTRIBUTE_APPLICATION = "application";

    private static final ThreadGroup topThreadGroup = new ThreadGroup("TopThreadGroup") {
        public void uncaughtException(Thread t, Throwable e) {
            Boot.LOG.log(Level.WARNING, "[uncaughtException]" + e, e);
        }
    };

    private PlatformImpl myPlatform;

    public void run(final Document config, final URI home, final String[] args) {
        myPlatform = new PlatformImpl(home);
        Boot.LOG.info("Eclipsito platform is running.");
        ShutdownHook.install();

        final String application = config.getDocumentElement().getAttribute(ATTRIBUTE_APPLICATION);
        PluginDescriptor[] plugins = getPlugins(config, home);
        run(plugins, application, args);
        // start all bundles to let them initialize their services,
        // this should be done before an application is started
    }

    protected PluginDescriptor[] getPlugins(Document config, URI home) {
        String modulesdir = config.getDocumentElement().getAttribute(ATTRIBUTE_MODULES_DIRECTORY);
        String descriptorPattern = config.getDocumentElement().getAttribute(ATTRIBUTE_DESCRIPTOR_FILE_PATTERN);
        URL modulesUrl = getClass().getResource(modulesdir);
        PluginDescriptor[] plugins = ModulesDirectoryProcessor.process(modulesUrl, descriptorPattern);
        return plugins;

    }
    public void run(PluginDescriptor[] plugins, final String application, final String[] args) {
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

    }
    public void shutdown() {
        myPlatform.stop();
        Boot.LOG.info("Eclipsito platform is shut down.");
    }

    private static class ShutdownHook extends Thread {
        public ShutdownHook() {
            super(topThreadGroup, "ShutdownHook-Thread");
            setPriority(Thread.MAX_PRIORITY);
        }

        public void run() {
            Boot.getInstance().shutdown();
        }

        public static void install() {
            Runtime.getRuntime().addShutdownHook(new ShutdownHook());
        }
    }

}
