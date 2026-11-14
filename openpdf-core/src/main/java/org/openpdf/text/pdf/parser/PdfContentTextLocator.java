/*
  Copyright 2014 by Tizra Inc.
  The contents of this file are subject to the Mozilla Public License Version 1.1
  (the "License"); you may not use this file except in compliance with the License.
  You may obtain a copy of the License at http://www.mozilla.org/MPL/

  Software distributed under the License is distributed on an "AS IS" basis,
  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  for the specific language governing rights and limitations under the License.

  The Original Code is 'iText, a free JAVA-PDF library'.

  The Initial Developer of the Original Code is Bruno Lowagie. Portions created by
  the Initial Developer are Copyright (C) 1999-2008 by Bruno Lowagie.
  All Rights Reserved.
  Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
  are Copyright (C) 2000-2008 by Paulo Soares. All Rights Reserved.

  Contributor(s): all the names of the contributors are added in the source code
  where applicable.

  Alternatively, the contents of this file may be used under the terms of the
  LGPL license (the "GNU LIBRARY GENERAL PUBLIC LICENSE"), in which case the
  provisions of LGPL are applicable instead of those above.  If you wish to
  allow use of your version of this file only under the terms of the LGPL
  License and not to allow others to use your version of this file under
  the MPL, indicate your decision by deleting the provisions above and
  replace them with the notice and other provisions required by the LGPL.
  If you do not delete the provisions above, a recipient may use your version
  of this file under either the MPL or the GNU LIBRARY GENERAL PUBLIC LICENSE.

  This library is free software; you can redistribute it and/or modify it
  under the terms of the MPL as stated above or under the terms of the GNU
  Library General Public License as published by the Free Software Foundation;
  either version 2 of the License, or any later version.

  This library is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  FOR A PARTICULAR PURPOSE. See the GNU Library general Public License for more
  details.
 */
