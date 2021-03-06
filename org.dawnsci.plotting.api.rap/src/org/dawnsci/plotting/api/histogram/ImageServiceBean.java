/*-
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

package org.dawnsci.plotting.api.histogram;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.PaletteData;

import uk.ac.diamond.scisoft.analysis.dataset.IDataset;

/**
 * As histogramming has become more complex and gained more options, this class has become more
 * complex. As much optional information as possible has been defaulted to reduce the values which must be set.
 * 
 * See ImageTrace for how to use the bean. For some calls, only 
 * 
 */
public class ImageServiceBean {
	
	
	private HistogramBound  maximumCutBound = HistogramBound.DEFAULT_MAXIMUM;
	private HistogramBound  minimumCutBound = HistogramBound.DEFAULT_MINIMUM;
	private HistogramBound  nanBound        = HistogramBound.DEFAULT_NAN;
	private IDataset        image;
	private IDataset        mask;
	private PaletteData     palette;
	private ImageOrigin     origin;
	private Number          min;
	private Number          max;
	private IProgressMonitor monitor;
	private HistoType       histogramType = HistoType.MEAN;
	private int             depth=8; // Either 8 or 16 usually. If function object !=null then 
	                                 // this is assumed to override the depth
	private Object          functionObject;
	private boolean         logColorScale=false; // Normally linear, can switch to log color scale.
	private double          logOffset=0.0;
	/**
	 * Only used with HistoType.OUTLIER_VALUES algorithm
	 */
	private double          lo;
	/**
	 * Only used with HistoType.OUTLIER_VALUES algorithm
	 */
	private double          hi;

	public ImageServiceBean() {
		
	}
	
	/**
	 * Clones everything apart from the data, mask and palette.
	 */
	public ImageServiceBean  clone() {
		ImageServiceBean ret = new ImageServiceBean();
		
		ret.min             = (min == null) ? null : min.doubleValue();
		ret.max             = (max == null) ? null : max.doubleValue();
		ret.lo              = lo;
		ret.hi              = hi;
		ret.histogramType   = histogramType;
		ret.logColorScale   = logColorScale;
		ret.logOffset       = logOffset;
		ret.maximumCutBound = cloneBound(maximumCutBound);
		ret.minimumCutBound = cloneBound(minimumCutBound);
		ret.nanBound        = cloneBound(nanBound);
		ret.origin          = origin;
		
		if (getPalette()!=null) {
		    ret.palette = new PaletteData(getPalette().getRGBs());
		}
		return ret;
	}
	
    private HistogramBound cloneBound(HistogramBound clone) {
		return new HistogramBound(clone.getBound(), clone.getColor());
	}

	public ImageServiceBean(IDataset slice, HistoType histoType) {
		this.image = slice;
		this.histogramType = histoType;
	}

	/**
     * Removes all references
     */
	public void dispose() {
		maximumCutBound=null;
		minimumCutBound=null;
		nanBound=null;
		image=null;
		palette=null;
		origin=null;
		min=null;
		max=null;
		monitor=null;
		histogramType=null;
	}

	
	/**
	 * 
	 * @return the original downsampled data. If log scale the 
	 * image will be shifted.
	 */
	public IDataset getImage() {
		return image;
	}

