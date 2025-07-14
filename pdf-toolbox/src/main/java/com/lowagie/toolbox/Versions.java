/*
 * $Id: Versions.java 3372 2008-05-12 03:16:52Z xlv $
 * Copyright (c) 2005-2007 Carsten Hammer
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

/*
 * This class was originally published under the MPL by Bruno Lowagie,
 * Paulo Soares, and Carsten Hammer.
 * It was a part of iText, a Java-PDF library. You can now use it under
 * the MIT License; for backward compatibility you can also use it under
 * the MPL version 1.1: http://www.mozilla.org/MPL/
 * A copy of the MPL license is bundled with the source code FYI.
 */

/*
 * This class was originally written by Carsten Hammer.
 * Changes were made by Bruno Lowagie, Paulo Soares and Xavier Le Vourch.
 * These people were contacted before changing the license from MPL/LGPL to MIT.
 * Current copyright holders are Bruno Lowagie and Carsten Hammer.
 */

package com.lowagie.toolbox;

import com.lowagie.text.Document;
import java.awt.BorderLayout;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Properties;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 * JFrame that shows the plugin_versions of all the plugins.
 *
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class Versions
        extends JFrame {

    /**
     * The serial version UID of this class.
     */
    private static final long serialVersionUID = 2925242862240301106L;

    /**
     * A label with info about the library, JVM,...
     */
    JLabel library_versions = new JLabel();

    /**
     * The table with all the plug-ins (name, version and date).
     */
    JTable plugin_versions = new JTable();

    /**
     * A scrollpane for the plugin_versions table.
     */
    JScrollPane scroll_versions = new JScrollPane();

    /**
     * Constructs a Versions object.
     */
    public Versions() {
        super("Plugins and their version");
        try {
            initialize();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Main method (test purposes only)
     *
     * @param args String[]
     */
    public static void main(String[] args) {
        Versions version = new Versions();
        version.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        version.setVisible(true);
    }

    /**
     * Initialization of the jFrame.
     *
     * @throws Exception
     */
    private void initialize() throws Exception {
        this.getContentPane().setLayout(new BorderLayout());
        scroll_versions.setViewportView(plugin_versions);
        library_versions.setIcon(new ImageIcon(Versions.class.getResource(
                "1t3xt.gif")));
        this.getContentPane().add(library_versions, BorderLayout.NORTH);
        this.getContentPane().add(scroll_versions, BorderLayout.CENTER);
        Properties properties = System.getProperties();
        Runtime runtime = Runtime.getRuntime();
        StringBuilder sb = new StringBuilder();
        sb.append("<html>");
        sb.append("<p>iTexttoolbox version: ").append(Versions.class.getPackage().getImplementationVersion())
                .append("</p>");
        sb.append("<p>iText version: ").append(Document.getVersion()).append("</p>");
        sb.append("<p>java.version: ").append(properties.getProperty("java.version")).append("</p>");
        sb.append("<p>java.vendor: ").append(properties.getProperty("java.vendor")).append("</p>");
        sb.append("<p>java.home: ").append(properties.getProperty("java.home")).append("</p>");
        sb.append("<p>java.freeMemory: ").append(runtime.freeMemory()).append(" bytes").append("</p>");
        sb.append("<p>java.totalMemory: ").append(runtime.totalMemory()).append(" bytes").append("</p>");
        sb.append("<p>user.home: ").append(properties.getProperty("user.home")).append("</p>");
        sb.append("<p>os.name: ").append(properties.getProperty("os.name")).append("</p>");
        sb.append("<p>os.arch: ").append(properties.getProperty("os.arch")).append("</p>");
        sb.append("<p>os.version: ").append(properties.getProperty("os.version")).append("</p>");
        sb.append("</html>");
        library_versions.setText(sb.toString());

        TableModel model = getVersionTableModel(AbstractTool.versionsarray);
        RowSorter<TableModel> sorter =
                new TableRowSorter<>(model);
        plugin_versions.setRowSorter(sorter);
        plugin_versions.setModel(model);

        pack();
    }

    /**
     * Returns the TableModel implementation that will be used to show the plugin_versions.
     *
     * @param versionsarray ArrayList
     * @return TableModel
     */
    public TableModel getVersionTableModel(final ArrayList<String> versionsarray) {
        return new AbstractTableModel() {

            private static final long serialVersionUID = 5105003782164682777L;

            public int getColumnCount() {
                return 4;
            }

            public int getRowCount() {
                return versionsarray.size();
            }

            public Object getValueAt(int rowIndex, int columnIndex) {
                String dummy;
                switch (columnIndex) {
                    case 0:
                        dummy = versionsarray.get(rowIndex);
                        return dummy.split(".java")[0];
                    case 1:
                        dummy = versionsarray.get(rowIndex);
                        return dummy.split(" ")[1];
                    case 2:
                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        dummy = versionsarray.get(rowIndex);
                        try {
                            return df.parse(dummy.split(" ")[2] + " "
                                    + dummy.split(" ")[3]);
                        } catch (ParseException ex) {
                            return null;
                        }
                    case 3:
                        dummy = versionsarray.get(rowIndex);
                        return dummy.split(" ")[4];

                }
                return versionsarray;
            }

            public String getColumnName(int column) {
                switch (column) {
                    case 0:
                        return "Name";
                    case 1:
                        return "Version";
                    case 2:
                        return "Changed";
                    case 3:
                        return "ChangeBy";
                    default:
                        return "";
                }
            }

            public Class<? extends Object> getColumnClass(int column) {
                switch (column) {
                    case 0:
                        return String.class;
                    case 1:
                        return String.class;
                    case 2:
                        return java.util.Date.class;
                    case 3:
                        return String.class;
                    default:
                        return null;
                }
            }
        };

    }
}
