/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Joshua Marinacci
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.	See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.openpdf.test;

import org.w3c.dom.Document;
import org.openpdf.render.Box;
import org.openpdf.simple.Graphics2DRenderer;
import org.openpdf.util.Uu;
import org.openpdf.util.XMLUtil;
import org.openpdf.util.XRLog;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.logging.Level;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.openpdf.util.ImageUtil.withGraphics;

public class DocumentDiffTest {

    public static void generateTestFile(String test, String diff, int width, int height) throws Exception {
        Uu.p("test = " + test);
        String out = xhtmlToDiff(test, width, height);
        string_to_file(out, new File(diff));
    }

    public static String xhtmlToDiff(String xhtml, int width, int height) throws Exception {
        Document doc = XMLUtil.documentFromFile(xhtml);
        Graphics2DRenderer renderer = new Graphics2DRenderer(doc, new File(xhtml).toURI().toURL().toString());

        BufferedImage buff = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
        withGraphics(buff, g -> {
            Dimension dim = new Dimension(width, height);
            renderer.layout(g, dim);
            renderer.render(g);
        });

        getDiff(renderer.getPanel().getRootBox(), "");
        return "";
    }

    public boolean compareTestFile(String test, String diff, int width, int height) throws Exception {
        String tin = xhtmlToDiff(test, width, height);
        String din;
        try {
            din = file_to_string(diff);
        } catch (FileNotFoundException ex) {
            XRLog.log("unittests", Level.WARNING, "diff file missing");
            return false;
        }
        if (tin.equals(din)) {
            return true;
        }
        XRLog.log("unittests", Level.WARNING, "warning not equals");
        File dfile = new File("correct.diff");
        File tfile = new File("test.diff");
        XRLog.log("unittests", Level.WARNING, "writing to " + dfile + " and " + tfile);
        string_to_file(tin, tfile);
        string_to_file(din, dfile);
        return false;
    }

    /**
     * Gets the diff attribute of the DocumentDiffTest object
     */
    private static void getDiff(Box box, String tab) {
        for (int i = 0; i < box.getChildCount(); i++) {
            getDiff(box.getChild(i), tab + " ");
        }
    }

    private static String file_to_string(String filename) throws IOException {
        File file = new File(filename);
        return file_to_string(file);
    }

    private static String file_to_string(File file) throws IOException {
        try (FileReader reader = new FileReader(file, UTF_8)) {
            try (StringWriter writer = new StringWriter()) {
                char[] buf = new char[1000];
                while (true) {
                    int n = reader.read(buf, 0, 1000);
                    if (n == -1) {
                        break;
                    }
                    writer.write(buf, 0, n);
                }
                return writer.toString();
            }
        }
    }

    public static void string_to_file(String text, File file)
            throws IOException {
        try (FileWriter writer = new FileWriter(file, UTF_8)) {
            try (StringReader reader = new StringReader(text)) {
                char[] buf = new char[1000];
                while (true) {
                    int n = reader.read(buf, 0, 1000);
                    if (n == -1) {
                        break;
                    }
                    writer.write(buf, 0, n);
                }
                writer.flush();
            }
        }
    }
}
