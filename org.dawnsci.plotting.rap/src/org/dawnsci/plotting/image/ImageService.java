/*
 * Copyright (c) 2012 European Synchrotron Radiation Facility,
 *                    Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */ 
package org.dawnsci.plotting.image;

import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;

import org.dawb.common.services.IPaletteService;
import org.dawnsci.plotting.api.histogram.IImageService;
import org.dawnsci.plotting.api.histogram.ImageServiceBean;
import org.dawnsci.plotting.api.histogram.ImageServiceBean.HistoType;
import org.dawnsci.plotting.api.histogram.ImageServiceBean.ImageOrigin;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.AbstractServiceFactory;
import org.eclipse.ui.services.IServiceLocator;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.BooleanDataset;
import uk.ac.diamond.scisoft.analysis.dataset.DatasetUtils;
import uk.ac.diamond.scisoft.analysis.dataset.Maths;
import uk.ac.diamond.scisoft.analysis.dataset.RGBDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Stats;
import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;

/**
 * 
 
   Histogramming Explanation
   ---------------------------
   Image intensity distribution:

                ++----------------------**---------------
                +                      *+ *              
                ++                    *    *             
                |                     *    *             
                ++                    *     *            
                *                    *       *            
                +*                   *       *            
                |*                  *        *            
                +*                  *        *           
                |                  *          *         
                ++                 *          *          
                |                  *           *        
                ++                 *           *        
                |                 *            *        
                ++                *            *       
                                 *              *      
        Min Cut           Min    *              *      Max                     Max cut
 Red <- |   (min colour)  |    (color range, palette)  |      (max color)      | -> Blue
                                *                 *  
                |              *        +         *  
----------------++------------**---------+----------**----+---------------**+---------------++

 
 * @author fcp94556
 *
 */
public class ImageService extends AbstractServiceFactory implements IImageService {
	
	
	static {
		// We just use file extensions
		LoaderFactory.setLoaderSearching(false); 
		// This now applies for the whole workbench
	}

	public ImageService() {
		
	}
	
	/**
	 * This method is not thread safe
	 */
	public Image getImage(ImageServiceBean bean) {
		final ImageData data = getImageData(bean);
		return new Image(Display.getCurrent(), data);
	}
	
	private static final int MIN_PIX_INDEX = 253;
	private static final int NAN_PIX_INDEX = 254;
	private static final int MAX_PIX_INDEX = 255;
	
	private static final byte MIN_PIX_BYTE = (byte)(MIN_PIX_INDEX & 0xFF);
	private static final byte NAN_PIX_BYTE = (byte)(NAN_PIX_INDEX & 0xFF);
	private static final byte MAX_PIX_BYTE = (byte)(MAX_PIX_INDEX & 0xFF);
	
