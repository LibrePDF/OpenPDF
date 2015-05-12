/*
 * $Id: FontFactory.java 4065 2009-09-16 23:09:11Z psoares33 $
 * $Name$
 *
 * Copyright 2002 by Bruno Lowagie.
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

import java.awt.Color;
import java.util.Properties;
import java.util.Set;
import com.lowagie.text.error_messages.MessageLocalization;

import com.lowagie.text.pdf.BaseFont;

/**
 * If you are using True Type fonts, you can declare the paths of the different ttf- and ttc-files
 * to this static class first and then create fonts in your code using one of the static getFont-method
 * without having to enter a path as parameter.
 *
 * @author  Bruno Lowagie
 */

public final class FontFactory {
    
/** This is a possible value of a base 14 type 1 font */
    public static final String COURIER = BaseFont.COURIER;
    
/** This is a possible value of a base 14 type 1 font */
    public static final String COURIER_BOLD = BaseFont.COURIER_BOLD;
    
/** This is a possible value of a base 14 type 1 font */
    public static final String COURIER_OBLIQUE = BaseFont.COURIER_OBLIQUE;
    
/** This is a possible value of a base 14 type 1 font */
    public static final String COURIER_BOLDOBLIQUE = BaseFont.COURIER_BOLDOBLIQUE;
    
/** This is a possible value of a base 14 type 1 font */
    public static final String HELVETICA = BaseFont.HELVETICA;
    
/** This is a possible value of a base 14 type 1 font */
    public static final String HELVETICA_BOLD = BaseFont.HELVETICA_BOLD;
    
/** This is a possible value of a base 14 type 1 font */
    public static final String HELVETICA_OBLIQUE = BaseFont.HELVETICA_OBLIQUE;
    
/** This is a possible value of a base 14 type 1 font */
    public static final String HELVETICA_BOLDOBLIQUE = BaseFont.HELVETICA_BOLDOBLIQUE;
    
/** This is a possible value of a base 14 type 1 font */
    public static final String SYMBOL = BaseFont.SYMBOL;
    
/** This is a possible value of a base 14 type 1 font */
    public static final String TIMES = "Times";
    
/** This is a possible value of a base 14 type 1 font */
    public static final String TIMES_ROMAN = BaseFont.TIMES_ROMAN;
    
/** This is a possible value of a base 14 type 1 font */
    public static final String TIMES_BOLD = BaseFont.TIMES_BOLD;
    
/** This is a possible value of a base 14 type 1 font */
    public static final String TIMES_ITALIC = BaseFont.TIMES_ITALIC;
    
/** This is a possible value of a base 14 type 1 font */
    public static final String TIMES_BOLDITALIC = BaseFont.TIMES_BOLDITALIC;
    
/** This is a possible value of a base 14 type 1 font */
    public static final String ZAPFDINGBATS = BaseFont.ZAPFDINGBATS;
    
    private static FontFactoryImp fontImp = new FontFactoryImp();
    
/** This is the default encoding to use. */
    public static String defaultEncoding = BaseFont.WINANSI;
    
/** This is the default value of the <VAR>embedded</VAR> variable. */
    public static boolean defaultEmbedding = BaseFont.NOT_EMBEDDED;
    
/** Creates new FontFactory */
    private FontFactory() {
    }
    
/**
 * Constructs a <CODE>Font</CODE>-object.
 *
 * @param	fontname    the name of the font
 * @param	encoding    the encoding of the font
 * @param       embedded    true if the font is to be embedded in the PDF
 * @param	size	    the size of this font
 * @param	style	    the style of this font
 * @param	color	    the <CODE>Color</CODE> of this font.
 * @return the Font constructed based on the parameters
 */
    
    public static Font getFont(String fontname, String encoding, boolean embedded, float size, int style, Color color) {
        return fontImp.getFont(fontname, encoding, embedded, size, style, color);
    }
    
/**
 * Constructs a <CODE>Font</CODE>-object.
 *
 * @param	fontname    the name of the font
 * @param	encoding    the encoding of the font
 * @param       embedded    true if the font is to be embedded in the PDF
 * @param	size	    the size of this font
 * @param	style	    the style of this font
 * @param	color	    the <CODE>Color</CODE> of this font.
 * @param	cached 		true if the font comes from the cache or is added to
 * 				the cache if new, false if the font is always created new
 * @return the Font constructed based on the parameters
 */
    
