/*
 * $Id: ListFields.java 3688 2009-02-10 22:27:37Z mstorer $
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

package com.lowagie.examples.forms;


import com.lowagie.text.pdf.PRAcroForm;
import com.lowagie.text.pdf.PdfArray;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfIndirectReference;
import com.lowagie.text.pdf.PdfLister;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfString;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Demonstrates the use of PageSize.
 *
 * @author blowagie
 */
public class ListFields {

    /**
     * Creates a PDF document with a certain pagesize
     *
     * @param args no arguments needed here
     */
    public static void main(String[] args) {

        System.out.println("Listfields");

        try {
            PrintStream stream = new PrintStream(new FileOutputStream("listfields.txt"));
            stream.println("ListFields output file");
            stream.println("==================================================");
            for (String arg : args) {
                stream.print("Filename: ");
                stream.println(arg);
                stream.println();
                PdfReader reader = new PdfReader(arg);
                PRAcroForm form = reader.getAcroForm();
                if (form == null) {
                    stream.println("This document has no fields.");
                    break;
                }
                PdfLister list = new PdfLister(stream);
                Map<Integer, PRAcroForm.FieldInformation> refToField = new HashMap<>();
                ArrayList fields = form.getFields();
                for (Object field1 : fields) {
                    PRAcroForm.FieldInformation field = (PRAcroForm.FieldInformation) field1;
                    refToField.put(field.getRef().getNumber(), field);
                }
                for (int page = 1; page <= reader.getNumberOfPages(); ++page) {
                    PdfDictionary dPage = reader.getPageN(page);
                    PdfArray annots = dPage.getAsArray(PdfName.ANNOTS);
                    if (annots == null) {
                        continue;
                    }
                    for (int annotIdx = 0; annotIdx < annots.size(); ++annotIdx) {
                        PdfIndirectReference ref = annots.getAsIndirectObject(annotIdx);
                        PdfDictionary annotDict = annots.getAsDict(annotIdx);
                        PdfName subType = annotDict.getAsName(PdfName.SUBTYPE);
                        if (subType == null || !subType.equals(PdfName.WIDGET)) {
                            continue;
                        }
                        PdfArray rect = annotDict.getAsArray(PdfName.RECT);
                        StringBuilder fName = new StringBuilder();
                        PRAcroForm.FieldInformation field = null;
                        while (annotDict != null) {
                            PdfString tName = annotDict.getAsString(PdfName.T);
                            if (tName != null) {
                                fName.insert(0, tName.toString() + ".");
                            }
                            if (ref != null) {
                                field = refToField.get(ref.getNumber());
                            }
                            ref = annotDict.getAsIndirectObject(PdfName.PARENT);
                            annotDict = annotDict.getAsDict(PdfName.PARENT);
                        }
                        if (fName.toString().endsWith(".")) {
                            fName = new StringBuilder(fName.substring(0, fName.length() - 1));
                        }
                        stream.println("page " + page + ", name - " + fName);
                        list.listAnyObject(rect);
                        if (field != null) {
                            stream.println("Merged attributes of " + field.getName());
                            list.listAnyObject(field.getInfo());
                            stream.println("Dictionary of " + field.getName());
                            list.listAnyObject(PdfReader.getPdfObject(field.getRef()));
                        }
                    }
                }
                stream.println("==================================================");
            }
            stream.flush();
            stream.close();
        } catch (IOException ioe) {
            System.err.println(ioe.getMessage());
        }
    }
}
