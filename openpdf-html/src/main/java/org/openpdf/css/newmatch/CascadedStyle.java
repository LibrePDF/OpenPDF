/*
 * CascadedStyle.java
 * Copyright (c) 2004, 2005 Patrick Wright, Torbjoern Gannholm
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
 *
 */
package org.openpdf.css.newmatch;

import org.w3c.dom.css.CSSPrimitiveValue;
import org.openpdf.css.constants.CSSName;
import org.openpdf.css.constants.IdentValue;
import org.openpdf.css.parser.PropertyValue;
import org.openpdf.css.sheet.PropertyDeclaration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.openpdf.css.sheet.StylesheetInfo.Origin.USER;


/**
 * Holds a set of {@link org.openpdf.css.sheet.PropertyDeclaration}s for
 * each unique CSS property name. What properties belong in the set is not
 * determined, except that multiple entries are resolved into a single set using
 * cascading rules. The set is cascaded during instantiation, so once you have a
 * CascadedStyle, the PropertyDeclarations you retrieve from it will have been
 * resolved following the CSS cascading rules. Note that this class knows
 * nothing about CSS selector-matching rules. Before creating a CascadedStyle,
 * you will need to determine which PropertyDeclarations belong in the set--for
 * example, by matching {@link org.openpdf.css.sheet.Ruleset}s to {@link
 * org.w3c.dom.Document} {@link org.w3c.dom.Element}s via their selectors. You
 * can get individual properties by using {@link #propertyByName(CSSName)} or an
 * {@link java.util.Iterator} of properties with {@link
 * #getCascadedPropertyDeclarations()}. Check for individual property assignments
 * using {@link #hasProperty(CSSName)}. A CascadedStyle is immutable, as
 * properties can not be added or removed from it once instantiated.
 *
 * @author Torbjoern Gannholm
 * @author Patrick Wright
 */
public class CascadedStyle {
    private final Map<CSSName, PropertyDeclaration> cascadedProperties;

    private String fingerprint;

    /**
     * Creates a {@code CascadedStyle}, setting the display property
     * to the value of the {@code display} parameter.
     */
    public static CascadedStyle createAnonymousStyle(IdentValue display) {
        CSSPrimitiveValue val = new PropertyValue(display);

        List<PropertyDeclaration> props = singletonList(
                new PropertyDeclaration(CSSName.DISPLAY, val, true, USER));

        return new CascadedStyle(props);
    }

    /**
     * Creates a {@code CascadedStyle} using the provided property
     * declarations.  It is used when a box requires a style that does not
     * correspond to anything in the parsed stylesheets.
     * @param declarations An array of PropertyDeclaration objects created with
     * {@link #createLayoutPropertyDeclaration(CSSName, IdentValue)}
     * @see #createLayoutPropertyDeclaration(CSSName, IdentValue)
     */
    public static CascadedStyle createLayoutStyle(PropertyDeclaration... declarations) {
        return new CascadedStyle(asList(declarations));
    }

    public static CascadedStyle createLayoutStyle(List<PropertyDeclaration> declarations) {
        return new CascadedStyle(declarations);
    }

    /**
     * Creates a {@code CascadedStyle} using style information from
     * {@code startingPoint} and then adding the property declarations
     * from {@code decls}.
     * @param decls An array of PropertyDeclaration objects created with
     * {@link #createLayoutPropertyDeclaration(CSSName, IdentValue)}
     * @see #createLayoutPropertyDeclaration(CSSName, IdentValue)
     */
    public static CascadedStyle createLayoutStyle(
            CascadedStyle startingPoint, PropertyDeclaration[] decls) {
        return new CascadedStyle(startingPoint.cascadedProperties, asList(decls).iterator());
    }

    /**
     * Creates a {@code PropertyDeclaration} suitable for passing to
     * {@link #createLayoutStyle(List)} or
     * {@link #createLayoutStyle(CascadedStyle, PropertyDeclaration[])}
     */
    public static PropertyDeclaration createLayoutPropertyDeclaration(
            CSSName cssName, IdentValue display) {
        CSSPrimitiveValue val = new PropertyValue(display);
        // Urk... kind of ugly, but we really want this value to be used
        return new PropertyDeclaration(cssName, val, true, USER);
    }

