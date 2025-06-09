/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Torbjoern Gannholm, Joshua Marinacci
 * Copyright (c) 2006 Wisconsin Court System
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

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.EntityReference;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.openpdf.css.constants.CSSName;
import org.openpdf.css.constants.IdentValue;
import org.openpdf.css.constants.MarginBoxName;
import org.openpdf.css.constants.PageElementPosition;
import org.openpdf.css.extend.ContentFunction;
import org.openpdf.css.newmatch.CascadedStyle;
import org.openpdf.css.newmatch.PageInfo;
import org.openpdf.css.parser.FSFunction;
import org.openpdf.css.parser.PropertyValue;
import org.openpdf.css.sheet.PropertyDeclaration;
import org.openpdf.css.sheet.StylesheetInfo.Origin;
import org.openpdf.css.style.CalculatedStyle;
import org.openpdf.css.style.EmptyStyle;
import org.openpdf.css.style.FSDerivedValue;
import org.openpdf.newtable.TableBox;
import org.openpdf.newtable.TableCellBox;
import org.openpdf.newtable.TableColumn;
import org.openpdf.newtable.TableRowBox;
import org.openpdf.newtable.TableSectionBox;
import org.openpdf.render.AnonymousBlockBox;
import org.openpdf.render.BlockBox;
import org.openpdf.render.BlockBox.ContentType;
import org.openpdf.render.Box;
import org.openpdf.render.FloatedBoxData;
import org.openpdf.render.InlineBox;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static java.lang.Integer.parseInt;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.openpdf.css.constants.IdentValue.INLINE;
import static org.openpdf.css.newmatch.CascadedStyle.createLayoutPropertyDeclaration;
import static org.openpdf.css.parser.PropertyValue.Type.VALUE_TYPE_FUNCTION;
import static org.openpdf.layout.BoxBuilder.MarginDirection.HORIZONTAL;
import static org.openpdf.layout.BoxBuilder.MarginDirection.VERTICAL;

/**
 * This class is responsible for creating the box tree from the DOM.  This is
 * mostly just a one-to-one translation from the {@code Element} to an
 * {@code InlineBox} or a {@code BlockBox} (or some subclass of
 * {@code BlockBox}), but the tree is reorganized according to the CSS rules.
 * This includes inserting anonymous block and inline boxes, anonymous table
 * content, and {@code :before} and {@code :after} content.  White
 * space is also normalized at this point.  Table columns and table column groups
 * are added to the table which owns them, but are not created as regular boxes.
 * Floated and absolutely positioned content is always treated as inline
 * content for purposes of inserting anonymous block boxes and calculating
 * the kind of content contained in a given block box.
 */
public class BoxBuilder {
    private static final Logger log = LoggerFactory.getLogger(BoxBuilder.class);

    public enum MarginDirection {
        VERTICAL,
        HORIZONTAL
    }

    private static final int CONTENT_LIST_DOCUMENT = 1;
    private static final int CONTENT_LIST_MARGIN_BOX = 2;

    public static BlockBox createRootBox(LayoutContext c, Document document) {
        Element root = document.getDocumentElement();

        CalculatedStyle style = c.getSharedContext().getStyle(root);

        BlockBox result = style.isTable() || style.isInlineTable() ?
                new TableBox(root, style, false) :
                new BlockBox(root, style, false);

        c.resolveCounters(style);

        c.pushLayer(result);
        if (c.isPrint()) {
            if (! style.isIdent(CSSName.PAGE, IdentValue.AUTO)) {
                c.setPageName(style.getStringProperty(CSSName.PAGE));
            }
            c.getRootLayer().addPage(c);
        }

        return result;
    }

    public static void createChildren(LayoutContext c, BlockBox parent) {

        List<Styleable> children = new ArrayList<>();

        ChildBoxInfo info = new ChildBoxInfo();

        createChildren(c, parent, parent.getElement(), children, info, false);

        boolean parentIsNestingTableContent = isNestingTableContent(parent.getStyle().getIdent(
                CSSName.DISPLAY));
        if (!parentIsNestingTableContent && !info.isContainsTableContent()) {
            resolveChildren(parent, children, info);
        } else {
            stripAllWhitespace(children);
            if (parentIsNestingTableContent) {
                resolveTableContent(c, parent, children, info);
            } else {
                resolveChildTableContent(c, parent, children, info, IdentValue.TABLE_CELL);
            }
        }
    }

