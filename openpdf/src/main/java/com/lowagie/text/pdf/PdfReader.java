/*
 * $Id: PdfReader.java 4096 2009-11-12 15:31:13Z blowagie $
 *
 * Copyright 2001, 2002 Paulo Soares
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
 * https://github.com/LibrePDF/OpenPDF
 */

package com.lowagie.text.pdf;

import com.lowagie.bouncycastle.BouncyCastleHelper;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.PageSize;
import com.lowagie.text.Rectangle;
import com.lowagie.text.error_messages.MessageLocalization;
import com.lowagie.text.exceptions.BadPasswordException;
import com.lowagie.text.exceptions.InvalidPdfException;
import com.lowagie.text.exceptions.UnsupportedPdfException;
import com.lowagie.text.pdf.interfaces.PdfViewerPreferences;
import com.lowagie.text.pdf.internal.PdfViewerPreferencesImp;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.Key;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.zip.InflaterInputStream;

/**
 * Reads a PDF document.
 * 
 * @author Paulo Soares (psoares@consiste.pt)
 * @author Kazuya Ujihara
 */
public class PdfReader implements PdfViewerPreferences, Closeable {

  static final PdfName[] pageInhCandidates = {PdfName.MEDIABOX,
          PdfName.ROTATE, PdfName.RESOURCES, PdfName.CROPBOX};

  private static final byte[] endstream = PdfEncodings
          .convertToBytes("endstream", null);
  private static final byte[] endobj = PdfEncodings.convertToBytes("endobj", null);
  protected PRTokeniser tokens;
  // Each xref pair is a position
  // type 0 -> -1, 0
  // type 1 -> offset, 0
  // type 2 -> index, obj num
  protected int[] xref;
  protected Map<Integer, IntHashtable> objStmMark;
  protected IntHashtable objStmToOffset;
  protected boolean newXrefType;
  private List<PdfObject> xrefObj;
  PdfDictionary rootPages;
  protected PdfDictionary trailer;
  protected PdfDictionary catalog;
  protected PageRefs pageRefs;
  protected PRAcroForm acroForm = null;
  protected boolean acroFormParsed = false;
  protected boolean encrypted = false;
  protected boolean rebuilt = false;
  protected int freeXref;
  protected boolean tampered = false;
  protected int lastXref;
  protected int eofPos;
  protected char pdfVersion;
  protected PdfEncryption decrypt;
  protected byte[] password = null; // added by ujihara for decryption
  protected Key certificateKey = null; // added by Aiken Sam for certificate
                                       // decryption
  protected Certificate certificate = null; // added by Aiken Sam for
                                            // certificate decryption
  protected String certificateKeyProvider = null; // added by Aiken Sam for
                                                  // certificate decryption
  private boolean ownerPasswordUsed;

  // allow the PDF to be modified even if the owner password was not supplied
  // if encrypted (non-encrypted documents may be modified regardless)
  private boolean modificationAllowedWithoutOwnerPassword = true;

  protected List<PdfObject> strings = new ArrayList<>();
  protected boolean sharedStreams = true;
  protected boolean consolidateNamedDestinations = false;
  protected boolean remoteToLocalNamedDestinations = false;
  protected int rValue;
  protected int pValue;
  private int objNum;
  private int objGen;
  private int fileLength;
  private boolean hybridXref;
  private int lastXrefPartial = -1;
  private boolean partial;

  private PRIndirectReference cryptoRef;
  private final PdfViewerPreferencesImp viewerPreferences = new PdfViewerPreferencesImp();
  private boolean encryptionError;

  /**
   * Holds value of property appendable.
   */
  private boolean appendable;

  protected PdfReader() {
  }

  /**
   * Reads and parses a PDF document.
   * 
   * @param filename
   *          the file name of the document
   * @throws IOException
   *           on error
   */
  public PdfReader(String filename) throws IOException {
    this(filename, null);
  }

  /**
   * Reads and parses a PDF document.
   * 
   * @param filename
   *          the file name of the document
   * @param ownerPassword
   *          the password to read the document
   * @throws IOException
   *           on error
   */
  public PdfReader(String filename, byte[] ownerPassword) throws IOException {
    password = ownerPassword;
    tokens = new PRTokeniser(filename);
    readPdf();
  }

  /**
   * Reads and parses a PDF document.
   * 
   * @param pdfIn
   *          the byte array with the document
   * @throws IOException
   *           on error
   */
  public PdfReader(byte[] pdfIn) throws IOException {
    this(pdfIn, null);
  }

  /**
   * Reads and parses a PDF document.
   * 
   * @param pdfIn
   *          the byte array with the document
   * @param ownerPassword
   *          the password to read the document
   * @throws IOException
   *           on error
   */
  public PdfReader(byte[] pdfIn, byte[] ownerPassword) throws IOException {
    password = ownerPassword;
    tokens = new PRTokeniser(pdfIn);
    readPdf();
  }

  /**
   * Reads and parses a PDF document.
   * 
   * @param filename
   *          the file name of the document
   * @param certificate
   *          the certificate to read the document
   * @param certificateKey
   *          the private key of the certificate
   * @param certificateKeyProvider
   *          the security provider for certificateKey
   * @throws IOException
   *           on error
   */
  public PdfReader(String filename, Certificate certificate,
      Key certificateKey, String certificateKeyProvider) throws IOException {
    this.certificate = certificate;
    this.certificateKey = certificateKey;
    this.certificateKeyProvider = certificateKeyProvider;
    tokens = new PRTokeniser(filename);
    readPdf();
  }

  /**
   * Reads and parses a PDF document.
   * 
   * @param url
   *          the URL of the document
   * @throws IOException
   *           on error
   */
  public PdfReader(URL url) throws IOException {
    this(url, null);
  }

  /**
   * Reads and parses a PDF document.
   * 
   * @param url
   *          the URL of the document
   * @param ownerPassword
   *          the password to read the document
   * @throws IOException
   *           on error
   */
  public PdfReader(URL url, byte[] ownerPassword) throws IOException {
    password = ownerPassword;
    tokens = new PRTokeniser(new RandomAccessFileOrArray(url));
    readPdf();
  }

  /**
   * Reads and parses a PDF document.
   * 
   * @param is
   *          the <CODE>InputStream</CODE> containing the document. The stream
   *          is read to the end but is not closed
   * @param ownerPassword
   *          the password to read the document
   * @throws IOException
   *           on error
   */
  public PdfReader(InputStream is, byte[] ownerPassword) throws IOException {
    password = ownerPassword;
    tokens = new PRTokeniser(new RandomAccessFileOrArray(is));
    readPdf();
  }

  /**
   * Reads and parses a PDF document.
   * 
   * @param is
   *          the <CODE>InputStream</CODE> containing the document. The stream
   *          is read to the end but is not closed
   * @throws IOException
   *           on error
   */
  public PdfReader(InputStream is) throws IOException {
    this(is, null);
  }

  /**
   * Reads and parses a pdf document. Contrary to the other constructors only
   * the xref is read into memory. The reader is said to be working in "partial"
   * mode as only parts of the pdf are read as needed. The pdf is left open but
   * may be closed at any time with <CODE>PdfReader.close()</CODE>, reopen is
   * automatic.
   * 
   * @param raf
   *          the document location
   * @param ownerPassword
   *          the password or <CODE>null</CODE> for no password
   * @throws IOException
   *           on error
   */
  public PdfReader(RandomAccessFileOrArray raf, byte[] ownerPassword)
      throws IOException {
    password = ownerPassword;
    partial = true;
    tokens = new PRTokeniser(raf);
    readPdfPartial();
  }

  /**
   * Creates an independent duplicate.
   * 
   * @param reader
   *          the <CODE>PdfReader</CODE> to duplicate
   */
  public PdfReader(PdfReader reader) {
    this.appendable = reader.appendable;
    this.consolidateNamedDestinations = reader.consolidateNamedDestinations;
    this.encrypted = reader.encrypted;
    this.rebuilt = reader.rebuilt;
    this.sharedStreams = reader.sharedStreams;
    this.tampered = reader.tampered;
    this.password = reader.password;
    this.pdfVersion = reader.pdfVersion;
    this.eofPos = reader.eofPos;
    this.freeXref = reader.freeXref;
    this.lastXref = reader.lastXref;
    this.tokens = new PRTokeniser(reader.tokens.getSafeFile());
    if (reader.decrypt != null)
      this.decrypt = new PdfEncryption(reader.decrypt);
    this.pValue = reader.pValue;
    this.rValue = reader.rValue;
    this.xrefObj = new ArrayList<>(reader.xrefObj);
    for (int k = 0; k < reader.xrefObj.size(); ++k) {
      this.xrefObj.set(k,
              duplicatePdfObject(reader.xrefObj.get(k), this));
    }
    this.pageRefs = new PageRefs(reader.pageRefs, this);
    this.trailer = (PdfDictionary) duplicatePdfObject(reader.trailer, this);
    this.catalog = trailer.getAsDict(PdfName.ROOT);
    this.rootPages = catalog.getAsDict(PdfName.PAGES);
    this.fileLength = reader.fileLength;
    this.partial = reader.partial;
    this.hybridXref = reader.hybridXref;
    this.objStmToOffset = reader.objStmToOffset;
    this.xref = reader.xref;
    this.cryptoRef = (PRIndirectReference) duplicatePdfObject(reader.cryptoRef,
        this);
    this.ownerPasswordUsed = reader.ownerPasswordUsed;
  }

  /**
   * Gets a new file instance of the original PDF document.
   * 
   * @return a new file instance of the original PDF document
   */
  public RandomAccessFileOrArray getSafeFile() {
    return tokens.getSafeFile();
  }

  protected PdfReaderInstance getPdfReaderInstance(PdfWriter writer) {
    return new PdfReaderInstance(this, writer);
  }

  /**
   * Gets the number of pages in the document.
   * 
   * @return the number of pages in the document
   */
  public int getNumberOfPages() {
    return pageRefs.size();
  }

  /**
   * Returns the document's catalog. This dictionary is not a copy, any changes
   * will be reflected in the catalog.
   * 
   * @return the document's catalog
   */
  public PdfDictionary getCatalog() {
    return catalog;
  }

  /**
   * Returns the document's acroform, if it has one.
   * 
   * @return the document's acroform
   */
  public PRAcroForm getAcroForm() {
    if (!acroFormParsed) {
      acroFormParsed = true;
      PdfObject form = catalog.get(PdfName.ACROFORM);
      if (form != null) {
        try {
          acroForm = new PRAcroForm(this);
          acroForm.readAcroForm((PdfDictionary) getPdfObject(form));
        } catch (Exception e) {
          acroForm = null;
        }
      }
    }
    return acroForm;
  }

  /**
   * Gets the page rotation. This value can be 0, 90, 180 or 270.
   * 
   * @param index
   *          the page number. The first page is 1
   * @return the page rotation
   */
  public int getPageRotation(int index) {
    return getPageRotation(pageRefs.getPageNRelease(index));
  }

  int getPageRotation(PdfDictionary page) {
    PdfNumber rotate = page.getAsNumber(PdfName.ROTATE);
    if (rotate == null)
      return 0;
    else {
      int n = rotate.intValue();
      n %= 360;
      return n < 0 ? n + 360 : n;
    }
  }

  /**
   * Gets the page size, taking rotation into account. This is a
   * <CODE>Rectangle</CODE> with the value of the /MediaBox and the /Rotate key.
   * 
   * @param index
   *          the page number. The first page is 1
   * @return a <CODE>Rectangle</CODE>
   */
  public Rectangle getPageSizeWithRotation(int index) {
    return getPageSizeWithRotation(pageRefs.getPageNRelease(index));
  }

  /**
   * Gets the rotated page from a page dictionary.
   * 
   * @param page
   *          the page dictionary
   * @return the rotated page
   */
  public Rectangle getPageSizeWithRotation(PdfDictionary page) {
    Rectangle rect = getPageSize(page);
    int rotation = getPageRotation(page);
    while (rotation > 0) {
      rect = rect.rotate();
      rotation -= 90;
    }
    return rect;
  }

  /**
   * Gets the page size without taking rotation into account. This is the value
   * of the /MediaBox key.
   * 
   * @param index
   *          the page number. The first page is 1
   * @return the page size
   */
  public Rectangle getPageSize(int index) {
    return getPageSize(pageRefs.getPageNRelease(index));
  }

  /**
   * Gets the page from a page dictionary
   * 
   * @param page
   *          the page dictionary
   * @return the page
   */
  public Rectangle getPageSize(PdfDictionary page) {
    PdfArray mediaBox = page.getAsArray(PdfName.MEDIABOX);
    return getNormalizedRectangle(mediaBox);
  }

  /**
   * Gets the crop box without taking rotation into account. This is the value
   * of the /CropBox key. The crop box is the part of the document to be
   * displayed or printed. It usually is the same as the media box but may be
   * smaller. If the page doesn't have a crop box the page size will be
   * returned.
   * 
   * @param index
   *          the page number. The first page is 1
   * @return the crop box
   */
  public Rectangle getCropBox(int index) {
    PdfDictionary page = pageRefs.getPageNRelease(index);
    PdfArray cropBox = (PdfArray) getPdfObjectRelease(page.get(PdfName.CROPBOX));
    if (cropBox == null)
      return getPageSize(page);
    return getNormalizedRectangle(cropBox);
  }

  /**
   * Gets the box size. Allowed names are: "crop", "trim", "art", "bleed" and
   * "media".
   * 
   * @param index
   *          the page number. The first page is 1
   * @param boxName
   *          the box name
   * @return the box rectangle or null
   */
  public Rectangle getBoxSize(int index, String boxName) {
    PdfDictionary page = pageRefs.getPageNRelease(index);
    PdfArray box = null;
      switch (boxName) {
          case "trim":
              box = (PdfArray) getPdfObjectRelease(page.get(PdfName.TRIMBOX));
              break;
          case "art":
              box = (PdfArray) getPdfObjectRelease(page.get(PdfName.ARTBOX));
              break;
          case "bleed":
              box = (PdfArray) getPdfObjectRelease(page.get(PdfName.BLEEDBOX));
              break;
          case "crop":
              box = (PdfArray) getPdfObjectRelease(page.get(PdfName.CROPBOX));
              break;
          case "media":
              box = (PdfArray) getPdfObjectRelease(page.get(PdfName.MEDIABOX));
              break;
      }
    if (box == null)
      return null;
    return getNormalizedRectangle(box);
  }

  /**
   * Returns the content of the document information dictionary as a
   * <CODE>HashMap</CODE> of <CODE>String</CODE>.
   * 
   * @return content of the document information dictionary
   */
  public Map<String, String> getInfo() {
    Map<String, String> map = new HashMap<>();
    PdfDictionary info = trailer.getAsDict(PdfName.INFO);
    if (info == null)
      return map;
    for (Object o : info.getKeys()) {
      PdfName key = (PdfName) o;
      PdfObject obj = getPdfObject(info.get(key));
      if (obj == null)
        continue;
      String value = obj.toString();
      switch (obj.type()) {
        case PdfObject.STRING: {
          value = ((PdfString) obj).toUnicodeString();
          break;
        }
        case PdfObject.NAME: {
          value = PdfName.decodeName(value);
          break;
        }
      }
      map.put(PdfName.decodeName(key.toString()), value);
    }
    return map;
  }

  /**
   * Normalizes a <CODE>Rectangle</CODE> so that llx and lly are smaller than
   * urx and ury.
   * 
   * @param box
   *          the original rectangle
   * @return a normalized <CODE>Rectangle</CODE>
   */
  public static Rectangle getNormalizedRectangle(PdfArray box) {
    float llx = ((PdfNumber) getPdfObjectRelease(box.getPdfObject(0)))
        .floatValue();
    float lly = ((PdfNumber) getPdfObjectRelease(box.getPdfObject(1)))
        .floatValue();
    float urx = ((PdfNumber) getPdfObjectRelease(box.getPdfObject(2)))
        .floatValue();
    float ury = ((PdfNumber) getPdfObjectRelease(box.getPdfObject(3)))
        .floatValue();
    return new Rectangle(Math.min(llx, urx), Math.min(lly, ury), Math.max(llx,
        urx), Math.max(lly, ury));
  }