	public void setImage(IDataset image) {
		this.image = image;
	}
	public PaletteData getPalette() {
		return palette;
	}
	/**
	 * *IMPORTANT* - remember to give the bean a copy of a palette which 
	 * you do not mind it changing. This potentially dangerous way the 
	 * bean works (rather than always making a copy for instance) is 
	 * for efficiency reasons.
	 * 
	 * @param palette
	 */
	public void setPalette(PaletteData palette) {
		this.palette = palette;
	}
	public ImageOrigin getOrigin() {
		return origin;
	}
	public void setOrigin(ImageOrigin origin) {
		this.origin = origin;
	}
	/**
	 * The max valid value for the 
	 * @return
	 */
	public Number getMin() {
		return min;
	}
	public void setMin(Number min) {
		this.min = min;
	}
	public Number getMax() {
		return max;
	}
	public void setMax(Number max) {
		this.max = max;
	}
	public void setMonitor(IProgressMonitor monitor) {
		this.monitor = monitor;
	}
	public boolean isCancelled() {
		return monitor!=null && monitor.isCanceled();
	}
	public IProgressMonitor getMonitor() {
		return monitor;
	}
	/**
	 * Default Color positive infinity with blue
	 * @return
	 */
	public HistogramBound getMaximumCutBound() {
		return maximumCutBound;
	}
	public void setMaximumCutBound(HistogramBound maximumBound) {
		this.maximumCutBound = maximumBound;
	}
	/**
	 * Default Color negative infinity with red
	 * @return
	 */
	public HistogramBound getMinimumCutBound() {
		return minimumCutBound;
	}
	public void setMinimumCutBound(HistogramBound minimumBound) {
		this.minimumCutBound = minimumBound;
	}
	/**
	 * Default Color NaN with green
	 * @return
	 */
	public HistogramBound getNanBound() {
		return nanBound;
	}
	public void setNanBound(HistogramBound nanBound) {
		this.nanBound = nanBound;
	}
	public HistoType getHistogramType() {
		return histogramType;
	}
	public void setHistogramType(HistoType histogramType) {
		this.histogramType = histogramType;
	}

	


	public int getDepth() {
		return depth;
	}
	
	/**
	 * NOTE PaletteData with RGB[] only works with 8-bit or below
	 * For 16-bit and above you must use a direct ImagePalette using the
	 * constructor ImagePalette(int,int,int)
	 * 
	 * @param colorDepth
	 */
	public void setDepth(int colorDepth) {
		this.depth = colorDepth;
	}
	
	/**
	 * Normally null or may be a SDAFunctionBean which
	 * defines the functions to use.
	 * 
	 * @return
	 */
	public Object getFunctionObject() {
		return functionObject;
	}
	/**
	 * Normally null or you may set to a SDAFunctionBean which
	 * defines the functions to use.
	 * 
	 * @return
	 */
	public void setFunctionObject(Object userObject) {
		this.functionObject = userObject;
	}

	public boolean isInBounds(double dv) {
		if (!isInsideMinCut(dv)) return false;
		if (!isInsideMaxCut(dv)) return false;
        return true;
	}
	
	public boolean isInsideMinCut(double dv) {
		if (getMinimumCutBound()==null) return true;
	    if (dv<=getMinimumCutBound().getBound().doubleValue()) return false;
		return true;
	}
	public boolean isInsideMaxCut(double dv) {
		if (getMaximumCutBound()==null) return true;
	    if (dv>=getMaximumCutBound().getBound().doubleValue()) return false;
		return true;
	}
	public boolean isValidNumber(double dv) {
		if (getNanBound()==null) return true;
		if (Double.isNaN(dv)) return false;
		if (Float.isNaN((float)dv)) return false;

		return true;
	}
	
	/**
	 * The mask is false to mask and true to do nothing
	 * @return
	 */
	public IDataset getMask() {
		return mask;
	}
	
	/**
	 * The mask is false to mask and true to do nothing
	 * @return
	 */
	public void setMask(IDataset mask) {
		this.mask = mask;
	}
	public boolean isLogColorScale() {
		return logColorScale;
	}
	public void setLogColorScale(boolean logColorScale) {
		this.logColorScale = logColorScale;
		if(logColorScale) {
			logOffset = image.min().doubleValue()-1.0;
		}
	}
	

	public enum HistoType {
		
		/**
		 * NOTE These strings are in preferences and referenced by value in LivePerspective
		 */
		MEAN(0, "Mean"), MEDIAN(1, "Median"), OUTLIER_VALUES(2, "Outlier Values");

		public final String label;
		public final int    index;
		HistoType(int index, String label) {
			this.index = index;
			this.label = label;
		}
		public String getLabel() {
			return label;
		}
		public int getIndex() {
			return index;
		}
		public static List<HistoType> histoTypes;
		static {
			histoTypes = new ArrayList<HistoType>();
			histoTypes.add(MEAN);
			histoTypes.add(MEDIAN);
			histoTypes.add(OUTLIER_VALUES);
		}
		public static HistoType forLabel(String label) {
			for (HistoType t : histoTypes) {
				if (t.label.equals(label)) return t;
			}
			return null;
		}

	}



