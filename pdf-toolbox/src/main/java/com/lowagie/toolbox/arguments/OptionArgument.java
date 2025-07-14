/*
 * $Id: OptionArgument.java 3271 2008-04-18 20:39:42Z xlv $
 * Copyright (c) 2005-2007 Bruno Lowagie, Carsten Hammer
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
 * This class was originally published under the MPL by Bruno Lowagie
 * and Carsten Hammer.
 * It was a part of iText, a Java-PDF library. You can now use it under
 * the MIT License; for backward compatibility you can also use it under
 * the MPL version 1.1: http://www.mozilla.org/MPL/
 * A copy of the MPL license is bundled with the source code FYI.
 */

package com.lowagie.toolbox.arguments;

import com.lowagie.toolbox.AbstractTool;
import java.awt.event.ActionEvent;
import java.util.TreeMap;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;

/**
 * Argument that can be one of several options.
 *
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class OptionArgument extends AbstractArgument {

    private TreeMap<String, Entry> options = new TreeMap<>();


    /**
     * Constructs an OptionArgument.
     *
     * @param tool        the tool that needs this argument
     * @param name        the name of the argument
     * @param description the description of the argument
     */
    public OptionArgument(AbstractTool tool, String name, String description) {
        super(tool, name, description, null);
//        this.setClassname(new Entry("").getClass().getName());
    }

    /**
     * Adds an Option.
     *
     * @param description the description of the option
     * @param value       the value of the option
     */
    public void addOption(Object description, Object value) {
        options.put(value.toString(), new Entry(description, value));
    }

    /**
     * Gets the argument as an object.
     *
     * @return an object
     * @throws InstantiationException if the specified key cannot be compared with the keys currently in the map
     */
    public Object getArgument() throws InstantiationException {
        if (value == null) {
            return null;
        }
        try {
            return options.get(value).getValue();
        } catch (Exception e) {
            throw new InstantiationException(e.getMessage());
        }
    }

    /**
     * @return String
     * @see com.lowagie.toolbox.arguments.StringArgument#getUsage()
     */
    public String getUsage() {
        StringBuilder buf = new StringBuilder(super.getUsage());
        buf.append("    possible options:\n");
        for (Entry entry : options.values()) {
            buf.append("    - ");
            buf.append(entry.getValueToString());
            buf.append(": ");
            buf.append(entry.toString());
            buf.append('\n');
        }
        return buf.toString();
    }

    /**
     * @param evt ActionEvent
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent evt) {
        Object[] message = new Object[2];
        message[0] = "Choose one of the following options:";
        JComboBox<Entry> cb = new JComboBox<>();
        for (Entry entry : options.values()) {
            cb.addItem(entry);
        }
        message[1] = cb;
        int result = JOptionPane.showOptionDialog(
                tool.getInternalFrame(),
                message,
                description,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                null,
                null
        );
        if (result == 0) {
            Entry entry = (Entry) cb.getSelectedItem();
            setValue(entry.getValueToString());
        }
    }

    /**
     * An Entry that can be chosen as option.
     */
    public class Entry {

        /**
         * Describes the option.
         */
        private Object description;
        /**
         * Holds the actual value of the option.
         */
        private Object value;

        /**
         * Constructs an entry.
         *
         * @param value the value of the entry (that will be identical to the description)
         */
        public Entry(Object value) {
            this.value = value;
            this.description = value;
        }

        /**
         * Constructs an entry.
         *
         * @param description the description of the entry
         * @param value       the value of the entry
         */
        public Entry(Object description, Object value) {
            this.description = description;
            this.value = value;
        }

        /**
         * String representation of the Entry.
         *
         * @return a description of the entry
         */
        public String toString() {
            return description.toString();
        }

        /**
         * Gets the value of the String.
         *
         * @return the toString of the value
         */
        public String getValueToString() {
            return value.toString();
        }

        /**
         * @return Returns the description.
         */
        public Object getDescription() {
            return description;
        }

        /**
         * @param description The description to set.
         */
        public void setDescription(Object description) {
            this.description = description;
        }

        /**
         * @return Returns the value.
         */
        public Object getValue() {
            return value;
        }

        /**
         * @param value The value to set.
         */
        public void setValue(Object value) {
            this.value = value;
        }
    }
}