    public static Font getFont(String fontname, String encoding, boolean embedded, float size, int style, Color color, boolean cached) {
        return fontImp.getFont(fontname, encoding, embedded, size, style, color, cached);
    }
    
/**
 * Constructs a <CODE>Font</CODE>-object.
 *
 * @param   attributes  the attributes of a <CODE>Font</CODE> object.
 * @return the Font constructed based on the attributes
 */
    
    public static Font getFont(Properties attributes) {
        fontImp.defaultEmbedding = defaultEmbedding;
        fontImp.defaultEncoding = defaultEncoding;
        return fontImp.getFont(attributes);
    }
    
/**
 * Constructs a <CODE>Font</CODE>-object.
 *
 * @param	fontname    the name of the font
 * @param	encoding    the encoding of the font
 * @param       embedded    true if the font is to be embedded in the PDF
 * @param	size	    the size of this font
 * @param	style	    the style of this font
 * @return the Font constructed based on the parameters
 */
    
    public static Font getFont(String fontname, String encoding, boolean embedded, float size, int style) {
        return getFont(fontname, encoding, embedded, size, style, null);
    }
    
/**
 * Constructs a <CODE>Font</CODE>-object.
 *
 * @param	fontname    the name of the font
 * @param	encoding    the encoding of the font
 * @param       embedded    true if the font is to be embedded in the PDF
 * @param	size	    the size of this font
 * @return the Font constructed based on the parameters
 */
    
    public static Font getFont(String fontname, String encoding, boolean embedded, float size) {
        return getFont(fontname, encoding, embedded, size, Font.UNDEFINED, null);
    }
    
/**
 * Constructs a <CODE>Font</CODE>-object.
 *
 * @param	fontname    the name of the font
 * @param	encoding    the encoding of the font
 * @param       embedded    true if the font is to be embedded in the PDF
 * @return the Font constructed based on the parameters
 */
    
    public static Font getFont(String fontname, String encoding, boolean embedded) {
        return getFont(fontname, encoding, embedded, Font.UNDEFINED, Font.UNDEFINED, null);
    }
    
/**
 * Constructs a <CODE>Font</CODE>-object.
 *
 * @param	fontname    the name of the font
 * @param	encoding    the encoding of the font
 * @param	size	    the size of this font
 * @param	style	    the style of this font
 * @param	color	    the <CODE>Color</CODE> of this font.
 * @return the Font constructed based on the parameters
 */
    
    public static Font getFont(String fontname, String encoding, float size, int style, Color color) {
        return getFont(fontname, encoding, defaultEmbedding, size, style, color);
    }
    
/**
 * Constructs a <CODE>Font</CODE>-object.
 *
 * @param	fontname    the name of the font
 * @param	encoding    the encoding of the font
 * @param	size	    the size of this font
 * @param	style	    the style of this font
 * @return the Font constructed based on the parameters
 */
    
    public static Font getFont(String fontname, String encoding, float size, int style) {
        return getFont(fontname, encoding, defaultEmbedding, size, style, null);
    }
    
/**
 * Constructs a <CODE>Font</CODE>-object.
 *
 * @param	fontname    the name of the font
 * @param	encoding    the encoding of the font
 * @param	size	    the size of this font
 * @return the Font constructed based on the parameters
 */
    
    public static Font getFont(String fontname, String encoding, float size) {
        return getFont(fontname, encoding, defaultEmbedding, size, Font.UNDEFINED, null);
    }
    
/**
 * Constructs a <CODE>Font</CODE>-object.
 *
 * @param	fontname    the name of the font
 * @param	encoding    the encoding of the font
 * @return the Font constructed based on the parameters
 */
    
    public static Font getFont(String fontname, String encoding) {
        return getFont(fontname, encoding, defaultEmbedding, Font.UNDEFINED, Font.UNDEFINED, null);
    }
    
/**
 * Constructs a <CODE>Font</CODE>-object.
 *
 * @param	fontname    the name of the font
 * @param	size	    the size of this font
 * @param	style	    the style of this font
 * @param	color	    the <CODE>Color</CODE> of this font.
 * @return the Font constructed based on the parameters
 */
    
    public static Font getFont(String fontname, float size, int style, Color color) {
        return getFont(fontname, defaultEncoding, defaultEmbedding, size, style, color);
    }
    
/**
 * Constructs a <CODE>Font</CODE>-object.
 *
 * @param	fontname    the name of the font
 * @param	size	    the size of this font
 * @param	color	    the <CODE>Color</CODE> of this font.
 * @return the Font constructed based on the parameters
 * @since 2.1.0
 */
    
