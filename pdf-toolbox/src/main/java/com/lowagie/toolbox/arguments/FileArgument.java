/*
 * $Id: FileArgument.java 3271 2008-04-18 20:39:42Z xlv $
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

import com.lowagie.toolbox.AbstractTool;
import com.lowagie.toolbox.arguments.filters.DirFilter;
import com.lowagie.toolbox.swing.PdfInformationPanel;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

/**
 * FileArgument class if the argument is a java.io.File.
 *
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class FileArgument extends AbstractArgument {

    /**
     * a filter to put on the FileChooser.
     */
    protected FileFilter filter;
    /**
     * indicates if the argument has to point to a new or an existing file.
     */
    protected boolean newFile;
    /**
     * the label
     */
    PdfInformationPanel label = null;

    public FileArgument() {
        super();
    }

    /**
     * Constructs a FileArgument.
     *
     * @param tool        the tool that needs this argument
     * @param name        the name of the argument
     * @param description the description of the argument
     * @param newFile     makes the difference between an Open or Save dialog
     * @param filter      FileFilter
     */
    public FileArgument(AbstractTool tool, String name, String description,
            boolean newFile, FileFilter filter) {
        super(tool, name, description, null);
        this.newFile = newFile;
        this.filter = filter;
    }

    /**
     * Constructs a FileArgument.
     *
     * @param tool        the tool that needs this argument
     * @param name        the name of the argument
     * @param description the description of the argument
     * @param newFile     makes the difference between an Open or Save dialog
     */
    public FileArgument(AbstractTool tool, String name, String description,
            boolean newFile) {
        this(tool, name, description, newFile, null);
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
            return new File(value.toString());
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
            if (filter instanceof DirFilter) {
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            }
        }
        if (label != null) {
            fc.setAccessory(label);
            fc.addPropertyChangeListener(
                    JFileChooser.SELECTED_FILE_CHANGED_PROPERTY, label);
        }
        if (newFile) {
            fc.showSaveDialog(tool.getInternalFrame());
        } else {
            fc.showOpenDialog(tool.getInternalFrame());
        }
        try {
            setValue(fc.getSelectedFile());
        } catch (NullPointerException npe) {
        }
    }

    /**
     * @return Returns the filter.
     */
    public FileFilter getFilter() {
        return filter;
    }

    /**
     * @param filter The filter to set.
     */
    public void setFilter(FileFilter filter) {
        this.filter = filter;
    }

    /**
     * @return Returns the label.
     */
    public PdfInformationPanel getLabel() {
        return label;
    }

    /**
     * @param label The label to set.
     */
    public void setLabel(PdfInformationPanel label) {
        this.label = label;
    }

}
