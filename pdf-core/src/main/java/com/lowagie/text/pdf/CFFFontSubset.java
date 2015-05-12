/*
 * $Id: CFFFontSubset.java 3573 2008-07-21 15:08:04Z blowagie $
 *
 * Copyright 2004 Oren Manor and Ygal Blum
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
 * the Initial Developer are Copyright (C) 1999-2005 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000-2005 by Paulo Soares. All Rights Reserved.
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * This Class subsets a CFF Type Font. The subset is preformed for CID fonts and NON CID fonts.
 * The Charstring is subsetted for both types. For CID fonts only the FDArray which are used are embedded. 
 * The Lsubroutines of the FDArrays used are subsetted as well. The Subroutine subset supports both Type1 and Type2
 * formatting although only tested on Type2 Format.
 * For Non CID the Lsubroutines are subsetted. On both types the Gsubroutines is subsetted. 
 * A font which was not of CID type is transformed into CID as a part of the subset process. 
 * The CID synthetic creation was written by Sivan Toledo (sivan@math.tau.ac.il) 
 * @author Oren Manor (manorore@post.tau.ac.il) and Ygal Blum (blumygal@post.tau.ac.il)
 */
public class CFFFontSubset extends CFFFont {
	
	/**
	 *  The Strings in this array represent Type1/Type2 operator names
	 */
	static final String SubrsFunctions[] = {
			"RESERVED_0","hstem","RESERVED_2","vstem","vmoveto","rlineto","hlineto","vlineto",
			"rrcurveto","RESERVED_9","callsubr","return","escape","RESERVED_13",
			"endchar","RESERVED_15","RESERVED_16","RESERVED_17","hstemhm","hintmask",
			"cntrmask","rmoveto","hmoveto","vstemhm","rcurveline","rlinecurve","vvcurveto",
			"hhcurveto","shortint","callgsubr","vhcurveto","hvcurveto"
			};
	/**
	 * The Strings in this array represent Type1/Type2 escape operator names
	 */
	static final String SubrsEscapeFuncs[] = {
			"RESERVED_0","RESERVED_1","RESERVED_2","and","or","not","RESERVED_6",
			"RESERVED_7","RESERVED_8","abs","add","sub","div","RESERVED_13","neg",
			"eq","RESERVED_16","RESERVED_17","drop","RESERVED_19","put","get","ifelse",
			"random","mul","RESERVED_25","sqrt","dup","exch","index","roll","RESERVED_31",
			"RESERVED_32","RESERVED_33","hflex","flex","hflex1","flex1","RESERVED_REST"
	};
	
	/**
	*  Operator codes for unused  CharStrings and unused local and global Subrs
	*/
	static final byte ENDCHAR_OP = 14;
	static final byte RETURN_OP = 11;
	
	/**
	 * A HashMap containing the glyphs used in the text after being converted
	 * to glyph number by the CMap 
	 */
	HashMap GlyphsUsed;
	/**
	 * The GlyphsUsed keys as an ArrayList
	 */
	ArrayList glyphsInList;
	/**
	 * A HashMap for keeping the FDArrays being used by the font
	 */
	HashMap FDArrayUsed = new HashMap();
	/**
	 * A HashMaps array for keeping the subroutines used in each FontDict
	 */
	HashMap[] hSubrsUsed;
	/**
	 * The SubroutinesUsed HashMaps as ArrayLists
	 */
	ArrayList[] lSubrsUsed;
	/**
	 * A HashMap for keeping the Global subroutines used in the font
	 */
	HashMap hGSubrsUsed  = new HashMap();
	/**
	 * The Global SubroutinesUsed HashMaps as ArrayLists
	 */
	ArrayList lGSubrsUsed = new ArrayList();
	/**
	 * A HashMap for keeping the subroutines used in a non-cid font
	 */
	HashMap hSubrsUsedNonCID  = new HashMap();
	/**
	 * The SubroutinesUsed HashMap as ArrayList
	 */
	ArrayList lSubrsUsedNonCID = new ArrayList();
	/**
	 * An array of the new Indexes for the local Subr. One index for each FontDict
	 */
	byte[][] NewLSubrsIndex;
	/**
	 * The new subroutines index for a non-cid font
	 */
	byte[] NewSubrsIndexNonCID;
	/**
	 * The new global subroutines index of the font
	 */
	byte[] NewGSubrsIndex;
	/**
	 * The new CharString of the font
	 */
	byte[] NewCharStringsIndex;
	
	/**
	 * The bias for the global subroutines
	 */
	int GBias = 0;
	
	/**
	 * The linked list for generating the new font stream
	 */
	LinkedList OutputList;
	
	/**
	 * Number of arguments to the stem operators in a subroutine calculated recursively
	 */
	int NumOfHints=0;

	
	/**	 
	 * C'tor for CFFFontSubset
	 * @param rf - The font file
	 * @param GlyphsUsed - a HashMap that contains the glyph used in the subset 
	 */
    public CFFFontSubset(RandomAccessFileOrArray rf,HashMap GlyphsUsed){
		// Use CFFFont c'tor in order to parse the font file.
    	super(rf);
		this.GlyphsUsed = GlyphsUsed;
		//Put the glyphs into a list
		glyphsInList = new ArrayList(GlyphsUsed.keySet());
		
		
		for (int i=0;i<fonts.length;++i)
		{
			// Read the number of glyphs in the font
			seek(fonts[i].charstringsOffset);
	        fonts[i].nglyphs = getCard16();
	        
	    	// Jump to the count field of the String Index
	        seek(stringIndexOffset);
	        fonts[i].nstrings = getCard16()+standardStrings.length;
	        
	        // For each font save the offset array of the charstring
			fonts[i].charstringsOffsets = getIndex(fonts[i].charstringsOffset);
			
			// Process the FDSelect if exist 
			if (fonts[i].fdselectOffset>=0)
			{
				// Process the FDSelect
	            readFDSelect(i);
	            // Build the FDArrayUsed hashmap
            	BuildFDArrayUsed(i);
			}
			if (fonts[i].isCID)
				// Build the FD Array used Hash Map
				ReadFDArray(i);
			// compute the charset length 
			fonts[i].CharsetLength = CountCharset(fonts[i].charsetOffset,fonts[i].nglyphs);
		}
	}

    /**
     * Calculates the length of the charset according to its format
     * @param Offset The Charset Offset
     * @param NumofGlyphs Number of glyphs in the font
     * @return the length of the Charset
     */
    int CountCharset(int Offset,int NumofGlyphs){
    	int format;
    	int Length=0;
    	seek(Offset);
    	// Read the format
    	format = getCard8();
    	// Calc according to format
    	switch (format){
    		case 0:
    			Length = 1+2*NumofGlyphs;
    			break;
    		case 1:
    			Length = 1+3*CountRange(NumofGlyphs,1);
    			break;
    		case 2:
    			Length = 1+4*CountRange(NumofGlyphs,2);
    			break;
    		default:
    			break;
    	}
    	return Length;
    }
    
    /**
     * Function calculates the number of ranges in the Charset
     * @param NumofGlyphs The number of glyphs in the font
     * @param Type The format of the Charset
     * @return The number of ranges in the Charset data structure
     */
    int CountRange(int NumofGlyphs,int Type){
    	int num=0;
    	char Sid;
    	int i=1,nLeft;
    	while (i<NumofGlyphs){
    		num++;
    		Sid = getCard16();
    		if (Type==1)
    			nLeft = getCard8();
    		else
    			nLeft = getCard16();
    		i += nLeft+1;
    	}
    	return num;
    }


