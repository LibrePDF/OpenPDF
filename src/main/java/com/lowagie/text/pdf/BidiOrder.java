package com.lowagie.text.pdf;

/*
 * Copyright 2003 Paulo Soares
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

/*
 * (C) Copyright IBM Corp. 1999, All Rights Reserved
 *
 * version 1.1
 */

/*
 * As stated in the Javadoc comments below, materials from Unicode.org
 * are used in this class. The following license applies to these materials:
 * http://www.unicode.org/copyright.html#Exhibit1
 * 
 * EXHIBIT 1
 * UNICODE, INC. LICENSE AGREEMENT - DATA FILES AND SOFTWARE
 * 
 * Unicode Data Files include all data files under the directories
 * http://www.unicode.org/Public/, http://www.unicode.org/reports/,
 * and http://www.unicode.org/cldr/data/ .
 * Unicode Software includes any source code published in the Unicode Standard
 * or under the directories http://www.unicode.org/Public/, http://www.unicode.org/reports/,
 * and http://www.unicode.org/cldr/data/.
 * 
 * NOTICE TO USER: Carefully read the following legal agreement. BY DOWNLOADING,
 * INSTALLING, COPYING OR OTHERWISE USING UNICODE INC.'S DATA FILES ("DATA FILES"),
 * AND/OR SOFTWARE ("SOFTWARE"), YOU UNEQUIVOCALLY ACCEPT, AND AGREE TO BE BOUND BY,
 * ALL OF THE TERMS AND CONDITIONS OF THIS AGREEMENT. IF YOU DO NOT AGREE, DO NOT
 * DOWNLOAD, INSTALL, COPY, DISTRIBUTE OR USE THE DATA FILES OR SOFTWARE.
 * 
 * COPYRIGHT AND PERMISSION NOTICE
 * Copyright (C) 1991-2007 Unicode, Inc. All rights reserved. Distributed under
 * the Terms of Use in http://www.unicode.org/copyright.html.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of the Unicode data files and any associated documentation (the "Data Files")
 * or Unicode software and any associated documentation (the "Software") to deal
 * in the Data Files or Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, and/or sell copies
 * of the Data Files or Software, and to permit persons to whom the Data Files
 * or Software are furnished to do so, provided that (a) the above copyright
 * notice(s) and this permission notice appear with all copies of the Data Files
 * or Software, (b) both the above copyright notice(s) and this permission notice
 * appear in associated documentation, and (c) there is clear notice in each
 * modified Data File or in the Software as well as in the documentation associated
 * with the Data File(s) or Software that the data or software has been modified.
 * 
 * THE DATA FILES AND SOFTWARE ARE PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT OF THIRD PARTY RIGHTS.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR HOLDERS INCLUDED IN THIS NOTICE BE
 * LIABLE FOR ANY CLAIM, OR ANY SPECIAL INDIRECT OR CONSEQUENTIAL DAMAGES, OR ANY
 * DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN
 * CONNECTION WITH THE USE OR PERFORMANCE OF THE DATA FILES OR SOFTWARE.
 * 
 * Except as contained in this notice, the name of a copyright holder shall not
 * be used in advertising or otherwise to promote the sale, use or other dealings
 * in these Data Files or Software without prior written authorization of the
 * copyright holder.
 */

/**
 * Reference implementation of the Unicode 3.0 Bidi algorithm.
 *
 * <p>
 * This implementation is not optimized for performance.  It is intended
 * as a reference implementation that closely follows the specification
 * of the Bidirectional Algorithm in The Unicode Standard version 3.0.
 * <p>
 * <b>Input:</b><br>
 * There are two levels of input to the algorithm, since clients may prefer
 * to supply some information from out-of-band sources rather than relying on
 * the default behavior.
 * <ol>
 * <li>unicode type array
 * <li>unicode type array, with externally supplied base line direction
 * </ol>
 * <p><b>Output:</b><br>
 * Output is separated into several stages as well, to better enable clients
 * to evaluate various aspects of implementation conformance.
 * <ol>
 * <li>levels array over entire paragraph
 * <li>reordering array over entire paragraph
 * <li>levels array over line
 * <li>reordering array over line
 * </ol>
 * Note that for conformance, algorithms are only required to generate correct
 * reordering and character directionality (odd or even levels) over a line.
 * Generating identical level arrays over a line is not required.  Bidi
 * explicit format codes (LRE, RLE, LRO, RLO, PDF) and BN can be assigned
 * arbitrary levels and positions as long as the other text matches.
 * <p>
 * As the algorithm is defined to operate on a single paragraph at a time,
 * this implementation is written to handle single paragraphs.  Thus
 * rule P1 is presumed by this implementation-- the data provided to the
 * implementation is assumed to be a single paragraph, and either contains no
 * 'B' codes, or a single 'B' code at the end of the input.  'B' is allowed
 * as input to illustrate how the algorithm assigns it a level.
 * <p>
 * Also note that rules L3 and L4 depend on the rendering engine that uses
 * the result of the bidi algorithm.  This implementation assumes that the
 * rendering engine expects combining marks in visual order (e.g. to the
 * left of their base character in RTL runs) and that it adjust the glyphs
 * used to render mirrored characters that are in RTL runs so that they
 * render appropriately.
 *
 * @author Doug Felt
 */

import com.lowagie.text.error_messages.MessageLocalization;

public final class BidiOrder {
    private byte[] initialTypes;
    private byte[] embeddings; // generated from processing format codes
    private byte paragraphEmbeddingLevel = -1; // undefined
    
    private int textLength; // for convenience
    private byte[] resultTypes; // for paragraph, not lines
    private byte[] resultLevels; // for paragraph, not lines
    
    // The bidi types
    
    /** Left-to-right*/
    public static final byte L = 0;
    
    /** Left-to-Right Embedding */
    public static final byte LRE = 1;
    
    /** Left-to-Right Override */
    public static final byte LRO = 2;
    
    /** Right-to-Left */
    public static final byte R = 3;
    
    /** Right-to-Left Arabic */
    public static final byte AL = 4;
    
    /** Right-to-Left Embedding */
    public static final byte RLE = 5;
    
    /** Right-to-Left Override */
    public static final byte RLO = 6;
    
    /** Pop Directional Format */
    public static final byte PDF = 7;
    
    /** European Number */
    public static final byte EN = 8;
    
    /** European Number Separator */
    public static final byte ES = 9;
    
    /** European Number Terminator */
    public static final byte ET = 10;
    
    /** Arabic Number */
    public static final byte AN = 11;
    
    /** Common Number Separator */
    public static final byte CS = 12;
    
    /** Non-Spacing Mark */
    public static final byte NSM = 13;
    
    /** Boundary Neutral */
    public static final byte BN = 14;
    
    /** Paragraph Separator */
    public static final byte B = 15;
    
    /** Segment Separator */
    public static final byte S = 16;
    
    /** Whitespace */
    public static final byte WS = 17;
    
