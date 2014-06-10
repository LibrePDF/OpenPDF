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

/**
 * This class represents a hyphenated word.
 *
 * @author Carlos Villegas <cav@uniscope.co.jp>
 */
public class Hyphenation {
    
    private int[] hyphenPoints;
    private String word;

    /**
     * number of hyphenation points in word
     */
    private int len;

    /**
     * rawWord as made of alternating strings and {@link Hyphen Hyphen}
     * instances
     */
    Hyphenation(String word, int[] points) {
        this.word = word;
        hyphenPoints = points;
        len = points.length;
    }

    /**
     * @return the number of hyphenation points in the word
     */
    public int length() {
        return len;
    }

    /**
     * @return the pre-break text, not including the hyphen character
     */
    public String getPreHyphenText(int index) {
        return word.substring(0, hyphenPoints[index]);
    }

    /**
     * @return the post-break text
     */
    public String getPostHyphenText(int index) {
        return word.substring(hyphenPoints[index]);
    }

    /**
     * @return the hyphenation points
     */
    public int[] getHyphenationPoints() {
        return hyphenPoints;
    }

    public String toString() {
        StringBuffer str = new StringBuffer();
        int start = 0;
        for (int i = 0; i < len; i++) {
            str.append(word.substring(start, hyphenPoints[i])).append('-');
            start = hyphenPoints[i];
        }
        str.append(word.substring(start));
        return str.toString();
    }

}
