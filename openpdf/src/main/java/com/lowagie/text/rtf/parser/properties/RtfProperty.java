/* $Id: RtfProperty.java 3373 2008-05-12 16:21:24Z xlv $
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
package com.lowagie.text.rtf.parser.properties;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.lowagie.text.rtf.parser.ctrlwords.RtfCtrlWordData;

/**
 * <code>RtfProperty</code> handles document, paragraph, etc. property values
 * 
 * @author Howard Shank (hgshank@yahoo.com)
 * @since 2.0.8
 */
public class RtfProperty {
	public static final int OFF = 0;
	public static final int ON = 1;
	
	/* property groups */
	public static final String COLOR = "color.";
	public static final String CHARACTER = "character.";
	public static final String PARAGRAPH = "paragraph.";
	public static final String SECTION = "section.";
	public static final String DOCUMENT = "document.";
	
	/* color properties */
	public static final String COLOR_FG = COLOR + "fg"; //Color Object
	public static final String COLOR_BG = COLOR + "bg"; //Color Object
	
	/* character properties */
	public static final String CHARACTER_BOLD = CHARACTER + "bold";	
	public static final String CHARACTER_UNDERLINE = CHARACTER + "underline";
	public static final String CHARACTER_ITALIC = CHARACTER + "italic";
	public static final String CHARACTER_SIZE = CHARACTER + "size"; 
	public static final String CHARACTER_FONT = CHARACTER + "font";
	public static final String CHARACTER_STYLE = CHARACTER + "style";

	/* paragraph properties */
	/** Justify left */
	public static final int JUSTIFY_LEFT = 0;
	/** Justify right */
	public static final int JUSTIFY_RIGHT = 1;
	/** Justify center */
	public static final int JUSTIFY_CENTER = 2;
	/** Justify full */
	public static final int JUSTIFY_FULL = 3;
	
	public static final String PARAGRAPH_INDENT_LEFT = PARAGRAPH + "indentLeft";	//  twips
	public static final String PARAGRAPH_INDENT_RIGHT = PARAGRAPH + "indentRight";	// twips
	public static final String PARAGRAPH_INDENT_FIRST_LINE = PARAGRAPH + "indentFirstLine";	// twips
	public static final String PARAGRAPH_JUSTIFICATION = PARAGRAPH + "justification";
	
	public static final String PARAGRAPH_BORDER = PARAGRAPH + "border";
	public static final String PARAGRAPH_BORDER_CELL = PARAGRAPH + "borderCell";
	
	/** possible border settting */
	public static final int PARAGRAPH_BORDER_NIL = 0;
	/** possible border settting */
	public static final int PARAGRAPH_BORDER_BOTTOM = 1;
	/** possible border settting */
	public static final int PARAGRAPH_BORDER_TOP = 2;
	/** possible border settting */
	public static final int PARAGRAPH_BORDER_LEFT = 4;
	/** possible border settting */
	public static final int PARAGRAPH_BORDER_RIGHT = 8;
	/** possible border settting */
	public static final int PARAGRAPH_BORDER_DIAGONAL_UL_LR = 16;
	/** possible border settting */
	public static final int PARAGRAPH_BORDER_DIAGONAL_UR_LL = 32;
	/** possible border settting */
	public static final int PARAGRAPH_BORDER_TABLE_HORIZONTAL = 64;
	/** possible border settting */
	public static final int PARAGRAPH_BORDER_TABLE_VERTICAL = 128;
	
	/* section properties */
	/** Decimal number format */
	public static final int PGN_DECIMAL = 0; 
	/** Uppercase Roman Numeral */
	public static final int PGN_ROMAN_NUMERAL_UPPERCASE = 1;
	/** Lowercase Roman Numeral */
	public static final int PGN_ROMAN_NUMERAL_LOWERCASE = 2;
	/** Uppercase Letter */
	public static final int PGN_LETTER_UPPERCASE = 3;
	/** Lowercase Letter */
	public static final int PGN_LETTER_LOWERCASE = 4;
	/** Section Break None */
	public static final int SBK_NONE = 0;
	/** Section Break Column break */
	public static final int SBK_COLUMN = 1;
	/** Section Break Even page break */
	public static final int SBK_EVEN = 2;
	/** Section Break Odd page break */
	public static final int SBK_ODD = 3;
	/** Section Break Page break */
	public static final int SBK_PAGE = 4;
	
