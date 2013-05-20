/*
 * Copyright 2008 by Kevin Day.
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
 * the Initial Developer are Copyright (C) 1999-2008 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000-2008 by Paulo Soares. All Rights Reserved.
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
package com.lowagie.text.pdf.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.error_messages.MessageLocalization;
import com.lowagie.text.pdf.CMapAwareDocumentFont;
import com.lowagie.text.pdf.DocumentFont;
import com.lowagie.text.pdf.PRIndirectReference;
import com.lowagie.text.pdf.PRTokeniser;
import com.lowagie.text.pdf.PdfArray;
import com.lowagie.text.pdf.PdfContentParser;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfLiteral;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfNumber;
import com.lowagie.text.pdf.PdfString;

/**
 * Processor for a PDF content Stream.
 * @since	2.1.4
 */
public class PdfContentStreamProcessor {

	/** A map with all supported operators operators (PDF syntax). */
    private Map operators;
    /** Resources for the content stream. */
    private PdfDictionary resources;
    /** Stack keeping track of the graphics state. */
    private Stack gsStack = new Stack();
    /** Text matrix. */
    private Matrix textMatrix;
    /** Text line matrix. */
    private Matrix textLineMatrix;
    /** A list of listeners that will be notified of render events */
    final private RenderListener renderListener;
    
    /**
     * Creates a new PDF Content Stream Processor that will send it's output to the
     * designated render listener.
     * 
     * @param renderListener the {@link AbstractRenderListener} that will receive rendering notifications
     */
    public PdfContentStreamProcessor(RenderListener renderListener) {
        this.renderListener = renderListener;
        populateOperators();
        reset();
    }

    /**
     * Loads all the supported graphics and text state operators in a map.
     */
    private void populateOperators(){
        operators = new HashMap();
        
        registerContentOperator("q", new PushGraphicsState());
        registerContentOperator("Q", new PopGraphicsState());
        registerContentOperator("cm", new ModifyCurrentTransformationMatrix());
        registerContentOperator("gs", new ProcessGraphicsStateResource());
        
        SetTextCharacterSpacing tcOperator = new SetTextCharacterSpacing();
        registerContentOperator("Tc", tcOperator);
        SetTextWordSpacing twOperator = new SetTextWordSpacing();
        registerContentOperator("Tw", twOperator);
        registerContentOperator("Tz", new SetTextHorizontalScaling());
        SetTextLeading tlOperator = new SetTextLeading();
        registerContentOperator("TL", tlOperator);
        registerContentOperator("Tf", new SetTextFont());
        registerContentOperator("Tr", new SetTextRenderMode());
        registerContentOperator("Ts", new SetTextRise());
        
        registerContentOperator("BT", new BeginText());
        registerContentOperator("ET", new EndText());

        TextMoveStartNextLine tdOperator = new TextMoveStartNextLine();
        registerContentOperator("Td", tdOperator);
        registerContentOperator("TD", new TextMoveStartNextLineWithLeading(tdOperator, tlOperator));
        registerContentOperator("Tm", new TextSetTextMatrix());
        TextMoveNextLine tstarOperator = new TextMoveNextLine(tdOperator);
        registerContentOperator("T*", tstarOperator);

        ShowText tjOperator = new ShowText();
        registerContentOperator("Tj", new ShowText());
        MoveNextLineAndShowText tickOperator = new MoveNextLineAndShowText(tstarOperator, tjOperator);
        registerContentOperator("'", tickOperator);
        registerContentOperator("\"", new MoveNextLineAndShowTextWithSpacing(twOperator, tcOperator, tickOperator));
        registerContentOperator("TJ", new ShowTextArray());
    }
    
    /**
     * Registers a content operator that will be called when the specified operator string is encountered during content processing.
     * Each operator may be registered only once (it is not legal to have multiple operators with the same operatorString)
     * @param operatorString the operator id
     * @param operator the operator that will receive notification when the operator is encountered
     * @since 2.1.7
     */
    public void registerContentOperator(String operatorString, ContentOperator operator){
        if (operators.containsKey(operatorString))
            throw new IllegalArgumentException(MessageLocalization.getComposedMessage("operator.1.already.registered", operatorString));
        operators.put(operatorString, operator);
    }
    
