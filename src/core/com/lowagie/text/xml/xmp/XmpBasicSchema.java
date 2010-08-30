/*
 * $Id: XmpBasicSchema.java 3373 2008-05-12 16:21:24Z xlv $
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
public class XmpBasicSchema extends XmpSchema {

	private static final long serialVersionUID = -2416613941622479298L;
	/** default namespace identifier*/
	public static final String DEFAULT_XPATH_ID = "xmp";
	/** default namespace uri*/
	public static final String DEFAULT_XPATH_URI = "http://ns.adobe.com/xap/1.0/";
	
	/** An unordered array specifying properties that were edited outside the authoring application. Each item should contain a single namespace and XPath separated by one ASCII space (U+0020). */
	public static final String ADVISORY = "xmp:Advisory";
	/** The base URL for relative URLs in the document content. If this document contains Internet links, and those links are relative, they are relative to this base URL. This property provides a standard way for embedded relative URLs to be interpreted by tools. Web authoring tools should set the value based on their notion of where URLs will be interpreted. */
	public static final String BASEURL = "xmp:BaseURL";
	/** The date and time the resource was originally created. */
	public static final String CREATEDATE = "xmp:CreateDate";
	/** The name of the first known tool used to create the resource. If history is present in the metadata, this value should be equivalent to that of xmpMM:History's softwareAgent property. */
	public static final String CREATORTOOL = "xmp:CreatorTool";
	/** An unordered array of text strings that unambiguously identify the resource within a given context. */
	public static final String IDENTIFIER = "xmp:Identifier";
	/** The date and time that any metadata for this resource was last changed. */
	public static final String METADATADATE = "xmp:MetadataDate";
	/** The date and time the resource was last modified. */
	public static final String MODIFYDATE = "xmp:ModifyDate";
	/** A short informal name for the resource. */
	public static final String NICKNAME = "xmp:Nickname";
	/** An alternative array of thumbnail images for a file, which can differ in characteristics such as size or image encoding. */
	public static final String THUMBNAILS = "xmp:Thumbnails";

	
	public XmpBasicSchema() {
		super("xmlns:" + DEFAULT_XPATH_ID + "=\"" + DEFAULT_XPATH_URI + "\"");
	}
	
	/**
	 * Adds the creatortool.
	 * @param creator
	 */
	public void addCreatorTool(String creator) {
		setProperty(CREATORTOOL, creator);
	}
	
	/**
	 * Adds the creation date.
	 * @param date
	 */
	public void addCreateDate(String date) {
		setProperty(CREATEDATE, date);
	}
	
	/**
	 * Adds the modification date.
	 * @param date
	 */
	public void addModDate(String date) {
		setProperty(MODIFYDATE, date);
	}

	/**
	 * Adds the meta data date.
	 * @param date
	 */
	public void addMetaDataDate(String date) {
		setProperty(METADATADATE, date);
	}

	/** Adds the identifier.
	 * @param id
	 */
	public void addIdentifiers(String[] id) {
		XmpArray array = new XmpArray(XmpArray.UNORDERED);
		for (int i = 0; i < id.length; i++) {
			array.add(id[i]);
		}
		setProperty(IDENTIFIER, array);
	}

	/** Adds the nickname.
	 * @param name
	 */
	public void addNickname(String name) {
		setProperty(NICKNAME, name);
	}
}
