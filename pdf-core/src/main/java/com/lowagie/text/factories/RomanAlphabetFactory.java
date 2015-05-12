/*
 * $Id: RomanAlphabetFactory.java 4065 2009-09-16 23:09:11Z psoares33 $
 *
 * Copyright 2007 by Bruno Lowagie.
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

package com.lowagie.text.factories;

import com.lowagie.text.error_messages.MessageLocalization;

/**
 * This class can produce String combinations representing a number.
 * "a" to "z" represent 1 to 26, "AA" represents 27, "AB" represents 28,
 * and so on; "ZZ" is followed by "AAA".
 */
public class RomanAlphabetFactory {

	/**
	 * Translates a positive integer (not equal to zero)
	 * into a String using the letters 'a' to 'z';
	 * 1 = a, 2 = b, ..., 26 = z, 27 = aa, 28 = ab,...
	 */
	public static final String getString(int index) {
    	if (index < 1) throw new NumberFormatException(MessageLocalization.getComposedMessage("you.can.t.translate.a.negative.number.into.an.alphabetical.value"));
    	
    	index--;
    	int bytes = 1;
    	int start = 0;
    	int symbols = 26;  
    	while(index >= symbols + start) {
    		bytes++;
    	    start += symbols;
    		symbols *= 26;
    	}
    	      
    	int c = index - start;
    	char[] value = new char[bytes];
    	while(bytes > 0) {
    		value[--bytes] = (char)( 'a' + (c % 26));
    		c /= 26;
    	}
    	
    	return new String(value);
	}
	
	/**
	 * Translates a positive integer (not equal to zero)
	 * into a String using the letters 'a' to 'z';
	 * 1 = a, 2 = b, ..., 26 = z, 27 = aa, 28 = ab,...
	 */
	public static final String getLowerCaseString(int index) {
		return getString(index);		
	}
	
	/**
	 * Translates a positive integer (not equal to zero)
	 * into a String using the letters 'A' to 'Z';
	 * 1 = A, 2 = B, ..., 26 = Z, 27 = AA, 28 = AB,...
	 */
	public static final String getUpperCaseString(int index) {
		return getString(index).toUpperCase();		
	}

	
	/**
	 * Translates a positive integer (not equal to zero)
	 * into a String using the letters 'a' to 'z'
	 * (a = 1, b = 2, ..., z = 26, aa = 27, ab = 28,...).
	 */
	public static final String getString(int index, boolean lowercase) {
		if (lowercase) {
			return getLowerCaseString(index);
		}
		else {
			return getUpperCaseString(index);
		}
	}
	
	/**
	 * Test this class using this main method.
	 */
	public static void main(String[] args) {
		for (int i = 1; i < 32000; i++) {
			System.out.println(getString(i));
		}
	}
}
