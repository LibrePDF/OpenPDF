/*
 * $Id: MetaDo.java 4065 2009-09-16 23:09:11Z psoares33 $
 * $Name$
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
 * http://www.lowagie.com/iText/
 */

package com.lowagie.text.pdf.codec.wmf;
import java.awt.Color;
import java.awt.Point;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import com.lowagie.text.error_messages.MessageLocalization;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.codec.BmpImage;

public class MetaDo {
    
    public static final int META_SETBKCOLOR            = 0x0201;
    public static final int META_SETBKMODE             = 0x0102;
    public static final int META_SETMAPMODE            = 0x0103;
    public static final int META_SETROP2               = 0x0104;
    public static final int META_SETRELABS             = 0x0105;
    public static final int META_SETPOLYFILLMODE       = 0x0106;
    public static final int META_SETSTRETCHBLTMODE     = 0x0107;
    public static final int META_SETTEXTCHAREXTRA      = 0x0108;
    public static final int META_SETTEXTCOLOR          = 0x0209;
    public static final int META_SETTEXTJUSTIFICATION  = 0x020A;
    public static final int META_SETWINDOWORG          = 0x020B;
    public static final int META_SETWINDOWEXT          = 0x020C;
    public static final int META_SETVIEWPORTORG        = 0x020D;
    public static final int META_SETVIEWPORTEXT        = 0x020E;
    public static final int META_OFFSETWINDOWORG       = 0x020F;
    public static final int META_SCALEWINDOWEXT        = 0x0410;
    public static final int META_OFFSETVIEWPORTORG     = 0x0211;
    public static final int META_SCALEVIEWPORTEXT      = 0x0412;
    public static final int META_LINETO                = 0x0213;
    public static final int META_MOVETO                = 0x0214;
    public static final int META_EXCLUDECLIPRECT       = 0x0415;
    public static final int META_INTERSECTCLIPRECT     = 0x0416;
    public static final int META_ARC                   = 0x0817;
    public static final int META_ELLIPSE               = 0x0418;
    public static final int META_FLOODFILL             = 0x0419;
    public static final int META_PIE                   = 0x081A;
    public static final int META_RECTANGLE             = 0x041B;
    public static final int META_ROUNDRECT             = 0x061C;
    public static final int META_PATBLT                = 0x061D;
    public static final int META_SAVEDC                = 0x001E;
    public static final int META_SETPIXEL              = 0x041F;
    public static final int META_OFFSETCLIPRGN         = 0x0220;
    public static final int META_TEXTOUT               = 0x0521;
    public static final int META_BITBLT                = 0x0922;
    public static final int META_STRETCHBLT            = 0x0B23;
    public static final int META_POLYGON               = 0x0324;
    public static final int META_POLYLINE              = 0x0325;
    public static final int META_ESCAPE                = 0x0626;
    public static final int META_RESTOREDC             = 0x0127;
    public static final int META_FILLREGION            = 0x0228;
    public static final int META_FRAMEREGION           = 0x0429;
    public static final int META_INVERTREGION          = 0x012A;
    public static final int META_PAINTREGION           = 0x012B;
    public static final int META_SELECTCLIPREGION      = 0x012C;
    public static final int META_SELECTOBJECT          = 0x012D;
    public static final int META_SETTEXTALIGN          = 0x012E;
    public static final int META_CHORD                 = 0x0830;
    public static final int META_SETMAPPERFLAGS        = 0x0231;
    public static final int META_EXTTEXTOUT            = 0x0a32;
    public static final int META_SETDIBTODEV           = 0x0d33;
    public static final int META_SELECTPALETTE         = 0x0234;
    public static final int META_REALIZEPALETTE        = 0x0035;
    public static final int META_ANIMATEPALETTE        = 0x0436;
    public static final int META_SETPALENTRIES         = 0x0037;
    public static final int META_POLYPOLYGON           = 0x0538;
    public static final int META_RESIZEPALETTE         = 0x0139;
    public static final int META_DIBBITBLT             = 0x0940;
    public static final int META_DIBSTRETCHBLT         = 0x0b41;
    public static final int META_DIBCREATEPATTERNBRUSH = 0x0142;
    public static final int META_STRETCHDIB            = 0x0f43;
    public static final int META_EXTFLOODFILL          = 0x0548;
    public static final int META_DELETEOBJECT          = 0x01f0;
    public static final int META_CREATEPALETTE         = 0x00f7;
    public static final int META_CREATEPATTERNBRUSH    = 0x01F9;
    public static final int META_CREATEPENINDIRECT     = 0x02FA;
    public static final int META_CREATEFONTINDIRECT    = 0x02FB;
    public static final int META_CREATEBRUSHINDIRECT   = 0x02FC;
    public static final int META_CREATEREGION          = 0x06FF;