    /** Other Neutrals */
    public static final byte ON = 18;
    
    /** Minimum bidi type value. */
    public static final byte TYPE_MIN = 0;
    
    /** Maximum bidi type value. */
    public static final byte TYPE_MAX = 18;
    
    //
    // Input
    //
    
    /**
     * Initialize using an array of direction types.  Types range from TYPE_MIN to TYPE_MAX inclusive
     * and represent the direction codes of the characters in the text.
     *
     * @param types the types array
     */
    public BidiOrder(byte[] types) {
        validateTypes(types);
        
        this.initialTypes = (byte[])types.clone(); // client type array remains unchanged
        
        runAlgorithm();
    }
    
    /**
     * Initialize using an array of direction types and an externally supplied paragraph embedding level.
     * The embedding level may be -1, 0, or 1.  -1 means to apply the default algorithm (rules P2 and P3),
     * 0 is for LTR paragraphs, and 1 is for RTL paragraphs.
     *
     * @param types the types array
     * @param paragraphEmbeddingLevel the externally supplied paragraph embedding level.
     */
    public BidiOrder(byte[] types, byte paragraphEmbeddingLevel) {
        validateTypes(types);
        validateParagraphEmbeddingLevel(paragraphEmbeddingLevel);
        
        this.initialTypes = (byte[])types.clone(); // client type array remains unchanged
        this.paragraphEmbeddingLevel = paragraphEmbeddingLevel;
        
        runAlgorithm();
    }
    
    public BidiOrder(char text[], int offset, int length, byte paragraphEmbeddingLevel) {
        initialTypes = new byte[length];
        for (int k = 0; k < length; ++k) {
            initialTypes[k] = rtypes[text[offset + k]];
        }
        validateParagraphEmbeddingLevel(paragraphEmbeddingLevel);
        
        this.paragraphEmbeddingLevel = paragraphEmbeddingLevel;
        
        runAlgorithm();
    }
    
    public final static byte getDirection(char c) {
        return rtypes[c];
    }
    
    /**
     * The algorithm.
     * Does not include line-based processing (Rules L1, L2).
     * These are applied later in the line-based phase of the algorithm.
     */
    private void runAlgorithm() {
        textLength = initialTypes.length;
        
        // Initialize output types.
        // Result types initialized to input types.
        resultTypes = (byte[])initialTypes.clone();
        
        
        // 1) determining the paragraph level
        // Rule P1 is the requirement for entering this algorithm.
        // Rules P2, P3.
        // If no externally supplied paragraph embedding level, use default.
        if (paragraphEmbeddingLevel == -1) {
            determineParagraphEmbeddingLevel();
        }
        
        // Initialize result levels to paragraph embedding level.
        resultLevels = new byte[textLength];
        setLevels(0, textLength, paragraphEmbeddingLevel);
        
        // 2) Explicit levels and directions
        // Rules X1-X8.
        determineExplicitEmbeddingLevels();
        
        // Rule X9.
        textLength = removeExplicitCodes();
        
        // Rule X10.
        // Run remainder of algorithm one level run at a time
        byte prevLevel = paragraphEmbeddingLevel;
        int start = 0;
        while (start < textLength) {
            byte level = resultLevels[start];
            byte prevType = typeForLevel(Math.max(prevLevel, level));
            
            int limit = start + 1;
            while (limit < textLength && resultLevels[limit] == level) {
                ++limit;
            }
            
            byte succLevel = limit < textLength ? resultLevels[limit] : paragraphEmbeddingLevel;
            byte succType = typeForLevel(Math.max(succLevel, level));
            
            // 3) resolving weak types
            // Rules W1-W7.
            resolveWeakTypes(start, limit, level, prevType, succType);
            
            // 4) resolving neutral types
            // Rules N1-N3.
            resolveNeutralTypes(start, limit, level, prevType, succType);
            
            // 5) resolving implicit embedding levels
            // Rules I1, I2.
            resolveImplicitLevels(start, limit, level, prevType, succType);
            
            prevLevel = level;
            start = limit;
        }
        
        // Reinsert explicit codes and assign appropriate levels to 'hide' them.
        // This is for convenience, so the resulting level array maps 1-1
        // with the initial array.
        // See the implementation suggestions section of TR#9 for guidelines on
        // how to implement the algorithm without removing and reinserting the codes.
        textLength = reinsertExplicitCodes(textLength);
    }
    
    /**
     * 1) determining the paragraph level.
     * <p>
     * Rules P2, P3.
     * <p>
     * At the end of this function, the member variable paragraphEmbeddingLevel is set to either 0 or 1.
     */
    private void determineParagraphEmbeddingLevel() {
        byte strongType = -1; // unknown
        
        // Rule P2.
        for (int i = 0; i < textLength; ++i) {
            byte t = resultTypes[i];
            if (t == L || t == AL || t == R) {
                strongType = t;
                break;
            }
        }
        
        // Rule P3.
        if (strongType == -1) { // none found
            // default embedding level when no strong types found is 0.
            paragraphEmbeddingLevel = 0;
        } else if (strongType == L) {
            paragraphEmbeddingLevel = 0;
        } else { // AL, R
            paragraphEmbeddingLevel = 1;
        }
    }
    
    /**
     * Process embedding format codes.
     * <p>
     * Calls processEmbeddings to generate an embedding array from the explicit format codes.  The
     * embedding overrides in the array are then applied to the result types, and the result levels are
     * initialized.
     * @see #processEmbeddings
     */
    private void determineExplicitEmbeddingLevels() {
        embeddings = processEmbeddings(resultTypes, paragraphEmbeddingLevel);
        
        for (int i = 0; i < textLength; ++i) {
            byte level = embeddings[i];
            if ((level & 0x80) != 0) {
                level &= 0x7f;
                resultTypes[i] = typeForLevel(level);
            }
            resultLevels[i] = level;
        }
    }
    
    /**
     * Rules X9.
     * Remove explicit codes so that they may be ignored during the remainder
     * of the main portion of the algorithm.  The length of the resulting text
     * is returned.
     * @return the length of the data excluding explicit codes and BN.
     */
    private int removeExplicitCodes() {
        int w = 0;
        for (int i = 0; i < textLength; ++i) {
            byte t = initialTypes[i];
            if (!(t == LRE || t == RLE || t == LRO || t == RLO || t == PDF || t == BN)) {
                embeddings[w] = embeddings[i];
                resultTypes[w] = resultTypes[i];
                resultLevels[w] = resultLevels[i];
                w++;
            }
        }
        return w; // new textLength while explicit levels are removed
    }
    
