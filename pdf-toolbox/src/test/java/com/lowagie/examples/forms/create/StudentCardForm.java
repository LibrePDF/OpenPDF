/*
 * $Id: StudentCardForm.java 3373 2008-05-12 16:21:24Z xlv $
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

package com.lowagie.examples.forms.create;


import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfFormField;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPCellEvent;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.TextField;
import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Generates a StudentCard as a form
 *
 * @author blowagie
 */
public class StudentCardForm implements PdfPCellEvent {

    /**
     * the writer with the acroform
     */
    private PdfFormField field;

    /**
     * Construct an implementation of PdfPCellEvent.
     *
     * @param field a form field
     */
    public StudentCardForm(PdfFormField field) {
        this.field = field;
    }

    /**
     * Generates a StudentCard as a form
     *
     * @param args no arguments needed here
     */
    public static void main(String[] args) {

        System.out.println("StudentCard as a form");

        // step 1: creation of a document-object
        Rectangle rect = new Rectangle(243, 153);
        rect.setBackgroundColor(new Color(0xFF, 0xFF, 0xCC));
        Document document = new Document(rect, 10, 10, 10, 10);

        try {

            // step 2:
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("studentcardform.pdf"));

            // step 3: we open the document
            document.open();

            // step 4:
            Font font = FontFactory.getFont(FontFactory.HELVETICA, 14, Font.BOLD, Color.BLUE);
            Paragraph p = new Paragraph("Ghent University", font);
            p.setAlignment(Element.ALIGN_CENTER);
            document.add(p);
            Font f = FontFactory.getFont(FontFactory.HELVETICA, 8);
            PdfPTable outertable = new PdfPTable(3);
            outertable.setTotalWidth(200);
            outertable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            float[] outer = {60, 25, 15};
            outertable.setWidths(outer);
            PdfPTable innertable = new PdfPTable(2);
            float[] inner = {35, 65};
            innertable.setWidths(inner);
            PdfPCell cell;
            TextField text;
            innertable.addCell(new Paragraph("name:", f));
            cell = new PdfPCell();
            text = new TextField(writer, new Rectangle(0, 0), "name");
            text.setOptions(TextField.MULTILINE);
            text.setFontSize(8);
            PdfFormField name = text.getTextField();
            cell.setCellEvent(new StudentCardForm(name));
            innertable.addCell(cell);
            innertable.addCell(new Paragraph("date of birth:", f));
            cell = new PdfPCell();
            text = new TextField(writer, new Rectangle(0, 0), "birthday");
            text.setOptions(TextField.MULTILINE);
            text.setFontSize(8);
            PdfFormField birthdate = text.getTextField();
            cell.setCellEvent(new StudentCardForm(birthdate));
            innertable.addCell(cell);
            innertable.addCell(new Paragraph("Study Program:", f));
            cell = new PdfPCell();
            text = new TextField(writer, new Rectangle(0, 0), "studyprogram");
            text.setOptions(TextField.MULTILINE);
            text.setFontSize(8);
            PdfFormField studyprogram = text.getTextField();
            studyprogram.setFieldName("studyprogram");
            cell.setCellEvent(new StudentCardForm(studyprogram));
            innertable.addCell(cell);
            innertable.addCell(new Paragraph("option:", f));
            cell = new PdfPCell();
            text = new TextField(writer, new Rectangle(0, 0), "option");
            text.setOptions(TextField.MULTILINE);
            text.setFontSize(8);
            PdfFormField option = text.getTextField();
            option.setFieldName("option");
            cell.setCellEvent(new StudentCardForm(option));
            innertable.addCell(cell);
            outertable.addCell(innertable);
            cell = new PdfPCell();
            cell.setBackgroundColor(new Color(0xFF, 0xDE, 0xAD));
            PdfFormField picture = PdfFormField.createPushButton(writer);
            picture.setFieldName("picture");
            cell.setCellEvent(new StudentCardForm(picture));
            outertable.addCell(cell);
            cell = new PdfPCell();
            cell.setBackgroundColor(Color.WHITE);
            PdfFormField barcode = PdfFormField.createPushButton(writer);
            barcode.setFieldName("barcode");
            cell.setCellEvent(new StudentCardForm(barcode));
            outertable.addCell(cell);
            outertable.writeSelectedRows(0, -1, 20, 100, writer.getDirectContent());
            writer.addAnnotation(name);
            writer.addAnnotation(birthdate);
            writer.addAnnotation(studyprogram);
            writer.addAnnotation(option);
            writer.addAnnotation(picture);
            writer.addAnnotation(barcode);
        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }

        // step 5: we close the document
        document.close();
    }

    /**
     * @see com.lowagie.text.pdf.PdfPCellEvent#cellLayout(com.lowagie.text.pdf.PdfPCell, com.lowagie.text.Rectangle,
     * com.lowagie.text.pdf.PdfContentByte[])
     */
    public void cellLayout(PdfPCell cell, Rectangle position,
            PdfContentByte[] canvases) {
        field.setWidget(position, null);
    }
}