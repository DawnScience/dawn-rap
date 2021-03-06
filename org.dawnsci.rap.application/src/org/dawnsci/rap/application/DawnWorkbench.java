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

import org.dawnsci.rap.application.presentation.DemoPresentationWorkbenchAdvisor;
import org.eclipse.rap.rwt.application.EntryPoint;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPreferenceConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.application.WorkbenchAdvisor;
import org.eclipse.ui.internal.util.PrefUtil;
import org.eclipse.ui.preferences.ScopedPreferenceStore;


public class DawnWorkbench implements EntryPoint {

  private static final String DEMO_PRESENTATION = "org.dawnsci.rap.application.presentation";

  public int createUI() {
    ScopedPreferenceStore prefStore = ( ScopedPreferenceStore )PrefUtil.getAPIPreferenceStore();
    String keyPresentationId = IWorkbenchPreferenceConstants.PRESENTATION_FACTORY_ID;
    String presentationId = prefStore.getString( keyPresentationId );

    WorkbenchAdvisor worbenchAdvisor = new DawnWorkbenchAdvisor();
    if( DEMO_PRESENTATION.equals( presentationId ) ) {
      worbenchAdvisor = new DemoPresentationWorkbenchAdvisor();
    }

    Display display = PlatformUI.createDisplay();
    int result = PlatformUI.createAndRunWorkbench( display, worbenchAdvisor );
    display.dispose();
    return result;
  }
}
