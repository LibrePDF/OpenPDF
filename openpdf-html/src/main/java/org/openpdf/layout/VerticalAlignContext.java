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

import org.jspecify.annotations.Nullable;
import org.openpdf.css.constants.CSSName;
import org.openpdf.css.constants.IdentValue;
import org.openpdf.render.Box;
import org.openpdf.render.InlineLayoutBox;

import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNullElse;

/**
 * This class performs the real work of vertically positioning inline boxes
 * within a line (i.e. implementing the vertical-align property).  Because
 * of the requirements of vertical-align: top/bottom, a {@code VerticalAlignContext}
 * is actually a tree of {@code VerticalAlignContext} objects which all
 * must be taken into consideration when aligning content.
 */
public class VerticalAlignContext {
    private final List<InlineBoxMeasurements> _measurements = new ArrayList<>();

    private int _inlineTop;
    private boolean _inlineTopSet = false;

    private int _inlineBottom;
    private boolean _inlineBottomSet = false;

    private int _paintingTop;
    private boolean _paintingTopSet = false;

    private int _paintingBottom;
    private boolean _paintingBottomSet = false;

    private final List<ChildContextData> _children = new ArrayList<>();

    @Nullable
    private final VerticalAlignContext _parent;

    public VerticalAlignContext(VerticalAlignContext parent) {
        _parent = parent;
    }

    public VerticalAlignContext(InlineBoxMeasurements initialMeasurements) {
        _parent = null;
        _measurements.add(initialMeasurements);
    }

    private void moveTrackedValues(int ty) {
        if (_inlineTopSet) {
            _inlineTop += ty;
        }

        if (_inlineBottomSet) {
            _inlineBottom += ty;
        }

        if (_paintingTopSet) {
            _paintingTop += ty;
        }

        if (_paintingBottomSet) {
            _paintingBottom += ty;
        }
    }

    public int getInlineBottom() {
        return _inlineBottom;
    }

    public int getInlineTop() {
        return _inlineTop;
    }

    public void updateInlineTop(int inlineTop) {
        if (! _inlineTopSet || inlineTop < _inlineTop) {
            _inlineTop = inlineTop;
            _inlineTopSet = true;
        }
    }

    public void updatePaintingTop(int paintingTop) {
        if (! _paintingTopSet || paintingTop < _paintingTop) {
            _paintingTop = paintingTop;
            _paintingTopSet = true;
        }
    }

    public void updateInlineBottom(int inlineBottom) {
        if (! _inlineBottomSet || inlineBottom > _inlineBottom) {
            _inlineBottom = inlineBottom;
            _inlineBottomSet = true;
        }
    }

    public void updatePaintingBottom(int paintingBottom) {
        if (! _paintingBottomSet || paintingBottom > _paintingBottom) {
            _paintingBottom = paintingBottom;
            _paintingBottomSet = true;
        }
    }

    public int getLineBoxHeight() {
        return _inlineBottom - _inlineTop;
    }

    public void pushMeasurements(InlineBoxMeasurements measurements) {
        _measurements.add(measurements);

        updateInlineTop(measurements.getInlineTop());
        updateInlineBottom(measurements.getInlineBottom());

        updatePaintingTop(measurements.getPaintingTop());
        updatePaintingBottom(measurements.getPaintingBottom());
    }

    public InlineBoxMeasurements getParentMeasurements() {
        return _measurements.get(_measurements.size()-1);
    }

    public void popMeasurements() {
        _measurements.remove(_measurements.size()-1);
    }

    public int getPaintingBottom() {
        return _paintingBottom;
    }

    public int getPaintingTop() {
        return _paintingTop;
    }

    public VerticalAlignContext createChild(Box root) {
        VerticalAlignContext vaRoot = getRoot();
        InlineBoxMeasurements initial = vaRoot._measurements.get(0);

        VerticalAlignContext result = new VerticalAlignContext(vaRoot);
        result.pushMeasurements(initial);

        vaRoot._children.add(new ChildContextData(root, result));
        return result;
    }

    private List<ChildContextData> getChildren() {
        return _children;
    }

    @Nullable
    public VerticalAlignContext getParent() {
        return _parent;
    }

    private VerticalAlignContext getRoot() {
        return requireNonNullElse(_parent, this);
    }

    private void merge(VerticalAlignContext context) {
        updateInlineBottom(context.getInlineBottom());
        updateInlineTop(context.getInlineTop());

        updatePaintingBottom(context.getPaintingBottom());
        updatePaintingTop(context.getPaintingTop());
    }

    public void alignChildren() {
        List<ChildContextData> children = getChildren();
        for (ChildContextData data : children) {
            data.align();
            merge(data.getVerticalAlignContext());
        }
    }

    private static final class ChildContextData {
        private final Box _root;
        private final VerticalAlignContext _verticalAlignContext;

        public ChildContextData(Box root, VerticalAlignContext vaContext) {
            _root = root;
            _verticalAlignContext = vaContext;
        }

        public VerticalAlignContext getVerticalAlignContext() {
            return _verticalAlignContext;
        }

        private void moveContextContents(int ty) {
            moveInlineContents(_root, ty);
        }

        private void moveInlineContents(Box box, int ty) {
            if (canBeMoved(box)) {
                box.setY(box.getY() + ty);
                if (box instanceof InlineLayoutBox iB) {
                    for (int i = 0; i < iB.getInlineChildCount(); i++) {
                        Object child = iB.getInlineChild(i);
                        if (child instanceof Box) {
                            moveInlineContents((Box)child, ty);
                        }
                    }
                }
            }
        }

        private boolean canBeMoved(Box box) {
            IdentValue vAlign = box.getStyle().getIdent(CSSName.VERTICAL_ALIGN);
            return box == _root ||
                ! (vAlign == IdentValue.TOP || vAlign == IdentValue.BOTTOM);
        }

        public void align() {
            IdentValue vAlign = _root.getStyle().getIdent(CSSName.VERTICAL_ALIGN);
            final int delta;
            if (vAlign == IdentValue.TOP) {
                delta = _verticalAlignContext.getRoot().getInlineTop() -
                    _verticalAlignContext.getInlineTop();
            } else if (vAlign == IdentValue.BOTTOM) {
                delta = _verticalAlignContext.getRoot().getInlineBottom() -
                    _verticalAlignContext.getInlineBottom();
            } else {
                throw new RuntimeException("internal error");
            }

            _verticalAlignContext.moveTrackedValues(delta);
            moveContextContents(delta);
        }
    }
}