	public static final String SECTION_NUMBER_OF_COLUMNS =  SECTION + "numberOfColumns";
	public static final String SECTION_BREAK_TYPE = SECTION + "SectionBreakType";
	public static final String SECTION_PAGE_NUMBER_POSITION_X = SECTION + "pageNumberPositionX";
	public static final String SECTION_PAGE_NUMBER_POSITION_Y = SECTION + "pageNumberPositionY";
	public static final String SECTION_PAGE_NUMBER_FORMAT = SECTION + "pageNumberFormat";
	
	/* document properties */
	/** Portrait orientation */
	public static final String PAGE_PORTRAIT = "0";
	/** Landscape orientation */
	public static final String PAGE_LANDSCAPE = "1";
	
	public static final String DOCUMENT_PAGE_WIDTH_TWIPS = DOCUMENT + "pageWidthTwips";
	public static final String DOCUMENT_PAGE_HEIGHT_TWIPS = DOCUMENT + "pageHeightTwips";
	public static final String DOCUMENT_MARGIN_LEFT_TWIPS = DOCUMENT + "marginLeftTwips";
	public static final String DOCUMENT_MARGIN_TOP_TWIPS = DOCUMENT + "marginTopTwips";
	public static final String DOCUMENT_MARGIN_RIGHT_TWIPS = DOCUMENT + "marginRightTwips";
	public static final String DOCUMENT_MARGIN_BOTTOM_TWIPS = DOCUMENT + "marginBottomTwips";
	public static final String DOCUMENT_PAGE_NUMBER_START = DOCUMENT + "pageNumberStart";
	public static final String DOCUMENT_ENABLE_FACING_PAGES = DOCUMENT + "enableFacingPages";
	public static final String DOCUMENT_PAGE_ORIENTATION = DOCUMENT + "pageOrientation";
	public static final String DOCUMENT_DEFAULT_FONT_NUMER = DOCUMENT + "defaultFontNumber";
	
	/** Properties for this RtfProperty object */
	protected HashMap properties = new HashMap();
	
	private boolean modifiedCharacter = false; 
	private boolean modifiedParagraph = false; 
	private boolean modifiedSection = false; 
	private boolean modifiedDocument = false; 

	
	/** The <code>RtfPropertyListener</code>. */
    private ArrayList listeners = new ArrayList();
	/**
	 * Set all property objects to default values.
	 * @since 2.0.8
	 */
	public void setToDefault() {
		setToDefault(COLOR);
		setToDefault(CHARACTER);
		setToDefault(PARAGRAPH);
		setToDefault(SECTION);
		setToDefault(DOCUMENT);
	}
	/**
	 * Set individual property group to default values.
	 * @param propertyGroup <code>String</code> name of the property group to set to default.
	 * @since 2.0.8
	 */
	public void setToDefault(String propertyGroup) {
		if(COLOR.equals(propertyGroup)) {
			setProperty(COLOR_FG, new Color(0,0,0));
			setProperty(COLOR_BG, new Color(255,255,255));
			return;
		}
		if(CHARACTER.equals(propertyGroup)) {
			setProperty(CHARACTER_BOLD, 0);
			setProperty(CHARACTER_UNDERLINE, 0);
			setProperty(CHARACTER_ITALIC, 0);
			setProperty(CHARACTER_SIZE, 24);// 1/2 pt sizes
			setProperty(CHARACTER_FONT, 0);
			return;
		}
		if(PARAGRAPH.equals(propertyGroup)) {
			setProperty(PARAGRAPH_INDENT_LEFT, 0);
			setProperty(PARAGRAPH_INDENT_RIGHT, 0);
			setProperty(PARAGRAPH_INDENT_FIRST_LINE, 0);
			setProperty(PARAGRAPH_JUSTIFICATION, JUSTIFY_LEFT);
			setProperty(PARAGRAPH_BORDER, PARAGRAPH_BORDER_NIL);
			setProperty(PARAGRAPH_BORDER_CELL, PARAGRAPH_BORDER_NIL);
			return;
		}
		if(SECTION.equals(propertyGroup)) {
			setProperty(SECTION_NUMBER_OF_COLUMNS, 0);
			setProperty(SECTION_BREAK_TYPE, SBK_NONE);
			setProperty(SECTION_PAGE_NUMBER_POSITION_X, 0);
			setProperty(SECTION_PAGE_NUMBER_POSITION_Y, 0);
			setProperty(SECTION_PAGE_NUMBER_FORMAT, PGN_DECIMAL);
			return;
		}
		if(DOCUMENT.equals(propertyGroup)) {
			setProperty(DOCUMENT_PAGE_WIDTH_TWIPS, 12240);
			setProperty(DOCUMENT_PAGE_HEIGHT_TWIPS, 15480);
			setProperty(DOCUMENT_MARGIN_LEFT_TWIPS, 1800);
			setProperty(DOCUMENT_MARGIN_TOP_TWIPS, 1440);
			setProperty(DOCUMENT_MARGIN_RIGHT_TWIPS, 1800);
			setProperty(DOCUMENT_MARGIN_BOTTOM_TWIPS, 1440);
			setProperty(DOCUMENT_PAGE_NUMBER_START, 1);
			setProperty(DOCUMENT_ENABLE_FACING_PAGES, 1);
			setProperty(DOCUMENT_PAGE_ORIENTATION, PAGE_PORTRAIT);
			setProperty(DOCUMENT_DEFAULT_FONT_NUMER, 0);	
			return;
		}
	}


