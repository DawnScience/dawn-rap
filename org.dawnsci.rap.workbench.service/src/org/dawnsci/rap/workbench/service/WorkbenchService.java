package org.dawnsci.rap.workbench.service;

import org.dawb.common.services.IWorkbenchService;
import org.eclipse.ui.PlatformUI;

/**
 * RAP Implementation because hard dependency on RAP ui in this plugin.
 * @author fcp94556
 *
 */
public class WorkbenchService implements IWorkbenchService {

	@Override
	public boolean isWorkbenchRunning() {
		return PlatformUI.isWorkbenchRunning();
	}

	@Override
	public Object getService(Class<?> clazz) {
		return PlatformUI.getWorkbench().getService(clazz);
	}

}
