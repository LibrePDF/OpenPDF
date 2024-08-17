package com.sun.pdfview.font.ttf;

import java.io.*;
import java.util.*;

/**
 * Build an object which provides access to all the Adobe glyph names, using
 * a unicode value, and which can translate a glyph name to one or more
 * unicode values.
 *
# ###################################################################################
# Copyright (c) 1997,1998,2002,2007 Adobe Systems Incorporated
#
# Permission is hereby granted, free of charge, to any person obtaining a
# copy of this documentation file to use, copy, publish, distribute,
# sublicense, and/or sell copies of the documentation, and to permit
# others to do the same, provided that:
# - No modification, editing or other alteration of this document is
# allowed; and
# - The above copyright notice and this permission notice shall be
# included in all copies of the documentation.
#
# Permission is hereby granted, free of charge, to any person obtaining a
# copy of this documentation file, to create their own derivative works
# from the content of this document to use, copy, publish, distribute,
# sublicense, and/or sell the derivative works, and to permit others to do
# the same, provided that the derived work is not represented as being a
# copy or version of this document.
#
# Adobe shall not be liable to any party for any loss of revenue or profit
# or for indirect, incidental, special, consequential, or other similar
# damages, whether based on tort (including without limitation negligence
# or strict liability), contract or other legal or equitable grounds even
# if Adobe has been advised or had reason to know of the possibility of
# such damages. The Adobe materials are provided on an "AS IS" basis.
# Adobe specifically disclaims all express, statutory, or implied
# warranties relating to the Adobe materials, including but not limited to
# those concerning merchantability or fitness for a particular purpose or
# non-infringement of any third party rights regarding the Adobe
# materials.
# ###################################################################################
# Name:          Adobe Glyph List
# Table version: 2.0
# Date:          September 20, 2002
#
# See http://partners.adobe.com/asn/developer/typeforum/unicodegn.html
#
# Format: Semicolon-delimited fields:
#            (1) glyph name
#            (2) Unicode scalar value
 *
 * @author tomoke
 */
public class AdobeGlyphList {

    /** provide a translation from a glyph name to the possible unicode values. */
    static private HashMap<String, int[]> glyphToUnicodes;
    /** provide a translation from a unicode value to a glyph name. */
    static private HashMap<Integer, String> unicodeToGlyph;
    /** the loader thread we are reading through. */
    static Thread glyphLoaderThread = null;


    static {
        new AdobeGlyphList();
    }

    /** 
     * <p>private constructor to restrict creation to a singleton.</p>
     * 
     * <p>We initialize by creating the storage and parsing the glyphlist
     * into the tables.</p>
     */
    private AdobeGlyphList() {
        glyphToUnicodes = new HashMap<String, int[]>(4500);
        unicodeToGlyph = new HashMap<Integer, String>(4500);
        glyphLoaderThread = new Thread(new Runnable() {

            @Override
			public void run() {
                int[] codes;
                StringTokenizer codeTokens;
                String glyphName;
                StringTokenizer tokens;
                ArrayList<String> unicodes = new ArrayList<String>();

                InputStream istr = getClass().getResourceAsStream("/com/sun/pdfview/font/ttf/resource/glyphlist.txt");

                BufferedReader reader = new BufferedReader(new InputStreamReader(istr));
                String line = "";
                while (line != null) {
                    try {
                        unicodes.clear();
                        line = reader.readLine();
                        if (line == null) {
                            break;
                        }
                        line = line.trim();
                        if (line.length() > 0 && !line.startsWith("#")) {
                            // ignore comment lines
                            tokens = new StringTokenizer(line, ";");
                            glyphName = tokens.nextToken();
                            codeTokens = new StringTokenizer(tokens.nextToken(), " ");
                            while (codeTokens.hasMoreTokens()) {
                                unicodes.add(codeTokens.nextToken());
                            }
                            codes = new int[unicodes.size()];
                            for (int i = 0; i < unicodes.size(); i++) {
                                codes[i] = Integer.parseInt(unicodes.get(i), 16);
                                unicodeToGlyph.put(Integer.valueOf(codes[i]), glyphName);
                            }
                            glyphToUnicodes.put(glyphName, codes);
                        }

                    } catch (IOException ex) {
                        break;
                    }
                }
            }
        }, "Adobe Glyph Loader Thread");
        glyphLoaderThread.setDaemon(true);
        glyphLoaderThread.setPriority(Thread.MIN_PRIORITY);
        glyphLoaderThread.start();
    }

    /**
     * translate a glyph name into the possible unicode values that it
     * might represent. It is possible to have more than one unicode
     * value for a single glyph name.
     *
     * @param glyphName
     * @return int[]
     */
    public static int[] getUnicodeValues(String glyphName) {
        while (glyphLoaderThread != null && glyphLoaderThread.isAlive()) {
            synchronized (glyphToUnicodes) {
                try {
                    glyphToUnicodes.wait(250);
                } catch (InterruptedException ex) {
                    // ignore
                }
            }
        }
        return glyphToUnicodes.get(glyphName);
    }

    /**
     * return a single index for a glyph, though there may be multiples.
     * 
     * @param glyphName
     * @return Integer
     */
    public static Integer getGlyphNameIndex(String glyphName) {
        int [] unicodes = getUnicodeValues(glyphName);
        if (unicodes == null) {
            return null;
        } else {
            return Integer.valueOf(unicodes[0]);
        }
    }

    /**
     * translate a unicode value into a glyph name. It is possible for
     * different unicode values to translate into the same glyph name.
     *
     * @param unicode
     * @return String
     */
    public static String getGlyphName(int unicode) {
        while (glyphLoaderThread != null && glyphLoaderThread.isAlive()) {
            synchronized (glyphToUnicodes) {
                try {
                    glyphToUnicodes.wait(250);
                } catch (InterruptedException ex) {
                    // ignore
                }
            }
        }
        return unicodeToGlyph.get(Integer.valueOf(unicode));
    }
}