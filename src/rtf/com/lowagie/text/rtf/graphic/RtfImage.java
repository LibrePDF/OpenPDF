/*
 * $Id: RtfImage.java 4065 2009-09-16 23:09:11Z psoares33 $
 *
 * Copyright 2001, 2002, 2003, 2004 by Mark Hall
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
 * LGPL license (the ?GNU LIBRARY GENERAL PUBLIC LICENSE?), in which case the
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

package com.lowagie.text.rtf.graphic;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import com.lowagie.text.error_messages.MessageLocalization;

import com.lowagie.text.DocWriter;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.codec.wmf.MetaDo;
import com.lowagie.text.rtf.RtfElement;
import com.lowagie.text.rtf.document.RtfDocument;
import com.lowagie.text.rtf.document.output.RtfByteArrayBuffer;
import com.lowagie.text.rtf.style.RtfParagraphStyle;
import com.lowagie.text.rtf.text.RtfParagraph;

/**
 * The RtfImage contains one image. Supported image types are jpeg, png, wmf, bmp.
 * 
 * @version $Id: RtfImage.java 4065 2009-09-16 23:09:11Z psoares33 $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 * @author Paulo Soares
 * @author Thomas Bickel (tmb99@inode.at)
 */
public class RtfImage extends RtfElement {
    
    /**
     * Constant for the shape/picture group
     */
    private static final byte[] PICTURE_GROUP = DocWriter.getISOBytes("\\*\\shppict");
    /**
     * Constant for a picture
     */
    private static final byte[] PICTURE = DocWriter.getISOBytes("\\pict");
    /**
     * Constant for a jpeg image
     */
    private static final byte[] PICTURE_JPEG = DocWriter.getISOBytes("\\jpegblip");
    /**
     * Constant for a png image
     */
    private static final byte[] PICTURE_PNG = DocWriter.getISOBytes("\\pngblip");
    /**
     * Constant for a wmf image
     */
    private static final byte[] PICTURE_WMF = DocWriter.getISOBytes("\\wmetafile8");
    /**
     * Constant for the picture width
     */
    private static final byte[] PICTURE_WIDTH = DocWriter.getISOBytes("\\picw");
    /**
     * Constant for the picture height
     */
    private static final byte[] PICTURE_HEIGHT = DocWriter.getISOBytes("\\pich");
    /**
     * Constant for the picture width scale
     */
    private static final byte[] PICTURE_SCALED_WIDTH = DocWriter.getISOBytes("\\picwgoal");
    /**
     * Constant for the picture height scale
     */
    private static final byte[] PICTURE_SCALED_HEIGHT = DocWriter.getISOBytes("\\pichgoal");
    /**
     * Constant for horizontal picture scaling
     */
    private static final byte[] PICTURE_SCALE_X = DocWriter.getISOBytes("\\picscalex");
    /**
     * Constant for vertical picture scaling
     */
    private static final byte[] PICTURE_SCALE_Y = DocWriter.getISOBytes("\\picscaley");
    /**
     * "\bin" constant
     */
    private static final byte[] PICTURE_BINARY_DATA = DocWriter.getISOBytes("\\bin");
    /**
     * Constant for converting pixels to twips
     */
    private static final int PIXEL_TWIPS_FACTOR = 15;
    
    /**
     * The type of image this is.
     */
    private final int imageType;
    /**
     * Binary image data.
     */
    private final byte[][] imageData;
    /**
     * The alignment of this picture
     */
    private int alignment = Element.ALIGN_LEFT;
    /**
     * The width of this picture
     */
    private float width = 0;
    /**
     * The height of this picture
     */
    private float height = 0;
    /**
     * The intended display width of this picture
     */
    private float plainWidth = 0;
    /**
     * The intended display height of this picture
     */
    private float plainHeight = 0;
    /**
     * Whether this RtfImage is a top level element and should
     * be an extra paragraph.
     */
    private boolean topLevelElement = false;
    
