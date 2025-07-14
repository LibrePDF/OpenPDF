/*
 * $Id: ImageArgument.java 3271 2008-04-18 20:39:42Z xlv $
 * Copyright (c) 2005-2007 Bruno Lowagie, Carsten Hammer
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
 * This class was originally published under the MPL by Bruno Lowagie
 * and Carsten Hammer.
 * It was a part of iText, a Java-PDF library. You can now use it under
 * the MIT License; for backward compatibility you can also use it under
 * the MPL version 1.1: http://www.mozilla.org/MPL/
 * A copy of the MPL license is bundled with the source code FYI.
 */

package com.lowagie.toolbox.arguments;

import com.lowagie.text.Image;
import com.lowagie.toolbox.AbstractTool;
import com.lowagie.toolbox.arguments.filters.ImageFilter;
import java.awt.event.ActionEvent;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * StringArgument class if the argument is a com.lowagie.text.Image.
 *
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class ImageArgument extends AbstractArgument {

    /**
     * a filter to put on the FileChooser.
     */
    private FileFilter filter;

    /**
     * Constructs a FileArgument.
     *
     * @param tool        the tool that needs this argument
     * @param name        the name of the argument
     * @param description the description of the argument
     * @param filter      a custom filter
     */
    public ImageArgument(AbstractTool tool, String name, String description,
            FileFilter filter) {
        super(tool, name, description, null);
        this.filter = filter;
    }

    /**
     * Constructs a FileArgument.
     *
     * @param tool        the tool that needs this argument
     * @param name        the name of the argument
     * @param description the description of the argument
     */
    public ImageArgument(AbstractTool tool, String name, String description) {
        this(tool, name, description, new ImageFilter());
    }

    /**
     * Gets the argument as an object.
     *
     * @return an object
     * @throws InstantiationException if the specified key cannot be compared with the keys currently in the map
     */
    public Object getArgument() throws InstantiationException {
        if (value == null) {
            return null;
        }
        try {
            return Image.getInstance(value.toString());
        } catch (Exception e) {
            throw new InstantiationException(e.getMessage());
        }
    }

    /**
     * @param e ActionEvent
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent e) {
        JFileChooser fc = new JFileChooser();
        if (filter != null) {
            fc.setFileFilter(filter);
        }
        fc.showOpenDialog(tool.getInternalFrame());
        try {
            setValue(fc.getSelectedFile().getAbsolutePath());
        } catch (NullPointerException npe) {
        }
    }

}
