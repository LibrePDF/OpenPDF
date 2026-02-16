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
    private final int page;
    private final float[] coordinates = new float[4];

    /**
     * Constructor to pair a strip of text with its bounding box coordinates inside a page.
     * The coordinates system has the origin (0, 0) in the lower left point of the page
     * and uses PDF points as unit measure.
     *
     * @param text          string
     * @param page          int
     * @param llx           float lower left x coordinate
     * @param lly           float lower left y coordinate
     * @param urx           float upper right x coordinate
     * @param ury           float upper right y coordinate
     */
    MatchedPattern(String text, int page, float llx, float lly, float urx, float ury) {
        this.text = text;
        this.page = page;
        coordinates[0] = llx;
        coordinates[1] = lly;
        coordinates[2] = urx;
        coordinates[3] = ury;
    }

    public String getText() {
        return text;
    }

    public int getPage() {
        return page;
    }

    public float[] getCoordinates() {
        return coordinates;
    }

    @Override
    public String toString() {
        String[] c = new String[4];
        for(int i = 0; i < 4; i++) {
            c[i] = String.valueOf(coordinates[i]);
        }
        return "[" + String.join(", ", c) + "]";
    }

}
