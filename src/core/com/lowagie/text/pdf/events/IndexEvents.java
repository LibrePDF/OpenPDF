/*
 * Copyright 2005 by Michael Niedermair.
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
 * the Initial Developer are Copyright (C) 1999-2005 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000-2005 by Paulo Soares. All Rights Reserved.
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Class for an index.
 * 
 * @author Michael Niedermair
 */
public class IndexEvents extends PdfPageEventHelper {

    /**
     * keeps the indextag with the pagenumber
     */
    private Map indextag = new TreeMap();

    /**
     * All the text that is passed to this event, gets registered in the indexentry.
     * 
     * @see com.lowagie.text.pdf.PdfPageEventHelper#onGenericTag(
     *      com.lowagie.text.pdf.PdfWriter, com.lowagie.text.Document,
     *      com.lowagie.text.Rectangle, java.lang.String)
     */
    public void onGenericTag(PdfWriter writer, Document document,
            Rectangle rect, String text) {
        indextag.put(text, new Integer(writer.getPageNumber()));
    }

    // --------------------------------------------------------------------
    /**
     * indexcounter
     */
    private long indexcounter = 0;

    /**
     * the list for the index entry
     */
    private List indexentry = new ArrayList();

    /**
     * Create an index entry.
     *
     * @param text  The text for the Chunk.
     * @param in1   The first level.
     * @param in2   The second level.
     * @param in3   The third level.
     * @return Returns the Chunk.
     */
    public Chunk create(final String text, final String in1, final String in2,
            final String in3) {

        Chunk chunk = new Chunk(text);
        String tag = "idx_" + (indexcounter++);
        chunk.setGenericTag(tag);
        chunk.setLocalDestination(tag);
        Entry entry = new Entry(in1, in2, in3, tag);
        indexentry.add(entry);
        return chunk;
    }

    /**
     * Create an index entry.
     *
     * @param text  The text for the Chunk.
     * @param in1   The first level.
     * @return Returns the Chunk.
     */
    public Chunk create(final String text, final String in1) {
        return create(text, in1, "", "");
    }

    /**
     * Create an index entry.
     *
     * @param text  The text for the Chunk.
     * @param in1   The first level.
     * @param in2   The second level.
     * @return Returns the Chunk.
     */
    public Chunk create(final String text, final String in1, final String in2) {
        return create(text, in1, in2, "");
    }

    /**
     * Create an index entry.
     *
     * @param text  The text.
     * @param in1   The first level.
     * @param in2   The second level.
     * @param in3   The third level.
     */
    public void create(final Chunk text, final String in1, final String in2,
            final String in3) {

        String tag = "idx_" + (indexcounter++);
        text.setGenericTag(tag);
        text.setLocalDestination(tag);
        Entry entry = new Entry(in1, in2, in3, tag);
        indexentry.add(entry);
    }

    /**
     * Create an index entry.
     *
     * @param text  The text.
     * @param in1   The first level.
     */
    public void create(final Chunk text, final String in1) {
        create(text, in1, "", "");
    }

    /**
     * Create an index entry.
     *
     * @param text  The text.
     * @param in1   The first level.
     * @param in2   The second level.
     */
    public void create(final Chunk text, final String in1, final String in2) {
        create(text, in1, in2, "");
    }

    /**
     * Comparator for sorting the index
     */
    private Comparator comparator = new Comparator() {

        public int compare(Object arg0, Object arg1) {
            Entry en1 = (Entry) arg0;
            Entry en2 = (Entry) arg1;

            int rt = 0;
            if (en1.getIn1() != null && en2.getIn1() != null) {
                if ((rt = en1.getIn1().compareToIgnoreCase(en2.getIn1())) == 0) {
                    // in1 equals
                    if (en1.getIn2() != null && en2.getIn2() != null) {
                        if ((rt = en1.getIn2()
                                .compareToIgnoreCase(en2.getIn2())) == 0) {
                            // in2 equals
                            if (en1.getIn3() != null && en2.getIn3() != null) {
                                rt = en1.getIn3().compareToIgnoreCase(
                                        en2.getIn3());
                            }
                        }
                    }
                }
            }
            return rt;
        }
    };

