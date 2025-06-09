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
import org.w3c.dom.css.CSSPrimitiveValue;
import org.openpdf.css.constants.CSSName;
import org.openpdf.css.constants.MarginBoxName;
import org.openpdf.css.newmatch.PageInfo;
import org.openpdf.css.parser.FSFunction;
import org.openpdf.css.parser.PropertyValue;
import org.openpdf.css.sheet.PropertyDeclaration;
import org.openpdf.css.style.CalculatedStyle;
import org.openpdf.css.style.CalculatedStyle.Edge;
import org.openpdf.css.style.CssContext;
import org.openpdf.css.style.derived.LengthValue;
import org.openpdf.css.style.derived.RectPropertySet;
import org.openpdf.layout.BoxBuilder;
import org.openpdf.layout.BoxBuilder.MarginDirection;
import org.openpdf.layout.Layer.PagedMode;
import org.openpdf.layout.LayoutContext;
import org.openpdf.newtable.TableBox;

import java.awt.*;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Locale;

import static org.openpdf.css.constants.CSSName.FS_PAGE_HEIGHT;
import static org.openpdf.css.constants.CSSName.FS_PAGE_ORIENTATION;
import static org.openpdf.css.constants.CSSName.FS_PAGE_WIDTH;
import static org.openpdf.css.constants.IdentValue.LANDSCAPE;
import static org.openpdf.css.parser.PropertyValue.Type.VALUE_TYPE_FUNCTION;
import static org.openpdf.css.style.CalculatedStyle.Edge.BOTTOM;
import static org.openpdf.css.style.CalculatedStyle.Edge.LEFT;
import static org.openpdf.css.style.CalculatedStyle.Edge.RIGHT;
import static org.openpdf.css.style.CalculatedStyle.Edge.TOP;
import static org.openpdf.layout.BoxBuilder.MarginDirection.HORIZONTAL;
import static org.openpdf.layout.BoxBuilder.MarginDirection.VERTICAL;

public class PageBox {
    private static final MarginArea[] MARGIN_AREA_DEFS = {
        new TopLeftCorner(),
        new TopMarginArea(),
        new TopRightCorner(),

        new LeftMarginArea(),
        new RightMarginArea(),

        new BottomLeftCorner(),
        new BottomMarginArea(),
        new BottomRightCorner(),
    };

    private static final int LEADING_TRAILING_SPLIT = 5;

    private final CalculatedStyle _style;

    private final int _top;
    private final int _bottom;

    private int _paintingTop;
    private int _paintingBottom;

    private final int _pageNo;

    private final int _outerPageWidth;

    @Nullable
    private PageDimensions _pageDimensions;

    @NonNull
    private final PageInfo _pageInfo;

    @Nullable
    private final MarginAreaContainer[] _marginAreas = new MarginAreaContainer[MARGIN_AREA_DEFS.length];

    @Nullable
    private Element _metadata;

    public PageBox(PageInfo pageInfo, CssContext cssContext, CalculatedStyle style, int top, int pageNo) {
        _pageInfo = pageInfo;
        _style = style;
        _outerPageWidth = getWidth(cssContext);
        _top = top;
        _bottom = top + getContentHeight(cssContext);
        _pageNo = pageNo;
    }

    public int getWidth(CssContext cssCtx) {
        return getPageDimensions(cssCtx).width();
    }

    public int getHeight(CssContext cssCtx) {
        return getPageDimensions(cssCtx).height();
    }

    @CheckReturnValue
    private PageDimensions getPageDimensions(CssContext cssCtx) {
        if (_pageDimensions == null) {
            _pageDimensions = resolvePageDimensions(cssCtx);
        }
        return _pageDimensions;
    }

    @CheckReturnValue
    private PageDimensions resolvePageDimensions(CssContext cssCtx) {
        CalculatedStyle style = getStyle();

        int width = style.isLength(FS_PAGE_WIDTH) ?
                style.getIntPropertyProportionalTo(FS_PAGE_WIDTH, 0, cssCtx) :
                resolveAutoPageWidth(cssCtx);

        int height = style.isLength(FS_PAGE_HEIGHT) ?
                style.getIntPropertyProportionalTo(FS_PAGE_HEIGHT, 0, cssCtx) :
                resolveAutoPageHeight(cssCtx);

        //noinspection SuspiciousNameCombination
        return style.isIdent(FS_PAGE_ORIENTATION, LANDSCAPE) ?
                new PageDimensions(height, width) :
                new PageDimensions(width, height);
    }

