/*
 * $Id: PdfInformationPanel.java 3372 2008-05-12 03:16:52Z xlv $
 * Copyright (c) 2005-2007 Carsten Hammer, Bruno Lowagie
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
 * This class was originally published under the MPL by Carsten Hammer.
 * It was a part of iText, a Java-PDF library. You can now use it under
 * the MIT License; for backward compatibility you can also use it under
 * the MPL version 1.1: http://www.mozilla.org/MPL/
 * A copy of the MPL license is bundled with the source code FYI.
 */
package com.lowagie.toolbox.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import com.lowagie.text.pdf.PdfDate;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.RandomAccessFileOrArray;

/**
 * Label for the FileChooser
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class PdfInformationPanel extends JPanel implements PropertyChangeListener {

	/** A serial version id */
	private static final long serialVersionUID = -4171577284617028707L;

	/** The file name of the PDF we're going to label. */
	String filename = "";

	/** the label containing the metadata */
	JLabel label = new JLabel();

	/** the scrollpane to scroll through the label */
	JScrollPane scrollpane = new JScrollPane();

	/** the panel to witch the scrollpane will be added. */
	JPanel panel = new JPanel();

	/** Construct the information label (actually it's a JPanel). */
	public PdfInformationPanel() {
		try {
			this.setLayout(new BorderLayout());
			label.setHorizontalAlignment(SwingConstants.CENTER);
			panel.setLayout(new BorderLayout());
			this.add(panel, BorderLayout.CENTER);
			scrollpane.setPreferredSize(new Dimension(200, 200));
			panel.add(scrollpane, BorderLayout.CENTER);
			scrollpane.setViewportView(label);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

    /**
     * Reads a PDF file for retrieving its metadata.
     *
     * @param file File
     */
    public void createTextFromPDF(File file) {
		if (file.exists()) {
			int page = 1;
			PdfReader reader = null;

			try {
				reader = new PdfReader(new RandomAccessFileOrArray(file.getAbsolutePath()), null);
				HashMap<String, String> pdfinfo = reader.getInfo();

				StringBuffer sb = new StringBuffer();
				sb.append("<html>=== Document Information ===<p>");
				sb.append(reader.getCropBox(page).getHeight() + "*"
						+ reader.getCropBox(page).getWidth() + "<p>");
				sb.append("PDF Version: " + reader.getPdfVersion() + "<p>");
				sb.append("Number of pages: " + reader.getNumberOfPages()
						+ "<p>");
				sb.append("Number of PDF objects: " + reader.getXrefSize()
						+ "<p>");
				sb.append("File length: " + reader.getFileLength() + "<p>");
				sb.append("Encrypted= " + reader.isEncrypted() + "<p>");
				if (pdfinfo.get("Title") != null) {
					sb.append("Title= " + pdfinfo.get("Title") + "<p>");
				}
				if (pdfinfo.get("Author") != null) {
					sb.append("Author= " + pdfinfo.get("Author") + "<p>");
				}
				if (pdfinfo.get("Subject") != null) {
					sb.append("Subject= " + pdfinfo.get("Subject") + "<p>");
				}
				if (pdfinfo.get("Producer") != null) {
					sb.append("Producer= " + pdfinfo.get("Producer") + "<p>");
				}
				if (pdfinfo.get("ModDate") != null) {
					sb.append("ModDate= "
							+ PdfDate.decode(pdfinfo.get("ModDate"))
									.getTime() + "<p>");
				}
				if (pdfinfo.get("CreationDate") != null) {
					sb.append("CreationDate= "
							+ PdfDate.decode(
									pdfinfo.get("CreationDate"))
									.getTime() + "<p>");
				}
				sb.append("</html>");
				label.setText(sb.toString());
			} catch (IOException ex) {
				label.setText("");
			}
		}
	}

    /**
     *
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     * @param evt PropertyChangeEvent
     */
    public void propertyChange(PropertyChangeEvent evt) {
		filename = evt.getPropertyName();
		if (filename.equals(JFileChooser.SELECTED_FILE_CHANGED_PROPERTY)) {
			File file = (File) evt.getNewValue();
			if (file != null) {
				this.createTextFromPDF(file);
				this.repaint();
			}
		}
	}
}