    /**
     * Constructs a new CascadedStyle, given an {@link java.util.Iterator} of
     * {@link org.openpdf.css.sheet.PropertyDeclaration}s already sorted
     * by specificity of the CSS selector they came from. The Iterator can have
     * multiple PropertyDeclarations with the same name; the property cascade
     * will be resolved during instantiation, resulting in a set of
     * PropertyDeclarations. Once instantiated, properties may be retrieved
     * using the normal API for the class.
     *
     * @param iter An Iterator containing PropertyDeclarations in order of
     *             specificity.
     */
    CascadedStyle(Iterable<PropertyDeclaration> iter) {
        this(emptyMap(), iter.iterator());
    }

    private CascadedStyle(Map<CSSName, PropertyDeclaration> startingPoint, Iterator<PropertyDeclaration> iter) {
        //do a bucket-sort on importance and origin
        //properties should already be in order of specificity
        List<List<PropertyDeclaration>> buckets = new ArrayList<>(PropertyDeclaration.IMPORTANCE_AND_ORIGIN_COUNT);
        for (int i = 0; i < PropertyDeclaration.IMPORTANCE_AND_ORIGIN_COUNT; i++) {
            buckets.add(new ArrayList<>());
        }

        while (iter.hasNext()) {
            PropertyDeclaration prop = iter.next();
            buckets.get(prop.getImportanceAndOrigin()).add(prop);
        }

        Map<CSSName, PropertyDeclaration> cascadedProperties = new TreeMap<>(startingPoint);
        for (List<PropertyDeclaration> bucket : buckets) {
            for (PropertyDeclaration prop : bucket) {
                cascadedProperties.put(prop.getCSSName(), prop);
            }
        }
        this.cascadedProperties = cascadedProperties;
    }

    /**
     * Default constructor with no initialization. Don't use this to instantiate
     * the class, as the class is immutable and this will leave it without any
     * properties.
     */
    private CascadedStyle(Map<CSSName, PropertyDeclaration> cascadedProperties) {
        this.cascadedProperties = cascadedProperties;
    }

    /**
     * Get an empty singleton, used to negate inheritance of properties
     */
    public static final CascadedStyle emptyCascadedStyle = new CascadedStyle(new TreeMap<>());

    /**
     * Returns true if property has been defined in this style.
     *
     * @param cssName The CSS property name, e.g. "font-family".
     * @return True if the property is defined in this set.
     */
    public boolean hasProperty(CSSName cssName) {
        return cascadedProperties.containsKey(cssName);
    }


    /**
     * Returns a {@link org.openpdf.css.sheet.PropertyDeclaration} by CSS
     * property name, e.g. "font-family". Properties are already cascaded during
     * instantiation, so this will return the actual property (and corresponding
     * value) to use for CSS-based layout and rendering.
     *
     * @param cssName The CSS property name, e.g. "font-family".
     * @return The PropertyDeclaration, if declared in this set, or null
     *         if not found.
     */
    public PropertyDeclaration propertyByName(CSSName cssName) {
        return cascadedProperties.get(cssName);
    }

    /**
     * Gets the ident attribute of the CascadedStyle object
     */
    public IdentValue getIdent(CSSName cssName) {
        PropertyDeclaration pd = propertyByName(cssName);
        return (pd == null ? null : pd.asIdentValue());
    }


    /**
     * Returns an {@link java.util.Iterator} over the set of {@link
     * org.openpdf.css.sheet.PropertyDeclaration}s already matched in this
     * CascadedStyle. For a given property name, there may be no match, in which
     * case there will be no {@code PropertyDeclaration} for that property
     * name in the Iterator.
     *
     * @return Iterator over a set of properly cascaded PropertyDeclarations.
     */
    public Iterator<PropertyDeclaration> getCascadedPropertyDeclarations() {
        List<PropertyDeclaration> list = new ArrayList<>(cascadedProperties.size());
        list.addAll(cascadedProperties.values());
        return list.iterator();
    }

    public int countAssigned() { return cascadedProperties.size(); }

    public String getFingerprint() {
        if (fingerprint == null) {
            StringBuilder sb = new StringBuilder();
            for (PropertyDeclaration propertyDeclaration : cascadedProperties.values()) {
                sb.append(propertyDeclaration.getFingerprint());
            }
            fingerprint = sb.toString();
        }
        return fingerprint;
    }
}
