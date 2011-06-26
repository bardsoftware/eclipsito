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

    public static PluginDescriptor[] process(final URL modulesdirUri, final String descriptorPattern) {
        return processDescriptors(findModuleDescriptors(modulesdirUri, descriptorPattern));
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

    protected static URL[] findModuleDescriptors(final URL modulesdirUri, final String descriptorPattern) {
        ArrayList<URL> result = new ArrayList<URL>();
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

    private static File[] findSubdirectories(URL rootUri) {
        String path;
        try {
            path = URLDecoder.decode(rootUri.getPath(), "UTF-8");
            Boot.LOG.info("Searching for plugins in " + path);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            path = rootUri.getPath();
        }
        File[] result = new File(path).listFiles(new FileFilter() {
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
