package org.bardsoftware.test.eclipsito;

import java.net.URI;

import org.bardsoftware.impl.eclipsito.PluginDescriptor;
import org.w3c.dom.NodeList;

import junit.framework.TestCase;

public class TestsEclipsitoBase extends TestCase {
    
    public void allTestsSetUp() {
    }
    
    public void allTestsTearDown() {
    }

    protected PluginDescriptorMock[] createDescriptorsArrayWithIntNames(int size) {
        PluginDescriptorMock[] result = new PluginDescriptorMock[size];
        for (int i=0; i<size; i++) {
            result[i] = new PluginDescriptorMock(URI.create("")); 
            result[i].setId(String.valueOf(i));
        }
        return result;
    }

    protected PluginDescriptorMock[] createDescriptorsArrayWithIntNamesAndIntPointNames(int descriptorsSize, int pointsCount) {
        PluginDescriptorMock[] result = createDescriptorsArrayWithIntNames(descriptorsSize);
        for (int i=0; i<result.length; i++) {
            for (int j=0; j<pointsCount; j++) {
                result[i].addExtensionPointDescriptor(String.valueOf(j), "label", null);
            }
        }
        return result;
    }
    
    protected PluginDescriptorMock[] createDescriptorsArrayWithIntNamesAndIntPointNamesAndNExtensions(int descriptorsSize, int pointsCount, int extesnionsCount) {
        PluginDescriptorMock[] result = createDescriptorsArrayWithIntNamesAndIntPointNames(descriptorsSize, pointsCount);
        for (int i=0; i<result.length; i++) {
            for (int j=0; j<pointsCount; j++) {
                for (int k=0; k<extesnionsCount; k++) {
                    result[i].addExtension(null, null, result[i].getExtensionPointDescriptors()[j].getId(), null);
                }
            
            }
        }
        return result;
    }
    
    protected static class PluginDescriptorMock extends PluginDescriptor {
        public PluginDescriptorMock(URI uri) {
            super(uri);
        }
        
        public void setId(String id) {
            super.setId(id);
        }
        
        public void setClassName(String className) {
            super.setClassName(className);
        }
        
        public void addRequiredPluginId(String pluginId) {
            super.addRequiredPluginId(pluginId);
        }
        
        public void addExtensionPointDescriptor(String name, String label, String schema) {
            super.addExtensionPointDescriptor(name, label, schema);
        }
        
        public void addExtension(String id, String label, String extensionPointId, NodeList configTags) {
            super.addExtension(id, label, extensionPointId, configTags);
        }
    }
}
