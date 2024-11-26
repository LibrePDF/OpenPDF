/*
 * $Id: PdfSignatureAppearance.java 4065 2009-09-16 23:09:11Z psoares33 $
 *
 * Copyright 2004-2006 by Paulo Soares.
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

import com.lowagie.text.Chunk;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.error_messages.MessageLocalization;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.security.PrivateKey;
import java.security.cert.CRL;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

/**
 * This class takes care of the cryptographic options and appearances that form a signature.
 */
public class PdfSignatureAppearance {

    /**
     * The rendering mode is just the description
     */
    public static final int SignatureRenderDescription = 0;
    /**
     * The rendering mode is the name of the signer and the description
     */
    public static final int SignatureRenderNameAndDescription = 1;
    /**
     * The rendering mode is an image and the description
     */
    public static final int SignatureRenderGraphicAndDescription = 2;

    /**
     * The rendering mode is an image and the description
     */
    public static final int SignatureRenderGraphic = 3;

    /**
     * The self signed filter.
     */
    public static final PdfName SELF_SIGNED = PdfName.ADOBE_PPKLITE;
    /**
     * The VeriSign filter.
     */
    public static final PdfName VERISIGN_SIGNED = PdfName.VERISIGN_PPKVS;
    /**
     * The Windows Certificate Security.
     */
    public static final PdfName WINCER_SIGNED = PdfName.ADOBE_PPKMS;

    public static final int NOT_CERTIFIED = -1;
    public static final int CERTIFIED_ALL_CHANGES_ALLOWED = 0;
    public static final int CERTIFIED_NO_CHANGES_ALLOWED = 1;
    public static final int CERTIFIED_FORM_FILLING = 2;
    public static final int CERTIFIED_FORM_FILLING_AND_ANNOTATIONS = 3;
    /**
     * Commands to draw a yellow question mark in a stream content
     */
    public static final String questionMark = "% DSUnknown\n" + "q\n" + "1 G\n"
            + "1 g\n" + "0.1 0 0 0.1 9 0 cm\n" + "0 J 0 j 4 M []0 d\n" + "1 i \n"
            + "0 g\n" + "313 292 m\n" + "313 404 325 453 432 529 c\n"
            + "478 561 504 597 504 645 c\n" + "504 736 440 760 391 760 c\n"
            + "286 760 271 681 265 626 c\n" + "265 625 l\n" + "100 625 l\n"
            + "100 828 253 898 381 898 c\n" + "451 898 679 878 679 650 c\n"
            + "679 555 628 499 538 435 c\n" + "488 399 467 376 467 292 c\n"
            + "313 292 l\n" + "h\n" + "308 214 170 -164 re\n" + "f\n" + "0.44 G\n"
            + "1.2 w\n" + "1 1 0.4 rg\n" + "287 318 m\n"
            + "287 430 299 479 406 555 c\n" + "451 587 478 623 478 671 c\n"
            + "478 762 414 786 365 786 c\n" + "260 786 245 707 239 652 c\n"
            + "239 651 l\n" + "74 651 l\n" + "74 854 227 924 355 924 c\n"
            + "425 924 653 904 653 676 c\n" + "653 581 602 525 512 461 c\n"
            + "462 425 441 402 441 318 c\n" + "287 318 l\n" + "h\n"
            + "282 240 170 -164 re\n" + "B\n" + "Q\n";
    private static final float TOP_SECTION = 0.3f;
    private static final float MARGIN = 2;
    private final PdfTemplate[] app = new PdfTemplate[5];
    private final PdfStamperImp writer;
    private Rectangle rect;
    private Rectangle pageRect;
    private PdfTemplate frm;
    private String layer2Text;
    private String reason;
    private String location;
    private Calendar signDate;
    private String provider;
    private int page = 1;
    private String fieldName;
    private PrivateKey privKey;
    private CRL[] crlList;
    private PdfName filter;
    private boolean newField;
    private ByteBuffer sigout;
    private OutputStream originalout;
    private File tempFile;
    private PdfDictionary cryptoDictionary;
    private PdfStamper stamper;
    private boolean preClosed = false;
    private PdfSigGenericPKCS sigStandard;
    private long[] range;
    private RandomAccessFile raf;
    private byte[] bout;
    private int boutLen;
    private byte[] externalDigest;
    private byte[] externalRSAdata;
    private String digestEncryptionAlgorithm;
    private Map<PdfName, PdfLiteral> exclusionLocations;

    // ******************************************************************************
    private Certificate[] certChain;
    private int render = SignatureRenderDescription;
    private Image signatureGraphic = null;
    private PdfImportedPage signaturePDF = null;
    /**
     * Holds value of property contact.
     */
    private String contact;
    /**
     * Holds value of property layer2Font.
     */
    private Font layer2Font;
    /**
     * Holds value of property layer4Text.
     */
    private String layer4Text;
    /**
     * Holds value of property acro6Layers.
     */
    private boolean acro6Layers;
    /**
     * Holds value of property runDirection.
     */
    private int runDirection = PdfWriter.RUN_DIRECTION_NO_BIDI;
    /**
     * Holds value of property signatureEvent.
     */
    private SignatureEvent signatureEvent;
    /**
     * Holds value of property image.
     */
    private Image image;
    /**
     * Holds value of property imageScale.
     */
    private float imageScale;
    private int certificationLevel = NOT_CERTIFIED;

    PdfSignatureAppearance(PdfStamperImp writer) {
        this.writer = writer;
        fieldName = getNewSigName();
    }

    /**
     * Fits the text to some rectangle adjusting the font size as needed.
     *
     * @param font         the font to use
     * @param text         the text
     * @param rect         the rectangle where the text must fit
     * @param maxFontSize  the maximum font size
     * @param runDirection the run direction
     * @return the calculated font size that makes the text fit
     */
    public static float fitText(Font font, String text, Rectangle rect,
            float maxFontSize, int runDirection) {
        try {
            ColumnText ct = null;
            int status = 0;
            if (maxFontSize <= 0) {
                int cr = 0;
                int lf = 0;
                char[] t = text.toCharArray();
                for (char c : t) {
                    if (c == '\n') {
                        ++lf;
                    } else if (c == '\r') {
                        ++cr;
                    }
                }
                int minLines = Math.max(cr, lf) + 1;
                maxFontSize = Math.abs(rect.getHeight()) / minLines - 0.001f;
            }
            font.setSize(maxFontSize);
            Phrase ph = new Phrase(text, font);
            ct = new ColumnText(null);
            ct.setSimpleColumn(ph, rect.getLeft(), rect.getBottom(), rect.getRight(),
                    rect.getTop(), maxFontSize, Element.ALIGN_LEFT);
            ct.setRunDirection(runDirection);
            status = ct.go(true);
            if ((status & ColumnText.NO_MORE_TEXT) != 0) {
                return maxFontSize;
            }
            float precision = 0.1f;
            float min = 0;
            float max = maxFontSize;
            float size = maxFontSize;
            for (int k = 0; k < 50; ++k) { // just in case it doesn't converge
                size = (min + max) / 2;
                ct = new ColumnText(null);
                font.setSize(size);
                ct.setSimpleColumn(new Phrase(text, font), rect.getLeft(),
                        rect.getBottom(), rect.getRight(), rect.getTop(), size,
                        Element.ALIGN_LEFT);
                ct.setRunDirection(runDirection);
                status = ct.go(true);
                if ((status & ColumnText.NO_MORE_TEXT) != 0) {
                    if (max - min < size * precision) {
                        return size;
                    }
                    min = size;
                } else {
                    max = size;
                }
            }
            return size;
        } catch (Exception e) {
            throw new ExceptionConverter(e);
        }
    }

