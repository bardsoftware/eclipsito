package org.bardsoftware.impl.eclipsito;

import java.io.File;
import java.io.FileFilter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;

import org.bardsoftware.eclipsito.Boot;

public class ModulesDirectoryProcessor {

    public static PluginDescriptor[] process(File pluginDir, String descriptorPattern) {
        return processDescriptors(findModuleDescriptors(pluginDir, descriptorPattern));
    }

    protected static PluginDescriptor[] processDescriptors(URL[] moduleDescriptorUris) {
        ArrayList<PluginDescriptor> result = new ArrayList<PluginDescriptor>();
        for (int i = 0; i < moduleDescriptorUris.length; i++) {
            PluginDescriptor pluginDescriptor = DescriptorParser.parse(moduleDescriptorUris[i]);
            if (pluginDescriptor != null) {
                result.add(pluginDescriptor);
            }
        }
        return (PluginDescriptor[]) result.toArray(new PluginDescriptor[result.size()]);
    }

    protected static URL[] findModuleDescriptors(File pluginDir, final String descriptorPattern) {
        ArrayList<URL> result = new ArrayList<URL>();
        File[] directories = findSubdirectories(pluginDir);
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

    private static File[] findSubdirectories(File pluginDir) {
        File[] result = pluginDir.listFiles(new FileFilter() {
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
