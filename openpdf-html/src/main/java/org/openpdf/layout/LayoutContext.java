/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci, Torbjoern Gannholm
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

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
import org.openpdf.context.ContentFunctionFactory;
import org.openpdf.context.StyleReference;
import org.openpdf.css.constants.CSSName;
import org.openpdf.css.constants.IdentValue;
import org.openpdf.css.parser.CounterData;
import org.openpdf.css.style.CalculatedStyle;
import org.openpdf.css.style.CssContext;
import org.openpdf.css.value.FontSpecification;
import org.openpdf.extend.FSCanvas;
import org.openpdf.extend.FontContext;
import org.openpdf.extend.NamespaceHandler;
import org.openpdf.extend.ReplacedElementFactory;
import org.openpdf.extend.TextRenderer;
import org.openpdf.extend.UserAgentCallback;
import org.openpdf.render.Box;
import org.openpdf.render.FSFont;
import org.openpdf.render.FSFontMetrics;
import org.openpdf.render.MarkerData;
import org.openpdf.render.PageBox;

import java.awt.*;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;

/**
 * This class tracks state which changes over the course of a layout run.
 * Generally speaking, if possible, state information should be stored in the box
 * tree and not here.  It also provides pass-though calls to many methods in
 * {@link SharedContext}.
 */
public class LayoutContext implements CssContext {
    private final SharedContext _sharedContext;

    @Nullable
    private Layer _rootLayer;

    private StyleTracker _firstLines;
    private StyleTracker _firstLetters;
    @Nullable
    private MarkerData _currentMarkerData;

    private final Deque<BlockFormattingContext> _blockFormattingContexts = new ArrayDeque<>();
    private final Deque<Layer> _layers = new ArrayDeque<>();

    private final FontContext _fontContext;

    private final ContentFunctionFactory _contentFunctionFactory = new ContentFunctionFactory();

    private int _extraSpaceTop;
    private int _extraSpaceBottom;

    private final Map<CalculatedStyle, CounterContext> _counterContextMap = new HashMap<>();

    @Nullable
    private String _pendingPageName;
    @Nullable
    private String _pageName;

    private int _noPageBreak;

    @Nullable
    private Layer _rootDocumentLayer;
    @Nullable
    private PageBox _page;

    private boolean _mayCheckKeepTogether = true;

    @Nullable
    private BreakAtLineContext _breakAtLineContext;

    public TextRenderer getTextRenderer() {
        return _sharedContext.getTextRenderer();
    }

    @CheckReturnValue
    @Override
    public StyleReference getCss() {
        return _sharedContext.getCss();
    }

    public FSCanvas getCanvas() {
        return _sharedContext.getCanvas();
    }

    public Rectangle getFixedRectangle() {
        return _sharedContext.getFixedRectangle();
    }

    public NamespaceHandler getNamespaceHandler() {
        return _sharedContext.getNamespaceHandler();
    }

    //the stuff that needs to have a separate instance for each run.
    LayoutContext(SharedContext sharedContext, FontContext fontContext) {
        _sharedContext = sharedContext;
        _firstLines = new StyleTracker();
        _firstLetters = new StyleTracker();
        this._fontContext = fontContext;
    }

    public void reInit(boolean keepLayers) {
        _firstLines = new StyleTracker();
        _firstLetters = new StyleTracker();
        _currentMarkerData = null;

        _blockFormattingContexts.clear();

        if (! keepLayers) {
            _rootLayer = null;
            _layers.clear();
        }

        _extraSpaceTop = 0;
        _extraSpaceBottom = 0;
    }

    public LayoutState captureLayoutState() {
        return isPrint() ?
                new LayoutState(_firstLines, _firstLetters, _currentMarkerData, _blockFormattingContexts, getPageName(), getExtraSpaceBottom(), getExtraSpaceTop(), getNoPageBreak()) :
                new LayoutState(_firstLines, _firstLetters, _currentMarkerData, _blockFormattingContexts);
    }

    public void restoreLayoutState(LayoutState layoutState) {
        _firstLines = layoutState.getFirstLines();
        _firstLetters = layoutState.getFirstLetters();

        _currentMarkerData = layoutState.getCurrentMarkerData();

        _blockFormattingContexts.clear();
        _blockFormattingContexts.addAll(layoutState.getBFCs());

        if (isPrint()) {
            setPageName(layoutState.getPageName());
            setExtraSpaceBottom(layoutState.getExtraSpaceBottom());
            setExtraSpaceTop(layoutState.getExtraSpaceTop());
            setNoPageBreak(layoutState.getNoPageBreak());
        }
    }

    public LayoutState copyStateForRelayout() {
        return isPrint() ?
                new LayoutState(_firstLines.copyOf(), _firstLetters.copyOf(), _currentMarkerData, emptyList(), getPageName(), 0, 0, 0) :
                new LayoutState(_firstLines.copyOf(), _firstLetters.copyOf(), _currentMarkerData, emptyList());
    }

