/*
 * $Id: Add3D.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * Adapted from an example in C# from Christian Neuhold
 * published on the itext mailing list by Paulo Soares at 16. April 2007.
 * Changes by Paulo Soares to display without the need to click.
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
 *
 */

package com.lowagie.toolbox.plugins;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfAction;
import com.lowagie.text.pdf.PdfAnnotation;
import com.lowagie.text.pdf.PdfAppearance;
import com.lowagie.text.pdf.PdfArray;
import com.lowagie.text.pdf.PdfBoolean;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfDictionary;
import com.lowagie.text.pdf.PdfEncodings;
import com.lowagie.text.pdf.PdfFormField;
import com.lowagie.text.pdf.PdfIndirectObject;
import com.lowagie.text.pdf.PdfIndirectReference;
import com.lowagie.text.pdf.PdfName;
import com.lowagie.text.pdf.PdfNumber;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfStream;
import com.lowagie.text.pdf.PdfString;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.PushbuttonField;
import com.lowagie.toolbox.AbstractTool;
import com.lowagie.toolbox.arguments.AbstractArgument;
import com.lowagie.toolbox.arguments.FileArgument;
import com.lowagie.toolbox.arguments.StringArgument;
import com.lowagie.toolbox.arguments.filters.PdfFilter;
import com.lowagie.toolbox.arguments.filters.U3DFilter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;

