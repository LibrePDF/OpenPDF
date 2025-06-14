package com.sun.pdfview.annotation;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import com.sun.pdfview.Configuration;
import com.sun.pdfview.PDFCmd;
import com.sun.pdfview.PDFObject;
import com.sun.pdfview.PDFParseException;

/*****************************************************************************
 * Encapsulate a PDF annotation. This is only the super-class of PDF annotations, 
 * which has an "unknown" annotation type. 
 * Use the createAnnotation() method for getting an annotation of the correct 
 * type (if implemented).
 *
 * @author  Katja Sondermann
 * @since 03.07.2009
 ****************************************************************************/
public class PDFAnnotation{
	
	/** Definition of some annotation sub-types*/
	public static final String GOTO = "GoTo";
	public static final String GOTOE = "GoToE";
	public static final String GOTOR = "GoToR";
	public static final String URI = "URI";
	
	public enum Flags {
		UNKNOWN,  // 0 
		INVISIBLE, // 1
		HIDDEN, // 2
		PRINT, // 3
		NO_ZOOM, // 4
		NO_ROTATE, // 5
		NO_VIEW, // 6
		READ_ONLY, // 7
		LOCKED, // 8
		TOGGLE_NO_VIEW, // 9
		LOCKED_CONTENTS // 10
	}
	
	
	private final PDFObject pdfObj;
	private final AnnotationType type;
	private final Float rect;
	private final String subType;
	private final String contents;
	
	private final String annotationName;
	private String modified;
	private Integer flags;
	private String appearanceState;

	/*************************************************************************
	 * Constructor
	 * @param annotObject - the PDFObject which contains the annotation description
	 * @throws IOException 
	 ************************************************************************/
	public PDFAnnotation(PDFObject annotObject) throws IOException{
		this(annotObject, AnnotationType.UNKNOWN);
	}

	/*************************************************************************
	 * Constructor
	 * @param annotObject - the PDFObject which contains the annotation description
	 * @throws IOException 
	 ************************************************************************/
	public PDFAnnotation(PDFObject annotObject, AnnotationType type) throws IOException{
		this.pdfObj = annotObject;
		// in case a general "PdfAnnotation" is created the type is unknown
		this.type = type;
		
		this.subType = annotObject.getDictRefAsString("Subtype");
		this.contents = annotObject.getDictRefAsString("Contents");
		this.annotationName = annotObject.getDictRefAsString("NM");
		this.modified = annotObject.getDictRefAsString("M");
		this.flags = annotObject.getDictRefAsInt("F");
		this.appearanceState = annotObject.getDictRefAsString("AS");
		
		// TODO add Border, C, StructParent, OC
		
		this.rect = this.parseRect(annotObject.getDictRef("Rect"));
	}

	/*************************************************************************
	 * Create a new PDF annotation object.
	 * 
	 * Currently supported annotation types:
	 * <li>Link annotation</li>
	 * 
	 * @param parent
	 * @return PDFAnnotation
	 * @throws IOException 
	 ************************************************************************/
	public static PDFAnnotation createAnnotation(PDFObject parent) throws IOException{
		PDFObject subtypeValue = parent.getDictRef("Subtype");
		if(subtypeValue == null) {
			return null;
		}
		String subtypeS = subtypeValue.getStringValue();
		AnnotationType annotationType = AnnotationType.getByDefinition(subtypeS);
		
		//if Subtype is Widget than check if it is also a Signature
		if(annotationType == AnnotationType.WIDGET) {
			PDFObject sigType = parent.getDictRef("FT");
			if(sigType != null) {
				String sigTypeS = sigType.getStringValue();
				if(AnnotationType.getByDefinition(sigTypeS) == AnnotationType.SIGNATURE) {
					annotationType = AnnotationType.getByDefinition(sigTypeS);
				}
			}
		}
		
		if(annotationType.displayAnnotation()) {
			Class<?> className = annotationType.getClassName();
			
			try {
				if (className.equals(MarkupAnnotation.class) || className.equals(TextMarkupAnnotation.class)) {
					Constructor<?> constructor = className.getConstructor(PDFObject.class, AnnotationType.class);
					return (PDFAnnotation)constructor.newInstance(parent, annotationType);
				} else {
					Constructor<?> constructor = className.getConstructor(PDFObject.class);
					return (PDFAnnotation)constructor.newInstance(parent);
				}
			} catch (Exception e) {
				throw new PDFParseException("Could not parse annotation!", e);
			} 
		}
		
		return null;
	}

	/**
     * Get a Rectangle2D.Float representation for a PDFObject that is an
     * array of four Numbers.
     * @param obj a PDFObject that represents an Array of exactly four
     * Numbers.
     */
    public Rectangle2D.Float parseRect(PDFObject obj) throws IOException {
        if (obj.getType() == PDFObject.ARRAY) {
            PDFObject bounds[] = obj.getArray();
            if (bounds.length == 4) {
                return new Rectangle2D.Float(bounds[0].getFloatValue(),
                        bounds[1].getFloatValue(),
                        bounds[2].getFloatValue() - bounds[0].getFloatValue(),
                        bounds[3].getFloatValue() - bounds[1].getFloatValue());
            } else {
                throw new PDFParseException("Rectangle definition didn't have 4 elements");
            }
        } else {
            throw new PDFParseException("Rectangle definition not an array");
        }
    }

	/*************************************************************************
     * Get the PDF Object which contains the annotation values
     * @return PDFObject
     ************************************************************************/
	public PDFObject getPdfObj() {
		return this.pdfObj;
	}

	/*************************************************************************
	 * Get the annotation type
	 * @return int
	 ************************************************************************/
	public AnnotationType getType() {
		return this.type;
	}

	/*************************************************************************
	 * Get the rectangle on which the annotation should be applied to
	 * @return Rectangle2D.Float	
	 ************************************************************************/
	public Float getRect() {
		return this.rect;
	}
	
	public String getSubType() {
		return subType;
	}
	
	public String getAnnotationName() {
		return annotationName;
	}
	
	public String getAppearanceState() {
		return appearanceState;
	}
	
	public String getContents() {
		return contents;
	}
	
	public Integer getFlags() {
		return flags;
	}
	
	public boolean isFlagSet(Flags flag) {
		return flags != null 
				&& BigInteger.valueOf(flags).testBit(flag.ordinal());
	}
	
	public String getModified() {
		return modified;
	}

	@Override
	public String toString() {
		return this.pdfObj.toString();
	}
	
	/**
	 * Get list of pdf commands for this annotation
	 * @return 
	 */
	public List<PDFCmd> getPageCommandsForAnnotation() {
		return new ArrayList<PDFCmd>();
	}
	

	protected AffineTransform getScalingTransformation(Float bbox) {
		AffineTransform at = new AffineTransform();		
		double scaleHeight = getRect().getHeight()/bbox.getHeight();
        double scaleWidth = getRect().getWidth()/bbox.getWidth();
        at.scale(scaleWidth, scaleHeight);
		return at;
	}

}
