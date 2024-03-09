/*
 * $Id: FileList.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * Copyright 2007 by Carsten Hammer.
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
package com.lowagie.toolbox.swing;

import com.lowagie.text.pdf.PdfReader;
import java.awt.BorderLayout;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Vector;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowSorter;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

/**
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class FileList
        extends JInternalFrame implements DropTargetListener {

    private static final long serialVersionUID = -7238230038043975672L;

    private final Vector<RowContainer> filevector = new Vector<>();
    private final JPanel jPanel1 = new JPanel();
    private final BorderLayout borderLayout1 = new BorderLayout();
    private final JPanel jPanel2 = new JPanel();
    private final BorderLayout borderLayout2 = new BorderLayout();
    private final JScrollPane jScrollPane1 = new JScrollPane();
    private final FileTableModel model = new FileTableModel();
    private final JTable jTable1 = new JTable(model);
    private final RowSorter<TableModel> sorter = new TableRowSorter<>(model);
    private final BorderLayout borderLayout3 = new BorderLayout();
    private final DropTarget dt = new DropTarget(this, DnDConstants.ACTION_COPY_OR_MOVE, this, true, null);
    private final JPanel jPanel3 = new JPanel();
    private final JLabel jLabel1 = new JLabel();
    private final JLabel jLabel2 = new JLabel();

    public FileList() {
        super("FileList", true, true, true);
        try {
            jbInit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void jbInit() {
        this.getContentPane().setLayout(borderLayout1);
        jTable1.addKeyListener(new FileListJTable1KeyAdapter(this));
        jLabel1.setText("pages");
        jLabel2.setText("-");
        model.addTableModelListener(new FileListFtmTableModelAdapter(this));
        this.getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);
        jPanel1.setLayout(borderLayout2);
        jPanel2.setLayout(borderLayout3);
        jPanel1.add(jPanel2, java.awt.BorderLayout.CENTER);
        jPanel2.add(jScrollPane1, java.awt.BorderLayout.CENTER);
        jPanel1.add(jPanel3, java.awt.BorderLayout.NORTH);
        jPanel3.add(jLabel2);
        jPanel3.add(jLabel1);
        jScrollPane1.setViewportView(jTable1);
        jTable1.setRowSorter(sorter);
//    this.setSize(200,100);
        this.pack();
    }

    public void dragEnter(DropTargetDragEvent dtde) {
    }

    public void dragOver(DropTargetDragEvent dtde) {
    }

    public void dropActionChanged(DropTargetDragEvent dtde) {
        System.out.println("actionchanged");
    }

    public void drop(DropTargetDropEvent dtde) {
        if ((dtde.getDropAction() & DnDConstants.ACTION_COPY_OR_MOVE) == 0) {
            dtde.rejectDrop();
            return;
        }
        dtde.acceptDrop(DnDConstants.ACTION_COPY);

        Transferable transferable = dtde.getTransferable();
        try {
            // Transferable is not generic, so the cast can only be unchecked.
            @SuppressWarnings("unchecked")
            List<File> filelist = (List<File>) transferable.getTransferData(DataFlavor.javaFileListFlavor);
            for (File f : filelist) {
                filevector.add(new RowContainer(f));

                model.fireTableDataChanged();
                System.out.println(f.toString());
            }
        } catch (IOException | UnsupportedFlavorException ex) {
            ex.printStackTrace();
        }
        dtde.dropComplete(true);
        File[] filar = new File[filevector.size()];
        for (int i = 0; i < filevector.size(); i++) {
            filar[i] = filevector.get(i).getFile();
        }
        super.firePropertyChange("filevector",
                null, filar);
    }

    public void dragExit(DropTargetEvent dte) {
    }

    public void jTable1_keyPressed(KeyEvent e) {
        if (e.getKeyCode() == 127) {
            int[] selected = jTable1.getSelectedRows();
            for (int i = selected.length - 1; i >= 0; i--) {
                model.removeRow(selected[i]);
                model.fireTableDataChanged();
            }
        }
    }

    public void ftm_tableChanged(TableModelEvent e) {
        int sum = 0;
        for (RowContainer c : filevector) {
            sum += c.getPages();
        }
        this.jLabel2.setText(Integer.toString(sum));
    }

    public Vector<RowContainer> getFilevector() {
        return filevector;
    }

    public String getStringreprasentation() {
        StringBuilder sb = new StringBuilder();
        List<RowContainer> vec = getFilevector();
        for (RowContainer c : vec) {
            sb.append(c.getFile().getAbsolutePath()).append('\n');
        }
        return sb.toString();
    }

    static class FileListFtmTableModelAdapter
            implements TableModelListener {

        private final FileList adaptee;

        FileListFtmTableModelAdapter(FileList adaptee) {
            this.adaptee = adaptee;
        }

        public void tableChanged(TableModelEvent e) {
            adaptee.ftm_tableChanged(e);
        }
    }

    static class FileListJTable1KeyAdapter
            extends KeyAdapter {

        private final FileList adaptee;

        FileListJTable1KeyAdapter(FileList adaptee) {
            this.adaptee = adaptee;
        }

        public void keyPressed(KeyEvent e) {
            adaptee.jTable1_keyPressed(e);
        }
    }

    static class RowContainer {

        private File file;
        private int pages;

        /**
         * RowContainer
         */
        RowContainer(File file) {
            this.file = file;
            PdfReader reader = null;
            try {
                reader = new PdfReader(file.getAbsolutePath());
            } catch (IOException ignored) {
            }
            this.pages = reader.getNumberOfPages();
        }

        public File getFile() {
            return file;
        }

        public void setFile(File file) {
            this.file = file;
        }

        public int getPages() {
            return pages;
        }

        public void setPages(int pages) {
            this.pages = pages;
        }
    }

    class FileTableModel extends AbstractTableModel {

        private static final long serialVersionUID = -8173736343997473512L;
        private final String[] columnNames = {
                "Filename", "Pages", "Directory"};

        public int getColumnCount() {
            return columnNames.length;
        }

        public int getRowCount() {
            return filevector.size();
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            switch (col) {
                case 0:
                    return filevector.get(row).getFile().getName();
                case 1:
                    return filevector.get(row).getPages();
                case 2:
                    return filevector.get(row).getFile().getParent();
            }
            return null;
        }

        public Class<?> getColumnClass(int col) {
            switch (col) {
                case 0:
                    return String.class;
                case 1:
                    return Integer.class;
                case 2:
                    return String.class;
            }
            return null;
        }

        public void removeRow(int row) {
            filevector.remove(row);
        }
    }
}