    /**
     * Resets the graphics state stack, matrices and resources.
     */
    public void reset(){
        gsStack.removeAllElements();
        gsStack.add(new GraphicsState());
        textMatrix = null;
        textLineMatrix = null;
        resources = null;
    }
    
    /**
     * Returns the current graphics state.
     * @return	the graphics state
     */
    private GraphicsState gs(){
        return (GraphicsState)gsStack.peek();
    }
    
    /**
     * Returns the current text matrix.
     * @return	the text matrix
     * @since 2.1.5
     */
    private Matrix getCurrentTextMatrix(){
        return textMatrix;
    }
    
    /**
     * Returns the current line matrix.
     * @return	the line matrix
     * @since 2.1.5
     */
    private Matrix getCurrentTextLineMatrix(){
        return textLineMatrix;
    }
    
    /**
     * Invokes an operator.
     * @param operator	the PDF Syntax of the operator
     * @param operands	a list with operands
     */
    private void invokeOperator(PdfLiteral operator, ArrayList operands){
        ContentOperator op = (ContentOperator)operators.get(operator.toString());
        if (op == null){
            //System.out.println("Skipping operator " + operator);
            return;
        }
        
        op.invoke(this, operator, operands);
    }

    /**
     * Decodes a PdfString (which will contain glyph ids encoded in the font's encoding)
     * based on the active font, and determine the unicode equivalent
     * @param in	the String that needs to be encoded
     * @return	the encoded String
     * @since 2.1.7
     */
    private String decode(PdfString in){
        byte[] bytes = in.getBytes();
        return gs().font.decode(bytes, 0, bytes.length);
    }
    
    /**
     * Displays text.
     * @param string	the text to display
     */
    private void displayPdfString(PdfString string){
        String unicode = decode(string);
        
        TextRenderInfo renderInfo = new TextRenderInfo(unicode, gs(), textMatrix);

        renderListener.renderText(renderInfo);

        textMatrix = new Matrix(renderInfo.getUnscaledWidth(), 0).multiply(textMatrix);
    }
    
    /**
     * Adjusts the text matrix for the specified adjustment value (see TJ operator in the PDF spec for information)
     * @param tj the text adjustment
     */
    private void applyTextAdjust(float tj){
        float adjustBy = -tj/1000f * gs().fontSize * gs().horizontalScaling;
        
        textMatrix = new Matrix(adjustBy, 0).multiply(textMatrix);
    }
    
    /**
     * Processes PDF syntax
     * @param contentBytes	the bytes of a content stream
     * @param resources		the resources that come with the content stream
     */
    public void processContent(byte[] contentBytes, PdfDictionary resources){
        
        reset();
        this.resources = resources;
        try {
            PdfContentParser ps = new PdfContentParser(new PRTokeniser(contentBytes));
            ArrayList operands = new ArrayList();
            while (ps.parse(operands).size() > 0){
                PdfLiteral operator = (PdfLiteral)operands.get(operands.size()-1);
                invokeOperator(operator, operands);
            }
            
        }
        catch (Exception e) {
            throw new ExceptionConverter(e);
        }    
        
    }
    

    /**
     * A content operator implementation (TJ).
     */
    private static class ShowTextArray implements ContentOperator{
        public void invoke(PdfContentStreamProcessor processor, PdfLiteral operator, ArrayList operands) {
            PdfArray array = (PdfArray)operands.get(0);
            float tj = 0;
            for (Iterator i = array.listIterator(); i.hasNext(); ) {
            	Object entryObj = i.next();
                if (entryObj instanceof PdfString){
                    processor.displayPdfString((PdfString)entryObj);
                    tj = 0;
                } else {
                    tj = ((PdfNumber)entryObj).floatValue();
                    processor.applyTextAdjust(tj);
                }
            }

        }
    }

