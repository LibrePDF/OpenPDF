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
import org.openpdf.layout.CollapsedBorderSide;
import org.openpdf.layout.LayoutContext;
import org.openpdf.render.BlockBox;
import org.openpdf.render.BorderPainter;
import org.openpdf.render.Box;
import org.openpdf.render.ContentLimit;
import org.openpdf.render.ContentLimitContainer;
import org.openpdf.render.PageBox;
import org.openpdf.render.RenderingContext;

import java.awt.*;
import java.util.List;
import java.util.Set;

public class TableCellBox extends BlockBox {
    public static final TableCellBox SPANNING_CELL = new TableCellBox(null, null, false);

    private int _row;
    private int _col;

    @Nullable
    private TableBox _table;
    @Nullable
    private TableSectionBox _section;

    private BorderPropertySet _collapsedLayoutBorder;
    private BorderPropertySet _collapsedPaintingBorder;

    private CollapsedBorderValue _collapsedBorderTop;
    private CollapsedBorderValue _collapsedBorderRight;
    private CollapsedBorderValue _collapsedBorderBottom;
    private CollapsedBorderValue _collapsedBorderLeft;

    // 'double', 'solid', 'dashed', 'dotted', 'ridge', 'outset', 'groove', and the lowest: 'inset'.
    private static final int[] BORDER_PRIORITIES = new int[IdentValue.getIdentCount()];

    static {
        BORDER_PRIORITIES[IdentValue.DOUBLE.FS_ID] = 1;
        BORDER_PRIORITIES[IdentValue.SOLID.FS_ID] = 2;
        BORDER_PRIORITIES[IdentValue.DASHED.FS_ID] = 3;
        BORDER_PRIORITIES[IdentValue.DOTTED.FS_ID] = 4;
        BORDER_PRIORITIES[IdentValue.RIDGE.FS_ID] = 5;
        BORDER_PRIORITIES[IdentValue.OUTSET.FS_ID] = 6;
        BORDER_PRIORITIES[IdentValue.GROOVE.FS_ID] = 7;
        BORDER_PRIORITIES[IdentValue.INSET.FS_ID] = 8;
    }

    private static final int BCELL = 10;
    private static final int BROW = 9;
    private static final int BROWGROUP = 8;
    private static final int BCOL = 7;
    private static final int BTABLE = 6;

    public TableCellBox(@Nullable Element source, @Nullable CalculatedStyle style, boolean anonymous) {
        super(source, style, anonymous);
    }

    @Override
    public BlockBox copyOf() {
        return new TableCellBox(getElement(), getStyle(), isAnonymous());
    }

    @Override
    public BorderPropertySet getBorder(CssContext cssCtx) {
        if (getTable().getStyle().isCollapseBorders()) {
            // Should always be non-null, but might not be if layout code crashed
            return _collapsedLayoutBorder == null ?
                    BorderPropertySet.EMPTY_BORDER : _collapsedLayoutBorder;
        } else {
            return super.getBorder(cssCtx);
        }
    }

    public void calcCollapsedBorder(CssContext c) {
        CollapsedBorderValue top = collapsedTopBorder(c);
        CollapsedBorderValue right = collapsedRightBorder(c);
        CollapsedBorderValue bottom = collapsedBottomBorder(c);
        CollapsedBorderValue left = collapsedLeftBorder(c);

        _collapsedPaintingBorder = new BorderPropertySet(top, right, bottom, left);

        // Give the extra pixel to top and left.
        _collapsedBorderTop = top.withWidth((top.width() + 1) / 2);
        _collapsedBorderRight = right.withWidth(right.width() / 2);
        _collapsedBorderBottom = bottom.withWidth(bottom.width() / 2);
        _collapsedBorderLeft = left.withWidth((left.width() + 1) / 2);
        _collapsedLayoutBorder = new BorderPropertySet(_collapsedBorderTop, _collapsedBorderRight, _collapsedBorderBottom, _collapsedBorderLeft);
    }

    public int getCol() {
        return _col;
    }

    public void setCol(int col) {
        _col = col;
    }

    public int getRow() {
        return _row;
    }

