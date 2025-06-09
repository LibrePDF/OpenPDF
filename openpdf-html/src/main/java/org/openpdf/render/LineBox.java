/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci
 * Copyright (c) 2006, 2007 Wisconsin Court System
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

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;
import org.openpdf.css.constants.CSSName;
import org.openpdf.css.constants.IdentValue;
import org.openpdf.css.parser.FSRGBColor;
import org.openpdf.css.style.CalculatedStyle;
import org.openpdf.css.style.CssContext;
import org.openpdf.layout.BoxCollector;
import org.openpdf.layout.InlineBoxing;
import org.openpdf.layout.InlinePaintable;
import org.openpdf.layout.Layer;
import org.openpdf.layout.LayoutContext;
import org.openpdf.layout.PaintingInfo;
import org.openpdf.util.XRRuntimeException;

import java.awt.*;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.lineSeparator;
import static java.util.Collections.emptyList;
import static org.openpdf.css.constants.CSSName.LETTER_SPACING;
import static org.openpdf.css.constants.IdentValue.NORMAL;
import static org.openpdf.render.Box.Dump.RENDER;

/**
 * A line box contains a single line of text (or other inline content).  It
 * is created during layout.  It also tracks floated and absolute content
 * added while laying out the line.
 */
public class LineBox extends Box implements InlinePaintable {
    private static final float JUSTIFY_NON_SPACE_SHARE = 0.20f;
    private static final float JUSTIFY_SPACE_SHARE = 1 - JUSTIFY_NON_SPACE_SHARE;

    private boolean _endsOnNL;
    private boolean _containsContent;
    private boolean _containsBlockLevelContent;

    @Nullable
    private FloatDistances _floatDistances;

    @Nullable
    private List<TextDecoration> _textDecorations;

    private int _paintingTop;
    private int _paintingHeight;

    @Nullable
    private List<Box> _nonFlowContent;

    @Nullable
    private MarkerData _markerData;

    private boolean _containsDynamicFunction;

    private int _contentStart;

    private int _baseline;

    @Nullable
    private JustificationInfo _justificationInfo;

    public LineBox(@Nullable Box parent, @Nullable CalculatedStyle style) {
        super(parent, style);
    }

    @Override
    public String dump(LayoutContext c, String indent, Dump which) {
        if (which != RENDER) {
            throw new IllegalArgumentException(String.format("Unsupported which: %s (expected: %s)", which, RENDER));
        }

        StringBuilder result = new StringBuilder(indent);
        result.append(this);
        result.append('\n');

        dumpBoxes(c, indent, getNonFlowContent(), RENDER, result);
        if (!getNonFlowContent().isEmpty()) {
            result.append('\n');
        }
        dumpBoxes(c, indent, getChildren(), RENDER, result);

        return result.toString();
    }

    @Override
    public Rectangle getMarginEdge(CssContext cssCtx, int tx, int ty) {
        Rectangle result = new Rectangle(getX(), getY(), getContentWidth(), getHeight());
        result.translate(tx, ty);
        return result;
    }

    @Override
    public void paintInline(RenderingContext c) {
        if (! getParent().getStyle().isVisible()) {
            return;
        }

        if (isContainsDynamicFunction()) {
            lookForDynamicFunctions(c);
            int totalLineWidth = InlineBoxing.positionHorizontally(c, this, 0);
            setContentWidth(totalLineWidth);
            calcChildLocations();
            align(true);
            calcPaintingInfo(c, false);
        }

        if (_textDecorations != null) {
            c.getOutputDevice().drawTextDecoration(c, this);
        }

        if (c.debugDrawLineBoxes()) {
            c.getOutputDevice().drawDebugOutline(c, this, FSRGBColor.GREEN);
        }
    }

    private void lookForDynamicFunctions(RenderingContext c) {
        if (getChildCount() > 0) {
            for (int i = 0; i < getChildCount(); i++) {
                Box b = getChild(i);
                if (b instanceof InlineLayoutBox) {
                    ((InlineLayoutBox)b).lookForDynamicFunctions(c);
                }
            }
        }
    }

    public boolean isFirstLine() {
        Box parent = getParent();
        return parent != null && parent.getChildCount() > 0 && parent.getChild(0) == this;
    }

    public void prunePendingInlineBoxes() {
        if (getChildCount() > 0) {
            for (int i = getChildCount() - 1; i >= 0; i--) {
                Box b = getChild(i);
                if (! (b instanceof InlineLayoutBox iB)) {
                    break;
                }
                iB.prunePending();
                if (iB.isPending()) {
                    removeChild(i);
                }
            }
        }
    }

    public boolean isContainsContent() {
        return _containsContent;
    }