    /**
     * A content operator implementation (").
     */
    private static class MoveNextLineAndShowTextWithSpacing implements ContentOperator{
        private final SetTextWordSpacing setTextWordSpacing;
        private final SetTextCharacterSpacing setTextCharacterSpacing;
        private final MoveNextLineAndShowText moveNextLineAndShowText;
        
        public MoveNextLineAndShowTextWithSpacing(SetTextWordSpacing setTextWordSpacing, SetTextCharacterSpacing setTextCharacterSpacing, MoveNextLineAndShowText moveNextLineAndShowText) {
            this.setTextWordSpacing = setTextWordSpacing;
            this.setTextCharacterSpacing = setTextCharacterSpacing;
            this.moveNextLineAndShowText = moveNextLineAndShowText;
        }
        
        public void invoke(PdfContentStreamProcessor processor, PdfLiteral operator, ArrayList operands) {
            PdfNumber aw = (PdfNumber)operands.get(0);
            PdfNumber ac = (PdfNumber)operands.get(1);
            PdfString string = (PdfString)operands.get(2);
            
            ArrayList twOperands = new ArrayList(1);
            twOperands.add(0, aw);
            setTextWordSpacing.invoke(processor, null, twOperands);

            ArrayList tcOperands = new ArrayList(1);
            tcOperands.add(0, ac);
            setTextCharacterSpacing.invoke(processor, null, tcOperands);
            
            ArrayList tickOperands = new ArrayList(1);
            tickOperands.add(0, string);
            moveNextLineAndShowText.invoke(processor, null, tickOperands);
        }
    }

    /**
     * A content operator implementation (').
     */
    private static class MoveNextLineAndShowText implements ContentOperator{
        private final TextMoveNextLine textMoveNextLine;
        private final ShowText showText;
        public MoveNextLineAndShowText(TextMoveNextLine textMoveNextLine, ShowText showText) {
            this.textMoveNextLine = textMoveNextLine;
            this.showText = showText;
        }
        
        public void invoke(PdfContentStreamProcessor processor, PdfLiteral operator, ArrayList operands) {
            textMoveNextLine.invoke(processor, null, new ArrayList(0));
            showText.invoke(processor, null, operands);
        }
    }

    /**
     * A content operator implementation (Tj).
     */
    private static class ShowText implements ContentOperator{
        public void invoke(PdfContentStreamProcessor processor, PdfLiteral operator, ArrayList operands) {
            PdfString string = (PdfString)operands.get(0);
            
            processor.displayPdfString(string);
        }
    }
    

    /**
     * A content operator implementation (T*).
     */
    private static class TextMoveNextLine implements ContentOperator{
        private final TextMoveStartNextLine moveStartNextLine;
        public TextMoveNextLine(TextMoveStartNextLine moveStartNextLine){
            this.moveStartNextLine = moveStartNextLine;
        }
        
        public void invoke(PdfContentStreamProcessor processor, PdfLiteral operator, ArrayList operands) {
            ArrayList tdoperands = new ArrayList(2);
            tdoperands.add(0, new PdfNumber(0));
            tdoperands.add(1, new PdfNumber(-processor.gs().leading));
            moveStartNextLine.invoke(processor, null, tdoperands);
        }
    }

    /**
     * A content operator implementation (Tm).
     */
    private static class TextSetTextMatrix implements ContentOperator{
        public void invoke(PdfContentStreamProcessor processor, PdfLiteral operator, ArrayList operands) {
            float a = ((PdfNumber)operands.get(0)).floatValue();
            float b = ((PdfNumber)operands.get(1)).floatValue();
            float c = ((PdfNumber)operands.get(2)).floatValue();
            float d = ((PdfNumber)operands.get(3)).floatValue();
            float e = ((PdfNumber)operands.get(4)).floatValue();
            float f = ((PdfNumber)operands.get(5)).floatValue();
            
            processor.textLineMatrix = new Matrix(a, b, c, d, e, f);
            processor.textMatrix = processor.textLineMatrix;
        }
    }

