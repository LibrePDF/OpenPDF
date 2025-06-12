/*
 * Matcher.java
 * Copyright (c) 2004, 2005 Torbjoern Gannholm
 * Copyright (c) 2006 Wisconsin Court System
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
 *
 */
package org.openpdf.css.newmatch;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.openpdf.css.constants.MarginBoxName;
import org.openpdf.css.extend.AttributeResolver;
import org.openpdf.css.extend.StylesheetFactory;
import org.openpdf.css.extend.TreeResolver;
import org.openpdf.css.sheet.FontFaceRule;
import org.openpdf.css.sheet.MediaRule;
import org.openpdf.css.sheet.PageRule;
import org.openpdf.css.sheet.PropertyDeclaration;
import org.openpdf.css.sheet.Ruleset;
import org.openpdf.css.sheet.Stylesheet;
import org.openpdf.util.Util;
import org.openpdf.util.XRLog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import static java.util.Collections.synchronizedMap;
import static java.util.Collections.synchronizedSet;
import static java.util.Comparator.comparingLong;
import static java.util.Objects.requireNonNullElseGet;
import static org.openpdf.css.newmatch.Selector.Axis.IMMEDIATE_SIBLING_AXIS;
import static org.openpdf.css.sheet.StylesheetInfo.Origin.AUTHOR;


/**
 * @author Torbjoern Gannholm
 */
public class Matcher {

    private final Mapper docMapper;
    private final AttributeResolver _attRes;
    private final TreeResolver _treeRes;
    private final StylesheetFactory _styleFactory;

    private final Map<Node, Mapper> _map = synchronizedMap(new HashMap<>());

    //handle dynamic
    private final Set<Node> _hoverElements = synchronizedSet(new HashSet<>(0));
    private final List<PageRule> _pageRules = new ArrayList<>(0);
    private final List<FontFaceRule> _fontFaceRules = new ArrayList<>(0);

    public Matcher(TreeResolver tr, AttributeResolver ar,
                   StylesheetFactory factory, List<Stylesheet> stylesheets, String medium) {
        _treeRes = tr;
        _attRes = ar;
        _styleFactory = factory;
        docMapper = createDocumentMapper(stylesheets, medium);
    }

    public CascadedStyle getCascadedStyle(Element e, boolean restyle) {
        Mapper em = restyle ? matchElement(e) : getMapper(e);
        return em.getCascadedStyle(e);
    }

    /**
     * May return null.
     * We assume that restyle has already been done by a getCascadedStyle if necessary.
     */
    @Nullable
    public CascadedStyle getPECascadedStyle(Element e, String pseudoElement) {
        Mapper em = getMapper(e);
        return em.getPECascadedStyle(pseudoElement);
    }

    @NonNull
    @CheckReturnValue
    public PageInfo getPageCascadedStyle(@Nullable String pageName, String pseudoPage) {
        List<PropertyDeclaration> props = new ArrayList<>();
        Map<MarginBoxName, List<PropertyDeclaration>> marginBoxes = new HashMap<>();

        for (PageRule pageRule : _pageRules) {
            if (pageRule.applies(pageName, pseudoPage)) {
                props.addAll(pageRule.getRuleset().getPropertyDeclarations());
                marginBoxes.putAll(pageRule.getMarginBoxes());
            }
        }

        CascadedStyle style = props.isEmpty() ? CascadedStyle.emptyCascadedStyle : new CascadedStyle(props);
        return new PageInfo(props, style, marginBoxes);
    }

    public List<FontFaceRule> getFontFaceRules() {
        return _fontFaceRules;
    }

    public boolean isHoverStyled(Node e) {
        return _hoverElements.contains(e);
    }

    private Mapper matchElement(Node e) {
        Node parent = _treeRes.getParentElement(e);

        if (parent != null) {
            return getMapper(parent).mapChild(e);
        } else { // has to be a document or a fragment node
            return docMapper.mapChild(e);
        }
    }

    private Mapper createDocumentMapper(List<Stylesheet> stylesheets, String medium) {
        Map<String, Selector> sorter = new TreeMap<>();
        addAllStylesheets(stylesheets, sorter, medium);
        XRLog.match("Matcher created with " + sorter.size() + " selectors");
        return new Mapper(sorter.values());
    }

    private void addAllStylesheets(List<Stylesheet> stylesheets, Map<String, Selector> sorter, String medium) {
        int count = 0;
        int pCount = 0;
        for (Stylesheet stylesheet : stylesheets) {
            for (Object obj : stylesheet.getContents()) {
                if (obj instanceof Ruleset ruleSet) {
                    for (Selector selector : ruleSet.getFSSelectors()) {
                        selector.setPos(++count);
                        sorter.put(selector.getOrder(), selector);
                    }
                } else if (obj instanceof PageRule pageRule) {
                    pageRule.setPos(++pCount);
                    _pageRules.add(pageRule);
                } else if (obj instanceof MediaRule mediaRule) {
                    if (mediaRule.matches(medium)) {
                        for (Ruleset ruleset : mediaRule.getContents()) {
                            for (Selector selector : ruleset.getFSSelectors()) {
                                selector.setPos(++count);
                                sorter.put(selector.getOrder(), selector);
                            }
                        }
                    }
                }
            }

            _fontFaceRules.addAll(stylesheet.getFontFaceRules());
        }

        _pageRules.sort(comparingLong(PageRule::getOrder));
    }

    private void link(Node e, Mapper m) {
        _map.put(e, m);
    }

