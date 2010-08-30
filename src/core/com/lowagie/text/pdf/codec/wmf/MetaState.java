/*
 * $Id: MetaState.java 3427 2008-05-24 18:32:31Z xlv $
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
import java.util.ArrayList;
import java.util.Stack;

import com.lowagie.text.pdf.PdfContentByte;

public class MetaState {
    
    public static final int TA_NOUPDATECP = 0;
    public static final int TA_UPDATECP = 1;
    public static final int TA_LEFT = 0;
    public static final int TA_RIGHT = 2;
    public static final int TA_CENTER = 6;
    public static final int TA_TOP = 0;
    public static final int TA_BOTTOM = 8;
    public static final int TA_BASELINE = 24;
    
    public static final int TRANSPARENT = 1;
    public static final int OPAQUE = 2;

    public static final int ALTERNATE = 1;
    public static final int WINDING = 2;

    public Stack savedStates;
    public ArrayList MetaObjects;
    public Point currentPoint;
    public MetaPen currentPen;
    public MetaBrush currentBrush;
    public MetaFont currentFont;
    public Color currentBackgroundColor = Color.white;
    public Color currentTextColor = Color.black;
    public int backgroundMode = OPAQUE;
    public int polyFillMode = ALTERNATE;
    public int lineJoin = 1;
    public int textAlign;
    public int offsetWx;
    public int offsetWy;
    public int extentWx;
    public int extentWy;
    public float scalingX;
    public float scalingY;
    

    /** Creates new MetaState */
    public MetaState() {
        savedStates = new Stack();
        MetaObjects = new ArrayList();
        currentPoint = new Point(0, 0);
        currentPen = new MetaPen();
        currentBrush = new MetaBrush();
        currentFont = new MetaFont();
    }

    public MetaState(MetaState state) {
        setMetaState(state);
    }
    
    public void setMetaState(MetaState state) {
        savedStates = state.savedStates;
        MetaObjects = state.MetaObjects;
        currentPoint = state.currentPoint;
        currentPen = state.currentPen;
        currentBrush = state.currentBrush;
        currentFont = state.currentFont;
        currentBackgroundColor = state.currentBackgroundColor;
        currentTextColor = state.currentTextColor;
        backgroundMode = state.backgroundMode;
        polyFillMode = state.polyFillMode;
        textAlign = state.textAlign;
        lineJoin = state.lineJoin;
        offsetWx = state.offsetWx;
        offsetWy = state.offsetWy;
        extentWx = state.extentWx;
        extentWy = state.extentWy;
        scalingX = state.scalingX;
        scalingY = state.scalingY;
    }

    public void addMetaObject(MetaObject object) {
        for (int k = 0; k < MetaObjects.size(); ++k) {
            if (MetaObjects.get(k) == null) {
                MetaObjects.set(k, object);
                return;
            }
        }
        MetaObjects.add(object);
    }
    
    public void selectMetaObject(int index, PdfContentByte cb) {
        MetaObject obj = (MetaObject)MetaObjects.get(index);
        if (obj == null)
            return;
        int style;
        switch (obj.getType()) {
            case MetaObject.META_BRUSH:
                currentBrush = (MetaBrush)obj;
                style = currentBrush.getStyle();
                if (style == MetaBrush.BS_SOLID) {
                    Color color = currentBrush.getColor();
                    cb.setColorFill(color);
                }
                else if (style == MetaBrush.BS_HATCHED) {
                    Color color = currentBackgroundColor;
                    cb.setColorFill(color);
                }
                break;
            case MetaObject.META_PEN:
            {
                currentPen = (MetaPen)obj;
                style = currentPen.getStyle();
                if (style != MetaPen.PS_NULL) {
                    Color color = currentPen.getColor();
                    cb.setColorStroke(color);
                    cb.setLineWidth(Math.abs(currentPen.getPenWidth() * scalingX / extentWx));
                    switch (style) {
                        case MetaPen.PS_DASH:
                            cb.setLineDash(18, 6, 0);
                            break;
                        case MetaPen.PS_DASHDOT:
                            cb.setLiteral("[9 6 3 6]0 d\n");
                            break;
                        case MetaPen.PS_DASHDOTDOT:
                            cb.setLiteral("[9 3 3 3 3 3]0 d\n");
                            break;
                        case MetaPen.PS_DOT:
                            cb.setLineDash(3, 0);
                            break;
                        default:
                            cb.setLineDash(0);
                            break;                            
                    }
                }
                break;
            }
            case MetaObject.META_FONT:
            {
                currentFont = (MetaFont)obj;
                break;
            }
        }
    }
    
    public void deleteMetaObject(int index) {
        MetaObjects.set(index, null);
    }
    
    public void saveState(PdfContentByte cb) {
        cb.saveState();
        MetaState state = new MetaState(this);
        savedStates.push(state);
    }

    public void restoreState(int index, PdfContentByte cb) {
        int pops;
        if (index < 0)
            pops = Math.min(-index, savedStates.size());
        else
            pops = Math.max(savedStates.size() - index, 0);
        if (pops == 0)
            return;
        MetaState state = null;
        while (pops-- != 0) {
            cb.restoreState();
            state = (MetaState)savedStates.pop();
        }
        setMetaState(state);
    }
    
    public void cleanup(PdfContentByte cb) {
        int k = savedStates.size();
        while (k-- > 0)
            cb.restoreState();
    }
    
    public float transformX(int x) {
        return ((float)x - offsetWx) * scalingX / extentWx;
    }

    public float transformY(int y) {
        return (1f - ((float)y - offsetWy) / extentWy) * scalingY;
    }
    
    public void setScalingX(float scalingX) {
        this.scalingX = scalingX;
    }
    
    public void setScalingY(float scalingY) {
        this.scalingY = scalingY;
    }
    
    public void setOffsetWx(int offsetWx) {
        this.offsetWx = offsetWx;
    }
    
    public void setOffsetWy(int offsetWy) {
        this.offsetWy = offsetWy;
    }
    
    public void setExtentWx(int extentWx) {
        this.extentWx = extentWx;
    }
    
    public void setExtentWy(int extentWy) {
        this.extentWy = extentWy;
    }
    
    public float transformAngle(float angle) {
        float ta = scalingY < 0 ? -angle : angle;
        return (float)(scalingX < 0 ? Math.PI - ta : ta);
    }
    
    public void setCurrentPoint(Point p) {
        currentPoint = p;
    }

    public Point getCurrentPoint() {
        return currentPoint;
    }
    
    public MetaBrush getCurrentBrush() {
        return currentBrush;
    }

    public MetaPen getCurrentPen() {
        return currentPen;
    }

    public MetaFont getCurrentFont() {
        return currentFont;
    }
    
    /** Getter for property currentBackgroundColor.
     * @return Value of property currentBackgroundColor.
     */
    public Color getCurrentBackgroundColor() {
        return currentBackgroundColor;
    }
    
    /** Setter for property currentBackgroundColor.
     * @param currentBackgroundColor New value of property currentBackgroundColor.
     */
    public void setCurrentBackgroundColor(Color currentBackgroundColor) {
        this.currentBackgroundColor = currentBackgroundColor;
    }
    
    /** Getter for property currentTextColor.
     * @return Value of property currentTextColor.
     */
    public Color getCurrentTextColor() {
        return currentTextColor;
    }
    
    /** Setter for property currentTextColor.
     * @param currentTextColor New value of property currentTextColor.
     */
    public void setCurrentTextColor(Color currentTextColor) {
        this.currentTextColor = currentTextColor;
    }
    
    /** Getter for property backgroundMode.
     * @return Value of property backgroundMode.
     */
    public int getBackgroundMode() {
        return backgroundMode;
    }
    
    /** Setter for property backgroundMode.
     * @param backgroundMode New value of property backgroundMode.
     */
    public void setBackgroundMode(int backgroundMode) {
        this.backgroundMode = backgroundMode;
    }
    
    /** Getter for property textAlign.
     * @return Value of property textAlign.
     */
    public int getTextAlign() {
        return textAlign;
    }
    
    /** Setter for property textAlign.
     * @param textAlign New value of property textAlign.
     */
    public void setTextAlign(int textAlign) {
        this.textAlign = textAlign;
    }
    
    /** Getter for property polyFillMode.
     * @return Value of property polyFillMode.
     */
    public int getPolyFillMode() {
        return polyFillMode;
    }
    
    /** Setter for property polyFillMode.
     * @param polyFillMode New value of property polyFillMode.
     */
    public void setPolyFillMode(int polyFillMode) {
        this.polyFillMode = polyFillMode;
    }
    
    public void setLineJoinRectangle(PdfContentByte cb) {
        if (lineJoin != 0) {
            lineJoin = 0;
            cb.setLineJoin(0);
        }
    }
    
    public void setLineJoinPolygon(PdfContentByte cb) {
        if (lineJoin == 0) {
            lineJoin = 1;
            cb.setLineJoin(1);
        }
    }
    
    public boolean getLineNeutral() {
        return (lineJoin == 0);
    }
    
}
