/*
 * Copyright 2008 by Kevin Day.
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the License.
 *
 * The Original Code is 'iText, a free JAVA-PDF library'.
 *
 * The Initial Developer of the Original Code is Bruno Lowagie. Portions created by
 * the Initial Developer are Copyright (C) 1999-2008 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000-2008 by Paulo Soares. All Rights Reserved.
 *
 * Contributor(s): all the names of the contributors are added in the source code
 * where applicable.
 *
 * Alternatively, the contents of this file may be used under the terms of the
 * LGPL license (the "GNU LIBRARY GENERAL PUBLIC LICENSE"), in which case the
 * provisions of LGPL are applicable instead of those above.  If you wish to
 * allow use of your version of this file only under the terms of the LGPL
 * License and not to allow others to use your version of this file under
 * the MPL, indicate your decision by deleting the provisions above and
 * replace them with the notice and other provisions required by the LGPL.
 * If you do not delete the provisions above, a recipient may use your version
 * of this file under either the MPL or the GNU LIBRARY GENERAL PUBLIC LICENSE.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the MPL as stated above or under the terms of the GNU
 * Library General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library general Public License for more
 * details.
 *
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * http://www.lowagie.com/iText/
 */
package com.lowagie.text.pdf.parser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.RandomAccessFileOrArray;

/**
 * Tool that parses the content of a PDF document.
 * @since	2.1.4
 */
public class PdfContentReaderTool {

	/**
	 * Shows the detail of a dictionary.
	 * This is similar to the PdfLister functionality.
	 * @param dic	the dictionary of which you want the detail
	 * @return	a String representation of the dictionary
	 */
    static public String getDictionaryDetail(PdfDictionary dic){
        return getDictionaryDetail(dic, 0);
    }
    
    /**
     * Shows the detail of a dictionary.
     * @param dic	the dictionary of which you want the detail
     * @param depth	the depth of the current dictionary (for nested dictionaries)
     * @return	a String representation of the dictionary
     */
    static public String getDictionaryDetail(PdfDictionary dic, int depth){
        StringBuffer builder = new StringBuffer();
        builder.append('(');
        List subDictionaries = new ArrayList();
        for (Iterator i = dic.getKeys().iterator(); i.hasNext(); ) {
            PdfName key = (PdfName)i.next();
            PdfObject val = dic.getDirectObject(key);
            if (val.isDictionary())
                subDictionaries.add(key);
            builder.append(key);
            builder.append('=');
            builder.append(val);
            builder.append(", ");
        }
        builder.setLength(builder.length()-2);
        builder.append(')');
        PdfName pdfSubDictionaryName;
        for (Iterator it = subDictionaries.iterator(); it.hasNext(); ) {
        	pdfSubDictionaryName = (PdfName)it.next();
            builder.append('\n');
            for(int i = 0; i < depth+1; i++){
                builder.append('\t');
            }
            builder.append("Subdictionary ");
            builder.append(pdfSubDictionaryName);
            builder.append(" = ");
            builder.append(getDictionaryDetail(dic.getAsDict(pdfSubDictionaryName), depth+1));
        }
        return builder.toString();
    }

    /**
     * Writes information about a specific page from PdfReader to the specified output stream.
     * @since 2.1.5
     * @param reader    the PdfReader to read the page content from
     * @param pageNum   the page number to read
     * @param out       the output stream to send the content to
     * @throws IOException
     */
    static public void listContentStreamForPage(PdfReader reader, int pageNum, PrintWriter out) throws IOException {
        out.println("==============Page " + pageNum + "====================");
        out.println("- - - - - Dictionary - - - - - -");
        PdfDictionary pageDictionary = reader.getPageN(pageNum);
        out.println(getDictionaryDetail(pageDictionary));
        out.println("- - - - - Content Stream - - - - - -");
        RandomAccessFileOrArray f = reader.getSafeFile();
        
        byte[] contentBytes = reader.getPageContent(pageNum, f);
        f.close();

        
        InputStream is = new ByteArrayInputStream(contentBytes);
        int ch;
        while ((ch = is.read()) != -1){
            out.print((char)ch);
        }

        out.println("- - - - - Text Extraction - - - - - -");
        PdfTextExtractor extractor = new PdfTextExtractor(reader, new SimpleTextExtractingPdfContentRenderListener());
        String extractedText = extractor.getTextFromPage(pageNum);
        if (extractedText.length() != 0)
            out.println(extractedText);
        else
            out.println("No text found on page " + pageNum);
        
        out.println();
        
    }
    
    /**
     * Writes information about each page in a PDF file to the specified output stream.
     * @since 2.1.5
     * @param pdfFile	a File instance referring to a PDF file
     * @param out       the output stream to send the content to
     * @throws IOException
     */
    static public void listContentStream(File pdfFile, PrintWriter out) throws IOException {
        PdfReader reader = new PdfReader(pdfFile.getCanonicalPath());
            
        int maxPageNum = reader.getNumberOfPages();
        
        for (int pageNum = 1; pageNum <= maxPageNum; pageNum++){
            listContentStreamForPage(reader, pageNum, out);
        }       
            
    }

    /**
     * Writes information about the specified page in a PDF file to the specified output stream.
     * @since 2.1.5
     * @param pdfFile   a File instance referring to a PDF file
     * @param pageNum   the page number to read
     * @param out       the output stream to send the content to
     * @throws IOException
     */
    static public void listContentStream(File pdfFile, int pageNum, PrintWriter out) throws IOException {
        PdfReader reader = new PdfReader(pdfFile.getCanonicalPath());
        
        listContentStreamForPage(reader, pageNum, out);
    }
    
    /**
     * Writes information about each page in a PDF file to the specified file, or System.out.
     * @param args
     */
    public static void main(String[] args) {
        try{
            if (args.length < 1 || args.length > 3){
                System.out.println("Usage:  PdfContentReaderTool <pdf file> [<output file>|stdout] [<page num>]");
                return;
            }
            
            PrintWriter writer = new PrintWriter(System.out);
            if (args.length >= 2){
                if (args[1].compareToIgnoreCase("stdout") != 0){
                    System.out.println("Writing PDF content to " + args[1]);
                    writer = new PrintWriter(new FileOutputStream(new File(args[1])));
                }
            }

            int pageNum = -1;
            if (args.length >= 3){
                pageNum = Integer.parseInt(args[2]);
            }
            
            if (pageNum == -1){
                listContentStream(new File(args[0]), writer);
            } else {
                listContentStream(new File(args[0]), pageNum, writer);
            }
            writer.flush();
            
            if (args.length >= 2){
                writer.close();
                System.out.println("Finished writing content to " + args[1]);
            }
        } catch (Exception e){
            e.printStackTrace(System.err);
        }
    }

}