    @Nullable
    public static TableBox createMarginTable(
            LayoutContext c,
            PageInfo pageInfo,
            MarginBoxName[] names,
            int height,
            MarginDirection direction)
    {
        if (! pageInfo.hasAny(names)) {
            return null;
        }

        Element source = c.getRootLayer().getMaster().getElement(); // HACK

        ChildBoxInfo info = new ChildBoxInfo();
        CalculatedStyle pageStyle = new EmptyStyle().deriveStyle(pageInfo.getPageStyle());

        CalculatedStyle tableStyle = pageStyle.deriveStyle(
                CascadedStyle.createLayoutStyle(
                        new PropertyDeclaration(
                                CSSName.DISPLAY,
                                new PropertyValue(IdentValue.TABLE),
                                true,
                                Origin.USER),
                        new PropertyDeclaration(
                                CSSName.WIDTH,
                                new PropertyValue(CSSPrimitiveValue.CSS_PERCENTAGE, 100.0f, "100%"),
                                true,
                                Origin.USER)
                ));
        TableBox result = (TableBox)createBlockBox(source, tableStyle, info, false, true);
        result.setMarginAreaRoot(true);
        result.setChildrenContentType(ContentType.BLOCK);

        CalculatedStyle tableSectionStyle = pageStyle.createAnonymousStyle(IdentValue.TABLE_ROW_GROUP);
        TableSectionBox section = (TableSectionBox)createBlockBox(source, tableSectionStyle, info, false, true);
        section.setChildrenContentType(ContentType.BLOCK);

        result.addChild(section);

        TableRowBox row = null;
        if (direction == HORIZONTAL) {
            CalculatedStyle tableRowStyle = pageStyle.createAnonymousStyle(IdentValue.TABLE_ROW);
            row = (TableRowBox)createBlockBox(source, tableRowStyle, info, false, true);
            row.setChildrenContentType(ContentType.BLOCK);

            row.setHeightOverride(height);

            section.addChild(row);
        }

        int cellCount = 0;
        boolean alwaysCreate = names.length > 1 && direction == HORIZONTAL;

        for (MarginBoxName name : names) {
            CascadedStyle cellStyle = pageInfo.createMarginBoxStyle(name, alwaysCreate);
            if (cellStyle != null) {
                TableCellBox cell = createMarginBox(c, cellStyle, alwaysCreate);
                if (cell != null) {
                    if (direction == VERTICAL) {
                        CalculatedStyle tableRowStyle = pageStyle.createAnonymousStyle(IdentValue.TABLE_ROW);
                        row = (TableRowBox) createBlockBox(source, tableRowStyle, info, false, true);
                        row.setChildrenContentType(ContentType.BLOCK);
                        row.setHeightOverride(height);
                        section.addChild(row);
                    }
                    row.addChild(cell);
                    cellCount++;
                }
            }
        }

        if (direction == VERTICAL && cellCount > 0) {
            int rHeight = 0;
            for (Box box : section.getChildren()) {
                TableRowBox r = (TableRowBox) box;
                r.setHeightOverride(height / cellCount);
                rHeight += r.getHeightOverride();
            }

            for (Iterator<Box> i = section.getChildren().iterator(); i.hasNext() && rHeight < height; ) {
                TableRowBox r = (TableRowBox)i.next();
                r.setHeightOverride(r.getHeightOverride()+1);
                rHeight++;
            }
        }

        return cellCount > 0 ? result : null;
    }

    @Nullable
    private static TableCellBox createMarginBox(
            LayoutContext c,
            CascadedStyle cascadedStyle,
            boolean alwaysCreate) {
        boolean hasContent = true;

        PropertyDeclaration contentDecl = cascadedStyle.propertyByName(CSSName.CONTENT);

        CalculatedStyle style = new EmptyStyle().deriveStyle(cascadedStyle);

        if (style.isDisplayNone() && ! alwaysCreate) {
            return null;
        }

        if (style.isIdent(CSSName.CONTENT, IdentValue.NONE) ||
                style.isIdent(CSSName.CONTENT, IdentValue.NORMAL)) {
            hasContent = false;
        }

        if (style.isAutoWidth() && ! alwaysCreate && ! hasContent) {
            return null;
        }

        List<Styleable> children = new ArrayList<>();

        ChildBoxInfo info = new ChildBoxInfo(true);
        info.setContainsTableContent();

        Element element = c.getRootLayer().getMaster().getElement(); // XXX Doesn't make sense, but we need something here
        TableCellBox result = new TableCellBox(element, style, true);

        if (hasContent && ! style.isDisplayNone()) {
            children.addAll(createGeneratedMarginBoxContent(
                    c,
                    c.getRootLayer().getMaster().getElement(),
                    (PropertyValue)contentDecl.getValue(),
                    style,
                    info));

            stripAllWhitespace(children);
        }

        if (children.isEmpty() && style.isAutoWidth() && ! alwaysCreate) {
            return null;
        }

        resolveChildTableContent(c, result, children, info, IdentValue.TABLE_CELL);

        return result;
    }

    private static void resolveChildren(BlockBox owner, List<Styleable> children, ChildBoxInfo info) {
        if (!children.isEmpty()) {
            if (info.isContainsBlockLevelContent()) {
                insertAnonymousBlocks(owner, children, info.isLayoutRunningBlocks());
                owner.setChildrenContentType(ContentType.BLOCK);
            } else {
                WhitespaceStripper.stripInlineContent(children);
                if (!children.isEmpty()) {
                    owner.setInlineContent(children);
                    owner.setChildrenContentType(ContentType.INLINE);
                } else {
                    owner.setChildrenContentType(ContentType.EMPTY);
                }
            }
        } else {
            owner.setChildrenContentType(ContentType.EMPTY);
        }
    }

    private static boolean isAllProperTableNesting(IdentValue parentDisplay, List<Styleable> children) {
        for (Styleable child : children) {
            if (!isProperTableNesting(parentDisplay, child.getStyle().getIdent(CSSName.DISPLAY))) {
                return false;
            }
        }

        return true;
    }

    /**
     * Handles the situation when we find table content, but our parent is not
     * table related.  For example, {@code div} -> {@code td</td>}.
     * Anonymous tables are then constructed by repeatedly pulling together
     * consecutive same-table-level siblings and wrapping them in the next
     * highest table level (e.g. consecutive {@code td} elements will
     * be wrapped in an anonymous {@code tr}, then a {@code tbody}, and
     * finally a {@code table}).
     */
    private static void resolveChildTableContent(
            LayoutContext c, BlockBox parent, List<Styleable> children, ChildBoxInfo info, IdentValue target) {
        List<Styleable> childrenForAnonymous = new ArrayList<>();
        List<Styleable> childrenWithAnonymous = new ArrayList<>();

        IdentValue nextUp = getPreviousTableNestingLevel(target);
        for (Styleable styleable : children) {
            if (matchesTableLevel(target, styleable.getStyle().getIdent(CSSName.DISPLAY))) {
                childrenForAnonymous.add(styleable);
            } else {
                if (!childrenForAnonymous.isEmpty()) {
                    createAnonymousTableContent(c, (BlockBox) childrenForAnonymous.get(0), nextUp,
                            childrenForAnonymous, childrenWithAnonymous);

                    childrenForAnonymous = new ArrayList<>();
                }
                childrenWithAnonymous.add(styleable);
            }
        }

        if (!childrenForAnonymous.isEmpty()) {
            createAnonymousTableContent(c, (BlockBox) childrenForAnonymous.get(0), nextUp,
                    childrenForAnonymous, childrenWithAnonymous);
        }

        if (nextUp == IdentValue.TABLE) {
            rebalanceInlineContent(childrenWithAnonymous);
            info.setContainsBlockLevelContent();
            resolveChildren(parent, childrenWithAnonymous, info);
        } else {
            resolveChildTableContent(c, parent, childrenWithAnonymous, info, nextUp);
        }
    }

