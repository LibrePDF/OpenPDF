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
import org.jspecify.annotations.Nullable;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.openpdf.css.constants.CSSName;
import org.openpdf.css.constants.MarginBoxName;
import org.openpdf.css.extend.TreeResolver;
import org.openpdf.css.newmatch.Selector;
import org.openpdf.css.newmatch.Selector.Axis;
import org.openpdf.css.parser.property.PropertyBuilder;
import org.openpdf.css.sheet.FontFaceRule;
import org.openpdf.css.sheet.MediaRule;
import org.openpdf.css.sheet.PageRule;
import org.openpdf.css.sheet.PropertyDeclaration;
import org.openpdf.css.sheet.Ruleset;
import org.openpdf.css.sheet.RulesetContainer;
import org.openpdf.css.sheet.Stylesheet;
import org.openpdf.css.sheet.StylesheetInfo;
import org.openpdf.css.sheet.StylesheetInfo.Origin;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyMap;
import static java.util.Locale.ROOT;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_NUMBER;
import static org.w3c.dom.css.CSSPrimitiveValue.CSS_PERCENTAGE;
import static org.openpdf.css.newmatch.Selector.ACTIVE_PSEUDOCLASS;
import static org.openpdf.css.newmatch.Selector.Axis.CHILD_AXIS;
import static org.openpdf.css.newmatch.Selector.Axis.DESCENDANT_AXIS;
import static org.openpdf.css.newmatch.Selector.Axis.IMMEDIATE_SIBLING_AXIS;
import static org.openpdf.css.newmatch.Selector.FOCUS_PSEUDOCLASS;
import static org.openpdf.css.newmatch.Selector.HOVER_PSEUDOCLASS;
import static org.openpdf.css.newmatch.Selector.VISITED_PSEUDOCLASS;
import static org.openpdf.css.parser.Token.Type.AT_RULE;

@SuppressWarnings("MissingCasesInEnumSwitch")
public class CSSParser {

    @Nullable
    private Token _saved;
    private final Lexer _lexer;
    private final CSSErrorHandler _errorHandler;
    @Nullable
    private String _uri;

    private final Map<String, String> _namespaces = new HashMap<>();
    private boolean _supportCMYKColors;

    public CSSParser(CSSErrorHandler errorHandler) {
        _lexer = new Lexer(new StringReader(""));
        _errorHandler = errorHandler;
    }

    @CheckReturnValue
    public Stylesheet parseStylesheet(@Nullable String uri, Origin origin, Reader reader) throws IOException {
        _uri = uri;
        reset(reader);

        Stylesheet result = new Stylesheet(uri, origin);
        stylesheet(result);

        return result;
    }