    /**
     * A content operator implementation (TD).
     */
    private static class TextMoveStartNextLineWithLeading implements ContentOperator{
        private final TextMoveStartNextLine moveStartNextLine;
        private final SetTextLeading setTextLeading;
        public TextMoveStartNextLineWithLeading(TextMoveStartNextLine moveStartNextLine, SetTextLeading setTextLeading){
            this.moveStartNextLine = moveStartNextLine;
            this.setTextLeading = setTextLeading;
        }
        public void invoke(PdfContentStreamProcessor processor, PdfLiteral operator, ArrayList operands) {
            float ty = ((PdfNumber)operands.get(1)).floatValue();
            
            ArrayList tlOperands = new ArrayList(1);
            tlOperands.add(0, new PdfNumber(-ty));
            setTextLeading.invoke(processor, null, tlOperands);
            moveStartNextLine.invoke(processor, null, operands);
        }
    }

    /**
     * A content operator implementation (Td).
     */
    private static class TextMoveStartNextLine implements ContentOperator{
        public void invoke(PdfContentStreamProcessor processor, PdfLiteral operator, ArrayList operands) {
            float tx = ((PdfNumber)operands.get(0)).floatValue();
            float ty = ((PdfNumber)operands.get(1)).floatValue();
            
            Matrix translationMatrix = new Matrix(tx, ty);
            processor.textMatrix =  translationMatrix.multiply(processor.textLineMatrix);
            processor.textLineMatrix = processor.textMatrix;
        }
    }

    /**
     * A content operator implementation (Tf).
     */
    private static class SetTextFont implements ContentOperator{
        public void invoke(PdfContentStreamProcessor processor, PdfLiteral operator, ArrayList operands) {
            PdfName fontResourceName = (PdfName)operands.get(0);
            float size = ((PdfNumber)operands.get(1)).floatValue();
            
            PdfDictionary fontsDictionary = processor.resources.getAsDict(PdfName.FONT);
            CMapAwareDocumentFont font = new CMapAwareDocumentFont((PRIndirectReference)fontsDictionary.get(fontResourceName));
            
            processor.gs().font = font;
            processor.gs().fontSize = size;
            
        }
    }

    /**
     * A content operator implementation (Tr).
     */
    private static class SetTextRenderMode implements ContentOperator{
        public void invoke(PdfContentStreamProcessor processor, PdfLiteral operator, ArrayList operands) {
            PdfNumber render = (PdfNumber)operands.get(0);
            processor.gs().renderMode = render.intValue();
        }
    }

    /**
     * A content operator implementation (Ts).
     */
    private static class SetTextRise implements ContentOperator{
        public void invoke(PdfContentStreamProcessor processor, PdfLiteral operator, ArrayList operands) {
            PdfNumber rise = (PdfNumber)operands.get(0);
            processor.gs().rise = rise.floatValue();
        }
    }

    /**
     * A content operator implementation (TL).
     */
    private static class SetTextLeading implements ContentOperator{
        public void invoke(PdfContentStreamProcessor processor, PdfLiteral operator, ArrayList operands) {
            PdfNumber leading = (PdfNumber)operands.get(0);
            processor.gs().leading = leading.floatValue();
        }
    }

    /**
     * A content operator implementation (Tz).
     */
    private static class SetTextHorizontalScaling implements ContentOperator{
        public void invoke(PdfContentStreamProcessor processor, PdfLiteral operator, ArrayList operands) {
            PdfNumber scale = (PdfNumber)operands.get(0);
            processor.gs().horizontalScaling = scale.floatValue();
        }
    }

    /**
     * A content operator implementation (Tc).
     */
    private static class SetTextCharacterSpacing implements ContentOperator{
        public void invoke(PdfContentStreamProcessor processor, PdfLiteral operator, ArrayList operands) {
            PdfNumber charSpace = (PdfNumber)operands.get(0);
            processor.gs().characterSpacing = charSpace.floatValue();
        }
    }

