/*
 * Copyright 2004 Paulo Soares
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
 * the Initial Developer are Copyright (C) 1999, 2000, 2001, 2002 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000, 2001, 2002 by Paulo Soares. All Rights Reserved.
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
 * Contributions by:
 * Lubos Strapko
 * 
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * http://www.lowagie.com/iText/
 */

package com.lowagie.text.html.simpleparser;

import java.awt.Color;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;

import com.lowagie.text.Chunk;
import com.lowagie.text.Element;
import com.lowagie.text.ElementTags;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.ListItem;
import com.lowagie.text.Paragraph;
import com.lowagie.text.html.Markup;
import com.lowagie.text.html.HtmlTags;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.HyphenationAuto;
import com.lowagie.text.pdf.HyphenationEvent;
import com.lowagie.text.FontProvider;
/**
 *
 * @author  psoares
 */
public class FactoryProperties {

	/**
	 * @since	iText 5.0	This used to be a FontFactoryImp
	 */
	private FontProvider fontImp = FontFactory.getFontImp();

	/** Creates a new instance of FactoryProperties */
	public FactoryProperties() {
	}

	public Chunk createChunk(String text, ChainedProperties props) {
		Font font = getFont(props);
		float size = font.getSize();
		size /= 2;
		Chunk ck = new Chunk(text, font);
		if (props.hasProperty("sub"))
			ck.setTextRise(-size);
		else if (props.hasProperty("sup"))
			ck.setTextRise(size);
		ck.setHyphenation(getHyphenation(props));
		return ck;
	}

	private static void setParagraphLeading(Paragraph p, String leading) {
		if (leading == null) {
			p.setLeading(0, 1.5f);
			return;
		}
		try {
			StringTokenizer tk = new StringTokenizer(leading, " ,");
			String v = tk.nextToken();
			float v1 = Float.parseFloat(v);
			if (!tk.hasMoreTokens()) {
				p.setLeading(v1, 0);
				return;
			}
			v = tk.nextToken();
			float v2 = Float.parseFloat(v);
			p.setLeading(v1, v2);
		} catch (Exception e) {
			p.setLeading(0, 1.5f);
		}
	}

	public static void createParagraph(Paragraph p, ChainedProperties props) {
		String value = props.getProperty("align");
		if (value != null) {
			if (value.equalsIgnoreCase("center"))
				p.setAlignment(Element.ALIGN_CENTER);
			else if (value.equalsIgnoreCase("right"))
				p.setAlignment(Element.ALIGN_RIGHT);
			else if (value.equalsIgnoreCase("justify"))
				p.setAlignment(Element.ALIGN_JUSTIFIED);
		}
		p.setHyphenation(getHyphenation(props));
		setParagraphLeading(p, props.getProperty("leading"));
		value = props.getProperty("before");
		if (value != null) {
			try {
				p.setSpacingBefore(Float.parseFloat(value));
			} catch (Exception e) {
			}
		}
		value = props.getProperty("after");
		if (value != null) {
			try {
				p.setSpacingAfter(Float.parseFloat(value));
			} catch (Exception e) {
			}
		}
		value = props.getProperty("extraparaspace");
		if (value != null) {
			try {
				p.setExtraParagraphSpace(Float.parseFloat(value));
			} catch (Exception e) {
			}
		}
	}

	public static Paragraph createParagraph(ChainedProperties props) {
		Paragraph p = new Paragraph();
		createParagraph(p, props);
		return p;
	}

	public static ListItem createListItem(ChainedProperties props) {
		ListItem p = new ListItem();
		createParagraph(p, props);
		return p;
	}

	public Font getFont(ChainedProperties props) {
		String face = props.getProperty(ElementTags.FACE);
		if (face != null) {
			StringTokenizer tok = new StringTokenizer(face, ",");
			while (tok.hasMoreTokens()) {
				face = tok.nextToken().trim();
				if (face.startsWith("\""))
					face = face.substring(1);
				if (face.endsWith("\""))
					face = face.substring(0, face.length() - 1);
				if (fontImp.isRegistered(face))
					break;
			}
		}
		int style = 0;
		if (props.hasProperty(HtmlTags.I))
			style |= Font.ITALIC;
		if (props.hasProperty(HtmlTags.B))
			style |= Font.BOLD;
		if (props.hasProperty(HtmlTags.U))
			style |= Font.UNDERLINE;
		if (props.hasProperty(HtmlTags.S))
			style |= Font.STRIKETHRU;
		String value = props.getProperty(ElementTags.SIZE);
		float size = 12;
		if (value != null)
			size = Float.parseFloat(value);
		Color color = Markup.decodeColor(props.getProperty("color"));
		String encoding = props.getProperty("encoding");
		if (encoding == null)
			encoding = BaseFont.WINANSI;
		return fontImp.getFont(face, encoding, true, size, style, color);
	}

