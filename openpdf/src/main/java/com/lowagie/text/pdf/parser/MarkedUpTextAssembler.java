/**
 * Copyright 2014 by Tizra Inc.
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 * <p>
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the License.
 * <p>
 * The Original Code is 'iText, a free JAVA-PDF library'.
 * <p>
 * The Initial Developer of the Original Code is Bruno Lowagie. Portions created by
 * the Initial Developer are Copyright (C) 1999-2008 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000-2008 by Paulo Soares. All Rights Reserved.
 * <p>
 * Contributor(s): all the names of the contributors are added in the source code
 * where applicable.
 * <p>
 * Alternatively, the contents of this file may be used under the terms of the
 * LGPL license (the "GNU LIBRARY GENERAL PUBLIC LICENSE"), in which case the
 * provisions of LGPL are applicable instead of those above.  If you wish to
 * allow use of your version of this file only under the terms of the LGPL
 * License and not to allow others to use your version of this file under
 * the MPL, indicate your decision by deleting the provisions above and
 * replace them with the notice and other provisions required by the LGPL.
 * If you do not delete the provisions above, a recipient may use your version
 * of this file under either the MPL or the GNU LIBRARY GENERAL PUBLIC LICENSE.
 * <p>
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the MPL as stated above or under the terms of the GNU
 * Library General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or any later version.
 * <p>
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library general Public License for more
 * details.
 */
package com.lowagie.text.pdf.parser;

import com.lowagie.text.pdf.PdfReader;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

/**
 * We'll get called on a variety of marked section content (perhaps including
 * the results of nested sections), and will assemble it into an order as we
 * can.
 *
 * @author dgd
 *
 */
public class MarkedUpTextAssembler implements TextAssembler {
    /**
     * our result may be partially processed already, in which case we'll just
     * add things to it, once ready.
     */
    List<FinalText> result = new ArrayList<>();
    private PdfReader reader;
    @Nullable
    private ParsedTextImpl inProgress = null;
    private int page;
    private int wordIdCounter = 1;
    private boolean usePdfMarkupElements = false;
    /**
     * as we get new content (final or not), we accumulate it until we reach the
     * end of a parsing unit
     *
     * Each parsing unit may have a tag name that should wrap its content
     */
    private List<TextAssemblyBuffer> partialWords = new ArrayList<>();

    MarkedUpTextAssembler(PdfReader reader) {
        this.reader = reader;
    }

    MarkedUpTextAssembler(PdfReader reader, boolean usePdfMarkupElements) {
        this.reader = reader;
        this.usePdfMarkupElements = usePdfMarkupElements;
    }

    /**
     * Remember an unassembled chunk until we hit the end of this element, or we
     * hit an assembled chunk, and need to pull things together.
     *
     * @param unassembled
     *            chunk of text rendering instruction to contribute to final
     *            text
     */
    @Override
    public void process(ParsedText unassembled, String contextName) {
        partialWords.addAll(unassembled.getAsPartialWords());
    }

    /**
     * Slot fully-assembled chunk into our result at the current location. If
     * there are unassembled chunks waiting, assemble them first.
     *
     * @param completed
     *            This is a chunk from a nested element
     */
    @Override
    public void process(FinalText completed, String contextName) {
        clearAccumulator();
        result.add(completed);

    }

    /**
     * {@inheritDoc}
     * @see com.lowagie.text.pdf.parser.TextAssembler#process(com.lowagie.text.pdf.parser.Word,
     *      String)
     */
    @Override
    public void process(Word completed, String contextName) {
        partialWords.add(completed);
    }

    /**
     *
     */
    private void clearAccumulator() {
        for (TextAssemblyBuffer partialWord : partialWords) {
            // Visit each partialWord, calling renderText 
            partialWord.assemble(this);
        }
        partialWords.clear();
        if (inProgress != null) {
            result.add(inProgress.getFinalText(reader, page, this, usePdfMarkupElements));
            inProgress = null;
        }
    }

