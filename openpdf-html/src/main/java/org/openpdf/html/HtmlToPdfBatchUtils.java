/*
 * Copyright 2025 OpenPDF
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the License.
 *
 * The Original Code is 'iText, a free JAVA-PDF library'.
 *
 * The Initial Developer of the Original Code is Bruno Lowagie. Portions created by
 * the Initial Developer are Copyright (C) 1999, 2000, 2001, 2002 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000, 2001, 2002 by Paulo Soares. All Rights Reserved.
 *
 * Contributor(s): all the names of the contributors are added in the source code
 * where applicable.
 *
 * Alternatively, the contents of this file may be used under the terms of the
 * LGPL license (the "GNU LIBRARY GENERAL PUBLIC LICENSE"), in which case the
 * provisions of LGPL are applicable instead of those above.  If you wish to
 * allow use of your version of this file only under the terms of the LGPL
 * License and not to allow others to use your version of this file under
 * the MPL, indicate your decision by deleting the provisions above and
 * replace them with the notice and other provisions required by the LGPL.
 * If you do not delete the provisions above, a recipient may use your version
 * of this file under either the MPL or the GNU LIBRARY GENERAL PUBLIC LICENSE.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the MPL as stated above or under the terms of the GNU
 * Library General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library general Public License for more
 * details.
 *
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * https://github.com/LibrePDF/OpenPDF
 */

package org.openpdf.html;

import org.openpdf.pdf.ITextRenderer;
import org.openpdf.layout.SharedContext;
import org.openpdf.text.utils.PdfBatch;
import org.openpdf.text.utils.PdfBatch.BatchResult;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

/**
 * High-performance batch HTML-to-PDF conversion utilities for OpenPDF HTML (Flying Saucer),
 * powered by Java 21 virtual threads for scalable concurrent execution.
 *
 * <p>This class provides simple, high-level APIs for converting HTML sources to PDF files using
 * {@link ITextRenderer}, with support for:</p>
 * <ul>
 *   <li>Raw HTML strings (with optional base URI for resolving relative resources)</li>
 *   <li>Local HTML files and their relative assets (CSS, images, fonts, etc.)</li>
 *   <li>Remote HTML content from HTTP/HTTPS URLs</li>
 *   <li>Optional injection of custom CSS before rendering</li>
 *   <li>Renderer customization hooks for advanced control (e.g., DPI, fonts, user agent)</li>
 * </ul>
 *
 * <h3>Key Features</h3>
 * <ul>
 *   <li>Runs each rendering task on a virtual thread for massive concurrency without thread exhaustion.</li>
 *   <li>Provides convenient {@code batch*} methods to process large collections of jobs in parallel.</li>
 *   <li>Supports flexible output path handling (directories created automatically).</li>
 *   <li>Encapsulates results in a {@link BatchResult} containing separate lists of successes and failures.</li>
 * </ul>
 *
 * <h3>Usage Examples</h3>
 *
 * <h4>Convert a single HTML string to PDF</h4>
 * <pre>{@code
 * Path pdf = Html2PdfBatchUtils.renderHtmlString(
 *     "<html><body>Hello World</body></html>",
 *     "https://example.com/",
 *     Path.of("out.pdf"),
 *     Html2PdfBatchUtils.CSS_A4_20MM,
 *     Html2PdfBatchUtils.setDpi(150)
 * );
 * }</pre>
 *
 * <h4>Batch convert multiple HTML files</h4>
 * <pre>{@code
 * List<HtmlFileJob> jobs = List.of(
 *     new HtmlFileJob(Path.of("file1.html"), Path.of("."), Path.of("file1.pdf"),
 *                     Optional.of(Html2PdfBatchUtils.CSS_LETTER_HALF_IN),
 *                     Optional.empty()),
 *     new HtmlFileJob(Path.of("file2.html"), Path.of("."), Path.of("file2.pdf"),
 *                     Optional.empty(),
 *                     Optional.of(Html2PdfBatchUtils.registerFontDir(Path.of("fonts"))))
 * );
 *
 * BatchResult<Path> result = Html2PdfBatchUtils.batchHtmlFiles(jobs,
 *     path -> System.out.println("Created PDF: " + path),
 *     error -> error.printStackTrace()
 * );
 * }</pre>
 *
 * <h3>Notes & Best Practices</h3>
 * <ul>
 *   <li>Set page size and margins via CSS <code>@page</code> rules in your HTML or via the {@code injectCss} parameter.</li>
 *   <li>Always provide a {@code baseUri} (or {@code baseDir}) if your HTML references relative resources.</li>
 *   <li>Use {@code rendererCustomizer} to adjust advanced rendering settings like DPI or font loading.</li>
 *   <li>Batch methods allow optional success and failure callbacks for real-time feedback during processing.</li>
 * </ul>
 *
 * @implNote Internally uses {@link Executors#newVirtualThreadPerTaskExecutor()} for efficient parallelism.
 * @since 3.0.0
 */

