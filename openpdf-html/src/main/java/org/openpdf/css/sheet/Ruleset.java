/*
 * Ruleset.java
 * Copyright (c) 2004, 2005 Patrick Wright, Torbjoern Gannholm
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
package org.openpdf.css.sheet;

import org.openpdf.css.newmatch.Selector;
import org.openpdf.css.sheet.StylesheetInfo.Origin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * @author Torbjoern Gannholm
 * @author Patrick Wright
 */
public class Ruleset {
    private final Origin _origin;
    private final List<PropertyDeclaration> _props = new ArrayList<>();
    private final List<Selector> _fsSelectors = new ArrayList<>();

    public Ruleset(Origin orig) {
        _origin = orig;
    }

    /**
     * Returns an Iterator of PropertyDeclarations pulled from this
     * CSSStyleRule.
     *
     * @return The propertyDeclarations value
     */
    public List<PropertyDeclaration> getPropertyDeclarations() {
        return Collections.unmodifiableList(_props);
    }

    public void addProperty(PropertyDeclaration decl) {
        _props.add(decl);
    }

    public void addAllProperties(List<PropertyDeclaration> props) {
        _props.addAll(props);
    }

    public void addFSSelector(Selector selector) {
        _fsSelectors.add(selector);
    }

    public List<Selector> getFSSelectors() {
        return _fsSelectors;
    }

    public Origin getOrigin() {
        return _origin;
    }

    @Override
    public String toString() {
        return "%s{%s %s %s}".formatted(getClass().getSimpleName(), _origin, _props, _fsSelectors);
    }
}
