/*
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 */
package com.sun.pdfview.font;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;

import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PDFShapeCmd;

/**
* A single glyph in a stream of PDF text, which knows how to write itself
* onto a PDF command stream
*/
public class PDFGlyph {
    /** the character code of this glyph */
    private final char src;
    /** the name of this glyph */
    private final String name;
    /** the advance from this glyph */
    private final Point2D advance;
    /** the shape represented by this glyph (for all fonts but type 3) */
    private GeneralPath shape;
    /** the PDFPage storing this glyph's commands (for type 3 fonts) */
    private PDFPage page;

    /** Creates a new instance of PDFGlyph based on a shape */
    public PDFGlyph(char src, String name, GeneralPath shape, Point2D.Float advance) {
        this.shape = shape;
        this.advance = advance;
        this.src = src;
        this.name = name;
    }

    /** Creates a new instance of PDFGlyph based on a page */
    public PDFGlyph(char src, String name, PDFPage page, Point2D advance) {
        this.page = page;
        this.advance = advance;
        this.src = src;
        this.name = name;
    }

    /** Get the character code of this glyph */
    public char getChar() {
        return this.src;
    }

    /** Get the name of this glyph */
    public String getName() {
        return this.name;
    }

    /** Get the shape of this glyph */
    public GeneralPath getShape() {
        return this.shape;
    }

    /** Get the PDFPage for a type3 font glyph */
    public PDFPage getPage() {
        return this.page;
    }

    /** Add commands for this glyph to a page */
    public Point2D addCommands(PDFPage cmds, AffineTransform transform, int mode) {
        if (this.shape != null) {
            GeneralPath outline = (GeneralPath) this.shape.createTransformedShape(transform);
            cmds.addCommand(new PDFShapeCmd(outline, mode, false));
        } else if (this.page != null) {
            cmds.addCommands(this.page, transform);
        }
        return this.advance;
    }

    public Point2D getAdvance() {
        return advance;
    }

    @Override
    public String toString() {
        StringBuffer str = new StringBuffer();
        str.append(this.name);
        return str.toString();
    }
}