    /**
     * Gets the rendering mode for this signature.
     *
     * @return the rendering mode for this signature
     */
    public int getRender() {
        return render;
    }

    /**
     * Sets the rendering mode for this signature. The rendering modes can be the constants
     * <CODE>SignatureRenderDescription</CODE>,
     * <CODE>SignatureRenderNameAndDescription</CODE> or
     * <CODE>SignatureRenderGraphicAndDescription</CODE>. The two last modes
     * should be used with Acrobat 6 layer type.
     *
     * @param render the render mode
     */
    public void setRender(int render) {
        this.render = render;
    }

    // ******************************************************************************

    /**
     * Gets the Image object to render.
     *
     * @return the image
     */
    public Image getSignatureGraphic() {
        return signatureGraphic;
    }

    /**
     * Gets the PDF page to render.
     *
     * @return the PDF page
     */
    public PdfImportedPage getSignaturePDF() {
        return signaturePDF;
    }

    /**
     * Check whether we already have a valid signature graphic (image or PDF).
     *
     * @return true if we have one valid signature graphic
     */
    public boolean hasValidSignatureGraphic() {
        return (signatureGraphic != null || signaturePDF != null);
    }

    /**
     * Sets the Image object to render when Render is set to
     * <CODE>SignatureRenderGraphicAndDescription</CODE>
     *
     * @param signatureGraphic image rendered. If <CODE>null</CODE> the mode is defaulted to
     *                         <CODE>SignatureRenderDescription</CODE>
     */
    public void setSignatureGraphic(Image signatureGraphic) {
        this.signatureGraphic = signatureGraphic;
        this.signaturePDF = null;
    }

    /**
     * Sets the PDF page to render as signature when Render is set to
     * <CODE>SignatureRenderGraphicAndDescription</CODE>
     *
     * @param sigFile
     *          The PDF file to be rendered.
     * @param pgnum
     *          The page number in the sigFile to be rendered (start from 1).
     */
    public void setSignaturePDF(PdfReader sigFile, int pgnum) {
        this.signaturePDF = writer.getImportedPage(sigFile, pgnum);
        this.signatureGraphic = null;
    }

    /**
     * Gets the signature text identifying the signer if set by setLayer2Text().
     *
     * @return the signature text identifying the signer
     */
    public String getLayer2Text() {
        return layer2Text;
    }

    /**
     * Sets the signature text identifying the signer.
     *
     * @param text the signature text identifying the signer. If <CODE>null</CODE> or not set a standard description
     *             will be used
     */
    public void setLayer2Text(String text) {
        layer2Text = text;
    }

    /**
     * Gets the text identifying the signature status if set by setLayer4Text().
     *
     * @return the text identifying the signature status
     */
    public String getLayer4Text() {
        return layer4Text;
    }

    /**
     * Sets the text identifying the signature status.
     *
     * @param text the text identifying the signature status. If <CODE>null</CODE> or not set the description "Signature
     *             Not Verified" will be used
     */
    public void setLayer4Text(String text) {
        layer4Text = text;
    }

    /**
     * Gets the rectangle representing the signature dimensions.
     *
     * @return the rectangle representing the signature dimensions. It may be
     * <CODE>null</CODE> or have zero width or height for invisible
     * signatures
     */
    public Rectangle getRect() {
        return rect;
    }

    /**
     * Gets the visibility status of the signature.
     *
     * @return the visibility status of the signature
     */
    public boolean isInvisible() {
        return (rect == null || rect.getWidth() == 0 || rect.getHeight() == 0);
    }

    /**
     * Sets the cryptographic parameters.
     *
     * @param privKey     the private key
     * @param certificate the certificate
     * @param crl         the certificate revocation list. It may be <CODE>null</CODE>
     * @param filter      the cryptographic filter type. It can be SELF_SIGNED, VERISIGN_SIGNED or WINCER_SIGNED
     */
    public void setCrypto(PrivateKey privKey, X509Certificate certificate,
            CRL crl, PdfName filter) {
        this.privKey = privKey;
        if (certificate == null) {
            throw new IllegalArgumentException("Null certificate not allowed");
        }
        this.certChain = new Certificate[1];
        this.certChain[0] = certificate;
        if (crl != null) {
            this.crlList = new CRL[1];
            this.crlList[0] = crl;
        }
        this.filter = filter;
    }

    /**
     * Sets the cryptographic parameters.
     *
     * @param privKey   the private key
     * @param certChain the certification chain
     * @param crlList   the crl list
     * @param filter    the PdfName
     */
    public void setCrypto(PrivateKey privKey, Certificate[] certChain, CRL[] crlList, PdfName filter) {
        this.privKey = privKey;
        this.certChain = certChain;
        this.crlList = crlList;
        this.filter = filter;
    }

    // OJO... Modificacion de
    // flopez-------------------------------------------------
    public void setVisibleSignature(Rectangle pageRect, int page) {
        setVisibleSignature(pageRect, page, getNewSigName());
    }

    /**
     * Sets the signature to be visible. It creates a new visible signature field.
     *
     * @param pageRect  the position and dimension of the field in the page
     * @param page      the page to place the field. The fist page is 1
     * @param fieldName the field name or <CODE>null</CODE> to generate automatically a new field name
     */
    public void setVisibleSignature(Rectangle pageRect, int page, String fieldName) {
        if (fieldName != null) {
            if (fieldName.indexOf('.') >= 0) {
                throw new IllegalArgumentException(
                        MessageLocalization
                                .getComposedMessage("field.names.cannot.contain.a.dot"));
            }
            AcroFields af = writer.getAcroFields();
            AcroFields.Item item = af.getFieldItem(fieldName);
            if (item != null) {
                throw new IllegalArgumentException(
                        MessageLocalization.getComposedMessage(
                                "the.field.1.already.exists", fieldName));
            }
            this.fieldName = fieldName;
        }
        // OJO... Modificacion de
        // flopez--------------------------------------------------
        // if (page < 1 || page > writer.reader.getNumberOfPages())
        if (page < 0 || page > writer.reader.getNumberOfPages()) {
            throw new IllegalArgumentException(
                    MessageLocalization.getComposedMessage("invalid.page.number.1", page));
        }
        // ******************************************************************************
        this.pageRect = new Rectangle(pageRect);
        this.pageRect.normalize();
        rect = new Rectangle(this.pageRect.getWidth(), this.pageRect.getHeight());
        this.page = page;
        newField = true;
    }

