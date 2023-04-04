package com.lowagie.text;

import com.lowagie.text.pdf.FopGlyphProcessor;

/**
 * Text rendering options, including the default language of the document and a flag
 * to enable font glyph substitution (if FOP is available).
 * 
 * @author Lucian Chirita (lucianc@users.sourceforge.net)
 * @see Document#setTextRenderingOptions(TextRenderingOptions)
 * @since 3.1.15
 */
public class TextRenderingOptions {

	public static final String DOCUMENT_LANGUAGE_DEFAULT = "dflt";
	
    /**
     * The default language of the document. Can be set to values like "en_US".
     * This language is used in {@link FopGlyphProcessor} to determine which glyphs are to be substituted.
     * The default "dflt" means that all glyphs which can be replaced will be substituted.
     * 
     */
	private final String documentLanguage;
	
	private final boolean glyphSubstitutionEnabled;

	/**
	 * Creates a text rendering options instance with the default options: glyph substitution enabled 
	 * and "dflt" as document language.
	 */
	public TextRenderingOptions() {
		this(DOCUMENT_LANGUAGE_DEFAULT, true);
	}

	/**
	 * Creates a text rendering options instance.
	 * 
     * @param documentLanguage the wanted language
	 * @param glyphSubstitutionEnabled
	 */
	public TextRenderingOptions(String documentLanguage, boolean glyphSubstitutionEnabled) {
		this.documentLanguage = documentLanguage;
		this.glyphSubstitutionEnabled = glyphSubstitutionEnabled;
	}

    /**
     * The default language of the document. Can be set to values like "en_US". This language is used in
     * FopGlyphProcessor to determine which glyphs are to be substituted.
     * <P/>
     * The default "dflt" means that all glyphs which can be replaced will be substituted.
     *
     * @return the current document language
     */
	public String getDocumentLanguage() {
		return documentLanguage;
	}

	/**
	 * Returns the glyph substitution enabled flag.
	 * 
	 * @return the glyph substitution enabled flag
	 * #see {@link Document#setGlyphSubstitutionEnabled(boolean)}
	 */
	public boolean isGlyphSubstitutionEnabled() {
		return glyphSubstitutionEnabled;
	}
	
	/**
	 * Returns a new instance with the specified document language.
	 * 
	 * @param documentLanguage the document language
	 * @return a new instance with the document language
	 */
	public TextRenderingOptions withDocumentLanguage(String documentLanguage) {
		return new TextRenderingOptions(documentLanguage, glyphSubstitutionEnabled);
	}
	
	/**
	 * Returns a new instance with the specified glyph substitution enabled flag.
	 * 
	 * @param glyphSubstitutionEnabled whether glyph substitution is enabled
	 * @return a new instance with the glyph substitution enabled flag
	 */
	public TextRenderingOptions withGlyphSubstitutionsEnabled(boolean glyphSubstitutionEnabled) {
		return new TextRenderingOptions(documentLanguage, glyphSubstitutionEnabled);
	}
}
