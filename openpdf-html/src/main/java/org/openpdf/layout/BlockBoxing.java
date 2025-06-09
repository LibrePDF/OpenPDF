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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.openpdf.layout;

import org.openpdf.css.constants.CSSName;
import org.openpdf.css.constants.IdentValue;
import org.openpdf.render.BlockBox;
import org.openpdf.render.Box;
import org.openpdf.render.LineBox;
import org.openpdf.render.PageBox;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.RandomAccess;

/**
 * Utility class for laying block content.  It is called when a block box
 * contains block level content.  {@link BoxBuilder} will have made sure that
 * the block we're working on will either contain only inline or block content.
 * If we're in a paged media environment, the various page break related
 * properties are also handled here.  If a rule is violated, the affected run
 * of boxes will be laid out again.  If the rule still cannot be satisfied,
 * the rule will be dropped.
 */
public class BlockBoxing {
    private static final int NO_PAGE_TRIM = -1;

    private BlockBoxing() {
    }

    public static void layoutContent(LayoutContext c, BlockBox block, int contentStart) {
        int offset = -1;

        List<Box> localChildren = block.getChildren();
        if (c.isPrint() && ! (localChildren instanceof RandomAccess)) {
            localChildren = new ArrayList<>(localChildren);
        }

        int childOffset = block.getHeight() + contentStart;

        RelayoutDataList relayoutDataList = null;
        if (c.isPrint()) {
            relayoutDataList = new RelayoutDataList(localChildren.size());
        }

        int pageCount = NO_PAGE_TRIM;
        BlockBox previousChildBox = null;
        for (Box localChild : localChildren) {
            BlockBox child = (BlockBox) localChild;
            offset++;

            RelayoutData relayoutData = null;

            boolean mayCheckKeepTogether = false;
            if (c.isPrint()) {
                relayoutData = relayoutDataList.get(offset);
                relayoutData.setLayoutState(c.copyStateForRelayout());
                relayoutData.setChildOffset(childOffset);
                pageCount = c.getRootLayer().getPages().size();

                child.setNeedPageClear(false);

                if ((child.getStyle().isAvoidPageBreakInside() || child.getStyle().isKeepWithInline())
                        && c.isMayCheckKeepTogether()) {
                    mayCheckKeepTogether = true;
                    c.setMayCheckKeepTogether(false);
                }
            }

            layoutBlockChild(
                    c, block, child, false, childOffset, NO_PAGE_TRIM,
                    relayoutData == null ? null : relayoutData.getLayoutState());

            if (c.isPrint()) {
                boolean needPageClear = child.isNeedPageClear();
                if (needPageClear || mayCheckKeepTogether) {
                    c.setMayCheckKeepTogether(mayCheckKeepTogether);
                    boolean tryToAvoidPageBreak = child.getStyle().isAvoidPageBreakInside() && child.crossesPageBreak(c);
                    boolean keepWithInline = child.isNeedsKeepWithInline(c);
                    if (tryToAvoidPageBreak || needPageClear || keepWithInline) {
                        c.restoreStateForRelayout(relayoutData.getLayoutState());
                        child.reset(c);
                        layoutBlockChild(
                                c, block, child, true, childOffset, pageCount, relayoutData.getLayoutState());

                        if (tryToAvoidPageBreak && child.crossesPageBreak(c) && !keepWithInline) {
                            c.restoreStateForRelayout(relayoutData.getLayoutState());
                            child.reset(c);
                            layoutBlockChild(
                                    c, block, child, false, childOffset, pageCount, relayoutData.getLayoutState());
                        }
                    }
                }
                c.getRootLayer().ensureHasPage(c, child);
            }

            Dimension relativeOffset = child.getRelativeOffset();
            if (relativeOffset == null) {
                childOffset = child.getY() + child.getHeight();
            } else {
                // Box will have been positioned by this point so calculate
                // relative to where it would have been if it hadn't been
                // moved
                childOffset = child.getY() - relativeOffset.height + child.getHeight();
            }

            if (childOffset > block.getHeight()) {
                block.setHeight(childOffset);
            }

            if (c.isPrint()) {
                if (child.getStyle().isForcePageBreakAfter()) {
                    block.forcePageBreakAfter(c, child.getStyle().getIdent(CSSName.PAGE_BREAK_AFTER));
                    childOffset = block.getHeight();
                }

                if (previousChildBox != null) {
                    relayoutDataList.markRun(offset, previousChildBox, child);
                }

                RelayoutRunResult runResult =
                        processPageBreakAvoidRun(
                                c, block, localChildren, offset, relayoutDataList, relayoutData);
                if (runResult.isChanged()) {
                    childOffset = runResult.getChildOffset();
                    if (childOffset > block.getHeight()) {
                        block.setHeight(childOffset);
                    }
                }
            }

            previousChildBox = child;
        }
    }

