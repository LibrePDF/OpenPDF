package com.github.librepdf.pdfrenderer.decode;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.github.librepdf.pdfrenderer.PDFObject;
import com.github.librepdf.pdfrenderer.PDFParseException;

/**
 * Decode a DCT encoded array into a byte array. This class uses Java's
 * built-in JPEG image class to do the decoding.
 *
 * @author Mike Wessler
 */
public class DCTDecode {

	/**
	 * Decode an array of bytes in DCT format.
	 * <p>
	 * DCT is the format used by JPEG images, so this class simply
	 * loads the DCT-format bytes as an image, then reads the bytes out
	 * of the image to create the array. Unfortunately, their most
	 * likely use is to get turned BACK into an image, so this isn't
	 * terribly efficient... but is general... don't hit, please.
	 * <p>
	 * The DCT-encoded stream may have 1, 3, or 4 samples per pixel, depending
	 * on the colorspace of the image. In decoding, we look for the colorspace
	 * in the stream object's dictionary to decide how to decode this image.
	 * If no colorspace is present, we guess 3 samples per pixel.
	 *
	 * @param dict the stream dictionary
	 * @param buf the DCT-encoded buffer
	 * @param params the parameters to the decoder (ignored)
	 * @return the decoded buffer
	 * @throws PDFParseException
	 */
	protected static ByteBuffer decode(PDFObject dict, ByteBuffer buf, PDFObject params) throws PDFParseException {
		BufferedImage bimg = loadImageData(buf);
		byte[] output = ImageDataDecoder.decodeImageData(bimg);
		return ByteBuffer.wrap(output);
	}

	/**
	 * Load image data from the buffer.
	 *
	 * @param buf the buffer containing the image data
	 * @return a BufferedImage representing the image
	 * @throws PDFParseException if an error occurs during image loading
	 */
	private static BufferedImage loadImageData(ByteBuffer buf) throws PDFParseException {
		buf.rewind();
		byte[] input = new byte[buf.remaining()];
		buf.get(input);
		BufferedImage bimg;
		try (ByteArrayInputStream bais = new ByteArrayInputStream(input)) {
			try {
				bimg = ImageIO.read(bais);
				if (bimg == null) {
					throw new PDFParseException("DCTDecode failed: ImageIO.read returned null");
				}
			} catch (IOException ex) {
				// If there's an issue reading the image, attempt to load it another way
				Image img = Toolkit.getDefaultToolkit().createImage(input);
				// Wait until the image is fully loaded
				ImageIcon imageIcon = new ImageIcon(img);
				// Copy to buffered image
				bimg = new BufferedImage(imageIcon.getIconWidth(), imageIcon.getIconHeight(), BufferedImage.TYPE_INT_RGB);
				bimg.getGraphics().drawImage(img, 0, 0, null);
			}
		} catch (Exception ex) {
			PDFParseException ex2 = new PDFParseException("DCTDecode failed");
			ex2.initCause(ex);
			throw ex2;
		}

		return bimg;
	}
}

/**
 * Image tracker. This class tracks the status of an image loading operation.
 */
class MyTracker implements ImageObserver {
	boolean done = false;

	/**
	 * Create a new MyTracker that watches this image. The image will start loading immediately.
	 */
	public MyTracker(Image img) {
		img.getWidth(this);
	}

	/**
	 * More information has come in about the image.
	 */
	@Override
	public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
		if ((infoflags & (ALLBITS | ERROR | ABORT)) != 0) {
			synchronized (this) {
				this.done = true;
				notifyAll();
			}
			return false;
		}
		return true;
	}

	/**
	 * Wait until the image is done, then return.
	 */
	public synchronized void waitForAll() {
		if (!this.done) {
			try {
				wait();
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
		}
	}
}
