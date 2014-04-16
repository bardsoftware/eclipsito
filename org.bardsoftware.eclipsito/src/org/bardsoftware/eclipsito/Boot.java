package org.bardsoftware.eclipsito;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.bardsoftware.impl.eclipsito.BootImpl;
import org.w3c.dom.Document;

public abstract class Boot {

    public abstract void run(String application, String modulesDir, String descriptorPattern, List<String> args);
    public abstract void shutdown();
    
    public static final Logger LOG = Logger.getLogger(Boot.class.getName());
    
    // properties
    private static final String IMPLEMENTATION_CLASSNAME = "org.bardsoftware.modules.regxp.platform-implementation.classname";

    // config attributes
    private static final String ATTRIBUTE_PLATFORM_CLASSNAME = "platform-classname";
    private static final String ATTRIBUTE_LOGGING_LEVEL = "logging-level";
    private static final String ATTRIBUTE_MODULES_DIRECTORY = "modules-directory";
    private static final String ATTRIBUTE_DESCRIPTOR_FILE_PATTERN = "descriptor-file-pattern";
    private static final String ATTRIBUTE_APPLICATION = "application";

    private static Boot ourInstance;

    private static void parseArgs(Map<String, String> options, List<String> args) {
      int firstArgPos = -1;
      for (int i = 0; i < args.size(); i++) {
        String arg = args.get(i);
        if (!arg.startsWith("-")) {
          firstArgPos = i;
          break;
        }
        assert i < args.size() - 1;
        options.put(arg, args.get(i + 1));
        i++;
        continue;
      }
      if (firstArgPos == -1) {
        firstArgPos = args.size();
      }
      args.subList(0, firstArgPos).clear();
    }
    
    public static void main(String args[]) {
        try {
          LOG.setLevel(Level.ALL);
          Map<String, String> options = new HashMap<String, String>();
          List<String> argList = new ArrayList<String>(Arrays.asList(args));
          parseArgs(options, argList);
          
          String application;
          String modulesDir;
          String descriptorPattern;
          String implementationClass;
          if (options.isEmpty()) {
            String configName = argList.isEmpty() ? "eclipsito-config.xml" : argList.remove(0);
            URL configResource = Boot.class.getClassLoader().getResource(configName);
            if (configResource==null) {
                throw new RuntimeException("Eclipsito configuration file="+configName+" has not been found!");
            }
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(configResource.openStream());
            implementationClass = doc.getDocumentElement().getAttribute(ATTRIBUTE_PLATFORM_CLASSNAME);
            application = doc.getDocumentElement().getAttribute(ATTRIBUTE_APPLICATION);
            modulesDir = doc.getDocumentElement().getAttribute(ATTRIBUTE_MODULES_DIRECTORY);
            descriptorPattern = doc.getDocumentElement().getAttribute(ATTRIBUTE_DESCRIPTOR_FILE_PATTERN);
          } else {
            application = options.get("-app");
            modulesDir = options.get("-plugins");
            descriptorPattern = options.get("-include");
            if (descriptorPattern == null) {
              descriptorPattern = "plugin.xml";
            }
            implementationClass = BootImpl.class.getName();
          }
          assert modulesDir != null : "Plugins directory not specified";
          assert application != null : "Application ID not specified";
          assert descriptorPattern != null : "Descriptor pattern not specified";
    	    getInstance(implementationClass).run(application, modulesDir, descriptorPattern, argList);
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