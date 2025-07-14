/*
 * $Id: AbstractTool.java 3276 2008-04-19 00:32:58Z xlv $
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

package com.lowagie.toolbox;

import com.lowagie.toolbox.arguments.AbstractArgument;
import com.lowagie.tools.Executable;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;

/**
 * Every iText tool has to extend this abstract class.
 *
 * @author not attributable
 * @version $Id: AbstractTool.java 3276 2008-04-19 00:32:58Z xlv $
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public abstract class AbstractTool implements ActionListener {

    /**
     * a menu option
     */
    public static final int MENU_EXECUTE = 1;
    /**
     * a menu option
     */
    public static final int MENU_EXECUTE_SHOW = 2;
    /**
     * a menu option
     */
    public static final int MENU_EXECUTE_PRINT = 4;
    /**
     * a menu option
     */
    public static final int MENU_EXECUTE_PRINT_SILENT = 8;
    /**
     * An array with the plugin_versions of the tool.
     */
    public static ArrayList<String> versionsarray = new ArrayList<>();
    /**
     * The internal frame of the tool.
     */
    protected JInternalFrame internalFrame = null;
    /**
     * The list of arguments needed by the tool.
     */
    protected ArrayList<AbstractArgument> arguments = new ArrayList<>();
    /**
     * Execute menu options
     */
    protected int menuoptions = MENU_EXECUTE;
    /**
     * awtdesktop
     */
    private Desktop awtdesktop = null;
    private JMenuBar menubar;

    /**
     * AbstractTool
     */
    public AbstractTool() {
        if (Desktop.isDesktopSupported()) {
            awtdesktop = Desktop.getDesktop();
        }
    }

    /**
     * Add the version of the plugin to the plugin_versions array.
     *
     * @param version the version to add.
     */
    protected static void addVersion(String version) {
        version = version.substring(5, version.length() - 2);
        versionsarray.add(version);
    }

    /**
     * Sets the arguments.
     *
     * @param args the arguments as String-array.
     */
    public void setMainArguments(String[] args) {
        int counter = 0;
        for (AbstractArgument argument : arguments) {
            if (args.length > counter) {
                argument.setValue(args[counter]);
            } else {
                break;
            }
            counter++;
        }
    }

    /**
     * Gets the arguments.
     *
     * @return Returns the arguments.
     */
    public ArrayList<AbstractArgument> getArguments() {
        return arguments;
    }

    /**
     * Sets the arguments.
     *
     * @param arguments The arguments to set.
     */
    public void setArguments(ArrayList<AbstractArgument> arguments) {
        this.arguments = arguments;
    }

    /**
     * Gets the value of a given argument.
     *
     * @param name the name of the argument
     * @return the value of an argument as an Object.
     * @throws InstantiationException on error
     */
    public Object getValue(String name) throws InstantiationException {
        for (AbstractArgument argument : arguments) {
            if (name.equals(argument.getName())) {
                return argument.getArgument();
            }
        }
        return null;
    }

    /**
     * Returns the internal frame. Creates one if it's null.
     *
     * @return Returns the internalFrame.
     */
    public JInternalFrame getInternalFrame() {
        if (internalFrame == null) {
            createFrame();
        }
        return internalFrame;
    }

    /**
     * Sets the internal frame.
     *
     * @param internalFrame The internalFrame to set.
     */
    public void setInternalFrame(JInternalFrame internalFrame) {
        this.internalFrame = internalFrame;
    }

    /**
     * Gets the menubar.
     *
     * @return a menubar for this tool
     */
    public JMenuBar getMenubar() {
        menubar = new JMenuBar();
        JMenu tool = new JMenu(ToolMenuItems.TOOL);
        tool.setMnemonic(KeyEvent.VK_F);
        JMenuItem usage = new JMenuItem(ToolMenuItems.USAGE);
        usage.setMnemonic(KeyEvent.VK_U);
        usage.addActionListener(this);
        tool.add(usage);
        JMenuItem args = new JMenuItem(ToolMenuItems.ARGUMENTS);
        args.setMnemonic(KeyEvent.VK_A);
        args.addActionListener(this);
        tool.add(args);
        if ((menuoptions & MENU_EXECUTE) > 0) {
            JMenuItem execute = new JMenuItem(ToolMenuItems.EXECUTE);
            execute.setMnemonic(KeyEvent.VK_E);
            execute.addActionListener(this);
            tool.add(execute);
        }
        if ((menuoptions & MENU_EXECUTE_SHOW) > 0) {
            JMenuItem execute = new JMenuItem(ToolMenuItems.EXECUTESHOW);
            execute.addActionListener(this);
            tool.add(execute);
        }
        if ((menuoptions & MENU_EXECUTE_PRINT) > 0) {
            JMenuItem execute = new JMenuItem(ToolMenuItems.EXECUTEPRINT);
            execute.addActionListener(this);
            tool.add(execute);
        }
        if ((menuoptions & MENU_EXECUTE_PRINT_SILENT) > 0) {
            JMenuItem execute = new JMenuItem(ToolMenuItems.EXECUTEPRINTSILENT);
            execute.addActionListener(this);
            tool.add(execute);
        }
        JMenuItem close = new JMenuItem(ToolMenuItems.CLOSE);
        close.setMnemonic(KeyEvent.VK_C);
        close.addActionListener(this);
        tool.add(close);
        menubar.add(tool);
        if (!arguments.isEmpty()) {
            JMenu params = new JMenu(ToolMenuItems.ARGUMENTS);
            tool.setMnemonic(KeyEvent.VK_T);
            JMenuItem item;
            for (AbstractArgument argument : arguments) {
                item = new JMenuItem(argument.getName());
                item.setToolTipText(argument.getDescription());
                item.addActionListener(argument);
                params.add(item);
            }
            menubar.add(params);
        }
        return menubar;
    }

    public void setMenubar(JMenuBar menubar) {
        this.menubar = menubar;
    }

    /**
     * Gets the usage of the tool.
     *
     * @return a String describing how to use the tool.
     */
    public String getUsage() {
        StringBuilder buf = new StringBuilder("java ");
        buf.append(getClass().getName());
        for (AbstractArgument argument : arguments) {
            buf.append(' ');
            buf.append(argument.getName());
        }
        buf.append('\n');
        for (AbstractArgument argument : arguments) {
            buf.append(argument.getUsage());
        }
        return buf.toString();
    }

    /**
     * Gets the current arguments of the tool.
     *
     * @return a String with the list of arguments and their values.
     */
    private String getArgs() {
        StringBuilder buf = new StringBuilder("Current arguments:\n");
        for (AbstractArgument argument : arguments) {
            buf.append("  ");
            buf.append(argument.getName());
            if (argument.getValue() == null) {
                buf.append(" = null\n");
            } else {
                buf.append(" = '");
                buf.append(argument.toString());
                buf.append("'\n");
            }
        }
        return buf.toString();
    }

    /**
     * @param evt ActionEvent
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent evt) {
        if (ToolMenuItems.CLOSE.equals(evt.getActionCommand())) {
            System.out.println("=== " + getInternalFrame().getTitle() +
                    " CLOSED ===");
            internalFrame.dispose();
        }
        if (ToolMenuItems.USAGE.equals(evt.getActionCommand())) {
            JOptionPane.showMessageDialog(internalFrame, getUsage());
        }
        if (ToolMenuItems.ARGUMENTS.equals(evt.getActionCommand())) {
            JOptionPane.showMessageDialog(internalFrame, getArgs());
        }
        if (ToolMenuItems.EXECUTE.equals(evt.getActionCommand())) {
            this.execute();
        }
        if (ToolMenuItems.EXECUTESHOW.equals(evt.getActionCommand())) {
            this.execute();
            try {
                if (awtdesktop != null &&
                        awtdesktop.isSupported(Desktop.Action.OPEN)) {
                    awtdesktop.open(getDestPathPDF());
                } else {
                    Executable.openDocument(getDestPathPDF());
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
        if (ToolMenuItems.EXECUTEPRINT.equals(evt.getActionCommand())) {
            this.execute();
            try {
                if (awtdesktop != null &&
                        awtdesktop.isSupported(Desktop.Action.PRINT)) {
                    awtdesktop.print(getDestPathPDF());
                } else {
                    Executable.printDocument(getDestPathPDF());
                }
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
        if (ToolMenuItems.EXECUTEPRINTSILENT.equals(evt.getActionCommand())) {
            this.execute();
            try {
                Executable.printDocumentSilent(getDestPathPDF());
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
    }

    /**
     * Gets the PDF file that should be generated (or null if the output isn't a PDF file).
     *
     * @return the PDF file that should be generated
     * @throws InstantiationException on error
     */
    protected abstract File getDestPathPDF() throws InstantiationException;

    /**
     * Creates the internal frame.
     */
    protected abstract void createFrame();

    /**
     * Executes the tool (in most cases this generates a PDF file).
     */
    public abstract void execute();

    /**
     * Indicates that the value of an argument has changed.
     *
     * @param arg the argument that has changed
     */
    public abstract void valueHasChanged(AbstractArgument arg);
}
