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
package org.openpdf.css.parser;

import org.jspecify.annotations.Nullable;

public class CSSParseException extends RuntimeException {
    @Nullable
    private final Token _found;
    private final Token @Nullable [] _expected;
    private int _line;

    @Nullable
    private final String _genericMessage;

    private boolean _callerNotified;

    public CSSParseException(String message, int line) {
        this(message, line, null);
    }

    public CSSParseException(String message, int line, @Nullable Throwable cause) {
        super(message, cause);
        _found = null;
        _expected = null;
        _line = line;
        _genericMessage = message;
    }

    public CSSParseException(Token found, Token expected, int line) {
        _found = found;
        _expected = new Token[] { expected };
        _line = line;
        _genericMessage = null;
    }

    public CSSParseException(Token found, Token @Nullable [] expected, int line) {
        _found = found;
        _expected = expected == null ? new Token[]{} : expected.clone();
        _line = line;
        _genericMessage = null;
    }

    @Override
    public String getMessage() {
        if (_genericMessage != null) {
            return _genericMessage + " at line " + (_line+1) + ".";
        } else {
            String found = _found == null ? "end of file" : _found.getExternalName();
            return "Found " + found + " where " +
                descr(_expected) + " was expected at line " + (_line+1) + ".";
        }
    }

    private String descr(Token[] tokens) {
        if (tokens.length == 1) {
            return tokens[0].getExternalName();
        } else {
            StringBuilder result = new StringBuilder();
            if (tokens.length > 2) {
                result.append("one of ");
            }
            for (int i = 0; i < tokens.length; i++) {
                result.append(tokens[i].getExternalName());
                if (i < tokens.length - 2) {
                    result.append(", ");
                } else if (i == tokens.length - 2) {
                    if (tokens.length > 2) {
                        result.append(", or ");
                    } else {
                        result.append(" or ");
                    }
                }
            }
            return result.toString();
        }
    }

    public Token getFound() {
        return _found;
    }

    public int getLine() {
        return _line;
    }

    public void setLine(int i) {
        _line = i;
    }

    public boolean isEOF() {
        return _found == Token.TK_EOF;
    }

    public boolean isCallerNotified() {
        return _callerNotified;
    }

    public void setCallerNotified(boolean callerNotified) {
        _callerNotified = callerNotified;
    }
}
