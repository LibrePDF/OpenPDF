package com.sun.pdfview.function.postscript;

import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

/*****************************************************************************
 * Very simple post script parser / tokenizer
 *
 * @author  Bernd Rosstauscher
 * @since 22.10.2010
 ****************************************************************************/

public class PostScriptParser {
	
	/*************************************************************************
	 * Constructor
	 ************************************************************************/
	
	public PostScriptParser() {
		super();
	}
	
	/*************************************************************************
	 * Parses the given script and returns a list of tokens.
	 * @param scriptContent to parse.
	 * @return the list of tokens.
	 ************************************************************************/
	
	public List<String> parse(String scriptContent) {
		List<String> tokens = new LinkedList<String>();
		StringTokenizer tok = new StringTokenizer(scriptContent, " \t\n\r"); 
		while (tok.hasMoreTokens()) {
			String t = tok.nextToken();
			t = filterBlockStart(t);
			t = filterBlockEnd(t);
			if (t.length() > 0) {
				tokens.add(t.trim());
			}
		}
		return tokens;
	}

	/*************************************************************************
	 * @param t
	 * @return
	 ************************************************************************/
	private String filterBlockEnd(String t) {
		if (t.endsWith("}")) {
			t = t.substring(0, t.length()-1);
		}
		return t;
	}

	/*************************************************************************
	 * @param t
	 * @return
	 ************************************************************************/
	private String filterBlockStart(String t) {
		if (t.startsWith("{")) {
			t = t.substring(1);
		}
		return t;
	}
	
	

}

