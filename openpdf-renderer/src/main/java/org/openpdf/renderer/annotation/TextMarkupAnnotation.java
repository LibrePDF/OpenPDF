package org.openpdf.renderer.annotation;

import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openpdf.renderer.PDFCmd;
import org.openpdf.renderer.PDFObject;
import org.openpdf.renderer.PDFPage;
import org.openpdf.renderer.PDFShapeCmd;

/*****************************************************************************
 * PDF annotation describing a text markup: Highlight, Underline, Squiggle, StrikeOut
 *
 * @author Bernd Rosstauscher
 ****************************************************************************/
public class TextMarkupAnnotation extends MarkupAnnotation {

	private int[] quadPoints;
	private List<PDFCmd> highlightCommands;
	
	/*************************************************************************
	 * Constructor
	 * @param annotObject
	 * @throws IOException 
	 ************************************************************************/
	public TextMarkupAnnotation(PDFObject annotObject, AnnotationType annotationType) throws IOException {
		super(annotObject, annotationType);
		this.quadPoints = annotObject.getDictRefAsIntArray("QuadPoints");
		
		// No AP so use the quad points and highlight mode
		if (annotObject.getDictRef("AP") == null) {
			parseHighlightCommands();
		}
		
	}
	
	/**
	 * Parse the highlight commands 
	 */
	private void parseHighlightCommands() {
		// invalid quad points
		if (this.quadPoints == null || this.quadPoints.length % 4 != 0) {
			return;
		}
		highlightCommands = new ArrayList<PDFCmd>();
		highlightCommands.add(PDFPage.createPushCmd());
		
		//TODO currently we use the same code for: Highlight, Underline, Squiggle, StrikeOut
		// We should also set the correct colors and such.
		
		// Draw a box
		for (int i = 0; i < quadPoints.length; i+=4) {
			GeneralPath gp = new GeneralPath(new Rectangle(quadPoints[i], quadPoints[i+1], quadPoints[i+2], quadPoints[i+3]));
			highlightCommands.add(new PDFShapeCmd(gp, PDFShapeCmd.FILL, true));
		}
		highlightCommands.add(PDFPage.createPopCmd());
	}
	
	/**
	 * Gets the highlight painting commands
	 * Use either Quads or Appearance Stream
	 */
	@Override
	public List<PDFCmd> getCurrentCommand() {
		List<PDFCmd> apCommand = super.getCurrentCommand();
		if (apCommand != null) {
			return apCommand;
		}
		return this.highlightCommands;
	}
	
}
