/*
 * $Id: CustomDialog.java 3271 2008-04-18 20:39:42Z xlv $
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
package com.lowagie.toolbox.swing;

import java.awt.Toolkit;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

/**
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class CustomDialog {

    JDialog dialog = null;
    JPanel jPanel1 = new JPanel();
    PlainDocument plainDocument;
    String msgString1;
    Object[] array;
    private JTextField textField = new JTextField(10);
    private JOptionPane optionPane;

    public CustomDialog(String msgstring, PlainDocument plainDocument) {
        super();
        this.setMsgString1(msgstring);
        this.plainDocument = plainDocument;
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public CustomDialog() {
        this("Enter a value:", new PlainDocument());
    }

    public static PlainDocument instantiateFloatDocument() {
        PlainDocument floatDocument = new PlainDocument() {
            private static final long serialVersionUID = 1874451914306029381L;

            public void insertString(int offset, String str, AttributeSet a) throws
                    BadLocationException {
                super.insertString(offset, str, a);
                try {
                    Float.parseFloat(super.getText(0, this.getLength()));
                } catch (Exception ex) {
                    super.remove(offset, 1);
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
            }
        };
        return floatDocument;
    }

    public static PlainDocument instantiateIntegerDocument() {
        PlainDocument intDocument = new PlainDocument() {
            private static final long serialVersionUID = -8735280090112457273L;

            public void insertString(int offset, String str, AttributeSet a) throws
                    BadLocationException {
                super.insertString(offset, str, a);
                try {
                    Integer.parseInt(super.getText(0, this.getLength()));
                } catch (Exception ex) {
                    super.remove(offset, 1);
                    Toolkit.getDefaultToolkit().beep();
                    return;
                }
            }
        };
        return intDocument;
    }

    public static PlainDocument instantiateStringDocument() {
        PlainDocument stringDocument = new PlainDocument() {
            private static final long serialVersionUID = -1244429733606195330L;

            public void insertString(int offset, String str, AttributeSet a) throws
                    BadLocationException {
                super.insertString(offset, str, a);
            }
        };
        return stringDocument;
    }

    private void jbInit() throws Exception {
        textField.setDocument(plainDocument);
    }

    public void setMsgString1(String msgString1) {
        this.msgString1 = msgString1;
        array = new Object[]{msgString1, textField};
        optionPane = new JOptionPane(array, JOptionPane.QUESTION_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION);
        dialog = optionPane.createDialog(UIManager.getString(
                "OptionPane.inputDialogTitle", null));
    }

    public String showInputDialog(String startvalue) {
        textField.setText(startvalue);
        dialog.setVisible(true);
        dialog.dispose();
        return textField.getText();
    }
}
