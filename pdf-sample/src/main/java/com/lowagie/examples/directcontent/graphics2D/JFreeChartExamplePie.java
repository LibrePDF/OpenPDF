/*
 * $Id: JFreeChartExample.java 3838 2009-04-07 18:34:15Z mstorer $
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
package com.lowagie.examples.directcontent.graphics2D;

import com.lowagie.examples.AbstractSample;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.DefaultFontMapper;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

/**
 * JFreeChart example.
 */
public class JFreeChartExamplePie extends AbstractSample {

    @Override
    public String getFileName() {
        return "/jfreechart_pie";
    }

    public static void main(String[] args) {
        JFreeChartExamplePie templates = new JFreeChartExamplePie();
        templates.run(args);
    }

    /**
     * @param path
     */
    public void render(String path) {

        System.out.println("DirectContent :: Graphics2D :: JFreeChart example");
        convertToPdf(getPieChart(), 400, 600, path + getFileName() + ".pdf");
    }

    /**
     * Converts a JFreeChart to PDF syntax.
     *
     * @param filename the name of the PDF file
     * @param chart    the JFreeChart
     * @param width    the width of the resulting PDF
     * @param height   the height of the resulting PDF
     */
    private static void convertToPdf(JFreeChart chart, int width, int height, String filename) {
        // tag::generation[]
        // step 1
        try (Document document = new Document(new Rectangle(width, height))) {
            // step 2
            PdfWriter writer;
            writer = PdfWriter.getInstance(document, new FileOutputStream(filename));
            // step 3
            document.open();
            // step 4
            PdfContentByte cb = writer.getDirectContent();
            PdfTemplate tp = cb.createTemplate(width, height);
            Graphics2D g2d = tp.createGraphics(width, height, new DefaultFontMapper());
            Rectangle2D r2d = new Rectangle2D.Double(0, 0, width, height);
            chart.draw(g2d, r2d);
            g2d.dispose();
            tp.sanityCheck();
            cb.addTemplate(tp, 0, 0);
            cb.sanityCheck();
        } catch (DocumentException | FileNotFoundException de) {
            de.printStackTrace();
        }
        // end::generation[]
    }

    /**
     * Gets an example piechart.
     *
     * @return a piechart
     */
    public static JFreeChart getPieChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        dataset.setValue("iText", 60);
        dataset.setValue("cinema.lowagie.com", 10);
        dataset.setValue("tutorial", 30);
        return ChartFactory.createPieChart(
                "Website popularity",
                dataset,
                true,
                true,
                false);
    }
}
