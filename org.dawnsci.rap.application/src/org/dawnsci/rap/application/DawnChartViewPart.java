/*******************************************************************************
 * Copyright (c) 2012, 2013 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.dawnsci.rap.application;

import java.util.Arrays;

import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.PlotType;
import org.dawnsci.plotting.api.PlottingFactory;
import org.dawnsci.plotting.api.tool.IToolPageSystem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.scisoft.analysis.dataset.AbstractDataset;
import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
import uk.ac.diamond.scisoft.analysis.dataset.Random;


public class DawnChartViewPart extends ViewPart {

	private static final Logger logger = LoggerFactory.getLogger(DawnChartViewPart.class);

	private IPlottingSystem plottingSystem;

	public DawnChartViewPart() {
		try {
			this.plottingSystem = PlottingFactory.createPlottingSystem();
		} catch (Exception e) {
			logger.error("Cannot create a plotting system!", e);
		}
	}

	@Override
	public void createPartControl( Composite parent ) {

		plottingSystem.createPlotPart(parent, "Test Plot", getViewSite().getActionBars(), PlotType.XY, this);
		
		
		final IDataset x = AbstractDataset.arange(100, AbstractDataset.INT32);
		final IDataset image= Random.rand(new int[]{1024,1024});
		plottingSystem.createPlot2D(image, null, null);
		
		
	}

	@Override
	public void setFocus() {
	
	}
	
	@Override
	public void dispose() {
		plottingSystem.dispose();
	}
	
	@Override
	public Object getAdapter(Class clazz) {
		if (clazz == IPlottingSystem.class) return plottingSystem;
		if (clazz == IToolPageSystem.class) return plottingSystem.getAdapter(clazz);
		return super.getAdapter(clazz);
	}
}
