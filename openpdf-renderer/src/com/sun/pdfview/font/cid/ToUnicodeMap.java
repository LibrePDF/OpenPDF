package com.sun.pdfview.font.cid;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sun.pdfview.PDFObject;

/*****************************************************************************
 * Parses a CMAP and builds a lookup table to map CMAP based codes to unicode.
 * This is not a fully functional CMAP parser but a stripped down parser
 * that should be able to parse some limited variants of CMAPs that are
 * used for the ToUnicode mapping found for some Type0 fonts.
 *
 * @author  Bernd Rosstauscher
 * @since 03.08.2011
 ****************************************************************************/

public class ToUnicodeMap extends PDFCMap {
	
	/*****************************************************************************
	 * Small helper class to define a code range.
	 ****************************************************************************/

	private static class CodeRangeMapping {
		char srcStart;
		char srcEnd;
		
		CodeRangeMapping(char srcStart, char srcEnd) {
			this.srcStart = srcStart;
			this.srcEnd = srcEnd;
		}
		
		boolean contains(char c) {
			return this.srcStart <= c 
								&& c <= this.srcEnd;
		}
		
	}
	
	/*****************************************************************************
	 * Small helper class to define a char range.
	 ****************************************************************************/

	private static class CharRangeMapping {
		char srcStart;
		char srcEnd;
		char destStart;
		
		CharRangeMapping(char srcStart, char srcEnd, char destStart) {
			this.srcStart = srcStart;
			this.srcEnd = srcEnd;
			this.destStart = destStart;
		}
		
		boolean contains(char c) {
			return this.srcStart <= c 
								&& c <= this.srcEnd;
		}
		
		char map(char src) {
			return (char) (this.destStart + (src-this.srcStart));
		}
		
	}
	
	private final Map<Character, Character> singleCharMappings;
	private final List<CharRangeMapping> charRangeMappings;
	private final List<CodeRangeMapping> codeRangeMappings;

	/*************************************************************************
	 * Constructor
	 * @param map 
	 * @throws IOException 
	 ************************************************************************/
	
	public ToUnicodeMap(PDFObject map) throws IOException {
		super();
		this.singleCharMappings = new HashMap<Character, Character>();
		this.charRangeMappings = new ArrayList<CharRangeMapping>();
		this.codeRangeMappings = new ArrayList<CodeRangeMapping>();
		parseMappings(map);
	}
	
	/*************************************************************************
	 * @param map
	 * @throws IOException 
	 ************************************************************************/
	
	private void parseMappings(PDFObject map) throws IOException {
		try {
			StringReader reader = new StringReader(new String(map.getStream(), "ASCII"));
			BufferedReader bf = new BufferedReader(reader);
			String line = bf.readLine();
			while (line != null) {
				if (line.contains("beginbfchar") || line.contains("begincidchar")) {
					parseSingleCharMappingSection(bf, line.contains("begincidchar"));
				}
				if (line.contains("beginbfrange") || line.contains("begincidrange")) {
					parseCharRangeMappingSection(bf, line.contains("begincidrange"));
				}
				if (line.contains("begincodespacerange")) {
					parseCodeRangeMappingSection(bf, line);
				}
				line = bf.readLine();
			}
		} catch (UnsupportedEncodingException e) {
			throw new IOException(e);
		} 
	}

	/*************************************************************************
	 * @param bf
	 * @throws IOException 
	 ************************************************************************/
	
	private void parseCharRangeMappingSection(BufferedReader bf, boolean isCid) throws IOException {
		String line = bf.readLine();
		while (line != null) {
			if (line.contains("endbfrange") || line.contains("endcidrange")) {
				break;
			}
			parseRangeLine(line, isCid);
			line = bf.readLine();
		}
	}

	private void parseCodeRangeMappingSection(BufferedReader bf, String line) throws IOException {
		//check if the prev line contains "endcodespacerange"
		if(line.contains("endcodespacerange")) {
			int indexOf = line.indexOf("endcodespacerange");
			line = line.substring(0, indexOf);
			indexOf = line.indexOf("begincodespacerange");
			line = line.substring(indexOf+"begincodespacerange".length(), line.length());
			line = line.trim();
			
			parseCodeRangeLine(line);
		}
		else {
			String rline = bf.readLine();
			while (rline != null) {
				if (rline.contains("endcodespacerange")) {
					break;
				}
				parseCodeRangeLine(rline);
				rline = bf.readLine();
			}
		}
	}

	/*************************************************************************
	 * @param line
	 * @return
	 ************************************************************************/
	