    /**
     * Reinsert levels information for explicit codes.
     * This is for ease of relating the level information
     * to the original input data.  Note that the levels
     * assigned to these codes are arbitrary, they're
     * chosen so as to avoid breaking level runs.
     * @param textLength the length of the data after compression
     * @return the length of the data (original length of
     * types array supplied to constructor)
     */
    private int reinsertExplicitCodes(int textLength) {
        for (int i = initialTypes.length; --i >= 0;) {
            byte t = initialTypes[i];
            if (t == LRE || t == RLE || t == LRO || t == RLO || t == PDF || t == BN) {
                embeddings[i] = 0;
                resultTypes[i] = t;
                resultLevels[i] = -1;
            } else {
                --textLength;
                embeddings[i] = embeddings[textLength];
                resultTypes[i] = resultTypes[textLength];
                resultLevels[i] = resultLevels[textLength];
            }
        }
        
        // now propagate forward the levels information (could have
        // propagated backward, the main thing is not to introduce a level
        // break where one doesn't already exist).
        
        if (resultLevels[0] == -1) {
            resultLevels[0] = paragraphEmbeddingLevel;
        }
        for (int i = 1; i < initialTypes.length; ++i) {
            if (resultLevels[i] == -1) {
                resultLevels[i] = resultLevels[i-1];
            }
        }
        
        // Embedding information is for informational purposes only
        // so need not be adjusted.
        
        return initialTypes.length;
    }
    
    /**
     * 2) determining explicit levels
     * Rules X1 - X8
     *
     * The interaction of these rules makes handling them a bit complex.
     * This examines resultTypes but does not modify it.  It returns embedding and
     * override information in the result array.  The low 7 bits are the level, the high
     * bit is set if the level is an override, and clear if it is an embedding.
     */
    private static byte[] processEmbeddings(byte[] resultTypes, byte paragraphEmbeddingLevel) {
        final int EXPLICIT_LEVEL_LIMIT = 62;
        
        int textLength = resultTypes.length;
        byte[] embeddings = new byte[textLength];
        
        // This stack will store the embedding levels and override status in a single byte
        // as described above.
        byte[] embeddingValueStack = new byte[EXPLICIT_LEVEL_LIMIT];
        int stackCounter = 0;
        
        // An LRE or LRO at level 60 is invalid, since the new level 62 is invalid.  But
        // an RLE at level 60 is valid, since the new level 61 is valid.  The current wording
        // of the rules requires that the RLE remain valid even if a previous LRE is invalid.
        // This keeps track of ignored LRE or LRO codes at level 60, so that the matching PDFs
        // will not try to pop the stack.
        int overflowAlmostCounter = 0;
        
        // This keeps track of ignored pushes at level 61 or higher, so that matching PDFs will
        // not try to pop the stack.
        int overflowCounter = 0;
        
        // Rule X1.
        
        // Keep the level separate from the value (level | override status flag) for ease of access.
        byte currentEmbeddingLevel = paragraphEmbeddingLevel;
        byte currentEmbeddingValue = paragraphEmbeddingLevel;
        
        // Loop through types, handling all remaining rules
        for (int i = 0; i < textLength; ++i) {
            
            embeddings[i] = currentEmbeddingValue;
            
            byte t = resultTypes[i];
            
            // Rules X2, X3, X4, X5
            switch (t) {
                case RLE:
                case LRE:
                case RLO:
                case LRO:
                    // Only need to compute new level if current level is valid
                    if (overflowCounter == 0) {
                        byte newLevel;
                        if (t == RLE || t == RLO) {
                            newLevel = (byte)((currentEmbeddingLevel + 1) | 1); // least greater odd
                        } else { // t == LRE || t == LRO
                            newLevel = (byte)((currentEmbeddingLevel + 2) & ~1); // least greater even
                        }
                        
                        // If the new level is valid, push old embedding level and override status
                        // No check for valid stack counter, since the level check suffices.
                        if (newLevel < EXPLICIT_LEVEL_LIMIT) {
                            embeddingValueStack[stackCounter] = currentEmbeddingValue;
                            stackCounter++;
                            
                            currentEmbeddingLevel = newLevel;
                            if (t == LRO || t == RLO) { // override
                                currentEmbeddingValue = (byte)(newLevel | 0x80);
                            } else {
                                currentEmbeddingValue = newLevel;
                            }
                            
                            // Adjust level of format mark (for expositional purposes only, this gets
                            // removed later).
                            embeddings[i] = currentEmbeddingValue;
                            break;
                        }
                        
                        // Otherwise new level is invalid, but a valid level can still be achieved if this
                        // level is 60 and we encounter an RLE or RLO further on.  So record that we
                        // 'almost' overflowed.
                        if (currentEmbeddingLevel == 60) {
                            overflowAlmostCounter++;
                            break;
                        }
                    }
                    
                    // Otherwise old or new level is invalid.
                    overflowCounter++;
                    break;
                    
                case PDF:
                    // The only case where this did not actually overflow but may have almost overflowed
                    // is when there was an RLE or RLO on level 60, which would result in level 61.  So we
                    // only test the almost overflow condition in that case.
                    //
                    // Also note that there may be a PDF without any pushes at all.
                    
                    if (overflowCounter > 0) {
                        --overflowCounter;
                    } else if (overflowAlmostCounter > 0 && currentEmbeddingLevel != 61) {
                        --overflowAlmostCounter;
                    } else if (stackCounter > 0) {
                        --stackCounter;
                        currentEmbeddingValue = embeddingValueStack[stackCounter];
                        currentEmbeddingLevel = (byte)(currentEmbeddingValue & 0x7f);
                    }
                    break;
                    
                case B:
                    // Rule X8.
                    
                    // These values are reset for clarity, in this implementation B can only
                    // occur as the last code in the array.
                    stackCounter = 0;
                    overflowCounter = 0;
                    overflowAlmostCounter = 0;
                    currentEmbeddingLevel = paragraphEmbeddingLevel;
                    currentEmbeddingValue = paragraphEmbeddingLevel;
                    
                    embeddings[i] = paragraphEmbeddingLevel;
                    break;
                    
                default:
                    break;
            }
        }
        
        return embeddings;
    }
    
    
    /**
     * 3) resolving weak types
     * Rules W1-W7.
     *
     * Note that some weak types (EN, AN) remain after this processing is complete.
     */
    private void resolveWeakTypes(int start, int limit, byte level, byte sor, byte eor) {
        
        // Rule W1.
        // Changes all NSMs.
        byte preceedingCharacterType = sor;
        for (int i = start; i < limit; ++i) {
            byte t = resultTypes[i];
            if (t == NSM) {
                resultTypes[i] = preceedingCharacterType;
            } else {
                preceedingCharacterType = t;
            }
        }
        
        // Rule W2.
        // EN does not change at the start of the run, because sor != AL.
        for (int i = start; i < limit; ++i) {
            if (resultTypes[i] == EN) {
                for (int j = i - 1; j >= start; --j) {
                    byte t = resultTypes[j];
                    if (t == L || t == R || t == AL) {
                        if (t == AL) {
                            resultTypes[i] = AN;
                        }
                        break;
                    }
                }
            }
        }
        
        // Rule W3.
        for (int i = start; i < limit; ++i) {
            if (resultTypes[i] == AL) {
                resultTypes[i] = R;
            }
        }
        
        // Rule W4.
        // Since there must be values on both sides for this rule to have an
        // effect, the scan skips the first and last value.
        //
        // Although the scan proceeds left to right, and changes the type values
        // in a way that would appear to affect the computations later in the scan,
        // there is actually no problem.  A change in the current value can only
        // affect the value to its immediate right, and only affect it if it is
        // ES or CS.  But the current value can only change if the value to its
        // right is not ES or CS.  Thus either the current value will not change,
        // or its change will have no effect on the remainder of the analysis.
        
        for (int i = start + 1; i < limit - 1; ++i) {
            if (resultTypes[i] == ES || resultTypes[i] == CS) {
                byte prevSepType = resultTypes[i-1];
                byte succSepType = resultTypes[i+1];
                if (prevSepType == EN && succSepType == EN) {
                    resultTypes[i] = EN;
                } else if (resultTypes[i] == CS && prevSepType == AN && succSepType == AN) {
                    resultTypes[i] = AN;
                }
            }
        }
        
        // Rule W5.
        for (int i = start; i < limit; ++i) {
            if (resultTypes[i] == ET) {
                // locate end of sequence
                int runstart = i;
                int runlimit = findRunLimit(runstart, limit, new byte[] { ET });
                
                // check values at ends of sequence
                byte t = runstart == start ? sor : resultTypes[runstart - 1];
                
                if (t != EN) {
                    t = runlimit == limit ? eor : resultTypes[runlimit];
                }
                
                if (t == EN) {
                    setTypes(runstart, runlimit, EN);
                }
                
                // continue at end of sequence
                i = runlimit;
            }
        }
        
        // Rule W6.
        for (int i = start; i < limit; ++i) {
            byte t = resultTypes[i];
            if (t == ES || t == ET || t == CS) {
                resultTypes[i] = ON;
            }
        }
        
        // Rule W7.
        for (int i = start; i < limit; ++i) {
            if (resultTypes[i] == EN) {
                // set default if we reach start of run
                byte prevStrongType = sor;
                for (int j = i - 1; j >= start; --j) {
                    byte t = resultTypes[j];
                    if (t == L || t == R) { // AL's have been removed
                        prevStrongType = t;
                        break;
                    }
                }
                if (prevStrongType == L) {
                    resultTypes[i] = L;
                }
            }
        }
    }
    
