/*
 * {{{ header & license
 * Copyright (c) 2005 Wisconsin Court System
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
package org.openpdf.layout;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
import org.openpdf.css.style.CssContext;
import org.openpdf.render.BlockBox;
import org.openpdf.render.Box;
import org.openpdf.render.LineBox;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.openpdf.layout.FloatManager.Direction.LEFT;
import static org.openpdf.layout.FloatManager.Direction.RIGHT;

/**
 * A class that manages all floated boxes in a given block formatting context.
 * It is responsible for positioning floats and calculating clearance for
 * non-floated (block) boxes.
 */
public class FloatManager {
    enum Direction {LEFT, RIGHT}

    private final List<BoxOffset> _leftFloats = new ArrayList<>();
    private final List<BoxOffset> _rightFloats = new ArrayList<>();
    private final Box _master;

    public FloatManager(Box master) {
        _master = master;
    }

    public void floatBox(LayoutContext c, Layer layer, BlockFormattingContext bfc, BlockBox box) {
        if (box.getStyle().isFloatedLeft()) {
            position(c, bfc, box, LEFT);
            save(box, layer, bfc, LEFT);
        } else if (box.getStyle().isFloatedRight()) {
            position(c, bfc, box, RIGHT);
            save(box, layer, bfc, RIGHT);
        }
    }

    public void clear(CssContext cssCtx, BlockFormattingContext bfc, Box box) {
        if (box.getStyle().isClearLeft()) {
            moveClear(cssCtx, bfc, box, getFloats(LEFT));
        }
        if (box.getStyle().isClearRight()) {
            moveClear(cssCtx, bfc, box, getFloats(RIGHT));
        }
    }

    private void save(BlockBox current, Layer layer, BlockFormattingContext bfc, Direction direction) {
        Point p = bfc.getOffset();
        getFloats(direction).add(new BoxOffset(current, p.x, p.y));
        layer.addFloat(current);
        current.getFloatedBoxData().setManager(this);

        current.calcCanvasLocation();
        current.calcChildLocations();
    }

    private void position(CssContext cssCtx, BlockFormattingContext bfc,
                          BlockBox current, Direction direction) {
        moveAllTheWayOver(current, direction);

        alignToLastOpposingFloat(cssCtx, bfc, current, direction);
        alignToLastFloat(cssCtx, bfc, current, direction);

        if (!fitsInContainingBlock(current) ||
                overlaps(cssCtx, bfc, current, getFloats(direction))) {
            moveAllTheWayOver(current, direction);
            moveFloatBelow(cssCtx, bfc, current, getFloats(direction));
        }

        if (overlaps(cssCtx, bfc, current, getOpposingFloats(direction))) {
            moveAllTheWayOver(current, direction);
            moveFloatBelow(cssCtx, bfc, current, getFloats(direction));
            moveFloatBelow(cssCtx, bfc, current, getOpposingFloats(direction));
        }

        if (current.getStyle().isCleared()) {
            if (current.getStyle().isClearLeft() && direction == LEFT) {
                moveAllTheWayOver(current, LEFT);
            } else if (current.getStyle().isClearRight() && direction == RIGHT) {
                moveAllTheWayOver(current, RIGHT);
            }
            moveFloatBelow(cssCtx, bfc, current, getFloats(direction));
        }
    }

    private List<BoxOffset> getFloats(Direction direction) {
        return direction == LEFT ? _leftFloats : _rightFloats;
    }

    private List<BoxOffset> getOpposingFloats(Direction direction) {
        return direction == LEFT ? _rightFloats : _leftFloats;
    }

    private void alignToLastFloat(CssContext cssCtx,
                                  BlockFormattingContext bfc, BlockBox current, Direction direction) {

        List<BoxOffset> floats = getFloats(direction);
        if (!floats.isEmpty()) {
            Point offset = bfc.getOffset();
            BoxOffset lastOffset = floats.get(floats.size() - 1);
            BlockBox last = lastOffset.box();

            Rectangle currentBounds = current.getMarginEdge(cssCtx, -offset.x, -offset.y);

            Rectangle lastBounds = last.getMarginEdge(cssCtx, -lastOffset.x(), -lastOffset.y());

            boolean moveOver = false;

            if (currentBounds.y < lastBounds.y) {
                currentBounds.translate(0, lastBounds.y - currentBounds.y);
                moveOver = true;
            }

            if (currentBounds.y >= lastBounds.y && currentBounds.y < lastBounds.y + lastBounds.height) {
                moveOver = true;
            }

            if (moveOver) {
                if (direction == LEFT) {
                    currentBounds.x = lastBounds.x + last.getWidth();
                } else if (direction == RIGHT) {
                    currentBounds.x = lastBounds.x - current.getWidth();
                }

                currentBounds.translate(offset.x, offset.y);

                current.setX(currentBounds.x);
                current.setY(currentBounds.y);
            }
        }
    }

