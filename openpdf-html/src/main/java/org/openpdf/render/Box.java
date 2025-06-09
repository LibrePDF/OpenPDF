/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci
 * Copyright (c) 2005, 2006 Wisconsin Court System
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
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.openpdf.css.constants.CSSName;
import org.openpdf.css.constants.IdentValue;
import org.openpdf.css.parser.FSColor;
import org.openpdf.css.parser.FSRGBColor;
import org.openpdf.css.style.CalculatedStyle;
import org.openpdf.css.style.CalculatedStyle.Edge;
import org.openpdf.css.style.CssContext;
import org.openpdf.css.style.derived.BorderPropertySet;
import org.openpdf.css.style.derived.RectPropertySet;
import org.openpdf.layout.Layer;
import org.openpdf.layout.LayoutContext;
import org.openpdf.layout.PaintingInfo;
import org.openpdf.layout.Styleable;
import org.openpdf.util.XRLog;

import java.awt.*;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import static java.util.Objects.requireNonNullElseGet;
import static org.openpdf.render.Box.State.NOTHING;

public abstract class Box implements Styleable {
    @Nullable
    private Element _element;

    private int _x;
    private int _y;

    private int _absY;
    private int _absX;

    /**
     * Box width.
     */
    private int _contentWidth;
    private int _rightMBP;
    private int _leftMBP;

    private int _height;

    @Nullable
    private Layer _layer;
    @Nullable
    private Layer _containingLayer;

    @Nullable
    private Box _parent;

    private final List<Box> _boxes = new ArrayList<>(3);

    /**
     * Keeps track of the start of children's containing block.
     */
    private int _tx;
    private int _ty;

    @Nullable
    private CalculatedStyle _style;
    @Nullable
    private Box _containingBlock;

    @Nullable
    private Dimension _relativeOffset;

    @Nullable
    private PaintingInfo _paintingInfo;

    @Nullable
    private RectPropertySet _workingMargin;

    private int _index;

    @Nullable
    private String _pseudoElementOrClass;

    private final boolean _anonymous;

    protected Box(@Nullable Box parent, @Nullable CalculatedStyle style) {
        this._parent = parent;
        this.setStyle(style);
        _anonymous = false;
    }

    protected Box(@Nullable Element element, @Nullable CalculatedStyle style, boolean anonymous) {
        this._element = element;
        this.setStyle(style);
        _anonymous = anonymous;
    }

    public abstract String dump(LayoutContext c, String indent, Dump which);

    protected void dumpBoxes(
            LayoutContext c, String indent, List<Box> boxes,
            Dump which, StringBuilder result) {
        for (Iterator<Box> i = boxes.iterator(); i.hasNext(); ) {
            Box b = i.next();
            result.append(b.dump(c, indent + "  ", which));
            if (i.hasNext()) {
                result.append('\n');
            }
        }
    }

    public int getWidth() {
        return getContentWidth() + getLeftMBP() + getRightMBP();
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append(getClass().getSimpleName());
        result.append(": ");
        appendPosition(result);
        appendSize(result);
        return result.toString().trim();
    }

    protected void appendPosition(StringBuilder result) {
        if (getAbsX() != 0 || getAbsY() != 0) {
            result.append("pos: (").append(getAbsX()).append(",").append(getAbsY()).append(") ");
        }
    }

    protected void appendSize(StringBuilder result) {
        if (getWidth() != 0 || getHeight() != 0) {
            result.append("size: (").append(getWidth()).append("x").append(getHeight()).append(") ");
        }
    }

    public void addChildForLayout(LayoutContext c, Box child) {
        addChild(child);

        child.initContainingLayer(c);
    }

    public void addChild(Box child) {
        if (child == null) {
            throw new NullPointerException("trying to add null child");
        }
        child.setParent(this);
        child.setIndex(_boxes.size());
        _boxes.add(child);
    }

    public void addAllChildren(List<Box> children) {
        for (Box box : children) {
            addChild(box);
        }
    }