    public void setRow(int row) {
        _row = row;
    }

    @Override
    public void layout(LayoutContext c) {
        super.layout(c);
    }

    @Nullable
    public TableBox getTable() {
        // cell -> row -> section -> table
        if (_table == null) {
            _table = (TableBox)getParent().getParent().getParent();
        }
        return _table;
    }

    @Nullable
    protected TableSectionBox getSection() {
        if (_section == null) {
            _section = (TableSectionBox)getParent().getParent();
        }
        return _section;
    }

    public Length getOuterStyleWidth(CssContext c) {
        Length result = getStyle().asLength(c, CSSName.WIDTH);
        if (result.isVariable() || result.isPercent()) {
            return result;
        }

        int bordersAndPadding = 0;
        BorderPropertySet border = getBorder(c);
        bordersAndPadding += (int)border.left() + (int)border.right();

        RectPropertySet padding = getPadding(c);
        bordersAndPadding += (int)padding.left() + (int)padding.right();

        return new Length(result.value() + bordersAndPadding, result.type());
    }

    @CheckReturnValue
    public Length getOuterStyleOrColWidth(CssContext c) {
        Length result = getOuterStyleWidth(c);
        if (getStyle().getColSpan() > 1 || ! result.isVariable()) {
            return result;
        }
        TableColumn col = getTable().colElement(getCol());
        if (col != null) {
            // XXX Need to add in collapsed borders from cell (if collapsing borders)
            result = col.getStyle().asLength(c, CSSName.WIDTH);
        }
        return result;
    }

    public void setLayoutWidth(LayoutContext c, int width) {
        calcDimensions(c);

        setContentWidth(width - getLeftMBP() - getRightMBP());
    }

    @Override
    public boolean isAutoHeight() {
        return getStyle().isAutoHeight() || ! getStyle().hasAbsoluteUnit(CSSName.HEIGHT);
    }

    @Override
    public int calcBaseline(LayoutContext c) {
        int result = super.calcBaseline(c);
        if (result != NO_BASELINE) {
            return result;
        } else {
            Rectangle contentArea = getContentAreaEdge(getAbsX(), getAbsY(), c);
            return (int)contentArea.getY();
        }
    }

    public int calcBlockBaseline(LayoutContext c) {
        return super.calcBaseline(c);
    }

    public void moveContent(final int deltaY) {
        for (int i = 0; i < getChildCount(); i++) {
            Box b = getChild(i);
            b.setY(b.getY() + deltaY);
        }

        getPersistentBFC().getFloatManager().performFloatOperation(
                floater -> floater.setY(floater.getY() + deltaY));

        calcChildLocations();
    }

    public boolean isPageBreaksChange(LayoutContext c, int posDeltaY) {
        if (! c.isPageBreaksAllowed()) {
            return false;
        }

        PageBox page = c.getRootLayer().getFirstPage(c, this);

        int bottomEdge = getAbsY() + getChildrenHeight();

        return page != null && (bottomEdge >= page.getBottom() - c.getExtraSpaceBottom() ||
                    bottomEdge + posDeltaY >= page.getBottom() - c.getExtraSpaceBottom());
    }

    public IdentValue getVerticalAlign() {
        IdentValue val = getStyle().getIdent(CSSName.VERTICAL_ALIGN);

        if (val == IdentValue.TOP || val == IdentValue.MIDDLE || val == IdentValue.BOTTOM) {
            return val;
        } else {
            return IdentValue.BASELINE;
        }
    }

    private boolean isPaintBackgroundsAndBorders() {
        boolean showEmpty = getStyle().isShowEmptyCells();
        // XXX Not quite right, but good enough for now
        // (e.g. absolute boxes will be counted as content here when the spec
        // says the cell should be treated as empty).
        return showEmpty || getChildrenContentType() != ContentType.EMPTY;

    }

    @Override
    public void paintBackground(RenderingContext c) {
        if (isPaintBackgroundsAndBorders() && getStyle().isVisible()) {
            Rectangle bounds;
            if (c.isPrint() && getTable().getStyle().isPaginateTable()) {
                bounds = getContentLimitedBorderEdge(c);
            } else {
                bounds = getPaintingBorderEdge(c);
            }

            if (bounds != null) {
                paintBackgroundStack(c, bounds);
            }
        }
    }