    /**
     * Sets the signature to be visible. An empty signature field with the same name must already exist.
     *
     * @param fieldName the existing empty signature field name
     */
    public void setVisibleSignature(String fieldName) {
        AcroFields af = writer.getAcroFields();
        AcroFields.Item item = af.getFieldItem(fieldName);
        if (item == null) {
            throw new IllegalArgumentException(
                    MessageLocalization.getComposedMessage("the.field.1.does.not.exist",
                            fieldName));
        }
        PdfDictionary merged = item.getMerged(0);
        if (!PdfName.SIG.equals(PdfReader.getPdfObject(merged.get(PdfName.FT)))) {
            throw new IllegalArgumentException(
                    MessageLocalization.getComposedMessage(
                            "the.field.1.is.not.a.signature.field", fieldName));
        }
        this.fieldName = fieldName;
        PdfArray r = merged.getAsArray(PdfName.RECT);
        float llx = r.getAsNumber(0).floatValue();
        float lly = r.getAsNumber(1).floatValue();
        float urx = r.getAsNumber(2).floatValue();
        float ury = r.getAsNumber(3).floatValue();
        pageRect = new Rectangle(llx, lly, urx, ury);
        pageRect.normalize();
        page = item.getPage(0);
        int rotation = writer.reader.getPageRotation(page);
        Rectangle pageSize = writer.reader.getPageSizeWithRotation(page);
        switch (rotation) {
            case 90:
                pageRect = new Rectangle(pageRect.getBottom(), pageSize.getTop()
                        - pageRect.getLeft(), pageRect.getTop(), pageSize.getTop()
                        - pageRect.getRight());
                break;
            case 180:
                pageRect = new Rectangle(pageSize.getRight() - pageRect.getLeft(),
                        pageSize.getTop() - pageRect.getBottom(), pageSize.getRight()
                        - pageRect.getRight(), pageSize.getTop() - pageRect.getTop());
                break;
            case 270:
                pageRect = new Rectangle(pageSize.getRight() - pageRect.getBottom(),
                        pageRect.getLeft(), pageSize.getRight() - pageRect.getTop(),
                        pageRect.getRight());
                break;
        }
        if (rotation != 0) {
            pageRect.normalize();
        }
        rect = new Rectangle(this.pageRect.getWidth(), this.pageRect.getHeight());
    }

    /**
     * Gets a template layer to create a signature appearance. The layers can go from 0 to 4.
     * <p>
     * Consult <A HREF="http://partners.adobe.com/asn/developer/pdfs/tn/PPKAppearances.pdf" >PPKAppearances.pdf</A> for
     * further details.
     *
     * @param layer the layer
     * @return a template
     */
    public PdfTemplate getLayer(int layer) {
        if (layer < 0 || layer >= app.length) {
            return null;
        }
        PdfTemplate t = app[layer];
        if (t == null) {
            t = app[layer] = new PdfTemplate(writer);
            t.setBoundingBox(rect);
            writer.addDirectTemplateSimple(t, new PdfName("n" + layer));
        }
        return t;
    }

    /**
     * Gets the template that aggregates all appearance layers. This corresponds to the /FRM resource.
     * <p>
     * Consult <A HREF="http://partners.adobe.com/asn/developer/pdfs/tn/PPKAppearances.pdf" >PPKAppearances.pdf</A> for
     * further details.
     *
     * @return the template that aggregates all appearance layers
     */
    public PdfTemplate getTopLayer() {
        if (frm == null) {
            frm = new PdfTemplate(writer);
            frm.setBoundingBox(rect);
            writer.addDirectTemplateSimple(frm, new PdfName("FRM"));
        }
        return frm;
    }

