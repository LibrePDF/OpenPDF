/*
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.sun.pdfview.pattern;

import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;

import com.sun.pdfview.PDFObject;
import com.sun.pdfview.PDFPaint;
import com.sun.pdfview.PDFParseException;
import com.sun.pdfview.colorspace.PDFColorSpace;
import com.sun.pdfview.function.PDFFunction;

/**
 * A shader that performs axial shader based on a function.
 */
public class ShaderType2 extends PDFShader {
    /** the start of the axis */
    private Point2D axisStart;
    
    /** the end of the axis */
    private Point2D axisEnd;
    
    /** the domain minimum */
    private float minT = 0f;
    
    /** the domain maximum */
    private float maxT = 1f;
    
    /** whether to extend the start of the axis */
    private boolean extendStart = false;
    
    /** whether to extend the end of the axis */
    private boolean extendEnd = false;
    
    /** functions, as an array of either 1 or n functions */
    private PDFFunction[] functions;
     
    /** Creates a new instance of ShaderType2 */
    public ShaderType2() {
        super(2);
    }
    
    /** 
     * Parse the shader-specific data
     */
    @Override
	public void parse(PDFObject shaderObj) throws IOException
    {
        // read the axis coordinates (required)
        PDFObject coordsObj = shaderObj.getDictRef("Coords");
        if (coordsObj == null) {
            throw new PDFParseException("No coordinates found!");
        }
        PDFObject[] coords = coordsObj.getArray();
        Point2D start = new Point2D.Float(coords[0].getFloatValue(),
                                          coords[1].getFloatValue());
        Point2D end   = new Point2D.Float(coords[2].getFloatValue(),
                                          coords[3].getFloatValue());
        setAxisStart(start);
        setAxisEnd(end);
        
        // read the domain (optional)
        PDFObject domainObj = shaderObj.getDictRef("Domain");
        if (domainObj != null) {
            PDFObject[] domain = domainObj.getArray();
            setMinT(domain[0].getFloatValue());
            setMaxT(domain[1].getFloatValue());
        }
        
        // read the functions (required)
        PDFObject functionObj = shaderObj.getDictRef("Function");
        if (functionObj == null) {
            throw new PDFParseException("No function defined for shader!");
        }
        PDFObject[] functionArray = functionObj.getArray();
        PDFFunction[] functions = new PDFFunction[functionArray.length];
        for (int i = 0; i < functions.length; i++) {
            functions[i] = PDFFunction.getFunction(functionArray[i]);
        }
        setFunctions(functions);
        
        // read the extend array (optional)
        PDFObject extendObj = shaderObj.getDictRef("Extend");
        if (extendObj != null) {
            PDFObject[] extendArray = extendObj.getArray();
            setExtendStart(extendArray[0].getBooleanValue());
            setExtendEnd(extendArray[1].getBooleanValue());
        }
        
    }
    
    /**
     * Create a paint that paints this pattern
     */
    @Override
	public PDFPaint getPaint() {
        return PDFPaint.getPaint(new Type2Paint());
    }
    
    /** 
     * Get the start of the axis
     */
    public Point2D getAxisStart() {
        return this.axisStart;
    }
    
    /**
     * Set the start of the axis
     */
    protected void setAxisStart(Point2D axisStart) {
        this.axisStart = axisStart;
    }
    
    /** 
     * Get the end of the axis
     */
    public Point2D getAxisEnd() {
        return this.axisEnd;
    }
    
    /**
     * Set the start of the axis
     */
    protected void setAxisEnd(Point2D axisEnd) {
        this.axisEnd = axisEnd;
    }
    
    /** 
     * Get the domain minimum
     */
    public float getMinT() {
        return this.minT;
    }
    
    /**
     * Set the domain minimum
     */
    protected void setMinT(float minT) {
        this.minT = minT;
    }
    
    /** 
     * Get the domain maximum
     */
    public float getMaxT() {
        return this.maxT;
    }
    
    /**
     * Set the domain maximum
     */
    protected void setMaxT(float maxT) {
        this.maxT = maxT;
    }
    
    /**
     * Get whether to extend the start of the axis
     */
    public boolean getExtendStart() {
        return this.extendStart;
    }
    
    /**
     * Set whether to extend the start of the axis
     */
    protected void setExtendStart(boolean extendStart) {
        this.extendStart = extendStart;
    }
    
    /**
     * Get whether to extend the end of the axis
     */
    public boolean getExtendEnd() {
        return this.extendEnd;
    }
    
    /**
     * Set whether to extend the end of the axis
     */
    protected void setExtendEnd(boolean extendEnd) {
        this.extendEnd = extendEnd;
    }
    
    /**
     * Get the functions associated with this shader
     */
    public PDFFunction[] getFunctions() {
        return this.functions;
    }
    
    /**
     * Set the functions associated with this shader
     */
    protected void setFunctions(PDFFunction[] functions) {
        this.functions = functions;
    }
    