    private boolean isUseLetterSize() {
        Locale l = Locale.getDefault();
        String county = l.getCountry();

        // Per http://en.wikipedia.org/wiki/Paper_size, letter paper is
        // a de facto standard in Canada (although the government uses
        // its own standard) and Mexico (even though it is officially an ISO
        // country)
        return county.equals("US") || county.equals("CA") || county.equals("MX");
    }

    private int resolveAutoPageWidth(CssContext cssCtx) {
        if (isUseLetterSize()) {
            return (int)LengthValue.calcFloatProportionalValue(
                    getStyle(),
                    FS_PAGE_WIDTH,
                    "8.5in",
                    8.5f,
                    CSSPrimitiveValue.CSS_IN,
                    0,
                    cssCtx);
        } else {
            return (int)LengthValue.calcFloatProportionalValue(
                    getStyle(),
                    FS_PAGE_WIDTH,
                    "210mm",
                    210.0f,
                    CSSPrimitiveValue.CSS_MM,
                    0,
                    cssCtx);
        }
    }

    private int resolveAutoPageHeight(CssContext cssCtx) {
        if (isUseLetterSize()) {
            return (int)LengthValue.calcFloatProportionalValue(
                    getStyle(),
                    FS_PAGE_HEIGHT,
                    "11in",
                    11.0f,
                    CSSPrimitiveValue.CSS_IN,
                    0,
                    cssCtx);
        } else {
            return (int)LengthValue.calcFloatProportionalValue(
                    getStyle(),
                    FS_PAGE_HEIGHT,
                    "297mm",
                    297.0f,
                    CSSPrimitiveValue.CSS_MM,
                    0,
                    cssCtx);
        }
    }

    public int getContentHeight(CssContext cssCtx) {
        int height = getHeight(cssCtx) - getMarginBorderPadding(cssCtx, TOP) - getMarginBorderPadding(cssCtx, BOTTOM);
        if (height <= 0) {
            throw new IllegalArgumentException(
                    "The content height cannot be zero or less.  Check your document margin definition.");
        }
        return height;
    }

    public int getContentWidth(CssContext cssCtx) {
        int width = getWidth(cssCtx) - getMarginBorderPadding(cssCtx, LEFT) - getMarginBorderPadding(cssCtx, RIGHT);
        if (width <= 0) {
            throw new IllegalArgumentException(
                    "The content width cannot be zero or less.  Check your document margin definition.");
        }
        return width;
    }


    public CalculatedStyle getStyle() {
        return _style;
    }

    public int getBottom() {
        return _bottom;
    }

    public int getTop() {
        return _top;
    }

    public int getPaintingBottom() {
        return _paintingBottom;
    }

    public void setPaintingBottom(int paintingBottom) {
        _paintingBottom = paintingBottom;
    }

    public int getPaintingTop() {
        return _paintingTop;
    }

    public void setPaintingTop(int paintingTop) {
        _paintingTop = paintingTop;
    }

    public Rectangle getScreenPaintingBounds(CssContext cssCtx, int additionalClearance) {
        return new Rectangle(
                additionalClearance, getPaintingTop(),
                getWidth(cssCtx), getPaintingBottom()-getPaintingTop());
    }

    public Rectangle getPrintPaintingBounds(CssContext cssCtx) {
        return new Rectangle(
                0, 0,
                getWidth(cssCtx), getHeight(cssCtx));
    }

    @CheckReturnValue
    public Rectangle getPagedViewClippingBounds(CssContext cssCtx, int additionalClearance) {
        return new Rectangle(
                additionalClearance + getMarginBorderPadding(cssCtx, LEFT),
                getPaintingTop() + getMarginBorderPadding(cssCtx, TOP),
                getContentWidth(cssCtx),
                getContentHeight(cssCtx));
    }

    @CheckReturnValue
    public Rectangle getPrintClippingBounds(CssContext cssCtx) {
        return new Rectangle(
                getMarginBorderPadding(cssCtx, LEFT),
                getMarginBorderPadding(cssCtx, TOP),
                getContentWidth(cssCtx),
                getContentHeight(cssCtx) - 1);
    }

    @CheckReturnValue
    public RectPropertySet getMargin(CssContext cssCtx) {
        return getStyle().getMarginRect(_outerPageWidth, cssCtx);
    }

