/**
 * dgd: com.lowagie.text.pdf.parser
 */
package com.lowagie.text.pdf.parser;

import com.lowagie.text.pdf.DocumentFont;
import com.lowagie.text.pdf.PdfReader;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dgd
 * 
 */
public class ParsedText extends ParsedTextImpl {

	protected final Matrix textToUserSpaceTransformMatrix;
	protected final GraphicsState gs;

	ParsedText(String text, GraphicsState gs, Matrix textMatrix) {
		this(text, new GraphicsState(gs), textMatrix, convertWidthToUser(
				getUnscaledFontSpaceWidth(gs), textMatrix.multiply(gs.ctm)));
	}

	/**
	 * This constructor should only be called when the origin for text display
	 * is at (0,0) and the graphical state reflects all transformations of the
	 * baseline. This is in text space units.
	 * 
	 * @param text
	 *            string
	 * @param gs
	 *            graphical state
	 * @param textMatrix
	 *            transform from text space to graphics (drawing space)
	 * @param spaceWidth
	 *            width of the space character in the font.
	 */
	ParsedText(String text, GraphicsState gs, Matrix textMatrix,
			float spaceWidth) {
		super(text, pointToUserSpace(0, 0, textMatrix.multiply(gs.ctm)),
				pointToUserSpace(getStringWidth(text, gs), 0f,
						textMatrix.multiply(gs.ctm)), convertHeightToUser(
						gs.font.getFontDescriptor(DocumentFont.ASCENT,
								gs.fontSize), textMatrix.multiply(gs.ctm)),
				convertHeightToUser(gs.font.getFontDescriptor(
						DocumentFont.DESCENT, gs.fontSize),
						textMatrix.multiply(gs.ctm)), spaceWidth);
		this.textToUserSpaceTransformMatrix = textMatrix.multiply(gs.ctm);
		this.getStartPoint();
		this.gs = gs;
	}

	/**
	 * @param xoffset
	 * @param yoffset
	 * @param textToUserSpaceTransformMatrix
	 * @return
	 */
	private static Vector pointToUserSpace(float xoffset, float yoffset,
			Matrix textToUserSpaceTransformMatrix) {
		Vector result = new Vector(xoffset, yoffset, 1f)
				.cross(textToUserSpaceTransformMatrix);
		return result;
	}

	/**
	 * Calculates the width of a space character. If the font does not define a
	 * width for a standard space character , we also attempt to use the width
	 * of \u00A0 (a non-breaking space in many fonts)
	 * 
	 * @param gs
	 *            TODO
	 * 
	 * @return the width of a single space character in text space units
	 */
	private static float getUnscaledFontSpaceWidth(GraphicsState gs) {
		char charToUse = ' ';
		if (gs.font.getWidth(charToUse) == 0) {
			charToUse = '\u00A0';
		}
		return getStringWidth(String.valueOf(charToUse), gs);
	}

	/**
	 * Gets the width of a String in text space units
	 * 
	 * @param string
	 *            the string that needs measuring
	 * @param gs
	 *            TODO
	 * @return the width of a String in text space units
	 */
	private static float getStringWidth(String string, GraphicsState gs) {
		DocumentFont font = gs.font;
		char[] chars = string.toCharArray();
		float totalWidth = 0;
		for (char c : chars) {
			float w = font.getWidth(c) / 1000.0f;
			float wordSpacing = c == 32 ? gs.wordSpacing : 0f;
			totalWidth += (w * gs.fontSize + gs.characterSpacing + wordSpacing)
					* gs.horizontalScaling;
		}

		return totalWidth;
	}