    private void paintBackgroundStack(RenderingContext c, Rectangle bounds) {
        Rectangle imageContainer;

        BorderPropertySet border = getStyle().getBorder(c);
        TableColumn column = getTable().colElement(getCol());
        if (column != null) {
            c.getOutputDevice().paintBackground(
                    c, column.getStyle(),
                    bounds, getTable().getColumnBounds(c, getCol()),
                    border);
        }

        Box row = getParent();
        Box section = row.getParent();

        CalculatedStyle tableStyle = getTable().getStyle();

        CalculatedStyle sectionStyle = section.getStyle();

        imageContainer = section.getPaintingBorderEdge(c);
        imageContainer.y += tableStyle.getBorderVSpacing(c);
        imageContainer.height -= tableStyle.getBorderVSpacing(c);
        imageContainer.x += tableStyle.getBorderHSpacing(c);
        imageContainer.width -= 2*tableStyle.getBorderHSpacing(c);

        c.getOutputDevice().paintBackground(c, sectionStyle, bounds, imageContainer, sectionStyle.getBorder(c));

        CalculatedStyle rowStyle = row.getStyle();

        imageContainer = row.getPaintingBorderEdge(c);
        imageContainer.x += tableStyle.getBorderHSpacing(c);
        imageContainer.width -= 2*tableStyle.getBorderHSpacing(c);

        c.getOutputDevice().paintBackground(c, rowStyle, bounds, imageContainer, rowStyle.getBorder(c));
        c.getOutputDevice().paintBackground(c, getStyle(), bounds, getPaintingBorderEdge(c), border);
    }

    @Override
    public void paintBorder(RenderingContext c) {
        if (isPaintBackgroundsAndBorders() && ! hasCollapsedPaintingBorder()) {
            // Collapsed table borders are painted separately
            if (c.isPrint() && getTable().getStyle().isPaginateTable() && getStyle().isVisible()) {
                Rectangle bounds = getContentLimitedBorderEdge(c);
                if (bounds != null) {
                    c.getOutputDevice().paintBorder(c, getStyle(), bounds, getBorderSides());
                }
            } else {
                super.paintBorder(c);
            }
        }
    }

    public void paintCollapsedBorder(RenderingContext c, int side) {
        c.getOutputDevice().paintCollapsedBorder(
                c, getCollapsedPaintingBorder(), getCollapsedBorderBounds(c), side);
    }

    private Rectangle getContentLimitedBorderEdge(RenderingContext c) {
        Rectangle result = getPaintingBorderEdge(c);

        TableSectionBox section = getSection();
        if (section.isHeader() || section.isFooter()) {
            return result;
        }

        ContentLimitContainer contentLimitContainer = ((TableRowBox)getParent()).getContentLimitContainer();
        ContentLimit limit = contentLimitContainer != null ? contentLimitContainer.getContentLimit(c.getPageNo()) : null;

        if (limit == null) {
            return null;
        } else {
            if (limit.getTop() == ContentLimit.UNDEFINED ||
                    limit.getBottom() == ContentLimit.UNDEFINED) {
                return result;
            }

            int top;
            if (c.getPageNo() == contentLimitContainer.getInitialPageNo()) {
                top = result.y;
            } else {
                top = limit.getTop() - ((TableRowBox)getParent()).getExtraSpaceTop() ;
            }

            int bottom;
            if (c.getPageNo() == contentLimitContainer.getLastPageNo()) {
                bottom = result.y + result.height;
            } else {
                bottom = limit.getBottom() + ((TableRowBox)getParent()).getExtraSpaceBottom();
            }

            result.y = top;
            result.height = bottom - top;

            return result;
        }
    }

    @CheckReturnValue
    @Override
    public Rectangle getChildrenClipEdge(RenderingContext c) {
        if (c.isPrint() && getTable().getStyle().isPaginateTable()) {
            Rectangle bounds = getContentLimitedBorderEdge(c);
            if (bounds != null) {
                BorderPropertySet border = getBorder(c);
                RectPropertySet padding = getPadding(c);
                bounds.y += (int)border.top() + (int)padding.top();
                bounds.height -= (int)border.height() + (int)padding.height();
                return bounds;
            }
        }

        return super.getChildrenClipEdge(c);
    }

