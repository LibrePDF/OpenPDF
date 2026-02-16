/*
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */
package org.openpdf.renderer.font;

import java.awt.Font;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;

import org.openpdf.renderer.PDFObject;

/**
 * This class represents the 14 built-in fonts.  It reads these fonts
 * from files in the "res" directory, as specified in
 * BaseNames.properties.
 */
public class BuiltinFont extends Type1Font {

    /** the properties file */
    private static Properties props;
    /** the fonts themselves */
    private static Map fonts;
    /** the names of the 14 base fonts */
    private static final String[] baseFonts = {
        "Courier", "Courier-Bold", "Courier-BoldOblique", "Courier-Oblique",
        "Helvetica", "Helvetica-Bold", "Helvetica-BoldOblique",
        "Helvetica-Oblique", "Times-Roman", "Times-Bold", "Times-BoldItalic",
        "Times-Italic", "Symbol", "ZapfDingbats"
    };
    /** fonts others (e.g. Acrobad PDFWriter 3.02 for Windows) assume
     *  are there, even though they're not in the spec.  Grrr...
     *
     * the format is <Name_in_PDF> <Builtin_To_Use>
     */
    private static final String[] mappedFonts = {
        // map arial to helvetica
        "Arial", "Helvetica",
        "Arial,Bold", "Helvetica-Bold",
        "Arial,BoldItalic", "Helvetica-BoldOblique",
        "Arial,Italic", "Helvetica-Oblique",
        // map ArialMT to Helvetica
        "ArialMT", "Helvetica",
        "Arial-BoldMT", "Helvetica-Bold",
        "Arial-BoldItalicMT", "Helvetica-BoldOblique",
        "Arial-ItalicMT", "Helvetica-Oblique",
        // map TimesNewRoman to Times
        "TimesNewRoman", "Times-Roman",
        "TimesNewRoman,Bold", "Times-Bold",
        "TimesNewRoman,BoldItalic", "Times-BoldItalic",
        "TimesNewRoman,Italic", "Times-Italic",
        // map TimesNewRomanPSMT to Times
        "TimesNewRomanPSMT", "Times-Roman",
        "TimesNewRomanPS-BoldMT", "Times-Bold",
        "TimesNewRomanPS-BoldItalicMT", "Times-BoldItalic",
        "TimesNewRomanPS-ItalicMT", "Times-Italic",
        //Map some variants of Courier
        "Courier,Bold", "Courier-Bold",
        "Courier,BoldItalic", "Courier-BoldOblique",
        "Courier,Italic", "Courier-Oblique",
        // map CourierNew to Courier
        "CourierNew", "Courier",
        "CourierNew,Bold", "Courier-Bold",
        "CourierNew,BoldItalic", "Courier-BoldOblique",
        "CourierNew,Italic", "Courier-Oblique",
    };

    /**
     * Create a new Builtin object based on the name of a built-in font
     *
     * This must be the name of one of the 14 built-in fonts!
     *
     * @param baseFont the name of the font, from the PDF file
     * @param fontObj the object containing font information
     */
    public BuiltinFont(String baseFont, PDFObject fontObj) throws IOException {
        super(baseFont, fontObj, null);

        parseFont(baseFont);
    }

