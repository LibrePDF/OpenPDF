/*
 * {{{ header & license
 * Copyright (c) 2006 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.openpdf.pdf;

import org.openpdf.text.DocumentException;
import org.openpdf.text.pdf.BaseFont;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openpdf.css.constants.IdentValue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Use this class if you need to load iTextAsian fonts in addition to default fonts loaded by {@link ITextRenderer}
 */
public class CJKFontResolver extends ITextFontResolver {
    private static final Logger log = LoggerFactory.getLogger(CJKFontResolver.class);
    
    @Override
    protected Map<String, FontFamily> loadFonts() {
        Map<String, FontFamily> result = super.loadFonts();
        result.putAll(loadCJKFonts());
        return result;
    }

    // fontFamilyName, fontName, encoding
    private static final String[][] cjkFonts = {
            {"STSong-Light-H", "STSong-Light", "UniGB-UCS2-H"},
            {"STSong-Light-V", "STSong-Light", "UniGB-UCS2-V"},
            {"STSongStd-Light-H", "STSongStd-Light", "UniGB-UCS2-H"},
            {"STSongStd-Light-V", "STSongStd-Light", "UniGB-UCS2-V"},
            {"MHei-Medium-H", "MHei-Medium", "UniCNS-UCS2-H"},
            {"MHei-Medium-V", "MHei-Medium", "UniCNS-UCS2-V"},
            {"MSung-Light-H", "MSung-Light", "UniCNS-UCS2-H"},
            {"MSung-Light-V", "MSung-Light", "UniCNS-UCS2-V"},
            {"MSungStd-Light-H", "MSungStd-Light", "UniCNS-UCS2-H"},
            {"MSungStd-Light-V", "MSungStd-Light", "UniCNS-UCS2-V"},
            {"HeiseiMin-W3-H", "HeiseiMin-W3", "UniJIS-UCS2-H"},
            {"HeiseiMin-W3-V", "HeiseiMin-W3", "UniJIS-UCS2-V"},
            {"HeiseiKakuGo-W5-H", "HeiseiKakuGo-W5", "UniJIS-UCS2-H"},
            {"HeiseiKakuGo-W5-V", "HeiseiKakuGo-W5", "UniJIS-UCS2-V"},
            {"KozMinPro-Regular-H", "KozMinPro-Regular", "UniJIS-UCS2-HW-H"},
            {"KozMinPro-Regular-V", "KozMinPro-Regular", "UniJIS-UCS2-HW-V"},
            {"HYGoThic-Medium-H", "HYGoThic-Medium", "UniKS-UCS2-H"},
            {"HYGoThic-Medium-V", "HYGoThic-Medium", "UniKS-UCS2-V"},
            {"HYSMyeongJo-Medium-H", "HYSMyeongJo-Medium", "UniKS-UCS2-H"},
            {"HYSMyeongJo-Medium-V", "HYSMyeongJo-Medium", "UniKS-UCS2-V"},
            {"HYSMyeongJoStd-Medium-H", "HYSMyeongJoStd-Medium", "UniKS-UCS2-H"},
            {"HYSMyeongJoStd-Medium-V", "HYSMyeongJoStd-Medium", "UniKS-UCS2-V"}
    };

    /**
     * Try and load the iTextAsian fonts
     */
    private Map<String, FontFamily> loadCJKFonts() {
        Map<String, FontFamily> fontFamilyMap = new HashMap<>();

        for (String[] cjkFont : cjkFonts) {
            String fontFamilyName = cjkFont[0];
            String fontName = cjkFont[1];
            String encoding = cjkFont[2];

            try {
                FontFamily fontFamily = addCJKFont(fontFamilyName, fontName, encoding);
                fontFamilyMap.put(fontFamilyName, fontFamily);
            }
            catch (DocumentException | IOException e) {
                log.error("Failed to load font {} {} {}: {}", fontFamilyName, fontName, encoding, e.toString());
            }
        }
        return fontFamilyMap;
    }

    private FontFamily addCJKFont(String fontFamilyName, String fontName, String encoding) throws DocumentException, IOException {
        FontFamily fontFamily = new FontFamily(fontFamilyName);

        fontFamily.addFontDescription(new FontDescription(BaseFont.createFont(fontName+",BoldItalic", encoding, false), IdentValue.OBLIQUE, 700));
        fontFamily.addFontDescription(new FontDescription(BaseFont.createFont(fontName+",Italic", encoding, false), IdentValue.OBLIQUE, 400));
        fontFamily.addFontDescription(new FontDescription(BaseFont.createFont(fontName+",Bold", encoding, false), IdentValue.NORMAL, 700));
        fontFamily.addFontDescription(new FontDescription(BaseFont.createFont(fontName, encoding, false), IdentValue.NORMAL, 400));

        return fontFamily;
    }
}
