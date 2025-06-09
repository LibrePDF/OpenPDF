/*
 * StylesheetFactory.java
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
package org.openpdf.css.extend;

import com.google.errorprone.annotations.CheckReturnValue;
import org.openpdf.css.sheet.Ruleset;
import org.openpdf.css.sheet.Stylesheet;
import org.openpdf.css.sheet.StylesheetInfo;
import org.openpdf.css.sheet.StylesheetInfo.Origin;

import java.io.Reader;


/**
 * A Factory class for Cascading Style Sheets. Sheets are parsed using a single
 * parser instance for all sheets. Sheets are cached by URI using an LRU test,
 * but timestamp of file is not checked.
 *
 * @author Torbjoern Gannholm
 */
public interface StylesheetFactory {
    @CheckReturnValue
    Stylesheet parse(Reader reader, StylesheetInfo info);
    @CheckReturnValue
    Stylesheet parse(Reader reader, String uri, Origin origin);
    @CheckReturnValue
    Ruleset parseStyleDeclaration(Origin origin, String style);
    @CheckReturnValue
    Stylesheet getStylesheet(StylesheetInfo si);
}
