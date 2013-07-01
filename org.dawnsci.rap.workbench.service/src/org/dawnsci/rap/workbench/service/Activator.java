package org.dawnsci.rap.workbench.service;

import java.util.Hashtable;

import org.dawb.common.services.IWorkbenchService;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

public class Activator implements BundleActivator {

	private static BundleContext context;

	static BundleContext getContext() {
		return context;
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext bundleContext) throws Exception {
		Activator.context = bundleContext;
		final Hashtable<String,String> props = new Hashtable<String,String>();
		props.put("name",  "IWorkbenchService");
		props.put("class", IWorkbenchService.class.getName());
		props.put("rap",   "true");
		context.registerService(IWorkbenchService.class, new WorkbenchService(), props);
	}

	/*
	 * (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext bundleContext) throws Exception {
		Activator.context = null;
	}

}
