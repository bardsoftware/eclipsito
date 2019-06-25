package com.bardsoftware.eclipsito.runtime;

import com.bardsoftware.eclipsito.PluginDescriptor;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;

import java.util.ArrayList;
import java.util.Arrays;

public class ExtensionRegistryImpl implements IExtensionRegistry {
    private final IExtensionPoint[] myExtensionPoints;

    public ExtensionRegistryImpl(IExtensionPoint[] points) {
        myExtensionPoints = points;
    }

    public ExtensionRegistryImpl(PluginDescriptor[] descriptors) {
        myExtensionPoints = ExtensionsProcessor.resolveExtensionPoints(descriptors);
    }

    public IConfigurationElement[] getConfigurationElementsFor(String extensionPointId) {
        ArrayList result = new ArrayList();
        IExtensionPoint point = getExtensionPoint(extensionPointId);
        if (point != null) {
            IExtension[] extensions = getExtensionPoint(extensionPointId).getExtensions();
            for (int i=0; extensions != null && i<extensions.length; i++) {
                result.addAll(Arrays.asList(extensions[i].getConfigurationElements()));
            }
        }
        return (IConfigurationElement[]) result.toArray(new IConfigurationElement[result.size()]);
    }

    public IExtension getExtension(String extensionId) {
        IExtension result = null;
        for (int i=0; myExtensionPoints != null && i<myExtensionPoints.length; i++) {
            IExtension extension = myExtensionPoints[i].getExtension(extensionId);
            if (extension != null) {
                result = extension;
                break;
            }
        }
        return result;
    }

    public IExtension getExtension(String extensionPointId, String extensionId) {
        IExtension result = null;
        for (int i=0; myExtensionPoints != null && i<myExtensionPoints.length; i++) {
            IExtension extension = myExtensionPoints[i].getExtension(extensionId);
            if (myExtensionPoints[i].getUniqueIdentifier().equals(extensionPointId) && extension != null) {
                result = extension;
                break;
            }
        }
        return result;
    }

    public IExtensionPoint[] getExtensionPoints() {
        return myExtensionPoints;
    }

    public IExtensionPoint getExtensionPoint(String extensionPointId) {
        IExtensionPoint result = null;
        for (int i=0; myExtensionPoints != null && i<myExtensionPoints.length; i++) {
            if (myExtensionPoints[i].getUniqueIdentifier().equals(extensionPointId)) {
                result = myExtensionPoints[i];
                break;
            }
        }
        return result;
    }

    // ignored methods, may be implemented in future

    public IConfigurationElement[] getConfigurationElementsFor(String namespace, String extensionPointName) {
        throw new UnsupportedOperationException();
    }

    public IConfigurationElement[] getConfigurationElementsFor(String namespace, String extensionPointName, String extensionId) {
        throw new UnsupportedOperationException();

    }

    public IExtension getExtension(String namespace, String extensionPointName, String extensionId) {
        throw new UnsupportedOperationException();

    }

    public IExtension[] getExtensions(String namespace) {
        throw new UnsupportedOperationException();
    }

    public IExtensionPoint getExtensionPoint(String namespace, String extensionPointName) {
        throw new UnsupportedOperationException();
    }

    public IExtensionPoint[] getExtensionPoints(String namespace) {
        throw new UnsupportedOperationException();
    }

    public String[] getNamespaces() {
        throw new UnsupportedOperationException();
    }

}
