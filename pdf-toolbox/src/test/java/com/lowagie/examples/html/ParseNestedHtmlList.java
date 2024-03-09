/*
 * $Id: HelloHtml.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * This code is part of the 'OpenPDF Tutorial'.
 * You can find the complete tutorial at the following address:
 * https://github.com/LibrePDF/OpenPDF/wiki/Tutorial
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 *
 */

package com.lowagie.examples.html;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.html.simpleparser.StyleSheet;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates a simple 'Hello World' HTML page.
 *
 * @author blowagie
 */

public class ParseNestedHtmlList {

    /**
     * Generates an HTML page with the text 'Hello World'
     *
     * @param args no arguments needed here
     */
    public static void main(String[] args) {
        System.out.println("Parse Nested HTML List");
        try {
            final String htmlText =
                    "<html>"
                            + "<body>"
                            + "<p>What should you say?</p>"
                            + "<ul>"
                            + "  <li>Hello</li>"
                            + "  <li>World</li>"
                            + "</ul>"
                            + "<ol>"
                            + "  <li>Element-1"
                            + "    <ol>"
                            + "      <li>Element-1-1</li>"
                            + "      <li>Element-1-2</li>"
                            + "    </ol>"
                            + "  </li>"
                            + "  <li>Element-2"
                            + "    <ol>"
                            + "      <li>Element-2-1"
                            + "        <ol>"
                            + "          <li>Element-2-1-1"
                            + "            <ol>"
                            + "              <li>Element-2-1-1-1</li>"
                            + "              <li>Element-2-1-1-2</li>"
                            + "            </ol>"
                            + "          </li>"
                            + "          <li>Element-2-1-2"
                            + "            <ol>"
                            + "              <li>Element-2-1-2-1</li>"
                            + "              <li>Element-2-1-2-2</li>"
                            + "            </ol>"
                            + "          </li>"
                            + "        </ol>"
                            + "      </li>"
                            + "      <li>Element-2-2</li>"
                            + "    </ol>"
                            + "  </li>"
                            + "</ol>"
                            + "</body>"
                            + "</html>";

            final StringReader reader = new StringReader(htmlText);
            final StyleSheet styleSheet = new StyleSheet();
            final Map<String, Object> interfaceProps = new HashMap<>();

            final List<Element> elements = HTMLWorker.parseToList(reader, styleSheet, interfaceProps);
            printElement("", elements);

        } catch (DocumentException | IOException de) {
            System.err.println(de.getMessage());
        }
    }

    private static void printElement(String depth, List<Element> elements) {
        for (Element element : elements) {
            System.out.println(depth + "- element.getClass() = " + element.getClass());
            if (element instanceof com.lowagie.text.List) {
                com.lowagie.text.List elementList = (com.lowagie.text.List) element;
                printElement(depth + "    ", elementList.getItems());
            } else {
                System.out.println(depth + "  element = " + element.getChunks().get(0).toString());
            }
        }
    }
}