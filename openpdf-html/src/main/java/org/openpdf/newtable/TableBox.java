/*
 * {{{ header & license
 * Copyright (c) 2007 Wisconsin Court System
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
package org.openpdf.newtable;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;
import org.openpdf.css.constants.CSSName;
import org.openpdf.css.constants.IdentValue;
import org.openpdf.css.style.CalculatedStyle;
import org.openpdf.css.style.CssContext;
import org.openpdf.css.style.Length;
import org.openpdf.css.style.derived.BorderPropertySet;
import org.openpdf.css.style.derived.RectPropertySet;
import org.openpdf.layout.LayoutContext;
import org.openpdf.render.BlockBox;
import org.openpdf.render.Box;
import org.openpdf.render.ContentLimit;
import org.openpdf.render.ContentLimitContainer;
import org.openpdf.render.PageBox;
import org.openpdf.render.RenderingContext;
import org.openpdf.util.ArrayUtil;
import org.openpdf.util.XRLog;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

import static java.util.Collections.emptyList;
import static java.util.Comparator.comparingInt;
import static org.openpdf.css.style.Length.LengthType.PERCENT;
import static org.openpdf.css.style.Length.ZERO;

// Much of this code is directly inspired by (and even copied from)
// the equivalent code in KHTML (including the idea of "effective columns" to
// manage colspans and the details of the table layout algorithms).  Many kudos
// to the KHTML developers for making such an amazing piece of software!
public class TableBox extends BlockBox {
    private final List<ColumnData> _columns = new ArrayList<>();
    private int @Nullable [] _columnPos;
    @Nullable
    private TableLayout _tableLayout;

    @Nullable
    private List<TableColumn> _styleColumns;

    private int _pageClearance;

    private boolean _marginAreaRoot;

    @Nullable
    private ContentLimitContainer _contentLimitContainer;

    private int _extraSpaceTop;
    private int _extraSpaceBottom;

    public TableBox(@Nullable Element element, @Nullable CalculatedStyle style, boolean anonymous) {
        super(element, style, anonymous);
    }

    @Override
    public boolean isMarginAreaRoot() {
        return _marginAreaRoot;
    }

    public void setMarginAreaRoot(boolean marginAreaRoot) {
        _marginAreaRoot = marginAreaRoot;
    }

    @Override
    public BlockBox copyOf() {
        return new TableBox(getElement(), getStyle(), isAnonymous());
    }

    public void addStyleColumn(TableColumn col) {
        if (_styleColumns == null) {
            _styleColumns = new ArrayList<>();
        }
        _styleColumns.add(col);
    }

    public List<TableColumn> getStyleColumns() {
        return _styleColumns == null ? emptyList() : _styleColumns;
    }

    public int[] getColumnPos() {
        return ArrayUtil.cloneOrEmpty(_columnPos);
    }

    private void setColumnPos(int[] columnPos) {
        _columnPos = columnPos;
    }

    public int numEffCols() {
        return _columns.size();
    }

    public int spanOfEffCol(int effCol) {
        return _columns.get(effCol).getSpan();
    }

    public int colToEffCol(int col) {
        int c = 0;
        int i = 0;
        while (c < col && i < numEffCols()) {
            c += spanOfEffCol(i);
            i++;
        }
        return i;
    }

    public int effColToCol(int effCol) {
        int c = 0;
        for (int i = 0; i < effCol; i++) {
            c += spanOfEffCol(i);
        }
        return c;
    }

    public void appendColumn(int span) {
        ColumnData data = new ColumnData();
        data.setSpan(span);

        _columns.add(data);

        for (Box box : getChildren()) {
            TableSectionBox section = (TableSectionBox) box;
            section.extendGridToColumnCount(_columns.size());
        }
    }

    @Override
    public final void setStyle(CalculatedStyle style) {
        super.setStyle(style);

        if (isMarginAreaRoot()) {
            _tableLayout = new MarginTableLayout(this);
        } else if (style.isIdent(CSSName.TABLE_LAYOUT, IdentValue.AUTO) || style.isAutoWidth()) {
            _tableLayout = new AutoTableLayout(this);
        } else {
            _tableLayout = new FixedTableLayout(this);
        }
    }

    @Override
    public void calcMinMaxWidth(LayoutContext c) {
        if (! isMinMaxCalculated()) {
            recalcSections(c);
            if (getStyle().isCollapseBorders()) {
                calcBorders(c);
            }
            _tableLayout.calcMinMaxWidth(c);
            setMinMaxCalculated(true);
        }
    }

    public void splitColumn(int pos, int firstSpan) {
        ColumnData newColumn = new ColumnData();
        newColumn.setSpan(firstSpan);
        _columns.add(pos, newColumn);

        ColumnData leftOver = _columns.get(pos+1);
        leftOver.setSpan(leftOver.getSpan() - firstSpan);

        for (Box box : getChildren()) {
            TableSectionBox section = (TableSectionBox) box;
            section.splitColumn(pos);
        }
    }

    public int marginsBordersPaddingAndSpacing(CssContext c, boolean ignoreAutoMargins) {
        int result = 0;
        RectPropertySet margin = getMargin(c);
        if (! ignoreAutoMargins || ! getStyle().isAutoLeftMargin()) {
            result += (int)margin.left();
        }
        if (! ignoreAutoMargins || ! getStyle().isAutoRightMargin()) {
            result += (int)margin.right();
        }
        BorderPropertySet border = getBorder(c);
        result += (int)border.left() + (int)border.right();
        if (! getStyle().isCollapseBorders()) {
            RectPropertySet padding = getPadding(c);
            int hSpacing = getStyle().getBorderHSpacing(c);
            result += (int) (padding.left() + padding.right() + (numEffCols()+1) * hSpacing);
        }
        return result;
    }

    public List<ColumnData> getColumns() {
        return _columns;
    }

    private void recalcSections(LayoutContext c) {
        ensureChildren(c);
        for (Box box : getChildren()) {
            TableSectionBox section = (TableSectionBox) box;
            section.recalcCells(c);
        }
    }

    private void calcBorders(LayoutContext c) {
        ensureChildren(c);
        for (Box box : getChildren()) {
            TableSectionBox section = (TableSectionBox) box;
            section.calcBorders(c);
        }
    }

    @Override
    protected boolean isAllowHeightToShrink() {
        return false;
    }

    @Override
    public void layout(LayoutContext c) {
        calcMinMaxWidth(c);
        calcDimensions(c);
        calcWidth();
        calcPageClearance(c);

        // Recalc to pick up auto margins now that layout has been called on
        // containing block and the table has a content width
        if (! isAnonymous()) {
            setDimensionsCalculated(false);
            calcDimensions(c, getContentWidth());
        }

        _tableLayout.layout(c);

        setCellWidths(c);

        layoutTable(c);
    }

    @Override
    protected void resolveAutoMargins(LayoutContext c, int cssWidth, RectPropertySet padding,
                                      BorderPropertySet border) {
        // If our minimum width is greater than the calculated CSS width,
        // don't try to allocate any margin space to auto margins.  It
        // will just confuse the issue later when we expand the effective
        // table width to its minimum width.
        if (getMinWidth() <= getContentWidth() + marginsBordersPaddingAndSpacing(c, true)) {
            super.resolveAutoMargins(c, cssWidth, padding, border);
        } else {
            if (getStyle().isAutoLeftMargin()) {
                setMarginLeft(c, 0);
            }
            if (getStyle().isAutoRightMargin()) {
                setMarginRight(c, 0);
            }
        }
    }

    private void layoutTable(LayoutContext c) {
        boolean running = c.isPrint() && getStyle().isPaginateTable();
        int prevExtraTop = 0;
        int prevExtraBottom = 0;

        if (running) {
            prevExtraTop = c.getExtraSpaceTop();
            prevExtraBottom = c.getExtraSpaceBottom();

            c.setExtraSpaceTop(c.getExtraSpaceTop() +
                    (int)getPadding(c).top() +
                    (int)getBorder(c).top() +
                    getStyle().getBorderVSpacing(c));
            c.setExtraSpaceBottom(c.getExtraSpaceBottom() +
                    (int)getPadding(c).bottom() +
                    (int)getBorder(c).bottom() +
                    getStyle().getBorderVSpacing(c));
        }

        super.layout(c);

        if (running) {
            if (isNeedAnalyzePageBreaks()) {
                analyzePageBreaks(c);

                setExtraSpaceTop(0);
                setExtraSpaceBottom(0);
            } else {
                setExtraSpaceTop(c.getExtraSpaceTop() - prevExtraTop);
                setExtraSpaceBottom(c.getExtraSpaceBottom() - prevExtraBottom);
            }
            c.setExtraSpaceTop(prevExtraTop);
            c.setExtraSpaceBottom(prevExtraBottom);
        }
    }

    @Override
    protected void layoutChildren(LayoutContext c, int contentStart) {
        ensureChildren(c);
        // If we have a running footer, we need its dimensions right away
        boolean running = c.isPrint() && getStyle().isPaginateTable();
        if (running) {
            int headerHeight = layoutRunningHeader(c);
            int footerHeight = layoutRunningFooter(c);
            int spacingHeight = footerHeight == 0 ? 0 : getStyle().getBorderVSpacing(c);

            PageBox first = c.getRootLayer().getFirstPage(c, this);
            if (getAbsY() + getTy() + headerHeight + footerHeight + spacingHeight > first.getBottom()) {
                // XXX Performance problem here.  This forces the table
                // to move to the next page (which we want), but the initial
                // table layout run still completes (which we don't)
                setNeedPageClear(true);
            }
        }
        super.layoutChildren(c, contentStart);
    }

    private int layoutRunningHeader(LayoutContext c) {
        int result = 0;
        if (getChildCount() > 0) {
            TableSectionBox section = (TableSectionBox)getChild(0);
            if (section.isHeader()) {
                c.setNoPageBreak(c.getNoPageBreak() + 1);

                section.initContainingLayer(c);
                section.layout(c);

                c.setExtraSpaceTop(c.getExtraSpaceTop() + section.getHeight());

                result = section.getHeight();

                section.reset(c);

                c.setNoPageBreak(c.getNoPageBreak() - 1);
            }
        }

        return result;
    }

    private int layoutRunningFooter(LayoutContext c) {
        int result = 0;
        if (getChildCount() > 0) {
            TableSectionBox section = (TableSectionBox)getChild(getChildCount()-1);
            if (section.isFooter()) {
                c.setNoPageBreak(c.getNoPageBreak() + 1);

                section.initContainingLayer(c);
                section.layout(c);

                c.setExtraSpaceBottom(c.getExtraSpaceBottom() +
                        section.getHeight() +
                        getStyle().getBorderVSpacing(c));

                result = section.getHeight();

                section.reset(c);

                c.setNoPageBreak(c.getNoPageBreak() - 1);
            }
        }
        return result;
    }

    private boolean isNeedAnalyzePageBreaks() {
        Box b = getParent();
        while (b != null) {
            if (b.getStyle().isTable() && b.getStyle().isPaginateTable()) {
                return false;
            }

            b = b.getParent();
        }

        return true;
    }

    private void analyzePageBreaks(LayoutContext c) {
        analyzePageBreaks(c, null);
    }

    @Override
    public void analyzePageBreaks(LayoutContext c, @Nullable ContentLimitContainer container) {
        _contentLimitContainer = new ContentLimitContainer(c, getAbsY());
        _contentLimitContainer.setParent(container);

        if (container != null) {
            container.updateTop(c, getAbsY());
            container.updateBottom(c, getAbsY() + getHeight());
        }

        for (Box b : getChildren()) {
            b.analyzePageBreaks(c, _contentLimitContainer);
        }

        if (container != null && _contentLimitContainer.isContainsMultiplePages() &&
                (getExtraSpaceTop() > 0 || getExtraSpaceBottom() > 0)) {
            propagateExtraSpace(c, container, _contentLimitContainer, getExtraSpaceTop(), getExtraSpaceBottom());
        }
    }

    @Override
    public void paintBackground(RenderingContext c) {
        if (_contentLimitContainer == null) {
            super.paintBackground(c);
        } else if (getStyle().isVisible()) {
            c.getOutputDevice().paintBackground(
                    c, getStyle(), getContentLimitedBorderEdge(c), getPaintingBorderEdge(c),
                    getStyle().getBorder(c));
        }
    }

    @Override
    public void paintBorder(RenderingContext c) {
        if (_contentLimitContainer == null) {
            super.paintBorder(c);
        } else if (getStyle().isVisible()) {
            c.getOutputDevice().paintBorder(c, getStyle(), getContentLimitedBorderEdge(c), getBorderSides());
        }
    }

    private Rectangle getContentLimitedBorderEdge(RenderingContext c) {
        Rectangle result = getPaintingBorderEdge(c);

        ContentLimit limit = _contentLimitContainer.getContentLimit(c.getPageNo());

        if (limit == null) {
            XRLog.layout(Level.WARNING, "No content limit found");
        } else {
            if (limit.getTop() == ContentLimit.UNDEFINED ||
                    limit.getBottom() == ContentLimit.UNDEFINED) {
                return result;
            }

            RectPropertySet padding = getPadding(c);
            BorderPropertySet border = getBorder(c);

            int top;
            if (c.getPageNo() == _contentLimitContainer.getInitialPageNo()) {
                top = result.y;
            } else {
                top = limit.getTop() - (int)padding.top() -
                    (int)border.top() - getStyle().getBorderVSpacing(c);
                if (getChildCount() > 0) {
                    TableSectionBox section = (TableSectionBox)getChild(0);
                    if (section.isHeader()) {
                        top -= section.getHeight();
                    }
                }
            }

            int bottom;
            if (c.getPageNo() == _contentLimitContainer.getLastPageNo()) {
                bottom = result.y + result.height;
            } else {
                bottom = limit.getBottom() + (int)padding.bottom() +
                            (int)border.bottom() + getStyle().getBorderVSpacing(c);
                if (getChildCount() > 0) {
                    TableSectionBox section = (TableSectionBox)getChild(getChildCount()-1);
                    if (section.isFooter()) {
                        bottom += section.getHeight();
                    }
                }
            }

            result.y = top;
            result.height = bottom - top;

        }
        return result;
    }

    public void updateHeaderFooterPosition(RenderingContext c) {
        ContentLimit limit = _contentLimitContainer.getContentLimit(c.getPageNo());

        if (limit != null) {
            updateHeaderPosition(c, limit);
            updateFooterPosition(c, limit);
        }
    }

    private void updateHeaderPosition(RenderingContext c, ContentLimit limit) {
        if (limit.getTop() != ContentLimit.UNDEFINED ||
                c.getPageNo() == _contentLimitContainer.getInitialPageNo()) {
            if (getChildCount() > 0) {
                TableSectionBox section = (TableSectionBox)getChild(0);
                if (section.isHeader()) {
                    if (! section.isCapturedOriginalAbsY()) {
                        section.setOriginalAbsY(section.getAbsY());
                        section.setCapturedOriginalAbsY(true);
                    }

                    int newAbsY;
                    if (c.getPageNo() == _contentLimitContainer.getInitialPageNo()) {
                        newAbsY = section.getOriginalAbsY();
                    } else {
                        newAbsY = limit.getTop() -
                            getStyle().getBorderVSpacing(c) -
                            section.getHeight();
                    }

                    int diff = newAbsY - section.getAbsY();

                    if (diff != 0) {
                        section.setY(section.getY() + diff);
                        section.calcCanvasLocation();
                        section.calcChildLocations();
                        section.calcPaintingInfo(c, false);
                    }
                }
            }
        }
    }

    private void updateFooterPosition(RenderingContext c, ContentLimit limit) {
        if (limit.getBottom() != ContentLimit.UNDEFINED ||
                c.getPageNo() == _contentLimitContainer.getLastPageNo()) {
            if (getChildCount() > 0) {
                TableSectionBox section = (TableSectionBox)getChild(getChildCount()-1);
                if (section.isFooter()) {
                    if (! section.isCapturedOriginalAbsY()) {
                        section.setOriginalAbsY(section.getAbsY());
                        section.setCapturedOriginalAbsY(true);
                    }

                    int newAbsY;
                    if (c.getPageNo() == _contentLimitContainer.getLastPageNo()) {
                        newAbsY = section.getOriginalAbsY();
                    } else {
                        newAbsY = limit.getBottom();
                    }

                    int diff = newAbsY - section.getAbsY();

                    if (diff != 0) {
                        section.setY(section.getY() + diff);
                        section.calcCanvasLocation();
                        section.calcChildLocations();
                        section.calcPaintingInfo(c, false);
                    }
                }
            }
        }
    }

    private void calcPageClearance(LayoutContext c) {
        if (c.isPrint() && getStyle().isCollapseBorders()) {
            PageBox page = c.getRootLayer().getFirstPage(c, this);
            if (page != null) {
                TableRowBox row = getFirstRow();
                if (row != null) {
                    int spill = 0;
                    for (Box box : row.getChildren()) {
                        TableCellBox cell = (TableCellBox) box;
                        BorderPropertySet collapsed = cell.getCollapsedPaintingBorder();
                        int tmp = (int) collapsed.top() / 2;
                        if (tmp > spill) {
                            spill = tmp;
                        }
                    }

                    int borderTop = getAbsY() + (int)getMargin(c).top() - spill;
                    int delta = page.getTop() - borderTop;
                    if (delta > 0) {
                        setY(getY() + delta);
                        setPageClearance(delta);
                        calcCanvasLocation();
                        c.translate(0, delta);
                    }
                }
            }
        }
    }

    private void calcWidth() {
        if (getMinWidth() > getWidth()) {
            setContentWidth(getContentWidth() + getMinWidth() - getWidth());
        } else if (getStyle().isIdent(CSSName.WIDTH, IdentValue.AUTO) &&
                    getMaxWidth() < getWidth()) {
            setContentWidth(getContentWidth() - (getWidth() - getMaxWidth()));
        }
    }

    @Nullable
    public TableRowBox getFirstRow() {
        for (Box box : getChildren()) {
            TableSectionBox section = (TableSectionBox) box;
            if (section.getChildCount() > 0) {
                return (TableRowBox) section.getChild(0);
            }
        }

        return null;
    }

    @Nullable
    public TableRowBox getFirstBodyRow() {
        for (Box box : getChildren()) {
            TableSectionBox section = (TableSectionBox) box;
            if (section.isHeader() || section.isFooter()) {
                continue;
            }
            if (section.getChildCount() > 0) {
                return (TableRowBox) section.getChild(0);
            }
        }

        return null;
    }

    private void setCellWidths(LayoutContext c) {
        for (Box value : getChildren()) {
            BlockBox box = (BlockBox) value;
            if (box.getStyle().isTableSection()) {
                ((TableSectionBox) box).setCellWidths(c);
            }
        }
    }

    @Override
    protected void calcLayoutHeight(LayoutContext c, BorderPropertySet border,
                                    RectPropertySet margin, RectPropertySet padding) {
        super.calcLayoutHeight(c, border, margin, padding);

        if (getChildCount() > 0) {
            setHeight(getHeight() + getStyle().getBorderVSpacing(c));
        }
    }

    @Override
    public void reset(LayoutContext c) {
        super.reset(c);

        _contentLimitContainer = null;

        _tableLayout.reset();
    }

    @Override
    protected int getCSSWidth(CssContext c) {
        if (getStyle().isAutoWidth()) {
            return -1;
        } else {
            // XHTML 1.0 specifies that a table width refers to the border
            // width.  This can be removed if/when we support the box-sizing
            // property.
            int result = (int)getStyle().getFloatPropertyProportionalWidth(
                    CSSName.WIDTH, getContainingBlock().getContentWidth(), c);

            BorderPropertySet border = getBorder(c);
            result -= (int)border.left() + (int)border.right();
            if (! getStyle().isCollapseBorders()) {
                RectPropertySet padding = getPadding(c);
                result -= (int)padding.left() + (int)padding.right();
            }

            return result >= 0 ? result : -1;
        }
    }

    @Nullable
    @CheckReturnValue
    public TableColumn colElement(int col) {
        List<TableColumn> styleColumns = getStyleColumns();
        if (styleColumns.isEmpty()) {
            return null;
        }
        int cCol = 0;
        for (TableColumn colElem : styleColumns) {
            int span = colElem.getStyle().getColSpan();
            cCol += span;
            if (cCol > col) {
                return colElem;
            }
        }
        return null;
    }

    public Rectangle getColumnBounds(CssContext c, int col) {
        int effCol = colToEffCol(col);

        int hspacing = getStyle().getBorderHSpacing(c);
        int vspacing = getStyle().getBorderVSpacing(c);

        Rectangle result = getContentAreaEdge(getAbsX(), getAbsY(), c);

        result.y += vspacing;
        result.height -= vspacing*2;

        result.x += _columnPos[effCol] + hspacing;

        return result;
    }

    @Override
    public BorderPropertySet getBorder(CssContext cssCtx) {
        if (getStyle().isCollapseBorders()) {
            return BorderPropertySet.EMPTY_BORDER;
        } else {
            return super.getBorder(cssCtx);
        }
    }

    public int calcFixedHeightRowBottom(CssContext c) {
        if (! isAnonymous()) {
            int cssHeight = getCSSHeight(c);
            if (cssHeight != -1) {
                return getAbsY() + cssHeight
                    - (int)getBorder(c).bottom() - (int)getPadding(c).bottom()
                    - getStyle().getBorderVSpacing(c);
            }
        }

        return -1;
    }

    @Override
    protected boolean isMayCollapseMarginsWithChildren() {
        return false;
    }

    @Nullable
    protected TableSectionBox sectionAbove(
            TableSectionBox section, boolean skipEmptySections) {
        TableSectionBox prevSection = (TableSectionBox)section.getPreviousSibling();

        if (prevSection == null) {
            return null;
        }

        while (prevSection != null) {
            if (prevSection.numRows() > 0 || !skipEmptySections) {
                break;
            }
            prevSection = (TableSectionBox)prevSection.getPreviousSibling();
        }

        return prevSection;
    }

    @Nullable
    protected TableSectionBox sectionBelow(
            TableSectionBox section, boolean skipEmptySections) {
        TableSectionBox nextSection = (TableSectionBox)section.getNextSibling();

        if (nextSection == null) {
            return null;
        }

        while (nextSection != null) {
            if (nextSection.numRows() > 0 || !skipEmptySections) {
                break;
            }
            nextSection = (TableSectionBox)nextSection.getNextSibling();
        }

        return nextSection;
    }

    @Nullable
    protected TableCellBox cellAbove(TableCellBox cell) {
        // Find the section and row to look in
        int r = cell.getRow();
        final TableSectionBox section;
        int rAbove = 0;
        if (r > 0) {
            // cell is not in the first row, so use the above row in its own
            // section
            section = cell.getSection();
            rAbove = r - 1;
        } else {
            section = sectionAbove(cell.getSection(), true);
            if (section != null) {
                rAbove = section.numRows() - 1;
            }
        }

        // Look up the cell in the section's grid, which requires effective col
        // index
        return getTableCellBox(cell, section, rAbove);
    }

    protected TableCellBox cellBelow(TableCellBox cell) {
        // Find the section and row to look in
        int r = cell.getRow() + cell.getStyle().getRowSpan() - 1;
        final TableSectionBox section;
        int rBelow = 0;
        if (r < cell.getSection().numRows() - 1) {
            // The cell is not in the last row, so use the next row in the
            // section.
            section = cell.getSection();
            rBelow = r + 1;
        } else {
            section = sectionBelow(cell.getSection(), true);
            if (section != null)
                rBelow = 0;
        }

        // Look up the cell in the section's grid, which requires effective col
        // index
        return getTableCellBox(cell, section, rBelow);
    }

    @Nullable
    private TableCellBox getTableCellBox(TableCellBox cell, @Nullable TableSectionBox section, int rBelow) {
        if (section == null) {
            return null;
        }

        int effCol = colToEffCol(cell.getCol());
        TableCellBox belowCell;
        // If we hit a colspan back up to a real cell.
        do {
            belowCell = section.cellAt(rBelow, effCol);
            effCol--;
        } while (belowCell == TableCellBox.SPANNING_CELL && effCol >= 0);
        return (belowCell == TableCellBox.SPANNING_CELL) ? null : belowCell;
    }

    @Nullable
    protected TableCellBox cellLeft(TableCellBox cell) {
        TableSectionBox section = cell.getSection();
        int effCol = colToEffCol(cell.getCol());
        if (effCol == 0) {
            return null;
        }

        // If we hit a colspan back up to a real cell.
        TableCellBox prevCell;
        do {
            prevCell = section.cellAt(cell.getRow(), effCol - 1);
            effCol--;
        } while (prevCell == TableCellBox.SPANNING_CELL && effCol >= 0);
        return (prevCell == TableCellBox.SPANNING_CELL) ? null : prevCell;
    }

    @Nullable
    protected TableCellBox cellRight(TableCellBox cell) {
        int effCol = colToEffCol(cell.getCol() + cell.getStyle().getColSpan());
        if (effCol >= numEffCols()) {
            return null;
        }
        TableCellBox result = cell.getSection().cellAt(cell.getRow(), effCol);
        return (result == TableCellBox.SPANNING_CELL) ? null : result;
    }

    @Override
    public int calcInlineBaseline(CssContext c) {
        List<Box> children = getChildren();
        if (!children.isEmpty()) {
            TableSectionBox section = (TableSectionBox) children.get(0);
            List<Box> sectionChildren = section.getChildren();
            if (!sectionChildren.isEmpty()) {
                TableRowBox row = (TableRowBox) sectionChildren.get(0);
                return row.getAbsY() + row.getBaseline() - getAbsY();
            }
        }
        return getHeight();
    }


    @Override
    protected int getPageClearance() {
        return _pageClearance;
    }

    protected void setPageClearance(int pageClearance) {
        _pageClearance = pageClearance;
    }

    public boolean hasContentLimitContainer() {
        return _contentLimitContainer != null;
    }

    public int getExtraSpaceTop() {
        return _extraSpaceTop;
    }

    public void setExtraSpaceTop(int extraSpaceTop) {
        _extraSpaceTop = extraSpaceTop;
    }

    public int getExtraSpaceBottom() {
        return _extraSpaceBottom;
    }

    public void setExtraSpaceBottom(int extraSpaceBottom) {
        _extraSpaceBottom = extraSpaceBottom;
    }

    private interface TableLayout {
        void calcMinMaxWidth(LayoutContext c);
        void layout(LayoutContext c);
        void reset();
    }

    /**
     * A specialization of {@code AutoTableLayout} used for laying out the
     * tables used to approximate the margin box layout algorithm from CSS3
     * GCPM.
     */
    private static class MarginTableLayout extends AutoTableLayout {
        private MarginTableLayout(TableBox table) {
            super(table);
        }

        @Override
        protected int getMinColWidth() {
            return 0;
        }

        @Override
        public void calcMinMaxWidth(LayoutContext c) {
            super.calcMinMaxWidth(c);

            Layout[] layoutStruct = getLayoutStruct();

            if (layoutStruct.length == 3) {
                Layout center = layoutStruct[1];

                if (! (center.width().isVariable() && center.maxWidth() == 0)) {
                    if (layoutStruct[0].minWidth() > layoutStruct[2].minWidth()) {
                        layoutStruct[2] = layoutStruct[0];
                    } else if (layoutStruct[2].minWidth() > layoutStruct[0].minWidth()) {
                        layoutStruct[0] = layoutStruct[2];
                    } else {
                        Layout l = new Layout();
                        l.setMinWidth(Math.max(layoutStruct[0].minWidth(), layoutStruct[2].minWidth()));
                        l.setEffMinWidth(l.minWidth());
                        l.setMaxWidth(Math.max(layoutStruct[0].maxWidth(), layoutStruct[2].maxWidth()));
                        l.setEffMaxWidth(l.maxWidth());

                        layoutStruct[0] = l;
                        layoutStruct[2] = l;
                    }
                }
            }
        }
    }

    private static class FixedTableLayout implements TableLayout {
        private final TableBox _table;
        @Nullable
        private List<Length> _widths;

        private FixedTableLayout(TableBox table) {
            _table = table;
        }

        @Override
        public void reset() {
            _widths = null;
        }

        private void initWidths() {
            _widths = new ArrayList<>(_table.numEffCols());
            for (int i = 0; i < _table.numEffCols(); i++) {
                _widths.add(ZERO);
            }
        }

        private int calcWidthArray(LayoutContext c) {
            initWidths();

            TableBox table = _table;

            int cCol = 0;
            int nEffCols = table.numEffCols();
            int usedWidth = 0;

            for (TableColumn col : table.getStyleColumns()) {
                int span = col.getStyle().getColSpan();
                Length w = col.getStyle().asLength(c, CSSName.WIDTH);
                if (w.isVariable() && col.getParent() != null) {
                    w = col.getParent().getStyle().asLength(c, CSSName.WIDTH);
                }

                long effWidth = 0;
                if (w.isFixed() && w.value() > 0) {
                    effWidth = w.value();
                    effWidth = Math.min(effWidth, Length.MAX_WIDTH);
                }

                int usedSpan = 0;
                int i = 0;
                while (usedSpan < span) {
                    if (cCol + i >= nEffCols) {
                        table.appendColumn(span - usedSpan);
                        nEffCols++;
                        _widths.add(ZERO);
                    }
                    int eSpan = table.spanOfEffCol(cCol + i);
                    if ((w.isFixed() || w.isPercent()) && w.value() > 0) {
                        _widths.set(cCol + i, new Length(w.value() * eSpan, w.type()));
                        usedWidth += (int) (effWidth * eSpan);
                    }
                    usedSpan += eSpan;
                    i++;
                }
                cCol += i;
            }

            cCol = 0;
            TableRowBox firstRow = _table.getFirstRow();
            if (firstRow != null) {
                for (Box box : firstRow.getChildren()) {
                    TableCellBox cell = (TableCellBox) box;
                    Length w = cell.getOuterStyleWidth(c);
                    int span = cell.getStyle().getColSpan();
                    long effWidth = 0;
                    if (w.isFixed() && w.value() > 0) {
                        effWidth = w.value();
                    }

                    int usedSpan = 0;
                    int i = 0;
                    while (usedSpan < span) {
                        int eSpan = _table.spanOfEffCol(cCol + i);

                        Length columnWidth = _widths.get(cCol + i);
                        // only set if no col element has already set it.
                        if (columnWidth.isVariable() && !w.isVariable()) {
                            _widths.set(cCol + i, new Length(w.value() * eSpan, w.type()));
                            usedWidth += (int) (effWidth * eSpan);
                        }

                        usedSpan += eSpan;
                        i++;
                    }

                    cCol += i;
                }
            }

            return usedWidth;
        }

        @Override
        public void calcMinMaxWidth(LayoutContext c) {
            int bs = _table.marginsBordersPaddingAndSpacing(c, true);

            _table.calcDimensions(c);

            // Reset to allow layout to have another crack at this.  If we're
            // participating in a nested max/min-width calculation, the values
            // calculated above may be wrong and may need updating once our
            // parent has a width.
            _table.setDimensionsCalculated(false);

            int mw = calcWidthArray(c) + bs;
            _table.setMinWidth(Math.max(mw, _table.getWidth()));
            _table.setMaxWidth(_table.getMinWidth());

            boolean haveNonFixed = false;
            for (Length w : _widths) {
                if (!w.isFixed()) {
                    haveNonFixed = true;
                    break;
                }
            }

            if (haveNonFixed) {
                _table.setMaxWidth(Length.MAX_WIDTH);
            }
        }

        @Override
        public void layout(LayoutContext c) {
            int tableWidth = _table.getWidth() - _table.marginsBordersPaddingAndSpacing(c, false);
            int available = tableWidth;
            int nEffCols = _table.numEffCols();

            long[] calcWidth = new long[nEffCols];
            Arrays.fill(calcWidth, -1);

            // first assign fixed width
            for ( int i = 0; i < nEffCols; i++ ) {
                Length l = _widths.get(i);
                if ( l.isFixed() ) {
                    calcWidth[i] = l.value();
                    available -= (int) l.value();
                }
            }

            // assign percent width
            if ( available > 0 ) {
                int totalPercent = 0;
                for ( int i = 0; i < nEffCols; i++ ) {
                    Length l = _widths.get(i);
                    if ( l.isPercent() ) {
                        totalPercent += (int) l.value();
                    }
                }

                // calculate how much to distribute to percent cells.
                int base = tableWidth * totalPercent / 100;
                if (base > available) {
                    base = available;
                }

                for ( int i = 0; available > 0 && i < nEffCols; i++ ) {
                    Length l = _widths.get(i);
                    if ( l.isPercent() ) {
                        long w = base * l.value() / totalPercent;
                        available -= (int) w;
                        calcWidth[i] = w;
                    }
                }
            }

            // assign variable width
            if ( available > 0 ) {
                int totalVariable = 0;
                for ( int i = 0; i < nEffCols; i++ ) {
                    Length l = _widths.get(i);
                    if ( l.isVariable() ) {
                        totalVariable++;
                    }
                }

                for ( int i = 0; available > 0 && i < nEffCols; i++ ) {
                    Length l = _widths.get(i);
                    if ( l.isVariable() ) {
                        int w = available / totalVariable;
                        available -= w;
                        calcWidth[i] = w;
                        totalVariable--;
                    }
                }
            }

            for ( int i = 0; i < nEffCols; i++ ) {
                if ( calcWidth[i] < 0 ) {
                    calcWidth[i] = 0; // IE gives min 1 px...
                }
            }

            // spread extra space over columns
            if ( available > 0 ) {
                int total = nEffCols;
                // still have some width to spread
                int i = nEffCols;
                while ( i-- > 0 ) {
                    int w = available / total;
                    available -= w;
                    total--;
                    calcWidth[i] += w;
                }
            }

            int pos = 0;
            int hspacing = _table.getStyle().getBorderHSpacing(c);
            int[] columnPos = new int[nEffCols+1];
            for ( int i = 0; i < nEffCols; i++ ) {
                columnPos[i] = pos;
                pos += (int) (calcWidth[i] + hspacing);
            }

            columnPos[columnPos.length-1] = pos;

            _table.setColumnPos(columnPos);
        }
    }

    private static class AutoTableLayout implements TableLayout {
        private final TableBox _table;
        @Nullable
        private Layout[] _layoutStruct;
        @Nullable
        private List<TableCellBox> _spanCells;

        private AutoTableLayout(TableBox table) {
            _table = table;
        }

        @Override
        public void reset() {
            _layoutStruct = null;
            _spanCells = null;
        }

        @Nullable
        protected Layout[] getLayoutStruct() {
            return _layoutStruct;
        }

        private void fullRecalc(LayoutContext c) {
            _layoutStruct = new Layout[_table.numEffCols()];
            for (int i = 0; i < _layoutStruct.length; i++) {
                _layoutStruct[i] = new Layout();
                _layoutStruct[i].setMinWidth(getMinColWidth());
                _layoutStruct[i].setMaxWidth(getMinColWidth());
            }

            _spanCells = new ArrayList<>();

            TableBox table = _table;
            int nEffCols = table.numEffCols();

            int cCol = 0;
            for (TableColumn col : table.getStyleColumns()) {
                int span = col.getStyle().getColSpan();
                Length w = col.getStyle().asLength(c, CSSName.WIDTH);
                if (w.isVariable() && col.getParent() != null) {
                    w = col.getParent().getStyle().asLength(c, CSSName.WIDTH);
                }

                if ((w.isFixed() && w.value() == 0) || (w.isPercent() && w.value() == 0)) {
                    w = ZERO;
                }
                int cEffCol = table.colToEffCol(cCol);
                if (!w.isVariable() && span == 1 && cEffCol < nEffCols) {
                    if (table.spanOfEffCol(cEffCol) == 1) {
                        _layoutStruct[cEffCol].setWidth(w);
                        if (w.isFixed() && _layoutStruct[cEffCol].maxWidth() < w.value()) {
                            _layoutStruct[cEffCol].setMaxWidth(w.value());
                        }
                    }
                }
                cCol += span;

            }

            for (int i = 0; i < nEffCols; i++) {
                recalcColumn(c, i);
            }
        }

        protected int getMinColWidth() {
            return 1;
        }

        @SuppressWarnings("MissingCasesInEnumSwitch")
        private void recalcColumn(LayoutContext c, int effCol) {
            Layout l = _layoutStruct[effCol];

            // first we iterate over all rows.
            for (Box box : _table.getChildren()) {
                TableSectionBox section = (TableSectionBox) box;
                int numRows = section.numRows();
                for (int i = 0; i < numRows; i++) {
                    TableCellBox cell = section.cellAt(i, effCol);
                    if (cell == TableCellBox.SPANNING_CELL || cell == null) {
                        continue;
                    }
                    if (cell.getStyle().getColSpan() == 1) {
                        // A cell originates in this column. Ensure we have
                        // a min/max width of at least 1px for this column now.
                        l.setMinWidth(Math.max(l.minWidth(), getMinColWidth()));
                        l.setMaxWidth(Math.max(l.maxWidth(), getMinColWidth()));

                        cell.calcMinMaxWidth(c);
                        if (cell.getMinWidth() > l.minWidth()) {
                            l.setMinWidth(cell.getMinWidth());
                        }
                        if (cell.getMaxWidth() > l.maxWidth()) {
                            l.setMaxWidth(cell.getMaxWidth());
                        }

                        Length outerLength = cell.getOuterStyleOrColWidth(c);
                        Length w = new Length(Math.min(Length.MAX_WIDTH, Math.max(0, outerLength.value())), outerLength.type());

                        switch (w.type()) {
                            case FIXED:
                                if (w.value() > 0 && !l.width().isPercent()) {
                                    if (l.width().isFixed()) {
                                        if (w.value() > l.width().value()) {
                                            l.setWidth(w);
                                        }
                                    } else {
                                        l.setWidth(w);
                                    }
                                    if (w.value() > l.maxWidth()) {
                                        l.setMaxWidth(w.value());
                                    }
                                }
                                break;
                            case PERCENT:
                                if (w.value() > 0
                                        && (!l.width().isPercent() || w.value() > l.width().value())) {
                                    l.setWidth(w);
                                    break;
                                }
                        }
                    } else {
                        if (effCol == 0 || section.cellAt(i, effCol - 1) != cell) {
                            // This spanning cell originates in this column.
                            // Ensure we have a min/max width of at least 1px for this column now.
                            l.setMinWidth(Math.max(l.minWidth(), getMinColWidth()));
                            l.setMaxWidth(Math.max(l.maxWidth(), getMinColWidth()));

                            _spanCells.add(cell);
                        }
                    }
                }
            }

            l.setMaxWidth(Math.max(l.maxWidth(), l.minWidth()));
        }

        /*
         * This method takes care of colspans. effWidth is the same as width for
         * cells without colspans. If we have colspans, they get modified.
         */
        private long calcEffectiveWidth(LayoutContext c) {
            long tMaxWidth = 0;

            Layout[] layoutStruct = _layoutStruct;

            int nEffCols = layoutStruct.length;
            int hspacing = _table.getStyle().getBorderHSpacing(c);

            for (Layout layout : layoutStruct) {
                layout.setEffWidth(layout.width());
                layout.setEffMinWidth(layout.minWidth());
                layout.setEffMaxWidth(layout.maxWidth());
            }

            _spanCells.sort(comparingInt(c2 -> c2.getStyle().getColSpan()));

            for (TableCellBox cell : _spanCells) {
                cell.calcMinMaxWidth(c);

                int span = cell.getStyle().getColSpan();
                Length w = cell.getOuterStyleOrColWidth(c);
                if (w.value() == 0) {
                    w = ZERO; // make it Variable
                }

                int col = _table.colToEffCol(cell.getCol());
                int lastCol = col;
                int cMinWidth = cell.getMinWidth() + hspacing;
                int cMaxWidth = cell.getMaxWidth() + hspacing;
                int totalPercent = 0;
                int minWidth = 0;
                int maxWidth = 0;
                boolean allColsArePercent = true;
                boolean allColsAreFixed = true;
                boolean haveVariable = false;
                int fixedWidth = 0;

                while (lastCol < nEffCols && span > 0) {
                    switch (layoutStruct[lastCol].width().type()) {
                        case PERCENT:
                            totalPercent += (int) layoutStruct[lastCol].width().value();
                            allColsAreFixed = false;
                            break;
                        case FIXED:
                            if (layoutStruct[lastCol].width().value() > 0) {
                                fixedWidth += (int) layoutStruct[lastCol].width().value();
                                allColsArePercent = false;
                                break;
                            }
                            // fall through
                        case VARIABLE:
                            haveVariable = true;
                            // fall through
                        default:
                            // If the column is a percentage width, do not let the spanning cell overwrite the
                            // width value.  This caused a mis-rendering on amazon.com.
                            // Sample snippet:
                            // <table border=2 width=100%><
                            //   <tr><td>1</td><td colspan=2>2-3</tr>
                            //   <tr><td>1</td><td colspan=2 width=100%>2-3</td></tr>
                            // </table>
                            if (!layoutStruct[lastCol].effWidth().isPercent()) {
                                layoutStruct[lastCol].setEffWidth(ZERO);
                                allColsArePercent = false;
                            } else {
                                totalPercent += (int) layoutStruct[lastCol].effWidth().value();
                            }
                            allColsAreFixed = false;
                    }

                    span -= _table.spanOfEffCol(lastCol);
                    minWidth += (int) layoutStruct[lastCol].effMinWidth();
                    maxWidth += (int) layoutStruct[lastCol].effMaxWidth();
                    lastCol++;
                    cMinWidth -= hspacing;
                    cMaxWidth -= hspacing;
                }

                // adjust table max width if needed
                if (w.isPercent()) {
                    if (totalPercent > w.value() || allColsArePercent) {
                        // can't satisfy this condition, treat as variable
                        w = ZERO;
                    } else {
                        int spanMax = Math.max(maxWidth, cMaxWidth);
                        tMaxWidth = Math.max(tMaxWidth, spanMax * 100L / w.value());

                        // all non-percent columns in the span get percent
                        // values to sum up correctly.
                        long percentMissing = w.value() - totalPercent;
                        int totalWidth = 0;
                        for (int pos = col; pos < lastCol; pos++) {
                            if (!layoutStruct[pos].width().isPercent()) {
                                totalWidth += (int) layoutStruct[pos].effMaxWidth();
                            }
                        }

                        for (int pos = col; pos < lastCol && totalWidth > 0; pos++) {
                            if (!layoutStruct[pos].width().isPercent()) {
                                long percent = percentMissing * layoutStruct[pos].effMaxWidth()
                                        / totalWidth;
                                totalWidth -= (int) layoutStruct[pos].effMaxWidth();
                                percentMissing -= percent;
                                if (percent > 0) {
                                    layoutStruct[pos].setEffWidth(new Length(percent, PERCENT));
                                } else {
                                    layoutStruct[pos].setEffWidth(ZERO);
                                }
                            }
                        }
                    }
                }

                // make sure minWidth and maxWidth of the spanning cell are honoured
                if (cMinWidth > minWidth) {
                    if (allColsAreFixed) {
                        for (int pos = col; fixedWidth > 0 && pos < lastCol; pos++) {
                            long cWidth = Math.max(layoutStruct[pos].effMinWidth(), cMinWidth
                                    * layoutStruct[pos].width().value() / fixedWidth);
                            fixedWidth -= (int) layoutStruct[pos].width().value();
                            cMinWidth -= (int) cWidth;
                            layoutStruct[pos].setEffMinWidth(cWidth);
                        }
                    } else if (allColsArePercent) {
                        int maxw = maxWidth;
                        int minw = minWidth;
                        int cminw = cMinWidth;

                        for (int pos = col; maxw > 0 && pos < lastCol; pos++) {
                            if (layoutStruct[pos].effWidth().isPercent()
                                    && layoutStruct[pos].effWidth().value() > 0
                                    && fixedWidth <= cMinWidth) {
                                long cWidth = layoutStruct[pos].effMinWidth();
                                cWidth = Math.max(cWidth, cminw
                                        * layoutStruct[pos].effWidth().value() / totalPercent);
                                cWidth = Math.min(layoutStruct[pos].effMinWidth()
                                        + (cMinWidth - minw), cWidth);
                                maxw -= (int) layoutStruct[pos].effMaxWidth();
                                minw -= (int) layoutStruct[pos].effMinWidth();
                                cMinWidth -= (int) cWidth;
                                layoutStruct[pos].setEffMinWidth(cWidth);
                            }
                        }
                    } else {
                        int maxw = maxWidth;
                        int minw = minWidth;

                        // Give min to variable first, to fixed second, and to
                        // others third.
                        for (int pos = col; maxw > 0 && pos < lastCol; pos++) {
                            if (layoutStruct[pos].width().isFixed() && haveVariable
                                    && fixedWidth <= cMinWidth) {
                                long cWidth = Math.max(layoutStruct[pos].effMinWidth(),
                                        layoutStruct[pos].width().value());
                                fixedWidth -= (int) layoutStruct[pos].width().value();
                                minw -= (int) layoutStruct[pos].effMinWidth();
                                maxw -= (int) layoutStruct[pos].effMaxWidth();
                                cMinWidth -= (int) cWidth;
                                layoutStruct[pos].setEffMinWidth(cWidth);
                            }
                        }

                        for (int pos = col; maxw > 0 && pos < lastCol && minw < cMinWidth; pos++) {
                            if (!(layoutStruct[pos].width().isFixed() && haveVariable && fixedWidth <= cMinWidth)) {
                                long cWidth = Math.max(layoutStruct[pos].effMinWidth(), cMinWidth
                                        * layoutStruct[pos].effMaxWidth() / maxw);
                                cWidth = Math.min(layoutStruct[pos].effMinWidth()
                                        + (cMinWidth - minw), cWidth);

                                maxw -= (int) layoutStruct[pos].effMaxWidth();
                                minw -= (int) layoutStruct[pos].effMinWidth();
                                cMinWidth -= (int) cWidth;
                                layoutStruct[pos].setEffMinWidth(cWidth);
                            }
                        }
                    }
                }

                if (!w.isPercent()) {
                    if (cMaxWidth > maxWidth) {
                        for (int pos = col; maxWidth > 0 && pos < lastCol; pos++) {
                            long cWidth = Math.max(layoutStruct[pos].effMaxWidth(), cMaxWidth
                                    * layoutStruct[pos].effMaxWidth() / maxWidth);
                            maxWidth -= (int) layoutStruct[pos].effMaxWidth();
                            cMaxWidth -= (int) cWidth;
                            layoutStruct[pos].setEffMaxWidth(cWidth);
                        }
                    }
                } else {
                    for (int pos = col; pos < lastCol; pos++) {
                        layoutStruct[pos].setMaxWidth(Math.max(layoutStruct[pos].maxWidth(),
                                layoutStruct[pos].minWidth()));
                    }
                }
            }

            return tMaxWidth;
        }

        @Override
        public void calcMinMaxWidth(LayoutContext c) {
            TableBox table = _table;

            fullRecalc(c);

            Layout[] layoutStruct = _layoutStruct;

            long spanMaxWidth = calcEffectiveWidth(c);
            long minWidth = 0;
            long maxWidth = 0;
            long maxPercent = 0;
            long maxNonPercent = 0;

            int remainingPercent = 100;
            for (Layout layout : layoutStruct) {
                minWidth += layout.effMinWidth();
                maxWidth += layout.effMaxWidth();
                if (layout.effWidth().isPercent()) {
                    long percent = Math.min(layout.effWidth().value(), remainingPercent);
                    long pw = (layout.effMaxWidth() * 100) / Math.max(percent, 1);
                    remainingPercent -= (int) percent;
                    maxPercent = Math.max(pw, maxPercent);
                } else {
                    maxNonPercent += layout.effMaxWidth();
                }
            }

            maxNonPercent = (maxNonPercent * 100 + 50) / Math.max(remainingPercent, 1);
            maxWidth = Math.max(maxNonPercent, maxWidth);
            maxWidth = Math.max(maxWidth, maxPercent);
            maxWidth = Math.max(maxWidth, spanMaxWidth);

            int bs = table.marginsBordersPaddingAndSpacing(c, true);
            minWidth += bs;
            maxWidth += bs;

            Length tw = table.getStyle().asLength(c, CSSName.WIDTH);
            if (tw.isFixed() && tw.value() > 0) {
                table.calcDimensions(c);
                int width = table.getContentWidth() + table.marginsBordersPaddingAndSpacing(c, true);
                minWidth = Math.max(minWidth, width);
                maxWidth = minWidth;
            }

            table.setMaxWidth((int)Math.min(maxWidth, Length.MAX_WIDTH));
            table.setMinWidth((int)Math.min(minWidth, Length.MAX_WIDTH));
        }


        @Override
        public void layout(LayoutContext c) {
            TableBox table = _table;
            // table layout based on the values collected in the layout
            // structure.
            int tableWidth = table.getWidth() - table.marginsBordersPaddingAndSpacing(c, false);
            int available = tableWidth;
            int nEffCols = table.numEffCols();

            boolean havePercent = false;
            int numVariable = 0;
            int numFixed = 0;
            int totalVariable = 0;
            int totalFixed = 0;
            int totalPercent = 0;
            int allocVariable = 0;

            Layout[] layoutStruct = _layoutStruct;

            // fill up every cell with it's minWidth
            for (int i = 0; i < nEffCols; i++) {
                long w = layoutStruct[i].effMinWidth();
                layoutStruct[i].setCalcWidth(w);
                available -= (int) w;
                Length width = layoutStruct[i].effWidth();
                switch (width.type()) {
                    case PERCENT -> {
                        havePercent = true;
                        totalPercent += (int) width.value();
                    }
                    case FIXED -> {
                        numFixed++;
                        totalFixed += (int) layoutStruct[i].effMaxWidth();
                    }
                    case VARIABLE -> {
                        numVariable++;
                        totalVariable += (int) layoutStruct[i].effMaxWidth();
                        allocVariable += (int) w;
                    }
                }
            }

            // allocate width to percent cols
            if (available > 0 && havePercent) {
                for (int i = 0; i < nEffCols; i++) {
                    Length width = layoutStruct[i].effWidth();
                    if (width.isPercent()) {
                        long w = Math.max(layoutStruct[i].effMinWidth(), width.minWidth(tableWidth));
                        available += (int) (layoutStruct[i].calcWidth() - w);
                        layoutStruct[i].setCalcWidth(w);
                    }
                }
                if (totalPercent > 100) {
                    // remove over-allocated space from the last columns
                    int excess = tableWidth * (totalPercent - 100) / 100;
                    for (int i = nEffCols - 1; i >= 0; i--) {
                        if (layoutStruct[i].effWidth().isPercent()) {
                            long w = layoutStruct[i].calcWidth();
                            long reduction = Math.min(w, excess);
                            // the lines below might look inconsistent, but
                            // that's the way it's handled in mozilla
                            excess -= (int) reduction;
                            long newWidth = Math.max(layoutStruct[i].effMinWidth(), w - reduction);
                            available += (int) (w - newWidth);
                            layoutStruct[i].setCalcWidth(newWidth);
                            // qDebug("col %d: reducing to %d px
                            // (reduction=%d)", i, newWidth, reduction );
                        }
                    }
                }
            }

            // then allocate width to fixed cols
            if (available > 0) {
                for (int i = 0; i < nEffCols; ++i) {
                    Length width = layoutStruct[i].effWidth();
                    if (width.isFixed() && width.value() > layoutStruct[i].calcWidth()) {
                        available += (int) (layoutStruct[i].calcWidth() - width.value());
                        layoutStruct[i].setCalcWidth(width.value());
                    }
                }
            }

            // now satisfy variable
            if (available > 0 && numVariable > 0) {
                available += allocVariable; // this gets redistributed
                // qDebug("redistributing %dpx to %d variable columns.
                // totalVariable=%d", available, numVariable, totalVariable );
                for (int i = 0; i < nEffCols; i++) {
                    Length width = layoutStruct[i].effWidth();
                    if (width.isVariable() && totalVariable != 0) {
                        long w = Math.max(layoutStruct[i].calcWidth(), available
                                * layoutStruct[i].effMaxWidth() / totalVariable);
                        available -= (int) w;
                        totalVariable -= (int) layoutStruct[i].effMaxWidth();
                        layoutStruct[i].setCalcWidth(w);
                    }
                }
            }

            // spread over fixed columns
            if (available > 0 && numFixed > 0) {
                // still have some width to spread, distribute to fixed columns
                for (int i = 0; i < nEffCols; i++) {
                    Length width = layoutStruct[i].effWidth();
                    if (width.isFixed()) {
                        long w = available * layoutStruct[i].effMaxWidth() / totalFixed;
                        available -= (int) w;
                        totalFixed -= (int) layoutStruct[i].effMaxWidth();
                        layoutStruct[i].setCalcWidth(layoutStruct[i].calcWidth() + w);
                    }
                }
            }

            // spread over percent columns
            if (available > 0 && havePercent && totalPercent < 100) {
                // still have some width to spread, distribute weighted to
                // percent columns
                for (int i = 0; i < nEffCols; i++) {
                    Length width = layoutStruct[i].effWidth();
                    if (width.isPercent()) {
                        long w = available * width.value() / totalPercent;
                        available -= (int) w;
                        totalPercent -= (int) width.value();
                        layoutStruct[i].setCalcWidth(layoutStruct[i].calcWidth() + w);
                        if (available == 0 || totalPercent == 0) {
                            break;
                        }
                    }
                }
            }

            // spread over the rest
            if (available > 0) {
                int total = nEffCols;
                // still have some width to spread
                int i = nEffCols;
                while (i-- > 0) {
                    int w = available / total;
                    available -= w;
                    total--;
                    layoutStruct[i].setCalcWidth(layoutStruct[i].calcWidth() + w);
                }
            }

            // if we have over-allocated, reduce every cell according to the
            // difference between desired width and min-width
            // this seems to produce to the pixel exact results with IE. Wonder
            // is some of this also holds for width distributing.
            if (available < 0) {
                // Need to reduce cells with the following prioritization:
                // (1) Variable
                // (2) Relative
                // (3) Fixed
                // (4) Percent
                // This is basically the reverse of how we grew the cells.
                if (available < 0) {
                    int mw = 0;
                    for (int i = nEffCols - 1; i >= 0; i--) {
                        Length width = layoutStruct[i].effWidth();
                        if (width.isVariable())
                            mw += (int) (layoutStruct[i].calcWidth() - layoutStruct[i].effMinWidth());
                    }

                    for (int i = nEffCols - 1; i >= 0 && mw > 0; i--) {
                        Length width = layoutStruct[i].effWidth();
                        if (width.isVariable()) {
                            long minMaxDiff = layoutStruct[i].calcWidth()
                                    - layoutStruct[i].effMinWidth();
                            long reduce = available * minMaxDiff / mw;
                            layoutStruct[i].setCalcWidth(layoutStruct[i].calcWidth() + reduce);
                            available -= (int) reduce;
                            mw -= (int) minMaxDiff;
                            if (available >= 0)
                                break;
                        }
                    }
                }

                if (available < 0) {
                    int mw = 0;
                    for (int i = nEffCols - 1; i >= 0; i--) {
                        Length width = layoutStruct[i].effWidth();
                        if (width.isFixed())
                            mw += (int) (layoutStruct[i].calcWidth() - layoutStruct[i].effMinWidth());
                    }

                    for (int i = nEffCols - 1; i >= 0 && mw > 0; i--) {
                        Length width = layoutStruct[i].effWidth();
                        if (width.isFixed()) {
                            long minMaxDiff = layoutStruct[i].calcWidth()
                                    - layoutStruct[i].effMinWidth();
                            long reduce = available * minMaxDiff / mw;
                            layoutStruct[i].setCalcWidth(layoutStruct[i].calcWidth() + reduce);
                            available -= (int) reduce;
                            mw -= (int) minMaxDiff;
                            if (available >= 0)
                                break;
                        }
                    }
                }

                if (available < 0) {
                    int mw = 0;
                    for (int i = nEffCols - 1; i >= 0; i--) {
                        Length width = layoutStruct[i].effWidth();
                        if (width.isPercent())
                            mw += (int) (layoutStruct[i].calcWidth() - layoutStruct[i].effMinWidth());
                    }

                    for (int i = nEffCols - 1; i >= 0 && mw > 0; i--) {
                        Length width = layoutStruct[i].effWidth();
                        if (width.isPercent()) {
                            long minMaxDiff = layoutStruct[i].calcWidth()
                                    - layoutStruct[i].effMinWidth();
                            long reduce = available * minMaxDiff / mw;
                            layoutStruct[i].setCalcWidth(layoutStruct[i].calcWidth() + reduce);
                            available -= (int) reduce;
                            mw -= (int) minMaxDiff;
                            if (available >= 0)
                                break;
                        }
                    }
                }
            }

            int pos = 0;
            int hspacing = _table.getStyle().getBorderHSpacing(c);
            int[] columnPos = new int[nEffCols + 1];
            for (int i = 0; i < nEffCols; i++) {
                columnPos[i] = pos;
                pos += (int) (layoutStruct[i].calcWidth() + hspacing);
            }

            columnPos[columnPos.length - 1] = pos;

            _table.setColumnPos(columnPos);
        }

        protected static class Layout {
            private Length _width = ZERO;
            private Length _effWidth = ZERO;
            private long _minWidth = 1;
            private long _maxWidth = 1;
            private long _effMinWidth;
            private long _effMaxWidth;
            private long _calcWidth;

            public Layout() {
            }

            public Length width() {
                return _width;
            }

            public void setWidth(Length l) {
                _width = l;
            }

            public Length effWidth() {
                return _effWidth;
            }

            public void setEffWidth(Length l) {
                _effWidth = l;
            }

            public long minWidth() {
                return _minWidth;
            }

            public void setMinWidth(long i) {
                _minWidth = i;
            }

            public long maxWidth() {
                return _maxWidth;
            }

            public void setMaxWidth(long i) {
                _maxWidth = i;
            }

            public long effMinWidth() {
                return _effMinWidth;
            }

            public void setEffMinWidth(long i) {
                _effMinWidth = i;
            }

            public long effMaxWidth() {
                return _effMaxWidth;
            }

            public void setEffMaxWidth(long i) {
                _effMaxWidth = i;
            }

            public long calcWidth() {
                return _calcWidth;
            }

            public void setCalcWidth(long i) {
                _calcWidth = i;
            }
        }
    }
}