    private static RelayoutRunResult processPageBreakAvoidRun(final LayoutContext c, final BlockBox block,
                                                              List<Box> localChildren, int offset,
                                                              RelayoutDataList relayoutDataList, RelayoutData relayoutData) {
        RelayoutRunResult result = new RelayoutRunResult();
        if (offset > 0) {
            boolean mightNeedRelayout = false;
            int runEnd = -1;
            if (offset == localChildren.size() - 1 && relayoutData.isEndsRun()) {
                mightNeedRelayout = true;
                runEnd = offset;
            } else if (offset > 0) {
                RelayoutData previousRelayoutData = relayoutDataList.get(offset - 1);
                if (previousRelayoutData.isEndsRun()) {
                    mightNeedRelayout = true;
                    runEnd = offset - 1;
                }
            }
            if (mightNeedRelayout) {
                int runStart = relayoutDataList.getRunStart(runEnd);
                if ( isPageBreakBetweenChildBoxes(runStart, runEnd, c, block) ) {
                    result.setChanged();
                    block.resetChildren(c, runStart, offset);
                    result.setChildOffset(relayoutRun(c, localChildren, block,
                            relayoutDataList, runStart, offset, true));
                    if ( isPageBreakBetweenChildBoxes(runStart, runEnd, c, block) ) {
                        block.resetChildren(c, runStart, offset);
                        result.setChildOffset(relayoutRun(c, localChildren, block,
                                relayoutDataList, runStart, offset, false));
                    }
                }
            }
        }
        return result;
    }

    private static boolean isPageBreakBetweenChildBoxes(int runStart, int runEnd, LayoutContext c, BlockBox block) {
        for ( int i = runStart; i < runEnd; i++ ) {
            Box prevChild = block.getChild(i);
            Box nextChild = block.getChild(i+1);
            // if nextChild is made of several lines, then only the first line
            // is relevant for "page-break-before: avoid".
            Box nextLine = getFirstLine(nextChild) == null ? nextChild : getFirstLine(nextChild);
            int prevChildEnd = prevChild.getAbsY() + prevChild.getHeight();
            int nextLineEnd = nextLine.getAbsY() + nextLine.getHeight();
            if ( c.getRootLayer().crossesPageBreak(c, prevChildEnd, nextLineEnd) ) {
                return true;
            }
        }
        return false;
    }

    private static LineBox getFirstLine(Box box) {
        for ( Box child = box; child.getChildCount()>0; child = child.getChild(0) ) {
            if ( child instanceof LineBox ) {
                return (LineBox) child;
            }
        }
        return null;
    }

