package com.sun.pdfview;

import java.awt.Rectangle;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import javax.imageio.ImageIO;

public class PDFDebugger {
    public final static String DEBUG_DCTDECODE_DATA = "debugdctdecode";
    public static final boolean DEBUG_TEXT = false;
    public static final boolean DEBUG_IMAGES = false;
    public static final boolean DEBUG_OPERATORS = false;
    public static final boolean DEBUG_PATH = false;
    public static final int DEBUG_STOP_AT_INDEX = 0;
    public static final boolean DISABLE_TEXT = false;
    public static final boolean DISABLE_IMAGES = false;
    public static final boolean DISABLE_PATH_STROKE = false;
    public static final boolean DISABLE_PATH_FILL = false;
    public static final boolean DISABLE_PATH_STROKE_FILL = false;
    public static final boolean DISABLE_CLIP = false;
    public static final boolean DISABLE_FORMS = false;
    public static final boolean DISABLE_SHADER = false;
    public static final boolean SHOW_TEXT_REGIONS = false;
    public static final boolean SHOW_TEXT_ANCHOR = false;
    public static final boolean DISABLE_THUMBNAILS = false;
    public static final long DRAW_DELAY = 0;

    public static int debuglevel = 4000;

    @SuppressWarnings("serial")
    public static class DebugStopException extends Exception {
        // nothing to do
    }

    public static void debugImage(BufferedImage image, String name) {
        if (PDFDebugger.DEBUG_IMAGES) {
            if(image == null) {
                return;
            }
            try {
                // retrieve image
                File outputfile = new File("D:/tmp/PDFimages/" + name + ".png");
                ImageIO.write(image, "png", outputfile);
            } catch (IOException e) {
                BaseWatchable.getErrorHandler().publishException(e);
            }
        }
    }

    // TODO: add debug level and print it? 
    public static void debug(String msg, int level) {
        if (level > debuglevel) {
            System.out.println(escape(msg));
        }
    }

    // TODO: add debug level and print it? 
    public static void debug(String msg) {
        debug(msg, debuglevel);
    }

    public static String escape(String msg) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < msg.length(); i++) {
            char c = msg.charAt(i);
            if (c != '\n' && (c < 32 || c >= 127)) {
                c = '?';
            }
            sb.append(c);
        }
        return sb.toString();
    }

    public static void setDebugLevel(int level) {
        debuglevel = level;
    }


    public static String dumpStream(byte[] stream) {
        return PDFDebugger.escape(new String(stream).replace('\r', '\n'));
    }

    public static void logPath(GeneralPath path, String operation) {
        if (PDFDebugger.DEBUG_PATH){
            if (operation != null) {
                System.out.println("Operation: " + operation + "; ");
            }
            System.out.println("Current path: ");
            Rectangle b = path.getBounds();
            if (b != null)
                System.out.println("        Bounds [x=" + b.x + ",y=" + b.y + ",width=" + b.width + ",height=" + b.height + "]");
            Point2D p = path.getCurrentPoint();
            if (p != null)
                System.out.println("        Point  [x=" + p.getX() + ",y=" + p.getY() + "]");
        }
    }

    /**
    * take a byte array and write a temporary file with it's data.
    * This is intended to capture data for analysis, like after decoders.
    *
    * @param ary
    * @param name
    */
    public static void emitDataFile(byte[] ary, String name) {
        FileOutputStream ostr;
        try {
            File file = File.createTempFile("DateFile", name);
            ostr = new FileOutputStream(file);
            PDFDebugger.debug("Write: " + file.getPath());
            ostr.write(ary);
            ostr.close();
        } catch (IOException ex) {
            // ignore
        }
    }

    public static void dump(PDFObject obj) throws IOException {
        PDFDebugger.debug("dumping PDF object: " + obj);
        if (obj == null) {
            return;
        }
        HashMap<String, PDFObject> dict = obj.getDictionary();
        PDFDebugger.debug("   dict = " + dict);
        for (Object key : dict.keySet()) {
            PDFDebugger.debug("key = " + key + " value = " + dict.get(key));
        }
    }

}