    public void removeAllChildren() {
        _boxes.clear();
    }

    public void removeChild(Box target) {
        boolean found = false;
        for (Iterator<Box> i = getChildren().iterator(); i.hasNext(); ) {
            Box child = i.next();
            if (child.equals(target)) {
                i.remove();
                found = true;
            } else if (found) {
                child.setIndex(child.getIndex() - 1);
            }
        }
    }

    @Nullable
    @CheckReturnValue
    public Box getPreviousSibling() {
        Box parent = getParent();
        return parent == null ? null : parent.getPrevious(this);
    }

    @Nullable
    @CheckReturnValue
    public Box getNextSibling() {
        Box parent = getParent();
        return parent == null ? null : parent.getNext(this);
    }

    @Nullable
    @CheckReturnValue
    protected Box getPrevious(Box child) {
        return child.getIndex() == 0 ? null : getChild(child.getIndex()-1);
    }

    @Nullable
    @CheckReturnValue
    protected Box getNext(Box child) {
        return child.getIndex() == getChildCount() - 1 ? null : getChild(child.getIndex()+1);
    }

    public void removeChild(int i) {
        removeChild(getChild(i));
    }

    public void setParent(@Nullable Box box) {
        _parent = box;
    }

    @Nullable
    @CheckReturnValue
    public Box getParent() {
        return _parent;
    }

    public int getChildCount() {
        return _boxes.size();
    }

    @CheckReturnValue
    public Box getChild(int i) {
        return _boxes.get(i);
    }

    @CheckReturnValue
    public List<Box> getChildren() {
        return _boxes;
    }

    public enum State {
        NOTHING,
        FLUX,
        CHILDREN_FLUX,
        DONE
    }

    private volatile State _state = NOTHING;

    public enum Dump {
        RENDER,
        LAYOUT
    }

    public State getState() {
        return _state;
    }

    public void setState(State state) {
        _state = state;
    }

    @Nullable
    @CheckReturnValue
    @Override
    public final CalculatedStyle getStyle() {
        return _style;
    }

    @Override
    public void setStyle(@Nullable CalculatedStyle style) {
        _style = style;
    }

    @Nullable
    @CheckReturnValue
    public Box getContainingBlock() {
        return _containingBlock == null ? getParent() : _containingBlock;
    }

    public void setContainingBlock(Box containingBlock) {
        _containingBlock = containingBlock;
    }

    public Rectangle getMarginEdge(int left, int top, CssContext cssCtx, int tx, int ty) {
        // Note that negative margins can mean this rectangle is inside the border
        // edge, but that's the way it's supposed to work...
        Rectangle result = new Rectangle(left, top, getWidth(), getHeight());
        result.translate(tx, ty);
        return result;
    }

    public Rectangle getMarginEdge(CssContext cssCtx, int tx, int ty) {
        return getMarginEdge(getX(), getY(), cssCtx, tx, ty);
    }

    public Rectangle getPaintingBorderEdge(CssContext cssCtx) {
        return getBorderEdge(getAbsX(), getAbsY(), cssCtx);
    }

    @CheckReturnValue
    public Rectangle getPaintingPaddingEdge(CssContext cssCtx) {
        return getPaddingEdge(getAbsX(), getAbsY(), cssCtx);
    }

    public Rectangle getPaintingClipEdge(CssContext cssCtx) {
        return getPaintingBorderEdge(cssCtx);
    }

    @CheckReturnValue
    public Rectangle getChildrenClipEdge(RenderingContext c) {
        return getPaintingPaddingEdge(c);
    }

    /**
     * <B>NOTE</B>: This method does not consider any children of this box
     */
    public boolean intersects(CssContext cssCtx, @Nullable Shape clip) {
        return clip == null || clip.intersects(getPaintingClipEdge(cssCtx));
    }