    /**
     * Constructs a RtfImage for an Image.
     * 
     * @param doc The RtfDocument this RtfImage belongs to
     * @param image The Image that this RtfImage wraps
     * @throws DocumentException If an error occurred accessing the image content
     */
    public RtfImage(RtfDocument doc, Image image) throws DocumentException
    {
        super(doc);
        imageType = image.getOriginalType();
        if (!(imageType == Image.ORIGINAL_JPEG || imageType == Image.ORIGINAL_BMP
                || imageType == Image.ORIGINAL_PNG || imageType == Image.ORIGINAL_WMF || imageType == Image.ORIGINAL_GIF)) {
            throw new DocumentException(MessageLocalization.getComposedMessage("only.bmp.png.wmf.gif.and.jpeg.images.are.supported.by.the.rtf.writer"));
        }
        alignment = image.getAlignment();
        width = image.getWidth();
        height = image.getHeight();
        plainWidth = image.getPlainWidth();
        plainHeight = image.getPlainHeight();
        this.imageData = getImageData(image);
    }
    
    /**
     * Extracts the image data from the Image.
     * 
     * @param image The image for which to extract the content
     * @return The raw image data, not formated
     * @throws DocumentException If an error occurs accessing the image content
     */
    private byte[][] getImageData(Image image) throws DocumentException 
    {
    	final int WMF_PLACEABLE_HEADER_SIZE = 22;
        final RtfByteArrayBuffer bab = new RtfByteArrayBuffer();
        
        try {
            if(imageType == Image.ORIGINAL_BMP) {
            	bab.append(MetaDo.wrapBMP(image));
            } else {            	
            	final byte[] iod = image.getOriginalData();
            	if(iod == null) {
            		
                	final InputStream imageIn = image.getUrl().openStream();
                    if(imageType == Image.ORIGINAL_WMF) { //remove the placeable header first
                    	for(int k = 0; k < WMF_PLACEABLE_HEADER_SIZE; k++) {
							if(imageIn.read() < 0) throw new EOFException(MessageLocalization.getComposedMessage("while.removing.wmf.placeable.header"));
						}
                    }
                    bab.write(imageIn);
                	imageIn.close();
                    
                } else {
                	
                	if(imageType == Image.ORIGINAL_WMF) {
                		//remove the placeable header                		
                		bab.write(iod, WMF_PLACEABLE_HEADER_SIZE, iod.length - WMF_PLACEABLE_HEADER_SIZE);
                	} else {
                		bab.append(iod);
                	}
                	
                }
            }
            
            return bab.toByteArrayArray();
            
        } catch(IOException ioe) {
            throw new DocumentException(ioe.getMessage());
        }
    }
    
    
    /**
     * lookup table used for converting bytes to hex chars.
     * TODO Should probably be refactored into a helper class
     */
    public final static byte[] byte2charLUT = new byte[512]; //'0001020304050607 ... fafbfcfdfeff'
    static {
    	char c = '0';
    	for(int k = 0; k < 16; k++) {
    		for(int x = 0; x < 16; x++) {
				byte2charLUT[((k*16)+x)*2] = byte2charLUT[(((x*16)+k)*2)+1] = (byte)c;
			}
    		if(++c == ':') c = 'a';
		}
    }
    
    /**
     * Writes the image data to the given buffer as hex encoded text.
     * 
     * @param bab
     * @throws IOException
     */
    private void writeImageDataHexEncoded(final OutputStream bab) throws IOException
    {
    	int cnt = 0;
    	for(int k = 0; k < imageData.length; k++) {
    		final byte[] chunk = imageData[k];
			for(int x = 0; x < chunk.length; x++) {
				bab.write(byte2charLUT, (chunk[x]&0xff)*2, 2);
				if(++cnt == 64) {
					bab.write('\n');
					cnt = 0;
				}
			}
		}    	
   		if(cnt > 0) bab.write('\n');
    }
    
    /**
     * Returns the image raw data size in bytes.
     * 
     * @return the size in bytes
     */
    private int imageDataSize()
    {
		int size = 0;
    	for(int k = 0; k < imageData.length; k++) {
    		size += imageData[k].length;
    	}   
    	return size;
    }
    
