/*
 * $Id: GreekAlphabetFactory.java 3373 2008-05-12 16:21:24Z xlv $
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
 * https://github.com/LibrePDF/OpenPDF
 */
package com.lowagie.text.factories;

import com.lowagie.text.SpecialSymbol;

/**
 * This class can produce String combinations representing a number built with
 * Greek letters (from alpha to omega, then alpha alpha, alpha beta, alpha gamma).
 * We are aware of the fact that the original Greek numbering is different;
 * See http://www.cogsci.indiana.edu/farg/harry/lan/grknum.htm#ancient
 * but this isn't implemented yet; the main reason being the fact that we
 * need a font that has the obsolete Greek characters qoppa and sampi.
 *
 * @since 2.0.7 (was called GreekNumberFactory in earlier versions)
 */
public class GreekAlphabetFactory {
    /**
     * Changes an int into a lower case Greek letter combination.
     * @param index the original number
     * @return the letter combination
     */
    public static final String getString(int index) {
        return getString(index, true);
    }

    /**
     * Changes an int into a lower case Greek letter combination.
     * @param index the original number
     * @return the letter combination
     */
    public static final String getLowerCaseString(int index) {
        return getString(index);
    }

    /**
     * Changes an int into a upper case Greek letter combination.
     * @param index the original number
     * @return the letter combination
     */
    public static final String getUpperCaseString(int index) {
        return getString(index).toUpperCase();
    }

    /**
     * Changes an int into a Greek letter combination.
     * @param index the original number
     * @param lowercase true for lowercase, false for uppercase
     * @return the letter combination
     */
    public static final String getString(int index, boolean lowercase) {
        if (index < 1) return "";
        index--;

        int bytes = 1;
        int start = 0;
        int symbols = 24;
           while(index >= symbols + start) {
               bytes++;
               start += symbols;
               symbols *= 24;
           }

           int c = index - start;
           char[] value = new char[bytes];
           while(bytes > 0) {
               bytes--;
               value[bytes] = (char)(c % 24);
               if (value[bytes] > 16) value[bytes]++;
               value[bytes] += (lowercase ? 945 : 913);
               value[bytes] = SpecialSymbol.getCorrespondingSymbol(value[bytes]);
               c /= 24;
           }

           return String.valueOf(value);
    }
}
