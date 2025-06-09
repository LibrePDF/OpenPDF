/*
 * {{{ header & license
 * Copyright (c) 2007 Patrick Wright
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
package org.openpdf.simple;

import org.openpdf.swing.Java2DRenderer;
import org.openpdf.util.FSImageWriter;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Paths;

import static java.nio.file.Files.newOutputStream;

/**
 * <p>
 * ImageRenderer supports rendering of XHTML documents to image formats, writing out the generated image to an output stream
 * or a file in a given image format. There are two static utility methods, one for rendering
 * a {@link java.net.URL}, {@link #renderToImage(String, String, int)} and one
 * for rendering a {@link java.io.File}, {@link #renderToImage(File, String, int)}</p>
 *
 * <p>You can use this utility from the command line by passing in
 * the URL or file location as first parameter, and output file path as second
 * parameter:
 * <pre>{@code
 * java -cp %classpath% org.openpdf.simple.ImageRenderer <url> <img>
 * }</pre>
 * <p>If the second parameters is not provided, a PNG-format image will be created
 * in the same directory as the source (if source is a file) or as a temp file
 * in the standard temp directory; the output file name will be printed out
 * in either case.</p>
 *
 * <p>Image width must always be supplied; height is determined automatically.</p>
 *
 * @author Pete Brant
 * @author Patrick Wright
 */
public class ImageRenderer {
    /**
     * Renders the XML file at the given URL as an image file at the target location. Width must be provided,
     * height is determined automatically based on content and CSS.
     *
     * @param url	 url for the XML file to render
     * @param path path to the PDF file to create
     * @param width Width in pixels to which the document should be constrained.
     *
     * @throws java.io.IOException if the input URL, or output path location is invalid
     */
    public static BufferedImage renderToImage(String url, String path, int width) throws IOException {
        try (OutputStream os = new BufferedOutputStream(newOutputStream(Paths.get(path)))) {
            Java2DRenderer renderer = new Java2DRenderer(url, url, width);
            BufferedImage image = renderer.getImage();
            new FSImageWriter().write(image, os);
            return image;
        }
    }

    /**
     * Renders the XML file as an image file at the target location. Width must be provided, height is determined
     * automatically based on content and CSS.
     *
     * @param xhtmlFile  XML file to render
     * @param path path to the image file to create
     * @param width Width in pixels to which the document should be constrained.
     *
     * @throws java.io.IOException if the input URL, or output path location is invalid
     */
    public static BufferedImage renderToImage(File xhtmlFile, String path, int width) throws IOException {
        return renderToImage(xhtmlFile.toURI().toURL().toExternalForm(), path, width);
    }

    public static BufferedImage renderToImage(URL xhtmlUrl, String path, int width) throws IOException {
        return renderToImage(xhtmlUrl.toExternalForm(), path, width);
    }
}
