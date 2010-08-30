package com.lowagie.text.pdf.collection;

import java.util.Calendar;
import com.lowagie.text.error_messages.MessageLocalization;

import com.lowagie.text.pdf.PdfDate;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfNumber;
import com.lowagie.text.pdf.PdfObject;
import com.lowagie.text.pdf.PdfString;

public class PdfCollectionItem extends PdfDictionary {
	
	/** The PdfCollectionSchema with the names and types of the items. */
	PdfCollectionSchema schema;
	
	/**
	 * Constructs a Collection Item that can be added to a PdfFileSpecification.
	 */
	public PdfCollectionItem(PdfCollectionSchema schema) {
		super(PdfName.COLLECTIONITEM);
		this.schema = schema;
	}
	
	/**
	 * Sets the value of the collection item.
	 * @param value
	 */
	public void addItem(String key, String value) {
		PdfName fieldname = new PdfName(key);
		PdfCollectionField field = (PdfCollectionField)schema.get(fieldname);
		put(fieldname, field.getValue(value));
	}
	
	/**
	 * Sets the value of the collection item.
	 * @param value
	 */
	public void addItem(String key, PdfString value) {
		PdfName fieldname = new PdfName(key);
		PdfCollectionField field = (PdfCollectionField)schema.get(fieldname);
		if (field.fieldType == PdfCollectionField.TEXT) {
			put(fieldname, value);
		}
	}
	
	/**
	 * Sets the value of the collection item.
	 * @param d
	 */
	public void addItem(String key, PdfDate d) {
		PdfName fieldname = new PdfName(key);
		PdfCollectionField field = (PdfCollectionField)schema.get(fieldname);
		if (field.fieldType == PdfCollectionField.DATE) {
			put(fieldname, d);
		}
	}
	
	/**
	 * Sets the value of the collection item.
	 * @param n
	 */
	public void addItem(String key, PdfNumber n) {
		PdfName fieldname = new PdfName(key);
		PdfCollectionField field = (PdfCollectionField)schema.get(fieldname);
		if (field.fieldType == PdfCollectionField.NUMBER) {
			put(fieldname, n);
		}
	}
	
	/**
	 * Sets the value of the collection item.
	 * @param c
	 */
	public void addItem(String key, Calendar c) {
		addItem(key, new PdfDate(c));
	}
	
	/**
	 * Sets the value of the collection item.
	 * @param i
	 */
	public void addItem(String key, int i) {
		addItem(key, new PdfNumber(i));
	}
	
	/**
	 * Sets the value of the collection item.
	 * @param f
	 */
	public void addItem(String key, float f) {
		addItem(key, new PdfNumber(f));
	}
	
	/**
	 * Sets the value of the collection item.
	 * @param d
	 */
	public void addItem(String key, double d) {
		addItem(key, new PdfNumber(d));
	}
	
	/**
	 * Adds a prefix for the Collection item.
	 * You can only use this method after you have set the value of the item.
	 * @param prefix	a prefix
	 */
	public void setPrefix(String key, String prefix) {
		PdfName fieldname = new PdfName(key);
		PdfObject o = get(fieldname);
		if (o == null)
			throw new IllegalArgumentException(MessageLocalization.getComposedMessage("you.must.set.a.value.before.adding.a.prefix"));
		PdfDictionary dict = new PdfDictionary(PdfName.COLLECTIONSUBITEM);
		dict.put(PdfName.D, o);
		dict.put(PdfName.P, new PdfString(prefix, PdfObject.TEXT_UNICODE));
		put(fieldname, dict);
	}
}
