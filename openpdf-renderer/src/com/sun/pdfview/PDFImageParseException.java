package com.sun.pdfview;

import java.io.IOException;

/**
 * an exception class for recording errors when parsing an PDFImage
 * @author Katja Sondermann
 */
public class PDFImageParseException extends IOException {
    public PDFImageParseException(String msg) {
        super(msg);
    }

    public PDFImageParseException(String msg, Throwable cause) {
        this(msg);
        initCause(cause);
    }
}
