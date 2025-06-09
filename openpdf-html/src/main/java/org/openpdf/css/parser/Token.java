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

import com.google.errorprone.annotations.CheckReturnValue;

import static org.openpdf.css.parser.Token.Type.ANGLE;
import static org.openpdf.css.parser.Token.Type.ASTERISK;
import static org.openpdf.css.parser.Token.Type.AT_RULE;
import static org.openpdf.css.parser.Token.Type.CDC;
import static org.openpdf.css.parser.Token.Type.CDO;
import static org.openpdf.css.parser.Token.Type.CHARSET_SYM;
import static org.openpdf.css.parser.Token.Type.CM;
import static org.openpdf.css.parser.Token.Type.COLON;
import static org.openpdf.css.parser.Token.Type.COMMA;
import static org.openpdf.css.parser.Token.Type.DASHMATCH;
import static org.openpdf.css.parser.Token.Type.DIMENSION;
import static org.openpdf.css.parser.Token.Type.EMS;
import static org.openpdf.css.parser.Token.Type.EOF;
import static org.openpdf.css.parser.Token.Type.EQUALS;
import static org.openpdf.css.parser.Token.Type.EXS;
import static org.openpdf.css.parser.Token.Type.FONT_FACE_SYM;
import static org.openpdf.css.parser.Token.Type.FREQ;
import static org.openpdf.css.parser.Token.Type.FUNCTION;
import static org.openpdf.css.parser.Token.Type.GREATER;
import static org.openpdf.css.parser.Token.Type.HASH;
import static org.openpdf.css.parser.Token.Type.IDENT;
import static org.openpdf.css.parser.Token.Type.IMPORTANT_SYM;
import static org.openpdf.css.parser.Token.Type.IMPORT_SYM;
import static org.openpdf.css.parser.Token.Type.IN;
import static org.openpdf.css.parser.Token.Type.INCLUDES;
import static org.openpdf.css.parser.Token.Type.INVALID;
import static org.openpdf.css.parser.Token.Type.LBRACE;
import static org.openpdf.css.parser.Token.Type.LBRACKET;
import static org.openpdf.css.parser.Token.Type.MEDIA_SYM;
import static org.openpdf.css.parser.Token.Type.MINUS;
import static org.openpdf.css.parser.Token.Type.MM;
import static org.openpdf.css.parser.Token.Type.NAMESPACE_SYM;
import static org.openpdf.css.parser.Token.Type.NUMBER;
import static org.openpdf.css.parser.Token.Type.OTHER;
import static org.openpdf.css.parser.Token.Type.PAGE_SYM;
import static org.openpdf.css.parser.Token.Type.PC;
import static org.openpdf.css.parser.Token.Type.PERCENTAGE;
import static org.openpdf.css.parser.Token.Type.PERIOD;
import static org.openpdf.css.parser.Token.Type.PLUS;
import static org.openpdf.css.parser.Token.Type.PREFIXMATCH;
import static org.openpdf.css.parser.Token.Type.PT;
import static org.openpdf.css.parser.Token.Type.PX;
import static org.openpdf.css.parser.Token.Type.RBRACE;
import static org.openpdf.css.parser.Token.Type.RBRACKET;
import static org.openpdf.css.parser.Token.Type.RPAREN;
import static org.openpdf.css.parser.Token.Type.S;
import static org.openpdf.css.parser.Token.Type.SEMICOLON;
import static org.openpdf.css.parser.Token.Type.STRING;
import static org.openpdf.css.parser.Token.Type.SUBSTRINGMATCH;
import static org.openpdf.css.parser.Token.Type.SUFFIXMATCH;
import static org.openpdf.css.parser.Token.Type.TIME;
import static org.openpdf.css.parser.Token.Type.URI;
import static org.openpdf.css.parser.Token.Type.VERTICAL_BAR;
import static org.openpdf.css.parser.Token.Type.VIRGULE;

@SuppressWarnings("SpellCheckingInspection")
public class Token {
    public enum Type {
        S,
        CDO,
        CDC,
        INCLUDES,
        DASHMATCH,
        PREFIXMATCH,
        SUFFIXMATCH,
        SUBSTRINGMATCH,
        LBRACE,
        PLUS,
        GREATER,
        COMMA,
        STRING,
        INVALID,
        IDENT,
        HASH,
        IMPORT_SYM,
        PAGE_SYM,
        MEDIA_SYM,
        CHARSET_SYM,
        NAMESPACE_SYM,
        FONT_FACE_SYM,
        AT_RULE,
        IMPORTANT_SYM,
        EMS,
        EXS,
        PX,
        CM,
        MM,
        IN,
        PT,
        PC,
        ANGLE,
        TIME,
        FREQ,
        DIMENSION,
        PERCENTAGE,
        NUMBER,
        URI,
        FUNCTION,
        OTHER,
        RBRACE,
        SEMICOLON,
        VIRGULE,
        COLON,
        MINUS,
        RPAREN,
        LBRACKET,
        RBRACKET,
        PERIOD,
        EQUALS,
        ASTERISK,
        VERTICAL_BAR,
        EOF
    }

