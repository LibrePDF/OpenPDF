/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci, Torbjoern Gannholm
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
package org.openpdf.swing;

import com.google.errorprone.annotations.CheckReturnValue;
import org.openpdf.extend.FSGlyphVector;
import org.openpdf.extend.FontContext;
import org.openpdf.extend.OutputDevice;
import org.openpdf.extend.TextRenderer;
import org.openpdf.render.FSFont;
import org.openpdf.render.FSFontMetrics;
import org.openpdf.render.JustificationInfo;
import org.openpdf.render.LineMetricsAdapter;
import org.openpdf.util.Configuration;

import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.geom.Point2D;
import java.util.Map;


/**
 * Renders to a Graphics2D instance.
 *
 * @author   Joshua Marinacci
 * @author   Torbjoern Gannholm
 */
public class Java2DTextRenderer implements TextRenderer {
    private float scale;
    private float threshold;
    private Object antiAliasRenderingHint;
    private final Object fractionalFontMetricsHint;

    public Java2DTextRenderer() {
        scale = Configuration.valueAsFloat("xr.text.scale", 1.0f);
        threshold = Configuration.valueAsFloat("xr.text.aa-fontsize-threshhold", 0);

        Object dummy = new Object();

        Object aaHint = Configuration.valueFromClassConstant("xr.text.aa-rendering-hint", dummy);
        if (aaHint == dummy) {
            try {
                // we should be able to look up the "recommended" AA settings (that correspond to the user's
                // desktop preferences and machine capabilities
                // see: http://java.sun.com/javase/6/docs/api/java/awt/doc-files/DesktopProperties.html
                Map<RenderingHints.Key, Object> map = getFontDesktopHints();
                antiAliasRenderingHint = map.get(RenderingHints.KEY_TEXT_ANTIALIASING);
            } catch (Exception e) {
                // conceivably could get an exception in a webstart environment? not sure
                antiAliasRenderingHint = RenderingHints.VALUE_TEXT_ANTIALIAS_ON;
            }
        } else {
            antiAliasRenderingHint = aaHint;
        }
        if("true".equals(Configuration.valueFor("xr.text.fractional-font-metrics", "false"))) {
            fractionalFontMetricsHint = RenderingHints.VALUE_FRACTIONALMETRICS_ON;
        } else {
            fractionalFontMetricsHint = RenderingHints.VALUE_FRACTIONALMETRICS_OFF;
        }
    }

    @CheckReturnValue
    @SuppressWarnings("unchecked")
    private static Map<RenderingHints.Key, Object> getFontDesktopHints() {
        Toolkit tk = Toolkit.getDefaultToolkit();
        return (Map<RenderingHints.Key, Object>) tk.getDesktopProperty("awt.font.desktophints");
    }