    /**
     * 6) resolving neutral types
     * Rules N1-N2.
     */
    private void resolveNeutralTypes(int start, int limit, byte level, byte sor, byte eor) {
        
        for (int i = start; i < limit; ++i) {
            byte t = resultTypes[i];
            if (t == WS || t == ON || t == B || t == S) {
                // find bounds of run of neutrals
                int runstart = i;
                int runlimit = findRunLimit(runstart, limit, new byte[] {B, S, WS, ON});
                
                // determine effective types at ends of run
                byte leadingType;
                byte trailingType;
                
                if (runstart == start) {
                    leadingType = sor;
                } else {
                    leadingType = resultTypes[runstart - 1];
                    if (leadingType == L || leadingType == R) {
                        // found the strong type
                    } else if (leadingType == AN) {
                        leadingType = R;
                    } else if (leadingType == EN) {
                        // Since EN's with previous strong L types have been changed
                        // to L in W7, the leadingType must be R.
                        leadingType = R;
                    }
                }
                
                if (runlimit == limit) {
                    trailingType = eor;
                } else {
                    trailingType = resultTypes[runlimit];
                    if (trailingType == L || trailingType == R) {
                        // found the strong type
                    } else if (trailingType == AN) {
                        trailingType = R;
                    } else if (trailingType == EN) {
                        trailingType = R;
                    }
                }
                
                byte resolvedType;
                if (leadingType == trailingType) {
                    // Rule N1.
                    resolvedType = leadingType;
                } else {
                    // Rule N2.
                    // Notice the embedding level of the run is used, not
                    // the paragraph embedding level.
                    resolvedType = typeForLevel(level);
                }
                
                setTypes(runstart, runlimit, resolvedType);
                
                // skip over run of (former) neutrals
                i = runlimit;
            }
        }
    }
    
    /**
     * 7) resolving implicit embedding levels
     * Rules I1, I2.
     */
    private void resolveImplicitLevels(int start, int limit, byte level, byte sor, byte eor) {
        if ((level & 1) == 0) { // even level
            for (int i = start; i < limit; ++i) {
                byte t = resultTypes[i];
                // Rule I1.
                if (t == L ) {
                    // no change
                } else if (t == R) {
                    resultLevels[i] += 1;
                } else { // t == AN || t == EN
                    resultLevels[i] += 2;
                }
            }
        } else { // odd level
            for (int i = start; i < limit; ++i) {
                byte t = resultTypes[i];
                // Rule I2.
                if (t == R) {
                    // no change
                } else { // t == L || t == AN || t == EN
                    resultLevels[i] += 1;
                }
            }
        }
    }
    
    //
    // Output
    //
    
    public byte[] getLevels() {
        return getLevels(new int[]{textLength});
    }
    
    /**
     * Return levels array breaking lines at offsets in linebreaks. <br>
     * Rule L1.
     * <p>
     * The returned levels array contains the resolved level for each
     * bidi code passed to the constructor.
     * <p>
     * The linebreaks array must include at least one value.
     * The values must be in strictly increasing order (no duplicates)
     * between 1 and the length of the text, inclusive.  The last value
     * must be the length of the text.
     *
     * @param linebreaks the offsets at which to break the paragraph
     * @return the resolved levels of the text
     */
    public byte[] getLevels(int[] linebreaks) {
        
        // Note that since the previous processing has removed all
        // P, S, and WS values from resultTypes, the values referred to
        // in these rules are the initial types, before any processing
        // has been applied (including processing of overrides).
        //
        // This example implementation has reinserted explicit format codes
        // and BN, in order that the levels array correspond to the
        // initial text.  Their final placement is not normative.
        // These codes are treated like WS in this implementation,
        // so they don't interrupt sequences of WS.
        
        validateLineBreaks(linebreaks, textLength);
        
        byte[] result = (byte[])resultLevels.clone(); // will be returned to caller
        
        // don't worry about linebreaks since if there is a break within
        // a series of WS values preceding S, the linebreak itself
        // causes the reset.
        for (int i = 0; i < result.length; ++i) {
            byte t = initialTypes[i];
            if (t == B || t == S) {
                // Rule L1, clauses one and two.
                result[i] = paragraphEmbeddingLevel;
                
                // Rule L1, clause three.
                for (int j = i - 1; j >= 0; --j) {
                    if (isWhitespace(initialTypes[j])) { // including format codes
                        result[j] = paragraphEmbeddingLevel;
                    } else {
                        break;
                    }
                }
            }
        }
        
        // Rule L1, clause four.
        int start = 0;
        for (int i = 0; i < linebreaks.length; ++i) {
            int limit = linebreaks[i];
            for (int j = limit - 1; j >= start; --j) {
                if (isWhitespace(initialTypes[j])) { // including format codes
                    result[j] = paragraphEmbeddingLevel;
                } else {
                    break;
                }
            }
            
            start = limit;
        }
        
        return result;
    }
    