    /**
     * A content operator implementation (Tw).
     */
    private static class SetTextWordSpacing implements ContentOperator{
        public void invoke(PdfContentStreamProcessor processor, PdfLiteral operator, ArrayList operands) {
            PdfNumber wordSpace = (PdfNumber)operands.get(0);
            processor.gs().wordSpacing = wordSpace.floatValue();
        }
    }

    /**
     * A content operator implementation (gs).
     */
    private static class ProcessGraphicsStateResource implements ContentOperator{
        public void invoke(PdfContentStreamProcessor processor, PdfLiteral operator, ArrayList operands) {
            PdfName dictionaryName = (PdfName)operands.get(0);
            PdfDictionary extGState = processor.resources.getAsDict(PdfName.EXTGSTATE);
            if (extGState == null)
                throw new IllegalArgumentException(MessageLocalization.getComposedMessage("resources.do.not.contain.extgstate.entry.unable.to.process.operator.1", operator));
            PdfDictionary gsDic = extGState.getAsDict(dictionaryName);
            if (gsDic == null)
                throw new IllegalArgumentException(MessageLocalization.getComposedMessage("1.is.an.unknown.graphics.state.dictionary", dictionaryName));
            
            // at this point, all we care about is the FONT entry in the GS dictionary
            PdfArray fontParameter = gsDic.getAsArray(PdfName.FONT);
            if (fontParameter != null){
                CMapAwareDocumentFont font = new CMapAwareDocumentFont((PRIndirectReference)fontParameter.getPdfObject(0));
                float size = fontParameter.getAsNumber(1).floatValue();
                
                processor.gs().font = font;
                processor.gs().fontSize = size;
            }            
        }
    }

    /**
     * A content operator implementation (q).
     */
    private static class PushGraphicsState implements ContentOperator{
        public void invoke(PdfContentStreamProcessor processor, PdfLiteral operator, ArrayList operands) {
            GraphicsState gs = (GraphicsState) processor.gsStack.peek();
            GraphicsState copy = new GraphicsState(gs);
            processor.gsStack.push(copy);
        }
    }
    
    /**
     * A content operator implementation (cm).
     */
    private static class ModifyCurrentTransformationMatrix implements ContentOperator{
        public void invoke(PdfContentStreamProcessor processor, PdfLiteral operator, ArrayList operands) {
            float a = ((PdfNumber)operands.get(0)).floatValue();
            float b = ((PdfNumber)operands.get(1)).floatValue();
            float c = ((PdfNumber)operands.get(2)).floatValue();
            float d = ((PdfNumber)operands.get(3)).floatValue();
            float e = ((PdfNumber)operands.get(4)).floatValue();
            float f = ((PdfNumber)operands.get(5)).floatValue();
            Matrix matrix = new Matrix(a, b, c, d, e, f);
            GraphicsState gs = (GraphicsState)processor.gsStack.peek();
            gs.ctm = gs.ctm.multiply(matrix);
        }
    }

    /**
     * A content operator implementation (Q).
     */
    private static class PopGraphicsState implements ContentOperator{
        public void invoke(PdfContentStreamProcessor processor, PdfLiteral operator, ArrayList operands) {
            processor.gsStack.pop();
        }
    }

    /**
     * A content operator implementation (BT).
     */
    private static class BeginText implements ContentOperator{
        public void invoke(PdfContentStreamProcessor processor, PdfLiteral operator, ArrayList operands) {
            processor.textMatrix = new Matrix();
            processor.textLineMatrix = processor.textMatrix;
        }
    }

    /**
     * A content operator implementation (ET).
     */
    private static class EndText implements ContentOperator{
        public void invoke(PdfContentStreamProcessor processor, PdfLiteral operator, ArrayList operands) {
            processor.textMatrix = null;
            processor.textLineMatrix = null;
        }
    }
    
}