    public void restoreStateForRelayout(LayoutState layoutState) {
        _firstLines = layoutState.getFirstLines();
        _firstLetters = layoutState.getFirstLetters();

        _currentMarkerData = layoutState.getCurrentMarkerData();

        if (isPrint()) {
            setPageName(layoutState.getPageName());
        }
    }

    public BlockFormattingContext getBlockFormattingContext() {
        return _blockFormattingContexts.getLast();
    }

    public void pushBFC(BlockFormattingContext bfc) {
        _blockFormattingContexts.add(bfc);
    }

    public void popBFC() {
        _blockFormattingContexts.removeLast();
    }

    public void pushLayer(Box master) {
        Layer layer;

        if (_rootLayer == null) {
            layer = new Layer(master);
            _rootLayer = layer;
        } else {
            Layer parent = getLayer();
            layer = new Layer(parent, master);
            parent.addChild(layer);
        }

        pushLayer(layer);
    }

    public void pushLayer(Layer layer) {
        _layers.add(layer);
    }

    public void popLayer() {
        Layer layer = getLayer();

        layer.finish(this);

        _layers.removeLast();
    }

    public Layer getLayer() {
        return _layers.getLast();
    }

    @Nullable
    public Layer getRootLayer() {
        return _rootLayer;
    }

    public void translate(int x, int y) {
        getBlockFormattingContext().translate(x, y);
    }

    /* code to keep track of all the id'd boxes */
    public void addBoxId(String id, Box box) {
        _sharedContext.addBoxId(id, box);
    }

    public void removeBoxId(String id) {
        _sharedContext.removeBoxId(id);
    }

    public boolean isInteractive() {
        return _sharedContext.isInteractive();
    }

    @Override
    public float getMmPerDot() {
        return _sharedContext.getMmPerPx();
    }

    @Override
    public int getDotsPerPixel() {
        return _sharedContext.getDotsPerPixel();
    }

    @Override
    public float getFontSize2D(FontSpecification font) {
        return _sharedContext.getFont(font).getSize2D();
    }

    @Override
    public float getXHeight(FontSpecification parentFont) {
        return _sharedContext.getXHeight(getFontContext(), parentFont);
    }

    @Nullable
    @CheckReturnValue
    @Override
    public FSFont getFont(FontSpecification font) {
        return _sharedContext.getFont(font);
    }

    @CheckReturnValue
    public UserAgentCallback getUac() {
        return _sharedContext.getUac();
    }

    public boolean isPrint() {
        return _sharedContext.isPrint();
    }

    public StyleTracker getFirstLinesTracker() {
        return _firstLines;
    }

    public StyleTracker getFirstLettersTracker() {
        return _firstLetters;
    }

    @Nullable
    @CheckReturnValue
    public MarkerData getCurrentMarkerData() {
        return _currentMarkerData;
    }

    public void setCurrentMarkerData(@Nullable MarkerData currentMarkerData) {
        _currentMarkerData = currentMarkerData;
    }

    public ReplacedElementFactory getReplacedElementFactory() {
        return _sharedContext.getReplacedElementFactory();
    }

    public FontContext getFontContext() {
        return _fontContext;
    }

    public ContentFunctionFactory getContentFunctionFactory() {
        return _contentFunctionFactory;
    }

    public SharedContext getSharedContext() {
        return _sharedContext;
    }

    public int getExtraSpaceBottom() {
        return _extraSpaceBottom;
    }

    public void setExtraSpaceBottom(int extraSpaceBottom) {
        _extraSpaceBottom = extraSpaceBottom;
    }

    public int getExtraSpaceTop() {
        return _extraSpaceTop;
    }

    public void setExtraSpaceTop(int extraSpaceTop) {
        _extraSpaceTop = extraSpaceTop;
    }

    public void resolveCounters(CalculatedStyle style, @Nullable Integer startIndex) {
        //new context for child elements
        CounterContext cc = new CounterContext(style, startIndex);
        _counterContextMap.put(style, cc);
    }

    public void resolveCounters(CalculatedStyle style) {
        resolveCounters(style, null);
    }

    public CounterContext getCounterContext(CalculatedStyle style) {
        return _counterContextMap.get(style);
    }

    @CheckReturnValue
    @Override
    public FSFontMetrics getFSFontMetrics(FSFont font) {
        return getTextRenderer().getFSFontMetrics(getFontContext(), font, "");
    }

    public class CounterContext {
        private final Map<String, Integer> _counters = new HashMap<>();
        /**
         * This is different because it needs to work even when the counter-properties cascade,
         * and it should also logically be redefined on each level (think list-items within list-items)
         */
        @Nullable
        private CounterContext _parent;