	private void parseRangeLine(String line, boolean isCid) {
		String[] mapping = line.split(" ");
		if (mapping.length == 3) {
			Character srcStart = parseChar(mapping[0]);
			Character srcEnd = parseChar(mapping[1]);
			Character destStart;
			if(isCid) {
				destStart = (char)Integer.parseInt(mapping[2]);
			}
			else {
				destStart = parseChar(mapping[2]);
			}
			this.charRangeMappings.add(new CharRangeMapping(srcStart, srcEnd, destStart));
		}
		else {
			int indexOf1 = line.indexOf(">");
			String substring1 = line.substring(0, indexOf1+1);
			
			int indexOf2 = line.indexOf("<", indexOf1);
			int indexOf3 = line.indexOf(">", indexOf2);
			String substring2 = line.substring(indexOf2, indexOf3+1);
			
			int indexOf4 = line.indexOf("<", indexOf3);
			String substring3 = line.substring(indexOf4, line.length());
			
			if(!substring1.isEmpty() && !substring2.isEmpty() && !substring3.isEmpty()) {
				Character srcStart = parseChar(substring1);
				Character srcEnd = parseChar(substring2);
				Character destStart = parseChar(substring3);
				this.charRangeMappings.add(new CharRangeMapping(srcStart, srcEnd, destStart));
			}
		}
	}

	private void parseCodeRangeLine(String line) {
		String[] mapping = line.split(" ");
		if (mapping.length == 2) {
			Character srcStart = parseChar(mapping[0]);
			Character srcEnd = parseChar(mapping[1]);
			this.codeRangeMappings.add(new CodeRangeMapping(srcStart, srcEnd));
		}
		else {
			int indexOf1 = line.indexOf(">");
			String substring1 = line.substring(0, indexOf1+1);
			
			int indexOf2 = line.indexOf("<", indexOf1);
			String substring2 = line.substring(indexOf2, line.length());
			
			if(!substring1.isEmpty() && !substring2.isEmpty()) {
				Character srcStart = parseChar(substring1);
				Character srcEnd = parseChar(substring2);
				this.codeRangeMappings.add(new CodeRangeMapping(srcStart, srcEnd));
			}
		}
	}

	/*************************************************************************
	 * @param bf
	 * @throws IOException 
	 ************************************************************************/
	
	private void parseSingleCharMappingSection(BufferedReader bf, boolean isCID) throws IOException {
		String line = bf.readLine();
		while (line != null) {
			if (line.contains("endbfchar") || line.contains("endcidchar")) {
				break;
			}
			parseSingleCharMappingLine(line, isCID);
			line = bf.readLine();
		}
	}

	/*************************************************************************
	 * @param line
	 * @return
	 ************************************************************************/
	
	private void parseSingleCharMappingLine(String line, boolean isCID) {
		String[] mapping = line.split(" ");
		if (mapping.length == 2) {
			if(isCID) {
				this.singleCharMappings.put(parseChar(mapping[0]), (char)Integer.parseInt(mapping[1]));
			}
			else {
				this.singleCharMappings.put(parseChar(mapping[0]), parseChar(mapping[1]));
			}
		}
	}

	/*************************************************************************
	 * Parse a string of the format <0F3A> to a char.
	 * @param charDef
	 * @return
	 ************************************************************************/
	
	private Character parseChar(String charDef) {
		if (charDef.startsWith("<")) {
			charDef = charDef.substring(1);
		}
		if (charDef.endsWith(">")) {
			charDef = charDef.substring(0, charDef.length()-1);
		}
		try {
			long result = Long.decode("0x" + charDef);
			return (char) result;
		} catch (NumberFormatException e) {
			return (char) ' ';
		}
	}

	/*************************************************************************
	 * map
	 * @see com.sun.pdfview.font.cid.PDFCMap#map(char)
	 ************************************************************************/
	@Override
	public char map(char src) {
		Character mappedChar = null;
		for (CodeRangeMapping codeRange : this.codeRangeMappings) {
			if(codeRange.contains(src)) {
				mappedChar = this.singleCharMappings.get(src);
				if (mappedChar == null) {
					mappedChar = lookupInRanges(src);
				}
				break;
			}
		}
		if (mappedChar == null) {
			// TODO XOND 27.03.2012: PDF Spec. "9.7.6.3Handling Undefined Characters"
			mappedChar = 0;
		}
		return mappedChar;
	}

	/*************************************************************************
	 * @param src
	 * @return
	 ************************************************************************/
	
	private Character lookupInRanges(char src) {
		Character mappedChar = null;
		for (CharRangeMapping rangeMapping : this.charRangeMappings) {
			if (rangeMapping.contains(src)) {
				mappedChar = rangeMapping.map(src);
				break;
			}
		}
		return mappedChar;
	}

}