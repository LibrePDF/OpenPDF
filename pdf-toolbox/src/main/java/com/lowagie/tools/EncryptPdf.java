/*
 * $Id: EncryptPdf.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * Copyright 2002 by Paulo Soares
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
 * the Initial Developer are Copyright (C) 1999-2006 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000-2006 by Paulo Soares. All Rights Reserved.
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
package com.lowagie.tools;

import com.lowagie.text.pdf.PdfEncryptor;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Encrypts a PDF document. It needs iText (http://www.lowagie.com/iText).
 *
 * @author Paulo Soares (psoares@consiste.pt)
 * @since 2.1.1 (renamed to follow Java naming conventions)
 */
public class EncryptPdf {

    private final static int INPUT_FILE = 0;
    private final static int OUTPUT_FILE = 1;
    private final static int USER_PASSWORD = 2;
    private final static int OWNER_PASSWORD = 3;
    private final static int PERMISSIONS = 4;
    private final static int STRENGTH = 5;
    private final static int MOREINFO = 6;
    private final static int[] permit = {
            PdfWriter.ALLOW_PRINTING,
            PdfWriter.ALLOW_MODIFY_CONTENTS,
            PdfWriter.ALLOW_COPY,
            PdfWriter.ALLOW_MODIFY_ANNOTATIONS,
            PdfWriter.ALLOW_FILL_IN,
            PdfWriter.ALLOW_SCREENREADERS,
            PdfWriter.ALLOW_ASSEMBLY,
            PdfWriter.ALLOW_DEGRADED_PRINTING};

    private static void usage() {
        System.out.println(
                "usage: input_file output_file user_password owner_password permissions 128|40 [new info string pairs]");
        System.out.println("permissions is 8 digit long 0 or 1. Each digit has a particular security function:");
        System.out.println();
        System.out.println("AllowPrinting");
        System.out.println("AllowModifyContents");
        System.out.println("AllowCopy");
        System.out.println("AllowModifyAnnotations");
        System.out.println("AllowFillIn (128 bit only)");
        System.out.println("AllowScreenReaders (128 bit only)");
        System.out.println("AllowAssembly (128 bit only)");
        System.out.println("AllowDegradedPrinting (128 bit only)");
        System.out.println("Example permissions to copy and print would be: 10100000");
    }

    /**
     * Encrypts a PDF document.
     *
     * @param args input_file output_file user_password owner_password permissions 128|40 [new info string pairs]
     */
    public static void main(String[] args) {
        System.out.println("PDF document encryptor");
        if (args.length <= STRENGTH || args[PERMISSIONS].length() != 8) {
            usage();
            return;
        }
        try {
            int permissions = 0;
            String p = args[PERMISSIONS];
            for (int k = 0; k < p.length(); ++k) {
                permissions |= (p.charAt(k) == '0' ? 0 : permit[k]);
            }
            System.out.println("Reading " + args[INPUT_FILE]);
            PdfReader reader = new PdfReader(args[INPUT_FILE]);
            System.out.println("Writing " + args[OUTPUT_FILE]);
            Map<String, String> moreInfo = new HashMap<>();
            for (int k = MOREINFO; k < args.length - 1; k += 2) {
                moreInfo.put(args[k], args[k + 1]);
            }
            PdfEncryptor.encrypt(reader, new FileOutputStream(args[OUTPUT_FILE]),
                    args[USER_PASSWORD].getBytes(), args[OWNER_PASSWORD].getBytes(), permissions,
                    args[STRENGTH].equals("128"), moreInfo);
            System.out.println("Done.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}