    private static int relayoutRun(
            LayoutContext c, List<Box> localChildren, BlockBox block,
            RelayoutDataList relayoutDataList, int start, int end, boolean onNewPage) {
        int childOffset = relayoutDataList.get(start).getChildOffset();

        if (onNewPage) {
            Box startBox = localChildren.get(start);
            PageBox startPageBox = c.getRootLayer().getFirstPage(c, startBox);
            childOffset += startPageBox.getBottom() - startBox.getAbsY();
        }

        // reset height of parent as it is used for Y-setting of children
        block.setHeight(childOffset);


        for (int i = start; i <= end; i++) {
            BlockBox child = (BlockBox) localChildren.get(i);

            RelayoutData relayoutData = relayoutDataList.get(i);

            int pageCount = c.getRootLayer().getPages().size();

            //TODO:handle run-ins. For now, treat them as blocks

            c.restoreStateForRelayout(relayoutData.getLayoutState());
            relayoutData.setChildOffset(childOffset);
            boolean mayCheckKeepTogether = false;
            if ((child.getStyle().isAvoidPageBreakInside() || child.getStyle().isKeepWithInline())
                    && c.isMayCheckKeepTogether()) {
                mayCheckKeepTogether = true;
                c.setMayCheckKeepTogether(false);
            }
            layoutBlockChild(
                    c, block, child, false, childOffset, NO_PAGE_TRIM, relayoutData.getLayoutState());

            if (mayCheckKeepTogether) {
                c.setMayCheckKeepTogether(true);
                boolean tryToAvoidPageBreak =
                    child.getStyle().isAvoidPageBreakInside() && child.crossesPageBreak(c);
                boolean needPageClear = child.isNeedPageClear();
                boolean keepWithInline = child.isNeedsKeepWithInline(c);
                if (tryToAvoidPageBreak || needPageClear || keepWithInline) {
                    c.restoreStateForRelayout(relayoutData.getLayoutState());
                    child.reset(c);
                    layoutBlockChild(
                            c, block, child, true, childOffset, pageCount, relayoutData.getLayoutState());

                    if (tryToAvoidPageBreak && child.crossesPageBreak(c) && ! keepWithInline) {
                        c.restoreStateForRelayout(relayoutData.getLayoutState());
                        child.reset(c);
                        layoutBlockChild(
                                c, block, child, false, childOffset, pageCount, relayoutData.getLayoutState());
                    }
                }
            }

            c.getRootLayer().ensureHasPage(c, child);

            Dimension relativeOffset = child.getRelativeOffset();
            if (relativeOffset == null) {
                childOffset = child.getY() + child.getHeight();
            } else {
                childOffset = child.getY() - relativeOffset.height + child.getHeight();
            }

            if (childOffset > block.getHeight()) {
                block.setHeight(childOffset);
            }

            if (child.getStyle().isForcePageBreakAfter()) {
                block.forcePageBreakAfter(c, child.getStyle().getIdent(CSSName.PAGE_BREAK_AFTER));
                childOffset = block.getHeight();
            }
        }

        return childOffset;
    }

    private static void layoutBlockChild(
            LayoutContext c, BlockBox parent, BlockBox child,
            boolean needPageClear, int childOffset, int trimmedPageCount, LayoutState layoutState) {
        layoutBlockChild0(c, parent, child, needPageClear, childOffset, trimmedPageCount);
        BreakAtLineContext bContext = child.calcBreakAtLineContext(c);
        if (bContext != null) {
            c.setBreakAtLineContext(bContext);
            c.restoreStateForRelayout(layoutState);
            child.reset(c);
            layoutBlockChild0(c, parent, child, needPageClear, childOffset, trimmedPageCount);
            c.setBreakAtLineContext(null);
        }
    }

    private static void layoutBlockChild0(LayoutContext c, BlockBox parent, BlockBox child,
            boolean needPageClear, int childOffset, int trimmedPageCount) {
        child.setNeedPageClear(needPageClear);

        child.initStaticPos(c, parent, childOffset);

        child.initContainingLayer(c);
        child.calcCanvasLocation();

        c.translate(0, childOffset);
        repositionBox(c, child, trimmedPageCount);
        child.layout(c);
        c.translate(-child.getX(), -child.getY());
    }

