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
package org.openpdf.swing;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.css.CSSPrimitiveValue;
import org.openpdf.context.StyleReference;
import org.openpdf.css.constants.ValueConstants;

import javax.swing.*;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Objects.requireNonNull;

public final class DOMInspector extends JPanel {
    private StyleReference styleReference;
    @SuppressWarnings("NotNullFieldNotInitialized")
    private DOMSelectionListener nodeSelectionListener;
    private final JSplitPane splitPane;
    private Document doc;
    private final JTree tree;

    public DOMInspector(Document doc, StyleReference sr) {
        setLayout(new java.awt.BorderLayout());

        tree = new JTree();
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        JScrollPane scroll = new JScrollPane(tree);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(150);

        add(splitPane, "Center");
        splitPane.setLeftComponent(scroll);

        JButton close = new JButton("close");
        add(close, "South");
        setPreferredSize(new Dimension(300, 300));

        this.doc = doc;
        this.styleReference = sr;
        this.initForCurrentDocument();

        close.addActionListener(evt -> getFrame(this).setVisible(false));
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawLine(0, 0, 100, 100);
    }

    /**
     * Sets the forDocument attribute of the DOMInspector object
     */
    public void setForDocument(Document doc, StyleReference sr) {
        this.doc = requireNonNull(doc);
        this.styleReference = requireNonNull(sr);
        this.initForCurrentDocument();
    }

    /**
     * Gets the frame attribute of the DOMInspector object
     */
    public JFrame getFrame(Component comp) {
        if (comp instanceof JFrame) {
            return (JFrame) comp;
        }
        return getFrame(comp.getParent());
    }

    private void initForCurrentDocument() {
        // tree stuff
        TreeModel model = new DOMTreeModel(doc);
        tree.setModel(model);
        if (!(tree.getCellRenderer() instanceof DOMTreeCellRenderer)) {
            tree.setCellRenderer(new DOMTreeCellRenderer());
        }

        splitPane.remove(splitPane.getRightComponent());
        ElementPropertiesPanel elementPropPanel = new ElementPropertiesPanel(styleReference);
        splitPane.setRightComponent(elementPropPanel);

        tree.removeTreeSelectionListener(nodeSelectionListener);

        nodeSelectionListener = new DOMSelectionListener(tree, elementPropPanel);
        tree.addTreeSelectionListener(nodeSelectionListener);
    }
}

final class ElementPropertiesPanel extends JPanel {
    private final StyleReference _sr;
    private final JTable _properties;
    private final TableModel _defaultTableModel;

    ElementPropertiesPanel(StyleReference sr) {
        //this._context = context;
        this._sr = sr;

        this._properties = new PropertiesJTable();
        this._defaultTableModel = new DefaultTableModel();

        this.setLayout(new BorderLayout());
        this.add(new JScrollPane(_properties), BorderLayout.CENTER);
    }

    /**
     * Sets the forElement attribute of the ElementPropertiesPanel object
     *
     * @param node The new forElement value
     */
    public void setForElement(Node node) {
        _properties.setModel(tableModel(node));
        TableColumnModel model = _properties.getColumnModel();
        if (model.getColumnCount() > 0) {
            model.getColumn(0).sizeWidthToFit();
        }
    }

    private TableModel tableModel(Node node) {
        if (node.getNodeType() != Node.ELEMENT_NODE) {
            Toolkit.getDefaultToolkit().beep();
            return _defaultTableModel;
        }
        Map<String, CSSPrimitiveValue> props = _sr.getCascadedPropertiesMap((Element) node);
        return new PropertiesTableModel(props);
    }

    static final class PropertiesJTable extends JTable {
        private final Font propLabelFont;
        private final Font defaultFont;

        PropertiesJTable() {
            this.setColumnSelectionAllowed(false);
            this.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            propLabelFont = new Font("Courier New", Font.BOLD, 12);
            defaultFont = new Font("Default", Font.PLAIN, 12);
        }

