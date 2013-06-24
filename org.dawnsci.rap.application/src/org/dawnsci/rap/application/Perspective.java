/*******************************************************************************
 * Copyright (c) 2007, 2012 Innoopract Informationssysteme GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Innoopract Informationssysteme GmbH - initial API and implementation
 *    EclipseSource - ongoing development
 ******************************************************************************/

package org.dawnsci.rap.application;

import org.eclipse.ui.*;

public class Perspective implements IPerspectiveFactory {

  public void createInitialLayout( final IPageLayout layout ) {
    String editorArea = layout.getEditorArea();
    layout.setEditorAreaVisible( false );
    IFolderLayout topLeft = layout.createFolder( "topLeft",
                                                 IPageLayout.LEFT,
                                                 0.25f,
                                                 editorArea );
    topLeft.addView( "org.dawnsci.rap.application.DawnChartViewPart" );
    topLeft.addView( "org.dawnsci.rap.application.DawnTreeViewPartII" );
    IFolderLayout bottomLeft = layout.createFolder( "bottomLeft",
                                                    IPageLayout.BOTTOM,
                                                    0.50f,
                                                    "topLeft" );
    bottomLeft.addView( "org.dawnsci.rap.application.DawnTreeViewPart" );
    IFolderLayout bottom = layout.createFolder( "bottom",
                                                 IPageLayout.BOTTOM,
                                                 0.60f,
                                                 editorArea );
    bottom.addView( "org.dawnsci.rap.application.DawnTableViewPart" );
    bottom.addView( "org.dawnsci.rap.application.DawnFormViewPart" );
    IFolderLayout topRight = layout.createFolder( "topRight",
                                                  IPageLayout.RIGHT,
                                                  0.70f,
                                                  editorArea );
    topRight.addView( "org.dawnsci.rap.application.DawnSelectionViewPart" );
    topRight.addView( "org.dawnsci.rap.application.DawnBrowserViewPart" );

    // add shortcuts to show view menu
    layout.addShowViewShortcut("org.dawnsci.rap.application.DawnChartViewPart");
    layout.addShowViewShortcut("org.dawnsci.rap.application.DawnTreeViewPartII");

    // add shortcut for other perspective
    layout.addPerspectiveShortcut( "org.dawnsci.rap.application.perspective.planning" );
  }
}