    private static void repositionBox(LayoutContext c, BlockBox child, int trimmedPageCount) {
        boolean moved = false;
        if (child.getStyle().isRelative()) {
            Dimension delta = child.positionRelative(c);
            c.translate(delta.width, delta.height);
            moved = true;
        }
        if (c.isPrint()) {
            boolean pageClear = child.isNeedPageClear() ||
                                    child.getStyle().isForcePageBreakBefore();
            boolean needNewPageContext = child.checkPageContext(c);

            if (needNewPageContext && trimmedPageCount != NO_PAGE_TRIM) {
                c.getRootLayer().trimPageCount(trimmedPageCount);
            }

            if (pageClear || needNewPageContext) {
                int delta = child.forcePageBreakBefore(
                        c,
                        child.getStyle().getIdent(CSSName.PAGE_BREAK_BEFORE),
                        needNewPageContext);
                c.translate(0, delta);
                moved = true;
                child.setNeedPageClear(false);
            }
        }
        if (moved) {
            child.calcCanvasLocation();
        }
    }

    private static class RelayoutDataList {
        private final List<RelayoutData> _hints;

        private RelayoutDataList(int size) {
            _hints = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                _hints.add(new RelayoutData());
            }
        }

        RelayoutData get(int index) {
            return _hints.get(index);
        }

        private void markRun(int offset, BlockBox previous, BlockBox current) {
            RelayoutData previousData = get(offset - 1);
            RelayoutData currentData = get(offset);

            IdentValue previousAfter =
                    previous.getStyle().getIdent(CSSName.PAGE_BREAK_AFTER);
            IdentValue currentBefore =
                    current.getStyle().getIdent(CSSName.PAGE_BREAK_BEFORE);

            if ((previousAfter == IdentValue.AVOID && currentBefore == IdentValue.AUTO) ||
                    (previousAfter == IdentValue.AUTO && currentBefore == IdentValue.AVOID) ||
                    (previousAfter == IdentValue.AVOID && currentBefore == IdentValue.AVOID)) {
                if (! previousData.isInRun()) {
                    previousData.setStartsRun();
                }
                previousData.setInRun();
                currentData.setInRun();

                if (offset == _hints.size() - 1) {
                    currentData.setEndsRun();
                }
            } else {
                if (previousData.isInRun()) {
                    previousData.setEndsRun();
                }
            }
        }

        int getRunStart(int runEnd) {
            int offset = runEnd;
            RelayoutData current = get(offset);
            if (! current.isEndsRun()) {
                throw new RuntimeException("Not the end of a run");
            }
            while (! current.isStartsRun()) {
                current = get(--offset);
            }
            return offset;
        }
    }

    private static class RelayoutRunResult {
        private boolean _changed;
        private int _childOffset;

        boolean isChanged() {
            return _changed;
        }

        private void setChanged() {
            _changed = true;
        }

        int getChildOffset() {
            return _childOffset;
        }

        private void setChildOffset(int childOffset) {
            _childOffset = childOffset;
        }
    }

    private static class RelayoutData {
        private LayoutState _layoutState;

        private boolean _startsRun;
        private boolean _endsRun;
        private boolean _inRun;

        private int _childOffset;

        boolean isEndsRun() {
            return _endsRun;
        }

        private void setEndsRun() {
            _endsRun = true;
        }

        boolean isInRun() {
            return _inRun;
        }

        private void setInRun() {
            _inRun = true;
        }

        LayoutState getLayoutState() {
            return _layoutState;
        }

        private void setLayoutState(LayoutState layoutState) {
            _layoutState = layoutState;
        }

        boolean isStartsRun() {
            return _startsRun;
        }

        private void setStartsRun() {
            _startsRun = true;
        }

        int getChildOffset() {
            return _childOffset;
        }

        private void setChildOffset(int childOffset) {
            _childOffset = childOffset;
        }
    }
}
