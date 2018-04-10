package org.bardsoftware.test.eclipsito;

import java.net.URL;

import org.bardsoftware.eclipsito.Boot;
import org.bardsoftware.test.eclipsito.dependencies.Plugin10class;
import org.bardsoftware.test.eclipsito.dependencies.Plugin11class;
import org.bardsoftware.test.eclipsito.dependencies.Plugin12class;
import org.bardsoftware.test.eclipsito.dependencies.Plugin1class;
import org.bardsoftware.test.eclipsito.dependencies.Plugin2class;
import org.bardsoftware.test.eclipsito.dependencies.Plugin3class;
import org.bardsoftware.test.eclipsito.dependencies.Plugin4class;
import org.bardsoftware.test.eclipsito.dependencies.Plugin5class;
import org.bardsoftware.test.eclipsito.dependencies.Plugin6class;
import org.bardsoftware.test.eclipsito.dependencies.Plugin7class;
import org.bardsoftware.test.eclipsito.dependencies.Plugin8class;
import org.bardsoftware.test.eclipsito.dependencies.Plugin9class;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IModel;
import org.eclipse.core.runtime.Platform;

public class TestPluginDependencies extends TestsEclipsitoBase {

    public void setUp() throws Exception {
        URL configUrl = getClass().getClassLoader().getResource("platform-config.xml");
        assertNotNull("Failed to resolve path to config. Classpath problems?", configUrl);
        Boot.main(new String[] { configUrl.getPath() });
    }

    public void tearDown() throws Exception {
        Boot.getInstance().shutdown();
    }

    //  to run this test we have to comment out all platform threading!!!
    //  (we use threads for platform.start and platform.shutdown,
    //  and the latter is called by the jvm shutdown hook)
    //  see: cvs diff -r1.3 -r1.4 BootImpl.java
    public void _testDependeeStartsBeforeDependantBundle() throws Exception {
        assertTrue("plugin1 is expected to start before plugin2", Plugin1class.getStartNumber()<Plugin2class.getStartNumber());
        assertTrue("plugin4 is expected to start before plugin3", Plugin4class.getStartNumber()<Plugin3class.getStartNumber());
        assertNotNull(Platform.getBundle("plugin1"));
        assertNotNull(Platform.getBundle("plugin2"));
        assertNotNull(Platform.getBundle("plugin3"));
        assertNotNull(Platform.getBundle("plugin4"));
    }

    public void testDependencyLoops() throws Exception {
        // test simple dependency loop plugin5->plugin6->plugin5
        assertFalse(Plugin5class.wasStarted());
        assertFalse(Plugin6class.wasStarted());
        assertNull(Platform.getBundle("plugin5"));
        assertNull(Platform.getBundle("plugin6"));
        // test triangle dependency loop plugin7->plugin8->plugin9->plugin7
        assertFalse(Plugin7class.wasStarted());
        assertFalse(Plugin8class.wasStarted());
        assertFalse(Plugin9class.wasStarted());
        assertNull(Platform.getBundle("plugin7"));
        assertNull(Platform.getBundle("plugin8"));
        assertNull(Platform.getBundle("plugin9"));
    }

    public void testBrokenDependencies() throws Exception {
        // test broken dependency entry plugin10->x
        assertFalse(Plugin10class.wasStarted());
        assertNull(Platform.getBundle("plugin10"));
        // test indirect broken dependency plugin11->plugin12->x
        assertFalse(Plugin11class.wasStarted());
        assertFalse(Plugin12class.wasStarted());
        assertNull(Platform.getBundle("plugin11"));
        assertNull(Platform.getBundle("plugin12"));
    }

    public void testExtensionsAreFoundOnlyForValidExtensionPoints() throws Exception {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        // extension for non-existing point, goes quietly
        IExtension broken = registry.getExtension("plugin14.broken");
        assertNull(broken);
        IExtensionPoint[] points = registry.getExtensionPoints();
        // there are only 1 non-system extension point reacheable from the registry
        assertEquals(1 + 1, points.length);
        assertEquals(IModel.PI_RUNTIME+"."+IModel.PI_APPLICATIONS, points[0].getUniqueIdentifier());
        assertEquals("plugin13.point1", points[1].getUniqueIdentifier());
        // this point has only 1 extension in plugin14
        IExtension[] extensions = points[1].getExtensions();
        assertEquals(1, extensions.length);
        assertEquals("plugin14.good", extensions[0].getUniqueIdentifier());
        // there are 2 config elements inside
        IConfigurationElement[] elements = extensions[0].getConfigurationElements();
        assertEquals(2, elements.length);
    }

    public void testExtensionsAndExtensionPointsAreNotFoundInBrokenModules() {
        IExtensionRegistry registry = Platform.getExtensionRegistry();
        IExtensionPoint brokenPluginPoint = registry.getExtensionPoint("plugin15.point1");
        assertNull(brokenPluginPoint);
        IExtension brokenPluginExtension = registry.getExtension("plugin15.brokenPlugin");
        assertNull(brokenPluginExtension);
        IExtension brokenPluginExtensionForNotBrokenPoint = registry.getExtension("plugin15.goodPlugin");
        assertNull(brokenPluginExtensionForNotBrokenPoint);
    }
}