	/**
	 * Read the FDSelect of the font and compute the array and its length
	 * @param Font The index of the font being processed
	 */
	protected void readFDSelect(int Font)
	{
		// Restore the number of glyphs
		int NumOfGlyphs = fonts[Font].nglyphs;
		int[] FDSelect = new int[NumOfGlyphs];
		// Go to the beginning of the FDSelect
		seek(fonts[Font].fdselectOffset);
		// Read the FDSelect's format
		fonts[Font].FDSelectFormat = getCard8();
		
		switch(fonts[Font].FDSelectFormat){
			// Format==0 means each glyph has an entry that indicated
			// its FD.
			case 0:
				for (int i=0;i<NumOfGlyphs;i++)
				{
					FDSelect[i] = getCard8();
				}
				// The FDSelect's Length is one for each glyph + the format
				// for later use
				fonts[Font].FDSelectLength = fonts[Font].nglyphs+1;
				break;
			case 3:
				// Format==3 means the ranges version
				// The number of ranges
				int nRanges = getCard16();
				int l=0;
				// Read the first in the first range
				int first = getCard16();
				for (int i=0;i<nRanges;i++)
				{
					// Read the FD index
					int fd = getCard8();
					// Read the first of the next range
					int last = getCard16();
					// Calc the steps and write to the array
					int steps = last-first;
					for (int k=0;k<steps;k++)
					{
						FDSelect[l] = fd;
						l++;
					}
					// The last from this iteration is the first of the next
					first = last;
				}
				// Store the length for later use
				fonts[Font].FDSelectLength = 1+2+nRanges*3+2;
				break;
			default:
				break;
		}
		// Save the FDSelect of the font 
		fonts[Font].FDSelect = FDSelect; 
	}
	
	/**
	 * Function reads the FDSelect and builds the FDArrayUsed HashMap According to the glyphs used
	 * @param Font the Number of font being processed
	 */
	protected void BuildFDArrayUsed(int Font)
	{
		int[] FDSelect = fonts[Font].FDSelect;
		// For each glyph used
		for (int i=0;i<glyphsInList.size();i++)
		{
			// Pop the glyphs index
			int glyph = ((Integer)glyphsInList.get(i)).intValue();
			// Pop the glyph's FD
			int FD = FDSelect[glyph];
			// Put the FD index into the FDArrayUsed HashMap
			FDArrayUsed.put(new Integer(FD),null);
		}
	}

	/**
	 * Read the FDArray count, offsize and Offset array
	 * @param Font
	 */
	protected void ReadFDArray(int Font)
	{
		seek(fonts[Font].fdarrayOffset);
		fonts[Font].FDArrayCount = getCard16();
		fonts[Font].FDArrayOffsize = getCard8();
		// Since we will change values inside the FDArray objects
		// We increase its offsize to prevent errors 
		if (fonts[Font].FDArrayOffsize < 4)
			fonts[Font].FDArrayOffsize++;
		fonts[Font].FDArrayOffsets = getIndex(fonts[Font].fdarrayOffset);
	}

    
	/**
	 * The Process function extracts one font out of the CFF file and returns a
	 * subset version of the original.
	 * @param fontName - The name of the font to be taken out of the CFF
	 * @return The new font stream
	 * @throws IOException
	 */
	public byte[] Process(String fontName)throws IOException{
		try
		{	
			// Verify that the file is open
			buf.reOpen();
			// Find the Font that we will be dealing with
			int j;
	        for (j=0; j<fonts.length; j++)
	            if (fontName.equals(fonts[j].name)) break;
	        if (j==fonts.length) return null;
	        
			// Calc the bias for the global subrs
			if (gsubrIndexOffset >= 0)
				GBias = CalcBias(gsubrIndexOffset,j);

	        // Prepare the new CharStrings Index
			BuildNewCharString(j);
			 // Prepare the new Global and Local Subrs Indices
			BuildNewLGSubrs(j);
			// Build the new file 
			byte[] Ret = BuildNewFile(j);
			return Ret;
		}
		finally {
            try {
                buf.close();
            }
            catch (Exception e) {
                // empty on purpose
            }
		}
	}

	/**
	 * Function calcs bias according to the CharString type and the count
	 * of the subrs
	 * @param Offset The offset to the relevant subrs index
	 * @param Font the font
	 * @return The calculated Bias
	 */
	protected int CalcBias(int Offset,int Font)
	{
		seek(Offset);
		int nSubrs = getCard16();
		// If type==1 -> bias=0 
		if (fonts[Font].CharstringType == 1)
			return 0;
		// else calc according to the count
		else if (nSubrs < 1240)
			return 107;
		else if (nSubrs < 33900)
			return 1131;
		else
			return 32768;
	}

	/**
	 *Function uses BuildNewIndex to create the new index of the subset charstrings
	 * @param FontIndex the font
	 * @throws IOException
	 */
	protected void BuildNewCharString(int FontIndex) throws IOException 
	{
		NewCharStringsIndex = BuildNewIndex(fonts[FontIndex].charstringsOffsets,GlyphsUsed,ENDCHAR_OP);
	}
	
	/**
	 * Function builds the new local & global subsrs indices. IF CID then All of 
	 * the FD Array lsubrs will be subsetted. 
	 * @param Font the font
	 * @throws IOException
	 */
	protected void BuildNewLGSubrs(int Font)throws IOException
	{
		// If the font is CID then the lsubrs are divided into FontDicts.
		// for each FD array the lsubrs will be subsetted.
		if(fonts[Font].isCID)
		{
			// Init the hashmap-array and the arraylist-array to hold the subrs used
			// in each private dict.
			hSubrsUsed = new HashMap[fonts[Font].fdprivateOffsets.length];
			lSubrsUsed = new ArrayList[fonts[Font].fdprivateOffsets.length];
			// A [][] which will store the byte array for each new FD Array lsubs index
			NewLSubrsIndex = new byte[fonts[Font].fdprivateOffsets.length][];
			// An array to hold the offset for each Lsubr index 
			fonts[Font].PrivateSubrsOffset = new int[fonts[Font].fdprivateOffsets.length];
			// A [][] which will store the offset array for each lsubr index			
			fonts[Font].PrivateSubrsOffsetsArray = new int[fonts[Font].fdprivateOffsets.length][];
			
			// Put the FDarrayUsed into a list
			ArrayList FDInList = new ArrayList(FDArrayUsed.keySet());
			// For each FD array which is used subset the lsubr 
			for (int j=0;j<FDInList.size();j++)
			{
				// The FDArray index, Hash Map, Array List to work on
				int FD = ((Integer)FDInList.get(j)).intValue();
				hSubrsUsed[FD] = new HashMap();
				lSubrsUsed[FD] = new ArrayList();
				//Reads the private dicts looking for the subr operator and 
				// store both the offset for the index and its offset array
				BuildFDSubrsOffsets(Font,FD);
				// Verify that FDPrivate has a LSubrs index
				if(fonts[Font].PrivateSubrsOffset[FD]>=0)
				{
					//Scans the Charstring data storing the used Local and Global subroutines 
					// by the glyphs. Scans the Subrs recursively. 
					BuildSubrUsed(Font,FD,fonts[Font].PrivateSubrsOffset[FD],fonts[Font].PrivateSubrsOffsetsArray[FD],hSubrsUsed[FD],lSubrsUsed[FD]);
					// Builds the New Local Subrs index
					NewLSubrsIndex[FD] = BuildNewIndex(fonts[Font].PrivateSubrsOffsetsArray[FD],hSubrsUsed[FD],RETURN_OP);
				}
			}
		}
		// If the font is not CID && the Private Subr exists then subset:
		else if (fonts[Font].privateSubrs>=0)
		{
			// Build the subrs offsets;
			fonts[Font].SubrsOffsets = getIndex(fonts[Font].privateSubrs);
			//Scans the Charstring data storing the used Local and Global subroutines 
			// by the glyphs. Scans the Subrs recursively.
			BuildSubrUsed(Font,-1,fonts[Font].privateSubrs,fonts[Font].SubrsOffsets,hSubrsUsedNonCID,lSubrsUsedNonCID);
		}
		// For all fonts subset the Global Subroutines
		// Scan the Global Subr Hashmap recursively on the Gsubrs
		BuildGSubrsUsed(Font);
		if (fonts[Font].privateSubrs>=0)
			// Builds the New Local Subrs index
			NewSubrsIndexNonCID = BuildNewIndex(fonts[Font].SubrsOffsets,hSubrsUsedNonCID,RETURN_OP);
		//Builds the New Global Subrs index
		NewGSubrsIndex = BuildNewIndex(gsubrOffsets,hGSubrsUsed,RETURN_OP);
	}

