/*
 * $Id: ImageFilter.java 3271 2008-04-18 20:39:42Z xlv $
 * Copyright (c) 2005-2007 Bruno Lowagie
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

/*
 * This class was originally published under the MPL by Bruno Lowagie.
 * It was a part of iText, a Java-PDF library. You can now use it under
 * the MIT License; for backward compatibility you can also use it under
 * the MPL version 1.1: http://www.mozilla.org/MPL/
 * A copy of the MPL license is bundled with the source code FYI.
 */

package com.lowagie.toolbox.arguments.filters;

import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 * Filters images in a FileChooser.
 *
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class ImageFilter extends FileFilter {

    /**
     * Array with all kinds of image extensions.
     */
    public static final String[] IMAGES = new String[8];

    static {
        IMAGES[0] = ".jpg";
        IMAGES[1] = ".jpeg";
        IMAGES[2] = ".png";
        IMAGES[3] = ".gif";
        IMAGES[4] = ".bmp";
        IMAGES[5] = ".wmf";
        IMAGES[6] = ".tif";
        IMAGES[7] = ".tiff";
    }

    /**
     * array that enables you to filter on image types.
     */
    public boolean[] filter = new boolean[8];

    /**
     * Constructs an ImageFilter allowing all images.
     */
    public ImageFilter() {
        for (int i = 0; i < filter.length; i++) {
            filter[i] = true;
        }
    }

    /**
     * Constructs an ImageFilter allowing some images.
     *
     * @param jpeg indicates if jpegs are allowed
     * @param png  indicates if pngs are allowed
     * @param gif  indicates if gifs are allowed
     * @param bmp  indicates if bmps are allowed
     * @param wmf  indicates if wmfs are allowed
     * @param tiff indicates if tiffs are allowed
     */
    public ImageFilter(boolean jpeg, boolean png, boolean gif, boolean bmp, boolean wmf, boolean tiff) {
        if (jpeg) {
            filter[0] = true;
            filter[1] = true;
        }
        if (png) {
            filter[2] = true;
        }
        if (gif) {
            filter[3] = true;
        }
        if (bmp) {
            filter[4] = true;
        }
        if (wmf) {
            filter[5] = true;
        }
        if (tiff) {
            filter[6] = true;
            filter[7] = true;
        }
    }

    /**
     * @param f File
     * @return boolean
     * @see javax.swing.filechooser.FileFilter#accept(java.io.File)
     */
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        for (int i = 0; i < IMAGES.length; i++) {
            if (filter[i] && f.getName().toLowerCase().endsWith(IMAGES[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return String
     * @see javax.swing.filechooser.FileFilter#getDescription()
     */
    public String getDescription() {
        return "Image files";
    }
}