	/**
	 * getImageData(...) provides an image in a given palette data and origin.
	 * Faster than getting a resolved image
	 * 
	 * This method should be thread safe.
	 */
	public ImageData getImageData(ImageServiceBean bean) {
		
		AbstractDataset image    = getImageLoggedData(bean);
		ImageOrigin     origin   = bean.getOrigin();
		if (origin==null) origin = ImageOrigin.TOP_LEFT;
		PaletteData     palette  = bean.getPalette();
		
		if (image instanceof RGBDataset) {
			switch (origin) {
			case TOP_LEFT:
				break;
			case TOP_RIGHT:
				image = DatasetUtils.transpose(image);
				image = image.getSlice(null, null, new int[] {1,-1});
				break;
			case BOTTOM_LEFT:
				image = DatasetUtils.transpose(image);
				image = image.getSlice(null, null, new int[] {-1,1});
				break;
			case BOTTOM_RIGHT:
				image = image.getSlice(null, null, new int[] {-1,-1});
				break;
			}
			RGBDataset rgbImage = (RGBDataset) image;
			return SWTImageUtils.createImageData(rgbImage, 0, 255, null, null, null, false, false, false);
		}
		
		int depth = bean.getDepth();
		final int size  = (int)Math.round(Math.pow(2, depth));
		
		createMaxMin(bean);
		float max = getMax(bean);
		float min = getMin(bean);
		
		float maxCut = getMaxCut(bean);
		float minCut = getMinCut(bean);
		
		// now deal with the log if needed
		if(bean.isLogColorScale()) {
			image = Maths.log10(image);
			max = (float) Math.log10(max);
			min = (float) Math.log10(min);
			if (min <= 0 || Float.isNaN(min)) min = (float) 0.0000001;
			maxCut = (float) Math.log10(maxCut);
			minCut = (float) Math.log10(minCut);
		}
		
		if (bean.getFunctionObject()!=null ) {
			throw new RuntimeException("Function objects not supported in RAP!");
		}
		
		if (depth>8) { // Depth > 8 will not work properly at the moment.
			throw new RuntimeException(getClass().getSimpleName()+" only supports 8-bit images unless we a FunctionContainer has been set!");
			//if (depth == 16) palette = new PaletteData(0x7C00, 0x3E0, 0x1F);
			//if (depth == 24) palette = new PaletteData(0xFF, 0xFF00, 0xFF0000);
			//if (depth == 32) palette = new PaletteData(0xFF00, 0xFF0000, 0xFF000000);
		}
		
		final int[]   shape = image.getShape();
		if (bean.isCancelled()) return null;	
				
		int len = image.getSize();
		if (len == 0) return null;

		// The last three indices of the palette are always taken up with bound colours
		createCutColours(bean); // Modifies the palette data and sets the withheld indices
		
		float scale;
		float maxPixel;
		if (max > min) {
			// 4 because 1 less than size and then 1 for each bound colour is lost.
			scale = Float.valueOf(size-4) / (max - min);
			maxPixel = max - min;
		} else {
			scale = 1f;
			maxPixel = 0xFF;
		}
		if (bean.isCancelled()) return null;
		
		BooleanDataset mask = bean.getMask()!=null
				            ? (BooleanDataset)DatasetUtils.cast((AbstractDataset)bean.getMask(), AbstractDataset.BOOL)
				            : null;
				            
 		ImageData imageData = null;
 		
 		// We use a byte array directly as this is faster than using setPixel(...)
 		// on image data. Set pixel does extra floating point operations. The downside
 		// is that by doing this we certainly have to have 8 bit as getPixelColorIndex(...)
 		// forces the use of on byte.
		final byte[] scaledImageAsByte = new byte[len];

		if (origin==ImageOrigin.TOP_LEFT) { 
			
			int index = 0;
			// This loop is usually the same as the image is read in but not always depending on loader.
			for (int i = 0; i<shape[0]; ++i) {
				if (bean.isCancelled()) return null;				
				for (int j = 0; j<shape[1]; ++j) {
					
					// This saves a value lookup when the pixel is certainly masked.
					scaledImageAsByte[index] = mask==null || mask.getBoolean(i,j)
						            ? getPixelColorIndex(image.getFloat(i,j), min, max, scale, maxPixel, minCut, maxCut)
					                : NAN_PIX_BYTE;
					++index;
				}
			}
			imageData = new ImageData(shape[1], shape[0], 8, palette, 1, scaledImageAsByte);
	
		} else if (origin==ImageOrigin.BOTTOM_LEFT) {

			int index = 0;
			// This loop is slower than looping over all data and using image.getElementDoubleAbs(...)
			// However it reorders data for the axes
			for (int i = shape[1]-1; i>=0; --i) {
				if (bean.isCancelled()) return null;
				for (int j = 0; j<shape[0]; ++j) {
					
					// This saves a value lookup when the pixel is certainly masked.
					scaledImageAsByte[index]  = mask==null || mask.getBoolean(j,i)
				                    ? getPixelColorIndex(image.getFloat(j,i), min, max, scale, maxPixel, minCut, maxCut)
			                        : NAN_PIX_BYTE;
					index++;
				}
			}
			imageData = new ImageData(shape[0], shape[1], 8, palette, 1, scaledImageAsByte);
			
		} else if (origin==ImageOrigin.BOTTOM_RIGHT) {

			int index = 0;
			// This loop is slower than looping over all data and using image.getElementDoubleAbs(...)
			// However it reorders data for the axes
			for (int i = shape[0]-1; i>=0; --i) {
				if (bean.isCancelled()) return null;
			    for (int j = shape[1]-1; j>=0; --j) {
				

					// This saves a value lookup when the pixel is certainly masked.
					scaledImageAsByte[index] = mask==null || mask.getBoolean(i,j)
						            ? getPixelColorIndex(image.getFloat(i,j), min, max, scale, maxPixel, minCut, maxCut)
					                : NAN_PIX_BYTE;
						index++;
				}
			}
			imageData = new ImageData(shape[1], shape[0], 8, palette, 1, scaledImageAsByte);
			
		} else if (origin==ImageOrigin.TOP_RIGHT) {

			int index = 0;
			// This loop is slower than looping over all data and using image.getElementDoubleAbs(...)
			// However it reorders data for the axes
			for (int i = 0; i<shape[1]; ++i) {
				if (bean.isCancelled()) return null;
				for (int j = shape[0]-1; j>=0; --j) {
					
					scaledImageAsByte[index]  = mask==null || mask.getBoolean(j,i)
		                            ? getPixelColorIndex(image.getFloat(j, i), min, max, scale, maxPixel, minCut, maxCut)
	                                : NAN_PIX_BYTE;
		        	index++;
				}
			}
			imageData = new ImageData(shape[0], shape[1], 8, palette, 1, scaledImageAsByte);
		}
				
		return imageData;
	}

