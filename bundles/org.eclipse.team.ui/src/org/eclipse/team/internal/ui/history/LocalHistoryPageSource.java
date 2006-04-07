/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.team.internal.ui.history;

import org.eclipse.core.resources.IResource;
import org.eclipse.team.ui.history.HistoryPageSource;
import org.eclipse.ui.part.Page;

public class LocalHistoryPageSource extends HistoryPageSource {

	public boolean canShowHistoryFor(Object object) {
		return (object instanceof IResource && ((IResource) object).getType() == IResource.FILE);
	}

	public Page createPage(Object object) {
		LocalHistoryPage page = new LocalHistoryPage();
		return page;
	}

}