    /**
     * Gets the main appearance layer.
     * <p>
     * Consult <A HREF="http://partners.adobe.com/asn/developer/pdfs/tn/PPKAppearances.pdf" >PPKAppearances.pdf</A> for
     * further details.
     *
     * @return the main appearance layer
     * @throws DocumentException on error
     */
    public PdfTemplate getAppearance() throws DocumentException {
        if (isInvisible()) {
            PdfTemplate t = new PdfTemplate(writer);
            t.setBoundingBox(new Rectangle(0, 0));
            writer.addDirectTemplateSimple(t, null);
            return t;
        }
        if (app[0] == null) {
            PdfTemplate t = app[0] = new PdfTemplate(writer);
            t.setBoundingBox(new Rectangle(100, 100));
            writer.addDirectTemplateSimple(t, new PdfName("n0"));
            t.setLiteral("% DSBlank\n");
        }
        if (app[1] == null && !acro6Layers) {
            PdfTemplate t = app[1] = new PdfTemplate(writer);
            t.setBoundingBox(new Rectangle(100, 100));
            writer.addDirectTemplateSimple(t, new PdfName("n1"));
            t.setLiteral(questionMark);
        }
        if (app[2] == null) {
            String text;
            if (layer2Text == null) {
                StringBuilder buf = new StringBuilder();
                buf.append("Digitally signed by ")
                        .append(PdfPKCS7.getSubjectFields((X509Certificate) certChain[0]).getField("CN"))
                        .append('\n');
                SimpleDateFormat sd = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss z");
                buf.append("Date: ").append(sd.format(getSignDateNullSafe().getTime()));
                if (reason != null) {
                    buf.append('\n').append("Reason: ").append(reason);
                }
                if (location != null) {
                    buf.append('\n').append("Location: ").append(location);
                }
                text = buf.toString();
            } else {
                text = layer2Text;
            }
            PdfTemplate t = app[2] = new PdfTemplate(writer);
            t.setBoundingBox(rect);
            writer.addDirectTemplateSimple(t, new PdfName("n2"));
            if (image != null) {
                if (imageScale == 0) {
                    t.addImage(image, rect.getWidth(), 0, 0, rect.getHeight(), 0, 0);
                } else {
                    float usableScale = imageScale;
                    if (imageScale < 0) {
                        usableScale = Math.min(rect.getWidth() / image.getWidth(),
                                rect.getHeight() / image.getHeight());
                    }
                    float w = image.getWidth() * usableScale;
                    float h = image.getHeight() * usableScale;
                    float x = (rect.getWidth() - w) / 2;
                    float y = (rect.getHeight() - h) / 2;
                    t.addImage(image, w, 0, 0, h, x, y);
                }
            }
            Font font;
            if (layer2Font == null) {
                font = new Font();
            } else {
                font = new Font(layer2Font);
            }
            float size = font.getSize();

            Rectangle dataRect = null;
            Rectangle signatureRect = null;

            if (render == SignatureRenderNameAndDescription
                || (render == SignatureRenderGraphicAndDescription && hasValidSignatureGraphic())) {
                // origin is the bottom-left
                signatureRect = new Rectangle(MARGIN, MARGIN, rect.getWidth() / 2
                        - MARGIN, rect.getHeight() - MARGIN);
                dataRect = new Rectangle(rect.getWidth() / 2 + MARGIN / 2, MARGIN,
                        rect.getWidth() - MARGIN / 2, rect.getHeight() - MARGIN);

                if (rect.getHeight() > rect.getWidth()) {
                    signatureRect = new Rectangle(MARGIN, rect.getHeight() / 2,
                            rect.getWidth() - MARGIN, rect.getHeight());
                    dataRect = new Rectangle(MARGIN, MARGIN, rect.getWidth() - MARGIN,
                            rect.getHeight() / 2 - MARGIN);
                }
            } else if (this.render == SignatureRenderGraphic) {
                if (this.signatureGraphic == null) {
                    throw new IllegalArgumentException("Missing signature image for renderingmode: " + this.render);
                }
                signatureRect = new Rectangle(
                        MARGIN,
                        MARGIN,
                        rect.getWidth() - MARGIN,
                        rect.getHeight() - MARGIN);
            } else {
                dataRect = new Rectangle(MARGIN, MARGIN, rect.getWidth() - MARGIN,
                        rect.getHeight() * (1 - TOP_SECTION) - MARGIN);
            }

            if (render == SignatureRenderNameAndDescription) {
                String signedBy = PdfPKCS7.getSubjectFields((X509Certificate) certChain[0]).getField("CN");
                Rectangle sr2 = new Rectangle(signatureRect.getWidth() - MARGIN,
                        signatureRect.getHeight() - MARGIN);
                float signedSize = fitText(font, signedBy, sr2, -1, runDirection);

                ColumnText ct2 = new ColumnText(t);
                ct2.setRunDirection(runDirection);
                ct2.setSimpleColumn(new Phrase(signedBy, font),
                        signatureRect.getLeft(), signatureRect.getBottom(),
                        signatureRect.getRight(), signatureRect.getTop(), signedSize,
                        Element.ALIGN_LEFT);

                ct2.go();
            } else if (render == SignatureRenderGraphicAndDescription) {
                renderSignatureGraphic(t, signatureRect);
            } else if (this.render == SignatureRenderGraphic) {
                renderSignatureGraphic(t, signatureRect);
            }

            if (this.render != SignatureRenderGraphic) {
                if (size <= 0) {
                    Rectangle sr = new Rectangle(dataRect.getWidth(), dataRect.getHeight());
                    size = fitText(font, text, sr, 12, runDirection);
                }
                ColumnText ct = new ColumnText(t);
                ct.setRunDirection(runDirection);
                ct.setSimpleColumn(new Phrase(text, font), dataRect.getLeft(), dataRect.getBottom(),
                        dataRect.getRight(), dataRect.getTop(), size, Element.ALIGN_LEFT);
                ct.go();
            }
        }
        if (app[3] == null && !acro6Layers) {
            PdfTemplate t = app[3] = new PdfTemplate(writer);
            t.setBoundingBox(new Rectangle(100, 100));
            writer.addDirectTemplateSimple(t, new PdfName("n3"));
            t.setLiteral("% DSBlank\n");
        }
        if (app[4] == null && !acro6Layers) {
            PdfTemplate t = app[4] = new PdfTemplate(writer);
            t.setBoundingBox(new Rectangle(0, rect.getHeight() * (1 - TOP_SECTION),
                    rect.getRight(), rect.getTop()));
            writer.addDirectTemplateSimple(t, new PdfName("n4"));
            Font font;
            if (layer2Font == null) {
                font = new Font();
            } else {
                font = new Font(layer2Font);
            }
            float size = font.getSize();
            String text = "Signature Not Verified";
            if (layer4Text != null) {
                text = layer4Text;
            }
            Rectangle sr = new Rectangle(rect.getWidth() - 2 * MARGIN,
                    rect.getHeight() * TOP_SECTION - 2 * MARGIN);
            size = fitText(font, text, sr, 15, runDirection);
            ColumnText ct = new ColumnText(t);
            ct.setRunDirection(runDirection);
            ct.setSimpleColumn(new Phrase(text, font), MARGIN, 0, rect.getWidth()
                    - MARGIN, rect.getHeight() - MARGIN, size, Element.ALIGN_LEFT);
            ct.go();
        }
        int rotation = writer.reader.getPageRotation(page);
        Rectangle rotated = new Rectangle(rect);
        int n = rotation;
        while (n > 0) {
            rotated = rotated.rotate();
            n -= 90;
        }
        if (frm == null) {
            frm = new PdfTemplate(writer);
            frm.setBoundingBox(rotated);
            writer.addDirectTemplateSimple(frm, new PdfName("FRM"));
            float scale = Math.min(rect.getWidth(), rect.getHeight()) * 0.9f;
            float x = (rect.getWidth() - scale) / 2;
            float y = (rect.getHeight() - scale) / 2;
            scale /= 100;
            if (rotation == 90) {
                frm.concatCTM(0, 1, -1, 0, rect.getHeight(), 0);
            } else if (rotation == 180) {
                frm.concatCTM(-1, 0, 0, -1, rect.getWidth(), rect.getHeight());
            } else if (rotation == 270) {
                frm.concatCTM(0, -1, 1, 0, 0, rect.getWidth());
            }
            frm.addTemplate(app[0], 0, 0);
            if (!acro6Layers) {
                frm.addTemplate(app[1], scale, 0, 0, scale, x, y);
            }
            frm.addTemplate(app[2], 0, 0);
            if (!acro6Layers) {
                frm.addTemplate(app[3], scale, 0, 0, scale, x, y);
                frm.addTemplate(app[4], 0, 0);
            }
        }
        PdfTemplate napp = new PdfTemplate(writer);
        napp.setBoundingBox(rotated);
        writer.addDirectTemplateSimple(napp, null);
        napp.addTemplate(frm, 0, 0);
        return napp;
    }

    /**
     * A helper to render signature graphic (PDF or image) to a give new layer
     *
     * @param t the new PDF object/layer to render on
     */
    private void renderSignatureGraphic(PdfTemplate t, Rectangle signatureRect) {
        if (this.signatureGraphic != null) {
            ColumnText ct2 = new ColumnText(t);
            ct2.setRunDirection(this.runDirection);
            ct2.setSimpleColumn(signatureRect.getLeft(), signatureRect.getBottom(), signatureRect.getRight(),
                    signatureRect.getTop(), 0, Element.ALIGN_RIGHT);

            Image im = Image.getInstance(this.signatureGraphic);
            im.scaleToFit(signatureRect.getWidth(), signatureRect.getHeight());

            Paragraph p = new Paragraph(signatureRect.getHeight());
            // must calculate the point to draw from, to make image appear in the middle of the column
            float x = (signatureRect.getWidth() - im.getScaledWidth()) / 2f;
            float y = (signatureRect.getHeight() - im.getScaledHeight()) / 2f;

            p.add(new Chunk(im, x, y, false));

            ct2.addElement(p);
            ct2.go();
        } else if (this.signaturePDF != null) {
            float scale = Math.min(signatureRect.getWidth() / signaturePDF.getWidth(),
                    signatureRect.getHeight() / signaturePDF.getHeight());
            t.addTemplate(signaturePDF, scale, 0, 0, scale, 0, 0);
        }
    }