        /**
         * Gets the cellRenderer attribute of the PropertiesJTable object
         */
        @Override
        public TableCellRenderer getCellRenderer(int row, int col) {
            JLabel label = (JLabel) super.getCellRenderer(row, col);
            label.setBackground(Color.white);
            label.setFont(defaultFont);
            if (col == 0) {
                // BUG: not working?
                label.setFont(propLabelFont);
            } else if (col == 2) {
                PropertiesTableModel model = (PropertiesTableModel) getModel();
                Map.Entry<String, CSSPrimitiveValue> me = (Map.Entry<String, CSSPrimitiveValue>) model._properties.entrySet().toArray()[row];
                CSSPrimitiveValue cpv = me.getValue();
                if (cpv.getCssText().startsWith("rgb")) {
                    label.setBackground(org.openpdf.css.util.ConversionUtil.rgbToColor(cpv.getRGBColorValue()));
                }
            }
            return (TableCellRenderer) label;
        }
    }

    static class PropertiesTableModel extends AbstractTableModel {
        //String _colNames[] = {"Property Name", "Text", "Value", "Important-Inherit"};
        private final String[] _colNames = {"Property Name", "Text", "Value"};

        private final Map<String, CSSPrimitiveValue> _properties;

        PropertiesTableModel(Map<String, CSSPrimitiveValue> cssProperties) {
            _properties = cssProperties;
        }

        /**
         * Gets the columnName attribute of the PropertiesTableModel object
         */
        @Override
        public String getColumnName(int col) {
            return _colNames[col];
        }

        /**
         * Gets the columnCount attribute of the PropertiesTableModel object
         */
        @Override
        public int getColumnCount() {
            return _colNames.length;
        }

        /**
         * Gets the rowCount attribute of the PropertiesTableModel object
         */
        @Override
        public int getRowCount() {
            return _properties.size();
        }

        /**
         * Gets the valueAt attribute of the PropertiesTableModel object
         */
        @Nullable
        @CheckReturnValue
        @Override
        public Object getValueAt(int row, int col) {
            Map.Entry<String, CSSPrimitiveValue> me = (Map.Entry<String, CSSPrimitiveValue>) _properties.entrySet().toArray()[row];
            CSSPrimitiveValue cpv = me.getValue();

            return switch (col) {

                case 0 -> me.getKey();
                case 1 -> cpv.getCssText();
                case 2 -> ValueConstants.isNumber(cpv.getPrimitiveType()) ?
                        cpv.getFloatValue(cpv.getPrimitiveType()) :
                        ""; //actual.cssValue().getCssText();

                    /* ouch, can't do this now:
                    case 3 -> ( cpv.actual.isImportant() ? "!Imp" : "" ) +
                                " " +
                                ( actual.forcedInherit() ? "Inherit" : "" );
                     */
                default -> null;
            };
        }

        /**
         * Gets the cellEditable attribute of the PropertiesTableModel object
         */
        @Override
        public boolean isCellEditable(int row, int col) {
            return false;
        }
    }
}


class DOMSelectionListener implements TreeSelectionListener {

    private final JTree _tree;
    private final ElementPropertiesPanel _elemPropPanel;

    DOMSelectionListener(JTree tree, ElementPropertiesPanel panel) {
        _tree = tree;
        _elemPropPanel = panel;
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        Node node = (Node) _tree.getLastSelectedPathComponent();
        if (node != null) {
            _elemPropPanel.setForElement(node);
        }

    }
}

class DOMTreeModel implements TreeModel {

    /**
     * Our root for display
     */
    private Node root;

    private final Map<Object, List<Node>> displayableNodes = new HashMap<>();

    DOMTreeModel(Document doc) {
        Node tempRoot = doc.getDocumentElement();
        NodeList nl = tempRoot.getChildNodes();
        for (int i = 0; i < nl.getLength(); i++) {
            if (nl.item(i).getNodeName().equalsIgnoreCase("body")) {
                this.root = nl.item(i);
            }
        }
    }

