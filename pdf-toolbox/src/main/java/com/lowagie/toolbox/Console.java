package com.lowagie.toolbox;

import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import java.awt.Color;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;

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