    public Ruleset parseDeclaration(Origin origin, String text) {
        try {
            // XXX Set this to something more reasonable
            _uri = "style attribute";
            reset(new StringReader(text));

            skip_whitespace();

            Ruleset result = new Ruleset(origin);

            try {
                declaration_list(result, true, false, false);
            } catch (CSSParseException e) {
                // ignore, already handled
            }

            return result;
        } catch (IOException e) {
            // "Shouldn't" happen
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    @Nullable
    public PropertyValue parsePropertyValue(CSSName cssName, Origin origin, String expr) {
        _uri = cssName + " property value";
        try {
            reset(new StringReader(expr));
            List<PropertyValue> values = expr(
                    cssName.equals(CSSName.FONT_FAMILY) ||
                    cssName.equals(CSSName.FONT_SHORTHAND) ||
                    cssName.equals(CSSName.FS_PDF_FONT_ENCODING));

            PropertyBuilder builder = CSSName.getPropertyBuilder(cssName);
            List<PropertyDeclaration> props;
            try {
                props = builder.buildDeclarations(cssName, values, origin, false);
            } catch (CSSParseException e) {
                e.setLine(getCurrentLine());
                throw e;
            }

            if (props.size() != 1) {
                throw new CSSParseException(
                        "Builder created " + props.size() + "properties, expected 1", getCurrentLine());
            }

            return (PropertyValue) props.get(0).getValue();
        } catch (IOException e) {
            // "Shouldn't" happen
            throw new RuntimeException(e.getMessage(), e);
        } catch (CSSParseException e) {
            error(e, "property value", false);
            return null;
        }
    }

//    stylesheet
//    : [ CHARSET_SYM S* STRING S* ';' ]?
//      [S|CDO|CDC]* [ import [S|CDO|CDC]* ]*
//      [ namespace [S|CDO|CDC]* ]*
//      [ [ ruleset | media | page | font_face ] [S|CDO|CDC]* ]*
    private void stylesheet(Stylesheet stylesheet) throws IOException {
        Token t = la();
        try {
            if (t == Token.TK_CHARSET_SYM) {
                try {
                    t = next();
                    skip_whitespace();
                    t = next();
                    if (t == Token.TK_STRING) {
                        /* String charset = getTokenValue(t); */

                        skip_whitespace();
                        t = next();
                        if (t != Token.TK_SEMICOLON) {
                            push(t);
                            throw new CSSParseException(t, Token.TK_SEMICOLON, getCurrentLine());
                        }

                        // Do something
                    } else {
                        push(t);
                        throw new CSSParseException(t, Token.TK_STRING, getCurrentLine());
                    }
                } catch (CSSParseException e) {
                    error(e, "@charset rule", true);
                    recover(false, false);
                }
            }
            skip_whitespace_and_cdocdc();
            while (true) {
                t = la();
                if (t == Token.TK_IMPORT_SYM) {
                    import_rule(stylesheet);
                    skip_whitespace_and_cdocdc();
                } else {
                    break;
                }
            }
            while (true) {
                t = la();
                if (t == Token.TK_NAMESPACE_SYM) {
                    namespace();
                    skip_whitespace_and_cdocdc();
                } else {
                    break;
                }
            }
            while (true) {
                t = la();
                if (t == Token.TK_EOF) {
                    break;
                }
                switch (t.getType()) {
                    case PAGE_SYM -> page(stylesheet);
                    case MEDIA_SYM -> media(stylesheet);
                    case FONT_FACE_SYM -> font_face(stylesheet);
                    case IMPORT_SYM -> {
                        next();
                        error(new CSSParseException("@import not allowed here", getCurrentLine()),
                                "@import rule", true);
                        recover(false, false);
                    }
                    case NAMESPACE_SYM -> {
                        next();
                        error(new CSSParseException("@namespace not allowed here", getCurrentLine()),
                                "@namespace rule", true);
                        recover(false, false);
                    }
                    case AT_RULE -> {
                        next();
                        error(new CSSParseException(
                                "Invalid at-rule", getCurrentLine()), "at-rule", true);
                        recover(false, false);
                        ruleset(stylesheet);
                    }
                    default ->
                        ruleset(stylesheet);
                }
                skip_whitespace_and_cdocdc();
            }
        } catch (CSSParseException e) {
            // "shouldn't" happen
            if (! e.isCallerNotified()) {
                error(e, "stylesheet", false);
            }
        }
    }

//  import
//  : IMPORT_SYM S*
//    [STRING|URI] S* [ medium [ COMMA S* medium]* ]? ';' S*
//  ;
    private void import_rule(Stylesheet stylesheet) throws IOException {
        try {
            Token t = next();
            if (t == Token.TK_IMPORT_SYM) {
                String uri;
                List<String> mediaTypes = new ArrayList<>(1);

                skip_whitespace();
                t = next();
                switch (t.getType()) {
                    case STRING:
                    case URI:
                        // first see if we can set URI via URL
                        try {
                            uri = new URL(new URL(stylesheet.getURI()), getTokenValue(t)).toString();
                        } catch (MalformedURLException mue) {
                            // not a valid URL, may be a custom protocol which the user expects to handle
                            // in the user agent
                            //
                            // FIXME: using URI like this will not work for some cases of parent URI, depends
                            // on whether the URI class can parse the parent and child correctly
                            // This can lead to a bug where a stylesheet imported from another stylesheet ends
                            // up unresolved. This will be fixed in a later release by passing Stylesheet info
                            // all the way down to the UAC so that the end user can code for it
                            try {
                                URI parent = new URI(stylesheet.getURI());
                                String tokenValue = getTokenValue(t);
                                String resolvedUri = parent.resolve(tokenValue).toString();
                                uri = resolvedUri;
                            } catch (URISyntaxException use) {
                                throw new CSSParseException("Invalid URL, " + use.getMessage(), getCurrentLine(), use);
                            }

                        }
                        skip_whitespace();
                        t = la();
                        if (t == Token.TK_IDENT) {
                            mediaTypes.add(medium());
                            while (true) {
                                t = la();
                                if (t == Token.TK_COMMA) {
                                    next();
                                    skip_whitespace();
                                    t = la();
                                    if (t == Token.TK_IDENT) {
                                        mediaTypes.add(medium());
                                    } else {
                                        throw new CSSParseException(
                                                t, Token.TK_IDENT, getCurrentLine());
                                    }
                                } else {
                                    break;
                                }
                            }
                        }
                        t = next();
                        if (t == Token.TK_SEMICOLON) {
                            skip_whitespace();
                        } else {
                            push(t);
                            throw new CSSParseException(
                                    t, Token.TK_SEMICOLON, getCurrentLine());
                        }
                        break;
                    default:
                        push(t);
                        throw new CSSParseException(
                            t, new Token[] { Token.TK_STRING, Token.TK_URI }, getCurrentLine());
                }

                if (mediaTypes.isEmpty()) {
                    mediaTypes.add("all");
                }
                StylesheetInfo info = new StylesheetInfo(stylesheet.getOrigin(), uri, mediaTypes, null);
                stylesheet.addImportRule(info);
            } else {
                push(t);
                throw new CSSParseException(t, Token.TK_IMPORT_SYM, getCurrentLine());
            }
        } catch (CSSParseException e) {
            error(e, "@import rule", true);
            recover(false, false);
        }
    }

//  namespace
//  : NAMESPACE_SYM S* [namespace_prefix S*]? [STRING|URI] S* ';' S*
//  ;
//  namespace_prefix
//  : IDENT
//  ;
    private void namespace() throws IOException {
        try {
            Token t = next();
            if (t == Token.TK_NAMESPACE_SYM) {
                skip_whitespace();
                t = next();

                String prefix = null;
                if (t == Token.TK_IDENT) {
                    prefix = getTokenValue(t);
                    skip_whitespace();
                    t = next();
                }

                String url;
                if (t == Token.TK_STRING || t == Token.TK_URI) {
                    url = getTokenValue(t);
                } else {
                    throw new CSSParseException(
                            t, new Token[] { Token.TK_STRING, Token.TK_URI }, getCurrentLine());
                }

                skip_whitespace();

                t = next();
                if (t == Token.TK_SEMICOLON) {
                    skip_whitespace();

                    _namespaces.put(prefix, url);
                } else {
                    throw new CSSParseException(
                            t, Token.TK_SEMICOLON, getCurrentLine());
                }
            } else {
                throw new CSSParseException(t, Token.TK_NAMESPACE_SYM, getCurrentLine());
            }
        } catch (CSSParseException e) {
            error(e, "@namespace rule", true);
            recover(false, false);
        }
    }

//  media
//  : MEDIA_SYM S* medium [ COMMA S* medium ]* LBRACE S* ruleset* '}' S*
//  ;
    private void media(Stylesheet stylesheet) throws IOException {
        Token t = next();
        try {
            if (t == Token.TK_MEDIA_SYM) {
                MediaRule mediaRule = new MediaRule(stylesheet.getOrigin());
                skip_whitespace();
                t = la();
                if (t == Token.TK_IDENT) {
                    mediaRule.addMedium(medium());
                    while (true) {
                        t = la();
                        if (t == Token.TK_COMMA) {
                            next();
                            skip_whitespace();
                            t = la();
                            if (t == Token.TK_IDENT) {
                                mediaRule.addMedium(medium());
                            } else {
                                throw new CSSParseException(t, Token.TK_IDENT, getCurrentLine());
                            }
                        } else {
                            break;
                        }
                    }
                    t = next();
                    if (t == Token.TK_LBRACE) {
                        skip_whitespace();
                        while (true) {
                            t = la();
                            if (t == null) {
                                break;
                            }
                            if (t.getType() == Token.Type.RBRACE) {
                                next();
                                break;
                            }
                            ruleset(mediaRule);
                        }
                        skip_whitespace();
                    } else {
                        push(t);
                        throw new CSSParseException(t, Token.TK_LBRACE, getCurrentLine());
                    }
                } else {
                    throw new CSSParseException(t, Token.TK_IDENT, getCurrentLine());
                }

                stylesheet.addContent(mediaRule);
            } else {
                push(t);
                throw new CSSParseException(t, Token.TK_MEDIA_SYM, getCurrentLine());
            }
        } catch (CSSParseException e) {
            error(e, "@media rule", true);
            recover(false, false);
        }
    }

//  medium
//  : IDENT S*
//  ;
    private String medium() throws IOException {
        Token t = next();
        if (t == Token.TK_IDENT) {
            String result = getTokenValue(t);
            skip_whitespace();
            return result;
        } else {
            push(t);
            throw new CSSParseException(t, Token.TK_IDENT, getCurrentLine());
        }
    }

//  font_face
//    : FONT_FACE_SYM S*
//      '{' S* declaration [ ';' S* declaration ]* '}' S*
//    ;
    private void font_face(Stylesheet stylesheet) throws IOException {
        Token t = next();
        try {
            FontFaceRule fontFaceRule = new FontFaceRule(stylesheet.getOrigin());
            if (t == Token.TK_FONT_FACE_SYM) {
                skip_whitespace();

                Ruleset ruleset = new Ruleset(stylesheet.getOrigin());

                skip_whitespace();
                t = next();
                if (t == Token.TK_LBRACE) {
                    // Prevent runaway threads with a max loop/counter
                    int maxLoops = 1024 * 1024; // 1M is too much, 1K is probably too...
                    int i = 0;
                    while (true) {
                        if (++i >= maxLoops)
                            throw new CSSParseException(t, Token.TK_RBRACE, getCurrentLine());
                        skip_whitespace();
                        t = la();
                        if (t == Token.TK_RBRACE) {
                            next();
                            skip_whitespace();
                            break;
                        } else {
                            declaration_list(ruleset, false, true, true);
                        }
                    }
                } else {
                    push(t);
                    throw new CSSParseException(t, Token.TK_LBRACE, getCurrentLine());
                }

                fontFaceRule.addContent(ruleset);
                stylesheet.addFontFaceRule(fontFaceRule);
            } else {
                push(t);
                throw new CSSParseException(t, Token.TK_FONT_FACE_SYM, getCurrentLine());
            }
        } catch (CSSParseException e) {
            error(e, "@font-face rule", true);
            recover(false, false);
        }
    }

//  page :
//    PAGE_SYM S* IDENT? pseudo_page? S*
//    '{' S* [ declaration | margin ]? [ ';' S* [ declaration | margin ]? ]* '}' S*
//
    private void page(Stylesheet stylesheet) throws IOException {
        Token t = next();
        try {
            if (t == Token.TK_PAGE_SYM) {
                String pageName = null;
                String pseudoPage = null;
                Map<MarginBoxName, List<PropertyDeclaration>> margins = new HashMap<>();

                skip_whitespace();
                t = la();
                if (t == Token.TK_IDENT) {
                    pageName = getTokenValue(t);
                    if (pageName.equals("auto")) {
                        throw new CSSParseException("page name may not be auto", getCurrentLine());
                    }
                    next();
                    t = la();
                }
                if (t == Token.TK_COLON) {
                    pseudoPage = pseudo_page();
                }
                Ruleset ruleset = new Ruleset(stylesheet.getOrigin());

                skip_whitespace();
                t = next();
                if (t == Token.TK_LBRACE) {
                    while (true) {
                        skip_whitespace();
                        t = la();
                        if (t == Token.TK_RBRACE) {
                            next();
                            skip_whitespace();
                            break;
                        } else if (t == Token.TK_AT_RULE) {
                            margins.putAll(margin(stylesheet));
                        } else {
                            declaration_list(ruleset, false, true, false);
                        }
                    }
                } else {
                    push(t);
                    throw new CSSParseException(t, Token.TK_LBRACE, getCurrentLine());
                }

                PageRule pageRule = new PageRule(stylesheet.getOrigin(), pageName, pseudoPage, margins, ruleset);
                stylesheet.addContent(pageRule);
            } else {
                push(t);
                throw new CSSParseException(t, Token.TK_PAGE_SYM, getCurrentLine());
            }
        } catch (CSSParseException e) {
            error(e, "@page rule", true);
            recover(false, false);
        }
    }

//  margin :
//    margin_sym S* '{' declaration [ ';' S* declaration? ]* '}' S*
//    ;
    private Map<MarginBoxName, List<PropertyDeclaration>> margin(Stylesheet stylesheet) throws IOException {
        Token t = next();
        if (t != Token.TK_AT_RULE) {
            error(new CSSParseException(t, Token.TK_AT_RULE, getCurrentLine()), "at rule", true);
            recover(true, false);
            return emptyMap();
        }

        String name = getTokenValue(t);
        MarginBoxName marginBoxName = MarginBoxName.valueOf(name);
        if (marginBoxName == null) {
            error(new CSSParseException(name + " is not a valid margin box name", getCurrentLine()), "at rule", true);
            recover(true, false);
            return emptyMap();
        }

        skip_whitespace();
        try {
            t = next();
            if (t == Token.TK_LBRACE) {
                skip_whitespace();
                Ruleset ruleset = new Ruleset(stylesheet.getOrigin());
                declaration_list(ruleset, false, false, false);
                t = next();
                if (t != Token.TK_RBRACE) {
                    push(t);
                    throw new CSSParseException(t, Token.TK_RBRACE, getCurrentLine());
                }
                return Map.of(marginBoxName, ruleset.getPropertyDeclarations());
            } else {
                push(t);
                throw new CSSParseException(t, Token.TK_LBRACE, getCurrentLine());
            }
        } catch (CSSParseException e) {
            error(e, "margin box", true);
            recover(false, false);
        }
        return emptyMap();
    }


//  pseudo_page
//    : ':' IDENT
//    ;
    private String pseudo_page() throws IOException {
        Token t = next();
        if (t == Token.TK_COLON) {
            t = next();
            if (t == Token.TK_IDENT) {
                String result = getTokenValue(t);
                if (! (result.equals("first") || result.equals("left") || result.equals("right"))) {
                    throw new CSSParseException("Pseudo page must be one of first, left, or right", getCurrentLine());
                }
                return result;
            } else {
                push(t);
                throw new CSSParseException(t, Token.TK_IDENT, getCurrentLine());
            }
        } else {
            push(t);
            throw new CSSParseException(t, Token.TK_COLON, getCurrentLine());
        }
    }
//  operator
//    : '/' S* | COMMA S* | /* empty */
//    ;
    private void operator() throws IOException {
        Token t = la();
        switch (t.getType()) {
            case VIRGULE:
            case COMMA:
                next();
                skip_whitespace();
                break;
        }
    }

//  combinator
//    : PLUS S*
//    | GREATER S*
//    | S
//    ;
    private Token combinator() throws IOException {
        Token t = next();
        if (t == Token.TK_PLUS || t == Token.TK_GREATER) {
            skip_whitespace();
        } else if (t != Token.TK_S) {
            push(t);
            throw new CSSParseException(
                    t,
                    new Token[] { Token.TK_PLUS, Token.TK_GREATER, Token.TK_S },
                    getCurrentLine());
        }
        return t;
    }

//  unary_operator
//    : '-' | PLUS
//    ;
    private int unary_operator() throws IOException {
        Token t = next();
        if (! (t == Token.TK_MINUS || t == Token.TK_PLUS)) {
            push(t);
            throw new CSSParseException(
                    t, new Token[] { Token.TK_MINUS, Token.TK_PLUS}, getCurrentLine());
        }
        if (t == Token.TK_MINUS) {
            return -1;
        } else { /* t == Token.TK_PLUS */
            return 1;
        }
    }

//  property
//    : IDENT S*
//    ;
    private String property() throws IOException {
        Token t = next();
        String result;
        if (t == Token.TK_IDENT) {
            result = getTokenValue(t);
            skip_whitespace();
        } else {
            push(t);
            throw new CSSParseException(
                    t, Token.TK_IDENT, getCurrentLine());
        }

        return result;
    }

//  declaration_list
//    : [ declaration ';' S* ]*
    private void declaration_list(
            Ruleset ruleset, boolean expectEOF, boolean expectAtRule, boolean inFontFace) throws IOException {
        Token t;
        LOOP:
        while (true) {
            t = la();
            switch (t.getType()) {
                case SEMICOLON -> {
                    next();
                    skip_whitespace();
                }
                case RBRACE -> {
                    break LOOP;
                }
                case AT_RULE -> {
                    if (expectAtRule) {
                        break LOOP;
                    } else {
                        declaration(ruleset, inFontFace);
                    }
                }
                case EOF -> {
                    if (expectEOF) {
                        break LOOP;
                    }
                    declaration(ruleset, inFontFace);
                }
                default ->
                    declaration(ruleset, inFontFace);
            }
        }
    }

//  ruleset
//    : selector [ COMMA S* selector ]*
//      LBRACE S* [ declaration ';' S* ]* '}' S*
//    ;
    private void ruleset(RulesetContainer container) throws IOException {
        try {
            Ruleset ruleset = new Ruleset(container.getOrigin());

            selector(ruleset);
            Token t;
            while (true) {
                t = la();
                if (t == Token.TK_COMMA) {
                    next();
                    skip_whitespace();
                    selector(ruleset);
                } else {
                    break;
                }
            }
            t = next();
            if (t == Token.TK_LBRACE) {
                skip_whitespace();
                declaration_list(ruleset, false, false, false);
                t = next();
                if (t == Token.TK_RBRACE) {
                    skip_whitespace();
                } else {
                    push(t);
                    throw new CSSParseException(t, Token.TK_RBRACE, getCurrentLine());
                }
            } else {
                push(t);
                throw new CSSParseException(
                        t, new Token[] { Token.TK_COMMA, Token.TK_LBRACE }, getCurrentLine());
            }

            if (!ruleset.getPropertyDeclarations().isEmpty()) {
                container.addContent(ruleset);
            }
        } catch (CSSParseException e) {
            error(e, "ruleset", true);
            recover(true, false);
        }
    }

//  selector
//    : simple_selector [ combinator simple_selector ]*
//    ;
    private void selector(Ruleset ruleset) throws IOException {
        List<Selector> selectors = new ArrayList<>();
        List<Token> combinators = new ArrayList<>();
        selectors.add(simple_selector(ruleset));
        LOOP:
        while (true) {
            Token t = la();
            switch (t.getType()) {
                case PLUS, GREATER, S -> {
                    combinators.add(combinator());
                    t = la();
                    switch (t.getType()) {
                        case IDENT, ASTERISK, HASH, PERIOD, LBRACKET, COLON -> selectors.add(simple_selector(ruleset));
                        default -> throw new CSSParseException(t, new Token[]{Token.TK_IDENT,
                                Token.TK_ASTERISK, Token.TK_HASH, Token.TK_PERIOD,
                                Token.TK_LBRACKET, Token.TK_COLON}, getCurrentLine());
                    }
                }
                default -> {
                    break LOOP;
                }
            }
        }
        ruleset.addFSSelector(mergeSimpleSelectors(selectors, combinators));
    }

    @Nullable
    @CheckReturnValue
    private Selector mergeSimpleSelectors(List<Selector> selectors, List<Token> combinators) {
        int count = selectors.size();
        if (count == 1) {
            return selectors.get(0);
        }

        Axis lastDescendantOrChildAxis = DESCENDANT_AXIS;
        Selector result = null;
        for (int i = 0; i < count - 1; i++) {
            Selector first = selectors.get(i);
            Selector second = selectors.get(i+1);
            Token combinator = combinators.get(i);

            if (first.getPseudoElement() != null) {
                throw new CSSParseException(
                        "A simple selector with a pseudo element cannot be " +
                        "combined with another simple selector", getCurrentLine());
            }

            boolean sibling = false;
            if (combinator == Token.TK_S) {
                second.setAxis(DESCENDANT_AXIS);
                lastDescendantOrChildAxis = DESCENDANT_AXIS;
            } else if (combinator == Token.TK_GREATER) {
                second.setAxis(CHILD_AXIS);
                lastDescendantOrChildAxis = CHILD_AXIS;
            } else if (combinator == Token.TK_PLUS) {
                first.setAxis(IMMEDIATE_SIBLING_AXIS);
                sibling = true;
            }

            second.setSpecificityB(second.getSpecificityB() + first.getSpecificityB());
            second.setSpecificityC(second.getSpecificityC() + first.getSpecificityC());
            second.setSpecificityD(second.getSpecificityD() + first.getSpecificityD());

            if (! sibling) {
                if (result == null) {
                    result = first;
                }
                first.setChainedSelector(second);
            } else {
                second.setSiblingSelector(first);
                if (result == null || result == first) {
                    result = second;
                }
                if (i > 0) {
                    for (int j = i-1; j >= 0; j--) {
                        Selector selector = selectors.get(j);
                        if (selector.getChainedSelector() == first) {
                            selector.setChainedSelector(second);
                            second.setAxis(lastDescendantOrChildAxis);
                            break;
                        }
                    }
                }
            }
        }

        return result;
    }

//  simple_selector
//    : typed_value [ HASH | class | attrib | pseudo ]*
//    | [ HASH | class | attrib | pseudo ]+
//    ;
    private Selector simple_selector(Ruleset ruleset) throws IOException {
        Selector selector = new Selector();
        selector.setParent(ruleset);
        Token t = la();
        switch (t.getType()) {
            case ASTERISK,
                 IDENT,
                 VERTICAL_BAR -> {
                NamespacePair pair = typed_value(false);
                selector.setNamespaceURI(pair.getNamespaceURI());
                selector.setName(pair.getName());

                LOOP: while (true) {
                    t = la();
                    switch (t.getType()) {
                        case HASH -> {
                            t = next();
                            selector.addIDCondition(getTokenValue(t, true));
                        }
                        case PERIOD -> class_selector(selector);
                        case LBRACKET -> attrib(selector);
                        case COLON -> pseudo(selector);
                        default -> {
                            break LOOP;
                        }
                    }
                }
            }
            default -> {
                boolean found = false;
                LOOP:
                while (true) {
                    t = la();
                    switch (t.getType()) {
                        case HASH -> {
                            t = next();
                            selector.addIDCondition(getTokenValue(t, true));
                            found = true;
                        }
                        case PERIOD -> {
                            class_selector(selector);
                            found = true;
                        }
                        case LBRACKET -> {
                            attrib(selector);
                            found = true;
                        }
                        case COLON -> {
                            pseudo(selector);
                            found = true;
                        }
                        default -> {
                            if (!found) {
                                throw new CSSParseException(t, new Token[]{Token.TK_HASH,
                                        Token.TK_PERIOD, Token.TK_LBRACKET, Token.TK_COLON},
                                        getCurrentLine());
                            }
                            break LOOP;
                        }
                    }
                }
            }
        }
        return selector;
    }

//    type_selector
//    : [ namespace_prefix ]? element_name | IDENT
//    ;
//    namespace_prefix
//    : [ IDENT | '*' ]? '|'
//    ;
    private NamespacePair typed_value(boolean matchAttribute) throws IOException {
        String prefix = null;
        String name = null;

        Token t = la();
        if (t == Token.TK_ASTERISK || t == Token.TK_IDENT) {
            next();
            if (t == Token.TK_IDENT) {
                name = getTokenValue(t, true);
            }
            t = la();
        } else if (t == Token.TK_VERTICAL_BAR) {
            prefix = TreeResolver.NO_NAMESPACE;
        } else {
            throw new CSSParseException(
                    t, new Token[] { Token.TK_ASTERISK, Token.TK_IDENT, Token.TK_VERTICAL_BAR },
                    getCurrentLine());
        }

        if (t == Token.TK_VERTICAL_BAR) {
            next();
            t = next();
            if (t == Token.TK_ASTERISK || t == Token.TK_IDENT) {
                if (prefix == null) {
                    prefix = name;
                }
                if (t == Token.TK_IDENT) {
                    name = getTokenValue(t, true);
                }
            } else {
                throw new CSSParseException(
                        t, new Token[] { Token.TK_ASTERISK, Token.TK_IDENT }, getCurrentLine());
            }
        }

        String namespaceURI = null;
        if (prefix != null && !prefix.equals(TreeResolver.NO_NAMESPACE)) {
            namespaceURI = _namespaces.get(prefix.toLowerCase(ROOT));
            if (namespaceURI == null) {
                throw new CSSParseException("There is no namespace with prefix " + prefix + " defined",
                        getCurrentLine());
            }
        } else if (prefix == null && ! matchAttribute) {
            namespaceURI = _namespaces.get(null);
        }

        if (matchAttribute && name == null) {
            throw new CSSParseException("An attribute name is required", getCurrentLine());
        }

        return new NamespacePair(namespaceURI, name);
    }

//  class
//    : '.' IDENT
//    ;
    private void class_selector(Selector selector) throws IOException {
        Token t = next();
        if (t == Token.TK_PERIOD) {
            t = next();
            if (t == Token.TK_IDENT) {
                selector.addClassCondition(getTokenValue(t, true));
            } else {
                push(t);
                throw new CSSParseException(t, Token.TK_IDENT, getCurrentLine());
            }
        } else {
            push(t);
            throw new CSSParseException(t, Token.TK_PERIOD, getCurrentLine());
        }
    }

//    attrib
//    : '[' S* [ namespace_prefix ]? IDENT S*
//          [ [ PREFIXMATCH |
//              SUFFIXMATCH |
//              SUBSTRINGMATCH |
//              '=' |
//              INCLUDES |
//              DASHMATCH ] S* [ IDENT | STRING ] S*
//          ]? ']'
//    ;
    private void attrib(Selector selector) throws IOException {
        Token t = next();
        if (t == Token.TK_LBRACKET) {
            skip_whitespace();
            t = la();
            if (t == Token.TK_IDENT || t == Token.TK_ASTERISK || t == Token.TK_VERTICAL_BAR) {
                boolean existenceMatch = true;
                NamespacePair pair = typed_value(true);
                String attrNamespaceURI = pair.getNamespaceURI();
                String attrName = pair.getName();
                skip_whitespace();
                t = la();
                switch (t.getType()) {
                    case EQUALS,
                         INCLUDES,
                         DASHMATCH,
                         PREFIXMATCH,
                         SUFFIXMATCH,
                         SUBSTRINGMATCH -> {
                        existenceMatch = false;
                        Token selectorType = next();
                        skip_whitespace();
                        t = next();
                        if (t == Token.TK_IDENT || t == Token.TK_STRING) {
                            String value = getTokenValue(t, true);
                            switch (selectorType.getType()) {
                                case EQUALS -> selector.addAttributeEqualsCondition(attrNamespaceURI, attrName, value);
                                case DASHMATCH -> selector.addAttributeMatchesFirstPartCondition(attrNamespaceURI, attrName, value);
                                case INCLUDES -> selector.addAttributeMatchesListCondition(attrNamespaceURI, attrName, value);
                                case PREFIXMATCH -> selector.addAttributePrefixCondition(attrNamespaceURI, attrName, value);
                                case SUFFIXMATCH -> selector.addAttributeSuffixCondition(attrNamespaceURI, attrName, value);
                                case SUBSTRINGMATCH -> selector.addAttributeSubstringCondition(attrNamespaceURI, attrName, value);
                            }
                            skip_whitespace();
                        } else {
                            push(t);
                            throw new CSSParseException(t,
                                    new Token[]{Token.TK_IDENT, Token.TK_STRING},
                                    getCurrentLine());
                        }
                        skip_whitespace();
                        t = la();
                    }
                }
                if (existenceMatch) {
                    selector.addAttributeExistsCondition(attrNamespaceURI, attrName);
                }
                if (t == Token.TK_RBRACKET) {
                    next();
                } else {
                    throw new CSSParseException(t, new Token[] { Token.TK_EQUALS,
                            Token.TK_INCLUDES, Token.TK_DASHMATCH, Token.TK_PREFIXMATCH,
                            Token.TK_SUFFIXMATCH, Token.TK_SUBSTRINGMATCH, Token.TK_RBRACKET },
                            getCurrentLine());
                }
            } else {
                throw new CSSParseException(
                        t, new Token[] { Token.TK_IDENT, Token.TK_ASTERISK }, getCurrentLine());
            }
        } else {
            push(t);
            throw new CSSParseException(t, Token.TK_LBRACKET, getCurrentLine());
        }
    }

    private void addPseudoClassOrElement(Token t, Selector selector) {
        String value = getTokenValue(t);
        switch (value) {
            case "link" ->
                selector.addLinkCondition();
            case "visited" ->
                selector.setPseudoClass(VISITED_PSEUDOCLASS);
            case "hover" ->
                selector.setPseudoClass(HOVER_PSEUDOCLASS);
            case "focus" ->
                selector.setPseudoClass(FOCUS_PSEUDOCLASS);
            case "active" ->
                selector.setPseudoClass(ACTIVE_PSEUDOCLASS);
            case "first-child" ->
                selector.addFirstChildCondition();
            case "even" ->
                selector.addEvenChildCondition();
            case "odd" ->
                selector.addOddChildCondition();
            case "last-child" ->
                selector.addLastChildCondition();
            case "first-line", "first-letter", "before", "after" ->
                selector.setPseudoElement(value);
            default ->
                throw new CSSParseException(value + " is not a recognized pseudo-class", getCurrentLine());
        }
    }

    private void addPseudoClassOrElementFunction(Token t, Selector selector) throws IOException {
        final String f0 = getTokenValue(t);
        final String f = f0.substring(0, f0.length() - 1);

        switch (f) {
            case "lang" -> {
                skip_whitespace();
                t = next();
                if (t == Token.TK_IDENT) {
                    String lang = getTokenValue(t);
                    selector.addLangCondition(lang);
                    skip_whitespace();
                    t = next();
                } else {
                    push(t);
                    throw new CSSParseException(t, Token.TK_IDENT, getCurrentLine());
                }
            }
            case "nth-child" -> {
                StringBuilder number = new StringBuilder();
                while ((t = next()) != null && (t == Token.TK_IDENT || t == Token.TK_S || t == Token.TK_NUMBER || t == Token.TK_DIMENSION || t == Token.TK_PLUS || t == Token.TK_MINUS)) {
                    number.append(getTokenValue(t));
                }

                try {
                    selector.addNthChildCondition(number.toString());
                } catch (CSSParseException e) {
                    e.setLine(getCurrentLine());
                    push(t);
                    throw e;
                }
            }
            default -> {
                push(t);
                throw new CSSParseException(f + " is not a valid function in this context", getCurrentLine());
            }
        }

        if (t != Token.TK_RPAREN) {
            push(t);
            throw new CSSParseException(t, Token.TK_RPAREN, getCurrentLine());
        }
    }

    private void addPseudoElement(Token t, Selector selector) {
        String value = getTokenValue(t);
        switch (value) {
            case "first-line", "first-letter", "before", "after":
                selector.setPseudoElement(value);
                break;
            default:
                throw new CSSParseException(value + " is not a recognized pseudo-element", getCurrentLine());
        }
    }

//  pseudo
//    : ':' ':'? [ IDENT | FUNCTION S* IDENT? S* ')' ]
//    ;
    private void pseudo(Selector selector) throws IOException {
        Token t = next();
        if (t == Token.TK_COLON) {
            t = next();
            switch (t.getType()) {
                case COLON -> {
                    t = next();
                    addPseudoElement(t, selector);
                }
                case IDENT -> addPseudoClassOrElement(t, selector);
                case FUNCTION -> addPseudoClassOrElementFunction(t, selector);
                default -> {
                    push(t);
                    throw new CSSParseException(t,
                            new Token[]{Token.TK_IDENT, Token.TK_FUNCTION}, getCurrentLine());
                }
            }
        } else {
            push(t);
            throw new CSSParseException(t, Token.TK_COLON, getCurrentLine());
        }
    }

    private boolean checkCSSName(@Nullable CSSName cssName, String propertyName) {
        if (cssName == null) {
            _errorHandler.error(
                    _uri,
                    propertyName + " is an unrecognized CSS property at line "
                        + getCurrentLine() + ". Ignoring declaration.");
            return false;
        }

        if (! CSSName.isImplemented(cssName)) {
            _errorHandler.error(
                    _uri,
                    propertyName + " is not implemented at line "
                        + getCurrentLine() + ". Ignoring declaration.");
            return false;
        }

        PropertyBuilder builder = CSSName.getPropertyBuilder(cssName);
        if (builder == null) {
            _errorHandler.error(
                    _uri,
                    "(bug) No property builder defined for " + propertyName
                        + " at line " + getCurrentLine() + ". Ignoring declaration.");
            return false;
        }

        return true;
    }

//  declaration
//    : property ':' S* expr prio?
//    ;
    private void declaration(Ruleset ruleset, boolean inFontFace) throws IOException {
        try {
            Token t = la();
            if (t == Token.TK_IDENT) {
                String propertyName = property();
                CSSName cssName = CSSName.getByPropertyName(propertyName);

                boolean valid = checkCSSName(cssName, propertyName);

                t = next();
                if (t == Token.TK_COLON) {
                    skip_whitespace();

                    List<PropertyValue> values = expr(
                            CSSName.FONT_FAMILY.equals(cssName) ||
                            CSSName.FONT_SHORTHAND.equals(cssName) ||
                            CSSName.FS_PDF_FONT_ENCODING.equals(cssName));
                    boolean important = false;

                    t = la();
                    if (t == Token.TK_IMPORTANT_SYM) {
                        prio();
                        important = true;
                    }

                    t = la();
                    if (! (t == Token.TK_SEMICOLON || t == Token.TK_RBRACE || t == Token.TK_EOF)) {
                        throw new CSSParseException(
                                t,
                                new Token[] { Token.TK_SEMICOLON, Token.TK_RBRACE },
                                getCurrentLine());
                    }

                    if (valid) {
                        try {
                            PropertyBuilder builder = CSSName.getPropertyBuilder(cssName);
                            ruleset.addAllProperties(builder.buildDeclarations(
                                    cssName, values, ruleset.getOrigin(), important, !inFontFace));
                        } catch (CSSParseException e) {
                            e.setLine(getCurrentLine());
                            error(e, "declaration", true);
                        }
                    }
                } else {
                    push(t);
                    throw new CSSParseException(t, Token.TK_COLON, getCurrentLine());
                }
            } else {
                throw new CSSParseException(t, Token.TK_IDENT, getCurrentLine());
            }
        } catch (CSSParseException e) {
            error(e, "declaration", true);
            recover(false, true);
        }
    }

//  prio
//    : IMPORTANT_SYM S*
//    ;
    private void prio() throws IOException {
        //System.out.println("prio()");
        Token t = next();
        if (t == Token.TK_IMPORTANT_SYM) {
            skip_whitespace();
        } else {
            push(t);
            throw new CSSParseException(t, Token.TK_IMPORTANT_SYM, getCurrentLine());
        }
    }

//  expr
//    : term [ operator term ]*
//    ;
    private List<PropertyValue> expr(boolean literal) throws IOException {
        List<PropertyValue> result = new ArrayList<>(10);
        result.add(term(literal, null));
        LOOP: while (true) {
            Token t = la();
            boolean operator = false;
            Token operatorToken = null;
            switch (t.getType()) {
                case VIRGULE,
                     COMMA -> {
                    operatorToken = t;
                    operator();
                    t = la();
                    operator = true;
                }
            }
            switch (t.getType()) {
                case PLUS,
                     MINUS,
                     NUMBER,
                     PERCENTAGE,
                     PX,
                     CM,
                     MM,
                     IN,
                     PT,
                     PC,
                     EMS,
                     EXS,
                     ANGLE,
                     TIME,
                     FREQ,
                     STRING,
                     IDENT,
                     URI,
                     HASH,
                     FUNCTION -> result.add(term(literal, operatorToken));
                default -> {
                    if (operator) {
                        throw new CSSParseException(t, new Token[]{
                                Token.TK_NUMBER, Token.TK_PLUS, Token.TK_MINUS,
                                Token.TK_PERCENTAGE, Token.TK_PX, Token.TK_EMS, Token.TK_EXS,
                                Token.TK_PC, Token.TK_MM, Token.TK_CM, Token.TK_IN, Token.TK_PT,
                                Token.TK_ANGLE, Token.TK_TIME, Token.TK_FREQ, Token.TK_STRING,
                                Token.TK_IDENT, Token.TK_URI, Token.TK_HASH, Token.TK_FUNCTION},
                                getCurrentLine());
                    } else {
                        break LOOP;
                    }
                }
            }
        }

        return result;
    }

    private String extractNumber(Token t) {
        String token = getTokenValue(t);

        int offset = 0;
        char[] ch = token.toCharArray();
        for (char c : ch) {
            if (c < '0' || c > '9') {
                break;
            }
            offset++;
        }
        if (ch[offset] == '.') {
            offset++;

            for (int i = offset; i < ch.length; i++) {
                char c = ch[i];
                if (c < '0' || c > '9') {
                    break;
                }
                offset++;
            }
        }

        return token.substring(0, offset);
    }

    private String extractUnit(Token t) {
        String s = extractNumber(t);
        return getTokenValue(t).substring(s.length());
    }

    private String sign(float sign) {
        return sign == -1.0f ? "-" : "";
    }

//  term
//    : unary_operator?
//      [ NUMBER S* | PERCENTAGE S* | LENGTH S* | EMS S* | EXS S* | ANGLE S* |
//        TIME S* | FREQ S* ]
//    | STRING S* | IDENT S* | URI S* | hexcolor | function
//    ;
    private PropertyValue term(boolean literal, @Nullable Token operatorToken) throws IOException {
        float sign = 1;
        Token t = la();
        if (t == Token.TK_PLUS || t == Token.TK_MINUS) {
            sign = unary_operator();
            t = la();
        }
        final PropertyValue result;
        switch (t.getType()) {
            case ANGLE -> {
                String unit = extractUnit(t);
                short type = switch (unit) {
                    case "deg" -> CSSPrimitiveValue.CSS_DEG;
                    case "rad" -> CSSPrimitiveValue.CSS_RAD;
                    default -> throw new CSSParseException("Unsupported CSS unit " + unit, getCurrentLine());
                };

                result = new PropertyValue(type,
                        sign * Float.parseFloat(extractNumber(t)),
                        sign(sign) + getTokenValue(t));

                next();
                skip_whitespace();
            }


            case TIME, FREQ, DIMENSION -> throw new CSSParseException("Unsupported CSS unit " + extractUnit(t), getCurrentLine());
            case NUMBER -> {
                    result = new PropertyValue(
                            CSS_NUMBER,
                            sign * Float.parseFloat(getTokenValue(t)),
                            sign(sign) + getTokenValue(t),
                            operatorToken);
                    next();
                    skip_whitespace();
                }
            case PERCENTAGE -> {
                result = new PropertyValue(
                        CSS_PERCENTAGE,
                        sign * Float.parseFloat(extractNumber(t)),
                        sign(sign) + getTokenValue(t),
                        operatorToken);
                next();
                skip_whitespace();
            }
            case EMS -> {
                result = new PropertyValue(
                        CSSPrimitiveValue.CSS_EMS,
                        sign * Float.parseFloat(extractNumber(t)),
                        sign(sign) + getTokenValue(t),
                        operatorToken);
                next();
                skip_whitespace();
            }
            case EXS -> {
                result = new PropertyValue(
                        CSSPrimitiveValue.CSS_EXS,
                        sign * Float.parseFloat(extractNumber(t)),
                        sign(sign) + getTokenValue(t),
                        operatorToken);
                next();
                skip_whitespace();
            }
            case PX -> {
                result = new PropertyValue(
                        CSSPrimitiveValue.CSS_PX,
                        sign * Float.parseFloat(extractNumber(t)),
                        sign(sign) + getTokenValue(t),
                        operatorToken);
                next();
                skip_whitespace();
            }
            case CM -> {
                result = new PropertyValue(
                        CSSPrimitiveValue.CSS_CM,
                        sign * Float.parseFloat(extractNumber(t)),
                        sign(sign) + getTokenValue(t),
                        operatorToken);
                next();
                skip_whitespace();
            }
            case MM -> {
                result = new PropertyValue(
                        CSSPrimitiveValue.CSS_MM,
                        sign * Float.parseFloat(extractNumber(t)),
                        sign(sign) + getTokenValue(t),
                        operatorToken);
                next();
                skip_whitespace();
            }
            case IN -> {
                result = new PropertyValue(
                        CSSPrimitiveValue.CSS_IN,
                        sign * Float.parseFloat(extractNumber(t)),
                        sign(sign) + getTokenValue(t),
                        operatorToken);
                next();
                skip_whitespace();
            }
            case PT -> {
                result = new PropertyValue(
                        CSSPrimitiveValue.CSS_PT,
                        sign * Float.parseFloat(extractNumber(t)),
                        sign(sign) + getTokenValue(t),
                        operatorToken);
                next();
                skip_whitespace();
            }
            case PC -> {
                result = new PropertyValue(
                        CSSPrimitiveValue.CSS_PC,
                        sign * Float.parseFloat(extractNumber(t)),
                        sign(sign) + getTokenValue(t),
                        operatorToken);
                next();
                skip_whitespace();
            }
            case STRING -> {
                String s = getTokenValue(t);
                result = new PropertyValue(
                        CSSPrimitiveValue.CSS_STRING,
                        s,
                        getRawTokenValue(),
                        operatorToken);
                next();
                skip_whitespace();
            }
            case IDENT -> {
                String value = getTokenValue(t, literal);
                result = new PropertyValue(
                        CSSPrimitiveValue.CSS_IDENT,
                        value,
                        value,
                        operatorToken);
                next();
                skip_whitespace();
            }
            case URI -> {
                result = new PropertyValue(
                        CSSPrimitiveValue.CSS_URI,
                        getTokenValue(t),
                        getRawTokenValue(),
                        operatorToken);
                next();
                skip_whitespace();
            }
            case HASH -> result = hexcolor(operatorToken);
            case FUNCTION -> result = function(operatorToken);
            default -> throw new CSSParseException(t, new Token[] { Token.TK_NUMBER,
                    Token.TK_PERCENTAGE, Token.TK_PX, Token.TK_EMS, Token.TK_EXS,
                    Token.TK_PC, Token.TK_MM, Token.TK_CM, Token.TK_IN, Token.TK_PT,
                    Token.TK_ANGLE, Token.TK_TIME, Token.TK_FREQ, Token.TK_STRING,
                    Token.TK_IDENT, Token.TK_URI, Token.TK_HASH, Token.TK_FUNCTION },
                    getCurrentLine());
        }
        return result;
    }

//  function
//    : FUNCTION S* expr ')' S*
//    ;
    private PropertyValue function(Token operatorToken) throws IOException {
        final PropertyValue result;
        Token t = next();
        if (t == Token.TK_FUNCTION) {
            String f = getTokenValue(t);
            skip_whitespace();
            List<PropertyValue> params = expr(false);
            t = next();
            if (t != Token.TK_RPAREN) {
                push(t);
                throw new CSSParseException(t, Token.TK_RPAREN, getCurrentLine());
            }

            if (f.equals("rgb(") || f.equals("rgba(")) {
                result = new PropertyValue(createRGBColorFromFunction(params), operatorToken);
            } else if (f.equals("cmyk(")) {
                if (! isSupportCMYKColors()) {
                    throw new CSSParseException(
                            "The current output device does not support CMYK colors", getCurrentLine());
                }
                //in accordance to http://www.w3.org/TR/css3-gcpm/#cmyk-colors
                result = new PropertyValue(createCMYKColorFromFunction(params), operatorToken);
            } else {
                result = new PropertyValue(new FSFunction(f.substring(0, f.length()-1), params), operatorToken);
            }

            skip_whitespace();
        } else {
            push(t);
            throw new CSSParseException(t, Token.TK_FUNCTION, getCurrentLine());
        }

        return result;
    }

    private FSCMYKColor createCMYKColorFromFunction(List<PropertyValue> params) {
        if (params.size() != 4) {
            throw new CSSParseException(
                    "The cmyk() function must have exactly four parameters",
                    getCurrentLine());
        }

        float[] colorComponents = new float[4];

        for (int i = 0; i < params.size(); i++) {
            colorComponents[i] = parseCMYKColorComponent(params.get(i), (i+1)); //Warning on the truncation?
        }

        return new FSCMYKColor(colorComponents[0], colorComponents[1], colorComponents[2], colorComponents[3]);

    }

    private float parseCMYKColorComponent(PropertyValue value, int paramNo) {
        short type = value.getPrimitiveType();
        final float result;
        if (type == CSS_NUMBER) {
            result = value.getFloatValue();
        } else if (type == CSS_PERCENTAGE) {
            result = value.getFloatValue() / 100.0f;
        } else {
            throw new CSSParseException(
                    "Parameter " + paramNo + " to the cmyk() function is " +
                    "not a number or a percentage", getCurrentLine());
        }

        if (result < 0.0f || result > 1.0f) {
            throw new CSSParseException(
                    "Parameter " + paramNo + " to the cmyk() function must be between zero and one", getCurrentLine());
        }

        return result;
    }

    private FSRGBColor createRGBColorFromFunction(List<PropertyValue> params) {
        if (params.size() != 3 && params.size() != 4) {
            throw new CSSParseException(
                    "The rgb() function must have three or four parameters",
                    getCurrentLine());
        }

        int red = (int) calculateColor(params, 0);
        int green = (int) calculateColor(params, 1);
        int blue = (int) calculateColor(params, 2);
        float alpha = params.size() < 4 ? 1 : calculateColor(params, 3);

        return new FSRGBColor(red, green, blue, alpha);
    }

    private float calculateColor(List<PropertyValue> params, int index) {
        PropertyValue value = params.get(index);
        short type = validateType(index, value);

        float f = switch (type) {
            case CSS_PERCENTAGE -> value.getFloatValue() / 100 * 255;
            default -> value.getFloatValue();
        };

        if (f < 0) {
            return 0;
        } else if (f > 255) {
            return 255;
        } else {
            return f;
        }
    }

    private short validateType(int index, PropertyValue value) {
        short type = value.getPrimitiveType();
        if (type != CSS_PERCENTAGE && type != CSS_NUMBER) {
            throw new CSSParseException(
                    "Parameter " + (index +1) + " to the rgb() function is " +
                    "not a number or percentage", getCurrentLine());
        }

        if (type != CSS_NUMBER && index == 3) {
            throw new CSSParseException(
                    "Parameter alpha to the rgba() function is " +
                    "not a number", getCurrentLine());
        }
        return type;
    }

    //  /*
//  * There is a constraint on the color that it must
//  * have either 3 or 6 hex-digits (i.e., [0-9a-fA-F])
//  * after the "#"; e.g., "#000" is OK, but "#abcd" is not.
//  */
// hexcolor
//   : HASH S*
//   ;
    private PropertyValue hexcolor(Token operatorToken) throws IOException {
        final PropertyValue result;
        Token t = next();
        if (t == Token.TK_HASH) {
            String s = getTokenValue(t);
            if ((s.length() != 3 && s.length() != 6) || ! isHexString(s)) {
                push(t);
                throw new CSSParseException('#' + s + " is not a valid color definition", getCurrentLine());
            }
            FSRGBColor color;
            if (s.length() == 3) {
                color = new FSRGBColor(
                            convertToInteger(s.charAt(0), s.charAt(0)),
                            convertToInteger(s.charAt(1), s.charAt(1)),
                            convertToInteger(s.charAt(2), s.charAt(2)));
            } else { /* s.length == 6 */
                color = new FSRGBColor(
                        convertToInteger(s.charAt(0), s.charAt(1)),
                        convertToInteger(s.charAt(2), s.charAt(3)),
                        convertToInteger(s.charAt(4), s.charAt(5)));
            }
            result = new PropertyValue(color, operatorToken);
            skip_whitespace();
        } else {
            push(t);
            throw new CSSParseException(t, Token.TK_HASH, getCurrentLine());
        }

        return result;
    }

    private boolean isHexString(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (! isHexChar(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    private int convertToInteger(char hexchar1, char hexchar2) {
        int result = convertToInteger(hexchar1);
        result <<= 4;
        result |= convertToInteger(hexchar2);
        return result;
    }

    private int convertToInteger(char hexchar1) {
        if (hexchar1 >= '0' && hexchar1 <= '9') {
            return hexchar1 - '0';
        } else if (hexchar1 >= 'a' && hexchar1 <= 'f') {
            return hexchar1 - 'a' + 10;
        } else { /* if (hexchar1 >= 'A' && hexchar1 <= 'F') */
            return hexchar1 - 'A' + 10;
        }
    }

    private void skip_whitespace() throws IOException {
        Token t;
        while ( (t = next()) == Token.TK_S) {
            // skip
        }
        push(t);
    }

    private void skip_whitespace_and_cdocdc() throws IOException {
        Token t;
        do {
            t = next();
        } while (t == Token.TK_S || t == Token.TK_CDO || t == Token.TK_CDC);
        push(t);
    }

    private Token next() throws IOException {
        if (_saved != null) {
            Token result = _saved;
            _saved = null;
            return result;
        } else {
            return _lexer.yylex();
        }
    }

    private void push(Token t) {
        if (_saved != null) {
            throw new RuntimeException("saved must be null");
        }
        _saved = t;
    }

    private Token la() throws IOException {
        Token result = next();
        push(result);
        return result;
    }

    private void error(CSSParseException e, String what, boolean rethrowEOF) {
        if (! e.isCallerNotified()) {
            String message = e.getMessage() + " Skipping " + what + ".";
            _errorHandler.error(_uri, message);
        }
        e.setCallerNotified(true);
        if (e.isEOF() && rethrowEOF) {
            throw e;
        }
    }

    private void recover(boolean needBlock, boolean stopBeforeBlockClose) throws IOException {
        int braces = 0;
        boolean foundBlock = false;
        LOOP:
        while (true) {
            Token t = next();
            if (t == Token.TK_EOF) {
                return;
            }
            switch (t.getType()) {
                case LBRACE -> {
                    foundBlock = true;
                    braces++;
                }
                case RBRACE -> {
                    if (braces == 0) {
                        if (stopBeforeBlockClose) {
                            push(t);
                            break LOOP;
                        }
                    } else {
                        braces--;
                        if (braces == 0) {
                            break LOOP;
                        }
                    }
                }
                case SEMICOLON -> {
                    if (braces == 0 && (!needBlock || foundBlock)) {
                        break LOOP;
                    }
                }
            }
        }
        skip_whitespace();
    }

    public void reset(Reader r) {
        _saved = null;
        _namespaces.clear();
        _lexer.yyreset(r);
        _lexer.setyyline(0);
    }

    private String getRawTokenValue() {
        return _lexer.yytext();
    }

    private String getTokenValue(Token t) {
        return getTokenValue(t, false);
    }

    private String getTokenValue(Token t, boolean literal) {
        return switch (t.getType()) {
            case STRING ->
                    processEscapes(_lexer.yytext().toCharArray(), 1, _lexer.yylength() - 1);
            case HASH ->
                    processEscapes(_lexer.yytext().toCharArray(), 1, _lexer.yylength());
            case URI -> {
                char[] ch = _lexer.yytext().toCharArray();
                int start = 4;
                while (ch[start] == '\t' || ch[start] == '\r' ||
                        ch[start] == '\n' || ch[start] == '\f') {
                    start++;
                }
                if (ch[start] == '\'' || ch[start] == '"') {
                    start++;
                }
                int end = ch.length - 2;
                while (ch[end] == '\t' || ch[end] == '\r' ||
                        ch[end] == '\n' || ch[end] == '\f') {
                    end--;
                }
                if (ch[end] == '\'' || ch[end] == '"') {
                    end--;
                }

                String uriResult = processEscapes(ch, start, end + 1);

                // Relative URIs are resolved relative to CSS file, not XHTML file
                if (isRelativeURI(uriResult) && _uri != null) {
                    int lastSlash = _uri.lastIndexOf('/');
                    if (lastSlash != -1) {
                        uriResult = _uri.substring(0, lastSlash + 1) + uriResult;
                    }
                } else if (isServerRelativeURI(uriResult) && _uri != null) {
                    int uriOffset = _uri.indexOf("://") + 3;
                    int firstSlashAfterProtocol = _uri.substring(uriOffset).indexOf('/');
                    if (firstSlashAfterProtocol != -1) {
                        uriResult = _uri.substring(0, uriOffset + firstSlashAfterProtocol) + uriResult;
                    }
                }

                yield uriResult;
            }
            case AT_RULE,
                 IDENT,
                 FUNCTION -> {
                int start = 0;
                int count = _lexer.yylength();
                if (t.getType() == AT_RULE) {
                    start++;
                }
                String result = processEscapes(_lexer.yytext().toCharArray(), start, count);
                if (!literal) {
                    result = result.toLowerCase(ROOT);
                }
                yield result;
            }
            default -> _lexer.yytext();
        };
    }

    private boolean isRelativeURI(String uri) {
        try {
            return !uri.isEmpty() && (uri.charAt(0) != '/' && ! new URI(uri).isAbsolute());
        } catch (URISyntaxException ignore) {
            return false;
        }
    }

    private boolean isServerRelativeURI(String uri) {
        try {
            return !uri.isEmpty() && uri.charAt(0) == '/' && !new URI(uri).isAbsolute();
        } catch (URISyntaxException ignore) {
            return false;
        }
    }

    private int getCurrentLine() {
        return _lexer.yyline();
    }

    private static boolean isHexChar(char c) {
        return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f');
    }

    private static String processEscapes(char[] ch, int start, int end) {
        StringBuilder result = new StringBuilder(ch.length + 10);

        for (int i = start; i < end; i++) {
            char c = ch[i];

            if (c == '\\') {
                // eat escaped newlines and handle te\st == test situations
                if (i < end - 2 && (ch[i+1] == '\r' && ch[i+2] == '\n')) {
                    i += 2;
                    continue;
                } else {
                    if ((i+1) < ch.length && (ch[i+1] == '\n' || ch[i+1] == '\r' || ch[i+1] == '\f')) {
                        i++;
                        continue;
                    } else if ((i+1) >= ch.length) {
                       // process \ escaped (\\)
                       result.append(c);
                       continue;
                    } else if (! isHexChar(ch[i+1])) {
                        continue;
                    }
                }

                // Unicode escapes
                int current = ++i;
                while (i < end && isHexChar(ch[i]) && i - current < 6) {
                    i++;
                }

                int cvalue = Integer.parseInt(new String(ch, current, i - current), 16);
                if (cvalue < 0xFFFF) {
                    result.append((char)cvalue);
                }

                i--;

                if (i < end - 2 && (ch[i+1] == '\r' && ch[i+2] == '\n')) {
                    i += 2;
                } else if (i < end - 1 &&
                        (ch[i+1] == ' ' || ch[i+1] == '\t' ||
                                ch[i+1] == '\n' || ch[i+1] == '\r' ||
                                ch[i+1] == '\f')) {
                    i++;
                }
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }

    public boolean isSupportCMYKColors() {
        return _supportCMYKColors;
    }

    public void setSupportCMYKColors(boolean b) {
        _supportCMYKColors = b;
    }

    private static class NamespacePair {
        private final String _namespaceURI;
        private final String _name;

        private NamespacePair(String namespaceURI, String name) {
            _namespaceURI = namespaceURI;
            _name = name;
        }

        public String getNamespaceURI() {
            return _namespaceURI;
        }

        public String getName() {
            return _name;
        }
    }
}
