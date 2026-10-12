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
 * https://github.com/LibrePDF/OpenPDF
 */
package org.openpdf.text.pdf.parser;

public class MatchedPattern {

    private final String text;
    private final int startIndex;
    private final int endIndex;
    private final float leftX;
    private final float rightX;
    private final int page;
    private final float[] coordinates = new float[4];

    /**
     * Constructor to pair a strip of text with its bounding box coordinates inside a page. The coordinates system has
     * the origin (0, 0) in the lower left point of the page and uses PDF points as unit measure.
     *
     * @param text       original line containing the match
     * @param page       page the match was found on, one based
     * @param lineBox    bounding box of the whole line, as {llx, lly, urx, ury}
     * @param startIndex index of the first character of the match inside the text, inclusive
     * @param endIndex   index one past the last character of the match inside the text, exclusive
     * @param leftX      float x coordinate at which the match starts
     * @param rightX     float x coordinate at which the match ends
     */
    MatchedPattern(String text, int page, float[] lineBox, int startIndex, int endIndex,
            float leftX, float rightX) {
        this.text = text;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.leftX = leftX;
        this.rightX = rightX;
        this.page = page;
        System.arraycopy(lineBox, 0, coordinates, 0, coordinates.length);
    }

    public String getText() {
        return text;
    }

    /**
     * @return the matched substring of the line, or an empty string if the match indexes do not describe a range
     * within it
     */
    public String getMatchedText() {
        if (this.startIndex < 0 || this.endIndex > this.text.length() || this.startIndex > this.endIndex) {
            return "";
        }
        return text.substring(this.startIndex, this.endIndex);
    }

    public float[] getMatchedBBox() {
        return new float[]{this.leftX, this.coordinates[1], this.rightX, this.coordinates[3]};
    }

    public int getPage() {
        return page;
    }

    public float[] getCoordinates() {
        return coordinates;
    }

    public String printCoordinates() {
        return "[llx: " + coordinates[0] + ", lly: " + coordinates[1] + ", urx: " + coordinates[2] + ", ury: "
                + coordinates[3] + "]";
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Text: [")
                .append(this.text)
                .append("] - match: [")
                .append(this.startIndex)
                .append("::")
                .append(this.endIndex)
                .append("] - boundingBox: ")
                .append(this.printCoordinates())
                .append(" - page: [")
                .append(this.page)
                .append("]");
        return sb.toString();
    }

}
