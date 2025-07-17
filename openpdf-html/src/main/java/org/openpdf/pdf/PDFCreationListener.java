/*
 * {{{ header & license
 * Copyright (c) 2007 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.openpdf.pdf;

/**
 * Callback listener for PDF creation. To use this, call {@link ITextRenderer#setListener(PDFCreationListener)}.
 * Note that with a handle on the ITextRenderer instance (provided in the callback arguments) you can access
 * the {@link org.openpdf.text.pdf.PdfWriter} instance being used to create the document, using
 * {@link ITextRenderer#getOutputDevice()}, then calling {@link ITextOutputDevice#getWriter()}.
 */
public interface PDFCreationListener {
    /**
     * Called immediately after the iText Document instance is created but before the call to
     * {@link org.openpdf.text.Document#open()} is called. At this point you may still modify certain
     * properties of the PDF document header via the {@link org.openpdf.text.pdf.PdfWriter}; once
     * open() is called, you can't change, e.g. the version. See the iText documentation for what limitations
     * there are at this phase of processing.
     *
     * @param iTextRenderer the renderer preparing the document
     */
    void preOpen(ITextRenderer iTextRenderer);

    /**
     * Called immediately before the pages of the PDF file are about to be written out.
     * This is an opportunity to modify any document metadata that will be used to generate
     * the PDF header fields (the document information dictionary). Document metadata may be accessed
     * through the {@link ITextOutputDevice} that is returned by {@link ITextRenderer#getOutputDevice()}.
     *
     * @param iTextRenderer the renderer preparing the document
     * @param pageCount the number of pages that will be written to the PDF document
     */
    void preWrite(ITextRenderer iTextRenderer, int pageCount);

    /**
     * Called immediately before the iText Document instance is closed, e.g. before
     * {@link org.openpdf.text.Document#close()} is called.
     *
     * @param renderer the iTextRenderer preparing the document
     */
    void onClose(ITextRenderer renderer);
}
