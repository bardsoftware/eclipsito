package org.bardsoftware.eclipsito;

import org.bardsoftware.impl.eclipsito.BootImpl;
import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
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
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public abstract class Boot {

    public abstract void run(String application, List<File> modulesFiles, String descriptorPattern, List<String> args);
    public abstract void shutdown();

    public static final Logger LOG = Logger.getLogger(Boot.class.getName());

    private static final Set<String> CMDLINE_ARGS = new HashSet<>(
    		Arrays.asList(new String[] {"-app", "-plugins-dir", "-plugins-res", "-include", "-verbose"}));
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
          ConsoleHandler handler = new ConsoleHandler();
          handler.setLevel(Level.ALL);
          LOG.addHandler(handler);
          Map<String, String> options = new HashMap<String, String>();
          List<String> argList = new ArrayList<String>(Arrays.asList(args));
          argList = parseArgs(options, argList);

          String application;
          String modulesResource;
          String modulesDir = null;
          String descriptorPattern;
          String implementationClass;
          if (options.containsKey("-verbose")) {
            LOG.setLevel(Level.FINER);
          }
          if (options.containsKey("-plugins-res") || options.containsKey("-plugins-dir")) {
            application = options.get("-app");
            modulesResource = options.get("-plugins-res");
            modulesDir = options.get("-plugins-dir");
            descriptorPattern = options.get("-include");
            if (descriptorPattern == null) {
              descriptorPattern = "plugin.xml";
            }
            implementationClass = BootImpl.class.getName();
          } else {
            String configName = argList.isEmpty() ? "eclipsito-config.xml" : argList.remove(0);
            LOG.fine(String.format("Searching for config in %s", configName));

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
          }
          LOG.fine(String.format("Args: -plugins-dir=%s -plugins-res=%s descriptor-pattern=%s app=%s", modulesDir, modulesResource, descriptorPattern, application));

          List<File> modulesFiles = new ArrayList<>();
          if (modulesDir == null) {
            assert modulesResource != null : "Plugins directory not specified";
            modulesFiles.addAll(resolveModulesResource(modulesResource));
          } else {
            String[] modulesDirArray = getModulesPaths(modulesDir);
            for (String dir : modulesDirArray) {
              File modulesFile = new File(dir);
              assert modulesFile.isDirectory() && modulesFile.canRead() : String.format("File %s is not a directory or is not readable", modulesFile.getAbsolutePath());
              modulesFiles.add(modulesFile);
            }
          }
          assert application != null : "Application ID not specified";
          assert descriptorPattern != null : "Descriptor pattern not specified";
          getInstance(implementationClass).run(application, modulesFiles, descriptorPattern, argList);
        } catch(Exception e) {
            LOG.log(Level.SEVERE, e.getMessage(), e);
        }
    }

  private static List<File> resolveModulesResource(String modulesResource) {
    String[] modulesDirArray = getModulesPaths(modulesResource);
    List<File> modulesResources = new ArrayList<>();
    for (String resourceDir : modulesDirArray) {

      URL modulesUrl = Boot.class.getResource(resourceDir);
      if (modulesUrl == null) {
        Boot.LOG.severe("Can't resolve plugin resource=" + resourceDir);
        continue;
      }
      String path;
      try {
        path = URLDecoder.decode(modulesUrl.getPath(), "UTF-8");
        modulesResources.add(new File(path));
      } catch (UnsupportedEncodingException e) {
        Boot.LOG.log(Level.SEVERE, "Can't parse plugin location=" + modulesUrl, e);
        continue;
      }
    }
    return modulesResources;
  }

  public static String[] getModulesPaths(String modules) {
    if (modules == null) {
      return new String[0];
    }
    String[] pathParts = modules.split(File.pathSeparator);
    for (int i = 0; i < pathParts.length; i++) {
      if (pathParts[i].startsWith("~")) {
        pathParts[i] = pathParts[i].replaceFirst("~", System.getProperty("user.home"));
      }

    }
    return pathParts;
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
