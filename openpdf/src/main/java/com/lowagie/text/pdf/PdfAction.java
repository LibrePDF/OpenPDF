/*
 * $Id: PdfAction.java 4065 2009-09-16 23:09:11Z psoares33 $
 *
 * Copyright 2000 by Bruno Lowagie.
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
 * http://www.lowagie.com/iText/
 */

package com.lowagie.text.pdf;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import com.lowagie.text.error_messages.MessageLocalization;

import com.lowagie.text.pdf.collection.PdfTargetDictionary;

/**
 * A <CODE>PdfAction</CODE> defines an action that can be triggered from a PDF file.
 *
 * @see		PdfDictionary
 */

public class PdfAction extends PdfDictionary {
    
    /** A named action to go to the first page.
     */
    public static final int FIRSTPAGE = 1;
    /** A named action to go to the previous page.
     */
    public static final int PREVPAGE = 2;
    /** A named action to go to the next page.
     */
    public static final int NEXTPAGE = 3;
    /** A named action to go to the last page.
     */
    public static final int LASTPAGE = 4;

    /** A named action to open a print dialog.
     */
    public static final int PRINTDIALOG = 5;

    /** a possible submitvalue */
    public static final int SUBMIT_EXCLUDE = 1;
    /** a possible submitvalue */
    public static final int SUBMIT_INCLUDE_NO_VALUE_FIELDS = 2;
    /** a possible submitvalue */
    public static final int SUBMIT_HTML_FORMAT = 4;
    /** a possible submitvalue */
    public static final int SUBMIT_HTML_GET = 8;
    /** a possible submitvalue */
    public static final int SUBMIT_COORDINATES = 16;
    /** a possible submitvalue */
    public static final int SUBMIT_XFDF = 32;
    /** a possible submitvalue */
    public static final int SUBMIT_INCLUDE_APPEND_SAVES = 64;
    /** a possible submitvalue */
    public static final int SUBMIT_INCLUDE_ANNOTATIONS = 128;
    /** a possible submitvalue */
    public static final int SUBMIT_PDF = 256;
    /** a possible submitvalue */
    public static final int SUBMIT_CANONICAL_FORMAT = 512;
    /** a possible submitvalue */
    public static final int SUBMIT_EXCL_NON_USER_ANNOTS = 1024;
    /** a possible submitvalue */
    public static final int SUBMIT_EXCL_F_KEY = 2048;
    /** a possible submitvalue */
    public static final int SUBMIT_EMBED_FORM = 8196;
    /** a possible submitvalue */
    public static final int RESET_EXCLUDE = 1;

    // constructors
    
    /** Create an empty action.
     */    
    public PdfAction() {
    }
    
    /**
     * Constructs a new <CODE>PdfAction</CODE> of Subtype URI.
     *
     * @param url the Url to go to
     */
    
    public PdfAction(URL url) {
        this(url.toExternalForm());
    }
    
    /**
     * Construct a new <CODE>PdfAction</CODE> of Subtype URI that accepts the x and y coordinate of the position that was clicked.
     * @param url
     * @param isMap
     */
    public PdfAction(URL url, boolean isMap) {
        this(url.toExternalForm(), isMap);
    }
    
    /**
     * Constructs a new <CODE>PdfAction</CODE> of Subtype URI.
     *
     * @param url the url to go to
     */
    
    public PdfAction(String url) {
        this(url, false);
    }
    
    /**
     * Construct a new <CODE>PdfAction</CODE> of Subtype URI that accepts the x and y coordinate of the position that was clicked.
     * @param url
     * @param isMap
     */
    
    public PdfAction(String url, boolean isMap) {
        put(PdfName.S, PdfName.URI);
        put(PdfName.URI, new PdfString(url));
        if (isMap)
            put(PdfName.ISMAP, PdfBoolean.PDFTRUE);
    }
    
    /**
     * Constructs a new <CODE>PdfAction</CODE> of Subtype GoTo.
     * @param destination the destination to go to
     */
    
