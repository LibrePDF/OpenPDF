/*
 * Copyright 2024 Andreas Røsdal.
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the License.
 *
 * The Original Code is 'OpenPDF'.
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
 * https://github.com/LibrePDF/OpenPDF
 */

package com.lowagie.text.pdf.parser;
import com.lowagie.text.pdf.PdfReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

public class RegionPdfTextExtractorTest {

    private PdfReader pdfReader;

    @BeforeEach
    public void setUp() throws Exception {
        // Load the PDF file from the resources directory
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("invoice-1.pdf");
        assertNotNull(inputStream, "PDF file not found in resources");
        pdfReader = new PdfReader(inputStream);
    }

    @Test
    public void testExtractInvoiceFields() throws Exception {
        RegionPdfTextExtractor extractor = new RegionPdfTextExtractor(pdfReader);
        float pageHeight = extractor.getPageHeight(1);

        // Extract the company name
        float xCompanyName = 10.0f;
        float yCompanyName = pageHeight - 70;
        float widthCompanyName = 500.0f;
        float heightCompanyName = 50.0f;
        String companyName = extractor.getTextFromPageArea(1, xCompanyName, yCompanyName, widthCompanyName, heightCompanyName);
        assertNotNull(companyName, "Company name should not be null");
        System.out.println("Company Name: " + companyName);
        assertTrue(companyName.contains("Rør og Rygg Entreprenør AS"), "Extracted text should contain the company name");

        // Adjusted extraction coordinates for the invoice number
        float xInvoiceNumber = 10.0f;
        float yInvoiceNumber = pageHeight - 250;
        float widthInvoiceNumber = 500.0f;
        float heightInvoiceNumber = 150.0f;
        String invoiceNumberRegion = extractor.getTextFromPageArea(1, xInvoiceNumber, yInvoiceNumber, widthInvoiceNumber, heightInvoiceNumber);
        assertNotNull(invoiceNumberRegion, "Invoice number region should not be null");
        System.out.println("Extracted Invoice Number Region Text: " + invoiceNumberRegion);

        // Check for invoice number
        assertTrue(invoiceNumberRegion.contains("Fakturanr."), "Extracted text should contain the invoice number");

        // Extract the invoice date
        String invoiceDate = RegionPdfTextExtractor.extractDateFromText(invoiceNumberRegion, "Fakturadato");
        assertNotNull(invoiceDate, "Invoice date should not be null");
        System.out.println("Invoice Date: " + invoiceDate);
        assertTrue(invoiceDate.contains("2024-04-04"), "Extracted text should contain the invoice date");

        // Expand the search area for the due date
        float xDueDate = 10.0f;
        float yDueDate = pageHeight - 350;  // Move down the Y-coordinate to cover more area
        float widthDueDate = 500.0f;
        float heightDueDate = 150.0f;  // Increase the height to capture a larger area
        String dueDateRegion = extractor.getTextFromPageArea(1, xDueDate, yDueDate, widthDueDate, heightDueDate);
        assertNotNull(dueDateRegion, "Due date region should not be null");
        System.out.println("Extracted Due Date Region Text: " + dueDateRegion);

        // Extract the due date (enhanced)
        String dueDate = RegionPdfTextExtractor.extractDateFromText(dueDateRegion, "Forfallsdato");
        assertNotNull(dueDate, "Due date should not be null");
        System.out.println("Due Date: " + dueDate);
        assertTrue(dueDate.contains("2024-04-07"), "Extracted text should contain the due date");

        // Extract the payable amount with a wider region and clean-up
        float xAmount = 10.0f;
        float yAmount = pageHeight - 500;
        float widthAmount = 600.0f;
        float heightAmount = 100.0f;
        String amountRegion = extractor.getTextFromPageArea(1, xAmount, yAmount, widthAmount, heightAmount);
        assertNotNull(amountRegion, "Amount region should not be null");
        System.out.println("Extracted Payable Amount Region Text: " + amountRegion);

        // Clean the extracted text
        String cleanedAmountRegion = RegionPdfTextExtractor.cleanExtractedText(amountRegion);

        // Use regex to find all payable amounts
        Pattern pattern = Pattern.compile("\\d{1,3}(?: \\d{3})*,\\d{2}");
        Matcher matcher = pattern.matcher(cleanedAmountRegion);

        List<String> amounts = new ArrayList<>();
        while (matcher.find()) {
            amounts.add(matcher.group());
        }

        // Ensure we have at least one amount
        assertFalse(amounts.isEmpty(), "Extracted text should contain at least one payable amount");

        // Find the correct payable amount by context or by matching the expected value
        String expectedAmount = "44 139,26";
        String payableAmount = amounts.stream()
                .filter(amount -> amount.equals(expectedAmount))
                .findFirst()
                .orElse(null);

        assertNotNull(payableAmount, "Payable amount should not be null");
        System.out.println("Payable Amount: " + payableAmount);
        assertTrue(payableAmount.equals(expectedAmount), "Extracted text should match the expected payable amount");

        // Ensure the currency is present
        assertTrue(cleanedAmountRegion.contains("NOK"), "Extracted text should contain the currency NOK");
    }




}