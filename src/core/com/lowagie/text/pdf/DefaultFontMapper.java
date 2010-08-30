/*
 * Copyright 2004 by Paulo Soares.
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

import java.awt.Font;
import java.io.File;
import java.util.HashMap;

import com.lowagie.text.ExceptionConverter;
/** Default class to map awt fonts to BaseFont.
 * @author Paulo Soares (psoares@consiste.pt)
 */

public class DefaultFontMapper implements FontMapper {
    
    /** A representation of BaseFont parameters.
     */    
    public static class BaseFontParameters {
        /** The font name.
         */        
        public String fontName;
        /** The encoding for that font.
         */        
        public String encoding;
        /** The embedding for that font.
         */        
        public boolean embedded;
        /** Whether the font is cached of not.
         */        
        public boolean cached;
        /** The font bytes for ttf and afm.
         */        
        public byte ttfAfm[];
        /** The font bytes for pfb.
         */        
        public byte pfb[];
        
        /** Constructs default BaseFont parameters.
         * @param fontName the font name or location
         */        
        public BaseFontParameters(String fontName) {
            this.fontName = fontName;
            encoding = BaseFont.CP1252;
            embedded = BaseFont.EMBEDDED;
            cached = BaseFont.CACHED;
        }
    }
    
    /** Maps aliases to names.
     */    
    private HashMap aliases = new HashMap();
    /** Maps names to BaseFont parameters.
     */    
    private HashMap mapper = new HashMap();
    /**
     * Returns a BaseFont which can be used to represent the given AWT Font
     *
     * @param	font		the font to be converted
     * @return	a BaseFont which has similar properties to the provided Font
     */
    
    public BaseFont awtToPdf(Font font) {
        try {
            BaseFontParameters p = getBaseFontParameters(font.getFontName());
            if (p != null)
                return BaseFont.createFont(p.fontName, p.encoding, p.embedded, p.cached, p.ttfAfm, p.pfb);
            String fontKey = null;
            String logicalName = font.getName();

            if (logicalName.equalsIgnoreCase("DialogInput") || logicalName.equalsIgnoreCase("Monospaced") || logicalName.equalsIgnoreCase("Courier")) {

                if (font.isItalic()) {
                    if (font.isBold()) {
                        fontKey = BaseFont.COURIER_BOLDOBLIQUE;

                    } else {
                        fontKey = BaseFont.COURIER_OBLIQUE;
                    }

                } else {
                    if (font.isBold()) {
                        fontKey = BaseFont.COURIER_BOLD;

                    } else {
                        fontKey = BaseFont.COURIER;
                    }
                }

            } else if (logicalName.equalsIgnoreCase("Serif") || logicalName.equalsIgnoreCase("TimesRoman")) {

                if (font.isItalic()) {
                    if (font.isBold()) {
                        fontKey = BaseFont.TIMES_BOLDITALIC;

                    } else {
                        fontKey = BaseFont.TIMES_ITALIC;
                    }

                } else {
                    if (font.isBold()) {
                        fontKey = BaseFont.TIMES_BOLD;

                    } else {
                        fontKey = BaseFont.TIMES_ROMAN;
                    }
                }

            } else {  // default, this catches Dialog and SansSerif

                if (font.isItalic()) {
                    if (font.isBold()) {
                        fontKey = BaseFont.HELVETICA_BOLDOBLIQUE;

                    } else {
                        fontKey = BaseFont.HELVETICA_OBLIQUE;
                    }

                } else {
                    if (font.isBold()) {
                        fontKey = BaseFont.HELVETICA_BOLD;
                    } else {
                        fontKey = BaseFont.HELVETICA;
                    }
                }
            }
            return BaseFont.createFont(fontKey, BaseFont.CP1252, false);
        }
        catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }
    
    /**
     * Returns an AWT Font which can be used to represent the given BaseFont
     *
     * @param	font		the font to be converted
     * @param	size		the desired point size of the resulting font
     * @return	a Font which has similar properties to the provided BaseFont
     */
    
