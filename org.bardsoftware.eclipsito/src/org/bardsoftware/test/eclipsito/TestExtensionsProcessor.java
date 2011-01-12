package org.bardsoftware.test.eclipsito;

import org.bardsoftware.impl.eclipsito.ExtensionsProcessor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IModel;

public class TestExtensionsProcessor extends TestsEclipsitoBase {
    
    public void testProcessOnePointAndBrokenExtensionInOneDescriptor() throws Exception {
        PluginDescriptorMock[] descriptors = createDescriptorsArrayWithIntNames(1);
        descriptors[0].addExtensionPointDescriptor("id0", "label", null);
        descriptors[0].addExtension("id1", null, "x", null); 
        IExtensionPoint[] points = ExtensionsProcessor.resolveExtensionPoints(descriptors);
        assertNotNull(points);
        assertEquals(1 + 1, points.length);
        assertEquals(IModel.PI_APPLICATIONS, points[0].getSimpleIdentifier());
        assertEquals("id0", points[1].getSimpleIdentifier());
        assertEquals(0, points[0].getExtensions().length);
        assertEquals(0, points[1].getExtensions().length);
    }
    
    public void testProcessOnePointTwoExtensionsDifferentPlugins() throws Exception {
        PluginDescriptorMock[] descriptors = createDescriptorsArrayWithIntNames(3);
        descriptors[0].addExtensionPointDescriptor("id0", "label", null);
        descriptors[1].addExtension("id1", null, descriptors[0].getExtensionPointDescriptors()[0].getId(), null); 
        descriptors[2].addExtension("id2", null, descriptors[0].getExtensionPointDescriptors()[0].getId(), null); 
        IExtensionPoint[] points = ExtensionsProcessor.resolveExtensionPoints(descriptors);
        assertNotNull(points);
        assertEquals(1 + 1, points.length);
        assertEquals(IModel.PI_RUNTIME+"."+IModel.PI_APPLICATIONS, points[0].getUniqueIdentifier());
        assertNotNull(points[1]);
        assertEquals("id0", points[1].getSimpleIdentifier());
        IExtension[] extensions = points[1].getExtensions();
        assertNotNull(extensions);
        assertEquals(2, extensions.length);
        if ("id1".equals(extensions[0].getSimpleIdentifier()) && "id2".equals(extensions[1].getSimpleIdentifier())) {
            assertTrue(true);
        } else if ("id2".equals(extensions[0].getSimpleIdentifier()) && "id1".equals(extensions[1].getSimpleIdentifier())) {
            assertTrue(true);
        } else {
            fail();
        }
    }
    
    public void testProcessDescriptorsNPointsNExtensionsEach() throws Exception {
        int numberOfDescriptors = 2;
        int numberOfPointsInEachDescirptor = 2;
        int numberOfExtensionsForEachPoint = 2;
        PluginDescriptorMock[] descriptors = createDescriptorsArrayWithIntNamesAndIntPointNamesAndNExtensions(numberOfDescriptors, numberOfPointsInEachDescirptor, numberOfExtensionsForEachPoint);
        IExtensionPoint[] points = ExtensionsProcessor.resolveExtensionPoints(descriptors);
        assertNotNull(points);
        assertEquals(numberOfDescriptors*numberOfPointsInEachDescirptor + 1, points.length);
        assertEquals(IModel.PI_RUNTIME+"."+IModel.PI_APPLICATIONS, points[0].getUniqueIdentifier());
        for (int i=1; i<points.length; i++) {
            IExtensionPoint point = points[i];
            assertNotNull(point.getExtensions());
            assertEquals(numberOfExtensionsForEachPoint, point.getExtensions().length);
        }
    }
    
    public void testProcessDescriptorsNPointsNoExtensionsEach() throws Exception {
        int numberOfDescriptors = 2;
        int numberOfPointsInEachDescirptor = 2;
        PluginDescriptorMock[] descriptors = createDescriptorsArrayWithIntNamesAndIntPointNames(numberOfDescriptors, numberOfPointsInEachDescirptor);
        IExtensionPoint[] points = ExtensionsProcessor.resolveExtensionPoints(descriptors);
        assertNotNull(points);
        assertEquals(numberOfDescriptors*numberOfPointsInEachDescirptor + 1, points.length);
        assertEquals(IModel.PI_RUNTIME+"."+IModel.PI_APPLICATIONS, points[0].getUniqueIdentifier());
        for (int i=0; i<points.length; i++) {
            IExtensionPoint point = points[i];
            assertNotNull(point.getExtensions());
            assertEquals(0, point.getExtensions().length);
        }
    }
    
    public void testProcessDescriptorsNoPointsNoExtensions() throws Exception {
        int numberOfDescriptors = 2;
        PluginDescriptorMock[] descriptors = createDescriptorsArrayWithIntNames(numberOfDescriptors);
        IExtensionPoint[] points = ExtensionsProcessor.resolveExtensionPoints(descriptors);
        assertNotNull(points);
        assertEquals(0 + 1, points.length);
        assertEquals(IModel.PI_RUNTIME+"."+IModel.PI_APPLICATIONS, points[0].getUniqueIdentifier());
    }

    public void testProcessEmptyArray() throws Exception {
        PluginDescriptorMock[] descriptors = new PluginDescriptorMock[0];
        IExtensionPoint[] points = ExtensionsProcessor.resolveExtensionPoints(descriptors);
        assertNotNull(points);
        assertEquals(0 + 1, points.length);
        assertEquals(IModel.PI_RUNTIME+"."+IModel.PI_APPLICATIONS, points[0].getUniqueIdentifier());
    }

    public void testProcessNull() throws Exception {
        IExtensionPoint[] points = ExtensionsProcessor.resolveExtensionPoints(null);
        assertNotNull(points);
        assertEquals(0 + 1, points.length);
        assertEquals(IModel.PI_RUNTIME+"."+IModel.PI_APPLICATIONS, points[0].getUniqueIdentifier());
    }
    
}
