package com.sun.pdfview.annotation;

import java.io.IOException;

import com.sun.pdfview.PDFObject;

/**
 * Annotation border style 
 * @author Bernd Rosstauscher
 */
public class AnnotationBorderStyle {
	
	public enum BorderStyle {
		SOLID("S"), 
		DASHED("D"), 
		BEVELED("B"), 
		INSET("I"), 
		UNDERLINE("U");

		private String code;
		
		private BorderStyle(String code) {
			this.code = code;
		}
		
		public String getCode() {
			return code;
		}
		
		public static BorderStyle fromCode(String code) {
			for (BorderStyle bs : BorderStyle.values()) {
				if (bs.getCode().equals(code)) {
					return bs;
				}
			}
			return SOLID;
		}
		
	}
	
	private Integer width;
	private BorderStyle borderStyle;
	private int[] dashArray;
	
	/**
	 * Creates a annotation border style
	 */
	public AnnotationBorderStyle() {
		super();
	}
	
	/**
	 * Parse a border style from a BS dictionary.
	 * @param bs the pdf dictionary to parse.
	 * @return the border style object.
	 * @throws IOException
	 */
	public static AnnotationBorderStyle parseFromDictionary(PDFObject bs) throws IOException {
		AnnotationBorderStyle result = new AnnotationBorderStyle();
		result.width = bs.getDictRefAsInt("W");
		result.borderStyle = BorderStyle.fromCode(bs.getDictRefAsString("S"));
		result.dashArray = bs.getDictRefAsIntArray("D");
		// TODO BE Border effect not supported yet
		return result;
	}
	
	/**
	 * @return the border style enum
	 */
	public BorderStyle getBorderStyle() {
		return borderStyle;
	}
	
	/**
	 * @return the width of the border line
	 */
	public Integer getWidth() {
		return width;
	}
	
	/**
	 * @return specifying the dash for the border line
	 */
	public int[] getDashArray() {
		return dashArray;
	}

}
