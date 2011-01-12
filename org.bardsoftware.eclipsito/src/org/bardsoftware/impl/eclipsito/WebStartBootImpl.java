package org.bardsoftware.impl.eclipsito;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.security.AllPermission;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Policy;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class WebStartBootImpl extends BootImpl implements URLStreamHandlerFactory {
    public WebStartBootImpl() {
        URL.setURLStreamHandlerFactory(this);
        final Policy defaultPolicy = Policy.getPolicy();
        Policy.setPolicy(new Policy() {
            public PermissionCollection getPermissions(CodeSource codesource) {
                PermissionCollection defaultPermissions = defaultPolicy.getPermissions(codesource);
                defaultPermissions.add(new AllPermission());
                return defaultPermissions;
            }

            public void refresh() {
                defaultPolicy.refresh();
            }
        });
    }
    
    protected PluginDescriptor[] getPlugins(Document config, URI home) {
        //return super.getPlugins(config, home);
        List descriptors = new ArrayList();
        NodeList pluginTags = config.getElementsByTagName("plugin");
        for (int i=0; i<pluginTags.getLength(); i++) {
            Element nextPluginTag = (Element) pluginTags.item(i);
            String pluginDescriptorPath = nextPluginTag.getAttribute("descriptor-path");
            URL pluginDescriptorUrl = getClass().getClassLoader().getResource(pluginDescriptorPath);
            if (pluginDescriptorUrl==null) {
                throw new RuntimeException("[WebStartBootImpl] getPlugins(): failed to locate descriptor by path="+pluginDescriptorPath);
            }
            PluginDescriptor nextDescriptor = DescriptorParser.parse(pluginDescriptorUrl);
            descriptors.add(nextDescriptor);
        }
        return (PluginDescriptor[]) descriptors.toArray(new PluginDescriptor[0]);
    }

    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (BundleClassLoader.PACKED_JAR.equals(protocol)) {
            return new URLStreamHandler() {
                protected URLConnection openConnection(URL u) throws IOException {
                    throw new UnsupportedOperationException();
                }
            };
        }
        return null;
    }
}
