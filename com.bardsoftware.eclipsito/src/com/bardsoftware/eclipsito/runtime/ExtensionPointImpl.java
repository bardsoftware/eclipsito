package com.bardsoftware.eclipsito.runtime;

import com.bardsoftware.eclipsito.PluginDescriptor;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;

public class ExtensionPointImpl implements IExtensionPoint {
    private final String myNamespace;
    private final String mySimpleName;
    private final String myLabel;
    private final String mySchemaReference;
    private final IExtension[] myExtensions;

    ExtensionPointImpl(PluginDescriptor.ExtensionPointDescriptor descriptor, IExtension[] extensions) {
        mySimpleName = descriptor.myRelativeName;
        myNamespace = descriptor.myNamespace;
        myLabel = descriptor.myLabel;
        mySchemaReference = descriptor.mySchemaReference;
        myExtensions = extensions;
    }

    public IExtension getExtension(String extensionId) {
        IExtension result = null;
        for (int i=0; myExtensions != null && i<myExtensions.length; i++) {
            if (myExtensions[i].getUniqueIdentifier().equals(extensionId)) {
                result = myExtensions[i];
                break;
            }
        }
        return result;
    }

    public IExtension[] getExtensions() {
        return myExtensions;
    }

    public String getUniqueIdentifier() {
        return myNamespace+"."+mySimpleName;
    }

    public String getSimpleIdentifier() {
        return mySimpleName;
    }

    public String getNamespace() {
        return myNamespace;
    }

    public String getLabel() {
        return myLabel;
    }

    public String getSchemaReference() {
        return mySchemaReference;
    }

    // this method should never be used - extension point has no nested config elements due to plugin.dtd
    public IConfigurationElement[] getConfigurationElements() {
        return new IConfigurationElement[0];
    }

    public String toString() {
        return "extension point: " + getUniqueIdentifier();
    }

}
