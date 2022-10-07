/*
 * $Id: Element.java 3672 2009-02-01 15:32:09Z blowagie $
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

/**
 * Interface for a text element.
 * <P>
 * Remark: I looked at the interface javax.swing.text.Element, but I decided to write my own text-classes.
 *
 * @see Anchor
 * @see Cell
 * @see Chapter
 * @see Chunk
 * @see Header
 * @see Image
 * @see Jpeg
 * @see List
 * @see ListItem
 * @see Meta
 * @see Paragraph
 * @see Phrase
 * @see Rectangle
 * @see Row
 * @see Section
 * @see Table
 */

public interface Element {

    // static membervariables (meta information)

    /** This is a possible type of <CODE>Element</CODE>. */
    public static final int HEADER = 0;

    /** This is a possible type of <CODE>Element</CODE>. */
    public static final int TITLE = 1;

    /** This is a possible type of <CODE>Element</CODE>. */
    public static final int SUBJECT = 2;

    /** This is a possible type of <CODE>Element</CODE>. */
    public static final int KEYWORDS = 3;

    /** This is a possible type of <CODE>Element </CODE>. */
    public static final int AUTHOR = 4;

    /** This is a possible type of <CODE>Element </CODE>. */
    public static final int PRODUCER = 5;

    /** This is a possible type of <CODE>Element </CODE>. */
    public static final int CREATIONDATE = 6;

    /** This is a possible type of <CODE>Element </CODE>. */
    public static final int CREATOR = 7;
    
    /** This is a possible type of <CODE>Element </CIDE>. */
    public static final int MODIFICATIONDATE = 8;

    // static membervariables (content)

    /** This is a possible type of <CODE>Element</CODE>. */
    public static final int CHUNK = 10;

    /** This is a possible type of <CODE>Element</CODE>. */
    public static final int PHRASE = 11;

    /** This is a possible type of <CODE>Element</CODE>. */
    public static final int PARAGRAPH = 12;

    /** This is a possible type of <CODE>Element</CODE> */
    public static final int SECTION = 13;

    /** This is a possible type of <CODE>Element</CODE> */
    public static final int LIST = 14;

    /** This is a possible type of <CODE>Element</CODE> */
    public static final int LISTITEM = 15;

    /** This is a possible type of <CODE>Element</CODE> */
    public static final int CHAPTER = 16;

    /** This is a possible type of <CODE>Element</CODE> */
    public static final int ANCHOR = 17;

    // static membervariables (tables)

    /** This is a possible type of <CODE>Element</CODE>. */
    public static final int CELL = 20;

    /** This is a possible type of <CODE>Element</CODE>. */
    public static final int ROW = 21;

    /** This is a possible type of <CODE>Element</CODE>. */
    public static final int TABLE = 22;

    /** This is a possible type of <CODE>Element</CODE>. */
    public static final int PTABLE = 23;

    // static membervariables (annotations)

    /** This is a possible type of <CODE>Element</CODE>. */
    public static final int ANNOTATION = 29;

    // static membervariables (geometric figures)

    /** This is a possible type of <CODE>Element</CODE>. */
    public static final int RECTANGLE = 30;

    /** This is a possible type of <CODE>Element</CODE>. */
    public static final int JPEG = 32;

    /** This is a possible type of <CODE>Element</CODE>. */
    public static final int JPEG2000 = 33;

    /** This is a possible type of <CODE>Element</CODE>. */
    public static final int IMGRAW = 34;

    /** This is a possible type of <CODE>Element</CODE>. */
    public static final int IMGTEMPLATE = 35;

    /**
     * This is a possible type of <CODE>Element</CODE>.
     * @since    2.1.5
     */
    public static final int JBIG2 = 36;

    /** This is a possible type of <CODE>Element</CODE>. */
    public static final int MULTI_COLUMN_TEXT = 40;
    
    /** This is a possible type of <CODE>Element</CODE>. */
    public static final int MARKED = 50;

    /** This is a possible type of <CODE>Element</CODE>.
     * @since 2.1.2
     */
    public static final int YMARK = 55;