	/**
	 * The function finds for the FD array processed the local subr offset and its 
	 * offset array.  
	 * @param Font the font
	 * @param FD The FDARRAY processed
	 */
	protected void BuildFDSubrsOffsets(int Font,int FD)
	{
		// Initiate to -1 to indicate lsubr operator present
		fonts[Font].PrivateSubrsOffset[FD] = -1;
		// Goto beginning of objects
        seek(fonts[Font].fdprivateOffsets[FD]);
        // While in the same object:
        while (getPosition() < fonts[Font].fdprivateOffsets[FD]+fonts[Font].fdprivateLengths[FD])
        {
        	getDictItem();
        	// If the dictItem is the "Subrs" then find and store offset, 
        	if (key=="Subrs")
        		fonts[Font].PrivateSubrsOffset[FD] = ((Integer)args[0]).intValue()+fonts[Font].fdprivateOffsets[FD];
        }
        //Read the lsubr index if the lsubr was found
        if (fonts[Font].PrivateSubrsOffset[FD] >= 0)
        	fonts[Font].PrivateSubrsOffsetsArray[FD] = getIndex(fonts[Font].PrivateSubrsOffset[FD]); 
	}

	/**
	 * Function uses ReadAsubr on the glyph used to build the LSubr & Gsubr HashMap.
	 * The HashMap (of the lsubr only) is then scanned recursively for Lsubr & Gsubrs
	 * calls.  
	 * @param Font the font
	 * @param FD FD array processed. 0 indicates function was called by non CID font
	 * @param SubrOffset the offset to the subr index to calc the bias
	 * @param SubrsOffsets the offset array of the subr index
	 * @param hSubr HashMap of the subrs used
	 * @param lSubr ArrayList of the subrs used
	 */
	protected void BuildSubrUsed(int Font,int FD,int SubrOffset,int[] SubrsOffsets,HashMap hSubr,ArrayList lSubr)
	{

		// Calc the Bias for the subr index
		int LBias = CalcBias(SubrOffset,Font);
		
		// For each glyph used find its GID, start & end pos
		for (int i=0;i<glyphsInList.size();i++)
		{
			int glyph = ((Integer)glyphsInList.get(i)).intValue();
			int Start = fonts[Font].charstringsOffsets[glyph];
			int End = fonts[Font].charstringsOffsets[glyph+1];
			
			// IF CID:
			if (FD >= 0)
			{
				EmptyStack();
				NumOfHints=0;
				// Using FDSELECT find the FD Array the glyph belongs to.
				int GlyphFD = fonts[Font].FDSelect[glyph];
				// If the Glyph is part of the FD being processed 
				if (GlyphFD == FD)
					// Find the Subrs called by the glyph and insert to hash:
					ReadASubr(Start,End,GBias,LBias,hSubr,lSubr,SubrsOffsets);
			}
			else
				// If the font is not CID 
				//Find the Subrs called by the glyph and insert to hash:
				ReadASubr(Start,End,GBias,LBias,hSubr,lSubr,SubrsOffsets);
		}
		// For all Lsubrs used, check recursively for Lsubr & Gsubr used
		for (int i=0;i<lSubr.size();i++)
		{
			// Pop the subr value from the hash
			int Subr = ((Integer)lSubr.get(i)).intValue();
			// Ensure the Lsubr call is valid
			if (Subr < SubrsOffsets.length-1 && Subr>=0)
			{
				// Read and process the subr
				int Start = SubrsOffsets[Subr];
				int End = SubrsOffsets[Subr+1];
				ReadASubr(Start,End,GBias,LBias,hSubr,lSubr,SubrsOffsets);
			}
		}
	}
	
	/**
	 * Function scans the Glsubr used ArrayList to find recursive calls 
	 * to Gsubrs and adds to Hashmap & ArrayList
	 * @param Font the font
	 */
	protected void BuildGSubrsUsed(int Font)
	{
		int LBias = 0;
		int SizeOfNonCIDSubrsUsed = 0;
		if (fonts[Font].privateSubrs>=0)
		{
			LBias = CalcBias(fonts[Font].privateSubrs,Font);
			SizeOfNonCIDSubrsUsed = lSubrsUsedNonCID.size();
		}
		
		// For each global subr used 
		for (int i=0;i<lGSubrsUsed.size();i++)
		{
			//Pop the value + check valid 
			int Subr = ((Integer)lGSubrsUsed.get(i)).intValue();
			if (Subr < gsubrOffsets.length-1 && Subr>=0)
			{
				// Read the subr and process
				int Start = gsubrOffsets[Subr];
				int End = gsubrOffsets[Subr+1];
				
				if (fonts[Font].isCID)
					ReadASubr(Start,End,GBias,0,hGSubrsUsed,lGSubrsUsed,null);
				else
				{
					ReadASubr(Start,End,GBias,LBias,hSubrsUsedNonCID,lSubrsUsedNonCID,fonts[Font].SubrsOffsets);
					if (SizeOfNonCIDSubrsUsed < lSubrsUsedNonCID.size())
					{
						for (int j=SizeOfNonCIDSubrsUsed;j<lSubrsUsedNonCID.size();j++)
						{
							//Pop the value + check valid 
							int LSubr = ((Integer)lSubrsUsedNonCID.get(j)).intValue();
							if (LSubr < fonts[Font].SubrsOffsets.length-1 && LSubr>=0)
							{
								// Read the subr and process
								int LStart = fonts[Font].SubrsOffsets[LSubr];
								int LEnd = fonts[Font].SubrsOffsets[LSubr+1];
								ReadASubr(LStart,LEnd,GBias,LBias,hSubrsUsedNonCID,lSubrsUsedNonCID,fonts[Font].SubrsOffsets);
							}
						}
						SizeOfNonCIDSubrsUsed = lSubrsUsedNonCID.size();
					}
				}
			}
		}
	}

