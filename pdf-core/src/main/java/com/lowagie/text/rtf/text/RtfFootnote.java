/*
 * $Id: RtfFootnote.java 3580 2008-08-06 15:52:00Z howard_s $
 *
 * Copyright 2001, 2002, 2003, 2004 by Mark Hall
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
 * the Initial Developer are Copyright (C) 1999, 2000, 2001, 2002 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000, 2001, 2002 by Paulo Soares. All Rights Reserved.
 *
 * Contributor(s): all the names of the contributors are added in the source code
 * where applicable.
 *
 * Alternatively, the contents of this file may be used under the terms of the
 * LGPL license (the ?GNU LIBRARY GENERAL PUBLIC LICENSE?), in which case the
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
package com.lowagie.text.rtf.text;

import java.io.IOException;
import java.io.OutputStream;

import com.lowagie.text.Chunk;
import com.lowagie.text.DocWriter;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Footnote;
import com.lowagie.text.rtf.RtfBasicElement;
import com.lowagie.text.rtf.document.RtfDocument;
import com.lowagie.text.rtf.style.RtfFont;
import com.lowagie.text.rtf.style.RtfParagraphStyle;

/**
 * The RtfFootnote provides support for adding Footnotes to the rtf document.
 * Only simple Footnotes with Title / Content are supported.
 * <p>
 * 
 * @version $Id: RtfFootnote.java 3580 2008-08-06 15:52:00Z howard_s $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 * @author Thomas Bickel (tmb99@inode.at)
 */
public class RtfFootnote extends RtfPhrase {

  /**
   * Constant for the actual footnote
   */
  private static final byte[] FOOTNOTE = DocWriter.getISOBytes("\\*\\footnote");

  private static final byte[] SUPER = DocWriter.getISOBytes("\\super");
  private static final byte[] CHFTN = DocWriter.getISOBytes("\\chftn");

  /**
   * An optional RtfParagraphStyle to use for styling.
   */
  protected RtfParagraphStyle paragraphStyle = null;

  /**
   * Constructs a RtfFootnote based on an Footnote.
   * <p>
   * 
   * @param doc
   *          The RtfDocument this RtfFootnote belongs to
   * @param footnote
   *          The Footnote this RtfFootnote is based off
   */
  public RtfFootnote(RtfDocument doc, Footnote footnote) {
    super(doc);
    RtfFont baseFont = null;
    if (footnote.getFont() instanceof RtfParagraphStyle) {
      this.paragraphStyle = this.document.getDocumentHeader()
          .getRtfParagraphStyle(
              ((RtfParagraphStyle) footnote.getFont()).getStyleName());
      baseFont = this.paragraphStyle;
    } else {
      baseFont = new RtfFont(this.document, footnote.getFont());
      this.paragraphStyle = new RtfParagraphStyle(this.document, this.document
          .getDocumentHeader().getRtfParagraphStyle("Normal"));
    }
    for (int i = 0; i < footnote.size(); i++) {
      Element chunk = (Element) footnote.get(i);
      if (chunk instanceof Chunk) {
        ((Chunk) chunk).setFont(baseFont.difference(((Chunk) chunk).getFont()));
      }
      try {
        RtfBasicElement[] rtfElements = doc.getMapper().mapElement(chunk);
        for (int j = 0; j < rtfElements.length; j++) {
          chunks.add(rtfElements[j]);
        }
      } catch (DocumentException de) {
      }
    }
  }

  /**
   * Writes the content of the RtfFootnote
   */
  @Override
  public void writeContent(final OutputStream result) throws IOException {
    result.write(OPEN_GROUP);
    result.write(OPEN_GROUP);
    result.write(SUPER);
    result.write(CHFTN);

    result.write(OPEN_GROUP);
    result.write(FOOTNOTE);
    result.write(CHFTN);
    result.write(RtfParagraph.PARAGRAPH_DEFAULTS);
    result.write(RtfParagraph.PLAIN);

    if (this.paragraphStyle != null) {
      this.paragraphStyle.writeBegin(result);
    }

    for (int i = 0; i < chunks.size(); i++) {
      RtfBasicElement rbe = (RtfBasicElement) chunks.get(i);
      rbe.writeContent(result);
    }

    if (this.paragraphStyle != null) {
      this.paragraphStyle.writeEnd(result);
    }

    result.write(CLOSE_GROUP);
    result.write(CLOSE_GROUP);
    result.write(CLOSE_GROUP);
  }
}