    public PdfContentByte cb;
    public InputMeta in;
    int left;
    int top;
    int right;
    int bottom;
    int inch;
    MetaState state = new MetaState();

    public MetaDo(InputStream in, PdfContentByte cb) {
        this.cb = cb;
        this.in = new InputMeta(in);
    }
    
    public void readAll() throws IOException, DocumentException{
        if (in.readInt() != 0x9AC6CDD7) {
            throw new DocumentException(MessageLocalization.getComposedMessage("not.a.placeable.windows.metafile"));
        }
        in.readWord();
        left = in.readShort();
        top = in.readShort();
        right = in.readShort();
        bottom = in.readShort();
        inch = in.readWord();
        state.setScalingX((float)(right - left) / (float)inch * 72f);
        state.setScalingY((float)(bottom - top) / (float)inch * 72f);
        state.setOffsetWx(left);
        state.setOffsetWy(top);
        state.setExtentWx(right - left);
        state.setExtentWy(bottom - top);
        in.readInt();
        in.readWord();
        in.skip(18);
        
        int tsize;
        int function;
        cb.setLineCap(1);
        cb.setLineJoin(1);
        for (;;) {
            int lenMarker = in.getLength();
            tsize = in.readInt();
            if (tsize < 3)
                break;
            function = in.readWord();
            switch (function) {
                case 0:
                    break;
                case META_CREATEPALETTE:
                case META_CREATEREGION:
                case META_DIBCREATEPATTERNBRUSH:
                    state.addMetaObject(new MetaObject());
                    break;
                case META_CREATEPENINDIRECT:
                {
                    MetaPen pen = new MetaPen();
                    pen.init(in);
                    state.addMetaObject(pen);
                    break;
                }
                case META_CREATEBRUSHINDIRECT:
                {
                    MetaBrush brush = new MetaBrush();
                    brush.init(in);
                    state.addMetaObject(brush);
                    break;
                }
                case META_CREATEFONTINDIRECT:
                {
                    MetaFont font = new MetaFont();
                    font.init(in);
                    state.addMetaObject(font);
                    break;
                }
                case META_SELECTOBJECT:
                {
                    int idx = in.readWord();
                    state.selectMetaObject(idx, cb);
                    break;
                }
                case META_DELETEOBJECT:
                {
                    int idx = in.readWord();
                    state.deleteMetaObject(idx);
                    break;
                }
                case META_SAVEDC:
                    state.saveState(cb);
                    break;
                case META_RESTOREDC:
                {
                    int idx = in.readShort();
                    state.restoreState(idx, cb);
                    break;
                }
                case META_SETWINDOWORG:
                    state.setOffsetWy(in.readShort());
                    state.setOffsetWx(in.readShort());
                    break;
                case META_SETWINDOWEXT:
                    state.setExtentWy(in.readShort());
                    state.setExtentWx(in.readShort());
                    break;
                case META_MOVETO:
                {
                    int y = in.readShort();
                    Point p = new Point(in.readShort(), y);
                    state.setCurrentPoint(p);
                    break;
                }
                case META_LINETO:
                {
                    int y = in.readShort();
                    int x = in.readShort();
                    Point p = state.getCurrentPoint();
                    cb.moveTo(state.transformX(p.x), state.transformY(p.y));
                    cb.lineTo(state.transformX(x), state.transformY(y));
                    cb.stroke();
                    state.setCurrentPoint(new Point(x, y));
                    break;
                }
                case META_POLYLINE:
                {
                    state.setLineJoinPolygon(cb);
                    int len = in.readWord();
                    int x = in.readShort();
                    int y = in.readShort();
                    cb.moveTo(state.transformX(x), state.transformY(y));
                    for (int k = 1; k < len; ++k) {
                        x = in.readShort();
                        y = in.readShort();
                        cb.lineTo(state.transformX(x), state.transformY(y));
                    }
                    cb.stroke();
                    break;
                }
                case META_POLYGON:
                {
                    if (isNullStrokeFill(false))
                        break;
                    int len = in.readWord();
                    int sx = in.readShort();
                    int sy = in.readShort();
                    cb.moveTo(state.transformX(sx), state.transformY(sy));
                    for (int k = 1; k < len; ++k) {
                        int x = in.readShort();
                        int y = in.readShort();
                        cb.lineTo(state.transformX(x), state.transformY(y));
                    }
                    cb.lineTo(state.transformX(sx), state.transformY(sy));
                    strokeAndFill();
                    break;
                }
                case META_POLYPOLYGON:
                {
                    if (isNullStrokeFill(false))
                        break;
                    int numPoly = in.readWord();
                    int lens[] = new int[numPoly];
                    for (int k = 0; k < lens.length; ++k)
                        lens[k] = in.readWord();
                    for (int j = 0; j < lens.length; ++j) {
                        int len = lens[j];
                        int sx = in.readShort();
                        int sy = in.readShort();
                        cb.moveTo(state.transformX(sx), state.transformY(sy));
                        for (int k = 1; k < len; ++k) {
                            int x = in.readShort();
                            int y = in.readShort();
                            cb.lineTo(state.transformX(x), state.transformY(y));
                        }
                        cb.lineTo(state.transformX(sx), state.transformY(sy));
                    }
                    strokeAndFill();
                    break;
                }
                case META_ELLIPSE:
                {
                    if (isNullStrokeFill(state.getLineNeutral()))
                        break;
                    int b = in.readShort();
                    int r = in.readShort();
                    int t = in.readShort();
                    int l = in.readShort();
                    cb.arc(state.transformX(l), state.transformY(b), state.transformX(r), state.transformY(t), 0, 360);
                    strokeAndFill();
                    break;
                }
                case META_ARC:
                {
                    if (isNullStrokeFill(state.getLineNeutral()))
                        break;
                    float yend = state.transformY(in.readShort());
                    float xend = state.transformX(in.readShort());
                    float ystart = state.transformY(in.readShort());
                    float xstart = state.transformX(in.readShort());
                    float b = state.transformY(in.readShort());
                    float r = state.transformX(in.readShort());
                    float t = state.transformY(in.readShort());
                    float l = state.transformX(in.readShort());
                    float cx = (r + l) / 2;
                    float cy = (t + b) / 2;
                    float arc1 = getArc(cx, cy, xstart, ystart);
                    float arc2 = getArc(cx, cy, xend, yend);
                    arc2 -= arc1;
                    if (arc2 <= 0)
                        arc2 += 360;
                    cb.arc(l, b, r, t, arc1, arc2);
                    cb.stroke();
                    break;
                }
                case META_PIE:
                {
                    if (isNullStrokeFill(state.getLineNeutral()))
                        break;
                    float yend = state.transformY(in.readShort());
                    float xend = state.transformX(in.readShort());
                    float ystart = state.transformY(in.readShort());
                    float xstart = state.transformX(in.readShort());
                    float b = state.transformY(in.readShort());
                    float r = state.transformX(in.readShort());
                    float t = state.transformY(in.readShort());
                    float l = state.transformX(in.readShort());
                    float cx = (r + l) / 2;
                    float cy = (t + b) / 2;
                    float arc1 = getArc(cx, cy, xstart, ystart);
                    float arc2 = getArc(cx, cy, xend, yend);
                    arc2 -= arc1;
                    if (arc2 <= 0)
                        arc2 += 360;
                    ArrayList ar = PdfContentByte.bezierArc(l, b, r, t, arc1, arc2);
                    if (ar.isEmpty())
                        break;
                    float pt[] = (float [])ar.get(0);
                    cb.moveTo(cx, cy);
                    cb.lineTo(pt[0], pt[1]);
                    for (int k = 0; k < ar.size(); ++k) {
                        pt = (float [])ar.get(k);
                        cb.curveTo(pt[2], pt[3], pt[4], pt[5], pt[6], pt[7]);
                    }
                    cb.lineTo(cx, cy);
                    strokeAndFill();
                    break;
                }
                case META_CHORD:
                {
                    if (isNullStrokeFill(state.getLineNeutral()))
                        break;
                    float yend = state.transformY(in.readShort());
                    float xend = state.transformX(in.readShort());
                    float ystart = state.transformY(in.readShort());
                    float xstart = state.transformX(in.readShort());
                    float b = state.transformY(in.readShort());
                    float r = state.transformX(in.readShort());
                    float t = state.transformY(in.readShort());
                    float l = state.transformX(in.readShort());
                    float cx = (r + l) / 2;
                    float cy = (t + b) / 2;
                    float arc1 = getArc(cx, cy, xstart, ystart);
                    float arc2 = getArc(cx, cy, xend, yend);
                    arc2 -= arc1;
                    if (arc2 <= 0)
                        arc2 += 360;
                    ArrayList ar = PdfContentByte.bezierArc(l, b, r, t, arc1, arc2);
                    if (ar.isEmpty())
                        break;
                    float pt[] = (float [])ar.get(0);
                    cx = pt[0];
                    cy = pt[1];
                    cb.moveTo(cx, cy);
                    for (int k = 0; k < ar.size(); ++k) {
                        pt = (float [])ar.get(k);
                        cb.curveTo(pt[2], pt[3], pt[4], pt[5], pt[6], pt[7]);
                    }
                    cb.lineTo(cx, cy);
                    strokeAndFill();
                    break;
                }
                case META_RECTANGLE:
                {
                    if (isNullStrokeFill(true))
                        break;
                    float b = state.transformY(in.readShort());
                    float r = state.transformX(in.readShort());
                    float t = state.transformY(in.readShort());
                    float l = state.transformX(in.readShort());
                    cb.rectangle(l, b, r - l, t - b);
                    strokeAndFill();
                    break;
                }
                case META_ROUNDRECT:
                {
                    if (isNullStrokeFill(true))
                        break;
                    float h = state.transformY(0) - state.transformY(in.readShort());
                    float w = state.transformX(in.readShort()) - state.transformX(0);
                    float b = state.transformY(in.readShort());
                    float r = state.transformX(in.readShort());
                    float t = state.transformY(in.readShort());
                    float l = state.transformX(in.readShort());
                    cb.roundRectangle(l, b, r - l, t - b, (h + w) / 4);
                    strokeAndFill();
                    break;
                }
                case META_INTERSECTCLIPRECT:
                {
                    float b = state.transformY(in.readShort());
                    float r = state.transformX(in.readShort());
                    float t = state.transformY(in.readShort());
                    float l = state.transformX(in.readShort());
                    cb.rectangle(l, b, r - l, t - b);
                    cb.eoClip();
                    cb.newPath();
                    break;
                }
                case META_EXTTEXTOUT:
                {
                    int y = in.readShort();
                    int x = in.readShort();
                    int count = in.readWord();
                    int flag = in.readWord();
                    int x1 = 0;
                    int y1 = 0;
                    int x2 = 0;
                    int y2 = 0;
                    if ((flag & (MetaFont.ETO_CLIPPED | MetaFont.ETO_OPAQUE)) != 0) {
                        x1 = in.readShort();
                        y1 = in.readShort();
                        x2 = in.readShort();
                        y2 = in.readShort();
                    }
                    byte text[] = new byte[count];
                    int k;
                    for (k = 0; k < count; ++k) {
                        byte c = (byte)in.readByte();
                        if (c == 0)
                            break;
                        text[k] = c;
                    }
                    String s;
                    try {
                        s = new String(text, 0, k, "Cp1252");
                    }
                    catch (UnsupportedEncodingException e) {
                        s = new String(text, 0, k);
                    }
                    outputText(x, y, flag, x1, y1, x2, y2, s);
                    break;
                }
                case META_TEXTOUT:
                {
                    int count = in.readWord();
                    byte text[] = new byte[count];
                    int k;
                    for (k = 0; k < count; ++k) {
                        byte c = (byte)in.readByte();
                        if (c == 0)
                            break;
                        text[k] = c;
                    }
                    String s;
                    try {
                        s = new String(text, 0, k, "Cp1252");
                    }
                    catch (UnsupportedEncodingException e) {
                        s = new String(text, 0, k);
                    }
                    count = (count + 1) & 0xfffe;
                    in.skip(count - k);
                    int y = in.readShort();
                    int x = in.readShort();
                    outputText(x, y, 0, 0, 0, 0, 0, s);
                    break;
                }
                case META_SETBKCOLOR:
                    state.setCurrentBackgroundColor(in.readColor());
                    break;
                case META_SETTEXTCOLOR:
                    state.setCurrentTextColor(in.readColor());
                    break;
                case META_SETTEXTALIGN:
                    state.setTextAlign(in.readWord());
                    break;
                case META_SETBKMODE:
                    state.setBackgroundMode(in.readWord());
                    break;
                case META_SETPOLYFILLMODE:
                    state.setPolyFillMode(in.readWord());
                    break;
                case META_SETPIXEL:
                {
                    Color color = in.readColor();
                    int y = in.readShort();
                    int x = in.readShort();
                    cb.saveState();
                    cb.setColorFill(color);
                    cb.rectangle(state.transformX(x), state.transformY(y), .2f, .2f);
                    cb.fill();
                    cb.restoreState();
                    break;
                }
                case META_DIBSTRETCHBLT:
                case META_STRETCHDIB: {
                    int rop = in.readInt();
                    if (function == META_STRETCHDIB) {
                        /*int usage = */ in.readWord();
                    }
                    int srcHeight = in.readShort();
                    int srcWidth = in.readShort();
                    int ySrc = in.readShort();
                    int xSrc = in.readShort();
                    float destHeight = state.transformY(in.readShort()) - state.transformY(0);
                    float destWidth = state.transformX(in.readShort()) - state.transformX(0);
                    float yDest = state.transformY(in.readShort());
                    float xDest = state.transformX(in.readShort());
                    byte b[] = new byte[(tsize * 2) - (in.getLength() - lenMarker)];
                    for (int k = 0; k < b.length; ++k)
                        b[k] = (byte)in.readByte();
                    try {
                        ByteArrayInputStream inb = new ByteArrayInputStream(b);
                        Image bmp = BmpImage.getImage(inb, true, b.length);
                        cb.saveState();
                        cb.rectangle(xDest, yDest, destWidth, destHeight);
                        cb.clip();
                        cb.newPath();
                        bmp.scaleAbsolute(destWidth * bmp.getWidth() / srcWidth, -destHeight * bmp.getHeight() / srcHeight);
                        bmp.setAbsolutePosition(xDest - destWidth * xSrc / srcWidth, yDest + destHeight * ySrc / srcHeight - bmp.getScaledHeight());
                        cb.addImage(bmp);
                        cb.restoreState();
                    }
                    catch (Exception e) {
                        // empty on purpose
                    }
                    break;
                }
            }
            in.skip((tsize * 2) - (in.getLength() - lenMarker));
        }
        state.cleanup(cb);
    }
    
