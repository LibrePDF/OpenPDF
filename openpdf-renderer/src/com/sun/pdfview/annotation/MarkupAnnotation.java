package com.sun.pdfview.annotation;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Float;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.sun.pdfview.PDFCmd;
import com.sun.pdfview.PDFImage;
import com.sun.pdfview.PDFObject;
import com.sun.pdfview.PDFPage;
import com.sun.pdfview.PDFParseException;
import com.sun.pdfview.PDFParser;

/*****************************************************************************
 * PDF annotation describing a all kind of "markup" annotations which are visible 
 * in the PDF.
 *
 * @author Bernd Rosstauscher
 ****************************************************************************/
public class MarkupAnnotation extends PDFAnnotation {

	private PDFObject onAppearance;
	private PDFObject offAppearance;
	private List<PDFCmd> onCmd;
	private List<PDFCmd> offCmd;
	private boolean appearanceStateOn;
	private AnnotationBorderStyle borderStyle; 
	private String textLabel;
	private PDFAnnotation popupAnnotation;

	
	/*************************************************************************
	 * Constructor
	 * @param annotObject
	 * @throws IOException 
	 ************************************************************************/

	public MarkupAnnotation(PDFObject annotObject, AnnotationType type) throws IOException {
		super(annotObject, type);
		
		this.textLabel = annotObject.getDictRefAsString("T");
		// TODO more is missing here like CA, RC, ... 
		
		parsePopupAnnotation(annotObject.getDictRef("Popup"));
		parseAP(annotObject.getDictRef("AP"));
		parseBorderStyleDictionary(annotObject.getDictRef("BS"));
	}
	
	/**
	 * Parses the appearance stream into PDF commands
	 * @param dictRef
	 * @throws IOException
	 */
	protected void parseAP(PDFObject dictRef) throws IOException {
		if(dictRef == null) {
			return;
		}
		PDFObject normalAP = dictRef.getDictRef("N");
		if(normalAP == null) {
			return;
		}
		if(normalAP.getType() == PDFObject.DICTIONARY) {
			this.onAppearance = normalAP.getDictRef("On");
			this.offAppearance = normalAP.getDictRef("Off");
			PDFObject as = dictRef.getDictRef("AS");			
			this.appearanceStateOn = (as != null) && ("On".equals(as.getStringValue()));
		}else {
			this.onAppearance = normalAP;
			this.offAppearance = null;
			appearanceStateOn = true;
		}
		parseOnOffCommands();
	}

	/**
	 * Parses the mouse On or Off appearance stream
	 * depending on which one is currently active.
	 * @throws IOException
	 */
	private void parseOnOffCommands() throws IOException {
		if(onAppearance != null) {
			onCmd = parseIntoPdfCommands(onAppearance);
		}
		if(offAppearance != null) {
			offCmd = parseIntoPdfCommands(offAppearance);
		}
	}
	
	/**
	 * Parses the border style dictionary
	 * @param bs
	 * @throws IOException
	 */
	protected void parseBorderStyleDictionary(PDFObject bs) throws IOException {
		if (bs != null) {
			this.borderStyle = AnnotationBorderStyle.parseFromDictionary(bs);
		}
	}
	
	/**
	 * @return the border style or null if not specified.
	 */
	public AnnotationBorderStyle getBorderStyle() {
		return borderStyle;
	}
	
    /**
     * Parses the popup annotation
     * @param popupObj
     * @throws IOException
     */
    private void parsePopupAnnotation(PDFObject popupObj) throws IOException {
		this.popupAnnotation = (popupObj != null)?createAnnotation(popupObj):null;
	}


