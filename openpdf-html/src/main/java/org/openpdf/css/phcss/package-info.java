/**
 * Experimental CSS parsing layer for openpdf-html backed by
 * <a href="https://github.com/phax/ph-css">ph-css</a>.
 *
 * <h2>Status</h2>
 * <p>
 * This package is the planned successor to {@link org.openpdf.css.parser},
 * the hand-written JFlex-based CSS 2.1 parser that ships with openpdf-html.
 * The legacy parser remains the default implementation used by the renderer
 * and is fully functional; the classes in this package currently provide a
 * <strong>parallel entry point only</strong> and are not yet wired into the
 * rendering pipeline.
 * </p>
 *
 * <h2>Migration plan</h2>
 * <ol>
 *   <li>Add ph-css as a dependency (done in 3.0.5).</li>
 *   <li>Provide thin wrappers around ph-css for parsing CSS strings,
 *       readers and streams (see {@link org.openpdf.css.phcss.PhCssStylesheetFactory}).</li>
 *   <li>Build an adapter that maps ph-css' {@code CascadingStyleSheet} model
 *       into openpdf-html's {@code org.openpdf.css.sheet.Stylesheet} /
 *       {@code Ruleset} / {@code PropertyDeclaration} model
 *       (see {@link org.openpdf.css.phcss.PhCssToOpenPdfAdapter}).</li>
 *   <li>Switch {@code SharedContext}/{@code StylesheetFactory} to use the new
 *       parser by default, keeping the legacy parser available behind a flag.</li>
 *   <li>Remove {@link org.openpdf.css.parser} and the JFlex sources.</li>
 * </ol>
 *
 * <h2>Why ph-css</h2>
 * <ul>
 *   <li>Compliance: full support for CSS3/4 selectors and inheritance.</li>
 *   <li>Stability: robust handling of malformed or complex stylesheets.</li>
 *   <li>Maintenance: leverages a mature, dedicated CSS engine.</li>
 *   <li>Consistency: closer alignment with modern browser rendering.</li>
 * </ul>
 *
 * @since 3.0.5
 */
@NullMarked
package org.openpdf.css.phcss;

import org.jspecify.annotations.NullMarked;