    /**
     * Set the comparator.
     * @param aComparator The comparator to set.
     */
    public void setComparator(Comparator aComparator) {
        comparator = aComparator;
    }

    /**
     * Returns the sorted list with the entries and the collected page numbers.
     * @return Returns the sorted list with the entries and the collected page numbers.
     */
    public List getSortedEntries() {

        Map grouped = new HashMap();

        for (int i = 0; i < indexentry.size(); i++) {
            Entry e = (Entry) indexentry.get(i);
            String key = e.getKey();

            Entry master = (Entry) grouped.get(key);
            if (master != null) {
                master.addPageNumberAndTag(e.getPageNumber(), e.getTag());
            } else {
                e.addPageNumberAndTag(e.getPageNumber(), e.getTag());
                grouped.put(key, e);
            }
        }

        // copy to a list and sort it
        List sorted = new ArrayList(grouped.values());
        Collections.sort(sorted, comparator);
        return sorted;
    }

    // --------------------------------------------------------------------
    /**
     * Class for an index entry.
     * <p>
     * In the first step, only in1, in2,in3 and tag are used.
     * After the collections of the index entries, pagenumbers are used.
     * </p>
     */
    public class Entry {

        /**
         * first level
         */
        private String in1;

        /**
         * second level
         */
        private String in2;

        /**
         * third level
         */
        private String in3;

        /**
         * the tag
         */
        private String tag;

        /**
         * the list of all page numbers.
         */
        private List pagenumbers = new ArrayList();

        /**
         * the list of all tags.
         */
        private List tags = new ArrayList();

        /**
         * Create a new object.
         * @param aIn1   The first level.
         * @param aIn2   The second level.
         * @param aIn3   The third level.
         * @param aTag   The tag.
         */
        public Entry(final String aIn1, final String aIn2, final String aIn3,
                final String aTag) {
            in1 = aIn1;
            in2 = aIn2;
            in3 = aIn3;
            tag = aTag;
        }

        /**
         * Returns the in1.
         * @return Returns the in1.
         */
        public String getIn1() {
            return in1;
        }

        /**
         * Returns the in2.
         * @return Returns the in2.
         */
        public String getIn2() {
            return in2;
        }

        /**
         * Returns the in3.
         * @return Returns the in3.
         */
        public String getIn3() {
            return in3;
        }

        /**
         * Returns the tag.
         * @return Returns the tag.
         */
        public String getTag() {
            return tag;
        }

        /**
         * Returns the pagenumber for this entry.
         * @return Returns the pagenumber for this entry.
         */
        public int getPageNumber() {
            int rt = -1;
            Integer i = (Integer) indextag.get(tag);
            if (i != null) {
                rt = i.intValue();
            }
            return rt;
        }

        /**
         * Add a pagenumber.
         * @param number    The page number.
         * @param tag
         */
        public void addPageNumberAndTag(final int number, final String tag) {
            pagenumbers.add(new Integer(number));
            tags.add(tag);
        }

        /**
         * Returns the key for the map-entry.
         * @return Returns the key for the map-entry.
         */
        public String getKey() {
            return in1 + "!" + in2 + "!" + in3;
        }

        /**
         * Returns the pagenumbers.
         * @return Returns the pagenumbers.
         */
        public List getPagenumbers() {
            return pagenumbers;
        }

        /**
         * Returns the tags.
         * @return Returns the tags.
         */
        public List getTags() {
            return tags;
        }

        /**
         * print the entry (only for test)
         * @return the toString implementation of the entry
         */
        public String toString() {
            StringBuffer buf = new StringBuffer();
            buf.append(in1).append(' ');
            buf.append(in2).append(' ');
            buf.append(in3).append(' ');
            for (int i = 0; i < pagenumbers.size(); i++) {
                buf.append(pagenumbers.get(i)).append(' ');
            }
            return buf.toString();
        }
    }
}