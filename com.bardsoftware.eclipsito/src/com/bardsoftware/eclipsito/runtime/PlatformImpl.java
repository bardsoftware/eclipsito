package com.bardsoftware.eclipsito.runtime;

import com.bardsoftware.eclipsito.Launch;
import com.bardsoftware.eclipsito.PluginDescriptor;
import com.bardsoftware.eclipsito.update.Updater;
import org.bardsoftware.impl.eclipsito.JobManagerImpl;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

import java.util.logging.Level;

public class PlatformImpl extends Platform {
    private final Updater updater;
    private IExtensionRegistry myExtensionRegistry;
    private Bundle[] myBundles;

    public PlatformImpl(Updater updater) {
        setInstance(this);
        this.updater = updater;
    }

    protected void setup(PluginDescriptor[] descriptors) {
        PluginDescriptor[] resolved = new DependencyResolver(descriptors).resolveAll();
        myBundles = createBundles(resolved);
        IExtensionPoint[] points = ExtensionsProcessor.resolveExtensionPoints(resolved);
        myExtensionRegistry = new ExtensionRegistryImpl(points);
    }

    public void start() {
        for (int i = 0; myBundles != null && i < myBundles.length; i++) {
            try {
                int state = myBundles[i].getState();
                if (state == Bundle.INSTALLED || state == Bundle.RESOLVED) {
                    myBundles[i].start();
                }
            } catch (BundleException e) {
                Launch.LOG.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    public void stop() {
        for (int i = 0; myBundles != null && i < myBundles.length; i++) {
            try {
                if (myBundles[i].getState() == Bundle.ACTIVE) {
                    myBundles[i].stop();
                }
            } catch (BundleException e) {
                Launch.LOG.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    // method implements org.eclipse.core.runtime.Platform.getExtensionRegistry()
    protected IExtensionRegistry getExtensionRegistryImpl() {
        return myExtensionRegistry;
    }

    // method implements org.eclipse.core.runtime.Platform.getBundle(String symbolicName)
    protected Bundle getBundleImpl(String symbolicName) {
        Bundle result = null;
        for (int i=0; myBundles != null && i<myBundles.length; i++) {
            if (myBundles[i].getSymbolicName().equals(symbolicName)) {
                if (myBundles[i].getState() == Bundle.INSTALLED) {
                    try {
                        myBundles[i].start();
                    } catch (BundleException e) {
                        Launch.LOG.log(Level.WARNING, e.getMessage(), e);
                    }
                }
                result = myBundles[i];
                break;
            }
        }
        return result;
    }

    private Bundle[] createBundles(PluginDescriptor[] descriptors) {
        if (descriptors == null) {
            throw new IllegalArgumentException("Could not create Bundles if there are no descriptors");
        }
        Bundle[] result = new Bundle[descriptors.length];
        for (int i=0; i < descriptors.length; i++) {
            result[i] = new BundleImpl(descriptors[i]);
        }
        return result;
    }

    protected IJobManager getJobManagerImpl() {
        return JobManagerImpl.getInstance();
    }

    protected IPreferencesService getPreferencesServiceImpl() {
        IConfigurationElement[] prefServices = myExtensionRegistry.getConfigurationElementsFor(IPreferencesService.class.getName());
        if (prefServices == null || prefServices.length==0) {
            return null;
        }
        try {
            Object result = prefServices[0].createExecutableExtension("class");
            assert result instanceof IPreferencesService;
            return (IPreferencesService) result;
        } catch (CoreException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Updater geUpdaterImpl() {
        return this.updater;
    }

}