    public void setContainsContent(boolean containsContent) {
        _containsContent = containsContent;
    }

    public boolean isEndsOnNL() {
        return _endsOnNL;
    }

    public void setEndsOnNL(boolean endsOnNL) {
        _endsOnNL = endsOnNL;
    }

    public void align(boolean dynamic) {
        IdentValue align = getParent().getStyle().getIdent(CSSName.TEXT_ALIGN);

        int calcX = 0;

        if (align == IdentValue.LEFT || align == IdentValue.JUSTIFY) {
            int floatDistance = getFloatDistances().leftFloatDistance();
            calcX = getContentStart() + floatDistance;
            if (align == IdentValue.JUSTIFY && dynamic) {
                justify();
            }
        } else if (align == IdentValue.CENTER) {
            int leftFloatDistance = getFloatDistances().leftFloatDistance();
            int rightFloatDistance = getFloatDistances().rightFloatDistance();

            int midpoint = leftFloatDistance +
                (getParent().getContentWidth() - leftFloatDistance - rightFloatDistance) / 2;

            calcX = midpoint - (getContentWidth() + getContentStart()) / 2;
        } else if (align == IdentValue.RIGHT) {
            int floatDistance = getFloatDistances().rightFloatDistance();
            calcX = getParent().getContentWidth() - floatDistance - getContentWidth();
        }

        if (calcX != getX()) {
            setX(calcX);
            calcCanvasLocation();
            calcChildLocations();
        }
    }

    public void justify() {
        if (! isLastLineWithContent()) {
            int leftFloatDistance = getFloatDistances().leftFloatDistance();
            int rightFloatDistance = getFloatDistances().rightFloatDistance();

            int available = getParent().getContentWidth() -
                leftFloatDistance - rightFloatDistance - getContentStart();

            if (available > getContentWidth()) {
                int toAdd = available - getContentWidth();

                CharCounts counts = countJustifiableChars();

                JustificationInfo info = !getParent().getStyle().isIdent(LETTER_SPACING, NORMAL) ?
                        new JustificationInfo(0.0f, (float) toAdd / counts.getSpaceCount()) :
                        justificationInfo(counts, toAdd);

                adjustChildren(info);
                setJustificationInfo(info);
            }
        }
    }

    private static JustificationInfo justificationInfo(CharCounts counts, int toAdd) {
        float nonSpaceAdjust = counts.getNonSpaceCount() > 1 ?
                toAdd * JUSTIFY_NON_SPACE_SHARE / (counts.getNonSpaceCount() - 1) :
                0.0f;

        float spaceAdjust = counts.getSpaceCount() > 0 ?
                toAdd * JUSTIFY_SPACE_SHARE / counts.getSpaceCount() :
                0.0f;

        return new JustificationInfo(nonSpaceAdjust, spaceAdjust);
    }

    private void adjustChildren(JustificationInfo info) {
        float adjust = 0.0f;
        for (Box b : getChildren()) {
            b.setX(b.getX() + Math.round(adjust));

            if (b instanceof InlineLayoutBox) {
                adjust += ((InlineLayoutBox) b).adjustHorizontalPosition(info, adjust);
            }
        }

        calcChildLocations();
    }

    private boolean isLastLineWithContent() {
        LineBox current = (LineBox)getNextSibling();
        if (!_endsOnNL) {
            while (current != null) {
                if (current.isContainsContent()) {
                    return false;
                } else {
                    current = (LineBox)current.getNextSibling();
                }
            }
        }
        return true;
    }

    private CharCounts countJustifiableChars() {
        CharCounts result = new CharCounts();

        for (Box b : getChildren()) {
            if (b instanceof InlineLayoutBox) {
                ((InlineLayoutBox) b).countJustifiableChars(result);
            }
        }

        return result;
    }

    public FloatDistances getFloatDistances() {
        return _floatDistances;
    }

    public void setFloatDistances(@Nullable FloatDistances floatDistances) {
        _floatDistances = floatDistances;
    }

    public boolean isContainsBlockLevelContent() {
        return _containsBlockLevelContent;
    }

    public void setContainsBlockLevelContent(boolean containsBlockLevelContent) {
        _containsBlockLevelContent = containsBlockLevelContent;
    }

    @Override
    public boolean intersects(CssContext cssCtx, Shape clip) {
        return clip == null || (intersectsLine(cssCtx, clip) ||
            (isContainsBlockLevelContent() && intersectsInlineBlocks(cssCtx, clip)));
    }

    private boolean intersectsLine(CssContext cssCtx, Shape clip) {
        Rectangle result = getPaintingClipEdge(cssCtx);
        return clip.intersects(result);
    }

