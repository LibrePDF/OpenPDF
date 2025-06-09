/*
 * Stylesheet.java
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

import org.openpdf.css.sheet.StylesheetInfo.Origin;

import java.util.ArrayList;
import java.util.List;


/**
 * A representation of a CSS style sheet. A Stylesheet has the sheet's rules in
 * {@link Ruleset}, and has an origin--either user agent, user, or author. A
 * Stylesheet can only be instantiated from a SAC CSSStyleSheet instance-- this
 * would be the output of a SAC-compliant parser after parsing a CSS stream or
 * source. A Stylesheet is immutable; after instantiation, you can query the
 * origin and the {@link Ruleset}, but not modify either of them.
 *
 * @author Torbjoern Gannholm
 * @author Patrick Wright
 */
public class Stylesheet implements RulesetContainer {
    /**
     * The info for this stylesheet
     */
    private final String _uri;
    private final Origin _origin;

    private final List<FontFaceRule> _fontFaceRules = new ArrayList<>();
    private final List<StylesheetInfo> _importRules = new ArrayList<>();
    private final List<Object> _contents = new ArrayList<>();

    /**
     * Creates a new instance of Stylesheet
     */
    public Stylesheet(String uri, Origin origin) {
        _uri = uri;
        _origin = origin;
    }

    /**
     * Gets the origin attribute of the Stylesheet object
     *
     * @return The origin value
     */
    @Override
    public Origin getOrigin() {
        return _origin;
    }

    /**
     * Gets the URI of the Stylesheet object
     *
     * @return The URI
     */
    public String getURI() {
        return _uri;
    }

    @Override
    public void addContent(Ruleset ruleset) {
        _contents.add(ruleset);
    }

    public void addContent(MediaRule rule) {
        _contents.add(rule);
    }

    public void addContent(PageRule rule) {
        _contents.add(rule);
    }

    public List<Object> getContents() {
        return _contents;
    }

    public void addImportRule(StylesheetInfo info) {
        _importRules.add(info);
    }

    public List<StylesheetInfo> getImportRules() {
        return _importRules;
    }

    public void addFontFaceRule(FontFaceRule rule) {
        _fontFaceRules.add(rule);
    }

    public List<FontFaceRule> getFontFaceRules() {
        return _fontFaceRules;
    }

}
