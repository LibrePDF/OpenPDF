/*
 * {{{ header & license
 * Copyright (c) 2004, 2005 Torbjoern Gannholm
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
package org.openpdf.simple.extend;

import org.jspecify.annotations.Nullable;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.openpdf.extend.UserAgentCallback;
import org.openpdf.layout.LayoutContext;
import org.openpdf.render.BlockBox;
import org.openpdf.simple.extend.form.FormField;
import org.openpdf.simple.extend.form.FormFieldFactory;
import org.openpdf.util.XRLog;

import javax.swing.*;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Represents a form object
 *
 * @author Torbjoern Gannholm
 * @author Sean Bright
 */
public class XhtmlForm {
    private static final String FS_DEFAULT_GROUP = "__fs_default_group_";

    private static int _defaultGroupCount = 1;

    private final UserAgentCallback _userAgentCallback;
    private final Map<Element, FormField> _componentCache = new LinkedHashMap<>();
    private final Map<String, ButtonGroupWrapper> _buttonGroups = new HashMap<>();
    @Nullable
    private final Element _parentFormElement;
    private final FormSubmissionListener _formSubmissionListener;

    public XhtmlForm(UserAgentCallback uac, @Nullable Element e, FormSubmissionListener fsListener) {
        _userAgentCallback = uac;
        _parentFormElement = e;
        _formSubmissionListener = fsListener;
    }

    public XhtmlForm(UserAgentCallback uac, Element e) {
        this(uac, e, new DefaultFormSubmissionListener());
    }

    public UserAgentCallback getUserAgentCallback() {
        return _userAgentCallback;
    }

    public void addButtonToGroup(@Nullable String groupName, AbstractButton button) {
        if (groupName == null) {
            groupName = createNewDefaultGroupName();
        }

        ButtonGroupWrapper group = _buttonGroups.computeIfAbsent(groupName, (name) -> new ButtonGroupWrapper());

        group.add(button);
    }

    private static String createNewDefaultGroupName() {
        return FS_DEFAULT_GROUP + ++_defaultGroupCount;
    }

    private static boolean isFormField(Element e) {
        String nodeName = e.getNodeName();

        return nodeName.equals("input") || nodeName.equals("select") || nodeName.equals("textarea");
    }

    @Nullable
    public FormField addComponent(Element e, LayoutContext context, BlockBox box) {

        if (_componentCache.containsKey(e)) {
            return _componentCache.get(e);
        } else {
            if (!isFormField(e)) {
                return null;
            }

            FormField field = FormFieldFactory.create(this, context, box);

            if (field == null) {
                XRLog.layout("Unknown field type: " + e.getNodeName());
                return null;
            }

            _componentCache.put(e, field);
            return field;
        }
    }

    public void reset() {
        for (ButtonGroupWrapper buttonGroupWrapper : _buttonGroups.values()) {
            buttonGroupWrapper.clearSelection();
        }

        for (FormField formField : _componentCache.values()) {
            formField.reset();
        }
    }

    public void submit(JComponent source) {
        // If we don't have a <form> to tell us what to do, don't
        // do anything.
        if (_parentFormElement == null) {
            return;
        }

        StringBuilder data = new StringBuilder();
        String action = _parentFormElement.getAttribute("action");
        data.append(action).append("?");

        boolean first = true;
        for (Map.Entry<Element, FormField> entry : _componentCache.entrySet()) {
            FormField field = entry.getValue();

            if (field.includeInSubmission(source)) {
                for (String value : field.getFormDataStrings()) {
                    if (!first) {
                        data.append('&');
                    }

                    data.append(value);
                    first = false;
                }
            }
        }

        _formSubmissionListener.submit(data.toString());
    }

    public static String collectText(Element e) {
        StringBuilder result = new StringBuilder();
        Node node = e.getFirstChild();
        if (node != null) {
            do {
                short nodeType = node.getNodeType();
                if (nodeType == Node.TEXT_NODE || nodeType == Node.CDATA_SECTION_NODE) {
                    Text text = (Text) node;
                    result.append(text.getData());
                }
            } while ((node = node.getNextSibling()) != null);
        }
        return result.toString().trim();
    }

    private static class ButtonGroupWrapper {
        private final ButtonGroup _group = new ButtonGroup();
        private final AbstractButton _dummy = new JRadioButton();

        private ButtonGroupWrapper() {
            // We need a dummy button to have the appearance of all
            // the radio buttons being in an unselected state.
            //
            // From:
            //   http://java.sun.com/j2se/1.5/docs/api/javax/swing/ButtonGroup.html
            //
            // There is no way to turn a button programmatically to 'off', in
            // order to clear the button group. To give the appearance of 'none
            // selected', add an invisible radio button to the group and then
            // programmatically select that button to turn off all the displayed
            // radio buttons. For example, a normal button with the label 'none'
            // could be wired to select the invisible radio button.
            _group.add(_dummy);
        }

        public void add(AbstractButton b) {
            _group.add(b);
        }

        public void clearSelection() {
            _group.setSelected(_dummy.getModel(), true);
        }
    }
}
