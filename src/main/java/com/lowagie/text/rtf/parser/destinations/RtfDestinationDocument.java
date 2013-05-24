/*
 * $Id: RtfDestinationDocument.java 3456 2008-05-26 15:26:57Z howard_s $
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
package com.lowagie.text.rtf.parser.destinations;

import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.rtf.direct.RtfDirectContent;
import com.lowagie.text.rtf.document.*;
import com.lowagie.text.rtf.parser.RtfParser;
import com.lowagie.text.rtf.parser.ctrlwords.RtfCtrlWordData;
import com.lowagie.text.rtf.parser.ctrlwords.RtfCtrlWordType;
import com.lowagie.text.rtf.parser.properties.RtfProperty;
import com.lowagie.text.rtf.parser.properties.RtfPropertyListener;
/**
 * <code>RtfDestinationDocument</code> handles data destined for the document destination
 * 
 * @author Howard Shank (hgshank@yahoo.com)
 * @since 2.1.0
 */
public final class RtfDestinationDocument extends RtfDestination implements RtfPropertyListener {


	/**
	 * The RtfDocument object.
	 * 
	 * @see com.lowagie.text.rtf.document.RtfDocument
	 */
	private RtfDocument rtfDoc = null;
	
	/**
	 * The iText Document object.
	 * 
	 * @see com.lowagie.text.Document
	 */
	private Document doc = null;
	
	private StringBuffer buffer = null;
	/**
	 * Indicates the parser action. Import or Conversion.
	 * 
	 * @see com.lowagie.text.rtf.parser.RtfParser#TYPE_UNIDENTIFIED
	 * @see com.lowagie.text.rtf.parser.RtfParser#TYPE_CONVERT
	 * @see com.lowagie.text.rtf.parser.RtfParser#TYPE_IMPORT_FRAGMENT
	 * @see com.lowagie.text.rtf.parser.RtfParser#TYPE_IMPORT_FULL
	 */
	private int conversionType = 0;
	
	
	/**
	 * Indicates the current table level being processed
	 */
	private int tableLevel = 0;
	
	private static final List IMPORT_IGNORED_CTRLWORDS = Arrays.asList(new String[]{
		"rtf",
		"ansicpg",
		"deff",
		"ansi",
		"mac",
		"pca",
		"pc",
		"stshfdbch",
		"stshfloch",
		"stshfhich",
		"stshfbi",
		"deflang",
		"deflangfe",
		"adeflang",
		"adeflangfe"});

	private static final List CONVERT_IGNORED_CTRLWORDS = Arrays.asList(new String[]{"rtf"});

	private Paragraph iTextParagraph = null;
	
	public RtfDestinationDocument() {
		super(null);
	}
	/**
	 * Constructs a new <code>RtfDestinationDocument</code> using
	 * the parameters to initialize the object.
	 * @param parser an RtfParser.
	 */
	public RtfDestinationDocument(RtfParser parser) {
		super(parser);
		this.rtfDoc = parser.getRtfDocument();
		this.doc = parser.getDocument();
		this.conversionType = parser.getConversionType();
		setToDefaults();
		if(this.rtfParser.isConvert()) {
			this.rtfParser.getState().properties.addRtfPropertyListener(this);
		}
	}
	
	
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable {
		// TODO Auto-generated method stub
		if(this.rtfParser.isConvert()) {
			this.rtfParser.getState().properties.removeRtfPropertyListener(this);
		}
		super.finalize();
	}

	public void setParser(RtfParser parser) {
		this.rtfParser = parser;
		this.rtfDoc = parser.getRtfDocument();
		this.doc = parser.getDocument();
		this.conversionType = parser.getConversionType();
		setToDefaults();
		if(this.rtfParser.isConvert()) {
			this.rtfParser.getState().properties.addRtfPropertyListener(this);
		}
	}
	

	/* (non-Javadoc)
	 * @see com.lowagie.text.rtf.direct.RtfDestination#closeDestination()
	 */
	public boolean closeDestination() {
		if(this.rtfParser.isImport()) {
			if(this.buffer.length()>0) {
				writeBuffer();
			}
		}
		
		this.rtfParser.getState().properties.removeRtfPropertyListener(this);

		return true;
	}