    public Rectangle getBorderEdge(int left, int top, CssContext cssCtx) {
        RectPropertySet margin = getMargin(cssCtx);
        return new Rectangle(left + (int) margin.left(),
                top + (int) margin.top(),
                getWidth() - (int) margin.left() - (int) margin.right(),
                getHeight() - (int) margin.top() - (int) margin.bottom());
    }

    @CheckReturnValue
    public Rectangle getPaddingEdge(int left, int top, CssContext cssCtx) {
        RectPropertySet margin = getMargin(cssCtx);
        RectPropertySet border = getBorder(cssCtx);
        return new Rectangle(left + (int) margin.left() + (int) border.left(),
                top + (int) margin.top() + (int) border.top(),
                getWidth() - (int) margin.width() - (int) border.width(),
                getHeight() - (int) margin.height() - (int) border.height());
    }

    protected int getPaddingWidth(CssContext cssCtx) {
        RectPropertySet padding = getPadding(cssCtx);
        return (int)padding.left() + getContentWidth() + (int)padding.right();
    }

    public Rectangle getContentAreaEdge(int left, int top, CssContext cssCtx) {
        RectPropertySet margin = getMargin(cssCtx);
        RectPropertySet border = getBorder(cssCtx);
        RectPropertySet padding = getPadding(cssCtx);

        return new Rectangle(
                left + (int)margin.left() + (int)border.left() + (int)padding.left(),
                top + (int)margin.top() + (int)border.top() + (int)padding.top(),
                getWidth() - (int)margin.width() - (int)border.width() - (int)padding.width(),
                getHeight() - (int) margin.height() - (int) border.height() - (int) padding.height());
    }

    @Nullable
    @CheckReturnValue
    public Layer getLayer() {
        return _layer;
    }

    public void setLayer(@Nullable Layer layer) {
        _layer = layer;
    }

    @Nullable
    public Dimension positionRelative(CssContext cssCtx) {
        int initialX = getX();
        int initialY = getY();

        CalculatedStyle style = getStyle();
        if (! style.isIdent(CSSName.LEFT, IdentValue.AUTO)) {
            setX(getX() + (int)style.getFloatPropertyProportionalWidth(
                    CSSName.LEFT, getContainingBlock().getContentWidth(), cssCtx));
        } else if (! style.isIdent(CSSName.RIGHT, IdentValue.AUTO)) {
            setX(getX() - (int)style.getFloatPropertyProportionalWidth(
                    CSSName.RIGHT, getContainingBlock().getContentWidth(), cssCtx));
        }

        int cbContentHeight = 0;
        if (! getContainingBlock().getStyle().isAutoHeight()) {
            CalculatedStyle cbStyle = getContainingBlock().getStyle();
            cbContentHeight = (int)cbStyle.getFloatPropertyProportionalHeight(
                    CSSName.HEIGHT, 0, cssCtx);
        } else if (isInlineBlock()) {
            // FIXME Should be content height, not overall height
            cbContentHeight = getContainingBlock().getHeight();
        }

        if (!style.isIdent(CSSName.TOP, IdentValue.AUTO)) {
            setY(getY() + ((int)style.getFloatPropertyProportionalHeight(
                    CSSName.TOP, cbContentHeight, cssCtx)));
        } else if (!style.isIdent(CSSName.BOTTOM, IdentValue.AUTO)) {
            setY(getY() - ((int)style.getFloatPropertyProportionalHeight(
                    CSSName.BOTTOM, cbContentHeight, cssCtx)));
        }

        _relativeOffset = new Dimension(getX() - initialX, getY() - initialY);
        return getRelativeOffset();
    }

    protected boolean isInlineBlock() {
        return false;
    }

    public void setAbsY(int absY) {
        _absY = absY;
    }

    public int getAbsY() {
        return _absY;
    }

    public void setAbsX(int absX) {
        _absX = absX;
    }

    public int getAbsX() {
        return _absX;
    }

    public boolean isStyled() {
        return _style != null;
    }

    public int getBorderSides() {
        return BorderPainter.ALL;
    }

