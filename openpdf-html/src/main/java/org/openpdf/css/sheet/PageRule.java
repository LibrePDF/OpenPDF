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
package org.openpdf.css.sheet;

import org.jspecify.annotations.Nullable;
import org.openpdf.css.constants.MarginBoxName;
import org.openpdf.css.sheet.StylesheetInfo.Origin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PageRule implements RulesetContainer {
    private final String _name;
    private final String _pseudoPage;
    private final Ruleset _ruleset;
    private final Origin _origin;

    private final Map<MarginBoxName, List<PropertyDeclaration>> _marginBoxes = new HashMap<>();

    private int _pos;

    private final int _specificityF;
    private final int _specificityG;
    private final int _specificityH;

    public PageRule(Origin origin, @Nullable String name, @Nullable String pseudoPage, Map<MarginBoxName, List<PropertyDeclaration>> marginBoxes, Ruleset ruleset) {
        _origin = origin;
        _name = name;
        _specificityF = name == null ? 0 : 1;
        _ruleset = ruleset;
        _pseudoPage = pseudoPage;
        _specificityG = "first".equals(pseudoPage) ? 1 : 0;
        _specificityH = "first".equals(pseudoPage) ? 0 : 1;
        _marginBoxes.putAll(marginBoxes);
    }

    public String getPseudoPage() {
        return _pseudoPage;
    }

    public Ruleset getRuleset() {
        return _ruleset;
    }

    @Override
    public void addContent(Ruleset ruleset) {
        throw new IllegalStateException("Ruleset has already been set");
    }

    @Override
    public Origin getOrigin() {
        return _origin;
    }

    public String getName() {
        return _name;
    }

    public List<PropertyDeclaration> getMarginBoxProperties(MarginBoxName name) {
        return _marginBoxes.get(name);
    }

    public Map<MarginBoxName, List<PropertyDeclaration>> getMarginBoxes() {
        return _marginBoxes;
    }

    public long getOrder() {
        long result = 0;

        result |= (long)_specificityF << 32;
        result |= (long)_specificityG << 24;
        result |= (long)_specificityH << 16;
        result |= _pos;

        return result;
    }

    public boolean applies(@Nullable String pageName, String pseudoPage) {
        if (_name == null && _pseudoPage == null) {
            return true;
        } else if (_name == null &&
                (_pseudoPage.equals(pseudoPage) ||
                        (_pseudoPage.equals("right") && pseudoPage != null && pseudoPage.equals("first")))) { // assume first page is a right page
            return true;
        } else if (_name != null && _name.equals(pageName) && _pseudoPage == null) {
            return true;
        } else return _name != null && _name.equals(pageName) && _pseudoPage.equals(pseudoPage);
    }

    public int getPos() {
        return _pos;
    }

    public void setPos(int pos) {
        _pos = pos;
    }
}
