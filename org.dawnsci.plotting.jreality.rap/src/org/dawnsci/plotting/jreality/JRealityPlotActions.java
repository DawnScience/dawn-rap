package org.dawnsci.plotting.jreality;

import java.io.File;
import java.lang.reflect.Method;

import org.dawnsci.plotting.api.ActionType;
import org.dawnsci.plotting.api.IPlotActionSystem;
import org.dawnsci.plotting.api.IPlottingSystem;
import org.dawnsci.plotting.api.ManagerType;
import org.dawnsci.plotting.api.tool.IToolPage.ToolPageRole;
import org.dawnsci.plotting.jreality.impl.SurfPlotStyles;
import org.dawnsci.plotting.print.PrintSettings;
import org.dawnsci.plotting.util.CheckableActionGroup;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class JRealityPlotActions {
	
	private static final Logger logger = LoggerFactory.getLogger(JRealityPlotActions.class);

	private JRealityPlotViewer        plotter;
	private IPlottingSystem           system;
	private IPlotActionSystem         actionMan;

	public JRealityPlotActions(JRealityPlotViewer jRealityPlotViewer,
			                   IPlottingSystem    system) {
		this.plotter            = jRealityPlotViewer;
		this.system             = system;
		this.actionMan          = system.getPlotActionSystem();
	}

	public void createActions() {
				
		// Tools
 		actionMan.createToolDimensionalActions(ToolPageRole.ROLE_3D, "org.dawb.workbench.plotting.views.toolPageView.3D");

		// Configure
        createGridLineActions();

        // Print/export
        createExportActions();
 		
 		// Others
        createSurfaceModeActions();
	}

	private void createSurfaceModeActions() {
		CheckableActionGroup surfaceModeGroup = new CheckableActionGroup();
		actionMan.registerGroup("jreality.plotting.surface.mode.actions", ManagerType.MENUBAR);
		Action displayFilled = new Action("Filled", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				plotter.setPlot2DSurfStyle(SurfPlotStyles.FILLED);
				plotter.refresh(false);
				setChecked(true);
			}
		};
		displayFilled.setText("Filled mode");
		displayFilled.setDescription("Render the graph in filled mode");
		displayFilled.setImageDescriptor(Activator.getImageDescriptor("icons/save.gif"));
		displayFilled.setChecked(true);
		surfaceModeGroup.add(displayFilled);
		actionMan.registerAction("jreality.plotting.surface.mode.actions", displayFilled, ActionType.SURFACE, ManagerType.MENUBAR);

		Action displayWireframe = new Action("Wireframe", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				plotter.setPlot2DSurfStyle(SurfPlotStyles.WIREFRAME);
				SurfPlotStyles.values();
				plotter.refresh(false);
				setChecked(true);
			}
		};
		displayWireframe.setText("Wireframe mode");
		displayWireframe.setDescription("Render the graph in wireframe mode");
		surfaceModeGroup.add(displayWireframe);
		actionMan.registerAction("jreality.plotting.surface.mode.actions", displayWireframe, ActionType.SURFACE, ManagerType.MENUBAR);

		Action displayLinegraph = new Action("LineGraph", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				plotter.setPlot2DSurfStyle(SurfPlotStyles.LINEGRAPH);
				plotter.refresh(true);
				setChecked(true);
			}
		};
		displayLinegraph.setText("Linegraph mode");
		displayLinegraph.setDescription("Render the graph in linegraph mode");
		surfaceModeGroup.add(displayLinegraph);
		actionMan.registerAction("jreality.plotting.surface.mode.actions", displayLinegraph, ActionType.SURFACE, ManagerType.MENUBAR);

		Action displayPoint = new Action("Point", IAction.AS_CHECK_BOX) {
			@Override
			public void run() {
				plotter.setPlot2DSurfStyle(SurfPlotStyles.POINTS);
				plotter.refresh(false);	
				setChecked(true);
			}
		};
		displayPoint.setText("Point mode");
		displayPoint.setDescription("Render the graph in dot mode");
		surfaceModeGroup.add(displayPoint);
		actionMan.registerAction("jreality.plotting.surface.mode.actions", displayPoint, ActionType.SURFACE, ManagerType.MENUBAR);
	}

	private void createExportActions() {

		actionMan.registerGroup("jreality.plotting.export.actions", ManagerType.TOOLBAR);

		Action saveGraph = new Action("Save graph") {

			// Cache file name otherwise they have to keep
			// choosing the folder.
			private String filename;

			@Override
			public void run() {
				String [] filterExtensions = new String [] {"*.jpg;*.JPG;*.jpeg;*.JPEG;*.png;*.PNG", "*.ps;*.eps","*.svg;*.SVG"};
				filename = getInternalImageSavePath(filterExtensions);
				if (filename == null) return;

				//saveGraph(filename, PlotExportUtil.FILE_TYPES[0]);
			}
		};
		saveGraph.setImageDescriptor(Activator.getImageDescriptor("icons/save.gif"));
		actionMan.registerAction("jreality.plotting.export.actions", saveGraph, ActionType.THREED, ManagerType.TOOLBAR);

		Action copyGraph = new Action("Copy plot to clipboard.") {
			@Override
			public void run() {
				copyGraph();
			}
		};
		copyGraph.setImageDescriptor(Activator.getImageDescriptor("icons/copy.gif"));
		actionMan.registerAction("jreality.plotting.export.actions", copyGraph, ActionType.THREED, ManagerType.TOOLBAR);

		Action printGraph = new Action("Print current plot") {
			@Override
			public void run() {
				printGraph();
			}
		};
		printGraph.setImageDescriptor(Activator.getImageDescriptor("icons/print.png"));
		actionMan.registerAction("jreality.plotting.export.actions", printGraph, ActionType.THREED, ManagerType.TOOLBAR);

	}
	
	protected String getInternalImageSavePath(String[] filterExtensions) {
		
		try { // Swt use reflection
			Class clazz = getClass().getClassLoader().loadClass("org.eclipse.swt.widgets.FileDialog");
			
			Object dialog = clazz.getConstructor(Shell.class, int.class).newInstance(Display.getDefault().getShells()[0], SWT.SAVE);
			
			Method setFilterNamesMethod = clazz.getMethod("setFilterNames", String[].class);
			setFilterNamesMethod.invoke(dialog, new String[] { "PNG Files", "All Files (*.*)" });
			
			
			if (filterExtensions==null) filterExtensions = new String[] { "*.png", "*.*" };
			Method setFilterExtensionsMethod = clazz.getMethod("setFilterExtensions", String[].class);
			setFilterExtensionsMethod.invoke(dialog, filterExtensions);
			
			Method openMethod = clazz.getMethod("open");
			String path = (String)openMethod.invoke(dialog);
			return path;
			
		} catch (Throwable ne) {
			throw new RuntimeException(ne.getMessage(), ne);
		}
	}

	
	private PrinterData   defaultPrinterData;
	private PrintSettings settings;
	
	private void printGraph() {
		try {
            plotter.setExporting(true);
//            final PlottingMode currentMode = plotter.getPlottingMode();
//			if (currentMode == PlottingMode.ONED || currentMode == PlottingMode.ONED_THREED
//					|| currentMode == PlottingMode.SCATTER2D) {
//				if(defaultPrinterData==null)
//					defaultPrinterData=Printer.getDefaultPrinterData();
//				if (settings==null) settings = new PrintSettings();
//				JRealityPrintDialog dialog = new JRealityPrintDialog(plotter.getViewer(), plotter.getControl().getDisplay(),plotter.getGraphTable(), settings);
//				settings=dialog.open();
//			} else{
//				if(defaultPrinterData==null)
//					defaultPrinterData=Printer.getDefaultPrinterData();
//				if (settings==null) settings = new PrintSettings();
//				JRealityPrintDialog dialog = new JRealityPrintDialog(plotter.getViewer(), plotter.getControl().getDisplay(),null, settings);
//				settings=dialog.open();
//			}
		} finally {
			plotter.setExporting(false);
		}

	}

	
	/**
	 * Save the graph with the given filename. If the file name ends with a known extension, this is used as the file
	 * type otherwise it is the string passed in which is read from the save as dialog form normally.
	 * 
	 * @param filename
	 *            the name under which the graph should be saved
	 * @param fileType
	 *            type of the file
	 */

	private synchronized void saveGraph(String filename, String fileType) {
		
		try {
            plotter.setExporting(true);

            // Can't reach file permissions error message in JReality. Checking explicitly here. 
            File p = new File(filename).getParentFile();
            if (!p.canWrite()) {
            	String msg = "Saving failed: no permission to write in directory: " + p.getAbsolutePath();
            	logger.error(msg);
            	Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, msg); 
            	ErrorDialog.openError(Display.getDefault().getActiveShell(), "Image export error", "Error saving image file", status);
            	return;
            }

            try {
            	//PlotExportUtil.saveGraph(filename, fileType, plotter.getViewer());
            } catch (Exception e) {
            	logger.error("writing graph file failed", e);
            	Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e); 
            	ErrorDialog.openError(Display.getDefault().getActiveShell(), "Image export error", "Error saving image file", status);
            }
		} finally {
			plotter.setExporting(false);
		}
	}

	private synchronized void copyGraph() {
		try {
			//PlotExportUtil.copyGraph(plotter.getViewer());
		} catch (Exception e) {
			logger.error(e.getCause().getMessage(), e);
			Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e); 
			ErrorDialog.openError(Display.getDefault().getActiveShell(), "Image copy error", "Error copying image to clipboard", status);
		}
	}

	private Action boundingBox, xCoordGrid, yCoordGrid, zCoordGrid;

	private void createGridLineActions() {
		
		actionMan.registerGroup("jreality.plotting.grid.line.actions", ManagerType.TOOLBAR);
		
		
		Action reset = new Action("Reset orientation and zoom",IAction.AS_PUSH_BUTTON) {
			@Override
			public void run()
			{
				plotter.resetView();
				plotter.refresh(false);
			}
		};

		reset.setImageDescriptor(Activator.getImageDescriptor("icons/axis.png"));
		actionMan.registerAction("jreality.plotting.grid.line.actions", reset, ActionType.THREED, ManagerType.TOOLBAR);

		boundingBox = new Action("Toggle bounding box",IAction.AS_CHECK_BOX) {
			@Override
			public void run()
			{
				plotter.setBoundingBoxEnabled(boundingBox.isChecked());
				plotter.refresh(false);
			}
		};

		boundingBox.setChecked(true);
		boundingBox.setImageDescriptor(Activator.getImageDescriptor("icons/box.png"));
		actionMan.registerAction("jreality.plotting.grid.line.actions", boundingBox, ActionType.THREED, ManagerType.TOOLBAR);
	
		
		// Configure
		xCoordGrid = new Action("",IAction.AS_CHECK_BOX)
		{
			@Override
			public void run()
			{
				plotter.setTickGridLines(xCoordGrid.isChecked(), yCoordGrid.isChecked(),zCoordGrid.isChecked());
			}
		};
		xCoordGrid.setChecked(true);
		xCoordGrid.setText("X grid lines");
		xCoordGrid.setToolTipText("Toggle x axis grid lines");
		xCoordGrid.setImageDescriptor(Activator.getImageDescriptor("icons/xgrid.png"));
		actionMan.registerAction("jreality.plotting.grid.line.actions", xCoordGrid, ActionType.THREED, ManagerType.TOOLBAR);
		
		yCoordGrid = new Action("",IAction.AS_CHECK_BOX)
		{
			@Override
			public void run()
			{
				plotter.setTickGridLines(xCoordGrid.isChecked(), yCoordGrid.isChecked(),zCoordGrid.isChecked());
			}
		};
		yCoordGrid.setChecked(true);
		yCoordGrid.setText("Y grid lines");
		yCoordGrid.setToolTipText("Toggle y axis grid line");
		yCoordGrid.setImageDescriptor(Activator.getImageDescriptor("icons/ygrid.png"));		
		actionMan.registerAction("jreality.plotting.grid.line.actions", yCoordGrid, ActionType.THREED, ManagerType.TOOLBAR);

		zCoordGrid = new Action("",IAction.AS_CHECK_BOX)
		{
			@Override
			public void run()
			{
				plotter.setTickGridLines(xCoordGrid.isChecked(), yCoordGrid.isChecked(),zCoordGrid.isChecked());
				
			}
		};
		zCoordGrid.setChecked(true);
		zCoordGrid.setText("Z grid lines");
		zCoordGrid.setToolTipText("Toggle z axis grid lines");
		zCoordGrid.setImageDescriptor(Activator.getImageDescriptor("icons/zgrid.png"));
		actionMan.registerAction("jreality.plotting.grid.line.actions", zCoordGrid, ActionType.THREED, ManagerType.TOOLBAR);
		
	}

	public void dispose() {
		plotter            = null;
		system             = null;
	}

	public IPlottingSystem getPlottingSystem(){
		return system;
	}

}