    private static boolean matchesTableLevel(IdentValue target, IdentValue value) {
        if (target == IdentValue.TABLE_ROW_GROUP) {
            return value == IdentValue.TABLE_ROW_GROUP || value == IdentValue.TABLE_HEADER_GROUP
                    || value == IdentValue.TABLE_FOOTER_GROUP || value == IdentValue.TABLE_CAPTION;
        } else {
            return target == value;
        }
    }

    /**
     * Makes sure that any {@code InlineBox} in {@code content}
     * both starts and ends within {@code content}. Used to ensure that
     * it is always possible to construct anonymous blocks once an element's
     * children has been distributed among anonymous table objects.
     */
    private static void rebalanceInlineContent(List<Styleable> content) {
        Map<Element, InlineBox> boxesByElement = new HashMap<>();
        for (Styleable styleable : content) {
            if (styleable instanceof InlineBox iB) {
                Element elem = iB.getElement();

                if (!boxesByElement.containsKey(elem)) {
                    iB.setStartsHere(true);
                }

                boxesByElement.put(elem, iB);
            }
        }

        for (InlineBox iB : boxesByElement.values()) {
            iB.setEndsHere(true);
        }
    }

    private static void stripAllWhitespace(List<Styleable> content) {
        int start = 0;
        int current;
        boolean started = false;
        for (current = 0; current < content.size(); current++) {
            Styleable styleable = content.get(current);
            if (! styleable.getStyle().isLaidOutInInlineContext()) {
                if (started) {
                    int before = content.size();
                    WhitespaceStripper.stripInlineContent(content.subList(start, current));
                    int after = content.size();
                    current -= (before - after);
                }
                started = false;
            } else {
                if (! started) {
                    started = true;
                    start = current;
                }
            }
        }

        if (started) {
            WhitespaceStripper.stripInlineContent(content.subList(start, current));
        }
    }

    /**
     * Handles the situation when our current parent is table related.  If
     * everything is properly nested (e.g. a {@code tr} contains only
     * {@code td} elements), nothing is done.  Otherwise, anonymous boxes
     * are inserted to ensure the integrity of the table model.
     */
    private static void resolveTableContent(
            LayoutContext c, BlockBox parent, List<Styleable> children, ChildBoxInfo info) {
        IdentValue parentDisplay = parent.getStyle().getIdent(CSSName.DISPLAY);
        IdentValue next = getNextTableNestingLevel(parentDisplay);
        if (next == null && parent.isAnonymous() && containsOrphanedTableContent(children)) {
            resolveChildTableContent(c, parent, children, info, IdentValue.TABLE_CELL);
        } else if (next == null || isAllProperTableNesting(parentDisplay, children)) {
            if (parent.isAnonymous()) {
                rebalanceInlineContent(children);
            }
            resolveChildren(parent, children, info);
        } else {
            List<Styleable> childrenForAnonymous = new ArrayList<>();
            List<Styleable> childrenWithAnonymous = new ArrayList<>();
            for (Styleable child : children) {
                IdentValue childDisplay = child.getStyle().getIdent(CSSName.DISPLAY);

                if (isProperTableNesting(parentDisplay, childDisplay)) {
                    if (!childrenForAnonymous.isEmpty()) {
                        createAnonymousTableContent(c, parent, next, childrenForAnonymous,
                                childrenWithAnonymous);

                        childrenForAnonymous = new ArrayList<>();
                    }
                    childrenWithAnonymous.add(child);
                } else {
                    childrenForAnonymous.add(child);
                }
            }

            if (!childrenForAnonymous.isEmpty()) {
                createAnonymousTableContent(c, parent, next, childrenForAnonymous,
                        childrenWithAnonymous);
            }

            info.setContainsBlockLevelContent();
            resolveChildren(parent, childrenWithAnonymous, info);
        }
    }

    private static boolean containsOrphanedTableContent(List<Styleable> children) {
        for (Styleable child : children) {
            IdentValue display = child.getStyle().getIdent(CSSName.DISPLAY);
            if (display == IdentValue.TABLE_HEADER_GROUP ||
                    display == IdentValue.TABLE_ROW_GROUP ||
                    display == IdentValue.TABLE_FOOTER_GROUP ||
                    display == IdentValue.TABLE_ROW) {
                return true;
            }
        }

        return false;
    }

    private static boolean isParentInline(BlockBox box) {
        CalculatedStyle parentStyle = box.getStyle().getParent();
        return parentStyle != null && parentStyle.isInline();
    }

