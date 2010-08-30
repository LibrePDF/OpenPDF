/*
 * Copyright 2004 by Takenori.
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
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * http://www.lowagie.com/iText/
 */
package com.lowagie.text.pdf;

import java.awt.Font;

public class AsianFontMapper extends DefaultFontMapper {
	
	public static final String ChineseSimplifiedFont = "STSong-Light";
	public static final String ChineseSimplifiedEncoding_H = "UniGB-UCS2-H";
	public static final String ChineseSimplifiedEncoding_V = "UniGB-UCS2-V";
	
	public static final String ChineseTraditionalFont_MHei = "MHei-Medium";
	public static final String ChineseTraditionalFont_MSung = "MSung-Light";
	public static final String ChineseTraditionalEncoding_H = "UniCNS-UCS2-H";
	public static final String ChineseTraditionalEncoding_V = "UniCNS-UCS2-V";
	
	public static final String JapaneseFont_Go = "HeiseiKakuGo-W5";
	public static final String JapaneseFont_Min = "HeiseiMin-W3";
	public static final String JapaneseEncoding_H = "UniJIS-UCS2-H";
	public static final String JapaneseEncoding_V = "UniJIS-UCS2-V";
	public static final String JapaneseEncoding_HW_H = "UniJIS-UCS2-HW-H";
	public static final String JapaneseEncoding_HW_V = "UniJIS-UCS2-HW-V";
	
	public static final String KoreanFont_GoThic = "HYGoThic-Medium";
	public static final String KoreanFont_SMyeongJo = "HYSMyeongJo-Medium";
	public static final String KoreanEncoding_H = "UniKS-UCS2-H";
	public static final String KoreanEncoding_V = "UniKS-UCS2-V";
	
	private final String defaultFont;
	private final String encoding;

	public AsianFontMapper(String font, String encoding) {
		super();
		
		this.defaultFont = font;
		this.encoding = encoding;
	}

	public BaseFont awtToPdf(Font font) {
		try {
			BaseFontParameters p = getBaseFontParameters(font.getFontName());
			if (p != null){
				return BaseFont.createFont(p.fontName, p.encoding, p.embedded, p.cached, p.ttfAfm, p.pfb);
			}else{
				return BaseFont.createFont(defaultFont, encoding, true);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;

	}

}