    public void outputText(int x, int y, int flag, int x1, int y1, int x2, int y2, String text) {
        MetaFont font = state.getCurrentFont();
        float refX = state.transformX(x);
        float refY = state.transformY(y);
        float angle = state.transformAngle(font.getAngle());
        float sin = (float)Math.sin(angle);
        float cos = (float)Math.cos(angle);
        float fontSize = font.getFontSize(state);
        BaseFont bf = font.getFont();
        int align = state.getTextAlign();
        float textWidth = bf.getWidthPoint(text, fontSize);
        float tx = 0;
        float ty = 0;
        float descender = bf.getFontDescriptor(BaseFont.DESCENT, fontSize);
        float ury = bf.getFontDescriptor(BaseFont.BBOXURY, fontSize);
        cb.saveState();
        cb.concatCTM(cos, sin, -sin, cos, refX, refY);
        if ((align & MetaState.TA_CENTER) == MetaState.TA_CENTER)
            tx = -textWidth / 2;
        else if ((align & MetaState.TA_RIGHT) == MetaState.TA_RIGHT)
            tx = -textWidth;
        if ((align & MetaState.TA_BASELINE) == MetaState.TA_BASELINE)
            ty = 0;
        else if ((align & MetaState.TA_BOTTOM) == MetaState.TA_BOTTOM)
            ty = -descender;
        else
            ty = -ury;
        Color textColor;
        if (state.getBackgroundMode() == MetaState.OPAQUE) {
            textColor = state.getCurrentBackgroundColor();
            cb.setColorFill(textColor);
            cb.rectangle(tx, ty + descender, textWidth, ury - descender);
            cb.fill();
        }
        textColor = state.getCurrentTextColor();
        cb.setColorFill(textColor);
        cb.beginText();
        cb.setFontAndSize(bf, fontSize);
        cb.setTextMatrix(tx, ty);
        cb.showText(text);
        cb.endText();
        if (font.isUnderline()) {
            cb.rectangle(tx, ty - fontSize / 4, textWidth, fontSize / 15);
            cb.fill();
        }
        if (font.isStrikeout()) {
            cb.rectangle(tx, ty + fontSize / 3, textWidth, fontSize / 15);
            cb.fill();
        }
        cb.restoreState();
    }
    