    private Mapper getMapper(Node e) {
        return requireNonNullElseGet(_map.get(e),
                () -> matchElement(e));
    }

    private Ruleset getElementStyle(Node e) {
        if (_attRes == null || _styleFactory == null) {
            return null;
        }

        String style = _attRes.getElementStyling(e);
        if (Util.isNullOrEmpty(style)) {
            return null;
        }

        return _styleFactory.parseStyleDeclaration(AUTHOR, style);
    }

    private Ruleset getNonCssStyle(Node e) {
        if (_attRes == null || _styleFactory == null) {
            return null;
        }
        String style = _attRes.getNonCssStyling(e);
        if (Util.isNullOrEmpty(style)) {
            return null;
        }
        return _styleFactory.parseStyleDeclaration(AUTHOR, style);

    }

    /**
     * Mapper represents a local CSS for a Node that is used to match the Node's
     * children.
     *
     * @author Torbjoern Gannholm
     */
    private class Mapper {
        private final List<Selector> axes;
        private final Map<String, List<Selector>> pseudoSelectors;
        private final List<Selector> mappedSelectors;
        private Map<List<Integer>, Mapper> children;

        Mapper(Collection<Selector> selectors) {
            this(new ArrayList<>(selectors), null, null);
        }

        private Mapper(List<Selector> childAxes, Map<String, List<Selector>> pseudoSelectors, List<Selector> mappedSelectors) {
            this.axes = childAxes;
            this.pseudoSelectors = pseudoSelectors;
            this.mappedSelectors = mappedSelectors;
        }

        /**
         * Side effect: creates and stores a Mapper for the element
         *
         * @return The selectors that matched, sorted according to specificity
         *         (more correct: preserves the sort order from Matcher creation)
         */
        Mapper mapChild(Node e) {
            List<Selector> childAxes = new ArrayList<>(axes.size() + 10);
            Map<String, List<Selector>> pseudoSelectors = new HashMap<>();
            List<Selector> mappedSelectors = new ArrayList<>();
            List<Integer> key = new ArrayList<>();
            for (Selector axe : axes) {
                switch (axe.getAxis()) {
                    case DESCENDANT_AXIS -> childAxes.add(axe); // carry it forward to other descendants
                    case IMMEDIATE_SIBLING_AXIS ->
                            throw new RuntimeException("Selector axis: " + IMMEDIATE_SIBLING_AXIS);
                    case CHILD_AXIS -> {
                    }
                }

                if (!axe.matches(e, _attRes, _treeRes)) {
                    continue;
                }
                //Assumption: if it is a pseudo-element, it does not also have dynamic pseudo-class
                String pseudoElement = axe.getPseudoElement();
                if (pseudoElement != null) {
                    List<Selector> l = pseudoSelectors.computeIfAbsent(pseudoElement, k -> new ArrayList<>());
                    l.add(axe);
                    key.add(axe.getSelectorID());
                    continue;
                }
                if (axe.isPseudoClass(Selector.HOVER_PSEUDOCLASS)) {
                    _hoverElements.add(e);
                }
                if (!axe.matchesDynamic(e, _attRes, _treeRes)) {
                    continue;
                }
                key.add(axe.getSelectorID());
                Selector chain = axe.getChainedSelector();
                if (chain == null) {
                    mappedSelectors.add(axe);
                } else {
                    switch (chain.getAxis()) {
                        case IMMEDIATE_SIBLING_AXIS ->
                                throw new RuntimeException("Selector axis: " + IMMEDIATE_SIBLING_AXIS);
                        case CHILD_AXIS,
                             DESCENDANT_AXIS -> childAxes.add(chain);
                    }
                }
            }
            if (children == null) children = new HashMap<>();
            Mapper childMapper = children.computeIfAbsent(key, k ->
                    new Mapper(childAxes, pseudoSelectors, mappedSelectors));
            link(e, childMapper);
            return childMapper;
        }

        CascadedStyle getCascadedStyle(Node e) {
            Ruleset elementStyling = getElementStyle(e);
            Ruleset nonCssStyling = getNonCssStyle(e);
            List<PropertyDeclaration> propList = new ArrayList<>();
            //specificity 0,0,0,0
            if (nonCssStyling != null) {
                propList.addAll(nonCssStyling.getPropertyDeclarations());
            }
            //these should have been returned in order of specificity
            for (Selector selector : mappedSelectors) {
                propList.addAll(selector.getRuleset().getPropertyDeclarations());
            }
            //specificity 1,0,0,0
            if (elementStyling != null) {
                propList.addAll(elementStyling.getPropertyDeclarations());
            }
            return propList.isEmpty() ? CascadedStyle.emptyCascadedStyle : new CascadedStyle(propList);
        }

        /**
         * May return null.
         * We assume that restyle has already been done by a getCascadedStyle if necessary.
         */
        @Nullable
        public CascadedStyle getPECascadedStyle(String pseudoElement) {
            Iterator<Map.Entry<String, List<Selector>>> si = pseudoSelectors.entrySet().iterator();
            if (!si.hasNext()) {
                return null;
            }
            List<Selector> pe = pseudoSelectors.get(pseudoElement);
            if (pe == null) return null;

            List<PropertyDeclaration> propList = new ArrayList<>();
            for (Selector selector : pe) {
                propList.addAll(selector.getRuleset().getPropertyDeclarations());
            }

            if (propList.isEmpty())
                return CascadedStyle.emptyCascadedStyle; // already internalized
            else {
                return new CascadedStyle(propList);
            }
        }
    }
}

