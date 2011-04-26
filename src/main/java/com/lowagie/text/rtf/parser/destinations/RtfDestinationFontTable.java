/*
 * $Id: RtfDestinationFontTable.java 3832 2009-04-04 13:18:12Z blowagie $
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Properties;

import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.rtf.parser.RtfImportMgr;
import com.lowagie.text.rtf.parser.RtfParser;
import com.lowagie.text.rtf.parser.ctrlwords.RtfCtrlWordData;

/**
 * <code>RtfDestinationFontTable</code> handles data destined for the font table destination
 * 
 * @author Howard Shank (hgshank@yahoo.com)
 *
 * @since 2.0.8
 */
public final class RtfDestinationFontTable extends RtfDestination {
	/**
	 * The RtfImportHeader to add font mappings to.
	 */
	private RtfImportMgr importHeader = null;
	/**
	 * The theme (Office 2007)
	 */
	private String themeFont = "";
	/**
	 * The number of the font being parsed.
	 */
	private String fontNr = "";
	/**
	 * The family of the font being parsed.
	 */
	private String fontFamily = "";
	/**
	 * The \charset value
	 */
	private String charset = "";
	private static final String CHARSET_DEFAULT = "0";
	/**
	 * The \fprq
	 */
	private int fprq = 0;
	/**
	 * The \*\panose font matching value if primary font is not available.
	 */
	private String panose = "";
	/**
	 * The \*\fname
	 */
	private String nontaggedname = "";
	/**
	 * The name of the font being parsed.
	 */
	private String fontName = "";
	/**
	 * The \falt alternate font if primary font is not available.
	 */
	private String falt = "";
	/**
	 * The \falt alternate font if primary font is not available.
	 */
	private String fontemb = "";
	/**
	 * The \falt alternate font if primary font is not available.
	 */
	private String fontType = "";
	/**
	 * The \falt alternate font if primary font is not available.
	 */
	private String fontFile = "";
	/**
	 * The \falt alternate font if primary font is not available.
	 */
	private String fontFileCpg = "";
	/**
	 * The \fbias value
	 */
	private int fbias = 0;
	/**
	 * The \cpg value
	 */
	private String cpg = "";
	/**
	 * The \fnil, \fttruetype value
	 */
	private String trueType = "";

	/**
	 * state flag to handle different parsing of a font element
	 */
	private int state = 0;
	/* state values */
	/** Normal 	 */
	private static final int SETTING_NORMAL = 0;
	/** \falt 	 */
	private static final int SETTING_ALTERNATE = 1;
	/** \fname 	 */
	private static final int SETTING_FONTNAME = 2;
	/** \panose 	 */
	private static final int SETTING_PANOSE = 3;
	/** \fontemb	*/
	private static final int SETTING_FONT_EMBED = 4;
	/** \ffile  */
	private static final int SETTING_FONT_FILE = 5;
	
	/**
	 * Convert font mapping to <code>FontFactory</code> font objects.
	 */
	private HashMap fontMap = null;
	
	/**
	 * Constructor
	 */
	public RtfDestinationFontTable() {
		super(null);
	}
	/**
	 * Constructs a new RtfFontTableParser.
	 * 
	 * @param parser an RtfParser.
	 * 
	 * @since 2.0.8
	 */
	public RtfDestinationFontTable(RtfParser parser) {
		super(parser);
		this.init(true);
	}
	
	/* (non-Javadoc)
	 * @see com.lowagie.text.rtf.parser.destinations.RtfDestination#setParser(com.lowagie.text.rtf.parser.RtfParser)
	 * 
	 * @since 2.0.8
	 */
	public void setParser(RtfParser parser) {
		if(this.rtfParser != null && this.rtfParser.equals(parser)) return;
		this.rtfParser = parser;
		this.init(true);
	}
	/**
	 * Initialize the object.
	 * 
	 * @param importFonts true to import the fonts into the FontFactory, false do not load fonts
	 * 
	 * @since 2.0.8
	 */
	private void init(boolean importFonts) {
		fontMap = new HashMap();
		if(this.rtfParser != null) {
			this.importHeader = this.rtfParser.getImportManager();
		}
		this.setToDefaults();
		if(importFonts) {
			importSystemFonts();
		}
	}
	/* (non-Javadoc)
	 * @see com.lowagie.text.rtf.parser.destinations.RtfDestination#handleOpenNewGroup()
	 * 
	 * @since 2.0.8
	 */
	public boolean handleOpeningSubGroup() {
		return true;
	}
	/* (non-Javadoc)
	 * @see com.lowagie.text.rtf.direct.RtfDestination#closeDestination()
	 * 
	 * @since 2.0.8
	 */
	public boolean closeDestination() {
		return true;
	}