	public enum ImageOrigin {
		TOP_LEFT("Top left"), TOP_RIGHT("Top right"), BOTTOM_LEFT("Bottom left"), BOTTOM_RIGHT("Bottom right");

		public static List<ImageOrigin> origins;
		static {
			origins = new ArrayList<ImageOrigin>();
			origins.add(TOP_LEFT);
			origins.add(TOP_RIGHT);
			origins.add(BOTTOM_LEFT);
			origins.add(BOTTOM_RIGHT);
		}

		private String label;
		public String getLabel() {
			return label;
		}

		ImageOrigin(String label) {
			this.label = label;
		}

		public static ImageOrigin forLabel(String label) {
			for (ImageOrigin o : origins) {
				if (o.label.equals(label)) return o;
			}
			return null;
		}
	}



	public double getLo() {
		return lo;
	}

	public void setLo(double lo) {
		this.lo = lo;
	}

	public double getHi() {
		return hi;
	}

	public void setHi(double hi) {
		this.hi = hi;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + depth;
		result = prime * result
				+ ((functionObject == null) ? 0 : functionObject.hashCode());
		long temp;
		temp = Double.doubleToLongBits(hi);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result
				+ ((histogramType == null) ? 0 : histogramType.hashCode());
		result = prime * result + ((image == null) ? 0 : image.hashCode());
		temp = Double.doubleToLongBits(lo);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + (logColorScale ? 1231 : 1237);
		temp = Double.doubleToLongBits(logOffset);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		result = prime * result + ((mask == null) ? 0 : mask.hashCode());
		result = prime * result + ((max == null) ? 0 : max.hashCode());
		result = prime * result
				+ ((maximumCutBound == null) ? 0 : maximumCutBound.hashCode());
		result = prime * result + ((min == null) ? 0 : min.hashCode());
		result = prime * result
				+ ((minimumCutBound == null) ? 0 : minimumCutBound.hashCode());
		result = prime * result + ((monitor == null) ? 0 : monitor.hashCode());
		result = prime * result
				+ ((nanBound == null) ? 0 : nanBound.hashCode());
		result = prime * result + ((origin == null) ? 0 : origin.hashCode());
		result = prime * result + ((palette == null) ? 0 : palette.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ImageServiceBean other = (ImageServiceBean) obj;
		if (depth != other.depth)
			return false;
		if (functionObject == null) {
			if (other.functionObject != null)
				return false;
		} else if (!functionObject.equals(other.functionObject))
			return false;
		if (Double.doubleToLongBits(hi) != Double.doubleToLongBits(other.hi))
			return false;
		if (histogramType != other.histogramType)
			return false;
		if (image == null) {
			if (other.image != null)
				return false;
		} else if (!image.equals(other.image))
			return false;
		if (Double.doubleToLongBits(lo) != Double.doubleToLongBits(other.lo))
			return false;
		if (logColorScale != other.logColorScale)
			return false;
		if (Double.doubleToLongBits(logOffset) != Double
				.doubleToLongBits(other.logOffset))
			return false;
		if (mask == null) {
			if (other.mask != null)
				return false;
		} else if (!mask.equals(other.mask))
			return false;
		if (max == null) {
			if (other.max != null)
				return false;
		} else if (!max.equals(other.max))
			return false;
		if (maximumCutBound == null) {
			if (other.maximumCutBound != null)
				return false;
		} else if (!maximumCutBound.equals(other.maximumCutBound))
			return false;
		if (min == null) {
			if (other.min != null)
				return false;
		} else if (!min.equals(other.min))
			return false;
		if (minimumCutBound == null) {
			if (other.minimumCutBound != null)
				return false;
		} else if (!minimumCutBound.equals(other.minimumCutBound))
			return false;
		if (monitor == null) {
			if (other.monitor != null)
				return false;
		} else if (!monitor.equals(other.monitor))
			return false;
		if (nanBound == null) {
			if (other.nanBound != null)
				return false;
		} else if (!nanBound.equals(other.nanBound))
			return false;
		if (origin != other.origin)
			return false;
		if (palette == null) {
			if (other.palette != null)
				return false;
		} else if (!palette.equals(other.palette))
			return false;
		return true;
	}

	public double getLogOffset() {
		return logOffset;
	}

}