    private static void createAnonymousTableContent(LayoutContext c, BlockBox source,
                                                    IdentValue next,
                                                    List<Styleable> childrenForAnonymous,
                                                    List<Styleable> childrenWithAnonymous) {
        ChildBoxInfo nested = lookForBlockContent(childrenForAnonymous);
        IdentValue anonDisplay;
        if (isParentInline(source) && next == IdentValue.TABLE) {
            anonDisplay = IdentValue.INLINE_TABLE;
        } else {
            anonDisplay = next;
        }
        CalculatedStyle anonStyle = source.getStyle().createAnonymousStyle(anonDisplay);
        Element element = source.getElement(); // XXX Doesn't really make sense, but what to do?
        BlockBox anonBox = createBlockBox(element, anonStyle, nested, false, true);
        resolveTableContent(c, anonBox, childrenForAnonymous, nested);

        if (next == IdentValue.TABLE) {
            childrenWithAnonymous.add(reorderTableContent(c, (TableBox) anonBox));
        } else {
            childrenWithAnonymous.add(anonBox);
        }
    }

    /**
     * Reorganizes a table so that the header is the first row group and the
     * footer the last.  If the table has caption boxes, they will be pulled
     * out and added to an anonymous block box along with the table itself.
     * If not, the table is returned.
     */
    private static BlockBox reorderTableContent(LayoutContext c, TableBox table) {
        List<Box> topCaptions = new ArrayList<>();
        Box header = null;
        List<Box> bodies = new ArrayList<>();
        Box footer = null;
        List<Box> bottomCaptions = new ArrayList<>();

        for (Box b : table.getChildren()) {
            IdentValue display = b.getStyle().getIdent(CSSName.DISPLAY);
            if (display == IdentValue.TABLE_CAPTION) {
                IdentValue side = b.getStyle().getIdent(CSSName.CAPTION_SIDE);
                if (side == IdentValue.BOTTOM) {
                    bottomCaptions.add(b);
                } else { /* side == IdentValue.TOP */
                    topCaptions.add(b);
                }
            } else if (display == IdentValue.TABLE_HEADER_GROUP && header == null) {
                header = b;
            } else if (display == IdentValue.TABLE_FOOTER_GROUP && footer == null) {
                footer = b;
            } else {
                bodies.add(b);
            }
        }

        table.removeAllChildren();
        if (header != null) {
            ((TableSectionBox)header).setHeader(true);
            table.addChild(header);
        }
        table.addAllChildren(bodies);
        if (footer != null) {
            ((TableSectionBox)footer).setFooter(true);
            table.addChild(footer);
        }

        if (topCaptions.isEmpty() && bottomCaptions.isEmpty()) {
            return table;
        } else {
            // If we have a floated table with a caption, we need to float the
            // outer anonymous box and not the table
            CalculatedStyle anonStyle;
            if (table.getStyle().isFloated()) {
                CascadedStyle cascadedStyle = CascadedStyle.createLayoutStyle(
                        createLayoutPropertyDeclaration(CSSName.DISPLAY, IdentValue.BLOCK),
                        createLayoutPropertyDeclaration(CSSName.FLOAT, table.getStyle().getIdent(CSSName.FLOAT)));

                anonStyle = table.getStyle().deriveStyle(cascadedStyle);
            } else {
                anonStyle = table.getStyle().createAnonymousStyle(IdentValue.BLOCK);
            }

            BlockBox anonBox = new BlockBox(table.getElement(), anonStyle, true);
            anonBox.setFromCaptionedTable(true);

            anonBox.setChildrenContentType(ContentType.BLOCK);
            anonBox.addAllChildren(topCaptions);
            anonBox.addChild(table);
            anonBox.addAllChildren(bottomCaptions);

            if (table.getStyle().isFloated()) {
                anonBox.setFloatedBoxData(new FloatedBoxData());
                table.setFloatedBoxData(null);

                CascadedStyle original = c.getSharedContext().getCss().getCascadedStyle(
                        table.getElement(), false);
                CascadedStyle modified = CascadedStyle.createLayoutStyle(
                        original,
                        new PropertyDeclaration[]{
                                createLayoutPropertyDeclaration(
                                        CSSName.FLOAT, IdentValue.NONE)
                        });
                table.setStyle(table.getStyle().getParent().deriveStyle(modified));
            }

            return anonBox;
        }
    }

    private static ChildBoxInfo lookForBlockContent(List<Styleable> styleables) {
        ChildBoxInfo result = new ChildBoxInfo();
        for (Styleable s : styleables) {
            if (!s.getStyle().isLaidOutInInlineContext()) {
                result.setContainsBlockLevelContent();
                break;
            }
        }
        return result;
    }

    @Nullable
    private static IdentValue getNextTableNestingLevel(IdentValue display) {
        if (display == IdentValue.TABLE || display == IdentValue.INLINE_TABLE) {
            return IdentValue.TABLE_ROW_GROUP;
        } else if (display == IdentValue.TABLE_HEADER_GROUP
                || display == IdentValue.TABLE_ROW_GROUP
                || display == IdentValue.TABLE_FOOTER_GROUP) {
            return IdentValue.TABLE_ROW;
        } else if (display == IdentValue.TABLE_ROW) {
            return IdentValue.TABLE_CELL;
        } else {
            return null;
        }
    }

    @Nullable
    private static IdentValue getPreviousTableNestingLevel(IdentValue display) {
        if (display == IdentValue.TABLE_CELL) {
            return IdentValue.TABLE_ROW;
        } else if (display == IdentValue.TABLE_ROW) {
            return IdentValue.TABLE_ROW_GROUP;
        } else if (display == IdentValue.TABLE_HEADER_GROUP
                || display == IdentValue.TABLE_ROW_GROUP
                || display == IdentValue.TABLE_FOOTER_GROUP) {
            return IdentValue.TABLE;
        } else {
            return null;
        }
    }