    PdfAction(PdfIndirectReference destination) {
        put(PdfName.S, PdfName.GOTO);
        put(PdfName.D, destination);
    }
    
    /**
     * Constructs a new <CODE>PdfAction</CODE> of Subtype GoToR.
     * @param filename the file name to go to
     * @param name the named destination to go to
     */
    
    public PdfAction(String filename, String name) {
        put(PdfName.S, PdfName.GOTOR);
        put(PdfName.F, new PdfString(filename));
        put(PdfName.D, new PdfString(name));
    }
    
    /**
     * Constructs a new <CODE>PdfAction</CODE> of Subtype GoToR.
     * @param filename the file name to go to
     * @param page the page destination to go to
     */
    
    public PdfAction(String filename, int page) {
        put(PdfName.S, PdfName.GOTOR);
        put(PdfName.F, new PdfString(filename));
        put(PdfName.D, new PdfLiteral("[" + (page - 1) + " /FitH 10000]"));
    }
    
    /** Implements name actions. The action can be FIRSTPAGE, LASTPAGE,
     * NEXTPAGE, PREVPAGE and PRINTDIALOG.
     * @param named the named action
     */
    public PdfAction(int named) {
        put(PdfName.S, PdfName.NAMED);
        switch (named) {
            case FIRSTPAGE:
                put(PdfName.N, PdfName.FIRSTPAGE);
                break;
            case LASTPAGE:
                put(PdfName.N, PdfName.LASTPAGE);
                break;
            case NEXTPAGE:
                put(PdfName.N, PdfName.NEXTPAGE);
                break;
            case PREVPAGE:
                put(PdfName.N, PdfName.PREVPAGE);
                break;
            case PRINTDIALOG:
                put(PdfName.S, PdfName.JAVASCRIPT);
                put(PdfName.JS, new PdfString("this.print(true);\r"));
                break;
            default:
                throw new RuntimeException(MessageLocalization.getComposedMessage("invalid.named.action"));
        }
    }
    
    /** Launches an application or a document.
     * @param application the application to be launched or the document to be opened or printed.
     * @param parameters (Windows-specific) A parameter string to be passed to the application.
     * It can be <CODE>null</CODE>.
     * @param operation (Windows-specific) the operation to perform: "open" - Open a document,
     * "print" - Print a document.
     * It can be <CODE>null</CODE>.
     * @param defaultDir (Windows-specific) the default directory in standard DOS syntax.
     * It can be <CODE>null</CODE>.
     */
    public PdfAction(String application, String parameters, String operation, String defaultDir) {
        put(PdfName.S, PdfName.LAUNCH);
        if (parameters == null && operation == null && defaultDir == null)
            put(PdfName.F, new PdfString(application));
        else {
            PdfDictionary dic = new PdfDictionary();
            dic.put(PdfName.F, new PdfString(application));
            if (parameters != null)
                dic.put(PdfName.P, new PdfString(parameters));
            if (operation != null)
                dic.put(PdfName.O, new PdfString(operation));
            if (defaultDir != null)
                dic.put(PdfName.D, new PdfString(defaultDir));
            put(PdfName.WIN, dic);
        }
    }
    
    /** Launches an application or a document.
     * @param application the application to be launched or the document to be opened or printed.
     * @param parameters (Windows-specific) A parameter string to be passed to the application.
     * It can be <CODE>null</CODE>.
     * @param operation (Windows-specific) the operation to perform: "open" - Open a document,
     * "print" - Print a document.
     * It can be <CODE>null</CODE>.
     * @param defaultDir (Windows-specific) the default directory in standard DOS syntax.
     * It can be <CODE>null</CODE>.
     * @return a Launch action
     */
    public static PdfAction createLaunch(String application, String parameters, String operation, String defaultDir) {
        return new PdfAction(application, parameters, operation, defaultDir);
    }
    
