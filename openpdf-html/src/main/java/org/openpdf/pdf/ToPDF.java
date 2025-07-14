/*
 * {{{ header & license
 * Copyright (c) 2006 Wisconsin Court System
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 * }}}
 */
package org.openpdf.pdf;

import org.openpdf.text.DocumentException;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;

import static java.nio.file.Files.newOutputStream;

public class ToPDF {
    public static void main(String[] args) throws IOException, DocumentException {
        if (args.length != 2) {
            System.err.println("Usage: ... [url] [pdf]");
            System.exit(1);
        }
        String url = args[0];
        if (!url.contains("://")) {
            // maybe it's a file
            File f = new File(url);
            if (f.exists()) {
                url = f.toURI().toURL().toString();
            }
        }
        createPDF(url, args[1]);
    }

    private static void createPDF(String url, String pdf) throws IOException, DocumentException {
        try (OutputStream os = newOutputStream(Paths.get(pdf))) {
            ITextRenderer renderer = ITextRenderer.fromUrl(url);
            renderer.layout();
            renderer.createPDF(os);
        }
    }
}