    @Override
    protected boolean isFixedWidthAdvisoryOnly() {
        return getTable().getStyle().isIdent(CSSName.TABLE_LAYOUT, IdentValue.AUTO);
    }

    @Override
    protected boolean isSkipWhenCollapsingMargins() {
        return true;
    }

    // The following rules apply for resolving conflicts and figuring out which
    // border
    // to use.
    // (1) Borders with the 'border-style' of 'hidden' take precedence over all
    // other conflicting
    // borders. Any border with this value suppresses all borders at this
    // location.
    // (2) Borders with a style of 'none' have the lowest priority. Only if the
    // border properties of all
    // the elements meeting at this edge are 'none' will the border be omitted
    // (but note that 'none' is
    // the default value for the border style.)
    // (3) If none of the styles are 'hidden' and at least one of them is not
    // 'none', then narrow borders
    // are discarded in favor of wider ones. If several have the same
    // 'border-width' then styles are preferred
    // in this order: 'double', 'solid', 'dashed', 'dotted', 'ridge', 'outset',
    // 'groove', and the lowest: 'inset'.
    // (4) If border styles differ only in color, then a style set on a cell
    // wins over one on a row,
    // which wins over a row group, column, column group and, lastly, table. It
    // is undefined which color
    // is used when two elements of the same type disagree.
    public static CollapsedBorderValue compareBorders(
            CollapsedBorderValue border1, CollapsedBorderValue border2, boolean returnNullOnEqual) {
        // Sanity check the values passed in.  If either is null, return the other.
        if (!border2.defined()) {
            return border1;
        }

        if (!border1.defined()) {
            return border2;
        }

        // Rule #1 above.
        if (border1.style() == IdentValue.HIDDEN)
        {
            return border1;
        }
        if (border2.style() == IdentValue.HIDDEN)
        {
            return border2;
        }

        // Rule #2 above. A style of 'none' has the lowest priority and always loses
        // to any other border.
        if (border2.style() == IdentValue.NONE) {
            return border1;
        }

        if (border1.style() == IdentValue.NONE) {
            return border2;
        }

        // The first part of rule #3 above. Wider borders win.
        if (border1.width() != border2.width()) {
            return border1.width() > border2.width() ? border1 : border2;
        }

        // The borders have equal width. Sort by border style.
        if (border1.style() != border2.style()) {
            return BORDER_PRIORITIES[border1.style().FS_ID] >
                BORDER_PRIORITIES[border2.style().FS_ID] ? border1 : border2;
        }

        // The border have the same width and style. Rely on precedence (cell
        // over row group, etc.)
        if (returnNullOnEqual && border1.precedence() == border2.precedence()) {
            return null;
        } else {
            return border1.precedence() >= border2.precedence() ? border1 : border2;
        }
    }

    private static CollapsedBorderValue compareBorders(
            CollapsedBorderValue border1, CollapsedBorderValue border2) {
        return compareBorders(border1, border2, false);
    }

