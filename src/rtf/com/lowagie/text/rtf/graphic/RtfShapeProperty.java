package com.lowagie.text.rtf.graphic;

import java.awt.Color;
import java.awt.Point;
import java.io.IOException;
import java.io.OutputStream;

import com.lowagie.text.DocWriter;
import com.lowagie.text.DocumentException;
import com.lowagie.text.ExceptionConverter;
import com.lowagie.text.Image;
import com.lowagie.text.rtf.RtfAddableElement;

/**
 * The RtfShapeProperty stores all shape properties that are
 * not handled by the RtfShape and RtfShapePosition.<br /><br />
 * 
 * There is a huge selection of properties that can be set. For
 * the most important properites there are constants for the
 * property name, for all others you must find the correct
 * property name in the RTF specification (version 1.6).<br /><br />
 * 
 * The following types of property values are supported:
 * <ul>
 *   <li>long</li>
 *   <li>double</li>
 *   <li>boolean</li>
 *   <li>Color</li>
 *   <li>int[]</li>
 *   <li>Point[]</li>
 * </ul>
 * 
 * @version $Id: RtfShapeProperty.java 3580 2008-08-06 15:52:00Z howard_s $
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 * @author Thomas Bickel (tmb99@inode.at)
 */
public class RtfShapeProperty extends RtfAddableElement {
    /**
    * Property for defining an image.
    */
    public static final String PROPERTY_IMAGE = "pib";
    /**
     * Property for defining vertices in freeform shapes. Requires a
     * Point array as the value.
     */
    public static final String PROPERTY_VERTICIES = "pVerticies";
    /**
     * Property for defining the minimum vertical coordinate that is
     * visible. Requires a long value.
     */
    public static final String PROPERTY_GEO_TOP = "geoTop";
    /**
     * Property for defining the minimum horizontal coordinate that is
     * visible. Requires a long value.
     */
    public static final String PROPERTY_GEO_LEFT = "geoLeft";
    /**
     * Property for defining the maximum horizontal coordinate that is
     * visible. Requires a long value.
     */
    public static final String PROPERTY_GEO_RIGHT = "geoRight";
    /**
     * Property for defining the maximum vertical coordinate that is
     * visible. Requires a long value.
     */
    public static final String PROPERTY_GEO_BOTTOM = "geoBottom";
    /**
     * Property for defining that the shape is in a table cell. Requires
     * a boolean value.
     */
    public static final String PROPERTY_LAYOUT_IN_CELL = "fLayoutInCell";
    /**
     * Property for signaling a vertical flip of the shape. Requires a
     * boolean value.
     */
    public static final String PROPERTY_FLIP_V = "fFlipV";
    /**
     * Property for signaling a horizontal flip of the shape. Requires a
     * boolean value.
     */
    public static final String PROPERTY_FLIP_H = "fFlipH";
    /**
     * Property for defining the fill color of the shape. Requires a
     * Color value.
     */
    public static final String PROPERTY_FILL_COLOR = "fillColor";
    /**
     * Property for defining the line color of the shape. Requires a
     * Color value.
     */
    public static final String PROPERTY_LINE_COLOR = "lineColor";
    /**
     * Property for defining the first adjust handle for shapes. Used
     * with the rounded rectangle. Requires a long value.
     */
    public static final String PROPERTY_ADJUST_VALUE = "adjustValue";

    /**
     * The stored value is a long.
     */
    private static final int PROPERTY_TYPE_LONG = 1;
    /**
     * The stored value is boolean.
     */
	private static final int PROPERTY_TYPE_BOOLEAN = 2;
    /**
     * The stored value is a double.
     */
	private static final int PROPERTY_TYPE_DOUBLE = 3;
    /**
     * The stored value is a Color.
     */
	private static final int PROPERTY_TYPE_COLOR = 4;
    /**
     * The stored value is either an int or a Point array.
     */
	private static final int PROPERTY_TYPE_ARRAY = 5;
    /**
    * The stored value is an Image.
    */
    private static final int PROPERTY_TYPE_IMAGE = 6;
	
    /**
     * The value type.
     */
	private int type = 0;
    /**
     * The RtfShapeProperty name.
     */
	private String name = "";
    /**
     * The RtfShapeProperty value.
     */
	private Object value = null;
	
    /**
     * Internally used to create the RtfShape.
     * 
     * @param name The property name to use.
     * @param value The property value to use.
     */
	private RtfShapeProperty(String name, Object value) {
		this.name = name;
		this.value = value;
	}
	
    /**
     * Constructs a RtfShapeProperty with a long value.
     * 
     * @param name The property name to use.
     * @param value The long value to use.
     */
	public RtfShapeProperty(String name, long value) {
		this(name, new Long(value));
		this.type = PROPERTY_TYPE_LONG;
	}
	
    /**
     * Constructs a RtfShapeProperty with a double value.
     * 
     * @param name The property name to use.
     * @param value The double value to use.
     */
	public RtfShapeProperty(String name, double value) {
		this(name, new Double(value));
		this.type = PROPERTY_TYPE_DOUBLE;
	}
	