    @Override
    public void drawString(OutputDevice outputDevice, String string, float x, float y ) {
        Object aaHint = null;
        Graphics2D graphics = ((Java2DOutputDevice)outputDevice).getGraphics();
        if ( graphics.getFont().getSize() > threshold ) {
            aaHint = graphics.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
            graphics.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, antiAliasRenderingHint );
        }
        Object fracHint = graphics.getRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, fractionalFontMetricsHint);
        graphics.drawString( string, (int)x, (int)y );
        if ( graphics.getFont().getSize() > threshold ) {
            graphics.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, aaHint );
        }
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, fracHint);
    }

    @Override
    public void drawString(
            OutputDevice outputDevice, String string, float x, float y, JustificationInfo info) {
        Object aaHint = null;
        Graphics2D graphics = ((Java2DOutputDevice)outputDevice).getGraphics();
        if ( graphics.getFont().getSize() > threshold ) {
            aaHint = graphics.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
            graphics.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, antiAliasRenderingHint );
        }
        Object fracHint = graphics.getRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, fractionalFontMetricsHint);

        GlyphVector vector = graphics.getFont().createGlyphVector(
                graphics.getFontRenderContext(), string);

        adjustGlyphPositions(string, info, vector);

        graphics.drawGlyphVector(vector, x, y);

        if ( graphics.getFont().getSize() > threshold ) {
            graphics.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, aaHint );
        }
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, fracHint);
    }

    private void adjustGlyphPositions(
            String string, JustificationInfo info, GlyphVector vector) {
        float adjust = 0.0f;
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (i != 0) {
                Point2D point = vector.getGlyphPosition(i);
                vector.setGlyphPosition(
                        i, new Point2D.Double(point.getX() + adjust, point.getY()));
            }
            if (c == ' ' || c == '\u00a0' || c == '\u3000') {
                adjust += info.spaceAdjust();
            } else {
                adjust += info.nonSpaceAdjust();
            }
        }
    }

    @Override
    public void drawGlyphVector(OutputDevice outputDevice, FSGlyphVector fsGlyphVector, float x, float y ) {
        Object aaHint = null;
        Graphics2D graphics = ((Java2DOutputDevice)outputDevice).getGraphics();

        if ( graphics.getFont().getSize() > threshold ) {
            aaHint = graphics.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
            graphics.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, antiAliasRenderingHint );
        }
        Object fracHint = graphics.getRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, fractionalFontMetricsHint);

        GlyphVector vector = ((AWTFSGlyphVector)fsGlyphVector).getGlyphVector();
        graphics.drawGlyphVector(vector, (int)x, (int)y );
        if ( graphics.getFont().getSize() > threshold ) {
            graphics.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, aaHint );
        }
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, fracHint);
    }

    @Override
    public void setup(FontContext fontContext) {
//        ((Java2DFontContext)fontContext).getGraphics().setRenderingHint(
//                RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF );
    }

    @Override
    public void setFontScale(float scale ) {
        this.scale = scale;
    }

    @Override
    public void setSmoothingThreshold(float fontsize ) {
        threshold = fontsize;
    }

    @CheckReturnValue
    @Override
    public FSFontMetrics getFSFontMetrics(FontContext fc, FSFont font, String string ) {
        Graphics2D graphics = ((Java2DFontContext)fc).getGraphics();
        Object fracHint = graphics.getRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, fractionalFontMetricsHint);
        LineMetricsAdapter adapter = new LineMetricsAdapter(
                ((AWTFSFont)font).getAWTFont().getLineMetrics(
                        string, graphics.getFontRenderContext()));
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, fracHint);
        return adapter;
    }

    @Override
    public int getWidth(FontContext fc, FSFont font, String string) {
        Graphics2D graphics = ((Java2DFontContext)fc).getGraphics();
        Object fracHint = graphics.getRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, fractionalFontMetricsHint);
        Font awtFont = ((AWTFSFont)font).getAWTFont();
        int width;
        if(fractionalFontMetricsHint == RenderingHints.VALUE_FRACTIONALMETRICS_ON) {
            width = (int)Math.round(
                    graphics.getFontMetrics(awtFont).getStringBounds(string, graphics).getWidth());
        } else {
            width = (int)Math.ceil(
                    graphics.getFontMetrics(awtFont).getStringBounds(string, graphics).getWidth());
        }
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, fracHint);
        return width;
    }

    @Override
    public float getFontScale() {
        return this.scale;
    }

    /**
     * If anti-alias text is enabled, the value from RenderingHints to use for AA smoothing in Java2D. Defaults to
     * {@link java.awt.RenderingHints#VALUE_TEXT_ANTIALIAS_ON}.
     *
     * @return Current AA rendering hint
     */
    public Object getRenderingHints() {
        return antiAliasRenderingHint;
    }

    /**
     * If anti-alias text is enabled, the value from RenderingHints to use for AA smoothing in Java2D. Defaults to
     * {@link java.awt.RenderingHints#VALUE_TEXT_ANTIALIAS_ON}.
     *
     * @param renderingHints  rendering hint for AA smoothing in Java2D
     */
    public void setRenderingHints(Object renderingHints) {
        this.antiAliasRenderingHint = renderingHints;
    }

    public float[] getGlyphPositions(OutputDevice outputDevice, FSFont font, String text) {
        Object aaHint = null;
        Graphics2D graphics = ((Java2DOutputDevice)outputDevice).getGraphics();
        Font awtFont = ((AWTFSFont)font).getAWTFont();

        if (awtFont.getSize() > threshold ) {
            aaHint = graphics.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
            graphics.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, antiAliasRenderingHint );
        }
        Object fracHint = graphics.getRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, fractionalFontMetricsHint);

        GlyphVector vector = awtFont.createGlyphVector(
                graphics.getFontRenderContext(),
                text);
        float[] result = vector.getGlyphPositions(0, text.length() + 1, null);

        if (awtFont.getSize() > threshold ) {
            graphics.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, aaHint );
        }
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, fracHint);

        return result;
    }

    @Override
    public Rectangle getGlyphBounds(OutputDevice outputDevice, FSFont font, FSGlyphVector fsGlyphVector, int index, float x, float y) {
        Object aaHint = null;
        Graphics2D graphics = ((Java2DOutputDevice)outputDevice).getGraphics();
        Font awtFont = ((AWTFSFont)font).getAWTFont();

        if (awtFont.getSize() > threshold ) {
            aaHint = graphics.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
            graphics.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, antiAliasRenderingHint );
        }
        Object fracHint = graphics.getRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, fractionalFontMetricsHint);

        GlyphVector vector = ((AWTFSGlyphVector)fsGlyphVector).getGlyphVector();

        Rectangle result = vector.getGlyphPixelBounds(index, graphics.getFontRenderContext(), x, y);

        if (awtFont.getSize() > threshold ) {
            graphics.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, aaHint );
        }
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, fracHint);

        return result;
    }

    @Override
    public float[] getGlyphPositions(OutputDevice outputDevice, FSFont font, FSGlyphVector fsGlyphVector) {
        Object aaHint = null;
        Graphics2D graphics = ((Java2DOutputDevice)outputDevice).getGraphics();
        Font awtFont = ((AWTFSFont)font).getAWTFont();

        if (awtFont.getSize() > threshold ) {
            aaHint = graphics.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
            graphics.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, antiAliasRenderingHint );
        }
        Object fracHint = graphics.getRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, fractionalFontMetricsHint);

        GlyphVector vector = ((AWTFSGlyphVector)fsGlyphVector).getGlyphVector();

        float[] result = vector.getGlyphPositions(0, vector.getNumGlyphs() + 1, null);

        if (awtFont.getSize() > threshold ) {
            graphics.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, aaHint );
        }
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, fracHint);

        return result;
    }

    @Override
    public FSGlyphVector getGlyphVector(OutputDevice outputDevice, FSFont font, String text) {
        Object aaHint = null;
        Graphics2D graphics = ((Java2DOutputDevice)outputDevice).getGraphics();
        Font awtFont = ((AWTFSFont)font).getAWTFont();

        if (awtFont.getSize() > threshold ) {
            aaHint = graphics.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
            graphics.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, antiAliasRenderingHint );
        }
        Object fracHint = graphics.getRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, fractionalFontMetricsHint);

        GlyphVector vector = awtFont.createGlyphVector(
                graphics.getFontRenderContext(),
                text);

        if (awtFont.getSize() > threshold ) {
            graphics.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, aaHint );
        }
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, fracHint);

        return new AWTFSGlyphVector(vector);
    }
}

