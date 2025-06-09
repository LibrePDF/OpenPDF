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
package org.openpdf.layout;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
import org.openpdf.css.constants.CSSName;
import org.openpdf.css.constants.PageElementPosition;
import org.openpdf.css.newmatch.PageInfo;
import org.openpdf.css.style.CalculatedStyle;
import org.openpdf.css.style.CssContext;
import org.openpdf.css.style.EmptyStyle;
import org.openpdf.newtable.CollapsedBorderValue;
import org.openpdf.newtable.TableBox;
import org.openpdf.newtable.TableCellBox;
import org.openpdf.render.BlockBox;
import org.openpdf.render.BlockBox.Position;
import org.openpdf.render.Box;
import org.openpdf.render.BoxDimensions;
import org.openpdf.render.InlineLayoutBox;
import org.openpdf.render.PageBox;
import org.openpdf.render.RenderingContext;
import org.openpdf.render.ViewportBox;

import java.awt.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.sort;
import static java.util.Collections.unmodifiableList;
import static java.util.Comparator.comparingInt;
import static org.openpdf.layout.Layer.Width.AUTO;
import static org.openpdf.layout.Layer.Width.NEGATIVE;
import static org.openpdf.layout.Layer.Width.POSITIVE;
import static org.openpdf.layout.Layer.Width.ZERO;

/**
 * All positioned content as well as content with an overflow value other
 * than visible creates a layer.  Layers which define stacking contexts
 * provide the entry for rendering the box tree to an output device.  The main
 * purpose of this class is to provide an implementation of Appendix E of the
 * spec, but it also provides additional utility services including page
 * management and mapping boxes to coordinates (for e.g. links).  When
 * rendering to a paged output device, the layer is also responsible for laying
 * out absolute content (which is laid out after its containing block has
 * completed layout).
 */
public final class Layer {
    public enum PagedMode {
        PAGED_MODE_SCREEN,
        PAGED_MODE_PRINT
    }

    @Nullable
    private final Layer _parent;
    private final boolean _stackingContext;
    @Nullable
    private List<Layer> _children;
    private final Box _master;

    @Nullable
    private Box _end;

    @Nullable
    private List<BlockBox> _floats;

    private boolean _fixedBackground;

    private boolean _inline;
    private boolean _requiresLayout;

    private final List<PageBox> _pages = new ArrayList<>();
    @Nullable
    private PageBox _lastRequestedPage;

    @Nullable
    private Set<BlockBox> _pageSequences;
    @Nullable
    private List<BlockBox> _sortedPageSequences;

    @Nullable
    private Map<String, List<BlockBox>> _runningBlocks;

    public Layer(Box master) {
        this(null, master, true);
    }

    public Layer(@Nullable Layer parent, Box master) {
        this(parent, master, master.getStyle().isPositioned() && !master.getStyle().isAutoZIndex());
    }

    Layer(@Nullable Layer parent, Box master, boolean stackingContext) {
        _parent = parent;
        _master = master;
        _stackingContext = stackingContext;
        master.setLayer(this);
        master.setContainingLayer(this);
    }

    @Nullable
    @CheckReturnValue
    public Layer getParent() {
        return _parent;
    }

    @CheckReturnValue
    public boolean isStackingContext() {
        return _stackingContext;
    }

    @CheckReturnValue
    public int getZIndex() {
        return (int) _master.getStyle().asFloat(CSSName.Z_INDEX);
    }

    public float getOpacity() {
    	return _master.getStyle().getOpacity();
	}


    @CheckReturnValue
    public Box getMaster() {
        return _master;
    }

    public synchronized void addChild(Layer layer) {
        if (_children == null) {
            _children = new ArrayList<>();
        }
        _children.add(layer);
    }

    public void addFloat(BlockBox floater) {
        if (_floats == null) {
            _floats = new ArrayList<>();
        }

        _floats.add(floater);
        floater.getFloatedBoxData().setDrawingLayer(this);
    }

    public void removeFloat(BlockBox floater) {
        if (_floats != null) {
            _floats.remove(floater);
        }
    }

    private void paintFloats(RenderingContext c) {
        if (_floats != null) {
            for (int i = _floats.size() - 1; i >= 0; i--) {
                BlockBox floater = _floats.get(i);
                paintAsLayer(c, floater);
            }
        }
    }

    private void paintLayers(RenderingContext c, List<Layer> layers) {
        for (Layer layer : layers) {
            layer.paint(c);
        }
    }