    private CollapsedBorderValue collapsedLeftBorder(CssContext c) {
        BorderPropertySet border = getStyle().getBorder(c);
        // For border left, we need to check, in order of precedence:
        // (1) Our left border.
        CollapsedBorderValue result = CollapsedBorderValue.borderLeft(border, BCELL);

        // (2) The previous cell's right border.
        TableCellBox prevCell = getTable().cellLeft(this);
        if (prevCell != null) {
            result = compareBorders(
                    result, CollapsedBorderValue.borderRight(prevCell.getStyle().getBorder(c), BCELL));
            if (result.hidden()) {
                return result;
            }
        } else if (getCol() == 0) {
            // (3) Our row's left border.
            result = compareBorders(
                    result, CollapsedBorderValue.borderLeft(getParent().getStyle().getBorder(c), BROW));
            if (result.hidden()) {
                return result;
            }

            // (4) Our row group's left border.
            result = compareBorders(
                    result, CollapsedBorderValue.borderLeft(getSection().getStyle().getBorder(c), BROWGROUP));
            if (result.hidden()) {
                return result;
            }
        }

        // (5) Our column's left border.
        TableColumn colElt = getTable().colElement(getCol());
        if (colElt != null) {
            result = compareBorders(
                    result, CollapsedBorderValue.borderLeft(colElt.getStyle().getBorder(c), BCOL));
            if (result.hidden()) {
                return result;
            }
        }

        // (6) The previous column's right border.
        if (getCol() > 0) {
            colElt = getTable().colElement(getCol() - 1);
            if (colElt != null) {
                result = compareBorders(
                        result, CollapsedBorderValue.borderRight(colElt.getStyle().getBorder(c), BCOL));
                if (result.hidden()) {
                    return result;
                }
            }
        }

        if (getCol() == 0) {
            // (7) The table's left border.
            result = compareBorders(
                    result, CollapsedBorderValue.borderLeft(getTable().getStyle().getBorder(c), BTABLE));
            if (result.hidden()) {
                return result;
            }
        }

        return result;
    }

    private CollapsedBorderValue collapsedRightBorder(CssContext c) {
        TableBox tableElt = getTable();
        boolean inLastColumn = false;
        int effCol = tableElt.colToEffCol(getCol() + getStyle().getColSpan() - 1);
        if (effCol == tableElt.numEffCols() - 1) {
            inLastColumn = true;
        }

        // For border right, we need to check, in order of precedence:
        // (1) Our right border.
        CollapsedBorderValue result =
            CollapsedBorderValue.borderRight(getStyle().getBorder(c), BCELL);

        // (2) The next cell's left border.
        if (!inLastColumn) {
            TableCellBox nextCell = tableElt.cellRight(this);
            if (nextCell != null) {
                result = compareBorders(result,
                        CollapsedBorderValue.borderLeft(nextCell.getStyle().getBorder(c), BCELL));
                if (result.hidden()) {
                    return result;
                }
            }
        } else {
            // (3) Our row's right border.
            result = compareBorders(result,
                    CollapsedBorderValue.borderRight(getParent().getStyle().getBorder(c), BROW));
            if (result.hidden()) {
                return result;
            }

            // (4) Our row group's right border.
            result = compareBorders(result,
                    CollapsedBorderValue.borderRight(getSection().getStyle().getBorder(c), BROWGROUP));
            if (result.hidden()) {
                return result;
            }
        }

        // (5) Our column's right border.
        TableColumn colElt = getTable().colElement(getCol() + getStyle().getColSpan() - 1);
        if (colElt != null) {
            result = compareBorders(result,
                    CollapsedBorderValue.borderRight(colElt.getStyle().getBorder(c), BCOL));
            if (result.hidden()) {
                return result;
            }
        }

        // (6) The next column's left border.
        if (!inLastColumn) {
            colElt = tableElt.colElement(getCol() + getStyle().getColSpan());
            if (colElt != null) {
                result = compareBorders(result,
                        CollapsedBorderValue.borderLeft(colElt.getStyle().getBorder(c), BCOL));
                if (result.hidden()) {
                    return result;
                }
            }
        } else {
            // (7) The table's right border.
            result = compareBorders(result,
                    CollapsedBorderValue.borderRight(tableElt.getStyle().getBorder(c), BTABLE));
            if (result.hidden()) {
                return result;
            }
        }

        return result;
    }

