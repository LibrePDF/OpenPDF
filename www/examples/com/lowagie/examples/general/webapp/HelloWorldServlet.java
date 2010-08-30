/*
 * $Id: HelloWorldServlet.java 3373 2008-05-12 16:21:24Z xlv $
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

package com.lowagie.examples.general.webapp;

import java.io.IOException;
import java.util.Date;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Paragraph;
import com.lowagie.text.html.HtmlWriter;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.rtf.RtfWriter2;

/**
 * Hello World example as a Servlet.
 * 
 * @author blowagie
 */
public class HelloWorldServlet extends HttpServlet {
	   
    private static final long serialVersionUID = -6033026500372479591L;

	/**
     * Returns a PDF, RTF or HTML document.
     * 
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void doGet (HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException {
        
        // we retrieve the presentationtype
        String presentationtype = request.getParameter("presentationtype");
        
        // step 1
        Document document = new Document();
        try {
            // step 2: we set the ContentType and create an instance of the corresponding Writer
            if ("pdf".equals(presentationtype)) {
                response.setContentType("application/pdf");
                PdfWriter.getInstance(document, response.getOutputStream());
            }
            else if ("html".equals(presentationtype)) {
                response.setContentType("text/html");
                HtmlWriter.getInstance(document, response.getOutputStream());
            }
            else if ("rtf".equals(presentationtype)) {
                response.setContentType("text/rtf");
                RtfWriter2.getInstance(document, response.getOutputStream());
            }
            else {
                response.sendRedirect("http://itextdocs.lowagie.com/tutorial/general/webapp/index.html#HelloWorld");
            }
            
            // step 3
            document.open();
            
            // step 4
            document.add(new Paragraph("Hello World"));
            document.add(new Paragraph(new Date().toString()));
        }
        catch(DocumentException de) {
            de.printStackTrace();
            System.err.println("document: " + de.getMessage());
        }
        
        // step 5: we close the document (the outputstream is also closed internally)
        document.close();
    }
}