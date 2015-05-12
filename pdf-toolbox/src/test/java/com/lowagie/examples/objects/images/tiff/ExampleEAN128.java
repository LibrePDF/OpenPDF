/*
 * $Id: ExampleEAN128.java 3373 2008-05-12 16:21:24Z xlv $
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
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.Barcode128;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
/**
 * Example Barcode EAN128.
 */
public class ExampleEAN128 {

	/**
	 * Example Barcode EAN128.
	 * @param args no arguments needed
	 */
    public static void main(String[] args) {
    	// step 1
    	Document document = new Document();
    	try {
    		// step 2
    		PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("ean128.pdf"));
    		// step 3
    		document.open();
    		// step 4
    		PdfContentByte cb = writer.getDirectContent();
            PdfPTable pageTot = new PdfPTable(1);
            pageTot.getDefaultCell().setPadding(0f);
            pageTot.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            pageTot.getDefaultCell().setHorizontalAlignment(Element.ALIGN_LEFT);
            pageTot.setWidthPercentage(100f);
            //Data for the barcode : it is composed of 3 blocks whith AI 402, 90 and 421
            // The blocks whith the type 402 and 90 are of variable size so you must put a FNC1
            // to delimitate the block
            String code402 = "24132399420058289"+Barcode128.FNC1;
            String code90 = "3700000050"+Barcode128.FNC1;
            String code421 = "422356";
            String data = code402 + code90 + code421;
            
            PdfPTable cell = new PdfPTable(1);
            cell.getDefaultCell().setBorder(Rectangle.NO_BORDER);
            cell.getDefaultCell().setPadding(0f);
            
            PdfPCell info = new PdfPCell(new Phrase("Barcode EAN 128"));
            info.setBorder(Rectangle.NO_BORDER);
            pageTot.addCell(info);
            
            Barcode128 shipBarCode = new Barcode128();
            shipBarCode.setX(0.75f);
            shipBarCode.setN(1.5f);
            shipBarCode.setChecksumText(true);
            shipBarCode.setGenerateChecksum(true);
            shipBarCode.setSize(10f);
            shipBarCode.setTextAlignment(Element.ALIGN_CENTER);
            shipBarCode.setBaseline(10f);
            shipBarCode.setCode(data);
            shipBarCode.setBarHeight(50f);
            
            Image imgShipBarCode = shipBarCode.createImageWithBarcode(cb, Color.black, Color.blue);
            PdfPCell shipment = new PdfPCell(new Phrase(
            new Chunk(imgShipBarCode, 0, 0)));
            shipment.setFixedHeight(shipBarCode.getBarcodeSize().getHeight() + 16f);
            shipment.setPaddingTop(5f);
            shipment.setPaddingBottom(10f);
            shipment.setBorder(Rectangle.BOX);
            shipment.setVerticalAlignment(Element.ALIGN_TOP);
            shipment.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.addCell(shipment);
            
            pageTot.addCell(cell);
            
            
            document.add(pageTot);
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    	}
    	// step 5
    	document.close();
    }

}