	/**
	 * The function reads a subrs (glyph info) between begin and end.
	 * Adds calls to a Lsubr to the hSubr and lSubrs.
	 * Adds calls to a Gsubr to the hGSubr and lGSubrs.
	 * @param begin the start point of the subr
	 * @param end the end point of the subr
	 * @param GBias the bias of the Global Subrs
	 * @param LBias the bias of the Local Subrs
	 * @param hSubr the HashMap for the lSubrs
	 * @param lSubr the ArrayList for the lSubrs
	 */
	protected void ReadASubr(int begin,int end,int GBias,int LBias,HashMap hSubr,ArrayList lSubr,int[] LSubrsOffsets)
	{
		// Clear the stack for the subrs
		EmptyStack();
		NumOfHints = 0;
		// Goto beginning of the subr
        seek(begin);
        while (getPosition() < end)
        {
        	// Read the next command
        	ReadCommand();
        	int pos = getPosition();
        	Object TopElement=null;
        	if (arg_count > 0)
        		TopElement = args[arg_count-1];
        	int NumOfArgs = arg_count;
        	// Check the modification needed on the Argument Stack according to key;
        	HandelStack();
        	// a call to a Lsubr
        	if (key=="callsubr") 
        	{
        		// Verify that arguments are passed 
        		if (NumOfArgs > 0)
        		{
            		// Calc the index of the Subrs
            		int Subr = ((Integer)TopElement).intValue() + LBias;
            		// If the subr isn't in the HashMap -> Put in
        			if (!hSubr.containsKey(new Integer (Subr)))
            		{
            			hSubr.put(new Integer(Subr),null);
            			lSubr.add(new Integer(Subr));
            		}
        			CalcHints(LSubrsOffsets[Subr],LSubrsOffsets[Subr+1],LBias,GBias,LSubrsOffsets);
        			seek(pos);
        		}        		
        	}
        	// a call to a Gsubr
        	else if (key=="callgsubr")
        	{
        		// Verify that arguments are passed 
        		if (NumOfArgs > 0)
        		{
	        		// Calc the index of the Subrs
	        		int Subr = ((Integer)TopElement).intValue() + GBias;
	        		// If the subr isn't in the HashMap -> Put in
	        		if (!hGSubrsUsed.containsKey(new Integer (Subr)))
	        		{
	        			hGSubrsUsed.put(new Integer(Subr),null);
	        			lGSubrsUsed.add(new Integer(Subr));
	        		}
	        		CalcHints(gsubrOffsets[Subr],gsubrOffsets[Subr+1],LBias,GBias,LSubrsOffsets);
	        		seek(pos);
        		}
        	}
        	// A call to "stem"
        	else if (key == "hstem" || key == "vstem" || key == "hstemhm" || key == "vstemhm")
        		// Increment the NumOfHints by the number couples of of arguments
        		NumOfHints += NumOfArgs/2;
        	// A call to "mask"
        	else if (key == "hintmask" || key == "cntrmask")
        	{
        		// Compute the size of the mask
        		int SizeOfMask = NumOfHints/8;
        		if (NumOfHints%8 != 0 || SizeOfMask == 0)
        			SizeOfMask++;
        		// Continue the pointer in SizeOfMask steps
        		for (int i=0;i<SizeOfMask;i++)
        			getCard8();
        	}
        }
	}

	/**
	 * Function Checks how the current operator effects the run time stack after being run 
	 * An operator may increase or decrease the stack size
	 */
	protected void HandelStack()
	{
    	// Find out what the operator does to the stack
    	int StackHandel = StackOpp();
    	if (StackHandel < 2)
    	{
    		// The operators that enlarge the stack by one
    		if (StackHandel==1)
    			PushStack();
    		// The operators that pop the stack
    		else
    		{
    			// Abs value for the for loop
    			StackHandel *= -1;
    			for (int i=0;i<StackHandel;i++)
    				PopStack();
    		}
    		
    	}
    	// All other flush the stack
    	else
    		EmptyStack();		
	}
	
	/**
	 * Function checks the key and return the change to the stack after the operator
	 * @return The change in the stack. 2-> flush the stack
	 */
	protected int StackOpp()
	{
		if (key == "ifelse")
			return -3;
		if (key == "roll" || key == "put")
			return -2;
		if (key == "callsubr" || key == "callgsubr" || key == "add" || key == "sub" ||
			key == "div" || key == "mul" || key == "drop" || key == "and" || 
			key == "or" || key == "eq")
			return -1;
		if (key == "abs" || key == "neg" || key == "sqrt" || key == "exch" || 
			key == "index" || key == "get" || key == "not" || key == "return")
			return 0;
		if (key == "random" || key == "dup")
			return 1;
		return 2;
	}
	
	/**
	 * Empty the Type2 Stack
	 *
	 */
	protected void EmptyStack()
	{
		// Null the arguments
    	for (int i=0; i<arg_count; i++) args[i]=null;
        arg_count = 0;		
	}
	
	/**
	 * Pop one element from the stack 
	 *
	 */
	protected void PopStack()
	{
		if (arg_count>0)
		{
			args[arg_count-1]=null;
			arg_count--;
		}
	}
	
	/**
	 * Add an item to the stack
	 *
	 */
	protected void PushStack()
	{
		arg_count++;
	}
	
	/**
	 * The function reads the next command after the file pointer is set
	 */
	protected void ReadCommand()
	{
        key = null;
        boolean gotKey = false;
        // Until a key is found
        while (!gotKey) {
        	// Read the first Char
            char b0 = getCard8();
            // decode according to the type1/type2 format
            if (b0 == 28) // the two next bytes represent a short int;
            {
            	int first = getCard8();
            	int second = getCard8();
            	args[arg_count] = new Integer(first<<8 | second);
            	arg_count++;
            	continue;
            }
            if (b0 >= 32 && b0 <= 246) // The byte read is the byte;
            {
            	args[arg_count] = new Integer(b0 - 139);
            	arg_count++;
            	continue;
            }
            if (b0 >= 247 && b0 <= 250) // The byte read and the next byte constitute a short int
            {
            	int w = getCard8();
            	args[arg_count] = new Integer((b0-247)*256 + w + 108);
            	arg_count++;
            	continue;
            }
            if (b0 >= 251 && b0 <= 254)// Same as above except negative
            {
            	int w = getCard8();
            	args[arg_count] = new Integer(-(b0-251)*256 - w - 108);
            	arg_count++;
            	continue;
            }
            if (b0 == 255)// The next for bytes represent a double.
            {
            	int first = getCard8();
            	int second = getCard8();
            	int third = getCard8();
            	int fourth = getCard8();
            	args[arg_count] = new Integer(first<<24 | second<<16 | third<<8 | fourth);
            	arg_count++;
            	continue;
            }
            if (b0<=31 && b0 != 28) // An operator was found.. Set Key.
            {
            	gotKey=true;
            	// 12 is an escape command therefore the next byte is a part
            	// of this command
            	if (b0 == 12)
            	{
            		int b1 = getCard8();
            		if (b1>SubrsEscapeFuncs.length-1)
            			b1 = SubrsEscapeFuncs.length-1;
            		key = SubrsEscapeFuncs[b1];
            	}
            	else
            		key = SubrsFunctions[b0];
                continue;
            }
        }		
	}
	
