/**
 * dgd: com.lowagie.text.pdf.parser
 */
package com.lowagie.text.pdf.parser;

import com.lowagie.text.pdf.PdfReader;

/**
 * @author dgd
 * 
 */
public class FinalText implements TextAssemblyBuffer {

	String _content;

	public FinalText(String content) {
		_content = content;
	}

	/**
	 * @return
	 * @see com.lowagie.text.pdf.parser.TextAssemblyBuffer#getText()
	 */
	@Override
	public String getText() {
		return _content;
	}

	/**
	 * @param p
	 * @see com.lowagie.text.pdf.parser.TextAssemblyBuffer#accumulate(com.lowagie.text.pdf.parser.TextAssembler, String)
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

	/**
	 * @return
	 * @see com.lowagie.text.pdf.parser.TextAssemblyBuffer#getFinalText(PdfReader,
	 *      int, TextAssembler, boolean)
	 */
	@Override
	public FinalText getFinalText(PdfReader reader, int page, TextAssembler assembler, boolean useMarkup) {
		return this;
	}

	@Override
	public String toString() {
		return "[FinalText: [" + getText() + "] d]";
	}

}