	/**
	 * @return list of Word objects. We are guaranteed that every space
	 *         (internal word break) in this parsed text object will create a
	 *         new word in the result of this method. We are not guaranteed that
	 *         these Word objects are actually words until they have been
	 *         assembled. The word following any space preserves that space in
	 *         its string value, so that the assembler will not erroneously
	 *         merge words that should be separate, regardless of the spacing.
	 */
	public List<Word> getAsPartialWords() {
		ArrayList<Word> result = new ArrayList<Word>();
		DocumentFont font = gs.font;

		char[] chars = getText().toCharArray();
		float totalWidth = 0;
		StringBuffer wordAccum = new StringBuffer(3);
		float wordStartOffset = 0;

		/* go through string splitting at spaces, and calculating widths */
		for (char c : chars) {
			float w = font.getWidth(c) / 1000.0f;
			if (Character.isSpaceChar(c) && wordAccum.length() > 0) {
				result.add(new Word(wordAccum.toString(), getAscent(),
						getDescent(), pointToUserSpace(wordStartOffset, 0f,
								textToUserSpaceTransformMatrix),
						pointToUserSpace(totalWidth, 0f,
								textToUserSpaceTransformMatrix),
						getSingleSpaceWidth()));
				wordAccum = new StringBuffer();
				float wordSpacing = c == 32 ? gs.wordSpacing : 0f;
				totalWidth += (w * gs.fontSize + gs.characterSpacing + wordSpacing)
						* gs.horizontalScaling;
				wordStartOffset = totalWidth;
			} else {
				wordAccum.append(c);
				float wordSpacing = c == 32 ? gs.wordSpacing : 0f;
				totalWidth += (w * gs.fontSize + gs.characterSpacing + wordSpacing)
						* gs.horizontalScaling;
			}

		}
		if (wordAccum.length() > 0) {
			result.add(new Word(wordAccum.toString(), getAscent(),
					getDescent(), pointToUserSpace(wordStartOffset, 0f,
							textToUserSpaceTransformMatrix), pointToUserSpace(
							totalWidth, 0f, textToUserSpaceTransformMatrix),
					getSingleSpaceWidth()));
		}
		return result;
	}

	/**
	 * @param gs
	 *            TODO
	 * @return the unscaled (i.e. in Text space) width of the text
	 */
	public float getUnscaledWidth(GraphicsState gs) {
		return getStringWidth(getText(), gs);
	}

	/**
	 * @param width
	 * @param textToUserSpaceTransformMatrix
	 *            TODO
	 * @return
	 */
	private static float convertWidthToUser(float width,
			Matrix textToUserSpaceTransformMatrix) {
		Vector startPos = pointToUserSpace(0, 0, textToUserSpaceTransformMatrix);
		Vector endPos = pointToUserSpace(width, 0,
				textToUserSpaceTransformMatrix);
		return distance(startPos, endPos);
	}

	/**
	 * @param startPos
	 * @param endPos
	 * @return
	 */
	private static float distance(Vector startPos, Vector endPos) {
		return endPos.subtract(startPos).length();
	}

	/**
	 * @param height
	 * @param textToUserSpaceTransformMatrix
	 *            TODO
	 * @return
	 */
	private static float convertHeightToUser(float height,
			Matrix textToUserSpaceTransformMatrix) {
		Vector startPos = pointToUserSpace(0, 0, textToUserSpaceTransformMatrix);
		Vector endPos = pointToUserSpace(0, height,
				textToUserSpaceTransformMatrix);
		return distance(endPos, startPos);
	}

	/**
	 * @see com.lowagie.text.pdf.parser.TextAssemblyBuffer#accumulate(com.lowagie.text.pdf.parser.TextAssembler, String)
	 */
	@Override
	public void accumulate(TextAssembler p, String contextName) {
		p.process(this, contextName);
	}

	/**
	 * @see com.lowagie.text.pdf.parser.TextAssemblyBuffer#assemble(com.lowagie.text.pdf.parser.TextAssembler)
	 */
	@Override
	public void assemble(TextAssembler p) {
		p.renderText(this);
	}

	/**
	 * @see com.lowagie.text.pdf.parser.TextAssemblyBuffer#getFinalText(com.lowagie.text.pdf.PdfReader,
	 *      int, com.lowagie.text.pdf.parser.TextAssembler)
	 */
	@Override
	public FinalText getFinalText(PdfReader reader, int page,
			TextAssembler assembler) {
		throw new RuntimeException(
				"Final text should never be called on unprocessed word fragment.");
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[ParsedText: [" + getText() + "] " + getStartPoint() + ", "
				+ getEndPoint() + "] lead" + "]";
	}

}
