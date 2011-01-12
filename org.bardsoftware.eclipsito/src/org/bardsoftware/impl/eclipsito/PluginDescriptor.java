package org.bardsoftware.impl.eclipsito;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.InvalidParameterException;
import java.util.ArrayList;

import org.eclipse.core.runtime.IExtension;
import org.w3c.dom.NodeList;


public class PluginDescriptor {

    final URL myLocationUrl;

    private String myId;
    private String myName;
    private String myVersion;
    private String myProviderName;
    private String myClassName;

    private ArrayList/*<URL>*/ myRuntimeLibraries = new ArrayList/*<URL>*/();
    private ArrayList/*<String>*/ myRequiredPlugins = new ArrayList/*<String>*/();
    private ArrayList/*<IExtension>*/ myExtensions = new ArrayList/*<IExtension>*/();
    private ArrayList/*<ExtensionPointDescriptor>*/ myExtensionPoints = new ArrayList/*<ExtensionPointDescriptor>*/();
    
    protected PluginDescriptor(URL pluginDescriptorUrl) {
        myLocationUrl = pluginDescriptorUrl;
    }

    String getLocation() {
        return String.valueOf(myLocationUrl);
    }
    public String getId() {
        return myId;
    }
    
    protected void setId(String id) {
        if (id == null) {
            throw new InvalidParameterException("Plugin id cannot be null, please, correct "+myLocationUrl);
        }
        myId = id;
    }
    
    public String getName() {
        return myName;
    }
    
    protected void setName(String name) {
        if (name == null) {
            throw new InvalidParameterException("Plugin name cannot be null, please, correct "+myLocationUrl);
        }
        myName = name;
    }
    
    public String getVersion() {
        return myVersion;
    }
    
    protected void setVersion(String version) {
        if (version == null) {
            throw new InvalidParameterException("Plugin version cannot be null, please, correct "+myLocationUrl);
        }
        myVersion = version;
    }
    
    public String getClassName() {
        return myClassName;
    }
    
    protected void setClassName(String className) {
        myClassName = className;
    }
    
    public String getProviderName() {
        return myProviderName;
    }
    
    protected void setProviderName(String providerName) {
        myProviderName = providerName;
    }
    
    public String[] getRequiredPluginIds() {
        return (String[]) myRequiredPlugins.toArray(new String[myRequiredPlugins.size()]);
    }
    
    protected void addRequiredPluginId(String requiredPlugin) {
        if (requiredPlugin == null) {
            throw new IllegalArgumentException("Required plugin attribute cannot be null, please, correct "+myLocationUrl);
        }
        myRequiredPlugins.add(requiredPlugin);
    }
    
    public URL[] getRuntimeLibraries() {
        return (URL[]) myRuntimeLibraries.toArray(new URL[myRuntimeLibraries.size()]);
    }
    
    protected void addRuntimeLibrary(String relativePath) {
        if (relativePath == null) {
            throw new IllegalArgumentException("Runtime library name attribute cannot be null, please, correct "+myLocationUrl);
        }        
        URL url;
        try {
            url = new URL(myLocationUrl, relativePath);
            myRuntimeLibraries.add(url);
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        /*
        URI runtimeUri = myLocationUri.resolve(relativePath);
        if (!new File(runtimeUri).exists()) {
            throw new IllegalStateException("Runtime library="+relativePath+" cannot be resolved, please, check "+myLocationUri);
        }
        */
    }
    
    public ExtensionPointDescriptor[] getExtensionPointDescriptors() {
        return (ExtensionPointDescriptor[]) myExtensionPoints.toArray(new ExtensionPointDescriptor[myExtensionPoints.size()]);
    }
    
    protected void addExtensionPointDescriptor(String name, String label, String schemaReference) {
        if (name == null || label == null) {
            throw new IllegalArgumentException("Extension point id and name cannot be null, please, correct "+myLocationUrl);
        }
        myExtensionPoints.add(new ExtensionPointDescriptor(name, getId(), label, schemaReference));
    }
    
    public IExtension[] getExtensions() {
        return (IExtension[]) myExtensions.toArray(new IExtension[myExtensions.size()]);
    }
    
    protected void addExtension(String id, String label, String extensionPointId, NodeList configurationTags) {
        if (extensionPointId == null) {
            throw new IllegalArgumentException("Extension's point attribute cannot be null, please, correct "+myLocationUrl);
        }
        ExtensionImpl extension = new ExtensionImpl(id, label, extensionPointId, getId());
        extension.createConfigurationElements(configurationTags);
        myExtensions.add(extension);
    }
    
    public String toString() {
        return "plugin descriptor: id="+myId+" name="+myName+" version="+myVersion+" class="+myClassName+
               " required="+myRequiredPlugins+" runtime="+myRuntimeLibraries+" extensions="+myExtensions+
               " extension-points="+myExtensionPoints;
    }

    public static class ExtensionPointDescriptor {
        public final String myNamespace;
        public final String myRelativeName;
        public final String myLabel;
        public final String mySchemaReference;
        
        protected ExtensionPointDescriptor(String name, String namespace, String label, String schemaReference) {
            myRelativeName = name;
            myNamespace = namespace;
            myLabel = label;
            mySchemaReference = schemaReference;
        }
        
        public String getId() {
            return myNamespace+"."+myRelativeName;
        }
        
        public String toString() {
            return "extension point descriptor: id="+getId()+" label="+myLabel+" schema="+mySchemaReference;
        }
    }

}
