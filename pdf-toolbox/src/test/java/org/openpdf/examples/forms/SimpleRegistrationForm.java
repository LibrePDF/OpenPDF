/*
 * $Id: SimpleRegistrationForm.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * This code is part of the 'OpenPDF Tutorial'.
 * You can find the complete tutorial at the following address:
 * https://github.com/LibrePDF/OpenPDF/wiki/Tutorial
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *
 */
package org.openpdf.examples.forms;

import java.io.FileOutputStream;
import java.io.IOException;
import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Rectangle;
import org.openpdf.text.pdf.PdfContentByte;
import org.openpdf.text.pdf.PdfFormField;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPCellEvent;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.openpdf.text.pdf.TextField;

/**
 * General example using TableEvents and CellEvents.
 */
public class SimpleRegistrationForm implements PdfPCellEvent {

    /**
     * the writer with the acroform
     */
    private PdfWriter writer;

    /**
     * the current fieldname
     */
    private String fieldname = "NoName";

    /**
     * Construct an implementation of PdfPCellEvent.
     *
     * @param writer the writer with the Acroform that will have to hold the fields.
     */
    public SimpleRegistrationForm(PdfWriter writer) {
        this.writer = writer;
    }

    /**
     * Construct an implementation of PdfPCellEvent.
     *
     * @param writer    the writer with the Acroform that will have to hold the fields.
     * @param fieldname the name of the TextField
     */
    public SimpleRegistrationForm(PdfWriter writer, String fieldname) {
        this.writer = writer;
        this.fieldname = fieldname;
    }

    /**
     * Example originally written by Wendy Smoak to generate a Table with 'floating boxes'. Adapted by Bruno Lowagie.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // step 1
        Document document = new Document();
        try {
            // step 2

            PdfWriter writer = PdfWriter.getInstance(document,
                    new FileOutputStream("SimpleRegistrationForm.pdf"));
            // step 3
            document.open();
            // step 4
            PdfPTable table = new PdfPTable(2);
            PdfPCell cell;
            table.getDefaultCell().setPadding(5f);

            table.addCell("Your name:");
            cell = new PdfPCell();
            cell.setCellEvent(new SimpleRegistrationForm(writer, "name"));
            table.addCell(cell);

            table.addCell("Your home address:");
            cell = new PdfPCell();
            cell.setCellEvent(new SimpleRegistrationForm(writer, "address"));
            table.addCell(cell);

            table.addCell("Postal code:");
            cell = new PdfPCell();
            cell
                    .setCellEvent(new SimpleRegistrationForm(writer,
                            "postal_code"));
            table.addCell(cell);

            table.addCell("Your email address:");
            cell = new PdfPCell();
            cell.setCellEvent(new SimpleRegistrationForm(writer, "email"));
            table.addCell(cell);

            document.add(table);

        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }
        // step 5
        document.close();
    }

    /**
     * @see org.openpdf.text.pdf.PdfPCellEvent#cellLayout(org.openpdf.text.pdf.PdfPCell, org.openpdf.text.Rectangle,
     * org.openpdf.text.pdf.PdfContentByte[])
     */
    public void cellLayout(PdfPCell cell, Rectangle position,
            PdfContentByte[] canvases) {
        TextField tf = new TextField(writer, position, fieldname);
        tf.setFontSize(12);
        try {
            PdfFormField field = tf.getTextField();
            writer.addAnnotation(field);
        } catch (IOException | DocumentException e) {
            e.printStackTrace();
        }
    }
}
