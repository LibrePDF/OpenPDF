/*
 * Copyright 2026 the OpenPDF contributors.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 */
package org.openpdf.renderer.core;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.openpdf.text.pdf.CMapAwareDocumentFont;
import org.openpdf.text.pdf.PRIndirectReference;
import org.openpdf.text.pdf.PRStream;
import org.openpdf.text.pdf.PRTokeniser;
import org.openpdf.text.pdf.PdfArray;
import org.openpdf.text.pdf.PdfContentParser;
import org.openpdf.text.pdf.PdfDictionary;
import org.openpdf.text.pdf.PdfLiteral;
import org.openpdf.text.pdf.PdfName;
import org.openpdf.text.pdf.PdfNumber;
import org.openpdf.text.pdf.PdfObject;
import org.openpdf.text.pdf.PdfReader;
import org.openpdf.text.pdf.PdfString;

/**
 * Renders a single PDF page to a {@link Graphics2D} surface, parsing the page's
 * content stream with {@code openpdf-core}'s {@link PdfContentParser} and
 * dispatching the resulting operators to Java2D drawing calls.
 *
 * <p>This is the {@code openpdf-core}-driven Java2D rasterizer that backs
 * {@link OpenPdfCoreRenderer#renderPage(int, float)}.</p>
 *
 * <h2>Supported operator subset</h2>
 * <ul>
 *   <li>Graphics state: {@code q}, {@code Q}, {@code cm},
 *       {@code gs} (alpha {@code CA}/{@code ca} only)</li>
 *   <li>Line style: {@code w}, {@code J}, {@code j}, {@code M}, {@code d},
 *       {@code i} (flatness, no-op)</li>
 *   <li>Path construction: {@code m}, {@code l}, {@code c}, {@code v}, {@code y},
 *       {@code re}, {@code h}</li>
 *   <li>Path painting: {@code S}, {@code s}, {@code f}, {@code F}, {@code f*},
 *       {@code B}, {@code B*}, {@code b}, {@code b*}, {@code n}</li>
 *   <li>Clipping: {@code W}, {@code W*}</li>
 *   <li>Colors (DeviceGray / DeviceRGB / DeviceCMYK): {@code g}, {@code G},
 *       {@code rg}, {@code RG}, {@code k}, {@code K}, plus color-space-aware
 *       {@code cs}, {@code CS}, {@code sc}, {@code SC}, {@code scn}, {@code SCN}</li>
 *   <li>Text state: {@code BT}, {@code ET}, {@code Tf}, {@code Tc}, {@code Tw},
 *       {@code TL}, {@code Tz}, {@code Td}, {@code TD}, {@code Tm}, {@code T*},
 *       {@code Ts} (text rise)</li>
 *   <li>Text showing: {@code Tj}, {@code TJ}, {@code '}, {@code "}</li>
 *   <li>Marked content / compatibility (no-op): {@code BMC}, {@code BDC},
 *       {@code EMC}, {@code MP}, {@code DP}, {@code BX}, {@code EX}</li>
 *   <li>XObjects ({@code Do}): Form XObjects (recursive content streams with
 *       their own {@code BBox} / {@code Matrix}) and Image XObjects
 *       (JPEG via {@code DCTDecode}, JPEG2000 via {@code JPXDecode} when
 *       supported by {@code ImageIO}, uncompressed / Flate-decoded 8-bit
 *       DeviceGray / DeviceRGB / DeviceCMYK images, and 8-bit Indexed
 *       images expanded through the palette into their base color space).</li>
 * </ul>
 *
 * <p>Inline images ({@code BI}/{@code ID}/{@code EI}) are promoted out of the
 * content stream into synthetic Image XObjects during a preprocess pass, then
 * rendered via the same code path as regular Image XObjects. Uncompressed,
 * Flate-decoded and JPEG inline images all work. Shading {@code sh},
 * pattern / shading colors and type 3 font glyph operators are silently
 * ignored. Pages that rely heavily on those features may render with missing
 * content.</p>
 *
 * <h2>Text rendering</h2>
 * <p>For each {@code Tf}-selected font, the renderer pulls the embedded font
 * program ({@code FontFile2}, {@code FontFile3} or {@code FontFile} on the
 * FontDescriptor) out via {@code openpdf-core}'s {@code PdfReader} and hands
 * the bytes to {@link java.awt.Font#createFont}. The resulting AWT font is
 * cached and used to draw glyphs, so subsetted / embedded TrueType fonts
 * render with their own glyph shapes. When no font program is embedded (or
 * loading it fails), the renderer falls back to a generic Java2D family
 * picked by PostScript-name heuristics &mdash; correct shape only by
 * accident, but the glyph widths from the PDF font are still respected.</p>
 *
 * <h2>Coordinates</h2>
 * <p>The PDF user space has its origin at the bottom-left and Y growing up;
 * the {@link Graphics2D} target has its origin at the top-left and Y growing
 * down. The renderer applies an initial transform that flips Y and scales
 * by {@code dpi / 72} so that calling code receives a correctly-oriented
 * image of the requested resolution.</p>
 */
final class OpenPdfCorePageRenderer {

    private static final Logger LOG = Logger.getLogger(OpenPdfCorePageRenderer.class.getName());

    /**
     * Default user-space resolution of a PDF, in DPI.
     */
    private static final float PDF_USER_SPACE_DPI = 72f;

    // ExtGState dictionary keys not pre-defined as PdfName constants in openpdf-core.
    private static final PdfName EXTGS_LW = new PdfName("LW");
    private static final PdfName EXTGS_ML = new PdfName("ML");
    private static final PdfName EXTGS_LC = new PdfName("LC");
    private static final PdfName EXTGS_LJ = new PdfName("LJ");

    // FontDescriptor entries that may hold an embedded font program, in preference order:
    // FontFile2 (TrueType) is by far the most common in modern PDFs; FontFile3 holds CFF /
    // OpenType subsets which TYPE-1-flagged AWT Font.createFont also accepts in many cases;
    // FontFile is legacy Type 1 (rarely loadable as TRUETYPE_FONT but worth trying).
    private static final List<PdfName> EMBEDDED_FONT_KEYS = List.of(
            PdfName.FONTFILE2, PdfName.FONTFILE3, PdfName.FONTFILE);

    // Color-space identifiers used by imageComponents().
    private static final PdfName CS_ICC_BASED = new PdfName("ICCBased");
    private static final PdfName CS_CAL_GRAY = new PdfName("CalGray");
    private static final PdfName CS_CAL_RGB = new PdfName("CalRGB");
    private static final PdfName CS_LAB = new PdfName("Lab");
    private static final PdfName CS_INDEXED = new PdfName("Indexed");
    private static final PdfName CS_N = new PdfName("N");
    private static final Set<PdfName> DEVICE_GRAY_NAMES = Set.of(
            PdfName.DEVICEGRAY, new PdfName("G"), CS_CAL_GRAY);
    private static final Set<PdfName> DEVICE_RGB_NAMES = Set.of(
            PdfName.DEVICERGB, new PdfName("RGB"), CS_CAL_RGB);
    private static final Set<PdfName> DEVICE_CMYK_NAMES = Set.of(
            PdfName.DEVICECMYK, new PdfName("CMYK"));

    /**
     * Synthetic XObject-name prefix used for inline images that have been promoted out
     * of the content stream into {@link #inlineImages}. Keeping a clearly distinctive
     * prefix avoids collisions with real {@code /XObject} resource names.
     */
    private static final String INLINE_IMAGE_PREFIX = "__inline_image__";

    private final Graphics2D g2;
    private final PdfDictionary resources;
    private final Map<String, CMapAwareDocumentFont> fontCache = new HashMap<>();
    /**
     * Embedded-font program cache keyed by the FontDescriptor's identity. Re-parsing a
     * TrueType program for every {@code Tj} call would be wasteful; a single page can
     * reference the same font hundreds of times.
     */
    private final Map<PdfDictionary, Font> awtFontCache = new HashMap<>();
    /**
     * Decoded inline images, keyed by the synthetic XObject name we substitute into
     * the content stream in place of the original {@code BI...EI} block.
     */
    private final Map<String, BufferedImage> inlineImages = new HashMap<>();