	/* (non-Javadoc)
	 * @see com.lowagie.text.rtf.direct.RtfDestination#handleGroupEnd()
	 * 
	 * @since 2.0.8
	 */
	public boolean handleCloseGroup() {
		if(this.state == SETTING_NORMAL) {
			processFont();
		}
		this.state = SETTING_NORMAL;
		return true;
	}

	/* (non-Javadoc)
	 * @see com.lowagie.text.rtf.direct.RtfDestination#handleGroupStart()
	 * 
	 * @since 2.0.8
	 */
	public boolean handleOpenGroup() {

		return true;
	}
	
	/* (non-Javadoc)
	 * @see com.lowagie.text.rtf.direct.RtfDestination#handleCharacter(char[])
	 * 
	 * @since 2.0.8
	 */
	public boolean handleCharacter(int ch) {
		switch(this.state) {
		case SETTING_NORMAL:
			this.fontName += (char)ch;
			break;
		case SETTING_ALTERNATE:
			this.falt += (char)ch;
			break;
		case SETTING_PANOSE:
			this.panose += (char)ch;
			break;
		case SETTING_FONT_EMBED:
			break;
		case SETTING_FONT_FILE:
			break;
		case SETTING_FONTNAME:
			break;
			
		}
		return true;
	}
	/* (non-Javadoc)
	 * @see com.lowagie.text.rtf.parser.destinations.RtfDestination#handleControlWord(com.lowagie.text.rtf.parser.ctrlwords.RtfCtrlWordData)
	 * 
	 * @since 2.0.8
	 */
	public boolean handleControlWord(RtfCtrlWordData ctrlWordData) {
		boolean result = true;
		// just let fonttbl fall through and set last ctrl word object.
		
		if(ctrlWordData.ctrlWord.equals("f")) { this.setFontNumber(ctrlWordData.param); result=true;}
		if(ctrlWordData.ctrlWord.equals("fcharset")) { this.setCharset(ctrlWordData.param); result=true; }

		// font families
		if(ctrlWordData.ctrlWord.equals("fnil")) { this.setFontFamily("roman"); result=true; }
		if(ctrlWordData.ctrlWord.equals("froman")) { this.setFontFamily("roman"); result=true; }
		if(ctrlWordData.ctrlWord.equals("fswiss")) { this.setFontFamily("swiss"); result=true; }
		if(ctrlWordData.ctrlWord.equals("fmodern")) { this.setFontFamily("modern"); result=true; }
		if(ctrlWordData.ctrlWord.equals("fscript")) { this.setFontFamily("script"); result=true; }
		if(ctrlWordData.ctrlWord.equals("fdecor")) { this.setFontFamily("decor"); result=true; }
		if(ctrlWordData.ctrlWord.equals("ftech")) { this.setFontFamily("tech"); result=true; }
		if(ctrlWordData.ctrlWord.equals("fbidi")) { this.setFontFamily("bidi"); result=true; }
		// pitch
		if(ctrlWordData.ctrlWord.equals("fprq")) { this.setPitch(ctrlWordData.param); result=true; }
		// bias
		if(ctrlWordData.ctrlWord.equals("fbias")) { this.setBias(ctrlWordData.param); result=true; }
		// theme font information
		if(ctrlWordData.ctrlWord.equals("flomajor")) { this.setThemeFont("flomajor"); result= true; }
		if(ctrlWordData.ctrlWord.equals("fhimajor")) { this.setThemeFont("fhimajor"); result= true; }
		if(ctrlWordData.ctrlWord.equals("fdbmajor")) { this.setThemeFont("fdbmajor"); result= true; }
		if(ctrlWordData.ctrlWord.equals("fbimajor")) { this.setThemeFont("fbimajor"); result= true; }
		if(ctrlWordData.ctrlWord.equals("flominor")) { this.setThemeFont("flominor"); result= true; }
		if(ctrlWordData.ctrlWord.equals("fhiminor")) { this.setThemeFont("fhiminor"); result= true; }
		if(ctrlWordData.ctrlWord.equals("fdbminor")) { this.setThemeFont("fdbminor"); result= true; }
		if(ctrlWordData.ctrlWord.equals("fbiminor")) { this.setThemeFont("fbiminor"); result= true; }

		// panose
		if(ctrlWordData.ctrlWord.equals("panose")) {state = SETTING_PANOSE; result = true; }
		
		// \*\fname
		// <font name> #PCDATA
		if(ctrlWordData.ctrlWord.equals("fname")) {state = SETTING_FONTNAME; result = true; }

		// \*\falt
		if(ctrlWordData.ctrlWord.equals("falt")) { state = SETTING_ALTERNATE; result = true; }
		
		// \*\fontemb
		if(ctrlWordData.ctrlWord.equals("fontemb")) { state = SETTING_FONT_EMBED; result = true; }

		// font type
		if(ctrlWordData.ctrlWord.equals("ftnil")) { this.setTrueType("ftnil"); result= true; }
		if(ctrlWordData.ctrlWord.equals("fttruetype")) { this.setTrueType("fttruetype"); result= true; }
		
		// \*\fontfile
		if(ctrlWordData.ctrlWord.equals("fontemb")) { state = SETTING_FONT_FILE; result = true; }

		// codepage
		if(ctrlWordData.ctrlWord.equals("cpg")) { this.setCodePage(ctrlWordData.param); result= true; }
		
		this.lastCtrlWord = ctrlWordData;
		return result;
	}
	/**
	 * Set the code page
	 * @param value The code page value
	 * 
	 * @since 2.0.8
	 */
	public void setCodePage(String value) {
		this.cpg = value;
	}
	/**
	 * Set the TrueTtype type
	 * @param value The type
	 * 
	 * @since 2.0.8
	 */
	public void setTrueType(String value) {
		this.trueType = value;
	}
	/**
	 * Set the font pitch
	 * @param value Pitch value
	 * 
	 * @since 2.0.8
	 */
	public void setPitch(String value) {
		this.fprq = Integer.parseInt(value);
	}
	/**
	 * Set the font bias
	 * @param value Bias value
	 * 
	 * @since 2.0.8
	 */
	public void setBias(String value) {
		this.fbias = Integer.parseInt(value);
	}
	/**
	 * Set the font theme
	 * 
	 * @param themeFont Theme value
	 * 
	 * @since 2.0.8
	 */
	public void setThemeFont(String themeFont) {
		this.themeFont = themeFont;
	}
	/**
	 * Set the font name to the parsed value.
	 * 
	 * @param fontName The font name.
	 * 
	 * @since 2.0.8
	 */
	public void setFontName(String fontName) {
		this.fontName = fontName;
	}
	/**
	 * Set the font family to the parsed value.
	 * 
	 * @param fontFamily The font family.
	 * 
	 * @since 2.0.8
	 */
	public void setFontFamily(String fontFamily) {
		this.fontFamily = fontFamily;
	}
	/**
	 * Set the font number to the parsed value.
	 * This is used for mapping fonts to the new font numbers
	 * 
	 * @param fontNr The font number.
	 * 
	 * @since 2.0.8
	 */
	public void setFontNumber(String fontNr) {
		this.fontNr = fontNr;
	}
	/**
	 * Set the alternate font name.
	 * 
	 * @param fontAlternate The falt font value
	 * 
	 * @since 2.0.8
	 */
	public void setFontAlternate(String fontAlternate) {
		this.falt = fontAlternate;
	}
	/**
	 * Set the character-set to the parsed value.
	 * 
	 * @param charset The charset value
	 * 
	 * @since 2.0.8
	 */
	public void setCharset(String charset) {
		if(charset.length() == 0) {
			charset = CHARSET_DEFAULT;
		}
		this.charset = charset;
	}

