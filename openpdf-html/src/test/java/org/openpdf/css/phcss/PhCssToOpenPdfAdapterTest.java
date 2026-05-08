/*
 * Copyright (c) 2026 OpenPDF
 *
 * Licensed under the GNU Lesser General Public License (LGPL), version 2.1 or
 * later. See the project LICENSE file for details.
 */
package org.openpdf.css.phcss;

import com.helger.css.decl.CascadingStyleSheet;
import org.junit.jupiter.api.Test;
import org.openpdf.css.sheet.FontFaceRule;
import org.openpdf.css.sheet.MediaRule;
import org.openpdf.css.sheet.PageRule;
import org.openpdf.css.sheet.Ruleset;
import org.openpdf.css.sheet.Stylesheet;
import org.openpdf.css.sheet.StylesheetInfo;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.openpdf.css.sheet.StylesheetInfo.Origin.AUTHOR;

@SuppressWarnings("deprecation")
class PhCssToOpenPdfAdapterTest {

    private final PhCssParser parser = new PhCssParser();
    private final PhCssToOpenPdfAdapter adapter = new PhCssToOpenPdfAdapter();

    private Stylesheet parse(String css) {
        CascadingStyleSheet source = parser.parseStylesheet(null, css);
        return adapter.toStylesheet(source, "test.css", AUTHOR);
    }

    @Test
    void simpleStyleRule() {
        Stylesheet sheet = parse("p { color: red; font-size: 12px; }");

        assertThat(sheet.getContents()).hasSize(1);
        Ruleset ruleset = (Ruleset) sheet.getContents().get(0);
        assertThat(ruleset.getPropertyDeclarations()).isNotEmpty();
        assertThat(ruleset.getFSSelectors()).hasSize(1);
        assertThat(ruleset.getFSSelectors().get(0).getSelectorText()).contains("p");
    }

    @Test
    void multipleSelectors() {
        Stylesheet sheet = parse("h1, h2, h3 { font-weight: bold; }");

        // The legacy parser produces one Ruleset with three selectors for comma-separated groups.
        assertThat(sheet.getContents()).hasSize(1);
        Ruleset ruleset = (Ruleset) sheet.getContents().get(0);
        assertThat(ruleset.getFSSelectors()).hasSize(3);
    }

    @Test
    void classAndIdSelectors() {
        Stylesheet sheet = parse(".foo { color: blue; } #bar { color: green; }");

        assertThat(sheet.getContents()).hasSize(2);
        Ruleset foo = (Ruleset) sheet.getContents().get(0);
        assertThat(foo.getPropertyDeclarations()).isNotEmpty();
        Ruleset bar = (Ruleset) sheet.getContents().get(1);
        assertThat(bar.getPropertyDeclarations()).isNotEmpty();
    }

    @Test
    void descendantSelector() {
        Stylesheet sheet = parse("div p { color: red; }");

        assertThat(sheet.getContents()).hasSize(1);
        Ruleset ruleset = (Ruleset) sheet.getContents().get(0);
        assertThat(ruleset.getFSSelectors()).hasSize(1);
    }

    @Test
    void childSelector() {
        Stylesheet sheet = parse("ul > li { list-style: none; }");

        assertThat(sheet.getContents()).hasSize(1);
        assertThat(((Ruleset) sheet.getContents().get(0)).getFSSelectors()).hasSize(1);
    }

    @Test
    void mediaRule() {
        Stylesheet sheet = parse("@media print { p { color: black; } }");

        List<Object> contents = sheet.getContents();
        assertThat(contents).hasSize(1);
        assertThat(contents.get(0)).isInstanceOf(MediaRule.class);
        MediaRule media = (MediaRule) contents.get(0);
        assertThat(media.matches("print")).isTrue();
        assertThat(media.getContents()).hasSize(1);
    }

    @Test
    void fontFaceRule() {
        Stylesheet sheet = parse("@font-face { font-family: MyFont; src: url(myfont.woff2); }");

        assertThat(sheet.getFontFaceRules()).hasSize(1);
        FontFaceRule ffRule = sheet.getFontFaceRules().get(0);
        assertThat(ffRule).isNotNull();
    }

    @Test
    void pageRule() {
        Stylesheet sheet = parse("@page :first { margin: 2cm; }");

        List<Object> contents = sheet.getContents();
        assertThat(contents).hasSize(1);
        assertThat(contents.get(0)).isInstanceOf(PageRule.class);
        PageRule pageRule = (PageRule) contents.get(0);
        assertThat(pageRule.getPseudoPage()).isEqualTo("first");
    }

    @Test
    void importRule() {
        Stylesheet sheet = parse("@import url('base.css');");

        assertThat(sheet.getImportRules()).hasSize(1);
        StylesheetInfo info = sheet.getImportRules().get(0);
        assertThat(info.getUri()).contains("base.css");
    }

    @Test
    void importRuleWithMedia() {
        Stylesheet sheet = parse("@import url('print.css') print;");

        StylesheetInfo info = sheet.getImportRules().get(0);
        assertThat(info.appliesToMedia("print")).isTrue();
        assertThat(info.appliesToMedia("screen")).isFalse();
    }

    @Test
    void importantDeclaration() {
        Stylesheet sheet = parse("p { color: red !important; }");

        Ruleset ruleset = (Ruleset) sheet.getContents().get(0);
        assertThat(ruleset.getPropertyDeclarations())
                .anyMatch(pd -> pd.isImportant());
    }

    @Test
    void pseudoClassSelector() {
        Stylesheet sheet = parse("a:hover { color: blue; }");

        assertThat(sheet.getContents()).hasSize(1);
    }

    @Test
    void attributeSelector() {
        Stylesheet sheet = parse("input[type=text] { border: 1px solid; }");

        assertThat(sheet.getContents()).hasSize(1);
    }

    @Test
    void uriAndOriginPreserved() {
        CascadingStyleSheet source = parser.parseStylesheet(null, "p { color: red; }");
        Stylesheet sheet = adapter.toStylesheet(source, "styles.css", AUTHOR);

        assertThat(sheet.getURI()).isEqualTo("styles.css");
        assertThat(sheet.getOrigin()).isEqualTo(AUTHOR);
    }

    @Test
    void emptyStylesheet() {
        Stylesheet sheet = parse("");

        assertThat(sheet.getContents()).isEmpty();
        assertThat(sheet.getFontFaceRules()).isEmpty();
        assertThat(sheet.getImportRules()).isEmpty();
    }
}