	/**
	 * The function reads the subroutine and returns the number of the hint in it.
	 * If a call to another subroutine is found the function calls recursively.
	 * @param begin the start point of the subr
	 * @param end the end point of the subr
	 * @param LBias the bias of the Local Subrs
	 * @param GBias the bias of the Global Subrs
	 * @param LSubrsOffsets The Offsets array of the subroutines
	 * @return The number of hints in the subroutine read.
	 */
	protected int CalcHints(int begin,int end,int LBias,int GBias,int[] LSubrsOffsets)
	{
		// Goto beginning of the subr
        seek(begin);
        while (getPosition() < end)
        {
        	// Read the next command
        	ReadCommand();
        	int pos = getPosition();
        	Object TopElement = null;
        	if (arg_count>0)
        		TopElement = args[arg_count-1];
        	int NumOfArgs = arg_count;
            //Check the modification needed on the Argument Stack according to key;
        	HandelStack();
        	// a call to a Lsubr
        	if (key=="callsubr") 
        	{
        		if (NumOfArgs>0)
        		{
            		int Subr = ((Integer)TopElement).intValue() + LBias;
            		CalcHints(LSubrsOffsets[Subr],LSubrsOffsets[Subr+1],LBias,GBias,LSubrsOffsets);
            		seek(pos);        			
        		}
        	}
        	// a call to a Gsubr
        	else if (key=="callgsubr")
        	{
        		if (NumOfArgs>0)
        		{
            		int Subr = ((Integer)TopElement).intValue() + GBias;
            		CalcHints(gsubrOffsets[Subr],gsubrOffsets[Subr+1],LBias,GBias,LSubrsOffsets);
            		seek(pos);        			
        		}
        	}
        	// A call to "stem"
        	else if (key == "hstem" || key == "vstem" || key == "hstemhm" || key == "vstemhm")
        		// Increment the NumOfHints by the number couples of of arguments
        		NumOfHints += NumOfArgs/2;
        	// A call to "mask"
        	else if (key == "hintmask" || key == "cntrmask")
        	{
        		// Compute the size of the mask
        		int SizeOfMask = NumOfHints/8;
        		if (NumOfHints%8 != 0 || SizeOfMask == 0)
        			SizeOfMask++;
        		// Continue the pointer in SizeOfMask steps
        		for (int i=0;i<SizeOfMask;i++)
        			getCard8();
        	}
        }
        return NumOfHints;
	}


	/**
	 * Function builds the new offset array, object array and assembles the index.
	 * used for creating the glyph and subrs subsetted index 
	 * @param Offsets the offset array of the original index  
	 * @param Used the hashmap of the used objects
	 * @param OperatorForUnusedEntries the operator inserted into the data stream for unused entries
	 * @return the new index subset version 
	 * @throws IOException
	 */
	protected byte[] BuildNewIndex(int[] Offsets,HashMap Used,byte OperatorForUnusedEntries) throws IOException 
	{
		int unusedCount = 0;
		int Offset=0;
		int[] NewOffsets = new int[Offsets.length];
		// Build the Offsets Array for the Subset
		for (int i=0;i<Offsets.length;++i)
		{
			NewOffsets[i] = Offset;
			// If the object in the offset is also present in the used
			// HashMap then increment the offset var by its size
			if (Used.containsKey(new Integer(i))) {
				Offset += Offsets[i+1] - Offsets[i];
			} else {
				// Else the same offset is kept in i+1.
				unusedCount++;
			}
		}
		// Offset var determines the size of the object array
		byte[] NewObjects = new byte[Offset+unusedCount];
		// Build the new Object array
		int unusedOffset = 0;
		for (int i=0;i<Offsets.length-1;++i)
		{
			int start = NewOffsets[i];
			int end = NewOffsets[i+1];
			NewOffsets[i] = start+unusedOffset;
			// If start != End then the Object is used
			// So, we will copy the object data from the font file
			if (start != end)
			{
				// All offsets are Global Offsets relative to the beginning of the font file.
				// Jump the file pointer to the start address to read from.
				buf.seek(Offsets[i]);
				// Read from the buffer and write into the array at start.  
                buf.readFully(NewObjects, start+unusedOffset, end-start);
			} else {
				NewObjects[start+unusedOffset] = OperatorForUnusedEntries;
				unusedOffset++;
			}
		}
		NewOffsets[Offsets.length-1] += unusedOffset;
		// Use AssembleIndex to build the index from the offset & object arrays
		return AssembleIndex(NewOffsets,NewObjects);
	}

	/**
	 * Function creates the new index, inserting the count,offsetsize,offset array
	 * and object array.
	 * @param NewOffsets the subsetted offset array
	 * @param NewObjects the subsetted object array
	 * @return the new index created
	 */
	protected byte[] AssembleIndex(int[] NewOffsets,byte[] NewObjects)
	{
		// Calc the index' count field
		char Count = (char)(NewOffsets.length-1);
		// Calc the size of the object array
		int Size = NewOffsets[NewOffsets.length-1];
		// Calc the Offsize
		byte Offsize;
        if (Size <= 0xff) Offsize = 1;
        else if (Size <= 0xffff) Offsize = 2;
        else if (Size <= 0xffffff) Offsize = 3;
        else Offsize = 4;
        // The byte array for the new index. The size is calc by
        // Count=2, Offsize=1, OffsetArray = Offsize*(Count+1), The object array
        byte[] NewIndex = new byte[2+1+Offsize*(Count+1)+NewObjects.length];
        // The counter for writing
        int Place = 0;
        // Write the count field
        NewIndex[Place++] = (byte) ((Count >>> 8) & 0xff);
        NewIndex[Place++] = (byte) ((Count >>> 0) & 0xff);
        // Write the offsize field
        NewIndex[Place++] = Offsize;
        // Write the offset array according to the offsize
        for (int i=0;i<NewOffsets.length;i++)
        {
        	// The value to be written
        	int Num = NewOffsets[i]-NewOffsets[0]+1;
        	// Write in bytes according to the offsize
        	switch (Offsize) {
                case 4:
                	NewIndex[Place++] = (byte) ((Num >>> 24) & 0xff);
                case 3:
                	NewIndex[Place++] = (byte) ((Num >>> 16) & 0xff);
                case 2:
                	NewIndex[Place++] = (byte) ((Num >>>  8) & 0xff);
                case 1:
                	NewIndex[Place++] = (byte) ((Num >>>  0) & 0xff);
        	}                	
        }
        // Write the new object array one by one
        for (int i=0;i<NewObjects.length;i++)
        {
        	NewIndex[Place++] = NewObjects[i];
        }
        // Return the new index
        return NewIndex;
	}
	