    private final Deque<GState> stateStack = new ArrayDeque<>();
    private final Deque<AffineTransform> ctmStack = new ArrayDeque<>();
    // LinkedList because the PDF clip may be null (no clip), which ArrayDeque rejects.
    private final Deque<Shape> clipStack = new LinkedList<>();
    private GState state;

    private Path2D.Float currentPath = new Path2D.Float();
    // These fields maintain path state across consecutive dispatch() calls and cannot be local.
    @SuppressWarnings("PMD.SingularField")
    private float pathStartX;
    @SuppressWarnings("PMD.SingularField")
    private float pathStartY;
    @SuppressWarnings("PMD.SingularField")
    private float pathCurX;
    @SuppressWarnings("PMD.SingularField")
    private float pathCurY;

    /**
     * Text object state (between {@code BT} and {@code ET}).
     */
    private boolean inTextObject;
    private AffineTransform textMatrix;
    private AffineTransform textLineMatrix;

    private OpenPdfCorePageRenderer(Graphics2D g2, PdfDictionary resources) {
        this.g2 = g2;
        this.resources = resources;
        this.state = new GState();
    }

    /**
     * Renders the given page to {@code g2}, sized to {@code targetWidth x targetHeight}
     * pixels at the requested DPI.
     *
     * @param reader       the open PDF
     * @param pageNumber   1-based page number
     * @param g2           the destination graphics; will be transformed
     * @param targetWidth  destination width in pixels
     * @param targetHeight destination height in pixels
     * @param dpi          target resolution
     * @throws IOException if the page content cannot be read
     */
    static void render(PdfReader reader, int pageNumber, Graphics2D g2,
            int targetWidth, int targetHeight, float dpi) throws IOException {

        PdfDictionary pageDict = reader.getPageN(pageNumber);
        PdfDictionary resources = pageDict == null ? null : pageDict.getAsDict(PdfName.RESOURCES);

        // Background: opaque white, matching legacy behavior.
        g2.setComposite(AlphaComposite.SrcOver);
        g2.setColor(Color.WHITE);
        g2.fillRect(0, 0, targetWidth, targetHeight);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);

        // Map PDF user space (origin bottom-left, Y up) to image pixels (origin top-left, Y down),
        // applying page rotation and DPI scaling.
        org.openpdf.text.Rectangle pageSize = reader.getPageSizeWithRotation(pageNumber);
        float scale = dpi / PDF_USER_SPACE_DPI;
        int rotation = reader.getPageRotation(pageNumber) % 360;
        if (rotation < 0) {
            rotation += 360;
        }

        AffineTransform initial = new AffineTransform();
        initial.scale(scale, scale);
        // Place origin at bottom-left of the (rotated) page and flip Y so that
        // PDF user space (Y up) maps onto image pixel space (Y down).
        switch (rotation) {
            case 90:
                initial.translate(pageSize.getHeight(), pageSize.getWidth());
                initial.scale(1, -1);
                initial.rotate(Math.toRadians(90));
                break;
            case 180:
                initial.translate(pageSize.getWidth(), 0);
                initial.scale(-1, 1);
                initial.translate(0, pageSize.getHeight());
                initial.scale(1, -1);
                break;
            case 270:
                initial.rotate(Math.toRadians(-90));
                initial.scale(1, -1);
                break;
            case 0:
            default:
                initial.translate(0, pageSize.getHeight());
                initial.scale(1, -1);
                break;
        }
        g2.transform(initial);

