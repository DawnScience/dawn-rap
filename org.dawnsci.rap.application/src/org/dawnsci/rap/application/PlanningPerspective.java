/*******************************************************************************
 * Copyright (c) 2002, 2012 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Innoopract Informationssysteme GmbH - initial API and implementation
 *     EclipseSource - ongoing development
 ******************************************************************************/

package org.dawnsci.rap.application;

import org.eclipse.ui.*;

public class PlanningPerspective implements IPerspectiveFactory {

  public void createInitialLayout( final IPageLayout layout ) {
    String editorArea = layout.getEditorArea();
    layout.setEditorAreaVisible( true );
    IFolderLayout topLeft = layout.createFolder( "topLeft",
                                                 IPageLayout.LEFT,
                                                 0.25f,
                                                 editorArea );
    topLeft.addView( "org.dawnsci.rap.application.DemoSelectionViewPart" );
    topLeft.addView( "org.dawnsci.rap.application.DemoBrowserViewPart" );
    IFolderLayout bottomLeft = layout.createFolder( "bottomLeft",
                                                    IPageLayout.BOTTOM,
                                                    0.50f,
                                                    "topLeft" );
    bottomLeft.addView( "org.dawnsci.rap.application.DemoTreeViewPart" );
    IFolderLayout right = layout.createFolder( "right",
                                               IPageLayout.RIGHT,
                                               0.70f,
                                               editorArea );
    right.addView( "org.dawnsci.rap.application.DemoTableViewPart" );
    // add shortcuts to show view menu
    layout.addShowViewShortcut( "org.dawnsci.rap.application.DemoChartViewPart" );
    layout.addShowViewShortcut( "org.dawnsci.rap.application.DemoTreeViewPartII" );
    // add shortcut for other perspective
    layout.addPerspectiveShortcut( "org.dawnsci.rap.application.perspective" );
  }
}
