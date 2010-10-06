/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.e4.core.internal.tests.contexts.inject;

import javax.inject.Inject;
import javax.inject.Named;

import junit.framework.TestCase;

import org.eclipse.e4.core.contexts.ContextFunction;
import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.EclipseContextFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Optional;

public class ContextFunctionDynamicsTest extends TestCase {

	private static final String SELECTION = "selection"; //$NON-NLS-1$

	static class InjectTarget {
		Object input;

		@Inject
		@Optional
		void setInput(@Named(SELECTION) Object input) {
			this.input = input;
		}
	}
	
	/**
	 * Changing context function should update injected values
	 */
	public void testChangeICF() throws Exception {
		IEclipseContext context1 = EclipseContextFactory.create("context1");
		IEclipseContext context2 = context1.createChild("context2");

		context1.set(SELECTION, new ContextFunction() {
			@Override
			public Object compute(IEclipseContext context) {
				return "func1";
			}
		});

		InjectTarget target = new InjectTarget();
		ContextInjectionFactory.inject(target, context2);

		assertEquals("func1", target.input);
		
		context1.set(SELECTION, new ContextFunction() {
			@Override
			public Object compute(IEclipseContext context) {
				return "func2";
			}
		});
		
		assertEquals("func2", target.input);
	}
	
	/**
	 * Overriding context function with a regular value on a child node
	 */
	public void testOverrideICF() throws Exception {
		IEclipseContext context1 = EclipseContextFactory.create("context1");
		IEclipseContext context2 = context1.createChild("context2");
		IEclipseContext context3 = context2.createChild("context3");

		context1.set(SELECTION, new ContextFunction() {
			@Override
			public Object compute(IEclipseContext context) {
				return "func1";
			}
		});

		InjectTarget target = new InjectTarget();
		ContextInjectionFactory.inject(target, context3);
		
		assertEquals("func1", target.input);

		Object o = new Object();
		context2.set(SELECTION, o);

		assertEquals(o, target.input);
	}

	/**
	 * Tests updates in a chain of 4 contexts
	 */
	public void testLongChain() throws Exception {
		IEclipseContext context1 = EclipseContextFactory.create("context1");
		IEclipseContext context2 = context1.createChild("context2");
		IEclipseContext context3 = context2.createChild("context3");
		IEclipseContext context4 = context3.createChild("context4");

		// ICF set on top context
		context1.set(SELECTION, new ContextFunction() {
			@Override
			public Object compute(IEclipseContext context) {
				return "func1";
			}
		});

		InjectTarget target = new InjectTarget();
		ContextInjectionFactory.inject(target, context4);

		assertEquals("func1", target.input);
		
		// Override ICF set on the 2nd context
		context2.set(SELECTION, new ContextFunction() {
			@Override
			public Object compute(IEclipseContext context) {
				return "func2";
			}
		});
		
		assertEquals("func2", target.input);
		
		// Override removed
		context2.remove(SELECTION);
		assertEquals("func1", target.input);
		
		// Override simple value set on 3rd context
		context3.set(SELECTION, "abc");
		assertEquals("abc", target.input);
		
		// Simple value override removed from 3rd context
		context3.remove(SELECTION);
		assertEquals("func1", target.input);
	}
	
	
	
	public void testBug315109() throws Exception {
		IEclipseContext appContext = EclipseContextFactory.create();
		IEclipseContext windowContext = appContext.createChild();
		IEclipseContext partContext = windowContext.createChild();

		partContext.activateBranch();

		appContext.set(SELECTION, new ContextFunction() {
			@Override
			public Object compute(IEclipseContext context) {
				IEclipseContext parent = context.getParent();
				while (parent != null) {
					context = parent;
					parent = context.getParent();
				}
				return context.getActiveLeaf().get("out.selection");
			}
		});

		InjectTarget target = new InjectTarget();
		ContextInjectionFactory.inject(target, partContext);

		assertNull("No selection has been set, should be null", target.input); //$NON-NLS-1$

		Object o = new Object();
		windowContext.set(SELECTION, o);

		assertEquals(
				"A selection was set into the window, should have been injected into the part", //$NON-NLS-1$
				o, target.input);
	}
}
