/*
 * {{{ header & license
 * Copyright (c) 2007 Sean Bright
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
package org.openpdf.simple.extend.form;

import org.w3c.dom.Element;
import org.openpdf.layout.LayoutContext;
import org.openpdf.render.BlockBox;
import org.openpdf.simple.extend.XhtmlForm;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class FileField extends InputField implements ActionListener {
    private JTextField _pathTextField;
    private JButton _browseButton;

    FileField(Element e, XhtmlForm form, LayoutContext context, BlockBox box) {
        super(e, form, context, box);
    }

    @Override
    public JComponent create() {
        JPanel panel = new JPanel(new GridBagLayout());

        panel.setOpaque(false);

        _pathTextField = new JTextField();
        _pathTextField.setColumns(15);

        _browseButton = new JButton("Browse...");
        _browseButton.addActionListener(this);

        GridBagConstraints pathConstraints = new GridBagConstraints();
        pathConstraints.fill = GridBagConstraints.HORIZONTAL;
        pathConstraints.gridx = 0;
        pathConstraints.gridy = 0;
        pathConstraints.weightx = 1.0;
        pathConstraints.anchor = GridBagConstraints.EAST;
        pathConstraints.insets = new Insets(0, 0, 0, 0);
        panel.add(_pathTextField, pathConstraints);

        GridBagConstraints browseConstraints = new GridBagConstraints();
        browseConstraints.fill = GridBagConstraints.HORIZONTAL;
        browseConstraints.gridx = 1;
        browseConstraints.gridy = 0;
        browseConstraints.weightx = 0.0;
        browseConstraints.anchor = GridBagConstraints.EAST;
        browseConstraints.insets = new Insets(0, 5, 0, 0);
        panel.add(_browseButton, browseConstraints);

        return panel;
    }

    @Override
    protected void applyOriginalState() {
        // This is always the default, since you can't set a default
        // value for this in the HTML
        _pathTextField.setText("");
    }

    @Override
    protected String[] getFieldValues() {
        return new String [] {
                // TODO: This will have to be special once we aren't
                // just passing plain strings around
                _pathTextField.getText()
        };
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == _browseButton) {
            JFileChooser chooser = new JFileChooser();

            // TODO: We should probably use the BasicPanel as the parent
            int result = chooser.showOpenDialog(_browseButton);

            if (result == JFileChooser.APPROVE_OPTION) {
                _pathTextField.setText(chooser.getSelectedFile().getAbsolutePath());
                _pathTextField.setCaretPosition(0);

                _browseButton.requestFocus();
            }
        }
    }
}