    public static final Token TK_S = new Token(S, "S", "whitespace");
    public static final Token TK_CDO = new Token(CDO, "CDO", "<!--");
    public static final Token TK_CDC = new Token(CDC, "CDC", "-->");
    public static final Token TK_INCLUDES = new Token(INCLUDES, "INCLUDES", "an attribute word match");
    public static final Token TK_DASHMATCH = new Token(DASHMATCH, "DASHMATCH", "an attribute hyphen match");
    public static final Token TK_PREFIXMATCH = new Token(PREFIXMATCH, "PREFIXMATCH", "an attribute prefix match");
    public static final Token TK_SUFFIXMATCH = new Token(SUFFIXMATCH, "SUFFIXMATCH", "an attribute suffix match");
    public static final Token TK_SUBSTRINGMATCH = new Token(SUBSTRINGMATCH, "SUBSTRINGMATCH", "an attribute substring match");
    public static final Token TK_LBRACE = new Token(LBRACE, "LBRACE", "a {");
    public static final Token TK_PLUS = new Token(PLUS, "PLUS", "a +");
    public static final Token TK_GREATER = new Token(GREATER, "GREATER", "a >");
    public static final Token TK_COMMA = new Token(COMMA, "COMMA", "a comma");
    public static final Token TK_STRING = new Token(STRING, "STRING", "a string");
    public static final Token TK_INVALID = new Token(INVALID, "INVALID", "an unclosed string");
    public static final Token TK_IDENT = new Token(IDENT, "IDENT", "an identifier");
    public static final Token TK_HASH = new Token(HASH, "HASH", "a hex color");
    public static final Token TK_IMPORT_SYM = new Token(IMPORT_SYM, "IMPORT_SYM", "@import");
    public static final Token TK_PAGE_SYM = new Token(PAGE_SYM, "PAGE_SYM", "@page");
    public static final Token TK_MEDIA_SYM = new Token(MEDIA_SYM, "MEDIA_SYM", "@media");
    public static final Token TK_CHARSET_SYM = new Token(CHARSET_SYM, "CHARSET_SYM", "@charset");
    public static final Token TK_NAMESPACE_SYM = new Token(NAMESPACE_SYM, "NAMESPACE_SYM", "@namespace,");
    public static final Token TK_FONT_FACE_SYM = new Token(FONT_FACE_SYM, "FONT_FACE_SYM", "@font-face");
    public static final Token TK_AT_RULE = new Token(AT_RULE, "AT_RULE", "at rule");
    public static final Token TK_IMPORTANT_SYM = new Token(IMPORTANT_SYM, "IMPORTANT_SYM", "!important");
    public static final Token TK_EMS = new Token(EMS, "EMS", "an em value");
    public static final Token TK_EXS = new Token(EXS, "EXS", "an ex value");
    public static final Token TK_PX = new Token(PX, "PX", "a pixel value");
    public static final Token TK_CM = new Token(CM, "CM", "a centimeter value");
    public static final Token TK_MM = new Token(MM, "MM", "a millimeter value");
    public static final Token TK_IN = new Token(IN, "IN", "an inch value");
    public static final Token TK_PT = new Token(PT, "PT", "a point value");
    public static final Token TK_PC = new Token(PC, "PC", "a pica value");
    public static final Token TK_ANGLE = new Token(ANGLE, "ANGLE", "an angle value");
    public static final Token TK_TIME = new Token(TIME, "TIME", "a time value");
    public static final Token TK_FREQ = new Token(FREQ, "FREQ", "a freq value");
    public static final Token TK_DIMENSION = new Token(DIMENSION, "DIMENSION", "a dimension");
    public static final Token TK_PERCENTAGE = new Token(PERCENTAGE, "PERCENTAGE", "a percentage");
    public static final Token TK_NUMBER = new Token(NUMBER, "NUMBER", "a number");
    public static final Token TK_URI = new Token(URI, "URI", "a URI");
    public static final Token TK_FUNCTION = new Token(FUNCTION, "FUNCTION", "function");
    public static final Token TK_OTHER = new Token(OTHER, "OTHER", "other");
    public static final Token TK_RBRACE = new Token(RBRACE, "RBRACE", "}");
    public static final Token TK_SEMICOLON = new Token(SEMICOLON, "SEMICOLON", ";");
    public static final Token TK_VIRGULE = new Token(VIRGULE, "VIRGULE", "/");
    public static final Token TK_COLON = new Token(COLON, "COLON", ":");
    public static final Token TK_MINUS = new Token(MINUS, "MINUS", "-");
    public static final Token TK_RPAREN = new Token(RPAREN, "RPAREN", ")");
    public static final Token TK_LBRACKET = new Token(LBRACKET, "LBRACKET", "[");
    public static final Token TK_RBRACKET = new Token(RBRACKET, "RBRACKET", "]");
    public static final Token TK_PERIOD = new Token(PERIOD, "PERIOD", ".");
    public static final Token TK_EQUALS = new Token(EQUALS, "EQUALS", "=");
    public static final Token TK_ASTERISK = new Token(ASTERISK, "ASTERISK", "*");
    public static final Token TK_VERTICAL_BAR = new Token(VERTICAL_BAR, "VERTICAL_BAR", "|");
    public static final Token TK_EOF = new Token(EOF, "EOF", "end of file");


    private final Type _type;
    private final String _name;
    private final String _externalName;

    private Token(Type type, String name, String externalName) {
        _type = type;
        _name = name;
        _externalName = externalName;
    }

    @CheckReturnValue
    public Type getType() {
        return _type;
    }

    public String getName() {
        return _name;
    }

    public String getExternalName() {
        return _externalName;
    }

    @Override
    public String toString() {
        return _name;
    }

    public static Token createOtherToken(String value) {
        return new Token(OTHER, "OTHER", value + " (other)");
    }
}