    @CheckReturnValue
    private Rectangle getBorderEdge(int left, int top, CssContext cssCtx) {
        RectPropertySet margin = getMargin(cssCtx);
        return new Rectangle(left + (int) margin.left(),
                top + (int) margin.top(),
                getWidth(cssCtx) - (int) margin.left() - (int) margin.right(),
                getHeight(cssCtx) - (int) margin.top() - (int) margin.bottom());
    }

    public void paintBorder(RenderingContext c, int additionalClearance, PagedMode mode) {
        int top = switch (mode) {
            case PAGED_MODE_SCREEN -> getPaintingTop();
            case PAGED_MODE_PRINT -> 0;
        };
        c.getOutputDevice().paintBorder(c,
                getStyle(),
                getBorderEdge(additionalClearance, top, c),
                BorderPainter.ALL);
    }

    public void paintBackground(RenderingContext c, int additionalClearance, PagedMode mode) {
        Rectangle bounds = switch (mode) {
            case PAGED_MODE_SCREEN -> getScreenPaintingBounds(c, additionalClearance);
            case PAGED_MODE_PRINT -> getPrintPaintingBounds(c);
        };
        c.getOutputDevice().paintBackground(c, getStyle(), bounds, bounds, getStyle().getBorder(c));
    }

    public void paintMarginAreas(RenderingContext c, int additionalClearance, PagedMode mode) {
        for (int i = 0; i < MARGIN_AREA_DEFS.length; i++) {
            MarginAreaContainer container = _marginAreas[i];
            if (container != null) {
                TableBox table = container.table();
                Point p = container.area().getPaintingPosition(c, this, additionalClearance, mode);

                c.getOutputDevice().translate(p.x, p.y);
                table.getLayer().paint(c);
                c.getOutputDevice().translate(-p.x, -p.y);
            }
        }
    }

    public int getPageNo() {
        return _pageNo;
    }

    public int getOuterPageWidth() {
        return _outerPageWidth;
    }

    public int getMarginBorderPadding(CssContext cssCtx, Edge edge) {
        return getStyle().getMarginBorderPadding(cssCtx, getOuterPageWidth(), edge);
    }

    public PageInfo getPageInfo() {
        return _pageInfo;
    }

    @Nullable
    @CheckReturnValue
    public Element getMetadata() {
        return _metadata;
    }

    public void layout(LayoutContext c) {
        c.setPage(this);
        retrievePageMetadata(c);
        layoutMarginAreas(c);
    }

    // HACK Would much prefer to do this in ITextRenderer or ITextOutputDevice
    // but given the existing API, this is about the only place it can be done
    private void retrievePageMetadata(LayoutContext c) {
        List<PropertyDeclaration> props = getPageInfo().getXMPPropertyList();
        if (props != null && !props.isEmpty()) {
            for (PropertyDeclaration decl : props) {
                if (decl.getCSSName().equals(CSSName.CONTENT)) {
                    PropertyValue value = (PropertyValue) decl.getValue();
                    List<PropertyValue> values = value.getValues();
                    if (values.size() == 1) {
                        PropertyValue funcVal = values.get(0);
                        if (funcVal.getPropertyValueType() == VALUE_TYPE_FUNCTION) {
                            FSFunction func = funcVal.getFunction();
                            if (BoxBuilder.isElementFunction(func)) {
                                BlockBox metadata = BoxBuilder.getRunningBlock(c, funcVal);
                                if (metadata != null) {
                                    _metadata = metadata.getElement();
                                }
                            }
                        }
                    }
                    break;
                }
            }
        }
    }

    private void layoutMarginAreas(LayoutContext c) {
        RectPropertySet margin = getMargin(c);
        for (int i = 0; i < MARGIN_AREA_DEFS.length; i++) {
            MarginArea area = MARGIN_AREA_DEFS[i];

            Dimension dim = area.getLayoutDimension(c, this, margin);
            TableBox table = BoxBuilder.createMarginTable(
                    c, _pageInfo,
                    area.getMarginBoxNames(),
                    (int)dim.getHeight(),
                    area.getDirection());
            if (table != null) {
                table.setContainingBlock(new MarginBox(new Rectangle((int)dim.getWidth(), (int)dim.getHeight())));
                try {
                    c.setNoPageBreak(1);

                    c.reInit(false);
                    c.pushLayer(table);
                    c.getRootLayer().addPage(c);

                    table.layout(c);

                    c.popLayer();
                } finally {
                    c.setNoPageBreak(0);
                }
                _marginAreas[i] = new MarginAreaContainer(area, table);
            }
        }
    }

