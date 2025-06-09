package org.openpdf.simple;

import org.jspecify.annotations.Nullable;
import org.openpdf.util.Uu;

import java.awt.*;
import java.awt.print.PageFormat;
import java.awt.print.Printable;

/**
 * <p>
 *     XHTMLPrintable allows you to print XHTML content to a printer instead of
 *     rendering it to screen.
 * </p>
 *
 * <p>
 *   It is an implementation of {@link java.awt.print.Printable},
 *   so you can use it whenever you would use any other Printable object. The constructor
 *   requires an {@link XHTMLPanel}, so it's easiest to prepare an {@link XHTMLPanel} instance as normal, and then
 *   wrap a printable around it.
 * </p>
 *
 * <p>
 *   For example:
 * <pre>{@code
 *   import org.openpdf.simple.*;
 *   import java.awt.print.*;
 *   // . . . .
 *   // xhtml_panel created earlier
 *
 *   PrinterJob printJob = PrinterJob.getPrinterJob();
 *   printJob.setPrintable(new XHTMLPrintable(xhtml_panel));
 *
 *   if (printJob.printDialog()) {
 *     printJob.print();
 *   }
 * }</pre>
 */

public class XHTMLPrintable implements Printable {
    private final XHTMLPanel panel;

    @Nullable
    private Graphics2DRenderer g2r;


    /**
     * Creates a new XHTMLPrintable that will print
     * the current contents of the passed in XHTMLPanel.
     *
     * @param panel the XHTMLPanel to print
     */
    public XHTMLPrintable(XHTMLPanel panel) {
        this.panel = panel;
    }


    /**
     * <p>The implementation of the <i>print</i> method
     * from the @see java.awt.print.Printable interface.
     */
    @Override
    public int print(Graphics g, PageFormat pf, int page) {
        try {
            Graphics2D g2 = (Graphics2D) g;

            if (g2r == null) {
                g2r = new Graphics2DRenderer(panel.getDocument(), panel.getSharedContext().getUac().getBaseURL());
                g2r.getSharedContext().setPrint(true);
                g2r.getSharedContext().setInteractive(false);
                g2r.getSharedContext().setDPI(72f);
                g2r.getSharedContext().getTextRenderer().setSmoothingThreshold(0);
                g2r.getSharedContext().setUserAgentCallback(panel.getSharedContext().getUserAgentCallback());
                g2r.getSharedContext().setReplacedElementFactory(panel.getSharedContext().getReplacedElementFactory());
                g2r.layout(g2, null);
                g2r.getPanel().assignPagePrintPositions(g2);
            }

            if (page >= g2r.getPanel().getRootLayer().getPages().size()) {
                return Printable.NO_SUCH_PAGE;
            }

            // render the document
            g2r.getPanel().paintPage(g2, page);

            return Printable.PAGE_EXISTS;
        } catch (Exception ex) {
            Uu.p(ex);
            return Printable.NO_SUCH_PAGE;
        }
    }
}