        /**
         * A CounterContext should really be reflected in the element hierarchy, but CalculatedStyles
         * reflect the ancestor hierarchy just as well and also handles pseudo-elements seamlessly.
         */
        CounterContext(CalculatedStyle style, @Nullable Integer startIndex) {
            // Numbering restarted via <ol start="x">
            if (startIndex != null) {
                _counters.put("list-item", startIndex);
            }
            _parent = _counterContextMap.get(style.getParent());
            if (_parent == null) _parent = new CounterContext();//top-level context, above root element
            //first the explicitly named counters
            List<CounterData> resets = style.getCounterReset();
            if (resets != null) {
                for (CounterData cd : resets) {
                    _parent.resetCounter(cd);
                }
            }

            List<CounterData> increments = style.getCounterIncrement();
            if (increments != null) {
                for (CounterData cd : increments) {
                    if (!_parent.incrementCounter(cd)) {
                        _parent.resetCounter(new CounterData(cd.getName(), 0));
                        _parent.incrementCounter(cd);
                    }
                }
            }

            // then the implicit list-item counter
            if (style.isIdent(CSSName.DISPLAY, IdentValue.LIST_ITEM)) {
                // Numbering restarted via <li value="x">
                if (startIndex != null) {
                    _parent._counters.put("list-item", startIndex);
                }
                _parent.incrementListItemCounter(1);
            }
        }

        private CounterContext() {

        }

        /**
         * @return true if a counter was found and incremented
         */
        private boolean incrementCounter(CounterData cd) {
            if ("list-item".equals(cd.getName())) {//reserved name for list-item counter in CSS3
                incrementListItemCounter(cd.getValue());
                return true;
            } else {
                Integer currentValue = _counters.get(cd.getName());
                if (currentValue == null) {
                    if (_parent == null) return false;
                    return _parent.incrementCounter(cd);
                } else {
                    _counters.put(cd.getName(), currentValue + cd.getValue());
                    return true;
                }
            }
        }

        private void incrementListItemCounter(int increment) {
            Integer currentValue = _counters.get("list-item");
            if (currentValue == null) {
                currentValue = 0;
            }
            _counters.put("list-item", currentValue + increment);
        }

        private void resetCounter(CounterData cd) {
            _counters.put(cd.getName(), cd.getValue());
        }

        public int getCurrentCounterValue(String name) {
            //only the counters of the parent are in scope
            //_parent is never null for a publicly accessible CounterContext
            Integer value = _parent.getCounter(name);
            if (value == null) {
                _parent.resetCounter(new CounterData(name, 0));
                return 0;
            } else {
                return value;
            }
        }

        @Nullable
        private Integer getCounter(String name) {
            Integer value = _counters.get(name);
            if (value != null) return value;
            if (_parent == null) return null;
            return _parent.getCounter(name);
        }

        public List<Integer> getCurrentCounterValues(String name) {
            //only the counters of the parent are in scope
            //_parent is never null for a publicly accessible CounterContext
            List<Integer> values = new ArrayList<>();
            _parent.getCounterValues(name, values);
            if (values.isEmpty()) {
                _parent.resetCounter(new CounterData(name, 0));
                values.add(0);
            }
            return values;
        }

        private void getCounterValues(String name, List<Integer> values) {
            if (_parent != null) _parent.getCounterValues(name, values);
            Integer value = _counters.get(name);
            if (value != null) values.add(value);
        }
    }

    @Nullable
    @CheckReturnValue
    public String getPageName() {
        return _pageName;
    }

    public void setPageName(@Nullable String currentPageName) {
        _pageName = currentPageName;
    }

    public int getNoPageBreak() {
        return _noPageBreak;
    }

    public void setNoPageBreak(int noPageBreak) {
        _noPageBreak = noPageBreak;
    }

    public boolean isPageBreaksAllowed() {
        return _noPageBreak == 0;
    }

    @Nullable
    @CheckReturnValue
    public String getPendingPageName() {
        return _pendingPageName;
    }

    public void setPendingPageName(@Nullable String pendingPageName) {
        _pendingPageName = pendingPageName;
    }

    @Nullable
    @CheckReturnValue
    public Layer getRootDocumentLayer() {
        return _rootDocumentLayer;
    }

    public void setRootDocumentLayer(Layer rootDocumentLayer) {
        _rootDocumentLayer = rootDocumentLayer;
    }

    @Nullable
    @CheckReturnValue
    public PageBox getPage() {
        return _page;
    }

    public void setPage(PageBox page) {
        _page = page;
    }

    public boolean isMayCheckKeepTogether() {
        return _mayCheckKeepTogether;
    }

    public void setMayCheckKeepTogether(boolean mayKeepTogether) {
        _mayCheckKeepTogether = mayKeepTogether;
    }

    @Nullable
    @CheckReturnValue
    public BreakAtLineContext getBreakAtLineContext() {
        return _breakAtLineContext;
    }

    public void setBreakAtLineContext(@Nullable BreakAtLineContext breakAtLineContext) {
        _breakAtLineContext = breakAtLineContext;
    }
}
