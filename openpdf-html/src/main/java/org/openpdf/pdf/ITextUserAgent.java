/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Torbjoern Gannholm
 * Copyright (c) 2006 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.openpdf.pdf;

import org.openpdf.text.BadElementException;
import org.openpdf.text.Image;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfReader;
import org.jspecify.annotations.Nullable;
import org.openpdf.extend.FSImage;
import org.openpdf.resource.ImageResource;
import org.openpdf.swing.NaiveUserAgent;
import org.openpdf.util.Configuration;
import org.openpdf.util.ContentTypeDetectingInputStreamWrapper;
import org.openpdf.util.ImageUtil;
import org.openpdf.util.XRLog;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import static org.openpdf.util.IOUtil.readBytes;
import static org.openpdf.util.ImageUtil.isEmbeddedBase64Image;

public class ITextUserAgent extends NaiveUserAgent {
    private static final int IMAGE_CACHE_CAPACITY = 32;

    private final ITextOutputDevice _outputDevice;
    private final int dotsPerPixel;

    public ITextUserAgent(ITextOutputDevice outputDevice, int dotsPerPixel) {
        super(Configuration.valueAsInt("xr.image.cache-capacity", IMAGE_CACHE_CAPACITY));
        _outputDevice = outputDevice;
        this.dotsPerPixel = dotsPerPixel;
    }

    int getDotsPerPixel() {
        return dotsPerPixel;
    }

    @Override
    public ImageResource getImageResource(String uriStr) {
        String unresolvedUri = uriStr;
        if (!isEmbeddedBase64Image(uriStr)) {
            uriStr = resolveURI(uriStr);
        }
        ImageResource resource = _imageCache.get(unresolvedUri);

        if (resource == null) {
            resource = loadImageResource(uriStr);
            _imageCache.put(unresolvedUri, resource);
        }
        if (resource != null) {
            FSImage image = resource.getImage();
            if (image instanceof ITextFSImage) {
                image = (FSImage) ((ITextFSImage) resource.getImage()).clone();
            }
            return new ImageResource(resource.getImageUri(), image);
        } else {
            return new ImageResource(uriStr, null);
        }
    }

    @Nullable
    private ImageResource loadImageResource(String uriStr) {
        if (isEmbeddedBase64Image(uriStr)) {
            return loadEmbeddedBase64ImageResource(uriStr);
        }
        try (InputStream is = resolveAndOpenStream(uriStr)) {
            if (is != null) {
                try (ContentTypeDetectingInputStreamWrapper cis = new ContentTypeDetectingInputStreamWrapper(is)) {
                    if (cis.isPdf()) {
                        URI uri = new URI(uriStr);
                        PdfReader reader = _outputDevice.getReader(uri);
                        Rectangle rect = reader.getPageSizeWithRotation(1);
                        float initialWidth = rect.getWidth() * _outputDevice.getDotsPerPoint();
                        float initialHeight = rect.getHeight() * _outputDevice.getDotsPerPoint();
                        PDFAsImage image = new PDFAsImage(uri, initialWidth, initialHeight);
                        return new ImageResource(uriStr, image);
                    } else {
                        Image image = Image.getInstance(readBytes(cis));
                        scaleToOutputResolution(image);
                        return new ImageResource(uriStr, new ITextFSImage(image));
                    }
                }
            }
        } catch (BadElementException | IOException | URISyntaxException e) {
            XRLog.exception("Can't read image file; unexpected problem for URI '" + uriStr + "'", e);
        }
        return null;
    }

    private ImageResource loadEmbeddedBase64ImageResource(final String uri) {
        try {
            byte[] buffer = ImageUtil.getEmbeddedBase64Image(uri);
            Image image = Image.getInstance(buffer);
            scaleToOutputResolution(image);
            return new ImageResource(null, new ITextFSImage(image));
        } catch (BadElementException | IOException e) {
            XRLog.exception("Can't read XHTML embedded image.", e);
        }
        return new ImageResource(null, null);
    }

    private void scaleToOutputResolution(Image image) {
        float factor = dotsPerPixel;
        if (factor != 1.0f) {
            image.scaleAbsolute(image.getPlainWidth() * factor, image.getPlainHeight() * factor);
        }
    }
}