	private List<PDFCmd> parseIntoPdfCommands(PDFObject obj) throws IOException {
		// TODO see also WidgetAnnotation.parseCommand which seems to be copied code 
		// We should merge these two
        String type = obj.getDictRef("Subtype").getStringValue();
        if (type == null) {
            type = obj.getDictRef ("S").getStringValue ();
        }
        ArrayList<PDFCmd> result = new ArrayList<PDFCmd>();
        result.add(PDFPage.createPushCmd());
        result.add(PDFPage.createPushCmd());
        if (type.equals("Image")) {
            // stamp annotation transformation
            AffineTransform rectAt = getPositionTransformation();
            result.add(PDFPage.createXFormCmd(rectAt));
            
        	PDFImage img = PDFImage.createImage(obj, new HashMap<String, PDFObject>() , false);        	
        	result.add(PDFPage.createImageCmd(img));
        } else if (type.equals("Form")) {
        	
            // rats.  parse it.
            PDFObject bobj = obj.getDictRef("BBox");
            float xMin = bobj.getAt(0).getFloatValue();
            float yMin = bobj.getAt(1).getFloatValue();
			float xMax = bobj.getAt(2).getFloatValue();
			float yMax = bobj.getAt(3).getFloatValue();
			Float bbox = new Rectangle2D.Float(xMin,
                    yMin,
                    xMax - xMin,
                    yMax - yMin);
            PDFPage formCmds = new PDFPage(bbox, 0);
            
            // stamp annotation transformation
            AffineTransform rectAt = getPositionTransformation();           
           formCmds.addXform(rectAt);
           
           AffineTransform rectScaled = getScalingTransformation(bbox);
           formCmds.addXform(rectScaled);

           // form transformation
            AffineTransform at;
            PDFObject matrix = obj.getDictRef("Matrix");
            if (matrix == null) {
                at = new AffineTransform();
            } else {
                float elts[] = new float[6];
                for (int i = 0; i < elts.length; i++) {
                    elts[i] = (matrix.getAt(i)).getFloatValue();
                }
                at = new AffineTransform(elts);
            }
            formCmds.addXform(at);
            
            HashMap<String,PDFObject> r = new HashMap<String,PDFObject>(new HashMap<String, PDFObject>());
            PDFObject rsrc = obj.getDictRef("Resources");
            if (rsrc != null) {
                r.putAll(rsrc.getDictionary());
            }

            PDFParser form = new PDFParser(formCmds, obj.getStream(), r);
            form.go(true);

            result.addAll(formCmds.getCommands());
        } else {
            throw new PDFParseException("Unknown XObject subtype: " + type);
        }
        result.add(PDFPage.createPopCmd());
        result.add(PDFPage.createPopCmd());
        return result;
	}

	/**
	 * Transform to the position of the stamp annotation
	 * @return
	 */
	private AffineTransform getPositionTransformation() {
		Float rect2 = getRect();
		double[] f = new double[] {1,
				0,
				0,
				1,
				rect2.getMinX(),
				rect2.getMinY()};
		return new AffineTransform(f);
	}

	/**
	 * @return the onAppearance
	 */
	public PDFObject getOnAppearance() {
		return onAppearance;
	}

	/**
	 * @return the offAppearance
	 */
	public PDFObject getOffAppearance() {
		return offAppearance;
	}

	/**
	 * @return the appearanceStateOn
	 */
	public boolean isAppearanceStateOn() {
		return appearanceStateOn;
	}

	public void switchAppearance() {
		this.appearanceStateOn = !this.appearanceStateOn;
	}

	public PDFObject getCurrentAppearance() {
		return appearanceStateOn?onAppearance:offAppearance;
	}

	public List<PDFCmd> getCurrentCommand() {
		return appearanceStateOn?onCmd:offCmd;
	}

	@Override
	public List<PDFCmd> getPageCommandsForAnnotation() {
		List<PDFCmd> pageCommandsForAnnotation = super.getPageCommandsForAnnotation();
		pageCommandsForAnnotation.addAll(getCurrentCommand());
		return pageCommandsForAnnotation;
	}
	
	/**
	 * @return the popupAnnotation
	 */
	public PDFAnnotation getPopupAnnotation() {
		return popupAnnotation;
	}
	
	public String getTextLabel() {
		return textLabel;
	}

}
