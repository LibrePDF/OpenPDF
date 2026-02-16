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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import org.openpdf.text.ExceptionConverter;
import org.openpdf.text.error_messages.MessageLocalization;
import org.openpdf.text.pdf.CMapAwareDocumentFont;
import org.openpdf.text.pdf.PRIndirectReference;
import org.openpdf.text.pdf.PRStream;
import org.openpdf.text.pdf.PRTokeniser;
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

/**
 * @author dgd
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class PdfContentTextExtractor extends PdfContentStreamHandler {

    public PdfContentTextExtractor(TextAssembler renderListener) {
        super(renderListener);
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
        String contextName = contextNames.pop();
        List<TextAssemblyBuffer> newBuffer = textFragmentStreams.pop();
        // put together set of unparsed text fragments
        renderListener.reset();
        for (TextAssemblyBuffer fragment : textFragments) {
            fragment.accumulate(renderListener, contextName);
        }

        FinalText contextResult = renderListener.endParsingContext(contextName);
        Optional.ofNullable(contextResult)
                .map(FinalText::getText)
                .filter(text -> !text.isEmpty())
                .ifPresent(text -> newBuffer.add(contextResult));

        textFragments = newBuffer;
    }

    void pushContext(String newContextName) {
        contextNames.push(newContextName);
        textFragmentStreams.push(textFragments);
        textFragments = new ArrayList<>();
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
     * Displays text.
     *
     * @param string the text to display
     */
    void displayPdfString(PdfString string) {
        ParsedText renderInfo = ParsedText.create(string, graphicsState(), textMatrix);
        if (contextNames.peek() != null) {
            textFragments.add(renderInfo);
        }
        textMatrix = new Matrix(renderInfo.getUnscaledTextWidth(graphicsState()), 0)
                .multiply(textMatrix);
    }

    /**
     * @return result text
     */
    public String getResultantText() {
        if (contextNames.size() > 0) {
            throw new RuntimeException("can't get text with unprocessed stack items");
        }
        StringBuilder res = new StringBuilder();
        for (TextAssemblyBuffer fragment : textFragments) {
            res.append(fragment.getText());
        }
        return res.toString().trim();
    }
}
