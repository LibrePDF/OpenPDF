/**
 * dgd: org.openpdf.text.pdf.parser
 */
package org.openpdf.text.pdf.parser;

import org.openpdf.text.pdf.PdfReader;


/**
 * @author dgd
 */
public class FinalText implements TextAssemblyBuffer {

    private String content;

    public FinalText(String content) {
        this.content = content;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.openpdf.text.pdf.parser.TextAssemblyBuffer#getText()
     */
    @Override
    public String getText() {
        return content;
    }

    /**
     * {@inheritDoc}
     *
     * @see org.openpdf.text.pdf.parser.TextAssemblyBuffer#accumulate(org.openpdf.text.pdf.parser.TextAssembler, String)
     */
    @Override
    public void accumulate(TextAssembler p, String contextName) {
        p.process(this, contextName);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.openpdf.text.pdf.parser.TextAssemblyBuffer#assemble(org.openpdf.text.pdf.parser.TextAssembler)
     */
    @Override
    public void assemble(TextAssembler p) {
        p.renderText(this);
    }

    /**
     * {@inheritDoc}
     *
     * @see org.openpdf.text.pdf.parser.TextAssemblyBuffer#getFinalText(PdfReader, int, TextAssembler, boolean)
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
