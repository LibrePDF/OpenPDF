/*
 * Selector.java
 * Copyright (c) 2004, 2005 Torbjoern Gannholm
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
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Node;
import org.openpdf.css.extend.AttributeResolver;
import org.openpdf.css.extend.TreeResolver;
import org.openpdf.css.parser.Token;
import org.openpdf.css.sheet.Ruleset;
import org.openpdf.util.XRLog;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import static org.openpdf.css.newmatch.Selector.Axis.DESCENDANT_AXIS;


/**
 * A Selector is really a chain of CSS selectors that all need to be valid for
 * the selector to match.
 *
 * @author Torbjoern Gannholm
 */
public class Selector {
    private Ruleset _parent;
    private Selector chainedSelector;
    private Selector siblingSelector;
    private Axis _axis = DESCENDANT_AXIS;
    private String _name;
    private String _text;
    private String _namespaceURI;
    private int _pc;
    private String _pe;

    //specificity - correct values are gotten from the last Selector in the chain
    private int _specificityB;
    private int _specificityC;
    private int _specificityD;

    private int _pos;//to distinguish between selectors of same specificity

    private List<Condition> conditions;

    public enum Axis {DESCENDANT_AXIS, CHILD_AXIS, IMMEDIATE_SIBLING_AXIS}

    public static final int VISITED_PSEUDOCLASS = 2;
    public static final int HOVER_PSEUDOCLASS = 4;
    public static final int ACTIVE_PSEUDOCLASS = 8;
    public static final int FOCUS_PSEUDOCLASS = 16;

    /**
     * Give each a unique ID to be able to create a key to internalize Matcher.Mappers
     */
    private final int selectorID;
    private static int selectorCount;

    public Selector() {
        selectorID = selectorCount++;
    }

