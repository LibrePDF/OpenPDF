/*
 * $Id: RtfDestinationColorTable.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * Copyright 2007 by Howard Shank (hgshank@yahoo.com)
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
package com.lowagie.text.rtf.parser.destinations;

import java.awt.Color;
import java.util.HashMap;

import com.lowagie.text.rtf.parser.RtfImportMgr;
import com.lowagie.text.rtf.parser.RtfParser;
import com.lowagie.text.rtf.parser.ctrlwords.RtfCtrlWordData;
import com.lowagie.text.rtf.parser.enumerations.RtfColorThemes;

/**
 * <code>RtfDestinationColorTable</code> handles data destined for the color table destination
 * 
 * @author Howard Shank (hgshank@yahoo.com)
 * 
 * @since 2.0.8
 */
public class RtfDestinationColorTable extends RtfDestination  {

	/**
	 * The RtfImportHeader to add color mappings to.
	 */
	private RtfImportMgr importHeader = null;
	/**
	 * The number of the current color being parsed.
	 */
	private int colorNr = 0;
	/**
	 * The red component of the current color being parsed.
	 */
	private int red = -1;
	/**
	 * The green component of the current color being parsed.
	 */
	private int green = -1;
	/**
	 * The blue component of the current color being parsed.
	 */
	private int blue = -1;
	/*
	 * Color themes - Introduced Word 2007
	 */
	/**
	 * Specifies the tint when specifying a theme color.
	 * RTF control word ctint
	 * 
	 * 0 - 255: 0 = full tint(white), 255 = no tint. 
	 * Default value: 255
	 * 
	 * If tint is specified and is less than 255, cshade must equal 255.
	 * ctint/cshade are mutually exclusive
	 * 
	 * @see com.lowagie.text.rtf.parser.destinations.RtfDestinationColorTable#cshade
	 * @see com.lowagie.text.rtf.parser.destinations.RtfDestinationColorTable#themeColor
	 */
	private int ctint = 255;
	/**
	 * Specifies the shade when specifying a theme color.
	 * RTF control word cshade
	 * 
	 * 0 - 255: 0 = full shade(black), 255 = no shade. 
	 * Default value: 255
	 * 
	 * If shade is specified and is less than 255, ctint must equal 255.
	 * cshade/ctint are mutually exclusive
	 * 
	 * @see com.lowagie.text.rtf.parser.destinations.RtfDestinationColorTable#ctint
	 * @see com.lowagie.text.rtf.parser.destinations.RtfDestinationColorTable#themeColor
	 */
	private int cshade = 255;
	/**
	 * Specifies the use of a theme color.
	 * 
	 * @see com.lowagie.text.rtf.parser.enumerations.RtfColorThemes
	 * @see com.lowagie.text.rtf.parser.destinations.RtfDestinationColorTable#ctint
	 * @see com.lowagie.text.rtf.parser.destinations.RtfDestinationColorTable#cshade
	 */
	private int themeColor = RtfColorThemes.THEME_UNDEFINED;
	/**
	 * Color map object for conversions
	 */
	private HashMap colorMap = null;
	
	/**
	 * Constructor.
	 */
	public RtfDestinationColorTable() {
		super(null);
		colorMap = new HashMap();
		this.colorNr = 0;
	}
	
	/**
	 * Constructs a new RtfColorTableParser.
	 * 
	 * @param parser an RtfParser
	 */
	public RtfDestinationColorTable(RtfParser parser) {
		super(parser);
		colorMap = new HashMap();
		this.colorNr = 0;
		this.importHeader = parser.getImportManager();
		this.setToDefaults();
	}
	
	public void setParser(RtfParser parser) {
		this.rtfParser = parser;
		colorMap = new HashMap();
		this.colorNr = 0;
		this.importHeader = parser.getImportManager();
		this.setToDefaults();
	}
	/* (non-Javadoc)
	 * @see com.lowagie.text.rtf.parser.destinations.RtfDestination#handleOpenNewGroup()
	 */
	public boolean handleOpeningSubGroup() {
		return true;
	}

	public boolean closeDestination() {
		return true;
	}

	public boolean handleCloseGroup() {
		processColor();
		return true;
	}
	public boolean handleOpenGroup() {
		return true;
	}
	
	public boolean handleCharacter(int ch) {
		// color elements end with a semicolon (;)
		if(ch == ';') {
			this.processColor();
		}
		return true;
	}
	
