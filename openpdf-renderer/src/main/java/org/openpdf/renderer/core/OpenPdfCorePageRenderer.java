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
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openpdf.text.pdf.CMapAwareDocumentFont;
import org.openpdf.text.pdf.PRIndirectReference;
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
 *   <li>Graphics state: {@code q}, {@code Q}, {@code cm}</li>
 *   <li>Path construction: {@code m}, {@code l}, {@code c}, {@code v}, {@code y},
 *       {@code re}, {@code h}</li>
 *   <li>Path painting: {@code S}, {@code s}, {@code f}, {@code F}, {@code f*},
 *       {@code B}, {@code B*}, {@code b}, {@code b*}, {@code n}</li>
 *   <li>Line width: {@code w}</li>
 *   <li>Colors (DeviceGray and DeviceRGB): {@code g}, {@code G}, {@code rg}, {@code RG}</li>
 *   <li>Text state: {@code BT}, {@code ET}, {@code Tf}, {@code Tc}, {@code Tw},
 *       {@code TL}, {@code Tz}, {@code Td}, {@code TD}, {@code Tm}, {@code T*}</li>
 *   <li>Text showing: {@code Tj}, {@code TJ}, {@code '}, {@code "}</li>
 * </ul>
 *
 * <p>Other operators (extended graphics state {@code gs}, CMYK colors,
 * {@code cs}/{@code CS}/{@code scn}/{@code SCN}, clipping {@code W}/{@code W*},
 * XObject {@code Do}, inline images, shading patterns, marked content, ...)
 * are silently ignored. Pages that rely heavily on those features may render
 * with missing content; this renderer is intentionally a focused subset that
 * handles typical text + simple-vector PDFs correctly.</p>
 *
 * <h2>Coordinates</h2>
 * <p>The PDF user space has its origin at the bottom-left and Y growing up;
 * the {@link Graphics2D} target has its origin at the top-left and Y growing
 * down. The renderer applies an initial transform that flips Y and scales
 * by {@code dpi / 72} so that calling code receives a correctly-oriented
 * image of the requested resolution.</p>
 */
final class OpenPdfCorePageRenderer {

    /** Default user-space resolution of a PDF, in DPI. */
    private static final float PDF_USER_SPACE_DPI = 72f;

    private final Graphics2D g2;
    private final PdfDictionary resources;
    private final Map<String, CMapAwareDocumentFont> fontCache = new HashMap<>();

    private final Deque<GState> stateStack = new ArrayDeque<>();
    private GState state;

    private Path2D.Float currentPath = new Path2D.Float();
    private float pathStartX;
    private float pathStartY;
    private float pathCurX;
    private float pathCurY;

    /** Text object state (between {@code BT} and {@code ET}). */
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
        PdfContentParser parser = new PdfContentParser(new PRTokeniser(contentBytes));
        List<PdfObject> operands = new ArrayList<>();
        while (!parser.parse(operands).isEmpty()) {
            PdfLiteral op = (PdfLiteral) operands.get(operands.size() - 1);
            try {
                dispatch(op.toString(), operands);
            } catch (RuntimeException ignored) {
                // Robustness: a malformed individual operator must not abort the whole page.
            }
        }
    }

    /** Dispatches one operator. Operands include the trailing operator literal at index size-1. */
    private void dispatch(String op, List<PdfObject> operands) throws IOException {
        switch (op) {
            // --- Graphics state ---
            case "q":
                stateStack.push(state);
                state = new GState(state);
                break;
            case "Q":
                if (!stateStack.isEmpty()) {
                    state = stateStack.pop();
                }
                break;
            case "cm":
                concatCtm(num(operands, 0), num(operands, 1), num(operands, 2),
                        num(operands, 3), num(operands, 4), num(operands, 5));
                break;

            // --- Line width ---
            case "w":
                state.lineWidth = num(operands, 0);
                break;

            // --- Colors ---
            case "g":
                state.fillColor = gray(num(operands, 0));
                break;
            case "G":
                state.strokeColor = gray(num(operands, 0));
                break;
            case "rg":
                state.fillColor = rgb(num(operands, 0), num(operands, 1), num(operands, 2));
                break;
            case "RG":
                state.strokeColor = rgb(num(operands, 0), num(operands, 1), num(operands, 2));
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

            default:
                // Unsupported operator: ignore quietly so partial pages still render.
                break;
        }
    }

    // ---------- Path painting helpers ----------

    private void strokePath() {
        g2.setColor(state.strokeColor);
        g2.setStroke(new BasicStroke(Math.max(state.lineWidth, 0.001f)));
        g2.draw(currentPath);
    }

    private void fillPath(int windingRule) {
        Path2D.Float p = (Path2D.Float) currentPath.clone();
        p.setWindingRule(windingRule);
        g2.setColor(state.fillColor);
        g2.fill(p);
    }

    private void resetPath() {
        currentPath = new Path2D.Float();
    }

    private void concatCtm(float a, float b, float c, float d, float e, float f) {
        AffineTransform m = new AffineTransform(a, b, c, d, e, f);
        g2.transform(m);
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
            g2.scale(1, -1);
            g2.drawString(text, 0f, 0f);
        } finally {
            g2.setTransform(saved);
        }

        // Advance text matrix by the displayed width so following Tj's continue inline.
        float displayed = estimateTextAdvance(text);
        AffineTransform adv = new AffineTransform(textMatrix);
        adv.translate(displayed, 0);
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
                return state.font.decode(raw.getBytes(), 0, raw.getBytes().length);
            } catch (RuntimeException ignored) {
                // Fall through.
            }
        }
        return raw.toUnicodeString();
    }

    private float estimateTextAdvance(String text) {
        // We don't have access to per-glyph widths from CMapAwareDocumentFont in a
        // package-public way, so fall back to a Java2D measurement at the current font.
        return (float) g2.getFontMetrics().getStringBounds(text, g2).getWidth();
    }

    private Font mapFont(CMapAwareDocumentFont docFont, float size) {
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
        return new Font(family, style, 1).deriveFont(Math.max(size, 0.1f));
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

    private static float clamp01(float v) {
        if (v < 0f) {
            return 0f;
        }
        if (v > 1f) {
            return 1f;
        }
        return v;
    }

    /** Mutable per-graphics-state snapshot. Not thread-safe. */
    private static final class GState {
        Color fillColor = Color.BLACK;
        Color strokeColor = Color.BLACK;
        float lineWidth = 1.0f;

        CMapAwareDocumentFont font;
        float fontSize;
        float charSpacing;
        float wordSpacing;
        float leading;
        float horizontalScaling = 1.0f;

        GState() {
        }

        GState(GState other) {
            this.fillColor = other.fillColor;
            this.strokeColor = other.strokeColor;
            this.lineWidth = other.lineWidth;
            this.font = other.font;
            this.fontSize = other.fontSize;
            this.charSpacing = other.charSpacing;
            this.wordSpacing = other.wordSpacing;
            this.leading = other.leading;
            this.horizontalScaling = other.horizontalScaling;
        }
    }
}