    /**
     * Sets the digest/signature to an external calculated value.
     *
     * @param digest                    the digest. This is the actual signature
     * @param RSAdata                   the extra data that goes into the data tag in PKCS#7
     * @param digestEncryptionAlgorithm the encryption algorithm. It may must be <CODE>null</CODE> if the
     *                                  <CODE>digest</CODE> is also <CODE>null</CODE>. If the
     *                                  <CODE>digest</CODE> is not <CODE>null</CODE> then it may be "RSA"
     *                                  or "DSA"
     */
    public void setExternalDigest(byte[] digest, byte[] RSAdata,
            String digestEncryptionAlgorithm) {
        externalDigest = digest;
        externalRSAdata = RSAdata;
        this.digestEncryptionAlgorithm = digestEncryptionAlgorithm;
    }

    /**
     * Gets the signing reason or null if not set.
     *
     * @return the signing reason
     */
    public String getReason() {
        return this.reason;
    }

    /**
     * Sets the signing reason.
     *
     * @param reason the signing reason
     */
    public void setReason(String reason) {
        this.reason = reason;
    }

    /**
     * Gets the signing location or null if not set.
     *
     * @return the signing location
     */
    public String getLocation() {
        return this.location;
    }

    /**
     * Sets the signing location.
     *
     * @param location the signing location
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Returns the Cryptographic Service Provider that will sign the document. This method might return null if the
     * provider was not set.
     *
     * @return provider the name of the provider, for example "SUN", or
     * <code>null</code> to use the default provider.
     */
    public String getProvider() {
        return this.provider;
    }

    /**
     * Sets the Cryptographic Service Provider that will sign the document.
     *
     * @param provider the name of the provider, for example "SUN", or <code>null</code> to use the default provider.
     */
    public void setProvider(String provider) {
        this.provider = provider;
    }

    /**
     * Gets the private key.
     *
     * @return the private key
     */
    public java.security.PrivateKey getPrivKey() {
        return privKey;
    }

    /**
     * Gets the certificate revocation list.
     *
     * @return the certificate revocation list
     */
    public java.security.cert.CRL[] getCrlList() {
        return this.crlList;
    }

    /**
     * Gets the filter used to sign the document.
     *
     * @return the filter used to sign the document
     */
    public com.lowagie.text.pdf.PdfName getFilter() {
        return filter;
    }

    /**
     * Checks if a new field was created.
     *
     * @return <CODE>true</CODE> if a new field was created, <CODE>false</CODE> if
     * signing an existing field or if the signature is invisible
     */
    public boolean isNewField() {
        return this.newField;
    }

    /**
     * Gets the page number of the field.
     *
     * @return the page number of the field
     */
    public int getPage() {
        return page;
    }

    /**
     * Gets the field name.
     *
     * @return the field name
     */
    public java.lang.String getFieldName() {
        return fieldName;
    }

    /**
     * Sets the field name for a new invisible signature field
     *
     * @param fieldName for the new invisible signature field
     */
    public void setFieldNameForInvisibleSignatures(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * Gets the rectangle that represent the position and dimension of the signature in the page.
     *
     * @return the rectangle that represent the position and dimension of the signature in the page
     */
    public com.lowagie.text.Rectangle getPageRect() {
        return pageRect;
    }

    /**
     * Gets the signature date.
     *
     * @return the signature date
     */
    public java.util.Calendar getSignDate() {
        return signDate;
    }

    /**
     * Sets the signature date.
     *
     * @param signDate the signature date
     */
    public void setSignDate(java.util.Calendar signDate) {
        this.signDate = signDate;
    }

    /**
     * Gets the signature date. If the date is not set the current Date is returned.
     *
     * @return the signature date
     */
    public java.util.Calendar getSignDateNullSafe() {
        if (this.signDate == null) {
            return new GregorianCalendar();
        }
        return this.signDate;
    }

    com.lowagie.text.pdf.ByteBuffer getSigout() {
        return sigout;
    }

    void setSigout(com.lowagie.text.pdf.ByteBuffer sigout) {
        this.sigout = sigout;
    }

    java.io.OutputStream getOriginalout() {
        return originalout;
    }

    void setOriginalout(java.io.OutputStream originalout) {
        this.originalout = originalout;
    }

    /**
     * Gets the temporary file.
     *
     * @return the temporary file or <CODE>null</CODE> is the document is created in memory
     */
    public java.io.File getTempFile() {
        return tempFile;
    }

    void setTempFile(java.io.File tempFile) {
        this.tempFile = tempFile;
    }

    /**
     * Gets a new signature fied name that doesn't clash with any existing name.
     *
     * @return a new signature fied name
     */
    public final String getNewSigName() {
        AcroFields af = writer.getAcroFields();
        String name = "Signature";
        int step = 0;
        boolean found = false;
        while (!found) {
            ++step;
            String n1 = name + step;
            if (af.getFieldItem(n1) != null) {
                continue;
            }
            n1 += ".";
            found = true;
            for (String fn : af.getAllFields().keySet()) {
                if (fn.startsWith(n1)) {
                    found = false;
                    break;
                }
            }
        }
        name += step;
        return name;
    }

    /**
     * This is the first method to be called when using external signatures. The general sequence is: preClose(),
     * getDocumentBytes() and close().
     * <p>
     * If calling preClose() <B>dont't</B> call PdfStamper.close().
     * <p>
     * No external signatures are allowed if this method is called.
     *
     * @throws IOException       on error
     * @throws DocumentException on error
     */
    public void preClose() throws IOException, DocumentException {
        preClose(null);
    }

    /**
     * This is the first method to be called when using external signatures. The general sequence is: preClose(),
     * getDocumentBytes() and close().
     * <p>
     * If calling preClose() <B>dont't</B> call PdfStamper.close().
     * <p>
     * If using an external signature <CODE>exclusionSizes</CODE> must contain at least the
     * <CODE>PdfName.CONTENTS</CODE> key with the size that it will take in the document. Note that due to the hex
     * string coding this size should be byte_size*2+2.
     *
     * @param exclusionSizes a <CODE>HashMap</CODE> with names and sizes to be excluded in the signature calculation.
     *                       The key is a <CODE>PdfName</CODE> and the value an <CODE>Integer</CODE>. At least the
     *                       <CODE>PdfName.CONTENTS</CODE> must be present
     * @throws IOException       on error
     * @throws DocumentException on error
     */
    public void preClose(Map<PdfName, Integer> exclusionSizes) throws IOException,
            DocumentException {
        if (preClosed) {
            throw new DocumentException(
                    MessageLocalization.getComposedMessage("document.already.pre.closed"));
        }
        preClosed = true;
        AcroFields af = writer.getAcroFields();
        String name = getFieldName();
        boolean fieldExists = !(isInvisible() || isNewField());
        PdfIndirectReference refSig = writer.getPdfIndirectReference();
        writer.setSigFlags(3);
        if (fieldExists) {
            //Patch by Lonzak: the signature dictionary must be added to the formfield and no the widget! (testdoc: SignatureWidgetFormfield-Separate.pdf)
            PdfDictionary data = af.getFieldItem(name).getValue(0);
            writer.markUsed(data);
            data.put(PdfName.V, refSig);
            //for widget attributes
            PdfDictionary widget = af.getFieldItem(name).getWidget(0);
            writer.markUsed(widget);
            widget.put(PdfName.P, writer.getPageReference(getPage()));
            PdfObject obj = PdfReader.getPdfObjectRelease(widget.get(PdfName.F));
            int flags = 0;
            if (obj != null && obj.isNumber()) {
                flags = ((PdfNumber) obj).intValue();
            }
            flags |= PdfAnnotation.FLAGS_LOCKED;
            widget.put(PdfName.F, new PdfNumber(flags));
            PdfDictionary ap = new PdfDictionary();
            ap.put(PdfName.N, getAppearance().getIndirectReference());
            widget.put(PdfName.AP, ap);
        } else {
            PdfFormField sigField = PdfFormField.createSignature(writer);
            sigField.setFieldName(name);
            sigField.put(PdfName.V, refSig);
            sigField.setFlags(PdfAnnotation.FLAGS_PRINT | PdfAnnotation.FLAGS_LOCKED);

            int pagen = getPage();
            // OJO... Modificacion de
            // flopez-----------------------------------------------------
            // if (!isInvisible())
            // sigField.setWidget(getPageRect(), null);
            // else
            // sigField.setWidget(new Rectangle(0, 0), null);
            if ((!isInvisible()) && (pagen == 0)) { // Si pagina en cero tonces firma
                // en todas las paginas
                int pages = writer.reader.getNumberOfPages();
                for (int i = 1; i <= pages; i++) {
                    PdfFormField field = PdfFormField.createEmpty(writer);
                    this.page = i;
                    pagen = i;
                    field.setWidget(getPageRect(), null);
                    field.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, getAppearance());
                    field.setPlaceInPage(i);
                    field.setPage(i);
                    field.setFlags(PdfAnnotation.FLAGS_PRINT);
                    sigField.addKid(field);
                    field = null;
                }
            } else if (!isInvisible()) {
                // Si es una pagina especifica
                sigField.setWidget(getPageRect(), null);
            } else {
                sigField.setWidget(new Rectangle(0, 0), null);
            }
            // ******************************************************************************
            sigField.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, getAppearance());
            sigField.setPage(pagen);
            writer.addAnnotation(sigField, pagen);
        }