    /**
     * Return reordering array breaking lines at offsets in linebreaks.
     * <p>
     * The reordering array maps from a visual index to a logical index.
     * Lines are concatenated from left to right.  So for example, the
     * fifth character from the left on the third line is
     * <pre> getReordering(linebreaks)[linebreaks[1] + 4]</pre>
     * (linebreaks[1] is the position after the last character of the
     * second line, which is also the index of the first character on the
     * third line, and adding four gets the fifth character from the left).
     * <p>
     * The linebreaks array must include at least one value.
     * The values must be in strictly increasing order (no duplicates)
     * between 1 and the length of the text, inclusive.  The last value
     * must be the length of the text.
     *
     * @param linebreaks the offsets at which to break the paragraph.
     */
    public int[] getReordering(int[] linebreaks) {
        validateLineBreaks(linebreaks, textLength);
        
        byte[] levels = getLevels(linebreaks);
        
        return computeMultilineReordering(levels, linebreaks);
    }
    
    /**
     * Return multiline reordering array for a given level array.
     * Reordering does not occur across a line break.
     */
    private static int[] computeMultilineReordering(byte[] levels, int[] linebreaks) {
        int[] result = new int[levels.length];
        
        int start = 0;
        for (int i = 0; i < linebreaks.length; ++i) {
            int limit = linebreaks[i];
            
            byte[] templevels = new byte[limit - start];
            System.arraycopy(levels, start, templevels, 0, templevels.length);
            
            int[] temporder = computeReordering(templevels);
            for (int j = 0; j < temporder.length; ++j) {
                result[start + j] = temporder[j] + start;
            }
            
            start = limit;
        }
        
        return result;
    }
    
    /**
     * Return reordering array for a given level array.  This reorders a single line.
     * The reordering is a visual to logical map.  For example,
     * the leftmost char is string.charAt(order[0]).
     * Rule L2.
     */
    private static int[] computeReordering(byte[] levels) {
        int lineLength = levels.length;
        
        int[] result = new int[lineLength];
        
        // initialize order
        for (int i = 0; i < lineLength; ++i) {
            result[i] = i;
        }
        
        // locate highest level found on line.
        // Note the rules say text, but no reordering across line bounds is performed,
        // so this is sufficient.
        byte highestLevel = 0;
        byte lowestOddLevel = 63;
        for (int i = 0; i < lineLength; ++i) {
            byte level = levels[i];
            if (level > highestLevel) {
                highestLevel = level;
            }
            if (((level & 1) != 0) && level < lowestOddLevel) {
                lowestOddLevel = level;
            }
        }
        
        for (int level = highestLevel; level >= lowestOddLevel; --level) {
            for (int i = 0; i < lineLength; ++i) {
                if (levels[i] >= level) {
                    // find range of text at or above this level
                    int start = i;
                    int limit = i + 1;
                    while (limit < lineLength && levels[limit] >= level) {
                        ++limit;
                    }
                    
                    // reverse run
                    for (int j = start, k = limit - 1; j < k; ++j, --k) {
                        int temp = result[j];
                        result[j] = result[k];
                        result[k] = temp;
                    }
                    
                    // skip to end of level run
                    i = limit;
                }
            }
        }
        
        return result;
    }
    
    /**
     * Return the base level of the paragraph.
     */
    public byte getBaseLevel() {
        return paragraphEmbeddingLevel;
    }
    
    // --- internal utilities -------------------------------------------------
    
    /**
     * Return true if the type is considered a whitespace type for the line break rules.
     */
    private static boolean isWhitespace(byte biditype) {
        switch (biditype) {
            case LRE:
            case RLE:
            case LRO:
            case RLO:
            case PDF:
            case BN:
            case WS:
                return true;
            default:
                return false;
        }
    }
    
    /**
     * Return the strong type (L or R) corresponding to the level.
     */
    private static byte typeForLevel(int level) {
        return ((level & 0x1) == 0) ? L : R;
    }
    
    /**
     * Return the limit of the run starting at index that includes only resultTypes in validSet.
     * This checks the value at index, and will return index if that value is not in validSet.
     */
    private int findRunLimit(int index, int limit, byte[] validSet) {
        --index;
        loop:
            while (++index < limit) {
                byte t = resultTypes[index];
                for (int i = 0; i < validSet.length; ++i) {
                    if (t == validSet[i]) {
                        continue loop;
                    }
                }
                // didn't find a match in validSet
                return index;
            }
            return limit;
    }
    
    /**
     * Return the start of the run including index that includes only resultTypes in validSet.
     * This assumes the value at index is valid, and does not check it.
     */
    private int findRunStart(int index, byte[] validSet) {
        loop:
            while (--index >= 0) {
                byte t = resultTypes[index];
                for (int i = 0; i < validSet.length; ++i) {
                    if (t == validSet[i]) {
                        continue loop;
                    }
                }
                return index + 1;
            }
            return 0;
    }
    
    /**
     * Set resultTypes from start up to (but not including) limit to newType.
     */
    private void setTypes(int start, int limit, byte newType) {
        for (int i = start; i < limit; ++i) {
            resultTypes[i] = newType;
        }
    }
    
    /**
     * Set resultLevels from start up to (but not including) limit to newLevel.
     */
    private void setLevels(int start, int limit, byte newLevel) {
        for (int i = start; i < limit; ++i) {
            resultLevels[i] = newLevel;
        }
    }
    
    // --- input validation ---------------------------------------------------
    
    /**
     * Throw exception if type array is invalid.
     */
    private static void validateTypes(byte[] types) {
        if (types == null) {
            throw new IllegalArgumentException(MessageLocalization.getComposedMessage("types.is.null"));
        }
        for (int i = 0; i < types.length; ++i) {
            if (types[i] < TYPE_MIN || types[i] > TYPE_MAX) {
                throw new IllegalArgumentException(MessageLocalization.getComposedMessage("illegal.type.value.at.1.2", String.valueOf(i), String.valueOf(types[i])));
            }
        }
        for (int i = 0; i < types.length - 1; ++i) {
            if (types[i] == B) {
                throw new IllegalArgumentException(MessageLocalization.getComposedMessage("b.type.before.end.of.paragraph.at.index.1", i));
            }
        }
    }
    
    /**
     * Throw exception if paragraph embedding level is invalid. Special allowance for -1 so that
     * default processing can still be performed when using this API.
     */
    private static void validateParagraphEmbeddingLevel(byte paragraphEmbeddingLevel) {
        if (paragraphEmbeddingLevel != -1 &&
        paragraphEmbeddingLevel != 0 &&
        paragraphEmbeddingLevel != 1) {
            throw new IllegalArgumentException(MessageLocalization.getComposedMessage("illegal.paragraph.embedding.level.1", paragraphEmbeddingLevel));
        }
    }
    
