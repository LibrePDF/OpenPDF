/*
 * $Id: RtfParserException.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * Copyright 2007 by Howard Shank (hgshank@yahoo.com)
 *
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
 * the Initial Developer are Copyright (C) 1999-2006 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000-2006 by Paulo Soares. All Rights Reserved.
 *
 * Contributor(s): all the names of the contributors are added in the source code
 * where applicable.
 *
 * Alternatively, the contents of this file may be used under the terms of the
 * LGPL license (the ?GNU LIBRARY GENERAL PUBLIC LICENSE?), in which case the
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
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * http://www.lowagie.com/iText/
 */
package com.lowagie.text.rtf.parser.exceptions;

/*
 * Signals that an error has occurred in a <CODE>RtfParser</CODE>.
 */
/**
 * <code>RtfParserException</code> is the exception object thrown by
 * the parser
 * 
 * @author Howard Shank (hgshank@yahoo.com)
 * @since 2.0.8
 */
public class RtfParserException extends Exception {
	private static final long serialVersionUID = 2857489935812968235L;
	/**
	 * Contained inner exception object.
	 */
	private Exception ex;

    /**
     * Creates a RtfParserException object.
     * @param ex an exception that has to be turned into a RtfParserException
     */
    public RtfParserException(Exception ex) {
        this.ex = ex;
    }
    
    // constructors
    
    /**
     * Constructs a <CODE>RtfParserException</CODE> whithout a message.
     */
    public RtfParserException() {
        super();
    }
    
    /**
     * Constructs a <code>RtfParserException</code> with a message.
     *
     * @param		message			a message describing the exception
     */
    public RtfParserException(String message) {
        super(message);
    }

    /**
     * We print the message of the checked exception 
     * @return the error message
     */
    public String getMessage() {
        if (ex == null)
            return super.getMessage();
        else
            return ex.getMessage();
    }

    /**
     * and make sure we also produce a localized version 
     * @return a localized message
     */
    public String getLocalizedMessage() {
        if (ex == null)
            return super.getLocalizedMessage();
        else
            return ex.getLocalizedMessage();
    }

    /**
     * The toString() is changed to be prefixed with ExceptionConverter 
     * @return the String version of the exception
     */
    public String toString() {
        if (ex == null)
            return super.toString();
        else
            return split(getClass().getName()) + ": " + ex;
    }

    /** we have to override this as well */
    public void printStackTrace() {
        printStackTrace(System.err);
    }

    /**
     * here we prefix, with s.print(), not s.println(), the stack
     * trace with "ExceptionConverter:" 
     * @param s a printstream object
     */
    public void printStackTrace(java.io.PrintStream s) {
        if (ex == null)
            super.printStackTrace(s);
        else {
            synchronized (s) {
                s.print(split(getClass().getName()) + ": ");
                ex.printStackTrace(s);
            }
        }
    }

    /**
     * Again, we prefix the stack trace with "ExceptionConverter:" 
     * @param s A PrintWriter object
     */
    public void printStackTrace(java.io.PrintWriter s) {
        if (ex == null)
            super.printStackTrace(s);
        else {
            synchronized (s) {
                s.print(split(getClass().getName()) + ": ");
                ex.printStackTrace(s);
            }
        }
    }

    /**
     * Removes everything in a String that comes before a '.'
     * @param s the original string
     * @return the part that comes after the dot
     */
    private static String split(String s) {
        int i = s.lastIndexOf('.');
        if (i < 0)
            return s;
        else
            return s.substring(i + 1);
    }
}