    private void alignToLastOpposingFloat(CssContext cssCtx,
                                          BlockFormattingContext bfc, BlockBox current, Direction direction) {

        List<BoxOffset> floats = getOpposingFloats(direction);
        if (!floats.isEmpty()) {
            Point offset = bfc.getOffset();
            BoxOffset lastOffset = floats.get(floats.size() - 1);

            Rectangle currentBounds = current.getMarginEdge(cssCtx, -offset.x, -offset.y);

            Rectangle lastBounds = lastOffset.box().getMarginEdge(cssCtx,
                    -lastOffset.x(), -lastOffset.y());

            if (currentBounds.y < lastBounds.y) {
                currentBounds.translate(0, lastBounds.y - currentBounds.y);

                currentBounds.translate(offset.x, offset.y);

                current.setY(currentBounds.y);
            }
        }
    }

    private void moveAllTheWayOver(BlockBox current, Direction direction) {
        if (direction == LEFT) {
            current.setX(0);
        } else if (direction == RIGHT) {
            current.setX(current.getContainingBlock().getContentWidth() - current.getWidth());
        }
    }

    private boolean fitsInContainingBlock(BlockBox current) {
        return current.getX() >= 0 &&
                (current.getX() + current.getWidth()) <= current.getContainingBlock().getContentWidth();
    }

    private int findLowestY(CssContext cssCtx, List<BoxOffset> floats) {
        int result = 0;

        for (BoxOffset floater : floats) {
            Rectangle bounds = floater.box().getMarginEdge(
                    cssCtx, -floater.x(), -floater.y());
            if (bounds.y + bounds.height > result) {
                result = bounds.y + bounds.height;
            }
        }

        return result;
    }

    public int getClearDelta(CssContext cssCtx, int bfcRelativeY) {
        int lowestLeftY = findLowestY(cssCtx, getFloats(LEFT));
        int lowestRightY = findLowestY(cssCtx, getFloats(RIGHT));

        int lowestY = Math.max(lowestLeftY, lowestRightY);

        return lowestY - bfcRelativeY;
    }

    private boolean overlaps(CssContext cssCtx, BlockFormattingContext bfc,
                             BlockBox current, List<BoxOffset> floats) {
        Point offset = bfc.getOffset();
        Rectangle bounds = current.getMarginEdge(cssCtx, -offset.x, -offset.y);

        for (BoxOffset floater : floats) {
            Rectangle floaterBounds = floater.box().getMarginEdge(cssCtx,
                    -floater.x(), -floater.y());

            if (floaterBounds.intersects(bounds)) {
                return true;
            }
        }

        return false;
    }

    private void moveFloatBelow(CssContext cssCtx, BlockFormattingContext bfc,
                                   Box current, List<BoxOffset> floats) {
        if (floats.isEmpty()) {
            return;
        }

        Point offset = bfc.getOffset();
        int boxY = current.getY() - offset.y;
        int floatY = findLowestY(cssCtx, floats);

        if (floatY - boxY > 0) {
            current.setY(current.getY() + (floatY - boxY));
        }
    }

    private void moveClear(CssContext cssCtx, BlockFormattingContext bfc,
                           Box current, List<BoxOffset> floats) {
        if (floats.isEmpty()) {
            return;
        }

        // Translate from box coords to BFC coords
        Point offset = bfc.getOffset();
        Rectangle bounds = current.getBorderEdge(
                current.getX()-offset.x, current.getY()-offset.y, cssCtx);

        int y = findLowestY(cssCtx, floats);

        if (bounds.y < y) {
            // Translate bottom margin edge of lowest float back to box coords
            // and set the box's border edge to that value
            bounds.y = y;

            bounds.translate(offset.x, offset.y);

            current.setY(bounds.y - (int)current.getMargin(cssCtx).top());
        }
    }

    public void removeFloat(BlockBox floater) {
        removeFloat(floater, getFloats(LEFT));
        removeFloat(floater, getFloats(RIGHT));
    }

    private void removeFloat(BlockBox floater, List<BoxOffset> floats) {
        for (Iterator<BoxOffset> i = floats.iterator(); i.hasNext();) {
            BoxOffset boxOffset = i.next();
            if (boxOffset.box().equals(floater)) {
                i.remove();
                floater.getFloatedBoxData().setManager(null);
            }
        }
    }

    public void calcFloatLocations() {
        calcFloatLocations(getFloats(LEFT));
        calcFloatLocations(getFloats(RIGHT));
    }

    private void calcFloatLocations(List<BoxOffset> floats) {
        for (BoxOffset boxOffset : floats) {
            boxOffset.box().calcCanvasLocation();
            boxOffset.box().calcChildLocations();
        }
    }

