package org.bardsoftware.eclipsito;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;

import org.bardsoftware.impl.eclipsito.BootImpl;
import org.bardsoftware.impl.eclipsito.PluginDescriptor;
import org.w3c.dom.Document;

public abstract class Boot {

    public abstract void run(String application, File modulesFile, String descriptorPattern, List<String> args);
    public abstract void shutdown();
    
    public static final Logger LOG = Logger.getLogger(Boot.class.getName());
    
    private static final Set<String> CMDLINE_ARGS = new HashSet<>(
    		Arrays.asList(new String[] {"-app", "-plugins-dir", "-plugins-res", "-include"}));
    // properties
    private static final String IMPLEMENTATION_CLASSNAME = "org.bardsoftware.modules.regxp.platform-implementation.classname";

    // config attributes
    private static final String ATTRIBUTE_PLATFORM_CLASSNAME = "platform-classname";
    private static final String ATTRIBUTE_LOGGING_LEVEL = "logging-level";
    private static final String ATTRIBUTE_MODULES_RESOURCE = "modules-resource";
    private static final String ATTRIBUTE_DESCRIPTOR_FILE_PATTERN = "descriptor-file-pattern";
    private static final String ATTRIBUTE_APPLICATION = "application";
	private static final String ATTRIBUTE_MODULES_DIR = "modules-dir";

    private static Boot ourInstance;

    private static List<String> parseArgs(Map<String, String> options, List<String> args) {
      List<String> unknownArgs = new ArrayList<>();
      String argName = null;
      for (int i = 0; i < args.size(); i++) {
        String arg = args.get(i);
        if (arg.startsWith("-")) {
          if (argName != null) {
	          if (CMDLINE_ARGS.contains(argName)) {
	            options.put(argName, "");
	          } else {
	        	unknownArgs.add(argName);
	          }
          }
          argName = arg;
          continue;
        }
        if (argName == null) {
          unknownArgs.add(arg);
          continue;
        }
        if (CMDLINE_ARGS.contains(argName)) {
          options.put(argName, arg);
        } else {
          unknownArgs.add(argName);
          unknownArgs.add(arg);
        }
        argName = null;
      }
      if (argName != null) {
        if (CMDLINE_ARGS.contains(argName)) {
          options.put(argName, "");
        } else {
          unknownArgs.add(argName);
        }    	  
      }
      return unknownArgs;
    }
    
    public static void main(String args[]) {
        try {
          LOG.setLevel(Level.ALL);
          Map<String, String> options = new HashMap<String, String>();
          List<String> argList = new ArrayList<String>(Arrays.asList(args));
          argList = parseArgs(options, argList);
          
          String application;
          String modulesResource;
          String modulesDir = null;
          String descriptorPattern;
          String implementationClass;
          if (options.isEmpty()) {
            String configName = argList.isEmpty() ? "eclipsito-config.xml" : argList.remove(0);
            LOG.info(String.format("No options passed to Eclipsito. Searching for config in %s", configName));

            URL configResource = Boot.class.getClassLoader().getResource(configName);
            if (configResource==null) {
                throw new RuntimeException("Eclipsito configuration file="+configName+" has not been found!");
            }
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(configResource.openStream());
            implementationClass = doc.getDocumentElement().getAttribute(ATTRIBUTE_PLATFORM_CLASSNAME);
            application = doc.getDocumentElement().getAttribute(ATTRIBUTE_APPLICATION);
            modulesResource = doc.getDocumentElement().getAttribute(ATTRIBUTE_MODULES_RESOURCE);
            if (modulesResource == null || "".equals(modulesResource)) {
              modulesDir = doc.getDocumentElement().getAttribute(ATTRIBUTE_MODULES_DIR);
              assert modulesDir != null && !modulesDir.isEmpty() : "Neither plugin resource nor plugin directory were specified";
            }            
            descriptorPattern = doc.getDocumentElement().getAttribute(ATTRIBUTE_DESCRIPTOR_FILE_PATTERN);
          } else {
            application = options.get("-app");
            modulesResource = options.get("-plugins-res");
            modulesDir = options.get("-plugins-dir");
            descriptorPattern = options.get("-include");
            if (descriptorPattern == null) {
              descriptorPattern = "plugin.xml";
            }
            implementationClass = BootImpl.class.getName();
          }
          LOG.info(String.format("Args: -plugins-dir=%s -plugins-res=%s descriptor-pattern=%s app=%s", modulesDir, modulesResource, descriptorPattern, application));
          File modulesFile;
          if (modulesDir == null) {
            assert modulesResource != null : "Plugins directory not specified";
            modulesFile = resolveModulesResource(modulesResource);
          } else {
        	modulesFile = new File(modulesDir);
          }
          assert modulesFile != null : "Failed to find plugins directory";
          assert modulesFile.isDirectory() && modulesFile.canRead() : String.format("File %s is not a directory or is not readable", modulesFile.getAbsolutePath());
          assert application != null : "Application ID not specified";
          assert descriptorPattern != null : "Descriptor pattern not specified";
          getInstance(implementationClass).run(application, modulesFile, descriptorPattern, argList);
        } catch(Exception e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }

    private static File resolveModulesResource(String modulesResource) {
        URL modulesUrl = Boot.class.getResource(modulesResource);
        if (modulesUrl == null) {
          Boot.LOG.severe("Can't resolve plugin resource=" + modulesResource);
          return null;
        }
        String path;
        try {
          path = URLDecoder.decode(modulesUrl.getPath(), "UTF-8");
          return new File(path);
        } catch (UnsupportedEncodingException e) {
          Boot.LOG.log(Level.SEVERE, "Can't parse plugin location=" + modulesUrl, e);
          return null;
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