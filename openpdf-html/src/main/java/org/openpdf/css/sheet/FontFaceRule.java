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

import org.openpdf.css.newmatch.CascadedStyle;
import org.openpdf.css.sheet.StylesheetInfo.Origin;
import org.openpdf.css.style.CalculatedStyle;
import org.openpdf.css.style.EmptyStyle;
import org.openpdf.util.LazyEvaluated;
import org.openpdf.util.XRRuntimeException;

import static org.openpdf.util.LazyEvaluated.lazy;

public class FontFaceRule implements RulesetContainer {
    private final Origin _origin;
    private Ruleset _ruleset;
    private final LazyEvaluated<CalculatedStyle> _calculatedStyle = lazy(() ->
        new EmptyStyle().deriveStyle(
            CascadedStyle.createLayoutStyle(_ruleset.getPropertyDeclarations()))
    );

    public FontFaceRule(Origin origin) {
        _origin = origin;
    }

    @Override
    public void addContent(Ruleset ruleset) {
        if (_ruleset != null) {
            throw new XRRuntimeException("Ruleset can only be set once");
        }
        _ruleset = ruleset;
    }

    @Override
    public Origin getOrigin() {
        return _origin;
    }

    public CalculatedStyle getCalculatedStyle() {
        return _calculatedStyle.get();
    }

    public boolean hasFontFamily() {
        return has("font-family");
    }

    public boolean hasFontWeight() {
        return has("font-weight");
    }

    public boolean hasFontStyle() {
        return has("font-style");
    }

    private boolean has(String property) {
        return _ruleset.getPropertyDeclarations().stream()
            .anyMatch(declaration -> property.equals(declaration.getPropertyName()));
    }
}
