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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.openpdf.extend;

import org.jspecify.annotations.Nullable;
import org.openpdf.css.parser.FSColor;
import org.openpdf.css.style.CalculatedStyle;
import org.openpdf.css.style.derived.BorderPropertySet;
import org.openpdf.css.style.derived.FSLinearGradient;
import org.openpdf.render.BlockBox;
import org.openpdf.render.Box;
import org.openpdf.render.FSFont;
import org.openpdf.render.InlineLayoutBox;
import org.openpdf.render.InlineText;
import org.openpdf.render.LineBox;
import org.openpdf.render.RenderingContext;
import org.openpdf.render.TextDecoration;

import java.awt.*;

public interface OutputDevice {
    void drawText(RenderingContext c, InlineText inlineText);
    void drawSelection(RenderingContext c, InlineText inlineText);

    void drawTextDecoration(RenderingContext c, LineBox lineBox);
    void drawTextDecoration(
            RenderingContext c, InlineLayoutBox iB, TextDecoration decoration);

    void paintBorder(RenderingContext c, Box box);
    void paintBorder(RenderingContext c, CalculatedStyle style,
                     Rectangle edge, int sides);
    void paintCollapsedBorder(
            RenderingContext c, BorderPropertySet border, Rectangle bounds, int side);

    void paintBackground(RenderingContext c, Box box);
    void paintBackground(
            RenderingContext c, CalculatedStyle style,
            Rectangle bounds, Rectangle bgImageContainer,
            BorderPropertySet border);

    void paintReplacedElement(RenderingContext c, BlockBox box);

    void drawDebugOutline(RenderingContext c, Box box, FSColor color);

    void setFont(FSFont font);

    void setColor(FSColor color);
    void setOpacity(float opacity);

    void drawRect(int x, int y, int width, int height);
    void drawOval(int x, int y, int width, int height);

    void drawBorderLine(Shape bounds, int side, int width, boolean solid);

    void drawImage(FSImage image, int x, int y);
    void drawLinearGradient(FSLinearGradient gradient, int x, int y, int width, int height);

    void draw(Shape s);
    void fill(Shape s);
    void fillRect(int x, int y, int width, int height);
    void fillOval(int x, int y, int width, int height);

    void clip(Shape s);
    @Nullable
    Shape getClip();
    void setClip(Shape s);

    void translate(double tx, double ty);

    void setStroke(Stroke s);
    Stroke getStroke();

    @Nullable
    Object getRenderingHint(RenderingHints.Key key);

    void setRenderingHint(RenderingHints.Key key, Object value);

    boolean isSupportsSelection();

    boolean isSupportsCMYKColors();
}
