/*
 * $Id: PdfPageEventForwarder.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * Copyright 2005 Bruno Lowagie
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

package com.lowagie.text.pdf.events;

import java.util.ArrayList;
import java.util.List;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPageEvent;
import com.lowagie.text.pdf.PdfWriter;

/**
 * If you want to add more than one page event to a PdfWriter,
 * you have to construct a PdfPageEventForwarder, add the
 * different events to this object and add the forwarder to
 * the PdfWriter.
 */

public class PdfPageEventForwarder implements PdfPageEvent {

    /** ArrayList containing all the PageEvents that have to be executed. */
    protected List<PdfPageEvent> events = new ArrayList<>();

    /**
     * Add a page event to the forwarder.
     * @param event an event that has to be added to the forwarder.
     */
    public void addPageEvent(PdfPageEvent event) {
        events.add(event);
    }

    /**
     * Called when the document is opened.
     *
     * @param writer
     *            the <CODE>PdfWriter</CODE> for this document
     * @param document
     *            the document
     */
    public void onOpenDocument(PdfWriter writer, Document document) {
        for (PdfPageEvent event : events) {
            event.onOpenDocument(writer, document);
        }
    }

    /**
     * Called when a page is initialized.
     * <P>
     * Note that if even if a page is not written this method is still called.
     * It is preferable to use <CODE>onEndPage</CODE> to avoid infinite loops.
     *
     * @param writer
     *            the <CODE>PdfWriter</CODE> for this document
     * @param document
     *            the document
     */
    public void onStartPage(PdfWriter writer, Document document) {
        for (PdfPageEvent event : events) {
            event.onStartPage(writer, document);
        }
    }

    /**
     * Called when a page is finished, just before being written to the
     * document.
     *
     * @param writer
     *            the <CODE>PdfWriter</CODE> for this document
     * @param document
     *            the document
     */
    public void onEndPage(PdfWriter writer, Document document) {
        for (PdfPageEvent event : events) {
            event.onEndPage(writer, document);
        }
    }

    /**
     * Called when the document is closed.
     * <P>
     * Note that this method is called with the page number equal to the last
     * page plus one.
     *
     * @param writer
     *            the <CODE>PdfWriter</CODE> for this document
     * @param document
     *            the document
     */
    public void onCloseDocument(PdfWriter writer, Document document) {
        for (PdfPageEvent event : events) {
            event.onCloseDocument(writer, document);
        }
    }

    /**
     * Called when a Paragraph is written.
     * <P>
     * <CODE>paragraphPosition</CODE> will hold the height at which the
     * paragraph will be written to. This is useful to insert bookmarks with
     * more control.
     *
     * @param writer
     *            the <CODE>PdfWriter</CODE> for this document
     * @param document
     *            the document
     * @param paragraphPosition
     *            the position the paragraph will be written to
     */
    public void onParagraph(PdfWriter writer, Document document,
            float paragraphPosition) {
        for (PdfPageEvent event : events) {
            event.onParagraph(writer, document, paragraphPosition);
        }
    }

    /**
     * Called when a Paragraph is written.
     * <P>
     * <CODE>paragraphPosition</CODE> will hold the height of the end of the
     * paragraph.
     *
     * @param writer
     *            the <CODE>PdfWriter</CODE> for this document
     * @param document
     *            the document
     * @param paragraphPosition
     *            the position of the end of the paragraph
     */
    public void onParagraphEnd(PdfWriter writer, Document document,
            float paragraphPosition) {
        for (PdfPageEvent event : events) {
            event.onParagraphEnd(writer, document, paragraphPosition);
        }
    }

    /**
     * Called when a Chapter is written.
     * <P>
     * <CODE>position</CODE> will hold the height at which the chapter will be
     * written to.
     *
     * @param writer
     *            the <CODE>PdfWriter</CODE> for this document
     * @param document
     *            the document
     * @param paragraphPosition
     *            the position the chapter will be written to
     * @param title
     *            the title of the Chapter
     */
    public void onChapter(PdfWriter writer, Document document,
            float paragraphPosition, Paragraph title) {
        for (PdfPageEvent event : events) {
            event.onChapter(writer, document, paragraphPosition, title);
        }
    }

    /**
     * Called when the end of a Chapter is reached.
     * <P>
     * <CODE>position</CODE> will hold the height of the end of the chapter.
     *
     * @param writer
     *            the <CODE>PdfWriter</CODE> for this document
     * @param document
     *            the document
     * @param position
     *            the position of the end of the chapter.
     */
    public void onChapterEnd(PdfWriter writer, Document document, float position) {
        for (PdfPageEvent event : events) {
            event.onChapterEnd(writer, document, position);
        }
    }

    /**
     * Called when a Section is written.
     * <P>
     * <CODE>position</CODE> will hold the height at which the section will be
     * written to.
     *
     * @param writer
     *            the <CODE>PdfWriter</CODE> for this document
     * @param document
     *            the document
     * @param paragraphPosition
     *            the position the section will be written to
     * @param depth
     *            the number depth of the Section
     * @param title
     *            the title of the section
     */
    public void onSection(PdfWriter writer, Document document,
            float paragraphPosition, int depth, Paragraph title) {
        for (PdfPageEvent event : events) {
            event.onSection(writer, document, paragraphPosition, depth, title);
        }
    }

    /**
     * Called when the end of a Section is reached.
     * <P>
     * <CODE>position</CODE> will hold the height of the section end.
     *
     * @param writer
     *            the <CODE>PdfWriter</CODE> for this document
     * @param document
     *            the document
     * @param position
     *            the position of the end of the section
     */
    public void onSectionEnd(PdfWriter writer, Document document, float position) {
        for (PdfPageEvent event : events) {
            event.onSectionEnd(writer, document, position);
        }
    }

    /**
     * Called when a <CODE>Chunk</CODE> with a generic tag is written.
     * <P>
     * It is useful to pinpoint the <CODE>Chunk</CODE> location to generate
     * bookmarks, for example.
     *
     * @param writer
     *            the <CODE>PdfWriter</CODE> for this document
     * @param document
     *            the document
     * @param rect
     *            the <CODE>Rectangle</CODE> containing the <CODE>Chunk
     *            </CODE>
     * @param text
     *            the text of the tag
     */
    public void onGenericTag(PdfWriter writer, Document document,
            Rectangle rect, String text) {
        for (PdfPageEvent event : events) {
            event.onGenericTag(writer, document, rect, text);
        }
    }
}