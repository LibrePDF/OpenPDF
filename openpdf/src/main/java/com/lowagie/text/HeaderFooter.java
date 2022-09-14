/*
 * $Id: HeaderFooter.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * Copyright 1999, 2000, 2001, 2002 by Bruno Lowagie.
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
 * https://github.com/LibrePDF/OpenPDF
 */

package com.lowagie.text;

import java.util.ArrayList;
import java.util.List;

/**
 * A <CODE>HeaderFooter</CODE>-object is a <CODE>Rectangle</CODe> with text that can be put above and/or below every
 * page.
 * <p>
 * Example: <BLOCKQUOTE>
 *
 * <PRE>
 * <STRONG>HeaderFooter header = new HeaderFooter(new Phrase("This is a header."), false);</STRONG>
 * <STRONG>HeaderFooter footer = new HeaderFooter(new Phrase("This is page "), new Phrase("."));</STRONG>
 * document.setHeader(header);
 * document.setFooter(footer);
 * </PRE>
 *
 * </BLOCKQUOTE>
 */

public class HeaderFooter extends Rectangle {

    // membervariables

    /**
     * Does the page contain a pagenumber?
     */
    private boolean numbered;

    /**
     * This is the <CODE>Phrase</CODE> that comes before the pagenumber.
     */
    private Phrase before = null;

    /**
     * This is number of the page.
     */
    private int pageN;

    /**
     * This is the <CODE>Phrase</CODE> that comes after the pagenumber.
     */
    private Phrase after = null;

    /**
     * This is alignment of the header/footer.
     */
    private int alignment;

    /**
     * This is the <CODE>List</CODE> containing non-text <CODE>Element</CODE>.
     */
    private java.util.List<Element> specialContent = null;

    /**
     * This is the padding of height of header/footer.
     */
    private float padding;

    // constructors

    /**
     * Constructs a <CODE>HeaderFooter</CODE>-object.
     *
     * @param before the <CODE>Phrase</CODE> before the pagenumber
     * @param after  the <CODE>Phrase</CODE> before the pagenumber
     */
    public HeaderFooter(Phrase before, Phrase after) {
        super(0, 0, 0, 0);
        setBorder(TOP + BOTTOM);
        setBorderWidth(1);

        numbered = true;
        this.before = before;
        this.after = after;
    }

    /**
     * Constructs a <CODE>Header</CODE>-object with a pagenumber at the end.
     *
     * @param before   the <CODE>Phrase</CODE> before the pagenumber
     * @param numbered page will be numbered if <CODE>true</CODE>
     */
    public HeaderFooter(Phrase before, boolean numbered) {
        super(0, 0, 0, 0);
        setBorder(TOP + BOTTOM);
        setBorderWidth(1);

        this.numbered = numbered;
        this.before = before;
    }

    /**
     * Constructs a <CODE>Header</CODE>-object with a pagenumber at the beginning.
     *
     * @param numbered page will be numbered if <CODE>true</CODE>
     * @param after    the <CODE>Phrase</CODE> after the pagenumber
     */
    public HeaderFooter(boolean numbered, Phrase after) {
        super(0, 0, 0, 0);
        setBorder(TOP + BOTTOM);
        setBorderWidth(1);

        this.numbered = numbered;
        this.after = after;
    }

    /**
     * Constructs a <CODE>Header</CODE>-object with only a pagenumber.
     *
     * @param numbered <CODE>true</CODE> if the page has to be numbered
     */
    public HeaderFooter(boolean numbered) {
        this(null, true);
        this.numbered = numbered;
    }

    // methods

    /**
     * Checks if the HeaderFooter contains a page number.
     *
     * @return true if the page has to be numbered
     */
    public boolean isNumbered() {
        return numbered;
    }

    /**
     * Gets the part that comes before the pageNumber.
     *
     * @return a Phrase
     */
    public Phrase getBefore() {
        return before;
    }

    /**
     * Gets the part that comes after the pageNumber.
     *
     * @return a Phrase
     */
    public Phrase getAfter() {
        return after;
    }

    /**
     * Sets the page number.
     *
     * @param pageN the new page number
     */
    public void setPageNumber(int pageN) {
        this.pageN = pageN;
    }

    /**
     * Sets the alignment.
     *
     * @param alignment the new alignment
     */
    public void setAlignment(int alignment) {
        this.alignment = alignment;
    }

    /**
     * Gets padding of height of header/footer.
     *
     * @return the padding of height
     */
    public float getPadding() {
        return padding;
    }

    /**
     * Sets padding of height of header/footer.
     *
     * @param padding the new padding of height
     */
    public void setPadding(float padding) {
        this.padding = padding;
    }

    /**
     * Increases current padding by adding new value into it
     *
     * @param augment the new value
     */
    public void addPadding(float augment) {
        padding += augment;
    }

    /**
     * Adds non-text <CODE>Element</CODE> into <CODE>specialContent</CODE>
     *
     * @param element the new non-text <CODE>Element</CODE>
     */
    public void addSpecialContent(Element element) {
        if (specialContent == null) {
            specialContent = new ArrayList<>();
        }
        specialContent.add(element);
    }

    /**
     * Gets <CODE>specialContent</CODE>
     *
     * @return <CODE>specialContent</CODE>
     */
    public List<Element> getSpecialContent() {
        return specialContent;
    }

    // methods to retrieve the membervariables

    /**
     * Gets the <CODE>Paragraph</CODE> that can be used as header or footer.
     *
     * @return a <CODE>Paragraph</CODE>
     */
    public Paragraph paragraph() {
        Paragraph paragraph;

        if (before != null) {
            paragraph = new Paragraph(before.getLeading());
            paragraph.add(before);

            // Adding a Paragraph to another Paraghraph adds a newline that needs to be removed in headers and footers
            if (before instanceof Paragraph
                    &&
                    paragraph.size() >= 2
                    &&
                    "\n".equals(paragraph.get(paragraph.size() - 1).toString())) {
                paragraph.remove(paragraph.size() - 1);
            }
        } else {
            paragraph = new Paragraph();
        }

        if (numbered) {
            Font font = getFont();
            if (font != null) {
                paragraph.addSpecial(new Chunk(String.valueOf(pageN), font));
            } else {
                paragraph.addSpecial(new Chunk(String.valueOf(pageN)));
            }
        }

        if (after != null) {
            paragraph.addSpecial(after);
        }

        paragraph.setAlignment(alignment);

        return paragraph;
    }

    private Font getFont() {
        if (before != null) {
            return before.getFont();
        }

        if (after != null) {
            return after.getFont();
        }

        return null;
    }

    /**
     * Gets the alignment of this HeaderFooter.
     *
     * @return alignment
     */
    public int alignment() {
        return alignment;
    }
}