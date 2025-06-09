/*
 * {{{ header & license
 * Copyright (c) 2007 Christophe Marchand
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */

package org.openpdf.swing;

import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;
import org.openpdf.extend.UserAgentCallback;
import org.openpdf.layout.Layer;
import org.openpdf.layout.PaintingInfo;
import org.openpdf.render.Box;
import org.openpdf.render.RenderingContext;
import org.openpdf.simple.XHTMLPanel;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.print.PrinterGraphics;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * ScalableXHTMLPanel extends {@link org.openpdf.simple.XHTMLPanel} to allow zoom on output.
 *
 * @author chm
 */
public class ScalableXHTMLPanel extends XHTMLPanel {

    public static final int SCALE_POLICY_NONE = 0;
    public static final int SCALE_POLICY_FIT_WIDTH = 0x01;
    public static final int SCALE_POLICY_FIT_HEIGHT = 0x02;
    public static final int SCALE_POLICY_FIT_WHOLE = SCALE_POLICY_FIT_WIDTH + SCALE_POLICY_FIT_HEIGHT;

    private int scalePolicy = SCALE_POLICY_NONE;
    private double scale = -1.0d;
    private final List<ScaleChangeListener> scListeners = new ArrayList<>();

    /**
     * The lastly calculated layout size
     */
    @Nullable
    private Dimension lastLayoutSize;

    /**
     * Instantiates a panel with a custom {@link org.openpdf.extend.UserAgentCallback}
     * implementation.
     *
     * @param uac The custom UserAgentCallback implementation.
     */
    public ScalableXHTMLPanel(UserAgentCallback uac) {
        super(uac);
    }

    /**
     * Renders a Document using a URL as a base URL for relative
     * paths.
     *
     * @param doc The new document value
     * @param url The new document value
     */
    @Override
    public void setDocument(Document doc, @Nullable String url) {
        resetScaleAccordingToPolicy();
        lastLayoutSize = null;
        super.setDocument(doc, url);
    }

    /**
     * Renders a Document read from an InputStream using a URL
     * as a base URL for relative paths.
     *
     * @param stream The stream to read the Document from.
     * @param url	The URL used to resolve relative path references.
     */
    @Override
    public void setDocument(InputStream stream, String url) {
        resetScaleAccordingToPolicy();
        lastLayoutSize = null;
        super.setDocument(stream, url);
    }

    private void resetScaleAccordingToPolicy() {
        if (getScalePolicy() != SCALE_POLICY_NONE) scale = -1.0d;
    }

    /**
     * Search Box according to scale factor
     *
     * @param x The displayed x position
     * @param y the displayed y position
     */
    @Override
    @Nullable
    public Box find(int x, int y) {
        Point p = convertFromScaled(x, y);
        Layer l = getRootLayer();
        if (l != null) {
            return l.find(getLayoutContext(), p.x, p.y, false);
        }
        return null;
    }

    /**
     * Force scale to use
     *
     * @param newScale The scale to use
     * @throws IllegalArgumentException If {@code newScale <= <tt>0.0d</tt>}.
     */
    public void setScale(double newScale) throws IllegalArgumentException {
        if (newScale <= 0.0d) throw new IllegalArgumentException("Only positive scales are allowed.");
        this.scale = newScale;
        scalePolicy = SCALE_POLICY_NONE;
        lastLayoutSize = null;
        repaint(getFixedRectangle());
        scaleChanged();
    }

    public double getScale() {
        return scale;
    }

    public void addScaleChangeListener(ScaleChangeListener scl) {
        scListeners.add(scl);
    }

    public void removeScaleChangeListener(ScaleChangeListener scl) {
        scListeners.remove(scl);
    }

    private void scaleChanged() {
        ScaleChangeEvent evt = new ScaleChangeEvent(this, scale);
        for (ScaleChangeListener scListener : scListeners) {
            scListener.scaleChanged(evt);
        }
    }

    /**
     * Renders according to scale factor
     *
     * @param c	the RenderingContext to use
     * @param root The Layer to render
     */
    @Override
    protected void doRender(RenderingContext c, Layer root) {
        Graphics2D g = ((Java2DOutputDevice) c.getOutputDevice()).getGraphics();
        if (!(g instanceof PrinterGraphics) && isOpaque()) {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        AffineTransform current = g.getTransform();

        PaintingInfo pI = root.getMaster().getPaintingInfo();
        if (pI != null) {
            Dimension layoutSize = pI.getOuterMarginCorner();

            calculateScaleAccordingToPolicy(layoutSize);

            if (lastLayoutSize == null) {
                lastLayoutSize = layoutSize;
                setPreferredSize(new Dimension((int) (lastLayoutSize.width * scale), (int) (lastLayoutSize.height * scale)));
                revalidate();
            }
        }

        g.transform(AffineTransform.getScaleInstance(scale, scale));
        super.doRender(c, root);
        g.setTransform(current);
    }

    protected void calculateScaleAccordingToPolicy(Dimension layoutSize) {
        Rectangle viewportBounds = getFixedRectangle();
        if (getScalePolicy() == SCALE_POLICY_NONE) {
            // FIXME: float comparison
            if (scale == -1.0d) scale = 1.0d;
            return;
        }
        double xScale, yScale;
        if (viewportBounds.width < layoutSize.width) {
            xScale = (double) viewportBounds.width / layoutSize.width;
        } else {
            xScale = 1.0d;
        }
        if (viewportBounds.height < layoutSize.height) {
            yScale = (double) viewportBounds.height / layoutSize.height;
        } else {
            yScale = 1.0d;
        }
        if (getScalePolicy() == SCALE_POLICY_FIT_WIDTH) scale = xScale;
        else if (getScalePolicy() == SCALE_POLICY_FIT_HEIGHT) scale = yScale;
        else scale = Math.min(xScale, yScale);
    }

    protected Point convertToScaled(Point origin) {
        if (scale <= 0.0d) return origin;
        return new Point((int) (origin.x * scale), (int) (origin.y * scale));
    }

    protected Point convertFromScaled(Point origin) {
        if (scale <= 0.0d) return origin;
        return new Point((int) (origin.x / scale), (int) (origin.y / scale));
    }

    protected Point convertToScaled(int x, int y) {
        if (scale <= 0.0d) return new Point(x, y);
        return new Point((int) (x * scale), (int) (y * scale));
    }

    protected Point convertFromScaled(int x, int y) {
        if (scale <= 0.0d) return new Point(x, y);
        return new Point((int) (x / scale), (int) (y / scale));
    }

    public int getScalePolicy() {
        return scalePolicy;
    }

    public void setScalePolicy(int scalePolicy) {
        this.scalePolicy = scalePolicy;
        lastLayoutSize = null;
        repaint(getFixedRectangle());
    }
}
