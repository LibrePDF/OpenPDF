package com.sun.pdfview.colorspace;

import java.awt.color.ColorSpace;

import com.sun.pdfview.function.PDFFunction;

/*****************************************************************************
 * Color Space implementation for handling the PDF AlternateColorSpace.
 * A PDF function is applied to colorvalues before converting.
 *
 * @author  Katja Sondermann
 * @since 06.01.2011
 ****************************************************************************/
public class AltColorSpace extends ColorSpace {

	private PDFFunction fkt;
	private ColorSpace origCs;
	/**
	 * Create a new CMYKColorSpace Instance.
	 */
	public AltColorSpace(PDFFunction fkt, ColorSpace origCs) {
		super(origCs.getType(), fkt.getNumInputs());
		this.fkt = fkt;
		this.origCs = origCs;
	}

	/**
	 * Converts from CIEXYZ. 
	 * 
	 * @see java.awt.color.ColorSpace#fromCIEXYZ(float[])
	 * @see org.scantegrity.lib.CMYKColorSpace#toCIEXYZ
	 */
	@Override
	public float[] fromCIEXYZ(float[] p_colorvalue) {
		p_colorvalue = this.fkt.calculate(p_colorvalue);
		return this.origCs.fromCIEXYZ(p_colorvalue);
	}

	/**
	 * Converts a given RGB. 
	 *  
	 * @param p_rgbvalue - The color to translate 
	 * @return a float[4] of the CMYK values.
	 * @see java.awt.color.ColorSpace#fromRGB(float[])
	 */
	@Override
	public float[] fromRGB(float[] p_rgbvalue) {
		p_rgbvalue = this.fkt.calculate(p_rgbvalue);
		return this.origCs.fromCIEXYZ(p_rgbvalue);
	}

	/** 
	 * Converts to CIEXYZ. 
	 * @see java.awt.color.ColorSpace#toCIEXYZ(float[])
	 */
	@Override
	public float[] toCIEXYZ(float[] p_colorvalue) {
		float[] colorvalue = this.fkt.calculate(p_colorvalue);
		return this.origCs.toCIEXYZ(colorvalue);
	}

	/**
	 * Converts to RGB. 
	 * 
	 * @param p_colorvalue The color in CMYK.
	 * @see java.awt.color.ColorSpace#toRGB(float[])
	 */
	@Override
	public float[] toRGB(float[] p_colorvalue) {
		float[] colorvalue = this.fkt.calculate(p_colorvalue);
		return this.origCs.toRGB(colorvalue);
	}
}
