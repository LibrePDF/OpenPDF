package com.lowagie.text.html;

import static org.junit.Assert.assertNotNull;

import java.io.StringReader;

import org.junit.Test;

import com.lowagie.text.Document;
import com.lowagie.text.TextElementArray;

public class HtmlParserTest {

	/**
	 * Bug fix scenario: a table with a space throws a {@link TextElementArray}
	 * class cast Exception. A table without spaces is parsed correctly.
	 */
	@Test
	public void testParse_tableWithNoSpaces() {
		Document doc1 = new Document();
		doc1.open();
		HtmlParser.parse(doc1, new StringReader("<table><tr><td>test</td></tr></table>")); // succeeds
		assertNotNull(doc1);
	}

	/**
	 * Bug fix scenario: a table with a space throws a {@link TextElementArray}
	 * class cast Exception.
	 */
	@Test
	public void testParse_tableWithSpaces() {
		Document doc1 = new Document();
		doc1.open();
		HtmlParser.parse(doc1, new StringReader("<table> <tr><td>test</td></tr></table>")); // fails
		assertNotNull(doc1);
	}

}
