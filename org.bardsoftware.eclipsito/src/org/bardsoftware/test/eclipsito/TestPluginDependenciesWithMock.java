package org.bardsoftware.test.eclipsito;

import org.bardsoftware.impl.eclipsito.PlatformImpl;
import org.bardsoftware.impl.eclipsito.PluginDescriptor;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class TestPluginDependenciesWithMock extends TestsEclipsitoBase {

    public void testDependeeStartsBeforeDependantBundle() throws Exception {
        PlatformImplMock platform = new PlatformImplMock();
        PluginDescriptorMock[] descriptors = createDescriptorsArrayWithIntNames(4);
        descriptors[0].setClassName(OurTestPlugin0.class.getName());
        descriptors[1].setClassName(OurTestPlugin1.class.getName());
        descriptors[2].setClassName(OurTestPlugin2.class.getName());
        descriptors[3].setClassName(OurTestPlugin3.class.getName());
        descriptors[1].addRequiredPluginId(descriptors[0].getId());
        descriptors[2].addRequiredPluginId(descriptors[3].getId());
        platform.setup(descriptors);
        platform.start(); // this will start all bundles in, hopefully, correct order
        assertTrue("plugin 0 is expected to start before plugin 1", OurTestPlugin0.getStartNumber()<OurTestPlugin1.getStartNumber());
        assertTrue("plugin 3 is expected to start before plugin 2", OurTestPlugin3.getStartNumber()<OurTestPlugin2.getStartNumber());
        assertNotNull(Platform.getBundle(descriptors[0].getId()));
        assertNotNull(Platform.getBundle(descriptors[1].getId()));
        assertNotNull(Platform.getBundle(descriptors[2].getId()));
        assertNotNull(Platform.getBundle(descriptors[3].getId()));
        platform.stop();
    }

    private static class PlatformImplMock extends PlatformImpl {
        public PlatformImplMock() {
            super();
        }

        public void setup(PluginDescriptor[] descriptors) {
            super.setup(descriptors);
        }
    }

    public abstract static class OurTestPluginBase {
        protected static int ourStartedPluginCounter = 0;

        public void stop(BundleContext context) throws Exception {
        }
    }

    public static class OurTestPlugin0 extends OurTestPluginBase implements BundleActivator {
        private static int ourStartNumber = 0;

        public void start(BundleContext context) throws Exception {
            ourStartNumber = ++ourStartedPluginCounter;
        }

        public static int getStartNumber() {
            return ourStartNumber;
        }
    }

    public static class OurTestPlugin1 extends OurTestPluginBase implements BundleActivator {
        private static int ourStartNumber = 0;

        public void start(BundleContext context) throws Exception {
            ourStartNumber = ++ourStartedPluginCounter;
        }

        public static int getStartNumber() {
            return ourStartNumber;
        }
    }

    public static class OurTestPlugin2 extends OurTestPluginBase implements BundleActivator {
        private static int ourStartNumber = 0;

        public void start(BundleContext context) throws Exception {
            ourStartNumber = ++ourStartedPluginCounter;
        }

        public static int getStartNumber() {
            return ourStartNumber;
        }
    }

    public static class OurTestPlugin3 extends OurTestPluginBase implements BundleActivator {
        private static int ourStartNumber = 0;

        public void start(BundleContext context) throws Exception {
            ourStartNumber = ++ourStartedPluginCounter;
        }

        public static int getStartNumber() {
            return ourStartNumber;
        }
    }
}