     /**Creates a Rendition action
     * @param file
     * @param fs
     * @param mimeType
     * @param ref
     * @return a Media Clip action
     * @throws IOException
     */
    public static PdfAction rendition(String file, PdfFileSpecification fs, String mimeType, PdfIndirectReference ref) throws IOException {
        PdfAction js = new PdfAction();
        js.put(PdfName.S, PdfName.RENDITION);
        js.put(PdfName.R, new PdfRendition(file, fs, mimeType));
        js.put(new PdfName("OP"), new PdfNumber(0));
        js.put(new PdfName("AN"), ref);
        return js;
     }
  
    /** Creates a JavaScript action. If the JavaScript is smaller than
     * 50 characters it will be placed as a string, otherwise it will
     * be placed as a compressed stream.
     * @param code the JavaScript code
     * @param writer the writer for this action
     * @param unicode select JavaScript unicode. Note that the internal
     * Acrobat JavaScript engine does not support unicode,
     * so this may or may not work for you
     * @return the JavaScript action
     */    
    public static PdfAction javaScript(String code, PdfWriter writer, boolean unicode) {
        PdfAction js = new PdfAction();
        js.put(PdfName.S, PdfName.JAVASCRIPT);
        if (unicode && code.length() < 50) {
                js.put(PdfName.JS, new PdfString(code, PdfObject.TEXT_UNICODE));
        }
        else if (!unicode && code.length() < 100) {
                js.put(PdfName.JS, new PdfString(code));
        }
        else {
            try {
                byte b[] = PdfEncodings.convertToBytes(code, unicode ? PdfObject.TEXT_UNICODE : PdfObject.TEXT_PDFDOCENCODING);
                PdfStream stream = new PdfStream(b);
                stream.flateCompress(writer.getCompressionLevel());
                js.put(PdfName.JS, writer.addToBody(stream).getIndirectReference());
            }
            catch (Exception e) {
                js.put(PdfName.JS, new PdfString(code));
            }
        }
        return js;
    }

    /** Creates a JavaScript action. If the JavaScript is smaller than
     * 50 characters it will be place as a string, otherwise it will
     * be placed as a compressed stream.
     * @param code the JavaScript code
     * @param writer the writer for this action
     * @return the JavaScript action
     */    
    public static PdfAction javaScript(String code, PdfWriter writer) {
        return javaScript(code, writer, false);
    }
    
    /**
     * A Hide action hides or shows an object.
     * @param obj object to hide or show
     * @param hide true is hide, false is show
     * @return a Hide Action
     */
    static PdfAction createHide(PdfObject obj, boolean hide) {
        PdfAction action = new PdfAction();
        action.put(PdfName.S, PdfName.HIDE);
        action.put(PdfName.T, obj);
        if (!hide)
            action.put(PdfName.H, PdfBoolean.PDFFALSE);
        return action;
    }
    
    /**
     * A Hide action hides or shows an annotation.
     * @param annot
     * @param hide
     * @return A Hide Action
     */
    public static PdfAction createHide(PdfAnnotation annot, boolean hide) {
        return createHide(annot.getIndirectReference(), hide);
    }
    
    /**
     * A Hide action hides or shows an annotation.
     * @param name
     * @param hide
     * @return A Hide Action
     */
    public static PdfAction createHide(String name, boolean hide) {
        return createHide(new PdfString(name), hide);
    }
    
    static PdfArray buildArray(Object names[]) {
        PdfArray array = new PdfArray();
        for (int k = 0; k < names.length; ++k) {
            Object obj = names[k];
            if (obj instanceof String)
                array.add(new PdfString((String)obj));
            else if (obj instanceof PdfAnnotation)
                array.add(((PdfAnnotation)obj).getIndirectReference());
            else
                throw new RuntimeException(MessageLocalization.getComposedMessage("the.array.must.contain.string.or.pdfannotation"));
        }
        return array;
    }
    
    /**
     * A Hide action hides or shows objects.
     * @param names
     * @param hide
     * @return A Hide Action
     */
    public static PdfAction createHide(Object names[], boolean hide) {
        return createHide(buildArray(names), hide);
    }
    
