/*
 * $Id: RtfParser.java 3580 2008-08-06 15:52:00Z howard_s $
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
package com.lowagie.text.rtf.parser;

import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.EventListener;
import java.util.Iterator;
import java.util.Stack;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.List;
import com.lowagie.text.rtf.direct.RtfDirectContent;
import com.lowagie.text.rtf.document.RtfDocument;
import com.lowagie.text.rtf.parser.ctrlwords.RtfCtrlWordData;
import com.lowagie.text.rtf.parser.ctrlwords.RtfCtrlWordListener;
import com.lowagie.text.rtf.parser.ctrlwords.RtfCtrlWordMgr;
import com.lowagie.text.rtf.parser.destinations.RtfDestination;
import com.lowagie.text.rtf.parser.destinations.RtfDestinationMgr;

/**
 * The RtfParser allows the importing of RTF documents or
 * RTF document fragments. The RTF document or fragment is tokenised,
 * font and color definitions corrected and then added to
 * the document being written.
 * 
 * @author Mark Hall (Mark.Hall@mail.room3b.eu)
 * @author Howard Shank (hgshank@yahoo.com)
 * @since 2.0.8
 */

public class RtfParser {
	/**
	 * Debugging flag.
	 */
	private static final boolean debugParser = false;	// DEBUG Files are unlikely to be read by any reader!
	private String logFile = null;
	private boolean logging = false;
	private boolean logAppend = false;
	
	/**
	 * The iText element to add the RTF document to.
	 * @since 2.1.3
	 */
	private Element elem = null;
	/**
	 * The iText document to add the RTF document to.
	 */
	private Document document = null;
	/**
	 * The RtfDocument to add the RTF document or fragment to.
	 */
	private RtfDocument rtfDoc = null;
	/**
	 * The RtfKeywords that creates and handles keywords that are implemented.
	 */
	private RtfCtrlWordMgr rtfKeywordMgr = null;
	/**
	 * The RtfImportHeader to store imported font and color mappings in.
	 */
	private RtfImportMgr importMgr = null;
	/**
	 * The RtfDestinationMgr object to manage destinations.
	 */
	private RtfDestinationMgr destinationMgr = null;
	/**
	 * Stack for saving states for groups
	 */
	private Stack stackState = null;
	/**
	 * The current parser state.
	 */	
	private RtfParserState currentState = null;
	/**
	 * The pushback reader to read the input stream.
	 */
	private PushbackInputStream pbReader = null;
	/**
	 * Conversion type. Identifies if we are doing in import or a convert.
	 */
	private int conversionType = TYPE_IMPORT_FULL;
	

	/*
	 * Bitmapping:
	 * 
	 * 0111 1111 1111 1111 = Unkown state
	 * 0xxx xxxx xxxx xxxx = In Header
	 * 1xxx xxxx xxxx xxxx = In Document
	 * 2xxx xxxx xxxx xxxx = Reserved
	 * 4xxx xxxx xxxx xxxx = Other
	 * 8xxx xxxx xxxx xxxx = Errors
	 */
	
	/*
	 * Header state values
	 */

	/**
	 * Currently the RTF document header is being parsed.
	 */
	public static final int PARSER_IN_HEADER = (0x0 << 28) | 0x000000;
	/**
	 * Currently the RTF charset is being parsed.
	 */
	public static final int PARSER_IN_CHARSET = PARSER_IN_HEADER | 0x000001;
	/**
	 * Currently the RTF deffont is being parsed.
	 */
	public static final int PARSER_IN_DEFFONT = PARSER_IN_HEADER | 0x000002;
	/**
	 * Currently the RTF font table is being parsed.
	 */
	public static final int PARSER_IN_FONT_TABLE = PARSER_IN_HEADER | 0x000003;
	/**
	 * Currently a RTF font table info element is being parsed.
	 */
	public static final int PARSER_IN_FONT_TABLE_INFO = PARSER_IN_HEADER | 0x000004;
	/**
	 * Currently the RTF filetbl is being parsed.
	 */
	public static final int PARSER_IN_FILE_TABLE = PARSER_IN_HEADER | 0x000005;
	/**
	 * Currently the RTF color table is being parsed.
	 */
	public static final int PARSER_IN_COLOR_TABLE = PARSER_IN_HEADER | 0x000006;
	/**
	 * Currently the RTF  stylesheet is being parsed.
	 */
	public static final int PARSER_IN_STYLESHEET = PARSER_IN_HEADER | 0x000007;
	/**
	 * Currently the RTF listtables is being parsed.
	 */
	public static final int PARSER_IN_LIST_TABLE = PARSER_IN_HEADER | 0x000008;
	/**
	 * Currently the RTF listtable override is being parsed.
	 */
	public static final int PARSER_IN_LISTOVERRIDE_TABLE = PARSER_IN_HEADER | 0x000009;
	/**
	 * Currently the RTF revtbl is being parsed.
	 */
	public static final int PARSER_IN_REV_TABLE = PARSER_IN_HEADER | 0x00000A;
	/**
	 * Currently the RTF rsidtable is being parsed.
	 */
	public static final int PARSER_IN_RSID_TABLE = PARSER_IN_HEADER | 0x0000B;
	/**
	 * Currently the RTF generator is being parsed.
	 */
	public static final int PARSER_IN_GENERATOR = PARSER_IN_HEADER | 0x00000C;
	/**
	 * Currently the RTF Paragraph group properties Table (word 2002)
	 */
	public static final int PARSER_IN_PARAGRAPH_TABLE = PARSER_IN_HEADER | 0x00000E;
	/**
	 * Currently the RTF Old Properties.
	 */
	public static final int PARSER_IN_OLDCPROPS = PARSER_IN_HEADER | 0x00000F;
	/**
	 * Currently the RTF Old Properties.
	 */
	public static final int PARSER_IN_OLDPPROPS = PARSER_IN_HEADER | 0x000010;
	/**
	 * Currently the RTF Old Properties.
	 */
	public static final int PARSER_IN_OLDTPROPS = PARSER_IN_HEADER | 0x000012;
	/**
	 * Currently the RTF Old Properties.
	 */
	public static final int PARSER_IN_OLDSPROPS = PARSER_IN_HEADER | 0x000013;
	/**
	 * Currently the RTF User Protection Information.
	 */
	public static final int PARSER_IN_PROT_USER_TABLE = PARSER_IN_HEADER | 0x000014;
	/**
	 * Currently the Latent Style and Formatting usage restrictions
	 */
	public static final int PARSER_IN_LATENTSTYLES = PARSER_IN_HEADER | 0x000015;
	
	public static final int PARSER_IN_PARAGRAPH_GROUP_PROPERTIES =PARSER_IN_HEADER | 0x000016;
	
	/*
	 * Document state values
	 */
	
	/**
	 * Currently the RTF document content is being parsed.
	 */
	public static final int PARSER_IN_DOCUMENT = (0x2 << 28 ) | 0x000000;

