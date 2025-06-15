package org.openpdf.renderer.annotation;

import java.io.IOException;

import org.openpdf.renderer.PDFObject;

/*****************************************************************************
 * PDF annotation for a square
 *  
 * @author Bernd Rosstauscher
 ****************************************************************************/
public class SquareAnnotation extends MarkupAnnotation {
	
	// TODO Not all of this is fully implemented yet.
	// But it will work if the visual representation is done via an "Appearance Stream" 
	
	/*************************************************************************
	 * Constructor
	 * @param annotObject
	 * @throws IOException 
	 ************************************************************************/
	public SquareAnnotation(PDFObject annotObject) throws IOException {
		super(annotObject, AnnotationType.SQUARE);
	}
}