    @Override
    public void addTreeModelListener(TreeModelListener l) {
    }

    @Override
    public void removeTreeModelListener(TreeModelListener l) {
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue) {
        // no-op
    }

    /**
     * Gets the child attribute of the DOMTreeModel object
     */
    @Override
    public Object getChild(Object parent, int index) {

        Node node = (Node) parent;

        List<Node> children = this.displayableNodes.get(parent);
        if (children == null) {
            children = addDisplayable(node);
        }

        return children.get(index);
    }


    //Returns the number of children of parent.

    /**
     * Gets the childCount attribute of the DOMTreeModel object
     */
    @Override
    public int getChildCount(Object parent) {

        Node node = (Node) parent;
        List<Node> children = this.displayableNodes.get(parent);
        if (children == null) {
            children = addDisplayable(node);
        }

        return children.size();
    }


    //Returns the index of child in parent.

    /**
     * Gets the indexOfChild attribute of the DOMTreeModel object
     */
    @Override
    public int getIndexOfChild(Object parent, Object childNode) {
        Node node = (Node) parent;
        Node child = (Node) childNode;

        List<Node> children = this.displayableNodes.get(parent);
        if (children == null) {
            children = addDisplayable(node);
        }
        if (children.contains(child)) {
            return children.indexOf(child);
        } else {
            return -1;
        }
    }


    //Returns the root of the tree.

    /**
     * Gets the root attribute of the DOMTreeModel object
     *
     * @return The root value
     */
    @Override
    public Object getRoot() {
        return root;
    }


    //Returns true if node is a leaf.

    /**
     * Gets the leaf attribute of the DOMTreeModel object
     */
    @Override
    public boolean isLeaf(Object nd) {
        Node node = (Node) nd;
        return !node.hasChildNodes();
    }

    // only adds displayable nodes--not stupid DOM text filler nodes
    /**
     * Adds a feature to the Displayable attribute of the DOMTreeModel object
     *
     * @param parent The feature to be added to the Displayable attribute
     */
    private List<Node> addDisplayable(Node parent) {
        List<Node> children = displayableNodes.get(parent);
        if (children == null) {
            children = new ArrayList<>();
            displayableNodes.put(parent, children);
            NodeList nl = parent.getChildNodes();
            for (int i = 0, len = nl.getLength(); i < len; i++) {
                Node child = nl.item(i);
                if (child.getNodeType() == Node.ELEMENT_NODE ||
                        child.getNodeType() == Node.COMMENT_NODE ||
                        (child.getNodeType() == Node.TEXT_NODE && !child.getNodeValue().trim().isEmpty())) {
                    children.add(child);
                }
            }
            return children;
        } else {
            return new ArrayList<>();
        }
    }

}

class DOMTreeCellRenderer extends DefaultTreeCellRenderer {
    /**
     * Gets the treeCellRendererComponent attribute of the DOMTreeCellRenderer
     * object
     */
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
                                                  boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

        Node node = (Node) value;

        if (node.getNodeType() == Node.ELEMENT_NODE) {

            String cls = "";
            if (node.hasAttributes()) {
                Node cn = node.getAttributes().getNamedItem("class");
                if (cn != null) {
                    cls = " class='" + cn.getNodeValue() + "'";
                }
            }
            value = "<" + node.getNodeName() + cls + ">";

        }

        if (node.getNodeType() == Node.TEXT_NODE) {

            if (!node.getNodeValue().trim().isEmpty()) {
                value = "\"" + node.getNodeValue() + "\"";
            }
        }

        if (node.getNodeType() == Node.COMMENT_NODE) {

            value = "<!-- " + node.getNodeValue() + " -->";

        }

        DefaultTreeCellRenderer tcr = (DefaultTreeCellRenderer) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        tcr.setOpenIcon(null);
        tcr.setClosedIcon(null);

        return super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
    }
}
