package com.bardsoftware.eclipsito;

import com.bardsoftware.eclipsito.runtime.ExtensionImpl;
import org.eclipse.core.runtime.IExtension;
import org.w3c.dom.NodeList;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;


public class PluginDescriptor {

    private final File myFile;
    private final String myLocationUrl;
    private String myId;
    private String myName;
    private String myVersion;
    private String myProviderName;
    private String myClassName;

    private List<URL> myRuntimeLibraries = new ArrayList<>();
    private List<String> myRequiredPlugins = new ArrayList<>();
    private List<IExtension> myExtensions = new ArrayList<>();
    private List<ExtensionPointDescriptor> myExtensionPoints = new ArrayList<>();

    protected PluginDescriptor(File pluginDescriptorFile) {
        myFile = pluginDescriptorFile;
        myLocationUrl = myFile.getAbsolutePath();
    }

    public String getLocation() {
        return myLocationUrl;
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

    void setClassName(String className) {
        myClassName = className;
    }

    public String getProviderName() {
        return myProviderName;
    }

    void setProviderName(String providerName) {
        myProviderName = providerName;
    }

    public String[] getRequiredPluginIds() {
        return myRequiredPlugins.toArray(new String[myRequiredPlugins.size()]);
    }

    void addRequiredPluginId(String requiredPlugin) {
        if (requiredPlugin == null) {
            throw new IllegalArgumentException("Required plugin attribute cannot be null, please, correct "+myLocationUrl);
        }
        myRequiredPlugins.add(requiredPlugin);
    }

    public URL[] getRuntimeLibraries() {
        return myRuntimeLibraries.toArray(new URL[0]);
    }

    void addRuntimeLibrary(String template) {
        if (template.endsWith("*")) {
            Path parent = Paths.get(template.substring(0, template.length()-1));
            try {
                File descriptorDir = myFile.getParentFile();
                File templateDir = new File(descriptorDir, parent.toString());
                if (templateDir.exists() && templateDir.isDirectory()) {
                    for (File lib : templateDir.listFiles()) {
                        myRuntimeLibraries.add(lib.toURI().toURL());
                    }
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

        } else {
            doAddRuntimeLibrary(template);
        }
    }
    private void doAddRuntimeLibrary(String relativePath) {
        if (relativePath == null) {
            throw new IllegalArgumentException("Runtime library name attribute cannot be null, please, correct "+myLocationUrl);
        }

        URL url;
        try {
            url = myFile.toURI().resolve(relativePath).toURL();
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
        return myExtensionPoints.toArray(new ExtensionPointDescriptor[myExtensionPoints.size()]);
    }

    void addExtensionPointDescriptor(String name, String label, String schemaReference) {
        if (name == null || label == null) {
            throw new IllegalArgumentException("Extension point id and name cannot be null, please, correct "+myLocationUrl);
        }
        myExtensionPoints.add(new ExtensionPointDescriptor(name, getId(), label, schemaReference));
    }

    public IExtension[] getExtensions() {
        return myExtensions.toArray(new IExtension[myExtensions.size()]);
    }

    void addExtension(String id, String label, String extensionPointId, NodeList configurationTags) {
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

    public URL getUrl() {
        try {
            return myFile.toURI().toURL();
        } catch (MalformedURLException e) {
            Launch.LOG.log(Level.SEVERE, String.format("Descriptor in %s produces malformed URL", myFile.getAbsolutePath()), e);
            throw new RuntimeException(e);
        }
    }

    public static class ExtensionPointDescriptor {
        public final String myNamespace;
        public final String myRelativeName;
        public final String myLabel;
        public final String mySchemaReference;

        public ExtensionPointDescriptor(String name, String namespace, String label, String schemaReference) {
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
