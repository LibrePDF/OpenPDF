/*
 * Copyright 2003, 2004 by Paulo Soares.
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.SignatureException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.lowagie.text.error_messages.MessageLocalization;

import com.lowagie.text.DocWriter;
import com.lowagie.text.DocumentException;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.collection.PdfCollection;
import com.lowagie.text.pdf.interfaces.PdfEncryptionSettings;
import com.lowagie.text.pdf.interfaces.PdfViewerPreferences;
import java.security.cert.Certificate;

/** Applies extra content to the pages of a PDF document.
 * This extra content can be all the objects allowed in PdfContentByte
 * including pages from other Pdfs. The original PDF will keep
 * all the interactive elements including bookmarks, links and form fields.
 * <p>
 * It is also possible to change the field values and to
 * flatten them. New fields can be added but not flattened.
 * @author Paulo Soares (psoares@consiste.pt)
 */
public class PdfStamper
	implements PdfViewerPreferences, PdfEncryptionSettings {
    /**
     * The writer
     */    
    protected PdfStamperImp stamper;
    private HashMap moreInfo;
    private boolean hasSignature;
    private PdfSignatureAppearance sigApp;

    /** Starts the process of adding extra content to an existing PDF
     * document.
     * @param reader the original document. It cannot be reused
     * @param os the output stream
     * @throws DocumentException on error
     * @throws IOException on error
     */
    public PdfStamper(PdfReader reader, OutputStream os) throws DocumentException, IOException {
        stamper = new PdfStamperImp(reader, os, '\0', false);
    }

    /**
     * Starts the process of adding extra content to an existing PDF
     * document.
     * @param reader the original document. It cannot be reused
     * @param os the output stream
     * @param pdfVersion the new pdf version or '\0' to keep the same version as the original
     * document
     * @throws DocumentException on error
     * @throws IOException on error
     */
    public PdfStamper(PdfReader reader, OutputStream os, char pdfVersion) throws DocumentException, IOException {
        stamper = new PdfStamperImp(reader, os, pdfVersion, false);
    }

    /**
     * Starts the process of adding extra content to an existing PDF
     * document, possibly as a new revision.
     * @param reader the original document. It cannot be reused
     * @param os the output stream
     * @param pdfVersion the new pdf version or '\0' to keep the same version as the original
     * document
     * @param append if <CODE>true</CODE> appends the document changes as a new revision. This is
     * only useful for multiple signatures as nothing is gained in speed or memory
     * @throws DocumentException on error
     * @throws IOException on error
     */
    public PdfStamper(PdfReader reader, OutputStream os, char pdfVersion, boolean append) throws DocumentException, IOException {
        stamper = new PdfStamperImp(reader, os, pdfVersion, append);
    }

    /** Gets the optional <CODE>String</CODE> map to add or change values in
     * the info dictionary.
     * @return the map or <CODE>null</CODE>
     *
     */
    public HashMap getMoreInfo() {
        return this.moreInfo;
    }

    /** An optional <CODE>String</CODE> map to add or change values in
     * the info dictionary. Entries with <CODE>null</CODE>
     * values delete the key in the original info dictionary
     * @param moreInfo additional entries to the info dictionary
     *
     */
    public void setMoreInfo(HashMap moreInfo) {
        this.moreInfo = moreInfo;
    }

    /**
     * Replaces a page from this document with a page from other document. Only the content
     * is replaced not the fields and annotations. This method must be called before 
     * getOverContent() or getUndercontent() are called for the same page.
     * @param r the <CODE>PdfReader</CODE> from where the new page will be imported
     * @param pageImported the page number of the imported page
     * @param pageReplaced the page to replace in this document
     * @since iText 2.1.1
     */
    public void replacePage(PdfReader r, int pageImported, int pageReplaced) {
        stamper.replacePage(r, pageImported, pageReplaced);
    }
    
    /**
     * Inserts a blank page. All the pages above and including <CODE>pageNumber</CODE> will
     * be shifted up. If <CODE>pageNumber</CODE> is bigger than the total number of pages
     * the new page will be the last one.
     * @param pageNumber the page number position where the new page will be inserted
     * @param mediabox the size of the new page
     */    
    public void insertPage(int pageNumber, Rectangle mediabox) {
        stamper.insertPage(pageNumber, mediabox);
    }
    
    /**
     * Gets the signing instance. The appearances and other parameters can the be set.
     * @return the signing instance
     */    
    public PdfSignatureAppearance getSignatureAppearance() {
        return sigApp;
    }

    /**
     * Closes the document. No more content can be written after the
     * document is closed.
     * <p>
     * If closing a signed document with an external signature the closing must be done
     * in the <CODE>PdfSignatureAppearance</CODE> instance.
     * @throws DocumentException on error
     * @throws IOException on error
     */
    public void close() throws DocumentException, IOException {
        if (!hasSignature) {
            stamper.close(moreInfo);
            return;
        }
        sigApp.preClose();
        PdfSigGenericPKCS sig = sigApp.getSigStandard();
        PdfLiteral lit = (PdfLiteral)sig.get(PdfName.CONTENTS);
        int totalBuf = (lit.getPosLength() - 2) / 2;
        byte buf[] = new byte[8192];
        int n;
        InputStream inp = sigApp.getRangeStream();
        try {
            while ((n = inp.read(buf)) > 0) {
                sig.getSigner().update(buf, 0, n);
            }
        }
        catch (SignatureException se) {
            throw new ExceptionConverter(se);
        }
        buf = new byte[totalBuf];
        byte[] bsig = sig.getSignerContents();
        System.arraycopy(bsig, 0, buf, 0, bsig.length);
        PdfString str = new PdfString(buf);
        str.setHexWriting(true);
        PdfDictionary dic = new PdfDictionary();
        dic.put(PdfName.CONTENTS, str);
        sigApp.close(dic);
        stamper.reader.close();
    }

    /** Gets a <CODE>PdfContentByte</CODE> to write under the page of
     * the original document.
     * @param pageNum the page number where the extra content is written
     * @return a <CODE>PdfContentByte</CODE> to write under the page of
     * the original document
     */
    public PdfContentByte getUnderContent(int pageNum) {
        return stamper.getUnderContent(pageNum);
    }

    /** Gets a <CODE>PdfContentByte</CODE> to write over the page of
     * the original document.
     * @param pageNum the page number where the extra content is written
     * @return a <CODE>PdfContentByte</CODE> to write over the page of
     * the original document
     */
    public PdfContentByte getOverContent(int pageNum) {
        return stamper.getOverContent(pageNum);
    }

    /** Checks if the content is automatically adjusted to compensate
     * the original page rotation.
     * @return the auto-rotation status
     */
    public boolean isRotateContents() {
        return stamper.isRotateContents();
    }

    /** Flags the content to be automatically adjusted to compensate
     * the original page rotation. The default is <CODE>true</CODE>.
     * @param rotateContents <CODE>true</CODE> to set auto-rotation, <CODE>false</CODE>
     * otherwise
     */
    public void setRotateContents(boolean rotateContents) {
        stamper.setRotateContents(rotateContents);
    }

    /** Sets the encryption options for this document. The userPassword and the
     *  ownerPassword can be null or have zero length. In this case the ownerPassword
     *  is replaced by a random string. The open permissions for the document can be
     *  AllowPrinting, AllowModifyContents, AllowCopy, AllowModifyAnnotations,
     *  AllowFillIn, AllowScreenReaders, AllowAssembly and AllowDegradedPrinting.
     *  The permissions can be combined by ORing them.
     * @param userPassword the user password. Can be null or empty
     * @param ownerPassword the owner password. Can be null or empty
     * @param permissions the user permissions
     * @param strength128Bits <code>true</code> for 128 bit key length, <code>false</code> for 40 bit key length
     * @throws DocumentException if anything was already written to the output
     */
    public void setEncryption(byte userPassword[], byte ownerPassword[], int permissions, boolean strength128Bits) throws DocumentException {
        if (stamper.isAppend())
            throw new DocumentException(MessageLocalization.getComposedMessage("append.mode.does.not.support.changing.the.encryption.status"));
        if (stamper.isContentWritten())
            throw new DocumentException(MessageLocalization.getComposedMessage("content.was.already.written.to.the.output"));
        stamper.setEncryption(userPassword, ownerPassword, permissions, strength128Bits ? PdfWriter.STANDARD_ENCRYPTION_128 : PdfWriter.STANDARD_ENCRYPTION_40);
    }

    /** Sets the encryption options for this document. The userPassword and the
     *  ownerPassword can be null or have zero length. In this case the ownerPassword
     *  is replaced by a random string. The open permissions for the document can be
     *  AllowPrinting, AllowModifyContents, AllowCopy, AllowModifyAnnotations,
     *  AllowFillIn, AllowScreenReaders, AllowAssembly and AllowDegradedPrinting.
     *  The permissions can be combined by ORing them.
     * @param userPassword the user password. Can be null or empty
     * @param ownerPassword the owner password. Can be null or empty
     * @param permissions the user permissions
     * @param encryptionType the type of encryption. It can be one of STANDARD_ENCRYPTION_40, STANDARD_ENCRYPTION_128 or ENCRYPTION_AES128.
     * Optionally DO_NOT_ENCRYPT_METADATA can be ored to output the metadata in cleartext
     * @throws DocumentException if the document is already open
     */
    public void setEncryption(byte userPassword[], byte ownerPassword[], int permissions, int encryptionType) throws DocumentException {
        if (stamper.isAppend())
            throw new DocumentException(MessageLocalization.getComposedMessage("append.mode.does.not.support.changing.the.encryption.status"));
        if (stamper.isContentWritten())
            throw new DocumentException(MessageLocalization.getComposedMessage("content.was.already.written.to.the.output"));
        stamper.setEncryption(userPassword, ownerPassword, permissions, encryptionType);
    }

    /**
     * Sets the encryption options for this document. The userPassword and the
     *  ownerPassword can be null or have zero length. In this case the ownerPassword
     *  is replaced by a random string. The open permissions for the document can be
     *  AllowPrinting, AllowModifyContents, AllowCopy, AllowModifyAnnotations,
     *  AllowFillIn, AllowScreenReaders, AllowAssembly and AllowDegradedPrinting.
     *  The permissions can be combined by ORing them.
     * @param strength <code>true</code> for 128 bit key length, <code>false</code> for 40 bit key length
     * @param userPassword the user password. Can be null or empty
     * @param ownerPassword the owner password. Can be null or empty
     * @param permissions the user permissions
     * @throws DocumentException if anything was already written to the output
     */
    public void setEncryption(boolean strength, String userPassword, String ownerPassword, int permissions) throws DocumentException {
        setEncryption(DocWriter.getISOBytes(userPassword), DocWriter.getISOBytes(ownerPassword), permissions, strength);
    }

    /**
     * Sets the encryption options for this document. The userPassword and the
     *  ownerPassword can be null or have zero length. In this case the ownerPassword
     *  is replaced by a random string. The open permissions for the document can be
     *  AllowPrinting, AllowModifyContents, AllowCopy, AllowModifyAnnotations,
     *  AllowFillIn, AllowScreenReaders, AllowAssembly and AllowDegradedPrinting.
     *  The permissions can be combined by ORing them.
     * @param encryptionType the type of encryption. It can be one of STANDARD_ENCRYPTION_40, STANDARD_ENCRYPTION_128 or ENCRYPTION_AES128.
     * Optionally DO_NOT_ENCRYPT_METADATA can be ored to output the metadata in cleartext
     * @param userPassword the user password. Can be null or empty
     * @param ownerPassword the owner password. Can be null or empty
     * @param permissions the user permissions
     * @throws DocumentException if anything was already written to the output
     */
    public void setEncryption(int encryptionType, String userPassword, String ownerPassword, int permissions) throws DocumentException {
        setEncryption(DocWriter.getISOBytes(userPassword), DocWriter.getISOBytes(ownerPassword), permissions, encryptionType);
    }

    /**
     * Sets the certificate encryption options for this document. An array of one or more public certificates
     * must be provided together with an array of the same size for the permissions for each certificate.
     *  The open permissions for the document can be
     *  AllowPrinting, AllowModifyContents, AllowCopy, AllowModifyAnnotations,
     *  AllowFillIn, AllowScreenReaders, AllowAssembly and AllowDegradedPrinting.
     *  The permissions can be combined by ORing them.
     * Optionally DO_NOT_ENCRYPT_METADATA can be ored to output the metadata in cleartext
     * @param certs the public certificates to be used for the encryption
     * @param permissions the user permissions for each of the certificates
     * @param encryptionType the type of encryption. It can be one of STANDARD_ENCRYPTION_40, STANDARD_ENCRYPTION_128 or ENCRYPTION_AES128.
     * @throws DocumentException if the encryption was set too late
     */
     public void setEncryption(Certificate[] certs, int[] permissions, int encryptionType) throws DocumentException {
        if (stamper.isAppend())
            throw new DocumentException(MessageLocalization.getComposedMessage("append.mode.does.not.support.changing.the.encryption.status"));
        if (stamper.isContentWritten())
            throw new DocumentException(MessageLocalization.getComposedMessage("content.was.already.written.to.the.output"));
        stamper.setEncryption(certs, permissions, encryptionType);
     }
     
    /** Gets a page from other PDF document. Note that calling this method more than
     * once with the same parameters will retrieve the same object.
     * @param reader the PDF document where the page is
     * @param pageNumber the page number. The first page is 1
     * @return the template representing the imported page
     */
    public PdfImportedPage getImportedPage(PdfReader reader, int pageNumber) {
        return stamper.getImportedPage(reader, pageNumber);
    }

    /** Gets the underlying PdfWriter.
     * @return the underlying PdfWriter
     */
    public PdfWriter getWriter() {
        return stamper;
    }

    /** Gets the underlying PdfReader.
     * @return the underlying PdfReader
     */
    public PdfReader getReader() {
        return stamper.reader;
    }

    /** Gets the <CODE>AcroFields</CODE> object that allows to get and set field values
     * and to merge FDF forms.
     * @return the <CODE>AcroFields</CODE> object
     */
    public AcroFields getAcroFields() {
        return stamper.getAcroFields();
    }

    /** Determines if the fields are flattened on close. The fields added with
     * {@link #addAnnotation(PdfAnnotation,int)} will never be flattened.
     * @param flat <CODE>true</CODE> to flatten the fields, <CODE>false</CODE>
     * to keep the fields
     */
    public void setFormFlattening(boolean flat) {
        stamper.setFormFlattening(flat);
    }

    /** Determines if the FreeText annotations are flattened on close. 
     * @param flat <CODE>true</CODE> to flatten the FreeText annotations, <CODE>false</CODE>
     * (the default) to keep the FreeText annotations as active content.
     */
    public void setFreeTextFlattening(boolean flat) {
    	stamper.setFreeTextFlattening(flat);
	}

    /**
     * Adds an annotation of form field in a specific page. This page number
     * can be overridden with {@link PdfAnnotation#setPlaceInPage(int)}.
     * @param annot the annotation
     * @param page the page
     */
    public void addAnnotation(PdfAnnotation annot, int page) {
        stamper.addAnnotation(annot, page);
    }

    /**
     * Adds an empty signature.
     * @param name	the name of the signature
     * @param page	the page number
     * @param llx	lower left x coordinate of the signature's position
     * @param lly	lower left y coordinate of the signature's position
     * @param urx	upper right x coordinate of the signature's position
     * @param ury	upper right y coordinate of the signature's position
     * @return	a signature form field
     * @since	2.1.4
     */
    public PdfFormField addSignature(String name, int page, float llx, float lly, float urx, float ury) {
        PdfAcroForm acroForm = stamper.getAcroForm();
        PdfFormField signature = PdfFormField.createSignature(stamper);
        acroForm.setSignatureParams(signature, name, llx, lly, urx, ury);
        acroForm.drawSignatureAppearences(signature, llx, lly, urx, ury);
        addAnnotation(signature, page);
        return signature;
    }
    
    /**
     * Adds the comments present in an FDF file.
     * @param fdf the FDF file
     * @throws IOException on error
     */    
    public void addComments(FdfReader fdf) throws IOException {
        stamper.addComments(fdf);
    }
    
    /**
     * Sets the bookmarks. The list structure is defined in
     * {@link SimpleBookmark}.
     * @param outlines the bookmarks or <CODE>null</CODE> to remove any
     */
    public void setOutlines(List outlines) {
        stamper.setOutlines(outlines);
    }

    /**
     * Sets the thumbnail image for a page.
     * @param image the image
     * @param page the page
     * @throws PdfException on error
     * @throws DocumentException on error
     */    
    public void setThumbnail(Image image, int page) throws PdfException, DocumentException {
        stamper.setThumbnail(image, page);
    }
    
    /**
     * Adds <CODE>name</CODE> to the list of fields that will be flattened on close,
     * all the other fields will remain. If this method is never called or is called
     * with invalid field names, all the fields will be flattened.
     * <p>
     * Calling <CODE>setFormFlattening(true)</CODE> is needed to have any kind of
     * flattening.
     * @param name the field name
     * @return <CODE>true</CODE> if the field exists, <CODE>false</CODE> otherwise
     */
    public boolean partialFormFlattening(String name) {
        return stamper.partialFormFlattening(name);
    }

    /** Adds a JavaScript action at the document level. When the document
     * opens all this JavaScript runs. The existing JavaScript will be replaced.
     * @param js the JavaScript code
     */
    public void addJavaScript(String js) {
        stamper.addJavaScript(js, !PdfEncodings.isPdfDocEncoding(js));
    }

    /** Adds a file attachment at the document level. Existing attachments will be kept.
     * @param description the file description
     * @param fileStore an array with the file. If it's <CODE>null</CODE>
     * the file will be read from the disk
     * @param file the path to the file. It will only be used if
     * <CODE>fileStore</CODE> is not <CODE>null</CODE>
     * @param fileDisplay the actual file name stored in the pdf
     * @throws IOException on error
     */    
    public void addFileAttachment(String description, byte fileStore[], String file, String fileDisplay) throws IOException {
        addFileAttachment(description, PdfFileSpecification.fileEmbedded(stamper, file, fileDisplay, fileStore));
    }

    /** Adds a file attachment at the document level. Existing attachments will be kept.
     * @param description the file description
     * @param fs the file specification
     */    
    public void addFileAttachment(String description, PdfFileSpecification fs) throws IOException {
        stamper.addFileAttachment(description, fs);
    }

    /**
     * This is the most simple way to change a PDF into a
     * portable collection. Choose one of the following names:
     * <ul>
     * <li>PdfName.D (detailed view)
     * <li>PdfName.T (tiled view)
     * <li>PdfName.H (hidden)
     * </ul>
     * Pass this name as a parameter and your PDF will be
     * a portable collection with all the embedded and
     * attached files as entries.
     * @param initialView can be PdfName.D, PdfName.T or PdfName.H
     */
    public void makePackage( PdfName initialView ) {
    	PdfCollection collection = new PdfCollection(0);
    	collection.put(PdfName.VIEW, initialView);
    	stamper.makePackage( collection );
    }

    /**
     * Adds or replaces the Collection Dictionary in the Catalog.
     * @param	collection	the new collection dictionary.
     */
    public void makePackage(PdfCollection collection) {
    	stamper.makePackage(collection);    	
    }
    
    /**
     * Sets the viewer preferences.
     * @param preferences the viewer preferences
     * @see PdfViewerPreferences#setViewerPreferences(int)
     */
    public void setViewerPreferences(int preferences) {
        stamper.setViewerPreferences(preferences);
    }
    
    /** Adds a viewer preference
     * @param key a key for a viewer preference
     * @param value the value for the viewer preference
     * @see PdfViewerPreferences#addViewerPreference
     */
    
    public void addViewerPreference(PdfName key, PdfObject value) {
    	stamper.addViewerPreference(key, value);
    }

    /**
     * Sets the XMP metadata.
     * @param xmp
     * @see PdfWriter#setXmpMetadata(byte[])
     */
    public void setXmpMetadata(byte[] xmp) {
        stamper.setXmpMetadata(xmp);
    }

    /**
     * Gets the 1.5 compression status.
     * @return <code>true</code> if the 1.5 compression is on
     */
    public boolean isFullCompression() {
        return stamper.isFullCompression();
    }

    /**
     * Sets the document's compression to the new 1.5 mode with object streams and xref
     * streams. It can be set at any time but once set it can't be unset.
     */
    public void setFullCompression() {
        if (stamper.isAppend())
            return;
        stamper.setFullCompression();
    }

    /**
     * Sets the open and close page additional action.
     * @param actionType the action type. It can be <CODE>PdfWriter.PAGE_OPEN</CODE>
     * or <CODE>PdfWriter.PAGE_CLOSE</CODE>
     * @param action the action to perform
     * @param page the page where the action will be applied. The first page is 1
     * @throws PdfException if the action type is invalid
     */    
    public void setPageAction(PdfName actionType, PdfAction action, int page) throws PdfException {
        stamper.setPageAction(actionType, action, page);
    }

    /**
     * Sets the display duration for the page (for presentations)
     * @param seconds   the number of seconds to display the page. A negative value removes the entry
     * @param page the page where the duration will be applied. The first page is 1
     */
    public void setDuration(int seconds, int page) {
        stamper.setDuration(seconds, page);
    }
    
    /**
     * Sets the transition for the page
     * @param transition   the transition object. A <code>null</code> removes the transition
     * @param page the page where the transition will be applied. The first page is 1
     */
    public void setTransition(PdfTransition transition, int page) {
        stamper.setTransition(transition, page);
    }

    /**
     * Applies a digital signature to a document, possibly as a new revision, making
     * possible multiple signatures. The returned PdfStamper
     * can be used normally as the signature is only applied when closing.
     * <p>
     * A possible use for adding a signature without invalidating an existing one is:
     * <p>
     * <pre>
     * KeyStore ks = KeyStore.getInstance("pkcs12");
     * ks.load(new FileInputStream("my_private_key.pfx"), "my_password".toCharArray());
     * String alias = (String)ks.aliases().nextElement();
     * PrivateKey key = (PrivateKey)ks.getKey(alias, "my_password".toCharArray());
     * Certificate[] chain = ks.getCertificateChain(alias);
     * PdfReader reader = new PdfReader("original.pdf");
     * FileOutputStream fout = new FileOutputStream("signed.pdf");
     * PdfStamper stp = PdfStamper.createSignature(reader, fout, '\0', new
     * File("/temp"), true);
     * PdfSignatureAppearance sap = stp.getSignatureAppearance();
     * sap.setCrypto(key, chain, null, PdfSignatureAppearance.WINCER_SIGNED);
     * sap.setReason("I'm the author");
     * sap.setLocation("Lisbon");
     * // comment next line to have an invisible signature
     * sap.setVisibleSignature(new Rectangle(100, 100, 200, 200), 1, null);
     * stp.close();
     * </pre>
     * @param reader the original document
     * @param os the output stream or <CODE>null</CODE> to keep the document in the temporary file
     * @param pdfVersion the new pdf version or '\0' to keep the same version as the original
     * document
     * @param tempFile location of the temporary file. If it's a directory a temporary file will be created there.
     *     If it's a file it will be used directly. The file will be deleted on exit unless <CODE>os</CODE> is null.
     *     In that case the document can be retrieved directly from the temporary file. If it's <CODE>null</CODE>
     *     no temporary file will be created and memory will be used
     * @param append if <CODE>true</CODE> the signature and all the other content will be added as a
     * new revision thus not invalidating existing signatures
     * @return a <CODE>PdfStamper</CODE>
     * @throws DocumentException on error
     * @throws IOException on error
     */
    public static PdfStamper createSignature(PdfReader reader, OutputStream os, char pdfVersion, File tempFile, boolean append) throws DocumentException, IOException {
        PdfStamper stp;
        if (tempFile == null) {
            ByteBuffer bout = new ByteBuffer();
            stp = new PdfStamper(reader, bout, pdfVersion, append);
            stp.sigApp = new PdfSignatureAppearance(stp.stamper);
            stp.sigApp.setSigout(bout);
        }
        else {
            if (tempFile.isDirectory())
                tempFile = File.createTempFile("pdf", null, tempFile);
            FileOutputStream fout = new FileOutputStream(tempFile);
            stp = new PdfStamper(reader, fout, pdfVersion, append);
            stp.sigApp = new PdfSignatureAppearance(stp.stamper);
            stp.sigApp.setTempFile(tempFile);
        }
        stp.sigApp.setOriginalout(os);
        stp.sigApp.setStamper(stp);
        stp.hasSignature = true;
        PdfDictionary catalog = reader.getCatalog();
        PdfDictionary acroForm = (PdfDictionary)PdfReader.getPdfObject(catalog.get(PdfName.ACROFORM), catalog);
        if (acroForm != null) {
            acroForm.remove(PdfName.NEEDAPPEARANCES);
            stp.stamper.markUsed(acroForm);
        }
        return stp;
    }

    /**
     * Applies a digital signature to a document. The returned PdfStamper
     * can be used normally as the signature is only applied when closing.
     * <p>
     * Note that the pdf is created in memory.
     * <p>
     * A possible use is:
     * <p>
     * <pre>
     * KeyStore ks = KeyStore.getInstance("pkcs12");
     * ks.load(new FileInputStream("my_private_key.pfx"), "my_password".toCharArray());
     * String alias = (String)ks.aliases().nextElement();
     * PrivateKey key = (PrivateKey)ks.getKey(alias, "my_password".toCharArray());
     * Certificate[] chain = ks.getCertificateChain(alias);
     * PdfReader reader = new PdfReader("original.pdf");
     * FileOutputStream fout = new FileOutputStream("signed.pdf");
     * PdfStamper stp = PdfStamper.createSignature(reader, fout, '\0');
     * PdfSignatureAppearance sap = stp.getSignatureAppearance();
     * sap.setCrypto(key, chain, null, PdfSignatureAppearance.WINCER_SIGNED);
     * sap.setReason("I'm the author");
     * sap.setLocation("Lisbon");
     * // comment next line to have an invisible signature
     * sap.setVisibleSignature(new Rectangle(100, 100, 200, 200), 1, null);
     * stp.close();
     * </pre>
     * @param reader the original document
     * @param os the output stream
     * @param pdfVersion the new pdf version or '\0' to keep the same version as the original
     * document
     * @throws DocumentException on error
     * @throws IOException on error
     * @return a <CODE>PdfStamper</CODE>
     */
    public static PdfStamper createSignature(PdfReader reader, OutputStream os, char pdfVersion) throws DocumentException, IOException {
        return createSignature(reader, os, pdfVersion, null, false);
    }
    
    /**
     * Applies a digital signature to a document. The returned PdfStamper
     * can be used normally as the signature is only applied when closing.
     * <p>
     * A possible use is:
     * <p>
     * <pre>
     * KeyStore ks = KeyStore.getInstance("pkcs12");
     * ks.load(new FileInputStream("my_private_key.pfx"), "my_password".toCharArray());
     * String alias = (String)ks.aliases().nextElement();
     * PrivateKey key = (PrivateKey)ks.getKey(alias, "my_password".toCharArray());
     * Certificate[] chain = ks.getCertificateChain(alias);
     * PdfReader reader = new PdfReader("original.pdf");
     * FileOutputStream fout = new FileOutputStream("signed.pdf");
     * PdfStamper stp = PdfStamper.createSignature(reader, fout, '\0', new File("/temp"));
     * PdfSignatureAppearance sap = stp.getSignatureAppearance();
     * sap.setCrypto(key, chain, null, PdfSignatureAppearance.WINCER_SIGNED);
     * sap.setReason("I'm the author");
     * sap.setLocation("Lisbon");
     * // comment next line to have an invisible signature
     * sap.setVisibleSignature(new Rectangle(100, 100, 200, 200), 1, null);
     * stp.close();
     * </pre>
     * @param reader the original document
     * @param os the output stream or <CODE>null</CODE> to keep the document in the temporary file
     * @param pdfVersion the new pdf version or '\0' to keep the same version as the original
     * document
     * @param tempFile location of the temporary file. If it's a directory a temporary file will be created there.
     *     If it's a file it will be used directly. The file will be deleted on exit unless <CODE>os</CODE> is null.
     *     In that case the document can be retrieved directly from the temporary file. If it's <CODE>null</CODE>
     *     no temporary file will be created and memory will be used
     * @return a <CODE>PdfStamper</CODE>
     * @throws DocumentException on error
     * @throws IOException on error
     */
    public static PdfStamper createSignature(PdfReader reader, OutputStream os, char pdfVersion, File tempFile) throws DocumentException, IOException 
    {
        return createSignature(reader, os, pdfVersion, tempFile, false);
    }
    
    /**
     * Gets the PdfLayer objects in an existing document as a Map
     * with the names/titles of the layers as keys.
     * @return	a Map with all the PdfLayers in the document (and the name/title of the layer as key)
     * @since	2.1.2
     */
    public Map getPdfLayers() {
    	return stamper.getPdfLayers();
    }
}
