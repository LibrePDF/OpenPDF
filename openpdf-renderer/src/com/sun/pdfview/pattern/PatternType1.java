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

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.PaintContext;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.sun.pdfview.PDFObject;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PDFPaint;
import com.sun.pdfview.PDFParser;
import com.sun.pdfview.PDFRenderer;

/**
 * A type 1 (tiling) pattern
 */
public class PatternType1 extends PDFPattern {
    /** paint types */
    public static final int PAINT_COLORED = 1;
    public static final int PAINT_UNCOLORED = 2;
   
    /** tiling types */
    public static final int TILE_CONSTANT = 1;
    public static final int TILE_NODISTORT = 2;
    public static final int TILE_FASTER = 3;
    
    /** the resources used by the image we will tile */
    private HashMap<String,PDFObject> resources;
    
    /** the paint type (colored or uncolored) */
    private int paintType;
    
    /** the tiling type (constant, no distort or faster) */
    private int tilingType;
    
    /** the bounding box of the tile, in tile space */
    private Rectangle2D bbox;
    
    /** the horiztonal tile spacing, in tile space */
    private int xStep;
    
    /** the vertical spacing, in tile space */
    private int yStep;
    
    /** the stream data */
    private byte[] data;
    
    /** Creates a new instance of PatternType1 */
    public PatternType1() {
        super(1);    
    }
    
    /**
     * Parse the pattern from the PDFObject
     *
     * Note the resources passed in are ignored...
     */
    @Override
	protected void parse(PDFObject patternObj, Map rsrc) throws IOException
    {
        this.data = patternObj.getStream();
        
        this.resources = patternObj.getDictRef("Resources").getDictionary();
        this.paintType = patternObj.getDictRef("PaintType").getIntValue();
        this.tilingType = patternObj.getDictRef("TilingType").getIntValue();
        
        PDFObject bboxObj = patternObj.getDictRef("BBox");
	this.bbox= new Rectangle2D.Float(bboxObj.getAt(0).getFloatValue(),
                                    bboxObj.getAt(1).getFloatValue(),
                                    bboxObj.getAt(2).getFloatValue(),
                                    bboxObj.getAt(3).getFloatValue());
        
        this.xStep = patternObj.getDictRef("XStep").getIntValue();
        this.yStep = patternObj.getDictRef("YStep").getIntValue();
    }
    
