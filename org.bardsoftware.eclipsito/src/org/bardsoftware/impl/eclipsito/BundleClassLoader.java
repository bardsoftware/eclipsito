package org.bardsoftware.impl.eclipsito;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;

import org.osgi.framework.Bundle;


public class BundleClassLoader extends URLClassLoader {
    static final String PACKED_JAR = "org.bardsoftware.eclipsito.packedjar";
    private ArrayList/*<Bundle>*/ myParentBundles = new ArrayList/*<Bundle>*/();
    
    public BundleClassLoader() {
        super(new URL[0]);
    }
    
    public BundleClassLoader(URL[] defaultUrls, ClassLoader mainParent) {
        super(new URL[0], mainParent);
        //System.err.println("[BundleClassLoader] BundleClassLoader(): urls="+Arrays.asList(defaultUrls));
        for (int i=0; i<defaultUrls.length; i++) {
            URL next = defaultUrls[i];
            if (next.getProtocol().equals(PACKED_JAR) && next.getFile().endsWith(".jar")) {
                super.addURL(unpackJar(next));
            }
            else {
                super.addURL(next);
            }
        }
//        for (int i=0; defaultUrls != null && i<defaultUrls.length; i++) {
//            exploreTimestampsIn(new File(defaultUrls[i].getFile()));
//        }
    }

    protected URL unpackJar(URL packedJarUrl) {
        assert packedJarUrl!=null && PACKED_JAR.equals(packedJarUrl.getProtocol());
        String className = packedJarUrl.getHost();
        final Class clazz;
        InputStream in = null;
        try {
            clazz = Class.forName(className);
            String path = packedJarUrl.getPath();
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
            in = clazz.getClassLoader().getResourceAsStream(path);
            if (in==null) {
                throw new RuntimeException("Failed to access input stream addressed by path="+packedJarUrl.getPath()+" of URL="+packedJarUrl);
            }
            File file = File.createTempFile( "org.bardsoftware.eclipsito.WebStartBootImpl-", ".jar");
            if (file==null) {
                throw new RuntimeException("Null file created");
            }
            saveStream(in,file);
            
            //FileUtils.saveStreamToFile(in, file);
            return file.toURL();
            
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to find class mentioned in packed JAR URL="+packedJarUrl, e);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to extract jar addressed by url="+packedJarUrl, e);
        }
        finally {
            if (in!=null) {
                try {
                    in.close();
                } catch (IOException e) {
                    throw new RuntimeException("Failed to extract jar addressed by url="+packedJarUrl, e);
                }
            }
        }
        
    }
    
    private void saveStream(InputStream inputStream, File outFile) throws IOException {
        final FileOutputStream out = new FileOutputStream(outFile);
        try {
            final byte[] buf = new byte[4096];
            while (true) {
                final int bytes_read = inputStream.read(buf);
                if (bytes_read == -1) {
                    break;
                }
                out.write(buf, 0, bytes_read);
            }
        } finally {
            out.close();
        }
    }
    
    public void addURL(URL url) {
        super.addURL(url);
    }
    
    public void addParent(Bundle dependencyBundle) {
        myParentBundles.add(dependencyBundle);
    }


    protected Bundle[] parents() {
        return (Bundle[]) myParentBundles.toArray(new BundleClassLoader[myParentBundles.size()]);
    }
    
    // I expect findClass to be called by loadClass to search for non-system classes
    public Class findClass(String name) throws ClassNotFoundException {
        Class result = null;
        // first let's delegate this to our parent bundles to force them searching their own URL arrays
        for (int i=0; i<myParentBundles.size(); i++) {
            Bundle nextParent = (Bundle) myParentBundles.get(i);
            try {
                result = nextParent.loadClass(name);
            } catch (ClassNotFoundException e) {
                // ignore, if parent could not find class we search our own URLs
            }
            if (result != null) {
                break;
            }
        }
        // if parents were unable to find requested class, search our own URLs array 
        if (result == null) {
            // we use default URLClassLoader mechanism here to load.
            // if it is unable to find class, it will throw ClassNotFoundException
            //System.err.println("[BundleClassLoader] findClass(): class="+name+"\nurls="+Arrays.asList(super.getURLs()));
            result = super.findClass(name);
        }
        return result;
    }

}
