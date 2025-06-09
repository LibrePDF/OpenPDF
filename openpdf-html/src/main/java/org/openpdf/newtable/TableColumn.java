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
package org.openpdf.newtable;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;
import org.openpdf.css.style.CalculatedStyle;
import org.openpdf.layout.Styleable;

/**
 * An object representing an element with {@code display: table-column} or
 * {@code display: table-column-group}.
 */
public class TableColumn implements Styleable {
    @Nullable
    private Element _element;
    private CalculatedStyle _style;

    private TableColumn _parent;

    public TableColumn() {
    }

    public TableColumn(Element element, CalculatedStyle style) {
        _element = element;
        _style = style;
    }

    @Override
    public Element getElement() {
        return _element;
    }

    @Nullable
    @CheckReturnValue
    @Override
    public String getPseudoElementOrClass() {
        return null;
    }

    @Nullable
    @CheckReturnValue
    @Override
    public CalculatedStyle getStyle() {
        return _style;
    }

    @Override
    public void setElement(@Nullable Element e) {
        _element = e;
    }

    @Override
    public void setStyle(@Nullable CalculatedStyle style) {
        _style = style;
    }

    public TableColumn getParent() {
        return _parent;
    }

    public void setParent(TableColumn parent) {
        _parent = parent;
    }
}
