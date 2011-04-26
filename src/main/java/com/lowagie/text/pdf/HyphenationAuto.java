/*
 * Copyright 2002 Paulo Soares
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

import com.lowagie.text.pdf.hyphenation.Hyphenation;
import com.lowagie.text.pdf.hyphenation.Hyphenator;

/** Hyphenates words automatically accordingly to the language and country.
 * The hyphenator engine was taken from FOP and uses the TEX patterns. If a language
 * is not provided and a TEX pattern for it exists, it can be easily adapted.
 *
 * @author Paulo Soares (psoares@consiste.pt)
 */
public class HyphenationAuto implements HyphenationEvent {

    /** The hyphenator engine.
     */    
    protected Hyphenator hyphenator;
    /** The second part of the hyphenated word.
     */    
    protected String post;
    
    /** Creates a new hyphenation instance usable in <CODE>Chunk</CODE>.
     * @param lang the language ("en" for English, for example)
     * @param country the country ("GB" for Great-Britain or "none" for no country, for example)
     * @param leftMin the minimum number of letters before the hyphen
     * @param rightMin the minimum number of letters after the hyphen
     */    
    public HyphenationAuto(String lang, String country, int leftMin, int rightMin) {
        hyphenator = new Hyphenator(lang, country, leftMin, rightMin);
    }

    /** Gets the hyphen symbol.
     * @return the hyphen symbol
     */    
    public String getHyphenSymbol() {
        return "-";
    }
    
    /** Hyphenates a word and returns the first part of it. To get
     * the second part of the hyphenated word call <CODE>getHyphenatedWordPost()</CODE>.
     * @param word the word to hyphenate
     * @param font the font used by this word
     * @param fontSize the font size used by this word
     * @param remainingWidth the width available to fit this word in
     * @return the first part of the hyphenated word including
     * the hyphen symbol, if any
     */    
    public String getHyphenatedWordPre(String word, BaseFont font, float fontSize, float remainingWidth) {
        post = word;
        String hyphen = getHyphenSymbol();
        float hyphenWidth = font.getWidthPoint(hyphen, fontSize);
        if (hyphenWidth > remainingWidth)
            return "";
        Hyphenation hyphenation = hyphenator.hyphenate(word);
        if (hyphenation == null) {
            return "";
        }
        int len = hyphenation.length();
        int k;
        for (k = 0; k < len; ++k) {
            if (font.getWidthPoint(hyphenation.getPreHyphenText(k), fontSize) + hyphenWidth > remainingWidth)
                break;
        }
        --k;
        if (k < 0)
            return "";
        post = hyphenation.getPostHyphenText(k);
        return hyphenation.getPreHyphenText(k) + hyphen;
    }
    
    /** Gets the second part of the hyphenated word. Must be called
     * after <CODE>getHyphenatedWordPre()</CODE>.
     * @return the second part of the hyphenated word
     */    
    public String getHyphenatedWordPost() {
        return post;
    }
    
}
