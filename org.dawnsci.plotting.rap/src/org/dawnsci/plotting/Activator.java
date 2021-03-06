package org.dawnsci.plotting;

import java.util.Hashtable;

import org.dawb.common.services.IClassLoaderService;
import org.dawnsci.plotting.api.histogram.IImageService;
import org.dawnsci.plotting.image.ImageService;
import org.dawnsci.plotting.service.ClassLoaderService;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.dawnsci.plotting"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		Hashtable<String, String> props = new Hashtable<String, String>(1);
		props.put("description", "A service used to register a class loader which is aware of scisoft classes.");
		context.registerService(IClassLoaderService.class, new ClassLoaderService(), props);
		
		props = new Hashtable<String, String>(1);
		props.put("description", "A service used to get colouring information for drawing images of synchrotron data.");
		context.registerService(IImageService.class, new ImageService(), props);

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	/**
	 * Creates the image, this should be disposed later.
	 * @param path
	 * @return Image
	 */
	public static Image getImage(String path) {
		ImageDescriptor des = imageDescriptorFromPlugin(PLUGIN_ID, path);
		return des.createImage();
	}

	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

}