	/**
	 * Currently the RTF info group is being parsed.
	 */
	public static final int PARSER_IN_INFO_GROUP = PARSER_IN_DOCUMENT | 0x000001;

	
	public static final int PARSER_IN_UPR = PARSER_IN_DOCUMENT | 0x000002;
	/**
	 * Currently a shppict control word is being parsed.
	 */
	public static final int PARSER_IN_SHPPICT = PARSER_IN_DOCUMENT | 0x000010; //16
	/**
	 * Currently a pict control word is being parsed.
	 */
	public static final int PARSER_IN_PICT = PARSER_IN_DOCUMENT | 0x000011; //17
	/**
	 * Currently a picprop control word is being parsed.
	 */
	public static final int PARSER_IN_PICPROP = PARSER_IN_DOCUMENT | 0x000012; //18
	/**
	 * Currently a blipuid control word is being parsed.
	 */
	public static final int PARSER_IN_BLIPUID = PARSER_IN_DOCUMENT | 0x000013; //19

	/* other states */
	/**
	 * The parser is at the beginning or the end of the file.
	 */
	public static final int PARSER_STARTSTOP = (0x4 << 28)| 0x0001;
	/* ERRORS */
	/**
	 * Currently the parser is in an error state.
	 */
	public static final int PARSER_ERROR = (0x8 << 28) | 0x0000;
	/**
	 * The parser reached the end of the file.
	 */
	public static final int PARSER_ERROR_EOF = PARSER_ERROR | 0x0001;
	/**
	 * Currently the parser is in an unknown state.
	 */
	public static final int PARSER_IN_UNKNOWN = PARSER_ERROR | 0x0FFFFFFF;
	
	
	/**
	 * Conversion type is unknown
	 */
	public static final int TYPE_UNIDENTIFIED = -1;
	/**
	 * Conversion type is an import. Uses direct content to add everything.
	 * This is what the original import does.
	 */
	public static final int TYPE_IMPORT_FULL = 0;
	/**
	 * Conversion type is an import of a partial file/fragment. Uses direct content to add everything.
	 */
	public static final int TYPE_IMPORT_FRAGMENT = 1;
	/**
	 * Conversion type is a conversion. This uses the document (not rtfDoc) to add
	 * all the elements making it a different supported documents depending on the writer used.
	 */
	public static final int TYPE_CONVERT = 2;
	/**
	 * Conversion type to import a document into an element. i.e. Chapter, Section, Table Cell, etc.
	 * @since 2.1.4
	 */
	public static final int TYPE_IMPORT_INTO_ELEMENT = 3;

	
	/**
	 * Destination is normal. Text is processed.
	 */
	public static final int DESTINATION_NORMAL = 0;
	/**
	 * Destination is skipping. Text is ignored.
	 */
	public static final int DESTINATION_SKIP = 1;
	
	//////////////////////////////////// TOKENISE VARIABLES ///////////////////
	/*
	 * State flags use 4/28 bitmask.
	 * First 4 bits (nibble) indicates major state. Used for unknown and error
	 * Last 28 bits indicates the value;
	 */
	
	/**
	 * The RtfTokeniser is in its ground state. Any token may follow.
	 */
	public static final int TOKENISER_NORMAL = 0x00000000;
	/**
	 * The last token parsed was a slash.
	 */
	public static final int TOKENISER_SKIP_BYTES = 0x00000001;
	/**
	 * The RtfTokeniser is currently tokenising a control word.
	 */
	public static final int TOKENISER_SKIP_GROUP = 0x00000002;
	/**
	 * The RtfTokeniser is currently reading binary stream.
	 */
	public static final int TOKENISER_BINARY= 0x00000003;
	/**
	 * The RtfTokeniser is currently reading hex data.
	 */
	public static final int TOKENISER_HEX= 0x00000004;
	/**
	 * The RtfTokeniser ignore result
	 */
	public static final int TOKENISER_IGNORE_RESULT= 0x00000005;
	/**
	 * The RtfTokeniser is currently in error state
	 */
	public static final int TOKENISER_STATE_IN_ERROR =  0x80000000; // 1000 0000 0000 0000 0000 0000 0000 0000
	/**
	 * The RtfTokeniser is currently in an unkown state
	 */
	public static final int TOKENISER_STATE_IN_UNKOWN = 0xFF000000; // 1111 0000 0000 0000 0000 0000 0000 0000
	
	/**
	 * The current group nesting level.
	 */
	private int groupLevel = 0;
	/**
	 * The current document group nesting level. Used for fragments.
	 */
	private int docGroupLevel = 0;
	/**
	 * When the tokeniser is Binary.
	 */
	private long binByteCount = 0;
	/**
	 * When the tokeniser is set to skip bytes, binSkipByteCount is the number of bytes to skip.
	 */
	private long binSkipByteCount = 0;
	/**
	 * When the tokeniser is set to skip to next group, this is the group indentifier to return to.
	 */
	private int skipGroupLevel = 0;

	//RTF parser error codes
	public static final int  errOK =0;                        // Everything's fine!
	public static final int  errStackUnderflow   =  -1;       // Unmatched '}'
	public static final int  errStackOverflow    =  -2;       // Too many '{' -- memory exhausted
	public static final int  errUnmatchedBrace   =  -3;       // RTF ended during an open group.
	public static final int  errInvalidHex       =  -4;       // invalid hex character found in data
	public static final int  errBadTable         =  -5;       // RTF table (sym or prop) invalid
	public static final int  errAssertion        =  -6;       // Assertion failure
	public static final int  errEndOfFile        =  -7;       // End of file reached while reading RTF
	public static final int  errCtrlWordNotFound =  -8;		  // control word was not found
	//////////////////////////////////// TOKENISE VARIABLES ///////////////////
	
	
	//////////////////////////////////// STATS VARIABLES ///////////////////
	/**
	 * Total bytes read.
	 */
	private long byteCount = 0;
	/**
	 * Total control words processed.
	 * 
	 * Contains both known and unknown.
	 * 
	 * <code>ctrlWordCount</code> should equal 
	 * <code>ctrlWrodHandlecCount</code> + <code>ctrlWordNotHandledCount</code + <code>ctrlWordSkippedCount</code>
	 */
	private long ctrlWordCount = 0;
	/**
	 * Total { encountered as an open group token.
	 */
	private long openGroupCount = 0;
	/**
	 * Total } encountered as a close group token.
	 */
	private long closeGroupCount = 0;
	/**
	 * Total clear text characters processed.
	 */
	private long characterCount = 0;
	/**
	 * Total control words recognized.
	 */
	private long ctrlWordHandledCount = 0;
	/**
	 * Total control words not handled.
	 */
	private long ctrlWordNotHandledCount = 0;
	/**
	 * Total control words skipped.
	 */
	private long ctrlWordSkippedCount = 0;
	/**
	 * Total groups skipped. Includes { and } as a group.
	 */
	private long groupSkippedCount = 0;
	/**
	 * Start time as a long.
	 */
	private long startTime = 0;
	/**
	 * Stop time as a long.
	 */
	private long endTime = 0;
	/**
	 * Start date as a date.
	 */
	private Date startDate = null;
	/**
	 * End date as a date.
	 */
	private Date endDate = null;
	//////////////////////////////////// STATS VARIABLES ///////////////////
	/**
	 * Last control word and parameter processed.
	 */
	private RtfCtrlWordData lastCtrlWordParam = null;
	
