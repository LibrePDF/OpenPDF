/*
 * $Id: Toolbox.java 3271 2008-04-18 20:39:42Z xlv $
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

import com.lowagie.tools.Executable;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.Vector;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

/**
 * This is a utility that allows you to use a number of iText tools.
 *
 * @since 2.1.1 (imported from itexttoolbox project)
 */
public class Toolbox extends JFrame implements ActionListener {

    /**
     * A serial version ID
     */
    private static final long serialVersionUID = -3766198389452935073L;
    /**
     * The DesktopPane of the toolbox.
     */
    private final JDesktopPane desktop;
    /**
     * toolarray
     */
    private final ArrayList<AbstractTool> toolarray = new ArrayList<>();
    private final Vector<String> menulist = new Vector<>();
    private final Vector<String> menuitemlist = new Vector<>();
    /**
     * The ConsolePane of the toolbox.
     */
    private JScrollPane console;
    /**
     * The list of tools in the toolbox.
     */
    private Properties toolmap = new Properties();
    /**
     * x-coordinate of the location of a new internal frame.
     */
    private int locationX = 0;
    /**
     * y-coordinate of the location of a new internal frame.
     */
    private int locationY = 0;

    /**
     * Constructs the Toolbox object.
     */
    public Toolbox() {
        super();
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(true);
        setTitle("iText Toolbox");
        desktop = new JDesktopPane();
        setJMenuBar(getMenubar());
        try {
            setIconImage(new ImageIcon(com.lowagie.toolbox.Toolbox.class.getResource(
                    "1t3xt.gif")).getImage());
        } catch (Exception err) {
            System.err.println("Problem loading icon image.");
        }
        Console c;
        try {
            c = new Console();
            console = new JScrollPane(c.textArea);
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                desktop,
                console);
        splitPane.setContinuousLayout(true);
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(300);
        setContentPane(splitPane);
        centerFrame(this);
        setVisible(true);
    }