    /**
     * Check if the given Element matches this selector. Note: the parser should
     * give all class
     */
    public boolean matches(Node e, AttributeResolver attRes, TreeResolver treeRes) {
        if (siblingSelector != null) {
            Node sib = siblingSelector.getAppropriateSibling(e, treeRes);
            if (sib == null) {
                return false;
            }
            if (!siblingSelector.matches(sib, attRes, treeRes)) {
                return false;
            }
        }
        if (_name == null || treeRes.matchesElement(e, _namespaceURI, _name)) {
            if (conditions != null) {
                // all conditions need to be true
                for (Condition condition : conditions) {
                    if (!condition.matches(e, attRes, treeRes)) {
                        return false;
                    }
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Check if the given Element matches this selector's dynamic properties.
     * Note: the parser should give all class
     */
    public boolean matchesDynamic(Node e, AttributeResolver attRes, TreeResolver treeRes) {
        if (siblingSelector != null) {
            Node sib = siblingSelector.getAppropriateSibling(e, treeRes);
            if (sib == null) {
                return false;
            }
            if (!siblingSelector.matchesDynamic(sib, attRes, treeRes)) {
                return false;
            }
        }
        if (isPseudoClass(VISITED_PSEUDOCLASS)) {
            if (attRes == null || !attRes.isVisited(e)) {
                return false;
            }
        }
        if (isPseudoClass(ACTIVE_PSEUDOCLASS)) {
            if (attRes == null || !attRes.isActive(e)) {
                return false;
            }
        }
        if (isPseudoClass(HOVER_PSEUDOCLASS)) {
            if (attRes == null || !attRes.isHover(e)) {
                return false;
            }
        }
        if (isPseudoClass(FOCUS_PSEUDOCLASS)) {
            return attRes != null && attRes.isFocus(e);
        }
        return true;
    }

    /**
     * for unsupported or invalid CSS
     */
    public void addUnsupportedCondition() {
        addCondition(Condition.createUnsupportedCondition());
    }

    /**
     * the CSS condition that element has pseudo-class :link
     */
    public void addLinkCondition() {
        _specificityC++;
        addCondition(Condition.createLinkCondition());
    }

    /**
     * the CSS condition that element has pseudo-class :first-child
     */
    public void addFirstChildCondition() {
        _specificityC++;
        addCondition(Condition.createFirstChildCondition());
    }

    /**
     * the CSS condition that element has pseudo-class :last-child
     */
    public void addLastChildCondition() {
        _specificityC++;
        addCondition(Condition.createLastChildCondition());
    }

    /**
     * the CSS condition that element has pseudo-class :nth-child(an+b)
     */
    public void addNthChildCondition(String number) {
        _specificityC++;
        addCondition(Condition.createNthChildCondition(number));
    }

    /**
     * the CSS condition that element has pseudo-class :even
     */
    public void addEvenChildCondition() {
        _specificityC++;
        addCondition(Condition.createEvenChildCondition());
    }

    /**
     * the CSS condition that element has pseudo-class :odd
     */
    public void addOddChildCondition() {
        _specificityC++;
        addCondition(Condition.createOddChildCondition());
    }

    /**
     * the CSS condition :lang(Xx)
     */
    public void addLangCondition(String lang) {
        _specificityC++;
        addCondition(Condition.createLangCondition(lang));
    }

    /**
     * the CSS condition #ID
     */
    public void addIDCondition(String id) {
        _specificityB++;
        addCondition(Condition.createIDCondition(id));
    }

    /**
     * the CSS condition .class
     */
    public void addClassCondition(String className) {
        _specificityC++;
        addCondition(Condition.createClassCondition(className));
        _text = _name + Token.TK_PERIOD.getExternalName() + className;
    }

    /**
     * the CSS condition [attribute]
     */
    public void addAttributeExistsCondition(String namespaceURI, String name) {
        _specificityC++;
        addCondition(Condition.createAttributeExistsCondition(namespaceURI, name));
    }

    /**
     * the CSS condition [attribute=value]
     */
    public void addAttributeEqualsCondition(String namespaceURI, String name, String value) {
        _specificityC++;
        addCondition(Condition.createAttributeEqualsCondition(namespaceURI, name, value));
    }

    /**
     * the CSS condition [attribute^=value]
     */
    public void addAttributePrefixCondition(String namespaceURI, String name, String value) {
        _specificityC++;
        addCondition(Condition.createAttributePrefixCondition(namespaceURI, name, value));
    }

    /**
     * the CSS condition [attribute$=value]
     */
    public void addAttributeSuffixCondition(String namespaceURI, String name, String value) {
        _specificityC++;
        addCondition(Condition.createAttributeSuffixCondition(namespaceURI, name, value));
    }

    /**
     * the CSS condition [attribute*=value]
     */
    public void addAttributeSubstringCondition(String namespaceURI, String name, String value) {
        _specificityC++;
        addCondition(Condition.createAttributeSubstringCondition(namespaceURI, name, value));
    }

    /**
     * the CSS condition [attribute~=value]
     */
    public void addAttributeMatchesListCondition(String namespaceURI, String name, String value) {
        _specificityC++;
        addCondition(Condition.createAttributeMatchesListCondition(namespaceURI, name, value));
    }

    /**
     * the CSS condition [attribute|=value]
     */
    public void addAttributeMatchesFirstPartCondition(String namespaceURI, String name, String value) {
        _specificityC++;
        addCondition(Condition.createAttributeMatchesFirstPartCondition(namespaceURI, name, value));
    }

    /**
     * set which pseudo-classes must apply for this selector
     *
     * @param pc the values from AttributeResolver should be used. Once set
     *           they cannot be unset. Note that the pseudo-classes should be set one
     *           at a time, otherwise specificity of declaration becomes wrong.
     */
    public void setPseudoClass(int pc) {
        if (!isPseudoClass(pc)) {
            _specificityC++;
        }
        _pc |= pc;
    }

    /**
     * check if selector queries for dynamic properties
     *
     * @param pseudoElement The new pseudoElement value
     */
    /*
     * public boolean isDynamic() {
     * return (_pc != 0);
     * }
     */
    public void setPseudoElement(String pseudoElement) {
        if (_pe != null) {
            addUnsupportedCondition();
            XRLog.match(Level.WARNING, "Trying to set more than one pseudo-element");
        } else {
            _specificityD++;
            _pe = pseudoElement;
        }
    }

    /**
     * query if a pseudo-class must apply for this selector
     *
     * @param pc the values from AttributeResolver should be used.
     * @return The pseudoClass value
     */
    public boolean isPseudoClass(int pc) {
        return ((_pc & pc) != 0);
    }

    /**
     * Gets the pseudoElement attribute of the Selector object
     *
     * @return The pseudoElement value
     */
    public String getPseudoElement() {
        return _pe;
    }

    /**
     * get the next selector in the chain, for matching against elements along
     * the appropriate axis
     *
     * @return The chainedSelector value
     */
    public Selector getChainedSelector() {
        return chainedSelector;
    }

    /**
     * get the Ruleset that this Selector is part of
     *
     * @return The ruleset value
     */
    public Ruleset getRuleset() {
        return _parent;
    }

    /**
     * get the axis that this selector should be evaluated on
     *
     * @return The axis value
     */
    @CheckReturnValue
    public Axis getAxis() {
        return _axis;
    }

    /**
     * The correct specificity value for this selector and its sibling-axis
     * selectors
     */
    public int getSpecificityB() {
        return _specificityB;
    }

    /**
     * The correct specificity value for this selector and its sibling-axis
     * selectors
     */
    public int getSpecificityD() {
        return _specificityD;
    }

    /**
     * The correct specificity value for this selector and its sibling-axis
     * selectors
     */
    public int getSpecificityC() {
        return _specificityC;
    }

    /**
     * returns "a number in a large base" with specificity and specification
     * order of selector
     *
     * @return The order value
     */
    String getOrder() {
        if (chainedSelector != null) {
            return chainedSelector.getOrder();
        }//only "deepest" value is correct
        String b = "000" + getSpecificityB();
        String c = "000" + getSpecificityC();
        String d = "000" + getSpecificityD();
        String p = "00000" + _pos;
        return "0" + b.substring(b.length() - 3) + c.substring(c.length() - 3) + d.substring(d.length() - 3) + p.substring(p.length() - 5);
    }

    /**
     * Gets the appropriateSibling attribute of the Selector object
     *
     * @return The appropriateSibling value
     */
    @Nullable
    @CheckReturnValue
    Node getAppropriateSibling(Node e, TreeResolver treeRes) {
        return switch (_axis) {
            case IMMEDIATE_SIBLING_AXIS -> treeRes.getPreviousSiblingElement(e);
            case DESCENDANT_AXIS, CHILD_AXIS -> {
                XRLog.exception("Bad sibling axis");
                yield null;
            }
        };
    }

    /**
     * Adds a feature to the Condition attribute of the Selector object
     *
     * @param c The feature to be added to the Condition attribute
     */
    private void addCondition(Condition c) {
        if (conditions == null) {
            conditions = new ArrayList<>();
        }
        if (_pe != null) {
            conditions.add(Condition.createUnsupportedCondition());
            XRLog.match(Level.WARNING, "Trying to append conditions to pseudoElement " + _pe);
        }
        conditions.add(c);
    }

    public int getSelectorID() {
        return selectorID;
    }

    public void setName(String name) {
        _name = name;
        _text = name;
        _specificityD++;
    }

    public String getSelectorText() {
    	return _text + (chainedSelector != null ? (" "+chainedSelector.getSelectorText()) : "");
    }

    public void setPos(int pos) {
        _pos = pos;
        if (siblingSelector != null) {
            siblingSelector.setPos(pos);
        }
        if (chainedSelector != null) {
            chainedSelector.setPos(pos);
        }
    }

    public void setParent(Ruleset ruleset) {
        _parent = ruleset;
    }

    public void setAxis(Axis axis) {
        _axis = axis;
    }

    public void setSpecificityB(int b) {
        _specificityB = b;
    }

    public void setSpecificityC(int c) {
        _specificityC = c;
    }

    public void setSpecificityD(int d) {
        _specificityD = d;
    }

    public void setChainedSelector(Selector selector) {
        chainedSelector = selector;
    }

    public void setSiblingSelector(Selector selector) {
        siblingSelector = selector;
    }

    public void setNamespaceURI(String namespaceURI) {
        _namespaceURI = namespaceURI;
    }

    @Override
    public String toString() {
        return "%s{%s}".formatted(getClass().getSimpleName(), _name);
    }
}