	/* (non-Javadoc)
	 * @see com.lowagie.text.rtf.direct.RtfDestination#setDefaults()
	 * 
	 * @since 2.0.8
	 */
	public void setToDefaults() {
		this.themeFont = "";
		this.fontNr = "";
		this.fontName = "";
		this.fontFamily = "";
		
		this.charset = "";
		this.fprq = 0;
		this.panose = "";
		this.nontaggedname = "";
		this.falt = "";
		this.fontemb = "";
		this.fontType = "";
		this.fontFile = "";
		this.fontFileCpg = "";
		this.fbias = 0;
		this.cpg = "";
		this.trueType = "";
		this.state = SETTING_NORMAL;
	}
	/**
	 * Process the font information that was parsed from the input.
	 * 
	 * @since 2.0.8
	 */
	private void processFont() {
		this.fontName = this.fontName.trim();
		if(fontName.length() == 0) return;
		if(fontNr.length() == 0) return;
		
		if(fontName.length()>0 && fontName.indexOf(';') >= 0) {
			fontName = fontName.substring(0,fontName.indexOf(';'));
		}

		if(this.rtfParser.isImport()) {
			//TODO: If primary font fails, use the alternate
				//TODO: Problem: RtfFont defaults family to \froman and doesn't allow any other family.
				// if you set the family, it changes the font name and not the family in the Font.java class.
				
	//			if(this.fontFamily.length() > 0) {
	//				if(this.importHeader.importFont(this.fontNr, this.fontName, this.fontFamily, Integer.parseInt(this.charset)) == false) {
	//					if(this.falt.length() > 0) {
	//						this.importHeader.importFont(this.fontNr, this.falt, this.fontFamily, Integer.parseInt(this.charset));
	//					}
	//				}
	//			} else {
					if(!this.importHeader.importFont(this.fontNr, this.fontName, Integer.parseInt("".equals(this.charset)?CHARSET_DEFAULT:this.charset))) {
						if(this.falt.length() > 0) {
							this.importHeader.importFont(this.fontNr, this.falt, Integer.parseInt("".equals(this.charset)?CHARSET_DEFAULT:this.charset));
						}
					}
	//			}
			}
		if(this.rtfParser.isConvert()) {
			// This could probably be written as a better font matching function
			
			String fName = this.fontName;	// work variable for trimming name if needed.
			Font f1 = createfont(fName);
			if(f1.getBaseFont() == null && this.falt.length()>0)
				f1 = createfont(this.falt);
			
			if(f1.getBaseFont() == null) {
				// Did not find a font, let's try a substring of the first name.
				if(FontFactory.COURIER.indexOf(fName) > -1 ) {
					f1 = FontFactory.getFont(FontFactory.COURIER);
				} else if(FontFactory.HELVETICA.indexOf(fName) > -1 ) {
					f1 = FontFactory.getFont(FontFactory.HELVETICA);
				} else if(FontFactory.TIMES.indexOf(fName) > -1 ) {
					f1 = FontFactory.getFont(FontFactory.TIMES);
				} else if(FontFactory.SYMBOL.indexOf(fName) > -1 ) {
					f1 = FontFactory.getFont(FontFactory.SYMBOL);
				} else if(FontFactory.ZAPFDINGBATS.indexOf(fName) > -1 ) {
					f1 = FontFactory.getFont(FontFactory.ZAPFDINGBATS);
				} else {
					// we did not find a matching font in any form.
					// default to HELVETICA for now.
					f1 = FontFactory.getFont(FontFactory.HELVETICA);
				}
			}
			fontMap.put(this.fontNr, f1);
			//System.out.println(f1.getFamilyname());
		}
		this.setToDefaults();
	}
	/**
	 * Create a font via the <code>FontFactory</code>
	 * 
	 * @param fontName The font name to create
	 * @return The created <code>Font</code> object
	 * 
	 * @since 2.0.8
	 */
	private Font createfont(String fontName) {
		Font f1 = null;
		int pos=-1;
		do {
			f1 = FontFactory.getFont(fontName);
			
			if(f1.getBaseFont() != null) break;	// found a font, exit the do/while
			
			pos = fontName.lastIndexOf(' ');	// find the last space
			if(pos>0) {
				fontName = fontName.substring(0, pos );	// truncate it to the last space
			}
		} while(pos>0);
		return f1;
	}
	/**
	 * Get a <code>Font</code> object from the font map object
	 * 
	 * @param key The font number to get
	 * @return The mapped <code>Font</code> object.
	 * 
	 * @since 2.0.8
	 */
	public Font getFont(String key) {
		return (Font) fontMap.get(key);
	}
	/**
	 * Load system fonts into the static <code>FontFactory</code> object
	 * 
	 * @since 2.0.8
	 */
	private void importSystemFonts() {
		Properties pr = null;
		try {
			pr = getEnvironmentVariables();
		} catch (Throwable e) {
		}
		String systemRoot = pr.getProperty("SystemRoot");
		Runtime runtime = Runtime.getRuntime();
		String fileSeperator = System.getProperty("file.separator");
		int r = FontFactory.registerDirectory(systemRoot + fileSeperator + "fonts");
	}
	
