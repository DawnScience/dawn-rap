/*******************************************************************************
 * Copyright (c) 2002, 2012 Innoopract Informationssysteme GmbH and others.
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

import org.dawnsci.rap.application.DawnTreeViewPart.TreeObject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.*;
import org.eclipse.ui.part.ViewPart;

public class DawnBrowserViewPart extends ViewPart {

  Browser browser;
  private static String BIRT_DEMO
                 = "http://www.eclipse.org/birt/phoenix/examples/solution/TopSellingProducts.html";


  public void createPartControl( final Composite parent ) {
    browser = new Browser( parent, SWT.NONE );
    IWorkbench workbench = PlatformUI.getWorkbench();
    IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
    ISelection selection = window.getSelectionService().getSelection();
    setUrlFromSelection( selection );
    createSelectionListener();
  }

  public void setFocus() {
    browser.setFocus();
  }

  private void createSelectionListener() {
    IWorkbench workbench = PlatformUI.getWorkbench();
    IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
    ISelectionService selectionService = window.getSelectionService();
    selectionService.addSelectionListener( new ISelectionListener() {

      public void selectionChanged( final IWorkbenchPart part,
                                    final ISelection selection )
      {
        setUrlFromSelection( selection );
      }
    } );
  }

  private void setUrlFromSelection( final ISelection selection ) {
    if( !browser.isDisposed() ) {
      browser.setUrl( BIRT_DEMO );
      if( selection != null ) {
        IStructuredSelection sselection = ( IStructuredSelection )selection;
        Object firstElement = sselection.getFirstElement();
        if( firstElement instanceof TreeObject ) {
          String location = ( ( TreeObject )firstElement ).getLocation();
          if( location != null ) {
            browser.setUrl( location );
          }
        }
      }
    }
  }
}
