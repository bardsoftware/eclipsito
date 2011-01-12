package org.bardsoftware.impl.eclipsito;

import java.util.ArrayList;

import org.bardsoftware.impl.eclipsito.PluginDescriptor.ExtensionPointDescriptor;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;

public class ExtensionsProcessor {

    public static IExtensionPoint[] resolveExtensionPoints(PluginDescriptor[] pluginDescriptors) {
        ArrayList allPoints = new ArrayList();
        // first declare platform extension points (for now only org.eclipse.core.runtime.applications)
        allPoints.add(new ExtensionPointImpl(ApplicationLauncher.ourRuntimeApplicationExtensionPoint, 
                collectPointExtensions(ApplicationLauncher.ourAppsPointId, pluginDescriptors)));
        // then explore plugins-defined extension points
        for (int i=0; pluginDescriptors != null && i<pluginDescriptors.length; i++) {
            ExtensionPointDescriptor[] extensionPointDescriptors = pluginDescriptors[i].getExtensionPointDescriptors();
            for (int j=0; extensionPointDescriptors != null && j<extensionPointDescriptors.length; j++) {
                IExtension[] extensions = collectPointExtensions(extensionPointDescriptors[j].getId(), pluginDescriptors);
                ExtensionPointImpl point = new ExtensionPointImpl(extensionPointDescriptors[j], extensions);
                allPoints.add(point);
            }
        }
        return (IExtensionPoint[]) allPoints.toArray(new IExtensionPoint[0]);
    }

    protected static IExtension[] collectPointExtensions(String pointId, PluginDescriptor[] pluginDescriptors) {
        if (pointId == null) throw new IllegalArgumentException("Don't pass me null extension point id!");
        ArrayList result = new ArrayList();
        for (int i=0; pluginDescriptors != null && i<pluginDescriptors.length; i++) {
            IExtension[] extensions = pluginDescriptors[i].getExtensions();
            for (int j=0; extensions != null && j<extensions.length; j++) {
                if (pointId.equals(extensions[j].getExtensionPointUniqueIdentifier())) {
                    result.add(extensions[j]);
                }
            }
        }
        return (IExtension[]) result.toArray(new IExtension[result.size()]);
    }

}
