/*******************************************************************************
 * Copyright (c) 2012 EclipseSource and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/
package org.dawnsci.rap.application;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;


public class DawnApplication implements IApplication {

  public Object start( IApplicationContext context ) throws Exception {
    new DawnWorkbench().createUI();
    return new Integer( 0 );
  }

  public void stop() {
  }
}