    /**
     * create a new BuiltingFont object based on a description of the
     * font from the PDF file. Parse the description for key information
     * and use that to generate an appropriate font.
     */
    public BuiltinFont(String baseFont, PDFObject fontObj,
            PDFFontDescriptor descriptor)
            throws IOException {
        super(baseFont, fontObj, descriptor);

        String fontName = descriptor.getFontName();

        // check if it's one of the 14 base fonts
        for (int i = 0; i < baseFonts.length; i++) {
            if (fontName.equalsIgnoreCase(baseFonts[i])) {
                parseFont(fontName);
                return;
            }
        }

        // check if it's a mapped font
        for (int i = 0; i < mappedFonts.length; i += 2) {
            if (fontName.equalsIgnoreCase(mappedFonts[i])) {
                parseFont(mappedFonts[i + 1]);
                return;
            }
        }

        int flags = descriptor.getFlags();
        int style = ((flags & PDFFontDescriptor.FORCEBOLD) != 0) ? Font.BOLD : Font.PLAIN;

        if (fontName.indexOf("Bold") > 0) {
            style |= Font.BOLD;
        }
        if ((descriptor.getItalicAngle() != 0) ||
        		((flags & (PDFFontDescriptor.SCRIPT | PDFFontDescriptor.ITALIC)) != 0)) {
        	style |= Font.ITALIC;
        }
        String name = null;

        if ((flags & PDFFontDescriptor.FIXED_PITCH) != 0) { // fixed width
            if (((style & Font.BOLD) > 0) && ((style & Font.ITALIC) > 0)) {
                name = "Courier-BoldOblique";
            } else if ((style & Font.BOLD) > 0) {
                name = "Courier-Bold";
            } else if ((style & Font.ITALIC) > 0) {
                name = "Courier-Oblique";
            } else {
                name = "Courier";
            }
        } else if ((flags & PDFFontDescriptor.SERIF) != 0) {  // serif font
            if (((style & Font.BOLD) > 0) && ((style & Font.ITALIC) > 0)) {
                name = "Times-BoldItalic";
            } else if ((style & Font.BOLD) > 0) {
                name = "Times-Bold";
            } else if ((style & Font.ITALIC) > 0) {
                name = "Times-Italic";
            } else {
                name = "Times-Roman";
            }
        } else {
            if (((style & Font.BOLD) > 0) && ((style & Font.ITALIC) > 0)) {
                name = "Helvetica-BoldOblique";
            } else if ((style & Font.BOLD) > 0) {
                name = "Helvetica-Bold";
            } else if ((style & Font.ITALIC) > 0) {
                name = "Helvetica-Oblique";
            } else {
                name = "Helvetica";
            }
        }

        parseFont(name);
    }

    /**
     * Parse a font given only the name of a builtin font
     */
    private void parseFont(String baseFont) throws IOException {
        // load the base fonts properties files, if it isn't already loaded
        if (props == null) {
            props = new Properties();
            String path = "/BaseFonts.properties";
            try (InputStream in = BuiltinFont.class.getResourceAsStream(path)) {
                if (in == null) {
                    throw new IOException("Properties file not found in classpath: " + path);
                }
                props.load(in);
            }
        }


        // make sure we're a known font
        if (!props.containsKey(baseFont + ".file")) {
            throw new IllegalArgumentException("Unknown Base Font: " + baseFont);
        }

        // get the font information from the properties file
        String file = props.getProperty(baseFont + ".file");

        // the size of the file
        int length = Integer.parseInt(props.getProperty(baseFont + ".length"));
        // the size of the unencrypted portion
        int length1 = 0;
        // the size of the encrypted portion
        int length2 = 0;

        // read the data from the file
        byte[] data = new byte[length];
        InputStream fontStream = NativeFont.class.getResourceAsStream("/" + file);

        int cur = 0;
        while (cur < length) {
            cur += fontStream.read(data, cur, length - cur);
        }
        fontStream.close();

        // are we a pfb file?
        if ((data[0] & 0xff) == 0x80) {
            // read lengths from the file
            length1 = (data[2] & 0xff);
            length1 |= (data[3] & 0xff) << 8;
            length1 |= (data[4] & 0xff) << 16;
            length1 |= (data[5] & 0xff) << 24;
            length1 += 6;

            length2 = (data[length1 + 2] & 0xff);
            length2 |= (data[length1 + 3] & 0xff) << 8;
            length2 |= (data[length1 + 4] & 0xff) << 16;
            length2 |= (data[length1 + 5] & 0xff) << 24;
            length1 += 6;
        } else {
            // get the values from the properties file
            length1 = Integer.parseInt(props.getProperty(baseFont + ".length1"));

            if (props.containsKey(baseFont + ".length2")) {
                length2 = Integer.parseInt(props.getProperty(baseFont + ".lenth2"));
            } else {
                length2 = length - length1;
            }
        }

        parseFont(data, length1, length2);
    }
}
