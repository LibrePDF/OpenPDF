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
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
 * </ul>
 *
 * <p>Operators outside this subset (XObject {@code Do} for forms and images,
 * inline images {@code BI}/{@code ID}/{@code EI}, shading {@code sh},
 * pattern / shading colors, type 3 font glyph operators) are silently ignored.
 * Pages that rely heavily on those features may render with missing content;
 * this renderer is intentionally a focused subset that handles typical text +
 * simple-vector PDFs correctly.</p>
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

    /** Default user-space resolution of a PDF, in DPI. */
    private static final float PDF_USER_SPACE_DPI = 72f;

    // ExtGState dictionary keys not pre-defined as PdfName constants in openpdf-core.
    private static final PdfName EXTGS_LW = new PdfName("LW");
    private static final PdfName EXTGS_ML = new PdfName("ML");
    private static final PdfName EXTGS_LC = new PdfName("LC");
    private static final PdfName EXTGS_LJ = new PdfName("LJ");

    private final Graphics2D g2;
    private final PdfDictionary resources;
    private final Map<String, CMapAwareDocumentFont> fontCache = new HashMap<>();

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
        PdfContentParser parser = new PdfContentParser(new PRTokeniser(contentBytes));
        List<PdfObject> operands = new ArrayList<>();
        while (!parser.parse(operands).isEmpty()) {
            PdfLiteral op = (PdfLiteral) operands.get(operands.size() - 1);
            try {
                dispatch(op.toString(), operands);
            } catch (RuntimeException e) {
                // A malformed operator must not abort the whole page; log for diagnostics.
                LOG.log(Level.FINE, "Skipping operator ''{0}'' due to: {1}",
                        new Object[]{op, e});
            }
        }
    }

    /** Dispatches one operator. Operands include the trailing operator literal at index size-1. */
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
        if (resources == null) {
            return;
        }
        PdfDictionary gsResources = resources.getAsDict(PdfName.EXTGSTATE);
        if (gsResources == null) {
            return;
        }
        PdfObject obj = gsResources.get(new PdfName(name));
        PdfDictionary dict;
        if (obj instanceof PdfDictionary d) {
            dict = d;
        } else if (obj instanceof PRIndirectReference ref) {
            PdfObject resolved = PdfReader.getPdfObject(ref);
            dict = resolved instanceof PdfDictionary ? (PdfDictionary) resolved : null;
        } else {
            dict = null;
        }
        if (dict == null) {
            return;
        }
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

    /** Naive CMYK to sRGB approximation: r = (1-c)(1-k), g = (1-m)(1-k), b = (1-y)(1-k). */
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

    /** Picks numeric operands matching the active color space; non-numeric operands (e.g. pattern names) yield default. */
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

    /** Mutable per-graphics-state snapshot. Not thread-safe. */
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

