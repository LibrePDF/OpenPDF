/*
 * $Id: FileArrayArgument.java 3297 2008-05-01 12:19:24Z blowagie $
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
import com.lowagie.toolbox.swing.FileList;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyVetoException;
import java.io.File;


/**
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class FileArrayArgument extends AbstractArgument {

    FileList fileList1 = new FileList();

    public FileArrayArgument() {
        super();
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public FileArrayArgument(AbstractTool tool, String name, String description) {
        super(tool, name, description, null);
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        FileArrayArgument filearrayargument = new FileArrayArgument();
    }

    public void actionPerformed(ActionEvent e) {
        fileList1.setLocation(10, 10);
        fileList1.setVisible(true);
        this.getTool().getInternalFrame().getDesktopPane().add(fileList1);
        try {
            fileList1.setSelected(true);
        } catch (PropertyVetoException ex1) {
            System.out.println(ex1.getMessage());
        }

//        try {
//            setValue(fileList1.getFilevector().toArray());
//        } catch (NullPointerException npe) {
//        }
    }

    public Object getArgument() throws InstantiationException {
        if (value == null) {
            return null;
        }
        try {
            return value;
        } catch (Exception e) {
            throw new InstantiationException(e.getMessage());
        }
    }

    private void jbInit() throws Exception {
        fileList1.addPropertyChangeListener(this);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String propertyname = evt.getPropertyName();
        if (propertyname.equals("filevector")) {
            File[] filear = (File[]) evt.getNewValue();
            if (filear != null) {
                this.setValue(filear);
            }
        }
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    public String toString() {

        return fileList1.getStringreprasentation();
    }

}
