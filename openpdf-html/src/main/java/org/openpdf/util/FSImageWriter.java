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
package org.openpdf.util;

import org.jspecify.annotations.Nullable;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import static java.nio.file.Files.newOutputStream;

/**
 * <p>Writes out BufferedImages to some output stream, like a file. Allows image writer parameters to be specified and
 * thus controlled. Uses the java ImageIO libraries--see {@link javax.imageio.ImageIO} and related classes,
 * especially {@link javax.imageio.ImageWriter}.</p>
 * <p>
 * By default, FSImageWriter writes BufferedImages out in PNG format. The simplest possible usage is
 * <pre>
 * FSImageWriter writer = new FSImageWriter();
 * writer.write(img, new File("image.png"));
 * </pre>
 * <p>
 * <p>You can set the image format in the constructor ({@link org.openpdf.util.FSImageWriter#FSImageWriter(String)},
 * and can set compression settings using various setters; this lets you create writer to reuse across a number
 * of images, all output at the same compression level. Note that not all image formats support compression. For
 * those that do, you may need to set more than one compression setting, in combination, for it to work. For JPG,
 * it might look like this</p>
 * <pre>
 *      writer = new FSImageWriter("jpg");
 * 		writer.setWriteCompressionMode(ImageWriteParam.MODE_EXPLICIT);
 * 		writer.setWriteCompressionType("JPEG");
 * 		writer.setWriteCompressionQuality(.75f);
 * </pre>
 */
public class FSImageWriter {
    private final String imageFormat;
    private final float writeCompressionQuality;
    private final int writeCompressionMode;
    @Nullable
    private final String writeCompressionType;

    /**
     * New image writer for the PNG image format
     */
    public FSImageWriter() {
        this("png");
    }

    /**
     * New writer for a given image format, using the informal format name.
     *
     * @param imageFormat Informal image format name, e.g. "jpg", "png", "bmp"; usually the part that appears
     *                    as the file extension.
     */
    public FSImageWriter(String imageFormat) {
        this.imageFormat = imageFormat;
        this.writeCompressionMode = ImageWriteParam.MODE_COPY_FROM_METADATA;
        this.writeCompressionType = null;
        this.writeCompressionQuality = 1.0f;
    }

    /**
     * Writes the image out to the target file, creating the file if necessary, or overwriting if it already
     * exists.
     *
     * @param image     Image to write.
     * @param filePath Path for file to write. The extension for the file name is not changed; it is up to the
     *                 caller to make sure this corresponds to the image format.
     * @throws IOException If the file could not be written.
     */
    public void write(BufferedImage image, String filePath) throws IOException {
        File file = new File(filePath);
        if (file.exists()) {
            if (!file.delete()) {
                throw new IOException("File " + filePath + " exists already, and call to .delete() failed " +
                        "unexpectedly");
            }
        } else {
            if (!file.createNewFile()) {
                throw new IOException("Unable to create file at path " + filePath + ", call to .createNewFile() " +
                        "failed unexpectedly.");
            }
        }

        try (OutputStream fos = new BufferedOutputStream(newOutputStream(file.toPath()))) {
            write(image, fos);
        }
    }

    /**
     * Writes the image out to the target file, creating the file if necessary, or overwriting if it already
     * exists.
     *
     * @param image     Image to write.
     * @param os output stream to write to
     * @throws IOException If the file could not be written.
     */
    public void write(BufferedImage image, OutputStream os) throws IOException {
        ImageWriter writer = lookupImageWriterForFormat(imageFormat);

        try (ImageOutputStream ios = ImageIO.createImageOutputStream(os)) {
            writer.setOutput(ios);
            ImageWriteParam parameters = getImageWriteParameters(writer);

            writer.write(null, new IIOImage(image, null, null), parameters);
            ios.flush();
        } finally {
            writer.dispose();
        }
    }

    /**
     * Returns the image output parameters to control the output image quality, compression, etc. By default,
     * this uses the compression values set in this class. Override this method to get full control over the
     * ImageWriteParam used in image output.
     *
     * @param writer The ImageWriter we are going to use for image output.
     * @return ImageWriteParam configured for image output.
     */
    protected ImageWriteParam getImageWriteParameters(ImageWriter writer) {
        ImageWriteParam param = writer.getDefaultWriteParam();
        if (param.canWriteCompressed()) {
            if (writeCompressionMode != ImageWriteParam.MODE_COPY_FROM_METADATA) {
                param.setCompressionMode(writeCompressionMode);

                // see docs for IWP--only allowed to set type and quality if mode is EXPLICIT
                if (writeCompressionMode == ImageWriteParam.MODE_EXPLICIT) {
                    param.setCompressionType(writeCompressionType);
                    param.setCompressionQuality(writeCompressionQuality);
                }

            }
        }

        return param;
    }

    /**
     * Utility method to find an image writer.
     *
     * @param imageFormat String informal format name, "jpg"
     * @return ImageWriter corresponding to that format
     */
    private ImageWriter lookupImageWriterForFormat(String imageFormat) {
        Iterator<ImageWriter> iter = ImageIO.getImageWritersByFormatName(imageFormat);
        if (iter.hasNext()) {
            return iter.next();
        }
        throw new IllegalArgumentException("Image writer not found for format " + imageFormat);
    }
}
