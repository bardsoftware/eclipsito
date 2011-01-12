package org.bardsoftware.eclipsito;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

public abstract class Boot {

    public abstract void run(Document config, URI home, String[] args);
    public abstract void shutdown();
    
    public static final Logger LOG = Logger.getLogger(Boot.class.getName());
    
    // properties
    private static final String IMPLEMENTATION_CLASSNAME = "org.bardsoftware.modules.regxp.platform-implementation.classname";

    // config attributes
    private static final String ATTRIBUTE_PLATFORM_CLASSNAME = "platform-classname";
    private static final String ATTRIBUTE_LOGGING_LEVEL = "logging-level";

    private static Boot ourInstance;
    
    public static void main(String args[]) {
        try {
            String configName = args.length>0 ? args[0] : "eclipsito-config.xml";
            URL configResource = Boot.class.getClassLoader().getResource(configName);
            if (configResource==null) {
                throw new RuntimeException("Eclipsito configuration file="+configName+" has not been found!");
            }
	        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(configResource.openStream());
	        LOG.setLevel(Level.parse(doc.getDocumentElement().getAttribute(ATTRIBUTE_LOGGING_LEVEL)));
			String classname = doc.getDocumentElement().getAttribute(ATTRIBUTE_PLATFORM_CLASSNAME);

            URI home = new URI(configResource.toString());
            String[] realArgs;
            if (args.length>1) {
                realArgs = new String[args.length-1];
                System.arraycopy(args, 1, realArgs, 0, realArgs.length);
            }
            else {
                realArgs = new String[0];
            }
    	    getInstance(classname).run(doc, home, realArgs);
        } catch(Exception e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public static Boot getInstance() {
        return getInstance(null);
    }
    
    public static Boot getInstance(String classname) {
        if (ourInstance == null) {
            try {                
                String implClassname = classname==null ? System.getProperty(IMPLEMENTATION_CLASSNAME) : classname;
                if (implClassname == null || implClassname.length() == 0) {
                    LOG.severe("[RegXP platform] Platform implementation is not specified. Please set system property '" +
                            IMPLEMENTATION_CLASSNAME + "'");
                    System.exit(0);
                }
                Class implClass = Class.forName(implClassname);
                ourInstance = (Boot)implClass.newInstance();
            } catch (ClassNotFoundException e) {
                LOG.log(Level.SEVERE, e.getMessage(), e);
                System.exit(0);
            } catch (InstantiationException e) {
                LOG.log(Level.SEVERE, e.getMessage(), e);
                System.exit(0);
            } catch (IllegalAccessException e) {
                LOG.log(Level.SEVERE, e.getMessage(), e);
                System.exit(0);
            }
        }
        return ourInstance;
    }

    protected static void setInstance(Boot instance) {
        ourInstance = instance;
    }
    
}