/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci
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
package org.openpdf.extend;

import org.openpdf.render.FSFont;
import org.openpdf.render.FSFontMetrics;
import org.openpdf.render.JustificationInfo;

import java.awt.*;

public interface TextRenderer {
    void setup(FontContext context);

    void drawString(OutputDevice outputDevice, String string, float x, float y);
    void drawString(
            OutputDevice outputDevice, String string, float x, float y, JustificationInfo info);

    void drawGlyphVector(OutputDevice outputDevice, FSGlyphVector vector, float x, float y);

    FSGlyphVector getGlyphVector(OutputDevice outputDevice, FSFont font, String string);

    float[] getGlyphPositions(OutputDevice outputDevice, FSFont font, FSGlyphVector fsGlyphVector);
    Rectangle getGlyphBounds(OutputDevice outputDevice, FSFont font, FSGlyphVector fsGlyphVector, int index, float x, float y);

    FSFontMetrics getFSFontMetrics(
            FontContext context, FSFont font, String string);

    int getWidth(FontContext context, FSFont font, String string);

    void setFontScale(float scale);

    float getFontScale();

    /**
     * Set the smoothing threshold. This is a font size above which
     * all text will be anti-aliased. Text below this size will not be anti-aliased.
     * Set to -1 for no antialiasing.
     * Set to 0 for all antialiasing.
     * Else, set to the threshold font size. does not take font scaling
     * into account.
     */
    void setSmoothingThreshold(float fontsize);
}
