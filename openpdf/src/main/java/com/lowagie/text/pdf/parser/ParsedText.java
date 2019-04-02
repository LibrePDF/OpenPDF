/*
  dgd: com.lowagie.text.pdf.parser
  <p>
  Copyright 2005 by David G. Durand.
  <p>
  The contents of this file are subject to the Mozilla Public License Version 1.1
  (the "License"); you may not use this file except in compliance with the License.
  You may obtain a copy of the License at http://www.mozilla.org/MPL/
  *
  Software distributed under the License is distributed on an "AS IS" basis,
  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  for the specific language governing rights and limitations under the License.
  <p>
  The Original Code is 'iText, a free JAVA-PDF library'.
  <p>
  The Initial Developer of the Original Code is Bruno Lowagie. Portions created by
  the Initial Developer are Copyright (C) 1999, 2000, 2001, 2002 by Bruno Lowagie.
  All Rights Reserved.
  Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
  are Copyright (C) 2000, 2001, 2002 by Paulo Soares. All Rights Reserved.
  <p>
  Contributor(s): all the names of the contributors are added in the source code
  where applicable.
  <p>
  Alternatively, the contents of this file may be used under the terms of the
  LGPL license (the "GNU LIBRARY GENERAL PUBLIC LICENSE"), in which case the
  provisions of LGPL are applicable instead of those above.  If you wish to
  allow use of your version of this file only under the terms of the LGPL
  License and not to allow others to use your version of this file under
  the MPL, indicate your decision by deleting the provisions above and
  replace them with the notice and other provisions required by the LGPL.
  If you do not delete the provisions above, a recipient may use your version
  of this file under either the MPL or the GNU LIBRARY GENERAL PUBLIC LICENSE.
  <p>
  This library is free software; you can redistribute it and/or modify it
  under the terms of the MPL as stated above or under the terms of the GNU
  Library General Public License as published by the Free Software Foundation;
  either version 2 of the License, or any later version.
  <p>
  This library is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  FOR A PARTICULAR PURPOSE. See the GNU Library general Public License for more
  details.
 */
package com.lowagie.text.pdf.parser;

import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.CMapAwareDocumentFont;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfString;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author dgd
 */
public class ParsedText extends ParsedTextImpl {

    private final Matrix textToUserSpaceTransformMatrix;
    private final GraphicsState graphicsState;

    /**
     * retain original PdfString as we need to distinguish between the code points contained there,
     * and the standard Java (Unicode strings) that actually represent the content of this text.
     */
    private PdfString pdfText = null;

    /**
     * This constructor should only be called when the origin for text display is at (0,0) and the
     * graphical state reflects all transformations of the baseline. This is in text space units.
     * <p>
     * Decodes a String (which will contain glyph ids encoded in the font's encoding) based on
     * the active font. This is supported for compatibility, but is no longer preferred.
     *
     * @param text          string
     * @param graphicsState graphical state
     * @param textMatrix    transform from text space to graphics (drawing space)
     */
    @Deprecated
    ParsedText(String text, GraphicsState graphicsState, Matrix textMatrix) {
        this(text, new GraphicsState(graphicsState), textMatrix.multiply(graphicsState.getCtm()),
                getUnscaledFontSpaceWidth(graphicsState));
    }

    /**
     * This constructor should only be called when the origin for text display is at (0,0) and the
     * graphical state reflects all transformations of the baseline. This is in text space units.
     *
     * @param text          string
     * @param graphicsState graphical state
     * @param textMatrix    transform from text space to graphics (drawing space)
     */
    ParsedText(PdfString text, GraphicsState graphicsState, Matrix textMatrix) {
        this(text, new GraphicsState(graphicsState), textMatrix.multiply(graphicsState.getCtm()),
                getUnscaledFontSpaceWidth(graphicsState));
    }

