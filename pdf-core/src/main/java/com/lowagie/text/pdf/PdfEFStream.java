/*
 * $Id: PdfEFStream.java 3735 2009-02-26 01:44:03Z xlv $
 *
 * Copyright (c) 2008 by Bruno Lowagie
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
 * the Initial Developer are Copyright (C) 1999, 2000, 2001, 2002 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000, 2001, 2002 by Paulo Soares. All Rights Reserved.
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

package com.lowagie.text.pdf;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * Extends PdfStream and should be used to create Streams for Embedded Files
 * (file attachments).
 * @since	2.1.3
 */

public class PdfEFStream extends PdfStream {

	/**
	 * Creates a Stream object using an InputStream and a PdfWriter object
	 * @param	in	the InputStream that will be read to get the Stream object
	 * @param	writer	the writer to which the stream will be added
	 */
	public PdfEFStream(InputStream in, PdfWriter writer) {
		super(in, writer);
	}

	/**
	 * Creates a Stream object using a byte array
	 * @param	fileStore	the bytes for the stream
	 */
	public PdfEFStream(byte[] fileStore) {
		super(fileStore);
	}

    /**
     * @see com.lowagie.text.pdf.PdfDictionary#toPdf(com.lowagie.text.pdf.PdfWriter, java.io.OutputStream)
     */
    public void toPdf(PdfWriter writer, OutputStream os) throws IOException {
        if (inputStream != null && compressed)
            put(PdfName.FILTER, PdfName.FLATEDECODE);
        PdfEncryption crypto = null;
        if (writer != null)
            crypto = writer.getEncryption();
        if (crypto != null) {
            PdfObject filter = get(PdfName.FILTER);
            if (filter != null) {
                if (PdfName.CRYPT.equals(filter))
                    crypto = null;
                else if (filter.isArray()) {
                    PdfArray a = (PdfArray)filter;
                    if (!a.isEmpty() && PdfName.CRYPT.equals(a.getPdfObject(0)))
                        crypto = null;
                }
            }
        }
    	if (crypto != null && crypto.isEmbeddedFilesOnly()) {
    		PdfArray filter = new PdfArray();
    		PdfArray decodeparms = new PdfArray();
    		PdfDictionary crypt = new PdfDictionary();
    		crypt.put(PdfName.NAME, PdfName.STDCF);
    		filter.add(PdfName.CRYPT);
    		decodeparms.add(crypt);
    		if (compressed) {
    			filter.add(PdfName.FLATEDECODE);
    			decodeparms.add(new PdfNull());
    		}
    		put(PdfName.FILTER, filter);
    		put(PdfName.DECODEPARMS, decodeparms);
    	}
        PdfObject nn = get(PdfName.LENGTH);
        if (crypto != null && nn != null && nn.isNumber()) {
            int sz = ((PdfNumber)nn).intValue();
            put(PdfName.LENGTH, new PdfNumber(crypto.calculateStreamSize(sz)));
            superToPdf(writer, os);
            put(PdfName.LENGTH, nn);
        }
        else
            superToPdf(writer, os);

        os.write(STARTSTREAM);
        if (inputStream != null) {
            rawLength = 0;
            DeflaterOutputStream def = null;
            OutputStreamCounter osc = new OutputStreamCounter(os);
            OutputStreamEncryption ose = null;
            OutputStream fout = osc;
            if (crypto != null)
                fout = ose = crypto.getEncryptionStream(fout);
            Deflater deflater = null;
            if (compressed) {
                deflater = new Deflater(compressionLevel);
                fout = def = new DeflaterOutputStream(fout, deflater, 0x8000);
            }
            
            byte buf[] = new byte[4192];
            while (true) {
                int n = inputStream.read(buf);
                if (n <= 0)
                    break;
                fout.write(buf, 0, n);
                rawLength += n;
            }
            if (def != null) {
                def.finish();
                deflater.end();
            }
            if (ose != null)
                ose.finish();
            inputStreamLength = osc.getCounter();
        }
        else {
            if (crypto == null) {
                if (streamBytes != null)
                    streamBytes.writeTo(os);
                else
                    os.write(bytes);
            }
            else {
                byte b[];
                if (streamBytes != null) {
                    b = crypto.encryptByteArray(streamBytes.toByteArray());
                }
                else {
                    b = crypto.encryptByteArray(bytes);
                }
                os.write(b);
            }
        }
        os.write(ENDSTREAM);
    }
}
