package com.lowagie.text.rtf.graphic;

import java.io.IOException;
import java.io.OutputStream;

import com.lowagie.text.DocWriter;
import com.lowagie.text.rtf.RtfAddableElement;

/**
 * The RtfShapePosition stores position and ordering
 * information for one RtfShape.
 * 
 * @version $Id: RtfShapePosition.java 3580 2008-08-06 15:52:00Z howard_s $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 * @author Thomas Bickel (tmb99@inode.at)
 */
public class RtfShapePosition extends RtfAddableElement {
    /**
     * Constant for horizontal positioning relative to the page.
     */
	public static final int POSITION_X_RELATIVE_PAGE = 0;
    /**
     * Constant for horizontal positioning relative to the margin.
     */
	public static final int POSITION_X_RELATIVE_MARGIN = 1;
    /**
     * Constant for horizontal positioning relative to the column.
     */
	public static final int POSITION_X_RELATIVE_COLUMN = 2;
    /**
     * Constant for vertical positioning relative to the page.
     */
	public static final int POSITION_Y_RELATIVE_PAGE = 0;
    /**
     * Constant for vertical positioning relative to the margin.
     */
	public static final int POSITION_Y_RELATIVE_MARGIN = 1;
    /**
     * Constant for vertical positioning relative to the paragraph.
     */
	public static final int POSITION_Y_RELATIVE_PARAGRAPH = 2;
	
    /**
     * The top coordinate of this RtfShapePosition.
     */
	private int top = 0;
    /**
     * The left coordinate of this RtfShapePosition.
     */
	private int left = 0;
    /**
     * The right coordinate of this RtfShapePosition.
     */
	private int right = 0;
    /**
     * The bottom coordinate of this RtfShapePosition.
     */
	private int bottom = 0;
    /**
     * The z order of this RtfShapePosition.
     */
	private int zOrder = 0;
    /**
     * The horizontal relative position.
     */
	private int xRelativePos = POSITION_X_RELATIVE_PAGE;
    /**
     * The vertical relative position.
     */
	private int yRelativePos = POSITION_Y_RELATIVE_PAGE;
    /**
     * Whether to ignore the horizontal relative position.
     */
	private boolean ignoreXRelative = false;
    /**
     * Whether to ignore the vertical relative position.
     */
	private boolean ignoreYRelative = false;
    /**
     * Whether the shape is below the text.
     */
	private boolean shapeBelowText = false;

    /**
     * Constructs a new RtfShapePosition with the four bounding coordinates.
     * 
     * @param top The top coordinate.
     * @param left The left coordinate.
     * @param right The right coordinate.
     * @param bottom The bottom coordinate.
     */
	public RtfShapePosition(int top, int left, int right, int bottom) {
		this.top = top;
		this.left = left;
		this.right = right;
		this.bottom = bottom;
	}
	
    /**
     * Gets whether the shape is below the text.
     * 
     * @return <code>True</code> if the shape is below, <code>false</code> if the text is below.
     */
	public boolean isShapeBelowText() {
		return shapeBelowText;
	}

    /**
     * Sets whether the shape is below the text.
     * 
     * @param shapeBelowText <code>True</code> if the shape is below, <code>false</code> if the text is below.
     */
	public void setShapeBelowText(boolean shapeBelowText) {
		this.shapeBelowText = shapeBelowText;
	}

    /**
     * Sets the relative horizontal position. Use one of the constants
     * provided in this class.
     * 
     * @param relativePos The relative horizontal position to use.
     */
	public void setXRelativePos(int relativePos) {
		xRelativePos = relativePos;
	}

    /**
     * Sets the relative vertical position. Use one of the constants
     * provides in this class.
     * 
     * @param relativePos The relative vertical position to use.
     */
	public void setYRelativePos(int relativePos) {
		yRelativePos = relativePos;
	}

    /**
     * Sets the z order to use.
     * 
     * @param order The z order to use.
     */
	public void setZOrder(int order) {
		zOrder = order;
	}

    /**
     * Set whether to ignore the horizontal relative position.
     * 
     * @param ignoreXRelative <code>True</code> to ignore the horizontal relative position, <code>false</code> otherwise.
     */
	protected void setIgnoreXRelative(boolean ignoreXRelative) {
		this.ignoreXRelative = ignoreXRelative;
	}

    /**
     * Set whether to ignore the vertical relative position.
     * 
     * @param ignoreYRelative <code>True</code> to ignore the vertical relative position, <code>false</code> otherwise.
     */
	protected void setIgnoreYRelative(boolean ignoreYRelative) {
		this.ignoreYRelative = ignoreYRelative;
	}

    /**
     * Write this RtfShapePosition.
     */
    public void writeContent(final OutputStream result) throws IOException
    {    	
    	result.write(DocWriter.getISOBytes("\\shpleft"));
    	result.write(intToByteArray(this.left));
    	result.write(DocWriter.getISOBytes("\\shptop"));
    	result.write(intToByteArray(this.top));
    	result.write(DocWriter.getISOBytes("\\shpright"));
    	result.write(intToByteArray(this.right));
    	result.write(DocWriter.getISOBytes("\\shpbottom"));
    	result.write(intToByteArray(this.bottom));
    	result.write(DocWriter.getISOBytes("\\shpz"));
    	result.write(intToByteArray(this.zOrder));
    	switch(this.xRelativePos) {
    	case POSITION_X_RELATIVE_PAGE: result.write(DocWriter.getISOBytes("\\shpbxpage")); break;
    	case POSITION_X_RELATIVE_MARGIN: result.write(DocWriter.getISOBytes("\\shpbxmargin")); break;
    	case POSITION_X_RELATIVE_COLUMN: result.write(DocWriter.getISOBytes("\\shpbxcolumn")); break;
    	}
    	if(this.ignoreXRelative) {
    		result.write(DocWriter.getISOBytes("\\shpbxignore"));
    	}
    	switch(this.yRelativePos) {
    	case POSITION_Y_RELATIVE_PAGE: result.write(DocWriter.getISOBytes("\\shpbypage")); break;
    	case POSITION_Y_RELATIVE_MARGIN: result.write(DocWriter.getISOBytes("\\shpbymargin")); break;
    	case POSITION_Y_RELATIVE_PARAGRAPH: result.write(DocWriter.getISOBytes("\\shpbypara")); break;
    	}
    	if(this.ignoreYRelative) {
    		result.write(DocWriter.getISOBytes("\\shpbyignore"));
    	}
    	if(this.shapeBelowText) {
    		result.write(DocWriter.getISOBytes("\\shpfblwtxt1"));
    	} else {
    		result.write(DocWriter.getISOBytes("\\shpfblwtxt0"));
    	}
    }

}