    enum Width {POSITIVE, ZERO, NEGATIVE, AUTO}

    @CheckReturnValue
    private List<Layer> collectLayers(Width which) {
        List<Layer> result = new ArrayList<>();

        if (which != AUTO) {
            result.addAll(getStackingContextLayers(which));
        }

        List<Layer> children = getChildren();
        for (Layer child : children) {
            if (!child.isStackingContext()) {
                if (which == AUTO) {
                    result.add(child);
                }
                result.addAll(child.collectLayers(which));
            }
        }

        return result;
    }

    @CheckReturnValue
    private List<Layer> getStackingContextLayers(Width which) {
        List<Layer> result = new ArrayList<>();

        List<Layer> children = getChildren();
        for (Layer target : children) {
            if (target.isStackingContext()) {
                int zIndex = target.getZIndex();
                if (which == NEGATIVE && zIndex < 0) {
                    result.add(target);
                } else if (which == POSITIVE && zIndex > 0) {
                    result.add(target);
                } else if (which == ZERO && zIndex == 0) {
                    result.add(target);
                }
            }
        }

        return result;
    }

    @CheckReturnValue
	private List<Layer> getSortedLayers(Width which) {
        List<Layer> result = collectLayers(which);
        result.sort(new ZIndexComparator());
        return result;
    }

    private static class ZIndexComparator implements Comparator<Layer> {
        @Override
        public int compare(Layer l1, Layer l2) {
            return l1.getZIndex() - l2.getZIndex();
        }
    }

    private void paintBackgroundsAndBorders(
            RenderingContext c, List<Box> blocks,
            @Nullable Map<TableCellBox, List<CollapsedBorderSide>> collapsedTableBorders,
            BoxRangeLists rangeLists) {
        BoxRangeHelper helper = new BoxRangeHelper(c.getOutputDevice(), rangeLists.getBlock());

        for (int i = 0; i < blocks.size(); i++) {
            helper.popClipRegions(i);

            Box box = blocks.get(i);
            box.paintBackground(c);
            box.paintBorder(c);
            if (c.debugDrawBoxes() && box instanceof BlockBox) {
                ((BlockBox) box).paintDebugOutline(c);
            }

            if (collapsedTableBorders != null && box instanceof TableCellBox cell) {
                if (cell.hasCollapsedPaintingBorder()) {
                    List<CollapsedBorderSide> borders = collapsedTableBorders.get(cell);
                    if (borders != null) {
                        paintCollapsedTableBorders(c, borders);
                    }
                }
            }

            helper.pushClipRegion(c, i);
        }

        helper.popClipRegions(blocks.size());
    }

    private void paintInlineContent(RenderingContext c, List<Box> lines, BoxRangeLists rangeLists) {
        BoxRangeHelper helper = new BoxRangeHelper(
                c.getOutputDevice(), rangeLists.getInline());

        for (int i = 0; i < lines.size(); i++) {
            helper.popClipRegions(i);
            helper.pushClipRegion(c, i);
            ((InlinePaintable) lines.get(i)).paintInline(c);
        }

        helper.popClipRegions(lines.size());
    }

    private void paintSelection(RenderingContext c, List<Box> lines) {
        if (c.getOutputDevice().isSupportsSelection()) {
            for (Box paintable : lines) {
                if (paintable instanceof InlineLayoutBox) {
                    ((InlineLayoutBox) paintable).paintSelection(c);
                }
            }
        }
    }

    @CheckReturnValue
    public Dimension getPaintingDimension(LayoutContext c) {
        return calcPaintingDimension(c).getOuterMarginCorner();
    }

