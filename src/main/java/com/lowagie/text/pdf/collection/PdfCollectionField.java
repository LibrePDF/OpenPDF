package com.lowagie.text.pdf.collection;

import com.lowagie.text.pdf.PdfBoolean;
import com.lowagie.text.pdf.PdfDate;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfNumber;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfString;
import com.lowagie.text.error_messages.MessageLocalization;

/**
 * @author blowagie
 *
 */
public class PdfCollectionField extends PdfDictionary {
	/** A possible type of collection field. */
	public static final int TEXT = 0;
	/** A possible type of collection field. */
	public static final int DATE = 1;
	/** A possible type of collection field. */
	public static final int NUMBER = 2;
	/** A possible type of collection field. */
	public static final int FILENAME = 3;
	/** A possible type of collection field. */
	public static final int DESC = 4;
	/** A possible type of collection field. */
	public static final int MODDATE = 5;
	/** A possible type of collection field. */
	public static final int CREATIONDATE = 6;
	/** A possible type of collection field. */
	public static final int SIZE = 7;
	
	/**
	 * The type of the PDF collection field.
	 * @since 2.1.2 (was called <code>type</code> previously)
	 */
	protected int fieldType;

	/**
	 * Creates a PdfCollectionField.
	 * @param name		the field name
	 * @param type		the field type
	 */
	public PdfCollectionField(String name, int type) {
		super(PdfName.COLLECTIONFIELD);
		put(PdfName.N, new PdfString(name, PdfObject.TEXT_UNICODE));
		this.fieldType = type;
		switch(type) {
		default:
			put(PdfName.SUBTYPE, PdfName.S);
			break;
		case DATE:
			put(PdfName.SUBTYPE, PdfName.D);
			break;
		case NUMBER:
			put(PdfName.SUBTYPE, PdfName.N);
			break;
		case FILENAME:
			put(PdfName.SUBTYPE, PdfName.F);
			break;
		case DESC:
			put(PdfName.SUBTYPE, PdfName.DESC);
			break;
		case MODDATE:
			put(PdfName.SUBTYPE, PdfName.MODDATE);
			break;
		case CREATIONDATE:
			put(PdfName.SUBTYPE, PdfName.CREATIONDATE);
			break;
		case SIZE:
			put(PdfName.SUBTYPE, PdfName.SIZE);
			break;
		}
	}
	
	/**
	 * The relative order of the field name. Fields are sorted in ascending order.
	 * @param i	a number indicating the order of the field
	 */
	public void setOrder(int i) {
		put(PdfName.O, new PdfNumber(i));
	}
	
	/**
	 * Sets the initial visibility of the field.
	 * @param visible	the default is true (visible)
	 */
	public void setVisible(boolean visible) {
		put(PdfName.V, new PdfBoolean(visible));
	}
	
	/**
	 * Indication if the field value should be editable in the viewer.
	 * @param editable	the default is false (not editable)
	 */
	public void setEditable(boolean editable) {
		put(PdfName.E, new PdfBoolean(editable));
	}

	/**
	 * Checks if the type of the field is suitable for a Collection Item.
	 */
	public boolean isCollectionItem() {
		switch(fieldType) {
		case TEXT:
		case DATE:
		case NUMBER:
			return true;
		default:
			return false;
		}
	}
	
	/**
	 * Returns a PdfObject that can be used as the value of a Collection Item.
	 * @param v	value	the value that has to be changed into a PdfObject (PdfString, PdfDate or PdfNumber)	
	 */
	public PdfObject getValue(String v) {
		switch(fieldType) {
		case TEXT:
			return new PdfString(v, PdfObject.TEXT_UNICODE);
		case DATE:
			return new PdfDate(PdfDate.decode(v));
		case NUMBER:
			return new PdfNumber(v);
		}
		throw new IllegalArgumentException(MessageLocalization.getComposedMessage("1.is.not.an.acceptable.value.for.the.field.2", v, get(PdfName.N).toString()));
	}
}
