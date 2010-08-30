package com.lowagie.text.pdf.collection;

import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfString;

public class PdfCollection extends PdfDictionary {

	/** A type of PDF Collection */
	public static final int DETAILS = 0;
	/** A type of PDF Collection */
	public static final int TILE = 1;
	/** A type of PDF Collection */
	public static final int HIDDEN = 2;
	
	/**
	 * Constructs a PDF Collection.
	 * @param	type	the type of PDF collection.
	 */
	public PdfCollection(int type) {
		super(PdfName.COLLECTION);
		switch(type) {
		case TILE:
			put(PdfName.VIEW, PdfName.T);
			break;
		case HIDDEN:
			put(PdfName.VIEW, PdfName.H);
			break;
		default:
			put(PdfName.VIEW, PdfName.D);
		}
	}
	
	/**
	 * Identifies the document that will be initially presented
	 * in the user interface.
	 * @param description	the description that was used when attaching the file to the document
	 */
	public void setInitialDocument(String description) {
		put(PdfName.D, new PdfString(description, null));
	}
	
	/**
	 * Sets the Collection schema dictionary.
	 * @param schema	an overview of the collection fields
	 */
	public void setSchema(PdfCollectionSchema schema) {
		put(PdfName.SCHEMA, schema);
	}
	
	/**
	 * Gets the Collection schema dictionary.
	 * @return schema	an overview of the collection fields
	 */
	public PdfCollectionSchema getSchema() {
		return (PdfCollectionSchema)get(PdfName.SCHEMA);
	}
	
	/**
	 * Sets the Collection sort dictionary.
	 * @param sort	a collection sort dictionary
	 */
	public void setSort(PdfCollectionSort sort) {
		put(PdfName.SORT, sort);
	}
}