    public void paint(RenderingContext c) {
        if (getMaster().getStyle().isFixed()) {
            positionFixedLayer(c);
        }

        if (isRootLayer()) {
            getMaster().paintRootElementBackground(c);
        }

        if (! isInline() && ((BlockBox)getMaster()).isReplaced()) {
            paintLayerBackgroundAndBorder(c);
            paintReplacedElement(c, (BlockBox)getMaster());
        } else {
            BoxRangeLists rangeLists = new BoxRangeLists();

            List<Box> blocks = new ArrayList<>();
            List<Box> lines = new ArrayList<>();

            BoxCollector collector = new BoxCollector();
            collector.collect(c, c.getOutputDevice().getClip(), this, blocks, lines, rangeLists);

            if (! isInline()) {
                paintLayerBackgroundAndBorder(c);
                if (c.debugDrawBoxes()) {
                    ((BlockBox)getMaster()).paintDebugOutline(c);
                }
            }

            if (isRootLayer() || isStackingContext()) {
                paintLayers(c, getSortedLayers(NEGATIVE));
            }

            Map<TableCellBox, List<CollapsedBorderSide>> collapsedTableBorders = collectCollapsedTableBorders(blocks);

            paintBackgroundsAndBorders(c, blocks, collapsedTableBorders, rangeLists);
            paintFloats(c);
            paintListMarkers(c, blocks, rangeLists);
            paintInlineContent(c, lines, rangeLists);
            paintReplacedElements(c, blocks, rangeLists);
            paintSelection(c, lines); // XXX do only when there is a selection

            if (isRootLayer() || isStackingContext()) {
                paintLayers(c, collectLayers(AUTO));
                // TODO z-index: 0 layers should be painted atomically
                paintLayers(c, getSortedLayers(ZERO));
                paintLayers(c, getSortedLayers(POSITIVE));
            }
        }
    }

    @CheckReturnValue
    private List<BlockBox> getFloats() {
        return _floats == null ? emptyList() : _floats;
    }

    @Nullable
    @CheckReturnValue
    public Box find(CssContext cssCtx, int absX, int absY, boolean findAnonymous) {
        if (isRootLayer() || isStackingContext()) {
            Box result = find(cssCtx, absX, absY, getSortedLayers(POSITIVE), findAnonymous);
            if (result != null) {
                return result;
            }

            result = find(cssCtx, absX, absY, getSortedLayers(ZERO), findAnonymous);
            if (result != null) {
                return result;
            }

            result = find(cssCtx, absX, absY, collectLayers(AUTO), findAnonymous);
            if (result != null) {
                return result;
            }
        }

        for (int i = 0; i < getFloats().size(); i++) {
            Box floater = getFloats().get(i);
            Box result = floater.find(cssCtx, absX, absY, findAnonymous);
            if (result != null) {
                return result;
            }
        }

        Box result = getMaster().find(cssCtx, absX, absY, findAnonymous);
        if (result != null) {
            return result;
        }

        if (isRootLayer() || isStackingContext()) {
            result = find(cssCtx, absX, absY, getSortedLayers(NEGATIVE), findAnonymous);
            return result;
        }

        return null;
    }