    public boolean isLeftPage() {
        return _pageNo % 2 != 0;
    }

    public boolean isRightPage() {
        return _pageNo % 2 == 0;
    }

    public void exportLeadingText(RenderingContext c, Writer writer) throws IOException {
        for (int i = 0; i < LEADING_TRAILING_SPLIT; i++) {
            MarginAreaContainer container = _marginAreas[i];
            if (container != null) {
                container.table().exportText(c, writer);
            }
        }
    }

    public void exportTrailingText(RenderingContext c, Writer writer) throws IOException {
        for (int i = LEADING_TRAILING_SPLIT; i < _marginAreas.length; i++) {
            MarginAreaContainer container = _marginAreas[i];
            if (container != null) {
                container.table().exportText(c, writer);
            }
        }
    }

    private record PageDimensions(int width, int height) {
    }

    private record MarginAreaContainer(MarginArea area, TableBox table) {
    }

    private abstract static sealed class MarginArea {
        private final MarginBoxName[] _marginBoxNames;

        public abstract Dimension getLayoutDimension(CssContext c, PageBox page, RectPropertySet margin);

        @CheckReturnValue
        public abstract Point getPaintingPosition(
                RenderingContext c, PageBox page, int additionalClearance, PagedMode mode);

        private MarginArea(MarginBoxName marginBoxName) {
            _marginBoxNames = new MarginBoxName[] { marginBoxName };
        }

        private MarginArea(MarginBoxName[] marginBoxNames) {
            _marginBoxNames = marginBoxNames;
        }

        public MarginBoxName[] getMarginBoxNames() {
            return _marginBoxNames;
        }

        public MarginDirection getDirection() {
            return HORIZONTAL;
        }
    }

    private static final class TopLeftCorner extends MarginArea {
        private TopLeftCorner() {
            super(MarginBoxName.TOP_LEFT_CORNER);
        }

        @Override
        public Dimension getLayoutDimension(CssContext c, PageBox page, RectPropertySet margin) {
            return new Dimension((int)margin.left(), (int)margin.top());
        }

        @Override
        public Point getPaintingPosition(RenderingContext c, PageBox page, int additionalClearance, PagedMode mode) {
            int top = switch (mode) {
                case PAGED_MODE_SCREEN -> page.getPaintingTop();
                case PAGED_MODE_PRINT -> 0;
            };
            return new Point(additionalClearance, top);
        }

    }

    private static final class TopRightCorner extends MarginArea {
        private TopRightCorner() {
            super(MarginBoxName.TOP_RIGHT_CORNER);
        }

        @Override
        public Dimension getLayoutDimension(CssContext c, PageBox page, RectPropertySet margin) {
            return new Dimension((int)margin.right(), (int)margin.top());
        }

        @Override
        public Point getPaintingPosition(RenderingContext c, PageBox page, int additionalClearance, PagedMode mode) {
            int left = additionalClearance + page.getWidth(c) - (int)page.getMargin(c).right();
            int top = switch (mode) {
                case PAGED_MODE_SCREEN -> page.getPaintingTop();
                case PAGED_MODE_PRINT -> 0;
            };
            return new Point(left, top);
        }
    }

    private static final class BottomRightCorner extends MarginArea {
        private BottomRightCorner() {
            super(MarginBoxName.BOTTOM_RIGHT_CORNER);
        }

        @Override
        public Dimension getLayoutDimension(CssContext c, PageBox page, RectPropertySet margin) {
            return new Dimension((int)margin.right(), (int)margin.bottom());
        }

        @Override
        public Point getPaintingPosition(RenderingContext c, PageBox page, int additionalClearance, PagedMode mode) {
            int left = additionalClearance + page.getWidth(c) - (int)page.getMargin(c).right();
            int top = switch (mode) {
                case PAGED_MODE_SCREEN -> page.getPaintingBottom() - (int)page.getMargin(c).bottom();
                case PAGED_MODE_PRINT -> page.getHeight(c) - (int)page.getMargin(c).bottom();
            };
            return new Point(left, top);
        }
    }

    private static final class BottomLeftCorner extends MarginArea {
        private BottomLeftCorner() {
            super(MarginBoxName.BOTTOM_LEFT_CORNER);
        }

        @Override
        public Dimension getLayoutDimension(CssContext c, PageBox page, RectPropertySet margin) {
            return new Dimension((int)margin.left(), (int)margin.bottom());
        }

