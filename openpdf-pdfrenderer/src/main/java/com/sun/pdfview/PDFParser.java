/*
 * Copyright 2004 Sun Microsystems, Inc., 4150 Network Circle,
 * Santa Clara, California 95054, U.S.A. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
 */
package com.sun.pdfview;
import static java.awt.geom.Path2D.WIND_EVEN_ODD;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Stack;

import com.sun.pdfview.PDFDebugger.DebugStopException;
import com.sun.pdfview.colorspace.PDFColorSpace;
import com.sun.pdfview.colorspace.PatternSpace;
import com.sun.pdfview.decode.PDFDecoder;
import com.sun.pdfview.font.PDFFont;
import com.sun.pdfview.pattern.PDFShader;

/**
* PDFParser is the class that parses a PDF content stream and
* produces PDFCmds for a PDFPage. You should never ever see it run:
* it gets created by a PDFPage only if needed, and may even run in
* its own thread.
*
* @author Mike Wessler
*/
public class PDFParser extends BaseWatchable {
    private int mDebugCommandIndex;
    // ---- parsing variables
    private Stack<Object> stack; // stack of Object
    private Stack<ParserState> parserStates; // stack of RenderState
    // the current render state
    private ParserState state;
    private GeneralPath path;
    private int clip;
    private int loc;
    private boolean resend = false;
    private Tok tok;
    private boolean catchexceptions = true; // Indicates state of BX...EX
    /**
    * a weak reference to the page we render into. For the page
    * to remain available, some other code must retain a strong reference to
    * it.
    */
    private final WeakReference<PDFPage> pageRef;
    /**
    * the actual command, for use within a singe iteration. Note that
    * this must be released at the end of each iteration to assure the
    * page can be collected if not in use
    */
    private PDFPage cmds;
    // ---- result variables
    byte[] stream;
    HashMap<String, PDFObject> resources;

    boolean errorwritten = false;
    private boolean autoAdjustStroke = false;
    private boolean strokeOverprint;
    private int strokeOverprintMode;
    private boolean fillOverprint;
    private int fillOverprintMode;
	private boolean addAnnotation;

    /**
    * Don't call this constructor directly. Instead, use
    * PDFFile.getPage(int pagenum) to get a PDFPage. There should
    * never be any reason for a user to create, access, or hold
    * on to a PDFParser.
    */
    public PDFParser(PDFPage cmds, byte[] stream, HashMap<String, PDFObject> resources) {
        super();
        this.pageRef = new WeakReference<PDFPage>(cmds);
        this.resources = resources;
        if (resources == null) {
            this.resources = new HashMap<String, PDFObject>();
        }
        this.stream = stream;
    }

    // ///////////////////////////////////////////////////////////////
    // B E G I N R E A D E R S E C T I O N
    // ///////////////////////////////////////////////////////////////
    /**
    * a token from a PDF Stream
    */
    static class Tok {
        /** begin bracket &lt; */
        public static final int BRKB = 11;
        /** end bracket &gt; */
        public static final int BRKE = 10;
        /** begin array [ */
        public static final int ARYB = 9;
        /** end array ] */
        public static final int ARYE = 8;
        /** String (, readString looks for trailing ) */
        public static final int STR = 7;
        /** begin brace { */
        public static final int BRCB = 5;
        /** end brace } */
        public static final int BRCE = 4;
        /** number */
        public static final int NUM = 3;
        /** keyword */
        public static final int CMD = 2;
        /** name (begins with /) */
        public static final int NAME = 1;
        /** unknown token */
        public static final int UNK = 0;
        /** end of stream */
        public static final int EOF = -1;
        /** the string value of a STR, NAME, or CMD token */
        public String name;
        /** the value of a NUM token */
        public double value;
        /** the type of the token */
        public int type;

        /** a printable representation of the token */
        @Override
        public String toString() {
            if (this.type == NUM) {
                return "NUM: " + this.value;
            } else if (this.type == CMD) {
                return "CMD: " + this.name;
            } else if (this.type == UNK) {
                return "UNK";
            } else if (this.type == EOF) {
                return "EOF";
            } else if (this.type == NAME) {
                return "NAME: " + this.name;
            } else if (this.type == CMD) {
                return "CMD: " + this.name;
            } else if (this.type == STR) {
                return "STR: (" + this.name;
            } else if (this.type == ARYB) {
                return "ARY [";
            } else if (this.type == ARYE) {
                return "ARY ]";
            } else {
                return "some kind of brace (" + this.type + ")";
            }
        }

        /**
         * reset the token to it's original state
         */
        public void reset() {
            name = null;
            value = 0.0;
            type = UNK;
        }
    }