    private CollapsedBorderValue collapsedTopBorder(CssContext c) {
        // For border top, we need to check, in order of precedence:
        // (1) Our top border.
        CollapsedBorderValue result =
            CollapsedBorderValue.borderTop(getStyle().getBorder(c), BCELL);

        TableCellBox prevCell = getTable().cellAbove(this);
        if (prevCell != null) {
            // (2) A previous cell's bottom border.
            result = compareBorders(result,
                        CollapsedBorderValue.borderBottom(prevCell.getStyle().getBorder(c), BCELL));
            if (result.hidden()) {
                return result;
            }
        }

        // (3) Our row's top border.
        result = compareBorders(result,
                    CollapsedBorderValue.borderTop(getParent().getStyle().getBorder(c), BROW));
        if (result.hidden()) {
            return result;
        }

        // (4) The previous row's bottom border.
        if (prevCell != null) {
            final TableRowBox prevRow;
            if (prevCell.getSection() == getSection()) {
                prevRow = (TableRowBox) getParent().getPreviousSibling();
            } else {
                prevRow = prevCell.getSection().getLastRow();
            }

            if (prevRow != null) {
                result = compareBorders(result,
                            CollapsedBorderValue.borderBottom(prevRow.getStyle().getBorder(c), BROW));
                if (result.hidden()) {
                    return result;
                }
            }
        }

        // Now check row groups.
        TableSectionBox currSection = getSection();
        if (getRow() == 0) {
            // (5) Our row group's top border.
            result = compareBorders(result,
                        CollapsedBorderValue.borderTop(currSection.getStyle().getBorder(c), BROWGROUP));
            if (result.hidden()) {
                return result;
            }

            // (6) Previous row group's bottom border.
            currSection = getTable().sectionAbove(currSection, false);
            if (currSection != null) {
                result = compareBorders(result,
                            CollapsedBorderValue.borderBottom(currSection.getStyle().getBorder(c), BROWGROUP));
                if (result.hidden()) {
                    return result;
                }
            }
        }

        if (currSection == null) {
            // (8) Our column's top border.
            TableColumn colElt = getTable().colElement(getCol());
            if (colElt != null) {
                result = compareBorders(result,
                            CollapsedBorderValue.borderTop(colElt.getStyle().getBorder(c), BCOL));
                if (result.hidden()) {
                    return result;
                }
            }

            // (9) The table's top border.
            result = compareBorders(result,
                        CollapsedBorderValue.borderTop(getTable().getStyle().getBorder(c), BTABLE));
            if (result.hidden()) {
                return result;
            }
        }

        return result;
    }

    private CollapsedBorderValue collapsedBottomBorder(CssContext c) {
        // For border top, we need to check, in order of precedence:
        // (1) Our bottom border.
        CollapsedBorderValue result =
            CollapsedBorderValue.borderBottom(getStyle().getBorder(c), BCELL);

        TableCellBox nextCell = getTable().cellBelow(this);
        if (nextCell != null) {
            // (2) A following cell's top border.
            result = compareBorders(result,
                        CollapsedBorderValue.borderTop(nextCell.getStyle().getBorder(c), BCELL));
            if (result.hidden()) {
                return result;
            }
        }

        // (3) Our row's bottom border. (FIXME: Deal with rowspan!)
        result = compareBorders(result,
                    CollapsedBorderValue.borderBottom(getParent().getStyle().getBorder(c), BROW));
        if (result.hidden()) {
            return result;
        }

        // (4) The next row's top border.
        if (nextCell != null) {
            result = compareBorders(result,
                        CollapsedBorderValue.borderTop(nextCell.getParent().getStyle().getBorder(c), BROW));
            if (result.hidden()) {
                return result;
            }
        }

        // Now check row groups.
        TableSectionBox currSection = getSection();
        if (getRow() + getStyle().getRowSpan() >= currSection.numRows()) {
            // (5) Our row group's bottom border.
            result = compareBorders(result,
                        CollapsedBorderValue.borderBottom(currSection.getStyle().getBorder(c), BROWGROUP));
            if (result.hidden()) {
                return result;
            }

            // (6) Following row group's top border.
            currSection = getTable().sectionBelow(currSection, false);
            if (currSection != null) {
                result = compareBorders(result,
                            CollapsedBorderValue.borderTop(currSection.getStyle().getBorder(c), BROWGROUP));
                if (result.hidden()) {
                    return result;
                }
            }
        }

        if (currSection == null) {
            // (8) Our column's bottom border.
            TableColumn colElt = getTable().colElement(getCol());
            if (colElt != null) {
                result = compareBorders(result,
                            CollapsedBorderValue.borderBottom(colElt.getStyle().getBorder(c), BCOL));
                if (result.hidden()) {
                    return result;
                }
            }

            // (9) The table's bottom border.
            result = compareBorders(result,
                        CollapsedBorderValue.borderBottom(getTable().getStyle().getBorder(c), BTABLE));
            if (result.hidden()) {
                return result;
            }
        }

        return result;
    }

