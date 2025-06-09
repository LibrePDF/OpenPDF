/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci
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
package org.openpdf.simple;

import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;
import org.openpdf.layout.SharedContext;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.openpdf.util.ImageUtil.withGraphics;


/**
 * <p>
 * Graphics2DRenderer supports headless rendering of XHTML documents, and is useful
 * for rendering documents directly to images.</p>
 * <p>
 * <p>Graphics2DRenderer supports the {@link XHTMLPanel#setDocument(Document)},
 * {@link XHTMLPanel#doLayout()} method from
 * {@link XHTMLPanel}, as well as easy-to-use static utility methods.
 * For example, to render a document in an image that is 600 pixels wide use the
 * {@link #renderToImageAutoSize(String,int,int)} method like this:</p>
 *
 * <pre>{@code
 * BufferedImage img = Graphics2DRenderer.renderToImage( "test.xhtml", width);
 * }</pre>
 *
 * @author Joshua Marinacci
 */
public final class Graphics2DRenderer {
    /**
     * The panel we are using to render the document.
     */
    private final XHTMLPanel panel;

    public Graphics2DRenderer(String url) {
        panel = new XHTMLPanel();
        panel.setInteractive(false);
        panel.setDocument(url);
    }

    public Graphics2DRenderer(Document doc, String base_url) {
        panel = new XHTMLPanel();
        panel.setInteractive(false);
        panel.setDocument(doc, base_url);
    }

    // ASK maybe we could change the graphics2d to be a font rendering context?
    /**
     * Lay out the document with the specified dimensions,
     * without rendering.
     *
     * @param g2  the canvas to layout on.
     * @param dim dimensions of the container for the document
     */
    public void layout(Graphics2D g2, @Nullable Dimension dim) {
        if (dim != null) {
            panel.setSize(dim);
        }
        panel.doDocumentLayout(g2);
    }


    /**
     * Renders the document to the given canvas. Call layout() first.
     *
     * @param g2 Canvas to render to.
     */
    public void render(Graphics2D g2) {
        if (g2.getClip() == null) {
            g2.setClip(getMinimumSize());
        }
        panel.paintComponent(g2);
    }

    /**
     * Returns the size image needed to render the document without anything
     * going off the side. Could be different from the dimensions passed into
     * layout because of text that couldn't break or a table that's set to be
     * too big.
     *
     * @return A rectangle sized to the minimum size required for the
     * document.
     */
    public Rectangle getMinimumSize() {
        if (panel.getPreferredSize() != null) {
            return new Rectangle(0, 0,
                    (int) panel.getPreferredSize().getWidth(),
                    (int) panel.getPreferredSize().getHeight());
        } else {
            return new Rectangle(0, 0, panel.getWidth(), panel.getHeight());
        }
    }

    /**
     * Gets the SharedContext for layout and rendering.
     */
    public SharedContext getSharedContext() {
        return panel.getSharedContext();
    }

    /** Returns the panel used internally for rendering. */
    public XHTMLPanel getPanel() {
        return panel;
    }

    /**
     * A static utility method to automatically create an image from a
     * document; the image supports transparency. To render an image that does not support transparency,
     * use the overloaded version of this method {@link #renderToImage(String, int, int, int)}.
     *
     * @param url    URL for the document to render.
     * @param width  Width in pixels of the layout container
     * @param height Height in pixels of the layout container
     * @return Returns an Image containing the rendered document.
     */
    public static BufferedImage renderToImage(String url, int width, int height) {
        return renderToImage(url, width, height, BufferedImage.TYPE_INT_ARGB);
    }

    /**
     * A static utility method to automatically create an image from a
     * document. The buffered image type must be specified.
     *
     * @param url    URL for the document to render.
     * @param width  Width in pixels of the layout container
     * @param height Height in pixels of the layout container
     * @param bufferedImageType On of the pre-defined image types for a java.awt.image.BufferedImage, such
     * as TYPE_INT_ARGB or TYPE_INT_RGB.
     * @return Returns an Image containing the rendered document.
     */
    public static BufferedImage renderToImage(String url, int width, int height, int bufferedImageType) {
        Graphics2DRenderer g2r = new Graphics2DRenderer(url);

        Dimension dim = new Dimension(width, height);
        BufferedImage buff = new BufferedImage((int) dim.getWidth(), (int) dim.getHeight(), bufferedImageType);
        withGraphics(buff, g -> {
            g2r.layout(g, dim);
            g2r.render(g);
        });
        return buff;
    }

        /**
     * A static utility method to automatically create an image from a
     * document, where height is determined based on document content.
     * To estimate a size before rendering, use {@link #getMinimumSize()}.
     * The rendered image supports transparency.
     *
     * @param url    java.net.URL for the document to render.
     * @param width  Width in pixels of the layout container
     * @return Returns java.awt.Image containing the rendered document.
     */
    public static BufferedImage renderToImageAutoSize(String url, int width){
            return renderToImageAutoSize(url, width, BufferedImage.TYPE_INT_ARGB);
        }

    /**
     * A static utility method to automatically create an image from a
     * document, where height is determined based on document content.
     * To estimate a size before rendering, use {@link #getMinimumSize()}.
     *
     * @param url    java.net.URL for the document to render.
     * @param width  Width in pixels of the layout container
     * @param bufferedImageType On of the pre-defined image types for a java.awt.image.BufferedImage, such
     * as TYPE_INT_ARGB or TYPE_INT_RGB.
     * @return Returns java.awt.Image containing the rendered document.
     */
    public static BufferedImage renderToImageAutoSize(String url, int width, int bufferedImageType) {
        Graphics2DRenderer g2r = new Graphics2DRenderer(url);
        Dimension dim = new Dimension(width, 1000);

        // do layout with temp buffer
        BufferedImage tempBuffer = new BufferedImage((int) dim.getWidth(), (int) dim.getHeight(), bufferedImageType);
        withGraphics(tempBuffer, g -> g2r.layout(g, new Dimension(width, 1000)));

        // get size
        Rectangle rect = g2r.getMinimumSize();

        // render into real buffer
        BufferedImage buff = new BufferedImage((int) rect.getWidth(), (int) rect.getHeight(), bufferedImageType);
        withGraphics(buff, g -> g2r.render(g));

        // return real buffer
        return buff;
    }
}