    public static Font getFont(String fontname, float size, Color color) {
        return getFont(fontname, defaultEncoding, defaultEmbedding, size, Font.UNDEFINED, color);
    }
    
/**
 * Constructs a <CODE>Font</CODE>-object.
 *
 * @param	fontname    the name of the font
 * @param	size	    the size of this font
 * @param	style	    the style of this font
 * @return the Font constructed based on the parameters
 */
    
    public static Font getFont(String fontname, float size, int style) {
        return getFont(fontname, defaultEncoding, defaultEmbedding, size, style, null);
    }
    
/**
 * Constructs a <CODE>Font</CODE>-object.
 *
 * @param	fontname    the name of the font
 * @param	size	    the size of this font
 * @return the Font constructed based on the parameters
 */
    
    public static Font getFont(String fontname, float size) {
        return getFont(fontname, defaultEncoding, defaultEmbedding, size, Font.UNDEFINED, null);
    }
    
/**
 * Constructs a <CODE>Font</CODE>-object.
 *
 * @param	fontname    the name of the font
 * @return the Font constructed based on the parameters
 */
    
    public static Font getFont(String fontname) {
        return getFont(fontname, defaultEncoding, defaultEmbedding, Font.UNDEFINED, Font.UNDEFINED, null);
    }
    
    /**
     * Register a font by giving explicitly the font family and name.
     * @param familyName the font family
     * @param fullName the font name
     * @param path the font path
     */
    public void registerFamily(String familyName, String fullName, String path) {
        fontImp.registerFamily(familyName, fullName, path);
    }
    
/**
 * Register a ttf- or a ttc-file.
 *
 * @param   path    the path to a ttf- or ttc-file
 */
    
    public static void register(String path) {
        register(path, null);
    }
    
/**
 * Register a font file and use an alias for the font contained in it.
 *
 * @param   path    the path to a font file
 * @param   alias   the alias you want to use for the font
 */
    
    public static void register(String path, String alias) {
        fontImp.register(path, alias);
    }

    /** Register all the fonts in a directory.
     * @param dir the directory
     * @return the number of fonts registered
     */    
    public static int registerDirectory(String dir) {
        return fontImp.registerDirectory(dir);
    }

    /**
     * Register all the fonts in a directory and possibly its subdirectories.
     * @param dir the directory
     * @param scanSubdirectories recursively scan subdirectories if <code>true</true>
     * @return the number of fonts registered
     * @since 2.1.2
     */
    public static int registerDirectory(String dir, boolean scanSubdirectories) {
        return fontImp.registerDirectory(dir, scanSubdirectories);
    }

    /** Register fonts in some probable directories. It usually works in Windows,
     * Linux and Solaris.
     * @return the number of fonts registered
     */    
    public static int registerDirectories() {
        return fontImp.registerDirectories();
    }

/**
 * Gets a set of registered fontnames.
 * @return a set of registered fonts
 */
    
    public static Set getRegisteredFonts() {
        return fontImp.getRegisteredFonts();
    }
    
/**
 * Gets a set of registered fontnames.
 * @return a set of registered font families
 */
    
    public static Set getRegisteredFamilies() {
        return fontImp.getRegisteredFamilies();
    }
    
/**
 * Gets a set of registered fontnames.
 * @param fontname of a font that may or may not be registered
 * @return true if a given font is registered
 */
    
    public static boolean contains(String fontname) {
        return fontImp.isRegistered(fontname);
    }
    
/**
 * Checks if a certain font is registered.
 *
 * @param   fontname    the name of the font that has to be checked.
 * @return  true if the font is found
 */
    
    public static boolean isRegistered(String fontname) {
        return fontImp.isRegistered(fontname);
    }
    
    /**
     * Gets the font factory implementation.
     * @return the font factory implementation
     */    
    public static FontFactoryImp getFontImp() {
        return fontImp;
    }
    
    /**
     * Sets the font factory implementation.
     * @param fontImp the font factory implementation
     */    
    public static void setFontImp(FontFactoryImp fontImp) {
        if (fontImp == null)
            throw new NullPointerException(MessageLocalization.getComposedMessage("fontfactoryimp.cannot.be.null"));
        FontFactory.fontImp = fontImp;
    }
}