    private static boolean isProperTableNesting(IdentValue parent, IdentValue child) {
        return (parent == IdentValue.TABLE && (child == IdentValue.TABLE_HEADER_GROUP ||
                child == IdentValue.TABLE_ROW_GROUP ||
                child == IdentValue.TABLE_FOOTER_GROUP ||
                child == IdentValue.TABLE_CAPTION))
                || ((parent == IdentValue.TABLE_HEADER_GROUP ||
                parent == IdentValue.TABLE_ROW_GROUP ||
                parent == IdentValue.TABLE_FOOTER_GROUP) &&
                child == IdentValue.TABLE_ROW)
                || (parent == IdentValue.TABLE_ROW && child == IdentValue.TABLE_CELL)
                || (parent == IdentValue.INLINE_TABLE && (child == IdentValue.TABLE_HEADER_GROUP ||
                child == IdentValue.TABLE_ROW_GROUP ||
                child == IdentValue.TABLE_FOOTER_GROUP));

    }

    private static boolean isNestingTableContent(IdentValue display) {
        return display == IdentValue.TABLE || display == IdentValue.INLINE_TABLE ||
                display == IdentValue.TABLE_HEADER_GROUP || display == IdentValue.TABLE_ROW_GROUP ||
                display == IdentValue.TABLE_FOOTER_GROUP || display == IdentValue.TABLE_ROW;
    }

    private static boolean isAttrFunction(FSFunction function) {
        if (function.is("attr")) {
            List<PropertyValue> params = function.getParameters();
            if (params.size() == 1) {
                PropertyValue value = params.get(0);
                return value.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT;
            }
        }

        return false;
    }

    public static boolean isElementFunction(FSFunction function) {
        if (function.is("element")) {
            List<PropertyValue> params = function.getParameters();
            if (params.isEmpty() || params.size() > 2) {
                return false;
            }
            PropertyValue value1 = params.get(0);
            boolean ok = value1.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT;
            if (ok && params.size() == 2) {
                PropertyValue value2 = params.get(1);
                ok = value2.getPrimitiveType() == CSSPrimitiveValue.CSS_IDENT;
            }

            return ok;
        }

        return false;
    }

    @Nullable
    private static CounterFunction makeCounterFunction(FSFunction function, LayoutContext c, CalculatedStyle style) {
        if (function.is("counter")) {
            List<PropertyValue> params = function.getParameters();
            if (params.isEmpty() || params.size() > 2) {
                return null;
            }

            PropertyValue value = params.get(0);
            if (value.getPrimitiveType() != CSSPrimitiveValue.CSS_IDENT) {
                return null;
            }

            String s = value.getStringValue();
            // counter(page) and counter(pages) are handled separately
            if (s.equals("page") || s.equals("pages")) {
                return null;
            }

            String counter = value.getStringValue();
            IdentValue listStyleType = IdentValue.DECIMAL;
            if (params.size() == 2) {
                value = params.get(1);
                if (value.getPrimitiveType() != CSSPrimitiveValue.CSS_IDENT) {
                    return null;
                }

                IdentValue identValue = IdentValue.valueOf(value.getStringValue());
                if (identValue != null) {
                    value.setIdentValue(identValue);
                    listStyleType = identValue;
                }
            }

            int counterValue = c.getCounterContext(style).getCurrentCounterValue(counter);

            return new CounterFunction(counterValue, listStyleType);
        } else if (function.is("counters")) {
            List<PropertyValue> params = function.getParameters();
            if (params.size() < 2 || params.size() > 3) {
                return null;
            }

            PropertyValue value = params.get(0);
            if (value.getPrimitiveType() != CSSPrimitiveValue.CSS_IDENT) {
                return null;
            }

            String counter = value.getStringValue();

            value = params.get(1);
            if (value.getPrimitiveType() != CSSPrimitiveValue.CSS_STRING) {
                return null;
            }

            String separator = value.getStringValue();

            IdentValue listStyleType = IdentValue.DECIMAL;
            if (params.size() == 3) {
                value = params.get(2);
                if (value.getPrimitiveType() != CSSPrimitiveValue.CSS_IDENT) {
                    return null;
                }

                IdentValue identValue = IdentValue.valueOf(value.getStringValue());
                if (identValue != null) {
                    value.setIdentValue(identValue);
                    listStyleType = identValue;
                }
            }

            List<Integer> counterValues = c.getCounterContext(style).getCurrentCounterValues(counter);

            return new CounterFunction(counterValues, separator, listStyleType);
        } else {
            return null;
        }
    }

    private static String getAttributeValue(FSFunction attrFunc, Element e) {
        PropertyValue value = attrFunc.getParameters().get(0);
        return e.getAttribute(value.getStringValue());
    }

