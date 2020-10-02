package org.eclipse.core.runtime;

import com.bardsoftware.eclipsito.update.UpdaterImpl;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.preferences.IPreferencesService;
import org.osgi.framework.Bundle;

import java.io.IOException;
import java.net.URL;

/**
 * org.eclipse.runtime.Platform emulation, only those methods are implemented,
 * which we are interested in.
 */
public abstract class Platform {
  public static final String PI_RUNTIME = "org.eclipse.core.runtime";
  private static Platform ourInstance;

  protected static void setInstance(Platform platform) {
      ourInstance = platform;
  }

  public static IExtensionRegistry getExtensionRegistry() {
      IExtensionRegistry result = null;
      if (ourInstance != null) {
          result = ourInstance.getExtensionRegistryImpl();
      }
      return result;
  }

  public static Bundle getBundle(String symbolicName) {
      Bundle result = null;
      if (ourInstance != null) {
          result = ourInstance.getBundleImpl(symbolicName);
      }
      return result;
  }

  public static UpdaterImpl getUpdater() { return (ourInstance == null) ? null : ourInstance.geUpdaterImpl(); }


  public static IJobManager getJobManager() {
      return ourInstance.getJobManagerImpl();
  }

  public static String[] getCommandLineArgs() {
  return null;
}

	public static URL resolve(URL url) throws IOException {
	    return url;
	}

	public static IPreferencesService getPreferencesService() {
	    return ourInstance.getPreferencesServiceImpl();
	}

	protected abstract IExtensionRegistry getExtensionRegistryImpl();
  protected abstract Bundle getBundleImpl(String symbolicName);
  protected abstract IJobManager getJobManagerImpl();
  protected abstract IPreferencesService getPreferencesServiceImpl();
  protected abstract UpdaterImpl geUpdaterImpl();

}
