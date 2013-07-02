/*******************************************************************************
 * Copyright (c) 2010 Oak Ridge National Laboratory.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.dawnsci.plotting.jreality.util;

import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Transform;

/**Utility function for graphics operations.
 * @author Xihui Chen
 *
 */
public final class GraphicsUtil {
	
	/**
	 * Used for single sourcing, returns null if called in RAP Context.
	 * @param image
	 * @return
	 */
	public static GC createGC(Image image) {
		try {
		    return GC.class.getConstructor(Image.class).newInstance(image);
		} catch (Exception ne) {
			return null;
		}
	}
	
	public static void setTransform(GC gc, Transform transform) {
		try {
			GC.class.getMethod("setTransform", Transform.class).invoke(gc, transform);
		} catch (Exception ne) {
			return;
		}
	}


	public static Cursor createCursor(Device device, ImageData imageData, int width, int height) {
		try {
			return Cursor.class.getConstructor(Device.class, ImageData.class, int.class, int.class).newInstance(device, imageData, width, height);
		} catch (Exception ne) {
			return null;
		}
	}
}