    /**
     * Creates a submit form.
     * @param file	the URI to submit the form to
     * @param names	the objects to submit
     * @param flags	submit properties
     * @return A PdfAction
     */
    public static PdfAction createSubmitForm(String file, Object names[], int flags) {
        PdfAction action = new PdfAction();
        action.put(PdfName.S, PdfName.SUBMITFORM);
        PdfDictionary dic = new PdfDictionary();
        dic.put(PdfName.F, new PdfString(file));
        dic.put(PdfName.FS, PdfName.URL);
        action.put(PdfName.F, dic);
        if (names != null)
            action.put(PdfName.FIELDS, buildArray(names));
        action.put(PdfName.FLAGS, new PdfNumber(flags));
        return action;
    }
    
    /**
     * Creates a resetform.
     * @param names	the objects to reset
     * @param flags	submit properties
     * @return A PdfAction
     */
    public static PdfAction createResetForm(Object names[], int flags) {
        PdfAction action = new PdfAction();
        action.put(PdfName.S, PdfName.RESETFORM);
        if (names != null)
            action.put(PdfName.FIELDS, buildArray(names));
        action.put(PdfName.FLAGS, new PdfNumber(flags));
        return action;
    }
    
    /**
     * Creates an Import field.
     * @param file
     * @return A PdfAction
     */
    public static PdfAction createImportData(String file) {
        PdfAction action = new PdfAction();
        action.put(PdfName.S, PdfName.IMPORTDATA);
        action.put(PdfName.F, new PdfString(file));
        return action;
    }
    
    /** Add a chained action.
     * @param na the next action
     */    
    public void next(PdfAction na) {
        PdfObject nextAction = get(PdfName.NEXT);
        if (nextAction == null)
            put(PdfName.NEXT, na);
        else if (nextAction.isDictionary()) {
            PdfArray array = new PdfArray(nextAction);
            array.add(na);
            put(PdfName.NEXT, array);
        }
        else {
            ((PdfArray)nextAction).add(na);
        }
    }
    
    /** Creates a GoTo action to an internal page.
     * @param page the page to go. First page is 1
     * @param dest the destination for the page
     * @param writer the writer for this action
     * @return a GoTo action
     */    
    public static PdfAction gotoLocalPage(int page, PdfDestination dest, PdfWriter writer) {
        PdfIndirectReference ref = writer.getPageReference(page);
        dest.addPage(ref);
        PdfAction action = new PdfAction();
        action.put(PdfName.S, PdfName.GOTO);
        action.put(PdfName.D, dest);
        return action;
    }

    /**
     * Creates a GoTo action to a named destination.
     * @param dest the named destination
     * @param isName if true sets the destination as a name, if false sets it as a String
     * @return a GoTo action
     */
    public static PdfAction gotoLocalPage(String dest, boolean isName) {
        PdfAction action = new PdfAction();
        action.put(PdfName.S, PdfName.GOTO);
        if (isName)
            action.put(PdfName.D, new PdfName(dest));
        else
            action.put(PdfName.D, new PdfString(dest, null));
        return action;
    }

    /**
     * Creates a GoToR action to a named destination.
     * @param filename the file name to go to
     * @param dest the destination name
     * @param isName if true sets the destination as a name, if false sets it as a String
     * @param newWindow open the document in a new window if <CODE>true</CODE>, if false the current document is replaced by the new document.
     * @return a GoToR action
     */
    public static PdfAction gotoRemotePage(String filename, String dest, boolean isName, boolean newWindow) {
        PdfAction action = new PdfAction();
        action.put(PdfName.F, new PdfString(filename));
        action.put(PdfName.S, PdfName.GOTOR);
        if (isName)
            action.put(PdfName.D, new PdfName(dest));
        else
            action.put(PdfName.D, new PdfString(dest, null));
        if (newWindow)
            action.put(PdfName.NEWWINDOW, PdfBoolean.PDFTRUE);
        return action;
    }

    /**
     * Creates a GoToE action to an embedded file.
     * @param filename	the root document of the target (null if the target is in the same document)
     * @param dest the named destination
     * @param isName if true sets the destination as a name, if false sets it as a String
     * @return a GoToE action
     */
    public static PdfAction gotoEmbedded(String filename, PdfTargetDictionary target, String dest, boolean isName, boolean newWindow) {
        if (isName)
            return gotoEmbedded(filename, target, new PdfName(dest), newWindow);
        else
            return gotoEmbedded(filename, target, new PdfString(dest, null), newWindow);
    }