    /**
     * Writes the RtfImage content
     */ 
    public void writeContent(final OutputStream result) throws IOException
    {
    	
        if(this.topLevelElement) {
            result.write(RtfParagraph.PARAGRAPH_DEFAULTS);
            switch(alignment) {
                case Element.ALIGN_LEFT:
                    result.write(RtfParagraphStyle.ALIGN_LEFT);
                    break;
                case Element.ALIGN_RIGHT:
                    result.write(RtfParagraphStyle.ALIGN_RIGHT);
                    break;
                case Element.ALIGN_CENTER:
                    result.write(RtfParagraphStyle.ALIGN_CENTER);
                    break;
                case Element.ALIGN_JUSTIFIED:
                    result.write(RtfParagraphStyle.ALIGN_JUSTIFY);
                    break;
            }
        }
        result.write(OPEN_GROUP);
        result.write(PICTURE_GROUP);
        result.write(OPEN_GROUP);
        result.write(PICTURE);
        switch(imageType) {
        	case Image.ORIGINAL_JPEG:
        	    result.write(PICTURE_JPEG);
        		break;
        	case Image.ORIGINAL_PNG:
            case Image.ORIGINAL_GIF:
        	    result.write(PICTURE_PNG);
        		break;
        	case Image.ORIGINAL_WMF:
        	case Image.ORIGINAL_BMP:
        	    result.write(PICTURE_WMF);
        		break;
        }
        result.write(PICTURE_WIDTH);
        result.write(intToByteArray((int) width));
        result.write(PICTURE_HEIGHT);
        result.write(intToByteArray((int) height));
        if(this.document.getDocumentSettings().isWriteImageScalingInformation()) {
            result.write(PICTURE_SCALE_X);
            result.write(intToByteArray((int)(100 * plainWidth / width)));
            result.write(PICTURE_SCALE_Y);
            result.write(intToByteArray((int)(100 * plainHeight / height)));
        }
        if(this.document.getDocumentSettings().isImagePDFConformance()) {
            result.write(PICTURE_SCALED_WIDTH);
            result.write(intToByteArray((int) (plainWidth * RtfElement.TWIPS_FACTOR)));
            result.write(PICTURE_SCALED_HEIGHT);
            result.write(intToByteArray((int) (plainHeight * RtfElement.TWIPS_FACTOR)));
        } else {
            if(this.width != this.plainWidth || this.imageType == Image.ORIGINAL_BMP) {
                result.write(PICTURE_SCALED_WIDTH);
                result.write(intToByteArray((int) (plainWidth * PIXEL_TWIPS_FACTOR)));
            }
            if(this.height != this.plainHeight || this.imageType == Image.ORIGINAL_BMP) {
                result.write(PICTURE_SCALED_HEIGHT);
                result.write(intToByteArray((int) (plainHeight * PIXEL_TWIPS_FACTOR)));
            }
        }

        if(this.document.getDocumentSettings().isImageWrittenAsBinary()) {
        	//binary
        	result.write('\n');
        	result.write(PICTURE_BINARY_DATA);
        	result.write(intToByteArray(imageDataSize()));
            result.write(DELIMITER);
            if(result instanceof RtfByteArrayBuffer) {
            	((RtfByteArrayBuffer)result).append(imageData);
            } else {
            	for(int k = 0; k < imageData.length; k++) {
					result.write(imageData[k]);
				}
            }
        } else {
        	//hex encoded
            result.write(DELIMITER);
        	result.write('\n');
        	writeImageDataHexEncoded(result);
        }
        
        result.write(CLOSE_GROUP);
        result.write(CLOSE_GROUP);
        if(this.topLevelElement) {
            result.write(RtfParagraph.PARAGRAPH);
            result.write(RtfParagraph.PARAGRAPH);
        }
        result.write('\n');    	
    }
    
    /**
     * Sets the alignment of this RtfImage. Uses the alignments from com.lowagie.text.Element.
     * 
     * @param alignment The alignment to use.
     */
    public void setAlignment(int alignment) {
        this.alignment = alignment;
    }
    
    /**
     * Set whether this RtfImage should behave like a top level element
     * and enclose itself in a paragraph.
     * 
     * @param topLevelElement Whether to behave like a top level element.
     */
    public void setTopLevelElement(boolean topLevelElement) {
        this.topLevelElement = topLevelElement;
    }
    
    
}
