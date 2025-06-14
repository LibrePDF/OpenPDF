package com.sun.pdfview.annotation;

import com.sun.pdfview.Configuration;

/**
 * @author Bernd Rosstauscher
 *
 */
public enum AnnotationType{
	UNKNOWN("-", 0, PDFAnnotation.class),
	LINK("Link", 1, LinkAnnotation.class),
	WIDGET("Widget", 2, WidgetAnnotation.class),
	STAMP("Stamp", 3, StampAnnotation.class),
	FREETEXT("FreeText", 5, FreetextAnnotation.class),
	SIGNATURE("Sig", 6, WidgetAnnotation.class),

	// added more annotation types. Most of them only with basic features
	// We render them all via the base class MarkupAnnotation
	
	TEXT("Text", 7, MarkupAnnotation.class),
	LINE("Line", 8, MarkupAnnotation.class),
	SQUARE("Square", 9, SquareAnnotation.class),
	CIRCLE("Circle", 10, CircleAnnotation.class),
	POLYGON("Polygon", 11, MarkupAnnotation.class),
	POLYLINE("PolyLine", 12, MarkupAnnotation.class),
	HIGHLIGHT("Highlight", 13, TextMarkupAnnotation.class),
	UNDERLINE("Underline", 14, TextMarkupAnnotation.class),
	SQUIGGLY("Squiggly", 15, TextMarkupAnnotation.class),
	STRIKEOUT("StrikeOut", 16, TextMarkupAnnotation.class),
	CARET("Caret", 17, MarkupAnnotation.class),
	INK("Ink", 18, MarkupAnnotation.class),
	//POPUP("Popup", 19, MarkupAnnotation.class),
	FILEATTACHMENT("FileAttachment", 20, PDFAnnotation.class),
	SOUND("Sound", 21, PDFAnnotation.class),
	MOVIE("Movie", 22, PDFAnnotation.class),
	SCREEN("Screen", 23, PDFAnnotation.class),
	PRINTERMARK("PrinterMark", 24, PDFAnnotation.class),
	TRAPNET("TrapNet", 25, PDFAnnotation.class),
	WATERMARK("Watermark", 26, PDFAnnotation.class),
	THREED("3D", 27, PDFAnnotation.class),
	REDACT("Redact", 28, MarkupAnnotation.class),
	;
	
	 /**
	 * @return true if this annotation type should be displayed else false.
	 */
	boolean displayAnnotation() {
			switch(this) {
				case STAMP: return Configuration.getInstance().isPrintStampAnnotations();
				case WIDGET: return Configuration.getInstance().isPrintWidgetAnnotations();
				case FREETEXT: return Configuration.getInstance().isPrintFreetextAnnotations();
				case LINK: return Configuration.getInstance().isPrintLinkAnnotations();
				case SIGNATURE: return Configuration.getInstance().isPrintSignatureFields();
				case UNKNOWN: return false;
				default: {
					// Fallback for all the annotation types that are currently mapped to MarkupAnnotation
					return MarkupAnnotation.class.isAssignableFrom(this.className) 
							&& Configuration.getInstance().isPrintFreetextAnnotations();
				}
			}
		}
	
	private String definition; 
	private int internalId;
	private Class<?> className;
	
	private AnnotationType(String definition, int typeId, Class<?> className) {
		this.definition = definition;
		this.internalId = typeId;
		this.className = className;
	}
		
	/**
	 * @return the definition
	 */
	public String getDefinition() {
		return definition;
	}
	/**
	 * @return the internalId
	 */
	public int getInternalId() {
		return internalId;
	}
	
	/**
	 * @return the className
	 */
	public Class<?> getClassName() {
		return className;
	}
	
	/**
	 * Get annotation type by it's type 
	 * @param definition
	 * @return
	 */
	public static AnnotationType getByDefinition(String definition) {
		for (AnnotationType type : values()) {
			if(type.definition.equals(definition)) {
				return type;
			}
		}
		return UNKNOWN;
	}		
}