  protected void readPdf() throws IOException {
    try {
      fileLength = tokens.getFile().length();
      pdfVersion = tokens.checkPdfHeader();
      try {
        readXref();
      } catch (Exception e) {
        try {
          rebuilt = true;
          rebuildXref();
          lastXref = -1;
        } catch (Exception ne) {
          throw new InvalidPdfException(MessageLocalization.getComposedMessage(
              "rebuild.failed.1.original.message.2", ne.getMessage(),
              e.getMessage()));
        }
      }
      try {
        readDocObj();
      } catch (Exception e) {
        if (e instanceof BadPasswordException)
          throw new BadPasswordException(e.getMessage());
        if (rebuilt || encryptionError)
          throw new InvalidPdfException(e.getMessage());
        rebuilt = true;
        encrypted = false;
        rebuildXref();
        lastXref = -1;
        readDocObj();
      }

      strings.clear();
      readPages();
      eliminateSharedStreams();
      removeUnusedObjects();
    } finally {
      try {
        tokens.close();
      } catch (Exception e) {
        // empty on purpose
      }
    }
  }

  protected void readPdfPartial() throws IOException {
    try {
      fileLength = tokens.getFile().length();
      pdfVersion = tokens.checkPdfHeader();
      try {
        readXref();
      } catch (Exception e) {
        try {
          rebuilt = true;
          rebuildXref();
          lastXref = -1;
        } catch (Exception ne) {
          throw new InvalidPdfException(MessageLocalization.getComposedMessage(
              "rebuild.failed.1.original.message.2", ne.getMessage(),
              e.getMessage()));
        }
      }
      readDocObjPartial();
      readPages();
    } catch (IOException e) {
      try {
        tokens.close();
      } catch (Exception ignored) {
      }
      throw e;
    }
  }

  private boolean equalsArray(byte[] ar1, byte[] ar2, int size) {
    for (int k = 0; k < size; ++k) {
      if (ar1[k] != ar2[k])
        return false;
    }
    return true;
  }

  /**
   */
  private void readDecryptedDocObj() throws IOException {
    if (encrypted)
      return;
    if (trailer == null) {
      return;
    }
    PdfObject encDic = trailer.get(PdfName.ENCRYPT);
    if (encDic == null || encDic.toString().equals("null"))
      return;
    encryptionError = true;
    byte[] encryptionKey = null;
    encrypted = true;
    PdfDictionary enc = (PdfDictionary) getPdfObject(encDic);

    String s;
    PdfObject o;

    PdfArray documentIDs = trailer.getAsArray(PdfName.ID);
    byte[] documentID = null;
    if (documentIDs != null) {
      o = documentIDs.getPdfObject(0);
      strings.remove(o);
      s = o.toString();
      documentID = com.lowagie.text.DocWriter.getISOBytes(s);
      if (documentIDs.size() > 1)
        strings.remove(documentIDs.getPdfObject(1));
    }
    // just in case we have a broken producer
    if (documentID == null)
      documentID = new byte[0];
    byte[] uValue = null;
    byte[] oValue = null;
    int cryptoMode = PdfWriter.STANDARD_ENCRYPTION_40;
    int lengthValue = 0;

    PdfObject filter = getPdfObjectRelease(enc.get(PdfName.FILTER));

    if (filter.equals(PdfName.STANDARD)) {
      s = enc.get(PdfName.U).toString();
      strings.remove(enc.get(PdfName.U));
      uValue = com.lowagie.text.DocWriter.getISOBytes(s);
      s = enc.get(PdfName.O).toString();
      strings.remove(enc.get(PdfName.O));
      oValue = com.lowagie.text.DocWriter.getISOBytes(s);

      o = enc.get(PdfName.P);
      if (!o.isNumber())
        throw new InvalidPdfException(
            MessageLocalization.getComposedMessage("illegal.p.value"));
      pValue = ((PdfNumber) o).intValue();

      o = enc.get(PdfName.R);
      if (!o.isNumber())
        throw new InvalidPdfException(
            MessageLocalization.getComposedMessage("illegal.r.value"));
      rValue = ((PdfNumber) o).intValue();

      switch (rValue) {
      case 2:
        cryptoMode = PdfWriter.STANDARD_ENCRYPTION_40;
        break;
      case 3:
        o = enc.get(PdfName.LENGTH);
        if (!o.isNumber())
          throw new InvalidPdfException(
              MessageLocalization.getComposedMessage("illegal.length.value"));
        lengthValue = ((PdfNumber) o).intValue();
        if (lengthValue > 128 || lengthValue < 40 || lengthValue % 8 != 0)
          throw new InvalidPdfException(
              MessageLocalization.getComposedMessage("illegal.length.value"));
        cryptoMode = PdfWriter.STANDARD_ENCRYPTION_128;
        break;
      case 4:
        PdfDictionary dic = (PdfDictionary) enc.get(PdfName.CF);
        if (dic == null)
          throw new InvalidPdfException(
              MessageLocalization.getComposedMessage("cf.not.found.encryption"));
        dic = (PdfDictionary) dic.get(PdfName.STDCF);
        if (dic == null)
          throw new InvalidPdfException(
              MessageLocalization
                  .getComposedMessage("stdcf.not.found.encryption"));
        if (PdfName.V2.equals(dic.get(PdfName.CFM)))
          cryptoMode = PdfWriter.STANDARD_ENCRYPTION_128;
        else if (PdfName.AESV2.equals(dic.get(PdfName.CFM)))
          cryptoMode = PdfWriter.ENCRYPTION_AES_128;
        else
          throw new UnsupportedPdfException(
              MessageLocalization
                  .getComposedMessage("no.compatible.encryption.found"));
        PdfObject em = enc.get(PdfName.ENCRYPTMETADATA);
        if (em != null && em.toString().equals("false"))
          cryptoMode |= PdfWriter.DO_NOT_ENCRYPT_METADATA;
        break;
      case 6:
        cryptoMode = PdfWriter.ENCRYPTION_AES_256_V3;
        em = enc.get(PdfName.ENCRYPTMETADATA);
        if (em != null && em.toString().equals("false"))
          cryptoMode |= PdfWriter.DO_NOT_ENCRYPT_METADATA;
        break;
      default:
        throw new UnsupportedPdfException(
            MessageLocalization.getComposedMessage(
                "unknown.encryption.type.r.eq.1", rValue));
      }
    } else if (filter.equals(PdfName.PUBSEC)) {
      PdfArray recipients;

      o = enc.get(PdfName.V);
      if (!o.isNumber())
        throw new InvalidPdfException(
            MessageLocalization.getComposedMessage("illegal.v.value"));
      int vValue = ((PdfNumber) o).intValue();
      switch (vValue) {
      case 1:
        cryptoMode = PdfWriter.STANDARD_ENCRYPTION_40;
        lengthValue = 40;
        recipients = (PdfArray) enc.get(PdfName.RECIPIENTS);
        break;
      case 2:
        o = enc.get(PdfName.LENGTH);
        if (!o.isNumber())
          throw new InvalidPdfException(
              MessageLocalization.getComposedMessage("illegal.length.value"));
        lengthValue = ((PdfNumber) o).intValue();
        if (lengthValue > 128 || lengthValue < 40 || lengthValue % 8 != 0)
          throw new InvalidPdfException(
              MessageLocalization.getComposedMessage("illegal.length.value"));
        cryptoMode = PdfWriter.STANDARD_ENCRYPTION_128;
        recipients = (PdfArray) enc.get(PdfName.RECIPIENTS);
        break;
      case 4:
        PdfDictionary dic = (PdfDictionary) enc.get(PdfName.CF);
        if (dic == null)
          throw new InvalidPdfException(
              MessageLocalization.getComposedMessage("cf.not.found.encryption"));
        dic = (PdfDictionary) dic.get(PdfName.DEFAULTCRYPTFILTER);
        if (dic == null)
          throw new InvalidPdfException(
              MessageLocalization
                  .getComposedMessage("defaultcryptfilter.not.found.encryption"));
        if (PdfName.V2.equals(dic.get(PdfName.CFM))) {
          cryptoMode = PdfWriter.STANDARD_ENCRYPTION_128;
          lengthValue = 128;
        } else if (PdfName.AESV2.equals(dic.get(PdfName.CFM))) {
          cryptoMode = PdfWriter.ENCRYPTION_AES_128;
          lengthValue = 128;
        } else
          throw new UnsupportedPdfException(
              MessageLocalization
                  .getComposedMessage("no.compatible.encryption.found"));
        PdfObject em = dic.get(PdfName.ENCRYPTMETADATA);
        if (em != null && em.toString().equals("false"))
          cryptoMode |= PdfWriter.DO_NOT_ENCRYPT_METADATA;

        recipients = (PdfArray) dic.get(PdfName.RECIPIENTS);
        break;
      default:
        throw new UnsupportedPdfException(
            MessageLocalization.getComposedMessage(
                "unknown.encryption.type.v.eq.1", rValue));
      }
      BouncyCastleHelper.checkCertificateEncodingOrThrowException(certificate);
      byte[] envelopedData = BouncyCastleHelper.getEnvelopedData(recipients, strings, certificate, certificateKey, certificateKeyProvider);

      if (envelopedData == null) {
        throw new UnsupportedPdfException(
            MessageLocalization.getComposedMessage("bad.certificate.and.key"));
      }

      MessageDigest md;

      try {
        md = MessageDigest.getInstance("SHA-1");
        md.update(envelopedData, 0, 20);
        for (int i = 0; i < recipients.size(); i++) {
          byte[] encodedRecipient = recipients.getPdfObject(i).getBytes();
          md.update(encodedRecipient);
        }
        if ((cryptoMode & PdfWriter.DO_NOT_ENCRYPT_METADATA) != 0)
          md.update(new byte[] { (byte) 255, (byte) 255, (byte) 255, (byte) 255 });
        encryptionKey = md.digest();
      } catch (Exception f) {
        throw new ExceptionConverter(f);
      }
    }

    decrypt = new PdfEncryption();
    decrypt.setCryptoMode(cryptoMode, lengthValue);

    if (filter.equals(PdfName.STANDARD)) {
      if (rValue < 6) {
        // check by owner password
        decrypt.setupByOwnerPassword(documentID, password, uValue, oValue, pValue);
        if (!equalsArray(uValue, decrypt.userKey,
            (rValue == 3 || rValue == 4) ? 16 : 32)) {
          // check by user password
          decrypt.setupByUserPassword(documentID, password, oValue, pValue);
          if (!equalsArray(uValue, decrypt.userKey,
              (rValue == 3 || rValue == 4) ? 16 : 32)) {
            throw new BadPasswordException(
                MessageLocalization.getComposedMessage("bad.user.password"));
          }
        } else
          ownerPasswordUsed = true;
      } else {
        // implements Algorithm 2.A: Retrieving the file encryption key from an encrypted document in order to decrypt it (revision 6 and later) - ISO 32000-2 section 7.6.4.3.3
        s = enc.get(PdfName.UE).toString();
        strings.remove(enc.get(PdfName.UE));
        byte[] ueValue = com.lowagie.text.DocWriter.getISOBytes(s);
        s = enc.get(PdfName.OE).toString();
        strings.remove(enc.get(PdfName.OE));
        byte[] oeValue = com.lowagie.text.DocWriter.getISOBytes(s);
        s = enc.get(PdfName.PERMS).toString();
        strings.remove(enc.get(PdfName.PERMS));
        byte[] permsValue = com.lowagie.text.DocWriter.getISOBytes(s);

        // step b of Algorithm 2.A
        byte[] password = this.password;
        if (password == null)
          password = new byte[0];
        else if (password.length > 127)
          password = Arrays.copyOf(password, 127);

        // According to ISO 32000-2 the uValue is expected to be 48 bytes in length.
        // Actual documents from the wild tend to have the uValue filled with zeroes
        // to a 127 bytes length. As input to computeHash for owner password related
        // operations, though, we must only use the 48 bytes.
        if (uValue != null && uValue.length > 48)
            uValue = Arrays.copyOf(uValue, 48);

        try {
          // step c of Algorithm 2.A
          byte[] hashAlg2B = decrypt.hashAlg2B(password, Arrays.copyOfRange(oValue, 32, 40), uValue);
          if (equalsArray(hashAlg2B, oValue, 32)) {
            // step d of Algorithm 2.A
            decrypt.setupByOwnerPassword(documentID, password, uValue, ueValue, oValue, oeValue, pValue);
            // step f of Algorithm 2.A
            if (decrypt.decryptAndCheckPerms(permsValue))
              ownerPasswordUsed = true;
          }

          if (!ownerPasswordUsed) {
            // analog of step c of Algorithm 2.A for user password
            hashAlg2B = decrypt.hashAlg2B(password, Arrays.copyOfRange(uValue, 32, 40), null);
            if (!equalsArray(hashAlg2B, uValue, 32)) 
              throw new BadPasswordException(MessageLocalization.getComposedMessage("bad.user.password"));
            // step e of Algorithm 2.A
            decrypt.setupByUserPassword(documentID, password, uValue, ueValue, oValue, oeValue, pValue);
            // step f of Algorithm 2.A
            if (!decrypt.decryptAndCheckPerms(permsValue))
              throw new BadPasswordException(MessageLocalization.getComposedMessage("bad.user.password"));
          }
          pValue = decrypt.permissions;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException(e);
        }
      }
    } else if (filter.equals(PdfName.PUBSEC)) {
      decrypt.setupByEncryptionKey(encryptionKey, lengthValue);
      ownerPasswordUsed = true;
    }

    for (Object string : strings) {
      PdfString str = (PdfString) string;
      str.decrypt(this);
    }

    if (encDic.isIndirect()) {
      cryptoRef = (PRIndirectReference) encDic;
      xrefObj.set(cryptoRef.getNumber(), null);
    }
    encryptionError = false;
  }

  /**
   * @param obj an object of {@link PdfObject}
   * @return a PdfObject
   */
  public static PdfObject getPdfObjectRelease(PdfObject obj) {
    PdfObject obj2 = getPdfObject(obj);
    releaseLastXrefPartial(obj);
    return obj2;
  }

  /**
   * If given object is instance of {@link PdfNull}, then {@code null} is returned. The provided object otherwise.
   *
   * @param obj object to convert
   * @return provided object or null
   */
  public static PdfObject convertPdfNull(PdfObject obj) {
      if (obj == null || obj instanceof PdfNull) {
          return null;
      }
      return obj;
  }

  /**
   * Returns {@link #getPdfObjectRelease(PdfObject)} with applied {@link #convertPdfNull(PdfObject)}.
   */
  public static PdfObject getPdfObjectReleaseNullConverting(PdfObject obj) {
      return convertPdfNull(getPdfObjectRelease(obj));
  }

  /**
   * Reads a <CODE>PdfObject</CODE> resolving an indirect reference if needed.
   * 
   * @param obj
   *          the <CODE>PdfObject</CODE> to read
   * @return the resolved <CODE>PdfObject</CODE>
   */
  public static PdfObject getPdfObject(PdfObject obj) {
    if (obj == null)
      return null;
    if (!obj.isIndirect())
      return obj;
    try {
      PRIndirectReference ref = (PRIndirectReference) obj;
      int idx = ref.getNumber();
      boolean appendable = ref.getReader().appendable;
      obj = ref.getReader().getPdfObject(idx);
      if (obj == null) {
        return null;
      } else {
        if (appendable) {
          switch (obj.type()) {
          case PdfObject.NULL:
            obj = new PdfNull();
            break;
          case PdfObject.BOOLEAN:
            obj = new PdfBoolean(((PdfBoolean) obj).booleanValue());
            break;
          case PdfObject.NAME:
            obj = new PdfName(obj.getBytes());
            break;
          }
          obj.setIndRef(ref);
        }
        return obj;
      }
    } catch (Exception e) {
      throw new ExceptionConverter(e);
    }
  }

