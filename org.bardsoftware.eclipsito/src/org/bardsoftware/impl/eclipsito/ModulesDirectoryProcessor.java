package org.bardsoftware.impl.eclipsito;

import java.io.File;
import java.io.FileFilter;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;

public class ModulesDirectoryProcessor {

    public static PluginDescriptor[] process(final URI modulesdirUri, final String descriptorPattern) {
        return processDescriptors(findModuleDescriptors(modulesdirUri, descriptorPattern));
    }
    
    protected static PluginDescriptor[] processDescriptors(URL[] moduleDescriptorUris) {
        ArrayList result = new ArrayList();
        for (int i = 0; i < moduleDescriptorUris.length; i++) {
            PluginDescriptor pluginDescriptor = DescriptorParser.parse(moduleDescriptorUris[i]);
            if (pluginDescriptor != null) {
                result.add(pluginDescriptor);
            }
        }
		return (PluginDescriptor[]) result.toArray(new PluginDescriptor[result.size()]);
    }
    
    protected static URL[] findModuleDescriptors(final URI modulesdirUri, final String descriptorPattern) {
        ArrayList result = new ArrayList();
        File[] directories = findSubdirectories(modulesdirUri);
        for (int i = 0; descriptorPattern != null && i < directories.length; i++) {
            File descriptorFile = new File(directories[i], descriptorPattern);
            if (descriptorFile.exists() && descriptorFile.isFile()) {
                try {
                    result.add(descriptorFile.toURL());
                } catch (MalformedURLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
		return (URL[]) result.toArray(new URL[result.size()]);
    }
    
    private static File[] findSubdirectories(URI rootUri) {
        File[] result = new File(rootUri.getPath()).listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.isDirectory();
            }
        });
        if (result == null) {
            result = new File[0];
        }
        Arrays.sort(result);
        return result;
    }
}