    /**
    * get the next token.
    */
    private Tok nextToken() {
        if (this.resend) {
            this.resend = false;
            return this.tok;
        }
        if (this.tok != null) {
            this.tok.reset();
        } else {
            tok = new Tok();
        }
        // skip whitespace
        while (this.loc < this.stream.length && PDFFile.isWhiteSpace(this.stream[this.loc])) {
            this.loc++;
        }
        if (this.loc >= this.stream.length) {
            this.tok.type = Tok.EOF;
            return this.tok;
        }
        int c = this.stream[this.loc++];
        // examine the character:
        while (c == '%' || c == 28) {
            // skip comments
            StringBuffer comment = new StringBuffer();
            while (this.loc < this.stream.length && c != '\n') {
                comment.append((char) c);
                c = this.stream[this.loc++];
            }
            if (this.loc < this.stream.length) {
                c = this.stream[this.loc++]; // eat the newline
                if (c == '\r') {
                    c = this.stream[this.loc++]; // eat a following return
                }
                // skip whitespace
                while (this.loc < this.stream.length && PDFFile.isWhiteSpace(c)) {
                	c = this.stream[this.loc++];
                }
            }
            PDFDebugger.debug("Read comment: " + comment.toString(), -1);
        }
        if (c == '[') {
            this.tok.type = Tok.ARYB;
        } else if (c == ']') {
            this.tok.type = Tok.ARYE;
        } else if (c == '(') {
            // read a string
            this.tok.type = Tok.STR;
            this.tok.name = readString();
        } else if (c == '{') {
            this.tok.type = Tok.BRCB;
        } else if (c == '}') {
            this.tok.type = Tok.BRCE;
        } else if (c == '<' && this.stream[this.loc++] == '<') {
            this.tok.type = Tok.BRKB;
        } else if (c == '>' && this.stream[this.loc++] == '>') {
            this.tok.type = Tok.BRKE;
        } else if (c == '<') {
            this.loc--;
            this.tok.type = Tok.STR;
            this.tok.name = readByteArray();
        } else if (c == '/') {
            this.tok.type = Tok.NAME;
            this.tok.name = readName();
        } else if (c == '.' || c == '-' || (c >= '0' && c <= '9')) {
            this.loc--;
            this.tok.type = Tok.NUM;
            this.tok.value = readNum();
        } else if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '\'' || c == '"') {
            this.loc--;
            this.tok.type = Tok.CMD;
            this.tok.name = readName();
        } else {
            PDFDebugger.debug("Encountered character: " + c + " (" + (char) c + ")", 1);
            this.tok.type = Tok.UNK;
        }
        return this.tok;
    }

    /**
    * read a name (sequence of non-PDF-delimiting characters) from the
    * stream.
    */
    private String readName() {
        int start = this.loc;
        while (this.loc < this.stream.length && PDFFile.isRegularCharacter(this.stream[this.loc])) {
            this.loc++;
        }
        return new String(this.stream, start, this.loc - start);
    }

    /**
    * read a floating point number from the stream
    */
    private double readNum() {
        int c = this.stream[this.loc++];
        boolean neg = c == '-';
        boolean sawdot = c == '.';
        double dotmult = sawdot ? 0.1 : 1;
        double value = (c >= '0' && c <= '9') ? c - '0' : 0;
        while (true) {
            c = this.stream[this.loc++];
            if (c == '.') {
                if (sawdot) {
                    this.loc--;
                    break;
                }
                sawdot = true;
                dotmult = 0.1;
            } else if (c >= '0' && c <= '9') {
                int val = c - '0';
                if (sawdot) {
                    value += val * dotmult;
                    dotmult *= 0.1;
                } else {
                    value = value * 10 + val;
                }
            } else {
                this.loc--;
                break;
            }
        }
        if (neg) {
            value = -value;
        }
        return value;
    }

    /**
    * <p>
    * read a String from the stream. Strings begin with a '(' character, which has already been
    * read, and end with a balanced ')' character. A '\' character starts an escape sequence of up
    * to three octal digits.
    * </p>
    *
    * <p>
    * Parenthesis must be enclosed by a balanced set of parenthesis, so a string may enclose
    * balanced parenthesis.
    * </p>
    *
    * @return the string with escape sequences replaced with their
    * values
    */
    private String readString() {
        int parenLevel = 0;
        StringBuffer sb = new StringBuffer();
        while (this.loc < this.stream.length) {
            int c = this.stream[this.loc++];
            if (c == ')') {
                if (parenLevel-- == 0) {
                    break;
                }
            } else if (c == '(') {
                parenLevel++;
            } else if (c == '\\') {
                // escape sequences
                c = this.stream[this.loc++];
                if (c >= '0' && c < '8') {
                    int count = 0;
                    int val = 0;
                    while (c >= '0' && c < '8' && count < 3) {
                        val = val * 8 + c - '0';
                        c = this.stream[this.loc++];
                        count++;
                    }
                    this.loc--;
                    c = val;
                } else if (c == 'n') {
                    c = '\n';
                } else if (c == 'r') {
                    c = '\r';
                } else if (c == 't') {
                    c = '\t';
                } else if (c == 'b') {
                    c = '\b';
                } else if (c == 'f') {
                    c = '\f';
                } else if (c == '\n' || c == '\r') {
                    continue;
                }
            }
            sb.append((char) c);
        }
        return sb.toString();
    }

    /**
    * read a byte array from the stream. Byte arrays begin with a '<'
    * character, which has already been read, and end with a '>'
    * character. Each byte in the array is made up of two hex characters,
    * the first being the high-order bit.
    *
    * We translate the byte arrays into char arrays by combining two bytes
    * into a character, and then translate the character array into a string.
    * [JK FIXME this is probably a really bad idea!]
    *
    * @return the byte array
    */
    private String readByteArray() {
        StringBuffer buf = new StringBuffer();
        int count = 0;
        char w = (char) 0;
        // read individual bytes and format into a character array
        while ((this.loc < this.stream.length) && (this.stream[this.loc] != '>')) {
            char c = (char) this.stream[this.loc];
            byte b = (byte) 0;
            if (c >= '0' && c <= '9') {
                b = (byte) (c - '0');
            } else if (c >= 'a' && c <= 'f') {
                b = (byte) (10 + (c - 'a'));
            } else if (c >= 'A' && c <= 'F') {
                b = (byte) (10 + (c - 'A'));
            } else {
                this.loc++;
                continue;
            }
            // calculate where in the current byte this character goes
            int offset = 1 - (count % 2);
            w |= (0xf & b) << (offset * 4);
            // increment to the next char if we've written four bytes
            if (offset == 0) {
                buf.append(w);
                w = (char) 0;
            }
            count++;
            this.loc++;
        }
        // ignore trailing '>'
        this.loc++;
        return buf.toString();
    }

    // ///////////////////////////////////////////////////////////////
    // B E G I N P A R S E R S E C T I O N
    // ///////////////////////////////////////////////////////////////
    /**
    * Called to prepare for some iterations
    */
    @Override
    public void setup() {
        this.stack = new Stack<Object>();
        this.parserStates = new Stack<ParserState>();
        this.state = new ParserState();
        this.path = new GeneralPath();
        this.loc = 0;
        this.clip = 0;
        // initialize the ParserState
        this.state.fillCS = PDFColorSpace.getColorSpace(PDFColorSpace.COLORSPACE_GRAY);
        this.state.strokeCS = PDFColorSpace.getColorSpace(PDFColorSpace.COLORSPACE_GRAY);
        this.state.textFormat = new PDFTextFormat();
    }

    /**
    * parse the stream. commands are added to the PDFPage initialized
    * in the constructor as they are encountered.
    * <p>
    * Page numbers in comments refer to the Adobe PDF specification.<br>
    * commands are listed in PDF spec 32000-1:2008 in Table A.1
    *
    * @return <ul>
    * <li>Watchable.RUNNING when there are commands to be processed <li>Watchable.COMPLETED
    * when the page is done and all the commands have been processed <li>Watchable.STOPPED
    * if the page we are rendering into is no longer available
    * </ul>
    */
    @SuppressWarnings("unused")
    @Override
    public int iterate() throws Exception {
        // make sure the page is still available, and create the reference
        // to it for use within this iteration
        this.cmds = this.pageRef.get();
        if (this.cmds == null) {
            PDFDebugger.debug("Page gone. Stopping", 10);
            return Watchable.STOPPED;
        }
        Object obj;
        try {
            obj = parseObject();
        } catch (DebugStopException e) {
            return Watchable.STOPPED;
        }
        // if there's nothing left to parse, we're done
        if (obj == null) {
            return Watchable.COMPLETED;
        }
        if (obj instanceof Tok) {
            // it's a command. figure out what to do.
            // (if not, the token will be "pushed" onto the stack)
            String cmd = ((Tok) obj).name;
            PDFDebugger.debug("Command: " + cmd + " (stack size is " + this.stack.size() + ")", 10);
            if (cmd.equals("q")) {
                // push the parser state
                this.parserStates.push((ParserState) this.state.clone());
                // push graphics state
                this.cmds.addPush();
            } else if (cmd.equals("Q")) {
                processQCmd();
            } else if (cmd.equals("cm")) {
                // set transform to array of values
                float[] elts = popFloat(6);
                AffineTransform xform = new AffineTransform(elts);
                this.cmds.addXform(xform);
            } else if (cmd.equals("w")) {
                // set stroke width
                this.cmds.addStrokeWidth(popFloat());
            } else if (cmd.equals("J")) {
                // set end cap style
                this.cmds.addEndCap(popInt());
            } else if (cmd.equals("j")) {
                // set line join style
                this.cmds.addLineJoin(popInt());
            } else if (cmd.equals("M")) {
                // set miter limit
                this.cmds.addMiterLimit(popInt());
            } else if (cmd.equals("d")) {
                // set dash style and phase
                float phase = popFloat();
                float[] dashary = popFloatArray();
                if (!PDFDebugger.DISABLE_PATH_STROKE) {
                    this.cmds.addDash(dashary, phase);
                }
            } else if (cmd.equals("ri")) {
                popString();
                // TODO: do something with rendering intent (page 197)
            } else if (cmd.equals("i")) {
                popFloat();
                // TODO: do something with flatness tolerance
            } else if (cmd.equals("gs")) {
                // set graphics state to values in a named dictionary
                String popString = popString();
                PDFDebugger.debug("Set GS state "+popString, 10);
                setGSState(popString);
            } else if (cmd.equals("m")) {
                // path move to
                float y = popFloat();
                float x = popFloat();
                this.path.moveTo(x, y);
                PDFDebugger.logPath(path, "2 moved to " + x + ", " + y);
            } else if (cmd.equals("l")) {
                // path line to
                float y = popFloat();
                float x = popFloat();
                this.path.lineTo(x, y);
                PDFDebugger.logPath(path, "1 line to " + x + ", " + y);
            } else if (cmd.equals("c")) {
                // path curve to
                float a[] = popFloat(6);
                this.path.curveTo(a[0], a[1], a[2], a[3], a[4], a[5]);
                PDFDebugger.logPath(path, "1 curve to " + Arrays.toString(a));
            } else if (cmd.equals("v")) {
                // path curve; first control point= start
                float a[] = popFloat(4);
                Point2D cp = this.path.getCurrentPoint();
                this.path.curveTo((float) cp.getX(), (float) cp.getY(), a[0], a[1], a[2], a[3]);
                PDFDebugger.logPath(path, "2 curve to " + Arrays.toString(a) + ", " + cp.getX() + "," + cp.getY());
            } else if (cmd.equals("y")) {
                // path curve; last control point= end
                float a[] = popFloat(4);
                this.path.curveTo(a[0], a[1], a[2], a[3], a[2], a[3]);
                PDFDebugger.logPath(path, "3 curve to " + Arrays.toString(a));
            } else if (cmd.equals("h")) {
                tryClosingPath();
                PDFDebugger.logPath(path, "closed");
            } else if (cmd.equals("re")) {
                // path add rectangle
                float a[] = popFloat(4);
                this.path.moveTo(a[0], a[1]);
                PDFDebugger.logPath(path, "1 moved to " + a[0] + "," + a[1]);
                this.path.lineTo(a[0] + a[2], a[1]);
                PDFDebugger.logPath(path, "2 line to " + (a[0] + a[2]) + "," + a[1]);
                this.path.lineTo(a[0] + a[2], a[1] + a[3]);
                PDFDebugger.logPath(path, "3 line to " + (a[0] + a[2]) + "," + (a[1] + a[3]));
                this.path.lineTo(a[0], a[1] + a[3]);
                PDFDebugger.logPath(path, "4 line to " + a[0] + "," + (a[1] + a[3]));
                tryClosingPath();
                PDFDebugger.logPath(path, "closed");
            } else if (cmd.equals("S")) {
                // stroke the path
                if (!PDFDebugger.DISABLE_PATH_STROKE || (!PDFDebugger.DISABLE_CLIP && this.clip == PDFShapeCmd.CLIP)) {
                    if(autoAdjustStroke || strokeOverprint || fillOverprint) {
                        path.closePath();
                        PDFDebugger.logPath(path, "closed");
                    }
                    this.cmds.addPath(this.path, PDFShapeCmd.STROKE | this.clip, this.autoAdjustStroke);
                }
                this.clip = 0;
                this.path = new GeneralPath();
                PDFDebugger.logPath(path, "new path");
            } else if (cmd.equals("s")) {
                tryClosingPath();
                PDFDebugger.logPath(path, "closed");
                if (!PDFDebugger.DISABLE_PATH_STROKE || (!PDFDebugger.DISABLE_CLIP && this.clip == PDFShapeCmd.CLIP)) {
                    this.cmds.addPath(this.path, PDFShapeCmd.STROKE | this.clip, this.autoAdjustStroke);
                }
                this.clip = 0;
                this.path = new GeneralPath();
                PDFDebugger.logPath(path, "new path");
            } else if (cmd.equals("f") || cmd.equals("F")) {
                 tryClosingPath();
                // fill the path (close/not close identical)
                if (!PDFDebugger.DISABLE_PATH_FILL || (!PDFDebugger.DISABLE_CLIP && this.clip == PDFShapeCmd.CLIP)) {
                    this.cmds.addPath(this.path, PDFShapeCmd.FILL | this.clip, this.autoAdjustStroke);
                }
                this.clip = 0;
                this.path = new GeneralPath();
                PDFDebugger.logPath(path, "new path");
            } else if (cmd.equals("f*")) {
                // fill the path using even/odd rule
                this.path.setWindingRule(WIND_EVEN_ODD);
                PDFDebugger.logPath(path, "set winding rule" + WIND_EVEN_ODD);
                if (!PDFDebugger.DISABLE_PATH_FILL || (!PDFDebugger.DISABLE_CLIP && this.clip == PDFShapeCmd.CLIP)) {
                    this.cmds.addPath(this.path, PDFShapeCmd.FILL | this.clip, this.autoAdjustStroke);
                }
                this.clip = 0;
                this.path = new GeneralPath();
                PDFDebugger.logPath(path, "new path");
            } else if (cmd.equals("B")) {
                // fill and stroke the path
                if (!PDFDebugger.DISABLE_PATH_STROKE_FILL || (!PDFDebugger.DISABLE_CLIP && this.clip == PDFShapeCmd.CLIP)) {
                    this.cmds.addPath(this.path, PDFShapeCmd.BOTH | this.clip, this.autoAdjustStroke);
                }
                this.clip = 0;
                this.path = new GeneralPath();
                PDFDebugger.logPath(path, "new path");
            } else if (cmd.equals("B*")) {
                // fill path using even/odd rule and stroke it
                this.path.setWindingRule(WIND_EVEN_ODD);
                PDFDebugger.logPath(path, "set winding rule" + WIND_EVEN_ODD);
                if (!PDFDebugger.DISABLE_PATH_STROKE_FILL || (!PDFDebugger.DISABLE_CLIP && this.clip == PDFShapeCmd.CLIP)) {
                    this.cmds.addPath(this.path, PDFShapeCmd.BOTH | this.clip, this.autoAdjustStroke);
                }
                this.clip = 0;
                this.path = new GeneralPath();
                PDFDebugger.logPath(path, "new path");
            } else if (cmd.equals("b")) {
                tryClosingPath();
                PDFDebugger.logPath(path, "close");
                if (!PDFDebugger.DISABLE_PATH_STROKE_FILL || (!PDFDebugger.DISABLE_CLIP && this.clip == PDFShapeCmd.CLIP)) {
                    this.cmds.addPath(this.path, PDFShapeCmd.BOTH | this.clip, this.autoAdjustStroke);
                }
                this.clip = 0;
                this.path = new GeneralPath();
                PDFDebugger.logPath(path, "new path");
            } else if (cmd.equals("b*")) {
                tryClosingPath();
                PDFDebugger.logPath(path, "close");
                this.path.setWindingRule(WIND_EVEN_ODD);
                PDFDebugger.logPath(path, "set winding rule " + WIND_EVEN_ODD);
                if (!PDFDebugger.DISABLE_PATH_STROKE_FILL || (!PDFDebugger.DISABLE_CLIP && this.clip == PDFShapeCmd.CLIP)) {
                    this.cmds.addPath(this.path, PDFShapeCmd.BOTH | this.clip, this.autoAdjustStroke);
                }
                this.clip = 0;
                this.path = new GeneralPath();
                PDFDebugger.logPath(path, "new path");
            } else if (cmd.equals("n")) {
                if (path.getCurrentPoint() != null) {
                    tryClosingPath();
                    PDFDebugger.logPath(path, "closed");
                }
                // clip with the path and discard it
                if (!PDFDebugger.DISABLE_CLIP) {
                    if (this.clip != 0) {
                        this.cmds.addPath(this.path, this.clip, this.autoAdjustStroke);
                    }
                }
                this.clip = 0;
                this.path = new GeneralPath();
                PDFDebugger.logPath(path, "new path");
            } else if (cmd.equals("W")) {
                // mark this path for clipping!
                this.clip = PDFShapeCmd.CLIP;
            } else if (cmd.equals("W*")) {
                // mark this path using even/odd rule for clipping
                this.path.setWindingRule(WIND_EVEN_ODD);
                PDFDebugger.logPath(path, "set winding rule " + WIND_EVEN_ODD);
                this.clip = PDFShapeCmd.CLIP;
            } else if (cmd.equals("sh")) {
                // shade a region that is defined by the shader itself.
                // shading the current space from a dictionary
                // should only be used for limited-dimension shadings
                String gdictname = popString();
                // set up the pen to do a gradient fill according
                // to the dictionary
                PDFObject shobj = findResource(gdictname, "Shading");
                if (!PDFDebugger.DISABLE_SHADER) {
                    doShader(shobj);
                }
            } else if (cmd.equals("CS")) {
                // set the stroke color space
                this.state.strokeCS = parseColorSpace(new PDFObject(this.stack.pop()));
            } else if (cmd.equals("cs")) {
                // set the fill color space
                this.state.fillCS = parseColorSpace(new PDFObject(this.stack.pop()));
            } else if (cmd.equals("SC")) {
                // set the stroke color
                int n = this.state.strokeCS.getNumComponents();
                this.cmds.addStrokePaint(this.state.strokeCS.getPaint(popFloat(n)));
            } else if (cmd.equals("SCN")) {
                // set the stroke colour
                if (this.state.strokeCS instanceof PatternSpace) {
                    this.cmds.addFillPaint(doPattern((PatternSpace) this.state.strokeCS));
                } else {
                    int n = this.state.strokeCS.getNumComponents();
                    this.cmds.addStrokePaint(this.state.strokeCS.getPaint(popFloat(n)));
                }
            } else if (cmd.equals("sc")) {
                // set the fill color
                int n = this.state.fillCS.getNumComponents();
                this.cmds.addFillPaint(this.state.fillCS.getPaint(popFloat(n)));
            } else if (cmd.equals("scn")) {
                if (this.state.fillCS instanceof PatternSpace) {
                    this.cmds.addFillPaint(doPattern((PatternSpace) this.state.fillCS));
                } else {
                    int n = this.state.fillCS.getNumComponents();
                    this.cmds.addFillPaint(this.state.fillCS.getPaint(popFloat(n)));
                }
            } else if (cmd.equals("G")) {
                // set the stroke color to a Gray value
                this.state.strokeCS = PDFColorSpace.getColorSpace(PDFColorSpace.COLORSPACE_GRAY);
                this.cmds.addStrokePaint(this.state.strokeCS.getPaint(popFloat(1)));
            } else if (cmd.equals("g")) {
                // set the fill color to a Gray value
                this.state.fillCS = PDFColorSpace.getColorSpace(PDFColorSpace.COLORSPACE_GRAY);
                this.cmds.addFillPaint(this.state.fillCS.getPaint(popFloat(1)));
            } else if (cmd.equals("RG")) {
                // set the stroke color to an RGB value
                this.state.strokeCS = PDFColorSpace.getColorSpace(PDFColorSpace.COLORSPACE_RGB);
                this.cmds.addStrokePaint(this.state.strokeCS.getPaint(popFloat(3)));
            } else if (cmd.equals("rg")) {
                // set the fill color to an RGB value
                this.state.fillCS = PDFColorSpace.getColorSpace(PDFColorSpace.COLORSPACE_RGB);
                this.cmds.addFillPaint(this.state.fillCS.getPaint(popFloat(3)));
            } else if (cmd.equals("K")) {
//                if(strokeOverprint && strokeOverprintMode == 1) {
//                    if (this.state.strokeCS instanceof PatternSpace) {
//                        this.cmds.addFillPaint(doPattern((PatternSpace) this.state.strokeCS));
//                    } else {
//                        int n = this.state.strokeCS.getNumComponents();
//                        this.cmds.addStrokePaint(this.state.strokeCS.getPaint(popFloat(n)));
//                    }
//                }else {
                    // set the stroke color to a CMYK value                
                    this.state.strokeCS = PDFColorSpace.getColorSpace(PDFColorSpace.COLORSPACE_CMYK);
                    this.cmds.addStrokePaint(this.state.strokeCS.getPaint(popFloat(4)));
//                }
            } else if (cmd.equals("k")) {
//                if(fillOverprint && fillOverprintMode == 1) {
//                    // if OP = true and OPM = 1 apply the same as in "scn"
//                    if (this.state.fillCS instanceof PatternSpace) {
//                        this.cmds.addFillPaint(doPattern((PatternSpace) this.state.fillCS));
//                    } else {
//                        // set the fill color to a CMYK value
//                        int n = this.state.fillCS.getNumComponents();
//                        this.cmds.addFillPaint(this.state.fillCS.getPaint(popFloat(n)));
//                    }
//                }else {
                    this.state.fillCS = PDFColorSpace.getColorSpace(PDFColorSpace.COLORSPACE_CMYK);
                    this.cmds.addFillPaint(this.state.fillCS.getPaint(popFloat(4)));
//                }
            } else if (cmd.equals("Do")) {
                // make a do call on the referenced object
                String name = popString();
                if (PDFDebugger.DEBUG_IMAGES) {
                    PDFDebugger.debug("XObject reference to " + name);
                }
                PDFObject xobj = findResource(name, "XObject");
                doXObject(xobj);
            } else if (cmd.equals("BT")) {
                processBTCmd();
            } else if (cmd.equals("ET")) {
                // end of text. noop
                this.state.textFormat.end();
            } else if (cmd.equals("Tc")) {
                // set character spacing
                this.state.textFormat.setCharSpacing(popFloat());
            } else if (cmd.equals("Tw")) {
                // set word spacing
                this.state.textFormat.setWordSpacing(popFloat());
            } else if (cmd.equals("Tz")) {
                // set horizontal scaling
                this.state.textFormat.setHorizontalScale(popFloat());
            } else if (cmd.equals("TL")) {
                // set leading
                this.state.textFormat.setLeading(popFloat());
            } else if (cmd.equals("Tf")) {
                // set text font
                float sz = popFloat();
                String fontref = popString();
                this.state.textFormat.setFont(getFontFrom(fontref), sz);
            } else if (cmd.equals("Tr")) {
                // set text rendering mode
                this.state.textFormat.setMode(popInt());
            } else if (cmd.equals("Ts")) {
                // set text rise
                this.state.textFormat.setRise(popFloat());
            } else if (cmd.equals("Td")) {
                // set text matrix location
                float y = popFloat();
                float x = popFloat();
                this.state.textFormat.carriageReturn(x, y);
            } else if (cmd.equals("TD")) {
                // set leading and matrix: -y TL x y Td
                float y = popFloat();
                float x = popFloat();
                this.state.textFormat.setLeading(-y);
                this.state.textFormat.carriageReturn(x, y);
            } else if (cmd.equals("Tm")) {
                // set text matrix
                this.state.textFormat.setMatrix(popFloat(6));
            } else if (cmd.equals("T*")) {
                // go to next line
                this.state.textFormat.carriageReturn();
            } else if (cmd.equals("Tj")) {
                // show text
                this.state.textFormat.doText(this.cmds, popString(), this.autoAdjustStroke);
            } else if (cmd.equals("\'")) {
                // next line and show text: T* string Tj
                this.state.textFormat.carriageReturn();
                this.state.textFormat.doText(this.cmds, popString(), this.autoAdjustStroke);
            } else if (cmd.equals("\"")) {
                // draw string on new line with char & word spacing:
                // aw Tw ac Tc string '
                String string = popString();
                float ac = popFloat();
                float aw = popFloat();
                this.state.textFormat.setWordSpacing(aw);
                this.state.textFormat.setCharSpacing(ac);
                this.state.textFormat.doText(this.cmds, string, this.autoAdjustStroke);
            } else if (cmd.equals("TJ")) {
                // show kerned string
                this.state.textFormat.doText(this.cmds, popArray(), this.autoAdjustStroke);
            } else if (cmd.equals("BI")) {
                // parse inline image
                parseInlineImage();
            } else if (cmd.equals("BX")) {
                this.catchexceptions = true; // ignore errors
            } else if (cmd.equals("EX")) {
                this.catchexceptions = false; // stop ignoring errors
            } else if (cmd.equals("MP")) {
                // mark point (role= mark role name)
                popString();
            } else if (cmd.equals("DP")) {
                // mark point with dictionary (role, ref)
                // result is either inline dict or name in "Properties" rsrc
                this.stack.pop();
                popString();
            } else if (cmd.equals("BMC")) {
                // begin marked content (role)
                popString();
            } else if (cmd.equals("BDC")) {
                // begin marked content with dict (role, ref)
                // result is either inline dict or name in "Properties" rsrc
                this.stack.pop();
                popString();
            } else if (cmd.equals("EMC")) {
                // end marked content
            } else if (cmd.equals("d0")) {
                // character width in type3 fonts
                popFloat(2);
            } else if (cmd.equals("d1")) {
                // character width in type3 fonts
                popFloat(6);
            } else if (cmd.equals("QBT")) {// 'Q' & 'BT' mushed together!
                processQCmd();
                processBTCmd();
            } else if (cmd.equals("Qq")) {// 'Q' & 'q' mushed together!
                processQCmd();
                // push the parser state
                this.parserStates.push((ParserState) this.state.clone());
                // push graphics state
                this.cmds.addPush();
            } else if (cmd.equals("qBT")) {// 'q' & 'BT' mushed together!
            // push the parser state
                this.parserStates.push((ParserState) this.state.clone());
                // push graphics state
                this.cmds.addPush();
                processBTCmd();
            } else if (cmd.equals("q1")) {
                PDFDebugger.debug("**** WARNING: Not handled command: " + cmd + " **************************", 10);
            } else if (cmd.equals("q0")) {
                PDFDebugger.debug("**** WARNING: Not handled command: " + cmd + " **************************", 10);
            } else {
                if (this.catchexceptions) {
                    PDFDebugger.debug("**** WARNING: Unknown command: " + cmd + " **************************", 10);
                } else {
                    throw new PDFParseException("Unknown command: " + cmd);
                }
            }
            if (this.stack.size() != 0) {
                PDFDebugger.debug("**** WARNING! Stack not zero! (cmd=" + cmd + ", size=" + this.stack.size() + ") *************************", 10);
                this.stack.setSize(0);
            }
        } else {
            this.stack.push(obj);
        }
        // release or reference to the page object, so that it can be
        // gc'd if it is no longer in use
        this.cmds = null;
        return Watchable.RUNNING;
    }

    /**
     * Try to close a path but don't fail with exception if this is not working.
     * This is just a workaround for some PDFs with wrong content...
     */
    private void tryClosingPath() {
        try {
            this.path.closePath();
            PDFDebugger.logPath(path, "closed");
        }catch(java.awt.geom.IllegalPathStateException e) {
            PDFDebugger.debug("Failed to close path", 1000);
        }
    }

    @SuppressWarnings("unused")
    private void onNextObject(Tok obj) throws DebugStopException {
        String progress;
        if (true) {
            double percent = (100d * this.loc) / this.stream.length;
            NumberFormat nf = NumberFormat.getInstance();
            nf.setMinimumFractionDigits(1);
            nf.setMaximumFractionDigits(1);
            progress = nf.format(percent) + "%";
        } else {
            progress = this.loc + " of " + this.stream.length;
        }
        String operators = "";
        for (Object operator : this.stack) {
            operators += operator + " ";
        }
        if (PDFDebugger.DEBUG_OPERATORS) {
            PDFDebugger.debug("parser{" + hashCode() + "} " + progress + ": #" + mDebugCommandIndex + " \t" + operators + obj.name);
        }
        mDebugCommandIndex++;
        if (PDFDebugger.DEBUG_STOP_AT_INDEX > 0 && mDebugCommandIndex > PDFDebugger.DEBUG_STOP_AT_INDEX) {
            System.err.println("Debugging: stopped at instruction #" + mDebugCommandIndex);
            throw new DebugStopException();
        }
        if (PDFDebugger.DRAW_DELAY > 0) {
            try {
                Thread.sleep(PDFDebugger.DRAW_DELAY);
            } catch (InterruptedException e) {
            }
        }
    }

    /**
    * abstracted command processing for Q command. Used directly and as
    * part of processing of mushed QBT command.
    */
    private void processQCmd() {
        // pop graphics state ('Q')
        this.cmds.addPop();
        // pop the parser state
        if (this.parserStates.isEmpty() == false) {
            this.state = this.parserStates.pop();
        }
    }

    /**
    * abstracted command processing for BT command. Used directly and as
    * part of processing of mushed QBT command.
    */
    private void processBTCmd() {
        // begin text block: reset everything.
        this.state.textFormat.reset();
    }

    /**
    * Cleanup when iteration is done
    */
    @Override
    public void cleanup() {
        this.state.textFormat.flush();
        this.cmds.finish();
        this.stack = null;
        this.parserStates = null;
        this.state = null;
        this.path = null;
        this.cmds = null;
        this.tok = null;
    }

    public void dumpStreamToError() {
        if (this.errorwritten) {
            return;
        }
        this.errorwritten = true;
        try {
            File oops = File.createTempFile("PDFError", ".err");
            FileOutputStream fos = new FileOutputStream(oops);
            fos.write(this.stream);
            fos.close();
        } catch (IOException ioe) { /* Do nothing */
        }
        ;
    }

    // ///////////////////////////////////////////////////////////////
    // H E L P E R S
    // ///////////////////////////////////////////////////////////////
    /**
    * get a property from a named dictionary in the resources of this
    * content stream.
    *
    * @param name
    * the name of the property in the dictionary
    * @param inDict
    * the name of the dictionary in the resources
    * @return the value of the property in the dictionary
    */
    private PDFObject findResource(String name, String inDict) throws IOException {
        if (inDict != null) {
            PDFObject in = this.resources.get(inDict);
            if (in == null || in.getType() != PDFObject.DICTIONARY) {
                throw new PDFParseException("No dictionary called " + inDict + " found in the resources");
            }
            return in.getDictRef(name);
        } else {
            return this.resources.get(name);
        }
    }

    /**
    * Insert a PDF object into the command stream. The object must
    * either be an Image or a Form, which is a set of PDF commands
    * in a stream.
    *
    * @param obj
    * the object to insert, an Image or a Form.
    */
    private void doXObject(PDFObject obj) throws IOException {
        String type = obj.getDictRef("Subtype").getStringValue();
        if (type == null) {
            type = obj.getDictRef("S").getStringValue();
        }
        if (type.equals("Image")) {
            doImage(obj);
        } else if (type.equals("Form")) {
            doForm(obj);
        } else {
            throw new PDFParseException("Unknown XObject subtype: " + type);
        }
    }

    /**
    * Parse image data into a Java BufferedImage and add the image
    * command to the page.
    *
    * @param obj
    * contains the image data, and a dictionary describing
    * the width, height and color space of the image.
    */
    private void doImage(PDFObject obj) throws IOException {
        if (!PDFDebugger.DISABLE_IMAGES) {
            if (PDFDebugger.DEBUG_IMAGES) {
                final boolean jpegDecode = PDFDecoder.isLastFilter(obj, PDFDecoder.DCT_FILTERS);                
                if (jpegDecode) {
                    PDFDebugger.debug("Image is JPEG");
                } else {
                    PDFDebugger.debug("Image not JPEG");
                }
            }
            this.cmds.addImage(PDFImage.createImage(obj, this.resources, false));
        }
    }

    /**
    * Inject a stream of PDF commands onto the page. Optimized to cache
    * a parsed stream of commands, so that each Form object only needs
    * to be parsed once.
    *
    * @param obj
    * a stream containing the PDF commands, a transformation
    * matrix, bounding box, and resources.
    */
    private void doForm(PDFObject obj) throws IOException {
        // check to see if we've already parsed this sucker
        PDFPage formCmds = (PDFPage) obj.getCache();
        if (formCmds == null) {
            // rats. parse it.
            AffineTransform at;
            Rectangle2D bbox;
            PDFObject matrix = obj.getDictRef("Matrix");
            if (matrix == null) {
                at = new AffineTransform();
            } else {
                float elts[] = new float[6];
                for (int i = 0; i < elts.length; i++) {
                    elts[i] = (matrix.getAt(i)).getFloatValue();
                }
                at = new AffineTransform(elts);
            }
            PDFObject bobj = obj.getDictRef("BBox");
            bbox = new Rectangle2D.Float(bobj.getAt(0).getFloatValue(), bobj.getAt(1).getFloatValue(), bobj.getAt(2).getFloatValue(), bobj.getAt(3).getFloatValue());
            formCmds = new PDFPage(bbox, 0);
            formCmds.addXform(at);
            HashMap<String, PDFObject> r = new HashMap<String, PDFObject>(this.resources);
            PDFObject rsrc = obj.getDictRef("Resources");
            if (rsrc != null) {
                r.putAll(rsrc.getDictionary());
            }
            PDFParser form = new PDFParser(formCmds, obj.getStream(), r);
            form.go(true);
            obj.setCache(formCmds);
        }
        if (!PDFDebugger.DISABLE_FORMS) {
            this.cmds.addPush();
            this.cmds.addCommands(formCmds);
            this.cmds.addPop();
        }
    }

    /**
    * Set the values into a PatternSpace
    */
    private PDFPaint doPattern(PatternSpace patternSpace) throws IOException {
        float[] components = null;
        String patternName = popString();
        PDFObject pattern = findResource(patternName, "Pattern");
        if (pattern == null) {
            throw new PDFParseException("Unknown pattern : " + patternName);
        }
        if (this.stack.size() > 0) {
            components = popFloat(this.stack.size());
        }
        return patternSpace.getPaint(pattern, components, this.resources);
    }

    /**
    * Parse the next object out of the PDF stream. This could be a
    * Double, a String, a HashMap (dictionary), Object[] array, or
    * a Tok containing a PDF command.
    */
    private Object parseObject() throws PDFParseException, DebugStopException {
        Tok t = nextToken();
        if (t.type == Tok.NUM) {
            return Double.valueOf(this.tok.value);
        } else if (t.type == Tok.STR) {
            return this.tok.name;
        } else if (t.type == Tok.NAME) {
            return this.tok.name;
        } else if (t.type == Tok.BRKB) {
            HashMap<String, PDFObject> hm = new HashMap<String, PDFObject>();
            String name = null;
            Object obj;
            while ((obj = parseObject()) != null) {
                if (name == null) {
                    name = (String) obj;
                } else {
                    hm.put(name, new PDFObject(obj));
                    name = null;
                }
            }
            if (this.tok.type != Tok.BRKE) {
                throw new PDFParseException("Inline dict should have ended with '>>'");
            }
            return hm;
        } else if (t.type == Tok.ARYB) {
            // build an array
            ArrayList<Object> ary = new ArrayList<Object>();
            Object obj;
            while ((obj = parseObject()) != null) {
                ary.add(obj);
            }
            if (this.tok.type != Tok.ARYE) {
                throw new PDFParseException("Expected ']'");
            }
            return ary.toArray();
        } else if (t.type == Tok.CMD) {
            onNextObject(t);
            return t;
        }
        PDFDebugger.debug("**** WARNING! parseObject unknown token! (t.type=" + t.type + ") *************************", 10);
        return null;
    }

    /**
    * Parse an inline image. An inline image starts with BI (already
    * read, contains a dictionary until ID, and then image data until
    * EI.
    */
    private void parseInlineImage() throws IOException, DebugStopException {
        // build dictionary until ID, then read image until EI
        HashMap<String, PDFObject> hm = new HashMap<String, PDFObject>();
        while (true) {
            Tok t = nextToken();
            if (t.type == Tok.CMD) {
                onNextObject(t);
                if (t.name.equals("ID")) {
                    break;
                }
            }
            // it should be a name;
            String name = t.name;
            if(PDFDebugger.DEBUG_IMAGES) {
                PDFDebugger.debug("ParseInlineImage, token: " + name);
            }
            if (name.equals("BPC")) {
                name = "BitsPerComponent";
            } else if (name.equals("CS")) {
                name = "ColorSpace";
            } else if (name.equals("D")) {
                name = "Decode";
            } else if (name.equals("DP")) {
                name = "DecodeParms";
            } else if (name.equals("F")) {
                name = "Filter";
            } else if (name.equals("H")) {
                name = "Height";
            } else if (name.equals("IM")) {
                name = "ImageMask";
            } else if (name.equals("W")) {
                name = "Width";
            } else if (name.equals("I")) {
                name = "Interpolate";
            }
            Object vobj = parseObject();
            hm.put(name, new PDFObject(vobj));
        }
        if (this.stream[this.loc] == '\r') {
            this.loc++;
        }
        if (this.stream[this.loc] == '\n' || this.stream[this.loc] == ' ') {
            this.loc++;
        }
        PDFObject imObj = hm.get("ImageMask");
        if (imObj != null && imObj.getBooleanValue()) {
            // [PATCHED by michal.busta@gmail.com] - default value according to PDF spec. is [0, 1]
            // there is no need to swap array - PDF image should handle this values
            Double[] decode = { Double.valueOf(0), Double.valueOf(1) };
            PDFObject decodeObj = hm.get("Decode");
            if (decodeObj != null) {
                decode[0] = Double.valueOf(decodeObj.getAt(0).getDoubleValue());
                decode[1] = Double.valueOf(decodeObj.getAt(1).getDoubleValue());
            }
            hm.put("Decode", new PDFObject(decode));
        }
        PDFObject obj = new PDFObject(null, PDFObject.DICTIONARY, hm);
        int dstart = this.loc;
        // now skip data until a whitespace followed by EI
        while (!PDFFile.isWhiteSpace(this.stream[this.loc]) || this.stream[this.loc + 1] != 'E' || this.stream[this.loc + 2] != 'I') {
            this.loc++;
        }
        // data runs from dstart to loc
        if (PDFDebugger.DEBUG_IMAGES) {
            PDFDebugger.debug("InlineImage from " + dstart + " to " + this.loc);
        }
        byte[] data = new byte[this.loc - dstart];
        System.arraycopy(this.stream, dstart, data, 0, this.loc - dstart);
        obj.setStream(ByteBuffer.wrap(data));
        this.loc += 3;
        doImage(obj);
    }

    /**
    * build a shader from a dictionary.
    */
    private void doShader(PDFObject shaderObj) throws IOException {
        PDFShader shader = PDFShader.getShader(shaderObj, this.resources);
        if(shader == null) {
        	return;
        }
        this.cmds.addPush();
        Rectangle2D bbox = shader.getBBox();
        if (bbox != null) {
            this.cmds.addFillPaint(shader.getPaint());
            this.cmds.addPath(new GeneralPath(bbox), PDFShapeCmd.FILL, this.autoAdjustStroke);
        } else {
            this.cmds.addFillPaint(shader.getPaint());
            this.cmds.addPath(null, PDFShapeCmd.FILL, this.autoAdjustStroke);
        }
        this.cmds.addPop();
    }

    /**
    * get a PDFFont from the resources, given the resource name of the
    * font.
    *
    * @param fontref
    * the resource key for the font
    */
    private PDFFont getFontFrom(String fontref) throws IOException {
        PDFObject obj = findResource(fontref, "Font");
        return PDFFont.getFont(obj, this.resources);
    }

    /**
    * add graphics state commands contained within a dictionary.
    *
    * @param name
    * the resource name of the graphics state dictionary
    */
    private void setGSState(String name) throws IOException {        
        // obj must be a string that is a key to the "ExtGState" dict
        PDFObject gsobj = findResource(name, "ExtGState");
        // TODO: lots of graphic states are not yet considered, see chapter 8.4.5 of the PDF specification.  
        // get LW, LC, LJ, Font, SM, CA, ML, D, RI, FL, BM, ca
        // out of the reference, which is a dictionary
        if (gsobj == null) {
            return;
        }
        PDFObject d;
        boolean handled = false;
        if ((d = gsobj.getDictRef("LW")) != null) {
            this.cmds.addStrokeWidth(d.getFloatValue());
            handled = true;
        }
        if ((d = gsobj.getDictRef("LC")) != null) {
            this.cmds.addEndCap(d.getIntValue());
            handled = true;
        }
        if ((d = gsobj.getDictRef("LJ")) != null) {
            this.cmds.addLineJoin(d.getIntValue());
            handled = true;
        }
        if ((d = gsobj.getDictRef("Font")) != null) {
            this.state.textFormat.setFont(getFontFrom(d.getAt(0).getStringValue()), d.getAt(1).getFloatValue());
            handled = true;
        }
        if ((d = gsobj.getDictRef("ML")) != null) {
            this.cmds.addMiterLimit(d.getFloatValue());
            handled = true;
        }
        if ((d = gsobj.getDictRef("D")) != null) {
            PDFObject pdash[] = d.getAt(0).getArray();
            float dash[] = new float[pdash.length];
            for (int i = 0; i < pdash.length; i++) {
                dash[i] = pdash[i].getFloatValue();
            }
            if (!PDFDebugger.DISABLE_PATH_STROKE) {
                this.cmds.addDash(dash, d.getAt(1).getFloatValue());
            }
            handled = true;
        }
        if ((d = gsobj.getDictRef("CA")) != null) {
            this.cmds.addStrokeAlpha(d.getFloatValue());
            handled = true;
        }
        if ((d = gsobj.getDictRef("ca")) != null) {
            this.cmds.addFillAlpha(d.getFloatValue());
            handled = true;
        }
        if((d = gsobj.getDictRef("SA")) != null) {
            // automatic stroke adjustment
            this.autoAdjustStroke  = d.getBooleanValue();
            handled = true;
        }
        if((d = gsobj.getDictRef("OP")) != null) {
            this.strokeOverprint = d.getBooleanValue();
            PDFObject x = gsobj.getDictRef("OPM");
            if(x!= null) {
                this.strokeOverprintMode = x.getIntValue();
            }
            handled = true;
        }
        if((d = gsobj.getDictRef("op")) != null) {
            this.fillOverprint = d.getBooleanValue();
            PDFObject x = gsobj.getDictRef("OPM");
            if(x!= null) {
                this.fillOverprintMode = x.getIntValue();
            }
            handled = true;
        }
        if(!handled) {
            PDFDebugger.debug("graphic state command unknown!", 10);
        }
    }

    /**
    * generate a PDFColorSpace description based on a PDFObject. The
    * object could be a standard name, or the name of a resource in
    * the ColorSpace dictionary, or a color space name with a defining
    * dictionary or stream.
    */
    private PDFColorSpace parseColorSpace(PDFObject csobj) throws IOException {
        if (csobj == null) {
            return this.state.fillCS;
        }
        return PDFColorSpace.getColorSpace(csobj, this.resources);
    }

    /**
    * pop a single float value off the stack.
    *
    * @return the float value of the top of the stack
    * @throws PDFParseException
    * if the value on the top of the stack
    * isn't a number
    */
    private float popFloat() throws PDFParseException {
        if (this.stack.isEmpty() == false) {
            Object obj = this.stack.pop();
            if (obj instanceof Double) {
                return ((Double) obj).floatValue();
            } else {
                throw new PDFParseException("Expected a number here.");
            }
        }
        return 0;
    }

    /**
    * pop an array of float values off the stack. This is equivalent
    * to filling an array from end to front by popping values off the
    * stack.
    *
    * @param count
    * the number of numbers to pop off the stack
    * @return an array of length <tt>count</tt>
    * @throws PDFParseException
    * if any of the values popped off the
    * stack are not numbers.
    */
    private float[] popFloat(int count) throws PDFParseException {
        float[] ary = new float[count];
        for (int i = count - 1; i >= 0; i--) {
            ary[i] = popFloat();
        }
        return ary;
    }

    /**
    * pop a single integer value off the stack.
    *
    * @return the integer value of the top of the stack
    * @throws PDFParseException
    * if the top of the stack isn't a number.
    */
    private int popInt() throws PDFParseException {
        Object obj = this.stack.pop();
        if (obj instanceof Double) {
            return ((Double) obj).intValue();
        } else {
            throw new PDFParseException("Expected a number here.");
        }
    }

    /**
    * pop an array of integer values off the stack. This is equivalent
    * to filling an array from end to front by popping values off the
    * stack.
    *
    * @param count
    * the number of numbers to pop off the stack
    * @return an array of length <tt>count</tt>
    * @throws PDFParseException
    * if any of the values popped off the
    * stack are not numbers.
    */
    private float[] popFloatArray() throws PDFParseException {
        Object obj = this.stack.pop();
        if (!(obj instanceof Object[])) {
            throw new PDFParseException("Expected an [array] here.");
        }
        Object[] source = (Object[]) obj;
        float[] ary = new float[source.length];
        for (int i = 0; i < ary.length; i++) {
            if (source[i] instanceof Double) {
                ary[i] = ((Double) source[i]).floatValue();
            } else {
                throw new PDFParseException("This array doesn't consist only of floats.");
            }
        }
        return ary;
    }

    /**
    * pop a String off the stack.
    *
    * @return the String from the top of the stack
    * @throws PDFParseException
    * if the top of the stack is not a NAME
    * or STR.
    */
    private String popString() throws PDFParseException {
        Object obj = this.stack.pop();
        if (!(obj instanceof String)) {
            throw new PDFParseException("Expected string here: " + obj.toString());
        } else {
            return (String) obj;
        }
    }

    /**
    * pop a PDFObject off the stack.
    *
    * @return the PDFObject from the top of the stack
    * @throws PDFParseException
    * if the top of the stack does not contain
    * a PDFObject.
    */
    @SuppressWarnings("unused")
    private PDFObject popObject() throws PDFParseException {
        Object obj = this.stack.pop();
        if (!(obj instanceof PDFObject)) {
            throw new PDFParseException("Expected a reference here: " + obj.toString());
        }
        return (PDFObject) obj;
    }

    /**
    * pop an array off the stack
    *
    * @return the array of objects that is the top element of the stack
    * @throws PDFParseException
    * if the top element of the stack does not
    * contain an array.
    */
    private Object[] popArray() throws PDFParseException {
        Object obj = this.stack.pop();
        if (!(obj instanceof Object[])) {
            throw new PDFParseException("Expected an [array] here: " + obj.toString());
        }
        return (Object[]) obj;
    }

    /**
    * A class to store state needed whiel rendering. This includes the
    * stroke and fill color spaces, as well as the text formatting
    * parameters.
    */
    class ParserState implements Cloneable {
        /** the fill color space */
        PDFColorSpace fillCS;
        /** the stroke color space */
        PDFColorSpace strokeCS;
        /** the text paramters */
        PDFTextFormat textFormat;

        /**
        * Clone the render state.
        */
        @Override
        public Object clone() {
            ParserState newState = new ParserState();
            // no need to clone color spaces, since they are immutable
            newState.fillCS = this.fillCS;
            newState.strokeCS = this.strokeCS;
            // we do need to clone the textFormat
            newState.textFormat = (PDFTextFormat) this.textFormat.clone();
            return newState;
        }
    }
    
    @Override
    protected void setStatus(int status) {
    	if(status == BaseWatchable.COMPLETED) {
    		if(!addAnnotation){
    			// corresponding push in constructor PDFPage
    			this.cmds.addPop();
    			this.cmds.addAnnotations();
    			addAnnotation = true;
    		}
    	}
    	super.setStatus(status);
    }    
}
