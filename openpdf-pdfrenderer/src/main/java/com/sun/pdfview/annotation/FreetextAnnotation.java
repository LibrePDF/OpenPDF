package com.sun.pdfview.annotation;

import java.io.IOException;

import com.sun.pdfview.PDFObject;

/*****************************************************************************
 * PDF annotation describing a free text
 * Currently only supports the XObjects which can be found in the path AP->N
 * of the annotation object (same implementation as the stamp annotation) 
 * @author Katja Sondermann
 * @since 28.03.2012
 ****************************************************************************/
public class FreetextAnnotation extends MarkupAnnotation {
	
	/*************************************************************************
	 * Constructor
	 * @param annotObject
	 * @throws IOException 
	 ************************************************************************/
	public FreetextAnnotation(PDFObject annotObject) throws IOException {
		super(annotObject, AnnotationType.FREETEXT);
	}
}