	/** The <code>RtfCtrlWordListener</code>. */
    private ArrayList listeners = new ArrayList();
    
	/**
	 * Constructor 
	 * @param doc
	 * @since 2.1.3
	 */
    public RtfParser(Document doc) {
    	this.document = doc;
    }
	/* *********
	 *  READER *
	 ***********/
	/**
	 * Imports a complete RTF document.
	 * 
	 * @param readerIn 
	 * 		The Reader to read the RTF document from.
	 * @param rtfDoc 
	 * 		The RtfDocument to add the imported document to.
	 * @throws IOException On I/O errors.
	 *  @since 2.1.3
	 */
	public void importRtfDocument(InputStream readerIn, RtfDocument rtfDoc) throws IOException {
		if(readerIn == null || rtfDoc == null) return;
		this.init(TYPE_IMPORT_FULL, rtfDoc, readerIn, this.document, null);
		this.setCurrentDestination(RtfDestinationMgr.DESTINATION_NULL);
		startDate = new Date();
		startTime = System.currentTimeMillis();
		this.groupLevel = 0;
		try {
			this.tokenise();
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		endTime = System.currentTimeMillis();
		endDate = new Date();
	}
	/**
	 * Imports a complete RTF document into an Element, i.e. Chapter, section, Table Cell, etc.
	 * 
	 * @param elem The Element the document is to be imported into.
	 * @param readerIn 
	 * 		The Reader to read the RTF document from.
	 * @param rtfDoc 
	 * 		The RtfDocument to add the imported document to.
	 * @throws IOException On I/O errors.
	 * @since 2.1.4
	 */
	public void importRtfDocumentIntoElement(Element elem, InputStream readerIn, RtfDocument rtfDoc) throws IOException {
		if(readerIn == null || rtfDoc == null || elem == null) return;
		this.init(TYPE_IMPORT_INTO_ELEMENT, rtfDoc, readerIn, this.document, elem);
		this.setCurrentDestination(RtfDestinationMgr.DESTINATION_NULL);
		startDate = new Date();
		startTime = System.currentTimeMillis();
		this.groupLevel = 0;
		try {
			this.tokenise();
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		endTime = System.currentTimeMillis();
		endDate = new Date();
	}
	/**
	 * Converts an RTF document to an iText document.
	 * 
	 * Usage: Create a parser object and call this method with the input stream and the iText Document object
	 * 
	 * @param readerIn 
	 * 		The Reader to read the RTF file from.
	 * @param doc 
	 * 		The iText document that the RTF file is to be added to.
	 * @throws IOException 
	 * 		On I/O errors.
	 *  @since 2.1.3
	 */
	public void convertRtfDocument(InputStream readerIn, Document doc) throws IOException {
		if(readerIn == null || doc == null) return;
		this.init(TYPE_CONVERT, null, readerIn, doc, null);
		this.setCurrentDestination(RtfDestinationMgr.DESTINATION_DOCUMENT);
		startDate = new Date();
		startTime = System.currentTimeMillis();
		this.groupLevel = 0;
		this.tokenise();
		endTime = System.currentTimeMillis();
		endDate = new Date();
	}

	/**
	 * Imports an RTF fragment.
	 * 
	 * @param readerIn 
	 * 		The Reader to read the RTF fragment from.
	 * @param rtfDoc 
	 * 		The RTF document to add the RTF fragment to.
	 * @param importMappings 
	 * 		The RtfImportMappings defining font and color mappings for the fragment.
	 * @throws IOException 
	 * 		On I/O errors.
	 *   @since 2.1.3
	 */
	public void importRtfFragment(InputStream readerIn, RtfDocument rtfDoc, RtfImportMappings importMappings) throws IOException {
	//public void importRtfFragment2(Reader readerIn, RtfDocument rtfDoc, RtfImportMappings importMappings) throws IOException {
		if(readerIn == null || rtfDoc == null || importMappings==null) return;
		this.init(TYPE_IMPORT_FRAGMENT, rtfDoc, readerIn, null, null);
		this.handleImportMappings(importMappings);
		this.setCurrentDestination(RtfDestinationMgr.DESTINATION_DOCUMENT);
		this.groupLevel = 1;
		setParserState(RtfParser.PARSER_IN_DOCUMENT);
		startDate = new Date();
		startTime = System.currentTimeMillis();
		this.tokenise();
		endTime = System.currentTimeMillis();
		endDate = new Date();
	}
	
    // listener methods

	/**
	 * Adds a <CODE>EventListener</CODE> to the <CODE>RtfCtrlWordMgr</CODE>.
	 *
	 * @param listener
	 *            the new EventListener.
	 * @since 2.1.3
	 */
	public void addListener(EventListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes a <CODE>EventListener</CODE> from the <CODE>RtfCtrlWordMgr</CODE>.
	 *
	 * @param listener
	 *            the EventListener that has to be removed.
	 *  @since 2.1.3
	 */
	public void removeListener(EventListener listener) {
		listeners.remove(listener);
	}

	/**
	 * Initialize the parser object values. 
	 * 
	 * @param type Type of conversion or import
	 * @param rtfDoc The <code>RtfDocument</code>
	 * @param readerIn The input stream
	 * @param doc The iText <code>Document</code>
	 *   @since 2.1.3
	 */
	private void init(int type, RtfDocument rtfDoc, InputStream readerIn, Document doc, Element elem) {

		init_stats();
		// initialize reader to a PushbackReader
		this.pbReader = init_Reader(readerIn);
		
		this.conversionType = type;
		this.rtfDoc = rtfDoc;
		this.document = doc;
		this.elem = elem;
		this.currentState = new RtfParserState();
		this.stackState = new Stack();
		this.setParserState(PARSER_STARTSTOP);
		this.importMgr = new RtfImportMgr(this.rtfDoc, this.document);

		// get destination Mgr
		this.destinationMgr = RtfDestinationMgr.getInstance(this);
		// set the parser
		RtfDestinationMgr.setParser(this);


		// DEBUG INFO for timing and memory usage of RtfCtrlWordMgr object
		// create multiple new RtfCtrlWordMgr objects to check timing and memory usage
//		System.gc();
//		long endTime = 0;
//		Date endDate = null;		
//		long endFree = 0;
//		DecimalFormat df = new DecimalFormat("#,##0");
//		Date startDate = new Date();
//		long startTime = System.currentTimeMillis();
//		long startFree = Runtime.getRuntime().freeMemory();
//		System.out.println("1:");
		
		this.rtfKeywordMgr = new RtfCtrlWordMgr(this, this.pbReader);/////////DO NOT COMMENT OUT THIS LINE ///////////
		
		Object listener;
		for (Iterator iterator = listeners.iterator(); iterator.hasNext();) {
            listener = iterator.next();
            if(listener instanceof RtfCtrlWordListener) {
                this.rtfKeywordMgr.addRtfCtrlWordListener((RtfCtrlWordListener)listener);    
            }
        }
//		endFree = Runtime.getRuntime().freeMemory();
//		endTime = System.currentTimeMillis();
//		endDate = new Date();
//		System.out.println("RtfCtrlWordMgr start date: " + startDate.toLocaleString());
//		System.out.println("RtfCtrlWordMgr end date  : " + endDate.toLocaleString());
//		System.out.println("  Elapsed time    : " + Long.toString(endTime - startTime) + " milliseconds.");
//		System.out.println("Begin Constructor RtfCtrlWordMgr , free mem is " + df.format(startFree / 1024) + "k");
//		System.out.println("End Constructor RtfCtrlWordMgr , free mem is " + df.format(endFree / 1024) + "k");
//		System.out.println("RtfCtrlWordMgr used approximately " + df.format((startFree - endFree) / 1024) + "k");
//		
//		System.gc();
//		System.out.println("2:");
//		startDate = new Date();
//		startTime = System.currentTimeMillis();
//		startFree = Runtime.getRuntime().freeMemory();
//		RtfCtrlWordMgr rtfKeywordMgr2 = new RtfCtrlWordMgr(this, this.pbReader);		
//		endFree = Runtime.getRuntime().freeMemory();
//		endTime = System.currentTimeMillis();
//		endDate = new Date();
//		System.out.println("RtfCtrlWordMgr start date: " + startDate.toLocaleString());
//		System.out.println("RtfCtrlWordMgr end date  : " + endDate.toLocaleString());
//		System.out.println("  Elapsed time    : " + Long.toString(endTime - startTime) + " milliseconds.");
//		System.out.println("Begin Constructor RtfCtrlWordMgr , free mem is " + df.format(startFree / 1024) + "k");
//		System.out.println("End Constructor RtfCtrlWordMgr , free mem is " + df.format(endFree / 1024) + "k");
//		System.out.println("RtfCtrlWordMgr used approximately " + df.format((startFree - endFree) / 1024) + "k");
//		
//		System.gc();
//		System.out.println("3:");
//		startDate = new Date();
//		startTime = System.currentTimeMillis();
//		startFree = Runtime.getRuntime().freeMemory();
//		RtfCtrlWordMgr rtfKeywordMgr3 = new RtfCtrlWordMgr(this, this.pbReader);	
//		endFree = Runtime.getRuntime().freeMemory();
//		endTime = System.currentTimeMillis();
//		endDate = new Date();
//		System.out.println("RtfCtrlWordMgr start date: " + startDate.toLocaleString());
//		System.out.println("RtfCtrlWordMgr end date  : " + endDate.toLocaleString());
//		System.out.println("  Elapsed time    : " + Long.toString(endTime - startTime) + " milliseconds.");
//		System.out.println("Begin Constructor RtfCtrlWordMgr , free mem is " + df.format(startFree / 1024) + "k");
//		System.out.println("End Constructor RtfCtrlWordMgr , free mem is " + df.format(endFree / 1024) + "k");
//		System.out.println("RtfCtrlWordMgr used approximately " + df.format((startFree - endFree) / 1024) + "k");
//
//		System.gc();
//		System.out.println("4:");
//		startDate = new Date();
//		startTime = System.currentTimeMillis();
//		startFree = Runtime.getRuntime().freeMemory();
//		RtfCtrlWordMgr rtfKeywordMgr4 = new RtfCtrlWordMgr(this, this.pbReader);	
//		endFree = Runtime.getRuntime().freeMemory();
//		endTime = System.currentTimeMillis();
//		endDate = new Date();
//		System.out.println("RtfCtrlWordMgr start date: " + startDate.toLocaleString());
//		System.out.println("RtfCtrlWordMgr end date  : " + endDate.toLocaleString());
//		System.out.println("  Elapsed time    : " + Long.toString(endTime - startTime) + " milliseconds.");
//		System.out.println("Begin Constructor RtfCtrlWordMgr , free mem is " + df.format(startFree / 1024) + "k");
//		System.out.println("End Constructor RtfCtrlWordMgr , free mem is " + df.format(endFree / 1024) + "k");
//		System.out.println("RtfCtrlWordMgr used approximately " + df.format((startFree - endFree) / 1024) + "k");
//
//		System.gc();
//		System.out.println("5:");
//		startDate = new Date();
//		startTime = System.currentTimeMillis();
//		startFree = Runtime.getRuntime().freeMemory();
//		RtfCtrlWordMgr rtfKeywordMgr5 = new RtfCtrlWordMgr(this, this.pbReader);	
//		endFree = Runtime.getRuntime().freeMemory();
//		endTime = System.currentTimeMillis();
//		endDate = new Date();
//		System.out.println("RtfCtrlWordMgr start date: " + startDate.toLocaleString());
//		System.out.println("RtfCtrlWordMgr end date  : " + endDate.toLocaleString());
//		System.out.println("  Elapsed time    : " + Long.toString(endTime - startTime) + " milliseconds.");
//		System.out.println("Begin Constructor RtfCtrlWordMgr , free mem is " + df.format(startFree / 1024) + "k");
//		System.out.println("End Constructor RtfCtrlWordMgr , free mem is " + df.format(endFree / 1024) + "k");
//		System.out.println("RtfCtrlWordMgr used approximately " + df.format((startFree - endFree) / 1024) + "k");
//		System.gc();
//		System.out.println("At ed:");
//		startDate = new Date();
//		startTime = System.currentTimeMillis();
//		startFree = Runtime.getRuntime().freeMemory();
//		//RtfCtrlWordMgr rtfKeywordMgr6 = new RtfCtrlWordMgr(this, this.pbReader);	
//		endFree = Runtime.getRuntime().freeMemory();
//		endTime = System.currentTimeMillis();
//		endDate = new Date();
//		System.out.println("RtfCtrlWordMgr start date: " + startDate.toLocaleString());
//		System.out.println("RtfCtrlWordMgr end date  : " + endDate.toLocaleString());
//		System.out.println("  Elapsed time    : " + Long.toString(endTime - startTime) + " milliseconds.");
//		System.out.println("Begin Constructor RtfCtrlWordMgr , free mem is " + df.format(startFree / 1024) + "k");
//		System.out.println("End Constructor RtfCtrlWordMgr , free mem is " + df.format(endFree / 1024) + "k");
//		System.out.println("RtfCtrlWordMgr used approximately " + df.format((startFree - endFree) / 1024) + "k");
	}
	/**
	 * Initialize the statistics values.
	 * @since 2.1.3
	 */
	protected void init_stats() {
		byteCount = 0;
		ctrlWordCount = 0;
		openGroupCount = 0;
		closeGroupCount = 0;
		characterCount = 0;
		ctrlWordHandledCount = 0;
		ctrlWordNotHandledCount = 0;
		ctrlWordSkippedCount = 0;
		groupSkippedCount = 0;
		startTime = 0;
		endTime = 0;
		startDate = null;
		endDate = null;
	}
	
	/**
	 * Casts the input reader to a PushbackReader or 
	 * creates a new PushbackReader from the Reader passed in.
	 * The reader is also transformed into a BufferedReader if necessary.
	 * 
	 * @param readerIn
	 * 		The Reader object for the input file.
	 * @return
	 * 		PushbackReader object
	 * @since 2.1.3
	 */
	private PushbackInputStream init_Reader(InputStream readerIn) {
//		Reader newReader = readerIn;
//		// Initializing the reader as a BufferedReader 
//		// cut test processing time by approximately 50%
//		// default uses 8192 character buffer
//		if(!(newReader instanceof BufferedReader)) {
//			newReader = new BufferedReader(newReader);	// Since JDK1.1
//		}
//		// Initializing the reader as a PushbackReader is
//		// a requirement of the parser to be able to put back
//		// read ahead characters.
//		if(!(newReader instanceof PushbackReader)) {
//			newReader = new PushbackReader(newReader);	// Since JDK1.1
//		}
		
		if(!(readerIn instanceof BufferedInputStream)) {
			readerIn = new BufferedInputStream(readerIn);
		}
		if(!(readerIn instanceof PushbackInputStream)) {
			readerIn = new PushbackInputStream(readerIn);
		}
		// return the proper reader object to the parser setup
		return  (PushbackInputStream)readerIn;
	}
	
	/**
	 * Imports the mappings defined in the RtfImportMappings into the
	 * RtfImportHeader of this RtfParser2.
	 * 
	 * @param importMappings 
	 * 		The RtfImportMappings to import.
	 * @since 2.1.3
	 */
	private void handleImportMappings(RtfImportMappings importMappings) {
		Iterator it = importMappings.getFontMappings().keySet().iterator();
		while(it.hasNext()) {
			String fontNr = (String) it.next();
			this.importMgr.importFont(fontNr, (String) importMappings.getFontMappings().get(fontNr));
		}
		it = importMappings.getColorMappings().keySet().iterator();
		while(it.hasNext()) {
			String colorNr = (String) it.next();
			this.importMgr.importColor(colorNr, (Color) importMappings.getColorMappings().get(colorNr));
		}
		it = importMappings.getListMappings().keySet().iterator();
		while(it.hasNext()) {
			String listNr = (String) it.next();
			this.importMgr.importList(listNr, (String)importMappings.getListMappings().get(listNr));
		}
		it = importMappings.getStylesheetListMappings().keySet().iterator();
		while(it.hasNext()) {
			String stylesheetListNr = (String) it.next();
			this.importMgr.importStylesheetList(stylesheetListNr, (List) importMappings.getStylesheetListMappings().get(stylesheetListNr));
		}
		
	}
	
	
	/* *****************************************
	 *   DOCUMENT CONTROL METHODS
	 *   
	 *   Handles -
	 *   handleOpenGroup: 	Open groups		- '{'
	 *   handleCloseGroup: 	Close groups	- '}'
	 *   handleCtrlWord: 	Ctrl Words		- '\...'
	 *   handleCharacter: 	Characters		- Plain Text, etc.
	 * 
	 */
	
	/**
	 * Handles open group tokens. ({)
	 * 
	 * @return errOK if ok, other if an error occurred.
	 * @since 2.1.3
	 */
	public int handleOpenGroup() {
		int result = errOK;
		this.openGroupCount++;	// stats
		this.groupLevel++;		// current group level in tokeniser
		this.docGroupLevel++;	// current group level in document
		if (this.getTokeniserState() == TOKENISER_SKIP_GROUP) { 
			this.groupSkippedCount++;
		}
	
		RtfDestination dest = this.getCurrentDestination();
		boolean handled = false;
		
		if(dest != null) {
			if(debugParser) {
				RtfParser.outputDebug(this.rtfDoc, groupLevel, "DEBUG: before dest.handleOpeningSubGroup()");
				RtfParser.outputDebug(this.rtfDoc, groupLevel, "DEBUG: destination=" + dest.toString());
			}
			handled = dest.handleOpeningSubGroup();
			if(debugParser) {
				RtfParser.outputDebug(this.rtfDoc, groupLevel, "DEBUG: after dest.handleOpeningSubGroup()");
			}
		}

		this.stackState.push(this.currentState);
		this.currentState = new RtfParserState(this.currentState);
		// do not set this true until after the state is pushed
		// otherwise it inserts a { where one does not belong.
		this.currentState.newGroup = true;
		dest = this.getCurrentDestination();
		
		if(debugParser) {
			RtfParser.outputDebug(this.rtfDoc, groupLevel, "DEBUG: handleOpenGroup()");
			if(this.lastCtrlWordParam != null)
				RtfParser.outputDebug(this.rtfDoc, groupLevel, "DEBUG: LastCtrlWord=" + this.lastCtrlWordParam.ctrlWord);
			RtfParser.outputDebug(this.rtfDoc, groupLevel, "DEBUG: grouplevel=" + Integer.toString(groupLevel));
			RtfParser.outputDebug(this.rtfDoc, groupLevel, "DEBUG: destination=" + dest.toString());
		}

		if(dest != null) {
			handled = dest.handleOpenGroup();
		}
		
		if(debugParser) {
			RtfParser.outputDebug(this.rtfDoc, groupLevel, "DEBUG: after dest.handleOpenGroup(); handled=" + Boolean.toString(handled));
		}
		
		return result;
	}
	public static void outputDebug(Object doc, int groupLevel, String str) {
		System.out.println(str);
		if(doc == null) return;
		if(groupLevel<0) groupLevel = 0;
		char[] a; Arrays.fill(a= new char[groupLevel*2], ' ');
		String spaces= new String(a);
		if(doc instanceof RtfDocument) {
			((RtfDocument)doc).add(new RtfDirectContent("\n" + spaces + str));
		}
		else
			if(doc instanceof Document) {
				try {
					((Document)doc).add(new RtfDirectContent("\n" + spaces + str));
				} catch (DocumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
	}
	/**
	 * Handles close group tokens. (})
	 * 
	 * @return errOK if ok, other if an error occurred.
	 * @since 2.1.3
	 */
	public int handleCloseGroup() {
		int result = errOK;
		this.closeGroupCount++;	// stats

		if (this.getTokeniserState() != TOKENISER_SKIP_GROUP) {
			if(debugParser) {
				RtfParser.outputDebug(this.rtfDoc, groupLevel, "DEBUG: handleCloseGroup()");
				if(this.lastCtrlWordParam != null)
					RtfParser.outputDebug(this.rtfDoc, groupLevel, "DEBUG: LastCtrlWord=" + this.lastCtrlWordParam.ctrlWord);
				RtfParser.outputDebug(this.rtfDoc, groupLevel, "DEBUG: grouplevel=" + Integer.toString(groupLevel));
				RtfParser.outputDebug(this.rtfDoc, groupLevel, "DEBUG: destination=" + this.getCurrentDestination().toString());
				RtfParser.outputDebug(this.rtfDoc, groupLevel, "");
			}
			RtfDestination dest = this.getCurrentDestination();
			boolean handled = false;
			
			if(dest != null) {
				handled = dest.handleCloseGroup();
			}
			if(debugParser) {
				RtfParser.outputDebug(this.rtfDoc, groupLevel, "DEBUG: After dest.handleCloseGroup(); handled = " + Boolean.toString(handled));
				RtfParser.outputDebug(this.rtfDoc, groupLevel, "");
			}
		}
		
		if(this.stackState.size() >0 ) {
			this.currentState = (RtfParserState)this.stackState.pop();
		} else {
			result = errStackUnderflow;
		}
		
		this.docGroupLevel--;
		this.groupLevel--;
		
		if (this.getTokeniserState() == TOKENISER_SKIP_GROUP && this.groupLevel < this.skipGroupLevel) {
			this.setTokeniserState(TOKENISER_NORMAL);
		}

		return result;	
	}
	

	/**
	 * Handles control word tokens. Depending on the current
	 * state a control word can lead to a state change. When
	 * parsing the actual document contents, certain tabled
	 * values are remapped. i.e. colors, fonts, styles, etc.
	 * 
	 * @param ctrlWordData The control word to handle.
	 * @return errOK if ok, other if an error occurred.
	 * @since 2.1.3
	 */
	public int handleCtrlWord(RtfCtrlWordData ctrlWordData) {
		int result = errOK;
		this.ctrlWordCount++; // stats

		if(debugParser) {
			RtfParser.outputDebug(this.rtfDoc, groupLevel, "DEBUG: handleCtrlWord=" + ctrlWordData.ctrlWord + " param=[" + ctrlWordData.param + "]");
		}

		if (this.getTokeniserState() == TOKENISER_SKIP_GROUP) { 
			this.ctrlWordSkippedCount++;
			if(debugParser) {
				RtfParser.outputDebug(this.rtfDoc, groupLevel, "DEBUG: SKIPPED");
			}
			return result;
		}

		//		RtfDestination dest = (RtfDestination)this.getCurrentDestination();
//		boolean handled = false;
//		if(dest != null) {
//			handled = dest.handleControlWord(ctrlWordData);
//		}
		
		result = this.rtfKeywordMgr.handleKeyword(ctrlWordData, this.groupLevel);

		if( result == errOK){
			this.ctrlWordHandledCount++;
		} else {
			this.ctrlWordNotHandledCount++;
			result = errOK;	// hack for now.
		}
		
		return result;
	}

	/**
	 * Handles text tokens. These are either handed on to the
	 * appropriate destination handler.
	 * 
	 * @param nextChar
	 * 		The text token to handle.
	 * @return errOK if ok, other if an error occurred. 
	 * @since 2.1.3
	 */
//	public int handleCharacter(char[] nextChar) {		
	public int handleCharacter(int nextChar) {		
		this.characterCount++;	// stats

		if (this.getTokeniserState() == TOKENISER_SKIP_GROUP) { 
			return errOK;
		}

		boolean handled = false;

		RtfDestination dest = this.getCurrentDestination();
		if(dest != null) {
			handled = dest.handleCharacter(nextChar);
		}

		return errOK;
	}

	/**
	 * Get the state of the parser.
	 *
	 * @return
	 * 		The current RtfParserState state object.
	 * @since 2.1.3
	 */
	public RtfParserState getState(){
		return this.currentState;
	}	

	/**
	 * Get the current state of the parser.
	 * 
	 * @return 
	 * 		The current state of the parser.
	 * @since 2.1.3
	 */
	public int getParserState(){
		return this.currentState.parserState;
	}
	
	/**
	 * Set the state value of the parser.
	 *
	 * @param newState
	 * 		The new state for the parser
	 * @return
	 * 		The state of the parser.
	 * @since 2.1.3
	 */
	public int setParserState(int newState){
		this.currentState.parserState = newState;
		return this.currentState.parserState;
	}

	/**
	 * Get the conversion type.
	 * 
	 * @return
	 * 		The type of the conversion. Import or Convert.
	 * @since 2.1.3
	 */
	public int getConversionType() {
		return this.conversionType;
	}
	
	/**
	 * Get the RTF Document object.
	 * @return
	 * 		Returns the object rtfDoc.
	 * @since 2.1.3
	 */
	public RtfDocument getRtfDocument() {
		return this.rtfDoc;
	}
	
	/**
	 * Get the Document object.
	 * @return
	 * 		Returns the object rtfDoc.
	 * @since 2.1.3
	 */
	public Document getDocument() {
		return this.document;
	}

	/**
	 * Get the RtfImportHeader object.
	 * @return
	 * 		Returns the object importHeader.
	 * @since 2.1.3
	 */
	public RtfImportMgr getImportManager() {
		return importMgr;
	}
	
	
	/////////////////////////////////////////////////////////////
	// accessors for destinations
	/**
	 * Set the current destination object for the current state.
	 * @param destination The destination value to set.
	 * @since 2.1.3
	 */
	public boolean setCurrentDestination(String destination) {
			RtfDestination dest = RtfDestinationMgr.getDestination(destination);
			if(dest != null) {
				this.currentState.destination = dest;
				return false;
			} else {
				this.setTokeniserStateSkipGroup();
				return false;
			}
	}
	/**
	 * Get the current destination object.
	 * 
	 * @return The current state destination
	 * @since 2.1.3
	 */
	public RtfDestination getCurrentDestination() {
		return this.currentState.destination;
	}
	/**
	 * Get a destination from the map
	 * 
	 * @param destination The string destination.
	 * @return The destination object from the map
	 * @since 2.1.3
	 */
	public RtfDestination getDestination(String destination) {
		return RtfDestinationMgr.getDestination(destination);
	}
	
	/**
	 * Helper method to determine if this is a new group.
	 * 
	 * @return true if this is a new group, otherwise it returns false.
	 * @since 2.1.3
	 */
	public boolean isNewGroup() {
		return this.currentState.newGroup;
	}
	/**
	 * Helper method to set the new group flag
	 * @param value The boolean value to set the flag
	 * @return The value of newGroup
	 * @since 2.1.3
	 */
	public boolean setNewGroup(boolean value) {
		this.currentState.newGroup = value;
		return this.currentState.newGroup;
	}
	
	/* ************
	 *  TOKENISER *
	 **************/
	
	/**
	 * Read through the input file and parse the data stream into tokens.
	 * 
	 * @throws IOException on IO error.
	 * @since 2.1.3
	 */	
	public void tokenise() throws IOException {
		int errorCode = errOK;	// error code
		int nextChar = 0;
//		char[] nextChar = new char[1]; // input variable
//		nextChar[0]=0;	// set to 0
		this.setTokeniserState(TOKENISER_NORMAL);	// set initial tokeniser state
		
		
//		while(this.pbReader.read(nextChar) != -1) {
		while((nextChar = this.pbReader.read()) != -1) {
			this.byteCount++;
			
	        if (this.getTokeniserState() == TOKENISER_BINARY)                      // if we're parsing binary data, handle it directly
	        {
	            if ((errorCode = parseChar(nextChar)) != errOK)
	                return; 
	        }  else {
//				switch(nextChar[0]) {
				switch(nextChar) {
					case '{':	// scope delimiter - Open
						this.handleOpenGroup();
						break;
					case '}':  // scope delimiter - Close
						this.handleCloseGroup();
						break;
					case 0x0a:	// noise character
					case 0x0d:	// noise character
//						if(this.isImport()) {
//							this.rtfDoc.add(new RtfDirectContent(new String(nextChar)));
//						}
						break;
					case '\\':	// Control word start delimiter
							if(parseCtrlWord(pbReader) != errOK) {
							// TODO: Indicate some type of error
							return;
						}
						break;
					default:
						if(groupLevel == 0) { // BOMs
							break;
						}
						if(this.getTokeniserState() == TOKENISER_HEX) {
							StringBuffer hexChars = new StringBuffer();
							hexChars.append(nextChar);
//							if(pbReader.read(nextChar) == -1) {
							if((nextChar = pbReader.read()) == -1) {
								return;
							}
							this.byteCount++;
							hexChars.append(nextChar);
	                    	try {
//								nextChar[0]=(char)Integer.parseInt(hexChars.toString(), 16);
								nextChar=Integer.parseInt(hexChars.toString(), 16);
							} catch (NumberFormatException e) {
								return;
							}
		                    this.setTokeniserState(TOKENISER_NORMAL);
						}
						if ((errorCode = parseChar(nextChar)) != errOK) {
                        	return; // some error occurred. we should send a
									// real error
						}
						break;
				}	// switch(nextChar[0])
			}	// end if (this.getTokeniserState() == TOKENISER_BINARY)
	        
//	        if(groupLevel < 1 && this.isImportFragment()) return; //return errOK;
//	        if(groupLevel < 0 && this.isImportFull()) return; //return errStackUnderflow;
//	        if(groupLevel < 0 && this.isConvert()) return; //return errStackUnderflow;
	        
		}// end while(reader.read(nextChar) != -1)
		RtfDestination dest = this.getCurrentDestination();
		if(dest != null) {
			dest.closeDestination();
		}
	}
	
	/**
	 * Process the character and send it to the current destination.
	 * @param nextChar
	 * 		The character to process
	 * @return
	 * 		Returns an error code or errOK if no error.
	 * @since 2.1.3
	 */
	private int parseChar(int nextChar) {
		// figure out where to put the character
		// needs to handle group levels for parsing
		// examples
		/*
		 * {\f3\froman\fcharset2\fprq2{\*\panose 05050102010706020507}Symbol;}
		 * {\f7\fswiss\fcharset0\fprq2{\*\panose 020b0604020202030204}Helv{\*\falt Arial};} <- special case!!!!
		 * {\f5\froman\fcharset0 Tahoma;}
		 * {\f6\froman\fcharset0 Arial Black;}
		 * {\info(\author name}{\company company name}}
		 * ... document text ...
		 */
	    if (this.getTokeniserState() == TOKENISER_BINARY && --binByteCount <= 0)
	    	this.setTokeniserStateNormal();
	    if (this.getTokeniserState() == TOKENISER_SKIP_BYTES && --binSkipByteCount <= 0)
	    	this.setTokeniserStateNormal();
	    return this.handleCharacter(nextChar);
	}
	
	/**
	 * Parses a keyword and it's parameter if one exists
	 * @param reader
	 * 		This is a pushback reader for file input.
	 * @return
	 * 		Returns an error code or errOK if no error.
	 * @throws IOException
	 * 		Catch any file read problem.
	 * @since 2.1.3
	 */
	private int parseCtrlWord(PushbackInputStream reader) throws IOException {
		int nextChar = 0;
		int result = errOK;
		
		if((nextChar = reader.read()) == -1) {
			return errEndOfFile;
		}
		this.byteCount++;

		StringBuffer parsedCtrlWord = new StringBuffer();
		StringBuffer parsedParam= new StringBuffer();
		RtfCtrlWordData ctrlWordParam = new RtfCtrlWordData();
		
		if(!Character.isLetterOrDigit((char)nextChar)) {
			parsedCtrlWord.append((char)nextChar);
			ctrlWordParam.ctrlWord = parsedCtrlWord.toString();
			result =  this.handleCtrlWord(ctrlWordParam);
			lastCtrlWordParam = ctrlWordParam;
			return result;
		}
		
		do {
			parsedCtrlWord.append((char)nextChar);
			//TODO: catch EOF
			nextChar = reader.read();
			this.byteCount++;
		} while  (Character.isLetter((char)nextChar));
		
		ctrlWordParam.ctrlWord = parsedCtrlWord.toString();

		if(nextChar == '-') {
			ctrlWordParam.isNeg = true;
			if((nextChar = reader.read()) == -1) {
					return errEndOfFile;
			}
			this.byteCount++;
		}
		

		if(Character.isDigit((char)nextChar)) {
			ctrlWordParam.hasParam = true;
			do {
				parsedParam.append((char)nextChar);
				//TODO: catch EOF
				nextChar = reader.read();
				this.byteCount++;
				} while  (Character.isDigit((char)nextChar));
						
			ctrlWordParam.param = parsedParam.toString();
		}
		
		// push this character back into the stream
		if(nextChar != ' ') { 
			reader.unread(nextChar);
		}
		
	    if(debugParser) {
	//	    // debug: insrsid6254399
	//	    if(ctrlWordParam.ctrlWord.equals("proptype") && ctrlWordParam.param.equals("30")) {
	//	    	System.out.print("Debug value found\n");
	//	    }
//		    if(ctrlWordParam.ctrlWord.equals("cf") ) {
//		    	System.out.print("Debug value found\n");
//		    }
	    }
	    
		result = this.handleCtrlWord(ctrlWordParam);
		lastCtrlWordParam = ctrlWordParam;
		return result;

	}
	
	/**
	 * Set the current state of the tokeniser.
	 * @param value The new state of the tokeniser.
	 * @return The state of the tokeniser.
	 * @since 2.1.3
	 */
	public int setTokeniserState(int value) {
		this.currentState.tokeniserState = value;
		return this.currentState.tokeniserState;
	}
	
	/**
	 * Get the current state of the tokeniser.
	 * @return The current state of the tokeniser.
	 * @since 2.1.3
	 */
	public int getTokeniserState() {
		return this.currentState.tokeniserState;
	}

	/**
	 * Gets the current group level
	 * 
	 * @return
	 * 		The current group level value.
	 * @since 2.1.3
	 */
	public int getLevel() {
		return this.groupLevel;
	}
	

	/**
	 * Set the tokeniser state to skip to the end of the group.
	 * Sets the state to TOKENISER_SKIP_GROUP and skipGroupLevel to the current group level.
	 * @since 2.1.3
	 */
	public void setTokeniserStateNormal() {
		this.setTokeniserState(TOKENISER_NORMAL);
	}

	/**
	 * Set the tokeniser state to skip to the end of the group.
	 * Sets the state to TOKENISER_SKIP_GROUP and skipGroupLevel to the current group level.
	 * @since 2.1.3
	 */
	public void setTokeniserStateSkipGroup() {
		this.setTokeniserState(TOKENISER_SKIP_GROUP);
		this.skipGroupLevel = this.groupLevel;
	}
	
	/**
	 * Sets the number of bytes to skip and the state of the tokeniser.
	 * 
	 * @param numberOfBytesToSkip
	 * 			The numbere of bytes to skip in the file.
	 * @since 2.1.3
	 */
	public void setTokeniserSkipBytes(long numberOfBytesToSkip) {
		this.setTokeniserState(TOKENISER_SKIP_BYTES);
		this.binSkipByteCount = numberOfBytesToSkip;
	}
	
	/**
	 * Sets the number of binary bytes.
	 * 
	 * @param binaryCount
	 * 			The number of binary bytes.
	 * @since 2.1.3
	 */
	public void setTokeniserStateBinary(int binaryCount) {
		this.setTokeniserState(TOKENISER_BINARY);
		this.binByteCount = binaryCount;
	}
	/**
	 * Sets the number of binary bytes.
	 * 
	 * @param binaryCount
	 * 			The number of binary bytes.
	 * @since 2.1.3
	 */
	public void setTokeniserStateBinary(long binaryCount) {
		this.setTokeniserState(TOKENISER_BINARY);
		this.binByteCount = binaryCount;
	}
	/**
	 * Helper method to determin if conversion is TYPE_CONVERT
	 * @return true if TYPE_CONVERT, otherwise false
	 * @see com.lowagie.text.rtf.parser.RtfParser#TYPE_CONVERT
	 * @since 2.1.3
	 */
	public boolean isConvert() {
		return (this.getConversionType() == RtfParser.TYPE_CONVERT);
	}
	
	/**
	 * Helper method to determin if conversion is TYPE_IMPORT_FULL or TYPE_IMPORT_FRAGMENT
	 * @return true if TYPE_CONVERT, otherwise false
	 * @see com.lowagie.text.rtf.parser.RtfParser#TYPE_IMPORT_FULL
	 * @see com.lowagie.text.rtf.parser.RtfParser#TYPE_IMPORT_FRAGMENT
	 * @since 2.1.3
	 */
	public boolean isImport() {
		return (isImportFull() || this.isImportFragment());
	}
	/**
	 * Helper method to determin if conversion is TYPE_IMPORT_FULL
	 * @return true if TYPE_CONVERT, otherwise false
	 * @see com.lowagie.text.rtf.parser.RtfParser#TYPE_IMPORT_FULL
	 * @since 2.1.3
	 */
	public boolean isImportFull() {
		return (this.getConversionType() == RtfParser.TYPE_IMPORT_FULL);
	}
	/**
	 * Helper method to determin if conversion is TYPE_IMPORT_FRAGMENT
	 * @return true if TYPE_CONVERT, otherwise false
	 * @see com.lowagie.text.rtf.parser.RtfParser#TYPE_IMPORT_FRAGMENT
	 * @since 2.1.3
	 */
	public boolean isImportFragment() {
		return (this.getConversionType() == RtfParser.TYPE_IMPORT_FRAGMENT);
	}
	/**
	 * Helper method to indicate if this control word was a \* control word.
	 * @return true if it was a \* control word, otherwise false
	 * @since 2.1.3
	 */
	public boolean getExtendedDestination() {
		return this.currentState.isExtendedDestination;
	}
	/**
	 * Helper method to set the extended control word flag.
	 * @param value Boolean to set the value to.
	 * @return isExtendedDestination.
	 * @since 2.1.3
	 */
	public boolean setExtendedDestination(boolean value) {
		this.currentState.isExtendedDestination = value;
		return this.currentState.isExtendedDestination;
	}

	/**
	 * Get the logfile name.
	 * 
	 * @return the logFile
	 * @since 2.1.3
	 */
	public String getLogFile() {
		return logFile;
	}

	/**
	 * Set the logFile name
	 * 
	 * @param logFile the logFile to set
	 * @since 2.1.3
	 */
	public void setLogFile(String logFile) {
		this.logFile = logFile;
	}
	/**
	 * Set the logFile name
	 * 
	 * @param logFile the logFile to set
	 * @since 2.1.3
	 */
	public void setLogFile(String logFile, boolean logAppend) {
		this.logFile = logFile;
		this.setLogAppend(logAppend);
	}

	/**
	 * Get flag indicating if logging is on or off.
	 * 
	 * @return the logging
	 * @since 2.1.3
	 */
	public boolean isLogging() {
		return logging;
	}

	/**
	 * Set flag indicating if logging is on or off
	 * @param logging <code>true</code> to turn on logging, <code>false</code> to turn off logging.
	 * @since 2.1.3
	 */
	public void setLogging(boolean logging) {
		this.logging = logging;
	}

	/**
	 * @return the logAppend
	 * @since 2.1.3
	 */
	public boolean isLogAppend() {
		return logAppend;
	}

	/**
	 * @param logAppend the logAppend to set
	 * @since 2.1.3
	 */
	public void setLogAppend(boolean logAppend) {
		this.logAppend = logAppend;
	}

/*	
 *	Statistics
 *
 	public void printStats(PrintStream out) {
		if(out == null) return;
		
		out.println("");
		out.println("Parser statistics:");
		out.println("Process start date: " + startDate.toLocaleString());
		out.println("Process end date  : " + endDate.toLocaleString());
		out.println("  Elapsed time    : " + Long.toString(endTime - startTime) + " milliseconds.");
		out.println("Total bytes read  : " + Long.toString(byteCount));
		out.println("Open group count  : " + Long.toString(openGroupCount));
		out.print("Close group count : " + Long.toString(closeGroupCount));
		out.println(" (Groups Skipped): " + Long.toString(groupSkippedCount));
		out.print("Control word count: " + Long.toString(ctrlWordCount));
		out.print(" - Handled: " + Long.toString(ctrlWordHandledCount));
		out.print(" Not Handled: " + Long.toString(ctrlWordNotHandledCount));
		out.println(" Skipped: " + Long.toString(ctrlWordSkippedCount));
		out.println("Plain text char count: " + Long.toString(characterCount));		
	}*/
}
