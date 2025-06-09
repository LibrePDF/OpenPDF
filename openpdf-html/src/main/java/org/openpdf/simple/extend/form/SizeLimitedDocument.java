/*
 * {{{ header & license
 * Copyright (c) 2007 Sean Bright
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
package org.openpdf.simple.extend.form;

import org.jspecify.annotations.Nullable;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * When applied to a Swing component, limits the total number of
 * characters that can be entered.
 */
class SizeLimitedDocument extends PlainDocument {
    private final int _maximumLength;

    public SizeLimitedDocument(int maximumLength) {
        _maximumLength = maximumLength;
    }

    @Override
    public void insertString(int offset, @Nullable String str, AttributeSet attr)
        throws BadLocationException {
        if (str == null) {
            return;
        }
        if (getLength() + str.length() <= _maximumLength) {
            super.insertString(offset, str, attr);
        }
    }
}