	/**
	 * Toggle the value of the property identified by the <code>RtfCtrlWordData.specialHandler</code> parameter.
	 * Toggle values are assumed to be integer values per the RTF spec with a value of 0=off or 1=on.
	 * 
	 * @param ctrlWordData The property name to set
	 * @return <code>true</code> for handled or <code>false</code> if <code>propertyName</code> is <code>null</code> or <i>blank</i>
	 */
	public boolean toggleProperty(RtfCtrlWordData ctrlWordData) { //String propertyName) {
		
		String propertyName = ctrlWordData.specialHandler;
		
		if(propertyName == null || propertyName.length() == 0) return false;
		
		Object propertyValue = getProperty(propertyName);
		if(propertyValue == null) {
			propertyValue = new Integer(RtfProperty.ON);
		} else {
			if(propertyValue instanceof Integer) {
				int value = ((Integer)propertyValue).intValue();
				if(value != 0) {
					removeProperty(propertyName);
				}
				return true;
			} else {
				if(propertyValue instanceof Long) {
					long value = ((Long)propertyValue).intValue();
					if(value != 0) {
						removeProperty(propertyName);
					}
					return true;
				}
			}
		}
		setProperty(propertyName, propertyValue);
		return true;
	}
	/**
	 * Set the value of the property identified by the parameter.
	 * 
	 * @param ctrlWordData The controlword with the name to set
	 * @return <code>true</code> for handled or <code>false</code> if <code>propertyName</code> or <code>propertyValue</code> is <code>null</code>
	 */
	public boolean setProperty(RtfCtrlWordData ctrlWordData) { //String propertyName, Object propertyValueNew) {
		String propertyName = ctrlWordData.specialHandler;
		Object propertyValueNew = ctrlWordData.param;
		// depending on the control word, set mulitiple or reset settings, etc.
		//if pard then reset settings
		//
		setProperty(propertyName, propertyValueNew);
		return true;
	}
	/**
	 * Set the value of the property identified by the parameter.
	 * 
	 * @param propertyName The property name to set
	 * @param propertyValueNew The object to set the property value to
	 * @return <code>true</code> for handled or <code>false</code> if <code>propertyName</code> or <code>propertyValue</code> is <code>null</code>
	 */
	private boolean setProperty(String propertyName, Object propertyValueNew) {
		if(propertyName == null || propertyValueNew == null) return false;
		
		Object propertyValueOld = getProperty(propertyName);
		if(propertyValueOld instanceof Integer && propertyValueNew instanceof Integer) {
			int valueOld = ((Integer)propertyValueOld).intValue();
			int valueNew = ((Integer)propertyValueNew).intValue();
			if (valueOld==valueNew) return true;
		} else {
			if(propertyValueOld instanceof Long && propertyValueNew instanceof Long) {
				long valueOld = ((Long)propertyValueOld).intValue();
				long valueNew = ((Long)propertyValueNew).intValue();
				if (valueOld==valueNew) return true;
			}
		}
		beforeChange(propertyName);
		properties.put(propertyName, propertyValueNew);
		afterChange(propertyName);
		setModified(propertyName, true);
		return true;
	}
	/**
	 * Set the value of the property identified by the parameter.
	 * 
	 * @param propertyName The property name to set
	 * @param propertyValueNew The object to set the property value to
	 * @return <code>true</code> for handled or <code>false</code> if <code>propertyName</code> is <code>null</code>
	 */
	private boolean setProperty(String propertyName, int propertyValueNew) {
		if(propertyName == null) return false;
		Object propertyValueOld = getProperty(propertyName);
		if(propertyValueOld instanceof Integer) {
			int valueOld = ((Integer)propertyValueOld).intValue();
			if (valueOld==propertyValueNew) return true;
		} 
		beforeChange(propertyName);
		properties.put(propertyName, new Integer(propertyValueNew));
		afterChange(propertyName);
		setModified(propertyName, true);
		return true;
	}
	/**
	 * Add the value of the property identified by the parameter.
	 * 
	 * @param propertyName The property name to set
	 * @param propertyValue The object to set the property value to
	 * @return <code>true</code> for handled or <code>false</code> if <code>propertyName</code> is <code>null</code>
	 */
	private boolean addToProperty(String propertyName, int propertyValue) {
		if(propertyName == null) return false;
		int value = ((Integer)properties.get(propertyName)).intValue();
		if((value | propertyValue) == value) return true;
		value |= propertyValue;
		beforeChange(propertyName);
		properties.put(propertyName, new Integer(value));
		afterChange(propertyName);
		setModified(propertyName, true);
		return true;
	}
	/**
	 * Set the value of the property identified by the parameter.
	 * 
	 * @param propertyName The property name to set
	 * @param propertyValueNew The object to set the property value to
	 * @return <code>true</code> for handled or <code>false</code> if <code>propertyName</code> is <code>null</code>
	 */
	private boolean setProperty(String propertyName, long propertyValueNew) {
		if(propertyName == null) return false;
		Object propertyValueOld = getProperty(propertyName);
		if(propertyValueOld instanceof Long) {
			long valueOld = ((Long)propertyValueOld).longValue();
			if (valueOld==propertyValueNew) return true;
		} 
		beforeChange(propertyName);
		properties.put(propertyName, new Long(propertyValueNew));
		afterChange(propertyName);
		setModified(propertyName, true);
		return true;
	}
	/**
	 * Add the value of the property identified by the parameter.
	 * 
	 * @param propertyName The property name to set
	 * @param propertyValue The object to set the property value to
	 * @return <code>true</code> for handled or <code>false</code> if <code>propertyName</code> is <code>null</code>
	 */
	private boolean addToProperty(String propertyName, long propertyValue) {
		if(propertyName == null) return false;
		long value = ((Long)properties.get(propertyName)).longValue();
		if((value | propertyValue) == value) return true;
		value |= propertyValue;
		beforeChange(propertyName);
		properties.put(propertyName, new Long(value));
		afterChange(propertyName);
		setModified(propertyName, true);
		return true;
	}
	private boolean removeProperty(String propertyName) {
		if(propertyName == null) return false;
		if(properties.containsKey(propertyName)) {
			beforeChange(propertyName);
			properties.remove(propertyName);
			afterChange(propertyName);
			setModified(propertyName, true);
		}
		return true;
	}
	/**
	 * Get the value of the property identified by the parameter.
	 * 
	 * @param propertyName String containing the property name to get
	 * @return Property Object requested or null if not found in map.
	 */
	public Object getProperty(String propertyName) {
		return properties.get(propertyName);
	}
	/**
	 * Get a group of properties.
	 * 
	 * @param propertyGroup The group name to obtain.
	 * @return Properties object with requested values.
	 */
	public HashMap getProperties(String propertyGroup) {
		HashMap props = new HashMap();
		if(!properties.isEmpty()) {
			//properties.get
			Iterator it = properties.keySet().iterator();
			while(it.hasNext()) {
				String key = (String)it.next();
				if(key.startsWith(propertyGroup)) {
					props.put(key, this.properties.get(key));
				}
			}
		}
		return props;
	}
	
