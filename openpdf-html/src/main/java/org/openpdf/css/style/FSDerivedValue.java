/*
 * Copyright (c) 2005 Patrick Wright
 * Copyright (c) 2007, 2008 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the
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
 */


package org.openpdf.css.style;

import org.openpdf.css.constants.CSSName;
import org.openpdf.css.constants.IdentValue;
import org.openpdf.css.parser.FSColor;


/**
 * Marker interface for all derived values. All methods for any
 * possible style are declared here, which doesn't make complete
 * sense, as, for example, a length can't return a value for asColor().
 * This is done so that CalculatedStyle can just look up an
 * FSDerivedValue, without casting, and call the appropriate function
 * without a cast to the appropriate subtype.
 * The users of CalculatedStyle have to then make sure they don't
 * make meaningless calls like asColor(CSSName.HEIGHT). DerivedValue
 * and IdentValue, the two implementations of this interface, just
 * throw a RuntimeException if they can't handle the call.
 *
 * <b>NOTE:</b> When resolving proportional property values, implementations of this
 * interface must be prepared to handle calls with different base values.
 */
public interface FSDerivedValue {
    boolean isDeclaredInherit();

    float asFloat();
    FSColor asColor();

    float getFloatProportionalTo(
            CSSName cssName,
            float baseValue,
            CssContext ctx
    );
    String asString();
    String[] asStringArray();
    IdentValue asIdentValue();
    boolean hasAbsoluteUnit();
    boolean isDependentOnFontSize();
    boolean isIdent();
}
