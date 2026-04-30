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

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openpdf.text.pdf.BaseFont;
import org.openpdf.text.pdf.PdfString;

/**
 * @author dgd
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class PdfContentTextLocator extends PdfContentStreamHandler {

    private final ArrayList<MatchedPattern> accumulator = new ArrayList<>();

    private final ArrayList<ParsedText> fragments = new ArrayList<>();
    private final ArrayList<Float> fragmentsWidths = new ArrayList<>();

    private final int page;
    private Pattern p;
    private float[] coordinates;
    private final int mode;

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
        this.mode = 1;
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
        this.mode = 2;
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

    void popContext() {
        contextNames.pop();
        renderListener.reset();
    }

    void pushContext(String newContextName) {
        contextNames.push(newContextName);
    }

    public void reset() {
        if (gsStack == null || gsStack.isEmpty()) {
            gsStack = new Stack<>();
        }
        gsStack.add(new GraphicsState());
        textMatrix = null;
        textLineMatrix = null;
    }

    /**
     * Extract a PdfString content and coordinates based on the handler extraction pattern: either matches a given regex
     * or intersects a given bounding box
     *
     * @param string the text to inspect
     */
    void displayPdfString(PdfString string) {
        if (contextNames.peek() == null) {
            return;
        }
        String decoded;
        byte[] bytes;
        if (BaseFont.IDENTITY_H.equals(graphicsState().getFont().getEncoding())) {
            bytes = string.toString().getBytes(StandardCharsets.UTF_16);
        } else {
            bytes = string.toString().getBytes();
        }
        decoded = graphicsState().getFont().decode(bytes, 0, bytes.length);
        char[] chars = decoded.toCharArray();
        final float[] widths = new float[chars.length + 1];
        Vector startPoint = new Vector(0, 0, 1f).cross(textMatrix);
        float startWidth = startPoint.get(0);
        float totalWidth = 0;
        widths[0] = startWidth;
        int counter = 1;
        for (char c : chars) {
            float w = graphicsState().getFont().getWidth(c) / 1000.0f;
            float wordSpacing = Character.isSpaceChar(c) ? graphicsState().getWordSpacing() : 0f;
            float blockWidth = (w * graphicsState().getFontSize() + graphicsState().getCharacterSpacing() + wordSpacing)
                    * graphicsState().getHorizontalScaling();
            totalWidth += blockWidth;
            widths[counter] = startWidth + totalWidth;
            counter++;
        }

        float y = new Vector(0, 0, 1f).cross(textMatrix).get(1);
        float fontFloor = y + graphicsState().getFontDescentDescriptor();
        float fontCeiling = y + graphicsState().getFontAscentDescriptor();

        switch (this.mode) {
            case 1: {
                matchPdfString(decoded, widths, totalWidth, fontFloor, fontCeiling);
                break;
            }
            case 2: {
                locatePdfString(decoded, startWidth, totalWidth, fontFloor, fontCeiling);
                break;
            }
            default: {
                //do nothing for now
            }
        }
    }

    /**
     * Search for a pattern in a PdfString and if found, collect its bounding box
     *
     * @param decoded     the text to inspect
     * @param widths      array of prefix widths of each char
     * @param totalWidth  width of the text
     * @param fontFloor   lowest y-coordinate of the font
     * @param fontCeiling highest y-coordinate of the font
     */
    private void matchPdfString(String decoded, float[] widths, float totalWidth, float fontFloor, float fontCeiling) {
        Matcher m = p.matcher(decoded);
        while (m.find()) {
            float x1 = widths[m.start()];
            float x2 = widths[m.end()];
            MatchedPattern mp = new MatchedPattern(decoded, this.page, x1, fontFloor, x2, fontCeiling);
            accumulator.add(mp);
        }
        textMatrix = new Matrix(totalWidth, 0).multiply(textMatrix);
    }

    /**
     * Extract text if it's coordinates intersect with the given bounding box
     *
     * @param decoded     the text to inspect
     * @param startWidth  left-most x-coordinate of the text
     * @param totalWidth  width of the text
     * @param fontFloor   lowest y-coordinate of the font
     * @param fontCeiling highest y-coordinate of the font
     */
    private void locatePdfString(String decoded, float startWidth, float totalWidth, float fontFloor,
            float fontCeiling) {
        float endWidth = startWidth + totalWidth;
        textMatrix = new Matrix(totalWidth, 0).multiply(textMatrix);
        if (startWidth < this.coordinates[0] && endWidth < this.coordinates[0]) {
            return;
        }
        if (startWidth > this.coordinates[2]) {
            return;
        }
        if (fontFloor < this.coordinates[1] && fontCeiling < this.coordinates[1]) {
            return;
        }
        if (fontFloor > this.coordinates[3]) {
            return;
        }
        MatchedPattern mp = new MatchedPattern(decoded, this.page, startWidth, fontFloor, endWidth, fontCeiling);
        accumulator.add(mp);
    }

    private float convertHeightToUser(float height) {
        Vector endPos = new Vector(0, height, 1f).cross(textMatrix);
        return endPos.get(1);
    }

    @Override
    public String getResultantText() {
        return "";
    }

    /**
     * @return list of text strips that matches
     */
    public List<MatchedPattern> getMatchedPatterns() {
        return this.accumulator;
    }
}