	/**
	 * @return the modified
	 */
	public boolean isModified() {
		return modifiedCharacter || modifiedParagraph || modifiedSection || modifiedDocument;
	}
	/**
	 * @param propertyName the propertyName that is modified
	 * @param modified the modified to set
	 */
	public void setModified(String propertyName, boolean modified) {
		if(propertyName.startsWith(CHARACTER)) {
			this.setModifiedCharacter(modified);
		} else {
			if(propertyName.startsWith(PARAGRAPH)) {
				this.setModifiedParagraph(modified);
			} else {
				if(propertyName.startsWith(SECTION)) {
					this.setModifiedSection(modified);
				} else {
					if(propertyName.startsWith(DOCUMENT)) {
						this.setModifiedDocument(modified);
					}
				}
			}
		}
	}
	/**
	 * @return the modifiedCharacter
	 */
	public boolean isModifiedCharacter() {
		return modifiedCharacter;
	}
	/**
	 * @param modifiedCharacter the modifiedCharacter to set
	 */
	public void setModifiedCharacter(boolean modifiedCharacter) {
		this.modifiedCharacter = modifiedCharacter;
	}
	/**
	 * @return the modifiedParagraph
	 */
	public boolean isModifiedParagraph() {
		return modifiedParagraph;
	}
	/**
	 * @param modifiedParagraph the modifiedParagraph to set
	 */
	public void setModifiedParagraph(boolean modifiedParagraph) {
		this.modifiedParagraph = modifiedParagraph;
	}
	/**
	 * @return the modifiedSection
	 */
	public boolean isModifiedSection() {
		return modifiedSection;
	}
	/**
	 * @param modifiedSection the modifiedSection to set
	 */
	public void setModifiedSection(boolean modifiedSection) {
		this.modifiedSection = modifiedSection;
	}
	/**
	 * @return the modifiedDocument
	 */
	public boolean isModifiedDocument() {
		return modifiedDocument;
	}
	/**
	 * @param modifiedDocument the modifiedDocument to set
	 */
	public void setModifiedDocument(boolean modifiedDocument) {
		this.modifiedDocument = modifiedDocument;
	}
	