    /**
     * A subclass of paint that uses this shader to generate a paint
     */
    class Type2Paint implements Paint {
        public Type2Paint() {
        }
        
        /** create a paint context */
        @Override
		public PaintContext createContext(ColorModel cm, 
                                          Rectangle deviceBounds, 
                                          Rectangle2D userBounds,
                                          AffineTransform xform,
                                          RenderingHints hints) 
        {
            ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
            ColorModel model = new ComponentColorModel(cs, 
                                                       true, 
                                                       false, 
                                                       Transparency.TRANSLUCENT,
                                                       DataBuffer.TYPE_BYTE);
            
            Point2D devStart = xform.transform(getAxisStart(), null);
            Point2D devEnd = xform.transform(getAxisEnd(), null);
          
            return new Type2PaintContext(model, devStart, devEnd);
        }
                
        @Override
		public int getTransparency() {
            return Transparency.TRANSLUCENT;
        }
    }
    
    /** 
     * A simple paint context that uses an existing raster in device
     * space to generate pixels
     */
    class Type2PaintContext implements PaintContext {
        /** the color model */
        private ColorModel colorModel;
        
        /** the start of the axis */
        private Point2D start;
        
        /** the end of the axis */
        private Point2D end;
        
        
        private float dt1t0;
        private double dx1x0, dy1y0, sqdx1x0psqdy1y0;
        
        /**
         * Create a paint context
         */
        Type2PaintContext(ColorModel colorModel, Point2D start, Point2D end) {
            this.colorModel = colorModel;
            this.start = start;
            this.end = end;
            
            //pre calculate some often used values
            dt1t0 = getMaxT() - getMinT();
            dx1x0 = end.getX() - start.getX();
            dy1y0 = end.getY() - start.getY();
            sqdx1x0psqdy1y0 = dx1x0*dx1x0 + dy1y0*dy1y0;
        }
        
        @Override
		public void dispose() {
            this.colorModel = null;
        }
        
        @Override
		public ColorModel getColorModel() {
            return this.colorModel;
        }
        
        @Override
        public Raster getRaster(int x, int y, int w, int h) {
        	ColorSpace cs = getColorModel().getColorSpace();
            PDFColorSpace shadeCSpace = getColorSpace();


        	PDFFunction functions[] = getFunctions();
        	int numComponents = cs.getNumComponents();

        	float x0 = (float) this.start.getX();
        	float y0 = (float) this.start.getY();

        	float[] inputs = new float[1];
            float[] outputs = new float[shadeCSpace.getNumComponents()];
            float[] outputRBG = new float[numComponents];

        	// all the data, plus alpha channel
        	int[] data = new int[w * h * (numComponents + 1)];

        	// for each device coordinate
        	for (int j = 0; j < h; j++) {
        		for (int i = 0; i < w; i += 1) {
        			boolean render = true;
        			// find t for that user coordinate
        			float xp = getXPrime(i + x, j + y, x0, y0);
        			float t = 0;
        			if (xp >= 0 && xp <= 1) t = getMinT() + (dt1t0 * xp);
        			else if (xp < 0 && extendStart) t = getMinT();
        			else if (xp > 1 && extendEnd) t = getMaxT();
        			else render = false;

        			if (render) {
        				// calculate the pixel values at t
        				inputs[0] = t;
        				if (functions.length == 1) {
        					functions[0].calculate(inputs, 0, outputs, 0);
        				} else {
        					for (int c = 0; c < functions.length; c++) {
        						functions[c].calculate(inputs, 0, outputs, c);
        					} 
        				}
        				if (functions[0].getNumOutputs() != numComponents) {
        					//CMYK
        					outputRBG = shadeCSpace.getColorSpace().toRGB(outputs);
        				}
        				else outputRBG = outputs;

        				int base = (j * w + i) * (numComponents + 1);
        				for (int c = 0; c < numComponents; c++) {
        					data[base + c] = (int) (outputRBG[c] * 255);
        				}
        				data[base + numComponents] = 255; 
        			}
        		}
        	}

            WritableRaster raster =
                getColorModel().createCompatibleWritableRaster(w, h);
            raster.setPixels(0, 0, w, h, data);
          
            Raster child = raster.createTranslatedChild(x, y);
            return child;
        }
        
        /**
         * x' = (x1 - x0) * (x - x0) + (y1 - y0) * (y - y0)
         *      -------------------------------------------
         *               (x1 - x0)^2 + (y1 - y0)^2
         */
        private float getXPrime(float x, float y, float x0, float y0) {
           
            double tp = ((dx1x0* (x - x0)) + (dy1y0 * (y - y0))) / sqdx1x0psqdy1y0;
        
            return (float) tp;
        }
        
        /**
         * t = t0 + (t1 - t0) x x'
         */
        private float getT(float xp) {
        	
            if (xp < 0) {
                return getMinT();
            } else if (xp > 1) {
                return getMaxT();
            } else {
                return getMinT() + (dt1t0 * xp);
            }
        }
    }
}