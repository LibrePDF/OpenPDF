/*
 * $Id: Barcodes.java 3635 2008-12-23 19:52:34Z xlv $
 *
 * This code is part of the 'iText Tutorial'.
 * You can find the complete tutorial at the following address:
 * http://itextdocs.lowagie.com/tutorial/
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * itext-questions@lists.sourceforge.net
 */
package com.lowagie.examples.objects.images.tiff;

import java.awt.Color;
import java.io.FileOutputStream;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.Barcode;
import com.lowagie.text.pdf.Barcode128;
import com.lowagie.text.pdf.Barcode39;
import com.lowagie.text.pdf.BarcodeEAN;
import com.lowagie.text.pdf.BarcodeEANSUPP;
import com.lowagie.text.pdf.BarcodeInter25;
import com.lowagie.text.pdf.BarcodePostnet;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
/**
 * List with different Barcode types.
 */
public class Barcodes {
	/**
	 * List with different Barcode types.
	 * @param args no arguments needed
	 */
	public static void main(String[] args) {
        System.out.println("Barcodes");
        
        // step 1: creation of a document-object
        Document document = new Document(PageSize.A4, 50, 50, 50, 50);
        
        try {
            
            // step 2: creation of the writer
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("barcodes.pdf"));
            
            // step 3: we open the document
            document.open();
            
            // step 4: we add content to the document
            PdfContentByte cb = writer.getDirectContent();
            Barcode39 code39 = new Barcode39();
            code39.setCode("CODE39-1234567890");
            code39.setStartStopText(false);
            Image image39 = code39.createImageWithBarcode(cb, null, null);
            Barcode39 code39ext = new Barcode39();
            code39ext.setCode("The willows.");
            code39ext.setStartStopText(false);
            code39ext.setExtended(true);
            Image image39ext = code39ext.createImageWithBarcode(cb, null, null);
            Barcode128 code128 = new Barcode128();
            code128.setCode("1Z234786 hello");
            Image image128 = code128.createImageWithBarcode(cb, null, null);
            BarcodeEAN codeEAN = new BarcodeEAN();
            codeEAN.setCodeType(Barcode.EAN13);
            codeEAN.setCode("9780201615883");
            Image imageEAN = codeEAN.createImageWithBarcode(cb, null, null);
            BarcodeInter25 code25 = new BarcodeInter25();
            code25.setGenerateChecksum(true);
            code25.setCode("41-1200076041-001");
            Image image25 = code25.createImageWithBarcode(cb, null, null);
            BarcodePostnet codePost = new BarcodePostnet();
            codePost.setCode("12345");
            Image imagePost = codePost.createImageWithBarcode(cb, null, null);
            BarcodePostnet codePlanet = new BarcodePostnet();
            codePlanet.setCode("50201402356");
            codePlanet.setCodeType(Barcode.PLANET);
            Image imagePlanet = codePlanet.createImageWithBarcode(cb, null, null);
            BarcodeEAN codeSUPP = new BarcodeEAN();
            codeSUPP.setCodeType(Barcode.SUPP5);
            codeSUPP.setCode("54995");
            codeSUPP.setBaseline(-2);
            BarcodeEANSUPP eanSupp = new BarcodeEANSUPP(codeEAN, codeSUPP);
            Image imageEANSUPP = eanSupp.createImageWithBarcode(cb, null, Color.blue);
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            table.getDefaultCell().setHorizontalAlignment(Element.ALIGN_CENTER);
            table.getDefaultCell().setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.getDefaultCell().setFixedHeight(70);
            table.addCell("CODE 39");
            table.addCell(new Phrase(new Chunk(image39, 0, 0)));
            table.addCell("CODE 39 EXTENDED");
            table.addCell(new Phrase(new Chunk(image39ext, 0, 0)));
            table.addCell("CODE 128");
            table.addCell(new Phrase(new Chunk(image128, 0, 0)));
            table.addCell("CODE EAN");
            table.addCell(new Phrase(new Chunk(imageEAN, 0, 0)));
            table.addCell("CODE EAN\nWITH\nSUPPLEMENTAL 5");
            table.addCell(new Phrase(new Chunk(imageEANSUPP, 0, 0)));
            table.addCell("CODE INTERLEAVED");
            table.addCell(new Phrase(new Chunk(image25, 0, 0)));
            table.addCell("CODE POSTNET");
            table.addCell(new Phrase(new Chunk(imagePost, 0, 0)));
            table.addCell("CODE PLANET");
            table.addCell(new Phrase(new Chunk(imagePlanet, 0, 0)));
            document.add(table);
        }
        catch (Exception de) {
            de.printStackTrace();
        }
        
        // step 5: we close the document
        document.close();
	}
}
