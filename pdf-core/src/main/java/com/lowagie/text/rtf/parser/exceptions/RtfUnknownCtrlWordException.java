package com.lowagie.text.rtf.parser.exceptions;

/**
 * @since 2.1.0
 */
public class RtfUnknownCtrlWordException extends RtfParserException {

	/**
	 * Generated serial version UID.
	 */
	private static final long serialVersionUID = 7243046509505727849L;
    
    // constructors
    
    /**
     * Constructs a <CODE>RtfParserException</CODE> whithout a message.
     */
    public RtfUnknownCtrlWordException() {
        super("Unknown control word.");
    }
    
}
