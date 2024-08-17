package com.sun.pdfview.decode;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;

/*****************************************************************************
 * Decode image data to a usable color space.
 *
 * @since 25.03.2011
 ****************************************************************************/

public class ImageDataDecoder {
	
	/*************************************************************************
	 * @param bimg
	 * @return
	 ************************************************************************/
    
	static byte[] decodeImageData(BufferedImage bimg) {
		byte[] output = null;
	
		int type = bimg.getType();

		if (type == BufferedImage.TYPE_INT_RGB) {
			// read back the data
			DataBufferInt db = (DataBufferInt) bimg.getData()
					.getDataBuffer();
			int[] data = db.getData();

			output = new byte[data.length * 3];
			for (int i = 0, offset = 0; i < data.length; i++, offset += 3) {
				output[offset] = (byte) (data[i] >> 16);
				output[offset + 1] = (byte) (data[i] >> 8);
				output[offset + 2] = (byte) (data[i]);
			}
		} else if (type == BufferedImage.TYPE_BYTE_GRAY) {
			DataBufferByte db = (DataBufferByte) bimg.getData()
					.getDataBuffer();
			output = db.getData();
		} else if (type == BufferedImage.TYPE_INT_ARGB) {
			// read back the data
			DataBufferInt db = (DataBufferInt) bimg.getData()
					.getDataBuffer();
			int[] data = db.getData();

			output = new byte[data.length * 4];
			for (int i = 0, offset = 0; i < data.length; i++, offset += 4) {
				output[offset] = (byte) (data[i] >> 24);
				output[offset + 1] = (byte) (data[i] >> 16);
				output[offset + 2] = (byte) (data[i] >> 8);
				output[offset + 3] = (byte) (data[i]);
			}
		} else {
			// The raster is in some other format.
			// We have to convert it into TYPE_INT_RGB before we can use it.
			BufferedImage tmp = new BufferedImage(bimg.getWidth(),
					bimg.getHeight(), BufferedImage.TYPE_INT_RGB);
			Graphics g = tmp.createGraphics();
			g.drawImage(bimg, 0, 0, null);
			g.dispose();
			// read back the data
			DataBufferInt db = (DataBufferInt) tmp.getData()
					.getDataBuffer();
			int[] data = db.getData();

			output = new byte[data.length * 3];
			for (int i = 0, offset = 0; i < data.length; i++, offset += 3) {
				output[offset] = (byte) (data[i] >> 16);
				output[offset + 1] = (byte) (data[i] >> 8);
				output[offset + 2] = (byte) (data[i]);
			}
			tmp.flush();
		}
		return output;
	}


}