	/**
	 * The function builds the new output stream according to the subset process
	 * @param Font the font
	 * @return the subsetted font stream
	 */
	protected byte[] BuildNewFile(int Font)
    {
		// Prepare linked list for new font components
		OutputList = new LinkedList();

        // copy the header of the font
        CopyHeader();
                
        // create a name index
        BuildIndexHeader(1,1,1);
        OutputList.addLast(new UInt8Item((char)( 1+fonts[Font].name.length() )));
        OutputList.addLast(new StringItem(fonts[Font].name));
        
        // create the topdict Index
        BuildIndexHeader(1,2,1);
        OffsetItem topdictIndex1Ref = new IndexOffsetItem(2);
        OutputList.addLast(topdictIndex1Ref);
        IndexBaseItem topdictBase = new IndexBaseItem();
        OutputList.addLast(topdictBase);
                
        // Initialize the Dict Items for later use
        OffsetItem charsetRef     = new DictOffsetItem();
        OffsetItem charstringsRef = new DictOffsetItem();
        OffsetItem fdarrayRef     = new DictOffsetItem();
        OffsetItem fdselectRef    = new DictOffsetItem();
        OffsetItem privateRef     = new DictOffsetItem();
        
        // If the font is not CID create the following keys
        if ( !fonts[Font].isCID ) {
            // create a ROS key
        	OutputList.addLast(new DictNumberItem(fonts[Font].nstrings));
        	OutputList.addLast(new DictNumberItem(fonts[Font].nstrings+1));
        	OutputList.addLast(new DictNumberItem(0));
        	OutputList.addLast(new UInt8Item((char)12));
        	OutputList.addLast(new UInt8Item((char)30));
            // create a CIDCount key
        	OutputList.addLast(new DictNumberItem(fonts[Font].nglyphs));
        	OutputList.addLast(new UInt8Item((char)12));
            OutputList.addLast(new UInt8Item((char)34));
            // Sivan's comments
            // What about UIDBase (12,35)? Don't know what is it.
            // I don't think we need FontName; the font I looked at didn't have it.
        }
        // Go to the TopDict of the font being processed
        seek(topdictOffsets[Font]);
        // Run until the end of the TopDict
        while (getPosition() < topdictOffsets[Font+1]) {
            int p1 = getPosition();
            getDictItem();
            int p2 = getPosition();
            // The encoding key is disregarded since CID has no encoding
            if (key=="Encoding"
            // These keys will be added manually by the process.
            || key=="Private" 
            || key=="FDSelect"
            || key=="FDArray" 
            || key=="charset" 
            || key=="CharStrings"
            ) {
            }else {
            //OtherWise copy key "as is" to the output list
                OutputList.add(new RangeItem(buf,p1,p2-p1));
            }
        }
        // Create the FDArray, FDSelect, Charset and CharStrings Keys
        CreateKeys(fdarrayRef,fdselectRef,charsetRef,charstringsRef);
        
        // Mark the end of the top dict area
        OutputList.addLast(new IndexMarkerItem(topdictIndex1Ref,topdictBase));
        
        // Copy the string index

        if (fonts[Font].isCID) 
            OutputList.addLast(getEntireIndexRange(stringIndexOffset));
        // If the font is not CID we need to append new strings.
        // We need 3 more strings: Registry, Ordering, and a FontName for one FD.
        // The total length is at most "Adobe"+"Identity"+63 = 76
        else
        	CreateNewStringIndex(Font);
        
        // copy the new subsetted global subroutine index       
        OutputList.addLast(new RangeItem(new RandomAccessFileOrArray(NewGSubrsIndex),0,NewGSubrsIndex.length));
        
        // deal with fdarray, fdselect, and the font descriptors
        // If the font is CID:
        if (fonts[Font].isCID) {
            // copy the FDArray, FDSelect, charset
       
            // Copy FDSelect
        	// Mark the beginning
            OutputList.addLast(new MarkerItem(fdselectRef));
            // If an FDSelect exists copy it
            if (fonts[Font].fdselectOffset>=0)
        		OutputList.addLast(new RangeItem(buf,fonts[Font].fdselectOffset,fonts[Font].FDSelectLength));
            // Else create a new one
            else
            	CreateFDSelect(fdselectRef,fonts[Font].nglyphs);
            	           
          	// Copy the Charset
            // Mark the beginning and copy entirely 
            OutputList.addLast(new MarkerItem(charsetRef));
            OutputList.addLast(new RangeItem(buf,fonts[Font].charsetOffset,fonts[Font].CharsetLength));
            
            // Copy the FDArray
            // If an FDArray exists
            if (fonts[Font].fdarrayOffset>=0)
            {
                // Mark the beginning
	            OutputList.addLast(new MarkerItem(fdarrayRef));
	            // Build a new FDArray with its private dicts and their LSubrs
	            Reconstruct(Font);
            }
            else
            	// Else create a new one
            	CreateFDArray(fdarrayRef,privateRef,Font);
           
        }
        // If the font is not CID
        else 
        {
            // create FDSelect
        	CreateFDSelect(fdselectRef,fonts[Font].nglyphs);            
            // recreate a new charset
        	CreateCharset(charsetRef,fonts[Font].nglyphs);            
            // create a font dict index (fdarray)
        	CreateFDArray(fdarrayRef,privateRef,Font);            
        }
        
        // if a private dict exists insert its subsetted version
        if (fonts[Font].privateOffset>=0)
        {
        	// Mark the beginning of the private dict
        	IndexBaseItem PrivateBase = new IndexBaseItem();
	        OutputList.addLast(PrivateBase);
        	OutputList.addLast(new MarkerItem(privateRef));

        	OffsetItem Subr = new DictOffsetItem();
        	// Build and copy the new private dict
        	CreateNonCIDPrivate(Font,Subr);
        	// Copy the new LSubrs index
        	CreateNonCIDSubrs(Font,PrivateBase,Subr);
        }
        
        // copy the charstring index
        OutputList.addLast(new MarkerItem(charstringsRef));

        // Add the subsetted charstring
        OutputList.addLast(new RangeItem(new RandomAccessFileOrArray(NewCharStringsIndex),0,NewCharStringsIndex.length));
        
        // now create the new CFF font        
        int[] currentOffset = new int[1];
        currentOffset[0] = 0;
        // Count and save the offset for each item
        Iterator listIter = OutputList.iterator();
        while ( listIter.hasNext() ) {
            Item item = (Item) listIter.next();
            item.increment(currentOffset);
        }
        // Compute the Xref for each of the offset items
        listIter = OutputList.iterator();
        while ( listIter.hasNext() ) {
            Item item = (Item) listIter.next();
            item.xref();
        }
        
        int size = currentOffset[0];
        byte[] b = new byte[size];
        
        // Emit all the items into the new byte array
        listIter = OutputList.iterator();
        while ( listIter.hasNext() ) {
            Item item = (Item) listIter.next();
            item.emit(b);
        }
        // Return the new stream
        return b;
    }

	/**
	 * Function Copies the header from the original fileto the output list
	 */
	protected void CopyHeader()
	{
		seek(0);
        int major = getCard8();
        int minor = getCard8();
        int hdrSize = getCard8();
        int offSize = getCard8();
        nextIndexOffset = hdrSize;
        OutputList.addLast(new RangeItem(buf,0,hdrSize));
	}

	/**
	 * Function Build the header of an index
	 * @param Count the count field of the index
	 * @param Offsize the offsize field of the index
	 * @param First the first offset of the index
	 */
	protected void BuildIndexHeader(int Count,int Offsize,int First)
	{
		// Add the count field
		OutputList.addLast(new UInt16Item((char)Count)); // count
		// Add the offsize field
		OutputList.addLast(new UInt8Item((char)Offsize)); // offSize
		// Add the first offset according to the offsize
        switch(Offsize){
        	case 1:
        		OutputList.addLast(new UInt8Item((char)First)); // first offset
        		break;
        	case 2:
        		OutputList.addLast(new UInt16Item((char)First)); // first offset
        		break;
        	case 3:
        		OutputList.addLast(new UInt24Item((char)First)); // first offset
        		break;
        	case 4:
        		OutputList.addLast(new UInt32Item((char)First)); // first offset
        		break;
    		default:
    			break;	
        }
	}
	
	/**
	 * Function adds the keys into the TopDict
	 * @param fdarrayRef OffsetItem for the FDArray
	 * @param fdselectRef OffsetItem for the FDSelect
	 * @param charsetRef OffsetItem for the CharSet
	 * @param charstringsRef OffsetItem for the CharString
	 */
	protected void CreateKeys(OffsetItem fdarrayRef,OffsetItem fdselectRef,OffsetItem charsetRef,OffsetItem charstringsRef)
	{
	    // create an FDArray key
        OutputList.addLast(fdarrayRef);
        OutputList.addLast(new UInt8Item((char)12));
        OutputList.addLast(new UInt8Item((char)36));
        // create an FDSelect key
        OutputList.addLast(fdselectRef);
        OutputList.addLast(new UInt8Item((char)12));
        OutputList.addLast(new UInt8Item((char)37));
        // create an charset key
        OutputList.addLast(charsetRef);
        OutputList.addLast(new UInt8Item((char)15));
        // create a CharStrings key
        OutputList.addLast(charstringsRef);
        OutputList.addLast(new UInt8Item((char)17));
	}
	
