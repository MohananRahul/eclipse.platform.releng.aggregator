package org.eclipse.update.ui.forms.internal;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.*;
import org.eclipse.ui.part.*;
import org.eclipse.swt.custom.*;

public class NoTabsWorkbook implements IFormWorkbook {
	private Vector pages;
	private boolean firstPageSelected = true;
	private PageBook pageBook;
	private Vector listeners = new Vector();
	private IFormPage currentPage;

	public NoTabsWorkbook() {
		pages = new Vector();
	}
	public void addFormSelectionListener(IFormSelectionListener listener) {
		listeners.addElement(listener);
	}
	public void addPage(IFormPage page) {
		pages.add(page);

		if (firstPageSelected && currentPage == null)
			selectPage(page, true);
	}

	public void createControl(Composite parent) {
		pageBook = new PageBook(parent, SWT.NULL);
	}

	private void fireSelectionChanged(IFormPage page, boolean setFocus) {
		for (Iterator iter = listeners.iterator(); iter.hasNext();) {
			IFormSelectionListener listener = (IFormSelectionListener) iter.next();
			listener.formSelected(page, setFocus);
		}
	}

	public Control getControl() {
		return pageBook;
	}

	public IFormPage getCurrentPage() {
		return currentPage;
	}

	public boolean isFirstPageSelected() {
		return firstPageSelected;
	}
	public void removeFormSelectionListener(IFormSelectionListener listener) {
		listeners.removeElement(listener);
	}

	public void removePage(IFormPage page) {
		if (pages.contains(page)) {
			Control c = page.getControl();
			if (c!=null && !c.isDisposed())
				c.dispose();
			pages.remove(page);
		}
	}

	public void selectPage(final IFormPage page, final boolean setFocus) {
		final IFormPage oldPage = currentPage;
		currentPage = page;

		if (pageBook != null) {

			// It may take a while
			BusyIndicator.showWhile(pageBook.getDisplay(), new Runnable() {
				public void run() {
					switchPages(oldPage, page, setFocus);
				}
			});
		}
	}

	public void setFirstPageSelected(boolean newFirstPageSelected) {
		firstPageSelected = newFirstPageSelected;
	}

	private void switchPages(IFormPage oldPage, IFormPage newPage, boolean setFocus) {
		if (oldPage != null && oldPage != newPage) {
			oldPage.becomesInvisible(newPage);
		}
		if (newPage.getControl() == null)
			newPage.createControl(pageBook);
		pageBook.showPage(newPage.getControl());

		newPage.becomesVisible(oldPage);
		fireSelectionChanged(newPage, setFocus);
	}
}