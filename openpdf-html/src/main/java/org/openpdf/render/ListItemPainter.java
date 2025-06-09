/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci
 * Copyright (c) 2005 Wisconsin Court System
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
package org.openpdf.render;

import org.openpdf.css.constants.IdentValue;
import org.openpdf.css.style.CalculatedStyle;
import org.openpdf.extend.FSImage;

import static java.awt.RenderingHints.KEY_ANTIALIASING;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_DEFAULT;
import static java.awt.RenderingHints.VALUE_ANTIALIAS_ON;
import static java.util.Objects.requireNonNullElse;
import static org.openpdf.css.constants.CSSName.LIST_STYLE_TYPE;

/**
 * A utility class to paint list markers (all types).
 * @see MarkerData
 */
public class ListItemPainter {
    public static void paint(RenderingContext c, BlockBox box) {
        if (box.getMarkerData() == null) {
            return;
        }

        MarkerData markerData = box.getMarkerData();

        if (markerData.getImageMarker() != null) {
            drawImage(c, box, markerData);
        } else {
            CalculatedStyle style = box.getStyle();
            c.getOutputDevice().setColor(style.getColor());

            if (markerData.getGlyphMarker() != null) {
                drawGlyph(c, box, style);
            } else if (markerData.getTextMarker() != null) {
                drawText(c, box);
            }
        }
    }

    private static void drawImage(RenderingContext c, BlockBox box, MarkerData markerData) {
        MarkerData.ImageMarker marker = markerData.getImageMarker();
        FSImage img = marker.getImage();
        int x = getReferenceX(c, box);
        // FIXME: findbugs possible loss of precision, cf. int / (float)2
        x += -marker.getLayoutWidth() +
                (marker.getLayoutWidth() / 2 - img.getWidth() / 2);
        c.getOutputDevice().drawImage(img,
                x,
                getListItemCenterBaseline(c, box) - img.getHeight() / 2);
    }

    private static int getReferenceX(RenderingContext c, BlockBox box) {
        MarkerData markerData = box.getMarkerData();

        if (markerData.getReferenceLine() != null) {
            return markerData.getReferenceLine().getAbsX();
        } else {
            return box.getAbsX() + (int)box.getMargin(c).left();
        }
    }

    private static void drawGlyph(RenderingContext c, BlockBox box, CalculatedStyle style) {
        // save the old AntiAliasing setting, then force it on
        Object aa_key = c.getOutputDevice().getRenderingHint(KEY_ANTIALIASING);
        c.getOutputDevice().setRenderingHint(KEY_ANTIALIASING, VALUE_ANTIALIAS_ON);

        // calculations for bullets
        MarkerData.GlyphMarker marker = box.getMarkerData().getGlyphMarker();
        int x = getReferenceX(c, box) - marker.getLayoutWidth();
        int y = getListItemCenterBaseline(c, box) - marker.getDiameter() / 2;

        IdentValue listStyle = style.getIdent(LIST_STYLE_TYPE);
        if (listStyle == IdentValue.DISC) {
            c.getOutputDevice().fillOval(x, y, marker.getDiameter(), marker.getDiameter());
        } else if (listStyle == IdentValue.SQUARE) {
            c.getOutputDevice().fillRect(x, y, marker.getDiameter(), marker.getDiameter());
        } else if (listStyle == IdentValue.CIRCLE) {
            c.getOutputDevice().drawOval(x, y, marker.getDiameter(), marker.getDiameter());
        }

        // restore the old AntiAliasing setting
        c.getOutputDevice().setRenderingHint(KEY_ANTIALIASING, requireNonNullElse(aa_key, VALUE_ANTIALIAS_DEFAULT));
    }

    private static int getListItemCenterBaseline(final RenderingContext c, final BlockBox box) {
        return box.getAbsY()
                + getHeightOfFirstChild(box) / 2
                + (int) box.getMargin(c).top() / 2
                - (int) box.getMargin(c).bottom() / 2
                + (int) box.getPadding(c).top() / 2
                - (int) box.getPadding(c).bottom() / 2;
    }

    private static int getHeightOfFirstChild(final Box box) {
        if (box.getChildCount() > 1) {
            return getHeightOfFirstChild(box.getChild(0));
        } else {
            return box.getHeight();
        }
    }

    private static void drawText(RenderingContext c, BlockBox box) {
        MarkerData.TextMarker text = box.getMarkerData().getTextMarker();

        int x = getReferenceX(c, box) - text.getLayoutWidth();
        int y = getReferenceBaseline(box);

        c.getOutputDevice().setColor(box.getStyle().getColor());
        c.getOutputDevice().setFont(box.getStyle().getFSFont(c));
        c.getTextRenderer().drawString(c.getOutputDevice(), text.getText(), x, y);
    }

    private static int getReferenceBaseline(BlockBox box) {
        MarkerData markerData = box.getMarkerData();
        StrutMetrics strutMetrics = markerData.getStructMetrics();

        if (markerData.getReferenceLine() != null) {
            return markerData.getReferenceLine().getAbsY() + strutMetrics.getBaseline();
        } else {
            return box.getAbsY() + box.getTy() + strutMetrics.getBaseline();
        }
    }
}
