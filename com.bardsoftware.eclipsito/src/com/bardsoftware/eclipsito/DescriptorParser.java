package com.bardsoftware.eclipsito;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.net.URL;
import java.util.logging.Level;

class DescriptorParser {
    private static DocumentBuilder ourDocumentBuilder;

    static {
        try {
            ourDocumentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException | FactoryConfigurationError e) {
            Launch.LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    static PluginDescriptor parse(File file) {
        URL pluginDescriptorUrl = null;
        PluginDescriptor result = null;
        try {
            pluginDescriptorUrl = file.toURL();
            Launch.LOG.fine(String.format("[DescriptorParser] parse(): url=%s", pluginDescriptorUrl));
            Element root = ourDocumentBuilder.parse(pluginDescriptorUrl.openStream()).getDocumentElement();
            result = constructPluginDescriptor(root, pluginDescriptorUrl);
        } catch (Exception e) {
            Launch.LOG.log(Level.SEVERE, String.format("Failed to parse descriptor=%s. This plugin will be skipped.", pluginDescriptorUrl), e);
        }
        return result;
    }

    private static PluginDescriptor constructPluginDescriptor(Element pluginElement, URL pluginDescriptorUrl) {
        PluginDescriptor result = null;
        if (pluginElement != null && PLUGIN.equals(pluginElement.getTagName())) {
            result = new PluginDescriptor(pluginDescriptorUrl);
            handlePluginAttributes(pluginElement, result);
            handleRequiresElements(pluginElement.getElementsByTagName(PLUGIN_REQUIRES), result);
            handleRuntimeElements(pluginElement.getElementsByTagName(RUNTIME), result);
            handleExtensionElements(pluginElement.getElementsByTagName(EXTENSION), result);
            handleExtensionPointElements(pluginElement.getElementsByTagName(EXTENSION_POINT), result);
        } else {
            Launch.LOG.log(Level.WARNING, String.format(
                "Invalid root tag=%s in descriptor=%s. This plugin will be skipped",
                pluginElement.getTagName(), pluginDescriptorUrl));
        }
        return result;
    }

    private static void handlePluginAttributes(Element pluginElement, PluginDescriptor pluginDescriptor) {
        String requiredId = pluginElement.getAttribute(PLUGIN_ID);
        assertAttributeIsNotEmpty(pluginDescriptor, requiredId, PLUGIN_ID);
        String requiredName = pluginElement.getAttribute(PLUGIN_NAME);
        assertAttributeIsNotEmpty(pluginDescriptor, requiredName, PLUGIN_NAME);
        String requiredVersion = pluginElement.getAttribute(PLUGIN_VERSION);
        assertAttributeIsNotEmpty(pluginDescriptor, requiredVersion, PLUGIN_VERSION);
        pluginDescriptor.setName(requiredName);
        pluginDescriptor.setId(requiredId);
        pluginDescriptor.setVersion(requiredVersion);
        pluginDescriptor.setProviderName(pluginElement.getAttribute(PLUGIN_PROVIDER));
        pluginDescriptor.setClassName(pluginElement.getAttribute(PLUGIN_CLASS));
    }

    private static void assertAttributeIsNotEmpty(PluginDescriptor descriptor, String attribute, String attrName) {
        if ("".equals(attribute.trim())) {
            throw new IllegalArgumentException("Descriptor of plugin="+descriptor.getLocation()+" is missing required attribute="+attrName);
        }
    }
    private static void handleRequiresElements(NodeList requiresElements, PluginDescriptor pluginDescriptor) {
        if (requiresElements != null && requiresElements.getLength() != 0) {
            if (requiresElements.getLength() != 1) {
                throw new IllegalArgumentException("There can be only one <requires> element in "+pluginDescriptor.myLocationUrl);
            }
            Element element = (Element) requiresElements.item(0);
            NodeList imports = element.getElementsByTagName(PLUGIN_REQUIRES_IMPORT);
            if (imports == null || imports.getLength() < 1) {
                throw new IllegalArgumentException("<requires> element must have 1+ <import> in "+pluginDescriptor.myLocationUrl);
            }
            for (int i = 0; i < imports.getLength(); i++) {
                Element imported = (Element) imports.item(i);
                String requiredPluginId = imported.getAttribute(PLUGIN_REQUIRES_PLUGIN);
                pluginDescriptor.addRequiredPluginId(requiredPluginId);
            }
        }
    }

    private static void handleRuntimeElements(NodeList runtimeElements, PluginDescriptor pluginDescriptor) {
        if (runtimeElements != null && runtimeElements.getLength() != 0) {
            if (runtimeElements.getLength() != 1) {
                throw new IllegalArgumentException("There can be only one <runtime> element in "+pluginDescriptor.myLocationUrl);
            }
            Element element = (Element) runtimeElements.item(0);
            NodeList libraries = element.getElementsByTagName(LIBRARY);
            if (libraries == null || libraries.getLength() < 1) {
                throw new IllegalArgumentException("<runtime> element must have 1+ <library> in "+pluginDescriptor.myLocationUrl);
            }
            for (int i = 0; i < libraries.getLength(); i++) {
                Element library = (Element) libraries.item(i);
                String relativePath = library.getAttribute(LIBRARY_NAME);
                pluginDescriptor.addRuntimeLibrary(relativePath);
            }
        }
    }

    private static void handleExtensionPointElements(NodeList extensionPoints, PluginDescriptor pluginDescriptor) {
        for(int i=0; extensionPoints != null && i<extensionPoints.getLength(); i++) {
            Element element = (Element) extensionPoints.item(i);
            String label = element.getAttribute(EXTENSION_POINT_NAME);
            String uniqueIdentifier = element.getAttribute(EXTENSION_POINT_ID);
            String schemaReference = element.getAttribute(EXTENSION_POINT_SCHEMA);
            pluginDescriptor.addExtensionPointDescriptor(uniqueIdentifier, label, schemaReference);
        }
    }

    private static void handleExtensionElements(NodeList extensionElements, PluginDescriptor pluginDescriptor) {
        for(int i=0; extensionElements != null && i<extensionElements.getLength(); i++) {
            Element element = (Element) extensionElements.item(i);
            String label = element.getAttribute(EXTENSION_NAME);
            String id = element.getAttribute(EXTENSION_ID);
            String pointId = element.getAttribute(EXTENSION_TARGET);
            NodeList configurationTags = element.getElementsByTagName("*");
            pluginDescriptor.addExtension(id, label, pointId, configurationTags);
        }
    }
    private static final String PLUGIN = "plugin";
    private static final String PLUGIN_ID = "id";
    private static final String PLUGIN_NAME = "name"; //$NON-NLS-1$
    private static final String PLUGIN_PROVIDER = "provider-name"; //$NON-NLS-1$
    private static final String PLUGIN_VERSION = "version"; //$NON-NLS-1$
    private static final String PLUGIN_CLASS = "class"; //$NON-NLS-1$

    private static final String PLUGIN_REQUIRES = "requires"; //$NON-NLS-1$
    private static final String PLUGIN_REQUIRES_PLUGIN = "plugin"; //$NON-NLS-1$
    private static final String PLUGIN_REQUIRES_IMPORT = "import"; //$NON-NLS-1$
    private static final String RUNTIME = "runtime"; //$NON-NLS-1$

    private static final String LIBRARY = "library"; //$NON-NLS-1$
    private static final String LIBRARY_NAME = "name"; //$NON-NLS-1$

    private static final String EXTENSION_POINT = "extension-point"; //$NON-NLS-1$
    private static final String EXTENSION_POINT_NAME = "name"; //$NON-NLS-1$
    private static final String EXTENSION_POINT_ID = "id"; //$NON-NLS-1$
    private static final String EXTENSION_POINT_SCHEMA = "schema"; //$NON-NLS-1$

    private static final String EXTENSION = "extension"; //$NON-NLS-1$
    private static final String EXTENSION_NAME = "name"; //$NON-NLS-1$
    private static final String EXTENSION_ID = "id"; //$NON-NLS-1$
    private static final String EXTENSION_TARGET = "point"; //$NON-NLS-1$



}