    @CheckReturnValue
    private static List<Styleable> createGeneratedContentList(
            LayoutContext c, Element element, PropertyValue propValue,
            @Nullable String peName, CalculatedStyle style, int mode, @Nullable ChildBoxInfo info) {
        List<PropertyValue> values = propValue.getValues();

        List<Styleable> result = new ArrayList<>(values.size());

        for (PropertyValue value : values) {
            ContentFunction contentFunction = null;
            FSFunction function = null;

            String content = null;

            short type = value.getPrimitiveType();
            if (type == CSSPrimitiveValue.CSS_STRING) {
                content = value.getStringValue();
            } else if (value.getPropertyValueType() == VALUE_TYPE_FUNCTION) {
                if (mode == CONTENT_LIST_DOCUMENT && isAttrFunction(value.getFunction())) {
                    content = getAttributeValue(value.getFunction(), element);
                } else {
                    CounterFunction cFunc = null;

                    if (mode == CONTENT_LIST_DOCUMENT) {
                        cFunc = makeCounterFunction(value.getFunction(), c, style);
                    }

                    if (cFunc != null) {
                        //TODO: counter functions may be called with non-ordered list-style-types, e.g. disc
                        content = cFunc.evaluate();
                    } else if (mode == CONTENT_LIST_MARGIN_BOX && isElementFunction(value.getFunction())) {
                        BlockBox target = getRunningBlock(c, value);
                        if (target != null) {
                            result.add(target.copyOf());
                            info.setContainsBlockLevelContent();
                        }
                    } else {
                        contentFunction =
                                c.getContentFunctionFactory().lookupFunction(c, value.getFunction());
                        if (contentFunction != null) {
                            function = value.getFunction();

                            if (contentFunction.isStatic()) {
                                content = contentFunction.calculate(c, function);
                                contentFunction = null;
                                function = null;
                            } else {
                                content = contentFunction.getLayoutReplacementText();
                            }
                        }
                    }
                }
            } else if (type == CSSPrimitiveValue.CSS_IDENT) {
                FSDerivedValue dv = style.valueByName(CSSName.QUOTES);

                if (dv != IdentValue.NONE) {
                    IdentValue ident = value.getIdentValue();

                    if (ident == IdentValue.OPEN_QUOTE) {
                        String[] quotes = style.asStringArray(CSSName.QUOTES);
                        content = quotes[0];
                    } else if (ident == IdentValue.CLOSE_QUOTE) {
                        String[] quotes = style.asStringArray(CSSName.QUOTES);
                        content = quotes[1];
                    }
                }
            }

            if (content != null) {
                InlineBox iB = new InlineBox(content, null, contentFunction, function, element, peName);
                iB.setStartsHere(true);
                iB.setEndsHere(true);

                result.add(iB);
            }
        }

        return result;
    }

    @Nullable
    @CheckReturnValue
    public static BlockBox getRunningBlock(LayoutContext c, PropertyValue value) {
        List<PropertyValue> params = value.getFunction().getParameters();
        String ident = params.get(0).getStringValue();
        PageElementPosition position = null;
        if (params.size() == 2) {
            position = PageElementPosition.byIdent(params.get(1).getStringValue());
        }
        if (position == null) {
            position = PageElementPosition.FIRST;
        }
        return c.getRootDocumentLayer().getRunningBlock(ident, c.getPage(), position);
    }

    private static void insertGeneratedContent(
            LayoutContext c, Element element, CalculatedStyle parentStyle,
            String peName, List<Styleable> children, ChildBoxInfo info) {
        CascadedStyle peStyle = c.getCss().getPseudoElementStyle(element, peName);
        if (peStyle != null) {
            PropertyDeclaration contentDecl = peStyle.propertyByName(CSSName.CONTENT);
            PropertyDeclaration counterResetDecl = peStyle.propertyByName(CSSName.COUNTER_RESET);
            PropertyDeclaration counterIncrDecl = peStyle.propertyByName(CSSName.COUNTER_INCREMENT);

            CalculatedStyle calculatedStyle = null;
            if (contentDecl != null || counterResetDecl != null || counterIncrDecl != null) {
                calculatedStyle = parentStyle.deriveStyle(peStyle);
                if (calculatedStyle.isDisplayNone()) return;
                if (calculatedStyle.isIdent(CSSName.CONTENT, IdentValue.NONE)) return;
                if (calculatedStyle.isIdent(CSSName.CONTENT, IdentValue.NORMAL) && (peName.equals("before") || peName.equals("after")))
                    return;

                if (calculatedStyle.isTable() || calculatedStyle.isTableRow() || calculatedStyle.isTableSection()) {
                    CascadedStyle newPeStyle =
                        CascadedStyle.createLayoutStyle(peStyle, new PropertyDeclaration[] {
                            createLayoutPropertyDeclaration(
                                CSSName.DISPLAY,
                                IdentValue.BLOCK),
                        });
                    calculatedStyle = parentStyle.deriveStyle(newPeStyle);
                }
                c.resolveCounters(calculatedStyle);
            }

            if (contentDecl != null) {
                CSSPrimitiveValue propValue = contentDecl.getValue();
                children.addAll(createGeneratedContent(c, element, peName, calculatedStyle,
                        (PropertyValue) propValue, info));
            }
        }
    }

    private static List<Styleable> createGeneratedContent(
            LayoutContext c, Element element, String peName,
            CalculatedStyle style, PropertyValue property, ChildBoxInfo info) {
        if (style.isDisplayNone() || style.isIdent(CSSName.DISPLAY, IdentValue.TABLE_COLUMN)
                || style.isIdent(CSSName.DISPLAY, IdentValue.TABLE_COLUMN_GROUP)) {
            return emptyList();
        }

        List<Styleable> inlineBoxes = createGeneratedContentList(
                c, element, property, peName, style, CONTENT_LIST_DOCUMENT, null);

        if (style.isInline()) {
            for (Styleable inlineBox : inlineBoxes) {
                InlineBox iB = (InlineBox) inlineBox;
                iB.setStyle(style);
                iB.applyTextTransform();
            }
            return inlineBoxes;
        } else {
            CalculatedStyle anon = style.createAnonymousStyle(INLINE);
            for (Styleable inlineBox : inlineBoxes) {
                InlineBox iB = (InlineBox) inlineBox;
                iB.setStyle(anon);
                iB.applyTextTransform();
                iB.setElement(null);
            }

            BlockBox result = createBlockBox(element, style, info, true, false);
            result.setInlineContent(inlineBoxes);
            result.setChildrenContentType(ContentType.INLINE);
            result.setPseudoElementOrClass(peName);

            if (! style.isLaidOutInInlineContext()) {
                info.setContainsBlockLevelContent();
            }

            return new ArrayList<>(singletonList(result));
        }
    }

