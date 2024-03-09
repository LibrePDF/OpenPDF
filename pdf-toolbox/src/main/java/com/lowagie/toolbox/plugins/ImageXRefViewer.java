/*
 * $Id: ImageXRefViewer.java 3271 2008-04-18 20:39:42Z xlv $
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

package com.lowagie.toolbox.plugins;

import com.lowagie.text.pdf.PRStream;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStream;
import com.lowagie.toolbox.AbstractTool;
import com.lowagie.toolbox.arguments.AbstractArgument;
import com.lowagie.toolbox.arguments.FileArgument;
import com.lowagie.toolbox.arguments.filters.PdfFilter;
import com.lowagie.toolbox.swing.EventDispatchingThread;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Cursor;
import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Allows you to inspect the Image XObjects inside a PDF file.
 *
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class ImageXRefViewer extends AbstractTool {

    static {
        addVersion("$Id: ImageXRefViewer.java 3271 2008-04-18 20:39:42Z xlv $");
    }

    /**
     * The total number of pictures inside the PDF.
     */
    int total_number_of_pictures = 0;
    /**
     * The spinner that will allow you to select an image.
     */
    JSpinner jSpinner = new JSpinner();
    /**
     * The panel that will show the images.
     */
    JPanel image_panel = new JPanel();
    /**
     * The layout with the images.
     */
    CardLayout layout = new CardLayout();

    /**
     * Creates a ViewImageXObjects object.
     */
    public ImageXRefViewer() {
        arguments.add(new FileArgument(this, "srcfile",
                "The file you want to inspect", false, new PdfFilter()));
    }

    /**
     * Shows the images that are in the PDF as Image XObjects.
     *
     * @param args String[]
     */
    public static void main(String[] args) {
        ImageXRefViewer tool = new ImageXRefViewer();
        if (args.length < 1) {
            System.err.println(tool.getUsage());
        }
        tool.setMainArguments(args);
        tool.execute();
    }

    /**
     * @return File
     * @throws InstantiationException on error
     * @see com.lowagie.toolbox.AbstractTool#getDestPathPDF()
     */
    protected File getDestPathPDF() throws InstantiationException {
        throw new InstantiationException("There is no file to show.");
    }

    /**
     * @see com.lowagie.toolbox.AbstractTool#createFrame()
     */
    protected void createFrame() {
        internalFrame = new JInternalFrame("View Image XObjects", true, false,
                true);
        internalFrame.setSize(500, 300);
        internalFrame.setJMenuBar(getMenubar());
        internalFrame.getContentPane().setLayout(new BorderLayout());

        JPanel master_panel = new JPanel();
        master_panel.setLayout(new BorderLayout());
        internalFrame.getContentPane().add(master_panel,
                java.awt.BorderLayout.CENTER);

        // images
        image_panel.setLayout(layout);
        jSpinner.addChangeListener(new SpinnerListener(this));
        image_panel.setBorder(BorderFactory.createEtchedBorder());

        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(image_panel);
        master_panel.add(scrollPane, java.awt.BorderLayout.CENTER);

        // spinner

        JPanel spinner_panel = new JPanel();
        spinner_panel.setLayout(new BorderLayout());
        spinner_panel.add(jSpinner, java.awt.BorderLayout.CENTER);

        JLabel image_label = new JLabel();
        image_label.setHorizontalAlignment(SwingConstants.CENTER);
        image_label.setText("images");
        spinner_panel.add(image_label, java.awt.BorderLayout.NORTH);

        master_panel.add(spinner_panel, java.awt.BorderLayout.NORTH);

        System.out.println("=== Image XObject Viewer OPENED ===");
    }

    /**
     * @param arg StringArgument
     * @see com.lowagie.toolbox.AbstractTool#valueHasChanged(com.lowagie.toolbox.arguments.AbstractArgument)
     */
    public void valueHasChanged(AbstractArgument arg) {
        // do nothing
    }

    /**
     * Reflects the change event in the JSpinner object.
     *
     * @param evt ChangeEvent
     */
    public void propertyChange(ChangeEvent evt) {
        int picture = Integer.parseInt(jSpinner.getValue().toString());
        if (picture < 0) {
            picture = 0;
            jSpinner.setValue("0");
        }
        if (picture >= total_number_of_pictures) {
            picture = total_number_of_pictures - 1;
            jSpinner.setValue(String.valueOf(picture));
        }
        layout.show(image_panel, String.valueOf(picture));
        image_panel.repaint();
    }

    /**
     * @see com.lowagie.toolbox.AbstractTool#execute()
     */
    public void execute() {
        total_number_of_pictures = 0;
        try {
            if (getValue("srcfile") == null) {
                throw new InstantiationException(
                        "You need to choose a sourcefile");
            }
            EventDispatchingThread task = new EventDispatchingThread() {
                public Object construct() {
                    try {
                        PdfReader reader = new PdfReader(
                                ((File) getValue("srcfile")).getAbsolutePath());
                        for (int i = 0; i < reader.getXrefSize(); i++) {
                            PdfObject pdfobj = reader.getPdfObject(i);
                            if (pdfobj != null) {
                                if (pdfobj.isStream()) {
                                    PdfStream pdfdict = (PdfStream) pdfobj;
                                    PdfObject pdfsubtype = pdfdict
                                            .get(PdfName.SUBTYPE);
                                    if (pdfsubtype == null) {
                                        continue;
                                    }
                                    if (!pdfsubtype.toString().equals(
                                            PdfName.IMAGE.toString())) {
                                        continue;
                                    }
                                    System.out.println("total_number_of_pictures: "
                                            + total_number_of_pictures);
                                    System.out.println("height:"
                                            + pdfdict.get(PdfName.HEIGHT));
                                    System.out.println("width:"
                                            + pdfdict.get(PdfName.WIDTH));
                                    System.out.println("bitspercomponent:"
                                            + pdfdict.get(PdfName.BITSPERCOMPONENT));
                                    byte[] barr = PdfReader
                                            .getStreamBytesRaw((PRStream) pdfdict);
                                    java.awt.Image im = Toolkit
                                            .getDefaultToolkit().createImage(barr);
                                    javax.swing.ImageIcon ii = new javax.swing.ImageIcon(im);

                                    JLabel label = new JLabel();
                                    label.setIcon(ii);
                                    image_panel.add(label, String.valueOf(total_number_of_pictures++));
                                }
                            }
                        }
                    } catch (InstantiationException | IOException ex) {
                    }
                    internalFrame.setCursor(Cursor.getDefaultCursor());
                    return null;
                }
            };
            internalFrame.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            task.start();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(internalFrame, e.getMessage(), e
                    .getClass().getName(), JOptionPane.ERROR_MESSAGE);
            System.err.println(e.getMessage());
        }
    }

    class SpinnerListener implements ChangeListener {

        private ImageXRefViewer adaptee;

        SpinnerListener(ImageXRefViewer adaptee) {
            this.adaptee = adaptee;
        }

        /**
         * @param e ChangeEvent
         * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
         */
        public void stateChanged(ChangeEvent e) {
            adaptee.propertyChange(e);
        }
    }
}