	/* (non-Javadoc)
	 * @see com.lowagie.text.rtf.direct.RtfDestination#handleGroupStart()
	 */
	public boolean handleOpenGroup() {
		this.onOpenGroup();	// event handler
		
		if(this.rtfParser.isImport()) {
		}
		if(this.rtfParser.isConvert()) {
			if(this.iTextParagraph == null) this.iTextParagraph = new Paragraph();
		}
		return true;
	}
	/* (non-Javadoc)
	 * @see com.lowagie.text.rtf.parser.destinations.RtfDestination#handleOpenNewGroup()
	 */
	public boolean handleOpeningSubGroup() {
		if(this.rtfParser.isImport()) {
			if(this.buffer.length()>0) {
				writeBuffer();
			}
		}
		return true;
	}
	/* (non-Javadoc)
	 * @see com.lowagie.text.rtf.direct.RtfDestination#handleGroupEnd()
	 */
	public boolean handleCloseGroup() {
		this.onCloseGroup();	// event handler
		
		if(this.rtfParser.isImport()) {
			if(this.buffer.length()>0) {
				writeBuffer();
			}
			writeText("}");
		}
		if(this.rtfParser.isConvert()) {
			if(this.buffer.length() > 0 && this.iTextParagraph == null) {
				this.iTextParagraph = new Paragraph();
			}
			if(this.buffer.length() > 0 ) {
				Chunk chunk = new Chunk();
				chunk.append(this.buffer.toString());
				this.iTextParagraph.add(chunk);
			}
			if(this.iTextParagraph != null) {
				addParagraphToDocument();
			}
		}
		return true;
	}
	/* (non-Javadoc)
	 * @see com.lowagie.text.rtf.direct.RtfDestination#handleCharacter(int)
	 */
	public boolean handleCharacter(int ch) {
		boolean result = true;
		this.onCharacter(ch);	// event handler
		
		if(this.rtfParser.isImport()) {
			if(buffer.length() > 254) {
				this.writeBuffer();
			}
			buffer.append((char)ch);
		}
		if(this.rtfParser.isConvert()) {
			buffer.append((char)ch);
		}
		return result;
	}

	
	public boolean handleControlWord(RtfCtrlWordData ctrlWordData) {
		boolean result = false;
		this.onCtrlWord(ctrlWordData);	// event handler
		
		if(this.rtfParser.isImport()) {
			// map font information
			if(ctrlWordData.ctrlWord.equals("f")) { ctrlWordData.param =  this.rtfParser.getImportManager().mapFontNr(ctrlWordData.param);}
			
			// map color information
			//colors
			if(ctrlWordData.ctrlWord.equals("cb")) { ctrlWordData.param = this.rtfParser.getImportManager().mapColorNr(ctrlWordData.param);}
			if(ctrlWordData.ctrlWord.equals("cf")) { ctrlWordData.param = this.rtfParser.getImportManager().mapColorNr(ctrlWordData.param);}
			//cells
			if(ctrlWordData.ctrlWord.equals("clcbpat")) { ctrlWordData.param = this.rtfParser.getImportManager().mapColorNr(ctrlWordData.param);}
			if(ctrlWordData.ctrlWord.equals("clcbpatraw")) { ctrlWordData.param = this.rtfParser.getImportManager().mapColorNr(ctrlWordData.param);}
			if(ctrlWordData.ctrlWord.equals("clcfpat")) { ctrlWordData.param = this.rtfParser.getImportManager().mapColorNr(ctrlWordData.param);}
			if(ctrlWordData.ctrlWord.equals("clcfpatraw")) { ctrlWordData.param = this.rtfParser.getImportManager().mapColorNr(ctrlWordData.param);}
			//table rows
			if(ctrlWordData.ctrlWord.equals("trcfpat")) { ctrlWordData.param = this.rtfParser.getImportManager().mapColorNr(ctrlWordData.param);}
			if(ctrlWordData.ctrlWord.equals("trcbpat")) { ctrlWordData.param = this.rtfParser.getImportManager().mapColorNr(ctrlWordData.param);}
			//paragraph border
			if(ctrlWordData.ctrlWord.equals("brdrcf")) { ctrlWordData.param = this.rtfParser.getImportManager().mapColorNr(ctrlWordData.param);}
			// map lists
			if(ctrlWordData.ctrlWord.equals("ls")) { ctrlWordData.param = this.rtfParser.getImportManager().mapListNr(ctrlWordData.param);}
		}
		

		
		if(this.rtfParser.isConvert()) {
			if(ctrlWordData.ctrlWord.equals("par")) { addParagraphToDocument(); }
			// Set Font
			if(ctrlWordData.ctrlWord.equals("f")) {}
			
			// color information
			//colors
			if(ctrlWordData.ctrlWord.equals("cb")) {}
			if(ctrlWordData.ctrlWord.equals("cf")) {}
			//cells
			if(ctrlWordData.ctrlWord.equals("clcbpat")) {}
			if(ctrlWordData.ctrlWord.equals("clcbpatraw")) {}
			if(ctrlWordData.ctrlWord.equals("clcfpat")) {}
			if(ctrlWordData.ctrlWord.equals("clcfpatraw")) {}
			//table rows
			if(ctrlWordData.ctrlWord.equals("trcfpat")) {}
			if(ctrlWordData.ctrlWord.equals("trcbpat")) {}
			//paragraph border
			if(ctrlWordData.ctrlWord.equals("brdrcf")) {}
			
			/* TABLES */
			if(ctrlWordData.ctrlWord.equals("trowd")) /*Beginning of row*/ { tableLevel++;}
			if(ctrlWordData.ctrlWord.equals("cell")) /*End of Cell Denotes the end of a table cell*/ {
//				String ctl = ctrlWordData.ctrlWord;
//				System.out.print("cell found");
			}
			if(ctrlWordData.ctrlWord.equals("row")) /*End of row*/ { tableLevel++;}
			if(ctrlWordData.ctrlWord.equals("lastrow")) /*Last row of the table*/ {}
			if(ctrlWordData.ctrlWord.equals("row")) /*End of row*/ { tableLevel++;}
			if(ctrlWordData.ctrlWord.equals("irow")) /*param  is the row index of this row.*/ {}
			if(ctrlWordData.ctrlWord.equals("irowband")) /*param is the row index of the row, adjusted to account for header rows. A header row has a value of -1.*/ {}
			if(ctrlWordData.ctrlWord.equals("tcelld")) /*Sets table cell defaults*/ {}
			if(ctrlWordData.ctrlWord.equals("nestcell")) /*Denotes the end of a nested cell.*/ {}
			if(ctrlWordData.ctrlWord.equals("nestrow")) /*Denotes the end of a nested row*/ {}
			if(ctrlWordData.ctrlWord.equals("nesttableprops")) /*Defines the properties of a nested table. This is a destination control word*/ {}
			if(ctrlWordData.ctrlWord.equals("nonesttables")) /*Contains text for readers that do not understand nested tables. This destination should be ignored by readers that support nested tables.*/ {}
			if(ctrlWordData.ctrlWord.equals("trgaph")) /*Half the space between the cells of a table row in twips.*/ {}
			if(ctrlWordData.ctrlWord.equals("cellx")) /*param Defines the right boundary of a table cell, including its half of the space between cells.*/ {}
			if(ctrlWordData.ctrlWord.equals("clmgf")) /*The first cell in a range of table cells to be merged.*/ {}
			if(ctrlWordData.ctrlWord.equals("clmrg")) /*Contents of the table cell are merged with those of the preceding cell*/ {}
			if(ctrlWordData.ctrlWord.equals("clvmgf")) /*The first cell in a range of table cells to be vertically merged.*/ {}
			if(ctrlWordData.ctrlWord.equals("clvmrg")) /*Contents of the table cell are vertically merged with those of the preceding cell*/ {}
			/* TABLE: table row revision tracking */
			if(ctrlWordData.ctrlWord.equals("trauth")) /*With revision tracking enabled, this control word identifies the author of changes to a table row's properties. N refers to a value in the revision table*/ {}
			if(ctrlWordData.ctrlWord.equals("trdate")) /*With revision tracking enabled, this control word identifies the date of a revision*/ {}
			/* TABLE: Autoformatting flags */
			if(ctrlWordData.ctrlWord.equals("tbllkborder")) /*Flag sets table autoformat to format borders*/ {}
			if(ctrlWordData.ctrlWord.equals("tbllkshading")) /*Flag sets table autoformat to affect shading.*/ {}
			if(ctrlWordData.ctrlWord.equals("tbllkfont")) /*Flag sets table autoformat to affect font*/ {}
			if(ctrlWordData.ctrlWord.equals("tbllkcolor")) /*Flag sets table autoformat to affect color*/ {}
			if(ctrlWordData.ctrlWord.equals("tbllkbestfit")) /*Flag sets table autoformat to apply best fit*/ {}
			if(ctrlWordData.ctrlWord.equals("tbllkhdrrows")) /*Flag sets table autoformat to format the first (header) row*/ {}
			if(ctrlWordData.ctrlWord.equals("tbllklastrow")) /*Flag sets table autoformat to format the last row.*/ {}
			if(ctrlWordData.ctrlWord.equals("tbllkhdrcols")) /*Flag sets table autoformat to format the first (header) column*/ {}
			if(ctrlWordData.ctrlWord.equals("tbllklastcol")) /*Flag sets table autoformat to format the last column*/ {}
			if(ctrlWordData.ctrlWord.equals("tbllknorowband")) /*Specifies row banding conditional formatting shall not be applied*/ {}
			if(ctrlWordData.ctrlWord.equals("tbllknocolband")) /*Specifies column banding conditional formatting shall not be applied.*/ {}
			/* TABLE: Row Formatting */
			if(ctrlWordData.ctrlWord.equals("taprtl")) /*Table direction is right to left*/ {}
			if(ctrlWordData.ctrlWord.equals("trautofit")) /*param = AutoFit:
0	No AutoFit (default).
1	AutoFit is on for the row. Overridden by \clwWidthN and \trwWidthN in any table row.
*/ {}
			if(ctrlWordData.ctrlWord.equals("trhdr")) /*Table row header. This row should appear at the top of every page on which the current table appears*/ {}
			if(ctrlWordData.ctrlWord.equals("trkeep")) /*Keep table row together. This row cannot be split by a page break. This property is assumed to be off unless the control word is present*/ {}
			if(ctrlWordData.ctrlWord.equals("trkeepfollow")) /*Keep row in the same page as the following row.*/ {}
			if(ctrlWordData.ctrlWord.equals("trleft")) /*Position in twips of the leftmost edge of the table with respect to the left edge of its column.*/ {}
			if(ctrlWordData.ctrlWord.equals("trqc")) /*Centers a table row with respect to its containing column.*/ {}
			if(ctrlWordData.ctrlWord.equals("trql")) /*Left-justifies a table row with respect to its containing column.*/ {}
			if(ctrlWordData.ctrlWord.equals("trqr")) /*Right-justifies a table row with respect to its containing column*/ {}
			if(ctrlWordData.ctrlWord.equals("trrh")) /*Height of a table row in twips. When 0, the height is sufficient for all the text in the line; when positive, the height is guaranteed to be at least the specified height; when negative, the absolute value of the height is used, regardless of the height of the text in the line*/ {}
			if(ctrlWordData.ctrlWord.equals("trpaddb")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("trpaddl")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("trpaddr")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("trpaddt")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("trpaddfb")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("trpaddfl")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("trpaddfr")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("trpaddft")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("trspdl")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("trspdt")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("trspdb")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("trspdr")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("trspdfl")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("trspdft")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("trspdfb")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("trspdfr")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("trwWidth")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("trftsWidth")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("trwWidthB")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("trftsWidthB")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("trftsWidthB")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("trwWidthA")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("trftsWidthA")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("tblind")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("tblindtype")) /**/ {}
			/*TABLE: Row shading and Background COlors*/
			if(ctrlWordData.ctrlWord.equals("trcbpat")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("trcfpat")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("trpat")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("trshdng")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("trbgbdiag")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("trbgcross")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("trbgdcross")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("trbgdkbdiag")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("trbgdkcross")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("trbgdkdcross")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("trbgdkfdiag")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("trbgdkhor")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("trbgdkvert")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("trbgfdiag")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("trbghoriz")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("trbgvert")) /**/ {}
			/* TABLE: Cell Formatting*/
			if(ctrlWordData.ctrlWord.equals("clFitText")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("clNoWrap")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("clpadl")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("clpadt")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("clpadb")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("clpadr")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("clpadfl")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("clpadft")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("clpadfb")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("clpadfr")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("clwWidth")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("clftsWidth")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("clhidemark")) /**/ {}
			/* TABLE: Compared Table Cells */
			if(ctrlWordData.ctrlWord.equals("clins")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("cldel")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("clmrgd")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("clmrgdr")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("clsplit")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("clsplitr")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("clinsauth")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("clinsdttm")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("cldelauth")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("cldeldttm")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("clmrgdauth")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("clmrgddttm")) /**/ {}
			/*TABLE: Position Wrapped Tables (The following properties must be the same for all rows in the table.)*/
			if(ctrlWordData.ctrlWord.equals("tdfrmtxtLeft")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("tdfrmtxtRight")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("tdfrmtxtTop")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("tdfrmtxtBottom")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("tabsnoovrlp")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("tphcol")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("tphmrg")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("tphpg")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("tposnegx")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("tposnegy")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("tposx")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("tposxc")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("tposxi")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("tposxl")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("tposxo")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("tposxr")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("tposy")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("tposyb")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("tposyc")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("tposyil")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("tposyin")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("tposyout")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("tposyt")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("tpvmrg")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("tpvpara")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("tpvpg")) /**/ {}
			/* TABLE: Bidirectional Controls */
			if(ctrlWordData.ctrlWord.equals("rtlrow")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("ltrrow")) /**/ {}
			/* TABLE: Row Borders */
			if(ctrlWordData.ctrlWord.equals("trbrdrt")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("trbrdrl")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("trbrdrb")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("trbrdrr")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("trbrdrh")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("trbrdrv")) /**/ {}
			/* TABLE: Cell Borders */
			if(ctrlWordData.ctrlWord.equals("brdrnil")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("clbrdrb")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("clbrdrt")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("clbrdrl")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("clbrdrr")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("cldglu")) /**/ {}
			if(ctrlWordData.ctrlWord.equals("cldgll")) /**/ {}
		}
		if(ctrlWordData.ctrlWordType == RtfCtrlWordType.TOGGLE) {
			this.rtfParser.getState().properties.toggleProperty(ctrlWordData);//ctrlWordData.specialHandler);
		}
		
		if(ctrlWordData.ctrlWordType == RtfCtrlWordType.FLAG || 
				ctrlWordData.ctrlWordType == RtfCtrlWordType.VALUE) {
			this.rtfParser.getState().properties.setProperty(ctrlWordData);//ctrlWordData.specialHandler, ctrlWordData.param);
		}
		
		switch(conversionType) {
		case RtfParser.TYPE_IMPORT_FULL:
			if(!IMPORT_IGNORED_CTRLWORDS.contains(ctrlWordData.ctrlWord)) {
				writeBuffer();
				writeText(ctrlWordData.toString());
			}
			result = true;
			break;		
		case RtfParser.TYPE_IMPORT_FRAGMENT:
			if(!IMPORT_IGNORED_CTRLWORDS.contains(ctrlWordData.ctrlWord)) {
				writeBuffer();
				writeText(ctrlWordData.toString());
			}
			result = true;
			break;
		case RtfParser.TYPE_CONVERT:
			if(!IMPORT_IGNORED_CTRLWORDS.contains(ctrlWordData.ctrlWord)) {
			}
			result = true;
			break;
		default:	// error because is should be an import or convert
			result = false;
			break;
		}
		
		
		
		
		return result;
	}
	/**
	 * Write the accumulated buffer to the destination.
	 * Used for direct content
	 */
	private void writeBuffer() {
		writeText(this.buffer.toString());
		setToDefaults();
	}
	/**
	 * Write the string value to the destination.
	 * Used for direct content
	 * @param value
	 */
	private void writeText(String value) {
		if(this.rtfParser.isNewGroup()) {
			this.rtfDoc.add(new RtfDirectContent("{"));
			this.rtfParser.setNewGroup(false);
		}
		if(value.length() > 0) {
			this.rtfDoc.add(new RtfDirectContent(value));
		}
	}
	/* (non-Javadoc)
	 * @see com.lowagie.text.rtf.direct.RtfDestination#setDefaults()
	 */
	public void setToDefaults() {
		this.buffer = new StringBuffer(255);
	}
	/* (non-Javadoc)
	 * @see com.lowagie.text.rtf.parser.properties.RtfPropertyListener#afterChange(java.lang.String)
	 */
	public void afterPropertyChange(String propertyName) {
		if(propertyName.startsWith(RtfProperty.CHARACTER)) {
		} else {
			if(propertyName.startsWith(RtfProperty.PARAGRAPH)) {
			} else {
				if(propertyName.startsWith(RtfProperty.SECTION)) {
				} else {
					if(propertyName.startsWith(RtfProperty.DOCUMENT)) {

					}
				}
			}
		}		
	}
	
