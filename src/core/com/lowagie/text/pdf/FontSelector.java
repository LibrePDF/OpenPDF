/*
 * Copyright 2003 by Paulo Soares.
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

import java.util.ArrayList;
import com.lowagie.text.error_messages.MessageLocalization;

import com.lowagie.text.Chunk;
import com.lowagie.text.Font;
import com.lowagie.text.Phrase;
import com.lowagie.text.Utilities;

/** Selects the appropriate fonts that contain the glyphs needed to
 * render text correctly. The fonts are checked in order until the 
 * character is found.
 * <p>
 * The built in fonts "Symbol" and "ZapfDingbats", if used, have a special encoding
 * to allow the characters to be referred by Unicode.
 * @author Paulo Soares (psoares@consiste.pt)
 */
public class FontSelector {
    
    protected ArrayList fonts = new ArrayList();

    /**
     * Adds a <CODE>Font</CODE> to be searched for valid characters.
     * @param font the <CODE>Font</CODE>
     */    
    public void addFont(Font font) {
        if (font.getBaseFont() != null) {
            fonts.add(font);
            return;
        }
        BaseFont bf = font.getCalculatedBaseFont(true);
        Font f2 = new Font(bf, font.getSize(), font.getCalculatedStyle(), font.getColor());
        fonts.add(f2);
    }
    
    /**
     * Process the text so that it will render with a combination of fonts
     * if needed.
     * @param text the text
     * @return a <CODE>Phrase</CODE> with one or more chunks
     */    
    public Phrase process(String text) {
        int fsize = fonts.size();
        if (fsize == 0)
            throw new IndexOutOfBoundsException(MessageLocalization.getComposedMessage("no.font.is.defined"));
        char cc[] = text.toCharArray();
        int len = cc.length;
        StringBuffer sb = new StringBuffer();
        Font font = null;
        int lastidx = -1;
        Phrase ret = new Phrase();
        for (int k = 0; k < len; ++k) {
            char c = cc[k];
            if (c == '\n' || c == '\r') {
                sb.append(c);
                continue;
            }
            if (Utilities.isSurrogatePair(cc, k)) {
                int u = Utilities.convertToUtf32(cc, k);
                for (int f = 0; f < fsize; ++f) {
                    font = (Font)fonts.get(f);
                    if (font.getBaseFont().charExists(u)) {
                        if (lastidx != f) {
                            if (sb.length() > 0 && lastidx != -1) {
                                Chunk ck = new Chunk(sb.toString(), (Font)fonts.get(lastidx));
                                ret.add(ck);
                                sb.setLength(0);
                            }
                            lastidx = f;
                        }
                        sb.append(c);
                        sb.append(cc[++k]);
                        break;
                    }
                }
            }
            else {
                for (int f = 0; f < fsize; ++f) {
                    font = (Font)fonts.get(f);
                    if (font.getBaseFont().charExists(c)) {
                        if (lastidx != f) {
                            if (sb.length() > 0 && lastidx != -1) {
                                Chunk ck = new Chunk(sb.toString(), (Font)fonts.get(lastidx));
                                ret.add(ck);
                                sb.setLength(0);
                            }
                            lastidx = f;
                        }
                        sb.append(c);
                        break;
                    }
                }
            }
        }
        if (sb.length() > 0) {
            Chunk ck = new Chunk(sb.toString(), (Font)fonts.get(lastidx == -1 ? 0 : lastidx));
            ret.add(ck);
        }
        return ret;
    }
}
