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

package org.dawnsci.plotting.jreality.overlay.primitives;

import java.awt.Color;

import org.dawnsci.plotting.jreality.overlay.VectorOverlayStyles;

import de.jreality.geometry.IndexedFaceSetFactory;
import de.jreality.math.MatrixBuilder;
import de.jreality.scene.Appearance;
import de.jreality.scene.IndexedFaceSet;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.DefaultPolygonShader;
import de.jreality.shader.ShaderUtility;

/**
 *
 */
public class CirclePrimitive extends OverlayPrimitive {

	private double [][] coords;
	private int [][] faces;
	private int [][] edges;
	private DefaultLineShader dls = null;
	private DefaultGeometryShader dgs = null;
	private DefaultPolygonShader dps = null;
	private VectorOverlayStyles currentStyle = VectorOverlayStyles.FILLED;
    private Color outlineColour;
    private static final int NUMOFSEGMENTS = 72;
    private IndexedFaceSetFactory factory = null;
    private double lineThickness = 1.0;
	
    /**
     * Constructor of a CirclePrimitive
	 * @param comp SceneGraph node this primitive is associated to
	 */
	
	public CirclePrimitive(SceneGraphComponent comp) {
		this(comp,false);
	}
	
    /**
     * Constructor of a CirclePrimitive
	 * @param comp SceneGraph node this primitive is associated to
     * @param isFixed is the size fixed (invariant to zoom) true or false
	 */
	
	public CirclePrimitive(SceneGraphComponent comp, boolean isFixed) {
		super(comp,isFixed);
		ap = new Appearance();
		comp.setAppearance(ap);
		ap.setAttribute(CommonAttributes.POINT_SHADER+ "." + CommonAttributes.SPHERES_DRAW,true);
		ap.setAttribute(CommonAttributes.LINE_SHADER + "." + CommonAttributes.TUBES_DRAW, false);
		ap.setAttribute(CommonAttributes.LIGHTING_ENABLED, false);
		ap.setAttribute("useGLSL", false);
		dgs = 
			ShaderUtility.createDefaultGeometryShader(ap, true);
		 dls = 
			(DefaultLineShader)dgs.createLineShader("default");
		dls.setDiffuseColor(java.awt.Color.WHITE);
		dps = 
			(DefaultPolygonShader)dgs.createPolygonShader("default");
		dps.setDiffuseColor(java.awt.Color.WHITE);
		dgs.setShowFaces(true);
		dgs.setShowLines(false);
		dgs.setShowPoints(false);
		factory = new IndexedFaceSetFactory();
		coords = new double[NUMOFSEGMENTS+1][3];
		faces = new int[NUMOFSEGMENTS][3];
		edges = new int [NUMOFSEGMENTS][2];
		for (int i = 0; i < NUMOFSEGMENTS; i++)
		{
			faces[i][0] = 0;
			faces[i][1] = i+1;
			faces[i][2] = i+2;
			if (faces[i][2] > NUMOFSEGMENTS)
				faces[i][2] = 1;
			edges[i][0] = (i+1);
			edges[i][1] = (i+2);
			if (edges[i][1] > NUMOFSEGMENTS)
				edges[i][1] = 1;
		}
	}

	@Override
	public void setOutlineColour(Color outlineColour) {
		if (this.outlineColour != outlineColour)
		{
			this.outlineColour = outlineColour;
			needToUpdateApp = true;
		}
	}

	@Override
	public void setStyle(VectorOverlayStyles style) {
		if (currentStyle != style) {
			needToUpdateApp = true;
			currentStyle = style;
		}
	}

	/**
	 * Set the circle parameters
	 * @param cx x position of the centre of the circle 
	 * @param cy y position of the centre of the circle
	 * @param radius radius of the circle
	 */
	public void setCircleParameters(double cx, double cy, double radius) {
		coords[0][0] = cx;
		coords[0][1] = cy;
		coords[0][2] = 0.0005;
		for (int i = 0; i < NUMOFSEGMENTS; i++)
		{
			double ex = cx + radius * Math.cos((i * 2.0 * Math.PI) / NUMOFSEGMENTS);
			double ey = cy + radius * Math.sin((i * 2.0 * Math.PI) / NUMOFSEGMENTS);
			coords[i+1][0] = ex;
			coords[i+1][1] = ey;
			coords[i+1][2] = 0.0005;
		}
		needToUpdateGeom = true;
	}
	
	private IndexedFaceSet createCircleGeometry()
	{
		factory.setVertexCount(NUMOFSEGMENTS+1);
		factory.setFaceCount(NUMOFSEGMENTS);
		factory.setEdgeCount(NUMOFSEGMENTS);		
		factory.setVertexCoordinates(coords);
		factory.setFaceIndices(faces);
		factory.setEdgeIndices(edges);
		factory.update();
		return factory.getIndexedFaceSet();
	}
	
	@Override
	public void updateNode() {
		if (needToUpdateTrans) {
			MatrixBuilder.euclidean(transformMatrix).assignTo(comp);
		}
		if (needToUpdateGeom)
		{
			comp.setGeometry(createCircleGeometry());
		}
		if (needToUpdateApp)
		{
			dls.setDiffuseColor(colour);
			dps.setDiffuseColor(colour);
			dls.setLineWidth(lineThickness);
			switch (currentStyle) {
				case FILLED:
				{
					dgs.setShowFaces(true);
					dgs.setShowLines(false);
				}
				break;
				case OUTLINE:
				{
					dgs.setShowFaces(false);
					dgs.setShowLines(true);
				}
				break;
				case FILLED_WITH_OUTLINE:
				{
					dgs.setShowFaces(true);
					dgs.setShowLines(true);
					dls.setDiffuseColor(outlineColour);
				}
				break;
			}
		}
		needToUpdateApp = false;
		needToUpdateGeom = false;
		needToUpdateTrans = false;
		transformMatrix = null;
	}

	@Override
	public void setLineThickness(double thickness) {
		lineThickness = thickness;
		needToUpdateApp = true;
	}

	@Override
	public void setOutlineTransparency(double value) {
		if (value > 0.0f) {
			ap.setAttribute(CommonAttributes.LINE_SHADER+"." + CommonAttributes.TRANSPARENCY_ENABLED, true);
		} else {
			ap.setAttribute(CommonAttributes.LINE_SHADER+"." + CommonAttributes.TRANSPARENCY_ENABLED, false);
		}
		ap.setAttribute(CommonAttributes.LINE_SHADER+"." + CommonAttributes.TRANSPARENCY,value);
		ap.setAttribute(CommonAttributes.ADDITIVE_BLENDING_ENABLED,true);
	}

	@Override
	public void setTransparency(double value) {
		if (value > 0.0f) {
			ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, true);
		} else {
			ap.setAttribute(CommonAttributes.TRANSPARENCY_ENABLED, false);
		}
		ap.setAttribute(CommonAttributes.POLYGON_SHADER+"." + CommonAttributes.TRANSPARENCY,value);
		ap.setAttribute(CommonAttributes.ADDITIVE_BLENDING_ENABLED,true);
	}	

}