	/* (non-Javadoc)
	 * @see com.lowagie.text.rtf.parser.properties.RtfPropertyListener#beforeChange(java.lang.String)
	 */
	public void beforePropertyChange(String propertyName) {
		// do we have any text to do anything with?
		// if not, then just return without action.
		if(this.buffer.length() == 0) return;
		
		if(propertyName.startsWith(RtfProperty.CHARACTER)) {
			// this is a character change,
			// add a new chunk to the current paragraph using current character settings.
			Chunk chunk = new Chunk();
			chunk.append(this.buffer.toString());
			this.buffer = new StringBuffer(255);
			HashMap charProperties = this.rtfParser.getState().properties.getProperties(RtfProperty.CHARACTER);
			String defFont = (String)charProperties.get(RtfProperty.CHARACTER_FONT);
			if(defFont == null) defFont = "0";
			RtfDestinationFontTable fontTable = (RtfDestinationFontTable)this.rtfParser.getDestination("fonttbl");
			Font currFont = fontTable.getFont(defFont);
			int fs = Font.NORMAL;
			if(charProperties.containsKey(RtfProperty.CHARACTER_BOLD)) fs |= Font.BOLD; 
			if(charProperties.containsKey(RtfProperty.CHARACTER_ITALIC)) fs |= Font.ITALIC;
			if(charProperties.containsKey(RtfProperty.CHARACTER_UNDERLINE)) fs |= Font.UNDERLINE;
			Font useFont = FontFactory.getFont(currFont.getFamilyname(), 12, fs, new Color(0,0,0));
			
			
			chunk.setFont(useFont);
			if(iTextParagraph == null) this.iTextParagraph = new Paragraph();
			this.iTextParagraph.add(chunk);

		} else {
			if(propertyName.startsWith(RtfProperty.PARAGRAPH)) {
				// this is a paragraph change. what do we do?
			} else {
				if(propertyName.startsWith(RtfProperty.SECTION)) {
					
				} else {
					if(propertyName.startsWith(RtfProperty.DOCUMENT)) {

					}
				}
			}
		}		
	}
	
	private void addParagraphToDocument() {
		if(this.iTextParagraph != null) {
			try {
				this.rtfParser.getDocument().add(this.iTextParagraph);
			} catch (DocumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			this.iTextParagraph = null;
		}	
	}
}
