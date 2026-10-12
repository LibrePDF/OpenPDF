/*
  dgd: org.openpdf.text.pdf.parser
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
package org.openpdf.text.pdf.parser;

import org.openpdf.text.pdf.BaseFont;
import org.openpdf.text.pdf.CMapAwareDocumentFont;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.PdfString;
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
     * retain original PdfString as we need to distinguish between the code points contained there, and the standard
     * Java (Unicode strings) that actually represent the content of this text.
     */
    private PdfString pdfText = null;

    static protected ParsedText create(PdfString text, GraphicsState graphicsState, Matrix textMatrix) {
        PdfString codes = toCodePointString(text, graphicsState.getFont());
        float totalWidth = getStringWidth(codes.getOriginalChars(), graphicsState);
        return new ParsedText(codes, totalWidth, graphicsState, textMatrix);
    }

    /**
     * Retags a string taken from a content stream so that {@link PdfString#getOriginalChars()} yields one char per
     * character code. A two byte encoding packs each code into two chars of the raw string, so the codes cannot be
     * read off it directly.
     *
     * @param text the string as it appeared in the content stream
     * @param font the font in effect where the string was drawn
     * @return a string whose original chars are the character codes of the text
     */
    private static PdfString toCodePointString(PdfString text, CMapAwareDocumentFont font) {
        if (!BaseFont.IDENTITY_H.equals(font.getEncoding())) {
            return text;
        }
        if (!font.hasUnicodeCMAP()) {
            return new PdfString(new String(text.getBytes(), StandardCharsets.UTF_16));
        }
        return new PdfString(text.toString(), font.hasTwoByteUnicodeCMAP() ? "IDENTITY_H2" : "IDENTITY_H1");
    }

    /**
     * Gets the width of a run of character codes in text space units
     *
     * @param codes         the character codes that need measuring
     * @param graphicsState graphic state including current transformation to page coordinates from text measurement
     * @return the width of the codes in text space units
     */
    private static float getStringWidth(char[] codes, GraphicsState graphicsState) {
        float totalWidth = 0;
        for (char code : codes) {
            totalWidth += advanceForCode(code, graphicsState);
        }
        return totalWidth;
    }

    /**
     * Measures how far the pen moves when the glyph for a single character code is drawn. Widths are looked up by code
     * rather than by Unicode character, because that is how a PDF stores them.
     *
     * @param code          a character code in the encoding of the current font
     * @param graphicsState graphic state including current transformation to page coordinates from text measurement
     * @return the advance of the code in text space units
     */
    static float advanceForCode(char code, GraphicsState graphicsState) {
        float w = graphicsState.getFont().getWidthOfCode(code) / 1000.0f;
        return Character.isSpaceChar(code)
                ? graphicsState.calculateCharacterWidthWithSpace(w)
                : graphicsState.calculateCharacterWidthWithoutSpace(w);
    }

    /**
     * This constructor should only be called when the origin for text display is at (0,0) and the graphical state
     * reflects all transformations of the baseline. This is in text space units.
     *
     * @param text          string
     * @param graphicsState graphical state
     * @param textMatrix    transform from text space to graphics (drawing space)
     */
    private ParsedText(PdfString text, float unscaledWidth, GraphicsState graphicsState,
            Matrix textMatrix) {
        this(text, unscaledWidth, new GraphicsState(graphicsState),
                textMatrix.multiply(graphicsState.getCtm()),
                getUnscaledFontSpaceWidth(graphicsState));
    }

    /**
     * Internal constructor for a parsed text item. The constructors that call it gather some information from the
     * graphical state first.
     *
     * @param text               This is a PdfString containing code points for the current font, not actually
     *                           characters. If the font has multiByte glyphs, (Identity-H encoding) we reparse the
     *                           string so that the code points don't get split into multiple characters.
     * @param graphicsState      graphical state
     * @param textMatrix         transform from text space to graphics (drawing space)
     * @param unscaledSpaceWidth width of the space character in the font.
     */
    private ParsedText(PdfString text, float unscaledWidth, GraphicsState graphicsState,
            Matrix textMatrix,
            float unscaledSpaceWidth) {
        super(null, pointToUserSpace(0, 0, textMatrix),
                pointToUserSpace(unscaledWidth, 0f, textMatrix),
                pointToUserSpace(1.0f, 0f, textMatrix),
                convertHeightToUser(graphicsState.getFontAscentDescriptor(), textMatrix),
                convertHeightToUser(graphicsState.getFontDescentDescriptor(), textMatrix),
                convertWidthToUser(unscaledSpaceWidth, textMatrix));
        pdfText = text;
        textToUserSpaceTransformMatrix = textMatrix;
        this.graphicsState = graphicsState;
    }


    /**
     * @param xOffset                        offset in x direction
     * @param yOffset                        offset in y direction
     * @param textToUserSpaceTransformMatrix transform from text space to graphics (drawing space)
     * @return the cross product of the offset and the textToUserSpaceTransformMatrix
     */
    private static Vector pointToUserSpace(float xOffset, float yOffset,
            Matrix textToUserSpaceTransformMatrix) {
        return new Vector(xOffset, yOffset, 1f)
                .cross(textToUserSpaceTransformMatrix);
    }

    /**
     * Calculates the width of a space character. If the font does not define a width for a standard space character ,
     * we also attempt to use the width of \u00A0 (a non-breaking space in many fonts)
     *
     * @param graphicsState graphic state including current transformation to page coordinates from text measurement
     * @return the width of a single space character in text space units
     */
    private static float getUnscaledFontSpaceWidth(GraphicsState graphicsState) {
        // Keyed by Unicode rather than by code, because we are after the font's space glyph and have no code for it.
        int width = graphicsState.getFont().getWidth(' ');
        if (width == 0) {
            width = graphicsState.getFont().getWidth('\u00A0');
        }
        return graphicsState.calculateCharacterWidthWithSpace(width / 1000.0f);
    }

    /**
     * @param width                          which should be converted to user space
     * @param textToUserSpaceTransformMatrix transform from text space to graphics (drawing space)
     * @return distance between start and end position
     */
    private static float convertWidthToUser(float width,
            Matrix textToUserSpaceTransformMatrix) {
        Vector startPos = pointToUserSpace(0, 0, textToUserSpaceTransformMatrix);
        Vector endPos = pointToUserSpace(width, 0,
                textToUserSpaceTransformMatrix);
        return distance(startPos, endPos);
    }

    /**
     * @param startPos of the vector
     * @param endPos   of the vector
     * @return (endPos - startPos).length
     */
    private static float distance(Vector startPos, Vector endPos) {
        return endPos.subtract(startPos).length();
    }

    /**
     * @param height                         which should be converted to user space
     * @param textToUserSpaceTransformMatrix transform from text space to graphics (drawing space)
     * @return distance between start and end position
     */
    private static float convertHeightToUser(float height,
            Matrix textToUserSpaceTransformMatrix) {
        Vector startPos = pointToUserSpace(0, 0, textToUserSpaceTransformMatrix);
        Vector endPos = pointToUserSpace(0, height,
                textToUserSpaceTransformMatrix);
        return distance(endPos, startPos);
    }

    /**
     * This constructor should only be called when the origin for text display is at (0,0) and the graphical state
     * reflects all transformations of the baseline. This is in text space units.
     * <p>
     * Decodes a PdfString (which will contain glyph ids encoded in the font's encoding) based on the active font, and
     * determine the unicode equivalent
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
     * Break this string if there are spaces within it. If so, we mark the new Words appropriately for later assembly.
     * <p>
     * We are guaranteed that every space (internal word break) in this parsed text object will create a new word in the
     * result of this method. We are not guaranteed that these Word objects are actually words until they have been
     * assembled.
     * <p>
     * The word following any space preserves that space in its string value, so that the assembler will not erroneously
     * merge words that should be separate, regardless of the spacing.
     *
     * @return list of Word objects.
     */
    public List<Word> getAsPartialWords() {
        List<Word> result = new ArrayList<>();
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
            float advance = advanceForCode(c, graphicsState);
            if (hasSpace[i]) {
                if (wordAccum.length() > 0) {
                    result.add(createWord(wordAccum, wordStartOffset, totalWidth, getBaseline(),
                            wordsAreComplete, currentBreakBefore));
                    wordAccum = new StringBuffer();
                }
                if (!Character.isWhitespace(c)) {
                    wordStartOffset = totalWidth;
                }
                totalWidth += advance;
                if (Character.isWhitespace(c)) {
                    wordStartOffset = totalWidth;
                }
                wordAccum.append(c);
                currentBreakBefore = true; // next word will be marked as result of a space-character break
            } else {
                wordAccum.append(c);
                totalWidth += advance;
            }
        }
        if (wordAccum.length() > 0) {
            result.add(createWord(wordAccum, wordStartOffset, totalWidth, getBaseline(),
                    wordsAreComplete, currentBreakBefore));
        }
        return result;
    }

    /**
     * Calculate whether individual character positions (after font decoding from code to a character), contain spaces
     * and break words, and whether the resulting words should be treated as complete (i.e. if any spaces were found.
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

            if (charValue != null) {
                for (char cFinal : charValue.toCharArray()) {
                    if (Character.isSpaceChar(cFinal)) {
                        wordsAreComplete = true;
                        hasSpace[i] = true;
                    }
                }
            }
        }
        return wordsAreComplete;
    }

    /**
     * Create a word to represent a broken substring at a space. As spaces have zero "word length" make sure that they
     * also have a baseline to check
     *
     * @param wordAccum          buffer of characters
     * @param wordStartOffset    intial x-offset
     * @param wordEndOffset      ending x offset.
     * @param baseline           baseline of this word, so direction of progress can be measured in line ending
     *                           determination.
     * @param wordsAreComplete   true means characters in this word won't be split apart graphically
     * @param currentBreakBefore true if this word fragment represents a word boundary, and any preceding fragment is
     *                           complete.
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
     * {@inheritDoc}
     *
     * @see org.openpdf.text.pdf.parser.TextAssemblyBuffer#accumulate(org.openpdf.text.pdf.parser.TextAssembler, String)
     */
    @Override
    public void accumulate(TextAssembler textAssembler, String contextName) {
        textAssembler.process(this, contextName);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.openpdf.text.pdf.parser.TextAssemblyBuffer#assemble(org.openpdf.text.pdf.parser.TextAssembler)
     */
    @Override
    public void assemble(TextAssembler textAssembler) {
        textAssembler.renderText(this);
    }

    /**
     * when returning the text from this item, we need to decode the code points we have.
     *
     * @see org.openpdf.text.pdf.parser.ParsedTextImpl#getText()
     */
    @Override
    public String getText() {
        String text = super.getText();
        if (text == null && pdfText != null) {
            return decode(pdfText);
        }
        return text;
    }

    /**
     * @return the character codes of this text, one per element, with the codes of a two byte encoding already
     * assembled from their byte pairs
     */
    public char[] getCodePoints() {
        return pdfText == null ? new char[0] : pdfText.getOriginalChars();
    }

    /**
     * @return a string whose characters represent code points in a possibly two-byte font
     */
    public String getFontCodes() {
        return Optional.ofNullable(pdfText)
                .map(PdfString::toString)
                .orElse("");
    }

    /**
     * @param gs graphic state including current transformation to page coordinates from text measurement
     * @return the unscaled (i.e. in Text space) width of our text
     */
    public float getUnscaledTextWidth(GraphicsState gs) {
        return getStringWidth(pdfText == null ? new char[0] : pdfText.getOriginalChars(), gs);
    }

    /**
     * @see org.openpdf.text.pdf.parser.TextAssemblyBuffer#getFinalText(org.openpdf.text.pdf.PdfReader, int,
     * org.openpdf.text.pdf.parser.TextAssembler, boolean)
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
     * @see org.openpdf.text.pdf.parser.ParsedTextImpl#shouldNotSplit()
     */
    @Override
    public boolean shouldNotSplit() {
        return false;
    }

    /**
     * @return a boolean value
     * @see org.openpdf.text.pdf.parser.ParsedTextImpl#breakBefore()
     */
    @Override
    public boolean breakBefore() {
        return false;
    }

    protected GraphicsState getGraphicState() {
        return this.graphicsState;
    }

}