  /**
   * Reads a <CODE>PdfObject</CODE> resolving an indirect reference if needed.
   * If the reader was opened in partial mode the object will be released to
   * save memory.
   *
   * @param obj the <CODE>PdfObject</CODE> to read
   * @param parent parent object
   * @return a PdfObject
   */
  public static PdfObject getPdfObjectRelease(PdfObject obj, PdfObject parent) {
    PdfObject obj2 = getPdfObject(obj, parent);
    releaseLastXrefPartial(obj);
    return obj2;
  }

  /**
   * @param obj the <CODE>PdfObject</CODE> to read
   * @param parent parent object
   * @return a PdfObject
   */
  public static PdfObject getPdfObject(PdfObject obj, PdfObject parent) {
    if (obj == null)
      return null;
    if (!obj.isIndirect()) {
      PRIndirectReference ref;
      if (parent != null && (ref = parent.getIndRef()) != null
          && ref.getReader().isAppendable()) {
        switch (obj.type()) {
        case PdfObject.NULL:
          obj = new PdfNull();
          break;
        case PdfObject.BOOLEAN:
          obj = new PdfBoolean(((PdfBoolean) obj).booleanValue());
          break;
        case PdfObject.NAME:
          obj = new PdfName(obj.getBytes());
          break;
        }
        obj.setIndRef(ref);
      }
      return obj;
    }
    return getPdfObject(obj);
  }

  /**
   * Returns {@link #getPdfObject(PdfObject, PdfObject)} with applied {@link #convertPdfNull(PdfObject)}.
   */
  public static PdfObject getPdfObjectNullConverting(PdfObject obj, PdfObject parent) {
      return convertPdfNull(getPdfObject(obj, parent));
  }

  /**
   * @param idx index
   * @return a PdfObject
   */
  public PdfObject getPdfObjectRelease(int idx) {
    PdfObject obj = getPdfObject(idx);
    releaseLastXrefPartial();
    return obj;
  }

  /**
   * @param idx index
   * @return aPdfObject
   */
  public PdfObject getPdfObject(int idx) {
    try {
      lastXrefPartial = -1;
      if (idx < 0 || idx >= xrefObj.size())
        return null;
      PdfObject obj = xrefObj.get(idx);
      if (!partial || obj != null)
        return obj;
      if (idx * 2 >= xref.length)
        return null;
      obj = readSingleObject(idx);
      lastXrefPartial = -1;
      if (obj != null)
        lastXrefPartial = idx;
      return obj;
    } catch (Exception e) {
      throw new ExceptionConverter(e);
    }
  }

  /**
     *
     */
  public void resetLastXrefPartial() {
    lastXrefPartial = -1;
  }

  /**
     *
     */
  public void releaseLastXrefPartial() {
    if (partial && lastXrefPartial != -1) {
      xrefObj.set(lastXrefPartial, null);
      lastXrefPartial = -1;
    }
  }

  /**
   * @param obj an object of {@link PdfObject}
   */
  public static void releaseLastXrefPartial(PdfObject obj) {
    if (obj == null)
      return;
    if (!obj.isIndirect())
      return;
    if (!(obj instanceof PRIndirectReference))
      return;

    PRIndirectReference ref = (PRIndirectReference) obj;
    PdfReader reader = ref.getReader();
    if (reader.partial && reader.lastXrefPartial != -1
        && reader.lastXrefPartial == ref.getNumber()) {
      reader.xrefObj.set(reader.lastXrefPartial, null);
    }
    reader.lastXrefPartial = -1;
  }

  private void setXrefPartialObject(int idx, PdfObject obj) {
    if (!partial || idx < 0)
      return;
    xrefObj.set(idx, obj);
  }

  /**
   * @param obj an object of {@link PdfObject}
   * @return an indirect reference
   */
  public PRIndirectReference addPdfObject(PdfObject obj) {
    xrefObj.add(obj);
    return new PRIndirectReference(this, xrefObj.size() - 1);
  }

  protected void readPages() throws IOException {
    catalog = trailer.getAsDict(PdfName.ROOT);
    rootPages = catalog.getAsDict(PdfName.PAGES);
    pageRefs = new PageRefs(this);
  }

  protected void readDocObjPartial() throws IOException {
    xrefObj = new ArrayList<>(xref.length / 2);
    xrefObj.addAll(Collections.nCopies(xref.length / 2, null));
    readDecryptedDocObj();
    if (objStmToOffset != null) {
      int[] keys = objStmToOffset.getKeys();
      for (int n : keys) {
        objStmToOffset.put(n, xref[n * 2]);
        xref[n * 2] = -1;
      }
    }
  }

  protected PdfObject readSingleObject(int k) throws IOException {
    strings.clear();
    int k2 = k * 2;
    int pos = xref[k2];
    if (pos < 0)
      return null;
    if (xref[k2 + 1] > 0)
      pos = objStmToOffset.get(xref[k2 + 1]);
    if (pos == 0)
      return null;
    tokens.seek(pos);
    tokens.nextValidToken();
    if (tokens.getTokenType() != PRTokeniser.TK_NUMBER)
      tokens.throwError(MessageLocalization
          .getComposedMessage("invalid.object.number"));
    objNum = tokens.intValue();
    tokens.nextValidToken();
    if (tokens.getTokenType() != PRTokeniser.TK_NUMBER)
      tokens.throwError(MessageLocalization
          .getComposedMessage("invalid.generation.number"));
    objGen = tokens.intValue();
    tokens.nextValidToken();
    if (!tokens.getStringValue().equals("obj"))
      tokens.throwError(MessageLocalization
          .getComposedMessage("token.obj.expected"));
    PdfObject obj;
    try {
      obj = readPRObject();
      for (PdfObject string : strings) {
        PdfString str = (PdfString) string;
        str.decrypt(this);
      }
      if (obj.isStream()) {
        checkPRStreamLength((PRStream) obj);
      }
    } catch (Exception e) {
      obj = null;
    }
    if (xref[k2 + 1] > 0) {
      obj = readOneObjStm((PRStream) obj, xref[k2]);
    }
    xrefObj.set(k, obj);
    return obj;
  }

  protected PdfObject readOneObjStm(PRStream stream, int idx)
      throws IOException {
    int first = stream.getAsNumber(PdfName.FIRST).intValue();
    byte[] b = getStreamBytes(stream, tokens.getFile());
    PRTokeniser saveTokens = tokens;
    tokens = new PRTokeniser(b);
    try {
      int address = 0;
      boolean ok = true;
      ++idx;
      for (int k = 0; k < idx; ++k) {
        ok = tokens.nextToken();
        if (!ok)
          break;
        if (tokens.getTokenType() != PRTokeniser.TK_NUMBER) {
          ok = false;
          break;
        }
        ok = tokens.nextToken();
        if (!ok)
          break;
        if (tokens.getTokenType() != PRTokeniser.TK_NUMBER) {
          ok = false;
          break;
        }
        address = tokens.intValue() + first;
      }
      if (!ok)
        throw new InvalidPdfException(
            MessageLocalization.getComposedMessage("error.reading.objstm"));
      tokens.seek(address);
      return readPRObject();
    } finally {
      tokens = saveTokens;
    }
  }

  /**
   * @return the percentage of the cross reference table that has been read
   */
  public double dumpPerc() {
    int total = 0;
    for (PdfObject aXrefObj : xrefObj) {
      if (aXrefObj != null)
        ++total;
    }
    return (total * 100.0 / xrefObj.size());
  }

  protected void readDocObj() throws IOException {
    List<PdfObject> streams = new ArrayList<>();
    xrefObj = new ArrayList<>(xref.length / 2);
    xrefObj.addAll(Collections.nCopies(xref.length / 2, null));
    for (int k = 2; k < xref.length; k += 2) {
      int pos = xref[k];
      if (pos <= 0 || ((xref.length > k + 1) && (xref[k + 1] > 0))) {
        continue;
      }
      tokens.seek(pos);
      tokens.nextValidToken();
      if (tokens.getTokenType() != PRTokeniser.TK_NUMBER)
        tokens.throwError(MessageLocalization
            .getComposedMessage("invalid.object.number"));
      objNum = tokens.intValue();
      tokens.nextValidToken();
      if (tokens.getTokenType() != PRTokeniser.TK_NUMBER)
        tokens.throwError(MessageLocalization
            .getComposedMessage("invalid.generation.number"));
      objGen = tokens.intValue();
      tokens.nextValidToken();
      if (!tokens.getStringValue().equals("obj"))
        tokens.throwError(MessageLocalization
            .getComposedMessage("token.obj.expected"));
      PdfObject obj;
      try {
        obj = readPRObject();
        if (obj.isStream()) {
          streams.add(obj);
        }
      } catch (Exception e) {
        obj = null;
      }
      xrefObj.set(k / 2, obj);
    }
    for (PdfObject stream : streams) {
      checkPRStreamLength((PRStream) stream);
    }
    readDecryptedDocObj();
    if (objStmMark != null) {
      for (Object o : objStmMark.entrySet()) {
        Map.Entry entry = (Map.Entry) o;
        int n = (Integer) entry.getKey();
        IntHashtable h = (IntHashtable) entry.getValue();
        readObjStm((PRStream) xrefObj.get(n), h);
        xrefObj.set(n, null);
      }
      objStmMark = null;
    }
    xref = null;
  }

  private void checkPRStreamLength(PRStream stream) throws IOException {
    int fileLength = tokens.length();
    int start = stream.getOffset();
    boolean calc = false;
    int streamLength = 0;
    PdfObject obj = getPdfObjectRelease(stream.get(PdfName.LENGTH));
    if (obj != null && obj.type() == PdfObject.NUMBER) {
      streamLength = ((PdfNumber) obj).intValue();
      if (streamLength + start > fileLength - 20)
        calc = true;
      else {
        tokens.seek(start + streamLength);
        String line = tokens.readString(20);
        if (!line.startsWith("\nendstream")
            && !line.startsWith("\r\nendstream")
            && !line.startsWith("\rendstream") && !line.startsWith("endstream"))
          calc = true;
      }
    } else
      calc = true;
    if (calc) {
      byte[] tline = new byte[16];
      tokens.seek(start);
      while (true) {
        int pos = tokens.getFilePointer();
        if (!tokens.readLineSegment(tline))
          break;
        if (equalsn(tline, endstream)) {
          streamLength = pos - start;
          break;
        }
        if (equalsn(tline, endobj)) {
          tokens.seek(pos - 16);
          String s = tokens.readString(16);
          int index = s.indexOf("endstream");
          if (index >= 0)
            pos = pos - 16 + index;
          streamLength = pos - start;
          break;
        }
      }
    }
    stream.setLength(streamLength);
  }

  protected void readObjStm(PRStream stream, IntHashtable map)
      throws IOException {
    int first = stream.getAsNumber(PdfName.FIRST).intValue();
    int n = stream.getAsNumber(PdfName.N).intValue();
    byte[] b = getStreamBytes(stream, tokens.getFile());
    PRTokeniser saveTokens = tokens;
    tokens = new PRTokeniser(b);
    try {
      int[] address = new int[n];
      int[] objNumber = new int[n];
      boolean ok = true;
      for (int k = 0; k < n; ++k) {
        ok = tokens.nextToken();
        if (!ok)
          break;
        if (tokens.getTokenType() != PRTokeniser.TK_NUMBER) {
          ok = false;
          break;
        }
        objNumber[k] = tokens.intValue();
        ok = tokens.nextToken();
        if (!ok)
          break;
        if (tokens.getTokenType() != PRTokeniser.TK_NUMBER) {
          ok = false;
          break;
        }
        address[k] = tokens.intValue() + first;
      }
      if (!ok)
        throw new InvalidPdfException(
            MessageLocalization.getComposedMessage("error.reading.objstm"));
      for (int k = 0; k < n; ++k) {
        if (map.containsKey(k)) {
          tokens.seek(address[k]);
          PdfObject obj = readPRObject();
          xrefObj.set(objNumber[k], obj);
        }
      }
    } finally {
      tokens = saveTokens;
    }
  }

  /**
   * Eliminates the reference to the object freeing the memory used by it and
   * clearing the xref entry.
   * 
   * @param obj
   *          the object. If it's an indirect reference it will be eliminated
   * @return the object or the already erased dereferenced object
   */
  public static PdfObject killIndirect(PdfObject obj) {
    if (obj == null || obj.isNull())
      return null;
    PdfObject ret = getPdfObjectRelease(obj);
    if (obj.isIndirect()) {
      PRIndirectReference ref = (PRIndirectReference) obj;
      PdfReader reader = ref.getReader();
      int n = ref.getNumber();
      reader.xrefObj.set(n, null);
      if (reader.partial)
        reader.xref[n * 2] = -1;
    }
    return ret;
  }

  private void ensureXrefSize(int size) {
    if (size == 0)
      return;
    if (xref == null)
      xref = new int[size];
    else {
      if (xref.length < size) {
        int[] xref2 = new int[size];
        System.arraycopy(xref, 0, xref2, 0, xref.length);
        xref = xref2;
      }
    }
  }

  protected void readXref() throws IOException {
    hybridXref = false;
    newXrefType = false;
    tokens.seek(tokens.getStartxref());
    tokens.nextToken();
    if (!tokens.getStringValue().equals("startxref"))
      throw new InvalidPdfException(
          MessageLocalization.getComposedMessage("startxref.not.found"));
    tokens.nextToken();
    if (tokens.getTokenType() != PRTokeniser.TK_NUMBER)
      throw new InvalidPdfException(
          MessageLocalization
              .getComposedMessage("startxref.is.not.followed.by.a.number"));
    int startxref = tokens.intValue();
    lastXref = startxref;
    eofPos = tokens.getFilePointer();
    try {
      if (readXRefStream(startxref)) {
        newXrefType = true;
        return;
      }
    } catch (Exception ignored) {
    }
    xref = null;
    tokens.seek(startxref);
    trailer = readXrefSection();
    PdfDictionary trailer2 = trailer;
    while (true) {
      PdfNumber prev = (PdfNumber) trailer2.get(PdfName.PREV);
      if (prev == null)
        break;
      if (prev.intValue() == startxref)
        throw new InvalidPdfException(
            MessageLocalization
                .getComposedMessage("xref.infinite.loop"));
      tokens.seek(prev.intValue());
      trailer2 = readXrefSection();
    }
  }

