package org.bardsoftware.test.eclipsito;

import java.net.URL;

import org.bardsoftware.impl.eclipsito.BundleClassLoader;
import org.bardsoftware.impl.eclipsito.BundleImpl;
import org.bardsoftware.impl.eclipsito.PluginDescriptor;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class TestBundleReloading extends TestsEclipsitoBase {
    
    public void testBundleStartsAndStopsIfModified() throws Exception {
        PluginDescriptorMock descriptor = createDescriptorsArrayWithIntNames(1)[0];
        descriptor.setClassName(OurTestPlugin.class.getName());
        BundleImplMock bundle = new BundleImplMock(descriptor);
        int timesStarted = 0;
        int timesStopped = 0;
        
        bundle.update();
        assertEquals("Bundle was started by update without being started for the first time", timesStarted, OurTestPlugin.getTimesStarted());
        assertEquals("Bundle was stopped by update without being started for the first time", timesStopped, OurTestPlugin.getTimesStopped());
        
        bundle.start();
        assertEquals("Bundle was not started", ++timesStarted, OurTestPlugin.getTimesStarted());
        assertEquals("Bundle was stopped", timesStopped, OurTestPlugin.getTimesStopped());
        
        bundle.getClassLoaderMock().setModified(false);        
        bundle.update();
        assertEquals("Bundle was started by update without being modified", timesStarted, OurTestPlugin.getTimesStarted());
        assertEquals("Bundle was stopped by update without being modified", timesStopped, OurTestPlugin.getTimesStopped());
        
        bundle.getClassLoaderMock().setModified(true);
        bundle.update();
        assertEquals("Bundle was not stopped by update being modified", ++timesStopped, OurTestPlugin.getTimesStopped());
        assertEquals("Bundle was not started by update being modified", ++timesStarted, OurTestPlugin.getTimesStarted());
        
        bundle.update();
        assertEquals("Bundle was updated twice after being modified", timesStopped, OurTestPlugin.getTimesStopped());
        assertEquals("Bundle was updated twice after being modified", timesStarted, OurTestPlugin.getTimesStarted());
        
        bundle.stop();
        assertEquals("Bundle was not stopped", ++timesStopped, OurTestPlugin.getTimesStopped());
        
        bundle.getClassLoaderMock().setModified(true);
        bundle.update();
        assertEquals("Bundle was stopped by update after being stopped", timesStopped, OurTestPlugin.getTimesStopped());
        assertEquals("Bundle was started by update after being stopped", timesStarted, OurTestPlugin.getTimesStarted());
        assertEquals(OurTestPlugin.getTimesStarted(), OurTestPlugin.getTimesStopped());
    }
    
    public static class OurTestPlugin implements BundleActivator {
        private static int numberOfTimesStarted = 0;
        private static int numberOfTimesStopped = 0;

        public void start(BundleContext context) throws Exception {
            numberOfTimesStarted++;
        }

        public void stop(BundleContext context) throws Exception {
            numberOfTimesStopped++;
        }

        public static int getTimesStarted() {
            return numberOfTimesStarted;
        }
        
        public static int getTimesStopped() {
            return numberOfTimesStopped;
        }
    }
    
    private static class BundleImplMock extends BundleImpl {
        public BundleImplMock(PluginDescriptor descriptor) {
            super(descriptor);
        }
        
        protected BundleClassLoader newClassLoader(URL[] urls, ClassLoader parent) {
            return new BundleClassLoaderMock(urls, parent);
        }
        
        protected void firstTimeStartReloaderIfReloadable(boolean reloadable) {
           // super.firstTimeStartReloaderIfReloadable(false);
        }
        
        public BundleClassLoaderMock getClassLoaderMock() {
            return (BundleClassLoaderMock) super.getClassLoader();
        }        
    }
    
    private static class BundleClassLoaderMock extends BundleClassLoader {
        private boolean isModified = false;
        
        public BundleClassLoaderMock() {
            super();
        }
        
        public BundleClassLoaderMock(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }
        
        public boolean isModified() {
            return isModified;
        }
        
        public void setModified(boolean modified) {
            isModified = modified;
        }
    }
}