    /**
     * Starts the Toolbox utility.
     * <p>
     * use as first argument the name of the plugin, then the arguments of the plugin used.
     * <p>
     * e.g.
     * <p>
     * java -jar itext.jar Burst inputfile.pdf
     * <p>
     * That way you can call plugins by name directly.
     *
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        try {
            Class.forName("com.lowagie.text.Document");
        } catch (ClassNotFoundException e) {
            JOptionPane.showMessageDialog(null,
                    "You need the iText.jar in your CLASSPATH!",
                    e.getClass().getName(),
                    JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
        Toolbox toolbox = new Toolbox();
        if (args.length > 0) {
            try {
                AbstractTool tool = toolbox.createFrame(args[0]);
                String[] nargs = new String[args.length - 1];
                System.arraycopy(args, 1, nargs, 0, args.length - 1);
                tool.setMainArguments(nargs);
                tool.execute();
            } catch (PropertyVetoException | InstantiationException | IllegalAccessException |
                     ClassNotFoundException ex) {
            }
        }
    }

    /**
     * Centers a JFrame.
     *
     * @param f JFrame
     */
    public static void centerFrame(Frame f) {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = f.getSize();
        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }
        f.setLocation((screenSize.width - frameSize.width) / 2,
                (screenSize.height - frameSize.height) / 2);
    }

    /**
     * Gets the menubar.
     *
     * @return a menubar
     */
    private JMenuBar getMenubar() {
        Properties p = new Properties();
        try {
            p.load(Toolbox.class.getClassLoader().getResourceAsStream(
                    "tools.txt"));
            String usertoolstxt = System.getProperty("user.home") +
                    System.getProperty("file.separator") +
                    "tools.txt";
            File uttf = new File(usertoolstxt);
            if (uttf.isFile() && uttf.exists()) {
                p.load(new FileInputStream(usertoolstxt));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        toolmap = new Properties();
        JMenuBar menubar = new JMenuBar();
        JMenu file = new JMenu(ToolMenuItems.FILE);
        file.setMnemonic(KeyEvent.VK_T);
        JMenuItem close = new JMenuItem(ToolMenuItems.CLOSE);
        close.setMnemonic(KeyEvent.VK_C);
        close.addActionListener(this);
        file.add(close);
        JMenu view = new JMenu(ToolMenuItems.VIEW);
        JMenuItem reset = new JMenuItem(ToolMenuItems.RESET);
        reset.addActionListener(this);
        view.add(reset);
//        JMenuItem filelist = new JMenuItem(FILELIST);
//        filelist.addActionListener(this);
//        view.add(filelist);
        JMenu tools = new JMenu(ToolMenuItems.TOOLS);
// Here one day should be the wizard to help you create a new beanshell script
//        JMenuItem create = new JMenuItem(CREATE);
//        create.addActionListener(this);
//        tools.add(create);
        buildPluginMenuItems(new TreeMap<>(p), tools);
        JMenu help = new JMenu(ToolMenuItems.HELP);
        JMenuItem about = new JMenuItem(ToolMenuItems.ABOUT);
//        about.setIcon(new ImageIcon(Toolbox.class.getResource(
//                "Help24.gif")));
        about.setMnemonic(KeyEvent.VK_A);
        about.addActionListener(this);
        help.add(about);
        JMenuItem versions = new JMenuItem(ToolMenuItems.VERSION);
//        versions.setIcon(new ImageIcon(Toolbox.class.getResource(
//                "About24.gif")));
        versions.addActionListener(this);
        help.add(versions);
        menubar.add(file);
        menubar.add(tools);
        menubar.add(view);
        menubar.add(Box.createGlue());
        menubar.add(help);
        return menubar;
    }

    /**
     * BuildPluginMenuItems
     *
     * @param tmp   Map
     * @param tools JMenu
     */
    private void buildPluginMenuItems(Map<Object, Object> tmp, JMenu tools) {
        String name, tool;
        JMenu current = null;
        JMenuItem item;

        for (Map.Entry<Object, Object> entry : tmp.entrySet()) {
            name = (String) entry.getKey();
            if (current == null || !name.startsWith(current.getText())) {
                String menu = name.substring(0, name.indexOf('.'));
                menulist.add(menu);
                current = new JMenu(menu);
                tools.add(current);
            }
            String menuitem = name.substring(current.getText().length() + 1);
            menuitemlist.add(menuitem);
            item = new JMenuItem(menuitem);
            item.addActionListener(this);
            tool = (String) entry.getValue();
            try {
                if (Class.forName(tool) != null) {
                    toolmap.put(item.getText(), tool);
                    current.add(item);
                }
            } catch (ClassNotFoundException e) {
                System.err.println("Plugin " + name
                        + " was not found in your CLASSPATH.");
            }
        }
    }

    /**
     * Creates an Internal Frame.
     *
     * @param name the name of the application
     * @return AbstractTool
     * @throws InstantiationException on error
     * @throws IllegalAccessException on error with the access rights
     * @throws ClassNotFoundException on error
     * @throws PropertyVetoException  on error of property rights
     */
    public AbstractTool createFrame(String name) throws InstantiationException,
            IllegalAccessException, ClassNotFoundException,
            PropertyVetoException {
        AbstractTool ti = null;
        String classname = (String) toolmap.get(name);
        ti = (AbstractTool) Class.forName(classname).newInstance();
        toolarray.add(ti);
        JInternalFrame f = ti.getInternalFrame();
        f.setLocation(locationX, locationY);
        locationX += 25;
        if (locationX > this.getWidth() + 50) {
            locationX = 0;
        }
        locationY += 25;
        if (locationY > this.getHeight() + 50) {
            locationY = 0;
        }
        f.setVisible(true);
        desktop.add(f);
        f.setSelected(true);
        return ti;
    }

    /**
     * @param evt ActionEvent
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent evt) {
        if (ToolMenuItems.CLOSE.equals(evt.getActionCommand())) {
            System.out.println("The Toolbox is closed.");
            System.exit(0);
        } else if (ToolMenuItems.ABOUT.equals(evt.getActionCommand())) {
            System.out
                    .println(
                            "The iText Toolbox is part of iText, a Free Java-PDF Library.\nVisit http://itexttoolbox.sourceforge.net/ for more info.");
            try {
                Executable
                        .launchBrowser("http://itexttoolbox.sourceforge.net/");
            } catch (IOException ioe) {
                JOptionPane
                        .showMessageDialog(
                                this,
                                "The iText Toolbox is part of iText, a Free Java-PDF Library.\nVisit http://itexttoolbox.sourceforge.net/ for more info.");
            }
        } else if (ToolMenuItems.RESET.equals(evt.getActionCommand())) {
            JInternalFrame[] framearray = desktop.getAllFrames();
            int xx = 0, yy = 0;
            for (JInternalFrame jInternalFrame : framearray) {
                if (!jInternalFrame.isIcon()) {
                    try {
                        int frameDistance = jInternalFrame.getHeight() -
                                jInternalFrame.getContentPane().getHeight();
                        jInternalFrame.setMaximum(false);
                        int fwidth = jInternalFrame.getWidth();
                        int fheight = jInternalFrame.getHeight();
                        jInternalFrame.reshape(xx, yy, fwidth, fheight);
                        xx += frameDistance;
                        yy += frameDistance;
                        if (xx + fwidth > desktop.getWidth()) {
                            xx = 0;
                        }
                        if (yy + fheight > desktop.getHeight()) {
                            yy = 0;
                        }
                    } catch (PropertyVetoException e) {
                    }
                }
            }
        } else if (ToolMenuItems.VERSION.equals(evt.getActionCommand())) {
            JFrame f = new Versions();
            centerFrame(f);
            f.setVisible(true);
        } else {
            try {
                createFrame(evt.getActionCommand());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Vector<String> getMenulist() {
        return menulist;
    }

    /**
     * A Class that redirects output to System.out and System.err.
     */
    public class Console {

        PipedInputStream piOut;
        PipedInputStream piErr;
        PipedOutputStream poOut;
        PipedOutputStream poErr;
        ErrorContext errorcontext = new ErrorContext();
        JTextPane textArea = new JTextPane(new DefaultStyledDocument(
                errorcontext));
        PrintStream oriout;
        PrintStream orierr;

        /**
         * Creates a new Console object.
         *
         * @throws IOException on error
         */
        public Console() throws IOException {
            // Set up System.out
            piOut = new PipedInputStream();
            poOut = new PipedOutputStream(piOut);
            oriout = System.out;
            System.setOut(new PrintStream(poOut, true));

            // Set up System.err
            piErr = new PipedInputStream();
            poErr = new PipedOutputStream(piErr);
            orierr = System.err;
            System.setErr(new PrintStream(poErr, true));

            // Add a scrolling text area
            textArea.setEditable(false);

            // Create reader threads
            new ReaderThread(piOut, ErrorContext.STDOUT).start();
            new ReaderThread(piErr, ErrorContext.STDERROR).start();
        }

        class ErrorContext extends StyleContext {

            public static final String STDERROR = "Error";
            public static final String STDOUT = "StdOut";
            private static final long serialVersionUID = 7766294638325167438L;

            public ErrorContext() {
                super();
                Style root = getStyle(DEFAULT_STYLE);
                Style s = addStyle(STDERROR, root);
                StyleConstants.setForeground(s, Color.RED);
                s = addStyle(STDOUT, root);
                StyleConstants.setForeground(s, Color.BLACK);
            }
        }

        class ReaderThread extends Thread {

            PipedInputStream pi;
            String type;

            ReaderThread(PipedInputStream pi, String type) {
                this.pi = pi;
                this.type = type;
            }

            /**
             * @see java.lang.Thread#run()
             */
            public void run() {
                final byte[] buf = new byte[1024];

                while (true) {
                    try {
                        final int len = pi.read(buf);
                        if (len == -1) {
                            break;
                        }
                        javax.swing.text.Document doc = textArea.getDocument();
                        AttributeSet attset = errorcontext.getStyle(type);
                        String snippet = new String(buf, 0, len);
                        doc.insertString(doc.getLength(),
                                snippet, attset);
                        oriout.print(snippet);
                        textArea.setCaretPosition(textArea.getDocument().getLength());
                    } catch (BadLocationException | IOException ex) {
                    }
                }
            }
        }
    }
}
