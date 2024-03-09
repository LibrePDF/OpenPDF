/*
 * $Id: PageSizeArgument.java 3297 2008-05-01 12:19:24Z blowagie $
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

import com.lowagie.text.PageSize;
import com.lowagie.toolbox.AbstractTool;
import java.awt.event.ActionEvent;
import java.lang.reflect.Field;
import java.util.TreeMap;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;

/**
 * Argument that can be one of several options.
 *
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class PageSizeArgument extends OptionArgument {

    private TreeMap<String, String> options = new TreeMap<>();

    /**
     * Constructs an OptionArgument.
     *
     * @param tool        the tool that needs this argument
     * @param name        the name of the argument
     * @param description the description of the argument
     */
    public PageSizeArgument(AbstractTool tool, String name, String description) {
        super(tool, name, description);
        Class<?> ps = PageSize.class;
        Field[] sizes = ps.getDeclaredFields();
        try {
            for (Field size : sizes) {
                addOption(size.getName(), size.get(null));
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds an Option.
     *
     * @param description the description of the option
     * @param value       the value of the option
     */
    public void addOption(String description, String value) {
        options.put(description, value);
    }

    /**
     * Gets the options.
     *
     * @return Returns the options.
     */
    public TreeMap<String, String> getOptions() {
        return options;
    }

    /**
     * Gets the argument as an object.
     *
     * @return an object
     * @throws InstantiationException if the key can't be compared with the other ones in the map
     */
    public Object getArgument() throws InstantiationException {
        if (value == null) {
            return null;
        }
        try {
            return options.get(value);
        } catch (Exception e) {
            throw new InstantiationException(e.getMessage());
        }
    }

    /**
     * @return String
     * @see com.lowagie.toolbox.arguments.StringArgument#getUsage()
     */
    public String getUsage() {
        StringBuilder buf = new StringBuilder("  ");
        buf.append(name);
        buf.append(" -  ");
        buf.append(description);
        buf.append('\n');
        buf.append("    possible options:\n");
        String s;
        for (Object o : options.keySet()) {
            s = (String) o;
            buf.append("    - ");
            buf.append(s);
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
        message[0] = "Choose one of the following pagesizes:";
        JComboBox<String> cb = new JComboBox<>();
        for (String o : options.keySet()) {
            cb.addItem(o);
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
            setValue(cb.getSelectedItem());
        }
    }

    /**
     * Returns a string representation of the object.
     *
     * @return a string representation of the object.
     */
    public String toString() {
        return super.getValue().toString();
    }

}
