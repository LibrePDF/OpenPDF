package org.openpdf.renderer.annotation;

import java.io.IOException;

import org.openpdf.renderer.PDFObject;

/*****************************************************************************
 * PDF annotation for a circle
 *  
 * @author Bernd Rosstauscher
 ****************************************************************************/
public class CircleAnnotation extends MarkupAnnotation {
	
	// TODO Not all of this is fully implemented yet.
	// But it will work if the visual representation is done via an "Appearance Stream" 
	
	/*************************************************************************
	 * Constructor
	 * @param annotObject
	 * @throws IOException 
	 ************************************************************************/
	public CircleAnnotation(PDFObject annotObject) throws IOException {
		super(annotObject, AnnotationType.CIRCLE);
		
	}
}
