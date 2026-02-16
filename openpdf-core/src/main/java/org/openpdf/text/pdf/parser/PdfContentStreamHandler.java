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
import org.openpdf.text.pdf.CMapAwareDocumentFont;
import org.openpdf.text.pdf.PdfArray;
import org.openpdf.text.pdf.PdfContentParser;
import org.openpdf.text.pdf.PdfDictionary;
import org.openpdf.text.pdf.PdfIndirectReference;
import org.openpdf.text.pdf.PdfLiteral;
import org.openpdf.text.pdf.PdfName;
import org.openpdf.text.pdf.PdfNumber;
import org.openpdf.text.pdf.PdfObject;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.PdfStream;
import org.openpdf.text.pdf.PdfString;
import org.openpdf.text.pdf.PRIndirectReference;
import org.openpdf.text.pdf.PRStream;
import org.openpdf.text.pdf.PRTokeniser;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;

/**
 * @author dgd
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class PdfContentStreamHandler {

    protected final Stack<List<TextAssemblyBuffer>> textFragmentStreams = new Stack<>();
    protected final Stack<String> contextNames = new Stack<>();
    /**
     * detail parser for text within a marked section. used by TextAssembler
     */
    protected final TextAssembler renderListener;
    /**
     * A map with all supported operators operators (PDF syntax).
     * Protected to allow subclasses to override installDefaultOperators() and register additional operators.
     */
    protected Map<String, ContentOperator> operators;
    /**
     * Stack keeping track of the graphics state.
     */
    protected Stack<GraphicsState> gsStack;
    /**
     * Text matrix.
     */
    protected Matrix textMatrix;
    /**
     * Text line matrix.
     */
    protected Matrix textLineMatrix;
    protected List<TextAssemblyBuffer> textFragments = new ArrayList<>();


    public PdfContentStreamHandler(TextAssembler renderListener) {
        this.renderListener = renderListener;
    }

    private static Matrix getMatrix(List<PdfObject> operands) {
        float a = ((PdfNumber) operands.get(0)).floatValue();
        float b = ((PdfNumber) operands.get(1)).floatValue();
        float c = ((PdfNumber) operands.get(2)).floatValue();
        float d = ((PdfNumber) operands.get(3)).floatValue();
        float e = ((PdfNumber) operands.get(4)).floatValue();
        float f = ((PdfNumber) operands.get(5)).floatValue();
        return new Matrix(a, b, c, d, e, f);
    }

    /**
     * Registers a content operator that will be called when the specified operator string is encountered during content
     * processing. Each operator may be registered only once (it is not legal to have multiple operators with the same
     * operatorString)
     *
     * @param operator the operator that will receive notification when the operator is encountered
     * @since 2.1.7
     */
    public void registerContentOperator(ContentOperator operator) {
        String operatorString = operator.getOperatorName();
        if (operators.containsKey(operatorString)) {
            throw new IllegalArgumentException(MessageLocalization.getComposedMessage(
                    "operator.1.already.registered", operatorString));
        }
        operators.put(operatorString, operator);
    }

    /**
     * Loads all the supported graphics and text state operators in a map.
     * Subclasses can override this method to register additional operators.
     * When overriding, subclasses should call super.installDefaultOperators() first.
     */
    protected void installDefaultOperators() {
        operators = new HashMap<>();

        registerContentOperator(new PushGraphicsState());
        registerContentOperator(new PopGraphicsState());
        registerContentOperator(new ModifyCurrentTransformationMatrix());
        registerContentOperator(new ProcessGraphicsStateResource());

        SetTextCharacterSpacing tcOperator = new SetTextCharacterSpacing();
        registerContentOperator(tcOperator);
        SetTextWordSpacing twOperator = new SetTextWordSpacing();
        registerContentOperator(twOperator);
        registerContentOperator(new SetTextHorizontalScaling());
        SetTextLeading tlOperator = new SetTextLeading();
        registerContentOperator(tlOperator);
        registerContentOperator(new SetTextFont());
        registerContentOperator(new SetTextRenderMode());
        registerContentOperator(new SetTextRise());

        registerContentOperator(new BeginText());
        registerContentOperator(new EndText());

        TextMoveStartNextLine tdOperator = new TextMoveStartNextLine();
        registerContentOperator(tdOperator);
        registerContentOperator(new TextMoveStartNextLineWithLeading(tdOperator, tlOperator));
        registerContentOperator(new TextSetTextMatrix());
        TextMoveNextLine tstarOperator =
                new TextMoveNextLine(tdOperator);
        registerContentOperator(tstarOperator);

        ShowText tjOperator = new ShowText();
        registerContentOperator(new ShowText());
        MoveNextLineAndShowText tickOperator =
                new MoveNextLineAndShowText(tstarOperator, tjOperator);
        registerContentOperator(tickOperator);
        registerContentOperator(
                new MoveNextLineAndShowTextWithSpacing(twOperator, tcOperator, tickOperator));
        registerContentOperator(new ShowTextArray());

        registerContentOperator(new BeginMarked());
        registerContentOperator(new BeginMarkedDict());
        registerContentOperator(new EndMarked());
    }

    /**
     * Get the operator to process a command with a given name
     *
     * @param operatorName name of the operator that we might need to call
     * @return the operator or null if none present
     */
    protected Optional<ContentOperator> lookupOperator(String operatorName) {
        return Optional.ofNullable(operators.get(operatorName));
    }

    /**
     * Invokes an operator.
     *
     * @param operator  the PDF Syntax of the operator
     * @param operands  a list with operands
     * @param resources Pdf Resources found in the file containing the stream.
     */
    public void invokeOperator(PdfLiteral operator, List<PdfObject> operands, PdfDictionary resources) {
        String operatorName = operator.toString();
        lookupOperator(operatorName)
                .ifPresent(contentOperator -> contentOperator.invoke(operands, this, resources));
    }

    abstract void popContext();

    abstract void pushContext(String newContextName);

    /**
     * Returns the current graphics state.
     *
     * @return the graphics state
     */
    GraphicsState graphicsState() {
        return gsStack.peek();
    }

    public abstract void reset();

    /**
     * Returns the current text matrix.
     *
     * @return the text matrix
     * @since 2.1.5
     */
    protected Matrix getCurrentTextMatrix() {
        return textMatrix;
    }

    /**
     * Returns the current line matrix.
     *
     * @return the line matrix
     * @since 2.1.5
     */
    protected Matrix getCurrentTextLineMatrix() {
        return textLineMatrix;
    }

    /**
     * Adjusts the text matrix for the specified adjustment value (see TJ operator in the PDF spec for information)
     *
     * @param tj the text adjustment
     */
    void applyTextAdjust(float tj) {
        float adjustBy = -tj / 1000f * graphicsState().getFontSize() * graphicsState().getHorizontalScaling();
        textMatrix = new Matrix(adjustBy, 0).multiply(textMatrix);
    }

    /**
     * @return current font in processing state
     */
    public CMapAwareDocumentFont getCurrentFont() {
        return graphicsState().getFont();
    }

    /**
     * Displays text.
     *
     * @param string the text to display
     */
    abstract void displayPdfString(PdfString string);

    /**
     * @return result text
     */
    public abstract String getResultantText();

    /**
     * A content operator implementation (TJ).
     */
    static class ShowTextArray implements ContentOperator {

        /**
         * @see ContentOperator#getOperatorName()
         */
        @Override
        public String getOperatorName() {
            return "TJ";
        }

        @Override
        public void invoke(List<PdfObject> operands, PdfContentStreamHandler handler, PdfDictionary resources) {
            PdfArray array = (PdfArray) operands.get(0);
            for (PdfObject entryObj : array.getElements()) {
                if (entryObj instanceof PdfString) {
                    handler.displayPdfString((PdfString) entryObj);
                } else {
                    float tj = ((PdfNumber) entryObj).floatValue();
                    handler.applyTextAdjust(tj);
                }
            }

        }
    }

    /**
     * A content operator implementation (BT).
     */
    static class BeginText implements ContentOperator {

        /**
         * @see ContentOperator#getOperatorName()
         */
        @Override
        public String getOperatorName() {
            return "BT";
        }

        @Override
        public void invoke(List<PdfObject> operands, PdfContentStreamHandler handler, PdfDictionary resources) {
            handler.textMatrix = new Matrix();
            handler.textLineMatrix = handler.textMatrix;
        }
    }

    /**
     * A content operator implementation (ET).
     */
    static class EndText implements ContentOperator {

        /**
         * @see ContentOperator#getOperatorName()
         */
        @Override
        public String getOperatorName() {
            return "ET";
        }

        @Override
        public void invoke(List<PdfObject> operands, PdfContentStreamHandler handler, PdfDictionary resources) {
            handler.textMatrix = null;
            handler.textLineMatrix = null;
        }
    }

    /**
     * A content operator implementation (cm).
     */
    static class ModifyCurrentTransformationMatrix implements ContentOperator {

        /**
         * @see ContentOperator#getOperatorName()
         */
        @Override
        public String getOperatorName() {
            return "cm";
        }

        @Override
        public void invoke(List<PdfObject> operands, PdfContentStreamHandler handler, PdfDictionary resources) {
            Matrix matrix = getMatrix(operands);
            GraphicsState graphicsState = handler.gsStack.peek();
            graphicsState.multiplyCtm(matrix);
        }
    }

    /**
     * A content operator implementation (').
     */
    static class MoveNextLineAndShowText implements ContentOperator {

        private final TextMoveNextLine textMoveNextLine;
        private final ShowText showText;

        public MoveNextLineAndShowText(
                TextMoveNextLine textMoveNextLine,
                ShowText showText) {
            this.textMoveNextLine = textMoveNextLine;
            this.showText = showText;
        }

        /**
         * @see ContentOperator#getOperatorName()
         */
        @Override
        public String getOperatorName() {
            return "'";
        }

        @Override
        public void invoke(List<PdfObject> operands, PdfContentStreamHandler handler, PdfDictionary resources) {
            textMoveNextLine.invoke(new ArrayList<>(0), handler, resources);
            showText.invoke(operands, handler, resources);
        }
    }

    /**
     * A content operator implementation (").
     */
    static class MoveNextLineAndShowTextWithSpacing implements ContentOperator {

        private final SetTextWordSpacing setTextWordSpacing;
        private final SetTextCharacterSpacing setTextCharacterSpacing;
        private final MoveNextLineAndShowText moveNextLineAndShowText;

        public MoveNextLineAndShowTextWithSpacing(
                SetTextWordSpacing setTextWordSpacing,
                SetTextCharacterSpacing setTextCharacterSpacing,
                MoveNextLineAndShowText moveNextLineAndShowText) {
            this.setTextWordSpacing = setTextWordSpacing;
            this.setTextCharacterSpacing = setTextCharacterSpacing;
            this.moveNextLineAndShowText = moveNextLineAndShowText;
        }

        /**
         * @see ContentOperator#getOperatorName()
         */
        @Override
        public String getOperatorName() {
            return "\"";
        }

        @Override
        public void invoke(List<PdfObject> operands, PdfContentStreamHandler handler, PdfDictionary resources) {
            PdfNumber aw = (PdfNumber) operands.get(0);
            PdfNumber ac = (PdfNumber) operands.get(1);
            PdfString string = (PdfString) operands.get(2);

            List<PdfObject> twOperands = new ArrayList<>(1);
            twOperands.add(0, aw);
            setTextWordSpacing.invoke(twOperands, handler, resources);

            List<PdfObject> tcOperands = new ArrayList<>(1);
            tcOperands.add(0, ac);
            setTextCharacterSpacing.invoke(tcOperands, handler, resources);

            List<PdfObject> tickOperands = new ArrayList<>(1);
            tickOperands.add(0, string);
            moveNextLineAndShowText.invoke(tickOperands, handler, resources);
        }
    }

    /**
     * A content operator implementation (Q).
     */
    static class PopGraphicsState implements ContentOperator {

        /**
         * @see ContentOperator#getOperatorName()
         */
        @Override
        public String getOperatorName() {
            return "Q";
        }

        @Override
        public void invoke(List<PdfObject> operands, PdfContentStreamHandler handler, PdfDictionary resources) {
            handler.gsStack.pop();
        }
    }

    /**
     * A content operator implementation (gs).
     */
    static class ProcessGraphicsStateResource implements ContentOperator {

        /**
         * @see ContentOperator#getOperatorName()
         */
        @Override
        public String getOperatorName() {
            return "gs";
        }

        @Override
        public void invoke(List<PdfObject> operands, PdfContentStreamHandler handler, PdfDictionary resources) {
            PdfName dictionaryName = (PdfName) operands.get(0);
            PdfDictionary extGState = resources.getAsDict(PdfName.EXTGSTATE);
            if (extGState == null) {
                throw new IllegalArgumentException(
                        MessageLocalization.getComposedMessage(
                                "resources.do.not.contain.extgstate.entry.unable.to.process.operator.1",
                                getOperatorName()));
            }
            PdfDictionary gsDic = extGState.getAsDict(dictionaryName);
            if (gsDic == null) {
                throw new IllegalArgumentException(MessageLocalization.getComposedMessage(
                        "1.is.an.unknown.graphics.state.dictionary", dictionaryName));
            }

            // at this point, all we care about is the FONT entry in the GS
            // dictionary
            PdfArray fontParameter = gsDic.getAsArray(PdfName.FONT);
            if (fontParameter != null) {
                PdfObject pdfObject = fontParameter.getPdfObject(0);
                CMapAwareDocumentFont font = new CMapAwareDocumentFont((PRIndirectReference) pdfObject);
                float size = fontParameter.getAsNumber(1).floatValue();

                handler.graphicsState().setFont(font);
                handler.graphicsState().setFontSize(size);
            }
        }
    }

    /**
     * A content operator implementation (q).
     */
    static class PushGraphicsState implements ContentOperator {

        /**
         * @see ContentOperator#getOperatorName()
         */
        @Override
        public String getOperatorName() {
            return "q";
        }

        @Override
        public void invoke(List<PdfObject> operands, PdfContentStreamHandler handler, PdfDictionary resources) {
            GraphicsState gs = handler.gsStack.peek();
            GraphicsState copy = new GraphicsState(gs);
            handler.gsStack.push(copy);
        }
    }

    /**
     * A content operator implementation (Tc).
     */
    static class SetTextCharacterSpacing implements ContentOperator {

        /**
         * @see ContentOperator#getOperatorName()
         */
        @Override
        public String getOperatorName() {
            return "Tc";
        }

        @Override
        public void invoke(List<PdfObject> operands, PdfContentStreamHandler handler, PdfDictionary resources) {
            PdfNumber charSpace = (PdfNumber) operands.get(0);
            handler.graphicsState().setCharacterSpacing(charSpace.floatValue());
        }
    }

    /**
     * A content operator implementation (Tf).
     */
    static class SetTextFont implements ContentOperator {

        /**
         * @see ContentOperator#getOperatorName()
         */
        @Override
        public String getOperatorName() {
            return "Tf";
        }

        @Override
        public void invoke(List<PdfObject> operands, PdfContentStreamHandler handler, PdfDictionary resources) {
            PdfName fontResourceName = (PdfName) operands.get(0);
            float size = ((PdfNumber) operands.get(1)).floatValue();

            PdfDictionary fontsDictionary = resources.getAsDict(PdfName.FONT);
            PdfObject pdfObject = fontsDictionary.get(fontResourceName);
            CMapAwareDocumentFont font = new CMapAwareDocumentFont((PRIndirectReference) pdfObject);

            handler.graphicsState().setFont(font);
            handler.graphicsState().setFontSize(size);
        }
    }

    /**
     * A content operator implementation (Tm).
     */
    static class TextSetTextMatrix implements ContentOperator {

        /**
         * @see ContentOperator#getOperatorName()
         */
        @Override
        public String getOperatorName() {
            return "Tm";
        }

        @Override
        public void invoke(List<PdfObject> operands, PdfContentStreamHandler handler, PdfDictionary resources) {
            handler.textLineMatrix = getMatrix(operands);
            handler.textMatrix = handler.textLineMatrix;
        }
    }

    /**
     * A content operator implementation (TD).
     */
    static class TextMoveStartNextLineWithLeading implements ContentOperator {

        private final TextMoveStartNextLine moveStartNextLine;

        private final SetTextLeading setTextLeading;

        public TextMoveStartNextLineWithLeading(
                TextMoveStartNextLine moveStartNextLine,
                SetTextLeading setTextLeading) {
            this.moveStartNextLine = moveStartNextLine;
            this.setTextLeading = setTextLeading;
        }

        /**
         * @see ContentOperator#getOperatorName()
         */
        @Override
        public String getOperatorName() {
            return "TD";
        }

        @Override
        public void invoke(List<PdfObject> operands, PdfContentStreamHandler handler, PdfDictionary resources) {
            float ty = ((PdfNumber) operands.get(1)).floatValue();

            List<PdfObject> tlOperands = new ArrayList<>(1);
            tlOperands.add(0, new PdfNumber(-ty));
            setTextLeading.invoke(tlOperands, handler, resources);
            moveStartNextLine.invoke(operands, handler, resources);
        }
    }

    /**
     * A content operator implementation (Tj).
     */
    static class ShowText implements ContentOperator {

        /**
         * @see ContentOperator#getOperatorName()
         */
        @Override
        public String getOperatorName() {
            return "Tj";
        }

        @Override
        public void invoke(List<PdfObject> operands, PdfContentStreamHandler handler, PdfDictionary resources) {
            PdfString string = (PdfString) operands.get(0);
            handler.displayPdfString(string);
        }
    }

    /**
     * A content operator implementation (T*).
     */
    static class TextMoveNextLine implements ContentOperator {

        private final TextMoveStartNextLine moveStartNextLine;

        public TextMoveNextLine(TextMoveStartNextLine moveStartNextLine) {
            this.moveStartNextLine = moveStartNextLine;
        }

        /**
         * @see ContentOperator#getOperatorName()
         */
        @Override
        public String getOperatorName() {
            return "T*";
        }

        @Override
        public void invoke(List<PdfObject> operands, PdfContentStreamHandler handler, PdfDictionary resources) {
            List<PdfObject> tdoperands = new ArrayList<>(2);
            tdoperands.add(0, new PdfNumber(0));
            tdoperands.add(1, new PdfNumber(-handler.graphicsState().getLeading()));
            moveStartNextLine.invoke(tdoperands, handler, resources);
        }
    }

    /**
     * A content operator implementation (Td).
     */
    static class TextMoveStartNextLine implements ContentOperator {

        /**
         * @see ContentOperator#getOperatorName()
         */
        @Override
        public String getOperatorName() {
            return "Td";
        }

        @Override
        public void invoke(List<PdfObject> operands, PdfContentStreamHandler handler, PdfDictionary resources) {
            float tx = ((PdfNumber) operands.get(0)).floatValue();
            float ty = ((PdfNumber) operands.get(1)).floatValue();

            Matrix translationMatrix = new Matrix(tx, ty);
            handler.textMatrix = translationMatrix.multiply(handler.textLineMatrix);
            handler.textLineMatrix = handler.textMatrix;
        }
    }

    /**
     * A content operator implementation (Tr).
     */
    static class SetTextRenderMode implements ContentOperator {

        /**
         * @see ContentOperator#getOperatorName()
         */
        @Override
        public String getOperatorName() {
            return "Tr";
        }

        @Override
        public void invoke(List<PdfObject> operands, PdfContentStreamHandler handler, PdfDictionary resources) {
            PdfNumber render = (PdfNumber) operands.get(0);
            handler.graphicsState().setRenderMode(render.intValue());
        }
    }

    /**
     * A content operator implementation (Ts).
     */
    static class SetTextRise implements ContentOperator {

        /**
         * @see ContentOperator#getOperatorName()
         */
        @Override
        public String getOperatorName() {
            return "Ts";
        }

        @Override
        public void invoke(List<PdfObject> operands, PdfContentStreamHandler handler, PdfDictionary resources) {
            PdfNumber rise = (PdfNumber) operands.get(0);
            handler.graphicsState().setRise(rise.floatValue());
        }
    }

    /**
     * A content operator implementation (TL).
     */
    static class SetTextLeading implements ContentOperator {

        /**
         * @see ContentOperator#getOperatorName()
         */
        @Override
        public String getOperatorName() {
            return "TL";
        }

        @Override
        public void invoke(List<PdfObject> operands, PdfContentStreamHandler handler, PdfDictionary resources) {
            PdfNumber leading = (PdfNumber) operands.get(0);
            handler.graphicsState().setLeading(leading.floatValue());
        }
    }

    /**
     * A content operator implementation (Tz).
     */
    static class SetTextHorizontalScaling implements ContentOperator {

        /**
         * @see ContentOperator#getOperatorName()
         */
        @Override
        public String getOperatorName() {
            return "Tz";
        }

        @Override
        public void invoke(List<PdfObject> operands, PdfContentStreamHandler handler, PdfDictionary resources) {
            PdfNumber scale = (PdfNumber) operands.get(0);
            handler.graphicsState().setHorizontalScaling(scale.floatValue());
        }
    }

    /**
     * A content operator implementation (Tw).
     */
    static class SetTextWordSpacing implements ContentOperator {

        /**
         * @see ContentOperator#getOperatorName()
         */
        @Override
        public String getOperatorName() {
            return "Tw";
        }

        @Override
        public void invoke(List<PdfObject> operands, PdfContentStreamHandler handler, PdfDictionary resources) {
            PdfNumber wordSpace = (PdfNumber) operands.get(0);
            handler.graphicsState().setWordSpacing(wordSpace.floatValue());
        }
    }

    /**
     * A content operator implementation (BMC).
     */
    private static class BeginMarked implements ContentOperator {

        /**
         * @see ContentOperator#getOperatorName()
         */
        @Override
        public String getOperatorName() {
            return "BMC";
        }

        @Override
        public void invoke(List<PdfObject> operands, PdfContentStreamHandler handler, PdfDictionary resources) {
            PdfName tagName = (PdfName) operands.get(0);
            String realName = tagName.toString().substring(1).toLowerCase(Locale.ROOT);
            if ("artifact".equals(realName) || "placedpdf".equals(realName)) {
                handler.pushContext(null);
            } else {
                handler.pushContext(realName);
            }
        }

    }

    /**
     * A content operator implementation (BDC).
     */
    private static class BeginMarkedDict implements ContentOperator {

        /**
         * The BDC marked-content operator which brackets a marked-content sequence of objects within the content
         * stream.
         *
         * @param operands  list of operands
         * @param resources dictionary
         * @return PdfDictionary of type BDC marked-content
         */
        private static PdfDictionary getBDCDictionary(List<PdfObject> operands, PdfDictionary resources) {
            PdfObject pdfObject = operands.get(1);
            if (pdfObject.isName()) {
                PdfDictionary properties = resources.getAsDict(PdfName.PROPERTIES);
                PdfIndirectReference ir = properties.getAsIndirectObject((PdfName) pdfObject);
                if (ir != null) {
                    pdfObject = ir.getIndRef();
                } else {
                    pdfObject = properties.getAsDict((PdfName) pdfObject);
                }
            }
            return (PdfDictionary) pdfObject;
        }

        /**
         * @see ContentOperator#getOperatorName()
         */
        @Override
        public String getOperatorName() {
            return "BDC";
        }

        @Override
        public void invoke(List<PdfObject> operands, PdfContentStreamHandler handler, PdfDictionary resources) {
            PdfObject firstOperand = operands.get(0);
            String tagName = firstOperand.toString().substring(1).toLowerCase(Locale.ROOT);
            if ("artifact".equals(tagName) || "placedpdf".equals(tagName)
                    || handler.contextNames.peek() == null) {
                tagName = null;
            } else if ("l".equals(tagName)) {
                tagName = "ul";
            }
            PdfDictionary attrs = getBDCDictionary(operands, resources);
            if (attrs != null && tagName != null) {
                PdfString alternateText = attrs.getAsString(PdfName.E);
                if (alternateText != null) {
                    handler.pushContext(tagName);
                    handler.textFragments
                            .add(new FinalText(alternateText.toString()));
                    handler.popContext();
                    // ignore rest of the content of this element
                    handler.pushContext(null);
                    return;
                } else if (attrs.get(PdfName.TYPE) != null) {
                    // ignore tag for non-tag marked content that sometimes
                    // shows up.
                    tagName = "";
                }
            }
            handler.pushContext(tagName);
        }
    }

    /**
     * A content operator implementation (EMC).
     */
    private static class EndMarked implements ContentOperator {

        /**
         * @see ContentOperator#getOperatorName()
         */
        @Override
        public String getOperatorName() {
            return "EMC";
        }

        @Override
        public void invoke(List<PdfObject> operands, PdfContentStreamHandler handler, PdfDictionary resources) {
            handler.popContext();
        }
    }

    /**
     * Processes PDF content stream bytes.
     *
     * @param contentBytes the bytes of a content stream
     * @param resources    the resources that come with the content stream
     */
    protected void processContent(byte[] contentBytes, PdfDictionary resources) {
        try {
            PdfContentParser pdfContentParser = new PdfContentParser(new PRTokeniser(contentBytes));
            List<PdfObject> operands = new ArrayList<>();
            while (!pdfContentParser.parse(operands).isEmpty()) {
                PdfLiteral operator = (PdfLiteral) operands.getLast();
                invokeOperator(operator, operands, resources);
            }
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    /**
     * Gets the content bytes from a PdfObject, which may be a reference, a stream or an array.
     * This is a utility method that can be used by subclasses and other classes in this package.
     *
     * @param object the object to read bytes from
     * @return the content bytes
     * @throws java.io.IOException if there's an error reading the content
     */
    protected byte[] getContentBytesFromPdfObject(PdfObject object) throws java.io.IOException {
        return getContentBytesFromPdfObjectStatic(object);
    }

    /**
     * Gets the content bytes from a PdfObject, which may be a reference, a stream or an array.
     * This is a static utility method that can be used by any class in this package.
     *
     * @param object the object to read bytes from
     * @return the content bytes
     * @throws IOException if there's an error reading the content
     */
    static byte[] getContentBytesFromPdfObjectStatic(PdfObject object) throws IOException {
        switch (object.type()) {
            case PdfObject.INDIRECT:
                return getContentBytesFromPdfObjectStatic(PdfReader.getPdfObject(object));
            case PdfObject.STREAM:
                return PdfReader.getStreamBytes((PRStream) PdfReader.getPdfObject(object));
            case PdfObject.ARRAY:
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                for (PdfObject element : ((PdfArray) object).getElements()) {
                    baos.write(getContentBytesFromPdfObjectStatic(element));
                }
                return baos.toByteArray();
            default:
                throw new IllegalStateException("Unsupported type: " + object.getClass().getCanonicalName());
        }
    }

    /**
     * A content operator implementation (Do) for handling XObject forms.
     */
    protected class Do implements ContentOperator {

        /**
         * @see ContentOperator#getOperatorName()
         */
        @Override
        public String getOperatorName() {
            return "Do";
        }

        @Override
        public void invoke(List<PdfObject> operands, PdfContentStreamHandler handler, PdfDictionary resources) {
            PdfObject firstOperand = operands.getFirst();
            if (firstOperand instanceof PdfName) {
                PdfName name = (PdfName) firstOperand;
                PdfDictionary dictionary = resources.getAsDict(PdfName.XOBJECT);
                if (dictionary == null) {
                    return;
                }
                PdfStream stream = (PdfStream) dictionary.getDirectObject(name);
                PdfName subType = stream.getAsName(PdfName.SUBTYPE);
                if (PdfName.FORM.equals(subType)) {
                    PdfDictionary resources2 = stream.getAsDict(PdfName.RESOURCES);
                    if (resources2 == null) {
                        resources2 = resources;
                    }

                    byte[] data;
                    try {
                        data = handler.getContentBytesFromPdfObject(stream);
                    } catch (IOException ex) {
                        throw new ExceptionConverter(ex);
                    }
                    new PushGraphicsState().invoke(operands, handler, resources);
                    handler.processContent(data, resources2);
                    new PopGraphicsState().invoke(operands, handler, resources);
                }
            }
        }
    }
}
