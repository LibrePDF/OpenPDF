/*
 * $Id: SilentPrintServlet.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * This code is part of the 'OpenPDF Tutorial'.
 * You can find the complete tutorial at the following address:
 * https://github.com/LibrePDF/OpenPDF/wiki/Tutorialv
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
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Explains how to print silently via Servlet/Browser.
 *
 * @author Heiner Jostkleigrewe, Heiner.Jostkleigrewe@gt-net.de
 */
public class SilentPrintServlet extends HttpServlet {

    /**
     * a possible status
     */
    public static final int ACT_INIT = 0;
    /**
     * a possible status
     */
    public static final int ACT_REPORT_1 = 1;
    private static final long serialVersionUID = -3250788071256174348L;

    @Override
    public void doGet(HttpServletRequest requ, HttpServletResponse resp)
            throws IOException {
        doWork(requ, resp);
    }

    @Override
    public void doPost(HttpServletRequest requ, HttpServletResponse resp)
            throws IOException {
        doWork(requ, resp);
    }

    /**
     * The actual business logic.
     *
     * @param requ the request object
     * @param resp the response object
     * @throws IOException on error
     */
    public void doWork(HttpServletRequest requ, HttpServletResponse resp)
            throws IOException {
        ServletOutputStream out = resp.getOutputStream();

        // what did the user request?
        int action = ACT_INIT;
        int sub = ACT_INIT;
        try {
            action = Integer.parseInt(requ.getParameter("action"));
            sub = Integer.parseInt(requ.getParameter("sub"));
        } catch (Exception ignored) {
        }

        switch (action) {
            case ACT_INIT: {
                htmlHeader(out, resp);
                formular(out, requ, sub);
                break;
            }

            case ACT_REPORT_1: {
                Document document = new Document();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();

                try {
                    PdfWriter writer = PdfWriter.getInstance(document, baos);
                    document.open();
                    if (requ.getParameter("preview") == null) {
                        writer.addJavaScript("this.print(false);", false);
                    }
                    document.add(new Chunk("Silent Auto Print"));
                    document.close();
                } catch (DocumentException e) {
                    e.printStackTrace();
                }
                resp.setContentType("application/pdf");

                resp.setContentLength(baos.size());
                baos.writeTo(out);
                out.flush();
                break;
            }
        }
    }

    private void htmlHeader(ServletOutputStream out,
            HttpServletResponse resp) throws IOException {

        resp.setContentType("text/html; charset=ISO-8859-1");
        resp.setHeader("Cache-Control", "no-cache");
        out.println("<html>");
        out.println("<head>");
        out
                .println("<meta http-equiv='Content-Type' content='text/html;charset=iso-8859-1'>");
        out.println("<meta http-equiv='expires' content='0'>");
        out.println("<meta http-equiv='cache-control' content='no-cache'>");
        out.println("<meta http-equiv='pragma' content='no-cache'>");
        out.println("</head>");
        out.println("<body>");
    }

    private void formular(ServletOutputStream out, HttpServletRequest requ,
            int sub) throws IOException {
        out.print("<form method='post' action='");
        out.print(requ.getRequestURI());
        out.print("?action=");
        out.print(ACT_INIT);
        out.print("&sub=");
        out.print(ACT_REPORT_1);
        out.println("'>");
        out.print("<input type='checkbox' name='preview' value='Y'");
        if (requ.getParameter("preview") != null) {
            out.print(" checked ");
        }
        out.println(">preview<br>");

        out.println("<input type=submit value='Report 1'>");
        out.println("</form>");
        if (sub != ACT_INIT) {
            if (requ.getParameter("preview") != null) {
                out.println("<script language='JavaScript'>");
                out.print("w = window.open(\"");
                out.print(requ.getRequestURI());
                out.print("?action=");
                out.print(sub);
                out
                        .print("&preview=Y\", \"Printing\", \"width=800,height=450,scrollbars,menubar=yes,resizable=yes\");");
                out.println("</script>");
            } else {
                out.print("<iframe src='");
                out.print(requ.getRequestURI());
                out.print("?action=");
                out.print(sub);
                out.println("' width='10' height='10' name='pdf_box'>");
            }
        }
        out.println("</body>");
        out.println("</html>");
    }
}