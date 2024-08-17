package com.sun.pdfview.colorspace;

import java.awt.color.ColorSpace;

public class CMYKColorSpace extends ColorSpace {

	private static final long serialVersionUID = 1L;

	/**
	 * Create a new CMYKColorSpace instance.
	 */
	public CMYKColorSpace() {
		super(ColorSpace.TYPE_CMYK, 4);
	}

	@Override
	public float[] fromCIEXYZ(float[] colorvalue) {
		// Convert CIEXYZ to RGB first
		ColorSpace sRGB = ColorSpace.getInstance(ColorSpace.CS_sRGB);
		float[] rgb = sRGB.fromCIEXYZ(colorvalue);
		// Convert RGB to CMYK
		return fromRGB(rgb);
	}

	@Override
	public float[] fromRGB(float[] rgbvalue) {
		// Convert RGB to CMY
		float c = 1 - rgbvalue[0];
		float m = 1 - rgbvalue[1];
		float y = 1 - rgbvalue[2];

		// Extract the black key (K)
		float k = Math.min(c, Math.min(m, y));

		// Avoid division by zero and ensure we don't get negative values
		float divisor = (1 - k) == 0 ? 1 : (1 - k);
		float cC = (c - k) / divisor;
		float mC = (m - k) / divisor;
		float yC = (y - k) / divisor;

		return new float[]{cC, mC, yC, k};
	}

	@Override
	public float[] toCIEXYZ(float[] colorvalue) {
		// Convert CMYK to RGB first
		float[] rgb = toRGB(colorvalue);
		// Convert RGB to CIEXYZ
		ColorSpace sRGB = ColorSpace.getInstance(ColorSpace.CS_sRGB);
		return sRGB.toCIEXYZ(rgb);
	}

	@Override
	public float[] toRGB(float[] cmykvalue) {
		float c = cmykvalue[0];
		float m = cmykvalue[1];
		float y = cmykvalue[2];
		float k = cmykvalue[3];

		float r = (1 - c) * (1 - k);
		float g = (1 - m) * (1 - k);
		float b = (1 - y) * (1 - k);

		return new float[]{r, g, b};
	}

	/**
	 * Normalize ensures all color values returned are between 0 and 1.
	 *
	 * @param colors the color values to normalize
	 * @return the normalized color values
	 */
	private float[] normalize(float[] colors) {
		for (int i = 0; i < colors.length; i++) {
			colors[i] = Math.min(1.0f, Math.max(0.0f, colors[i]));
		}
		return colors;
	}
}
