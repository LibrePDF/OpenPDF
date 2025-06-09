/*
 * {{{ header & license
 * Copyright (c) 2006 Wisconsin Court System
 * Copyright (c) 2009 Patrick Wright
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
package org.openpdf.swing;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.openpdf.extend.FSImage;
import org.openpdf.util.ImageUtil;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.openpdf.util.ImageUtil.convertToBufferedImage;

public abstract class AWTFSImage implements FSImage {
    private static final FSImage NULL_FS_IMAGE = new NullImage();

    @CheckReturnValue
    public static FSImage createImage(@Nullable Image img) {
        if (img == null) {
            return NULL_FS_IMAGE;
        }
        BufferedImage bufferedImage = convertToBufferedImage(img, BufferedImage.TYPE_INT_ARGB);
        return new NewAWTFSImage(bufferedImage);
    }

    protected AWTFSImage() {
    }

    public abstract BufferedImage getImage();


    static class NewAWTFSImage extends AWTFSImage {
        private final BufferedImage img;

        public NewAWTFSImage(BufferedImage img) {
            this.img = img;
        }

        @Override
        public int getWidth() {
            return img.getWidth(null);
        }

        @Override
        public int getHeight() {
            return img.getHeight(null);
        }

        @Override
        public BufferedImage getImage() {
            return img;
        }

        @NonNull
        @CheckReturnValue
        @Override
        public FSImage scale(int width, int height) {
            if (width > 0 || height > 0) {
                int currentWith = getWidth();
                int currentHeight = getHeight();
                int targetWidth = width;
                int targetHeight = height;

                if (targetWidth == -1) {
                    targetWidth = (int)(currentWith * ((double)targetHeight / currentHeight));
                }

                if (targetHeight == -1) {
                    targetHeight = (int)(currentHeight * ((double)targetWidth / currentWith));
                }

                if (currentWith != targetWidth || currentHeight != targetHeight) {
                    return new NewAWTFSImage(ImageUtil.getScaledInstance(img, targetWidth, targetHeight));
                }
            }
            return this;
        }
    }

    private static final class NullImage extends AWTFSImage {
        private static final BufferedImage EMPTY_IMAGE = ImageUtil.createTransparentImage(1, 1);

        @Override
        public int getWidth() {
            return 0;
        }

        @Override
        public int getHeight() {
            return 0;
        }

        @NonNull
        @CheckReturnValue
        @Override
        public FSImage scale(int width, int height) {
            return this;
        }

        @Override
        public BufferedImage getImage() {
            return EMPTY_IMAGE;
        }
    }
}