        OpenPdfCorePageRenderer renderer = new OpenPdfCorePageRenderer(g2, resources);
        renderer.processContent(reader.getPageContent(pageNumber));
    }

    private void processContent(byte[] contentBytes) throws IOException {
        byte[] sanitized = preprocessInlineImages(contentBytes);
        PdfContentParser parser = new PdfContentParser(new PRTokeniser(sanitized));
        List<PdfObject> operands = new ArrayList<>();
        while (true) {
            List<PdfObject> parsed;
            try {
                parsed = parser.parse(operands);
            } catch (IOException | RuntimeException e) {
                // Malformed token sequence (e.g. unbalanced dict, unknown construct).
                // Stop processing rather than abort the whole page rendering.
                LOG.log(Level.FINE, "Aborting content stream early due to: {0}", e);
                return;
            }
            if (parsed.isEmpty()) {
                return;
            }
            PdfLiteral op = (PdfLiteral) parsed.get(parsed.size() - 1);
            try {
                dispatch(op.toString(), parsed);
            } catch (RuntimeException e) {
                // A malformed operator must not abort the whole page; log for diagnostics.
                LOG.log(Level.FINE, "Skipping operator ''{0}'' due to: {1}",
                        new Object[]{op, e});
            }
        }
    }

    /**
     * Walks {@code content} looking for inline-image blocks ({@code BI ... ID ... EI})
     * and rewrites each one into a synthetic {@code /name Do} invocation, with the decoded
     * pixel data stashed in {@link #inlineImages}. Blocks that can't be decoded are
     * dropped from the stream so the rest of the page still parses.
     *
     * <p>{@code PdfContentParser} has no native inline-image handling: the raw image
     * bytes between {@code ID} and {@code EI} would derail tokenization. Promoting them
     * to synthetic XObjects keeps the parser on a well-defined token grammar and lets
     * the rest of the renderer treat them exactly like any other image XObject.</p>
     */
    private byte[] preprocessInlineImages(byte[] content) {
        if (content == null || content.length == 0) {
            return content;
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream(content.length);
        int i = 0;
        while (i < content.length) {
            int biStart = findToken(content, i, 'B', 'I');
            if (biStart < 0) {
                out.write(content, i, content.length - i);
                break;
            }
            out.write(content, i, biStart - i);
            int idStart = findToken(content, biStart + 2, 'I', 'D');
            if (idStart < 0) {
                break;
            }
            // Dictionary tokens sit between "BI" (exclusive) and "ID" (exclusive).
            byte[] header = Arrays.copyOfRange(content, biStart + 2, idStart);
            int dataStart = idStart + 3; // skip "ID" + one whitespace byte
            int dataEnd = locateInlineImageDataEnd(content, dataStart, header);
            int eiEnd;
            if (dataEnd < 0) {
                // Couldn't locate a valid data end (e.g. malformed JPEG). Drop the
                // rest of the stream rather than risk a runaway parser.
                break;
            }
            // dataEnd points at the whitespace byte before "EI"; advance past "EI".
            eiEnd = dataEnd + 3;
            byte[] data = dataEnd > dataStart
                    ? Arrays.copyOfRange(content, dataStart, dataEnd)
                    : new byte[0];
            String synthName = registerInlineImage(header, data);
            if (synthName != null) {
                String invocation = " /" + synthName + " Do ";
                byte[] subst = invocation.getBytes(StandardCharsets.ISO_8859_1);
                out.write(subst, 0, subst.length);
            }
            i = eiEnd;
        }
        return out.toByteArray();
    }

    /**
     * Parses an inline-image header and decodes the image data, registering the result in
     * {@link #inlineImages}. Returns the synthetic XObject name to substitute into the
     * content stream, or {@code null} if the image couldn't be decoded.
     */
    private String registerInlineImage(byte[] header, byte[] data) {
        try {
            InlineImageHeader hdr = InlineImageHeader.parse(header);
            if (hdr.width <= 0 || hdr.height <= 0) {
                return null;
            }
            // For non-JPEG paths we need a known component count to size the raster;
            // JPEG decode goes through ImageIO which figures it out from the stream.
            if (!hdr.isJpeg() && hdr.components <= 0) {
                return null;
            }
            byte[] decoded = decodeInlineImageData(data, hdr);
            if (decoded == null) {
                return null;
            }
            BufferedImage img = buildImageForComponents(decoded, hdr.width, hdr.height, hdr.components);
            if (img == null) {
                return null;
            }
            String name = INLINE_IMAGE_PREFIX + inlineImages.size();
            inlineImages.put(name, img);
            return name;
        } catch (IOException | RuntimeException e) {
            LOG.log(Level.FINE, "Skipping inline image: {0}", e);
            return null;
        }
    }

    private static byte[] decodeInlineImageData(byte[] data, InlineImageHeader hdr) throws IOException {
        if (hdr.isJpeg()) {
            // ImageIO consumes the JPEG end-to-end; let the caller route via buildJpegInline.
            return data;
        }
        if (hdr.isFlate()) {
            return PdfReader.FlateDecode(data);
        }
        if (hdr.filter == null) {
            return data;
        }
        return null; // Unsupported filter (CCITT, LZW, ...) -- skip.
    }

    private BufferedImage buildImageForComponents(byte[] decoded, int width, int height, int components)
            throws IOException {
        // Synthetic JPEG path: hdr told us to defer to ImageIO.
        if (components == 0) {
            return ImageIO.read(new ByteArrayInputStream(decoded));
        }
        int rowBytes = width * components;
        if (decoded.length < rowBytes * height) {
            return null;
        }
        switch (components) {
            case 1:
                return buildGrayImage(decoded, width, height);
            case 3:
                return buildRgbImage(decoded, width, height);
            case 4:
                return buildCmykImage(decoded, width, height);
            default:
                return null;
        }
    }

    /**
     * Parsed form of an inline-image header dictionary. Only the entries the renderer
     * needs are extracted; everything else is ignored.
     */
    private static final class InlineImageHeader {
        int width;
        int height;
        int bitsPerComponent = 8;
        int components;
        String filter; // null = no filter
        boolean jpeg;

        boolean isJpeg() {
            return jpeg;
        }

        boolean isFlate() {
            return "FlateDecode".equals(filter) || "Fl".equals(filter);
        }

        static InlineImageHeader parse(byte[] header) {
            InlineImageHeader h = new InlineImageHeader();
            List<String> tokens = tokenizeHeader(header);
            for (int i = 0; i + 1 < tokens.size(); i += 2) {
                String key = tokens.get(i);
                String value = tokens.get(i + 1);
                if (!key.startsWith("/")) {
                    return h;
                }
                applyHeaderEntry(h, key.substring(1), value);
            }
            return h;
        }

        private static void applyHeaderEntry(InlineImageHeader h, String key, String value) {
            switch (key) {
                case "W":
                case "Width":
                    h.width = parseIntSafe(value);
                    break;
                case "H":
                case "Height":
                    h.height = parseIntSafe(value);
                    break;
                case "BPC":
                case "BitsPerComponent":
                    h.bitsPerComponent = parseIntSafe(value);
                    break;
                case "CS":
                case "ColorSpace":
                    h.components = componentsForAbbreviatedColorSpace(value);
                    break;
                case "F":
                case "Filter":
                    h.filter = stripLeadingSlash(value);
                    if ("DCT".equals(h.filter) || "DCTDecode".equals(h.filter)
                            || "JPXDecode".equals(h.filter)) {
                        h.jpeg = true;
                        h.components = 0; // signals "let ImageIO decide"
                    }
                    break;
                default:
                    // Ignored: Decode, Mask, ImageMask, Interpolate, DecodeParms, ...
                    break;
            }
        }

        private static int parseIntSafe(String s) {
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                return 0;
            }
        }

        private static String stripLeadingSlash(String s) {
            return s.startsWith("/") ? s.substring(1) : s;
        }

        private static int componentsForAbbreviatedColorSpace(String value) {
            String name = stripLeadingSlash(value);
            switch (name) {
                case "G":
                case "DeviceGray":
                case "CalGray":
                    return 1;
                case "RGB":
                case "DeviceRGB":
                case "CalRGB":
                    return 3;
                case "CMYK":
                case "DeviceCMYK":
                    return 4;
                default:
                    return 0;
            }
        }
    }

    /**
     * Splits an inline-image header dictionary into whitespace-separated tokens.
     * Arrays in the source (rare but legal) are kept as single bracketed tokens so the
     * key/value pairing stays consistent.
     */
    private static List<String> tokenizeHeader(byte[] header) {
        List<String> tokens = new ArrayList<>();
        int i = 0;
        while (i < header.length) {
            while (i < header.length && isPdfWhitespace(header[i])) {
                i++;
            }
            if (i >= header.length) {
                break;
            }
            int start = i;
            if (header[i] == '[') {
                while (i < header.length && header[i] != ']') {
                    i++;
                }
                if (i < header.length) {
                    i++;
                }
            } else {
                while (i < header.length && !isPdfWhitespace(header[i])) {
                    i++;
                }
            }
            tokens.add(new String(header, start, i - start, StandardCharsets.ISO_8859_1));
        }
        return tokens;
    }

    /**
     * Finds the offset of a two-byte token (e.g. {@code "BI"}) bounded by whitespace.
     */
    private static int findToken(byte[] buf, int from, char c1, char c2) {
        for (int i = from; i < buf.length - 1; i++) {
            if (buf[i] != c1 || buf[i + 1] != c2) {
                continue;
            }
            boolean leftOk = i == 0 || isPdfWhitespace(buf[i - 1]);
            boolean rightOk = i + 2 >= buf.length || isPdfWhitespace(buf[i + 2]);
            if (leftOk && rightOk) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the index of the whitespace byte preceding the closing {@code EI} for the
     * inline image whose data starts at {@code dataStart}, using filter-aware framing.
     * For JPEG inline images (Filter = DCT / DCTDecode) we scan for the JPEG end-of-image
     * marker {@code FFD9} rather than a whitespace-bounded {@code EI}, since the JPEG
     * payload routinely contains byte sequences that look like {@code EI} by accident.
     * For everything else we fall back to whitespace-bounded {@code EI} search.
     * Returns -1 when no usable end can be found.
     */
    private static int locateInlineImageDataEnd(byte[] buf, int dataStart, byte[] header) {
        if (isJpegInlineHeader(header)) {
            int eoi = findJpegEndOfImage(buf, dataStart);
            if (eoi >= 0) {
                // After FFD9 there should be a single whitespace byte and then "EI".
                int p = eoi + 2;
                while (p < buf.length && isPdfWhitespace(buf[p])) {
                    p++;
                }
                if (p + 1 < buf.length && buf[p] == 'E' && buf[p + 1] == 'I') {
                    return eoi + 2;
                }
                // EI marker not found right after EOI; still treat EOI as end of data.
                return eoi + 2;
            }
        }
        int eiEnd = findEndInlineImage(buf, dataStart);
        return eiEnd < 0 ? -1 : eiEnd - 3;
    }

    /**
     * Cheaply inspects an already-extracted inline-image header for a DCT filter entry
     * without going through the full token parser. Used by the framing detector before
     * we commit to a decode strategy.
     */
    private static boolean isJpegInlineHeader(byte[] header) {
        String s = new String(header, StandardCharsets.ISO_8859_1);
        return s.contains("/DCT") || s.contains("/DCTDecode") || s.contains("/JPXDecode");
    }

    /**
     * Returns the index of the {@code FF} byte of the JPEG end-of-image marker
     * ({@code FFD9}) at or after {@code from}, or -1 if not found.
     */
    private static int findJpegEndOfImage(byte[] buf, int from) {
        for (int i = from; i < buf.length - 1; i++) {
            if (buf[i] == (byte) 0xFF && buf[i + 1] == (byte) 0xD9) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Returns the index just past the closing {@code EI} of an inline image starting after {@code BI}.
     */
    private static int findEndInlineImage(byte[] buf, int from) {
        for (int i = from; i < buf.length - 1; i++) {
            if (buf[i] != 'E' || buf[i + 1] != 'I') {
                continue;
            }
            boolean leftOk = i > 0 && isPdfWhitespace(buf[i - 1]);
            boolean rightOk = i + 2 >= buf.length || isPdfWhitespace(buf[i + 2]);
            if (leftOk && rightOk) {
                return i + 2;
            }
        }
        return -1;
    }

    private static boolean isPdfWhitespace(byte b) {
        return b == ' ' || b == '\t' || b == '\n' || b == '\r' || b == '\f' || b == 0;
    }

    /**
     * Dispatches one operator. Operands include the trailing operator literal at index size-1.
     */
    private void dispatch(String op, List<PdfObject> operands) throws IOException {
        switch (op) {
            // --- Graphics state ---
            case "q":
                stateStack.push(state);
                ctmStack.push(g2.getTransform());
                clipStack.push(g2.getClip());
                state = new GState(state);
                break;
            case "Q":
                if (!stateStack.isEmpty()) {
                    state = stateStack.pop();
                    g2.setTransform(ctmStack.pop());
                    g2.setClip(clipStack.pop());
                }
                break;
            case "cm":
                concatCtm(num(operands, 0), num(operands, 1), num(operands, 2),
                        num(operands, 3), num(operands, 4), num(operands, 5));
                break;

            // --- Line style ---
            case "w":
                state.lineWidth = num(operands, 0);
                break;
            case "J":
                state.lineCap = (int) num(operands, 0);
                break;
            case "j":
                state.lineJoin = (int) num(operands, 0);
                break;
            case "M":
                state.miterLimit = Math.max(num(operands, 0), 1f);
                break;
            case "d":
                applyDashPattern((PdfArray) operands.get(0), num(operands, 1));
                break;
            case "i":
                // Flatness tolerance: Java2D handles flattening internally.
                break;

            // --- Extended graphics state ---
            case "gs":
                if (operands.get(0) instanceof PdfName extName) {
                    applyExtGState(extName.toString().substring(1));
                }
                break;

            // --- Colors ---
            case "g":
                state.fillColorSpace = ColorSpaceKind.GRAY;
                state.fillColor = applyAlpha(gray(num(operands, 0)), state.fillAlpha);
                break;
            case "G":
                state.strokeColorSpace = ColorSpaceKind.GRAY;
                state.strokeColor = applyAlpha(gray(num(operands, 0)), state.strokeAlpha);
                break;
            case "rg":
                state.fillColorSpace = ColorSpaceKind.RGB;
                state.fillColor = applyAlpha(
                        rgb(num(operands, 0), num(operands, 1), num(operands, 2)), state.fillAlpha);
                break;
            case "RG":
                state.strokeColorSpace = ColorSpaceKind.RGB;
                state.strokeColor = applyAlpha(
                        rgb(num(operands, 0), num(operands, 1), num(operands, 2)), state.strokeAlpha);
                break;
            case "k":
                state.fillColorSpace = ColorSpaceKind.CMYK;
                state.fillColor = applyAlpha(
                        cmyk(num(operands, 0), num(operands, 1), num(operands, 2), num(operands, 3)),
                        state.fillAlpha);
                break;
            case "K":
                state.strokeColorSpace = ColorSpaceKind.CMYK;
                state.strokeColor = applyAlpha(
                        cmyk(num(operands, 0), num(operands, 1), num(operands, 2), num(operands, 3)),
                        state.strokeAlpha);
                break;
            case "cs":
                state.fillColorSpace = colorSpaceFromName(operands.get(0));
                state.fillColor = applyAlpha(defaultColorFor(state.fillColorSpace), state.fillAlpha);
                break;
            case "CS":
                state.strokeColorSpace = colorSpaceFromName(operands.get(0));
                state.strokeColor = applyAlpha(defaultColorFor(state.strokeColorSpace), state.strokeAlpha);
                break;
            case "sc":
            case "scn":
                state.fillColor = applyAlpha(colorFromOperands(state.fillColorSpace, operands), state.fillAlpha);
                break;
            case "SC":
            case "SCN":
                state.strokeColor = applyAlpha(colorFromOperands(state.strokeColorSpace, operands), state.strokeAlpha);
                break;

            // --- Path construction ---
            case "m":
                pathCurX = num(operands, 0);
                pathCurY = num(operands, 1);
                pathStartX = pathCurX;
                pathStartY = pathCurY;
                currentPath.moveTo(pathCurX, pathCurY);
                break;
            case "l":
                pathCurX = num(operands, 0);
                pathCurY = num(operands, 1);
                currentPath.lineTo(pathCurX, pathCurY);
                break;
            case "c":
                currentPath.curveTo(num(operands, 0), num(operands, 1),
                        num(operands, 2), num(operands, 3),
                        num(operands, 4), num(operands, 5));
                pathCurX = num(operands, 4);
                pathCurY = num(operands, 5);
                break;
            case "v":
                currentPath.curveTo(pathCurX, pathCurY,
                        num(operands, 0), num(operands, 1),
                        num(operands, 2), num(operands, 3));
                pathCurX = num(operands, 2);
                pathCurY = num(operands, 3);
                break;
            case "y":
                currentPath.curveTo(num(operands, 0), num(operands, 1),
                        num(operands, 2), num(operands, 3),
                        num(operands, 2), num(operands, 3));
                pathCurX = num(operands, 2);
                pathCurY = num(operands, 3);
                break;
            case "re": {
                float x = num(operands, 0);
                float y = num(operands, 1);
                float w = num(operands, 2);
                float h = num(operands, 3);
                currentPath.moveTo(x, y);
                currentPath.lineTo(x + w, y);
                currentPath.lineTo(x + w, y + h);
                currentPath.lineTo(x, y + h);
                currentPath.closePath();
                pathStartX = x;
                pathStartY = y;
                pathCurX = x;
                pathCurY = y;
                break;
            }
            case "h":
                currentPath.closePath();
                pathCurX = pathStartX;
                pathCurY = pathStartY;
                break;

            // --- Path painting ---
            case "S":
                strokePath();
                resetPath();
                break;
            case "s":
                currentPath.closePath();
                strokePath();
                resetPath();
                break;
            case "f":
            case "F":
                fillPath(Path2D.WIND_NON_ZERO);
                resetPath();
                break;
            case "f*":
                fillPath(Path2D.WIND_EVEN_ODD);
                resetPath();
                break;
            case "B":
                fillPath(Path2D.WIND_NON_ZERO);
                strokePath();
                resetPath();
                break;
            case "B*":
                fillPath(Path2D.WIND_EVEN_ODD);
                strokePath();
                resetPath();
                break;
            case "b":
                currentPath.closePath();
                fillPath(Path2D.WIND_NON_ZERO);
                strokePath();
                resetPath();
                break;
            case "b*":
                currentPath.closePath();
                fillPath(Path2D.WIND_EVEN_ODD);
                strokePath();
                resetPath();
                break;
            case "n":
                resetPath();
                break;

            // --- Clipping ---
            case "W":
                state.pendingClipRule = Path2D.WIND_NON_ZERO;
                state.hasPendingClip = true;
                break;
            case "W*":
                state.pendingClipRule = Path2D.WIND_EVEN_ODD;
                state.hasPendingClip = true;
                break;

            // --- Text state ---
            case "BT":
                inTextObject = true;
                textMatrix = new AffineTransform();
                textLineMatrix = new AffineTransform();
                break;
            case "ET":
                inTextObject = false;
                textMatrix = null;
                textLineMatrix = null;
                break;
            case "Tf": {
                PdfObject nameOperand = operands.get(0);
                if (nameOperand instanceof PdfName name) {
                    String fontKey = name.toString().substring(1); // strip leading '/'
                    state.font = lookupFont(fontKey);
                }
                state.fontSize = num(operands, 1);
                break;
            }
            case "Tc":
                state.charSpacing = num(operands, 0);
                break;
            case "Tw":
                state.wordSpacing = num(operands, 0);
                break;
            case "TL":
                state.leading = num(operands, 0);
                break;
            case "Tz":
                state.horizontalScaling = num(operands, 0) / 100f;
                break;
            case "Ts":
                state.textRise = num(operands, 0);
                break;
            case "Td":
                textMoveTo(num(operands, 0), num(operands, 1));
                break;
            case "TD":
                state.leading = -num(operands, 1);
                textMoveTo(num(operands, 0), num(operands, 1));
                break;
            case "Tm":
                if (textMatrix != null) {
                    textMatrix.setTransform(num(operands, 0), num(operands, 1),
                            num(operands, 2), num(operands, 3),
                            num(operands, 4), num(operands, 5));
                    textLineMatrix.setTransform(textMatrix);
                }
                break;
            case "T*":
                textMoveTo(0, -state.leading);
                break;

            // --- Text showing ---
            case "Tj":
                showText(decodeString((PdfString) operands.get(0)));
                break;
            case "TJ":
                showTextArray((PdfArray) operands.get(0));
                break;
            case "'":
                textMoveTo(0, -state.leading);
                showText(decodeString((PdfString) operands.get(0)));
                break;
            case "\"":
                state.wordSpacing = num(operands, 0);
                state.charSpacing = num(operands, 1);
                textMoveTo(0, -state.leading);
                showText(decodeString((PdfString) operands.get(2)));
                break;

            // --- XObject invocation ---
            case "Do":
                if (operands.get(0) instanceof PdfName xobjName) {
                    doXObject(xobjName.toString().substring(1));
                }
                break;

            // --- Marked content / compatibility (parsed, no rendering effect) ---
            case "BMC":
            case "BDC":
            case "EMC":
            case "MP":
            case "DP":
            case "BX":
            case "EX":
                break;

            default:
                // Unsupported operator: ignore quietly so partial pages still render.
                break;
        }
    }

    // ---------- Path painting helpers ----------

    private void strokePath() {
        g2.setColor(state.strokeColor);
        g2.setStroke(new BasicStroke(
                Math.max(state.lineWidth, 0.001f),
                state.lineCap, state.lineJoin, state.miterLimit,
                state.dashPattern, state.dashPhase));
        g2.draw(currentPath);
    }

    private void fillPath(int windingRule) {
        Path2D.Float p = (Path2D.Float) currentPath.clone();
        p.setWindingRule(windingRule);
        g2.setColor(state.fillColor);
        g2.fill(p);
    }

    private void resetPath() {
        if (state.hasPendingClip) {
            Path2D.Float clip = (Path2D.Float) currentPath.clone();
            clip.setWindingRule(state.pendingClipRule);
            Shape existing = g2.getClip();
            if (existing == null) {
                g2.setClip(clip);
            } else {
                g2.clip(clip);
            }
            state.hasPendingClip = false;
        }
        currentPath = new Path2D.Float();
    }

    private void applyDashPattern(PdfArray array, float phase) {
        if (array == null || array.size() == 0) {
            state.dashPattern = null;
            state.dashPhase = 0f;
            return;
        }
        float[] dash = new float[array.size()];
        boolean allZero = true;
        for (int i = 0; i < array.size(); i++) {
            PdfObject e = array.getPdfObject(i);
            dash[i] = e instanceof PdfNumber n ? Math.max(n.floatValue(), 0f) : 0f;
            if (dash[i] > 0f) {
                allZero = false;
            }
        }
        if (allZero) {
            state.dashPattern = null;
            state.dashPhase = 0f;
        } else {
            state.dashPattern = dash;
            state.dashPhase = phase;
        }
    }

    private void applyExtGState(String name) {
        PdfDictionary dict = resolveExtGStateDict(name);
        if (dict == null) {
            return;
        }
        applyExtGStateAlpha(dict);
        applyExtGStateLineStyle(dict);
    }

    private PdfDictionary resolveExtGStateDict(String name) {
        if (resources == null) {
            return null;
        }
        PdfDictionary gsResources = resources.getAsDict(PdfName.EXTGSTATE);
        if (gsResources == null) {
            return null;
        }
        PdfObject obj = gsResources.get(new PdfName(name));
        PdfObject direct = obj instanceof PRIndirectReference ref ? PdfReader.getPdfObject(ref) : obj;
        return direct instanceof PdfDictionary d ? d : null;
    }

    private void applyExtGStateAlpha(PdfDictionary dict) {
        PdfNumber ca = dict.getAsNumber(PdfName.ca);
        if (ca != null) {
            state.fillAlpha = clamp01(ca.floatValue());
            state.fillColor = applyAlpha(state.fillColor, state.fillAlpha);
        }
        PdfNumber upperCA = dict.getAsNumber(PdfName.CA);
        if (upperCA != null) {
            state.strokeAlpha = clamp01(upperCA.floatValue());
            state.strokeColor = applyAlpha(state.strokeColor, state.strokeAlpha);
        }
    }

    private void applyExtGStateLineStyle(PdfDictionary dict) {
        PdfNumber lw = dict.getAsNumber(EXTGS_LW);
        if (lw != null) {
            state.lineWidth = lw.floatValue();
        }
        PdfNumber ml = dict.getAsNumber(EXTGS_ML);
        if (ml != null) {
            state.miterLimit = Math.max(ml.floatValue(), 1f);
        }
        PdfNumber lc = dict.getAsNumber(EXTGS_LC);
        if (lc != null) {
            state.lineCap = lc.intValue();
        }
        PdfNumber lj = dict.getAsNumber(EXTGS_LJ);
        if (lj != null) {
            state.lineJoin = lj.intValue();
        }
    }

    private void concatCtm(float a, float b, float c, float d, float e, float f) {
        AffineTransform m = new AffineTransform(a, b, c, d, e, f);
        g2.transform(m);
    }

    // ---------- XObject helpers ----------

    /**
     * Resolves an XObject by its resource name and dispatches to the right
     * sub-renderer (Form or Image). Unknown subtypes are silently ignored.
     */
    private void doXObject(String name) {
        BufferedImage inline = inlineImages.get(name);
        if (inline != null) {
            drawUnitSquareImage(inline);
            return;
        }
        if (resources == null) {
            return;
        }
        PdfDictionary xobjects = resources.getAsDict(PdfName.XOBJECT);
        if (xobjects == null) {
            return;
        }
        PdfObject ref = xobjects.get(new PdfName(name));
        PdfObject resolved = ref instanceof PRIndirectReference ind
                ? PdfReader.getPdfObject(ind) : ref;
        if (!(resolved instanceof PRStream stream)) {
            return;
        }
        PdfName subtype = stream.getAsName(PdfName.SUBTYPE);
        if (PdfName.FORM.equals(subtype)) {
            renderForm(stream);
        } else if (PdfName.IMAGE.equals(subtype)) {
            renderImage(stream);
        }
    }

    /**
     * Renders a Form XObject by parsing its content stream recursively, with
     * the form's own resources (falling back to the parent page's) and any
     * {@code /Matrix} entry applied on top of the current CTM. The current
     * graphics state and CTM are saved and restored around the call so the
     * form's content can't leak out.
     */
    private void renderForm(PRStream form) {
        AffineTransform savedTx = g2.getTransform();
        Shape savedClip = g2.getClip();
        GState savedState = state;
        try {
            state = new GState(state);
            PdfArray matrix = form.getAsArray(PdfName.MATRIX);
            if (matrix != null && matrix.size() >= 6) {
                AffineTransform m = new AffineTransform(
                        floatAt(matrix, 0), floatAt(matrix, 1),
                        floatAt(matrix, 2), floatAt(matrix, 3),
                        floatAt(matrix, 4), floatAt(matrix, 5));
                g2.transform(m);
            }
            PdfArray bbox = form.getAsArray(PdfName.BBOX);
            if (bbox != null && bbox.size() >= 4) {
                float x = floatAt(bbox, 0);
                float y = floatAt(bbox, 1);
                float w = floatAt(bbox, 2) - x;
                float h = floatAt(bbox, 3) - y;
                if (w > 0 && h > 0) {
                    g2.clip(new Rectangle2D.Float(x, y, w, h));
                }
            }
            PdfDictionary formResources = form.getAsDict(PdfName.RESOURCES);
            PdfDictionary effective = formResources != null ? formResources : resources;
            OpenPdfCorePageRenderer nested = new OpenPdfCorePageRenderer(g2, effective);
            nested.state = state;
            byte[] body = PdfReader.getStreamBytes(form);
            nested.processContent(body);
        } catch (IOException | RuntimeException e) {
            LOG.log(Level.FINE, "Skipping Form XObject due to: {0}", e);
        } finally {
            state = savedState;
            g2.setTransform(savedTx);
            g2.setClip(savedClip);
        }
    }

    /**
     * Renders an Image XObject under the current CTM. The image occupies the
     * unit square (0,0)-(1,1) in user space, per the PDF spec, with the CTM
     * supplying the actual placement/size.
     */
    private void renderImage(PRStream image) {
        BufferedImage img = decodeImage(image);
        if (img == null) {
            return;
        }
        drawUnitSquareImage(img);
    }

    /**
     * Draws a decoded image into the standard PDF image area: the (0,0)-(1,1) unit
     * square in user space, with the CTM supplying placement/size. Honors the current
     * fill alpha; saves and restores the {@link Graphics2D} transform.
     */
    private void drawUnitSquareImage(BufferedImage img) {
        AffineTransform saved = g2.getTransform();
        try {
            // PDF images map (0,0)-(1,1) in user space to the full image, with Y running up.
            // Java2D draws top-to-bottom, so we translate up by 1 and flip Y back.
            g2.translate(0, 1);
            g2.scale(1.0 / img.getWidth(), -1.0 / img.getHeight());
            Composite saveComposite = null;
            if (state.fillAlpha < 1f) {
                saveComposite = g2.getComposite();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, state.fillAlpha));
            }
            try {
                g2.drawImage(img, 0, 0, null);
            } finally {
                if (saveComposite != null) {
                    g2.setComposite(saveComposite);
                }
            }
        } finally {
            g2.setTransform(saved);
        }
    }

    private BufferedImage decodeImage(PRStream stream) {
        PdfNumber widthN = stream.getAsNumber(PdfName.WIDTH);
        PdfNumber heightN = stream.getAsNumber(PdfName.HEIGHT);
        if (widthN == null || heightN == null) {
            return null;
        }
        int width = widthN.intValue();
        int height = heightN.intValue();
        if (width <= 0 || height <= 0) {
            return null;
        }
        PdfObject filterObj = stream.get(PdfName.FILTER);
        if (hasFilter(filterObj, PdfName.DCTDECODE) || hasFilter(filterObj, PdfName.JPXDECODE)) {
            return decodeViaImageIO(stream);
        }
        PdfArray indexedCs = asIndexedColorSpace(stream.get(PdfName.COLORSPACE));
        if (indexedCs != null) {
            return decodeIndexedImage(stream, width, height, indexedCs);
        }
        return decodeRawRaster(stream, width, height);
    }

    /**
     * Returns the {@code [/Indexed base hival lookup]} array if {@code csObj} (possibly
     * indirect) is an indexed color space; {@code null} otherwise.
     */
    private static PdfArray asIndexedColorSpace(PdfObject csObj) {
        PdfObject direct = csObj instanceof PRIndirectReference ind
                ? PdfReader.getPdfObject(ind) : csObj;
        if (!(direct instanceof PdfArray arr) || arr.size() < 4) {
            return null;
        }
        PdfObject head = arr.getDirectObject(0);
        return CS_INDEXED.equals(head) ? arr : null;
    }

    /**
     * Decodes an Indexed image XObject: each pixel is a 1-byte palette index, expanded
     * via the lookup table into pixel values in the base color space. Supports 8-bit
     * indices and DeviceGray / DeviceRGB / DeviceCMYK base color spaces (the overwhelming
     * majority of indexed images produced by PNG-to-PDF conversion).
     */
    private BufferedImage decodeIndexedImage(PRStream stream, int width, int height, PdfArray indexedCs) {
        try {
            PdfNumber bpcN = stream.getAsNumber(PdfName.BITSPERCOMPONENT);
            int bpc = bpcN == null ? 8 : bpcN.intValue();
            if (bpc != 8) {
                // Bit-packed indices (1/2/4-bit) are legal but rare; not yet supported.
                return null;
            }
            int baseComponents = imageComponents(indexedCs.getDirectObject(1));
            if (baseComponents == 0) {
                return null;
            }
            byte[] lookup = readIndexedLookup(indexedCs.getDirectObject(3));
            if (lookup == null || lookup.length == 0) {
                return null;
            }
            byte[] indices = PdfReader.getStreamBytes(stream);
            int pixels = width * height;
            if (indices.length < pixels) {
                return null;
            }
            byte[] expanded = new byte[pixels * baseComponents];
            int maxLookupIdx = lookup.length - baseComponents;
            for (int p = 0; p < pixels; p++) {
                int paletteOffset = Math.min((indices[p] & 0xFF) * baseComponents, Math.max(maxLookupIdx, 0));
                System.arraycopy(lookup, paletteOffset, expanded, p * baseComponents, baseComponents);
            }
            switch (baseComponents) {
                case 1:
                    return buildGrayImage(expanded, width, height);
                case 3:
                    return buildRgbImage(expanded, width, height);
                case 4:
                    return buildCmykImage(expanded, width, height);
                default:
                    return null;
            }
        } catch (IOException | RuntimeException e) {
            LOG.log(Level.FINE, "Skipping indexed image due to: {0}", e);
            return null;
        }
    }

    /**
     * Reads the lookup table of an Indexed color space, which may appear as either a
     * {@code PdfString} (containing the raw palette bytes) or a {@code PRStream} (whose
     * decoded content is the palette).
     */
    private static byte[] readIndexedLookup(PdfObject lookupObj) throws IOException {
        if (lookupObj instanceof PdfString s) {
            return s.getBytes();
        }
        if (lookupObj instanceof PRStream stream) {
            return PdfReader.getStreamBytes(stream);
        }
        return null;
    }

    private static boolean hasFilter(PdfObject filterObj, PdfName name) {
        if (filterObj == null) {
            return false;
        }
        if (filterObj instanceof PdfName n) {
            return n.equals(name);
        }
        if (filterObj instanceof PdfArray arr) {
            for (PdfObject e : arr.getElements()) {
                PdfObject direct = e instanceof PRIndirectReference ref
                        ? PdfReader.getPdfObject(ref) : e;
                if (name.equals(direct)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Decodes DCT/JPX-encoded image streams via the JRE's {@link ImageIO}.
     */
    private BufferedImage decodeViaImageIO(PRStream stream) {
        try {
            byte[] raw = PdfReader.getStreamBytesRaw(stream);
            BufferedImage img = ImageIO.read(new ByteArrayInputStream(raw));
            if (img != null) {
                return img;
            }
            // Some PDFs apply additional filters before DCTDecode; fall back to fully decoded bytes.
            byte[] decoded = PdfReader.getStreamBytes(stream);
            return ImageIO.read(new ByteArrayInputStream(decoded));
        } catch (IOException | RuntimeException e) {
            LOG.log(Level.FINE, "Skipping JPEG/JPX image XObject due to: {0}", e);
            return null;
        }
    }

    /**
     * Decodes an uncompressed / Flate-decoded image XObject into a {@link BufferedImage}.
     * Supports 8-bit DeviceGray, DeviceRGB and DeviceCMYK; bit depths and color spaces
     * outside that set yield {@code null}.
     */
    private BufferedImage decodeRawRaster(PRStream stream, int width, int height) {
        try {
            PdfNumber bpcN = stream.getAsNumber(PdfName.BITSPERCOMPONENT);
            int bpc = bpcN == null ? 8 : bpcN.intValue();
            if (bpc != 8) {
                return null;
            }
            int components = imageComponents(stream.get(PdfName.COLORSPACE));
            if (components <= 0) {
                return null;
            }
            byte[] decoded = PdfReader.getStreamBytes(stream);
            int rowBytes = width * components;
            int expected = rowBytes * height;
            if (decoded.length < expected) {
                return null;
            }
            switch (components) {
                case 1:
                    return buildGrayImage(decoded, width, height);
                case 3:
                    return buildRgbImage(decoded, width, height);
                case 4:
                    return buildCmykImage(decoded, width, height);
                default:
                    return null;
            }
        } catch (IOException | RuntimeException e) {
            LOG.log(Level.FINE, "Skipping raw image XObject due to: {0}", e);
            return null;
        }
    }

    /**
     * Returns the number of color components for a {@code /ColorSpace} entry, or 0 if unsupported.
     */
    private static int imageComponents(PdfObject csObj) {
        PdfObject direct = csObj instanceof PRIndirectReference ind ? PdfReader.getPdfObject(ind) : csObj;
        if (direct instanceof PdfName n) {
            return componentsForNamedColorSpace(n);
        }
        if (direct instanceof PdfArray arr) {
            return componentsForArrayColorSpace(arr);
        }
        return 0;
    }

    private static int componentsForNamedColorSpace(PdfName name) {
        if (DEVICE_GRAY_NAMES.contains(name)) {
            return 1;
        }
        if (DEVICE_RGB_NAMES.contains(name)) {
            return 3;
        }
        if (DEVICE_CMYK_NAMES.contains(name)) {
            return 4;
        }
        return 0;
    }

    private static int componentsForArrayColorSpace(PdfArray arr) {
        if (arr.size() < 1) {
            return 0;
        }
        PdfObject head = arr.getDirectObject(0);
        if (CS_ICC_BASED.equals(head)) {
            return iccBasedComponents(arr);
        }
        if (CS_CAL_GRAY.equals(head)) {
            return 1;
        }
        if (CS_CAL_RGB.equals(head) || CS_LAB.equals(head)) {
            return 3;
        }
        return 0;
    }

    private static int iccBasedComponents(PdfArray arr) {
        if (arr.size() < 2) {
            return 0;
        }
        PdfObject paramsObj = arr.getDirectObject(1);
        if (!(paramsObj instanceof PdfDictionary params)) {
            return 0;
        }
        PdfNumber n = params.getAsNumber(CS_N);
        return n == null ? 0 : n.intValue();
    }

    private static BufferedImage buildGrayImage(byte[] data, int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        byte[] target = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
        System.arraycopy(data, 0, target, 0, Math.min(target.length, data.length));
        return img;
    }

    private static BufferedImage buildRgbImage(byte[] data, int width, int height) {
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        // PDF DeviceRGB stores R,G,B; BufferedImage.TYPE_3BYTE_BGR stores B,G,R.
        // Swap on the fly while copying so the image displays with correct colors.
        WritableRaster raster = img.getRaster();
        byte[] dst = ((DataBufferByte) raster.getDataBuffer()).getData();
        int pixels = width * height;
        for (int p = 0, di = 0, si = 0; p < pixels; p++, di += 3, si += 3) {
            dst[di] = data[si + 2];     // B
            dst[di + 1] = data[si + 1]; // G
            dst[di + 2] = data[si];     // R
        }
        return img;
    }

    private static BufferedImage buildCmykImage(byte[] data, int width, int height) {
        // Build an sRGB BufferedImage and approximate CMYK -> RGB per pixel,
        // since Java2D can't natively draw a 4-component CMYK raster.
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        byte[] dst = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
        int pixels = width * height;
        for (int p = 0, di = 0, si = 0; p < pixels; p++, di += 3, si += 4) {
            float c = (data[si] & 0xFF) / 255f;
            float m = (data[si + 1] & 0xFF) / 255f;
            float y = (data[si + 2] & 0xFF) / 255f;
            float k = (data[si + 3] & 0xFF) / 255f;
            float oneMinusK = 1f - k;
            dst[di] = (byte) Math.round((1f - y) * oneMinusK * 255f); // B
            dst[di + 1] = (byte) Math.round((1f - m) * oneMinusK * 255f); // G
            dst[di + 2] = (byte) Math.round((1f - c) * oneMinusK * 255f); // R
        }
        return img;
    }

    private static float floatAt(PdfArray arr, int idx) {
        PdfObject obj = arr.getPdfObject(idx);
        return obj instanceof PdfNumber n ? n.floatValue() : 0f;
    }

    // ---------- Text helpers ----------

    private void textMoveTo(float tx, float ty) {
        if (textLineMatrix == null) {
            return;
        }
        AffineTransform m = new AffineTransform(textLineMatrix);
        m.translate(tx, ty);
        textLineMatrix = m;
        textMatrix = new AffineTransform(m);
    }

    private void showText(String text) {
        if (!inTextObject || text == null || text.isEmpty()) {
            return;
        }
        Font awtFont = mapFont(state.font, state.fontSize);
        g2.setFont(awtFont);
        g2.setColor(state.fillColor);

        AffineTransform saved = g2.getTransform();
        try {
            // Text matrix maps text-space to user space; we then need a Y-flip
            // because Graphics2D's font baseline is drawn in image-Y orientation.
            g2.transform(textMatrix);
            g2.scale(state.horizontalScaling, 1.0);
            if (state.textRise != 0f) {
                g2.translate(0, state.textRise);
            }
            g2.scale(1, -1);
            g2.drawString(text, 0f, 0f);
        } finally {
            g2.setTransform(saved);
        }

        // Advance text matrix using actual PDF font widths + char/word spacing.
        float advance = computeTextAdvance(text);
        AffineTransform adv = new AffineTransform(textMatrix);
        adv.translate(advance, 0);
        textMatrix = adv;
    }

    private void showTextArray(PdfArray array) {
        for (PdfObject obj : array.getElements()) {
            if (obj instanceof PdfString s) {
                showText(decodeString(s));
            } else if (obj instanceof PdfNumber n) {
                // Negative number = leftward shift in thousandths of font size.
                float adj = n.floatValue();
                float shift = -adj / 1000f * state.fontSize * state.horizontalScaling;
                AffineTransform m = new AffineTransform(textMatrix);
                m.translate(shift, 0);
                textMatrix = m;
            }
        }
    }

    private String decodeString(PdfString raw) {
        if (state.font != null) {
            try {
                byte[] bytes = raw.getBytes();
                return state.font.decode(bytes, 0, bytes.length);
            } catch (RuntimeException ignored) {
                // Fall through to unicode fallback.
            }
        }
        return raw.toUnicodeString();
    }

    private float computeTextAdvance(String text) {
        float fontAdvance;
        if (state.font != null) {
            // Use the PDF font's own width table so inter-character spacing is accurate.
            fontAdvance = state.font.getWidthPoint(text, state.fontSize);
        } else {
            // No PDF font info: fall back to Java2D measurement (less accurate).
            return (float) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
        }
        int spaces = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == ' ') {
                spaces++;
            }
        }
        // PDF spec: tx = (fontWidth + Tc * n + Tw * spaces) * Th
        return (fontAdvance + state.charSpacing * text.length() + state.wordSpacing * spaces)
                * state.horizontalScaling;
    }

    private Font mapFont(CMapAwareDocumentFont docFont, float size) {
        float pointSize = Math.max(size, 0.1f);
        Font embedded = embeddedFontFor(docFont);
        if (embedded != null) {
            return embedded.deriveFont(pointSize);
        }
        return mapFontByName(docFont).deriveFont(pointSize);
    }

    /**
     * Tries to load the font program embedded in the PDF and turn it into a Java AWT
     * {@link Font}. Returns {@code null} for fonts that don't embed a TrueType / OpenType
     * program, or for which the program fails to parse &mdash; callers should fall back
     * to {@link #mapFontByName(CMapAwareDocumentFont)}.
     */
    private Font embeddedFontFor(CMapAwareDocumentFont docFont) {
        if (docFont == null) {
            return null;
        }
        PdfDictionary descriptor = docFont.getFontDescriptor();
        if (descriptor == null) {
            return null;
        }
        if (awtFontCache.containsKey(descriptor)) {
            return awtFontCache.get(descriptor); // may be null = previously failed to load
        }
        // Try TrueType first (FontFile2), then OpenType / CFF (FontFile3), then Type1 (FontFile).
        PRStream program = fontProgramStream(descriptor);
        if (program == null) {
            awtFontCache.put(descriptor, null);
            return null;
        }
        try {
            byte[] bytes = PdfReader.getStreamBytes(program);
            Font font = Font.createFont(Font.TRUETYPE_FONT, new ByteArrayInputStream(bytes));
            awtFontCache.put(descriptor, font);
            return font;
        } catch (FontFormatException | IOException | RuntimeException e) {
            LOG.log(Level.FINE, "Falling back from embedded font program due to: {0}", e);
            awtFontCache.put(descriptor, null);
            return null;
        }
    }

    private static PRStream fontProgramStream(PdfDictionary descriptor) {
        for (PdfName key : EMBEDDED_FONT_KEYS) {
            PdfObject raw = descriptor.get(key);
            PdfObject direct = raw instanceof PRIndirectReference ref
                    ? PdfReader.getPdfObject(ref) : raw;
            if (direct instanceof PRStream stream) {
                return stream;
            }
        }
        return null;
    }

    /**
     * Maps a PDF font to a generic Java2D font family using the PostScript font name as
     * a hint. This is the last-resort path used when the PDF doesn't embed a font program
     * or the program can't be loaded.
     */
    private static Font mapFontByName(CMapAwareDocumentFont docFont) {
        String family = Font.SERIF;
        int style = Font.PLAIN;
        if (docFont != null) {
            String name = docFont.getPostscriptFontName();
            if (name != null) {
                String lower = name.toLowerCase();
                if (lower.contains("mono") || lower.contains("courier")) {
                    family = Font.MONOSPACED;
                } else if (lower.contains("sans") || lower.contains("helvetica")
                        || lower.contains("arial")) {
                    family = Font.SANS_SERIF;
                }
                if (lower.contains("bold")) {
                    style |= Font.BOLD;
                }
                if (lower.contains("italic") || lower.contains("oblique")) {
                    style |= Font.ITALIC;
                }
            }
        }
        return new Font(family, style, 1);
    }

    private CMapAwareDocumentFont lookupFont(String name) {
        if (resources == null) {
            return null;
        }
        CMapAwareDocumentFont cached = fontCache.get(name);
        if (cached != null) {
            return cached;
        }
        PdfDictionary fontDict = resources.getAsDict(PdfName.FONT);
        if (fontDict == null) {
            return null;
        }
        PdfObject ref = fontDict.get(new PdfName(name));
        if (!(ref instanceof PRIndirectReference indirect)) {
            return null;
        }
        try {
            CMapAwareDocumentFont font = new CMapAwareDocumentFont(indirect);
            fontCache.put(name, font);
            return font;
        } catch (RuntimeException ignored) {
            return null;
        }
    }

    // ---------- Operand helpers ----------

    private static float num(List<PdfObject> operands, int index) {
        return ((PdfNumber) operands.get(index)).floatValue();
    }

    private static Color gray(float v) {
        float c = clamp01(v);
        return new Color(c, c, c);
    }

    private static Color rgb(float r, float g, float b) {
        return new Color(clamp01(r), clamp01(g), clamp01(b));
    }

    /**
     * Naive CMYK to sRGB approximation: r = (1-c)(1-k), g = (1-m)(1-k), b = (1-y)(1-k).
     */
    private static Color cmyk(float c, float m, float y, float k) {
        float cc = clamp01(c);
        float mm = clamp01(m);
        float yy = clamp01(y);
        float kk = clamp01(k);
        float r = (1f - cc) * (1f - kk);
        float gg = (1f - mm) * (1f - kk);
        float b = (1f - yy) * (1f - kk);
        return new Color(r, gg, b);
    }

    private static Color applyAlpha(Color color, float alpha) {
        int a = Math.round(clamp01(alpha) * 255f);
        if (a == color.getAlpha()) {
            return color;
        }
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), a);
    }

    private static ColorSpaceKind colorSpaceFromName(PdfObject operand) {
        if (!(operand instanceof PdfName name)) {
            return ColorSpaceKind.UNKNOWN;
        }
        String n = name.toString();
        switch (n) {
            case "/DeviceGray":
            case "/G":
            case "/CalGray":
                return ColorSpaceKind.GRAY;
            case "/DeviceRGB":
            case "/RGB":
            case "/CalRGB":
                return ColorSpaceKind.RGB;
            case "/DeviceCMYK":
            case "/CMYK":
                return ColorSpaceKind.CMYK;
            default:
                return ColorSpaceKind.UNKNOWN;
        }
    }

    private static Color defaultColorFor(ColorSpaceKind kind) {
        return kind == ColorSpaceKind.CMYK ? cmyk(0, 0, 0, 1f) : Color.BLACK;
    }

    /**
     * Picks numeric operands matching the active color space; non-numeric operands (e.g. pattern names) yield default.
     */
    private static Color colorFromOperands(ColorSpaceKind kind, List<PdfObject> operands) {
        int numericCount = 0;
        for (int i = 0; i < operands.size() - 1; i++) {
            if (operands.get(i) instanceof PdfNumber) {
                numericCount++;
            }
        }
        switch (kind) {
            case GRAY:
                if (numericCount >= 1) {
                    return gray(num(operands, 0));
                }
                break;
            case RGB:
                if (numericCount >= 3) {
                    return rgb(num(operands, 0), num(operands, 1), num(operands, 2));
                }
                break;
            case CMYK:
                if (numericCount >= 4) {
                    return cmyk(num(operands, 0), num(operands, 1), num(operands, 2), num(operands, 3));
                }
                break;
            default:
                // Fall through: infer from operand count when color space is unknown / unsupported.
                if (numericCount >= 4) {
                    return cmyk(num(operands, 0), num(operands, 1), num(operands, 2), num(operands, 3));
                }
                if (numericCount == 3) {
                    return rgb(num(operands, 0), num(operands, 1), num(operands, 2));
                }
                if (numericCount == 1) {
                    return gray(num(operands, 0));
                }
                break;
        }
        return defaultColorFor(kind);
    }

    private static float clamp01(float v) {
        if (v < 0f) {
            return 0f;
        }
        if (v > 1f) {
            return 1f;
        }
        return v;
    }

    private enum ColorSpaceKind { GRAY, RGB, CMYK, UNKNOWN }

    /**
     * Mutable per-graphics-state snapshot. Not thread-safe.
     */
    private static final class GState {
        Color fillColor = Color.BLACK;
        Color strokeColor = Color.BLACK;
        ColorSpaceKind fillColorSpace = ColorSpaceKind.GRAY;
        ColorSpaceKind strokeColorSpace = ColorSpaceKind.GRAY;
        float fillAlpha = 1.0f;
        float strokeAlpha = 1.0f;

        float lineWidth = 1.0f;
        int lineCap = BasicStroke.CAP_BUTT;
        int lineJoin = BasicStroke.JOIN_MITER;
        float miterLimit = 10.0f;
        float[] dashPattern;
        float dashPhase;

        boolean hasPendingClip;
        int pendingClipRule = Path2D.WIND_NON_ZERO;

        CMapAwareDocumentFont font;
        float fontSize;
        float charSpacing;
        float wordSpacing;
        float leading;
        float horizontalScaling = 1.0f;
        float textRise;

        GState() {
        }

        GState(GState other) {
            this.fillColor = other.fillColor;
            this.strokeColor = other.strokeColor;
            this.fillColorSpace = other.fillColorSpace;
            this.strokeColorSpace = other.strokeColorSpace;
            this.fillAlpha = other.fillAlpha;
            this.strokeAlpha = other.strokeAlpha;
            this.lineWidth = other.lineWidth;
            this.lineCap = other.lineCap;
            this.lineJoin = other.lineJoin;
            this.miterLimit = other.miterLimit;
            this.dashPattern = other.dashPattern == null ? null : other.dashPattern.clone();
            this.dashPhase = other.dashPhase;
            this.font = other.font;
            this.fontSize = other.fontSize;
            this.charSpacing = other.charSpacing;
            this.wordSpacing = other.wordSpacing;
            this.leading = other.leading;
            this.horizontalScaling = other.horizontalScaling;
            this.textRise = other.textRise;
            // hasPendingClip / pendingClipRule are intentionally not copied:
            // the W / W* operators apply to the current path before any q/Q boundary.
        }
    }
}

