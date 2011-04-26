/*
 * $Id: FontFactoryImp.java 4063 2009-09-13 19:02:46Z psoares33 $
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
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Properties;
import java.util.Set;

import com.lowagie.text.html.Markup;
import com.lowagie.text.pdf.BaseFont;

/**
 * If you are using True Type fonts, you can declare the paths of the different ttf- and ttc-files
 * to this class first and then create fonts in your code using one of the getFont method
 * without having to enter a path as parameter.
 *
 * @author  Bruno Lowagie
 */

public class FontFactoryImp implements FontProvider {
        
/** This is a map of postscriptfontnames of True Type fonts and the path of their ttf- or ttc-file. */
    private Properties trueTypeFonts = new Properties();
    
    private static String[] TTFamilyOrder = {
        "3", "1", "1033",
        "3", "0", "1033",
        "1", "0", "0",
        "0", "3", "0"
    };

/** This is a map of fontfamilies. */
    private Hashtable fontFamilies = new Hashtable();
    
/** This is the default encoding to use. */
    public String defaultEncoding = BaseFont.WINANSI;
    
/** This is the default value of the <VAR>embedded</VAR> variable. */
    public boolean defaultEmbedding = BaseFont.NOT_EMBEDDED;
    
/** Creates new FontFactory */
    public FontFactoryImp() {
        trueTypeFonts.setProperty(FontFactory.COURIER.toLowerCase(), FontFactory.COURIER);
        trueTypeFonts.setProperty(FontFactory.COURIER_BOLD.toLowerCase(), FontFactory.COURIER_BOLD);
        trueTypeFonts.setProperty(FontFactory.COURIER_OBLIQUE.toLowerCase(), FontFactory.COURIER_OBLIQUE);
        trueTypeFonts.setProperty(FontFactory.COURIER_BOLDOBLIQUE.toLowerCase(), FontFactory.COURIER_BOLDOBLIQUE);
        trueTypeFonts.setProperty(FontFactory.HELVETICA.toLowerCase(), FontFactory.HELVETICA);
        trueTypeFonts.setProperty(FontFactory.HELVETICA_BOLD.toLowerCase(), FontFactory.HELVETICA_BOLD);
        trueTypeFonts.setProperty(FontFactory.HELVETICA_OBLIQUE.toLowerCase(), FontFactory.HELVETICA_OBLIQUE);
        trueTypeFonts.setProperty(FontFactory.HELVETICA_BOLDOBLIQUE.toLowerCase(), FontFactory.HELVETICA_BOLDOBLIQUE);
        trueTypeFonts.setProperty(FontFactory.SYMBOL.toLowerCase(), FontFactory.SYMBOL);
        trueTypeFonts.setProperty(FontFactory.TIMES_ROMAN.toLowerCase(), FontFactory.TIMES_ROMAN);
        trueTypeFonts.setProperty(FontFactory.TIMES_BOLD.toLowerCase(), FontFactory.TIMES_BOLD);
        trueTypeFonts.setProperty(FontFactory.TIMES_ITALIC.toLowerCase(), FontFactory.TIMES_ITALIC);
        trueTypeFonts.setProperty(FontFactory.TIMES_BOLDITALIC.toLowerCase(), FontFactory.TIMES_BOLDITALIC);
        trueTypeFonts.setProperty(FontFactory.ZAPFDINGBATS.toLowerCase(), FontFactory.ZAPFDINGBATS);

        ArrayList tmp;
        tmp = new ArrayList();
        tmp.add(FontFactory.COURIER);
        tmp.add(FontFactory.COURIER_BOLD);
        tmp.add(FontFactory.COURIER_OBLIQUE);
        tmp.add(FontFactory.COURIER_BOLDOBLIQUE);
        fontFamilies.put(FontFactory.COURIER.toLowerCase(), tmp);
        tmp = new ArrayList();
        tmp.add(FontFactory.HELVETICA);
        tmp.add(FontFactory.HELVETICA_BOLD);
        tmp.add(FontFactory.HELVETICA_OBLIQUE);
        tmp.add(FontFactory.HELVETICA_BOLDOBLIQUE);
        fontFamilies.put(FontFactory.HELVETICA.toLowerCase(), tmp);
        tmp = new ArrayList();
        tmp.add(FontFactory.SYMBOL);
        fontFamilies.put(FontFactory.SYMBOL.toLowerCase(), tmp);
        tmp = new ArrayList();
        tmp.add(FontFactory.TIMES_ROMAN);
        tmp.add(FontFactory.TIMES_BOLD);
        tmp.add(FontFactory.TIMES_ITALIC);
        tmp.add(FontFactory.TIMES_BOLDITALIC);
        fontFamilies.put(FontFactory.TIMES.toLowerCase(), tmp);
        fontFamilies.put(FontFactory.TIMES_ROMAN.toLowerCase(), tmp);
        tmp = new ArrayList();
        tmp.add(FontFactory.ZAPFDINGBATS);
        fontFamilies.put(FontFactory.ZAPFDINGBATS.toLowerCase(), tmp);
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
    public Font getFont(String fontname, String encoding, boolean embedded, float size, int style, Color color) {
        return getFont(fontname, encoding, embedded, size, style, color, true);
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
    public Font getFont(String fontname, String encoding, boolean embedded, float size, int style, Color color, boolean cached) {
    	if (fontname == null) return new Font(Font.UNDEFINED, size, style, color);
        String lowercasefontname = fontname.toLowerCase();
        ArrayList tmp = (ArrayList) fontFamilies.get(lowercasefontname);
        if (tmp != null) {
            // some bugs were fixed here by Daniel Marczisovszky
            int s = style == Font.UNDEFINED ? Font.NORMAL : style;
            int fs = Font.NORMAL;
            boolean found = false;
            for (Iterator i = tmp.iterator(); i.hasNext(); ) {
                String f = (String) i.next();
                String lcf = f.toLowerCase();
                fs = Font.NORMAL;
                if (lcf.toLowerCase().indexOf("bold") != -1) fs |= Font.BOLD;
                if (lcf.toLowerCase().indexOf("italic") != -1 || lcf.toLowerCase().indexOf("oblique") != -1) fs |= Font.ITALIC;
                if ((s & Font.BOLDITALIC) == fs) {
                    fontname = f;
                    found = true;
                    break;
                }
            }
            if (style != Font.UNDEFINED && found) {
                style &= ~fs;
            }
        }
        BaseFont basefont = null;
        try {
            try {
                // the font is a type 1 font or CJK font
                basefont = BaseFont.createFont(fontname, encoding, embedded, cached, null, null, true);
            }
            catch(DocumentException de) {
            }
            if (basefont == null) {
                // the font is a true type font or an unknown font
                fontname = trueTypeFonts.getProperty(fontname.toLowerCase());
                // the font is not registered as truetype font
                if (fontname == null) return new Font(Font.UNDEFINED, size, style, color);
                // the font is registered as truetype font
                basefont = BaseFont.createFont(fontname, encoding, embedded, cached, null, null);
            }
        }
        catch(DocumentException de) {
            // this shouldn't happen
            throw new ExceptionConverter(de);
        }
        catch(IOException ioe) {
            // the font is registered as a true type font, but the path was wrong
            return new Font(Font.UNDEFINED, size, style, color);
        }
        catch(NullPointerException npe) {
            // null was entered as fontname and/or encoding
            return new Font(Font.UNDEFINED, size, style, color);
        }
        return new Font(basefont, size, style, color);
    }
    
    
/**
 * Constructs a <CODE>Font</CODE>-object.
 *
 * @param   attributes  the attributes of a <CODE>Font</CODE> object.
 * @return the Font constructed based on the attributes
 */
    
    public Font getFont(Properties attributes) {
        String fontname = null;
        String encoding = defaultEncoding;
        boolean embedded = defaultEmbedding;
        float size = Font.UNDEFINED;
        int style = Font.NORMAL;
        Color color = null;
        String value = attributes.getProperty(Markup.HTML_ATTR_STYLE);
        if (value != null && value.length() > 0) {
            Properties styleAttributes = Markup.parseAttributes(value);
            if (styleAttributes.isEmpty()) {
                attributes.put(Markup.HTML_ATTR_STYLE, value);
            }
            else {
                fontname = styleAttributes.getProperty(Markup.CSS_KEY_FONTFAMILY);
                if (fontname != null) {
                    String tmp;
                    while (fontname.indexOf(',') != -1) {
                        tmp = fontname.substring(0, fontname.indexOf(','));
                        if (isRegistered(tmp)) {
                            fontname = tmp;
                        }
                        else {
                            fontname = fontname.substring(fontname.indexOf(',') + 1);
                        }
                    }
                }
                if ((value = styleAttributes.getProperty(Markup.CSS_KEY_FONTSIZE)) != null) {
                    size = Markup.parseLength(value);
                }
                if ((value = styleAttributes.getProperty(Markup.CSS_KEY_FONTWEIGHT)) != null) {
                    style |= Font.getStyleValue(value);
                }
                if ((value = styleAttributes.getProperty(Markup.CSS_KEY_FONTSTYLE)) != null) {
                    style |= Font.getStyleValue(value);
                }
                if ((value = styleAttributes.getProperty(Markup.CSS_KEY_COLOR)) != null) {
                    color = Markup.decodeColor(value);
                }
                attributes.putAll(styleAttributes);
                for (Enumeration e = styleAttributes.keys(); e.hasMoreElements();) {
                    Object o = e.nextElement();
                    attributes.put(o, styleAttributes.get(o));
                }
            }
        }
        if ((value = attributes.getProperty(ElementTags.ENCODING)) != null) {
            encoding = value;
        }
        if ("true".equals(attributes.getProperty(ElementTags.EMBEDDED))) {
            embedded = true;
        }
        if ((value = attributes.getProperty(ElementTags.FONT)) != null) {
            fontname = value;
        }
        if ((value = attributes.getProperty(ElementTags.SIZE)) != null) {
            size = Markup.parseLength(value);
        }
        if ((value = attributes.getProperty(Markup.HTML_ATTR_STYLE)) != null) {
            style |= Font.getStyleValue(value);
        }
        if ((value = attributes.getProperty(ElementTags.STYLE)) != null) {
            style |= Font.getStyleValue(value);
        }
        String r = attributes.getProperty(ElementTags.RED);
        String g = attributes.getProperty(ElementTags.GREEN);
        String b = attributes.getProperty(ElementTags.BLUE);
        if (r != null || g != null || b != null) {
            int red = 0;
            int green = 0;
            int blue = 0;
            if (r != null) red = Integer.parseInt(r);
            if (g != null) green = Integer.parseInt(g);
            if (b != null) blue = Integer.parseInt(b);
            color = new Color(red, green, blue);
        }
        else if ((value = attributes.getProperty(ElementTags.COLOR)) != null) {
            color = Markup.decodeColor(value);
        }
        if (fontname == null) {
            return getFont(null, encoding, embedded, size, style, color);
        }
        return getFont(fontname, encoding, embedded, size, style, color);
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
    
    public Font getFont(String fontname, String encoding, boolean embedded, float size, int style) {
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
    
    public Font getFont(String fontname, String encoding, boolean embedded, float size) {
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
    
    public Font getFont(String fontname, String encoding, boolean embedded) {
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
    
    public Font getFont(String fontname, String encoding, float size, int style, Color color) {
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
    
    public Font getFont(String fontname, String encoding, float size, int style) {
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
    
    public Font getFont(String fontname, String encoding, float size) {
        return getFont(fontname, encoding, defaultEmbedding, size, Font.UNDEFINED, null);
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
    
    public Font getFont(String fontname, float size, Color color) {
        return getFont(fontname, defaultEncoding, defaultEmbedding, size, Font.UNDEFINED, color);
    }
    
/**
 * Constructs a <CODE>Font</CODE>-object.
 *
 * @param	fontname    the name of the font
 * @param	encoding    the encoding of the font
 * @return the Font constructed based on the parameters
 */
    
    public Font getFont(String fontname, String encoding) {
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
    
    public Font getFont(String fontname, float size, int style, Color color) {
        return getFont(fontname, defaultEncoding, defaultEmbedding, size, style, color);
    }
    
/**
 * Constructs a <CODE>Font</CODE>-object.
 *
 * @param	fontname    the name of the font
 * @param	size	    the size of this font
 * @param	style	    the style of this font
 * @return the Font constructed based on the parameters
 */
    
    public Font getFont(String fontname, float size, int style) {
        return getFont(fontname, defaultEncoding, defaultEmbedding, size, style, null);
    }
    
/**
 * Constructs a <CODE>Font</CODE>-object.
 *
 * @param	fontname    the name of the font
 * @param	size	    the size of this font
 * @return the Font constructed based on the parameters
 */
    
    public Font getFont(String fontname, float size) {
        return getFont(fontname, defaultEncoding, defaultEmbedding, size, Font.UNDEFINED, null);
    }
    
/**
 * Constructs a <CODE>Font</CODE>-object.
 *
 * @param	fontname    the name of the font
 * @return the Font constructed based on the parameters
 */
    
    public Font getFont(String fontname) {
        return getFont(fontname, defaultEncoding, defaultEmbedding, Font.UNDEFINED, Font.UNDEFINED, null);
    }
    
    /**
     * Register a font by giving explicitly the font family and name.
     * @param familyName the font family
     * @param fullName the font name
     * @param path the font path
     */
    public void registerFamily(String familyName, String fullName, String path) {
        if (path != null)
            trueTypeFonts.setProperty(fullName, path);
        ArrayList tmp = (ArrayList) fontFamilies.get(familyName);
        if (tmp == null) {
            tmp = new ArrayList();
            tmp.add(fullName);
            fontFamilies.put(familyName, tmp);
        }
        else {
            int fullNameLength = fullName.length();
            boolean inserted = false;
            for (int j = 0; j < tmp.size(); ++j) {
                if (((String)tmp.get(j)).length() >= fullNameLength) {
                    tmp.add(j, fullName);
                    inserted = true;
                    break;
                }
            }
            if (!inserted)
                tmp.add(fullName);
        }
    }
    
/**
 * Register a ttf- or a ttc-file.
 *
 * @param   path    the path to a ttf- or ttc-file
 */
    
    public void register(String path) {
        register(path, null);
    }
    
/**
 * Register a font file and use an alias for the font contained in it.
 *
 * @param   path    the path to a font file
 * @param   alias   the alias you want to use for the font
 */
    
    public void register(String path, String alias) {
        try {
            if (path.toLowerCase().endsWith(".ttf") || path.toLowerCase().endsWith(".otf") || path.toLowerCase().indexOf(".ttc,") > 0) {
                Object allNames[] = BaseFont.getAllFontNames(path, BaseFont.WINANSI, null);
                trueTypeFonts.setProperty(((String)allNames[0]).toLowerCase(), path);
                if (alias != null) {
                    trueTypeFonts.setProperty(alias.toLowerCase(), path);
                }
                // register all the font names with all the locales
                String[][] names = (String[][])allNames[2]; //full name
                for (int i = 0; i < names.length; i++) {
                    trueTypeFonts.setProperty(names[i][3].toLowerCase(), path);
                }
                String fullName = null;
                String familyName = null;
                names = (String[][])allNames[1]; //family name
                for (int k = 0; k < TTFamilyOrder.length; k += 3) {
                    for (int i = 0; i < names.length; i++) {
                        if (TTFamilyOrder[k].equals(names[i][0]) && TTFamilyOrder[k + 1].equals(names[i][1]) && TTFamilyOrder[k + 2].equals(names[i][2])) {
                            familyName = names[i][3].toLowerCase();
                            k = TTFamilyOrder.length;
                            break;
                        }
                    }
                }
                if (familyName != null) {
                    String lastName = "";
                    names = (String[][])allNames[2]; //full name
                    for (int i = 0; i < names.length; i++) {
                        for (int k = 0; k < TTFamilyOrder.length; k += 3) {
                            if (TTFamilyOrder[k].equals(names[i][0]) && TTFamilyOrder[k + 1].equals(names[i][1]) && TTFamilyOrder[k + 2].equals(names[i][2])) {
                                fullName = names[i][3];
                                if (fullName.equals(lastName))
                                    continue;
                                lastName = fullName;
                                registerFamily(familyName, fullName, null);
                                break;
                            }
                        }
                    }
                }
            }
            else if (path.toLowerCase().endsWith(".ttc")) {
                if (alias != null)
                    System.err.println("class FontFactory: You can't define an alias for a true type collection.");
                String[] names = BaseFont.enumerateTTCNames(path);
                for (int i = 0; i < names.length; i++) {
                    register(path + "," + i);
                }
            }
            else if (path.toLowerCase().endsWith(".afm") || path.toLowerCase().endsWith(".pfm")) {
                BaseFont bf = BaseFont.createFont(path, BaseFont.CP1252, false);
                String fullName = bf.getFullFontName()[0][3].toLowerCase();
                String familyName = bf.getFamilyFontName()[0][3].toLowerCase();
                String psName = bf.getPostscriptFontName().toLowerCase();
                registerFamily(familyName, fullName, null);
                trueTypeFonts.setProperty(psName, path);
                trueTypeFonts.setProperty(fullName, path);
            }
        }
        catch(DocumentException de) {
            // this shouldn't happen
            throw new ExceptionConverter(de);
        }
        catch(IOException ioe) {
            throw new ExceptionConverter(ioe);
        }
    }

    /** Register all the fonts in a directory.
     * @param dir the directory
     * @return the number of fonts registered
     */    
    public int registerDirectory(String dir) {
        return registerDirectory(dir, false);
    }

    /**
     * Register all the fonts in a directory and possibly its subdirectories.
     * @param dir the directory
     * @param scanSubdirectories recursively scan subdirectories if <code>true</true>
     * @return the number of fonts registered
     * @since 2.1.2
     */
    public int registerDirectory(String dir, boolean scanSubdirectories) {
        int count = 0;
        try {
            File file = new File(dir);
            if (!file.exists() || !file.isDirectory())
                return 0;
            String files[] = file.list();
            if (files == null)
                return 0;
            for (int k = 0; k < files.length; ++k) {
                try {
                    file = new File(dir, files[k]);
                    if (file.isDirectory()) {
                        if (scanSubdirectories) {
                            count += registerDirectory(file.getAbsolutePath(), true);
                        }
                    } else {
                        String name = file.getPath();
                        String suffix = name.length() < 4 ? null : name.substring(name.length() - 4).toLowerCase();
                        if (".afm".equals(suffix) || ".pfm".equals(suffix)) {
                            /* Only register Type 1 fonts with matching .pfb files */
                            File pfb = new File(name.substring(0, name.length() - 4) + ".pfb");
                            if (pfb.exists()) {
                                register(name, null);
                                ++count;
                            }
                        } else if (".ttf".equals(suffix) || ".otf".equals(suffix) || ".ttc".equals(suffix)) {
                            register(name, null);
                            ++count;
                        }
                    }
                }
                catch (Exception e) {
                    //empty on purpose
                }
            }
        }
        catch (Exception e) {
            //empty on purpose
        }
        return count;
    }

    /** Register fonts in some probable directories. It usually works in Windows,
     * Linux and Solaris.
     * @return the number of fonts registered
     */    
    public int registerDirectories() {
        int count = 0;
        count += registerDirectory("c:/windows/fonts");
        count += registerDirectory("c:/winnt/fonts");
        count += registerDirectory("d:/windows/fonts");
        count += registerDirectory("d:/winnt/fonts");
        count += registerDirectory("/usr/share/X11/fonts", true);
        count += registerDirectory("/usr/X/lib/X11/fonts", true);
        count += registerDirectory("/usr/openwin/lib/X11/fonts", true);
        count += registerDirectory("/usr/share/fonts", true);
        count += registerDirectory("/usr/X11R6/lib/X11/fonts", true);
        count += registerDirectory("/Library/Fonts");
        count += registerDirectory("/System/Library/Fonts");
        return count;
    }

/**
 * Gets a set of registered fontnames.
 * @return a set of registered fonts
 */
    
    public Set getRegisteredFonts() {
        return Utilities.getKeySet(trueTypeFonts);
    }
    
/**
 * Gets a set of registered fontnames.
 * @return a set of registered font families
 */
    
    public Set getRegisteredFamilies() {
        return Utilities.getKeySet(fontFamilies);
    }
    
/**
 * Checks if a certain font is registered.
 *
 * @param   fontname    the name of the font that has to be checked.
 * @return  true if the font is found
 */
    public boolean isRegistered(String fontname) {
        return trueTypeFonts.containsKey(fontname.toLowerCase());
    }
}
