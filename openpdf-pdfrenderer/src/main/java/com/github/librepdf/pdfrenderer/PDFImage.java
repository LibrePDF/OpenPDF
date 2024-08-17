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

package com.github.librepdf.pdfrenderer;

import java.awt.*;
import java.awt.image.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;

import com.github.librepdf.pdfrenderer.colorspace.IndexedColor;
import com.github.librepdf.pdfrenderer.colorspace.PDFColorSpace;
import com.github.librepdf.pdfrenderer.decode.PDFDecoder;

public class PDFImage {

	private int[] colorKeyMask = null;
	private int width;
	private int height;
	private PDFColorSpace colorSpace;
	private int bpc;
	private boolean imageMask = false;
	private PDFImage sMask;
	private float[] decode;
	private final PDFObject imageObj;
	private final boolean jpegDecode;

	protected PDFImage(PDFObject imageObj) throws IOException {
		this.imageObj = imageObj;
		this.jpegDecode = PDFDecoder.isLastFilter(imageObj, PDFDecoder.DCT_FILTERS);
	}

	public static PDFImage createImage(PDFObject obj, Map<String, PDFObject> resources, boolean useAsSMask)
			throws IOException {
		PDFImage image = new PDFImage(obj);
		image.setWidth(obj.getDictRef("Width").getIntValue());
		image.setHeight(obj.getDictRef("Height").getIntValue());

		if (obj.getDictRef("ImageMask") != null) {
			image.setImageMask(obj.getDictRef("ImageMask").getBooleanValue());
			image.setBitsPerComponent(1);
			image.setColorSpace(new IndexedColor(useAsSMask ? new Color[]{Color.WHITE, Color.BLACK} : new Color[]{Color.BLACK, Color.WHITE}));
		} else {
			image.setBitsPerComponent(obj.getDictRef("BitsPerComponent").getIntValue());
			image.setColorSpace(PDFColorSpace.getColorSpace(obj.getDictRef("ColorSpace"), resources));
			if (obj.getDictRef("Decode") != null) {
				image.setDecode(parseDecodeArray(obj.getDictRef("Decode").getArray()));
			}
			if (obj.getDictRef("SMask") != null) {
				image.setSMask(PDFImage.createImage(obj.getDictRef("SMask"), resources, true));
			}
		}
		return image;
	}

	public BufferedImage getImage() throws PDFImageParseException {
		try {
			BufferedImage bi = (BufferedImage) this.imageObj.getCache();
			if (bi == null) {
				byte[] data = imageObj.getStream();
				ByteBuffer jpegBytes = this.jpegDecode ? imageObj.getStreamBuffer(PDFDecoder.DCT_FILTERS) : null;
				bi = parseData(data, jpegBytes);
				this.imageObj.setCache(bi);
			}
			return bi;
		} catch (IOException ioe) {
			throw new PDFImageParseException("Error reading image: " + ioe.getMessage(), ioe);
		}
	}

	protected BufferedImage parseData(byte[] data, ByteBuffer jpegData) throws IOException {
		ColorModel cm = createColorModel();
		BufferedImage bi;

		if (jpegData != null) {
			bi = decodeJPEG(jpegData, cm);
		} else {
			DataBuffer db = new DataBufferByte(data, data.length);

			// Check if the data array is large enough
			SampleModel sm = cm.createCompatibleSampleModel(getWidth(), getHeight());
			int expectedSize = sm.getWidth() * sm.getHeight() * sm.getNumBands();
			if (data.length < expectedSize) {
				throw new IOException("Data array too small, expected at least " + expectedSize + " bytes.");
			}

			WritableRaster raster = Raster.createWritableRaster(sm, db, null);
			bi = new BufferedImage(cm, raster, true, null);
		}

		if (sMask != null) {
			bi = applySMask(bi, sMask.getImage());
		}

		return bi;
	}


	private BufferedImage decodeJPEG(ByteBuffer jpegData, ColorModel cm) throws IOException {
		ImageReader jpegReader = ImageIO.getImageReadersByFormatName("jpeg").next();
		jpegReader.setInput(ImageIO.createImageInputStream(new ByteBufferInputStream(jpegData)), true, false);
		ImageReadParam param = new ImageReadParam();
		param.setDestination(new BufferedImage(cm, cm.createCompatibleWritableRaster(getWidth(), getHeight()), true, null));
		return jpegReader.read(0, param);
	}

	private BufferedImage applySMask(BufferedImage bi, BufferedImage smask) {
		int w = bi.getWidth();
		int h = bi.getHeight();
		BufferedImage combined = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int rgb = bi.getRGB(x, y);
				int alpha = smask.getRGB(x, y) & 0xFF;
				combined.setRGB(x, y, (alpha << 24) | (rgb & 0xFFFFFF));
			}
		}
		return combined;
	}

	private ColorModel createColorModel() {
		if (colorSpace instanceof IndexedColor) {
			return createIndexedColorModel((IndexedColor) colorSpace);
		} else {
			int[] bits = new int[colorSpace.getNumComponents()];
			for (int i = 0; i < bits.length; i++) {
				bits[i] = bpc;
			}
			return new ComponentColorModel(colorSpace.getColorSpace(), bits, false, false, Transparency.OPAQUE, DataBuffer.TYPE_BYTE);
		}
	}

	private ColorModel createIndexedColorModel(IndexedColor ics) {
		byte[] components = ics.getColorComponents();
		int num = ics.getCount();

		if (decode != null) {
			components = applyDecodeArray(components, decode);
		}

		return new IndexColorModel(bpc, num, components, 0, false);
	}

	private static byte[] applyDecodeArray(byte[] components, float[] decode) {
		byte[] normComps = new byte[components.length];
		for (int i = 0; i < components.length / 3; i++) {
			int idx = (int) ((decode[i * 2] * (components.length - 1)) / (decode[(i * 2) + 1] - decode[i * 2]));
			normComps[i * 3] = components[idx * 3];
			normComps[(i * 3) + 1] = components[(idx * 3) + 1];
			normComps[(i * 3) + 2] = components[(idx * 3) + 2];
		}
		return normComps;
	}

	private static float[] parseDecodeArray(PDFObject[] decodeArray) {
		float[] decode = new float[decodeArray.length];
		for (int i = 0; i < decodeArray.length; i++) {
            try {
                decode[i] = decodeArray[i].getFloatValue();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
		return decode;
	}

	public int getWidth() {
		return this.width;
	}

	protected void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return this.height;
	}

	protected void setHeight(int height) {
		this.height = height;
	}

	protected void setColorSpace(PDFColorSpace colorSpace) {
		this.colorSpace = colorSpace;
	}

	protected void setBitsPerComponent(int bpc) {
		this.bpc = bpc;
	}

	public boolean isImageMask() {
		return this.imageMask;
	}

	public void setImageMask(boolean imageMask) {
		this.imageMask = imageMask;
	}

	public PDFImage getSMask() {
		return this.sMask;
	}

	protected void setSMask(PDFImage sMask) {
		this.sMask = sMask;
	}

	protected void setDecode(float[] decode) {
		this.decode = decode;
	}
}
