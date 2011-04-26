package com.lowagie.text.pdf.collection;

import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfName;

public class PdfCollectionSchema extends PdfDictionary {
	/**
	 * Creates a Collection Schema dictionary.
	 */
	public PdfCollectionSchema() {
		super(PdfName.COLLECTIONSCHEMA);
	}
	
	/**
	 * Adds a Collection field to the Schema.
	 * @param name	the name of the collection field
	 * @param field	a Collection Field
	 */
	public void addField(String name, PdfCollectionField field) {
		put(new PdfName(name), field);
	}
}
