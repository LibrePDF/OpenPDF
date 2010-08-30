/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lowagie.text.pdf.hyphenation;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Hashtable;

import com.lowagie.text.pdf.BaseFont;

/**
 * This class is the main entry point to the hyphenation package.
 * You can use only the static methods or create an instance.
 *
 * @author Carlos Villegas <cav@uniscope.co.jp>
 */
public class Hyphenator {
    
    /** TODO: Don't use statics */
    private static Hashtable hyphenTrees = new Hashtable();

    private HyphenationTree hyphenTree = null;
    private int remainCharCount = 2;
    private int pushCharCount = 2;
    private static final String defaultHyphLocation = "com/lowagie/text/pdf/hyphenation/hyph/";
   
    /** Holds value of property hyphenDir. */
    private static String hyphenDir = "";    

    /**
     * @param lang
     * @param country
     * @param leftMin
     * @param rightMin
     */
    public Hyphenator(String lang, String country, int leftMin,
                      int rightMin) {
        hyphenTree = getHyphenationTree(lang, country);
        remainCharCount = leftMin;
        pushCharCount = rightMin;
    }

    /**
     * @param lang
     * @param country
     * @return the hyphenation tree
     */
    public static HyphenationTree getHyphenationTree(String lang,
            String country) {
        String key = lang;
        // check whether the country code has been used
        if (country != null && !country.equals("none")) {
            key += "_" + country;
        }
            // first try to find it in the cache
        if (hyphenTrees.containsKey(key)) {
            return (HyphenationTree)hyphenTrees.get(key);
        }
        if (hyphenTrees.containsKey(lang)) {
            return (HyphenationTree)hyphenTrees.get(lang);
        }

        HyphenationTree hTree = getResourceHyphenationTree(key);
        if (hTree == null)
            hTree = getFileHyphenationTree(key);
        // put it into the pattern cache
        if (hTree != null) {
            hyphenTrees.put(key, hTree);
        }
        return hTree;
    }

    /**
     * @param key
     * @return a hyphenation tree
     */
    public static HyphenationTree getResourceHyphenationTree(String key) {
        try {
            InputStream stream = BaseFont.getResourceStream(defaultHyphLocation + key + ".xml");
            if (stream == null && key.length() > 2)
                stream = BaseFont.getResourceStream(defaultHyphLocation + key.substring(0, 2) + ".xml");
            if (stream == null)
                return null;
            HyphenationTree hTree = new HyphenationTree();
            hTree.loadSimplePatterns(stream);
            return hTree;
        }
        catch (Exception e) {
            return null;
        }
    }

    /**
     * @param key
     * @return a hyphenation tree
     */
    public static HyphenationTree getFileHyphenationTree(String key) {
        try {
            if (hyphenDir == null)
                return null;
            InputStream stream = null;
            File hyphenFile = new File(hyphenDir, key + ".xml");
            if (hyphenFile.canRead())
                stream = new FileInputStream(hyphenFile);
            if (stream == null && key.length() > 2) {
                hyphenFile = new File(hyphenDir, key.substring(0, 2) + ".xml");
                if (hyphenFile.canRead())
                    stream = new FileInputStream(hyphenFile);
            }
            if (stream == null)
                return null;
            HyphenationTree hTree = new HyphenationTree();
            hTree.loadSimplePatterns(stream);
            return hTree;
        }
        catch (Exception e) {
            return null;
        }
    }

    /**
     * @param lang
     * @param country
     * @param word
     * @param leftMin
     * @param rightMin
     * @return a hyphenation object
     */
    public static Hyphenation hyphenate(String lang, String country,
                                        String word, int leftMin,
                                        int rightMin) {
        HyphenationTree hTree = getHyphenationTree(lang, country);
        if (hTree == null) {
            //log.error("Error building hyphenation tree for language "
            //                       + lang);
            return null;
        }
        return hTree.hyphenate(word, leftMin, rightMin);
    }

    /**
     * @param lang
     * @param country
     * @param word
     * @param offset
     * @param len
     * @param leftMin
     * @param rightMin
     * @return a hyphenation object
     */
    public static Hyphenation hyphenate(String lang, String country,
                                        char[] word, int offset, int len,
                                        int leftMin, int rightMin) {
        HyphenationTree hTree = getHyphenationTree(lang, country);
        if (hTree == null) {
            //log.error("Error building hyphenation tree for language "
            //                       + lang);
            return null;
        }
        return hTree.hyphenate(word, offset, len, leftMin, rightMin);
    }

    /**
     * @param min
     */
    public void setMinRemainCharCount(int min) {
        remainCharCount = min;
    }

    /**
     * @param min
     */
    public void setMinPushCharCount(int min) {
        pushCharCount = min;
    }

    /**
     * @param lang
     * @param country
     */
    public void setLanguage(String lang, String country) {
        hyphenTree = getHyphenationTree(lang, country);
    }

    /**
     * @param word
     * @param offset
     * @param len
     * @return a hyphenation object
     */
    public Hyphenation hyphenate(char[] word, int offset, int len) {
        if (hyphenTree == null) {
            return null;
        }
        return hyphenTree.hyphenate(word, offset, len, remainCharCount,
                                    pushCharCount);
    }

    /**
     * @param word
     * @return a hyphenation object
     */
    public Hyphenation hyphenate(String word) {
        if (hyphenTree == null) {
            return null;
        }
        return hyphenTree.hyphenate(word, remainCharCount, pushCharCount);
    }

    /** Getter for property hyphenDir.
     * @return Value of property hyphenDir.
     */
    public static String getHyphenDir() {
        return hyphenDir;
    }
    
    /** Setter for property hyphenDir.
     * @param _hyphenDir New value of property hyphenDir.
     */
    public static void setHyphenDir(String _hyphenDir) {
        hyphenDir = _hyphenDir;
    }
    
}