    /** 
     * Create a PDFPaint from this pattern and set of components.  
     * This creates a buffered image of this pattern using
     * the given paint, then uses that image to create the correct 
     * TexturePaint to use in the PDFPaint.
     *
     * @param basePaint the base paint to use, or null if not needed
     */
    @Override
	public PDFPaint getPaint(PDFPaint basePaint) {
        // create the outline of the pattern in user space by creating
        // a box with width xstep and height ystep.  Transform that
        // box using the pattern's matrix to get the user space
        // bounding box
        Rectangle2D anchor = new Rectangle2D.Double(getBBox().getMinX(),
                                                    getBBox().getMinY(),
                                                    getXStep(),
                                                    getYStep());
        //anchor = getTransform().createTransformedShape(anchor).getBounds2D();
        
        // now create a page bounded by the pattern's user space size
        final PDFPage page = new PDFPage(getBBox(), 0);
        
        // set the base paint if there is one
        if (basePaint != null) {
            page.addFillPaint(basePaint);
            page.addStrokePaint(basePaint);
        }
        
        // undo the page's transform to user space
        /*
        AffineTransform xform =
            new AffineTransform(1, 0, 0, -1, 0, getYStep());
            //new AffineTransform(1, 0, 0, -1, 0, getBBox().getHeight());
        page.addXform(xform);
        */
        
        // now parse the pattern contents
        PDFParser prc = new PDFParser(page, this.data, getResources());
        prc.go(true);
        
        int width = (int) getBBox().getWidth();
        int height = (int) getBBox().getHeight();
        
        // get actual image
        Paint paint = new Paint() {
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
                
                Rectangle2D devBBox = 
                        xform.createTransformedShape(userBounds).getBounds2D();
                
                double[] steps = new double[] { getXStep(), getYStep() };
                xform.deltaTransform(steps, 0, steps, 0, 1);
                
                int width = (int) Math.ceil(devBBox.getWidth());
                int height = (int) Math.ceil(devBBox.getHeight());
                
                BufferedImage img = (BufferedImage) page.getImage(width, height,  
                                                                  null, null, 
                                                                  false, true);
                                                                  
                return new Type1PaintContext(model, devBBox, 
                                             (float) steps[0],
                                             (float) steps[1],
                                             img.getData());
            }
            
            @Override
			public int getTransparency() {
                return Transparency.TRANSLUCENT;
            }
        };
                                            
        
        return new TilingPatternPaint(paint, this);
    }
    
    /** get the associated resources */
    public HashMap<String,PDFObject> getResources() {
        return this.resources;
    }
    
    /** get the paint type */
    public int getPaintType() {
        return this.paintType;
    }
    
    /** get the tiling type */
    public int getTilingType() {
        return this.tilingType;
    }
    
    /** get the bounding box */
    public Rectangle2D getBBox() {
        return this.bbox;
    }
    
    /** get the x step */
    public int getXStep() {
        return this.xStep;
    }
    
    /** get the y step */
    public int getYStep() {
        return this.yStep;
    }
    
    /** 
     * This class overrides PDFPaint to paint in the pattern coordinate space
     */
    static class TilingPatternPaint extends PDFPaint {
        /** the pattern to paint */
        private PatternType1 pattern;
        
        /** Create a tiling pattern paint */
        public TilingPatternPaint(Paint paint, PatternType1 pattern) {
            super (paint);
            
            this.pattern = pattern;
        }
        
        /**
         * fill a path with the paint, and record the dirty area.
         * @param state the current graphics state
         * @param g the graphics into which to draw
         * @param s the path to fill
         * @param drawn a Rectangle2D into which the dirty area (area drawn)
         * will be added.
         */
        @Override
		public Rectangle2D fill(PDFRenderer state, Graphics2D g,
                                GeneralPath s) {                        
            // first transform s into device space
            AffineTransform at = g.getTransform();
            Shape xformed = s.createTransformedShape(at);
                                    
            // push the graphics state so we can restore it
            state.push();
            
            // set the transform to be the inital transform concatentated
            // with the pattern matrix
            state.setTransform(state.getInitialTransform());
            state.transform(this.pattern.getTransform());
            
            // now figure out where the shape should be
            try {
                at = state.getTransform().createInverse();
            } catch (NoninvertibleTransformException nte) {
                // oh well (?)
            }
            xformed = at.createTransformedShape(xformed);
            
            // set the paint and draw the xformed shape
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER));
            g.setPaint(getPaint());
            g.fill(xformed);
            
            // restore the graphics state
            state.pop();
            
            // return the area changed
            return s.createTransformedShape(g.getTransform()).getBounds2D();
        }
    }
    
    /** 
     * A simple paint context that uses an existing raster in device
     * space to generate pixels
     */
    class Type1PaintContext implements PaintContext {
        /** the color model */
        private ColorModel colorModel;
        
        /** the anchor box */
        private Rectangle2D bbox;
        
        /** the x offset */
        private float xstep;
        
        /** the y offset */
        private float ystep;
        
        /** the image data, as a raster in device coordinates */
        private Raster data;
        
        /**
         * Create a paint context
         */
        Type1PaintContext(ColorModel colorModel, Rectangle2D bbox,
                          float xstep, float ystep, Raster data) 
        {
            this.colorModel = colorModel;
            this.bbox = bbox;
            this.xstep = xstep;
            this.ystep = ystep;
            this.data = data;
        }
        
        @Override
		public void dispose() {
            this.colorModel = null;
            this.bbox = null;
            this.data = null;
        }
        
        @Override
		public ColorModel getColorModel() {
            return this.colorModel;
        }
        
        @Override
		public Raster getRaster(int x, int y, int w, int h) {
            ColorSpace cs = getColorModel().getColorSpace();
       
            int numComponents = cs.getNumComponents();
  
            // all the data, plus alpha channel
            int[] imgData = new int[w * h * (numComponents + 1)];
  
	    // the x and y step, as ints	
            int useXStep = (int) Math.abs(Math.ceil(this.xstep));
	    int useYStep = (int) Math.abs(Math.ceil(this.ystep));
         
            // a completely transparent pixel (alpha of 0)
            int[] emptyPixel = new int[numComponents + 1];
            int[] usePixel = new int[numComponents + 1];
       
            // for each device coordinate
            for (int j = 0; j < h; j++) {
                for (int i = 0; i < w; i ++) {
                    // figure out what pixel we are at relative to the image
                    int xloc = (x + i) - (int) Math.ceil(this.bbox.getX());
                    int yloc = (y + j) - (int) Math.ceil(this.bbox.getY());
                    
                     //if useXStep is 0, we would divide through 0 so instead xloc is set to 0
                    if(useXStep == 0) {
                    	xloc = 0;
                    }
                    else {
                    	xloc %= useXStep;
                    }
                    
                    //if useYStep is 0, we would divide through 0 so instead yloc is set to 0
                    if(useYStep == 0) {
                    	yloc = 0;
                    }
                    else {
                    	yloc %= useYStep;
                    }
                    
                    if (xloc < 0) {
                        xloc = useXStep + xloc;
                    }
                    if (yloc < 0) {
                        yloc = useYStep + yloc;
                    }
                    
                    int[] pixel = emptyPixel;
                
                    // check if we are inside the image
                    if (xloc < this.data.getWidth() &&
                        yloc < this.data.getHeight()) {
                        pixel = this.data.getPixel(xloc, yloc, usePixel); 
                    } 
                            
                    int base = (j * w + i) * (numComponents + 1);
                    for (int c = 0; c < pixel.length; c++) {
                        imgData[base + c] = pixel[c];
                    }
                }
            }
            
            WritableRaster raster =
                getColorModel().createCompatibleWritableRaster(w, h);
            raster.setPixels(0, 0, w, h, imgData);
          
            Raster child = raster.createTranslatedChild(x, y);
      
            return child;
        }
    }
}