        @Override
        public Point getPaintingPosition(RenderingContext c, PageBox page, int additionalClearance, PagedMode mode) {
            int top = switch (mode) {
                case PAGED_MODE_SCREEN -> page.getPaintingBottom() - (int)page.getMargin(c).bottom();
                case PAGED_MODE_PRINT -> page.getHeight(c) - (int)page.getMargin(c).bottom();
            };
            return new Point(additionalClearance, top);
        }
    }

    private static final class LeftMarginArea extends MarginArea {
        private LeftMarginArea() {
            super(new MarginBoxName[] {
                    MarginBoxName.LEFT_TOP,
                    MarginBoxName.LEFT_MIDDLE,
                    MarginBoxName.LEFT_BOTTOM });
        }

        @Override
        public Dimension getLayoutDimension(CssContext c, PageBox page, RectPropertySet margin) {
            return new Dimension((int)margin.left(), page.getContentHeight(c));
        }

        @Override
        public Point getPaintingPosition(RenderingContext c, PageBox page, int additionalClearance, PagedMode mode) {
            int top = switch (mode) {
                case PAGED_MODE_SCREEN -> page.getPaintingTop() + (int)page.getMargin(c).top();
                case PAGED_MODE_PRINT -> (int) page.getMargin(c).top();
            };
            return new Point(additionalClearance, top);
        }

        @Override
        public MarginDirection getDirection() {
            return VERTICAL;
        }
    }

    private static final class RightMarginArea extends MarginArea {
        private RightMarginArea() {
            super(new MarginBoxName[] {
                    MarginBoxName.RIGHT_TOP,
                    MarginBoxName.RIGHT_MIDDLE,
                    MarginBoxName.RIGHT_BOTTOM });
        }

        @Override
        public Dimension getLayoutDimension(CssContext c, PageBox page, RectPropertySet margin) {
            return new Dimension((int)margin.left(), page.getContentHeight(c));
        }

        @Override
        public Point getPaintingPosition(RenderingContext c, PageBox page, int additionalClearance, PagedMode mode) {
            int left = additionalClearance + page.getWidth(c) - (int)page.getMargin(c).right();
            int top = switch (mode) {
                case PAGED_MODE_SCREEN -> page.getPaintingTop() + (int)page.getMargin(c).top();
                case PAGED_MODE_PRINT -> (int) page.getMargin(c).top();
            };
            return new Point(left, top);
        }

        @Override
        public MarginDirection getDirection() {
            return VERTICAL;
        }
    }

    private static final class TopMarginArea extends MarginArea {
        private TopMarginArea() {
            super(new MarginBoxName[] {
                    MarginBoxName.TOP_LEFT,
                    MarginBoxName.TOP_CENTER,
                    MarginBoxName.TOP_RIGHT });
        }

        @Override
        public Dimension getLayoutDimension(CssContext c, PageBox page, RectPropertySet margin) {
            return new Dimension(page.getContentWidth(c), (int)margin.top());
        }

        @Override
        public Point getPaintingPosition(RenderingContext c, PageBox page, int additionalClearance, PagedMode mode) {
            int left = additionalClearance + (int)page.getMargin(c).left();
            int top = switch (mode) {
                case PAGED_MODE_SCREEN -> page.getPaintingTop();
                case PAGED_MODE_PRINT -> 0;
            };
            return new Point(left, top);
        }
    }

    private static final class BottomMarginArea extends MarginArea {
        private BottomMarginArea() {
            super(new MarginBoxName[] {
                    MarginBoxName.BOTTOM_LEFT,
                    MarginBoxName.BOTTOM_CENTER,
                    MarginBoxName.BOTTOM_RIGHT });
        }

        @Override
        public Dimension getLayoutDimension(CssContext c, PageBox page, RectPropertySet margin) {
            return new Dimension(page.getContentWidth(c), (int)margin.bottom());
        }

        @Override
        public Point getPaintingPosition(
                RenderingContext c, PageBox page, int additionalClearance, PagedMode mode) {
            int left = additionalClearance + (int)page.getMargin(c).left();
            int top = switch (mode) {
                case PAGED_MODE_SCREEN -> page.getPaintingBottom() - (int)page.getMargin(c).bottom();
                case PAGED_MODE_PRINT -> page.getHeight(c) - (int)page.getMargin(c).bottom();
            };
            return new Point(left, top);
        }
    }
}
