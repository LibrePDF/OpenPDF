/*
 * $Id: Console.java 3242 2008-04-13 23:00:20Z xlv $
 *
 * Copyright 2007 Bruno Lowagie.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.lowagie.rups.view;

import java.awt.Color;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

/**
 * A Class that redirects everything written to System.out and System.err
 * to a JTextPane.
 */
public class Console implements Observer {

	/** Single Console instance. */
	private static Console console = null;
	
    /** Custom PrintStream. */
    PrintStream printStream;
    /** Custom OutputStream. */
    PipedOutputStream poCustom;
    /** Custom InputStream. */
    PipedInputStream piCustom;
    
    /** OutputStream for System.out. */
    PipedOutputStream poOut;
	/** InputStream for System.out. */
    PipedInputStream piOut;

    /** OutputStream for System.err. */
    PipedOutputStream poErr;
    /** InputStream for System.err. */
    PipedInputStream piErr;
    
    /** The StyleContext for the Console. */
    ConsoleStyleContext styleContext = new ConsoleStyleContext();
    
    /** The text area to which everything is written. */
    JTextPane textArea = new JTextPane(new DefaultStyledDocument(styleContext));

    /**
     * Creates a new Console object.
     * @throws IOException
     */
    private Console() throws IOException {
    	// Set up Custom
    	piCustom = new PipedInputStream();
    	poCustom = new PipedOutputStream();
        printStream = new PrintStream(poCustom);
    	
        // Set up System.out
        piOut = new PipedInputStream();
        poOut = new PipedOutputStream(piOut);
        System.setOut(new PrintStream(poOut, true));

        // Set up System.err
        piErr = new PipedInputStream();
        poErr = new PipedOutputStream(piErr);
        System.setErr(new PrintStream(poErr, true));

        // Add a scrolling text area
        textArea.setEditable(false);

        // Create reader threads
        new ReadWriteThread(piCustom, ConsoleStyleContext.CUSTOM).start();
        new ReadWriteThread(piOut, ConsoleStyleContext.SYSTEMOUT).start();
        new ReadWriteThread(piErr, ConsoleStyleContext.SYSTEMERR).start();
    }

    /**
     * Console is a Singleton class: you can only get one Console.
     */
    public static synchronized Console getInstance() {
    	if (console == null) {
    		try {
				console = new Console();
			} catch (IOException e) {
				console = null;
			}
    	}
    	return console;
    }

	/**
	 * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
	 */
	public void update(Observable observable, Object obj) {
		if (RupsMenuBar.CLOSE.equals(obj)) {
			textArea.setText("");
		}
		if (RupsMenuBar.OPEN.equals(obj)) {
			textArea.setText("");
		}
	}
	
    /**
     * Allows you to print something to the custom PrintStream.
     * @param	s	the message you want to send to the Console
     */
	public static void println(String s) {
		PrintStream ps = getInstance().getPrintStream();
		if (ps == null) {
			System.out.println(s);
		}
		else {
			ps.println(s);
			ps.flush();
		}
	}

    /**
     * Get the custom PrintStream of the console.
     */
	public PrintStream getPrintStream() {
		return printStream;
	}

	/**
	 * Get the JTextArea to which everything is written.
	 */
	public JTextPane getTextArea() {
		return textArea;
	}
    
	/**
	 * The thread that will write everything to the text area.
	 */
    class ReadWriteThread extends Thread {
    	/** The InputStream of this Thread */
        PipedInputStream pi;
        /** The type (CUSTOM, SYSTEMOUT, SYSTEMERR) of this Thread */
        String type;

        /** Create the ReaderThread. */
        ReadWriteThread(PipedInputStream pi, String type) {
        	super();
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
                    Document doc = textArea.getDocument();
                    AttributeSet attset = styleContext.getStyle(type);
                    String snippet = new String(buf, 0, len);
                    doc.insertString(doc.getLength(),
                                     snippet, attset);
                    printStream.print(snippet);
                    textArea.setCaretPosition(textArea.getDocument().
                                              getLength());
                } catch (BadLocationException ex) {
                } catch (IOException e) {
                }
            }
        }
    }	
    
    /**
     * The style context defining the styles of each type of PrintStream.
     */
    class ConsoleStyleContext extends StyleContext {

        /** A Serial Version UID. */
		private static final long serialVersionUID = 7253870053566811171L;
		/** The name of the Style used for Custom messages */
		public static final String CUSTOM = "Custom";
		/** The name of the Style used for System.out */
        public static final String SYSTEMOUT = "SystemOut";
		/** The name of the Style used for System.err */
		public static final String SYSTEMERR = "SystemErr";

        /** Creates the style context for the Console. */
        public ConsoleStyleContext() {
            super();
            Style root = getStyle(DEFAULT_STYLE);
            Style s = addStyle(CUSTOM, root);
            StyleConstants.setForeground(s, Color.BLACK);
            s = addStyle(SYSTEMOUT, root);
            StyleConstants.setForeground(s, Color.GREEN);
            s = addStyle(SYSTEMERR, root);
            StyleConstants.setForeground(s, Color.RED);
        }
    }
}
