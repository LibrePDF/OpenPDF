/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci, Torbjoern Gannholm
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
package org.openpdf.layout;

import org.jspecify.annotations.Nullable;
import org.openpdf.css.constants.CSSName;
import org.openpdf.render.BlockBox;
import org.openpdf.render.LineBox;
import org.openpdf.render.MarkerData;

import java.util.List;

/**
 * Contains utility methods to layout floated and absolute content.
 *
 * XXX Could/should be folded into BlockBox
 */
public class LayoutUtil {

    public static void layoutAbsolute(
            LayoutContext c, LineBox currentLine, BlockBox box) {
        MarkerData markerData = c.getCurrentMarkerData();
        c.setCurrentMarkerData(null);

        if (box.getStyle().isFixed()) {
            box.setContainingBlock(c.getRootLayer().getMaster().getContainingBlock());
        } else {
            box.setContainingBlock(c.getLayer().getMaster());
        }
        box.setStaticEquivalent(currentLine);

        // If printing, don't lay out until we know where it's going
        if (!c.isPrint()) {
            box.layout(c);
        } else {
            c.pushLayer(box);
            c.getLayer().setRequiresLayout(true);
            c.popLayer();
        }

        c.setCurrentMarkerData(markerData);
    }

    public static FloatLayoutResult layoutFloated(
            final LayoutContext c, LineBox currentLine, BlockBox block,
            int avail, @Nullable List<FloatLayoutResult> pendingFloats) {
        MarkerData markerData = c.getCurrentMarkerData();
        c.setCurrentMarkerData(null);

        block.setContainingBlock(currentLine.getParent());
        block.setContainingLayer(currentLine.getContainingLayer());
        block.setStaticEquivalent(currentLine);

        if (pendingFloats != null) {
            block.setY(currentLine.getY() + block.getFloatedBoxData().getMarginFromSibling());
        } else {
            block.setY(currentLine.getY() + currentLine.getHeight());
        }

        block.calcInitialFloatedCanvasLocation(c);

        int initialY = block.getY();

        block.layout(c);

        c.getBlockFormattingContext().floatBox(c, block);

        boolean pending = false;
        if (pendingFloats != null &&
                (!pendingFloats.isEmpty() || block.getWidth() > avail) &&
                currentLine.isContainsContent()) {
            block.reset(c);
            pending = true;
        } else {
            if (c.isPrint()) {
                positionFloatOnPage(c, currentLine, block, initialY != block.getY());
                c.getRootLayer().ensureHasPage(c, block);
            }
        }

        c.setCurrentMarkerData(markerData);

        return new FloatLayoutResult(pending, block);
    }

    private static void positionFloatOnPage(
            final LayoutContext c, LineBox currentLine, BlockBox block,
            boolean movedVertically) {
        if (block.getStyle().isForcePageBreakBefore()) {
            block.forcePageBreakBefore(c, block.getStyle().getIdent(CSSName.PAGE_BREAK_BEFORE), false);
            block.calcCanvasLocation();
            resetAndFloatBlock(c, currentLine, block);
        } else if (block.getStyle().isAvoidPageBreakInside() && block.crossesPageBreak(c)) {
            int clearDelta = block.forcePageBreakBefore(c, block.getStyle().getIdent(CSSName.PAGE_BREAK_BEFORE), false);

            block.calcCanvasLocation();
            resetAndFloatBlock(c, currentLine, block);

            if (block.crossesPageBreak(c)) {
                block.setY(block.getY() - clearDelta);
                block.calcCanvasLocation();
                resetAndFloatBlock(c, currentLine, block);
            }
        } else if (movedVertically) {
            resetAndFloatBlock(c, currentLine, block);
        }
    }

    private static void resetAndFloatBlock(final LayoutContext c, LineBox currentLine, BlockBox block) {
        block.reset(c);
        block.setContainingLayer(currentLine.getContainingLayer());
        block.layout(c);
        c.getBlockFormattingContext().floatBox(c, block);
    }
}