    public boolean isNullStrokeFill(boolean isRectangle) {
        MetaPen pen = state.getCurrentPen();
        MetaBrush brush = state.getCurrentBrush();
        boolean noPen = (pen.getStyle() == MetaPen.PS_NULL);
        int style = brush.getStyle();
        boolean isBrush = (style == MetaBrush.BS_SOLID || (style == MetaBrush.BS_HATCHED && state.getBackgroundMode() == MetaState.OPAQUE));
        boolean result = noPen && !isBrush;
        if (!noPen) {
            if (isRectangle)
                state.setLineJoinRectangle(cb);
            else
                state.setLineJoinPolygon(cb);
        }
        return result;
    }

    public void strokeAndFill(){
        MetaPen pen = state.getCurrentPen();
        MetaBrush brush = state.getCurrentBrush();
        int penStyle = pen.getStyle();
        int brushStyle = brush.getStyle();
        if (penStyle == MetaPen.PS_NULL) {
            cb.closePath();
            if (state.getPolyFillMode() == MetaState.ALTERNATE) {
                cb.eoFill();
            }
            else {
                cb.fill();
            }
        }
        else {
            boolean isBrush = (brushStyle == MetaBrush.BS_SOLID || (brushStyle == MetaBrush.BS_HATCHED && state.getBackgroundMode() == MetaState.OPAQUE));
            if (isBrush) {
                if (state.getPolyFillMode() == MetaState.ALTERNATE)
                    cb.closePathEoFillStroke();
                else
                    cb.closePathFillStroke();
            }
            else {
                cb.closePathStroke();
            }
        }
    }
    
