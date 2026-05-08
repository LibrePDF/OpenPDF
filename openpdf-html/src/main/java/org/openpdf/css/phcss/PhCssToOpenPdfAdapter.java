/*
 * Copyright (c) 2026 OpenPDF
 *
 * Licensed under the GNU Lesser General Public License (LGPL), version 2.1 or
 * later. See the project LICENSE file for details.
 */
package org.openpdf.css.phcss;

import com.helger.css.decl.CSSDeclaration;
import com.helger.css.decl.CSSFontFaceRule;
import com.helger.css.decl.CSSImportRule;
import com.helger.css.decl.CSSMediaQuery;
import com.helger.css.decl.CSSMediaRule;
import com.helger.css.decl.CSSPageRule;
import com.helger.css.decl.CSSStyleRule;
import com.helger.css.decl.CascadingStyleSheet;
import com.helger.css.decl.ICSSPageRuleMember;
import com.helger.css.decl.ICSSTopLevelRule;
import com.helger.css.writer.CSSWriterSettings;
import org.openpdf.css.constants.MarginBoxName;
import org.openpdf.css.parser.CSSParser;
import org.openpdf.css.sheet.FontFaceRule;
import org.openpdf.css.sheet.MediaRule;
import org.openpdf.css.sheet.PageRule;
import org.openpdf.css.sheet.PropertyDeclaration;
import org.openpdf.css.sheet.Ruleset;
import org.openpdf.css.sheet.Stylesheet;
import org.openpdf.css.sheet.StylesheetInfo;
import org.openpdf.css.sheet.StylesheetInfo.Origin;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Adapter that maps a ph-css {@link CascadingStyleSheet} into the
 * openpdf-html {@link Stylesheet} model used by the renderer.
 *
 * <p>Uses ph-css for structural parsing (rule types, @-rule detection) and
 * delegates selector and value parsing to the legacy {@link CSSParser} so
 * existing infrastructure is reused during the migration.</p>
 *
 * @since 3.0.5
 */
@SuppressWarnings("deprecation")
public class PhCssToOpenPdfAdapter {

    private static final CSSWriterSettings COMPACT = new CSSWriterSettings(true);
    private static final String STRING_READER_IO_MSG = "StringReader should not throw IOException";

    /**
     * Convert a ph-css stylesheet into an openpdf-html {@link Stylesheet}.
     *
     * @param source the ph-css parse result
     * @param uri    the originating URI (used for diagnostics and relative URL resolution)
     * @param origin where this stylesheet came from (user agent, user, author)
     * @return a populated {@link Stylesheet}
     */
    public Stylesheet toStylesheet(CascadingStyleSheet source, String uri, Origin origin) {
        Stylesheet target = new Stylesheet(uri, origin);
        CSSParser parser = new CSSParser((uri1, message) -> {});

        for (CSSImportRule importRule : source.getAllImportRules()) {
            target.addImportRule(buildImportRule(importRule, origin));
        }

        for (ICSSTopLevelRule rule : source.getAllRules()) {
            if (rule instanceof CSSStyleRule styleRule) {
                addStyleRules(target, styleRule, uri, origin, parser);
            } else if (rule instanceof CSSMediaRule mediaRule) {
                target.addContent(buildMediaRule(mediaRule, uri, origin, parser));
            } else if (rule instanceof CSSFontFaceRule fontFaceRule) {
                target.addFontFaceRule(buildFontFaceRule(fontFaceRule, origin, parser));
            } else if (rule instanceof CSSPageRule pageRule) {
                target.addContent(buildPageRule(pageRule, origin, parser));
            }
        }
        return target;
    }

    private void addStyleRules(Stylesheet target, CSSStyleRule styleRule,
                               String uri, Origin origin, CSSParser parser) {
        String css = styleRuleAsCss(styleRule);
        try {
            Stylesheet mini = parser.parseStylesheet(uri, origin, new StringReader(css));
            for (Object content : mini.getContents()) {
                if (content instanceof Ruleset ruleset) {
                    target.addContent(ruleset);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException(STRING_READER_IO_MSG, e);
        }
    }

    private MediaRule buildMediaRule(CSSMediaRule phRule, String uri, Origin origin, CSSParser parser) {
        MediaRule mediaRule = new MediaRule(origin);
        for (CSSMediaQuery query : phRule.getAllMediaQueries()) {
            String medium = query.getMedium();
            if (medium != null && !medium.isEmpty()) {
                mediaRule.addMedium(medium);
            }
        }
        for (ICSSTopLevelRule nested : phRule.getAllRules()) {
            if (nested instanceof CSSStyleRule styleRule) {
                String css = styleRuleAsCss(styleRule);
                try {
                    Stylesheet mini = parser.parseStylesheet(uri, origin, new StringReader(css));
                    for (Object content : mini.getContents()) {
                        if (content instanceof Ruleset ruleset) {
                            mediaRule.addContent(ruleset);
                        }
                    }
                } catch (IOException e) {
                    throw new IllegalStateException(STRING_READER_IO_MSG, e);
                }
            }
        }
        return mediaRule;
    }

    private FontFaceRule buildFontFaceRule(CSSFontFaceRule phRule, Origin origin, CSSParser parser) {
        FontFaceRule result = new FontFaceRule(origin);
        Ruleset ruleset = parser.parseDeclaration(origin, declarationBlock(phRule.getAllDeclarations()));
        result.addContent(ruleset);
        return result;
    }

    private PageRule buildPageRule(CSSPageRule phRule, Origin origin, CSSParser parser) {
        String pageName = null;
        String pseudoPage = null;
        List<String> selectors = phRule.getAllSelectors();
        if (!selectors.isEmpty()) {
            String s = selectors.get(0).trim();
            int colon = s.indexOf(':');
            if (colon >= 0) {
                String before = s.substring(0, colon).trim();
                pageName = before.isEmpty() ? null : before;
                String after = s.substring(colon + 1).trim();
                pseudoPage = after.isEmpty() ? null : after;
            } else {
                pageName = s.isEmpty() ? null : s;
            }
        }
        List<CSSDeclaration> decls = new ArrayList<>();
        for (ICSSPageRuleMember member : phRule.getAllMembers()) {
            if (member instanceof CSSDeclaration decl) {
                decls.add(decl);
            }
        }
        Ruleset ruleset = parser.parseDeclaration(origin, declarationBlock(decls));
        Map<MarginBoxName, List<PropertyDeclaration>> empty = Collections.emptyMap();
        return new PageRule(origin, pageName, pseudoPage, empty, ruleset);
    }

    private StylesheetInfo buildImportRule(CSSImportRule phImport, Origin origin) {
        List<String> media = new ArrayList<>();
        for (CSSMediaQuery query : phImport.getAllMediaQueries()) {
            String medium = query.getMedium();
            if (medium != null && !medium.isEmpty()) {
                media.add(medium);
            }
        }
        return new StylesheetInfo(origin, phImport.getLocationString(), media, null);
    }

    private String styleRuleAsCss(CSSStyleRule styleRule) {
        String selectors = styleRule.getAllSelectors().stream()
                .map(s -> s.getAsCSSString(COMPACT, 0))
                .collect(Collectors.joining(","));
        return selectors + "{" + declarationBlock(styleRule.getAllDeclarations()) + "}";
    }

    private String declarationBlock(List<CSSDeclaration> decls) {
        return decls.stream()
                .map(d -> d.getProperty() + ":" + d.getExpressionAsCSSString()
                        + (d.isImportant() ? "!important" : ""))
                .collect(Collectors.joining(";"));
    }
}
