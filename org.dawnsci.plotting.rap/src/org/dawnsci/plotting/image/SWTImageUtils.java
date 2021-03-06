/*
 * Copyright 2012 Diamond Light Source Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.dawnsci.plotting.image;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IndexIterator;
import uk.ac.diamond.scisoft.analysis.dataset.RGBDataset;
import uk.ac.diamond.scisoft.analysis.histogram.mapfunctions.AbstractMapFunction;

/**
 * Helper methods to convert to SWT images from datasets
 */
public class SWTImageUtils {

	static private ImageData createImageFromRGBADataset(RGBDataset rgbdata, long minv, long maxv)
	{
		ImageData img;
		final IndexIterator iter = rgbdata.getIterator(true);
		final int[] pos = iter.getPos();
		final int[] shape = rgbdata.getShape();
		final int height = shape[0];
		final int width = shape.length == 1 ? 1 : shape[1]; // allow 1D datasets to be saved
		long delta = maxv - minv;
		short off = (short) minv;

		short[] data = rgbdata.getData();
		if (delta < 32) { // 555
			img = new ImageData(width, height, 16, new PaletteData(0x7c00, 0x03e0, 0x001f));
			while (iter.hasNext()) {
				final int n = iter.index;
				final int rgb = (((data[n] - off) & 0x1f) << 10) | (((data[n + 1] - off) & 0x1f) << 5) | ((data[n + 2] - off)& 0x1f);
				img.setPixel(pos[1], pos[0], rgb);
			}
		} else if (delta < 64) { // 565
			img = new ImageData(width, height, 16, new PaletteData(0xf800, 0x07e0, 0x001f));

			while (iter.hasNext()) {
				final int n = iter.index;
				final int rgb = ((((data[n] - off) >> 1) & 0x1f) << 10) | (((data[n + 1] - off) & 0x3f) << 5) | (((data[n + 2] - off) >> 1) & 0x1f);
				img.setPixel(pos[1], pos[0], rgb);
			}
		} else if (delta < 256) { // 888
			img = new ImageData(width, height, 24, new PaletteData(0xff0000, 0x00ff00, 0x0000ff));

			while (iter.hasNext()) {
				final int n = iter.index;
				final int rgb = (((data[n] - off) & 0xff) << 16) | (((data[n + 1] - off) & 0xff) << 8) | ((data[n + 2] - off) & 0xff);
				img.setPixel(pos[1], pos[0], rgb);
			}
		} else {
			int shift = 0;
			while (delta >= 256) {
				shift++;
				delta >>= 1;
			}

			img = new ImageData(width, height, 24, new PaletteData(0xff0000, 0x00ff00, 0x0000ff));

			while (iter.hasNext()) {
				final int n = iter.index;
				final int rgb = ((((data[n] - off) >> shift) & 0xff) << 16) | ((((data[n + 1] - off) >> shift) & 0xff) << 8) | (((data[n + 2] - off) >> shift) & 0xff);
				img.setPixel(pos[1], pos[0], rgb);
			}
		}
		return img;
	}
	
	static private ImageData createImageFromDataset(AbstractDataset a,
													double minv,
													double maxv,
													AbstractMapFunction redFunc,
													AbstractMapFunction greenFunc,
													AbstractMapFunction blueFunc,
													boolean inverseRed,
													boolean inverseGreen,
													boolean inverseBlue) 
	{
		final int[] shape = a.getShape();
		final int height = shape[0];
		final int width = shape.length == 1 ? 1 : shape[1]; // allow 1D datasets to be saved
		ImageData img;
		final IndexIterator iter = a.getIterator(true);
		final int[] pos = iter.getPos();
		img = new ImageData(width, height, 24, new PaletteData(0xff0000, 0x00ff00, 0x0000ff));
		double delta = maxv - minv;
		while (iter.hasNext()) {
			double value = (a.getElementDoubleAbs(iter.index) - minv)/delta;
			final int red = (inverseRed ? (255-redFunc.mapToByte(value)) : redFunc.mapToByte(value));
			final int green = (inverseGreen ? (255-greenFunc.mapToByte(value)) : greenFunc.mapToByte(value));
			final int blue = (inverseBlue ? (255-blueFunc.mapToByte(value)) : blueFunc.mapToByte(value));
			final int rgb = (red << 16) | green << 8 | blue; 		
			img.setPixel(pos[1], pos[0],rgb); 
		}
		return img;
	}