    private static List<Styleable> createGeneratedMarginBoxContent(
            LayoutContext c, Element element, PropertyValue property,
            CalculatedStyle style, ChildBoxInfo info) {
        List<Styleable> result = createGeneratedContentList(
                c, element, property, null, style, CONTENT_LIST_MARGIN_BOX, info);

        CalculatedStyle anon = style.createAnonymousStyle(INLINE);
        for (Styleable s : result) {
            if (s instanceof InlineBox iB) {
                iB.setElement(null);
                iB.setStyle(anon);
                iB.applyTextTransform();
            }
        }

        return result;
    }

    private static BlockBox createBlockBox(
            Element source, CalculatedStyle style, ChildBoxInfo info, boolean generated, boolean anonymous) {
        if (style.isFloated() && !(style.isAbsolute() || style.isFixed())) {
            BlockBox result = style.isTable() || style.isInlineTable() ?
                    new TableBox(source, style, anonymous) :
                    new BlockBox(source, style, anonymous);
            result.setFloatedBoxData(new FloatedBoxData());
            return result;
        } else if (style.isSpecifiedAsBlock()) {
            return new BlockBox(source, style, anonymous);
        } else if (! generated && (style.isTable() || style.isInlineTable())) {
            return new TableBox(source, style, anonymous);
        } else if (style.isTableCell()) {
            info.setContainsTableContent();
            return new TableCellBox(source, style, anonymous);
        } else if (! generated && style.isTableRow()) {
            info.setContainsTableContent();
            return new TableRowBox(source, style, anonymous);
        } else if (! generated && style.isTableSection()) {
            info.setContainsTableContent();
            return new TableSectionBox(source, style, anonymous);
        } else if (style.isTableCaption()) {
            info.setContainsTableContent();
            return new BlockBox(source, style, anonymous);
        } else {
            return new BlockBox(source, style, anonymous);
        }
    }

