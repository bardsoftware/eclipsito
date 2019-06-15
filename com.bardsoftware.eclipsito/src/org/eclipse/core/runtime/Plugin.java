package org.eclipse.core.runtime;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

// just default empty implementation of BundleActivator
public abstract class Plugin implements BundleActivator {

	public void start(BundleContext context) throws Exception {
	}

	public void stop(BundleContext context) throws Exception {
	}

}