    /**
     * Constructs a RtfShapeProperty with a boolean value.
     * 
     * @param name The property name to use.
     * @param value The boolean value to use.
     */
	public RtfShapeProperty(String name, boolean value) {
		this(name, Boolean.valueOf(value));
		this.type = PROPERTY_TYPE_BOOLEAN;
	}
	
    /**
     * Constructs a RtfShapeProperty with a Color value.
     * 
     * @param name The property name to use.
     * @param value The Color value to use.
     */
	public RtfShapeProperty(String name, Color value) {
		this(name, (Object) value);
		this.type = PROPERTY_TYPE_COLOR;
	}
	
    /**
     * Constructs a RtfShapeProperty with an int array value.
     * 
     * @param name The property name to use.
     * @param value The int array to use.
     */
	public RtfShapeProperty(String name, int[] value) {
		this(name, (Object) value);
		this.type = PROPERTY_TYPE_ARRAY;
	}
    
    /**
     * Constructs a RtfShapeProperty with a Point array value.
     * 
     * @param name The property name to use.
     * @param value The Point array to use.
     */
    public RtfShapeProperty(String name, Point[] value) {
        this(name, (Object) value);
        this.type = PROPERTY_TYPE_ARRAY;
    }
	
    /**
    * Constructs a RtfShapeProperty with an Image value.
    * 
    * @param name The property name to use.
    * @param value The Image to use.
    */
    public RtfShapeProperty(String name, Image value) {
        this.name = name;
        this.value = value;
        this.type = PROPERTY_TYPE_IMAGE;
    }

    /**
     * Gets the name of this RtfShapeProperty.
     * 
     * @return The name of this RtfShapeProperty.
     */
    public String getName() {
        return this.name;
    }
    
    /**
     * Writes the property definition. How the property
     * is written depends on the property type.
     */
    public void writeContent(final OutputStream result) throws IOException
    {    	
    	result.write(OPEN_GROUP);
    	result.write(DocWriter.getISOBytes("\\sp"));
    	result.write(OPEN_GROUP);
    	result.write(DocWriter.getISOBytes("\\sn"));
    	result.write(DELIMITER);
    	result.write(DocWriter.getISOBytes(this.name));
    	result.write(CLOSE_GROUP);
    	result.write(OPEN_GROUP);
    	result.write(DocWriter.getISOBytes("\\sv"));
    	result.write(DELIMITER);
    	switch(this.type) {
    	case PROPERTY_TYPE_LONG: 
    	case PROPERTY_TYPE_DOUBLE:
    		result.write(DocWriter.getISOBytes(this.value.toString()));
    		break;
    	case PROPERTY_TYPE_BOOLEAN:
    		if(((Boolean) this.value).booleanValue()) {
    			result.write(DocWriter.getISOBytes("1"));
    		} else {
    			result.write(DocWriter.getISOBytes("0"));
    		}
    		break;
    	case PROPERTY_TYPE_COLOR:
            Color color = (Color) this.value;
            result.write(intToByteArray(color.getRed() | (color.getGreen() << 8) | (color.getBlue() << 16)));
    		break;
    	case PROPERTY_TYPE_ARRAY:
    	    if(this.value instanceof int[]) {
    	        int[] values = (int[]) this.value;
    	        result.write(DocWriter.getISOBytes("4;"));
    	        result.write(intToByteArray(values.length));
    	        result.write(COMMA_DELIMITER);
    	        for(int i = 0; i < values.length; i++) {
    	            result.write(intToByteArray(values[i]));
    	            if(i < values.length - 1) {
    	                result.write(COMMA_DELIMITER);
    	            }
    	        }
    	    } else if(this.value instanceof Point[]) {
    	        Point[] values = (Point[]) this.value;
                result.write(DocWriter.getISOBytes("8;"));
                result.write(intToByteArray(values.length));
                result.write(COMMA_DELIMITER);
                for(int i = 0; i < values.length; i++) {
                    result.write(DocWriter.getISOBytes("("));
                    result.write(intToByteArray(values[i].x));
                    result.write(DocWriter.getISOBytes(","));
                    result.write(intToByteArray(values[i].y));
                    result.write(DocWriter.getISOBytes(")"));
                    if(i < values.length - 1) {
                        result.write(COMMA_DELIMITER);
                    }
                }
            }
    		break;
        case PROPERTY_TYPE_IMAGE:
            Image image = (Image)this.value;
            RtfImage img = null;
            try {
                img = new RtfImage(this.doc, image);
            }
            catch (DocumentException de) {
                throw new ExceptionConverter(de);
            }
            img.setTopLevelElement(true);
            result.write(OPEN_GROUP);
            img.writeContent(result);
            result.write(CLOSE_GROUP);
            break;
    	}
    	result.write(CLOSE_GROUP);
    	result.write(CLOSE_GROUP);
    }
	
}