  protected PdfDictionary readXrefSection() throws IOException {
    tokens.nextValidToken();
    if (!tokens.getStringValue().equals("xref"))
      tokens.throwError(MessageLocalization
          .getComposedMessage("xref.subsection.not.found"));
    int start;
    int end;
    int pos;
    int gen;
    while (true) {
      tokens.nextValidToken();
      if (tokens.getStringValue().equals("trailer"))
        break;
      if (tokens.getTokenType() != PRTokeniser.TK_NUMBER)
        tokens
            .throwError(MessageLocalization
                .getComposedMessage("object.number.of.the.first.object.in.this.xref.subsection.not.found"));
      start = tokens.intValue();
      tokens.nextValidToken();
      if (tokens.getTokenType() != PRTokeniser.TK_NUMBER)
        tokens
            .throwError(MessageLocalization
                .getComposedMessage("number.of.entries.in.this.xref.subsection.not.found"));
      end = tokens.intValue() + start;
      if (start == 1) { // fix incorrect start number
        int back = tokens.getFilePointer();
        tokens.nextValidToken();
        pos = tokens.intValue();
        tokens.nextValidToken();
        gen = tokens.intValue();
        if (pos == 0 && gen == PdfWriter.GENERATION_MAX) {
          --start;
          --end;
        }
        tokens.seek(back);
      }
      ensureXrefSize(end * 2);
      for (int k = start; k < end; ++k) {
        tokens.nextValidToken();
        pos = tokens.intValue();
        tokens.nextValidToken();
        tokens.nextValidToken();
        int p = k * 2;
        if (tokens.getStringValue().equals("n")) {
          if (xref[p] == 0 && xref[p + 1] == 0) {
            // if (pos == 0)
            // tokens.throwError(MessageLocalization.getComposedMessage("file.position.0.cross.reference.entry.in.this.xref.subsection"));
            xref[p] = pos;
          }
        } else if (tokens.getStringValue().equals("f")) {
          if (xref[p] == 0 && xref[p + 1] == 0)
            xref[p] = -1;
        } else
          tokens
              .throwError(MessageLocalization
                  .getComposedMessage("invalid.cross.reference.entry.in.this.xref.subsection"));
      }
    }
    PdfDictionary trailer = (PdfDictionary) readPRObject();
    PdfNumber xrefSize = (PdfNumber) trailer.get(PdfName.SIZE);
    ensureXrefSize(xrefSize.intValue() * 2);
    PdfObject xrs = trailer.get(PdfName.XREFSTM);
    if (xrs != null && xrs.isNumber()) {
      int loc = ((PdfNumber) xrs).intValue();
      try {
        readXRefStream(loc);
        newXrefType = true;
        hybridXref = true;
      } catch (IOException e) {
        xref = null;
        throw e;
      }
    }
    return trailer;
  }

  protected boolean readXRefStream(int ptr) throws IOException {
    tokens.seek(ptr);
    int thisStream;
    if (!tokens.nextToken())
      return false;
    if (tokens.getTokenType() != PRTokeniser.TK_NUMBER)
      return false;
    thisStream = tokens.intValue();
    if (!tokens.nextToken() || tokens.getTokenType() != PRTokeniser.TK_NUMBER)
      return false;
    if (!tokens.nextToken() || !tokens.getStringValue().equals("obj"))
      return false;
    PdfObject object = readPRObject();
    PRStream stm;
    if (object.isStream()) {
      stm = (PRStream) object;
      if (!PdfName.XREF.equals(stm.get(PdfName.TYPE)))
        return false;
    } else
      return false;
    if (trailer == null) {
      trailer = new PdfDictionary();
      trailer.putAll(stm);
    }
    stm.setLength(((PdfNumber) stm.get(PdfName.LENGTH)).intValue());
    int size = ((PdfNumber) stm.get(PdfName.SIZE)).intValue();
    PdfArray index;
    PdfObject obj = stm.get(PdfName.INDEX);
    if (obj == null) {
      index = new PdfArray();
      index.add(new int[] { 0, size });
    } else
      index = (PdfArray) obj;
    PdfArray w = (PdfArray) stm.get(PdfName.W);
    int prev = -1;
    obj = stm.get(PdfName.PREV);
    if (obj != null)
      prev = ((PdfNumber) obj).intValue();
    // Each xref pair is a position
    // type 0 -> -1, 0
    // type 1 -> offset, 0
    // type 2 -> index, obj num
    ensureXrefSize(size * 2);
    if (objStmMark == null && !partial)
      objStmMark = new HashMap<>();
    if (objStmToOffset == null && partial)
      objStmToOffset = new IntHashtable();
    byte[] b = getStreamBytes(stm, tokens.getFile());
    int bptr = 0;
    int[] wc = new int[3];
    for (int k = 0; k < 3; ++k)
      wc[k] = w.getAsNumber(k).intValue();
    for (int idx = 0; idx < index.size(); idx += 2) {
      int start = index.getAsNumber(idx).intValue();
      int length = index.getAsNumber(idx + 1).intValue();
      ensureXrefSize((start + length) * 2);
      while (length-- > 0) {
        int type = 1;
        if (wc[0] > 0) {
          type = 0;
          for (int k = 0; k < wc[0]; ++k)
            type = (type << 8) + (b[bptr++] & 0xff);
        }
        int field2 = 0;
        for (int k = 0; k < wc[1]; ++k)
          field2 = (field2 << 8) + (b[bptr++] & 0xff);
        int field3 = 0;
        for (int k = 0; k < wc[2]; ++k)
          field3 = (field3 << 8) + (b[bptr++] & 0xff);
        int base = start * 2;
        if (xref[base] == 0 && xref[base + 1] == 0) {
          switch (type) {
          case 0:
            xref[base] = -1;
            break;
          case 1:
            xref[base] = field2;
            break;
          case 2:
            xref[base] = field3;
            xref[base + 1] = field2;
            if (partial) {
              objStmToOffset.put(field2, 0);
            } else {
              Integer on = field2;
              IntHashtable seq = objStmMark.get(on);
              if (seq == null) {
                seq = new IntHashtable();
                seq.put(field3, 1);
                objStmMark.put(on, seq);
              } else
                seq.put(field3, 1);
            }
            break;
          }
        }
        ++start;
      }
    }
    thisStream *= 2;
    if (thisStream + 1 < xref.length && xref[thisStream + 1] == 0 && xref[thisStream] == 0)
      xref[thisStream] = -1;

    if (prev == -1)
      return true;
    return readXRefStream(prev);
  }

  protected void rebuildXref() throws IOException {
    hybridXref = false;
    newXrefType = false;
    tokens.seek(0);
    int[][] xr = new int[1024][];
    int top = 0;
    trailer = null;
    byte[] line = new byte[64];
    for (;;) {
      int pos = tokens.getFilePointer();
      if (!tokens.readLineSegment(line))
        break;
      if (line[0] == 't') {
        if (!PdfEncodings.convertToString(line, null).startsWith("trailer"))
          continue;
        tokens.seek(pos);
        tokens.nextToken();
        pos = tokens.getFilePointer();
        try {
          PdfDictionary dic = (PdfDictionary) readPRObject();
          if (dic.get(PdfName.ROOT) != null)
            trailer = dic;
          else
            tokens.seek(pos);
        } catch (Exception e) {
          tokens.seek(pos);
        }
      } else if (line[0] >= '0' && line[0] <= '9') {
        int[] obj = PRTokeniser.checkObjectStart(line);
        if (obj == null)
          continue;
        int num = obj[0];
        int gen = obj[1];
        if (num >= xr.length) {
          int newLength = num * 2;
          int[][] xr2 = new int[newLength][];
          System.arraycopy(xr, 0, xr2, 0, top);
          xr = xr2;
        }
        if (num >= top)
          top = num + 1;
        if (xr[num] == null || gen >= xr[num][1]) {
          obj[0] = pos;
          xr[num] = obj;
        }
      }
    }
    xref = new int[top * 2];
    for (int k = 0; k < top; ++k) {
      int[] obj = xr[k];
      if (obj != null)
        xref[k * 2] = obj[0];
    }
  }

  protected PdfDictionary readDictionary() throws IOException {
    PdfDictionary dic = new PdfDictionary();
    while (true) {
      tokens.nextValidToken();
      if (tokens.getTokenType() == PRTokeniser.TK_END_DIC)
        break;
      if (tokens.getTokenType() != PRTokeniser.TK_NAME)
        tokens.throwError(MessageLocalization
            .getComposedMessage("dictionary.key.is.not.a.name"));
      PdfName name = new PdfName(tokens.getStringValue(), false);
      PdfObject obj = readPRObject();
      int type = obj.type();
      if (-type == PRTokeniser.TK_END_DIC)
        tokens.throwError(MessageLocalization
            .getComposedMessage("unexpected.gt.gt"));
      if (-type == PRTokeniser.TK_END_ARRAY)
        tokens.throwError(MessageLocalization
            .getComposedMessage("unexpected.close.bracket"));
      dic.put(name, obj);
    }
    return dic;
  }

  protected PdfArray readArray() throws IOException {
    PdfArray array = new PdfArray();
    while (true) {
      PdfObject obj = readPRObject();
      int type = obj.type();
      if (-type == PRTokeniser.TK_END_ARRAY)
        break;
      if (-type == PRTokeniser.TK_END_DIC)
        tokens.throwError(MessageLocalization
            .getComposedMessage("unexpected.gt.gt"));
      array.add(obj);
    }
    return array;
  }

  // Track how deeply nested the current object is, so
  // we know when to return an individual null or boolean, or
  // reuse one of the static ones.
  private int readDepth = 0;

  protected PdfObject readPRObject() throws IOException {
    tokens.nextValidToken();
    int type = tokens.getTokenType();
    switch (type) {
    case PRTokeniser.TK_START_DIC: {
      ++readDepth;
      PdfDictionary dic = readDictionary();
      --readDepth;
      int pos = tokens.getFilePointer();
      // be careful in the trailer. May not be a "next" token.
      boolean hasNext;
      do {
        hasNext = tokens.nextToken();
      } while (hasNext && tokens.getTokenType() == PRTokeniser.TK_COMMENT);

      if (hasNext && tokens.getStringValue().equals("stream")) {
        // skip whitespaces
        int ch;
        do {
          ch = tokens.read();
        } while (ch == 32 || ch == 9 || ch == 0 || ch == 12);
        if (ch != '\n')
          ch = tokens.read();
        if (ch != '\n')
          tokens.backOnePosition(ch);
        PRStream stream = new PRStream(this, tokens.getFilePointer());
        stream.putAll(dic);
        // crypto handling
        stream.setObjNum(objNum, objGen);

        return stream;
      } else {
        tokens.seek(pos);
        return dic;
      }
    }
    case PRTokeniser.TK_START_ARRAY: {
      ++readDepth;
      PdfArray arr = readArray();
      --readDepth;
      return arr;
    }
    case PRTokeniser.TK_NUMBER:
      return new PdfNumber(tokens.getStringValue());
    case PRTokeniser.TK_STRING:
      PdfString str = new PdfString(tokens.getStringValue(), null)
          .setHexWriting(tokens.isHexString());
      // crypto handling
      str.setObjNum(objNum, objGen);
      if (strings != null)
        strings.add(str);

      return str;
    case PRTokeniser.TK_NAME: {
      PdfName cachedName = PdfName.staticNames.get(tokens
          .getStringValue());
      if (readDepth > 0 && cachedName != null) {
        return cachedName;
      } else {
        // an indirect name (how odd...), or a non-standard one
        return new PdfName(tokens.getStringValue(), false);
      }
    }
    case PRTokeniser.TK_REF:
      int num = tokens.getReference();
      PRIndirectReference ref = new PRIndirectReference(this, num,
          tokens.getGeneration());
      return ref;
    case PRTokeniser.TK_ENDOFFILE:
      throw new IOException(
          MessageLocalization.getComposedMessage("unexpected.end.of.file"));
    default:
      String sv = tokens.getStringValue();
      if ("null".equals(sv)) {
        if (readDepth == 0) {
          return new PdfNull();
        } // else
        return PdfNull.PDFNULL;
      } else if ("true".equals(sv)) {
        if (readDepth == 0) {
          return new PdfBoolean(true);
        } // else
        return PdfBoolean.PDFTRUE;
      } else if ("false".equals(sv)) {
        if (readDepth == 0) {
          return new PdfBoolean(false);
        } // else
        return PdfBoolean.PDFFALSE;
      }
      return new PdfLiteral(-type, tokens.getStringValue());
    }
  }

  /**
   * Decodes a stream that has the FlateDecode filter.
   * 
   * @param in
   *          the input data
   * @return the decoded data
   */
  public static byte[] FlateDecode(byte[] in) {
    byte[] b = FlateDecode(in, true);
    if (b == null)
      return FlateDecode(in, false);
    return b;
  }

  /**
   * @param in the input data
   * @param dicPar an object of {@link PdfObject}
   * @return a byte array
   */
  public static byte[] decodePredictor(byte[] in, PdfObject dicPar) {
    if (dicPar == null || !dicPar.isDictionary())
      return in;
    PdfDictionary dic = (PdfDictionary) dicPar;
    PdfObject obj = getPdfObject(dic.get(PdfName.PREDICTOR));
    if (obj == null || !obj.isNumber())
      return in;
    int predictor = ((PdfNumber) obj).intValue();
    if (predictor < 10)
      return in;
    int width = 1;
    obj = getPdfObject(dic.get(PdfName.COLUMNS));
    if (obj != null && obj.isNumber())
      width = ((PdfNumber) obj).intValue();
    int colors = 1;
    obj = getPdfObject(dic.get(PdfName.COLORS));
    if (obj != null && obj.isNumber())
      colors = ((PdfNumber) obj).intValue();
    int bpc = 8;
    obj = getPdfObject(dic.get(PdfName.BITSPERCOMPONENT));
    if (obj != null && obj.isNumber())
      bpc = ((PdfNumber) obj).intValue();
    DataInputStream dataStream = new DataInputStream(new ByteArrayInputStream(
        in));
    ByteArrayOutputStream fout = new ByteArrayOutputStream(in.length);
    int bytesPerPixel = colors * bpc / 8;
    int bytesPerRow = (colors * width * bpc + 7) / 8;
    byte[] curr = new byte[bytesPerRow];
    byte[] prior = new byte[bytesPerRow];

    // Decode the (sub)image row-by-row
    while (true) {
      // Read the filter type byte and a row of data
      int filter;
      try {
        filter = dataStream.read();
        if (filter < 0) {
          return fout.toByteArray();
        }
        dataStream.readFully(curr, 0, bytesPerRow);
      } catch (Exception e) {
        return fout.toByteArray();
      }

      switch (filter) {
      case 0: // PNG_FILTER_NONE
        break;
      case 1: // PNG_FILTER_SUB
        for (int i = bytesPerPixel; i < bytesPerRow; i++) {
          curr[i] += curr[i - bytesPerPixel];
        }
        break;
      case 2: // PNG_FILTER_UP
        for (int i = 0; i < bytesPerRow; i++) {
          curr[i] += prior[i];
        }
        break;
      case 3: // PNG_FILTER_AVERAGE
        for (int i = 0; i < bytesPerPixel; i++) {
          curr[i] += prior[i] / (byte) 2;
        }
        for (int i = bytesPerPixel; i < bytesPerRow; i++) {
          curr[i] += ((curr[i - bytesPerPixel] & 0xff) + (prior[i] & 0xff)) / (byte) 2;
        }
        break;
      case 4: // PNG_FILTER_PAETH
        for (int i = 0; i < bytesPerPixel; i++) {
          curr[i] += prior[i];
        }

        for (int i = bytesPerPixel; i < bytesPerRow; i++) {
          int a = curr[i - bytesPerPixel] & 0xff;
          int b = prior[i] & 0xff;
          int c = prior[i - bytesPerPixel] & 0xff;

          int p = a + b - c;
          int pa = Math.abs(p - a);
          int pb = Math.abs(p - b);
          int pc = Math.abs(p - c);

          int ret;

          if ((pa <= pb) && (pa <= pc)) {
            ret = a;
          } else if (pb <= pc) {
            ret = b;
          } else {
            ret = c;
          }
          curr[i] += (byte) (ret);
        }
        break;
      default:
        // Error -- unknown filter type
        throw new RuntimeException(
            MessageLocalization.getComposedMessage("png.filter.unknown"));
      }
      try {
        fout.write(curr);
      } catch (IOException ioe) {
        // Never happens
      }

      // Swap curr and prior
      byte[] tmp = prior;
      prior = curr;
      curr = tmp;
    }
  }

