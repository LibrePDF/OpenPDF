/*
 * $Id: PdfGraphics2D.java 3611 2008-11-05 19:45:31Z blowagie $
 *
 * Copyright 2002 by Jim Moore <jim@scolamoore.com>.
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

package com.lowagie.text.pdf;

import com.lowagie.text.pdf.internal.PolylineShape;
import com.lowagie.text.utils.SystemPropertyUtil;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Composite;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Image;
import java.awt.MediaTracker;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.RenderingHints.Key;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.Transparency;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.TextAttribute;
import java.awt.geom.AffineTransform;
import java.awt.geom.Arc2D;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.awt.image.renderable.RenderableImage;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.plugins.jpeg.JPEGImageWriteParam;
import javax.imageio.stream.ImageOutputStream;

public class PdfGraphics2D extends Graphics2D {

    public static final int AFM_DIVISOR = 1000; // used to calculate coordinates
    private static final int FILL = 1;
    private static final int STROKE = 2;
    private static final int CLIP = 3;
    private static final AffineTransform IDENTITY = new AffineTransform();

    private static final Set<String> LOGICAL_FONT_NAMES = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("Dialog", "DialogInput", "Monospaced", "Serif", "SansSerif")));
    private static final String BOLD_FONT_FACE_NAME_SUFFIX = ".bold";
    private static final String BOLD_ITALIC_FONT_FACE_NAME_SUFFIX = ".bolditalic";
    private final CompositeFontDrawer compositeFontDrawer = new CompositeFontDrawer();
    // Added by Jurij Bilas
    protected boolean underline;          // indicates if the font style is underlined
    protected PdfGState[] fillGState = new PdfGState[256];
    protected PdfGState[] strokeGState = new PdfGState[256];
    protected int currentFillGState = 255;
    protected int currentStrokeGState = 255;
    private BasicStroke strokeOne = new BasicStroke(1);
    private Font font;
    private BaseFont baseFont;
    private float fontSize;
    private AffineTransform transform;
    private Paint paint;
    private Color background;
    private float width;
    private float height;
    private Area clip;
    private final RenderingHints rhints = new RenderingHints(null);
    private Stroke stroke;
    private Stroke originalStroke;
    private PdfContentByte cb;
    /**
     * Storage for BaseFont objects created.
     */
    private Map<String, BaseFont> baseFonts;
    private boolean disposeCalled = false;
    private FontMapper fontMapper;
    private List<Object> kids;
    private boolean kid = false;
    private Graphics2D dg2 = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB).createGraphics();
    private boolean onlyShapes = false;
    private Stroke oldStroke;
    private Paint paintFill;
    private Paint paintStroke;
    private MediaTracker mediaTracker;
    private boolean convertImagesToJPEG = false;
    private float jpegQuality = .95f;
    // Added by Alexej Suchov
    private float alpha;
    // Added by Alexej Suchov
    private Composite composite;
    // Added by Alexej Suchov
    private Paint realPaint;
    // make use of compositeFontDrawer configurable ... may be set via property or directly via setter
    private boolean isCompositeFontDrawerEnabled = SystemPropertyUtil.getBoolean(
            "com.github.librepdf.openpdf.compositeFontDrawerEnabled", true);

    private PdfGraphics2D() {
        dg2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        setRenderingHint(HyperLinkKey.KEY_INSTANCE, HyperLinkKey.VALUE_HYPERLINKKEY_OFF);
    }

    /**
     * Copy constructor for child PdfGraphics2D objects.
     *
     * @param parent the parent PdfGraphics2D
     * @see #create()
     */
    protected PdfGraphics2D(PdfGraphics2D parent) {
        this();
        rhints.putAll(parent.rhints);
        onlyShapes = parent.onlyShapes;
        transform = new AffineTransform(parent.transform);
        baseFonts = parent.baseFonts;
        fontMapper = parent.fontMapper;
        paint = parent.paint;
        fillGState = parent.fillGState;
        currentFillGState = parent.currentFillGState;
        currentStrokeGState = parent.currentStrokeGState;
        strokeGState = parent.strokeGState;
        background = parent.background;
        mediaTracker = parent.mediaTracker;
        convertImagesToJPEG = parent.convertImagesToJPEG;
        jpegQuality = parent.jpegQuality;
        setFont(parent.font);
        cb = parent.cb.getDuplicate();
        cb.saveState();
        width = parent.width;
        height = parent.height;
        followPath(new Area(new Rectangle2D.Float(0, 0, width, height)), CLIP);
        if (parent.clip != null) {
            clip = new Area(parent.clip);
        }
        composite = parent.composite;
        stroke = parent.stroke;
        originalStroke = parent.originalStroke;
        strokeOne = (BasicStroke) transformStroke(strokeOne);
        oldStroke = parent.strokeOne;
        setStrokeDiff(oldStroke, null);
        cb.saveState();
        if (clip != null) {
            followPath(clip, CLIP);
        }
        kid = true;
    }

    /**
     * Shortcut constructor for PDFGraphics2D.
     *
     * @param cb     the PdfContentByte
     * @param width  the width
     * @param height the height
     */
    public PdfGraphics2D(PdfContentByte cb, float width, float height) {
        this(cb, width, height, null, false, false, 0);
    }

    /**
     * Constructor for PDFGraphics2D.
     *
     * @param cb                  The PdfContentByte
     * @param width               The width
     * @param height              The height
     * @param fontMapper          The fonts to put in the BaseFont Map
     * @param onlyShapes          True if there are only shapes, false otherwise
     * @param convertImagesToJPEG True to convert to JPEG, false otherwise
     * @param quality             The JPEG quality
     */
    public PdfGraphics2D(PdfContentByte cb, float width, float height, FontMapper fontMapper, boolean onlyShapes,
            boolean convertImagesToJPEG, float quality) {
        super();
        dg2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        setRenderingHint(HyperLinkKey.KEY_INSTANCE, HyperLinkKey.VALUE_HYPERLINKKEY_OFF);
        this.convertImagesToJPEG = convertImagesToJPEG;
        this.jpegQuality = quality;
        this.onlyShapes = onlyShapes;
        this.transform = new AffineTransform();
        this.baseFonts = new HashMap<>();
        if (!onlyShapes) {
            this.fontMapper = fontMapper;
            if (this.fontMapper == null) {
                this.fontMapper = new DefaultFontMapper();
            }
        }
        paint = Color.black;
        background = Color.white;
        setFont(new Font("sanserif", Font.PLAIN, 12));
        this.cb = cb;
        cb.saveState();
        this.width = width;
        this.height = height;
        clip = new Area(new Rectangle2D.Float(0, 0, width, height));
        clip(clip);
        originalStroke = stroke = oldStroke = strokeOne;
        setStrokeDiff(stroke, null);
        cb.saveState();
    }

    /**
     * Calculates position and/or stroke thickness depending on the font size
     *
     * @param d value to be converted
     * @param i font size
     * @return position and/or stroke thickness depending on the font size
     */
    public static double asPoints(double d, int i) {
        return d * i / AFM_DIVISOR;
    }

    /**
     * @see Graphics2D#draw(Shape)
     */
    public void draw(Shape s) {
        followPath(s, STROKE);
    }

    /**
     * @see Graphics2D#drawImage(Image, AffineTransform, ImageObserver)
     */
    public boolean drawImage(Image img, AffineTransform xform, ImageObserver obs) {
        return drawImage(img, null, xform, null, obs);
    }

    /**
     * @see Graphics2D#drawImage(BufferedImage, BufferedImageOp, int, int)
     */
    public void drawImage(BufferedImage img, BufferedImageOp op, int x, int y) {
        BufferedImage result = img;
        if (op != null) {
            result = op.createCompatibleDestImage(img, img.getColorModel());
            result = op.filter(img, result);
        }
        drawImage(result, x, y, null);
    }

    /**
     * @see Graphics2D#drawRenderedImage(RenderedImage, AffineTransform)
     */
    public void drawRenderedImage(RenderedImage img, AffineTransform xform) {
        BufferedImage image = null;
        if (img instanceof BufferedImage) {
            image = (BufferedImage) img;
        } else {
            ColorModel cm = img.getColorModel();
            int width = img.getWidth();
            int height = img.getHeight();
            WritableRaster raster = cm.createCompatibleWritableRaster(width, height);
            boolean isAlphaPremultiplied = cm.isAlphaPremultiplied();
            Hashtable<String, Object> properties = new Hashtable<>();
            String[] keys = img.getPropertyNames();
            if (keys != null) {
                for (String key : keys) {
                    properties.put(key, img.getProperty(key));
                }
            }
            BufferedImage result = new BufferedImage(cm, raster, isAlphaPremultiplied, properties);
            img.copyData(raster);
            image = result;
        }
        drawImage(image, xform, null);
    }

    /**
     * @see Graphics2D#drawRenderableImage(RenderableImage, AffineTransform)
     */
    public void drawRenderableImage(RenderableImage img, AffineTransform xform) {
        drawRenderedImage(img.createDefaultRendering(), xform);
    }

    /**
     * @see Graphics#drawString(String, int, int)
     */
    public void drawString(String s, int x, int y) {
        drawString(s, (float) x, (float) y);
    }

    /**
     * This routine goes through the attributes and sets the font before calling the actual string drawing routine
     *
     * @param iter the AttributedCharacterIterator
     */
    @SuppressWarnings("unchecked")
    protected void doAttributes(AttributedCharacterIterator iter) {
        underline = false;
        Set<AttributedCharacterIterator.Attribute> set = iter.getAttributes().keySet();
        for (AttributedCharacterIterator.Attribute attribute : set) {
            if (!(attribute instanceof TextAttribute textattribute)) {
                continue;
            }
            if (textattribute.equals(TextAttribute.FONT)) {
                Font font = (Font) iter.getAttributes().get(textattribute);
                setFont(font);
            } else if (textattribute.equals(TextAttribute.UNDERLINE)) {
                if (iter.getAttributes().get(textattribute) == TextAttribute.UNDERLINE_ON) {
                    underline = true;
                }
            } else if (textattribute.equals(TextAttribute.SIZE)) {
                Object obj = iter.getAttributes().get(textattribute);
                if (obj instanceof Integer) {
                    int i = (Integer) obj;
                    setFont(getFont().deriveFont(getFont().getStyle(), i));
                } else if (obj instanceof Float) {
                    float f = (Float) obj;
                    setFont(getFont().deriveFont(getFont().getStyle(), f));
                }
            } else if (textattribute.equals(TextAttribute.FOREGROUND)) {
                setColor((Color) iter.getAttributes().get(textattribute));
            } else if (textattribute.equals(TextAttribute.FAMILY)) {
                Font font = getFont();
                Map<TextAttribute, Object> fontAttributes = (Map<TextAttribute, Object>) font.getAttributes();
                fontAttributes.put(TextAttribute.FAMILY, iter.getAttributes().get(textattribute));
                setFont(font.deriveFont(fontAttributes));
            } else if (textattribute.equals(TextAttribute.POSTURE)) {
                Font font = getFont();
                Map<TextAttribute, Object> fontAttributes = (Map<TextAttribute, Object>) font.getAttributes();
                fontAttributes.put(TextAttribute.POSTURE, iter.getAttributes().get(textattribute));
                setFont(font.deriveFont(fontAttributes));
            } else if (textattribute.equals(TextAttribute.WEIGHT)) {
                Font font = getFont();
                Map<TextAttribute, Object> fontAttributes = (Map<TextAttribute, Object>) font.getAttributes();
                fontAttributes.put(TextAttribute.WEIGHT, iter.getAttributes().get(textattribute));
                setFont(font.deriveFont(fontAttributes));
            }
        }
    }

    /**
     * @see Graphics2D#drawString(String, float, float)
     */
    public void drawString(String s, float x, float y) {
        if (s.length() == 0) {
            return;
        }
        setFillPaint();
        if (onlyShapes) {
            drawGlyphVector(this.font.layoutGlyphVector(getFontRenderContext(), s.toCharArray(), 0, s.length(),
                    java.awt.Font.LAYOUT_LEFT_TO_RIGHT), x, y);
//            Use the following line to compile in JDK 1.3
//            drawGlyphVector(this.font.createGlyphVector(getFontRenderContext(), s), x, y);
        } else {
            if (!Float.isFinite(fontSize) || fontSize < PdfContentByte.MIN_FONT_SIZE) {
                return;
            }
            double width = 0;
            if (isCompositeFontDrawerEnabled && CompositeFontDrawer.isSupported()
                    && compositeFontDrawer.isCompositeFont(font)) {
                width = compositeFontDrawer.drawString(s, font, x, y, this::getCachedBaseFont, this::drawString);
            } else {
                // Splitting string to the parts depending on they visibility preserves
                // the position of the visible parts of string and prevents alignment of the text in width
                // (increasing value of the character spacing parameter of the PdfContentByte)
                List<String> substrings = splitIntoSubstringsByVisibility(s);
                for (String str : substrings) {
                    width += drawString(str, baseFont, x + width, y);
                }
            }
            if (underline) {
                // These two are supposed to be taken from the .AFM file
                //int UnderlinePosition = -100;
                int UnderlineThickness = 50;
                //
                double d = asPoints(UnderlineThickness, (int) fontSize);
                Stroke savedStroke = originalStroke;
                setStroke(new BasicStroke((float) d));
                y = (float) (y + asPoints(UnderlineThickness, (int) fontSize));
                Line2D line = new Line2D.Double(x, y, width + x, y);
                draw(line);
                setStroke(savedStroke);
            }
        }
    }

    private double drawString(String s, BaseFont baseFont, double x, double y) {
        boolean restoreTextRenderingMode = false;
        AffineTransform at = getTransform();
        try {
            AffineTransform at2 = getTransform();
            at2.translate(x, y);
            at2.concatenate(font.getTransform());
            setTransform(at2);
            AffineTransform inverse = this.normalizeMatrix();
            AffineTransform flipper = AffineTransform.getScaleInstance(1, -1);
            inverse.concatenate(flipper);
            double[] mx = new double[6];
            inverse.getMatrix(mx);
            cb.beginText();
            cb.setFontAndSize(baseFont, fontSize);
            // Check if we need to simulate an italic font.
            if (font.isItalic()) {
                float angle = baseFont.getFontDescriptor(BaseFont.ITALICANGLE, 1000);
                float angle2 = font.getItalicAngle();
                // When there are different fonts for italic, bold, italic bold
                // the font.getName() will be different from the font.getFontName()
                // value. When they are the same value then we are normally dealing
                // with a single font that has been made into an italic or bold
                // font. When there are only a plain and a bold font available,
                // we need to enter this logic too.
                if (Objects.equals(font.getFontName(), font.getName()) || (angle == 0f && angle2 == 0f)) {
                    // We don't have an italic version of this font so we need
                    // to set the font angle ourselves to produce an italic font.
                    if (angle2 == 0) {
                        // The JavaVM didn't have an angle setting for making
                        // the font an italic font so use a default of
                        // italic angle of 15 degrees.
                        angle2 = 15.0f;
                    } else {
                        // This sign of the angle for Java and PDF seams
                        // seams to be reversed.
                        angle2 = -angle2;
                    }
                    if (angle == 0) {
                        mx[2] = angle2 / 100.0f;
                    }
                }
            }
            cb.setTextMatrix((float) mx[0], (float) mx[1], (float) mx[2], (float) mx[3], (float) mx[4], (float) mx[5]);
            Float fontTextAttributeWidth = (Float) font.getAttributes().get(TextAttribute.WIDTH);
            fontTextAttributeWidth = (fontTextAttributeWidth == null)
                    ? TextAttribute.WIDTH_REGULAR
                    : fontTextAttributeWidth;

            if (!TextAttribute.WIDTH_REGULAR.equals(fontTextAttributeWidth)) {
                cb.setHorizontalScaling(100.0f / fontTextAttributeWidth);
            }

            // Check if we need to simulate a bold font.
            // Do nothing if the BaseFont is already bold. This test is not foolproof but it will work most of the times.
            if (!baseFont.getPostscriptFontName().toLowerCase(Locale.ROOT).contains("bold")) {
                // Get the weight of the font so we can detect fonts with a weight
                // that makes them bold, but the Font.isBold() value is false.
                Float weight = (Float) font.getAttributes().get(TextAttribute.WEIGHT);
                if (weight == null) {
                    weight = (font.isBold()) ? TextAttribute.WEIGHT_BOLD
                            : TextAttribute.WEIGHT_REGULAR;
                }
                String fontFaceName = font.getFontName();
                String fontLogicalName = font.getName();
                if ((font.isBold() || (weight >= TextAttribute.WEIGHT_SEMIBOLD))
                        && (fontFaceName.equals(fontLogicalName) ||
                        // bold logical font face name has suffix ".bold" / ".bolditalic"
                        (LOGICAL_FONT_NAMES.contains(fontLogicalName) &&
                                (fontFaceName.equals(fontLogicalName + BOLD_FONT_FACE_NAME_SUFFIX) ||
                                        fontFaceName.equals(fontLogicalName + BOLD_ITALIC_FONT_FACE_NAME_SUFFIX))))) {
                    // Simulate a bold font.
                    float strokeWidth = font.getSize2D() * (weight - TextAttribute.WEIGHT_REGULAR) / 30f;
                    if (strokeWidth != 1) {
                        if (realPaint instanceof Color color) {
                            cb.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL_STROKE);
                            oldStroke = new BasicStroke(strokeWidth);
                            cb.setLineWidth(strokeWidth);
                            int alpha = color.getAlpha();
                            if (alpha != currentStrokeGState) {
                                currentStrokeGState = alpha;
                                PdfGState gs = strokeGState[alpha];
                                if (gs == null) {
                                    gs = new PdfGState();
                                    gs.setStrokeOpacity(alpha / 255f);
                                    strokeGState[alpha] = gs;
                                }
                                cb.setGState(gs);
                            }
                            setStrokePaint();
                            restoreTextRenderingMode = true;
                        }
                    }
                }
            }

            double width = 0;
            if (font.getSize2D() > 0) {
                float scale = 1000 / font.getSize2D();
                Font derivedFont = font.deriveFont(AffineTransform.getScaleInstance(scale, scale));
                width = derivedFont.getStringBounds(s, getFontRenderContext()).getWidth();
                if (derivedFont.isTransformed()) {
                    width /= scale;
                }
            }
            // if the hyperlink flag is set add an action to the text
            Object url = getRenderingHint(HyperLinkKey.KEY_INSTANCE);
            if (url != null && !url.equals(HyperLinkKey.VALUE_HYPERLINKKEY_OFF)) {
                float scale = 1000 / font.getSize2D();
                Font derivedFont = font.deriveFont(AffineTransform.getScaleInstance(scale, scale));
                double height = derivedFont.getStringBounds(s, getFontRenderContext()).getHeight();
                if (derivedFont.isTransformed()) {
                    height /= scale;
                }
                double leftX = cb.getXTLM();
                double leftY = cb.getYTLM();
                PdfAction action = new PdfAction(url.toString());
                cb.setAction(action, (float) leftX, (float) leftY, (float) (leftX + width), (float) (leftY + height));
            }
            if (s.length() > 1) {
                float adv = ((float) width - baseFont.getWidthPoint(s, fontSize)) / (s.length() - 1);
                cb.setCharacterSpacing(adv);
            }
            cb.showText(s);
            if (s.length() > 1) {
                cb.setCharacterSpacing(0);
            }
            if (!TextAttribute.WIDTH_REGULAR.equals(fontTextAttributeWidth)) {
                cb.setHorizontalScaling(100);
            }

            // Restore the original TextRenderingMode if needed.
            if (restoreTextRenderingMode) {
                cb.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL);
            }

            cb.endText();
            return width;
        } finally {
            setTransform(at);
        }
    }

    /**
     * @see Graphics#drawString(AttributedCharacterIterator, int, int)
     */
    public void drawString(AttributedCharacterIterator iterator, int x, int y) {
        drawString(iterator, (float) x, (float) y);
    }

    /**
     * @see Graphics2D#drawString(AttributedCharacterIterator, float, float)
     */
    public void drawString(AttributedCharacterIterator iter, float x, float y) {
/*
        StringBuffer sb = new StringBuffer();
        for(char c = iter.first(); c != AttributedCharacterIterator.DONE; c = iter.next()) {
            sb.append(c);
        }
        drawString(sb.toString(),x,y);
*/
        StringBuilder stringbuffer = new StringBuilder(iter.getEndIndex());
        for (char c = iter.first(); c != '\uFFFF'; c = iter.next()) {
            if (iter.getIndex() == iter.getRunStart()) {
                if (stringbuffer.length() > 0) {
                    drawString(stringbuffer.toString(), x, y);
                    FontMetrics fontmetrics = getFontMetrics();
                    x = (float) (x + fontmetrics.getStringBounds(stringbuffer.toString(), this).getWidth());
                    stringbuffer.delete(0, stringbuffer.length());
                }
                doAttributes(iter);
            }
            stringbuffer.append(c);
        }

        drawString(stringbuffer.toString(), x, y);
        underline = false;
    }

    /**
     * @see Graphics2D#drawGlyphVector(GlyphVector, float, float)
     */
    public void drawGlyphVector(GlyphVector g, float x, float y) {
        Shape s = g.getOutline(x, y);
        fill(s);
    }

    /**
     * @see Graphics2D#fill(Shape)
     */
    public void fill(Shape s) {
        followPath(s, FILL);
    }

    /**
     * @see Graphics2D#hit(Rectangle, Shape, boolean)
     */
    public boolean hit(Rectangle rect, Shape s, boolean onStroke) {
        if (onStroke) {
            s = stroke.createStrokedShape(s);
        }
        s = transform.createTransformedShape(s);
        Area area = new Area(s);
        if (clip != null) {
            area.intersect(clip);
        }
        return area.intersects(rect.x, rect.y, rect.width, rect.height);
    }

    /**
     * @see Graphics2D#getDeviceConfiguration()
     */
    public GraphicsConfiguration getDeviceConfiguration() {
        return dg2.getDeviceConfiguration();
    }

    private Stroke transformStroke(Stroke stroke) {
        if (!(stroke instanceof BasicStroke st)) {
            return stroke;
        }
        float scale = (float) Math.sqrt(Math.abs(transform.getDeterminant()));
        float[] dash = st.getDashArray();
        if (dash != null) {
            for (int k = 0; k < dash.length; ++k) {
                dash[k] *= scale;
            }
        }
        return new BasicStroke(st.getLineWidth() * scale, st.getEndCap(), st.getLineJoin(), st.getMiterLimit(), dash,
                st.getDashPhase() * scale);
    }

    private void setStrokeDiff(Stroke newStroke, Stroke oldStroke) {
        if (newStroke == oldStroke) {
            return;
        }
        if (!(newStroke instanceof BasicStroke basicStroke)) {
            return;
        }
        boolean oldOk = (oldStroke instanceof BasicStroke);
        BasicStroke oStroke = null;
        if (oldOk) {
            oStroke = (BasicStroke) oldStroke;
        }
        if (!oldOk || basicStroke.getLineWidth() != oStroke.getLineWidth()) {
            cb.setLineWidth(basicStroke.getLineWidth());
        }
        if (!oldOk || basicStroke.getEndCap() != oStroke.getEndCap()) {
            switch (basicStroke.getEndCap()) {
                case BasicStroke.CAP_BUTT:
                    cb.setLineCap(0);
                    break;
                case BasicStroke.CAP_SQUARE:
                    cb.setLineCap(2);
                    break;
                default:
                    cb.setLineCap(1);
            }
        }
        if (!oldOk || basicStroke.getLineJoin() != oStroke.getLineJoin()) {
            switch (basicStroke.getLineJoin()) {
                case BasicStroke.JOIN_MITER:
                    cb.setLineJoin(0);
                    break;
                case BasicStroke.JOIN_BEVEL:
                    cb.setLineJoin(2);
                    break;
                default:
                    cb.setLineJoin(1);
            }
        }
        if (!oldOk || basicStroke.getMiterLimit() != oStroke.getMiterLimit()) {
            cb.setMiterLimit(basicStroke.getMiterLimit());
        }
        boolean makeDash;
        if (oldOk) {
            if (basicStroke.getDashArray() != null) {
                if (basicStroke.getDashPhase() != oStroke.getDashPhase()) {
                    makeDash = true;
                } else {
                    makeDash = !java.util.Arrays.equals(basicStroke.getDashArray(), oStroke.getDashArray());
                }
            } else {
                makeDash = oStroke.getDashArray() != null;
            }
        } else {
            makeDash = true;
        }
        if (makeDash) {
            float[] dash = basicStroke.getDashArray();
            if (dash == null) {
                cb.setLiteral("[]0 d\n");
            } else {
                cb.setLiteral('[');
                int lim = dash.length;
                for (float dash1 : dash) {
                    cb.setLiteral(dash1);
                    cb.setLiteral(' ');
                }
                cb.setLiteral(']');
                cb.setLiteral(basicStroke.getDashPhase());
                cb.setLiteral(" d\n");
            }
        }
    }

    /**
     * Sets a rendering hint
     *
     * @param arg0 the Key
     * @param arg1 the Object to associate to it
     */
    public void setRenderingHint(Key arg0, Object arg1) {
        if (arg1 != null) {
            rhints.put(arg0, arg1);
        } else {
            if (arg0 instanceof HyperLinkKey) {
                rhints.put(arg0, HyperLinkKey.VALUE_HYPERLINKKEY_OFF);
            } else {
                rhints.remove(arg0);
            }
        }
    }

    /**
     * @param arg0 a key
     * @return the rendering hint
     */
    public Object getRenderingHint(Key arg0) {
        return rhints.get(arg0);
    }

    /**
     * @see Graphics2D#addRenderingHints(Map)
     */
    public void addRenderingHints(Map hints) {
        rhints.putAll(hints);
    }

    /**
     * @see Graphics2D#getRenderingHints()
     */
    public RenderingHints getRenderingHints() {
        return rhints;
    }

    /**
     * @see Graphics2D#setRenderingHints(Map)
     */
    public void setRenderingHints(Map hints) {
        rhints.clear();
        rhints.putAll(hints);
    }

    /**
     * @see Graphics#translate(int, int)
     */
    public void translate(int x, int y) {
        translate(x, (double) y);
    }

    /**
     * @see Graphics2D#translate(double, double)
     */
    public void translate(double tx, double ty) {
        transform.translate(tx, ty);
    }

    /**
     * @see Graphics2D#rotate(double)
     */
    public void rotate(double theta) {
        transform.rotate(theta);
    }

    /**
     * @see Graphics2D#rotate(double, double, double)
     */
    public void rotate(double theta, double x, double y) {
        transform.rotate(theta, x, y);
    }

    /**
     * @see Graphics2D#scale(double, double)
     */
    public void scale(double sx, double sy) {
        transform.scale(sx, sy);
        this.stroke = transformStroke(originalStroke);
    }

    /**
     * @see Graphics2D#shear(double, double)
     */
    public void shear(double shx, double shy) {
        transform.shear(shx, shy);
    }

    /**
     * @see Graphics2D#transform(AffineTransform)
     */
    public void transform(AffineTransform tx) {
        transform.concatenate(tx);
        this.stroke = transformStroke(originalStroke);
    }

    /**
     * @see Graphics2D#getTransform()
     */
    public AffineTransform getTransform() {
        return new AffineTransform(transform);
    }

    /**
     * @see Graphics2D#setTransform(AffineTransform)
     */
    public void setTransform(AffineTransform t) {
        transform = new AffineTransform(t);
        this.stroke = transformStroke(originalStroke);
    }

    /**
     * Method contributed by Alexej Suchov
     *
     * @see Graphics2D#getPaint()
     */
    public Paint getPaint() {
        if (realPaint != null) {
            return realPaint;
        } else {
            return paint;
        }
    }

    /**
     * Method contributed by Alexej Suchov
     *
     * @see Graphics2D#setPaint(Paint)
     */
    public void setPaint(Paint paint) {
        if (paint == null) {
            return;
        }
        this.paint = paint;
        realPaint = paint;

        if ((composite instanceof AlphaComposite co) && (paint instanceof Color)) {

            if (co.getRule() == 3) {
                Color c = (Color) paint;
                this.paint = new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (c.getAlpha() * alpha));
                realPaint = paint;
            }
        }

    }

    /**
     * @see Graphics2D#getComposite()
     */
    public Composite getComposite() {
        return composite;
    }

    /**
     * Method contributed by Alexej Suchov
     *
     * @see Graphics2D#setComposite(Composite)
     */
    public void setComposite(Composite comp) {

        if (comp instanceof AlphaComposite composite) {

            if (composite.getRule() == 3) {

                alpha = composite.getAlpha();
                this.composite = composite;

                if (realPaint != null && (realPaint instanceof Color c)) {

                    paint = new Color(c.getRed(), c.getGreen(), c.getBlue(),
                            (int) (c.getAlpha() * alpha));
                }
                return;
            }
        }

        this.composite = comp;
        alpha = 1.0F;

    }

    /**
     * @see Graphics2D#getBackground()
     */
    public Color getBackground() {
        return background;
    }

    /**
     * @see Graphics2D#setBackground(Color)
     */
    public void setBackground(Color color) {
        background = color;
    }

    /**
     * @see Graphics2D#getStroke()
     */
    public Stroke getStroke() {
        return originalStroke;
    }

    /**
     * @see Graphics2D#setStroke(Stroke)
     */
    public void setStroke(Stroke s) {
        originalStroke = s;
        this.stroke = transformStroke(s);
    }

    /**
     * @see Graphics2D#getFontRenderContext()
     */
    public FontRenderContext getFontRenderContext() {
        boolean antialias = RenderingHints.VALUE_TEXT_ANTIALIAS_ON.equals(
                getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING));
        boolean fractions = RenderingHints.VALUE_FRACTIONALMETRICS_ON.equals(
                getRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS));
        return new FontRenderContext(new AffineTransform(), antialias, fractions);
    }

    /**
     * @see Graphics#create()
     */
    public Graphics create() {
        PdfGraphics2D g2 = createChild();
        if (kids == null) {
            kids = new ArrayList<>();
        }
        kids.add(cb.getInternalBuffer().size());
        kids.add(g2);
        return g2;
    }

    protected PdfGraphics2D createChild() {
        return new PdfGraphics2D(this);
    }

    public PdfContentByte getContent() {
        return this.cb;
    }

    /**
     * @see Graphics#getColor()
     */
    public Color getColor() {
        if (paint instanceof Color) {
            return (Color) paint;
        } else {
            return Color.black;
        }
    }

    /**
     * @see Graphics#setColor(Color)
     */
    public void setColor(Color color) {
        setPaint(color);
    }

    /**
     * @see Graphics#setPaintMode()
     */
    public void setPaintMode() {
    }

    /**
     * @see Graphics#setXORMode(Color)
     */
    public void setXORMode(Color c1) {

    }

    /**
     * @see Graphics#getFont()
     */
    public Font getFont() {
        return font;
    }

    /**
     * @see Graphics#setFont(Font)
     */
    /**
     * Sets the current font.
     */
    public void setFont(Font f) {
        if (f == null) {
            return;
        }
        if (onlyShapes) {
            font = f;
            return;
        }
        if (f == font) {
            return;
        }
        font = f;
        fontSize = f.getSize2D();
        baseFont = getCachedBaseFont(f);
    }

    private BaseFont getCachedBaseFont(Font f) {
        synchronized (baseFonts) {
            BaseFont bf = baseFonts.get(f.getFontName());
            if (bf == null) {
                bf = fontMapper.awtToPdf(f);
                baseFonts.put(f.getFontName(), bf);
            }
            return bf;
        }
    }

    /**
     * @see Graphics#getFontMetrics(Font)
     */
    public FontMetrics getFontMetrics(Font f) {
        return dg2.getFontMetrics(f);
    }

    /**
     * @see Graphics#getClipBounds()
     */
    public Rectangle getClipBounds() {
        if (clip == null) {
            return null;
        }
        return getClip().getBounds();
    }

    /**
     * @see Graphics#clipRect(int, int, int, int)
     */
    public void clipRect(int x, int y, int width, int height) {
        Rectangle2D rect = new Rectangle2D.Double(x, y, width, height);
        clip(rect);
    }

    /**
     * @see Graphics#setClip(int, int, int, int)
     */
    public void setClip(int x, int y, int width, int height) {
        Rectangle2D rect = new Rectangle2D.Double(x, y, width, height);
        setClip(rect);
    }

    /**
     * @see Graphics2D#clip(Shape)
     */
    public void clip(Shape s) {
        if (s == null) {
            setClip(null);
            return;
        }
        s = transform.createTransformedShape(s);
        if (clip == null) {
            clip = new Area(s);
        } else {
            clip.intersect(new Area(s));
        }
        followPath(s, CLIP);
    }

    /**
     * @see Graphics#getClip()
     */
    public Shape getClip() {
        try {
            return transform.createInverse().createTransformedShape(clip);
        } catch (NoninvertibleTransformException e) {
            return null;
        }
    }

    /**
     * @see Graphics#setClip(Shape)
     */
    public void setClip(Shape s) {
        cb.restoreState();
        cb.saveState();
        if (s != null) {
            s = transform.createTransformedShape(s);
        }
        if (s == null) {
            clip = null;
        } else {
            clip = new Area(s);
            followPath(s, CLIP);
        }
        paintFill = paintStroke = null;
        currentFillGState = currentStrokeGState = -1;
        oldStroke = strokeOne;
    }

    /**
     * @see Graphics#copyArea(int, int, int, int, int, int)
     */
    public void copyArea(int x, int y, int width, int height, int dx, int dy) {

    }

    /**
     * @see Graphics#drawLine(int, int, int, int)
     */
    public void drawLine(int x1, int y1, int x2, int y2) {
        Line2D line = new Line2D.Double(x1, y1, x2, y2);
        draw(line);
    }

    /**
     * @see Graphics#fillRect(int, int, int, int)
     */
    public void drawRect(int x, int y, int width, int height) {
        draw(new Rectangle(x, y, width, height));
    }

    /**
     * @see Graphics#fillRect(int, int, int, int)
     */
    public void fillRect(int x, int y, int width, int height) {
        fill(new Rectangle(x, y, width, height));
    }

    /**
     * @see Graphics#clearRect(int, int, int, int)
     */
    public void clearRect(int x, int y, int width, int height) {
        Paint temp = paint;
        setPaint(background);
        fillRect(x, y, width, height);
        setPaint(temp);
    }

    /**
     * @see Graphics#drawRoundRect(int, int, int, int, int, int)
     */
    public void drawRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        RoundRectangle2D rect = new RoundRectangle2D.Double(x, y, width, height, arcWidth, arcHeight);
        draw(rect);
    }

    /**
     * @see Graphics#fillRoundRect(int, int, int, int, int, int)
     */
    public void fillRoundRect(int x, int y, int width, int height, int arcWidth, int arcHeight) {
        RoundRectangle2D rect = new RoundRectangle2D.Double(x, y, width, height, arcWidth, arcHeight);
        fill(rect);
    }

    /**
     * @see Graphics#drawOval(int, int, int, int)
     */
    public void drawOval(int x, int y, int width, int height) {
        Ellipse2D oval = new Ellipse2D.Float(x, y, width, height);
        draw(oval);
    }

    /**
     * @see Graphics#fillOval(int, int, int, int)
     */
    public void fillOval(int x, int y, int width, int height) {
        Ellipse2D oval = new Ellipse2D.Float(x, y, width, height);
        fill(oval);
    }

    /**
     * @see Graphics#drawArc(int, int, int, int, int, int)
     */
    public void drawArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        Arc2D arc = new Arc2D.Double(x, y, width, height, startAngle, arcAngle, Arc2D.OPEN);
        draw(arc);

    }

    /**
     * @see Graphics#fillArc(int, int, int, int, int, int)
     */
    public void fillArc(int x, int y, int width, int height, int startAngle, int arcAngle) {
        Arc2D arc = new Arc2D.Double(x, y, width, height, startAngle, arcAngle, Arc2D.PIE);
        fill(arc);
    }

    /**
     * @see Graphics#drawPolyline(int[], int[], int)
     */
    public void drawPolyline(int[] x, int[] y, int nPoints) {
        PolylineShape polyline = new PolylineShape(x, y, nPoints);
        draw(polyline);
    }

    /**
     * @see Graphics#drawPolygon(int[], int[], int)
     */
    public void drawPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        Polygon poly = new Polygon(xPoints, yPoints, nPoints);
        draw(poly);
    }

    /**
     * @see Graphics#fillPolygon(int[], int[], int)
     */
    public void fillPolygon(int[] xPoints, int[] yPoints, int nPoints) {
        Polygon poly = new Polygon();
        for (int i = 0; i < nPoints; i++) {
            poly.addPoint(xPoints[i], yPoints[i]);
        }
        fill(poly);
    }

    /**
     * @see Graphics#drawImage(Image, int, int, ImageObserver)
     */
    public boolean drawImage(Image img, int x, int y, ImageObserver observer) {
        return drawImage(img, x, y, null, observer);
    }

    /**
     * @see Graphics#drawImage(Image, int, int, int, int, ImageObserver)
     */
    public boolean drawImage(Image img, int x, int y, int width, int height, ImageObserver observer) {
        return drawImage(img, x, y, width, height, null, observer);
    }

    /**
     * @see Graphics#drawImage(Image, int, int, Color, ImageObserver)
     */
    public boolean drawImage(Image img, int x, int y, Color bgcolor, ImageObserver observer) {
        waitForImage(img);
        return drawImage(img, x, y, img.getWidth(observer), img.getHeight(observer), bgcolor, observer);
    }

    /**
     * @see Graphics#drawImage(Image, int, int, int, int, Color, ImageObserver)
     */
    public boolean drawImage(Image img, int x, int y, int width, int height, Color bgcolor, ImageObserver observer) {
        waitForImage(img);
        double scalex = width / (double) img.getWidth(observer);
        double scaley = height / (double) img.getHeight(observer);
        AffineTransform tx = AffineTransform.getTranslateInstance(x, y);
        tx.scale(scalex, scaley);
        return drawImage(img, null, tx, bgcolor, observer);
    }

    /**
     * @see Graphics#drawImage(Image, int, int, int, int, int, int, int, int, ImageObserver)
     */
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2,
            ImageObserver observer) {
        return drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null, observer);
    }

    /**
     * @see Graphics#drawImage(Image, int, int, int, int, int, int, int, int, Color, ImageObserver)
     */
    public boolean drawImage(Image img, int dx1, int dy1, int dx2, int dy2, int sx1, int sy1, int sx2, int sy2,
            Color bgcolor, ImageObserver observer) {
        waitForImage(img);
        double dwidth = (double) dx2 - dx1;
        double dheight = (double) dy2 - dy1;
        double swidth = (double) sx2 - sx1;
        double sheight = (double) sy2 - sy1;

        //if either width or height is 0, then there is nothing to draw
        if (dwidth == 0 || dheight == 0 || swidth == 0 || sheight == 0) {
            return true;
        }

        double scalex = dwidth / swidth;
        double scaley = dheight / sheight;

        double transx = sx1 * scalex;
        double transy = sy1 * scaley;
        AffineTransform tx = AffineTransform.getTranslateInstance(dx1 - transx, dy1 - transy);
        tx.scale(scalex, scaley);

        BufferedImage mask = new BufferedImage(img.getWidth(observer), img.getHeight(observer),
                BufferedImage.TYPE_BYTE_BINARY);
        Graphics g = mask.getGraphics();
        g.fillRect(sx1, sy1, (int) swidth, (int) sheight);
        drawImage(img, mask, tx, null, observer);
        g.dispose();
        return true;
    }

    /**
     * @see Graphics#dispose()
     */
    public void dispose() {
        if (kid) {
            return;
        }
        if (!disposeCalled) {
            disposeCalled = true;
            cb.restoreState();
            cb.restoreState();
            dg2.dispose();
            dg2 = null;
            if (kids != null) {
                ByteBuffer buf = new ByteBuffer();
                internalDispose(buf);
                ByteBuffer buf2 = cb.getInternalBuffer();
                buf2.reset();
                buf2.append(buf);
            }
        }
    }

    private void internalDispose(ByteBuffer buf) {
        int last = 0;
        int pos = 0;
        ByteBuffer buf2 = cb.getInternalBuffer();
        if (kids != null) {
            for (int k = 0; k < kids.size(); k += 2) {
                pos = (Integer) kids.get(k);
                PdfGraphics2D g2 = (PdfGraphics2D) kids.get(k + 1);
                g2.cb.restoreState();
                g2.cb.restoreState();
                buf.append(buf2.getBuffer(), last, pos - last);
                g2.dg2.dispose();
                g2.dg2 = null;
                g2.internalDispose(buf);
                last = pos;
            }
        }
        buf.append(buf2.getBuffer(), last, buf2.size() - last);
    }

    ///////////////////////////////////////////////
    //
    //
    //        implementation specific methods
    //
    //

    /**
     * Enables/Disables the composite font drawer due to issues with custom font mappers that do not always default to
     * one specific font but allow custom fonts.
     *
     * @param compositeFontDrawerEnabled true if the composite font drawer should be used else false.
     */
    public void setCompositeFontDrawerEnabled(boolean compositeFontDrawerEnabled) {
        isCompositeFontDrawerEnabled = compositeFontDrawerEnabled;
    }

    private void followPath(Shape s, int drawType) {
        if (s == null) {
            return;
        }
        if (drawType == STROKE) {
            if (!(stroke instanceof BasicStroke)) {
                s = stroke.createStrokedShape(s);
                followPath(s, FILL);
                return;
            }
        }
        if (drawType == STROKE) {
            setStrokeDiff(stroke, oldStroke);
            oldStroke = stroke;
            setStrokePaint();
        } else if (drawType == FILL) {
            setFillPaint();
        }
        PathIterator points;
        int traces = 0;
        if (drawType == CLIP) {
            points = s.getPathIterator(IDENTITY);
        } else {
            points = s.getPathIterator(transform);
        }
        float[] coords = new float[6];
        while (!points.isDone()) {
            ++traces;
            int segtype = points.currentSegment(coords);
            normalizeY(coords);
            switch (segtype) {
                case PathIterator.SEG_CLOSE:
                    cb.closePath();
                    break;

                case PathIterator.SEG_CUBICTO:
                    cb.curveTo(coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]);
                    break;

                case PathIterator.SEG_LINETO:
                    cb.lineTo(coords[0], coords[1]);
                    break;

                case PathIterator.SEG_MOVETO:
                    cb.moveTo(coords[0], coords[1]);
                    break;

                case PathIterator.SEG_QUADTO:
                    cb.curveTo(coords[0], coords[1], coords[2], coords[3]);
                    break;
            }
            points.next();
        }
        switch (drawType) {
            case FILL:
                if (traces > 0) {
                    if (points.getWindingRule() == PathIterator.WIND_EVEN_ODD) {
                        cb.eoFill();
                    } else {
                        cb.fill();
                    }
                }
                break;
            case STROKE:
                if (traces > 0) {
                    cb.stroke();
                }
                break;
            default: //drawType==CLIP
                if (traces == 0) {
                    cb.rectangle(0, 0, 0, 0);
                }
                if (points.getWindingRule() == PathIterator.WIND_EVEN_ODD) {
                    cb.eoClip();
                } else {
                    cb.clip();
                }
                cb.newPath();
        }
    }

    private float normalizeY(float y) {
        return this.height - y;
    }

    private void normalizeY(float[] coords) {
        coords[1] = normalizeY(coords[1]);
        coords[3] = normalizeY(coords[3]);
        coords[5] = normalizeY(coords[5]);
    }

    private AffineTransform normalizeMatrix() {
        double[] mx = new double[6];
        AffineTransform result = AffineTransform.getTranslateInstance(0, 0);
        result.getMatrix(mx);
        mx[3] = -1;
        mx[5] = height;
        result = new AffineTransform(mx);
        result.concatenate(transform);
        return result;
    }

    private boolean drawImage(Image img, Image mask, AffineTransform xform, Color bgColor, ImageObserver obs) {
        if (xform == null) {
            xform = new AffineTransform();
        } else {
            xform = new AffineTransform(xform);
        }
        xform.translate(0, img.getHeight(obs));
        xform.scale(img.getWidth(obs), img.getHeight(obs));

        AffineTransform inverse = this.normalizeMatrix();
        AffineTransform flipper = AffineTransform.getScaleInstance(1, -1);
        inverse.concatenate(xform);
        inverse.concatenate(flipper);

        double[] mx = new double[6];
        inverse.getMatrix(mx);
        if (currentFillGState != 255) {
            PdfGState gs = fillGState[255];
            if (gs == null) {
                gs = new PdfGState();
                gs.setFillOpacity(1);
                fillGState[255] = gs;
            }
            cb.setGState(gs);
        }

        try {
            com.lowagie.text.Image image = null;
            if (!convertImagesToJPEG) {
                image = com.lowagie.text.Image.getInstance(img, bgColor);
            } else {
                BufferedImage scaled = new BufferedImage(img.getWidth(null), img.getHeight(null),
                        BufferedImage.TYPE_INT_RGB);
                Graphics2D g3 = scaled.createGraphics();
                g3.drawImage(img, 0, 0, img.getWidth(null), img.getHeight(null), null);
                g3.dispose();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageWriteParam iwparam = new JPEGImageWriteParam(Locale.getDefault());
                iwparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                iwparam.setCompressionQuality(jpegQuality); //Set here your compression rate
                ImageWriter iw = ImageIO.getImageWritersByFormatName("jpg").next();
                ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
                iw.setOutput(ios);
                iw.write(null, new IIOImage(scaled, null, null), iwparam);
                iw.dispose();
                ios.close();

                scaled.flush();
                scaled = null;
                image = com.lowagie.text.Image.getInstance(baos.toByteArray());

            }
            if (mask != null) {
                com.lowagie.text.Image msk = com.lowagie.text.Image.getInstance(mask, null, true);
                msk.makeMask();
                msk.setInverted(true);
                image.setImageMask(msk);
            }
            cb.addImage(image, (float) mx[0], (float) mx[1], (float) mx[2], (float) mx[3], (float) mx[4],
                    (float) mx[5]);
            Object url = getRenderingHint(HyperLinkKey.KEY_INSTANCE);
            if (url != null && !url.equals(HyperLinkKey.VALUE_HYPERLINKKEY_OFF)) {
                PdfAction action = new PdfAction(url.toString());
                cb.setAction(action, (float) mx[4], (float) mx[5], (float) (mx[0] + mx[4]), (float) (mx[3] + mx[5]));
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException();
        }
        if (currentFillGState != 255 && currentFillGState != -1) {
            PdfGState gs = fillGState[currentFillGState];
            cb.setGState(gs);
        }
        return true;
    }

    private boolean checkNewPaint(Paint oldPaint) {
        if (paint == oldPaint) {
            return false;
        }
        return !((paint instanceof Color) && paint.equals(oldPaint));
    }

    private void setFillPaint() {
        if (checkNewPaint(paintFill)) {
            paintFill = paint;
            setPaint(false, 0, 0, true);
        }
    }

    private void setStrokePaint() {
        if (checkNewPaint(paintStroke)) {
            paintStroke = paint;
            setPaint(false, 0, 0, false);
        }
    }

    private void setPaint(boolean invert, double xoffset, double yoffset, boolean fill) {
        if (paint instanceof Color color) {
            int alpha = color.getAlpha();
            if (fill) {
                if (alpha != currentFillGState) {
                    currentFillGState = alpha;
                    PdfGState gs = fillGState[alpha];
                    if (gs == null) {
                        gs = new PdfGState();
                        gs.setFillOpacity(alpha / 255f);
                        fillGState[alpha] = gs;
                    }
                    cb.setGState(gs);
                }
                cb.setColorFill(color);
            } else {
                if (alpha != currentStrokeGState) {
                    currentStrokeGState = alpha;
                    PdfGState gs = strokeGState[alpha];
                    if (gs == null) {
                        gs = new PdfGState();
                        gs.setStrokeOpacity(alpha / 255f);
                        strokeGState[alpha] = gs;
                    }
                    cb.setGState(gs);
                }
                cb.setColorStroke(color);
            }
        } else if (paint instanceof GradientPaint gp) {
            Point2D p1 = gp.getPoint1();
            transform.transform(p1, p1);
            Point2D p2 = gp.getPoint2();
            transform.transform(p2, p2);
            Color c1 = gp.getColor1();
            Color c2 = gp.getColor2();
            PdfShading shading = PdfShading.simpleAxial(cb.getPdfWriter(), (float) p1.getX(),
                    normalizeY((float) p1.getY()), (float) p2.getX(), normalizeY((float) p2.getY()), c1, c2);
            PdfShadingPattern pat = new PdfShadingPattern(shading);
            if (fill) {
                cb.setShadingFill(pat);
            } else {
                cb.setShadingStroke(pat);
            }
        } else if (paint instanceof TexturePaint) {
            try {
                TexturePaint tp = (TexturePaint) paint;
                BufferedImage img = tp.getImage();
                Rectangle2D rect = tp.getAnchorRect();
                com.lowagie.text.Image image = com.lowagie.text.Image.getInstance(img, null);
                PdfPatternPainter pattern = cb.createPattern(image.getWidth(), image.getHeight());
                AffineTransform inverse = this.normalizeMatrix();
                inverse.translate(rect.getX(), rect.getY());
                inverse.scale(rect.getWidth() / image.getWidth(), -rect.getHeight() / image.getHeight());
                double[] mx = new double[6];
                inverse.getMatrix(mx);
                pattern.setPatternMatrix((float) mx[0], (float) mx[1], (float) mx[2], (float) mx[3], (float) mx[4],
                        (float) mx[5]);
                image.setAbsolutePosition(0, 0);
                pattern.addImage(image);
                if (fill) {
                    cb.setPatternFill(pattern);
                } else {
                    cb.setPatternStroke(pattern);
                }
            } catch (Exception ex) {
                if (fill) {
                    cb.setColorFill(Color.gray);
                } else {
                    cb.setColorStroke(Color.gray);
                }
            }
        } else {
            try {
                BufferedImage img = null;
                int type = BufferedImage.TYPE_4BYTE_ABGR;
                if (paint.getTransparency() == Transparency.OPAQUE) {
                    type = BufferedImage.TYPE_3BYTE_BGR;
                }
                img = new BufferedImage((int) width, (int) height, type);
                Graphics2D g = (Graphics2D) img.getGraphics();
                g.transform(transform);
                AffineTransform inv = transform.createInverse();
                Shape fillRect = new Rectangle2D.Double(0, 0, img.getWidth(), img.getHeight());
                fillRect = inv.createTransformedShape(fillRect);
                g.setPaint(paint);
                g.fill(fillRect);
                if (invert) {
                    AffineTransform tx = new AffineTransform();
                    tx.scale(1, -1);
                    tx.translate(-xoffset, -yoffset);
                    g.drawImage(img, tx, null);
                }
                g.dispose();
                g = null;
                com.lowagie.text.Image image = com.lowagie.text.Image.getInstance(img, null);
                PdfPatternPainter pattern = cb.createPattern(width, height);
                image.setAbsolutePosition(0, 0);
                pattern.addImage(image);

                if (fill) {
                    if (currentFillGState != 255) {
                        currentFillGState = 255;
                        PdfGState gs = fillGState[255];
                        if (gs == null) {
                            gs = new PdfGState();
                            gs.setFillOpacity(1);
                            fillGState[255] = gs;
                        }
                        cb.setGState(gs);
                    }
                    cb.setPatternFill(pattern);
                } else {
                    cb.setPatternStroke(pattern);
                }
            } catch (Exception ex) {
                if (fill) {
                    cb.setColorFill(Color.gray);
                } else {
                    cb.setColorStroke(Color.gray);
                }
            }
        }
    }

    private synchronized void waitForImage(java.awt.Image image) {
        if (mediaTracker == null) {
            mediaTracker = new MediaTracker(new PdfGraphics2D.FakeComponent());
        }
        mediaTracker.addImage(image, 0);
        try {
            mediaTracker.waitForID(0);
        } catch (InterruptedException e) {
            // empty on purpose
        }
        mediaTracker.removeImage(image);
    }

    /**
     * Split the given string into substrings depending on if all characters of substring can be displayed with the
     * defined font or not.
     *
     * @param s given string
     * @return list of substrings that can be displayed by with the defined font or not.
     */
    private List<String> splitIntoSubstringsByVisibility(String s) {
        List<String> stringParts = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean displayableLastChar = true;
        for (int charIndex = 0; charIndex < s.length(); charIndex++) {
            char c = s.charAt(charIndex);
            boolean b = font.canDisplay(c);
            if (charIndex > 0 && displayableLastChar != b) {
                stringParts.add(sb.toString());
                sb.setLength(0);
            }
            displayableLastChar = b;
            sb.append(c);
        }
        stringParts.add(sb.toString());
        return stringParts;
    }


    static private class FakeComponent extends Component {

        private static final long serialVersionUID = 6450197945596086638L;
    }

    /**
     * @since 2.0.8
     */
    public static class HyperLinkKey extends RenderingHints.Key {

        public static final HyperLinkKey KEY_INSTANCE = new HyperLinkKey(9999);
        public static final Object VALUE_HYPERLINKKEY_OFF = "0";

        protected HyperLinkKey(int arg0) {
            super(arg0);
        }

        public boolean isCompatibleValue(Object val) {
            return true;
        }

        public String toString() {
            return "HyperLinkKey";
        }
    }


    /**
     * Wrapper class that helps to draw string with sun.font.CompositeFont (Windows logical fonts).
     *
     * <p> If the given
     * font is a sun.font.CompositeFont than try to find some font (an implementation of sun.font.Font2D) that will
     * display current text. For some symbols that cannot be displayed with the font from the first slot of the
     * composite font all other font will be checked.
     * <p>
     * This processing is not necessary only for Mac OS - there isn't used "sun.font.CompositeFont", but
     * "sun.font.CFont".
     * <p>
     * Since the <code>sun.*</code> packages are not part of the supported, public interface the reflection will be
     * used.
     */
    private static class CompositeFontDrawer {

        private static final String GET_MODULE_METHOD_NAME = "getModule";
        private static final String IS_OPEN_METHOD_NAME = "isOpen";
        private static final String ADD_OPENS_METHOD_NAME = "addOpens";
        private static final String COMPOSITE_FONT_CLASS_NAME = "sun.font.CompositeFont";
        private static final Class<?> COMPOSITE_FONT_CLASS;
        private static final String GET_NUM_SLOTS_METHOD_NAME = "getNumSlots";
        private static final Method GET_NUM_SLOTS_METHOD;
        private static final String GET_SLOT_FONT_METHOD_NAME = "getSlotFont";
        private static final Method GET_SLOT_FONT_METHOD;
        private static final String FONT_UTILITIES_CLASS_NAME = "sun.font.FontUtilities";
        private static final Class<?> FONT_UTILITIES_CLASS;
        private static final String GET_FONT2D_METHOD_NAME = "getFont2D";
        private static final Method GET_FONT2D_METHOD;
        private static final String FONT2D_CLASS_NAME = "sun.font.Font2D";
        private static final Class<?> FONT2D_CLASS;
        private static final String CAN_DISPLAY_METHOD_NAME = "canDisplay";
        private static final Method CAN_DYSPLAY_METHOD;
        private static final String GET_FONT_NAME_METHOD_NAME = "getFontName";
        private static final Method GET_FONT_NAME_METHOD;
        private static final boolean SUPPORTED;

        static {
            String osName = System.getProperty("os.name", "unknownOS");
            boolean macOS = osName.startsWith("Mac");
            if (!macOS) {
                FONT_UTILITIES_CLASS = getClassForName(FONT_UTILITIES_CLASS_NAME);
                updateModuleToOpenPackage(FONT_UTILITIES_CLASS, "sun.font");
                GET_FONT2D_METHOD = getMethod(FONT_UTILITIES_CLASS, GET_FONT2D_METHOD_NAME, Font.class);
                COMPOSITE_FONT_CLASS = getClassForName(COMPOSITE_FONT_CLASS_NAME);
                GET_NUM_SLOTS_METHOD = getMethod(COMPOSITE_FONT_CLASS, GET_NUM_SLOTS_METHOD_NAME);
                GET_SLOT_FONT_METHOD = getMethod(COMPOSITE_FONT_CLASS, GET_SLOT_FONT_METHOD_NAME, int.class);
                FONT2D_CLASS = getClassForName(FONT2D_CLASS_NAME);
                CAN_DYSPLAY_METHOD = getMethod(FONT2D_CLASS, CAN_DISPLAY_METHOD_NAME, char.class);
                GET_FONT_NAME_METHOD = getMethod(FONT2D_CLASS, GET_FONT_NAME_METHOD_NAME, Locale.class);
            } else {
                FONT_UTILITIES_CLASS = null;
                GET_FONT2D_METHOD = null;
                COMPOSITE_FONT_CLASS = null;
                GET_NUM_SLOTS_METHOD = null;
                GET_SLOT_FONT_METHOD = null;
                FONT2D_CLASS = null;
                CAN_DYSPLAY_METHOD = null;
                GET_FONT_NAME_METHOD = null;
            }

            SUPPORTED = FONT_UTILITIES_CLASS != null && COMPOSITE_FONT_CLASS != null &&
                    FONT2D_CLASS != null && GET_FONT2D_METHOD != null && GET_NUM_SLOTS_METHOD != null &&
                    GET_SLOT_FONT_METHOD != null && CAN_DYSPLAY_METHOD != null && GET_FONT_NAME_METHOD != null;
        }

        private final transient StringBuilder sb = new StringBuilder();
        /**
         * Splitted parts of the string.
         */
        private final transient List<String> stringParts = new ArrayList<>();
        /**
         * {@link BaseFont Base fonts} that corresponds to the splitted part of the string
         */
        private final transient List<BaseFont> correspondingBaseFontsForParts = new ArrayList<>();
        private final transient Map<String, Boolean> fontFamilyComposite = new HashMap<>();

        static boolean isSupported() {
            return SUPPORTED;
        }

        /**
         * Update module of the given class to open the given package to the target module if the target module is
         * opened for the current module.
         *
         * <p>
         * This helps to avoid warnings for the
         * <code>--illegal-access=permit</code>. Actually (java 9-13) "permit" is default mode, but in the future java
         * releases the default mode will be "deny". It's also important to add <code>--add-opens</code> for the given
         * package if it's need.
         */
        private static void updateModuleToOpenPackage(Class<?> classInModule, String packageName) {
            if (classInModule == null || packageName == null) {
                return;
            }
            Method getModuleMethod = getMethod(Class.class, GET_MODULE_METHOD_NAME);
            if (getModuleMethod == null) {
                return;
            }
            try {
                Object targetModule = getModuleMethod.invoke(classInModule);
                if (targetModule == null) {
                    return;
                }
                Class<?> moduleClass = targetModule.getClass();
                Object callerModule = getModuleMethod.invoke(CompositeFontDrawer.class);
                Method isOpenMethod = getMethod(moduleClass, IS_OPEN_METHOD_NAME, String.class, moduleClass);
                if (isOpenMethod == null) {
                    return;
                }
                Object isOpened = isOpenMethod.invoke(targetModule, packageName, callerModule);
                if (isOpened instanceof Boolean && ((Boolean) isOpened)) {
                    Method addOpensMethod = getMethod(moduleClass, ADD_OPENS_METHOD_NAME, String.class, moduleClass);
                    if (callerModule != null) {
                        addOpensMethod.invoke(targetModule, packageName, callerModule);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private static Class<?> getClassForName(String className) {
            try {
                return Class.forName(className);
            } catch (Exception e) {
                return null;
            }
        }

        private static Method getMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
            if (clazz == null) {
                return null;
            }
            Method method;
            try {
                method = clazz.getDeclaredMethod(methodName, parameterTypes);
                if (!method.isAccessible()) {
                    method.setAccessible(true);
                }
            } catch (Exception e) {
                method = null;
            }
            return method;
        }

        /**
         * Check if the given font is a composite font.
         *
         * @param font given font
         * @return <code>true</code> if the given font is sun.font.CompositeFont. <code>False</code>
         * otherwise.
         */
        boolean isCompositeFont(Font font) {
            if (!isSupported() || font == null) {
                assert false;
                return false;
            }
            String fontFamily = font.getFamily();
            if (fontFamily != null && fontFamilyComposite.containsKey(fontFamily)) {
                return fontFamilyComposite.get(fontFamily);
            }

            try {
                Object result = GET_FONT2D_METHOD.invoke(null, font);
                boolean composite = result != null && result.getClass() == COMPOSITE_FONT_CLASS;
                if (fontFamily != null) {
                    fontFamilyComposite.put(fontFamily, composite);
                }
                return composite;
            } catch (Exception e) {
                return false;
            }
        }

        /**
         * Draw text with the given font at the specified position.
         * <p>This method splits the string into parts so that it
         * can be displayed with a matching (font that can display all symbols of this part of string) slot font.
         * <p>
         * If some class/method cannot be found or throw exception the default drawing string function will be used for
         * a drawing string.
         *
         * @param s                      given string that should be drawn
         * @param compositeFont          composite font. This font should be an instance of composite font, otherwise
         *                               the default drawing function will be called.
         * @param x                      the x coordinate of the location
         * @param y                      the y coordinate of the location
         * @param fontConverter          function that convert {@link java.awt.Font font} to the needed
         *                               {@link com.lowagie.text.pdf.BaseFont base font}
         * @param defaultDrawingFunction default drawing function that will be used for drawing string.
         * @return width of the drawn string.
         */
        double drawString(String s, Font compositeFont, double x, double y, Function<Font, BaseFont> fontConverter,
                DrawStringFunction defaultDrawingFunction) {
            String fontFamily = compositeFont.getFamily();
            if (!isSupported() || (fontFamily != null && !fontFamilyComposite.get(fontFamily))) {
                assert false;
                return defaultDrawingFunction
                        .drawString(s, fontConverter.apply(compositeFont), x, y);
            }

            try {
                splitStringIntoDisplayableParts(s, compositeFont, fontConverter);
                double width = 0;
                for (int i = 0; i < stringParts.size(); i++) {
                    String strPart = stringParts.get(i);
                    BaseFont correspondingBaseFont = correspondingBaseFontsForParts.get(i);
                    BaseFont baseFont = correspondingBaseFont == null
                            ? fontConverter.apply(compositeFont)
                            : correspondingBaseFont;
                    width += defaultDrawingFunction.drawString(strPart, baseFont, x + width, y);
                }
                return width;
            } catch (Exception e) {
                BaseFont baseFont = fontConverter.apply(compositeFont);
                return defaultDrawingFunction.drawString(s, baseFont, x, y);
            }
        }

        /**
         * Split string into visible and not visible parts.
         * <p>
         * This method split string into substring parts. For each splitted part correspond found
         * {@link BaseFont base font} from the slots of the composite font witch can display all characters of the part
         * of string. If no font found the {@link BaseFont base font} from the own composite font will be used.
         *
         * @param s
         * @param compositeFont
         * @param fontConverter
         * @throws IllegalAccessException
         * @throws IllegalArgumentException
         * @throws InvocationTargetException
         */
        private void splitStringIntoDisplayableParts(String s, Font compositeFont,
                Function<Font, BaseFont> fontConverter)
                throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
            Object result = GET_FONT2D_METHOD.invoke(null, compositeFont);
            if (result.getClass() != COMPOSITE_FONT_CLASS) {
                throw new IllegalArgumentException("Given font isn't a composite font.");
            }
            sb.setLength(0);
            stringParts.clear();
            correspondingBaseFontsForParts.clear();
            BaseFont baseFontFromCompositeFont = fontConverter.apply(compositeFont);
            BaseFont lastBaseFont = null;
            Object numSlotsResult = GET_NUM_SLOTS_METHOD.invoke(result);
            int numSlots = (Integer) numSlotsResult;
            for (int charIndex = 0; charIndex < s.length(); charIndex++) {
                char c = s.charAt(charIndex);
                boolean found = false;
                for (int slotIndex = 0; slotIndex < numSlots; slotIndex++) {
                    Object phFont = GET_SLOT_FONT_METHOD.invoke(result, slotIndex);
                    if (phFont == null) {
                        continue;
                    }
                    Boolean canBeDysplayedByPhysicalFont = (Boolean) CAN_DYSPLAY_METHOD.invoke(phFont, c);
                    if (canBeDysplayedByPhysicalFont) {
                        Object fontNameResult = GET_FONT_NAME_METHOD.invoke(phFont, (Locale) null);
                        Font font = new Font((String) fontNameResult, compositeFont.getStyle(),
                                compositeFont.getSize());
                        BaseFont correspondingBaseFont = fontConverter.apply(font);
                        if (correspondingBaseFont != null && correspondingBaseFont.charExists(c)) {
                            if (sb.length() == 0) {
                                correspondingBaseFontsForParts.add(correspondingBaseFont);
                                lastBaseFont = correspondingBaseFont;
                            } else if (!Objects.equals(lastBaseFont, correspondingBaseFont)) {
                                stringParts.add(sb.toString());
                                sb.setLength(0);
                                correspondingBaseFontsForParts.add(correspondingBaseFont);
                                lastBaseFont = correspondingBaseFont;
                            }
                            sb.append(c);
                            found = true;
                            break;
                        }
                    }
                }
                if (!found) {
                    if (sb.length() == 0) {
                        correspondingBaseFontsForParts.add(baseFontFromCompositeFont);
                        lastBaseFont = null;
                    } else if (lastBaseFont != null) {
                        stringParts.add(sb.toString());
                        sb.setLength(0);
                        correspondingBaseFontsForParts.add(baseFontFromCompositeFont);
                        lastBaseFont = null;
                    }
                    sb.append(c);
                }
            }
            stringParts.add(sb.toString());
            sb.setLength(0);
        }

        @FunctionalInterface
        public interface DrawStringFunction {

            double drawString(String s, BaseFont basicFont, double x, double y);
        }
    }
}