    /**
     * Throw exception if line breaks array is invalid.
     */
    private static void validateLineBreaks(int[] linebreaks, int textLength) {
        int prev = 0;
        for (int i = 0; i < linebreaks.length; ++i) {
            int next = linebreaks[i];
            if (next <= prev) {
                throw new IllegalArgumentException(MessageLocalization.getComposedMessage("bad.linebreak.1.at.index.2", String.valueOf(next), String.valueOf(i)));
            }
            prev = next;
        }
        if (prev != textLength) {
            throw new IllegalArgumentException(MessageLocalization.getComposedMessage("last.linebreak.must.be.at.1", textLength));
        }
    }
    
    private static final byte rtypes[] = new byte[0x10000];
    
    private static char baseTypes[] = {
        0, 8, (char)BN, 9, 9, (char)S, 10, 10, (char)B, 11, 11, (char)S, 12, 12, (char)WS, 13, 13, (char)B,
        14, 27, (char)BN, 28, 30, (char)B, 31, 31, (char)S, 32, 32, (char)WS, 33, 34, (char)ON, 35, 37, (char)ET,
        38, 42, (char)ON, 43, 43, (char)ET, 44, 44, (char)CS, 45, 45, (char)ET, 46, 46, (char)CS, 47, 47, (char)ES,
        48, 57, (char)EN, 58, 58, (char)CS, 59, 64, (char)ON, 65, 90, (char)L, 91, 96, (char)ON, 97, 122, (char)L,
        123, 126, (char)ON, 127, 132, (char)BN, 133, 133, (char)B, 134, 159, (char)BN, 160, 160, (char)CS,
        161, 161, (char)ON, 162, 165, (char)ET, 166, 169, (char)ON, 170, 170, (char)L, 171, 175, (char)ON,
        176, 177, (char)ET, 178, 179, (char)EN, 180, 180, (char)ON, 181, 181, (char)L, 182, 184, (char)ON,
        185, 185, (char)EN, 186, 186, (char)L, 187, 191, (char)ON, 192, 214, (char)L, 215, 215, (char)ON,
        216, 246, (char)L, 247, 247, (char)ON, 248, 696, (char)L, 697, 698, (char)ON, 699, 705, (char)L,
        706, 719, (char)ON, 720, 721, (char)L, 722, 735, (char)ON, 736, 740, (char)L, 741, 749, (char)ON,
        750, 750, (char)L, 751, 767, (char)ON, 768, 855, (char)NSM, 856, 860, (char)L, 861, 879, (char)NSM,
        880, 883, (char)L, 884, 885, (char)ON, 886, 893, (char)L, 894, 894, (char)ON, 895, 899, (char)L,
        900, 901, (char)ON, 902, 902, (char)L, 903, 903, (char)ON, 904, 1013, (char)L, 1014, 1014, (char)ON,
        1015, 1154, (char)L, 1155, 1158, (char)NSM, 1159, 1159, (char)L, 1160, 1161, (char)NSM,
        1162, 1417, (char)L, 1418, 1418, (char)ON, 1419, 1424, (char)L, 1425, 1441, (char)NSM,
        1442, 1442, (char)L, 1443, 1465, (char)NSM, 1466, 1466, (char)L, 1467, 1469, (char)NSM,
        1470, 1470, (char)R, 1471, 1471, (char)NSM, 1472, 1472, (char)R, 1473, 1474, (char)NSM,
        1475, 1475, (char)R, 1476, 1476, (char)NSM, 1477, 1487, (char)L, 1488, 1514, (char)R,
        1515, 1519, (char)L, 1520, 1524, (char)R, 1525, 1535, (char)L, 1536, 1539, (char)AL,
        1540, 1547, (char)L, 1548, 1548, (char)CS, 1549, 1549, (char)AL, 1550, 1551, (char)ON,
        1552, 1557, (char)NSM, 1558, 1562, (char)L, 1563, 1563, (char)AL, 1564, 1566, (char)L,
        1567, 1567, (char)AL, 1568, 1568, (char)L, 1569, 1594, (char)AL, 1595, 1599, (char)L,
        1600, 1610, (char)AL, 1611, 1624, (char)NSM, 1625, 1631, (char)L, 1632, 1641, (char)AN,
        1642, 1642, (char)ET, 1643, 1644, (char)AN, 1645, 1647, (char)AL, 1648, 1648, (char)NSM,
        1649, 1749, (char)AL, 1750, 1756, (char)NSM, 1757, 1757, (char)AL, 1758, 1764, (char)NSM,
        1765, 1766, (char)AL, 1767, 1768, (char)NSM, 1769, 1769, (char)ON, 1770, 1773, (char)NSM,
        1774, 1775, (char)AL, 1776, 1785, (char)EN, 1786, 1805, (char)AL, 1806, 1806, (char)L,
        1807, 1807, (char)BN, 1808, 1808, (char)AL, 1809, 1809, (char)NSM, 1810, 1839, (char)AL,
        1840, 1866, (char)NSM, 1867, 1868, (char)L, 1869, 1871, (char)AL, 1872, 1919, (char)L,
        1920, 1957, (char)AL, 1958, 1968, (char)NSM, 1969, 1969, (char)AL, 1970, 2304, (char)L,
        2305, 2306, (char)NSM, 2307, 2363, (char)L, 2364, 2364, (char)NSM, 2365, 2368, (char)L,
        2369, 2376, (char)NSM, 2377, 2380, (char)L, 2381, 2381, (char)NSM, 2382, 2384, (char)L,
        2385, 2388, (char)NSM, 2389, 2401, (char)L, 2402, 2403, (char)NSM, 2404, 2432, (char)L,
        2433, 2433, (char)NSM, 2434, 2491, (char)L, 2492, 2492, (char)NSM, 2493, 2496, (char)L,
        2497, 2500, (char)NSM, 2501, 2508, (char)L, 2509, 2509, (char)NSM, 2510, 2529, (char)L,
        2530, 2531, (char)NSM, 2532, 2545, (char)L, 2546, 2547, (char)ET, 2548, 2560, (char)L,
        2561, 2562, (char)NSM, 2563, 2619, (char)L, 2620, 2620, (char)NSM, 2621, 2624, (char)L,
        2625, 2626, (char)NSM, 2627, 2630, (char)L, 2631, 2632, (char)NSM, 2633, 2634, (char)L,
        2635, 2637, (char)NSM, 2638, 2671, (char)L, 2672, 2673, (char)NSM, 2674, 2688, (char)L,
        2689, 2690, (char)NSM, 2691, 2747, (char)L, 2748, 2748, (char)NSM, 2749, 2752, (char)L,
        2753, 2757, (char)NSM, 2758, 2758, (char)L, 2759, 2760, (char)NSM, 2761, 2764, (char)L,
        2765, 2765, (char)NSM, 2766, 2785, (char)L, 2786, 2787, (char)NSM, 2788, 2800, (char)L,
        2801, 2801, (char)ET, 2802, 2816, (char)L, 2817, 2817, (char)NSM, 2818, 2875, (char)L,
        2876, 2876, (char)NSM, 2877, 2878, (char)L, 2879, 2879, (char)NSM, 2880, 2880, (char)L,
        2881, 2883, (char)NSM, 2884, 2892, (char)L, 2893, 2893, (char)NSM, 2894, 2901, (char)L,
        2902, 2902, (char)NSM, 2903, 2945, (char)L, 2946, 2946, (char)NSM, 2947, 3007, (char)L,
        3008, 3008, (char)NSM, 3009, 3020, (char)L, 3021, 3021, (char)NSM, 3022, 3058, (char)L,
        3059, 3064, (char)ON, 3065, 3065, (char)ET, 3066, 3066, (char)ON, 3067, 3133, (char)L,
        3134, 3136, (char)NSM, 3137, 3141, (char)L, 3142, 3144, (char)NSM, 3145, 3145, (char)L,
        3146, 3149, (char)NSM, 3150, 3156, (char)L, 3157, 3158, (char)NSM, 3159, 3259, (char)L,
        3260, 3260, (char)NSM, 3261, 3275, (char)L, 3276, 3277, (char)NSM, 3278, 3392, (char)L,
        3393, 3395, (char)NSM, 3396, 3404, (char)L, 3405, 3405, (char)NSM, 3406, 3529, (char)L,
        3530, 3530, (char)NSM, 3531, 3537, (char)L, 3538, 3540, (char)NSM, 3541, 3541, (char)L,
        3542, 3542, (char)NSM, 3543, 3632, (char)L, 3633, 3633, (char)NSM, 3634, 3635, (char)L,
        3636, 3642, (char)NSM, 3643, 3646, (char)L, 3647, 3647, (char)ET, 3648, 3654, (char)L,
        3655, 3662, (char)NSM, 3663, 3760, (char)L, 3761, 3761, (char)NSM, 3762, 3763, (char)L,
        3764, 3769, (char)NSM, 3770, 3770, (char)L, 3771, 3772, (char)NSM, 3773, 3783, (char)L,
        3784, 3789, (char)NSM, 3790, 3863, (char)L, 3864, 3865, (char)NSM, 3866, 3892, (char)L,
        3893, 3893, (char)NSM, 3894, 3894, (char)L, 3895, 3895, (char)NSM, 3896, 3896, (char)L,
        3897, 3897, (char)NSM, 3898, 3901, (char)ON, 3902, 3952, (char)L, 3953, 3966, (char)NSM,
        3967, 3967, (char)L, 3968, 3972, (char)NSM, 3973, 3973, (char)L, 3974, 3975, (char)NSM,
        3976, 3983, (char)L, 3984, 3991, (char)NSM, 3992, 3992, (char)L, 3993, 4028, (char)NSM,
        4029, 4037, (char)L, 4038, 4038, (char)NSM, 4039, 4140, (char)L, 4141, 4144, (char)NSM,
        4145, 4145, (char)L, 4146, 4146, (char)NSM, 4147, 4149, (char)L, 4150, 4151, (char)NSM,
        4152, 4152, (char)L, 4153, 4153, (char)NSM, 4154, 4183, (char)L, 4184, 4185, (char)NSM,
        4186, 5759, (char)L, 5760, 5760, (char)WS, 5761, 5786, (char)L, 5787, 5788, (char)ON,
        5789, 5905, (char)L, 5906, 5908, (char)NSM, 5909, 5937, (char)L, 5938, 5940, (char)NSM,
        5941, 5969, (char)L, 5970, 5971, (char)NSM, 5972, 6001, (char)L, 6002, 6003, (char)NSM,
        6004, 6070, (char)L, 6071, 6077, (char)NSM, 6078, 6085, (char)L, 6086, 6086, (char)NSM,
        6087, 6088, (char)L, 6089, 6099, (char)NSM, 6100, 6106, (char)L, 6107, 6107, (char)ET,
        6108, 6108, (char)L, 6109, 6109, (char)NSM, 6110, 6127, (char)L, 6128, 6137, (char)ON,
        6138, 6143, (char)L, 6144, 6154, (char)ON, 6155, 6157, (char)NSM, 6158, 6158, (char)WS,
        6159, 6312, (char)L, 6313, 6313, (char)NSM, 6314, 6431, (char)L, 6432, 6434, (char)NSM,
        6435, 6438, (char)L, 6439, 6443, (char)NSM, 6444, 6449, (char)L, 6450, 6450, (char)NSM,
        6451, 6456, (char)L, 6457, 6459, (char)NSM, 6460, 6463, (char)L, 6464, 6464, (char)ON,
        6465, 6467, (char)L, 6468, 6469, (char)ON, 6470, 6623, (char)L, 6624, 6655, (char)ON,
        6656, 8124, (char)L, 8125, 8125, (char)ON, 8126, 8126, (char)L, 8127, 8129, (char)ON,
        8130, 8140, (char)L, 8141, 8143, (char)ON, 8144, 8156, (char)L, 8157, 8159, (char)ON,
        8160, 8172, (char)L, 8173, 8175, (char)ON, 8176, 8188, (char)L, 8189, 8190, (char)ON,
        8191, 8191, (char)L, 8192, 8202, (char)WS, 8203, 8205, (char)BN, 8206, 8206, (char)L,
        8207, 8207, (char)R, 8208, 8231, (char)ON, 8232, 8232, (char)WS, 8233, 8233, (char)B,
        8234, 8234, (char)LRE, 8235, 8235, (char)RLE, 8236, 8236, (char)PDF, 8237, 8237, (char)LRO,
        8238, 8238, (char)RLO, 8239, 8239, (char)WS, 8240, 8244, (char)ET, 8245, 8276, (char)ON,
        8277, 8278, (char)L, 8279, 8279, (char)ON, 8280, 8286, (char)L, 8287, 8287, (char)WS,
        8288, 8291, (char)BN, 8292, 8297, (char)L, 8298, 8303, (char)BN, 8304, 8304, (char)EN,
        8305, 8307, (char)L, 8308, 8313, (char)EN, 8314, 8315, (char)ET, 8316, 8318, (char)ON,
        8319, 8319, (char)L, 8320, 8329, (char)EN, 8330, 8331, (char)ET, 8332, 8334, (char)ON,
        8335, 8351, (char)L, 8352, 8369, (char)ET, 8370, 8399, (char)L, 8400, 8426, (char)NSM,
        8427, 8447, (char)L, 8448, 8449, (char)ON, 8450, 8450, (char)L, 8451, 8454, (char)ON,
        8455, 8455, (char)L, 8456, 8457, (char)ON, 8458, 8467, (char)L, 8468, 8468, (char)ON,
        8469, 8469, (char)L, 8470, 8472, (char)ON, 8473, 8477, (char)L, 8478, 8483, (char)ON,
        8484, 8484, (char)L, 8485, 8485, (char)ON, 8486, 8486, (char)L, 8487, 8487, (char)ON,
        8488, 8488, (char)L, 8489, 8489, (char)ON, 8490, 8493, (char)L, 8494, 8494, (char)ET,
        8495, 8497, (char)L, 8498, 8498, (char)ON, 8499, 8505, (char)L, 8506, 8507, (char)ON,
        8508, 8511, (char)L, 8512, 8516, (char)ON, 8517, 8521, (char)L, 8522, 8523, (char)ON,
        8524, 8530, (char)L, 8531, 8543, (char)ON, 8544, 8591, (char)L, 8592, 8721, (char)ON,
        8722, 8723, (char)ET, 8724, 9013, (char)ON, 9014, 9082, (char)L, 9083, 9108, (char)ON,
        9109, 9109, (char)L, 9110, 9168, (char)ON, 9169, 9215, (char)L, 9216, 9254, (char)ON,
        9255, 9279, (char)L, 9280, 9290, (char)ON, 9291, 9311, (char)L, 9312, 9371, (char)EN,
        9372, 9449, (char)L, 9450, 9450, (char)EN, 9451, 9751, (char)ON, 9752, 9752, (char)L,
        9753, 9853, (char)ON, 9854, 9855, (char)L, 9856, 9873, (char)ON, 9874, 9887, (char)L,
        9888, 9889, (char)ON, 9890, 9984, (char)L, 9985, 9988, (char)ON, 9989, 9989, (char)L,
        9990, 9993, (char)ON, 9994, 9995, (char)L, 9996, 10023, (char)ON, 10024, 10024, (char)L,
        10025, 10059, (char)ON, 10060, 10060, (char)L, 10061, 10061, (char)ON, 10062, 10062, (char)L,
        10063, 10066, (char)ON, 10067, 10069, (char)L, 10070, 10070, (char)ON, 10071, 10071, (char)L,
        10072, 10078, (char)ON, 10079, 10080, (char)L, 10081, 10132, (char)ON, 10133, 10135, (char)L,
        10136, 10159, (char)ON, 10160, 10160, (char)L, 10161, 10174, (char)ON, 10175, 10191, (char)L,
        10192, 10219, (char)ON, 10220, 10223, (char)L, 10224, 11021, (char)ON, 11022, 11903, (char)L,
        11904, 11929, (char)ON, 11930, 11930, (char)L, 11931, 12019, (char)ON, 12020, 12031, (char)L,
        12032, 12245, (char)ON, 12246, 12271, (char)L, 12272, 12283, (char)ON, 12284, 12287, (char)L,
        12288, 12288, (char)WS, 12289, 12292, (char)ON, 12293, 12295, (char)L, 12296, 12320, (char)ON,
        12321, 12329, (char)L, 12330, 12335, (char)NSM, 12336, 12336, (char)ON, 12337, 12341, (char)L,
        12342, 12343, (char)ON, 12344, 12348, (char)L, 12349, 12351, (char)ON, 12352, 12440, (char)L,
        12441, 12442, (char)NSM, 12443, 12444, (char)ON, 12445, 12447, (char)L, 12448, 12448, (char)ON,
        12449, 12538, (char)L, 12539, 12539, (char)ON, 12540, 12828, (char)L, 12829, 12830, (char)ON,
        12831, 12879, (char)L, 12880, 12895, (char)ON, 12896, 12923, (char)L, 12924, 12925, (char)ON,
        12926, 12976, (char)L, 12977, 12991, (char)ON, 12992, 13003, (char)L, 13004, 13007, (char)ON,
        13008, 13174, (char)L, 13175, 13178, (char)ON, 13179, 13277, (char)L, 13278, 13279, (char)ON,
        13280, 13310, (char)L, 13311, 13311, (char)ON, 13312, 19903, (char)L, 19904, 19967, (char)ON,
        19968, 42127, (char)L, 42128, 42182, (char)ON, 42183, 64284, (char)L, 64285, 64285, (char)R,
        64286, 64286, (char)NSM, 64287, 64296, (char)R, 64297, 64297, (char)ET, 64298, 64310, (char)R,
        64311, 64311, (char)L, 64312, 64316, (char)R, 64317, 64317, (char)L, 64318, 64318, (char)R,
        64319, 64319, (char)L, 64320, 64321, (char)R, 64322, 64322, (char)L, 64323, 64324, (char)R,
        64325, 64325, (char)L, 64326, 64335, (char)R, 64336, 64433, (char)AL, 64434, 64466, (char)L,
        64467, 64829, (char)AL, 64830, 64831, (char)ON, 64832, 64847, (char)L, 64848, 64911, (char)AL,
        64912, 64913, (char)L, 64914, 64967, (char)AL, 64968, 65007, (char)L, 65008, 65020, (char)AL,
        65021, 65021, (char)ON, 65022, 65023, (char)L, 65024, 65039, (char)NSM, 65040, 65055, (char)L,
        65056, 65059, (char)NSM, 65060, 65071, (char)L, 65072, 65103, (char)ON, 65104, 65104, (char)CS,
        65105, 65105, (char)ON, 65106, 65106, (char)CS, 65107, 65107, (char)L, 65108, 65108, (char)ON,
        65109, 65109, (char)CS, 65110, 65118, (char)ON, 65119, 65119, (char)ET, 65120, 65121, (char)ON,
        65122, 65123, (char)ET, 65124, 65126, (char)ON, 65127, 65127, (char)L, 65128, 65128, (char)ON,
        65129, 65130, (char)ET, 65131, 65131, (char)ON, 65132, 65135, (char)L, 65136, 65140, (char)AL,
        65141, 65141, (char)L, 65142, 65276, (char)AL, 65277, 65278, (char)L, 65279, 65279, (char)BN,
        65280, 65280, (char)L, 65281, 65282, (char)ON, 65283, 65285, (char)ET, 65286, 65290, (char)ON,
        65291, 65291, (char)ET, 65292, 65292, (char)CS, 65293, 65293, (char)ET, 65294, 65294, (char)CS,
        65295, 65295, (char)ES, 65296, 65305, (char)EN, 65306, 65306, (char)CS, 65307, 65312, (char)ON,
        65313, 65338, (char)L, 65339, 65344, (char)ON, 65345, 65370, (char)L, 65371, 65381, (char)ON,
        65382, 65503, (char)L, 65504, 65505, (char)ET, 65506, 65508, (char)ON, 65509, 65510, (char)ET,
        65511, 65511, (char)L, 65512, 65518, (char)ON, 65519, 65528, (char)L, 65529, 65531, (char)BN,
        65532, 65533, (char)ON, 65534, 65535, (char)L};
        
    static {
        for (int k = 0; k < baseTypes.length; ++k) {
            int start = baseTypes[k];
            int end = baseTypes[++k];
            byte b = (byte)baseTypes[++k];
            while (start <= end)
                rtypes[start++] = b;
        }
    }        
}