	/**
	 * Utility method to load the environment variables.
	 * 
	 * @return Properties object with environment variable information
	 * @throws Throwable
	 * 
	 * @since 2.0.8
	 */
	 private Properties getEnvironmentVariables() throws Throwable {
		Properties environmentVariables = new Properties();
		String operatingSystem = System.getProperty("os.name").toLowerCase();
		Runtime runtime = Runtime.getRuntime();
		Process process = null;
		if (operatingSystem.indexOf("windows 95") > -1
				|| operatingSystem.indexOf("windows 98") > -1
				|| operatingSystem.indexOf("me") > -1) {
			process = runtime.exec("command.com /c set");
		} else if ((operatingSystem.indexOf("nt") > -1)
				|| (operatingSystem.indexOf("windows 2000") > -1)
				|| (operatingSystem.indexOf("windows xp") > -1)
				|| (operatingSystem.indexOf("windows 2003") > -1)
				|| (operatingSystem.indexOf("windows vista") > -1)) {
			process = runtime.exec("cmd.exe /c set");
		} else {
			process = runtime.exec("env");
		}
		BufferedReader environmentStream = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String inputLine = "";
		int idx = -1;
		while ((inputLine = environmentStream.readLine()) != null) {
			idx = inputLine.indexOf('=');
			environmentVariables.setProperty(inputLine.substring(0, idx),
					inputLine.substring(idx + 1));
		}
		return environmentVariables;
	}

}
