/*
 * $Id: Normalize.java 3736 2009-02-26 08:52:21Z xlv $
 *
 * Copyright 2005 by Carsten Hammer.
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package com.lowagie.toolbox.plugins;

import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfArray;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfIndirectReference;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfNumber;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.toolbox.AbstractTool;
import com.lowagie.toolbox.arguments.AbstractArgument;
import com.lowagie.toolbox.arguments.FileArgument;
import com.lowagie.toolbox.arguments.filters.PdfFilter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import javax.swing.JInternalFrame;

/**
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class Normalize
        extends AbstractTool {

    static {
        addVersion("$Id: Normalize.java 3736 2009-02-26 08:52:21Z xlv $");
    }

    FileArgument destfile = null;
    int pagecount;
    float width;
    float height;
    PdfDictionary lastpage = null;
    float tolerancex = 60;
    float tolerancey = 60;
    int pagecountinsertedpages;
    int pagecountrotatedpages;

    /**
     * Constructs a Burst object.
     */
    public Normalize() {
        menuoptions = MENU_EXECUTE | MENU_EXECUTE_SHOW;
        FileArgument inputfile = new FileArgument(this, "srcfile",
                "The file you want to normalize", false,
                new PdfFilter());
        arguments.add(inputfile);
        destfile = new FileArgument(this, "destfile", "The resulting PDF", true,
                new PdfFilter());
        arguments.add(destfile);
        inputfile.addPropertyChangeListener(destfile);
    }

    /**
     * Normalize PDF file.
     *
     * @param args String[]
     */
    public static void main(String[] args) {
        Normalize tool = new Normalize();
        if (args.length < 2) {
            System.err.println(tool.getUsage());
        }
        tool.setMainArguments(args);
        tool.execute();
    }

    /**
     * @see com.lowagie.toolbox.AbstractTool#createFrame()
     */
    protected void createFrame() {
        internalFrame = new JInternalFrame("Normalize", true, false, true);
        internalFrame.setSize(300, 80);
        internalFrame.setJMenuBar(getMenubar());
        System.out.println("=== Normalize OPENED ===");
    }

    protected void iteratePages(PdfDictionary page, PdfReader pdfreader,
            ArrayList<PdfDictionary> pageInh,
            int count_in_leaf, PdfWriter writer) throws
            IOException {
        float curwidth;
        float curheight;
        PdfArray kidsPR = page.getAsArray(PdfName.KIDS);

        if (kidsPR == null) {
            PdfArray arr = page.getAsArray(PdfName.MEDIABOX);
            curwidth = Float.parseFloat(arr.getPdfObject(2).toString());
            curheight = Float.parseFloat(arr.getPdfObject(3).toString());

            PdfNumber rotation = page.getAsNumber(PdfName.ROTATE);

            if (rotation == null) {
                System.out.println("optional rotation missing");
                rotation = new PdfNumber(0);
            }

            Ausrichtung ausr = new Ausrichtung(rotation.floatValue(),
                    new Rectangle(curwidth, curheight));

            switch (ausr.type) {
                case Ausrichtung.A4Landscape:
                case Ausrichtung.A3Portrait:
                    ausr.rotate();
                    page.put(PdfName.ROTATE, new PdfNumber(ausr.getRotation()));
                    System.out.println("rotate page:" + (pagecount + 1) + " targetformat: " +
                            ausr);
                    this.pagecountrotatedpages++;

                    break;
            }

            curwidth = ausr.getM5();
            curheight = ausr.getM6();

            if (((pagecount + 1) % 2) == 0) {
                if ((Math.abs(curwidth - width) > tolerancex) ||
                        (Math.abs(curheight - height) > tolerancey)) {
                    Seitehinzufuegen(page, count_in_leaf, writer, arr);
                    this.pagecountinsertedpages++;
                }
            }

            /**
             * Bei ungeraden Seiten die Seitenabmessungen speichern
             */
            if (((pagecount + 1) % 2) == 1) {
                width = curwidth;
                height = curheight;
                lastpage = page;
            }

            pageInh.add(pagecount, page);
            pagecount++;
        } else {
            page.put(PdfName.TYPE, PdfName.PAGES);

            for (int k = 0; k < kidsPR.size(); ++k) {
                PdfDictionary kid = kidsPR.getAsDict(k);
                iteratePages(kid, pdfreader, pageInh, k, writer);
            }
        }
    }

    private void Seitehinzufuegen(PdfDictionary page, int count_in_leaf,
            PdfWriter writer,
            PdfArray array) throws IOException {
        System.out.print("change!");

        PdfDictionary parent = page.getAsDict(PdfName.PARENT);
        PdfArray kids = parent.getAsArray(PdfName.KIDS);
        PdfIndirectReference ref = writer.getPdfIndirectReference();
        kids.add(count_in_leaf, ref);

        PdfDictionary newPage = new PdfDictionary(PdfName.PAGE);
        newPage.merge(lastpage);
        newPage.remove(PdfName.CONTENTS);
        newPage.remove(PdfName.ANNOTS);
        newPage.put(PdfName.RESOURCES, new PdfDictionary());
        writer.addToBody(newPage, ref);

        PdfNumber count = null;

        while (parent != null) {
            count = parent.getAsNumber(PdfName.COUNT);

            parent.put(PdfName.COUNT, new PdfNumber(count.intValue() + 1));
            parent = parent.getAsDict(PdfName.PARENT);
        }

        System.out.println("page:" + (pagecount + 1) + " nr in leaf:" +
                count_in_leaf + " arl x:" +
                array.getPdfObject(0) + " y:" + array.getPdfObject(1) + " width:" + array.getPdfObject(2) +
                " height:" + array.getPdfObject(3));
    }

    /**
     * @see com.lowagie.toolbox.AbstractTool#execute()
     */
    public void execute() {
        try {
            if (getValue("srcfile") == null) {
                throw new InstantiationException("You need to choose a sourcefile");
            }
            File src = (File) getValue("srcfile");
            if (getValue("destfile") == null) {
                throw new InstantiationException(
                        "You need to choose a destination file");
            }
            File dest = (File) getValue("destfile");

            pagecountinsertedpages = 0;
            pagecountrotatedpages = 0;
            pagecount = 0;
            PdfReader reader = new PdfReader(src.getAbsolutePath());
            PdfStamper stp = new PdfStamper(reader, new FileOutputStream(dest));

            PdfWriter writer = stp.getWriter();

            ArrayList<PdfDictionary> pageInh = new ArrayList<>();
            PdfDictionary catalog = reader.getCatalog();
            PdfDictionary rootPages = catalog.getAsDict(PdfName.PAGES);
            iteratePages(rootPages, reader, pageInh, 0, writer);

            if (((pagecount) % 2) == 1) {
                appendemptypageatend(reader, writer);
                this.pagecountinsertedpages++;
            }

            stp.close();
            System.out.println("In " + dest.getAbsolutePath() + " pages= " +
                    pagecount +
                    " inserted pages=" + this.getPagecountinsertedpages() +
                    " rotated pages=" +
                    this.getPagecountrotatedpages());
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }

    private void appendemptypageatend(PdfReader reader, PdfWriter writer) throws
            IOException {
        System.out.println("last page odd. add page!");

        PdfDictionary page = reader.getPageN(reader.getNumberOfPages());
        PdfDictionary parent = page.getAsDict(PdfName.PARENT);
        PdfArray kids = parent.getAsArray(PdfName.KIDS);
        PdfIndirectReference ref = writer.getPdfIndirectReference();
        kids.add(ref);

        PdfDictionary newPage = new PdfDictionary(PdfName.PAGE);
        newPage.merge(lastpage);
        newPage.remove(PdfName.CONTENTS);
        newPage.remove(PdfName.ANNOTS);
        newPage.put(PdfName.RESOURCES, new PdfDictionary());
        writer.addToBody(newPage, ref);

        PdfNumber count = null;

        while (parent != null) {
            count = parent.getAsNumber(PdfName.COUNT);
            parent.put(PdfName.COUNT, new PdfNumber(count.intValue() + 1));
            parent = parent.getAsDict(PdfName.PARENT);
        }
    }

    public int getPagecountinsertedpages() {
        return pagecountinsertedpages;
    }

    public int getPagecountrotatedpages() {
        return pagecountrotatedpages;
    }

    /**
     * @param arg StringArgument
     * @see com.lowagie.toolbox.AbstractTool#valueHasChanged(com.lowagie.toolbox.arguments.AbstractArgument)
     */
    public void valueHasChanged(AbstractArgument arg) {
        if (internalFrame == null) {
            // if the internal frame is null, the tool was called from the command line
            return;
        }
        // represent the changes of the argument in the internal frame
        if (destfile.getValue() == null && arg.getName().equalsIgnoreCase("srcfile")) {
            String filename = arg.getValue().toString();
            String filenameout = filename.substring(0, filename.indexOf(".",
                    filename.length() - 4)) + "_out.pdf";
            destfile.setValue(filenameout);
        }
    }

    /**
     * @return File
     * @throws InstantiationException on error
     * @see com.lowagie.toolbox.AbstractTool#getDestPathPDF()
     */
    protected File getDestPathPDF() throws InstantiationException {
        return (File) getValue("destfile");
    }

    public class Ausrichtung {

        static final float tolerance = 60;
        static final int UNKNOWN = 0;
        static final int A4Portrait = 1;
        static final int A4Landscape = 2;
        static final int A3Portrait = 3;
        static final int A3Landscape = 4;
        float rotation;
        Rectangle rect;
        float m5;
        float m6;
        int type;

        public Ausrichtung() {
            this(0, new Rectangle(1, 1));
        }

        public Ausrichtung(float rotation, Rectangle unrotatedoriginalrect) {
            this.rotation = rotation;
            if ((rotation == 90) || (rotation == 270)) {
                rect = unrotatedoriginalrect.rotate();
            } else {
                rect = unrotatedoriginalrect;
            }

            m5 = rect.getWidth();
            m6 = rect.getHeight();
            klassifiziere();

        }

        private void klassifiziere() {
            if (Math.abs(rect.getWidth() - 595) < tolerance &&
                    Math.abs(rect.getHeight() - 842) < tolerance) {
                this.type = A4Portrait;
            } else if (Math.abs(rect.getWidth() - 842) < tolerance &&
                    Math.abs(rect.getHeight() - 595) < tolerance) {
                this.type = A4Landscape;
            } else if (Math.abs(rect.getWidth() - 1190) < tolerance &&
                    Math.abs(rect.getHeight() - 842) < tolerance) {
                this.type = A3Landscape;
            } else if (Math.abs(rect.getWidth() - 842) < tolerance &&
                    Math.abs(rect.getHeight() - 1190) < tolerance) {
                this.type = A3Portrait;
            } else {
                type = UNKNOWN;
            }
        }

        public float getM5() {
            return m5;
        }

        public float getM6() {
            return m6;
        }

        public String toString() {
            String back;
            switch (type) {
                case UNKNOWN:
                    back = rect.getWidth() + "*" + rect.getHeight();
                    break;
                case A3Landscape:
                    back = "A3 Landscape";
                    break;
                case A3Portrait:
                    back = "A3 Portrait";
                    break;
                case A4Landscape:
                    back = "A4 Landscape";
                    break;
                case A4Portrait:
                    back = "A4 Portrait";
                    break;
                default:
                    back = "";
            }
            return back;
        }

        public void rotate() {
            rect = rect.rotate();
            m5 = rect.getWidth();
            m6 = rect.getHeight();
            rotation += 90;
            rotation = rotation % 360;
            klassifiziere();
        }

        public float getRotation() {
            return rotation;
        }
    }

}
