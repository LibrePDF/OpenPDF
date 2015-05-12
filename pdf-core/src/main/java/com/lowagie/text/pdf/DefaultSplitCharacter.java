/*
 * $Id: DefaultSplitCharacter.java 3427 2008-05-24 18:32:31Z xlv $
 *
 * Copyright 2008 Bruno Lowagie and Xavier Le Vourch
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

import com.lowagie.text.SplitCharacter;

/**
 * The default class that is used to determine whether or not a character
 * is a split character. You can subclass this class to define your own
 * split characters.
 * @since	2.1.2
 */
public class DefaultSplitCharacter implements SplitCharacter {
	
	/**
	 * An instance of the default SplitCharacter.
	 */
	public static final SplitCharacter DEFAULT = new DefaultSplitCharacter();
	
	/**
	 * Checks if a character can be used to split a <CODE>PdfString</CODE>.
	 * <P>
	 * for the moment every character less than or equal to SPACE, the character '-'
	 * and some specific unicode ranges are 'splitCharacters'.
	 * 
	 * @param start start position in the array
	 * @param current current position in the array
	 * @param end end position in the array
	 * @param	cc		the character array that has to be checked
	 * @param ck chunk array
	 * @return	<CODE>true</CODE> if the character can be used to split a string, <CODE>false</CODE> otherwise
	 */
    public boolean isSplitCharacter(int start, int current, int end, char[] cc, PdfChunk[] ck) {
        char c = getCurrentCharacter(current, cc, ck);
        if (c <= ' ' || c == '-' || c == '\u2010') {
            return true;
        }
        if (c < 0x2002)
            return false;
        return ((c >= 0x2002 && c <= 0x200b)
        || (c >= 0x2e80 && c < 0xd7a0)
        || (c >= 0xf900 && c < 0xfb00)
        || (c >= 0xfe30 && c < 0xfe50)
        || (c >= 0xff61 && c < 0xffa0));
    }

    /**
     * Returns the current character
	 * @param current current position in the array
	 * @param	cc		the character array that has to be checked
	 * @param ck chunk array
     * @return	the current character
     */
    protected char getCurrentCharacter(int current, char[] cc, PdfChunk[] ck) {
    	if (ck == null) {
    		return cc[current];
    	}
    	return (char)ck[Math.min(current, ck.length - 1)].getUnicodeEquivalent(cc[current]);
    }
}
