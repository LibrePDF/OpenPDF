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

import org.openpdf.text.ExceptionConverter;
import org.openpdf.text.error_messages.MessageLocalization;
import org.openpdf.text.pdf.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author dgd
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class PdfContentTextLocator extends PdfContentStreamHandler {

    private final ArrayList<MatchedPattern> accumulator = new ArrayList<>();

    private final ArrayList<ParsedText> fragments = new ArrayList<>();
    private final ArrayList<Float> fragmentsWidths = new ArrayList<>();

    private final int page;
    private final Pattern p;


    public PdfContentTextLocator(TextAssembler renderListener, String pattern, int page) {
        super(renderListener);
        if(pattern == null) throw new IllegalArgumentException("Pattern cannot be null");
        //We check for length because we want to include whitespaces as possible patterns
        if(pattern.isEmpty()) throw new IllegalArgumentException("Pattern sequence must be longer than 0");
        this.p = Pattern.compile(pattern);
        this.page = page;
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
        renderListener.reset();
    }

    void pushContext(String newContextName) {
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
     * Search for a pattern in a PdfString
     * and if found, collect its bounding box
     *
     * @param string the text to inspect
     */
    void displayPdfString(PdfString string) {

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

        float pdfStringWidth = startWidth + totalWidth;
        float y = new Vector(0, 0, 1f).cross(textMatrix).get(1);
        float y1 =  y + graphicsState().getFontDescentDescriptor();
        float y2 =  y + graphicsState().getFontAscentDescriptor();

        Matcher m = p.matcher(decoded);
        while (m.find()) {
            float x1 = widths[m.start()];
            float x2 = widths[m.end()];
            MatchedPattern mp = new MatchedPattern(decoded, this.page, x1, y1, x2, y2);
            accumulator.add(mp);
        }

        textMatrix = new Matrix(totalWidth, 0).multiply(textMatrix);
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
    public ArrayList<MatchedPattern> getMatchedPatterns() {
        return this.accumulator;
    }
}
