package org.openpdf.pdf;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

class StrictErrorHandler implements ErrorHandler {
    @Override
    public void error(SAXParseException exception) throws SAXParseException {
        throw exception;
    }

    @Override
    public void fatalError(SAXParseException exception) throws SAXParseException {
        throw exception;
    }

    @Override
    public void warning(SAXParseException exception) throws SAXParseException {
        throw exception;
    }
}
