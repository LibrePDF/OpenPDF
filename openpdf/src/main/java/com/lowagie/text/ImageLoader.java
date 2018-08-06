/*
 *
 * Copyright 2018 Andreas Rosdal
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
 */

package com.lowagie.text;


import com.lowagie.text.exceptions.ExceptionUtil;
import org.apache.commons.io.IOUtils;
import org.apache.sanselan.common.byteSources.ByteSourceArray;
import org.apache.sanselan.formats.bmp.BmpImageParser;
import org.apache.sanselan.formats.gif.GifImageParser;
import org.apache.sanselan.formats.png.PngImageParser;
import org.apache.sanselan.formats.tiff.TiffImageParser;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

/**
 * Loads image files such as PNG, GIF, TIFF and BMP.
 *
 * @author Andreas Rosdal
 */
public class ImageLoader {

    public static Image getGifImage(URL url) {
        try {
            InputStream is = url.openStream();
            byte[] imageBytes = IOUtils.toByteArray(is);
            is.close();
            GifImageParser parser = new GifImageParser();
            BufferedImage bufferedImage = parser.getBufferedImage(new ByteSourceArray(imageBytes), new HashMap());
            return Image.getInstance(bufferedImage, null, false);

        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    public static Image getTiffImage(URL url) {
        try {
            InputStream is = url.openStream();
            byte[] imageBytes = IOUtils.toByteArray(is);
            is.close();
            TiffImageParser parser = new TiffImageParser();
            BufferedImage bufferedImage = parser.getBufferedImage(new ByteSourceArray(imageBytes), new HashMap());
            return Image.getInstance(bufferedImage, null, false);

        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    public static Image getPngImage(URL url) {
        try {
            InputStream is = url.openStream();
            byte[] imageBytes = IOUtils.toByteArray(is);
            is.close();
            PngImageParser parser = new PngImageParser();
            BufferedImage bufferedImage = parser.getBufferedImage(new ByteSourceArray(imageBytes), new HashMap());
            return Image.getInstance(bufferedImage, null, false);

        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }


    public static Image getBmpImage(URL url) {
        try {
            InputStream is = url.openStream();
            byte[] imageBytes = IOUtils.toByteArray(is);
            is.close();
            BmpImageParser parser = new BmpImageParser();
            BufferedImage bufferedImage = parser.getBufferedImage(new ByteSourceArray(imageBytes), new HashMap());
            return Image.getInstance(bufferedImage, null, false);

        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }


    public static Image getGifImage(byte imageData[]) {
        try {
            GifImageParser parser = new GifImageParser();
            BufferedImage bufferedImage = parser.getBufferedImage(new ByteSourceArray(imageData), new HashMap());
            return Image.getInstance(bufferedImage, null, false);

        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    public static Image getPngImage(byte imageData[]) {
        try {
            PngImageParser parser = new PngImageParser();
            BufferedImage bufferedImage = parser.getBufferedImage(new ByteSourceArray(imageData), new HashMap());
            return Image.getInstance(bufferedImage, null, false);

        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    public static Image getBmpImage(byte imageData[]) {
        try {
            BmpImageParser parser = new BmpImageParser();
            BufferedImage bufferedImage = parser.getBufferedImage(new ByteSourceArray(imageData), new HashMap());
            return Image.getInstance(bufferedImage, null, false);

        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }

    public static Image getTiffImage(byte imageData[]) {
        try {
            TiffImageParser parser = new TiffImageParser();
            BufferedImage bufferedImage = parser.getBufferedImage(new ByteSourceArray(imageData), new HashMap());
            return Image.getInstance(bufferedImage, null, false);

        } catch (Exception e) {
            throw ExceptionUtil.wrap(e);
        }
    }


}