	/**
	 * Function takes the original string item and adds the new strings
	 * to accommodate the CID rules
	 * @param Font the font
	 */
	protected void CreateNewStringIndex(int Font)
	{
        String fdFontName = fonts[Font].name+"-OneRange";
        if (fdFontName.length() > 127)
            fdFontName = fdFontName.substring(0,127);
        String extraStrings = "Adobe"+"Identity"+fdFontName;
        
        int origStringsLen = stringOffsets[stringOffsets.length-1]
        - stringOffsets[0];
        int stringsBaseOffset = stringOffsets[0]-1;
        
        byte stringsIndexOffSize;
        if (origStringsLen+extraStrings.length() <= 0xff) stringsIndexOffSize = 1;
        else if (origStringsLen+extraStrings.length() <= 0xffff) stringsIndexOffSize = 2;
        else if (origStringsLen+extraStrings.length() <= 0xffffff) stringsIndexOffSize = 3;
        else stringsIndexOffSize = 4;
        
        OutputList.addLast(new UInt16Item((char)((stringOffsets.length-1)+3))); // count
        OutputList.addLast(new UInt8Item((char)stringsIndexOffSize)); // offSize
        for (int i=0; i<stringOffsets.length; i++)
            OutputList.addLast(new IndexOffsetItem(stringsIndexOffSize,
            stringOffsets[i]-stringsBaseOffset));
        int currentStringsOffset = stringOffsets[stringOffsets.length-1]
        - stringsBaseOffset;
        //l.addLast(new IndexOffsetItem(stringsIndexOffSize,currentStringsOffset));
        currentStringsOffset += "Adobe".length();
        OutputList.addLast(new IndexOffsetItem(stringsIndexOffSize,currentStringsOffset));
        currentStringsOffset += "Identity".length();
        OutputList.addLast(new IndexOffsetItem(stringsIndexOffSize,currentStringsOffset));
        currentStringsOffset += fdFontName.length();
        OutputList.addLast(new IndexOffsetItem(stringsIndexOffSize,currentStringsOffset));
        
        OutputList.addLast(new RangeItem(buf,stringOffsets[0],origStringsLen));
        OutputList.addLast(new StringItem(extraStrings));
    }
     
	/**
	 * Function creates new FDSelect for non-CID fonts.
	 * The FDSelect built uses a single range for all glyphs
	 * @param fdselectRef OffsetItem for the FDSelect
	 * @param nglyphs the number of glyphs in the font
	 */
	protected void CreateFDSelect(OffsetItem fdselectRef,int nglyphs)
	{
		OutputList.addLast(new MarkerItem(fdselectRef));
	    OutputList.addLast(new UInt8Item((char)3)); // format identifier
	    OutputList.addLast(new UInt16Item((char)1)); // nRanges
	    
	    OutputList.addLast(new UInt16Item((char)0)); // Range[0].firstGlyph
	    OutputList.addLast(new UInt8Item((char)0)); // Range[0].fd
	    
	    OutputList.addLast(new UInt16Item((char)nglyphs)); // sentinel
	}

	/**
	 * Function creates new CharSet for non-CID fonts.
	 * The CharSet built uses a single range for all glyphs
	 * @param charsetRef OffsetItem for the CharSet
	 * @param nglyphs the number of glyphs in the font
	 */
	protected void CreateCharset(OffsetItem charsetRef,int nglyphs)
	{
		OutputList.addLast(new MarkerItem(charsetRef));
	    OutputList.addLast(new UInt8Item((char)2)); // format identifier
	    OutputList.addLast(new UInt16Item((char)1)); // first glyph in range (ignore .notdef)
	    OutputList.addLast(new UInt16Item((char)(nglyphs-1))); // nLeft
	}
    
	/**
	 * Function creates new FDArray for non-CID fonts.
	 * The FDArray built has only the "Private" operator that points to the font's
	 * original private dict 
	 * @param fdarrayRef OffsetItem for the FDArray
	 * @param privateRef OffsetItem for the Private Dict
	 * @param Font the font
	 */
	protected void CreateFDArray(OffsetItem fdarrayRef,OffsetItem privateRef,int Font)
	{
		OutputList.addLast(new MarkerItem(fdarrayRef));
		// Build the header (count=offsize=first=1)
		BuildIndexHeader(1,1,1);
	    
		// Mark
	    OffsetItem privateIndex1Ref = new IndexOffsetItem(1);
	    OutputList.addLast(privateIndex1Ref);
	    IndexBaseItem privateBase = new IndexBaseItem();
	    // Insert the private operands and operator
	    OutputList.addLast(privateBase);
	    // Calc the new size of the private after subsetting
	    // Origianl size
	    int NewSize = fonts[Font].privateLength;
	    // Calc the original size of the Subr offset in the private
		int OrgSubrsOffsetSize = CalcSubrOffsetSize(fonts[Font].privateOffset,fonts[Font].privateLength);
		// Increase the ptivate's size
		if (OrgSubrsOffsetSize != 0)
			NewSize += 5-OrgSubrsOffsetSize;
	    OutputList.addLast(new DictNumberItem(NewSize));
	    OutputList.addLast(privateRef);
	    OutputList.addLast(new UInt8Item((char)18)); // Private
	    
	    OutputList.addLast(new IndexMarkerItem(privateIndex1Ref,privateBase));
	}
    
	/**
	 * Function reconstructs the FDArray, PrivateDict and LSubr for CID fonts
	 * @param Font the font
	 */
	void Reconstruct(int Font)
	{
		// Init for later use
		OffsetItem[] fdPrivate = new DictOffsetItem[fonts[Font].FDArrayOffsets.length-1];
		IndexBaseItem[] fdPrivateBase = new IndexBaseItem[fonts[Font].fdprivateOffsets.length]; 
		OffsetItem[] fdSubrs = new DictOffsetItem[fonts[Font].fdprivateOffsets.length];
		// Reconstruct each type
		ReconstructFDArray(Font,fdPrivate);
		ReconstructPrivateDict(Font,fdPrivate,fdPrivateBase,fdSubrs);
		ReconstructPrivateSubrs(Font,fdPrivateBase,fdSubrs);
	}