	private float getMax(ImageServiceBean bean) {
		if (bean.getMaximumCutBound()==null ||  bean.getMaximumCutBound().getBound()==null) {
			return bean.getMax().floatValue();
		}
		return Math.min(bean.getMax().floatValue(), bean.getMaximumCutBound().getBound().floatValue());
	}
	
	private float getMin(ImageServiceBean bean) {
		if (bean.getMinimumCutBound()==null ||  bean.getMinimumCutBound().getBound()==null) {
			return bean.getMin().floatValue();
		}
		return Math.max(bean.getMin().floatValue(), bean.getMinimumCutBound().getBound().floatValue());
	}
	
	private float getMaxCut(ImageServiceBean bean) {
		if (bean.getMaximumCutBound()==null ||  bean.getMaximumCutBound().getBound()==null) {
			return Float.POSITIVE_INFINITY;
		}
		return bean.getMaximumCutBound().getBound().floatValue();
	}
	
	private float getMinCut(ImageServiceBean bean) {
		if (bean.getMinimumCutBound()==null ||  bean.getMinimumCutBound().getBound()==null) {
			return Float.NEGATIVE_INFINITY;
		}
		return bean.getMinimumCutBound().getBound().floatValue();
	}

	/**
	 * Calling this wipes out the last three RGBs. Even if you set max
	 * @param bean
	 */
	private void createCutColours(ImageServiceBean bean) {
		
		// We *DO NOT* copy the palette here so up to 3 of the original
		// colours can be changed. Instead whenever a palette is given to an
		// ImageService bean it should be original.
		if (bean.getPalette()==null) {
			try {
				final IPaletteService service = (IPaletteService)PlatformUI.getWorkbench().getService(IPaletteService.class);
				bean.setPalette(service.getPaletteData("Gray Scale"));
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
		
		// We have three special values, those which are greater than the max cut,
		// less than the min cut and the NaN number. For these we use special pixel
		// values in the palette as defined by the cut bound if it is set.
		if (bean.getMinimumCutBound()!=null && bean.getMinimumCutBound().getColor()!=null) {
			int[] ia = bean.getMinimumCutBound().getColor();
			bean.getPalette().colors[MIN_PIX_INDEX] = new RGB(ia[0], ia[1], ia[2]);
		}
		
		if (bean.getNanBound()!=null && bean.getNanBound().getColor()!=null) {
			int[] ia = bean.getNanBound().getColor();
			bean.getPalette().colors[NAN_PIX_INDEX] = new RGB(ia[0], ia[1], ia[2]);
		}
		
		if (bean.getMaximumCutBound()!=null && bean.getMaximumCutBound().getColor()!=null) {
			int[] ia = bean.getMaximumCutBound().getColor();
			bean.getPalette().colors[MAX_PIX_INDEX] = new RGB(ia[0], ia[1], ia[2]);
		}
		
	}

	private void createMaxMin(ImageServiceBean bean) {
		
		float[] stats  = null;
		if (bean.getMin()==null) {
			if (stats==null) stats = getFastStatistics(bean); // do not get unless have to
			bean.setMin(stats[0]);
		}
		
		if (bean.getMax()==null) {
			if (stats==null) stats = getFastStatistics(bean); // do not get unless have to
		    bean.setMax(stats[1]);
		}		
	}

	/**
	 * private finals inline well by the compiler.
	 * @param val
	 * @param min
	 * @param max
	 * @param scale
	 * @param maxPixel
	 * @param scaledImageAsByte
	 */
	private final static byte getPixelColorIndex(final double  val, 
												 final float  min, 
												 final float  max, 
												 final float  scale, 
												 final float  maxPixel,
												 final float  minCut,
												 final float  maxCut) {
	    
		// Deal with bounds
	    if (val<=minCut) return MIN_PIX_BYTE;		
		
		if (Double.isNaN(val)) return NAN_PIX_BYTE;
		//TODO is this necessary??
		if (Float.isNaN((float)val)) return NAN_PIX_BYTE;
	    
		if (val>=maxCut) return MAX_PIX_BYTE;	
		
		// If the pixel is within the bounds		
		float scaled_pixel;
		if (val < min) {
			scaled_pixel = 0;
		} else if (val >= max) {
			scaled_pixel = maxPixel;
		} else {
			scaled_pixel = (float)val - min;
		}
		scaled_pixel = scaled_pixel * scale;
		
		return (byte) (0x000000FF & ((int) scaled_pixel));
	}
	
	private AbstractDataset getImageLoggedData(ImageServiceBean bean) {
		AbstractDataset imageDataset = (AbstractDataset)bean.getImage();
//		if (bean.isLogColorScale()) {
//			AbstractDataset result = Maths.subtract(imageDataset, bean.getLogOffset());
//			imageDataset = Maths.log10(result);
//		}
		return imageDataset;
	}

	/**
	 * Fast statistics as a rough guide - this is faster than AbstractDataset.getMin()
	 * and getMax() which may cache but slows the opening of images too much.
	 * The return array[2] was added in "Updated for Diffraction Tool." commit,
	 * but no trace of such usage. However it should not be removed, because
	 * it is useful as return array[3].
	 * 
	 * @param bean
	 * @return [0] = min [1] = max(=mean*constant) [2] = mean [3] max
	 */
	public float[] getFastStatistics(ImageServiceBean bean) {
		
		AbstractDataset image    = getImageLoggedData(bean);
		if(bean.isLogColorScale()) {
			image = Maths.log10(image);
		}
		
		if (bean.getHistogramType()==HistoType.OUTLIER_VALUES) {

			try {
			    double[] stats = Stats.outlierValues(image, bean.getLo(), bean.getHi(), -1);
			    
			    if (bean.isLogColorScale()) {
			    	return new float[]{(float)Math.pow(stats[0],10), (float)Math.pow(stats[1],10), -1};
				}
			    return new float[]{(float)stats[0], (float)stats[1], -1};
			} catch (IllegalArgumentException iae) {
				bean.setLo(10);
				bean.setHi(90);
			    double[] stats = Stats.outlierValues(image, bean.getLo(), bean.getHi(), -1);
			    if (bean.isLogColorScale()) {
			    	return new float[]{(float)Math.pow(stats[0],10), (float)Math.pow(stats[1],10), -1};
				}
			    return new float[]{(float)stats[0], (float)stats[1], -1};
			}
		}
		
		float min = Float.MAX_VALUE;
		float max = -Float.MAX_VALUE;
		float sum = 0.0f;
		int size = 0;
		
		BooleanDataset mask = bean.getMask()!=null
	                        ? (BooleanDataset)DatasetUtils.cast((AbstractDataset)bean.getMask(), AbstractDataset.BOOL)
	                        : null;

	    // Big loop warning:
		for (int index = 0; index < image.getSize(); ++index) {
			
			final double dv = image.getElementDoubleAbs(index);
			try {
			    if (mask!=null && index<mask.getSize()) {
			    	if (!mask.getElementBooleanAbs(index)) {
			    		continue; // Masked!
			    	}
			    }
			} catch(java.lang.ArrayIndexOutOfBoundsException ignored) {
				// Mask may be different size, continue if is.
			}
			
			if (Double.isNaN(dv))      continue;
			if (!bean.isInBounds(dv))  continue;
						
			final float val = (float)dv;
			
			sum += val;
			if (val < min) min = val;
			if (val > max) max = val;
			size++;
			
		}
		
		float retMin = min;
		float retMax = Float.NaN;
		float retExtra = Float.NaN;
		
		if (bean.getHistogramType()==HistoType.MEAN) {
			float mean = sum / size;
			retMax = ((float)Math.E)*mean; // Not statistical, E seems to be better than 3...
			retExtra=mean;
			
		} else if (bean.getHistogramType()==HistoType.MEDIAN) { 
			
			float median = Float.NaN;
			try {
				median = ((Number)Stats.median(image)).floatValue(); // SLOW
			} catch (Exception ne) {
				median = ((Number)Stats.median(image.cast(AbstractDataset.INT16))).floatValue();// SLOWER
			}
			retMax = 2f*median;
			retExtra=median;
		}
		
		if (retMax > max)	retMax = max;
		
		if (bean.isLogColorScale()) {
			return new float[]{(float) Math.pow(retMin,10), (float) Math.pow(retMax,10), (float) Math.pow(retExtra,10)};
		}		
		return new float[]{retMin, retMax, retExtra, max};

	}

	@Override
	public Object create(@SuppressWarnings("rawtypes") Class serviceInterface, IServiceLocator parentLocator, IServiceLocator locator) {
		
		if (serviceInterface==IImageService.class) {
			return new ImageService();
		} 
		return null;
	}
	
	public static final class SDAFunctionBean {
		
	}

	
	/**
	 * Converts an SWT ImageData to an AWT BufferedImage.
	 * 
	 * @param bufferedImage
	 * @return
	 */
	@Override
	public BufferedImage getBufferedImage(ImageData data) {
		ColorModel colorModel = null;
		PaletteData palette = data.palette;
		if (palette.isDirect) {
			colorModel = new DirectColorModel(data.depth, palette.redMask,
					palette.greenMask, palette.blueMask);
			BufferedImage bufferedImage = new BufferedImage(colorModel,
					colorModel.createCompatibleWritableRaster(data.width,
							data.height), false, null);
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					int pixel = data.getPixel(x, y);
					RGB rgb = palette.getRGB(pixel);
					bufferedImage.setRGB(x, y, rgb.red << 16 | rgb.green << 8
							| rgb.blue);
				}
			}
			return bufferedImage;
		} else {
			RGB[] rgbs = palette.getRGBs();
			byte[] red = new byte[rgbs.length];
			byte[] green = new byte[rgbs.length];
			byte[] blue = new byte[rgbs.length];
			for (int i = 0; i < rgbs.length; i++) {
				RGB rgb = rgbs[i];
				red[i] = (byte) rgb.red;
				green[i] = (byte) rgb.green;
				blue[i] = (byte) rgb.blue;
			}
			if (data.transparentPixel != -1) {
				colorModel = new IndexColorModel(data.depth, rgbs.length, red,
						green, blue, data.transparentPixel);
			} else {
				colorModel = new IndexColorModel(data.depth, rgbs.length, red,
						green, blue);
			}
			BufferedImage bufferedImage = new BufferedImage(colorModel,
					colorModel.createCompatibleWritableRaster(data.width,
							data.height), false, null);
			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[1];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					int pixel = data.getPixel(x, y);
					pixelArray[0] = pixel;
					raster.setPixel(x, y, pixelArray);
				}
			}
			return bufferedImage;
		}
	}

}
