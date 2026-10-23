/*
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
 * the Initial Developer are Copyright (C) 1999, 2000, 2001, 2002 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000, 2001, 2002 by Paulo Soares. All Rights Reserved.
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
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * https://github.com/LibrePDF/OpenPDF
 */

/*
 * The original version of this class was published in an article by professor Heinz Kabutz.
 * Read http://www.javaspecialists.co.za/archive/newsletter.do?issue=033&print=yes&locale=en_US
 * "This material from The Java(tm) Specialists' Newsletter by Maximum Solutions (South Africa).
 * Please contact Maximum Solutions  for more information."
 *
 * Copyright (C) 2001 Dr. Heinz M. Kabutz
 */

/**
 * The OpenPDF project has permission from the author to use this class under a LGPL/MPL license on 2018-08-06:
 * <p>
 * Hi Andreas,
 * <p>
 * you have my permission to use my code with your license.
 * <p>
 * Regards
 * <p>
 * <p>
 * Heinz -- Dr Heinz M. Kabutz (PhD CompSci)
 */

package org.openpdf.text;

/**
 * The ExceptionConverter changes a checked exception into an unchecked exception.
 * <p>
 * The wrapped exception is set as the {@linkplain #getCause() cause}, so logging frameworks and
 * {@link #getStackTrace()} see the complete stack trace of both the conversion point and the
 * original exception (issue #1296 reported {@code getStackTrace()} returning an empty array
 * because the stack trace was suppressed and the original exception was not set as the cause).
 */
public class ExceptionConverter extends RuntimeException {

    private static final long serialVersionUID = 8657630363395849399L;

    /**
     * Construct a RuntimeException based on another Exception
     *
     * @param ex the exception that has to be turned into a RuntimeException
     */
    public ExceptionConverter(Exception ex) {
        super(ex);
    }

    /**
     * Convert an Exception into an unchecked exception. Return the exception if it is already an unchecked exception or
     * return an ExceptionConverter wrapper otherwise
     *
     * @param ex the exception to convert
     * @return an unchecked exception
     * @since 2.1.6
     */
    public static final RuntimeException convertException(Exception ex) {
        if (ex instanceof RuntimeException) {
            return (RuntimeException) ex;
        }
        return new ExceptionConverter(ex);
    }

    /**
     * and allow the user of ExceptionConverter to get a handle to it.
     *
     * @return the original exception
     */
    public Exception getException() {
        return (Exception) getCause();
    }

    /**
     * We print the message of the checked exception
     *
     * @return message of the original exception
     */
    @Override
    public String getMessage() {
        return getCause().getMessage();
    }

    /**
     * and make sure we also produce a localized version
     *
     * @return localized version of the message
     */
    @Override
    public String getLocalizedMessage() {
        return getCause().getLocalizedMessage();
    }

    /**
     * The toString() is changed to be prefixed with ExceptionConverter
     *
     * @return String version of the exception
     */
    @Override
    public String toString() {
        Throwable cause = getCause();
        return (cause instanceof RuntimeException ? "" : "ExceptionConverter: ") + String.valueOf(cause);
    }
}