package com.sun.pdfview.annotation;

import java.io.IOException;
import java.util.List;

import com.sun.pdfview.PDFCmd;
import com.sun.pdfview.PDFObject;

/*****************************************************************************
 * PDF annotation describing a stamp
 *
 * @author Katja Sondermann
 * @since 26.03.2012
 ****************************************************************************/
public class StampAnnotation extends MarkupAnnotation {

	private String iconName;
	private List<PDFCmd> iconCommands;
	
	/*************************************************************************
	 * Constructor
	 * @param annotObject
	 * @throws IOException 
	 ************************************************************************/
	public StampAnnotation(PDFObject annotObject) throws IOException {
		super(annotObject, AnnotationType.STAMP);
		this.iconName = annotObject.getDictRefAsString("Name");
		
		// No AP so use the icon name
		if (iconName != null && annotObject.getDictRef("AP") == null) {
			parseIconCommands();
		}
		
	}
	
	/**
	 * If the stamp is represented by one of the predefined icons 
	 * this will parse it and create PDFCommands for them.
	 */
	private void parseIconCommands() {
		// TODO Add code for the different icon constants.
		// fill iconCommands
		
		// These command names exist.
		
		// Approved, Experimental, NotApproved, AsIs, Expired , 
		// NotForPublicRelease, Confidential, Final, Sold, 
		// Departmental, ForComment, TopSecret, Draft, ForPublicRelease
	}
	
	/**
	 * @return the iconName
	 */
	public String getIconName() {
		return iconName;
	}
	
	/**
	 *@return the PDF commands to render this annotation
	 */
	@Override
	public List<PDFCmd> getCurrentCommand() {
		List<PDFCmd> apCommand = super.getCurrentCommand();
		if (apCommand != null) {
			return apCommand;
		}
		return this.iconCommands;
	}
	
}
