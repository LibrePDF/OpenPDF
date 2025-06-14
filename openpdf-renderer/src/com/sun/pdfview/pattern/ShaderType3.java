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
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;

import com.sun.pdfview.BaseWatchable;
import com.sun.pdfview.PDFObject;
import com.sun.pdfview.PDFPaint;
import com.sun.pdfview.PDFParseException;
import com.sun.pdfview.colorspace.PDFColorSpace;
import com.sun.pdfview.function.PDFFunction;

/**
 * A shader that performs radial shader based on a function.
 */
public class ShaderType3 extends PDFShader {
    /** the center of the first circle */
    private Point2D center1;
    
    /** the center of the second circle */
    private Point2D center2;
    
    /** the radius of the first circle */
    private float radius1;
    
    /** the radius of the second circle */
    private float radius2;

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
    public ShaderType3() {
        super(3);
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
        center1 = new Point2D.Float(coords[0].getFloatValue(),
                                          coords[1].getFloatValue());
        center2 = new Point2D.Float(coords[3].getFloatValue(),
                                          coords[4].getFloatValue());
        radius1 = coords[2].getFloatValue();
        radius2 = coords[5].getFloatValue();
        
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
        return PDFPaint.getPaint(new Type3Paint());
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
    class Type3Paint implements Paint {
        public Type3Paint() {
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
            
            return new Type3PaintContext(model, xform);
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
    class Type3PaintContext implements PaintContext {
        /** the color model */
        private ColorModel colorModel;
        
        /** the transformation */
        private AffineTransform invXform;
        
        private final double dx1x0, dy1y0, dr1r0, sqr0, denom;
        
        /**
         * Create a paint context
         */
        Type3PaintContext(ColorModel colorModel, AffineTransform xform) {
            this.colorModel = colorModel;
            
            //Precalculate some often needed values; 
            dx1x0 = center2.getX() - center1.getX();
            dy1y0 = center2.getY() - center1.getY();
            dr1r0 = radius2 - radius1;
            sqr0 = radius1*radius1;
            denom = dx1x0*dx1x0 + dy1y0*dy1y0 - dr1r0*dr1r0;
            
            try {
				this.invXform = xform.createInverse();
			}
			catch (NoninvertibleTransformException e) {
			    BaseWatchable.getErrorHandler().publishException(e);
			}
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

            float[] c1 = new float[2];
            
            float[] inputs = new float[1];
            float[] outputs = new float[shadeCSpace.getNumComponents()];
            float[] outputRBG = new float[numComponents];
            
            // all the data, plus alpha channel
            int[] data = new int[w * h * (numComponents + 1)];
        	float lastInput = Float.POSITIVE_INFINITY;
        	final float tol = TOLERANCE * (getMaxT() - getMinT());
            
            final int advance = 1;
            // for each device coordinate
            for (int j = 0; j < h; j++) {
            	for (int i = 0; i < w; i += advance) {
            		//Get point in user space
            		invXform.transform(new float[]{x + i, y + j}, 0, c1, 0, 1);
            		boolean render = true;
            		float[] s = calculateInputValues(c1[0], c1[1]);
            		//s[0] <= s[1] holds
            		//if (s[0] >= 0 && s[1] <= 1) s[1] = s[1];
            		if (s[1] >= 0 && s[1] <= 1) s[1] = s[1];
            		else if (extendEnd == true && s[1] >= 0 && radius1 + s[1]*dr1r0 >= 0) {
            			s[1] = s[1];
            		}
            		else if (s[0] >= 0 && s[0] <= 1) s[1] = s[0];
            		else if (extendStart == true && s[1] <= 0 && radius1 + s[1]*dr1r0 >= 0) {
            			s[1] = s[1];
            		}
            		else if (extendStart == true && s[0] <= 1 && radius1 + s[0]*dr1r0 >= 0) {
            			s[1] = s[0];
            		}
            		else render = false;
            		
            		if (render) {
            			float t = (getMinT() + s[1]*(getMaxT() - getMinT()));
            			// calculate the pixel values at t
            			inputs[0] = t;
            			if (Math.abs(lastInput - t) > tol) {

            				if (functions.length == 1) {
            					functions[0].calculate(inputs, 0, outputs, 0);
            				} else {
            					for (int c = 0; c < functions.length; c++) {
            						functions[c].calculate(inputs, 0, outputs, c);
            					} 
            				}
            				
        					if (!shadeCSpace.getColorSpace().isCS_sRGB()) {
        						//Can be quite slow
        						outputRBG = shadeCSpace.getColorSpace().toRGB(outputs);
        					}
        					else outputRBG = outputs;

        					lastInput = t;
            			}
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
         * From Adobe Technical Note #5600:
         * 
         * Given a geometric coordinate position (x, y) in or along the gradient gradient fill,
         * the corresponding value of s can be determined by solving the quadratic
         * constraint equation:
         *
         * [x - xc(s)]2 + [y - yc(s)]2 = [r(s)]2
         *
         * The following code calculates the 2 possible values of s.
         * 
         * @return Two possible values of s with s[0] <= s[1]
         */                 
        private float[] calculateInputValues(float x, float y) {
        	double p = -(x - center1.getX())*dx1x0 -(y - center1.getY())*dy1y0 - radius1*dr1r0;
            double q = (Math.pow(x - center1.getX(), 2) + Math.pow(y - center1.getY(), 2) - sqr0);
            double root = Math.sqrt(p*p - denom*q);
            float root1 = (float) ((-p + root)/denom);
            float root2 = (float) ((-p - root)/denom);
            if (denom < 0) return new float[]{root1, root2};
            else return new float[]{root2, root1};
        }
    }        
}