public final class HtmlToPdfBatchUtils {

    private HtmlToPdfBatchUtils() {}



    // ------------------------- Job records -------------------------

    /** Render raw HTML string to PDF. */
    public record HtmlStringJob(String html, String baseUri, Path output,
                                Optional<String> injectCss,
                                Optional<Consumer<ITextRenderer>> rendererCustomizer) {}

    /** Render an HTML file (and its relative assets) to PDF. */
    public record HtmlFileJob(Path htmlFile, Path baseDir, Path output,
                              Optional<String> injectCss,
                              Optional<Consumer<ITextRenderer>> rendererCustomizer) {}

    /** Render a URL to PDF. */
    public record UrlJob(String url, Path output,
                         Optional<String> injectCss,
                         Optional<Consumer<ITextRenderer>> rendererCustomizer) {}

    // ------------------------- Single operations -------------------------

    /** Render an HTML string. */
    public static Path renderHtmlString(String html, String baseUri, Path output,
            String injectCss,
            Consumer<ITextRenderer> rendererCustomizer) throws IOException {
        Objects.requireNonNull(html, "html");
        Objects.requireNonNull(output, "output");
        Files.createDirectories(output.getParent());

        String finalHtml = injectCss != null && !injectCss.isEmpty()
                ? injectCssBlock(injectCss).apply(html)
                : html;

        try (var out = new FileOutputStream(output.toFile())) {
            ITextRenderer renderer = new ITextRenderer();
            SharedContext sc = renderer.getSharedContext();
            // Slightly safer resource loading if you need custom schemes:
            sc.setUserAgentCallback(renderer.getOutputDevice().getSharedContext().getUserAgentCallback());

            if (rendererCustomizer != null) rendererCustomizer.accept(renderer);

            if (baseUri != null && !baseUri.isBlank()) {
                renderer.setDocumentFromString(finalHtml, baseUri);
            } else {
                renderer.setDocumentFromString(finalHtml);
            }
            renderer.layout();
            renderer.createPDF(out, true);
        }
        return output;
    }

    /** Render an HTML file (and relatives). */
    public static Path renderHtmlFile(Path htmlFile, Path baseDir, Path output,
            String injectCss,
            Consumer<ITextRenderer> rendererCustomizer) throws IOException {
        Objects.requireNonNull(htmlFile, "htmlFile");
        Objects.requireNonNull(output, "output");
        Files.createDirectories(output.getParent());

        String html = Files.readString(htmlFile, StandardCharsets.UTF_8);
        String base = (baseDir != null ? baseDir.toUri().toString() : htmlFile.getParent().toUri().toString());
        return renderHtmlString(html, base, output, injectCss, rendererCustomizer);
    }