    @Override
    public Rectangle getPaintingClipEdge(CssContext cssCtx) {
        Box parent = getParent();
        if (parent.getStyle().isIdent(
                CSSName.FS_TEXT_DECORATION_EXTENT, IdentValue.BLOCK) ||
                    getJustificationInfo() != null) {
            return new Rectangle(
                    getAbsX(), getAbsY() + _paintingTop,
                    parent.getAbsX() + parent.getTx() + parent.getContentWidth() - getAbsX(),
                    _paintingHeight);
        } else {
            return new Rectangle(
                    getAbsX(), getAbsY() + _paintingTop, getContentWidth(), _paintingHeight);
        }
    }

    private boolean intersectsInlineBlocks(CssContext cssCtx, Shape clip) {
        for (int i = 0; i < getChildCount(); i++) {
            Box child = getChild(i);
            if (child instanceof InlineLayoutBox) {
                boolean possibleResult = ((InlineLayoutBox)child).intersectsInlineBlocks(
                        cssCtx, clip);
                if (possibleResult) {
                    return true;
                }
            } else {
                BoxCollector collector = new BoxCollector();
                if (collector.intersectsAny(cssCtx, clip, child)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Nullable
    @CheckReturnValue
    public List<TextDecoration> getTextDecorations() {
        return _textDecorations;
    }

    public void setTextDecorations(List<TextDecoration> textDecorations) {
        _textDecorations = textDecorations;
    }

    public int getPaintingHeight() {
        return _paintingHeight;
    }

    public void setPaintingHeight(int paintingHeight) {
        _paintingHeight = paintingHeight;
    }

    public int getPaintingTop() {
        return _paintingTop;
    }

    public void setPaintingTop(int paintingTop) {
        _paintingTop = paintingTop;
    }


    public void addAllChildren(List<Box> list, Layer layer) {
        for (int i = 0; i < getChildCount(); i++) {
            Box child = getChild(i);
            if (getContainingLayer() == layer) {
                list.add(child);
                if (child instanceof InlineLayoutBox) {
                    ((InlineLayoutBox)child).addAllChildren(list, layer);
                }
            }
        }
    }

    @CheckReturnValue
    public List<Box> getNonFlowContent() {
        return _nonFlowContent == null ? emptyList() : _nonFlowContent;
    }

    public void addNonFlowContent(BlockBox box) {
        if (_nonFlowContent == null) {
            _nonFlowContent = new ArrayList<>();
        }

        _nonFlowContent.add(box);
    }

    @Override
    public void reset(LayoutContext c) {
        for (int i = 0; i < getNonFlowContent().size(); i++) {
            Box content = getNonFlowContent().get(i);
            content.reset(c);
        }
        if (_markerData != null) {
            _markerData.restorePreviousReferenceLine(this);
        }
        super.reset(c);
    }

    @Override
    public void calcCanvasLocation() {
        Box parent = getParent();
        if (parent == null) {
            throw new XRRuntimeException("calcCanvasLocation() called with no parent");
        }
        setAbsX(parent.getAbsX() + parent.getTx() + getX());
        setAbsY(parent.getAbsY() + parent.getTy() + getY());
    }

    @Override
    public void calcChildLocations() {
        super.calcChildLocations();

        // Update absolute boxes too.  Not necessary most of the time, but
        // it doesn't hurt (revisit this)
        for (int i = 0; i < getNonFlowContent().size(); i++) {
            Box content = getNonFlowContent().get(i);
            if (content.getStyle().isAbsolute()) {
                content.calcCanvasLocation();
                content.calcChildLocations();
            }
        }
    }

    @Nullable
    @CheckReturnValue
    public MarkerData getMarkerData() {
        return _markerData;
    }

    public void setMarkerData(MarkerData markerData) {
        _markerData = markerData;
    }

    public boolean isContainsDynamicFunction() {
        return _containsDynamicFunction;
    }

    public void setContainsDynamicFunction(boolean containsPageCounter) {
        _containsDynamicFunction |= containsPageCounter;
    }

    public int getContentStart() {
        return _contentStart;
    }

    public void setContentStart(int contentOffset) {
        _contentStart = contentOffset;
    }

    @Nullable
    @CheckReturnValue
    public InlineText findTrailingText() {
        if (getChildCount() == 0) {
            return null;
        }

        for (int offset = getChildCount() - 1; offset >= 0; offset--) {
            Box child = getChild(offset);
            if (child instanceof InlineLayoutBox) {
                InlineText result = ((InlineLayoutBox)child).findTrailingText();
                if (result != null && result.isEmpty()) {
                    continue;
                }
                return result;
            } else {
                return null;
            }
        }

        return null;
    }

    public void trimTrailingSpace(LayoutContext c) {
        InlineText text = findTrailingText();

        if (text != null) {
            InlineLayoutBox iB = text.getParent();
            IdentValue whitespace = iB.getStyle().getWhitespace();
            if (whitespace == NORMAL || whitespace == IdentValue.NOWRAP) {
                text.trimTrailingSpace(c);
            }
        }
    }

    @Nullable
    @CheckReturnValue
    @Override
    public Box find(CssContext cssCtx, int absX, int absY, boolean findAnonymous) {
        PaintingInfo pI = getPaintingInfo();
        if (pI !=null && ! pI.getAggregateBounds().contains(absX, absY)) {
            return null;
        }

        Box result;
        for (int i = 0; i < getChildCount(); i++) {
            Box child = getChild(i);
            result = child.find(cssCtx, absX, absY, findAnonymous);
            if (result != null) {
                return result;
            }
        }

        return null;
    }

    public int getBaseline() {
        return _baseline;
    }

    public void setBaseline(int baseline) {
        _baseline = baseline;
    }

    public boolean isContainsOnlyBlockLevelContent() {
        if (! isContainsBlockLevelContent()) {
            return false;
        }

        for (int i = 0; i < getChildCount(); i++) {
            Box b = getChild(i);
            if (! (b instanceof BlockBox)) {
                return false;
            }
        }

        return true;
    }

    @Override
    public Box getRestyleTarget() {
        return getParent();
    }

    @Override
    public void restyle(LayoutContext c) {
        Box parent = getParent();
        Element e = parent.getElement();
        if (e != null) {
            CalculatedStyle style = c.getSharedContext().getStyle(e, true);
            setStyle(style.createAnonymousStyle(IdentValue.BLOCK));
        }

        restyleChildren(c);
    }

    public boolean isContainsVisibleContent() {
        for (int i = 0; i < getChildCount(); i++) {
            Box b = getChild(i);
            if (b instanceof BlockBox) {
                if (b.getWidth() > 0 || b.getHeight() > 0) {
                    return true;
                }
            } else {
                boolean maybeResult = ((InlineLayoutBox)b).isContainsVisibleContent();
                if (maybeResult) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void clearSelection(List<Box> modified) {
        for (Box b : getNonFlowContent()) {
            b.clearSelection(modified);
        }

        super.clearSelection(modified);
    }

    @Override
    public void selectAll() {
        for (Box value : getNonFlowContent()) {
            BlockBox box = (BlockBox) value;
            box.selectAll();
        }

        super.selectAll();
    }

    @Override
    public void collectText(RenderingContext c, StringBuilder buffer) throws IOException {
        for (Box b : getNonFlowContent()) {
            b.collectText(c, buffer);
        }
        if (isContainsDynamicFunction()) {
            lookForDynamicFunctions(c);
        }
        super.collectText(c, buffer);
    }

    @Override
    public void exportText(RenderingContext c, Writer writer) throws IOException {
        int baselinePos = getAbsY() + getBaseline();
        if (baselinePos >= c.getPage().getBottom() && isInDocumentFlow()) {
            exportPageBoxText(c, writer, baselinePos);
        }

        for (Box b : getNonFlowContent()) {
            b.exportText(c, writer);
        }

        if (isContainsContent()) {
            StringBuilder result = new StringBuilder();
            collectText(c, result);
            writer.write(result.toString().trim());
            writer.write(lineSeparator());
        }
    }

    @Override
    public void analyzePageBreaks(LayoutContext c, ContentLimitContainer container) {
        container.updateTop(c, getAbsY());
        container.updateBottom(c, getAbsY() + getHeight());
    }

    public void checkPagePosition(LayoutContext c, boolean alwaysBreak) {
        if (! c.isPageBreaksAllowed()) {
            return;
        }

        PageBox pageBox = c.getRootLayer().getFirstPage(c, this);
        if (pageBox != null) {
            boolean needsPageBreak =
                alwaysBreak || getAbsY() + getHeight() >= pageBox.getBottom() - c.getExtraSpaceBottom();

           if (needsPageBreak) {
               forcePageBreakBefore(c, IdentValue.ALWAYS, false);
               calcCanvasLocation();
           } else if (pageBox.getTop() + c.getExtraSpaceTop() > getAbsY()) {
               int diff = pageBox.getTop() + c.getExtraSpaceTop() - getAbsY();

               setY(getY() + diff);
               calcCanvasLocation();
           }
        }
    }

    public JustificationInfo getJustificationInfo() {
        return _justificationInfo;
    }

    private void setJustificationInfo(JustificationInfo justificationInfo) {
        _justificationInfo = justificationInfo;
    }
}
