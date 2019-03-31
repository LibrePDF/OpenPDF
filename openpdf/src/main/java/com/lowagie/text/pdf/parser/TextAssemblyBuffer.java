/**
 * dgd: com.lowagie.text.pdf.parser
 */
package com.lowagie.text.pdf.parser;

import com.lowagie.text.pdf.PdfReader;

import javax.annotation.Nullable;

/**
 * @author dgd
 * 
 */
public interface TextAssemblyBuffer {

    /**
     * @return the text to render
     */
    @Nullable
    String getText();

    /**
     * @param reader
     *            pdfReader that knows about our document. (size, etc. available
     *            here).
     * @param page
     *            which page are we extracting text from.
     * @param assembler
     *            Builds result by accepting content from text components of
     *            various sorts.
     * @param useMarkup Should we generate tagged text, or just plain text.
     * @return the final text ready to concatenate into result string.
     */
    FinalText getFinalText(PdfReader reader, int page,
                           TextAssembler assembler, boolean useMarkup);

    /**
     * We pass ourselves to the assembler, which is a visitor, so that it can
     * accumulate information on this text depending on its type. The result is
     * calculated by a final "assembly" phase, after accumulation is done. This
     * is because we may have non-contiguous items in a PDF text stream.
     * 
     * @param p
     *            the assembler that is visiting us.
     * @param contextName Name of the surrounding markup element/"context" if
     * we're generating tagged output.
     * 
     * @see com.lowagie.text.pdf.parser.TextAssemblyBuffer#accumulate(com.lowagie.text.pdf.parser.TextAssembler, String)
     */
    void accumulate(TextAssembler p, String contextName);

    /**
     * @param p
     *            we may pass ourselves to this assembler again during the final
     *            assembly process.
     */
    void assemble(TextAssembler p);
}