	/**
	 * Create SWT ImageData from a dataset
	 * <p>
	 * The input dataset can be a RGB dataset in which case the mapping functions
	 * and inversion flags are ignored.
	 * @param a dataset
	 * @param max maximum value of dataset
	 * @param redFunc
	 * @param greenFunc
	 * @param blueFunc
	 * @param inverseRed
	 * @param inverseGreen
	 * @param inverseBlue
	 * @return an ImageData object for SWT
	 */
	static public ImageData createImageData(AbstractDataset a, Number max,
											AbstractMapFunction redFunc,
											AbstractMapFunction greenFunc,
											AbstractMapFunction blueFunc,
											boolean inverseRed,
											boolean inverseGreen,
											boolean inverseBlue) {
		return createImageData(a, 0, max, redFunc, greenFunc, blueFunc, inverseRed, inverseGreen, inverseBlue);
	}

	/**
	 * Create SWT ImageData from a dataset
	 * <p>
	 * The input dataset can be a RGB dataset in which case the mapping functions
	 * and inversion flags are ignored.
	 * @param a dataset
	 * @param min minimum value of dataset
	 * @param max maximum value of dataset
	 * @param redFunc
	 * @param greenFunc
	 * @param blueFunc
	 * @param inverseRed
	 * @param inverseGreen
	 * @param inverseBlue
	 * @return an ImageData object for SWT
	 */
	static public ImageData createImageData(AbstractDataset a, Number min, Number max,
											AbstractMapFunction redFunc,
											AbstractMapFunction greenFunc,
											AbstractMapFunction blueFunc,
											boolean inverseRed,
											boolean inverseGreen,
											boolean inverseBlue) {
		ImageData img;

		if (a instanceof RGBDataset) {
			img = createImageFromRGBADataset((RGBDataset)a, min.longValue(), max.longValue());
		} else {
			img = createImageFromDataset(a, min.doubleValue(), max.doubleValue(),redFunc,greenFunc,blueFunc,
										 inverseRed,inverseGreen,inverseBlue);
		}

		return img;
	}

	/**
	 * Create RGB dataset from an SWT image
	 * @param image
	 * @return a RGB dataset
	 */
	static public RGBDataset createRGBDataset(final ImageData image) {
		final int[] data = new int[image.width];
		final RGBDataset rgb = new RGBDataset(image.height, image.width);
		final short[] p = new short[3];
		final PaletteData palette = image.palette;
		if (palette.isDirect) {
			for (int i = 0; i < image.height; i++) {
				image.getPixels(0, i, image.width, data, 0);
				for (int j = 0; j < image.width; j++) {
					int value = data[j];
					p[0] = palette.redShift >= 0 ? (short) ((value & palette.redMask) << palette.redShift) :
						(short) ((value & palette.redMask) >>> -palette.redShift);
					p[1] = palette.greenShift >= 0 ? (short) ((value & palette.greenMask) << palette.greenShift) :
						(short) ((value & palette.greenMask) >>> -palette.greenShift);
					p[2] = palette.blueShift >= 0 ? (short) ((value & palette.blueMask) << palette.blueShift) :
						(short) ((value & palette.blueMask) >>> -palette.blueShift);
					rgb.setItem(p, i, j);
				}
			}
		} else {
			final RGB[] table = palette.getRGBs();
			for (int i = 0; i < image.height; i++) {
				image.getPixels(0, i, image.width, data, 0);
				for (int j = 0; j < image.width; j++) {
					RGB value = table[data[j]];
					p[0] = (short) value.red;
					p[1] = (short) value.green;
					p[2] = (short) value.blue;
					rgb.setItem(p, i, j);
				}
			}
		}

		return rgb;
	}

}
