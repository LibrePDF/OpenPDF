/*
 * $Id: OutSimplePdf.java 3373 2008-05-12 16:21:24Z xlv $
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

package com.lowagie.examples.general.webapp;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;

/**
 * Hello World example as a Servlet.
 */
public class OutSimplePdf extends HttpServlet {

    private static final long serialVersionUID = 2788260006560387781L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        makePdf(request, response, "GET");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        makePdf(request, response, "POST");
    }

    /**
     * Performs the action: generate a PDF from a GET or POST.
     *
     * @param request       the Servlets request object
     * @param response      the Servlets request object
     * @param methodGetPost the method that was used in the form
     */
    public void makePdf(HttpServletRequest request, HttpServletResponse response, String methodGetPost) {
        try {

            // take the message from the URL or create default message
            String msg = request.getParameter("msg");
            if (msg == null || msg.trim().length() <= 0) {
                msg = "[ specify a message in the 'msg' argument on the URL ]";
            }

            // create simple doc and write to a ByteArrayOutputStream
            Document document = new Document();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, baos);
            document.open();
            document.add(new Paragraph(msg));
            document.add(Chunk.NEWLINE);
            document.add(new Paragraph("The method used to generate this PDF was: " + methodGetPost));
            document.close();

            // setting some response headers
            response.setHeader("Expires", "0");
            response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
            response.setHeader("Pragma", "public");
            // setting the content type
            response.setContentType("application/pdf");
            // the contentlength is needed for MSIE!!!
            response.setContentLength(baos.size());
            // write ByteArrayOutputStream to the ServletOutputStream
            ServletOutputStream out = response.getOutputStream();
            baos.writeTo(out);
            out.flush();

        } catch (Exception e2) {
            System.out.println("Error in " + getClass().getName() + "\n" + e2);
        }
    }
}