	/**
	 * Adds a <CODE>RtfPropertyListener</CODE> to the <CODE>RtfProperty</CODE>.
	 *
	 * @param listener
	 *            the new RtfPropertyListener.
	 */
	public void addRtfPropertyListener(RtfPropertyListener listener) {
		listeners.add(listener);
	}
	/**
	 * Removes a <CODE>RtfPropertyListener</CODE> from the <CODE>RtfProperty</CODE>.
	 *
	 * @param listener
	 *            the new RtfPropertyListener.
	 */
	public void removeRtfPropertyListener(RtfPropertyListener listener) {
		listeners.remove(listener);
	}
	
	public void beforeChange(String propertyName) {
		// call listener for all
		RtfPropertyListener listener;
		for (Iterator iterator = listeners.iterator(); iterator.hasNext();) {
            listener = (RtfPropertyListener) iterator.next();
            listener.beforePropertyChange(propertyName);
        }
		
		if(propertyName.startsWith(CHARACTER)) {
			// call listener for character chane
		} else {
			if(propertyName.startsWith(PARAGRAPH)) {
				// call listener for paragraph change
			} else {
				if(propertyName.startsWith(SECTION)) {
					// call listener for section change
				} else {
					if(propertyName.startsWith(DOCUMENT)) {
						// call listener for document change
					}
				}
			}
		}
	}
	
	public void afterChange(String propertyName) {
		// call listener for all
		RtfPropertyListener listener;
		for (Iterator iterator = listeners.iterator(); iterator.hasNext();) {
            listener = (RtfPropertyListener) iterator.next();
            listener.afterPropertyChange(propertyName);
        }

		if(propertyName.startsWith(CHARACTER)) {
			// call listener for character chane
		} else {
			if(propertyName.startsWith(PARAGRAPH)) {
				// call listener for paragraph change
			} else {
				if(propertyName.startsWith(SECTION)) {
					// call listener for section change
				} else {
					if(propertyName.startsWith(DOCUMENT)) {
						// call listener for document change
					}
				}
			}
		}
	}
}