    private void applyLineHeightHack(CssContext cssCtx, Box line, Rectangle bounds) {
        // this is a hack to deal with lines w/o width or height. is this valid?
        // possibly, since the line doesn't know how long it should be until it's already
        // done float adjustments
        if (line.getHeight() == 0) {
            bounds.height = (int)line.getStyle().getLineHeight(cssCtx);
        }
    }

    public int getNextLineBoxDelta(CssContext cssCtx, BlockFormattingContext bfc,
            LineBox line, int containingBlockContentWidth) {
        BoxDistance left = getFloatDistance(cssCtx, bfc, line, containingBlockContentWidth, _leftFloats, LEFT);
        BoxDistance right = getFloatDistance(cssCtx, bfc, line, containingBlockContentWidth, _rightFloats, RIGHT);

        int leftDelta;
        int rightDelta;

        if (left.box() != null) {
            leftDelta = calcDelta(cssCtx, line, left);
        } else {
            leftDelta = 0;
        }

        if (right.box() != null) {
            rightDelta = calcDelta(cssCtx, line, right);
        } else {
            rightDelta = 0;
        }

        return Math.max(leftDelta, rightDelta);
    }

    private int calcDelta(CssContext cssCtx, LineBox line, BoxDistance boxDistance) {
        BlockBox floated = boxDistance.box();
        Rectangle rect = floated.getBorderEdge(floated.getAbsX(), floated.getAbsY(), cssCtx);
        int bottom = rect.y + rect.height;
        return bottom - line.getAbsY();
    }

    public int getLeftFloatDistance(CssContext cssCtx, BlockFormattingContext bfc,
            LineBox line, int containingBlockContentWidth) {
        return getFloatDistance(cssCtx, bfc, line, containingBlockContentWidth, _leftFloats, LEFT).distance();
    }

    public int getRightFloatDistance(CssContext cssCtx, BlockFormattingContext bfc,
            LineBox line, int containingBlockContentWidth) {
        return getFloatDistance(cssCtx, bfc, line, containingBlockContentWidth, _rightFloats, RIGHT).distance();
    }

    private BoxDistance getFloatDistance(CssContext cssCtx, BlockFormattingContext bfc,
                                 LineBox line, int containingBlockContentWidth,
                                 List<BoxOffset> floatsList, Direction direction) {
        if (floatsList.isEmpty()) {
            return new BoxDistance(null, 0);
        }

        Point offset = bfc.getOffset();
        Rectangle lineBounds = line.getMarginEdge(cssCtx, -offset.x, -offset.y);
        lineBounds.width = containingBlockContentWidth;

        int farthestOver = direction == LEFT ? lineBounds.x : lineBounds.x + lineBounds.width;

        applyLineHeightHack(cssCtx, line, lineBounds);
        BlockBox farthestOverBox = null;
        for (BoxOffset floater : floatsList) {
            Rectangle fr = floater.box().getMarginEdge(cssCtx, -floater.x(), -floater.y());
            if (lineBounds.intersects(fr)) {
                if (direction == LEFT && fr.x + fr.width > farthestOver) {
                    farthestOver = fr.x + fr.width;
                } else if (direction == RIGHT && fr.x < farthestOver) {
                    farthestOver = fr.x;
                }
                farthestOverBox = floater.box();
            }
        }

        if (direction == LEFT) {
            return new BoxDistance(farthestOverBox, farthestOver - lineBounds.x);
        } else {
            return new BoxDistance(farthestOverBox,lineBounds.x + lineBounds.width - farthestOver);
        }
    }

    public Box getMaster() {
        return _master;
    }

    public Point getOffset(BlockBox floater) {
        // FIXME inefficient (but probably doesn't matter)
        return getOffset(floater,
                floater.getStyle().isFloatedLeft() ? getFloats(LEFT) : getFloats(RIGHT));
    }

    @Nullable
    @CheckReturnValue
    private Point getOffset(BlockBox floater, List<BoxOffset> floats) {
        for (BoxOffset boxOffset : floats) {
            BlockBox box = boxOffset.box();

            if (box.equals(floater)) {
                return new Point(boxOffset.x(), boxOffset.y());
            }
        }

        return null;
    }

    private void performFloatOperation(FloatOperation op, List<BoxOffset> floats) {
        for (BoxOffset boxOffset : floats) {
            BlockBox box = boxOffset.box();

            box.setAbsX(box.getX() + getMaster().getAbsX() - boxOffset.x());
            box.setAbsY(box.getY() + getMaster().getAbsY() - boxOffset.y());

            op.operate(box);
        }
    }

    public void performFloatOperation(FloatOperation op) {
        performFloatOperation(op, getFloats(LEFT));
        performFloatOperation(op, getFloats(RIGHT));
    }

    private record BoxOffset(BlockBox box, int x, int y) {
    }

    private record BoxDistance(@Nullable BlockBox box, int distance) {
    }

    public interface FloatOperation {
        void operate(Box floater);
    }
}