    public void paintBorder(RenderingContext c) {
        c.getOutputDevice().paintBorder(c, this);
    }

    private boolean isPaintsRootElementBackground() {
        return (isRoot() && getStyle().isHasBackground()) ||
                (isBody() && ! getParent().getStyle().isHasBackground());
    }

    public void paintBackground(RenderingContext c) {
        if (! isPaintsRootElementBackground()) {
            c.getOutputDevice().paintBackground(c, this);
        }
    }

    public void paintRootElementBackground(RenderingContext c) {
        PaintingInfo pI = getPaintingInfo();
        if (pI != null) {
            if (getStyle().isHasBackground()) {
                paintRootElementBackground(c, pI);
            } else if (getChildCount() > 0) {
                Box body = getChild(0);
                body.paintRootElementBackground(c, pI);
            }
        }
    }

    private void paintRootElementBackground(RenderingContext c, PaintingInfo pI) {
        Dimension marginCorner = pI.getOuterMarginCorner();
        Rectangle canvasBounds = new Rectangle(0, 0, marginCorner.width, marginCorner.height);
        canvasBounds.add(c.getViewportRectangle());
        c.getOutputDevice().paintBackground(c, getStyle(), canvasBounds, canvasBounds, BorderPropertySet.EMPTY_BORDER);
    }

    @Nullable
    public Layer getContainingLayer() {
        return _containingLayer;
    }

    public void setContainingLayer(@Nullable Layer containingLayer) {
        _containingLayer = containingLayer;
    }

    public void initContainingLayer(LayoutContext c) {
        if (getLayer() != null) {
            setContainingLayer(getLayer());
        } else if (getContainingLayer() == null) {
            if (getParent() == null || getParent().getContainingLayer() == null) {
                throw new RuntimeException("internal error");
            }
            setContainingLayer(getParent().getContainingLayer());

            // FIXME Will be glacially slow for large inline relative layers.  Could
            // be much more efficient.  We're just looking for block boxes which are
            // directly wrapped by an inline relative layer (i.e. block boxes sandwiched
            // between anonymous block boxes)
            if (c.getLayer().isInline()) {
                List<Box> content =
                    ((InlineLayoutBox)c.getLayer().getMaster()).getElementWithContent();
                if (content.contains(this)) {
                    setContainingLayer(c.getLayer());
                }
            }
        }
    }

    public void connectChildrenToCurrentLayer(LayoutContext c) {

        for (int i = 0; i < getChildCount(); i++) {
            Box box = getChild(i);
            box.setContainingLayer(c.getLayer());
            box.connectChildrenToCurrentLayer(c);
        }
    }