    static float getArc(float xCenter, float yCenter, float xDot, float yDot) {
        double s = Math.atan2(yDot - yCenter, xDot - xCenter);
        if (s < 0)
            s += Math.PI * 2;
        return (float)(s / Math.PI * 180);
    }
    
    public static byte[] wrapBMP(Image image) throws IOException {
        if (image.getOriginalType() != Image.ORIGINAL_BMP)
            throw new IOException(MessageLocalization.getComposedMessage("only.bmp.can.be.wrapped.in.wmf"));
        InputStream imgIn;
        byte data[] = null;
        if (image.getOriginalData() == null) {
            imgIn = image.getUrl().openStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int b = 0;
            while ((b = imgIn.read()) != -1)
                out.write(b);
            imgIn.close();
            data = out.toByteArray();
        }
        else
            data = image.getOriginalData();
        int sizeBmpWords = (data.length - 14 + 1) >>> 1;
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        // write metafile header
        writeWord(os, 1);
        writeWord(os, 9);
        writeWord(os, 0x0300);
        writeDWord(os, 9 + 4 + 5 + 5 + (13 + sizeBmpWords) + 3); // total metafile size
        writeWord(os, 1);
        writeDWord(os, 14 + sizeBmpWords); // max record size
        writeWord(os, 0);
        // write records
        writeDWord(os, 4);
        writeWord(os, META_SETMAPMODE);
        writeWord(os, 8);

        writeDWord(os, 5);
        writeWord(os, META_SETWINDOWORG);
        writeWord(os, 0);
        writeWord(os, 0);

        writeDWord(os, 5);
        writeWord(os, META_SETWINDOWEXT);
        writeWord(os, (int)image.getHeight());
        writeWord(os, (int)image.getWidth());

        writeDWord(os, 13 + sizeBmpWords);
        writeWord(os, META_DIBSTRETCHBLT);
        writeDWord(os, 0x00cc0020);
        writeWord(os, (int)image.getHeight());
        writeWord(os, (int)image.getWidth());
        writeWord(os, 0);
        writeWord(os, 0);
        writeWord(os, (int)image.getHeight());
        writeWord(os, (int)image.getWidth());
        writeWord(os, 0);
        writeWord(os, 0);
        os.write(data, 14, data.length - 14);
        if ((data.length & 1) == 1)
            os.write(0);
//        writeDWord(os, 14 + sizeBmpWords);
//        writeWord(os, META_STRETCHDIB);
//        writeDWord(os, 0x00cc0020);
//        writeWord(os, 0);
//        writeWord(os, (int)image.height());
//        writeWord(os, (int)image.width());
//        writeWord(os, 0);
//        writeWord(os, 0);
//        writeWord(os, (int)image.height());
//        writeWord(os, (int)image.width());
//        writeWord(os, 0);
//        writeWord(os, 0);
//        os.write(data, 14, data.length - 14);
//        if ((data.length & 1) == 1)
//            os.write(0);

        writeDWord(os, 3);
        writeWord(os, 0);
        os.close();
        return os.toByteArray();
    }

    public static void writeWord(OutputStream os, int v) throws IOException {
        os.write(v & 0xff);
        os.write((v >>> 8) & 0xff);
    }
    
    public static void writeDWord(OutputStream os, int v) throws IOException {
        writeWord(os, v & 0xffff);
        writeWord(os, (v >>> 16) & 0xffff);
    }
}
