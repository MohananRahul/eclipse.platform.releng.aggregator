/**********************************************************************
 * Copyright (c) 2000,2002 IBM Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.core.resources;

/**
 * A project nature descriptor contains information about a project nature
 * obtained from the plug-in manifest (<code>plugin.xml</code>) file.
 * <p>
 * Nature descriptors are platform-defined objects that exist
 * independent of whether that nature's plug-in has been started. 
 * In contrast, a project nature's runtime object (<code>IProjectNature</code>) 
 * generally runs plug-in-defined code.
 * </p>
 * <p>
 * This interface is not intended to be implemented by clients.
 * </p>
 *
 * @see IProjectNature
 * @see IWorkspace#getNatureDescriptor
 * @since 2.0
 */
public interface IProjectNatureDescriptor {
/**
 * Returns the unique identifer of this nature.
 * <p>
 * The nature identifier is composed of the nature's plug-in id and the simple
 * id of the nature extension.  For example, if plug-in <code>"com.xyz"</code>
 * defines a nature extension with id <code>"myNature"</code>, the unique 
 * nature identifer will be <code>"com.xyz.myNature"</code>.
 * </p>
 * @return the unique nature identifier
 */
public String getNatureId();
/**
 * Returns a displayable label for this nature.
 * Returns the empty string if no label for this nature
 * is specified in the plug-in manifest file.
 * <p> Note that any translation specified in the plug-in manifest
 * file is automatically applied.
 * </p>
 *
 * @return a displayable string label for this nature,
 *    possibly the empty string
 */
public String getLabel();
/**
 * Returns the unique identifiers of the natures required by this nature.
 * Nature requirements are specified by the <code>"requires-nature"</code> 
 * element on a nature extension.
 * Returns an empty array if no natures are required by this nature.
 * 
 * @return an array of nature ids that this nature requires,
 * 	possibly an empty array.
 */
public String[] getRequiredNatureIds();
/**
 * Returns the identifiers of the nature sets that this nature belongs to.
 * Nature set inclusion is specified by the <code>"one-of-nature"</code> 
 * element on a nature extension.
 * Returns an empty array if no nature sets are specified for this nature.
 * 
 * @return an array of nature set ids that this nature belongs to,
 * 	possibly an empty array.
 */
public String[] getNatureSetIds();
/**
 * Returns whether this project nature allows linked resources to be created
 * in projects where this nature is installed.
 *  * @return boolean true if creating links is allowed, and false otherwise.
 * @see IFolder#createLink
 * @see IFile#createLink
 * @since 2.1 */
public boolean isLinkingAllowed();
}