    public Font pdfToAwt(BaseFont font, int size) {
        String names[][] = font.getFullFontName();
        if (names.length == 1)
            return new Font(names[0][3], 0, size);
        String name10 = null;
        String name3x = null;
        for (int k = 0; k < names.length; ++k) {
            String name[] = names[k];
            if (name[0].equals("1") && name[1].equals("0"))
                name10 = name[3];
            else if (name[2].equals("1033")) {
                name3x = name[3];
                break;
            }
        }
        String finalName = name3x;
        if (finalName == null)
            finalName = name10;
        if (finalName == null)
            finalName = names[0][3];
        return new Font(finalName, 0, size);
    }
    
    /** Maps a name to a BaseFont parameter.
     * @param awtName the name
     * @param parameters the BaseFont parameter
     */    
    public void putName(String awtName, BaseFontParameters parameters) {
        mapper.put(awtName, parameters);
    }
    
    /** Maps an alias to a name.
     * @param alias the alias
     * @param awtName the name
     */    
    public void putAlias(String alias, String awtName) {
        aliases.put(alias, awtName);
    }
    
    /** Looks for a BaseFont parameter associated with a name.
     * @param name the name
     * @return the BaseFont parameter or <CODE>null</CODE> if not found.
     */    
    public BaseFontParameters getBaseFontParameters(String name) {
        String alias = (String)aliases.get(name);
        if (alias == null)
            return (BaseFontParameters)mapper.get(name);
        BaseFontParameters p = (BaseFontParameters)mapper.get(alias);
        if (p == null)
            return (BaseFontParameters)mapper.get(name);
        else
            return p;
    }
    
    /**
     * Inserts the names in this map.
     * @param allNames the returned value of calling {@link BaseFont#getAllFontNames(String, String, byte[])}
     * @param path the full path to the font
     */    
    public void insertNames(Object allNames[], String path) {
        String names[][] = (String[][])allNames[2];
        String main = null;
        for (int k = 0; k < names.length; ++k) {
            String name[] = names[k];
            if (name[2].equals("1033")) {
                main = name[3];
                break;
            }
        }
        if (main == null)
            main = names[0][3];
        BaseFontParameters p = new BaseFontParameters(path);
        mapper.put(main, p);
        for (int k = 0; k < names.length; ++k) {
            aliases.put(names[k][3], main);
        }
        aliases.put(allNames[0], main);
    }
    
    /** Inserts all the fonts recognized by iText in the
     * <CODE>directory</CODE> into the map. The encoding
     * will be <CODE>BaseFont.CP1252</CODE> but can be
     * changed later.
     * @param dir the directory to scan
     * @return the number of files processed
     */    
    public int insertDirectory(String dir) {
        File file = new File(dir);
        if (!file.exists() || !file.isDirectory())
            return 0;
        File files[] = file.listFiles();
        if (files == null)
        	return 0;
        int count = 0;
        for (int k = 0; k < files.length; ++k) {
            file = files[k];
            String name = file.getPath().toLowerCase();
            try {
                if (name.endsWith(".ttf") || name.endsWith(".otf") || name.endsWith(".afm")) {
                    Object allNames[] = BaseFont.getAllFontNames(file.getPath(), BaseFont.CP1252, null);
                    insertNames(allNames, file.getPath());
                    ++count;
                }
                else if (name.endsWith(".ttc")) {
                    String ttcs[] = BaseFont.enumerateTTCNames(file.getPath());
                    for (int j = 0; j < ttcs.length; ++j) {
                        String nt = file.getPath() + "," + j;
                        Object allNames[] = BaseFont.getAllFontNames(nt, BaseFont.CP1252, null);
                        insertNames(allNames, nt);
                    }
                    ++count;
                }
            }
            catch (Exception e) {
            }
        }
        return count;
    }
    
    public HashMap getMapper() {
        return mapper;
    }
    
    public HashMap getAliases() {
        return aliases;
    }
}