    @CheckReturnValue
    private Rectangle getCollapsedBorderBounds(CssContext c) {
        BorderPropertySet border = getCollapsedPaintingBorder();
        Rectangle bounds = getPaintingBorderEdge(c);
        bounds.x -= (int) border.left() / 2;
        bounds.y -= (int) border.top() / 2;
        bounds.width += (int) border.left() / 2 + ((int) border.right() + 1) / 2;
        bounds.height += (int) border.top() / 2 + ((int) border.bottom() + 1) / 2;

        return bounds;
    }

    @CheckReturnValue
    @Override
    public Rectangle getPaintingClipEdge(CssContext c) {
        if (hasCollapsedPaintingBorder()) {
            return getCollapsedBorderBounds(c);
        } else {
            return super.getPaintingClipEdge(c);
        }
    }

    public boolean hasCollapsedPaintingBorder() {
        return _collapsedPaintingBorder != null;
    }

    protected BorderPropertySet getCollapsedPaintingBorder() {
        return _collapsedPaintingBorder;
    }

    public CollapsedBorderValue getCollapsedBorderBottom() {
        return _collapsedBorderBottom;
    }

    public CollapsedBorderValue getCollapsedBorderLeft() {
        return _collapsedBorderLeft;
    }

    public CollapsedBorderValue getCollapsedBorderRight() {
        return _collapsedBorderRight;
    }

    public CollapsedBorderValue getCollapsedBorderTop() {
        return _collapsedBorderTop;
    }

    public void addCollapsedBorders(Set<CollapsedBorderValue> all, List<CollapsedBorderSide> borders) {
        if (_collapsedBorderTop.exists() && !all.contains(_collapsedBorderTop)) {
            all.add(_collapsedBorderTop);
            borders.add(new CollapsedBorderSide(this, BorderPainter.TOP));
        }

        if (_collapsedBorderRight.exists() && !all.contains(_collapsedBorderRight)) {
            all.add(_collapsedBorderRight);
            borders.add(new CollapsedBorderSide(this, BorderPainter.RIGHT));
        }

        if (_collapsedBorderBottom.exists() && !all.contains(_collapsedBorderBottom)) {
            all.add(_collapsedBorderBottom);
            borders.add(new CollapsedBorderSide(this, BorderPainter.BOTTOM));
        }

        if (_collapsedBorderLeft.exists() && !all.contains(_collapsedBorderLeft)) {
            all.add(_collapsedBorderLeft);
            borders.add(new CollapsedBorderSide(this, BorderPainter.LEFT));
        }
    }

    // Treat height as if it specifies border height (i.e.
    // box-sizing: border-box in CSS3).  There doesn't seem to be any
    // justification in the spec for this, but everybody does it
    // (in standards mode) so I guess we will too
    @Override
    protected int getCSSHeight(CssContext c) {
        if (getStyle().isAutoHeight()) {
            return -1;
        } else {
            int result = (int)getStyle().getFloatPropertyProportionalWidth(
                    CSSName.HEIGHT, getContainingBlock().getContentWidth(), c);

            BorderPropertySet border = getBorder(c);
            result -= (int)border.top() + (int)border.bottom();

            RectPropertySet padding = getPadding(c);
            result -= (int)padding.top() + (int)padding.bottom();

            return result >= 0 ? result : -1;
        }
    }

    @Override
    protected boolean isAllowHeightToShrink() {
        return false;
    }

    @Override
    public boolean isNeedsClipOnPaint(RenderingContext c) {
        boolean result = super.isNeedsClipOnPaint(c);
        if (result) {
            return result;
        }
        ContentLimitContainer contentLimitContainer = ((TableRowBox)getParent()).getContentLimitContainer();
        if (contentLimitContainer == null) {
          return false;
        }
        return c.isPrint() && getTable().getStyle().isPaginateTable() &&
            contentLimitContainer.isContainsMultiplePages();
    }
}
