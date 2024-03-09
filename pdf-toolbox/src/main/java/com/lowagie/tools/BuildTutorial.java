/*
 * $Id: BuildTutorial.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * Copyright 2005 by Bruno Lowagie.
 *
 * The contents of this file are subject to the Mozilla Public License Version 1.1
 * (the "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the License.
 *
 * The Original Code is 'iText, a free JAVA-PDF library'.
 *
 * The Initial Developer of the Original Code is Bruno Lowagie. Portions created by
 * the Initial Developer are Copyright (C) 1999, 2000, 2001, 2002 by Bruno Lowagie.
 * All Rights Reserved.
 * Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
 * are Copyright (C) 2000, 2001, 2002 by Paulo Soares. All Rights Reserved.
 *
 * Contributor(s): all the names of the contributors are added in the source code
 * where applicable.
 *
 * Alternatively, the contents of this file may be used under the terms of the
 * LGPL license (the "GNU LIBRARY GENERAL PUBLIC LICENSE"), in which case the
 * provisions of LGPL are applicable instead of those above.  If you wish to
 * allow use of your version of this file only under the terms of the LGPL
 * License and not to allow others to use your version of this file under
 * the MPL, indicate your decision by deleting the provisions above and
 * replace them with the notice and other provisions required by the LGPL.
 * If you do not delete the provisions above, a recipient may use your version
 * of this file under either the MPL or the GNU LIBRARY GENERAL PUBLIC LICENSE.
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the MPL as stated above or under the terms of the GNU
 * Library General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Library general Public License for more
 * details.
 *
 * If you didn't download this code from the following link, you should check if
 * you aren't using an obsolete version:
 * https://github.com/LibrePDF/OpenPDF
 */
package com.lowagie.tools;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import javax.xml.XMLConstants;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * This class can be used to build the iText website.
 *
 * @author Bruno Lowagie
 */
public class BuildTutorial {

    static String root;
    static FileWriter build;

    //~ Methods
    // ----------------------------------------------------------------

    /**
     * Main method so you can call the convert method from the command line.
     *
     * @param args 4 arguments are expected:
     *             <ul><li>a sourcedirectory (root of the tutorial xml-files),
     *             <li>a destination directory (where the html and build.xml files will be generated),
     *             <li>an xsl to transform the index.xml into a build.xml
     *             <li>an xsl to transform the index.xml into am index.html</ul>
     */

    public static void main(String[] args) {
        if (args.length == 4) {
            File srcdir = new File(args[0]);
            File destdir = new File(args[1]);
            File xsl_examples = new File(srcdir, args[2]);
            File xsl_site = new File(srcdir, args[3]);
            try {
                System.out.print("Building tutorial: ");
                root = new File(args[1], srcdir.getName()).getCanonicalPath();
                System.out.println(root);
                build = new FileWriter(new File(root, "build.xml"));
                build.write("<project name=\"tutorial\" default=\"all\" basedir=\".\">\n");
                build.write("<target name=\"all\">\n");
                action(srcdir, destdir, xsl_examples, xsl_site);
                build.write("</target>\n</project>");
                build.flush();
                build.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        } else {
            System.err
                    .println("Wrong number of parameters.\nUsage: BuildSite srcdr destdir xsl_examples xsl_site");
        }
    }

    /**
     * Inspects a file or directory that is given and performs the necessary actions on it (transformation or
     * recursion).
     *
     * @param source       a sourcedirectory (possibly with a tutorial xml-file)
     * @param destination  a destination directory (where the html and build.xml file will be generated, if necessary)
     * @param xsl_examples an xsl to transform the index.xml into a build.xml
     * @param xsl_site     an xsl to transform the index.xml into am index.html
     * @throws IOException when something goes wrong while reading or creating a file or directory
     */
    public static void action(File source, File destination, File xsl_examples, File xsl_site) throws IOException {
        if (".svn".equals(source.getName())) {
            return;
        }
        System.out.print(source.getName());
        if (source.isDirectory()) {
            System.out.print(" ");
            System.out.println(source.getCanonicalPath());
            File dest = new File(destination, source.getName());
            dest.mkdir();
            File current;
            File[] xmlFiles = source.listFiles();
            if (xmlFiles != null) {
                for (File xmlFile : xmlFiles) {
                    current = xmlFile;
                    action(current, dest, xsl_examples, xsl_site);
                }
            } else {
                System.out.println("... skipped");
            }
        } else if (source.getName().equals("index.xml")) {
            System.out.println("... transformed");
            convert(source, xsl_site, new File(destination, "index.php"));
            File buildfile = new File(destination, "build.xml");
            String path = buildfile.getCanonicalPath().substring(root.length());
            path = path.replace(File.separatorChar, '/');
            if ("/build.xml".equals(path)) {
                return;
            }
            convert(source, xsl_examples, buildfile);
            build.write("\t<ant antfile=\"${basedir}");
            build.write(path);
            build.write("\" target=\"install\" inheritAll=\"false\" />\n");
        } else {
            System.out.println("... skipped");
        }
    }

    /**
     * Converts an <code>infile</code>, using an <code>xslfile</code> to an
     * <code>outfile</code>.
     *
     * @param infile  the path to an XML file
     * @param xslfile the path to the XSL file
     * @param outfile the path for the output file
     */
    public static void convert(File infile, File xslfile, File outfile) {
        try {
            // Create transformer factory
            TransformerFactory factory = TransformerFactory.newInstance();
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            // Use the factory to create a template containing the xsl file
            Templates template = factory.newTemplates(new StreamSource(
                    new FileInputStream(xslfile)));

            // Use the template to create a transformer
            Transformer xformer = template.newTransformer();

            // passing 2 parameters
            String branch = outfile.getParentFile().getCanonicalPath().substring(root.length());
            branch = branch.replace(File.separatorChar, '/');
            StringBuilder path = new StringBuilder();
            for (int i = 0; i < branch.length(); i++) {
                if (branch.charAt(i) == '/') {
                    path.append("/pdf-core/src/test");
                }
            }

            xformer.setParameter("branch", branch);
            xformer.setParameter("root", path.toString());

            // Prepare the input and output files
            Source source = new StreamSource(new FileInputStream(infile));
            Result result = new StreamResult(new FileOutputStream(outfile));

            // Apply the xsl file to the source file and write the result to the
            // output file
            xformer.transform(source, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
//The End
