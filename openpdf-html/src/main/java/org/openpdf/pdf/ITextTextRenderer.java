/*
 * {{{ header & license
 * Copyright (c) 2006 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.openpdf.pdf;

import org.openpdf.text.pdf.BaseFont;
import org.openpdf.extend.FSGlyphVector;
import org.openpdf.extend.FontContext;
import org.openpdf.extend.OutputDevice;
import org.openpdf.extend.TextRenderer;
import org.openpdf.render.FSFont;
import org.openpdf.render.FSFontMetrics;
import org.openpdf.render.JustificationInfo;

import java.awt.*;

public class ITextTextRenderer implements TextRenderer {
    private static final float TEXT_MEASURING_DELTA = 0.01f;

    @Override
    public void setup(FontContext context) {
    }

    @Override
    public void drawString(OutputDevice outputDevice, String string, float x, float y) {
        ((ITextOutputDevice)outputDevice).drawString(string, x, y, null);
    }

    @Override
    public void drawString(
            OutputDevice outputDevice, String string, float x, float y, JustificationInfo info) {
        ((ITextOutputDevice)outputDevice).drawString(string, x, y, info);
    }

    @Override
    public FSFontMetrics getFSFontMetrics(FontContext context, FSFont font, String string) {
        FontDescription description = ((ITextFSFont)font).getFontDescription();
        BaseFont bf = description.getFont();
        float size = font.getSize2D();
        float strikethroughThickness = description.getYStrikeoutSize() != 0 ?
                description.getYStrikeoutSize() / 1000f * size :
                size / 12.0f;

        return new ITextFSFontMetrics(
                bf.getFontDescriptor(BaseFont.BBOXURY, size),
                -bf.getFontDescriptor(BaseFont.BBOXLLY, size),
                -description.getYStrikeoutPosition() / 1000f * size,
                strikethroughThickness,
                -description.getUnderlinePosition() / 1000f * size,
                description.getUnderlineThickness() / 1000f * size
        );
    }

    @Override
    public int getWidth(FontContext context, FSFont font, String string) {
        BaseFont bf = ((ITextFSFont)font).getFontDescription().getFont();
        float result = bf.getWidthPoint(string, font.getSize2D());
        if (result - Math.floor(result) < TEXT_MEASURING_DELTA) {
            return (int)result;
        } else {
            return (int)Math.ceil(result);
        }
    }

    @Override
    public void setFontScale(float scale) {
    }

    @Override
    public float getFontScale() {
        return 1.0f;
    }

    @Override
    public void setSmoothingThreshold(float fontsize) {
    }

    @Override
    public Rectangle getGlyphBounds(OutputDevice outputDevice, FSFont font, FSGlyphVector fsGlyphVector, int index, float x, float y) {
        throw new UnsupportedOperationException("Unsupported operation: getGlyphBounds");
    }

    @Override
    public float[] getGlyphPositions(OutputDevice outputDevice, FSFont font, FSGlyphVector fsGlyphVector) {
        throw new UnsupportedOperationException("Unsupported operation: getGlyphPositions");
    }

    @Override
    public FSGlyphVector getGlyphVector(OutputDevice outputDevice, FSFont font, String string) {
        throw new UnsupportedOperationException("Unsupported operation: getGlyphVector");
    }

    @Override
    public void drawGlyphVector(OutputDevice outputDevice, FSGlyphVector vector, float x, float y) {
        throw new UnsupportedOperationException("Unsupported operation: drawGlyphVector");
    }
}
