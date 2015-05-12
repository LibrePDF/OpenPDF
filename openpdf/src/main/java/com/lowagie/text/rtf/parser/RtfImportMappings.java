/*
 * $Id: RtfImportMappings.java 3440 2008-05-25 18:16:48Z howard_s $
 *
 * Copyright 2006 by Mark Hall
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
 * the Initial Developer are Copyright (C) 1999-2006 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000-2006 by Paulo Soares. All Rights Reserved.
 *
 * Contributor(s): all the names of the contributors are added in the source code
 * where applicable.
 *
 * Alternatively, the contents of this file may be used under the terms of the
 * LGPL license (the ?GNU LIBRARY GENERAL PUBLIC LICENSE?), in which case the
 * provisions of LGPL are applicable instead of those above.  If you wish to
 * allow use of your version of this file only under the terms of the LGPL
 * License and not to allow others to use your version of this file under
 * the MPL, indicate your decision by deleting the provisions above and
 * replace them with the notice and other provisions required by the LGPL.
 * If you do not delete the provisions above, a recipient may use your version
 * of this file under either the MPL or the GNU LIBRARY GENERAL PUBLIC LICENSE.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the MPL as stated above or under the terms of the GNU
 * Library General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library general Public License for more
 * details.
 *
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * http://www.lowagie.com/iText/
 */
package com.lowagie.text.rtf.parser;

import java.awt.Color;
import java.util.HashMap;

/**
 * The RtfImportMappings make it possible to define font
 * and color mappings when using the RtfWriter2.importRtfFragment
 * method. This is necessary, because a RTF fragment does not
 * contain font or color information, just references to the
 * font and color tables.<br /><br />
 * 
 * The font mappings are fontNr -&gt; fontName and the color
 * mappigns are colorNr -&gt; Color.
 * 
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 * @author Howard Shank (hgshank@yahoo.com)
 * @since 2.1.0
 */
public class RtfImportMappings {
	/**
	 * The fontNr to fontName mappings.
	 */
	private HashMap fontMappings = null;
	/**
	 * The colorNr to Color mappings.
	 */
	private HashMap colorMappings = null;
	/**
	 * The listNr to List mappings.
	 */
	private HashMap listMappings = null;
	/**
	 * The sytlesheetListNr to Stylesheet mappings.
	 */
	private HashMap stylesheetListMappings = null;
	
	/**
	 * Constructs a new RtfImportMappings initialising the mappings.
	 */
	public RtfImportMappings() {
		this.fontMappings = new HashMap();
		this.colorMappings = new HashMap();
		this.listMappings = new HashMap();
		this.stylesheetListMappings = new HashMap();
	}
	
	/**
	 * Add a font to the list of mappings.
	 * 
	 * @param fontNr The font number.
	 * @param fontName The font name.
	 */
	public void addFont(String fontNr, String fontName) {
		this.fontMappings.put(fontNr, fontName);
	}
	/**
	 * Add a color to the list of mappings.
	 * 
	 * @param colorNr The color number.
	 * @param color The Color.
	 */
	public void addColor(String colorNr, Color color) {
		this.colorMappings.put(colorNr, color);
	}
	/**
	 * Add a List to the list of mappings.
	 * 
	 * @param listNr The List number.
	 * @param list The List.
	 */
	public void addList(String listNr, String list) {
		this.listMappings.put(listNr, list);
	}
	/**
	 * Add a Stylesheet List to the list of mappings.
	 * 
	 * @param stylesheetListNr The Stylesheet List number.
	 * @param list The StylesheetList.
	 */
	public void addStylesheetList(String stylesheetListNr, String list) {
		this.stylesheetListMappings.put(stylesheetListNr, list);
	}	
	
	/**
	 * Gets the list of font mappings. String to String.
	 * 
	 * @return The font mappings.
	 */
	public HashMap getFontMappings() {
		return this.fontMappings;
	}
	
	/**
	 * Gets the list of color mappings. String to Color.
	 * 
	 * @return The color mappings.
	 */
	public HashMap getColorMappings() {
		return this.colorMappings;
	}	
	
	/**
	 * Gets the list of List mappings.
	 * 
	 * @return The List mappings.
	 */
	public HashMap getListMappings() {
		return this.listMappings;
	}	
	
	/**
	 * Gets the list of Stylesheet mappings. .
	 * 
	 * @return The Stylesheet List mappings.
	 */
	public HashMap getStylesheetListMappings() {
		return this.stylesheetListMappings;
	}
}