	/**
	 * Function subsets the FDArray and builds the new one with new offsets
	 * @param Font The font
	 * @param fdPrivate OffsetItem Array (one for each FDArray)
	 */
	void ReconstructFDArray(int Font,OffsetItem[] fdPrivate)
	{
		// Build the header of the index
		BuildIndexHeader(fonts[Font].FDArrayCount,fonts[Font].FDArrayOffsize,1);

		// For each offset create an Offset Item
		OffsetItem[] fdOffsets = new IndexOffsetItem[fonts[Font].FDArrayOffsets.length-1];
		for (int i=0;i<fonts[Font].FDArrayOffsets.length-1;i++)
		{
			fdOffsets[i] = new IndexOffsetItem(fonts[Font].FDArrayOffsize);
			OutputList.addLast(fdOffsets[i]);
		}
		
		// Declare beginning of the object array
		IndexBaseItem fdArrayBase = new IndexBaseItem();
	    OutputList.addLast(fdArrayBase);
	    
		// For each object check if that FD is used.
	    // if is used build a new one by changing the private object
	    // Else do nothing
	    // At the end of each object mark its ending (Even if wasn't written)
		for (int k=0; k<fonts[Font].FDArrayOffsets.length-1; k++) {
			if (FDArrayUsed.containsKey(new Integer (k)))
			{
				// Goto beginning of objects
	            seek(fonts[Font].FDArrayOffsets[k]);
	            while (getPosition() < fonts[Font].FDArrayOffsets[k+1])
	            {
	            	int p1 = getPosition();
	            	getDictItem();
	            	int p2 = getPosition();
	            	// If the dictItem is the "Private" then compute and copy length, 
	            	// use marker for offset and write operator number
	            	if (key=="Private") {
	            		// Save the original length of the private dict
	            		int NewSize = ((Integer)args[0]).intValue();
	            		// Save the size of the offset to the subrs in that private
	            		int OrgSubrsOffsetSize = CalcSubrOffsetSize(fonts[Font].fdprivateOffsets[k],fonts[Font].fdprivateLengths[k]);
	            		// Increase the private's length accordingly
	            		if (OrgSubrsOffsetSize != 0)
	            			NewSize += 5-OrgSubrsOffsetSize;
	            		// Insert the new size, OffsetItem and operator key number
	            		OutputList.addLast(new DictNumberItem(NewSize));
	            		fdPrivate[k] = new DictOffsetItem();
	            		OutputList.addLast(fdPrivate[k]);
	            	    OutputList.addLast(new UInt8Item((char)18)); // Private
	            	    // Go back to place 
	            	    seek(p2);
	            	}
	            	// Else copy the entire range
	            	else  // other than private
	            		OutputList.addLast(new RangeItem(buf,p1,p2-p1));
	            }
			}
            // Mark the ending of the object (even if wasn't written)
            OutputList.addLast(new IndexMarkerItem(fdOffsets[k],fdArrayBase));
        }
	}
	/**
	 * Function Adds the new private dicts (only for the FDs used) to the list
	 * @param Font the font
	 * @param fdPrivate OffsetItem array one element for each private
	 * @param fdPrivateBase IndexBaseItem array one element for each private
	 * @param fdSubrs OffsetItem array one element for each private
	 */
	void ReconstructPrivateDict(int Font,OffsetItem[] fdPrivate,IndexBaseItem[] fdPrivateBase,
			OffsetItem[] fdSubrs)
	{
		
		// For each fdarray private dict check if that FD is used.
	    // if is used build a new one by changing the subrs offset
	    // Else do nothing
		for (int i=0;i<fonts[Font].fdprivateOffsets.length;i++)
		{
			if (FDArrayUsed.containsKey(new Integer (i)))
			{
				// Mark beginning
		        OutputList.addLast(new MarkerItem(fdPrivate[i]));
		        fdPrivateBase[i] = new IndexBaseItem();
		        OutputList.addLast(fdPrivateBase[i]);
				// Goto beginning of objects
	            seek(fonts[Font].fdprivateOffsets[i]);
	            while (getPosition() < fonts[Font].fdprivateOffsets[i]+fonts[Font].fdprivateLengths[i])
	            {
	            	int p1 = getPosition();
	            	getDictItem();
	            	int p2 = getPosition();
	            	// If the dictItem is the "Subrs" then, 
	            	// use marker for offset and write operator number
	            	if (key=="Subrs") {
	            		fdSubrs[i] = new DictOffsetItem();
	            		OutputList.addLast(fdSubrs[i]);
	            	    OutputList.addLast(new UInt8Item((char)19)); // Subrs
	            	}
	            	// Else copy the entire range
	            	else
	            		OutputList.addLast(new RangeItem(buf,p1,p2-p1));
	            }
			}
		}
	}
	
	/**
	 * Function Adds the new LSubrs dicts (only for the FDs used) to the list
	 * @param Font  The index of the font
	 * @param fdPrivateBase The IndexBaseItem array for the linked list
	 * @param fdSubrs OffsetItem array for the linked list
	 */
	
	void ReconstructPrivateSubrs(int Font,IndexBaseItem[] fdPrivateBase,
			OffsetItem[] fdSubrs)
	{
		// For each private dict
        for (int i=0;i<fonts[Font].fdprivateLengths.length;i++)
        {
        	// If that private dict's Subrs are used insert the new LSubrs
        	// computed earlier
        	if (fdSubrs[i]!= null && fonts[Font].PrivateSubrsOffset[i] >= 0)
        	{        		
        		OutputList.addLast(new SubrMarkerItem(fdSubrs[i],fdPrivateBase[i]));
        		OutputList.addLast(new RangeItem(new RandomAccessFileOrArray(NewLSubrsIndex[i]),0,NewLSubrsIndex[i].length));
        	}
        }
    }

	/**
	 * Calculates how many byte it took to write the offset for the subrs in a specific
	 * private dict.
	 * @param Offset The Offset for the private dict
	 * @param Size The size of the private dict
	 * @return The size of the offset of the subrs in the private dict
	 */
	int CalcSubrOffsetSize(int Offset,int Size)
	{
		// Set the size to 0
		int OffsetSize = 0;
		// Go to the beginning of the private dict
		seek(Offset);
		// Go until the end of the private dict 
		while (getPosition() < Offset+Size)
        {
        	int p1 = getPosition();
        	getDictItem();
        	int p2 = getPosition();
        	// When reached to the subrs offset
        	if (key=="Subrs") {
        		// The Offsize (minus the subrs key)
        		OffsetSize = p2-p1-1;
        	}
        	// All other keys are ignored
        }
		// return the size
		return OffsetSize;
	}
	
	/**
	 * Function computes the size of an index
	 * @param indexOffset The offset for the computed index
	 * @return The size of the index
	 */
	protected int countEntireIndexRange(int indexOffset) 
	{
		// Go to the beginning of the index 
	    seek(indexOffset);
	    // Read the count field
	    int count = getCard16();
	    // If count==0 -> size=2
	    if (count==0) 
	        return 2;
	    else 
	    {
	    	// Read the offsize field
	        int indexOffSize = getCard8();
	        // Go to the last element of the offset array
	        seek(indexOffset+2+1+count*indexOffSize);
	        // The size of the object array is the value of the last element-1
	        int size = getOffset(indexOffSize)-1;
	        // Return the size of the entire index
	        return 2+1+(count+1)*indexOffSize+size;
	    }
	}
	
	/**
	 * The function creates a private dict for a font that was not CID
	 * All the keys are copied as is except for the subrs key 
	 * @param Font the font
	 * @param Subr The OffsetItem for the subrs of the private 
	 */
	void CreateNonCIDPrivate(int Font,OffsetItem Subr)
	{
		// Go to the beginning of the private dict and read until the end
		seek(fonts[Font].privateOffset);
        while (getPosition() < fonts[Font].privateOffset+fonts[Font].privateLength)
        {
        	int p1 = getPosition();
        	getDictItem();
        	int p2 = getPosition();
        	// If the dictItem is the "Subrs" then, 
        	// use marker for offset and write operator number
        	if (key=="Subrs") {
        		OutputList.addLast(Subr);
        	    OutputList.addLast(new UInt8Item((char)19)); // Subrs
        	}
        	// Else copy the entire range
        	else
        		OutputList.addLast(new RangeItem(buf,p1,p2-p1));
        }
	}
	
	/**
	 * the function marks the beginning of the subrs index and adds the subsetted subrs
	 * index to the output list. 
	 * @param Font the font
	 * @param PrivateBase IndexBaseItem for the private that's referencing to the subrs
	 * @param Subrs OffsetItem for the subrs
	 */
	void CreateNonCIDSubrs(int Font,IndexBaseItem PrivateBase,OffsetItem Subrs)
	{
		// Mark the beginning of the Subrs index
		OutputList.addLast(new SubrMarkerItem(Subrs,PrivateBase));
		// Put the subsetted new subrs index
		OutputList.addLast(new RangeItem(new RandomAccessFileOrArray(NewSubrsIndexNonCID),0,NewSubrsIndexNonCID.length));
    }	
}