    /**
     * Internal constructor for a parsed text item. The constructors that call it
     * gather some information from the graphical state first.
     *
     * @param text          This is a PdfString containing code points for the current font, not actually characters.
     *                      If the font has multiByte glyphs, (Identity-H encoding) we reparse the string so that the code
     *                      points don't get split into multiple characters.
     * @param graphicsState graphical state
     * @param textMatrix    transform from text space to graphics (drawing space)
     * @param unscaledWidth width of the space character in the font.
     */
    private ParsedText(PdfString text, GraphicsState graphicsState, Matrix textMatrix, float unscaledWidth) {
        super(null, pointToUserSpace(0, 0, textMatrix),
                pointToUserSpace(getStringWidth(text.toString(), graphicsState), 0f, textMatrix),
                pointToUserSpace(1.0f, 0f, textMatrix),
                convertHeightToUser(graphicsState.getFontAscentDescriptor(), textMatrix),
                convertHeightToUser(graphicsState.getFontDescentDescriptor(), textMatrix),
                convertWidthToUser(unscaledWidth, textMatrix));
        if (BaseFont.IDENTITY_H.equals(graphicsState.getFont().getEncoding())) {
            pdfText = new PdfString(new String(text.getBytes(), StandardCharsets.UTF_16));
        } else {
            pdfText = text;
        }
        textToUserSpaceTransformMatrix = textMatrix;
        this.graphicsState = graphicsState;
    }

    /**
     * Internal constructor when the code points are already in a string.
     *
     * @param text          string
     * @param graphicsState graphical state
     * @param textMatrix    transform from text space to graphics (drawing space)
     * @param unscaledWidth width of the space character in the font.
     */
    @Deprecated
    private ParsedText(String text, GraphicsState graphicsState, Matrix textMatrix, float unscaledWidth) {
        super(text, pointToUserSpace(0, 0, textMatrix),
                pointToUserSpace(getStringWidth(text, graphicsState), 0f, textMatrix),
                pointToUserSpace(1.0f, 0f, textMatrix),
                convertHeightToUser(graphicsState.getFontAscentDescriptor(), textMatrix),
                convertHeightToUser(graphicsState.getFontDescentDescriptor(), textMatrix),
                convertWidthToUser(unscaledWidth, textMatrix));
        textToUserSpaceTransformMatrix = textMatrix;
        this.graphicsState = graphicsState;
    }

    /**
     * @param xOffset
     * @param yOffset
     * @param textToUserSpaceTransformMatrix
     * @return
     */
    private static Vector pointToUserSpace(float xOffset, float yOffset,
                                           Matrix textToUserSpaceTransformMatrix) {
        return new Vector(xOffset, yOffset, 1f)
                .cross(textToUserSpaceTransformMatrix);
    }

    /**
     * Calculates the width of a space character. If the font does not define a
     * width for a standard space character , we also attempt to use the width
     * of \u00A0 (a non-breaking space in many fonts)
     *
     * @param graphicsState graphic state including current transformation to page coordinates from
     *                      text measurement
     * @return the width of a single space character in text space units
     */
    private static float getUnscaledFontSpaceWidth(GraphicsState graphicsState) {
        char charToUse = ' ';
        if (graphicsState.getFont().getWidth(charToUse) == 0) {
            charToUse = '\u00A0';
        }
        return getStringWidth(String.valueOf(charToUse), graphicsState);
    }

    /**
     * Gets the width of a String in text space units
     *
     * @param string        the string that needs measuring
     * @param graphicsState graphic state including current transformation to page coordinates from
     *                      text measurement
     * @return the width of a String in text space units
     */
    private static float getStringWidth(String string, GraphicsState graphicsState) {
        char[] chars = string.toCharArray();
        float totalWidth = 0;
        for (char c : chars) {
            float w = graphicsState.getFont().getWidth(c) / 1000.0f;
            float wordSpacing = Character.isSpaceChar(c) ? graphicsState.getWordSpacing() : 0f;
            totalWidth += (w * graphicsState.getFontSize() + graphicsState.getCharacterSpacing() + wordSpacing)
                    * graphicsState.getHorizontalScaling();
        }

        return totalWidth;
    }

    /**
     * @param width
     * @param textToUserSpaceTransformMatrix
     * @return
     */
    private static float convertWidthToUser(float width,
                                            Matrix textToUserSpaceTransformMatrix) {
        Vector startPos = pointToUserSpace(0, 0, textToUserSpaceTransformMatrix);
        Vector endPos = pointToUserSpace(width, 0,
                textToUserSpaceTransformMatrix);
        return distance(startPos, endPos);
    }

