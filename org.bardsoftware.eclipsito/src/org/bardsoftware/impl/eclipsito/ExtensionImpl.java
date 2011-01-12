package org.bardsoftware.impl.eclipsito;

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

public class ExtensionImpl implements IExtension {
    private final String myLabel;
    private final String myId;
    private final String myExtensionPointId;
    private final String myPluginId;
    private IConfigurationElement[] myConfigurationElements;

    public ExtensionImpl(String id, String label, String extensionPointId, String pluginId) {
        myId = id;
        myLabel = label;
        myExtensionPointId = extensionPointId;
        myPluginId = pluginId;
    }

    public IConfigurationElement[] getConfigurationElements() {
        return myConfigurationElements;
    }

    public String getExtensionPointUniqueIdentifier() {
        return myExtensionPointId;
    }

    public String getLabel() {
        return myLabel;
    }

    public String getUniqueIdentifier() {
        return (myId == null || myId.length() == 0) ? myPluginId : myPluginId+"."+myId;
    }
    
    public String getSimpleIdentifier() {
        return myId;
    }

    public String getNamespace() {
        return myPluginId;
    }

    public String toString() {
        return "extension: " + getUniqueIdentifier() + " point=" + myExtensionPointId;
    }

    void createConfigurationElements(NodeList configTags) {
        if (configTags != null) {
            myConfigurationElements = handleConfigurationElements(configTags, null);
        } else {
            myConfigurationElements = new IConfigurationElement[0];
        }
    }

    private IConfigurationElement[] handleConfigurationElements(NodeList configurationElementNodes, IConfigurationElement parentElement) {
        ArrayList result = new ArrayList();
        for (int i=0;  i<configurationElementNodes.getLength(); i++) {
            Element element = (Element) configurationElementNodes.item(i);
            ConfigurationElementImpl configurationElement = new ConfigurationElementImpl(element.getNodeName(), element.getNodeValue());
            NamedNodeMap attrs = element.getAttributes();
            for (int j=0; attrs != null && j<attrs.getLength(); j++) {
                configurationElement.addAttribute(attrs.item(j).getNodeName(), attrs.item(j).getNodeValue());
            }
            configurationElement.setParent(parentElement==null ? (Object)this : parentElement);
            configurationElement.setChildren(handleConfigurationElements(element.getElementsByTagName("*"), configurationElement));
            result.add(configurationElement);
        }
        return (IConfigurationElement[]) result.toArray(new IConfigurationElement[result.size()]);
    }
     
    public class ConfigurationElementImpl implements IConfigurationElement {
        private HashMap myAttributes = new HashMap(5); // (String attrName, String attrValue)
        private IConfigurationElement[] myChildren;
        private Object myParent;
        private final String myName;
        private final String myValue;
        
        ConfigurationElementImpl(String name, String value) {
            myName = name;
            myValue = value;
        }

        public Object createExecutableExtension(String propertyName) throws CoreException {
            Object result = null;
            Bundle bundle = Platform.getBundle(ExtensionImpl.this.myPluginId);
            if (bundle != null) {
                try {
                    Class clazz = bundle.loadClass(getAttribute(propertyName));
                    result = clazz == null ? null : clazz.newInstance();
                } catch (ClassNotFoundException e) {
                    throw new CoreException(e);
                } catch (InstantiationException e) {
                    throw new CoreException(e);
                } catch (IllegalAccessException e) {
                    throw new CoreException(e);
                }
            }
            return result;
        }

        public String getAttribute(String name) {
            return (String) myAttributes.get(name);
        }

        public String[] getAttributeNames() {
            return (String[]) myAttributes.values().toArray(new String[0]);
        }

        public IConfigurationElement[] getChildren() {
            return myChildren == null ? new IConfigurationElement[0] : myChildren;
        }

        public IConfigurationElement[] getChildren(String name) {
            ArrayList result = new ArrayList();
            for (int i=0; i<myChildren.length; i++) {
                if (myChildren[i].getName().equals(name)) {
                    result.add(myChildren[i]);
                }
            }
            return (IConfigurationElement[]) result.toArray(new IConfigurationElement[result.size()]);
        }

        public IExtension getDeclaringExtension() {
            return ExtensionImpl.this;
        }

        public Object getParent() {
            return myParent;
        }

        public String getName() {
            return myName;
        }

        public String getValue() {
            return myValue;
        }
        
        public String toString() {
            return "configuration element: name=" + myName + " value=" + myValue + " attrs=" + myAttributes; 
        }
        
        void addAttribute(String attributeName, String attributeValue) {
            myAttributes.put(attributeName, attributeValue);
        }
        
        void setParent(Object parentElement) {
            myParent = parentElement;
        }
        
        void setChildren(IConfigurationElement[] children) {
            myChildren = children;
        }
        
    }
    
}
