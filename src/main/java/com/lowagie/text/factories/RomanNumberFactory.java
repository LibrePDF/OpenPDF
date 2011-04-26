/*
 * $Id: RomanNumberFactory.java 3373 2008-05-12 16:21:24Z xlv $
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
/**
 * This class can produce String combinations representing a roman number.
 */
public class RomanNumberFactory {
	/**
	 * Helper class for Roman Digits
	 */
	private static class RomanDigit {

		/** part of a roman number */
		public char digit;

		/** value of the roman digit */
		public int value;

		/** can the digit be used as a prefix */
		public boolean pre;

		/**
		 * Constructs a roman digit
		 * @param digit the roman digit
		 * @param value the value
		 * @param pre can it be used as a prefix
		 */
		RomanDigit(char digit, int value, boolean pre) {
			this.digit = digit;
			this.value = value;
			this.pre = pre;
		}
	}
	
	/**
	 * Array with Roman digits.
	 */
	private static final RomanDigit[] roman = {
		new RomanDigit('m', 1000, false),
		new RomanDigit('d', 500, false),
		new RomanDigit('c', 100, true),
		new RomanDigit('l', 50, false),
		new RomanDigit('x', 10, true),
		new RomanDigit('v', 5, false),
		new RomanDigit('i', 1, true)
	};
	
	/** 
	 * Changes an int into a lower case roman number.
	 * @param index the original number
	 * @return the roman number (lower case)
	 */
	public static final String getString(int index) {
		StringBuffer buf = new StringBuffer();

		// lower than 0 ? Add minus
		if (index < 0) {
			buf.append('-');
			index = -index;
		}

		// greater than 3000
		if (index > 3000) {
			buf.append('|');
			buf.append(getString(index / 1000));
			buf.append('|');
			// remainder
			index = index - (index / 1000) * 1000;
		}

		// number between 1 and 3000
		int pos = 0;
		while (true) {
			// loop over the array with values for m-d-c-l-x-v-i
			RomanDigit dig = roman[pos];
			// adding as many digits as we can
			while (index >= dig.value) {
				buf.append(dig.digit);
				index -= dig.value;
			}
			// we have the complete number
			if (index <= 0) {
				break;
			}
			// look for the next digit that can be used in a special way
			int j = pos;
			while (!roman[++j].pre);

			// does the special notation apply?
			if (index + roman[j].value >= dig.value) {
				buf.append(roman[j].digit).append(dig.digit);
				index -= dig.value - roman[j].value;
			}
			pos++;
		}
		return buf.toString();
	}
	
	/** 
	 * Changes an int into a lower case roman number.
	 * @param index the original number
	 * @return the roman number (lower case)
	 */
	public static final String getLowerCaseString(int index) {
		return getString(index);		
	}
	
	/** 
	 * Changes an int into an upper case roman number.
	 * @param index the original number
	 * @return the roman number (lower case)
	 */
	public static final String getUpperCaseString(int index) {
		return getString(index).toUpperCase();		
	}

	/** 
	 * Changes an int into a roman number.
	 * @param index the original number
	 * @return the roman number (lower case)
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
		for (int i = 1; i < 2000; i++) {
			System.out.println(getString(i));
		}
	}
}