        exclusionLocations = new HashMap<>();
        if (cryptoDictionary == null) {
            if (PdfName.ADOBE_PPKLITE.equals(getFilter())) {
                sigStandard = new PdfSigGenericPKCS.PPKLite(getProvider());
            } else if (PdfName.ADOBE_PPKMS.equals(getFilter())) {
                sigStandard = new PdfSigGenericPKCS.PPKMS(getProvider());
            } else if (PdfName.VERISIGN_PPKVS.equals(getFilter())) {
                sigStandard = new PdfSigGenericPKCS.VeriSign(getProvider());
            } else {
                throw new IllegalArgumentException(
                        MessageLocalization.getComposedMessage("unknown.filter.1",
                                getFilter()));
            }
            sigStandard.setExternalDigest(externalDigest, externalRSAdata,
                    digestEncryptionAlgorithm);
            if (getReason() != null) {
                sigStandard.setReason(getReason());
            }
            if (getLocation() != null) {
                sigStandard.setLocation(getLocation());
            }
            if (getContact() != null) {
                sigStandard.setContact(getContact());
            }
            sigStandard.put(PdfName.M, new PdfDate(getSignDateNullSafe()));
            sigStandard.setSignInfo(getPrivKey(), certChain, crlList);
            // ******************************************************************************
            PdfString contents = (PdfString) sigStandard.get(PdfName.CONTENTS);
            PdfLiteral lit = new PdfLiteral(
                    (contents.toString().length() + (PdfName.ADOBE_PPKLITE
                            .equals(getFilter()) ? 0 : 64)) * 2 + 2);
            exclusionLocations.put(PdfName.CONTENTS, lit);
            sigStandard.put(PdfName.CONTENTS, lit);
            lit = new PdfLiteral(80);
            exclusionLocations.put(PdfName.BYTERANGE, lit);
            sigStandard.put(PdfName.BYTERANGE, lit);
            if (this.certificationLevel >= 0) {
                addDocMDP(sigStandard);
            }
            if (signatureEvent != null) {
                signatureEvent.getSignatureDictionary(sigStandard);
            }
            writer.addToBody(sigStandard, refSig, false);
        } else {
            //the following block was added since otherwise this information would be lost.
            //The original idea might have been that if there is an external crypto dictionary then everything is added manually however
            //the method description of all the methods in the PdfSignatureAppearance class do not state this - so this would be highly error prone
            if (getReason() != null) {
                this.cryptoDictionary.put(PdfName.REASON, new PdfString(this.getReason(), PdfObject.TEXT_UNICODE));
            }
            if (getLocation() != null) {
                this.cryptoDictionary.put(PdfName.LOCATION, new PdfString(this.getLocation(), PdfObject.TEXT_UNICODE));
            }
            if (getContact() != null) {
                this.cryptoDictionary.put(PdfName.CONTACTINFO,
                        new PdfString(this.getContact(), PdfObject.TEXT_UNICODE));
            }
            if (this.getSignDate() != null) {
                this.cryptoDictionary.put(PdfName.M, new PdfDate(getSignDate()));
            }

            PdfLiteral lit = new PdfLiteral(80);
            exclusionLocations.put(PdfName.BYTERANGE, lit);
            cryptoDictionary.put(PdfName.BYTERANGE, lit);
            for (Object o : exclusionSizes.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                PdfName key = (PdfName) entry.getKey();
                Integer v = (Integer) entry.getValue();
                lit = new PdfLiteral(v);
                exclusionLocations.put(key, lit);
                cryptoDictionary.put(key, lit);
            }
            if (certificationLevel >= 0) {
                addDocMDP(cryptoDictionary);
            }
            if (signatureEvent != null) {
                signatureEvent.getSignatureDictionary(cryptoDictionary);
            }
            writer.addToBody(cryptoDictionary, refSig, false);
        }
        if (certificationLevel >= 0) {
            // add DocMDP entry to root
            PdfDictionary docmdp = new PdfDictionary();
            docmdp.put(new PdfName("DocMDP"), refSig);
            writer.reader.getCatalog().put(new PdfName("Perms"), docmdp);
        }
        writer.close(stamper.getInfoDictionary());

