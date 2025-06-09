/*
 * {{{ header & license
 * Copyright (c) 2006 Wisconsin Court System
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
package org.openpdf.layout;

import org.openpdf.css.extend.ContentFunction;
import org.openpdf.css.parser.FSFunction;

/**
 * A bean which contains all the information necessary to invoke a
 * {@link ContentFunction}.
 * @see ContentFunction
 */
public class FunctionData {
    private final ContentFunction _contentFunction;
    private final FSFunction _function;

    public FunctionData(ContentFunction contentFunction, FSFunction function) {
        _contentFunction = contentFunction;
        _function = function;
    }

    public ContentFunction getContentFunction() {
        return _contentFunction;
    }

    public FSFunction getFunction() {
        return _function;
    }
}