	public boolean handleControlWord(RtfCtrlWordData ctrlWordData) {
		if(ctrlWordData.ctrlWord.equals("blue")) this.setBlue(ctrlWordData.intValue());
		if(ctrlWordData.ctrlWord.equals("red")) this.setRed(ctrlWordData.intValue());
		if(ctrlWordData.ctrlWord.equals("green")) this.setGreen(ctrlWordData.intValue());
		if(ctrlWordData.ctrlWord.equals("cshade")) this.setShade(ctrlWordData.intValue());
		if(ctrlWordData.ctrlWord.equals("ctint")) this.setTint(ctrlWordData.intValue());
		//if(ctrlWordData.ctrlWord.equals("cmaindarkone")) this.setThemeColor(ctrlWordData.ctrlWord);
		//if(ctrlWordData.ctrlWord.equals("cmainlightone")) this.setThemeColor(ctrlWordData.ctrlWord);
		//if(ctrlWordData.ctrlWord.equals("cmaindarktwo")) this.setThemeColor(ctrlWordData.ctrlWord);
		//if(ctrlWordData.ctrlWord.equals("cmainlighttwo")) this.setThemeColor(ctrlWordData.ctrlWord);
		//if(ctrlWordData.ctrlWord.equals("caccentone")) this.setThemeColor(ctrlWordData.ctrlWord);
		//if(ctrlWordData.ctrlWord.equals("caccenttwo")) this.setThemeColor(ctrlWordData.ctrlWord);
		//if(ctrlWordData.ctrlWord.equals("caccentthree")) this.setThemeColor(ctrlWordData.ctrlWord);
		//if(ctrlWordData.ctrlWord.equals("caccentfour")) this.setThemeColor(ctrlWordData.ctrlWord);
		//if(ctrlWordData.ctrlWord.equals("caccentfive")) this.setThemeColor(ctrlWordData.ctrlWord);
		//if(ctrlWordData.ctrlWord.equals("caccentsix")) this.setThemeColor(ctrlWordData.ctrlWord);
		//if(ctrlWordData.ctrlWord.equals("chyperlink")) this.setThemeColor(ctrlWordData.ctrlWord);
		//if(ctrlWordData.ctrlWord.equals("cfollowedhyperlink")) this.setThemeColor(ctrlWordData.ctrlWord);
		//if(ctrlWordData.ctrlWord.equals("cbackgroundone")) this.setThemeColor(ctrlWordData.ctrlWord);
		//if(ctrlWordData.ctrlWord.equals("ctextone")) this.setThemeColor(ctrlWordData.ctrlWord);
		//if(ctrlWordData.ctrlWord.equals("cbacgroundtwo")) this.setThemeColor(ctrlWordData.ctrlWord);
		//if(ctrlWordData.ctrlWord.equals("ctexttwo")) this.setThemeColor(ctrlWordData.ctrlWord);
	return true;
	}
	
	/**
	 * Set default values.
	 */
	public void setToDefaults() {
		this.red = -1;
		this.green = -1;
		this.blue = -1;
		this.ctint = 255;
		this.cshade = 255;
		this.themeColor = RtfColorThemes.THEME_UNDEFINED;
		// do not reset colorNr
	}
	/**
	 * Processes the color triplet parsed from the document.
	 * Add it to the import mapping so colors can be mapped when encountered
	 * in the RTF import or conversion.
	 */
	private void processColor() {
		if(red != -1 && green != -1 && blue != -1) {
			if(this.rtfParser.isImport()) {
				this.importHeader.importColor(Integer.toString(this.colorNr), new Color(this.red, this.green, this.blue));
			}
		
			if(this.rtfParser.isConvert()) {
				colorMap.put(Integer.toString(this.colorNr), new Color(this.red, this.green, this.blue));
			}
		}
		this.setToDefaults();
		this.colorNr++;
	}
	/**
	 * Set the red color to value.
	 * @param value Value to set red to.
	 */
	private void setRed(int value) {
		if(value >= 0 && value <= 255) {
			this.red = value;
		}
	}
	/**
	 * Set the green color value.
	 * @param value Value to set green to.
	 */
	private void setGreen(int value) {
		if(value >= 0 && value <= 255) {
			this.green = value;
		}
	}
	/**
	 * Set the blue color value.
	 * @param value Value to set blue to.
	 */
	private void setBlue(int value) {
		if(value >= 0 && value <= 255) {
			this.blue = value;
		}
	}
	/**
	 * Set the tint value
	 * @param value Value to set the tint to
	 * @see com.lowagie.text.rtf.parser.destinations.RtfDestinationColorTable#ctint
	 */
	private void setTint(int value) {
		if(value >= 0 && value <= 255) {
			this.ctint = value;
			if(value >= 0 && value <255) {
				this.cshade = 255;
			}
		}
	}
	/**
	 * Set the shade value
	 * @param value Value to set the shade to
	 * @see com.lowagie.text.rtf.parser.destinations.RtfDestinationColorTable#cshade
	 */
	private void setShade(int value) {
		if(value >= 0 && value <= 255) {
			this.cshade = value;
			if(value >= 0 && value <255) {
				this.ctint = 255;
			}
		}
	}
	/**
	 * Set the theme color value.
	 * @param value Value to set the theme color to
	 * @see com.lowagie.text.rtf.parser.enumerations.RtfColorThemes
	 */
	private void setThemeColor(int value) {
		if(value >= RtfColorThemes.THEME_UNDEFINED && value <= RtfColorThemes.THEME_MAX) {
			this.themeColor = value;
		} else {
			this.themeColor = RtfColorThemes.THEME_UNDEFINED;
		}
	}
	
	// conversion functions
	/**
	 * Get the <code>Color</code> object that is mapped to the key.
	 * @param key The map number.
	 * *@return <code>Color</code> object from the map. null if key does not exist.
	 */
	public Color getColor(String key) {
		return (Color)colorMap.get(key);
	}
	
}
