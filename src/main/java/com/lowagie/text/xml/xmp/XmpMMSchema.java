/*
 * $Id: XmpMMSchema.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * Copyright 2005 by Bruno Lowagie.
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the License.
 *
 * The Original Code is 'iText, a free JAVA-PDF library'.
 *
 * The Initial Developer of the Original Code is Bruno Lowagie. Portions created by
 * the Initial Developer are Copyright (C) 1999-2005 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000-2005 by Paulo Soares. All Rights Reserved.
 *
 * Contributor(s): all the names of the contributors are added in the source code
 * where applicable.
 *
 * Alternatively, the contents of this file may be used under the terms of the
 * LGPL license (the "GNU LIBRARY GENERAL PUBLIC LICENSE"), in which case the
 * provisions of LGPL are applicable instead of those above.  If you wish to
 * allow use of your version of this file only under the terms of the LGPL
 * License and not to allow others to use your version of this file under
 * the MPL, indicate your decision by deleting the provisions above and
 * replace them with the notice and other provisions required by the LGPL.
 * If you do not delete the provisions above, a recipient may use your version
 * of this file under either the MPL or the GNU LIBRARY GENERAL PUBLIC LICENSE 
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the MPL as stated above or under the terms of the GNU
 * Library General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU LIBRARY GENERAL PUBLIC LICENSE for more
 * details.
 *
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * http://www.lowagie.com/iText/
 */

package com.lowagie.text.xml.xmp;

/**
 * An implementation of an XmpSchema.
 */
public class XmpMMSchema extends XmpSchema {

	private static final long serialVersionUID = 1408509596611634862L;
	/** default namespace identifier*/
	public static final String DEFAULT_XPATH_ID = "xmpMM";
	/** default namespace uri*/
	public static final String DEFAULT_XPATH_URI = "http://ns.adobe.com/xap/1.0/mm/";
	

	/** A reference to the original document from which this one is derived. It is a minimal reference; missing components can be assumed to be unchanged. For example, a new version might only need to specify the instance ID and version number of the previous version, or a rendition might only need to specify the instance ID and rendition class of the original. */
	public static final String DERIVEDFROM = "xmpMM:DerivedFrom"; 
	/** The common identifier for all versions and renditions of a document. */
	public static final String DOCUMENTID = "xmpMM:DocumentID";
	/** An ordered array of high-level user actions that resulted in this resource. It is intended to give human readers a general indication of the steps taken to make the changes from the previous version to this one. The list should be at an abstract level; it is not intended to be an exhaustive keystroke or other detailed history. */
	public static final String HISTORY = "xmpMM:History";
	/** A reference to the document as it was prior to becoming managed. It is set when a managed document is introduced to an asset management system that does not currently own it. It may or may not include references to different management systems. */
	public static final String MANAGEDFROM = "xmpMM:ManagedFrom";
	/** The name of the asset management system that manages this resource. */
	public static final String MANAGER = "xmpMM:Manager";
	/** A URI identifying the managed resource to the asset management system; the presence of this property is the formal indication that this resource is managed. The form and content of this URI is private to the asset management system. */
	public static final String MANAGETO = "xmpMM:ManageTo";
	/** A URI that can be used to access information about the managed resource through a web browser. It might require a custom browser plugin. */
	public static final String MANAGEUI = "xmpMM:ManageUI";
	/** Specifies a particular variant of the asset management system. The format of this property is private to the specific asset management system. */
	public static final String MANAGERVARIANT = "xmpMM:ManagerVariant";
	/** The rendition class name for this resource.*/
	public static final String RENDITIONCLASS = "xmpMM:RenditionClass";
	/**  Can be used to provide additional rendition parameters that are too complex or verbose to encode in xmpMM: RenditionClass. */
	public static final String RENDITIONPARAMS = "xmpMM:RenditionParams";
	/** The document version identifier for this resource. */
	public static final String VERSIONID = "xmpMM:VersionID";
	/** The version history associated with this resource.*/
	public static final String VERSIONS = "xmpMM:Versions";
	
	public XmpMMSchema() {
		super("xmlns:" + DEFAULT_XPATH_ID + "=\"" + DEFAULT_XPATH_URI + "\"");
	}
}