    /**
     * Creates a GoToE action to an embedded file.
     * @param filename	the root document of the target (null if the target is in the same document)
     * @param target	a path to the target document of this action
     * @param dest		the destination inside the target document, can be of type PdfDestination, PdfName, or PdfString
     * @param newWindow	if true, the destination document should be opened in a new window
     * @return a GoToE action
     */
    public static PdfAction gotoEmbedded(String filename, PdfTargetDictionary target, PdfObject dest, boolean newWindow) {
    	PdfAction action = new PdfAction();
    	action.put(PdfName.S, PdfName.GOTOE);
    	action.put(PdfName.T, target);
    	action.put(PdfName.D, dest);
    	action.put(PdfName.NEWWINDOW, new PdfBoolean(newWindow));
    	if (filename != null) {
    		action.put(PdfName.F, new PdfString(filename));
    	}
    	return action;
    }

    /**
     * A set-OCG-state action (PDF 1.5) sets the state of one or more optional content
     * groups.
     * @param state an array consisting of any number of sequences beginning with a <CODE>PdfName</CODE>
     * or <CODE>String</CODE> (ON, OFF, or Toggle) followed by one or more optional content group dictionaries
     * <CODE>PdfLayer</CODE> or a <CODE>PdfIndirectReference</CODE> to a <CODE>PdfLayer</CODE>.<br>
     * The array elements are processed from left to right; each name is applied
     * to the subsequent groups until the next name is encountered:
     * <ul>
     * <li>ON sets the state of subsequent groups to ON</li>
     * <li>OFF sets the state of subsequent groups to OFF</li>
     * <li>Toggle reverses the state of subsequent groups</li>
     * </ul>
     * @param preserveRB if <CODE>true</CODE>, indicates that radio-button state relationships between optional
     * content groups (as specified by the RBGroups entry in the current configuration
     * dictionary) should be preserved when the states in the
     * <CODE>state</CODE> array are applied. That is, if a group is set to ON (either by ON or Toggle) during
     * processing of the <CODE>state</CODE> array, any other groups belong to the same radio-button
     * group are turned OFF. If a group is set to OFF, there is no effect on other groups.<br>
     * If <CODE>false</CODE>, radio-button state relationships, if any, are ignored
     * @return the action
     */    
    public static PdfAction setOCGstate(ArrayList state, boolean preserveRB) {
        PdfAction action = new PdfAction();
        action.put(PdfName.S, PdfName.SETOCGSTATE);
        PdfArray a = new PdfArray();
        for (int k = 0; k < state.size(); ++k) {
            Object o = state.get(k);
            if (o == null)
                continue;
            if (o instanceof PdfIndirectReference)
                a.add((PdfIndirectReference)o);
            else if (o instanceof PdfLayer)
                a.add(((PdfLayer)o).getRef());
            else if (o instanceof PdfName)
                a.add((PdfName)o);
            else if (o instanceof String) {
                PdfName name = null;
                String s = (String)o;
                if (s.equalsIgnoreCase("on"))
                    name = PdfName.ON;
                else if (s.equalsIgnoreCase("off"))
                    name = PdfName.OFF;
                else if (s.equalsIgnoreCase("toggle"))
                    name = PdfName.TOGGLE;
                else
                    throw new IllegalArgumentException(MessageLocalization.getComposedMessage("a.string.1.was.passed.in.state.only.on.off.and.toggle.are.allowed", s));
                a.add(name);
            }
            else
                throw new IllegalArgumentException(MessageLocalization.getComposedMessage("invalid.type.was.passed.in.state.1", o.getClass().getName()));
        }
        action.put(PdfName.STATE, a);
        if (!preserveRB)
            action.put(PdfName.PRESERVERB, PdfBoolean.PDFFALSE);
        return action;
    }
}