    private static void addColumns(LayoutContext c, TableBox table, TableColumn parent) {
        SharedContext sharedContext = c.getSharedContext();

        Node working = parent.getElement().getFirstChild();
        boolean found = false;
        while (working != null) {
            if (working.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) working;
                CalculatedStyle style = sharedContext.getStyle(element);

                if (style.isIdent(CSSName.DISPLAY, IdentValue.TABLE_COLUMN)) {
                    found = true;
                    TableColumn col = new TableColumn(element, style);
                    col.setParent(parent);
                    table.addStyleColumn(col);
                }
            }
            working = working.getNextSibling();
        }
        if (! found) {
            table.addStyleColumn(parent);
        }
    }

    private static void addColumnOrColumnGroup(
            LayoutContext c, TableBox table, Element e, CalculatedStyle style) {
        if (style.isIdent(CSSName.DISPLAY, IdentValue.TABLE_COLUMN)) {
            table.addStyleColumn(new TableColumn(e, style));
        } else { /* style.isIdent(CSSName.DISPLAY, IdentValue.TABLE_COLUMN_GROUP) */
            addColumns(c, table, new TableColumn(e, style));
        }
    }

    private static InlineBox createInlineBox(
            String text, Element parent, CalculatedStyle parentStyle, @Nullable Text node) {

        InlineBox result = parentStyle.isInline() && !(parent.getParentNode() instanceof Document) ?
                new InlineBox(text, node, null, null, parent, null, parentStyle) :
                new InlineBox(text, node, null, null, null, null, parentStyle.createAnonymousStyle(INLINE));
        result.applyTextTransform();

        return result;
    }

    private static void createChildren(
            LayoutContext c, BlockBox blockParent, Element parent,
            List<Styleable> children, ChildBoxInfo info, boolean inline) {
        SharedContext sharedContext = c.getSharedContext();

        CalculatedStyle parentStyle = sharedContext.getStyle(parent);

        insertGeneratedContent(c, parent, parentStyle, "before", children, info);

        Node working = parent.getFirstChild();
        boolean needStartText = inline;
        boolean needEndText = inline;
        if (working != null) {
            InlineBox previousIB = null;
            do {
                Styleable child = null;
                short nodeType = working.getNodeType();
                if (nodeType == Node.ELEMENT_NODE) {
                    Element element = (Element) working;
                    CalculatedStyle style = sharedContext.getStyle(element);

                    if (style.isDisplayNone()) {
                        continue;
                    }

                    c.resolveCounters(style, parseStartIndex(working));

                    if (style.isIdent(CSSName.DISPLAY, IdentValue.TABLE_COLUMN)
                            || style.isIdent(CSSName.DISPLAY, IdentValue.TABLE_COLUMN_GROUP)) {
                        if ((blockParent != null) &&
                                (blockParent.getStyle().isTable() || blockParent.getStyle().isInlineTable())) {
                            TableBox table = (TableBox) blockParent;
                            addColumnOrColumnGroup(c, table, element, style);
                        }

                        continue;
                    }

                    if (style.isInline()) {
                        if (needStartText) {
                            needStartText = false;
                            InlineBox iB = createInlineBox("", parent, parentStyle, null);
                            iB.setStartsHere(true);
                            iB.setEndsHere(false);
                            children.add(iB);
                            previousIB = iB;
                        }
                        createChildren(c, null, element, children, info, true);
                        if (inline) {
                            if (previousIB != null) {
                                previousIB.setEndsHere(false);
                            }
                            needEndText = true;
                        }
                    } else {
                        child = createBlockBox(element, style, info, false, false);
                        if (style.isListItem()) {
                            BlockBox block = (BlockBox) child;
                            block.setListCounter(c.getCounterContext(style).getCurrentCounterValue("list-item"));
                        }

                        if (style.isTable() || style.isInlineTable()) {
                            TableBox table = (TableBox) child;
                            table.ensureChildren(c);

                            child = reorderTableContent(c, table);
                        }

                        if (!info.isContainsBlockLevelContent()
                                && !style.isLaidOutInInlineContext()) {
                            info.setContainsBlockLevelContent();
                        }

                        BlockBox block = (BlockBox) child;
                        if (block.getStyle().mayHaveFirstLine()) {
                            block.setFirstLineStyle(c.getCss().getPseudoElementStyle(element, "first-line"));
                        }
                        if (block.getStyle().mayHaveFirstLetter()) {
                            block.setFirstLetterStyle(c.getCss().getPseudoElementStyle(element, "first-letter"));
                        }
                        //I think we need to do this to evaluate counters correctly
                        block.ensureChildren(c);
                    }
                } else if (nodeType == Node.TEXT_NODE || nodeType == Node.CDATA_SECTION_NODE) {
                    needStartText = false;
                    needEndText = false;

                    Text textNode = (Text)working;

                    /*
                    StringBuilder text = new StringBuilder(textNode.getData());

                    Node maybeText = textNode;
                    while (true) {
                        maybeText = textNode.getNextSibling();
                        if (maybeText != null) {
                            short maybeNodeType = maybeText.getNodeType();
                            if (maybeNodeType == Node.TEXT_NODE ||
                                    maybeNodeType == Node.CDATA_SECTION_NODE) {
                                textNode = (Text)maybeText;
                                text.append(textNode.getData());
                            } else {
                                break;
                            }
                        } else {
                            break;
                        }
                    }

                    working = textNode;
                    child = createInlineBox(text.toString(), parent, parentStyle, textNode);
                    */

                    child = createInlineBox(textNode.getData(), parent, parentStyle, textNode);

                    InlineBox iB = (InlineBox) child;
                    iB.setEndsHere(true);
                    if (previousIB == null) {
                        iB.setStartsHere(true);
                    } else {
                        previousIB.setEndsHere(false);
                    }
                    previousIB = iB;
                } else if(nodeType == Node.ENTITY_REFERENCE_NODE) {
                    EntityReference entityReference = (EntityReference)working;
                    child = createInlineBox(entityReference.getTextContent(), parent, parentStyle, null);

                    InlineBox iB = (InlineBox) child;
                    iB.setEndsHere(true);
                    if (previousIB == null) {
                        iB.setStartsHere(true);
                    } else {
                        previousIB.setEndsHere(false);
                    }
                    previousIB = iB;
                }

                if (child != null) {
                    children.add(child);
                }
            } while ((working = working.getNextSibling()) != null);
        }
        if (needStartText || needEndText) {
            InlineBox iB = createInlineBox("", parent, parentStyle, null);
            iB.setStartsHere(needStartText);
            iB.setEndsHere(needEndText);
            children.add(iB);
        }
        insertGeneratedContent(c, parent, parentStyle, "after", children, info);
    }

    @Nullable
    @CheckReturnValue
    private static Integer parseStartIndex(Node node) {
        return switch (node.getNodeName()) {
            case "ol" -> parseAttribute(node, "start");
            case "li" -> parseAttribute(node, "value");
            default -> null;
        };
    }

    @Nullable
    @CheckReturnValue
    private static Integer parseAttribute(Node node, String attributeName) {
        Node startAttribute = node.getAttributes().getNamedItem(attributeName);
        if (startAttribute != null) {
            String attributeValue = startAttribute.getNodeValue();
            try {
                return parseInt(attributeValue) - 1;
            } catch (NumberFormatException e) {
                log.debug("Invalid attribute {}=\"{}\": {}", attributeName, attributeValue, e.toString());
            }
        }
        return null;
    }

    private static void insertAnonymousBlocks(Box parent, List<Styleable> children, boolean layoutRunningBlocks) {
        List<Styleable> inline = new ArrayList<>();

        Deque<InlineBox> parents = new ArrayDeque<>();
        List<InlineBox> savedParents = null;

        for (Styleable child : children) {
            if (child.getStyle().isLaidOutInInlineContext() &&
                    !(layoutRunningBlocks && child.getStyle().isRunning())) {
                inline.add(child);

                if (child.getStyle().isInline()) {
                    InlineBox iB = (InlineBox) child;
                    if (iB.isStartsHere()) {
                        parents.add(iB);
                    }
                    if (iB.isEndsHere()) {
                        parents.removeLast();
                    }
                }
            } else {
                if (!inline.isEmpty()) {
                    createAnonymousBlock(parent, inline, savedParents);
                    inline = new ArrayList<>();
                    savedParents = new ArrayList<>(parents);
                }
                parent.addChild((Box) child);
            }
        }

        createAnonymousBlock(parent, inline, savedParents);
    }

    private static void createAnonymousBlock(Box parent, List<Styleable> inline, List<InlineBox> savedParents) {
        WhitespaceStripper.stripInlineContent(inline);
        if (!inline.isEmpty()) {
            AnonymousBlockBox anonymousBox = new AnonymousBlockBox(parent.getElement(),
                    parent.getStyle().createAnonymousStyle(IdentValue.BLOCK),
                    savedParents, inline
            );

            parent.addChild(anonymousBox);
        }
    }

    private static class ChildBoxInfo {
        private boolean _containsBlockLevelContent;
        private boolean _containsTableContent;
        private final boolean _layoutRunningBlocks;

        private ChildBoxInfo() {
            this(false);
        }

        private ChildBoxInfo(boolean layoutRunningBlocks) {
            _layoutRunningBlocks = layoutRunningBlocks;
        }

        boolean isContainsBlockLevelContent() {
            return _containsBlockLevelContent;
        }

        private void setContainsBlockLevelContent() {
            _containsBlockLevelContent = true;
        }

        boolean isContainsTableContent() {
            return _containsTableContent;
        }

        private void setContainsTableContent() {
            _containsTableContent = true;
        }

        boolean isLayoutRunningBlocks() {
            return _layoutRunningBlocks;
        }
    }
}
