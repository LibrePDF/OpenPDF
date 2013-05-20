/*
 * $Id: PdfSchema.java 3676 2009-02-04 09:50:07Z blowagie $
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

import com.lowagie.text.Document;

/**
 * An implementation of an XmpSchema.
 */
public class PdfSchema extends XmpSchema {

	private static final long serialVersionUID = -1541148669123992185L;
	/** default namespace identifier*/
	public static final String DEFAULT_XPATH_ID = "pdf";
	/** default namespace uri*/
	public static final String DEFAULT_XPATH_URI = "http://ns.adobe.com/pdf/1.3/";
	
	/** Keywords. */
	public static final String KEYWORDS = "pdf:keywords";
	/** The PDF file version (for example: 1.0, 1.3, and so on). */
	public static final String VERSION = "pdf:PDFVersion";
	/** The Producer. */
	public static final String PRODUCER = "pdf:Producer";


	public PdfSchema() {
		super("xmlns:" + DEFAULT_XPATH_ID + "=\"" + DEFAULT_XPATH_URI + "\"");
		addProducer(Document.getVersion());
	}
	
	/**
	 * Adds keywords.
	 * @param keywords
	 */
	public void addKeywords(String keywords) {
		setProperty(KEYWORDS, keywords);
	}
	
	/**
	 * Adds the producer.
	 * @param producer
	 */
	public void addProducer(String producer) {
		setProperty(PRODUCER, producer);
	}

	/**
	 * Adds the version.
	 * @param version
	 */
	public void addVersion(String version) {
		setProperty(VERSION, version);
	}
}