    /** This is a possible type of <CODE>Element</CODE>.
     */
    public static final int FOOTNOTE = 56;

    // static membervariables (alignment)

    /**
     * A possible value for paragraph alignment. This specifies that the text is
     * aligned to the left indent and extra whitespace should be placed on the
     * right.
     */
    public static final int ALIGN_UNDEFINED = -1;

    /**
     * A possible value for paragraph alignment. This specifies that the text is
     * aligned to the left indent and extra whitespace should be placed on the
     * right.
     */
    public static final int ALIGN_LEFT = 0;

    /**
     * A possible value for paragraph alignment. This specifies that the text is
     * aligned to the center and extra whitespace should be placed equally on
     * the left and right.
     */
    public static final int ALIGN_CENTER = 1;

    /**
     * A possible value for paragraph alignment. This specifies that the text is
     * aligned to the right indent and extra whitespace should be placed on the
     * left.
     */
    public static final int ALIGN_RIGHT = 2;

    /**
     * A possible value for paragraph alignment. This specifies that extra
     * whitespace should be spread out through the rows of the paragraph with
     * the text lined up with the left and right indent except on the last line
     * which should be aligned to the left.
     */
    public static final int ALIGN_JUSTIFIED = 3;

    /**
     * A possible value for vertical alignment.
     */

    public static final int ALIGN_TOP = 4;

    /**
     * A possible value for vertical alignment.
     */

    public static final int ALIGN_MIDDLE = 5;

    /**
     * A possible value for vertical alignment.
     */

    public static final int ALIGN_BOTTOM = 6;

    /**
     * A possible value for vertical alignment.
     */
    public static final int ALIGN_BASELINE = 7;

    /**
     * Does the same as ALIGN_JUSTIFIED but the last line is also spread out.
     */
    public static final int ALIGN_JUSTIFIED_ALL = 8;

    // static member variables for CCITT compression

    /**
     * Pure two-dimensional encoding (Group 4)
     */
    public static final int CCITTG4 = 0x100;

    /**
     * Pure one-dimensional encoding (Group 3, 1-D)
     */
    public static final int CCITTG3_1D = 0x101;

    /**
     * Mixed one- and two-dimensional encoding (Group 3, 2-D)
     */
    public static final int CCITTG3_2D = 0x102;

    /**
     * A flag indicating whether 1-bits are to be interpreted as black pixels
     * and 0-bits as white pixels,
     */
    public static final int CCITT_BLACKIS1 = 1;

    /**
     * A flag indicating whether the filter expects extra 0-bits before each
     * encoded line so that the line begins on a byte boundary.
     */
    public static final int CCITT_ENCODEDBYTEALIGN = 2;

    /**
     * A flag indicating whether end-of-line bit patterns are required to be
     * present in the encoding.
     */
    public static final int CCITT_ENDOFLINE = 4;

    /**
     * A flag indicating whether the filter expects the encoded data to be
     * terminated by an end-of-block pattern, overriding the Rows parameter. The
     * use of this flag will set the key /EndOfBlock to false.
     */
    public static final int CCITT_ENDOFBLOCK = 8;

    // methods

    /**
     * Processes the element by adding it (or the different parts) to an <CODE>
     * ElementListener</CODE>.
     * 
     * @param listener
     *            an <CODE>ElementListener</CODE>
     * @return <CODE>true</CODE> if the element was processed successfully
     */

    boolean process(ElementListener listener);

    /**
     * Gets the type of the text element.
     * 
     * @return a type
     */

    public int type();

    /**
     * Checks if this element is a content object.
     * If not, it's a metadata object.
     * @since    iText 2.0.8
     * @return    true if this is a 'content' element; false if this is a 'metadata' element
     */

    public boolean isContent();
    
    /**
     * Checks if this element is nestable.
     * @since    iText 2.0.8
     * @return    true if this element can be nested inside other elements.
     */

    public boolean isNestable();
    
    /**
     * Gets all the chunks in this element.
     * 
     * @return an <CODE>ArrayList</CODE>
     */

    public ArrayList<Element> getChunks();

    /**
     * Gets the content of the text element.
     * 
     * @return a type
     */

    @Override
    public String toString();
}