    /** Render a URL to PDF. */
    public static Path renderUrl(String url, Path output,
            String injectCss,
            Consumer<ITextRenderer> rendererCustomizer) throws IOException {
        Objects.requireNonNull(url, "url");
        Objects.requireNonNull(output, "output");
        Files.createDirectories(output.getParent());

        try (var out = new FileOutputStream(output.toFile())) {
            ITextRenderer renderer = new ITextRenderer();
            if (rendererCustomizer != null) rendererCustomizer.accept(renderer);

            if (injectCss == null || injectCss.isEmpty()) {
                // No CSS injection: load DOM directly via user agent and set base URL
                org.w3c.dom.Document doc = renderer.getSharedContext().getUac().getXMLResource(url).getDocument();
                renderer.setDocument(doc, url);
            } else {
                // Inject CSS: fetch HTML as text, prepend <style> to <head>, and set with base URL
                String html;
                try (var in = java.net.URI.create(url).toURL().openStream()) {
                    html = new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                }
                String finalHtml = injectCssBlock(injectCss).apply(html);
                renderer.setDocumentFromString(finalHtml, url);
            }

            renderer.layout();
            renderer.createPDF(out, true);
        }
        return output;
    }

    // ------------------------- Batch operations -------------------------

    public static BatchResult<Path> batchHtmlStrings(List<HtmlStringJob> jobs,
            Consumer<Path> onSuccess,
            Consumer<Throwable> onFailure) {
        return PdfBatch.run(jobs.stream().map(j -> (Callable<Path>) () ->
                renderHtmlString(j.html, j.baseUri, j.output,
                        j.injectCss.orElse(null),
                        j.rendererCustomizer.orElse(null))
        ).toList(), onSuccess, onFailure);
    }

    public static BatchResult<Path> batchHtmlFiles(List<HtmlFileJob> jobs,
            Consumer<Path> onSuccess,
            Consumer<Throwable> onFailure) {
        return PdfBatch.run(jobs.stream().map(j -> (Callable<Path>) () ->
                renderHtmlFile(j.htmlFile, j.baseDir, j.output,
                        j.injectCss.orElse(null),
                        j.rendererCustomizer.orElse(null))
        ).toList(), onSuccess, onFailure);
    }

    public static BatchResult<Path> batchUrls(List<UrlJob> jobs,
            Consumer<Path> onSuccess,
            Consumer<Throwable> onFailure) {
        return PdfBatch.run(jobs.stream().map(j -> (Callable<Path>) () ->
                renderUrl(j.url, j.output, j.injectCss.orElse(null), j.rendererCustomizer.orElse(null))
        ).toList(), onSuccess, onFailure);
    }

    // ------------------------- Helpers -------------------------

    /**
     * Wraps an HTML string with a <style> block prepended to <head>.
     * If the HTML lacks a <head>, one is created.
     */
    public static UnaryOperator<String> injectCssBlock(String css) {
        return (html) -> {
            String style = "<style>" + css + "</style>";
            String lower = html.toLowerCase();
            int headIdx = lower.indexOf("<head>");
            if (headIdx >= 0) {
                int insertPos = headIdx + "<head>".length();
                return html.substring(0, insertPos) + style + html.substring(insertPos);
            }
            // No <head>: create minimal structure
            return "<html><head>" + style + "</head><body>" + html + "</body></html>";
        };
    }

    /** Sample CSS for A4 portrait with 20mm margins. */
    public static final String CSS_A4_20MM = "@page { size: A4; margin: 20mm; }";
    /** Sample CSS for US Letter portrait with 0.5in margins. */
    public static final String CSS_LETTER_HALF_IN = "@page { size: Letter; margin: 0.5in; }";

    /** Example customizer: set DPI for images/text. */
    public static Consumer<ITextRenderer> setDpi(int dpi) {
        return (renderer) -> {
            renderer.getSharedContext().setDPI(dpi);
        };
    }

    /** Example customizer: register a font directory (TrueType/OpenType). */
    public static Consumer<ITextRenderer> registerFontDir(Path dir) {
        return (renderer) -> {
            try {
                renderer.getFontResolver().addFontDirectory(dir.toString(), true);
            } catch (Exception e) {
                throw new RuntimeException("Failed to register fonts from: " + dir, e);
            }
        };
    }

}