    /**
     * @param startPos
     * @param endPos
     * @return
     */
    private static float distance(Vector startPos, Vector endPos) {
        return endPos.subtract(startPos).length();
    }

    /**
     * @param height
     * @param textToUserSpaceTransformMatrix
     * @return
     */
    private static float convertHeightToUser(float height,
                                             Matrix textToUserSpaceTransformMatrix) {
        Vector startPos = pointToUserSpace(0, 0, textToUserSpaceTransformMatrix);
        Vector endPos = pointToUserSpace(0, height,
                textToUserSpaceTransformMatrix);
        return distance(endPos, startPos);
    }

    /**
     * Decodes a Java String containing glyph ids encoded in the font's encoding, and determine the
     * unicode equivalent
     *
     * @param in the String that needs to be decoded
     * @return the decoded String
     */
    // FIXME unreachable block and default encoding
    protected String decode(String in) {
        byte[] bytes;
        if (BaseFont.IDENTITY_H.equals(graphicsState.getFont().getEncoding())) {
            bytes = in.getBytes(StandardCharsets.UTF_16);
        }
        bytes = in.getBytes();
        return graphicsState.getFont().decode(bytes, 0, bytes.length);
    }

    /**
     * This constructor should only be called when the origin for text display is at (0,0) and the
     * graphical state reflects all transformations of the baseline. This is in text space units.
     * <p>
     * Decodes a PdfString (which will contain glyph ids encoded in the font's encoding) based on
     * the active font, and determine the unicode equivalent
     *
     * @param pdfString the String that needs to be encoded
     * @return the encoded String
     * @since 2.1.7
     */
    protected String decode(PdfString pdfString) {
        byte[] bytes = pdfString.getOriginalBytes();
        return graphicsState.getFont().decode(bytes, 0, bytes.length);
    }