    private FinalText concatenateResult(@Nullable String containingElementName) {
        // null element means that this is a formatting artifact, not content.
        if (containingElementName == null) {
            // at some point, we may want to extract alternate text for some
            // artifacts.
            return null;
        }
        StringBuilder res = new StringBuilder();
        if (usePdfMarkupElements && !containingElementName.isEmpty()) {
            res.append('<').append(containingElementName).append('>');
        }
        for (FinalText item : result) {
            res.append(item.getText());
        }
        // important, as the stuff buffered in the result is now used up!
        result.clear();
        if (usePdfMarkupElements && !containingElementName.isEmpty()) {
            res.append("</");
            int spacePos = containingElementName.indexOf(' ');
            if (spacePos >= 0) {
                containingElementName = containingElementName.substring(0, spacePos);
            }
            res.append(containingElementName).append('>');
        }
        return new FinalText(res.toString());
    }

    /**
     * {@inheritDoc}
     * @see com.lowagie.text.pdf.parser.TextAssembler#endParsingContext(String)
     */
    @Override
    public FinalText endParsingContext(@Nullable String containingElementName) {
        clearAccumulator();
        return concatenateResult(containingElementName);
    }

    /**
     *
     * @see com.lowagie.text.pdf.parser.TextAssembler#reset()
     */
    @Override
    public void reset() {
        result.clear();
        partialWords.clear();
        inProgress = null;
    }

    @Override
    public void renderText(FinalText finalText) {
        result.add(finalText);
    }

    /**
     * Captures text using a simplified algorithm for inserting hard returns and
     * spaces
     *
     * @see com.lowagie.text.pdf.parser.AbstractRenderListener#renderText(java.lang.String,
     *      com.lowagie.text.pdf.parser.GraphicsState,
     *      com.lowagie.text.pdf.parser.Matrix,
     *      com.lowagie.text.pdf.parser.Matrix)
     */
    @Override
    public void renderText(ParsedTextImpl partialWord) {
        boolean firstRender = inProgress == null;
        boolean hardReturn = false;
        if (firstRender) {
            inProgress = partialWord;
            return;
        }
        Vector start = partialWord.getStartPoint();
        Vector lastStart = inProgress.getStartPoint();
        Vector lastEnd = inProgress.getEndPoint();

        // see
        // http://mathworld.wolfram.com/Point-LineDistance2-Dimensional.html
        float dist = inProgress.getBaseline()
                .subtract(lastStart)
                .cross(lastStart.subtract(start))
                .lengthSquared() / inProgress.getBaseline().subtract(lastStart).lengthSquared();

        float sameLineThreshold = partialWord.getAscent() * 0.5f;
        // let's try using 25% of current leading for vertical slop.
        if (dist > sameLineThreshold || Float.isNaN(dist)) {
            hardReturn = true;
        }
        /*
         * Note: Technically, we should check both the start and end positions,
         * in case the angle of the text changed without any displacement but
         * this sort of thing probably doesn't happen much in reality, so we'll
         * leave it alone for now
         */
        float spacing = lastEnd.subtract(start).length();
        if (hardReturn || partialWord.breakBefore()) {
            result.add(inProgress.getFinalText(reader, page, this, usePdfMarkupElements));
            if (hardReturn) {
                result.add(new FinalText("\n"));
                if (usePdfMarkupElements) {
                    result.add(new FinalText("<br class='t-pdf' />"));
                }
            }
            inProgress = partialWord;
            // System.out.println("<< Hard Return >>");
        } else if (spacing < partialWord.getSingleSpaceWidth() / 2.3 || inProgress.shouldNotSplit()) {
            inProgress = new Word(inProgress.getText() + partialWord.getText().trim(),
                    partialWord.getAscent(),
                    partialWord.getDescent(),
                    lastStart,
                    partialWord.getEndPoint(),
                    inProgress.getBaseline(),
                    partialWord.getSingleSpaceWidth(),
                    inProgress.shouldNotSplit(),
                    inProgress.breakBefore());
        } else {
            result.add(inProgress.getFinalText(reader, page, this, usePdfMarkupElements));
            inProgress = partialWord;
        }
    }

    /**
     * Getter.
     *
     * @see SimpleTextExtractingPdfContentRenderListener#_reader
     * @return reader
     */
    protected PdfReader getReader() {
        return reader;
    }

    /**
     * {@inheritDoc}
     * @see com.lowagie.text.pdf.parser.TextAssembler#setPage(int)
     */
    @Override
    public void setPage(int page) {
        this.page = page;
    }

    /**
     * {@inheritDoc}
     * @see com.lowagie.text.pdf.parser.TextAssembler#getWordId()
     */
    @Override
    public String getWordId() {
        return "word" + wordIdCounter++;
    }

}