        range = new long[exclusionLocations.size() * 2];
        long byteRangePosition = exclusionLocations
                .get(PdfName.BYTERANGE).getPosition();
        exclusionLocations.remove(PdfName.BYTERANGE);
        int idx = 1;
        for (Object o : exclusionLocations.values()) {
            PdfLiteral lit = (PdfLiteral) o;
            long n = lit.getPosition();
            range[idx++] = n;
            range[idx++] = lit.getPosLength() + n;
        }
        Arrays.sort(range, 1, range.length - 1);
        for (int k = 3; k < range.length - 2; k += 2) {
            range[k] -= range[k - 1];
        }

        if (tempFile == null) {
            bout = sigout.getBuffer();
            boutLen = sigout.size();
            range[range.length - 1] = boutLen - range[range.length - 2];
            ByteBuffer bf = new ByteBuffer();
            bf.append('[');
            for (long i : range) {
                bf.append(i).append(' ');
            }
            bf.append(']');
            System.arraycopy(bf.getBuffer(), 0, bout, (int) byteRangePosition, bf.size());
        } else {
            try {
                raf = new RandomAccessFile(tempFile, "rw");
                long boutL = raf.length();
                range[range.length - 1] = boutL - range[range.length - 2];
                ByteBuffer bf = new ByteBuffer();
                bf.append('[');
                for (long i : range) {
                    bf.append(i).append(' ');
                }
                bf.append(']');
                raf.seek(byteRangePosition);
                raf.write(bf.getBuffer(), 0, bf.size());
            } catch (IOException e) {
                try {
                    raf.close();
                } catch (Exception ee) {
                }
                try {
                    tempFile.delete();
                } catch (Exception ee) {
                }
                throw e;
            }
        }
    }

    /**
     * This is the last method to be called when using external signatures. The general sequence is: preClose(),
     * getDocumentBytes() and close().
     * <p>
     * <CODE>update</CODE> is a <CODE>PdfDictionary</CODE> that must have exactly
     * the same keys as the ones provided in {@link PdfSignatureAppearance#preClose(Map)}.
     *
     * @param update a <CODE>PdfDictionary</CODE> with the key/value that will fill the holes defined in
     *               {@link PdfSignatureAppearance#preClose(Map)}
     * @throws DocumentException on error
     * @throws IOException       on error
     */
    public void close(PdfDictionary update) throws IOException, DocumentException {
        try {
            if (!preClosed) {
                throw new DocumentException(
                        MessageLocalization
                                .getComposedMessage("preclose.must.be.called.first"));
            }
            ByteBuffer bf = new ByteBuffer();
            for (PdfName key : update.getKeys()) {
                PdfObject obj = update.get(key);
                PdfLiteral lit = exclusionLocations.get(key);
                if (lit == null) {
                    throw new IllegalArgumentException(
                            MessageLocalization.getComposedMessage(
                                    "the.key.1.didn.t.reserve.space.in.preclose", key.toString()));
                }
                bf.reset();
                obj.toPdf(null, bf);
                if (bf.size() > lit.getPosLength()) {
                    throw new IllegalArgumentException(
                            MessageLocalization.getComposedMessage(
                                    "the.key.1.is.too.big.is.2.reserved.3", key.toString(),
                                    String.valueOf(bf.size()), String.valueOf(lit.getPosLength())));
                }
                if (tempFile == null) {
                    System.arraycopy(bf.getBuffer(), 0, bout, (int) lit.getPosition(),
                            bf.size());
                } else {
                    raf.seek(lit.getPosition());
                    raf.write(bf.getBuffer(), 0, bf.size());
                }
            }
            if (update.size() != exclusionLocations.size()) {
                throw new IllegalArgumentException(
                        MessageLocalization
                                .getComposedMessage("the.update.dictionary.has.less.keys.than.required"));
            }
            if (tempFile == null) {
                originalout.write(bout, 0, boutLen);
            } else {
                if (originalout != null) {
                    raf.seek(0);
                    long length = raf.length();
                    byte[] buf = new byte[8192];
                    while (length > 0) {
                        int r = raf.read(buf, 0, (int) Math.min(buf.length, length));
                        if (r < 0) {
                            throw new EOFException(
                                    MessageLocalization.getComposedMessage("unexpected.eof"));
                        }
                        originalout.write(buf, 0, r);
                        length -= r;
                    }
                }
            }
        } finally {
            if (tempFile != null) {
                try {
                    raf.close();
                } catch (Exception ee) {
                }
                if (originalout != null) {
                    try {
                        tempFile.delete();
                    } catch (Exception ee) {
                    }
                }
            }
            if (originalout != null) {
                try {
                    originalout.close();
                } catch (Exception e) {
                }
            }
        }
    }

    private void addDocMDP(PdfDictionary crypto) {
        PdfDictionary reference = new PdfDictionary();
        PdfDictionary transformParams = new PdfDictionary();
        transformParams.put(PdfName.P, new PdfNumber(certificationLevel));
        transformParams.put(PdfName.V, new PdfName("1.2"));
        transformParams.put(PdfName.TYPE, PdfName.TRANSFORMPARAMS);
        reference.put(PdfName.TRANSFORMMETHOD, PdfName.DOCMDP);
        reference.put(PdfName.TYPE, PdfName.SIGREF);
        reference.put(PdfName.TRANSFORMPARAMS, transformParams);
        reference.put(new PdfName("DigestValue"), new PdfString("aa"));
        PdfArray loc = new PdfArray();
        loc.add(new PdfNumber(0));
        loc.add(new PdfNumber(0));
        reference.put(new PdfName("DigestLocation"), loc);
        reference.put(new PdfName("DigestMethod"), new PdfName("MD5"));
        reference.put(PdfName.DATA, writer.reader.getTrailer().get(PdfName.ROOT));
        PdfArray types = new PdfArray();
        types.add(reference);
        crypto.put(PdfName.REFERENCE, types);
    }

    /**
     * Gets the document bytes that are hashable when using external signatures. The general sequence is: preClose(),
     * getRangeStream() and close().
     * <p>
     *
     * @return the document bytes that are hashable
     */
    public InputStream getRangeStream() {
        return new PdfSignatureAppearance.RangeStream(raf, bout, range);
    }

    /**
     * Gets the user made signature dictionary. This is the dictionary at the /V key.
     *
     * @return the user made signature dictionary
     */
    public com.lowagie.text.pdf.PdfDictionary getCryptoDictionary() {
        return cryptoDictionary;
    }

    /**
     * Sets a user made signature dictionary. This is the dictionary at the /V key.
     *
     * @param cryptoDictionary a user made signature dictionary
     */
    public void setCryptoDictionary(
            com.lowagie.text.pdf.PdfDictionary cryptoDictionary) {
        this.cryptoDictionary = cryptoDictionary;
    }

    /**
     * Gets the <CODE>PdfStamper</CODE> associated with this instance.
     *
     * @return the <CODE>PdfStamper</CODE> associated with this instance
     */
    public com.lowagie.text.pdf.PdfStamper getStamper() {
        return stamper;
    }

    void setStamper(com.lowagie.text.pdf.PdfStamper stamper) {
        this.stamper = stamper;
    }

    /**
     * Checks if the document is in the process of closing.
     *
     * @return <CODE>true</CODE> if the document is in the process of closing,
     * <CODE>false</CODE> otherwise
     */
    public boolean isPreClosed() {
        return preClosed;
    }

    /**
     * Gets the instance of the standard signature dictionary. This instance is only available after pre close.
     * <p>
     * The main use is to insert external signatures.
     *
     * @return the instance of the standard signature dictionary
     */
    public com.lowagie.text.pdf.PdfSigGenericPKCS getSigStandard() {
        return sigStandard;
    }

    /**
     * Gets the signing contact.
     *
     * @return the signing contact
     */
    public String getContact() {
        return this.contact;
    }

    /**
     * Sets the signing contact.
     *
     * @param contact the signing contact
     */
    public void setContact(String contact) {
        this.contact = contact;
    }

    /**
     * Gets the n2 and n4 layer font.
     *
     * @return the n2 and n4 layer font
     */
    public Font getLayer2Font() {
        return this.layer2Font;
    }

    /**
     * Sets the n2 and n4 layer font. If the font size is zero, auto-fit will be used.
     *
     * @param layer2Font the n2 and n4 font
     */
    public void setLayer2Font(Font layer2Font) {
        this.layer2Font = layer2Font;
    }

    /**
     * Gets the Acrobat 6.0 layer mode.
     *
     * @return the Acrobat 6.0 layer mode
     */
    public boolean isAcro6Layers() {
        return this.acro6Layers;
    }

    /**
     * Acrobat 6.0 and higher recommends that only layer n2 and n4 be present. This method sets that mode.
     *
     * @param acro6Layers if <code>true</code> only the layers n2 and n4 will be present
     */
    public void setAcro6Layers(boolean acro6Layers) {
        this.acro6Layers = acro6Layers;
    }

    /**
     * Gets the run direction.
     *
     * @return the run direction
     */
    public int getRunDirection() {
        return runDirection;
    }

    /**
     * Sets the run direction in the n2 and n4 layer.
     *
     * @param runDirection the run direction
     */
    public void setRunDirection(int runDirection) {
        if (runDirection < PdfWriter.RUN_DIRECTION_DEFAULT
                || runDirection > PdfWriter.RUN_DIRECTION_RTL) {
            throw new RuntimeException(MessageLocalization.getComposedMessage(
                    "invalid.run.direction.1", runDirection));
        }
        this.runDirection = runDirection;
    }

    /**
     * Getter for property signatureEvent.
     *
     * @return Value of property signatureEvent.
     */
    public SignatureEvent getSignatureEvent() {
        return this.signatureEvent;
    }

    /**
     * Sets the signature event to allow modification of the signature dictionary.
     *
     * @param signatureEvent the signature event
     */
    public void setSignatureEvent(SignatureEvent signatureEvent) {
        this.signatureEvent = signatureEvent;
    }

    /**
     * Gets the background image for the layer 2.
     *
     * @return the background image for the layer 2
     */
    public Image getImage() {
        return this.image;
    }

    /**
     * Sets the background image for the layer 2.
     *
     * @param image the background image for the layer 2
     */
    public void setImage(Image image) {
        this.image = image;
    }

    /**
     * Gets the scaling to be applied to the background image.
     *
     * @return the scaling to be applied to the background image
     */
    public float getImageScale() {
        return this.imageScale;
    }

    /**
     * Sets the scaling to be applied to the background image. If it's zero the image will fully fill the rectangle. If
     * it's less than zero the image will fill the rectangle but will keep the proportions. If it's greater than zero
     * that scaling will be applied. In any of the cases the image will always be centered. It's zero by default.
     *
     * @param imageScale the scaling to be applied to the background image
     */
    public void setImageScale(float imageScale) {
        this.imageScale = imageScale;
    }

    /**
     * Gets the certified status of this document.
     *
     * @return the certified status
     */
    public int getCertificationLevel() {
        return this.certificationLevel;
    }

    /**
     * Sets the document type to certified instead of simply signed.
     *
     * @param certificationLevel the values can be: <code>NOT_CERTIFIED</code>,
     *                           <code>CERTIFIED_NO_CHANGES_ALLOWED</code>,
     *                           <code>CERTIFIED_FORM_FILLING</code> and
     *                           <code>CERTIFIED_FORM_FILLING_AND_ANNOTATIONS</code>
     */
    public void setCertificationLevel(int certificationLevel) {
        this.certificationLevel = certificationLevel;
    }

    /**
     * An interface to retrieve the signature dictionary for modification.
     */
    public interface SignatureEvent {

        /**
         * Allows modification of the signature dictionary.
         *
         * @param sig the signature dictionary
         */
        void getSignatureDictionary(PdfDictionary sig);
    }

    /**
     *
     */
    private static class RangeStream extends InputStream {

        private final byte[] b = new byte[1];
        private final RandomAccessFile raf;
        private final byte[] bout;
        private final long[] range;
        private long rangePosition = 0;

        private RangeStream(RandomAccessFile raf, byte[] bout, long[] range) {
            this.raf = raf;
            this.bout = bout;
            this.range = range;
        }

        /**
         * @see java.io.InputStream#read()
         */
        @Override
        public int read() throws IOException {
            int n = read(b);
            if (n != 1) {
                return -1;
            }
            return b[0] & 0xff;
        }

        /**
         * @see java.io.InputStream#read(byte[], int, int)
         */
        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            if (b == null) {
                throw new NullPointerException();
            } else if ((off < 0) || (off > b.length) || (len < 0)
                    || ((off + len) > b.length) || ((off + len) < 0)) {
                throw new IndexOutOfBoundsException();
            } else if (len == 0) {
                return 0;
            }
            if (rangePosition >= range[range.length - 2] + range[range.length - 1]) {
                return -1;
            }
            for (int k = 0; k < range.length; k += 2) {
                long start = range[k];
                long end = start;
                if (range.length > k + 1) {
                    end = start + range[k + 1];
                }
                if (rangePosition < start) {
                    rangePosition = start;
                }
                if (rangePosition < end) {
                    int lenf = (int) Math.min(len, end - rangePosition);
                    if (raf == null) {
                        System.arraycopy(bout, (int) rangePosition, b, off, lenf);
                    } else {
                        raf.seek(rangePosition);
                        raf.readFully(b, off, lenf);
                    }
                    rangePosition += lenf;
                    return lenf;
                }
            }
            return -1;
        }
    }
}