package org.openpdf.text.pdf.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author dgd
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class PdfContentTextLocator extends PdfContentStreamHandler {

    private final ArrayList<MatchedPattern> accumulator = new ArrayList<>();

    private final ArrayList<ParsedText> fragments = new ArrayList<>();

    private final int page;
    private Pattern p;
    private float[] coordinates;
    private final MatchingStrategy mode;

    private enum MatchingStrategy {
        PATTERN,
        BBOX,
    }

    /**
     * Construct a content PdfContetStreamHandler for regex-based text extraction pattern
     *
     * @param renderListener the text assembler
     * @param pattern        the pattern to match text against
     * @param page           PdfPage to inspect
     */
    public PdfContentTextLocator(TextAssembler renderListener, String pattern, int page) {
        super(renderListener);
        if (pattern == null) {
            throw new IllegalArgumentException("Pattern cannot be null");
        }
        //We check for length because we want to include whitespaces as possible patterns
        if (pattern.isEmpty()) {
            throw new IllegalArgumentException("Pattern sequence must be longer than 0");
        }
        this.p = Pattern.compile(pattern);
        this.page = page;
        this.mode = MatchingStrategy.PATTERN;
        installDefaultOperators();
        reset();
    }

    /**
     * Construct a content PdfContetStreamHandler for coordinates-based text extraction pattern
     *
     * @param renderListener the text assembler
     * @param coordinates    the bounding box to search text within
     * @param page           PdfPage to inspect
     */
    public PdfContentTextLocator(TextAssembler renderListener, float[] coordinates, int page) {
        super(renderListener);
        if (coordinates.length != 4) {
            throw new IllegalArgumentException("Coordinates bounding box must be an array of "
                    + "four floats, "
                    + "[x1, y1, x2, y2] {lower left point, upper right point}");
        }
        if (coordinates[2] < coordinates[0]) {
            throw new IllegalArgumentException("x2 {coordinates[2]} must be greater than or equal to x1 "
                    + "{coordinates[0]}");
        }
        if (coordinates[3] < coordinates[1]) {
            throw new IllegalArgumentException("y2 {coordinates[3]} must be greater than or equal to y1 "
                    + "{coordinates[1]}");
        }
        this.coordinates = coordinates;
        //We check for length because we want to include whitespaces as possible patterns
        this.page = page;
        this.mode = MatchingStrategy.BBOX;
        installDefaultOperators();
        reset();
    }

    /**
     * Loads all the supported graphics and text state operators in a map.
     */
    @Override
    protected void installDefaultOperators() {
        super.installDefaultOperators();
        registerContentOperator(this.new Do());
    }

    void pushContext(String newContextName) {
        contextNames.push(newContextName);
    }

    /**
     * Assemble partial words on the same line and execute the location strategy.
     *
     */
    void popContext() {
        contextNames.pop();
    }

    /**
     * Search for a pattern in a PdfString and if found, collect its bounding box
     *
     * @param decoded     the text to inspect
     * @param widths      cumulative list of end-widths of each character
     * @param fontFloor   lowest y-coordinate of the font
     * @param fontCeiling highest y-coordinate of the font
     */
    private void matchPdfString(String decoded, List<Float> widths, float fontFloor,
            float fontCeiling) {
        if (widths.size() <= 1) {
            return;
        }
        Matcher m = p.matcher(decoded);
        while (m.find()) {
            int beginning = m.start();
            int end = m.end();
            accumulator.add(new MatchedPattern(decoded, this.page, lineBox(widths, fontFloor, fontCeiling),
                    beginning, end, widths.get(beginning), widths.get(end)));
        }
    }

    /**
     * @param widths      cumulative list of end-widths of each character
     * @param fontFloor   lowest y-coordinate of the font
     * @param fontCeiling highest y-coordinate of the font
     * @return the bounding box of a whole line, as {llx, lly, urx, ury}
     */
    private static float[] lineBox(List<Float> widths, float fontFloor, float fontCeiling) {
        return new float[]{widths.getFirst(), fontFloor, widths.getLast(), fontCeiling};
    }

    /**
     * Extract text if it's coordinates intersect with the given bounding box
     *
     * @param decoded     the text to inspect
     * @param widths      cumulative list of end-widths of each character
     * @param fontFloor   lowest y-coordinate of the font
     * @param fontCeiling highest y-coordinate of the font
     */
    private void locatePdfString(String decoded, List<Float> widths, float fontFloor,
            float fontCeiling) {
        if (widths.size() <= 1 || isOutsideSearchBox(widths, fontFloor, fontCeiling)) {
            return;
        }
        int nearestLeft = nearestOffsetAtOrBefore(widths, this.coordinates[0]);
        int nearestRight = nearestOffsetAtOrBefore(widths, this.coordinates[2]);
        accumulator.add(new MatchedPattern(decoded, this.page, lineBox(widths, fontFloor, fontCeiling),
                nearestLeft, nearestRight, widths.get(nearestLeft), widths.get(nearestRight)));
    }

    /**
     * @param widths      cumulative list of end-widths of each character
     * @param fontFloor   lowest y-coordinate of the font
     * @param fontCeiling highest y-coordinate of the font
     * @return true if the line lies wholly outside the bounding box being searched
     */
    private boolean isOutsideSearchBox(List<Float> widths, float fontFloor, float fontCeiling) {
        float startWidth = widths.getFirst();
        float endWidth = widths.getLast();
        if (startWidth < this.coordinates[0] && endWidth < this.coordinates[0]) {
            return true;
        }
        if (startWidth > this.coordinates[2]) {
            return true;
        }
        if (fontFloor < this.coordinates[1] && fontCeiling < this.coordinates[1]) {
            return true;
        }
        return fontFloor > this.coordinates[3];
    }

    /**
     * @param offsets cumulative list of end-widths of each character, in ascending order
     * @param x       the x coordinate to look for
     * @return the index of the last offset at or before x, or 0 if x precedes them all
     */
    private static int nearestOffsetAtOrBefore(List<Float> offsets, float x) {
        int index = Collections.binarySearch(offsets, x);
        if (index < 0) {
            index = -(index + 1);
            if (index > 0) {
                index--;
            }
        }
        return index;
    }

    @Override
    public String getResultantText() {
        return "";
    }

    /**
     * @return list of text strips that matches
     */
    public List<MatchedPattern> getMatchedPatterns() {
        LineBuffer line = new LineBuffer();
        float currentY = -1000;
        for (TextAssemblyBuffer tbuff : textFragments) {
            if (!(tbuff instanceof ParsedText)) {
                continue;
            }
            ParsedText fragment = (ParsedText) tbuff;
            final float y = fragment.getStartPoint().get(1);
            if (y != currentY) {
                inspectLine(line);
                line.startAt(fragment, y);
            }
            line.append(fragment, y);
            currentY = y;
        }
        inspectLine(line);
        return this.accumulator;
    }

    /**
     * Runs the configured matching strategy over the line accumulated so far.
     *
     * @param line the line to inspect, which is left untouched
     */
    private void inspectLine(LineBuffer line) {
        if (!line.hasContent()) {
            return;
        }
        String inspected = line.text.toString();
        switch (this.mode) {
            case MatchingStrategy.PATTERN: {
                matchPdfString(inspected, line.offsets, line.fontFloor, line.fontCeiling);
                break;
            }
            case MatchingStrategy.BBOX: {
                locatePdfString(inspected, line.offsets, line.fontFloor, line.fontCeiling);
                break;
            }
            default: {
                //do nothing for now
            }
        }
    }

    /**
     * Accumulates the decoded text of a single line together with the x offset at which each of its characters
     * starts, so that a match found in the text can be mapped back onto the page.
     */
    private static final class LineBuffer {

        private final List<Float> offsets = new ArrayList<>();
        private StringBuilder text = new StringBuilder();
        private float totalWidth;
        private float fontFloor;
        private float fontCeiling;
        private ParsedText previous;

        /**
         * Discards whatever has been accumulated and begins a new line at the given fragment.
         *
         * @param fragment first text fragment of the new line
         * @param y        baseline of the new line
         */
        private void startAt(ParsedText fragment, float y) {
            offsets.clear();
            totalWidth = fragment.getStartPoint().get(0);
            offsets.add(totalWidth);
            fontFloor = y;
            fontCeiling = y;
            text = new StringBuilder();
            previous = null;
        }

        /**
         * Adds one text fragment to the current line.
         *
         * @param fragment the fragment to add
         * @param y        baseline of the line
         */
        private void append(ParsedText fragment, float y) {
            insertGapBefore(fragment);
            appendGlyphs(fragment);
            GraphicsState graphicsState = fragment.getGraphicState();
            fontFloor = Math.min(fontFloor, y + graphicsState.getFontDescentDescriptor());
            fontCeiling = Math.max(fontCeiling, y + graphicsState.getFontAscentDescriptor());
            previous = fragment;
        }

        /**
         * If the gap left by the previous fragment is at least as wide as a space, records a single space standing
         * in for it. The two fragments could be set in different sizes, so there is no exact number of spaces to be
         * had, and a matched pattern is not expected to tell one space from several.
         *
         * @param fragment the fragment about to be added
         */
        private void insertGapBefore(ParsedText fragment) {
            if (previous == null) {
                return;
            }
            float gap = previous.getEndPoint().subtract(fragment.getStartPoint()).get(0);
            float smallestSpace = Float.min(fragment.getSingleSpaceWidth(), previous.getSingleSpaceWidth());
            if (gap >= smallestSpace) {
                totalWidth += gap;
                offsets.add(totalWidth);
                text.append(' ');
            }
        }

        /**
         * Walks the character codes of a fragment rather than its decoded text: a PDF stores glyph widths against
         * codes, and measuring a decoded character instead would have to guess which code produced it. Text and
         * offsets are built together, so that offsets.get(i) stays the x position at which decoded character i
         * starts, which is the invariant the matching relies on.
         *
         * @param fragment the fragment to measure and decode
         */
        private void appendGlyphs(ParsedText fragment) {
            GraphicsState graphicsState = fragment.getGraphicState();
            for (char code : fragment.getCodePoints()) {
                String decodedCode = graphicsState.getFont().decode(code);
                float advance = ParsedText.advanceForCode(code, graphicsState);
                if (decodedCode == null || decodedCode.isEmpty()) {
                    // The code carries no text, but the pen still moves past its glyph.
                    totalWidth += advance;
                    continue;
                }
                // One code can decode to several characters, as a ligature does. There is no position to be had
                // inside a single glyph, so spread its advance evenly over the characters it produced.
                float advancePerChar = advance / decodedCode.length();
                for (int k = 0; k < decodedCode.length(); k++) {
                    text.append(decodedCode.charAt(k));
                    totalWidth += advancePerChar;
                    offsets.add(totalWidth);
                }
            }
        }

        /**
         * @return true if anything has been accumulated for the current line
         */
        private boolean hasContent() {
            return offsets.size() > 1;
        }
    }
}