	/**
	 * Gets a HyphenationEvent based on the hyphenation entry in ChainedProperties.
	 * @param	props	ChainedProperties
	 * @return	a HyphenationEvent
	 * @since	2.1.2
	 */
	public static HyphenationEvent getHyphenation(ChainedProperties props) {
		return getHyphenation(props.getProperty("hyphenation"));
	}

	/**
	 * Gets a HyphenationEvent based on the hyphenation entry in a HashMap.
	 * @param	props	a HashMap with properties
	 * @return	a HyphenationEvent
	 * @since	2.1.2
	 */
	public static HyphenationEvent getHyphenation(HashMap props) {
		return getHyphenation((String) props.get("hyphenation"));
	}

	/**
	 * Gets a HyphenationEvent based on a String.
	 * For instance "en_UK,3,2" returns new HyphenationAuto("en", "UK", 3, 2);
	 * @param	s a String, for instance "en_UK,2,2"
	 * @return	a HyphenationEvent
	 * @since	2.1.2
	 */
	public static HyphenationEvent getHyphenation(String s) {
		if (s == null || s.length() == 0) {
			return null;
		}
		String lang = s;
		String country = null;
		int leftMin = 2;
		int rightMin = 2;

		int pos = s.indexOf('_');
		if (pos == -1) {
			return new HyphenationAuto(lang, country, leftMin, rightMin);
		}
		lang = s.substring(0, pos);
		country = s.substring(pos + 1);
		pos = country.indexOf(',');
		if (pos == -1) {
			return new HyphenationAuto(lang, country, leftMin, rightMin);
		}
		s = country.substring(pos + 1);
		country = country.substring(0, pos);
		pos = s.indexOf(',');
		if (pos == -1) {
			leftMin = Integer.parseInt(s);
		} else {
			leftMin = Integer.parseInt(s.substring(0, pos));
			rightMin = Integer.parseInt(s.substring(pos + 1));
		}
		return new HyphenationAuto(lang, country, leftMin, rightMin);
	}

	/**
	 * This method isn't used by iText, but you can use it to analyze
	 * the value of a style attribute inside a HashMap.
	 * The different elements of the style attribute are added to the
	 * HashMap as key-value pairs.
	 * @param	h	a HashMap that should have at least a key named
	 * style. After this method is invoked, more keys could be added.
	 */
	public static void insertStyle(HashMap h) {
		String style = (String) h.get("style");
		if (style == null)
			return;
		Properties prop = Markup.parseAttributes(style);
		for (Iterator it = prop.keySet().iterator(); it.hasNext();) {
			String key = (String) it.next();
			if (key.equals(Markup.CSS_KEY_FONTFAMILY)) {
				h.put("face", prop.getProperty(key));
			} else if (key.equals(Markup.CSS_KEY_FONTSIZE)) {
				h.put("size", Float.toString(Markup.parseLength(prop
						.getProperty(key)))
						+ "pt");
			} else if (key.equals(Markup.CSS_KEY_FONTSTYLE)) {
				String ss = prop.getProperty(key).trim().toLowerCase();
				if (ss.equals("italic") || ss.equals("oblique"))
					h.put("i", null);
			} else if (key.equals(Markup.CSS_KEY_FONTWEIGHT)) {
				String ss = prop.getProperty(key).trim().toLowerCase();
				if (ss.equals("bold") || ss.equals("700") || ss.equals("800")
						|| ss.equals("900"))
					h.put("b", null);
			} else if (key.equals(Markup.CSS_KEY_TEXTDECORATION)) {
				String ss = prop.getProperty(key).trim().toLowerCase();
				if (ss.equals(Markup.CSS_VALUE_UNDERLINE))
					h.put("u", null);
			} else if (key.equals(Markup.CSS_KEY_COLOR)) {
				Color c = Markup.decodeColor(prop.getProperty(key));
				if (c != null) {
					int hh = c.getRGB();
					String hs = Integer.toHexString(hh);
					hs = "000000" + hs;
					hs = "#" + hs.substring(hs.length() - 6);
					h.put("color", hs);
				}
			} else if (key.equals(Markup.CSS_KEY_LINEHEIGHT)) {
				String ss = prop.getProperty(key).trim();
				float v = Markup.parseLength(prop.getProperty(key));
				if (ss.endsWith("%")) {
					h.put("leading", "0," + (v / 100));
				} else if ("normal".equalsIgnoreCase(ss)) {
					h.put("leading", "0,1.5");
				}
				else {
					h.put("leading", v + ",0");
				}
			} else if (key.equals(Markup.CSS_KEY_TEXTALIGN)) {
				String ss = prop.getProperty(key).trim().toLowerCase();
				h.put("align", ss);
			}
		}
	}

