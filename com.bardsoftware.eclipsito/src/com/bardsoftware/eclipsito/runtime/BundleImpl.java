package com.bardsoftware.eclipsito.runtime;

import com.bardsoftware.eclipsito.Launch;
import com.bardsoftware.eclipsito.PluginDescriptor;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceReference;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.logging.Level;

public class BundleImpl implements Bundle {
    private static final long ID = System.currentTimeMillis();
    private final PluginDescriptor myDescriptor;
    private final BundleContext myContext;
    private BundleClassLoader myClassLoader;
    private BundleActivator myPluginInstance;
    private boolean isReloaderStarted = false;
    private int myState;

    protected BundleImpl(PluginDescriptor descriptor) {
        myDescriptor = descriptor;
        myContext = null; // sorry
        myState = INSTALLED;
    }

    public void start() throws BundleException {
        if (getState() == INSTALLED) {
            myState = STARTING;
            myClassLoader = initializeClassLoader(myDescriptor);
            //firstTimeStartReloaderIfReloadable(true); // initialize boolean parameter from config file
            myState = RESOLVED;
        }
        if (getState() == RESOLVED) {
            myState = STARTING;
            if (myDescriptor.getClassName() != null && myDescriptor.getClassName().length() > 0) {
                try {
                    if (myPluginInstance == null) {
                        Class clazz = loadClass(myDescriptor.getClassName());
                        myPluginInstance = (BundleActivator) clazz.newInstance();
                    }
                    myPluginInstance.start(myContext);
                    myState = ACTIVE;
                } catch (Exception e) {
                    throw new BundleException(e.getMessage());
                }
            } else {
                // no class running to initializate plugin is needed, just change the state
                myState = ACTIVE;
            }
        } else {
            throw new IllegalStateException("Cannot start bundle from state "+getState());
        }
    }

    public void stop() throws BundleException {
        if (getState() == ACTIVE) {
            myState = STOPPING;
            if (myPluginInstance != null) {
                try {
                    myPluginInstance.stop(myContext); // sorry for null context, I got no time for it now...
                    myState = RESOLVED;
                } catch (Exception e) {
                    throw new BundleException(e.getMessage());
                }
            } else {
                // no class running to finalize plugin is needed, just change the state
                myState = RESOLVED;
            }
        } else {
            throw new IllegalStateException("Cannot stop bundle from state "+getState());
        }
    }

    public void uninstall() throws BundleException {
        myState = UNINSTALLED;
    }

    public int getState() {
        return myState;
    }

    // bundle metadata manipulators

    public long getBundleId() {
        return ID;
    }

    public String getSymbolicName() {
        return myDescriptor.getId();
    }

    public String getLocation() {
        return myDescriptor.myLocationUrl.getPath();
    }

    // class loading stuff

    public Class loadClass(String name) throws ClassNotFoundException {
        BundleClassLoader loader = getClassLoader();
        return loader == null ? null : loader.loadClass(name);
    }

    public URL getResource(String name) {
        BundleClassLoader loader = getClassLoader();
        return loader == null ? null : loader.getResource(name);
    }

    public void update() throws BundleException {
        throw new UnsupportedOperationException();
//        if (getState() == ACTIVE && getClassLoader() != null && getClassLoader().isModified()) {
//            stop();
//            if (getState() == RESOLVED || getState() == INSTALLED) {
//                myPluginInstance = null;
//                myClassLoader = null;
//                myState = INSTALLED;
//                start();
//            }
//            // in other states update is not appropriate
//        }
    }

    public URL getEntry(String name) {
        URL result = null;
        try {
            result = new URL(myDescriptor.myLocationUrl, name);
        } catch (MalformedURLException e) {
            Launch.LOG.log(Level.WARNING, e.getMessage(), e);
        }
        return result;
    }

    // ignored methods, may be implemented in future

    public void update(InputStream in) throws BundleException {
        throw new UnsupportedOperationException();
    }

    public boolean hasPermission(Object permission) {
        throw new UnsupportedOperationException();
    }

    public Enumeration getEntryPaths(String path) {
        throw new UnsupportedOperationException();
    }

    public Dictionary getHeaders() {
        throw new UnsupportedOperationException();
    }

    public Dictionary getHeaders(String localeString) {
        throw new UnsupportedOperationException();
    }

    // should adapt extensions and extension points for ServiceReference?

    public ServiceReference[] getRegisteredServices() {
        throw new UnsupportedOperationException();
    }

    public ServiceReference[] getServicesInUse() {
        throw new UnsupportedOperationException();
    }

    //

    protected BundleClassLoader getClassLoader() {
        return myClassLoader;
    }

    private BundleClassLoader initializeClassLoader(PluginDescriptor descriptor) {
        BundleClassLoader result = newClassLoader(descriptor.getRuntimeLibraries(),
                this.getClass().getClassLoader());
        String[] dependencies = descriptor.getRequiredPluginIds();
        for (int i=0; dependencies != null && i<dependencies.length; i++) {
            Bundle dependencyBundle = Platform.getBundle(dependencies[i]); // this will start bundle if needed
            if (dependencyBundle != null) {
                result.addParent(dependencyBundle);
            } else {
                Launch.LOG.log(Level.WARNING, "There is wrong dependency for "+descriptor.myLocationUrl);
            }
        }
        return result;
    }

//    private URL[] convertUriToUrl(URI[] uris) {
//        if (uris == null) throw new IllegalArgumentException("Don't pass me null");
//        ArrayList result = new ArrayList(uris.length);
//        for (int i=0; i<uris.length; i++) {
//            try {
//                result.add(uris[i].toURL());
//            } catch (MalformedURLException e) {
//                Launch.LOG.log(Level.WARNING, "There is wrong runtime entry for "+myDescriptor.myLocationUri);
//            }
//        }
//        return (URL[]) result.toArray(new URL[result.size()]);
//    }

    /*
    protected void firstTimeStartReloaderIfReloadable(boolean reloadable) {
        if (isReloaderStarted == false && reloadable) {
            new InternalReloader(this).threadStart();
            isReloaderStarted = true;
        }
    }
    */
    protected BundleClassLoader newClassLoader(URL[] urls, ClassLoader parent) {
        return new BundleClassLoader(urls, parent);
    }


}
