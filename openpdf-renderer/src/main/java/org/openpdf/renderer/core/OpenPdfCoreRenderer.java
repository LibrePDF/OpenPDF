/*
 * Copyright 2026 the OpenPDF contributors.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */
package org.openpdf.renderer.core;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.openpdf.text.pdf.PdfContentParser;
import org.openpdf.text.pdf.PdfLiteral;
import org.openpdf.text.pdf.PdfObject;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.PRTokeniser;
import org.openpdf.text.pdf.parser.PdfTextExtractor;

/**
 * Bridge entry point that uses {@code openpdf-core} ({@link PdfReader}) as the
 * underlying PDF parser <em>and</em> as the source of operators driving Java2D
 * rendering for the renderer module.
 *
 * <p>This class is the recommended replacement for the legacy in-tree parser
 * stack made up of {@link org.openpdf.renderer.PDFFile PDFFile},
 * {@link org.openpdf.renderer.PDFPage PDFPage},
 * {@link org.openpdf.renderer.PDFParser PDFParser} and
 * {@link org.openpdf.renderer.decode.PDFDecoder PDFDecoder}, which are all
 * deprecated as of 3.0.5.</p>
 *
 * <h2>Implementation</h2>
 * <p>All public methods are now backed entirely by {@code openpdf-core}:</p>
 * <ul>
 *   <li>Document open / page count / page geometry: {@link PdfReader}.</li>
 *   <li>Document metadata: {@link PdfReader#getInfo()}.</li>
 *   <li>Page text extraction: {@link PdfTextExtractor}.</li>
 *   <li>Decoded page content stream: {@link PdfReader#getPageContent(int)}.</li>
 *   <li>Content-stream operator listing: {@link PdfContentParser}.</li>
 *   <li>Page rasterization: {@link OpenPdfCorePageRenderer}, which parses the
 *       page content stream with {@link PdfContentParser} and dispatches PDF
 *       operators directly to a {@link Graphics2D}.</li>
 * </ul>
 *
 * <p>The legacy in-tree parser is no longer used by this class.</p>
 *
 * <h2>Usage</h2>
 * <pre>{@code
 * try (OpenPdfCoreRenderer renderer = new OpenPdfCoreRenderer(new File("document.pdf"))) {
 *     int pages = renderer.getNumPages();
 *     String title = renderer.getMetadata("Title");
 *     String text  = renderer.getTextFromPage(1);
 *     BufferedImage img = renderer.renderPage(1, 150f); // 150 DPI
 * }
 * }</pre>
 *
 * <p>Page numbers are <strong>1-based</strong>. Instances are not thread-safe.</p>
 *
 * @since 3.0.5
 */
public class OpenPdfCoreRenderer implements Closeable {

    /** Default user-space resolution of a PDF, in DPI. */
    private static final float PDF_USER_SPACE_DPI = 72f;

    private final PdfReader reader;
    private boolean closed;

    /**
     * Opens the given PDF file using {@code openpdf-core}.
     *
     * @param file the PDF file to open; must exist and be readable
     * @throws IOException if the file cannot be read or is not a valid PDF
     */
    public OpenPdfCoreRenderer(File file) throws IOException {
        this(Files.readAllBytes(Objects.requireNonNull(file, "file").toPath()));
    }

    /**
     * Opens the PDF at the given filesystem path.
     *
     * @param path the path to the PDF
     * @throws IOException if the file cannot be read or is not a valid PDF
     * @since 3.0.5
     */
    public OpenPdfCoreRenderer(Path path) throws IOException {
        this(Files.readAllBytes(Objects.requireNonNull(path, "path")));
    }

    /**
     * Opens a PDF from the given input stream. The stream is fully consumed
     * but not closed.
     *
     * @param in the stream containing PDF bytes
     * @throws IOException if the stream cannot be read or is not a valid PDF
     */
    public OpenPdfCoreRenderer(InputStream in) throws IOException {
        this(toByteArray(Objects.requireNonNull(in, "in")));
    }

    /**
     * Opens a PDF from the given byte array. The array is retained by
     * reference; do not mutate it after passing it in.
     *
     * @param pdfBytes the raw PDF bytes
     * @throws IOException if the bytes are not a valid PDF
     */
    public OpenPdfCoreRenderer(byte[] pdfBytes) throws IOException {
        this.reader = new PdfReader(Objects.requireNonNull(pdfBytes, "pdfBytes"));
    }

    /**
     * @return the number of pages in the document
     */
    public int getNumPages() {
        ensureOpen();
        return reader.getNumberOfPages();
    }

    /**
     * Returns the visible size of the requested page in PDF user space units
     * (1 unit = 1/72 inch), with the page rotation already applied so that
     * width/height match what a viewer would display. The returned rectangle
     * is anchored at the origin (0, 0).
     *
     * @param pageNumber 1-based page number
     * @return the page size as a {@link Rectangle2D}
     */
    public Rectangle2D getPageSize(int pageNumber) {
        ensureOpen();
        org.openpdf.text.Rectangle r = reader.getPageSizeWithRotation(pageNumber);
        return new Rectangle2D.Float(0f, 0f, r.getWidth(), r.getHeight());
    }

    /**
     * @param pageNumber 1-based page number
     * @return the page rotation in degrees, normalized to [0, 360)
     */
    public int getPageRotation(int pageNumber) {
        ensureOpen();
        return reader.getPageRotation(pageNumber);
    }

