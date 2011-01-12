package org.bardsoftware.test.eclipsito;

import java.util.Arrays;
import java.util.List;

import org.bardsoftware.impl.eclipsito.DependencyResolver;
import org.bardsoftware.impl.eclipsito.PluginDescriptor;

public class TestDependencyResolver extends TestsEclipsitoBase {

    public void testResolveDependencyCyclesSimpleLoop() {
        PluginDescriptorMock[] descriptors = createDescriptorsArrayWithIntNames(2);
        descriptors[0].addRequiredPluginId("1");
        descriptors[1].addRequiredPluginId("0");
        PluginDescriptor[] result = new DependencyResolver(descriptors).resolveAll();
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    public void testResolveDependencyCyclesTriangleLoop() {
        PluginDescriptorMock[] descriptors = createDescriptorsArrayWithIntNames(3);
        descriptors[0].addRequiredPluginId("1");
        descriptors[1].addRequiredPluginId("2");
        descriptors[2].addRequiredPluginId("0");
        PluginDescriptor[] result = new DependencyResolver(descriptors).resolveAll();
        assertNotNull(result);
        assertEquals(0, result.length);
    }
    
    public void testResolveDependencyCyclesMissingDependency() {
        PluginDescriptorMock[] descriptors = createDescriptorsArrayWithIntNames(1);
        descriptors[0].addRequiredPluginId("x");
        PluginDescriptor[] result = new DependencyResolver(descriptors).resolveAll();
        assertNotNull(result);
        assertEquals(0, result.length);
    }
    
    public void testResolveDependencyCyclesMissingDependencies() {
        PluginDescriptorMock[] descriptors = createDescriptorsArrayWithIntNames(2);
        descriptors[0].addRequiredPluginId("1");
        descriptors[1].addRequiredPluginId("x");
        PluginDescriptor[] result = new DependencyResolver(descriptors).resolveAll();
        assertNotNull(result);
        assertEquals(0, result.length);
    }
    
    public void testResolveDependencyCyclesOneSingleElement() {
        PluginDescriptorMock[] descriptors = createDescriptorsArrayWithIntNames(1);
        PluginDescriptor[] result = new DependencyResolver(descriptors).resolveAll();
        assertNotNull(result);
        assertEquals(1, result.length);
        assertEquals(descriptors[0], result[0]);
    }
    
    public void testResolveDependencyCyclesNSingleElements() {
        int elements = 3;
        PluginDescriptor[] descriptors = createDescriptorsArrayWithIntNames(elements);
        PluginDescriptor[] result = new DependencyResolver(descriptors).resolveAll();
        assertEquals(elements, result.length);
    }
    
    public void testResolveDependencyCyclesEmptyArray() {
        PluginDescriptor[] descriptors = createDescriptorsArrayWithIntNames(0);
        PluginDescriptor[] result = new DependencyResolver(descriptors).resolveAll();
        assertNotNull(result);
        assertEquals(0, result.length);
    }
    
    public void testResolveDependenciesNull() {
        try {
            new DependencyResolver(null);
            fail();
        } catch (IllegalArgumentException e) {
            assertTrue(true);
        }
    }
    
    public void testResolveDependenciesLinearForward() {
        PluginDescriptorMock[] descriptors = createDescriptorsArrayWithIntNames(3);
        descriptors[0].addRequiredPluginId("1");
        descriptors[1].addRequiredPluginId("2");
        List result = Arrays.asList(new DependencyResolver(descriptors).resolveAll());
        assertEquals(3, result.size());
        if (result.contains(descriptors[0]) && result.contains(descriptors[1]) && result.contains(descriptors[2]) ) {
            assertTrue(true);
        } else {
            fail();
        }
    }
    
    public void testResolveDependenciesLinearBackOrder() {
        PluginDescriptorMock[] descriptors = createDescriptorsArrayWithIntNames(3);
        descriptors[1].addRequiredPluginId("0");
        descriptors[2].addRequiredPluginId("1");
        List result = Arrays.asList(new DependencyResolver(descriptors).resolveAll());
        assertEquals(3, result.size());
        if (result.contains(descriptors[0]) && result.contains(descriptors[1]) && result.contains(descriptors[2]) ) {
            assertTrue(true);
        } else {
            fail();
        }
    }
    
    public void testResolveDependencyCyclesSomeRandomLargerLoopStructure() {
        PluginDescriptorMock[] descriptors = createDescriptorsArrayWithIntNames(8);
        descriptors[1].addRequiredPluginId("0");
        descriptors[2].addRequiredPluginId("4");
        descriptors[4].addRequiredPluginId("2");
        descriptors[3].addRequiredPluginId("1");
        descriptors[3].addRequiredPluginId("x");
        descriptors[5].addRequiredPluginId("1");
        descriptors[5].addRequiredPluginId("0");
        descriptors[5].addRequiredPluginId("6");
        descriptors[6].addRequiredPluginId("7");
        descriptors[7].addRequiredPluginId("5");
        descriptors[7].addRequiredPluginId("3");
        descriptors[7].addRequiredPluginId("2");
        List result = Arrays.asList(new DependencyResolver(descriptors).resolveAll());
        assertEquals(2, result.size());
        if (result.contains(descriptors[0]) && result.contains(descriptors[1])) {
            assertTrue(true);
        } else {
            fail();
        }
    }
    
}