	/**
	 * New method contributed by Lubos Strapko
	 * @param h
	 * @param cprops
	 * @since 2.1.3
	 */
	public static void insertStyle(HashMap h, ChainedProperties cprops) {
		String style = (String) h.get("style");
		if (style == null)
			return;
		Properties prop = Markup.parseAttributes(style);
		for (Iterator it = prop.keySet().iterator(); it.hasNext();) {
			String key = (String) it.next();
			if (key.equals(Markup.CSS_KEY_FONTFAMILY)) {
				h.put(ElementTags.FACE, prop.getProperty(key));
			} else if (key.equals(Markup.CSS_KEY_FONTSIZE)) {
				float actualFontSize = Markup.parseLength(cprops
						.getProperty(ElementTags.SIZE),
						Markup.DEFAULT_FONT_SIZE);
				if (actualFontSize <= 0f)
					actualFontSize = Markup.DEFAULT_FONT_SIZE;
				h.put(ElementTags.SIZE, Float.toString(Markup.parseLength(prop
						.getProperty(key), actualFontSize))
						+ "pt");
			} else if (key.equals(Markup.CSS_KEY_FONTSTYLE)) {
				String ss = prop.getProperty(key).trim().toLowerCase();
				if (ss.equals("italic") || ss.equals("oblique"))
					h.put("i", null);
			} else if (key.equals(Markup.CSS_KEY_FONTWEIGHT)) {
				String ss = prop.getProperty(key).trim().toLowerCase();
				if (ss.equals("bold") || ss.equals("700") || ss.equals("800")
						|| ss.equals("900"))
					h.put("b", null);
			} else if (key.equals(Markup.CSS_KEY_TEXTDECORATION)) {
				String ss = prop.getProperty(key).trim().toLowerCase();
				if (ss.equals(Markup.CSS_VALUE_UNDERLINE))
					h.put("u", null);
			} else if (key.equals(Markup.CSS_KEY_COLOR)) {
				Color c = Markup.decodeColor(prop.getProperty(key));
				if (c != null) {
					int hh = c.getRGB();
					String hs = Integer.toHexString(hh);
					hs = "000000" + hs;
					hs = "#" + hs.substring(hs.length() - 6);
					h.put("color", hs);
				}
			} else if (key.equals(Markup.CSS_KEY_LINEHEIGHT)) {
				String ss = prop.getProperty(key).trim();
				float actualFontSize = Markup.parseLength(cprops
						.getProperty(ElementTags.SIZE),
						Markup.DEFAULT_FONT_SIZE);
				if (actualFontSize <= 0f)
					actualFontSize = Markup.DEFAULT_FONT_SIZE;
				float v = Markup.parseLength(prop.getProperty(key),
						actualFontSize);
				if (ss.endsWith("%")) {
					h.put("leading", "0," + (v / 100));
					return;
				}
				if ("normal".equalsIgnoreCase(ss)) {
					h.put("leading", "0,1.5");
					return;
				}
				h.put("leading", v + ",0");
			} else if (key.equals(Markup.CSS_KEY_TEXTALIGN)) {
				String ss = prop.getProperty(key).trim().toLowerCase();
				h.put("align", ss);
			} else if (key.equals(Markup.CSS_KEY_PADDINGLEFT)) {
				String ss = prop.getProperty(key).trim().toLowerCase();
				h.put("indent", Float.toString(Markup.parseLength(ss)));
			}
		}
	}

	public FontProvider getFontImp() {
		return fontImp;
	}

	public void setFontImp(FontProvider fontImp) {
		this.fontImp = fontImp;
	}

	public static HashMap followTags = new HashMap();
	static {
		followTags.put("i", "i");
		followTags.put("b", "b");
		followTags.put("u", "u");
		followTags.put("sub", "sub");
		followTags.put("sup", "sup");
		followTags.put("em", "i");
		followTags.put("strong", "b");
		followTags.put("s", "s");
		followTags.put("strike", "s");
	}
}