    /**
     * Returns the document Info dictionary as an unmodifiable map of string
     * entries (e.g. {@code Title}, {@code Author}, {@code Subject},
     * {@code Producer}, {@code Creator}, {@code CreationDate}, {@code ModDate}).
     *
     * @return the document metadata; never {@code null}
     * @since 3.0.5
     */
    public Map<String, String> getMetadata() {
        ensureOpen();
        Map<String, String> info = reader.getInfo();
        return info == null ? Collections.emptyMap() : Collections.unmodifiableMap(info);
    }

    /**
     * Returns a single entry from the document Info dictionary.
     *
     * @param key the metadata key (e.g. {@code "Title"}); must not be {@code null}
     * @return the value, or {@code null} if missing
     * @since 3.0.5
     */
    public String getMetadata(String key) {
        Objects.requireNonNull(key, "key");
        return getMetadata().get(key);
    }

    /**
     * Extracts plain text from the requested page using {@link PdfTextExtractor}.
     *
     * @param pageNumber 1-based page number
     * @return the extracted text (may be empty, never {@code null})
     * @throws IOException if the page content cannot be parsed
     * @since 3.0.5
     */
    public String getTextFromPage(int pageNumber) throws IOException {
        ensureOpen();
        return new PdfTextExtractor(reader).getTextFromPage(pageNumber);
    }

    /**
     * Returns the decoded content stream bytes of the requested page,
     * with all stream filters (Flate, LZW, ASCII85, ...) already applied
     * by {@code openpdf-core}.
     *
     * @param pageNumber 1-based page number
     * @return the decoded content stream bytes
     * @throws IOException if the page content cannot be read
     * @since 3.0.5
     */
    public byte[] getPageContent(int pageNumber) throws IOException {
        ensureOpen();
        return reader.getPageContent(pageNumber);
    }

    /**
     * Returns the ordered list of PDF content-stream operator names that
     * appear on the requested page, parsed by {@code openpdf-core}'s
     * {@link PdfContentParser}.
     *
     * <p>Example: a typical text-only page might return
     * {@code [q, BT, Tf, Td, Tj, ET, Q]}.</p>
     *
     * @param pageNumber 1-based page number
     * @return the operator names in stream order; never {@code null}
     * @throws IOException if the page content cannot be parsed
     * @since 3.0.5
     */
    public List<String> getContentOperators(int pageNumber) throws IOException {
        ensureOpen();
        byte[] content = getPageContent(pageNumber);
        List<String> operators = new ArrayList<>();
        PdfContentParser parser = new PdfContentParser(new PRTokeniser(content));
        List<PdfObject> operands = new ArrayList<>();
        while (!parser.parse(operands).isEmpty()) {
            PdfLiteral op = (PdfLiteral) operands.get(operands.size() - 1);
            operators.add(op.toString());
        }
        return operators;
    }

    /**
     * Renders the requested page to a {@link BufferedImage} at the given DPI,
     * using {@link OpenPdfCorePageRenderer} to walk the page's content stream
     * with {@code openpdf-core}'s {@link PdfContentParser} and dispatch PDF
     * operators directly to Java2D.
     *
     * <p>The returned image is of type {@link BufferedImage#TYPE_INT_ARGB},
     * filled with an opaque white background, with antialiasing enabled for
     * shapes and text. Width and height are the page size (in PDF user space
     * units, i.e. 1/72 inch) scaled by {@code dpi / 72}.</p>
     *
     * @param pageNumber 1-based page number
     * @param dpi target resolution in dots per inch (e.g. 72, 150, 300)
     * @return the rendered page image
     * @throws IOException if reading the page fails
     * @throws IllegalArgumentException if {@code dpi <= 0} or {@code pageNumber}
     *         is out of range
     * @throws IllegalStateException if this renderer has been closed
     */
    public BufferedImage renderPage(int pageNumber, float dpi) throws IOException {
        ensureOpen();
        if (dpi <= 0f) {
            throw new IllegalArgumentException("dpi must be > 0, was " + dpi);
        }
        int numPages = getNumPages();
        if (pageNumber < 1 || pageNumber > numPages) {
            throw new IllegalArgumentException(
                    "pageNumber " + pageNumber + " out of range [1, " + numPages + "]");
        }

        Rectangle2D size = getPageSize(pageNumber);
        float scale = dpi / PDF_USER_SPACE_DPI;
        int width = Math.max(1, Math.round((float) size.getWidth() * scale));
        int height = Math.max(1, Math.round((float) size.getHeight() * scale));

        BufferedImage out = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = out.createGraphics();
        try {
            OpenPdfCorePageRenderer.render(reader, pageNumber, g2, width, height, dpi);
        } finally {
            g2.dispose();
        }
        return out;
    }

    /**
     * @return the underlying {@link PdfReader}, for advanced callers that need
     *         direct access to {@code openpdf-core} parsing APIs
     *         (page dictionaries, content streams, info dictionary, etc.)
     */
    public PdfReader getReader() {
        ensureOpen();
        return reader;
    }

    @Override
    public void close() {
        if (closed) {
            return;
        }
        closed = true;
        reader.close();
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("OpenPdfCoreRenderer is closed");
        }
    }

    private static byte[] toByteArray(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[8192];
        int n;
        while ((n = in.read(buf)) != -1) {
            out.write(buf, 0, n);
        }
        return out.toByteArray();
    }
}

