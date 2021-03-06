package org.dawnsci.plotting.api.axis;


/**
 * Interface for converting between index value and coordinate in plotting system
 * (which is screen pixels).
 * 
 * Each region receives their own copy of ICoordinateSystem and this is
 * disposed when the region is. This avoids memory leaks on any listeners added
 * to the axes.
 * 
 * For regions over images there are addition methods for dealing with custom axes
 * (because the ROIs are in the image coordinates by default). So the image indices
 * can then be converted to whatever custom axis labels are set.
 * 
 * @author fcp94556
 *
 */
public interface ICoordinateSystem {

	
	/**
	 * The position in pixels of a given value.
	 * @param value
	 * @return
	 */
	public int[] getValuePosition(double... value);
	
	/**
	 * The value for a position in pixels.
	 * @param position
	 * @return
	 */
	public double[] getPositionValue(int... position);

	/**
	 * Listen to the coordinates changing, zoom in or image rotated usually.
	 * @param l
	 */
	public void addCoordinateSystemListener(ICoordinateSystemListener l);
	
	/**
	 * Removing listening to the coordinates changing, zoom in or image rotated usually.
	 * @param l
	 */
	public void removeCoordinateSystemListener(ICoordinateSystemListener l);

	/**
	 * Get x-axis when the orientation is standard.
	 * @return
	 */
	public IAxis getX();
	
	
	/**
	 * Get x-axis when the orientation is standard.
	 * @return
	 */
	public IAxis getY();

	/**
	 * Called when the region is removed. This assumes each ICoordinateSystem instance is unique
	 * to each region.
	 */
	public void dispose();

	/**
	 * True if the x axis goes from high in the left to low in the right
	 * (Ie opposite to the way it is in TOP_LEFT orientation)
	 * @return
	 */
	public boolean isXReversed();

	/**
	 * True if the y axis goes from low in the bottom to high in the top.
	 * (Ie opposite to the way it is in TOP_LEFT orientation)
	 * @return
	 */
	public boolean isYReversed();

	/**
	 * Ratio of x axis to y axis scaling
	 * @return
	 */
	public double getAspectRatio();
	
	
	/**
	 * For regions over images: if the axis data set has been set, this method will return 
	 * a point in the coordinates of the axes labels rather
	 * than the indices. If no axes are set, then the original point is
	 * returned. If the plot is 1D then the original values are returned.
	 * 
	 * NOTE the double[] passed in is not the pixel coordinates point from
	 * events like a mouse click (int[]). It is the data point (indices of
	 * real data the case of the image). The return value is
	 * the data point looked up in the image custom axes. If no custom axes
	 * are set (via the setAxes(..) method) then you will simply get the 
	 * same double passed back.
	 *  
	 * @param  point in image index coordinates. 
	 * @return point in label coordinates. 
	 * 
	 * @throws Exception if the point could not be transformed or the point type
	 *         is unknown.
	 */
	public double[] getValueAxisLocation(final double... values) throws Exception;

	
	/**
	 * For regions over images: if the axis data set has been set, this method will return 
	 * a point in the coordinates of the image indices rather
	 * than the axes. If no axes are set, then the original point is
	 * returned. If the plot is 1D then the original values are returned.
	 * 
	 *  
	 * @param  point in axis coordinates. 
	 * @return point in image coordinates. 
	 * 
	 * @throws Exception if the point could not be transformed or the point type
	 *         is unknown.
	 */
	public double[] getAxisLocationValue(final double... axisLocation) throws Exception;

	/**
	 * 
	 * @return true if coordinates have been dispose because the region has been removed.
	 */
	public boolean isDisposed();

}