    /**
     * Break this string if there are spaces within it. If so, we mark the new Words appropriately
     * for later assembly.
     * <p>
     * We are guaranteed that every space (internal word break) in this parsed text object will
     * create a new word in the result of this method. We are not guaranteed that these Word objects
     * are actually words until they have been assembled.
     * <p>
     * The word following any space preserves that space in its string value, so that the assembler
     * will not erroneously merge words that should be separate, regardless of the spacing.
     *
     * @return list of Word objects.
     */
    public List<Word> getAsPartialWords() {
        List<Word> result = new ArrayList<>();
        CMapAwareDocumentFont font = graphicsState.getFont();
        char[] chars = pdfText.getOriginalChars();
        boolean[] hasSpace = new boolean[chars.length];
        float totalWidth = 0;
        StringBuffer wordAccum = new StringBuffer(3);
        float wordStartOffset = 0;
        boolean wordsAreComplete = preprocessString(chars, hasSpace);
        // Set When a word is created by whitespace that occurred before it.
        boolean currentBreakBefore = false;
        /* go through string splitting at spaces, and calculating widths */
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            float w = font.getWidth(c) / 1000.0f;

            if (hasSpace[i]) {
                if (wordAccum.length() > 0) {
                    result.add(createWord(wordAccum, wordStartOffset, totalWidth, getBaseline(),
                            wordsAreComplete, currentBreakBefore));
                    wordAccum = new StringBuffer();
                }
                if (!Character.isWhitespace(c)) {
                    wordStartOffset = totalWidth;
                }
                totalWidth += graphicsState.calculateCharacterWidthWithSpace(w);
                if (Character.isWhitespace(c)) {
                    wordStartOffset = totalWidth;
                }
                wordAccum.append(c);
                currentBreakBefore = true; // next word will be marked as result of a space-character break
            } else {
                wordAccum.append(c);
                totalWidth += graphicsState.calculateCharacterWidthWithoutSpace(w);
            }
        }
        if (wordAccum.length() > 0) {
            result.add(createWord(wordAccum, wordStartOffset, totalWidth, getBaseline(),
                    wordsAreComplete, currentBreakBefore));
        }
        return result;
    }

    /**
     * Calculate whether individual character positions (after font decoding from code to a
     * character), contain spaces and break words, and whether the resulting words should be treated
     * as complete (i.e. if any spaces were found.
     *
     * @param chars    to check
     * @param hasSpace array same length as chars, each position representing whether it breaks a word
     * @return true if any spaces were found.
     */
    private boolean preprocessString(char[] chars, boolean[] hasSpace) {
        boolean wordsAreComplete = false;
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            hasSpace[i] = false;
            String charValue = graphicsState.getFont().decode(c);

            if (charValue != null)
                for (char cFinal : charValue.toCharArray()) {
                    if (Character.isSpaceChar(cFinal)) {
                        wordsAreComplete = true;
                        hasSpace[i] = true;
                    }
                }
        }
        return wordsAreComplete;
    }

    /**
     * Create a word to represent a broken substring at a space. As spaces have zero "word length"
     * make sure that they also have a baseline to check
     *
     * @param wordAccum          buffer of characters
     * @param wordStartOffset    intial x-offset
     * @param wordEndOffset      ending x offset.
     * @param baseline           baseline of this word, so direction of progress can be measured in line ending
     *                           determination.
     * @param wordsAreComplete   true means characters in this word won't be split apart graphically
     * @param currentBreakBefore true if this word fragment represents a word boundary, and any preceding fragment
     *                           is complete.
     * @return the new word
     */
    private Word createWord(StringBuffer wordAccum,
                            float wordStartOffset,
                            float wordEndOffset,
                            Vector baseline,
                            boolean wordsAreComplete,
                            boolean currentBreakBefore) {
        return new Word(graphicsState.getFont().decode(wordAccum.toString()), getAscent(), getDescent(),
                pointToUserSpace(wordStartOffset, 0f, textToUserSpaceTransformMatrix),
                pointToUserSpace(wordEndOffset, 0f, textToUserSpaceTransformMatrix), baseline,
                getSingleSpaceWidth(), wordsAreComplete, currentBreakBefore);
    }

    /**
     * @param gs graphic state including current transformation to page coordinates from text
     *           measurement
     * @return the unscaled (i.e. in Text space) width of our text
     */
    public float getUnscaledTextWidth(GraphicsState gs) {
        return getStringWidth(getFontCodes(), gs);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.lowagie.text.pdf.parser.TextAssemblyBuffer#accumulate(com.lowagie.text.pdf.parser.TextAssembler, String)
     */
    @Override
    public void accumulate(TextAssembler textAssembler, String contextName) {
        textAssembler.process(this, contextName);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.lowagie.text.pdf.parser.TextAssemblyBuffer#assemble(com.lowagie.text.pdf.parser.TextAssembler)
     */
    @Override
    public void assemble(TextAssembler textAssembler) {
        textAssembler.renderText(this);
    }

    /**
     * when returning the text from this item, we need to decode the code points we have.
     *
     * @see com.lowagie.text.pdf.parser.ParsedTextImpl#getText()
     */
    @Nullable
    @Override
    public String getText() {
        String text = super.getText();
        if (text == null && pdfText != null) {
            return decode(pdfText);
        }
        return text;
    }

    /**
     * @return a string whose characters represent code points in a possibly two-byte font
     */
    @Nonnull
    public String getFontCodes() {
        return Optional.ofNullable(pdfText)
                .map(PdfString::toString)
                .orElse("");
    }

    /**
     * @see com.lowagie.text.pdf.parser.TextAssemblyBuffer#getFinalText(com.lowagie.text.pdf.PdfReader,
     * int, com.lowagie.text.pdf.parser.TextAssembler, boolean)
     */
    @Override
    public FinalText getFinalText(PdfReader reader, int page, TextAssembler assembler, boolean useMarkup) {
        throw new RuntimeException("Final text should never be called on unprocessed word fragment.");
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "[ParsedText: [" + getText() + "] " + getStartPoint() + ", " + getEndPoint() + "] lead" + "]";
    }

    /**
     * @see com.lowagie.text.pdf.parser.ParsedTextImpl#shouldNotSplit()
     */
    @Override
    public boolean shouldNotSplit() {
        return false;
    }

    /**
     * @return
     * @see com.lowagie.text.pdf.parser.ParsedTextImpl#breakBefore()
     */
    @Override
    public boolean breakBefore() {
        return false;
    }

}
