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

import org.eclipse.ui.application.*;


public class DawnWorkbenchAdvisor extends WorkbenchAdvisor {

  public void initialize( IWorkbenchConfigurer configurer ) {
    getWorkbenchConfigurer().setSaveAndRestore( true );
    super.initialize( configurer );
  }

  public String getInitialWindowPerspectiveId() {
    return "org.dawnsci.rap.application.perspective";
  }
  
  public WorkbenchWindowAdvisor createWorkbenchWindowAdvisor(
    final IWorkbenchWindowConfigurer windowConfigurer )
  {
    return new DawnWorkbenchWindowAdvisor( windowConfigurer );
  }
}
