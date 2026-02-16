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

import org.openpdf.text.error_messages.MessageLocalization;
import org.openpdf.text.pdf.CMapAwareDocumentFont;
import org.openpdf.text.pdf.PdfDictionary;
import org.openpdf.text.pdf.PdfLiteral;
import org.openpdf.text.pdf.PdfNumber;
import org.openpdf.text.pdf.PdfObject;
import org.openpdf.text.pdf.PdfString;
import java.util.ArrayList;
import java.util.List;
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
     */
    private Map<String, ContentOperator> operators;
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
     */
    protected abstract void installDefaultOperators();

    /**
     * Get the operator to process a command with a given name
     *
     * @param operatorName name of the operator that we might need to call
     * @return the operator or null if none present
     */
    protected abstract Optional<ContentOperator> lookupOperator(String operatorName);

    /**
     * Invokes an operator.
     *
     * @param operator  the PDF Syntax of the operator
     * @param operands  a list with operands
     * @param resources Pdf Resources found in the file containing the stream.
     */
    public abstract void invokeOperator(PdfLiteral operator, List<PdfObject> operands, PdfDictionary resources);

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
}