  /**
   * A helper to FlateDecode.
   * 
   * @param in
   *          the input data
   * @param strict
   *          <CODE>true</CODE> to read a correct stream. <CODE>false</CODE> to
   *          try to read a corrupted stream
   * @return the decoded data
   */
  public static byte[] FlateDecode(byte[] in, boolean strict) {
    ByteArrayInputStream stream = new ByteArrayInputStream(in);
    InflaterInputStream zip = new InflaterInputStream(stream);
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    byte[] b = new byte[strict ? 4092 : 1];
    try {
      int n;
      while ((n = zip.read(b)) >= 0) {
        out.write(b, 0, n);
      }
      zip.close();
      out.close();
      return out.toByteArray();
    } catch (Exception e) {
      if (strict)
        return null;
      return out.toByteArray();
    }
  }

  /**
   * Decodes a stream that has the ASCIIHexDecode filter.
   * 
   * @param in
   *          the input data
   * @return the decoded data
   */
  public static byte[] ASCIIHexDecode(byte[] in) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    boolean first = true;
    int n1 = 0;
      for (byte b : in) {
          int ch = b & 0xff;
          if (ch == '>')
              break;
          if (PRTokeniser.isWhitespace(ch))
              continue;
          int n = PRTokeniser.getHex(ch);
          if (n == -1)
              throw new RuntimeException(
                      MessageLocalization
                              .getComposedMessage("illegal.character.in.asciihexdecode"));
          if (first)
              n1 = n;
          else
              out.write((byte) ((n1 << 4) + n));
          first = !first;
      }
    if (!first)
      out.write((byte) (n1 << 4));
    return out.toByteArray();
  }

  /**
   * Decodes a stream that has the ASCII85Decode filter.
   * 
   * @param in
   *          the input data
   * @return the decoded data
   */
  public static byte[] ASCII85Decode(byte[] in) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    int state = 0;
    int[] chn = new int[5];
      for (byte b : in) {
          int ch = b & 0xff;
          if (ch == '~')
              break;
          if (PRTokeniser.isWhitespace(ch))
              continue;
          if (ch == 'z' && state == 0) {
              out.write(0);
              out.write(0);
              out.write(0);
              out.write(0);
              continue;
          }
          if (ch < '!' || ch > 'u')
              throw new RuntimeException(
                      MessageLocalization
                              .getComposedMessage("illegal.character.in.ascii85decode"));
          chn[state] = ch - '!';
          ++state;
          if (state == 5) {
              state = 0;
              int r = 0;
              for (int j = 0; j < 5; ++j)
                  r = r * 85 + chn[j];
              out.write((byte) (r >> 24));
              out.write((byte) (r >> 16));
              out.write((byte) (r >> 8));
              out.write((byte) r);
          }
      }
    int r;
    // We'll ignore the next two lines for the sake of perpetuating broken
    // PDFs
    // if (state == 1)
    // throw new
    // RuntimeException(MessageLocalization.getComposedMessage("illegal.length.in.ascii85decode"));
    if (state == 2) {
      r = chn[0] * 85 * 85 * 85 * 85 + chn[1] * 85 * 85 * 85 + 85 * 85 * 85
          + 85 * 85 + 85;
      out.write((byte) (r >> 24));
    } else if (state == 3) {
      r = chn[0] * 85 * 85 * 85 * 85 + chn[1] * 85 * 85 * 85 + chn[2] * 85 * 85
          + 85 * 85 + 85;
      out.write((byte) (r >> 24));
      out.write((byte) (r >> 16));
    } else if (state == 4) {
      r = chn[0] * 85 * 85 * 85 * 85 + chn[1] * 85 * 85 * 85 + chn[2] * 85 * 85
          + chn[3] * 85 + 85;
      out.write((byte) (r >> 24));
      out.write((byte) (r >> 16));
      out.write((byte) (r >> 8));
    }
    return out.toByteArray();
  }

  /**
   * Decodes a stream that has the LZWDecode filter.
   * 
   * @param in
   *          the input data
   * @return the decoded data
   */
  public static byte[] LZWDecode(byte[] in) {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    LZWDecoder lzw = new LZWDecoder();
    lzw.decode(in, out);
    return out.toByteArray();
  }

  /**
   * Checks if the document had errors and was rebuilt.
   * 
   * @return true if rebuilt.
   *
   */
  public boolean isRebuilt() {
    return this.rebuilt;
  }

  /**
   * Gets the dictionary that represents a page.
   * 
   * @param pageNum
   *          the page number. 1 is the first
   * @return the page dictionary
   */
  public PdfDictionary getPageN(int pageNum) {
    PdfDictionary dic = pageRefs.getPageN(pageNum);
    if (dic == null)
      return null;
    if (appendable)
      dic.setIndRef(pageRefs.getPageOrigRef(pageNum));
    return dic;
  }

  /**
   * @param pageNum page number
   * @return a Dictionary object
   */
  public PdfDictionary getPageNRelease(int pageNum) {
    PdfDictionary dic = getPageN(pageNum);
    pageRefs.releasePage(pageNum);
    return dic;
  }

  /**
   * @param pageNum page number
   */
  public void releasePage(int pageNum) {
    pageRefs.releasePage(pageNum);
  }

  /**
     *
     */
  public void resetReleasePage() {
    pageRefs.resetReleasePage();
  }

  /**
   * Gets the page reference to this page.
   * 
   * @param pageNum
   *          the page number. 1 is the first
   * @return the page reference
   */
  public PRIndirectReference getPageOrigRef(int pageNum) {
    return pageRefs.getPageOrigRef(pageNum);
  }

  /**
   * Gets the contents of the page.
   * 
   * @param pageNum
   *          the page number. 1 is the first
   * @param file
   *          the location of the PDF document
   * @throws IOException
   *           on error
   * @return the content
   */
  public byte[] getPageContent(int pageNum, RandomAccessFileOrArray file)
      throws IOException {
    PdfDictionary page = getPageNRelease(pageNum);
    if (page == null)
      return null;
    PdfObject contents = getPdfObjectRelease(page.get(PdfName.CONTENTS));
    if (contents == null)
      return new byte[0];
    ByteArrayOutputStream bout;
    if (contents.isStream()) {
      return getStreamBytes((PRStream) contents, file);
    } else if (contents.isArray()) {
      PdfArray array = (PdfArray) contents;
      bout = new ByteArrayOutputStream();
      for (int k = 0; k < array.size(); ++k) {
        PdfObject item = getPdfObjectRelease(array.getPdfObject(k));
        if (item == null || !item.isStream())
          continue;
        byte[] b = getStreamBytes((PRStream) item, file);
        bout.write(b);
        if (k != array.size() - 1)
          bout.write('\n');
      }
      return bout.toByteArray();
    } else
      return new byte[0];
  }

  /**
   * Gets the contents of the page.
   * 
   * @param pageNum
   *          the page number. 1 is the first
   * @throws IOException
   *           on error
   * @return the content
   */
  public byte[] getPageContent(int pageNum) throws IOException {
    RandomAccessFileOrArray rf = getSafeFile();
    try {
      rf.reOpen();
      return getPageContent(pageNum, rf);
    } finally {
      try {
        rf.close();
      } catch (Exception ignored) {
      }
    }
  }

  protected void killXref(PdfObject obj) {
    if (obj == null)
      return;
    if ((obj instanceof PdfIndirectReference) && !obj.isIndirect())
      return;
    switch (obj.type()) {
    case PdfObject.INDIRECT: {
      int xr = ((PRIndirectReference) obj).getNumber();
      obj = xrefObj.get(xr);
      xrefObj.set(xr, null);
      freeXref = xr;
      killXref(obj);
      break;
    }
    case PdfObject.ARRAY: {
      PdfArray t = (PdfArray) obj;
      for (int i = 0; i < t.size(); ++i)
        killXref(t.getPdfObject(i));
      break;
    }
    case PdfObject.STREAM:
    case PdfObject.DICTIONARY: {
      PdfDictionary dic = (PdfDictionary) obj;
      for (Object o : dic.getKeys()) {
        killXref(dic.get((PdfName) o));
      }
      break;
    }
    }
  }

  /**
   * Sets the contents of the page.
   * 
   * @param content
   *          the new page content
   * @param pageNum
   *          the page number. 1 is the first
   */
  public void setPageContent(int pageNum, byte[] content) {
    setPageContent(pageNum, content, PdfStream.DEFAULT_COMPRESSION);
  }

  /**
   * Sets the contents of the page.
   *
   * @param content the new page content
   * @param pageNum the page number. 1 is the first
   * @param compressionLevel compression level
   * @since 2.1.3 (the method already existed without param compressionLevel)
   */
  public void setPageContent(int pageNum, byte[] content, int compressionLevel) {
    PdfDictionary page = getPageN(pageNum);
    if (page == null)
      return;
    PdfObject contents = page.get(PdfName.CONTENTS);
    freeXref = -1;
    killXref(contents);
    if (freeXref == -1) {
      xrefObj.add(null);
      freeXref = xrefObj.size() - 1;
    }
    page.put(PdfName.CONTENTS, new PRIndirectReference(this, freeXref));
    xrefObj.set(freeXref, new PRStream(this, content, compressionLevel));
  }

  /**
   * Get the content from a stream applying the required filters.
   * 
   * @param stream
   *          the stream
   * @param file
   *          the location where the stream is
   * @throws IOException
   *           on error
   * @return the stream content
   */
  public static byte[] getStreamBytes(PRStream stream,
      RandomAccessFileOrArray file) throws IOException {
    PdfObject filter = getPdfObjectRelease(stream.get(PdfName.FILTER));
    byte[] b = getStreamBytesRaw(stream, file);
    List<PdfObject> filters = new ArrayList<>();
    filters = addFilters(filters, filter);
    List<PdfObject> dp = new ArrayList<>();
    PdfObject dpo = getPdfObjectRelease(stream.get(PdfName.DECODEPARMS));
    if (dpo == null || (!dpo.isDictionary() && !dpo.isArray()))
      dpo = getPdfObjectRelease(stream.get(PdfName.DP));
    if (dpo != null) {
      if (dpo.isDictionary())
        dp.add(dpo);
      else if (dpo.isArray())
        dp = ((PdfArray) dpo).getElements();
    }
    String name;
    for (int j = 0; j < filters.size(); ++j) {
      name = getPdfObjectRelease(filters.get(j))
          .toString();
        switch (name) {
            case "/FlateDecode":
            case "/Fl": {
                b = FlateDecode(b);
                PdfObject dicParam;
                if (j < dp.size()) {
                    dicParam = dp.get(j);
                    b = decodePredictor(b, dicParam);
                }
                break;
            }
            case "/ASCIIHexDecode":
            case "/AHx":
                b = ASCIIHexDecode(b);
                break;
            case "/ASCII85Decode":
            case "/A85":
                b = ASCII85Decode(b);
                break;
            case "/LZWDecode": {
                b = LZWDecode(b);
                PdfObject dicParam;
                if (j < dp.size()) {
                    dicParam = dp.get(j);
                    b = decodePredictor(b, dicParam);
                }
                break;
            }
            case "/Crypt":
                break;
            default:
                throw new UnsupportedPdfException(
                        MessageLocalization.getComposedMessage(
                                "the.filter.1.is.not.supported", name));
        }
    }
    return b;
  }

  /**
   * Get the content from a stream applying the required filters.
   * 
   * @param stream
   *          the stream
   * @throws IOException
   *           on error
   * @return the stream content
   */
  public static byte[] getStreamBytes(PRStream stream) throws IOException {
    RandomAccessFileOrArray rf = stream.getReader().getSafeFile();
    try {
      rf.reOpen();
      return getStreamBytes(stream, rf);
    } finally {
      try {
        rf.close();
      } catch (Exception e) {
      }
    }
  }

  /**
   * Get the content from a stream as it is without applying any filter.
   * 
   * @param stream
   *          the stream
   * @param file
   *          the location where the stream is
   * @throws IOException
   *           on error
   * @return the stream content
   */
  public static byte[] getStreamBytesRaw(PRStream stream,
      RandomAccessFileOrArray file) throws IOException {
    PdfReader reader = stream.getReader();
    byte[] b;
    if (stream.getOffset() < 0)
      b = stream.getBytes();
    else {
      b = new byte[stream.getLength()];
      file.seek(stream.getOffset());
      file.readFully(b);
      PdfEncryption decrypt = reader.getDecrypt();
      if (decrypt != null) {
        PdfObject filter = getPdfObjectRelease(stream.get(PdfName.FILTER));
        List<PdfObject> filters = new ArrayList<>();
        filters = addFilters(filters, filter);
        boolean skip = false;
        for (PdfObject filter1 : filters) {
          PdfObject obj = getPdfObjectRelease(filter1);
          if (obj != null && obj.toString().equals("/Crypt")) {
            skip = true;
            break;
          }
        }
        if (!skip) {
          decrypt.setHashKey(stream.getObjNum(), stream.getObjGen());
          b = decrypt.decryptByteArray(b);
        }
      }
    }
    return b;
  }

  private static List<PdfObject> addFilters(List<PdfObject> filters, PdfObject filter) {
    if (filter != null) {
      if (filter.isName())
        filters.add(filter);
      else if (filter.isArray())
        filters = ((PdfArray) filter).getElements();
    }
    return filters;
  }

  /**
   * Get the content from a stream as it is without applying any filter.
   * 
   * @param stream
   *          the stream
   * @throws IOException
   *           on error
   * @return the stream content
   */
  public static byte[] getStreamBytesRaw(PRStream stream) throws IOException {
    RandomAccessFileOrArray rf = stream.getReader().getSafeFile();
    try {
      rf.reOpen();
      return getStreamBytesRaw(stream, rf);
    } finally {
      try {
        rf.close();
      } catch (Exception ignored) {
      }
    }
  }

  /** Eliminates shared streams if they exist. */
  public void eliminateSharedStreams() {
    if (!sharedStreams)
      return;
    sharedStreams = false;
    if (pageRefs.size() == 1)
      return;
    List<PdfObject> newRefs = new ArrayList<>();
    List<PdfObject> newStreams = new ArrayList<>();
    IntHashtable visited = new IntHashtable();
    for (int k = 1; k <= pageRefs.size(); ++k) {
      PdfDictionary page = pageRefs.getPageN(k);
      if (page == null)
        continue;
      PdfObject contents = getPdfObject(page.get(PdfName.CONTENTS));
      if (contents == null)
        continue;
      if (contents.isStream()) {
        PRIndirectReference ref = (PRIndirectReference) page
            .get(PdfName.CONTENTS);
        if (visited.containsKey(ref.getNumber())) {
          // need to duplicate
          newRefs.add(ref);
          newStreams.add(new PRStream((PRStream) contents, null));
        } else
          visited.put(ref.getNumber(), 1);
      } else if (contents.isArray()) {
        PdfArray array = (PdfArray) contents;
        for (int j = 0; j < array.size(); ++j) {
          PRIndirectReference ref = (PRIndirectReference) array.getPdfObject(j);
          if (visited.containsKey(ref.getNumber())) {
            // need to duplicate
            newRefs.add(ref);
            newStreams.add(new PRStream((PRStream) getPdfObject(ref), null));
          } else
            visited.put(ref.getNumber(), 1);
        }
      }
    }
    if (newStreams.isEmpty())
      return;
    for (int k = 0; k < newStreams.size(); ++k) {
      xrefObj.add(newStreams.get(k));
      PRIndirectReference ref = (PRIndirectReference) newRefs.get(k);
      ref.setNumber(xrefObj.size() - 1, 0);
    }
  }

  /**
   * Checks if the document was changed.
   * 
   * @return <CODE>true</CODE> if the document was changed, <CODE>false</CODE>
   *         otherwise
   */
  public boolean isTampered() {
    return tampered;
  }

  /**
   * Sets the tampered state. A tampered PdfReader cannot be reused in
   * PdfStamper.
   * 
   * @param tampered
   *          the tampered state
   */
  public void setTampered(boolean tampered) {
    this.tampered = tampered;
    pageRefs.keepPages();
  }

  /**
   * Gets the XML metadata.
   * 
   * @throws IOException
   *           on error
   * @return the XML metadata
   */
  public byte[] getMetadata() throws IOException {
    PdfObject obj = getPdfObject(catalog.get(PdfName.METADATA));
    if (!(obj instanceof PRStream))
      return null;
    RandomAccessFileOrArray rf = getSafeFile();
    byte[] b;
    try {
      rf.reOpen();
      b = getStreamBytes((PRStream) obj, rf);
    } finally {
      try {
        rf.close();
      } catch (Exception e) {
        // empty on purpose
      }
    }
    return b;
  }

  /**
   * Gets the byte address of the last xref table.
   * 
   * @return the byte address of the last xref table
   */
  public int getLastXref() {
    return lastXref;
  }

  /**
   * Gets the number of xref objects.
   * 
   * @return the number of xref objects
   */
  public int getXrefSize() {
    return xrefObj.size();
  }

  /**
   * Gets the byte address of the %%EOF marker.
   * 
   * @return the byte address of the %%EOF marker
   */
  public int getEofPos() {
    return eofPos;
  }

  /**
   * Gets the PDF version. Only the last version char is returned. For example
   * version 1.4 is returned as '4'.
   * 
   * @return the PDF version
   */
  public char getPdfVersion() {
    return pdfVersion;
  }

  /**
   * Returns <CODE>true</CODE> if the PDF is encrypted.
   * 
   * @return <CODE>true</CODE> if the PDF is encrypted
   */
  public boolean isEncrypted() {
    return encrypted;
  }

  /**
   * Returns <CODE>true</CODE> if the owner password has been used to open the document.
   *
   * @return <CODE>true</CODE> if the owner password has been used to open the document.
   */
  public boolean isOwnerPasswordUsed() {
    return ownerPasswordUsed;
  }

  /**
   * Gets the encryption permissions. It can be used directly in
   * <CODE>PdfWriter.setEncryption()</CODE>.
   * 
   * @return the encryption permissions
   */
  public int getPermissions() {
    return pValue;
  }

  /**
   * Returns <CODE>true</CODE> if the PDF has a 128 bit key encryption.
   * 
   * @return <CODE>true</CODE> if the PDF has a 128 bit key encryption
   */
  public boolean is128Key() {
    return rValue == 3;
  }

  /**
   * Gets the trailer dictionary
   * 
   * @return the trailer dictionary
   */
  public PdfDictionary getTrailer() {
    return trailer;
  }

  PdfEncryption getDecrypt() {
    return decrypt;
  }

  private static boolean equalsn(byte[] a1, byte[] a2) {
    int length = a2.length;
    for (int k = 0; k < length; ++k) {
      if (a1[k] != a2[k])
        return false;
    }
    return true;
  }

  private static boolean existsName(PdfDictionary dic, PdfName key, PdfName value) {
    PdfObject type = getPdfObjectRelease(dic.get(key));
    if (type == null || !type.isName())
      return false;
    PdfName name = (PdfName) type;
    return name.equals(value);
  }

  static String getFontNameFromDescriptor(PdfDictionary dic) {
    return getFontName(dic, PdfName.FONTNAME);
  }

  private static String getFontName(PdfDictionary dic) {
    return getFontName(dic, PdfName.BASEFONT);
  }

  private static String getFontName(PdfDictionary dic, PdfName property) {
    if (dic == null)
      return null;
    PdfObject type = getPdfObjectRelease(dic.get(property));
    if (type == null || !type.isName())
      return null;
    return PdfName.decodeName(type.toString());
  }

  static boolean isFontSubset(String fontName) {
    return fontName != null && fontName.length() >= 8
        && fontName.charAt(6) == '+';
  }

  private static String getSubsetPrefix(PdfDictionary dic) {
    if (dic == null)
      return null;
    String s = getFontName(dic);
    if (s == null)
      return null;
    if (s.length() < 8 || s.charAt(6) != '+')
      return null;
    for (int k = 0; k < 6; ++k) {
      char c = s.charAt(k);
      if (c < 'A' || c > 'Z')
        return null;
    }
    return s;
  }

  /**
   * Finds all the font subsets and changes the prefixes to some random values.
   * 
   * @return the number of font subsets altered
   */
  public int shuffleSubsetNames() {
    int total = 0;
    for (int k = 1; k < xrefObj.size(); ++k) {
      PdfObject obj = getPdfObjectRelease(k);
      if (obj == null || !obj.isDictionary())
        continue;
      PdfDictionary dic = (PdfDictionary) obj;
      if (!existsName(dic, PdfName.TYPE, PdfName.FONT))
        continue;
      if (existsName(dic, PdfName.SUBTYPE, PdfName.TYPE1)
          || existsName(dic, PdfName.SUBTYPE, PdfName.MMTYPE1)
          || existsName(dic, PdfName.SUBTYPE, PdfName.TRUETYPE)) {
        String s = getSubsetPrefix(dic);
        if (s == null)
          continue;
        String ns = createRandomSubsetPrefix() + s.substring(7);
        PdfName newName = new PdfName(ns);
        dic.put(PdfName.BASEFONT, newName);
        setXrefPartialObject(k, dic);
        ++total;
        PdfDictionary fd = dic.getAsDict(PdfName.FONTDESCRIPTOR);
        if (fd == null)
          continue;
        fd.put(PdfName.FONTNAME, newName);
      } else if (existsName(dic, PdfName.SUBTYPE, PdfName.TYPE0)) {
        String s = getSubsetPrefix(dic);
        PdfArray arr = dic.getAsArray(PdfName.DESCENDANTFONTS);
        if (arr == null)
          continue;
        if (arr.isEmpty())
          continue;
        PdfDictionary desc = arr.getAsDict(0);
        String sde = getSubsetPrefix(desc);
        if (sde == null)
          continue;
        String ns = createRandomSubsetPrefix();
        if (s != null)
          dic.put(PdfName.BASEFONT, new PdfName(ns + s.substring(7)));
        setXrefPartialObject(k, dic);
        PdfName newName = new PdfName(ns + sde.substring(7));
        desc.put(PdfName.BASEFONT, newName);
        ++total;
        PdfDictionary fd = desc.getAsDict(PdfName.FONTDESCRIPTOR);
        if (fd == null)
          continue;
        fd.put(PdfName.FONTNAME, newName);
      }
    }
    return total;
  }

  /**
   * Creates a unique subset prefix to be added to the font name when the font
   * is embedded and subset.
   *
   * @return the subset prefix
   */
  private String createRandomSubsetPrefix() {
    String s = "";
    for (int k = 0; k < 6; ++k) {
      s += (char) (Math.random() * 26 + 'A');
    }
    return s + "+";
  }

  /**
   * Finds all the fonts not subset but embedded and marks them as subset.
   * 
   * @return the number of fonts altered
   */
  public int createFakeFontSubsets() {
    int total = 0;
    for (int k = 1; k < xrefObj.size(); ++k) {
      PdfObject obj = getPdfObjectRelease(k);
      if (obj == null || !obj.isDictionary())
        continue;
      PdfDictionary dic = (PdfDictionary) obj;
      if (!existsName(dic, PdfName.TYPE, PdfName.FONT))
        continue;
      if (existsName(dic, PdfName.SUBTYPE, PdfName.TYPE1)
          || existsName(dic, PdfName.SUBTYPE, PdfName.MMTYPE1)
          || existsName(dic, PdfName.SUBTYPE, PdfName.TRUETYPE)) {
        String s = getSubsetPrefix(dic);
        if (s != null)
          continue;
        s = getFontName(dic);
        if (s == null)
          continue;
        String ns = createRandomSubsetPrefix() + s;
        PdfDictionary fd = (PdfDictionary) getPdfObjectRelease(dic
            .get(PdfName.FONTDESCRIPTOR));
        if (fd == null)
          continue;
        if (fd.get(PdfName.FONTFILE) == null
            && fd.get(PdfName.FONTFILE2) == null
            && fd.get(PdfName.FONTFILE3) == null)
          continue;
        fd = dic.getAsDict(PdfName.FONTDESCRIPTOR);
        PdfName newName = new PdfName(ns);
        dic.put(PdfName.BASEFONT, newName);
        fd.put(PdfName.FONTNAME, newName);
        setXrefPartialObject(k, dic);
        ++total;
      }
    }
    return total;
  }

  private static PdfArray getNameArray(PdfObject obj) {
    if (obj == null)
      return null;
    obj = getPdfObjectRelease(obj);
    if (obj == null)
      return null;
    if (obj.isArray())
      return (PdfArray) obj;
    else if (obj.isDictionary()) {
      PdfObject arr2 = getPdfObjectRelease(((PdfDictionary) obj).get(PdfName.D));
      if (arr2 != null && arr2.isArray())
        return (PdfArray) arr2;
    }
    return null;
  }

  /**
   * Gets all the named destinations as an <CODE>HashMap</CODE>. The key is the
   * name and the value is the destinations array.
   * 
   * @return gets all the named destinations
   */
  public HashMap<Object, PdfObject> getNamedDestination() {
    return getNamedDestination(false);
  }

  /**
   * Gets all the named destinations as an <CODE>HashMap</CODE>. The key is the
   * name and the value is the destinations array.
   * 
   * @param keepNames
   *          true if you want the keys to be real PdfNames instead of Strings
   * @return gets all the named destinations
   * @since 2.1.6
   */
  public HashMap<Object, PdfObject> getNamedDestination(boolean keepNames) {
    HashMap<Object, PdfObject> names = getNamedDestinationFromNames(keepNames);
    names.putAll(getNamedDestinationFromStrings());
    return names;
  }

  /**
   * Gets the named destinations from the /Dests key in the catalog as an
   * <CODE>HashMap</CODE>. The key is the name and the value is the destinations
   * array.
   * 
   * @return gets the named destinations
   */
  public HashMap getNamedDestinationFromNames() {
    return getNamedDestinationFromNames(false);
  }

  /**
   * Gets the named destinations from the /Dests key in the catalog as an
   * <CODE>HashMap</CODE>. The key is the name and the value is the destinations
   * array.
   * 
   * @param keepNames
   *          true if you want the keys to be real PdfNames instead of Strings
   * @return gets the named destinations
   * @since 2.1.6
   */
  public HashMap<Object, PdfObject> getNamedDestinationFromNames(boolean keepNames) {
    HashMap<Object, PdfObject> names = new HashMap<>();
    if (catalog.get(PdfName.DESTS) != null) {
      PdfDictionary dic = (PdfDictionary) getPdfObjectRelease(catalog.get(PdfName.DESTS));
      if (dic == null)
        return names;
      Set keys = dic.getKeys();
        for (Object key1 : keys) {
            PdfName key = (PdfName) key1;
            PdfArray arr = getNameArray(dic.get(key));
            if (arr == null)
                continue;
            if (keepNames) {
                names.put(key, arr);
            } else {
                String name = PdfName.decodeName(key.toString());
                names.put(name, arr);
            }
        }
    }
    return names;
  }

  /**
   * Gets the named destinations from the /Names key in the catalog as an
   * <CODE>HashMap</CODE>. The key is the name and the value is the destinations
   * array.
   * 
   * @return gets the named destinations
   */
  public HashMap getNamedDestinationFromStrings() {
    if (catalog.get(PdfName.NAMES) != null) {
      PdfDictionary dic = (PdfDictionary) getPdfObjectRelease(catalog
          .get(PdfName.NAMES));
      if (dic != null) {
        dic = (PdfDictionary) getPdfObjectRelease(dic.get(PdfName.DESTS));
        if (dic != null) {
          HashMap<String, PdfObject> names = PdfNameTree.readTree(dic);
          for (Iterator<Map.Entry<String, PdfObject>> it = names.entrySet().iterator(); it.hasNext();) {
            Map.Entry<String, PdfObject> entry = it.next();
            PdfArray arr = getNameArray(entry.getValue());
            if (arr != null)
              entry.setValue(arr);
            else
              it.remove();
          }
          return names;
        }
      }
    }
    return new HashMap<>();
  }

  /**
   * Removes all the fields from the document.
   */
  public void removeFields() {
    pageRefs.resetReleasePage();
    for (int k = 1; k <= pageRefs.size(); ++k) {
      PdfDictionary page = pageRefs.getPageN(k);
      PdfArray annots = page.getAsArray(PdfName.ANNOTS);
      if (annots == null) {
        pageRefs.releasePage(k);
        continue;
      }
      for (int j = 0; j < annots.size(); ++j) {
        PdfObject obj = getPdfObjectRelease(annots.getPdfObject(j));
        if (obj == null || !obj.isDictionary())
          continue;
        PdfDictionary annot = (PdfDictionary) obj;
        if (PdfName.WIDGET.equals(annot.get(PdfName.SUBTYPE)))
          annots.remove(j--);
      }
      if (annots.isEmpty())
        page.remove(PdfName.ANNOTS);
      else
        pageRefs.releasePage(k);
    }
    catalog.remove(PdfName.ACROFORM);
    pageRefs.resetReleasePage();
  }

  /**
   * Removes all the annotations and fields from the document.
   */
  public void removeAnnotations() {
    pageRefs.resetReleasePage();
    for (int k = 1; k <= pageRefs.size(); ++k) {
      PdfDictionary page = pageRefs.getPageN(k);
      if (page.get(PdfName.ANNOTS) == null)
        pageRefs.releasePage(k);
      else
        page.remove(PdfName.ANNOTS);
    }
    catalog.remove(PdfName.ACROFORM);
    pageRefs.resetReleasePage();
  }

  public ArrayList<PdfAnnotation.PdfImportedLink> getLinks(int page) {
    pageRefs.resetReleasePage();
    ArrayList<PdfAnnotation.PdfImportedLink> result = new ArrayList<>();
    PdfDictionary pageDic = pageRefs.getPageN(page);
    if (pageDic.get(PdfName.ANNOTS) != null) {
      PdfArray annots = pageDic.getAsArray(PdfName.ANNOTS);
      for (int j = 0; j < annots.size(); ++j) {
        PdfDictionary annot = (PdfDictionary) getPdfObjectRelease(annots
            .getPdfObject(j));

        if (PdfName.LINK.equals(annot.get(PdfName.SUBTYPE))) {
          result.add(new PdfAnnotation.PdfImportedLink(annot));
        }
      }
    }
    pageRefs.releasePage(page);
    pageRefs.resetReleasePage();
    return result;
  }

  private void iterateBookmarks(PdfObject outlineRef, Map<Object, PdfObject> names) {
    while (outlineRef != null) {
      replaceNamedDestination(outlineRef, names);
      PdfDictionary outline = (PdfDictionary) getPdfObjectRelease(outlineRef);
      PdfObject first = outline.get(PdfName.FIRST);
      if (first != null) {
        iterateBookmarks(first, names);
      }
      outlineRef = outline.get(PdfName.NEXT);
    }
  }

  /**
   * Replaces remote named links with local destinations that have the same
   * name.
   * 
   * @since 5.0
   */
  public void makeRemoteNamedDestinationsLocal() {
    if (remoteToLocalNamedDestinations)
      return;
    remoteToLocalNamedDestinations = true;
    Map<Object, PdfObject> names = getNamedDestination(true);
    if (names.isEmpty())
      return;
    for (int k = 1; k <= pageRefs.size(); ++k) {
      PdfDictionary page = pageRefs.getPageN(k);
      PdfObject annotsRef;
      PdfArray annots = (PdfArray) getPdfObject(annotsRef = page
          .get(PdfName.ANNOTS));
      int annotIdx = lastXrefPartial;
      releaseLastXrefPartial();
      if (annots == null) {
        pageRefs.releasePage(k);
        continue;
      }
      boolean commitAnnots = false;
      for (int an = 0; an < annots.size(); ++an) {
        PdfObject objRef = annots.getPdfObject(an);
        if (convertNamedDestination(objRef, names) && !objRef.isIndirect())
          commitAnnots = true;
      }
      if (commitAnnots)
        setXrefPartialObject(annotIdx, annots);
      if (!commitAnnots || annotsRef.isIndirect())
        pageRefs.releasePage(k);
    }
  }

  /**
   * Converts a remote named destination GoToR with a local named destination if
   * there's a corresponding name.
   * 
   * @param obj
   *          an annotation that needs to be screened for links to external
   *          named destinations.
   * @param names
   *          a map with names of local named destinations
   * @since iText 5.0
   */
  private boolean convertNamedDestination(PdfObject obj, Map<Object, PdfObject> names) {
    obj = getPdfObject(obj);
    int objIdx = lastXrefPartial;
    releaseLastXrefPartial();
    if (obj != null && obj.isDictionary()) {
      PdfObject ob2 = getPdfObject(((PdfDictionary) obj).get(PdfName.A));
      if (ob2 != null) {
        int obj2Idx = lastXrefPartial;
        releaseLastXrefPartial();
        PdfDictionary dic = (PdfDictionary) ob2;
        PdfName type = (PdfName) getPdfObjectRelease(dic.get(PdfName.S));
        if (PdfName.GOTOR.equals(type)) {
          PdfObject ob3 = getPdfObjectRelease(dic.get(PdfName.D));
          Object name = null;
          if (ob3 != null) {
            if (ob3.isName())
              name = ob3;
            else if (ob3.isString())
              name = ob3.toString();
            PdfArray dest = (PdfArray) names.get(name);
            if (dest != null) {
              dic.remove(PdfName.F);
              dic.remove(PdfName.NEWWINDOW);
              dic.put(PdfName.S, PdfName.GOTO);
              setXrefPartialObject(obj2Idx, ob2);
              setXrefPartialObject(objIdx, obj);
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  /** Replaces all the local named links with the actual destinations. */
  public void consolidateNamedDestinations() {
    if (consolidateNamedDestinations)
      return;
    consolidateNamedDestinations = true;
    Map<Object, PdfObject> names = getNamedDestination(true);
    if (names.isEmpty())
      return;
    for (int k = 1; k <= pageRefs.size(); ++k) {
      PdfDictionary page = pageRefs.getPageN(k);
      PdfObject annotsRef;
      PdfArray annots = (PdfArray) getPdfObject(annotsRef = page
          .get(PdfName.ANNOTS));
      int annotIdx = lastXrefPartial;
      releaseLastXrefPartial();
      if (annots == null) {
        pageRefs.releasePage(k);
        continue;
      }
      boolean commitAnnots = false;
      for (int an = 0; an < annots.size(); ++an) {
        PdfObject objRef = annots.getPdfObject(an);
        if (replaceNamedDestination(objRef, names) && !objRef.isIndirect())
          commitAnnots = true;
      }
      if (commitAnnots)
        setXrefPartialObject(annotIdx, annots);
      if (!commitAnnots || annotsRef.isIndirect())
        pageRefs.releasePage(k);
    }
    PdfDictionary outlines = (PdfDictionary) getPdfObjectRelease(catalog
        .get(PdfName.OUTLINES));
    if (outlines == null)
      return;
    iterateBookmarks(outlines.get(PdfName.FIRST), names);
  }

  private boolean replaceNamedDestination(PdfObject obj, Map<Object, PdfObject> names) {
    obj = getPdfObject(obj);
    int objIdx = lastXrefPartial;
    releaseLastXrefPartial();
    if (obj != null && obj.isDictionary()) {
      PdfObject ob2 = getPdfObjectRelease(((PdfDictionary) obj)
          .get(PdfName.DEST));
      Object name = null;
      if (ob2 != null) {
        if (ob2.isName())
          name = ob2;
        else if (ob2.isString())
          name = ob2.toString();
        PdfArray dest = (PdfArray) names.get(name);
        if (dest != null) {
          ((PdfDictionary) obj).put(PdfName.DEST, dest);
          setXrefPartialObject(objIdx, obj);
          return true;
        }
      } else if ((ob2 = getPdfObject(((PdfDictionary) obj).get(PdfName.A))) != null) {
        int obj2Idx = lastXrefPartial;
        releaseLastXrefPartial();
        PdfDictionary dic = (PdfDictionary) ob2;
        PdfName type = (PdfName) getPdfObjectRelease(dic.get(PdfName.S));
        if (PdfName.GOTO.equals(type)) {
          PdfObject ob3 = getPdfObjectRelease(dic.get(PdfName.D));
          if (ob3 != null) {
            if (ob3.isName())
              name = ob3;
            else if (ob3.isString())
              name = ob3.toString();
          }
          PdfArray dest = (PdfArray) names.get(name);
          if (dest != null) {
            dic.put(PdfName.D, dest);
            setXrefPartialObject(obj2Idx, ob2);
            setXrefPartialObject(objIdx, obj);
            return true;
          }
        }
      }
    }
    return false;
  }

  protected static PdfDictionary duplicatePdfDictionary(PdfDictionary original,
      PdfDictionary copy, PdfReader newReader) {
    if (copy == null)
      copy = new PdfDictionary();
    for (Object o : original.getKeys()) {
      PdfName key = (PdfName) o;
      copy.put(key, duplicatePdfObject(original.get(key), newReader));
    }
    return copy;
  }

  protected static PdfObject duplicatePdfObject(PdfObject original,
      PdfReader newReader) {
    if (original == null)
      return null;
    switch (original.type()) {
    case PdfObject.DICTIONARY: {
      return duplicatePdfDictionary((PdfDictionary) original, null, newReader);
    }
    case PdfObject.STREAM: {
      PRStream org = (PRStream) original;
      PRStream stream = new PRStream(org, null, newReader);
      duplicatePdfDictionary(org, stream, newReader);
      return stream;
    }
    case PdfObject.ARRAY: {
      PdfArray arr = new PdfArray();
      ((PdfArray) original).getElements().forEach(pdfObject -> arr.add(duplicatePdfObject(pdfObject, newReader)));
      return arr;
    }
    case PdfObject.INDIRECT: {
      PRIndirectReference org = (PRIndirectReference) original;
      return new PRIndirectReference(newReader, org.getNumber(),
          org.getGeneration());
    }
    default:
      return original;
    }
  }

  /**
   * Closes the reader
   */
  @Override
  public void close() {
    if (!partial)
      return;
    try {
      tokens.close();
    } catch (IOException e) {
      throw new ExceptionConverter(e);
    }
  }

  @SuppressWarnings("unchecked")
  protected void removeUnusedNode(PdfObject obj, boolean[] hits) {
    Stack state = new Stack();
    state.push(obj);
    while (!state.empty()) {
      Object current = state.pop();
      if (current == null)
        continue;
      List<PdfObject> ar = null;
      PdfDictionary dic = null;
      PdfName[] keys = null;
      Object[] objs = null;
      int idx = 0;
      if (current instanceof PdfObject) {
        obj = (PdfObject) current;
        switch (obj.type()) {
        case PdfObject.DICTIONARY:
        case PdfObject.STREAM:
          dic = (PdfDictionary) obj;
          keys = new PdfName[dic.size()];
          dic.getKeys().toArray(keys);
          break;
        case PdfObject.ARRAY:
          ar = ((PdfArray) obj).getElements();
          break;
        case PdfObject.INDIRECT:
          PRIndirectReference ref = (PRIndirectReference) obj;
          int num = ref.getNumber();
          if (!hits[num]) {
            hits[num] = true;
            state.push(getPdfObjectRelease(ref));
          }
          continue;
        default:
          continue;
        }
      } else {
        objs = (Object[]) current;
        if (objs[0] instanceof ArrayList) {
          ar = (ArrayList) objs[0];
          idx = (Integer) objs[1];
        } else {
          keys = (PdfName[]) objs[0];
          dic = (PdfDictionary) objs[1];
          idx = (Integer) objs[2];
        }
      }
      if (ar != null) {
        for (int k = idx; k < ar.size(); ++k) {
          PdfObject v = ar.get(k);
          if (v.isIndirect()) {
            int num = ((PRIndirectReference) v).getNumber();
            if (num < 0 || num >= xrefObj.size() || (!partial && xrefObj.get(num) == null)) {
              ar.set(k, PdfNull.PDFNULL);
              continue;
            }
          }
          if (objs == null)
            state.push(new Object[] { ar, k + 1});
          else {
            objs[1] = k + 1;
            state.push(objs);
          }
          state.push(v);
          break;
        }
      } else {
        for (int k = idx; k < keys.length; ++k) {
          PdfName key = keys[k];
          PdfObject v = dic.get(key);
          if (v.isIndirect()) {
            int num = ((PRIndirectReference) v).getNumber();
            if (num < 0 || num >= xrefObj.size() || (!partial && xrefObj.get(num) == null)) {
              dic.put(key, PdfNull.PDFNULL);
              continue;
            }
          }
          if (objs == null)
            state.push(new Object[] { keys, dic, k + 1});
          else {
            objs[2] = k + 1;
            state.push(objs);
          }
          state.push(v);
          break;
        }
      }
    }
  }

  /**
   * Removes all the unreachable objects.
   * 
   * @return the number of indirect objects removed
   */
  public int removeUnusedObjects() {
    boolean[] hits = new boolean[xrefObj.size()];
    removeUnusedNode(trailer, hits);
    int total = 0;
    if (partial) {
      for (int k = 1; k < hits.length; ++k) {
        if (!hits[k]) {
          xref[k * 2] = -1;
          xref[k * 2 + 1] = 0;
          xrefObj.set(k, null);
          ++total;
        }
      }
    } else {
      for (int k = 1; k < hits.length; ++k) {
        if (!hits[k]) {
          xrefObj.set(k, null);
          ++total;
        }
      }
    }
    return total;
  }

  /**
   * Gets a read-only version of <CODE>AcroFields</CODE>.
   * 
   * @return a read-only version of <CODE>AcroFields</CODE>
   */
  public AcroFields getAcroFields() {
    return new AcroFields(this, null);
  }

  /**
   * Gets the global document JavaScript.
   * 
   * @param file
   *          the document file
   * @throws IOException
   *           on error
   * @return the global document JavaScript
   */
  public String getJavaScript(RandomAccessFileOrArray file) throws IOException {
    PdfDictionary names = (PdfDictionary) getPdfObjectRelease(catalog
        .get(PdfName.NAMES));
    if (names == null)
      return null;
    PdfDictionary js = (PdfDictionary) getPdfObjectRelease(names
        .get(PdfName.JAVASCRIPT));
    if (js == null)
      return null;
    Map<String, PdfObject> jscript = PdfNameTree.readTree(js);
    String[] sortedNames = new String[jscript.size()];
    sortedNames = jscript.keySet().toArray(sortedNames);
    Arrays.sort(sortedNames);
    StringBuilder buf = new StringBuilder();
    for (String sortedName : sortedNames) {
      PdfDictionary j = (PdfDictionary) getPdfObjectRelease(jscript.get(sortedName));
      if (j == null)
        continue;
      PdfObject obj = getPdfObjectRelease(j.get(PdfName.JS));
      if (obj != null) {
        if (obj.isString())
          buf.append(((PdfString) obj).toUnicodeString()).append('\n');
        else if (obj.isStream()) {
          byte[] bytes = getStreamBytes((PRStream) obj, file);
          if (bytes.length >= 2 && bytes[0] == (byte) 254
                  && bytes[1] == (byte) 255)
            buf.append(PdfEncodings.convertToString(bytes,
                    PdfObject.TEXT_UNICODE));
          else
            buf.append(PdfEncodings.convertToString(bytes,
                    PdfObject.TEXT_PDFDOCENCODING));
          buf.append('\n');
        }
      }
    }
    return buf.toString();
  }

  /**
   * Gets the global document JavaScript.
   * 
   * @throws IOException
   *           on error
   * @return the global document JavaScript
   */
  public String getJavaScript() throws IOException {
    RandomAccessFileOrArray rf = getSafeFile();
    try {
      rf.reOpen();
      return getJavaScript(rf);
    } finally {
      try {
        rf.close();
      } catch (Exception ignored) {
      }
    }
  }

  /**
   * Selects the pages to keep in the document. The pages are described as
   * ranges. The page ordering can be changed but no page repetitions are
   * allowed. Note that it may be very slow in partial mode.
   * 
   * @param ranges
   *          the comma separated ranges as described in {@link SequenceList}
   */
  public void selectPages(String ranges) {
    selectPages(SequenceList.expand(ranges, getNumberOfPages()));
  }

  /**
   * Selects the pages to keep in the document. The pages are described as a
   * <CODE>List</CODE> of <CODE>Integer</CODE>. The page ordering can be changed
   * but no page repetitions are allowed. Note that it may be very slow in
   * partial mode.
   * 
   * @param pagesToKeep
   *          the pages to keep in the document
   */
  public void selectPages(List<Integer> pagesToKeep) {
    pageRefs.selectPages(pagesToKeep);
    removeUnusedObjects();
  }

  /**
   * Sets the viewer preferences as the sum of several constants.
   * 
   * @param preferences
   *          the viewer preferences
   * @see PdfViewerPreferences#setViewerPreferences
   */
  @Override
  public void setViewerPreferences(int preferences) {
    this.viewerPreferences.setViewerPreferences(preferences);
    setViewerPreferences(this.viewerPreferences);
  }

  /**
   * Adds a viewer preference
   * 
   * @param key
   *          a key for a viewer preference
   * @param value
   *          a value for the viewer preference
   * @see PdfViewerPreferences#addViewerPreference
   */
  @Override
  public void addViewerPreference(PdfName key, PdfObject value) {
    this.viewerPreferences.addViewerPreference(key, value);
    setViewerPreferences(this.viewerPreferences);
  }

  void setViewerPreferences(PdfViewerPreferencesImp vp) {
    vp.addToCatalog(catalog);
  }

  /**
   * Returns a bitset representing the PageMode and PageLayout viewer
   * preferences. Doesn't return any information about the ViewerPreferences
   * dictionary.
   * 
   * @return an int that contains the Viewer Preferences.
   */
  public int getSimpleViewerPreferences() {
    return PdfViewerPreferencesImp.getViewerPreferences(catalog)
        .getPageLayoutAndMode();
  }

  /**
   * Getter for property appendable.
   * 
   * @return Value of property appendable.
   */
  public boolean isAppendable() {
    return this.appendable;
  }

  /**
   * Setter for property appendable.
   * 
   * @param appendable
   *          New value of property appendable.
   */
  public void setAppendable(boolean appendable) {
    this.appendable = appendable;
    if (appendable)
      getPdfObject(trailer.get(PdfName.ROOT));
  }

  /**
   * Getter for property newXrefType.
   * 
   * @return Value of property newXrefType.
   */
  public boolean isNewXrefType() {
    return newXrefType;
  }

  /**
   * Getter for property fileLength.
   * 
   * @return Value of property fileLength.
   */
  public int getFileLength() {
    return fileLength;
  }

  /**
   * Getter for property hybridXref.
   * 
   * @return Value of property hybridXref.
   */
  public boolean isHybridXref() {
    return hybridXref;
  }

  static class PageRefs {
    private final PdfReader reader;
    /**
     * ArrayList with the indirect references to every page. Element 0 = page 1;
     * 1 = page 2;... Not used for partial reading.
     */
    private List<PdfObject> refsn;
    /** The number of pages, updated only in case of partial reading. */
    private int sizep;
    /**
     * intHashtable that does the same thing as refsn in case of partial
     * reading: major difference: not all the pages are read.
     */
    private IntHashtable refsp;
    /** Page number of the last page that was read (partial reading only) */
    private int lastPageRead = -1;
    /**
     * stack to which pages dictionaries are pushed to keep track of the current
     * page attributes
     */
    private List<PdfDictionary> pageInh;
    private boolean keepPages;

    private PageRefs(PdfReader reader) {
      this.reader = reader;
      if (reader.partial) {
        refsp = new IntHashtable();
        PdfNumber npages = (PdfNumber) PdfReader
            .getPdfObjectRelease(reader.rootPages.get(PdfName.COUNT));
        sizep = npages.intValue();
      } else {
        readPages();
      }
    }

    PageRefs(PageRefs other, PdfReader reader) {
      this.reader = reader;
      this.sizep = other.sizep;
      if (other.refsn != null) {
        refsn = new ArrayList<>(other.refsn);
        for (int k = 0; k < refsn.size(); ++k) {
          refsn.set(k, duplicatePdfObject(refsn.get(k), reader));
        }
      } else
        this.refsp = (IntHashtable) other.refsp.clone();
    }

    int size() {
      if (refsn != null)
        return refsn.size();
      else
        return sizep;
    }

    void readPages() {
      if (refsn != null)
        return;
      refsp = null;
      refsn = new ArrayList<>();
      pageInh = new ArrayList<>();
      PdfObject obj = reader.catalog.get(PdfName.PAGES);
      if (obj instanceof PRIndirectReference)
        iteratePages((PRIndirectReference) obj);
      else if (obj instanceof PdfDictionary)
        iteratePages((PdfDictionary) obj);
      pageInh = null;
      reader.rootPages.put(PdfName.COUNT, new PdfNumber(refsn.size()));
    }

    void reReadPages() {
      refsn = null;
      readPages();
    }

    /**
     * Gets the dictionary that represents a page.
     * 
     * @param pageNum
     *          the page number. 1 is the first
     * @return the page dictionary
     */
    public PdfDictionary getPageN(int pageNum) {
      PRIndirectReference ref = getPageOrigRef(pageNum);
      return (PdfDictionary) PdfReader.getPdfObject(ref);
    }

    /**
     * @param pageNum
     * @return a dictionary object
     */
    public PdfDictionary getPageNRelease(int pageNum) {
      PdfDictionary page = getPageN(pageNum);
      releasePage(pageNum);
      return page;
    }

    /**
     * @param pageNum
     * @return an indirect reference
     */
    public PRIndirectReference getPageOrigRefRelease(int pageNum) {
      PRIndirectReference ref = getPageOrigRef(pageNum);
      releasePage(pageNum);
      return ref;
    }

    /**
     * Gets the page reference to this page.
     * 
     * @param pageNum
     *          the page number. 1 is the first
     * @return the page reference
     */
    public PRIndirectReference getPageOrigRef(int pageNum) {
      try {
        --pageNum;
        if (pageNum < 0 || pageNum >= size())
          return null;
        if (refsn != null)
          return (PRIndirectReference) refsn.get(pageNum);
        else {
          int n = refsp.get(pageNum);
          if (n == 0) {
            PRIndirectReference ref = getSinglePage(pageNum);
            if (reader.lastXrefPartial == -1)
              lastPageRead = -1;
            else
              lastPageRead = pageNum;
            reader.lastXrefPartial = -1;
            refsp.put(pageNum, ref.getNumber());
            if (keepPages)
              lastPageRead = -1;
            return ref;
          } else {
            if (lastPageRead != pageNum)
              lastPageRead = -1;
            if (keepPages)
              lastPageRead = -1;
            return new PRIndirectReference(reader, n);
          }
        }
      } catch (Exception e) {
        throw new ExceptionConverter(e);
      }
    }

    void keepPages() {
      if (refsp == null || keepPages)
        return;
      keepPages = true;
      refsp.clear();
    }

    /**
     */
    public void releasePage(int pageNum) {
      if (refsp == null)
        return;
      --pageNum;
      if (pageNum < 0 || pageNum >= size())
        return;
      if (pageNum != lastPageRead)
        return;
      lastPageRead = -1;
      reader.lastXrefPartial = refsp.get(pageNum);
      reader.releaseLastXrefPartial();
      refsp.remove(pageNum);
    }

    /**
         *
         */
    public void resetReleasePage() {
      if (refsp == null)
        return;
      lastPageRead = -1;
    }

    void insertPage(int pageNum, PRIndirectReference ref) {
      --pageNum;
      if (refsn != null) {
        if (pageNum >= refsn.size())
          refsn.add(ref);
        else
          refsn.add(pageNum, ref);
      } else {
        ++sizep;
        lastPageRead = -1;
        if (pageNum >= size()) {
          refsp.put(size(), ref.getNumber());
        } else {
          IntHashtable refs2 = new IntHashtable((refsp.size() + 1) * 2);
          for (Iterator it = refsp.getEntryIterator(); it.hasNext();) {
            IntHashtable.Entry entry = (IntHashtable.Entry) it.next();
            int p = entry.getKey();
            refs2.put(p >= pageNum ? p + 1 : p, entry.getValue());
          }
          refs2.put(pageNum, ref.getNumber());
          refsp = refs2;
        }
      }
    }

    /**
     * Adds a PdfDictionary to the pageInh stack to keep track of the page
     * attributes.
     * 
     * @param nodePages
     *          a Pages dictionary
     */
    private void pushPageAttributes(PdfDictionary nodePages) {
      PdfDictionary dic = new PdfDictionary();
      if (!pageInh.isEmpty()) {
        dic.putAll(pageInh.get(pageInh.size() - 1));
      }
      for (PdfName pageInhCandidate : pageInhCandidates) {
        PdfObject obj = nodePages.get(pageInhCandidate);
        if (obj != null)
          dic.put(pageInhCandidate, obj);
      }
      pageInh.add(dic);
    }

    /**
     * Removes the last PdfDictionary that was pushed to the pageInh stack.
     */
    private void popPageAttributes() {
      pageInh.remove(pageInh.size() - 1);
    }

    private void iteratePages(PRIndirectReference rpage) {
      PdfDictionary page = (PdfDictionary) getPdfObject(rpage);
      if (page == null)
        return;
      PdfArray kidsPR = page.getAsArray(PdfName.KIDS);
      // reference to a leaf
      if (kidsPR == null) {
        page.put(PdfName.TYPE, PdfName.PAGE);
        PdfDictionary dic = pageInh.get(pageInh.size() - 1);
        PdfName key;
        for (Object o : dic.getKeys()) {
          key = (PdfName) o;
          if (page.get(key) == null)
            page.put(key, dic.get(key));
        }
        if (page.get(PdfName.MEDIABOX) == null) {
          PdfArray arr = new PdfArray(new float[] { 0, 0,
                  PageSize.LETTER.getRight(), PageSize.LETTER.getTop() });
          page.put(PdfName.MEDIABOX, arr);
        }
        refsn.add(rpage);
      }
      // reference to a branch
      else {
        page.put(PdfName.TYPE, PdfName.PAGES);
        pushPageAttributes(page);
        for (int k = 0; k < kidsPR.size(); ++k) {
          PdfObject obj = kidsPR.getPdfObject(k);
          if (!obj.isIndirect()) {
            while (k < kidsPR.size())
              kidsPR.remove(k);
            break;
          }
          if (obj instanceof PRIndirectReference)
            iteratePages((PRIndirectReference) obj);
          else if (obj instanceof PdfDictionary)
            iteratePages((PdfDictionary) obj);
        }
        popPageAttributes();
      }
    }

    private void iteratePages(PdfDictionary page) {
      PdfArray kidsPR = page.getAsArray(PdfName.KIDS);
      // reference to a leaf
      if (kidsPR != null) {
        page.put(PdfName.TYPE, PdfName.PAGES);
        pushPageAttributes(page);
        for (int k = 0; k < kidsPR.size(); ++k) {
          PdfObject obj = kidsPR.getPdfObject(k);
          if (!obj.isIndirect()) {
            while (k < kidsPR.size())
              kidsPR.remove(k);
            break;
          }
          if (obj instanceof PRIndirectReference)
            iteratePages((PRIndirectReference) obj);
          else if (obj instanceof PdfDictionary)
            iteratePages((PdfDictionary) obj);
        }
        popPageAttributes();
      }
    }

    protected PRIndirectReference getSinglePage(int n) {
      PdfDictionary acc = new PdfDictionary();
      PdfDictionary top = reader.rootPages;
      int base = 0;
      while (true) {
        for (PdfName pageInhCandidate : pageInhCandidates) {
          PdfObject obj = top.get(pageInhCandidate);
          if (obj != null)
            acc.put(pageInhCandidate, obj);
        }
        PdfArray kids = (PdfArray) PdfReader.getPdfObjectRelease(top.get(PdfName.KIDS));
        for (PdfObject pdfObject : kids.getElements()) {
          PRIndirectReference ref = (PRIndirectReference) pdfObject;
          PdfDictionary dic = (PdfDictionary) getPdfObject(ref);
          int last = reader.lastXrefPartial;
          PdfObject count = getPdfObjectRelease(dic.get(PdfName.COUNT));
          reader.lastXrefPartial = last;
          int acn = 1;
          if (count != null && count.type() == PdfObject.NUMBER)
            acn = ((PdfNumber) count).intValue();
          if (n < base + acn) {
            if (count == null) {
              dic.mergeDifferent(acc);
              return ref;
            }
            reader.releaseLastXrefPartial();
            top = dic;
            break;
          }
          reader.releaseLastXrefPartial();
          base += acn;
        }
      }
    }

    private void selectPages(List<Integer> pagesToKeep) {
      IntHashtable pg = new IntHashtable();
      List<Integer> finalPages = new ArrayList<>();
      int psize = size();
      for (Integer aPagesToKeep : pagesToKeep) {
        if (aPagesToKeep >= 1 && aPagesToKeep <= psize && pg.put(aPagesToKeep, 1) == 0)
          finalPages.add(aPagesToKeep);
      }
      if (reader.partial) {
        for (int k = 1; k <= psize; ++k) {
          getPageOrigRef(k);
          resetReleasePage();
        }
      }
      PRIndirectReference parent = (PRIndirectReference) reader.catalog
          .get(PdfName.PAGES);
      PdfDictionary topPages = (PdfDictionary) PdfReader.getPdfObject(parent);
      List<PdfObject> newPageRefs = new ArrayList<>(finalPages.size());
      PdfArray kids = new PdfArray();
      for (Object finalPage : finalPages) {
        int p = (Integer) finalPage;
        PRIndirectReference pref = getPageOrigRef(p);
        resetReleasePage();
        kids.add(pref);
        newPageRefs.add(pref);
        getPageN(p).put(PdfName.PARENT, parent);
      }
      AcroFields af = reader.getAcroFields();
      boolean removeFields = (af.getAllFields().size() > 0);
      for (int k = 1; k <= psize; ++k) {
        if (!pg.containsKey(k)) {
          if (removeFields)
            af.removeFieldsFromPage(k);
          PRIndirectReference pref = getPageOrigRef(k);
          int nref = pref.getNumber();
          reader.xrefObj.set(nref, null);
          if (reader.partial) {
            reader.xref[nref * 2] = -1;
            reader.xref[nref * 2 + 1] = 0;
          }
        }
      }
      topPages.put(PdfName.COUNT, new PdfNumber(finalPages.size()));
      topPages.put(PdfName.KIDS, kids);
      refsp = null;
      refsn = newPageRefs;
    }
  }

  PdfIndirectReference getCryptoRef() {
    if (cryptoRef == null)
      return null;
    return new PdfIndirectReference(0, cryptoRef.getNumber(),
        cryptoRef.getGeneration());
  }

  /**
   * Removes any usage rights that this PDF may have. Only Adobe can grant usage
   * rights and any PDF modification with iText will invalidate them.
   * Invalidated usage rights may confuse Acrobat and it's advisable to remove
   * them altogether.
   */
  public void removeUsageRights() {
    PdfDictionary perms = catalog.getAsDict(PdfName.PERMS);
    if (perms == null)
      return;
    perms.remove(PdfName.UR);
    perms.remove(PdfName.UR3);
    if (perms.size() == 0)
      catalog.remove(PdfName.PERMS);
  }

  /**
   * Gets the certification level for this document. The return values can be
   * <code>PdfSignatureAppearance.NOT_CERTIFIED</code>,
   * <code>PdfSignatureAppearance.CERTIFIED_NO_CHANGES_ALLOWED</code>,
   * <code>PdfSignatureAppearance.CERTIFIED_FORM_FILLING</code> and
   * <code>PdfSignatureAppearance.CERTIFIED_FORM_FILLING_AND_ANNOTATIONS</code>
   * .
   * <p>
   * No signature validation is made, use the methods available for that in
   * <CODE>AcroFields</CODE>.
   * </p>
   * 
   * @return gets the certification level for this document
   */
  public int getCertificationLevel() {
    PdfDictionary dic = catalog.getAsDict(PdfName.PERMS);
    if (dic == null)
      return PdfSignatureAppearance.NOT_CERTIFIED;
    dic = dic.getAsDict(PdfName.DOCMDP);
    if (dic == null)
      return PdfSignatureAppearance.NOT_CERTIFIED;
    PdfArray arr = dic.getAsArray(PdfName.REFERENCE);
    if (arr == null || arr.size() == 0)
      return PdfSignatureAppearance.NOT_CERTIFIED;
    dic = arr.getAsDict(0);
    if (dic == null)
      return PdfSignatureAppearance.NOT_CERTIFIED;
    dic = dic.getAsDict(PdfName.TRANSFORMPARAMS);
    if (dic == null)
      return PdfSignatureAppearance.NOT_CERTIFIED;
    PdfNumber p = dic.getAsNumber(PdfName.P);
    if (p == null)
      return PdfSignatureAppearance.NOT_CERTIFIED;
    return p.intValue();
  }

  /**
   * Checks if an encrypted document may be modified if the owner password was not supplied.
   * If the document is not encrypted, the setting has no effect.
   *
   * @return <CODE>true</CODE> if the document may be modified even if the owner password was not
   *         supplied <CODE>false</CODE> otherwise
   */
  public boolean isModificationlowedWithoutOwnerPassword() {
    return this.modificationAllowedWithoutOwnerPassword;
  }

  /**
   * Sets whether the document (if encrypted) may be modified even if the owner password was not
   * supplied. If this is set to <CODE>false</CODE> an exception will be thrown when attempting to
   * access the Document if the owner password was not supplied (for encrypted documents.)
   *
   * @param modificationAllowedWithoutOwnerPassword
   *         the modificationAllowedWithoutOwnerPassword state.
   */
  public void setModificationAllowedWithoutOwnerPassword(
    boolean modificationAllowedWithoutOwnerPassword) {
    this.modificationAllowedWithoutOwnerPassword = modificationAllowedWithoutOwnerPassword;
  }

  /**
   * Checks if the document was opened with the owner password so that the end
   * application can decide what level of access restrictions to apply. If the
   * document is not encrypted it will return <CODE>true</CODE>.
   *
   * @return <CODE>true</CODE> if the document was opened with the owner password or if it's not
   *         encrypted or the modificationAllowedWithoutOwnerPassword flag is set,
   *         <CODE>false</CODE> otherwise.
   */
  public final boolean isOpenedWithFullPermissions() {
    return !encrypted || ownerPasswordUsed || modificationAllowedWithoutOwnerPassword;
  }

  public int getCryptoMode() {
    if (decrypt == null)
      return -1;
    else
      return decrypt.getCryptoMode();
  }

  public boolean isMetadataEncrypted() {
    if (decrypt == null)
      return false;
    else
      return decrypt.isMetadataEncrypted();
  }

  public byte[] computeUserPassword() {
    if (!encrypted || !ownerPasswordUsed)
      return null;
    return decrypt.computeUserPassword(password);
  }
}