    @Nullable
    @CheckReturnValue
    private Box find(CssContext cssCtx, int absX, int absY, List<Layer> layers, boolean findAnonymous) {
        // Work backwards since layers are painted forwards and we're looking
        // for the top-most box
        for (int i = layers.size()-1; i >= 0; i--) {
            Layer l = layers.get(i);
            Box result = l.find(cssCtx, absX, absY, findAnonymous);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    // A bit of a kludge here.  We need to paint collapsed table borders according
    // to priority so (for example) wider borders float to the top and aren't
    // over-painted by thinner borders.  This method scans the block boxes
    // we're about to draw and returns a map with the last cell in a given table
    // we'll paint as a key and a sorted list of borders as values.  These are
    // then painted after we've drawn the background for this cell.
    @Nullable
    @CheckReturnValue
    private Map<TableCellBox, List<CollapsedBorderSide>> collectCollapsedTableBorders(List<Box> blocks) {
        Map<TableBox, List<CollapsedBorderSide>> cellBordersByTable = new HashMap<>();
        Map<TableBox, TableCellBox> triggerCellsByTable = new HashMap<>();

        Set<CollapsedBorderValue> all = new HashSet<>();
        for (Box b : blocks) {
            if (b instanceof TableCellBox cell) {
                if (cell.hasCollapsedPaintingBorder()) {
                    List<CollapsedBorderSide> borders = cellBordersByTable.computeIfAbsent(cell.getTable(), k -> new ArrayList<>());
                    triggerCellsByTable.put(cell.getTable(), cell);
                    cell.addCollapsedBorders(all, borders);
                }
            }
        }

        if (triggerCellsByTable.isEmpty()) {
            return null;
        } else {
            Map<TableCellBox, List<CollapsedBorderSide>> result = new HashMap<>();

            for (TableCellBox cell : triggerCellsByTable.values()) {
                List<CollapsedBorderSide> borders = cellBordersByTable.get(cell.getTable());
                sort(borders);
                result.put(cell, borders);
            }

            return result;
        }
    }

    private void paintCollapsedTableBorders(RenderingContext c, List<CollapsedBorderSide> borders) {
        for (CollapsedBorderSide border : borders) {
            border.getCell().paintCollapsedBorder(c, border.getSide());
        }
    }

    public void paintAsLayer(RenderingContext c, BlockBox startingPoint) {
        BoxRangeLists rangeLists = new BoxRangeLists();

        List<Box> blocks = new ArrayList<>();
        List<Box> lines = new ArrayList<>();

        BoxCollector collector = new BoxCollector();
        collector.collect(c, c.getOutputDevice().getClip(),
                this, startingPoint, blocks, lines, rangeLists);

        Map<TableCellBox, List<CollapsedBorderSide>> collapsedTableBorders = collectCollapsedTableBorders(blocks);

        paintBackgroundsAndBorders(c, blocks, collapsedTableBorders, rangeLists);
        paintListMarkers(c, blocks, rangeLists);
        paintInlineContent(c, lines, rangeLists);
        paintSelection(c, lines); // XXX only do when there is a selection
        paintReplacedElements(c, blocks, rangeLists);
    }

    private void paintListMarkers(RenderingContext c, List<Box> blocks, BoxRangeLists rangeLists) {
        BoxRangeHelper helper = new BoxRangeHelper(c.getOutputDevice(), rangeLists.getBlock());

        for (int i = 0; i < blocks.size(); i++) {
            helper.popClipRegions(i);

            BlockBox box = (BlockBox)blocks.get(i);
            box.paintListMarker(c);

            helper.pushClipRegion(c, i);
        }

        helper.popClipRegions(blocks.size());
    }

    private void paintReplacedElements(RenderingContext c, List<Box> blocks, BoxRangeLists rangeLists) {
        BoxRangeHelper helper = new BoxRangeHelper(c.getOutputDevice(), rangeLists.getBlock());

        for (int i = 0; i < blocks.size(); i++) {
            helper.popClipRegions(i);

            BlockBox box = (BlockBox)blocks.get(i);
            if (box.isReplaced()) {
                paintReplacedElement(c, box);
            }

            helper.pushClipRegion(c, i);
        }

        helper.popClipRegions(blocks.size());
    }

    private void positionFixedLayer(RenderingContext c) {
        Rectangle rect = c.getFixedRectangle();

        Box fixed = getMaster();

        fixed.setX(0);
        fixed.setY(0);
        fixed.setAbsX(0);
        fixed.setAbsY(0);

        fixed.setContainingBlock(new ViewportBox(rect));
        ((BlockBox)fixed).positionAbsolute(c, Position.BOTH);

        fixed.calcPaintingInfo(c, false);
    }

    private void paintLayerBackgroundAndBorder(RenderingContext c) {
        if (getMaster() instanceof BlockBox box) {
            box.paintBackground(c);
            box.paintBorder(c);
        }
    }

    private void paintReplacedElement(RenderingContext c, BlockBox replaced) {
        Rectangle contentBounds = replaced.getContentAreaEdge(
                replaced.getAbsX(), replaced.getAbsY(), c);
        // Minor hack:  It's inconvenient to adjust for margins, border, padding during
        // layout so just do it here.
        Point loc = replaced.getReplacedElement().getLocation();
        if (contentBounds.x != loc.x || contentBounds.y != loc.y) {
            replaced.getReplacedElement().setLocation(contentBounds.x, contentBounds.y);
        }
        if (! c.isInteractive() || replaced.getReplacedElement().isRequiresInteractivePaint()) {
            c.getOutputDevice().paintReplacedElement(c, replaced);
        }
    }

    public boolean isRootLayer() {
        return getParent() == null && isStackingContext();
    }

    private void moveIfGreater(Dimension result, Dimension test) {
        if (test.width > result.width) {
            result.width = test.width;
        }
        if (test.height > result.height) {
            result.height = test.height;
        }
    }

    @CheckReturnValue
    private PaintingInfo calcPaintingDimension(LayoutContext c) {
        getMaster().calcPaintingInfo(c, true);
        PaintingInfo result = getMaster().getPaintingInfo().copyOf();

        List<Layer> children = getChildren();
        for (Layer child : children) {
            CalculatedStyle masterStyle = child.getMaster().getStyle();
            if (!masterStyle.isFixed() && masterStyle.isAbsolute()) {
                PaintingInfo info = child.calcPaintingDimension(c);
                moveIfGreater(result.getOuterMarginCorner(), info.getOuterMarginCorner());
            }
        }

        return result;
    }

    public void positionChildren(LayoutContext c) {
        for (Layer child : getChildren()) {
            child.position(c);
        }
    }

    private void position(LayoutContext c) {
        if (getMaster().getStyle().isAbsolute() && ! c.isPrint()) {
            ((BlockBox)getMaster()).positionAbsolute(c, Position.BOTH);
        } else if (getMaster().getStyle().isRelative() &&
                (isInline() || ((BlockBox)getMaster()).isInline())) {
            getMaster().positionRelative(c);
            if (! isInline()) {
                getMaster().calcCanvasLocation();
                getMaster().calcChildLocations();
            }

        }
    }

    @CheckReturnValue
    private boolean containsFixedLayer() {
        for (Layer child : getChildren()) {
            if (child.getMaster().getStyle().isFixed() || child.containsFixedLayer()) {
                return true;
            }
        }
        return false;
    }

    @CheckReturnValue
    public boolean containsFixedContent() {
        return _fixedBackground || containsFixedLayer();
    }

    public void setFixedBackground(boolean b) {
        _fixedBackground = b;
    }

    @CheckReturnValue
    public synchronized List<Layer> getChildren() {
        return _children == null ? emptyList() : unmodifiableList(_children);
    }

    private void remove(Layer layer) {
        boolean removed = false;

        // access to _children is synchronized
        synchronized (this) {
            if (_children != null) {
                for (Iterator<Layer> i = _children.iterator(); i.hasNext(); ) {
                    Layer child = i.next();
                    if (child == layer) {
                        removed = true;
                        i.remove();
                        break;
                    }
                }
            }
        }

        if (! removed) {
            throw new RuntimeException("Could not find layer to remove");
        }
    }

    public void detach() {
        if (getParent() != null) {
            getParent().remove(this);
        }
    }

    @CheckReturnValue
    public boolean isInline() {
        return _inline;
    }

    public void setInline(boolean inline) {
        _inline = inline;
    }

    @Nullable
    @CheckReturnValue
    public Box getEnd() {
        return _end;
    }

    public void setEnd(Box end) {
        _end = end;
    }

    @CheckReturnValue
    public boolean isRequiresLayout() {
        return _requiresLayout;
    }

    public void setRequiresLayout(boolean requiresLayout) {
        _requiresLayout = requiresLayout;
    }

    public void finish(LayoutContext c) {
        if (c.isPrint()) {
            layoutAbsoluteChildren(c);
        }
        if (! isInline()) {
            positionChildren(c);
        }
    }

    private void layoutAbsoluteChildren(LayoutContext c) {
        List<Layer> children = new ArrayList<>(getChildren());
        if (!children.isEmpty()) {
            LayoutState state = c.captureLayoutState();
            for (Layer layer : children) {
                if (layer.isRequiresLayout()) {
                    layoutAbsoluteChild(c, layer);
                    if (layer.getMaster().getStyle().isAvoidPageBreakInside() &&
                            layer.getMaster().crossesPageBreak(c)) {
                        layer.getMaster().reset(c);
                        ((BlockBox) layer.getMaster()).setNeedPageClear(true);
                        layoutAbsoluteChild(c, layer);
                        if (layer.getMaster().crossesPageBreak(c)) {
                            layer.getMaster().reset(c);
                            layoutAbsoluteChild(c, layer);
                        }
                    }
                    layer.setRequiresLayout(false);
                    layer.finish(c);
                    c.getRootLayer().ensureHasPage(c, layer.getMaster());
                }
            }
            c.restoreLayoutState(state);
        }
    }

    private void layoutAbsoluteChild(LayoutContext c, Layer child) {
        BlockBox master = (BlockBox)child.getMaster();
        if (child.getMaster().getStyle().isBottomAuto()) {
            // Set top, left
            master.positionAbsolute(c, Position.BOTH);
            master.positionAbsoluteOnPage(c);
            c.reInit(true);
            ((BlockBox)child.getMaster()).layout(c);
            // Set right
            master.positionAbsolute(c, Position.HORIZONTALLY);
        } else {
            // FIXME Not right in the face of pagination, but what
            // to do?  Not sure if just laying out and positioning
            // repeatedly will converge on the correct position,
            // so just guess for now
            c.reInit(true);
            master.layout(c);

            BoxDimensions before = master.getBoxDimensions();
            master.reset(c);
            BoxDimensions after = master.getBoxDimensions();
            master.setBoxDimensions(before);
            master.positionAbsolute(c, Position.BOTH);
            master.positionAbsoluteOnPage(c);
            master.setBoxDimensions(after);

            c.reInit(true);
            ((BlockBox)child.getMaster()).layout(c);
        }
    }

    @CheckReturnValue
    public List<PageBox> getPages() {
        return _pages;
    }

    public boolean isLastPage(PageBox pageBox) {
        return _pages.get(_pages.size()-1) == pageBox;
    }

    public void addPage(CssContext c) {
        List<PageBox> pages = getPages();
        int pagesCount = pages.size();
        String pseudoPage = pseudoPage(pagesCount);
        PageBox pageBox = pages.isEmpty() ?
                createPageBox(c, pseudoPage, 0, pagesCount) :
                createPageBox(c, pseudoPage, pages.get(pagesCount - 1).getBottom(), pagesCount);
        pages.add(pageBox);
    }

    private static String pseudoPage(int size) {
        if (size == 0) {
            return "first";
        } else if (size % 2 == 0) {
            return "right";
        } else {
            return "left";
        }
    }

    public void removeLastPage() {
        PageBox pageBox = _pages.remove(_pages.size() - 1);
        if (pageBox == getLastRequestedPage()) {
            setLastRequestedPage(null);
        }
    }

    @CheckReturnValue
    public static PageBox createPageBox(CssContext c, String pseudoPage) {
        return createPageBox(c, pseudoPage, 0, 0);
    }

    @CheckReturnValue
    public static PageBox createPageBox(CssContext c, String pseudoPage, int top, int pageNo) {
        String pageName = null;
        // HACK We only create pages during layout, but the OutputDevice
        // queries page positions and since pages are created lazily, changing
        // this method to use LayoutContext is tricky
        if (c instanceof LayoutContext) {
            pageName = ((LayoutContext)c).getPageName();
        }

        PageInfo pageInfo = c.getCss().getPageStyle(pageName, pseudoPage);
        CalculatedStyle cs = new EmptyStyle().deriveStyle(pageInfo.getPageStyle());
        return new PageBox(pageInfo, c, cs, top, pageNo);
    }

    @Nullable
    @CheckReturnValue
    public PageBox getFirstPage(CssContext c, Box box) {
        return getPage(c, box.getAbsY());
    }

    @Nullable
    @CanIgnoreReturnValue
    public PageBox getLastPage(CssContext c, Box box) {
        return getPage(c, box.getAbsY() + box.getHeight() - 1);
    }

    public void ensureHasPage(CssContext c, Box box) {
        getLastPage(c, box);
    }

    @Nullable
    @CanIgnoreReturnValue
    public PageBox getPage(CssContext c, int yOffset) {
        List<PageBox> pages = getPages();
        if (yOffset < 0) {
            return null;
        } else {
            PageBox lastRequested = getLastRequestedPage();
            if (lastRequested != null) {
                if (yOffset >= lastRequested.getTop() && yOffset < lastRequested.getBottom()) {
                    return lastRequested;
                }
            }
            PageBox last = pages.get(pages.size()-1);
            if (yOffset < last.getBottom()) {
                // The page we're looking for is probably at the end of the
                // document so do a linear search for the first few pages
                // and then fall back to a binary search if that doesn't work
                // out
                int count = pages.size();
                for (int i = count-1; i >= 0 && i >= count-5; i--) {
                    PageBox pageBox = pages.get(i);
                    if (yOffset >= pageBox.getTop() && yOffset < pageBox.getBottom()) {
                        setLastRequestedPage(pageBox);
                        return pageBox;
                    }
                }

                int low = 0;
                int high = count-6;

                while (low <= high) {
                    int mid = (low + high) >> 1;
                    PageBox pageBox = pages.get(mid);

                    if (yOffset >= pageBox.getTop() && yOffset < pageBox.getBottom()) {
                        setLastRequestedPage(pageBox);
                        return pageBox;
                    }

                    if (pageBox.getTop() < yOffset) {
                        low = mid + 1;
                    } else {
                        high = mid - 1;
                    }
                }
            } else {
                addPagesUntilPosition(c, yOffset);
                PageBox result = pages.get(pages.size()-1);
                setLastRequestedPage(result);
                return result;
            }
        }

        throw new RuntimeException("internal error");
    }

    private void addPagesUntilPosition(CssContext c, int position) {
        List<PageBox> pages = getPages();
        PageBox last = pages.get(pages.size()-1);
        while (position >= last.getBottom()) {
            addPage(c);
            last = pages.get(pages.size()-1);
        }
    }

    public void trimEmptyPages(int maxYHeight) {
        // Empty pages may result when a "keep together" constraint
        // cannot be satisfied and is dropped
        List<PageBox> pages = getPages();
        for (int i = pages.size() - 1; i > 0; i--) {
            PageBox page = pages.get(i);
            if (page.getTop() >= maxYHeight) {
                if (page == getLastRequestedPage()) {
                    setLastRequestedPage(null);
                }
                pages.remove(i);
            } else {
                break;
            }
        }
    }

    public void trimPageCount(int newPageCount) {
        while (_pages.size() > newPageCount) {
            PageBox pageBox = _pages.remove(_pages.size()-1);
            if (pageBox == getLastRequestedPage()) {
                setLastRequestedPage(null);
            }
        }
    }

    public void assignPagePaintingPositions(CssContext cssCtx, PagedMode mode) {
        assignPagePaintingPositions(cssCtx, mode, 0);
    }

    public void assignPagePaintingPositions(
            CssContext cssCtx, PagedMode mode, int additionalClearance) {
        List<PageBox> pages = getPages();
        int paintingTop = additionalClearance;
        for (PageBox page : pages) {
            page.setPaintingTop(paintingTop);
            switch (mode) {
                case PAGED_MODE_SCREEN -> page.setPaintingBottom(paintingTop + page.getHeight(cssCtx));
                case PAGED_MODE_PRINT -> page.setPaintingBottom(paintingTop + page.getContentHeight(cssCtx));
            }
            paintingTop = page.getPaintingBottom() + additionalClearance;
        }
    }

    public int getMaxPageWidth(CssContext cssCtx, int additionalClearance) {
        List<PageBox> pages = getPages();
        int maxWidth = 0;
        for (PageBox page : pages) {
            int pageWidth = page.getWidth(cssCtx) + additionalClearance * 2;
            if (pageWidth > maxWidth) {
                maxWidth = pageWidth;
            }
        }

        return maxWidth;
    }

    @Nullable
    public PageBox getLastPage() {
        List<PageBox> pages = getPages();
        return pages.isEmpty() ? null : pages.get(pages.size()-1);
    }

    public boolean crossesPageBreak(LayoutContext c, int top, int bottom) {
        if (top < 0) {
            return false;
        }
        PageBox page = getPage(c, top);
        return bottom >= page.getBottom() - c.getExtraSpaceBottom();
    }

    @CheckReturnValue
    public Layer findRoot() {
        if (isRootLayer()) {
            return this;
        } else {
            return getParent().findRoot();
        }
    }

    public void addRunningBlock(BlockBox block) {
        if (_runningBlocks == null) {
            _runningBlocks = new HashMap<>();
        }

        String identifier = block.getStyle().getRunningName();
        List<BlockBox> blocks = _runningBlocks.computeIfAbsent(identifier, k -> new ArrayList<>());
        blocks.add(block);
        blocks.sort(comparingInt(Box::getAbsY));
    }

    public void removeRunningBlock(BlockBox block) {
        if (_runningBlocks == null) {
            return;
        }

        String identifier = block.getStyle().getRunningName();

        List<BlockBox> blocks = _runningBlocks.get(identifier);
        if (blocks != null) {
            blocks.remove(block);
        }
    }

    @Nullable
    public BlockBox getRunningBlock(String identifier, PageBox page, PageElementPosition which) {
        if (_runningBlocks == null) {
            return null;
        }

        List<BlockBox> blocks = _runningBlocks.get(identifier);
        if (blocks == null) {
            return null;
        }

        if (which == PageElementPosition.START) {
            BlockBox prev = null;
            for (BlockBox b : blocks) {
                if (b.getStaticEquivalent().getAbsY() >= page.getTop()) {
                    break;
                }
                prev = b;
            }
            return prev;
        } else if (which == PageElementPosition.FIRST) {
            for (BlockBox b : blocks) {
                int absY = b.getStaticEquivalent().getAbsY();
                if (absY >= page.getTop() && absY < page.getBottom()) {
                    return b;
                }
            }
            return getRunningBlock(identifier, page, PageElementPosition.START);
        } else if (which == PageElementPosition.LAST) {
            BlockBox prev = null;
            for (BlockBox b : blocks) {
                if (b.getStaticEquivalent().getAbsY() > page.getBottom()) {
                    break;
                }
                prev = b;
            }
            return prev;
        } else if (which == PageElementPosition.LAST_EXCEPT) {
            BlockBox prev = null;
            for (BlockBox b : blocks) {
                int absY = b.getStaticEquivalent().getAbsY();
                if (absY >= page.getTop() && absY < page.getBottom()) {
                    return null;
                }
                if (absY > page.getBottom()) {
                    break;
                }
                prev = b;
            }
            return prev;
        }

        throw new RuntimeException("bug: internal error");
    }

    public void layoutPages(LayoutContext c) {
        c.setRootDocumentLayer(c.getRootLayer());
        for (PageBox pageBox : _pages) {
            pageBox.layout(c);
        }
    }

    public void addPageSequence(BlockBox start) {
        if (_pageSequences == null) {
            _pageSequences = new HashSet<>();
        }

        _pageSequences.add(start);
    }

    @Nullable
    @CanIgnoreReturnValue
    private List<BlockBox> getSortedPageSequences() {
        if (_pageSequences == null) {
            return null;
        }

        if (_sortedPageSequences == null) {
            List<BlockBox> result = new ArrayList<>(_pageSequences);
            result.sort(comparingInt(Box::getAbsY));
            _sortedPageSequences = result;
        }

        return _sortedPageSequences;
    }

    public int getRelativePageNo(RenderingContext c, int absY) {
        List<BlockBox> sequences = getSortedPageSequences();
        int initial = 0;
        if (c.getInitialPageNo() > 0) {
            initial = c.getInitialPageNo() - 1;
        }
        if ((sequences == null) || sequences.isEmpty()) {
            return initial + getPage(c, absY).getPageNo();
        } else {
            BlockBox pageSequence = findPageSequence(sequences, absY);
            int sequenceStartAbsolutePageNo = getPage(c, pageSequence.getAbsY()).getPageNo();
            int absoluteRequiredPageNo = getPage(c, absY).getPageNo();
            return absoluteRequiredPageNo - sequenceStartAbsolutePageNo;
        }
    }

    @Nullable
    @CheckReturnValue
    private BlockBox findPageSequence(List<BlockBox> sequences, int absY) {
        for (int i = 0; i < sequences.size(); i++) {
            BlockBox result = sequences.get(i);
            if ((i < sequences.size() - 1) && (sequences.get(i + 1).getAbsY() > absY)) {
                return result;
            }
        }

        return null;
    }

    @CheckReturnValue
    public int getRelativePageNo(RenderingContext c) {
        List<BlockBox> sequences = getSortedPageSequences();
        int initial = 0;
        if (c.getInitialPageNo() > 0) {
            initial = c.getInitialPageNo() - 1;
        }
        if (sequences == null) {
            return initial + c.getPageNo();
        } else {
            int sequenceStartIndex = getPageSequenceStart(sequences, c.getPage());
            if (sequenceStartIndex == -1) {
                return initial + c.getPageNo();
            } else {
                BlockBox block = sequences.get(sequenceStartIndex);
                return c.getPageNo() - getFirstPage(c, block).getPageNo();
            }
        }
    }

    @CheckReturnValue
    public int getRelativePageCount(RenderingContext c) {
        List<BlockBox> sequences = getSortedPageSequences();
        int initial = 0;
        if (c.getInitialPageNo() > 0) {
            initial = c.getInitialPageNo() - 1;
        }
        if (sequences == null) {
            return initial + c.getPageCount();
        } else {
            int firstPage;
            int lastPage;

            int sequenceStartIndex = getPageSequenceStart(sequences, c.getPage());

            if (sequenceStartIndex == -1) {
                firstPage = 0;
            } else {
                BlockBox block = sequences.get(sequenceStartIndex);
                firstPage = getFirstPage(c, block).getPageNo();
            }

            if (sequenceStartIndex < sequences.size() - 1) {
                BlockBox block = sequences.get(sequenceStartIndex+1);
                lastPage = getFirstPage(c, block).getPageNo();
            } else {
                lastPage = c.getPageCount();
            }

            int sequenceLength = lastPage - firstPage;
            if (sequenceStartIndex == -1) {
                sequenceLength += initial;
            }

            return sequenceLength;
        }
    }

    @CheckReturnValue
    private int getPageSequenceStart(List<BlockBox> sequences, PageBox page) {
        for (int i = sequences.size() - 1; i >= 0; i--) {
            BlockBox start = sequences.get(i);
            if (start.getAbsY() < page.getBottom() - 1) {
                return i;
            }
        }

        return -1;
    }

    @Nullable
    @CheckReturnValue
    private PageBox getLastRequestedPage() {
        return _lastRequestedPage;
    }

    private void setLastRequestedPage(@Nullable PageBox lastRequestedPage) {
        _lastRequestedPage = lastRequestedPage;
    }
}
