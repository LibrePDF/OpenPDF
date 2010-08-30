/*
 * $Id: SplitCharacter.java 3374 2008-05-12 18:42:56Z xlv $
 *
 * Copyright 2001, 2002 by Paulo Soares
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

package com.lowagie.text;

import com.lowagie.text.pdf.PdfChunk;

/** Interface for customizing the split character.
 *
 * @author Paulo Soares (psoares@consiste.pt)
 */

public interface SplitCharacter {
    
    /**
     * Returns <CODE>true</CODE> if the character can split a line. The splitting implementation
     * is free to look ahead or look behind characters to make a decision.
     * <p>
     * The default implementation is:
     * <p>
     * <pre>
     * public boolean isSplitCharacter(int start, int current, int end, char[] cc, PdfChunk[] ck) {
     *    char c;
     *    if (ck == null)
     *        c = cc[current];
     *    else
     *        c = (char) ck[Math.min(current, ck.length - 1)].getUnicodeEquivalent(cc[current]);
     *    if (c <= ' ' || c == '-') {
     *        return true;
     *    }
     *    if (c < 0x2e80)
     *        return false;
     *    return ((c >= 0x2e80 && c < 0xd7a0)
     *    || (c >= 0xf900 && c < 0xfb00)
     *    || (c >= 0xfe30 && c < 0xfe50)
     *    || (c >= 0xff61 && c < 0xffa0));
     * }
     * </pre>
     * @param start the lower limit of <CODE>cc</CODE> inclusive
     * @param current the pointer to the character in <CODE>cc</CODE>
     * @param end the upper limit of <CODE>cc</CODE> exclusive
     * @param cc an array of characters at least <CODE>end</CODE> sized
     * @param ck an array of <CODE>PdfChunk</CODE>. The main use is to be able to call
     * {@link PdfChunk#getUnicodeEquivalent(int)}. It may be <CODE>null</CODE>
     * or shorter than <CODE>end</CODE>. If <CODE>null</CODE> no conversion takes place.
     * If shorter than <CODE>end</CODE> the last element is used
     * @return <CODE>true</CODE> if the character(s) can split a line
     */
    public boolean isSplitCharacter(int start, int current, int end, char cc[], PdfChunk ck[]);
}
