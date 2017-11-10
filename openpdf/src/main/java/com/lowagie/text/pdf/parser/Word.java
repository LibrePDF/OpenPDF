/**
 * Copyright 2014 by Tizra Inc.
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the License.
 *
 * The Original Code is 'iText, a free JAVA-PDF library'.
 *
 * The Initial Developer of the Original Code is Bruno Lowagie. Portions created by
 * the Initial Developer are Copyright (C) 1999-2008 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000-2008 by Paulo Soares. All Rights Reserved.
 *
 * Contributor(s): all the names of the contributors are added in the source code
 * where applicable.
 *
 * Alternatively, the contents of this file may be used under the terms of the
 * LGPL license (the "GNU LIBRARY GENERAL PUBLIC LICENSE"), in which case the
 * provisions of LGPL are applicable instead of those above.  If you wish to
 * allow use of your version of this file only under the terms of the LGPL
 * License and not to allow others to use your version of this file under
 * the MPL, indicate your decision by deleting the provisions above and
 * replace them with the notice and other provisions required by the LGPL.
 * If you do not delete the provisions above, a recipient may use your version
 * of this file under either the MPL or the GNU LIBRARY GENERAL PUBLIC LICENSE.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the MPL as stated above or under the terms of the GNU
 * Library General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library general Public License for more
 * details.
 *
 * dgd: com.lowagie.text.pdf.parser
 */
package com.lowagie.text.pdf.parser;

import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfReader;

/**
 * @author dgd
 */
public class Word extends ParsedTextImpl {

	/**
	 * @param text
	 * @param ascent
	 * @param descent
	 *            TODO
	 * @param startPoint
	 * @param spaceWidth
	 */
	Word(String text, float ascent, float descent, Vector startPoint,
			Vector endPoint, float spaceWidth) {
		super(text, startPoint, endPoint, ascent, descent, spaceWidth);
	}

	/**
	 *
	 * @param p the assembler that is visiting us.
	 * @param contextName TODO
	 *
	 */

	@Override
	public void accumulate(TextAssembler p, String contextName) {
		p.process(this, contextName);
	}

	/**
	 * @param p
	 * @see com.lowagie.text.pdf.parser.TextAssemblyBuffer#assemble(com.lowagie.text.pdf.parser.TextAssembler)
	 */
	@Override
	public void assemble(TextAssembler p) {
		p.renderText(this);
	}

	private static String formatPercent(float f) {
		return String.format("%.2f%%", f);
	}

	/**
	 * Generate markup for this word. send the assembler a strings representing
	 * a CSS style that will format us nicely.
	 *
	 * @param text
	 *            passed in because we may have wanted to alter it, e.g. by
	 *            trimming white space, or filtering characters or something.
	 * @param reader
	 *            the file reader from which we are extracting
	 * @param page
	 *            number of the page we are reading text from
	 * @param assembler
	 *            object to assemble text from fragments and larger strings on a
	 *            page.
	 * @return markup to represent this one word.
	 */
	private String wordMarkup(String text, PdfReader reader, int page,
			TextAssembler assembler) {

		Rectangle mediaBox = reader.getBoxSize(page, "media");
		Rectangle clipBox = reader.getBoxSize(page, "clip");
		mediaBox.normalize();
		if (clipBox != null) {
			clipBox.normalize();
		} else {
			Rectangle cropBox = reader.getBoxSize(page, "trim");
			if (cropBox != null) {
				cropBox.normalize();
				clipBox = cropBox;
			} else {
				clipBox = mediaBox;
			}
		}
		float xOffset = clipBox.getLeft() - mediaBox.getLeft();
		float yOffset = clipBox.getTop() - mediaBox.getTop();
		Vector startPoint = getStartPoint();
		Vector endPoint = getEndPoint();
		float pageWidth = clipBox.getWidth();
		float pageHeight = clipBox.getHeight();
		float leftPercent = (float) ((startPoint.get(0) - xOffset) / pageWidth
				* 100.0);
		float bottom = endPoint.get(1) + yOffset - getDescent();
		float bottomPercent = bottom / pageHeight * 100f;
		StringBuilder result = new StringBuilder();
		float width = getWidth();
		float widthPercent = width / pageWidth * 100.0f;

		float height = getAscent();
		float heightPercent = height / pageHeight * 100.0f;
		String myId = assembler.getWordId();
		result.append("<span class=\"t-word\" style=\"bottom: ")
				.append(formatPercent(bottomPercent)).append("; left: ")
				.append(formatPercent(leftPercent)).append("; width: ")
				.append(formatPercent(widthPercent)).append("; height: ")
				.append(formatPercent(heightPercent)).append(";\"")
				.append(" id=\"").append(myId).append("\">")
				.append(escapeHTML(text)).append(" ");
		result.append("</span> ");

		return result.toString();
	}

	private static String escapeHTML(String s) {
		return s.replaceAll("&", "&amp;").replaceAll("<", "&lt;")
				.replaceAll(">", "&gt;");
	}

	/**
	 * @return
	 * @see com.lowagie.text.pdf.parser.TextAssemblyBuffer#getFinalText(PdfReader,
	 *      int, TextAssembler)
	 */
	@Override
	public FinalText getFinalText(PdfReader reader, int page,
			TextAssembler assembler) {
		return new FinalText(
				wordMarkup(getText().trim(), reader, page, assembler));
	}

	@Override
	public String toString() {
		return "[Word: [" + getText() + "] " + getStartPoint() + ", "
				+ getEndPoint() + "] lead" + getAscent() + "]";
	}
}
