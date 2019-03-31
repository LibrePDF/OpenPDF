/**
 * dgd: com.lowagie.text.pdf.parser
 */
package com.lowagie.text.pdf.parser;

import com.lowagie.text.pdf.PdfReader;

import javax.annotation.Nullable;

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
     * @see com.lowagie.text.pdf.parser.TextAssemblyBuffer#getText()
     */
    @Nullable
    @Override
    public String getText() {
        return content;
    }

    /**
     * {@inheritDoc}
     *
     * @see com.lowagie.text.pdf.parser.TextAssemblyBuffer#accumulate(com.lowagie.text.pdf.parser.TextAssembler, String)
     */
    @Override
    public void accumulate(TextAssembler p, String contextName) {
        p.process(this, contextName);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.lowagie.text.pdf.parser.TextAssemblyBuffer#assemble(com.lowagie.text.pdf.parser.TextAssembler)
     */
    @Override
    public void assemble(TextAssembler p) {
        p.renderText(this);
    }

    /**
     * {@inheritDoc}
     *
     * @see com.lowagie.text.pdf.parser.TextAssemblyBuffer#getFinalText(PdfReader,
     * int, TextAssembler, boolean)
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
