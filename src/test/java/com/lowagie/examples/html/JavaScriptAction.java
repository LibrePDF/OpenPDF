/*
 * $Id: JavaScriptAction.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * This code is part of the 'iText Tutorial'.
 * You can find the complete tutorial at the following address:
 * http://itextdocs.lowagie.com/tutorial/
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * itext-questions@lists.sourceforge.net
 */

package com.lowagie.examples.html;

import java.io.FileOutputStream;

import com.lowagie.text.Anchor;
import com.lowagie.text.Document;
import com.lowagie.text.Header;
import com.lowagie.text.Phrase;
import com.lowagie.text.html.HtmlTags;
import com.lowagie.text.html.HtmlWriter;

/**
 * Creates a documents with different named actions.
 * 
 * @author blowagie
 */

public class JavaScriptAction {

	/**
	 * Creates a document with Named Actions.
	 * 
	 * @param args
	 *            no arguments needed
	 */
	public static void main(String[] args) {

		System.out.println("Open Application");

		// step 1: creation of a document-object
		Document document = new Document();

		try {

			// step 2:
			HtmlWriter.getInstance(document, new FileOutputStream(
					"JavaScriptAction.html"));
			// step 3: we add Javascript as Metadata and we open the document
			
			StringBuffer javaScriptSection = new StringBuffer();
            javaScriptSection.append("\t\tfunction load() {\n");
            javaScriptSection.append("\t\t  alert('Page has been loaded.');\n");
            javaScriptSection.append("\t\t}\n");
          
            javaScriptSection.append("\t\tfunction unload(){\n");
            javaScriptSection.append("\t\t  alert('Page has been unloaded.');\n");
            javaScriptSection.append("\t\t}\n");

            javaScriptSection.append("\t\tfunction sayHi(){\n");
            javaScriptSection.append("\t\t  alert('Hi !!!');\n");
            javaScriptSection.append("\t\t}");

            document.add(new Header(HtmlTags.JAVASCRIPT, javaScriptSection.toString()));
            document.setJavaScript_onLoad  ("load()");
            document.setJavaScript_onUnLoad("unload()");
            
			document.open();
			// step 4: we add some content
			Phrase phrase1 = new Phrase("There are 3 JavaScript functions in the HTML page, load(), unload() and sayHi().\n\n" +
                                   "The first one will be called when the HTML page has been loaded by your browser.\n" +
                                   "The second one will be called when the HTML page is being unloaded,\n" +
                                   "for example when you go to another page.\n");
              document.add(phrase1);

            // add a HTML link <A HREF="...">
              Anchor anchor = new Anchor("Click here to execute the third JavaScript function.");
              anchor.setReference("JavaScript:sayHi()");
              document.add(anchor);

		} catch (Exception de) {
			de.printStackTrace();
		}

		// step 5: we close the document
		document.close();

	}
}