/**
 * This tool lets you add a embedded u3d 3d annotation to the first page of a document. Look for sample files at
 * http://u3d.svn.sourceforge.net/viewvc/u3d/trunk/Source/Samples/Data/
 *
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class Add3D extends AbstractTool {

    public static final String PDF_NAME_3D = "3D";
    public static final String PDF_NAME_3DD = "3DD";
    public static final String PDF_NAME_3DV = "3DV";
    public static final String PDF_NAME_3DVIEW = "3DView";
    public static final String PDF_NAME_C2W = "C2W";
    public static final String PDF_NAME_IN = "IN";
    public static final String PDF_NAME_MS = "MS";
    public static final String PDF_NAME_U3D = "U3D";
    public static final String PDF_NAME_XN = "XN";

    static {
        addVersion("$Id: Add3D.java 3373 2008-05-12 16:21:24Z xlv $");
    }

    FileArgument destfile = null;

    /**
     * This tool lets you add a embedded u3d 3d annotation to the first page of a document.
     */
    public Add3D() {
        super();
        menuoptions = MENU_EXECUTE | MENU_EXECUTE_SHOW;
        FileArgument inputfile = new FileArgument(this, "srcfile",
                "The file you want to add the u3d File", false,
                new PdfFilter());
        arguments.add(inputfile);
        FileArgument u3dinputfile = new FileArgument(this, "srcu3dfile",
                "The u3d file you want to add", false,
                new U3DFilter());
        arguments.add(u3dinputfile);
        StringArgument pagenumber = new StringArgument(this, "pagenumber",
                "The pagenumber where to add the u3d annotation");
        pagenumber.setValue("1");
        arguments.add(pagenumber);
        destfile = new FileArgument(this, "destfile",
                "The file that contains the u3d annotation after processing",
                true, new PdfFilter());
        arguments.add(destfile);
        inputfile.addPropertyChangeListener(destfile);
    }

    public static void AddButton(float x, float y, String fname, String js,
            String image, PdfWriter wr) {
        try {
//            URL url=Add3D.class.getResource(
//                image);
//            PdfFileSpecification fs=PdfFileSpecification.fileEmbedded(wr,image,image,null);
//            wr.addAnnotation(PdfAnnotation.createScreen(wr,new Rectangle(x, y, x + img.plainWidth(),
//                                  y + img.plainHeight())));
            Image img = Image.getInstance(image);
            PushbuttonField bt = new PushbuttonField(wr,
                    new Rectangle(x, y, x + img.getPlainWidth(),
                            y + img.getPlainHeight()), fname);
            bt.setLayout(PushbuttonField.LAYOUT_ICON_ONLY);
            bt.setImage(img);
            PdfFormField ff = bt.getField();
            PdfAction ac = PdfAction.javaScript(js, wr);
            ff.setAction(ac);
            wr.addAnnotation(ff);
        } catch (IOException | DocumentException ignored) {
            // ignored
        }
    }

    /**
     * This methods helps you running this tool as a standalone application.
     *
     * @param args the srcfile and destfile
     */
    public static void main(String[] args) {
        Add3D add3d = new Add3D();
        if (args.length != 4) {
            System.err.println(add3d.getUsage());
        }
        add3d.setMainArguments(args);
        add3d.execute();
    }

    /**
     * Creates the internal frame.
     */
    protected void createFrame() {
        internalFrame = new JInternalFrame("Add3D", true, true, true);
        internalFrame.setSize(300, 80);
        internalFrame.setJMenuBar(getMenubar());
        System.out.println("=== Add3D OPENED ===");
    }

    /**
     * Executes the tool (in most cases this generates a PDF file).
     */
    public void execute() {
        try {
            if (getValue("srcfile") == null) {
                throw new InstantiationException(
                        "You need to choose a sourcefile");
            }
            if (getValue("srcu3dfile") == null) {
                throw new InstantiationException(
                        "You need to choose a u3d file");
            }
            if (getValue("destfile") == null) {
                throw new InstantiationException(
                        "You need to choose a destination file");
            }
            int pagenumber = Integer.parseInt((String) getValue("pagenumber"));
            // Create 3D annotation
            // Required definitions
            PdfIndirectReference streamRef;
            PdfIndirectObject objRef;
            PdfReader reader = new PdfReader(((File) getValue("srcfile"))
                    .getAbsolutePath());

            String u3dFileName = ((File) getValue("srcu3dfile"))
                    .getAbsolutePath();
            PdfStamper stamp = new PdfStamper(reader, new FileOutputStream(
                    (File) getValue("destfile")));
            PdfWriter wr = stamp.getWriter();
            PdfContentByte cb = stamp.getUnderContent(pagenumber);
            Rectangle rectori = reader.getCropBox(pagenumber);
            Rectangle rect = new Rectangle(new Rectangle(100,
                    rectori.getHeight() - 550, rectori.getWidth() - 100,
                    rectori.getHeight() - 150));
            PdfStream oni = new PdfStream(PdfEncodings.convertToBytes(
                    "runtime.setCurrentTool(\"Rotate\");", null));
            oni.flateCompress();

// Create stream to carry attachment
            PdfStream stream = new PdfStream(new FileInputStream(u3dFileName),
                    wr);
            stream.put(new PdfName("OnInstantiate"),
                    wr.addToBody(oni).getIndirectReference());
            stream.put(PdfName.TYPE, new PdfName(PDF_NAME_3D)); // Mandatory keys
            stream.put(PdfName.SUBTYPE, new PdfName(PDF_NAME_U3D));
            stream.flateCompress();

            streamRef = wr.addToBody(stream)
                    .getIndirectReference(); // Write stream contents, get reference to stream object, write actual stream length
            stream.writeLength();

            // Create 3D view dictionary
            // PDF documentation states that this can be left out, but without normally we will just get a blank 3D
            // image because of wrong coordinate space transformations, etc. Instead of providing camera-to-world
            // transformation here, we could also reference view in U3D file itself (would be U3DPath key instead of
            // C2W key, U3D value instead of M value for MS key), but i haven't tried up to now We could also provide
            // an activation dictionary (defining activation behavior), and field-of-view for P entry if needed
            PdfDictionary dict = new PdfDictionary(new PdfName(PDF_NAME_3DVIEW));

            dict.put(new PdfName(PDF_NAME_XN), new PdfString("Default"));
            dict.put(new PdfName(PDF_NAME_IN), new PdfString("Unnamed"));
            dict.put(new PdfName(PDF_NAME_MS),
                    PdfName.M); // States that we have to provide camera-to-world coordinate transformation
            dict.put(new PdfName(PDF_NAME_C2W),
                    new PdfArray(new float[]{1, 0, 0, 0, 0, -1, 0, 1, 0, 3,
                            -235, 28F})); // 3d transformation matrix (demo for teapot)
            dict.put(PdfName.CO, new PdfNumber(235)); // Camera distance along z-axis (demo for teapot)

            objRef = wr.addToBody(dict); // Write view dictionary, get reference

            // Create appearance
            PdfAppearance ap = cb.createAppearance(rect.getRight() - rect.getLeft(),
                    rect.getTop() - rect.getBottom());

            ap.setBoundingBox(rect);

// Create annotation with reference to stream
            PdfAnnotation annot = new PdfAnnotation(wr, rect);

            annot.put(PdfName.CONTENTS, new PdfString("3D Model"));
            annot.put(PdfName.SUBTYPE, new PdfName(PDF_NAME_3D)); // Mandatory keys
            annot.put(PdfName.TYPE, PdfName.ANNOT);
            annot.put(new PdfName(PDF_NAME_3DD), streamRef); // Reference to stream object
            annot.put(new PdfName(PDF_NAME_3DV), objRef.getIndirectReference()); // Reference to view dictionary object
            annot.put(new PdfName("3DI"), PdfBoolean.PDFFALSE);

            PdfDictionary adi = new PdfDictionary();
            adi.put(PdfName.A, new PdfName("PO"));
            adi.put(new PdfName("DIS"), PdfName.I);
            annot.put(new PdfName("3DA"), adi);

            annot.setAppearance(PdfAnnotation.APPEARANCE_NORMAL, ap); // Assign appearance and page
            annot.setPage();

            // Actually write annotation
            stamp.addAnnotation(annot, pagenumber);
            AddButton(100, 100, "Rotate",
                    "im = this.getAnnots3D(0)[0].context3D;\rim.runtime.setCurrentTool(\"Rotate\");",
                    "rotate.png", wr);
            AddButton(150, 100, "Pan",
                    "im = this.getAnnots3D(0)[0].context3D;\rim.runtime.setCurrentTool(\"Pan\");",
                    "translate.png", wr);
            AddButton(200, 100, "Zoom",
                    "im = this.getAnnots3D(0)[0].context3D;\rim.runtime.setCurrentTool(\"Zoom\");",
                    "zoom.png", wr);
            stamp.close();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(internalFrame, e.getMessage(), e
                            .getClass().getName(),
                    JOptionPane.ERROR_MESSAGE);
            System.err.println(e.getMessage());
        }
    }

    /**
     * Gets the PDF file that should be generated (or null if the output isn't a PDF file).
     *
     * @return the PDF file that should be generated
     * @throws InstantiationException on error
     */
    protected File getDestPathPDF() throws InstantiationException {
        return (File) getValue("destfile");
    }

    /**
     * Indicates that the value of an argument has changed.
     *
     * @param arg the argument that has changed
     */
    public void valueHasChanged(AbstractArgument arg) {
        if (internalFrame == null) {
            // if the internal frame is null, the tool was called from the
            // command line
            return;
        }
        if (destfile.getValue() == null &&
                arg.getName().equalsIgnoreCase("srcfile")) {
            String filename = arg.getValue().toString();
            String filenameout = filename.substring(0, filename.indexOf(".",
                    filename.length() - 4)) + "_out.pdf";
            destfile.setValue(filenameout);
        }
    }
}