    public List<Box> getElementBoxes(Element elem) {
        List<Box> result = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i++) {
            Box child = getChild(i);
            if (child.getElement() == elem) {
                result.add(child);
            }
            result.addAll(child.getElementBoxes(elem));
        }
        return result;
    }

    public void reset(LayoutContext c) {
        resetChildren(c);
        if (_layer != null) {
            _layer.detach();
            _layer = null;
        }

        setContainingLayer(null);
        setLayer(null);
        setPaintingInfo(null);
        setContentWidth(0);

        _workingMargin = null;

        Element e = getElement();
        if (e != null) {
            String anchorName = c.getNamespaceHandler().getAnchorName(e);
            if (anchorName != null) {
                c.removeBoxId(anchorName);
            }

            String id = c.getNamespaceHandler().getID(e);
            if (id != null) {
                c.removeBoxId(id);
            }
        }
    }

    public void detach(LayoutContext c) {
        reset(c);

        if (getParent() != null) {
            getParent().removeChild(this);
            setParent(null);
        }
    }

    public void resetChildren(LayoutContext c, int start, int end) {
        for (int i = start; i <= end; i++) {
            Box box = getChild(i);
            box.reset(c);
        }
    }

    protected void resetChildren(LayoutContext c) {
        int remaining = getChildCount();
        for (int i = 0; i < remaining; i++) {
            Box box = getChild(i);
            box.reset(c);
        }
    }

    public abstract void calcCanvasLocation();

    public void calcChildLocations() {
        for (int i = 0; i < getChildCount(); i++) {
            Box child = getChild(i);
            child.calcCanvasLocation();
            child.calcChildLocations();
        }
    }

    public int forcePageBreakBefore(LayoutContext c, IdentValue pageBreakValue, boolean pendingPageName) {
        PageBox page = c.getRootLayer().getFirstPage(c, this);
        if (page == null) {
            XRLog.layout(Level.WARNING, "Box has no page");
            return 0;
        } else {
            int pageBreakCount = 1;
            if (page.getTop() == getAbsY()) {
                pageBreakCount--;
                if (pendingPageName && page == c.getRootLayer().getLastPage()) {
                    c.getRootLayer().removeLastPage();
                    c.setPageName(c.getPendingPageName());
                    c.getRootLayer().addPage(c);
                }
            }
            if ((page.isLeftPage() && pageBreakValue == IdentValue.LEFT) ||
                    (page.isRightPage() && pageBreakValue == IdentValue.RIGHT)) {
                pageBreakCount++;
            }

            if (pageBreakCount == 0) {
                return 0;
            }

            if (pageBreakCount == 1 && pendingPageName) {
                c.setPageName(c.getPendingPageName());
            }

            int delta = page.getBottom() + c.getExtraSpaceTop() - getAbsY();
            if (page == c.getRootLayer().getLastPage()) {
                c.getRootLayer().addPage(c);
            }

            if (pageBreakCount == 2) {
                page = c.getRootLayer().getPages().get(page.getPageNo()+1);
                delta += page.getContentHeight(c);

                if (pendingPageName) {
                    c.setPageName(c.getPendingPageName());
                }

                if (page == c.getRootLayer().getLastPage()) {
                    c.getRootLayer().addPage(c);
                }
            }

            setY(getY() + delta);

            return delta;
        }
    }

    public void forcePageBreakAfter(LayoutContext c, IdentValue pageBreakValue) {
        boolean needSecondPageBreak = false;
        PageBox page = c.getRootLayer().getLastPage(c, this);

        if (page != null) {
            if ((page.isLeftPage() && pageBreakValue == IdentValue.LEFT) ||
                    (page.isRightPage() && pageBreakValue == IdentValue.RIGHT)) {
                needSecondPageBreak = true;
            }

            int delta = page.getBottom() + c.getExtraSpaceTop() - (getAbsY() +
                    getMarginBorderPadding(c, Edge.TOP) + getHeight());

            if (page == c.getRootLayer().getLastPage()) {
                c.getRootLayer().addPage(c);
            }

            if (needSecondPageBreak) {
                page = c.getRootLayer().getPages().get(page.getPageNo()+1);
                delta += page.getContentHeight(c);

                if (page == c.getRootLayer().getLastPage()) {
                    c.getRootLayer().addPage(c);
                }
            }

            setHeight(getHeight() + delta);
        }
    }

    public boolean crossesPageBreak(LayoutContext c) {
        if (! c.isPageBreaksAllowed()) {
            return false;
        }

        PageBox pageBox = c.getRootLayer().getFirstPage(c, this);
        if (pageBox == null) {
            return false;
        } else {
            return getAbsY() + getHeight() >= pageBox.getBottom() - c.getExtraSpaceBottom();
        }
    }

    @Nullable
    @CheckReturnValue
    public Dimension getRelativeOffset() {
        return _relativeOffset;
    }

    @Nullable
    @CheckReturnValue
    public Box find(CssContext cssCtx, int absX, int absY, boolean findAnonymous) {
        PaintingInfo pI = getPaintingInfo();
        if (pI != null && ! pI.getAggregateBounds().contains(absX, absY)) {
            return null;
        }

        for (int i = 0; i < getChildCount(); i++) {
            Box child = getChild(i);
            Box result = child.find(cssCtx, absX, absY, findAnonymous);
            if (result != null) {
                return result;
            }
        }

        Rectangle edge = getContentAreaEdge(getAbsX(), getAbsY(), cssCtx);
        return edge.contains(absX, absY) && getStyle().isVisible() ? this : null;
    }

    public boolean isRoot() {
        return getElement() != null && ! isAnonymous() && getElement().getParentNode().getNodeType() == Node.DOCUMENT_NODE;
    }

    public boolean isBody() {
        return getParent() != null && getParent().isRoot();
    }

    @Override
    @Nullable
    @CheckReturnValue
    public Element getElement() {
        return _element;
    }

    @Override
    public final void setElement(@Nullable Element element) {
        _element = element;
    }

    protected final void setMarginTop(CssContext cssContext, int marginTop) {
        ensureWorkingMargin(cssContext).setTop(marginTop);
    }

    protected void setMarginBottom(CssContext cssContext, int marginBottom) {
        ensureWorkingMargin(cssContext).setBottom(marginBottom);
    }

    public void setMarginLeft(CssContext cssContext, int marginLeft) {
        ensureWorkingMargin(cssContext).setLeft(marginLeft);
    }

    protected void setMarginRight(CssContext cssContext, int marginRight) {
        ensureWorkingMargin(cssContext).setRight(marginRight);
    }

    @NonNull
    @CheckReturnValue
    private RectPropertySet ensureWorkingMargin(CssContext cssContext) {
        if (_workingMargin == null) {
            _workingMargin = getStyleMargin(cssContext).copyOf();
        }
        return _workingMargin;
    }

    public RectPropertySet getMargin(CssContext cssContext) {
        return _workingMargin != null ? _workingMargin : getStyleMargin(cssContext);
    }

    protected RectPropertySet getStyleMargin(CssContext cssContext) {
        return getStyle().getMarginRect(getContainingBlockWidth(), cssContext, true);
    }

    protected RectPropertySet getStyleMarginNoCache(CssContext cssContext) {
        return getStyle().getMarginRect(getContainingBlockWidth(), cssContext, false);
    }

    public RectPropertySet getPadding(CssContext cssCtx) {
        return getStyle().getPaddingRect(getContainingBlockWidth(), cssCtx);
    }

    public BorderPropertySet getBorder(CssContext cssCtx) {
        return getStyle().getBorder(cssCtx);
    }

    protected int getContainingBlockWidth() {
        return getContainingBlock().getContentWidth();
    }

    protected void resetTopMargin(CssContext cssContext) {
        if (_workingMargin != null) {
            RectPropertySet styleMargin = getStyleMargin(cssContext);

            _workingMargin.setTop(styleMargin.top());
        }
    }

    public void clearSelection(List<Box> modified) {
        for (int i = 0; i < getChildCount(); i++) {
            Box child = getChild(i);
            child.clearSelection(modified);
        }
    }

    public void selectAll() {
        for (int i = 0; i < getChildCount(); i++) {
            Box child = getChild(i);
            child.selectAll();
        }
    }

    public PaintingInfo calcPaintingInfo(CssContext c, boolean useCache) {
        PaintingInfo cached = getPaintingInfo();
        if (cached != null && useCache) {
            return cached;
        }

        Rectangle bounds = getMarginEdge(getAbsX(), getAbsY(), c, 0, 0);

        PaintingInfo result = new PaintingInfo(
                new Dimension(bounds.x + bounds.width, bounds.y + bounds.height),
                getPaintingClipEdge(c)
        );

        if (!getStyle().isOverflowApplies() || getStyle().isOverflowVisible()) {
            calcChildPaintingInfo(c, result, useCache);
        }

        setPaintingInfo(result);

        return result;
    }

    protected void calcChildPaintingInfo(
            CssContext c, PaintingInfo result, boolean useCache) {
        for (int i = 0; i < getChildCount(); i++) {
            Box child = getChild(i);
            PaintingInfo info = child.calcPaintingInfo(c, useCache);
            moveIfGreater(result.getOuterMarginCorner(), info.getOuterMarginCorner());
            result.getAggregateBounds().add(info.getAggregateBounds());
        }
    }

    public int getMarginBorderPadding(CssContext cssCtx, Edge edge) {
        BorderPropertySet border = getBorder(cssCtx);
        RectPropertySet margin = getMargin(cssCtx);
        RectPropertySet padding = getPadding(cssCtx);

        return switch (edge) {
            case LEFT -> (int) (margin.left() + border.left() + padding.left());
            case RIGHT -> (int) (margin.right() + border.right() + padding.right());
            case TOP -> (int) (margin.top() + border.top() + padding.top());
            case BOTTOM -> (int) (margin.bottom() + border.bottom() + padding.bottom());
        };
    }

    protected void moveIfGreater(Dimension result, Dimension test) {
        if (test.width > result.width) {
            result.width = test.width;
        }
        if (test.height > result.height) {
            result.height = test.height;
        }
    }

    public void restyle(LayoutContext c) {
        Element e = getElement();
        CalculatedStyle style = null;

        String pe = getPseudoElementOrClass();
        if (pe != null) {
            if (e != null) {
                style = c.getSharedContext().getStyle(e, true);
                style = style.deriveStyle(c.getCss().getPseudoElementStyle(e, pe));
            } else {
                BlockBox container = (BlockBox)getParent().getParent();
                e = container.getElement();
                style = c.getSharedContext().getStyle(e, true);
                style = style.deriveStyle(c.getCss().getPseudoElementStyle(e, pe));
                style = style.createAnonymousStyle(IdentValue.INLINE);
            }
        } else {
            if (e != null) {
                style = c.getSharedContext().getStyle(e, true);
                if (isAnonymous()) {
                    style = style.createAnonymousStyle(getStyle().getIdent(CSSName.DISPLAY));
                }
            } else {
                Box parent = getParent();
                if (parent != null) {
                    e = parent.getElement();
                    if (e != null) {
                        style = c.getSharedContext().getStyle(e, true);
                        style = style.createAnonymousStyle(IdentValue.INLINE);
                    }
                }
            }
        }

        if (style != null) {
            setStyle(style);
        }

        restyleChildren(c);
    }

    protected void restyleChildren(LayoutContext c) {
        for (int i = 0; i < getChildCount(); i++) {
            Box b = getChild(i);
            b.restyle(c);
        }
    }

    public Box getRestyleTarget() {
        return this;
    }

    protected int getIndex() {
        return _index;
    }

    protected void setIndex(int index) {
        _index = index;
    }

    @Nullable
    @CheckReturnValue
    @Override
    public String getPseudoElementOrClass() {
        return _pseudoElementOrClass;
    }

    public void setPseudoElementOrClass(String pseudoElementOrClass) {
        _pseudoElementOrClass = pseudoElementOrClass;
    }

    public void setX(int x) {
        _x = x;
    }

    public int getX() {
        return _x;
    }

    public void setY(int y) {
        _y = y;
    }

    public int getY() {
        return _y;
    }

    public void setTy(int ty) {
        _ty = ty;
    }

    public int getTy() {
        return _ty;
    }

    public void setTx(int tx) {
        _tx = tx;
    }

    public int getTx() {
        return _tx;
    }

    public void setRightMBP(int rightMBP) {
        _rightMBP = rightMBP;
    }

    public int getRightMBP() {
        return _rightMBP;
    }

    public void setLeftMBP(int leftMBP) {
        _leftMBP = leftMBP;
    }

    public int getLeftMBP() {
        return _leftMBP;
    }

    public void setHeight(int height) {
        _height = height;
    }

    public int getHeight() {
        return _height;
    }

    public void setContentWidth(int contentWidth) {
        _contentWidth = Math.max(contentWidth, 0);
    }

    public int getContentWidth() {
        return _contentWidth;
    }

    @Nullable
    @CheckReturnValue
    public PaintingInfo getPaintingInfo() {
        return _paintingInfo;
    }

    private void setPaintingInfo(@Nullable PaintingInfo paintingInfo) {
        _paintingInfo = paintingInfo;
    }

    public boolean isAnonymous() {
        return _anonymous;
    }

    public BoxDimensions getBoxDimensions() {
        return new BoxDimensions(getLeftMBP(), getRightMBP(), getContentWidth(), getHeight());
    }

    public void setBoxDimensions(BoxDimensions dimensions) {
        setLeftMBP(dimensions.getLeftMBP());
        setRightMBP(dimensions.getRightMBP());
        setContentWidth(dimensions.getContentWidth());
        setHeight(dimensions.getHeight());
    }

    public void collectText(RenderingContext c, StringBuilder buffer) throws IOException {
        for (Box b : getChildren()) {
            b.collectText(c, buffer);
        }
    }

    public void exportText(RenderingContext c, Writer writer) throws IOException {
        if (c.isPrint() && isRoot()) {
            c.setPage(0, c.getRootLayer().getPages().get(0));
            c.getPage().exportLeadingText(c, writer);
        }
        for (Box b : getChildren()) {
            b.exportText(c, writer);
        }
        if (c.isPrint() && isRoot()) {
            exportPageBoxText(c, writer);
        }
    }

    private void exportPageBoxText(RenderingContext c, Writer writer) throws IOException {
        c.getPage().exportTrailingText(c, writer);
        if (c.getPage() != c.getRootLayer().getLastPage()) {
            List<PageBox> pages = c.getRootLayer().getPages();
            do {
                PageBox next = pages.get(c.getPageNo()+1);
                c.setPage(next.getPageNo(), next);
                next.exportLeadingText(c, writer);
                next.exportTrailingText(c, writer);
            } while (c.getPage() != c.getRootLayer().getLastPage());
        }
    }

    protected void exportPageBoxText(RenderingContext c, Writer writer, int yPos) throws IOException {
        c.getPage().exportTrailingText(c, writer);
        List<PageBox> pages = c.getRootLayer().getPages();
        PageBox next = pages.get(c.getPageNo()+1);
        c.setPage(next.getPageNo(), next);
        while (next.getBottom() < yPos) {
            next.exportLeadingText(c, writer);
            next.exportTrailingText(c, writer);
            next = pages.get(c.getPageNo()+1);
            c.setPage(next.getPageNo(), next);
        }
        next.exportLeadingText(c, writer);
    }

    public boolean isInDocumentFlow() {
        Box flowRoot = this;
        while (true) {
            Box parent = flowRoot.getParent();
            if (parent == null) {
                break;
            } else {
                flowRoot = parent;
            }
        }

        return flowRoot.isRoot();
    }

    public void analyzePageBreaks(LayoutContext c, ContentLimitContainer container) {
        container.updateTop(c, getAbsY());
        for (Box b : getChildren()) {
            b.analyzePageBreaks(c, container);
        }
        container.updateBottom(c, getAbsY() + getHeight());
    }

    public FSColor getEffBackgroundColor(RenderingContext c) {
        FSColor result;
        Box current = this;
        while (current != null) {
            result = current.getStyle().getBackgroundColor();
            if (result != null) {
                return result;
            }

            current = current.getContainingBlock();
        }

        PageBox page = c.getPage();
        result = page.getStyle().getBackgroundColor();
        return requireNonNullElseGet(result, () -> new FSRGBColor(255, 255, 255));
    }

    protected boolean isMarginAreaRoot() {
        return false;
    }

    public boolean isContainedInMarginBox() {
        Box current = this;
        while (true) {
            Box parent = current.getParent();
            if (parent == null) {
                break;
            } else {
                current = parent;
            }
        }

        return current.isMarginAreaRoot();
    }

    public int getEffectiveWidth() {
        return getWidth();
    }

    protected boolean isInitialContainingBlock() {
        return false;
    }
}
