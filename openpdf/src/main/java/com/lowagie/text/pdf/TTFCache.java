package com.lowagie.text.pdf;

import org.apache.fop.fonts.apps.TTFReader;
import org.apache.fop.fonts.truetype.FontFileReader;
import org.apache.fop.fonts.truetype.OFFontLoader;
import org.apache.fop.fonts.truetype.TTFFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.lowagie.text.ExceptionConverter;

/**
 *
 * 
 * @author  Gajendra kumar (raaz2.gajendra@gmail.com)
 */
public class TTFCache {

    private static Map<String,TTFFile> ttfFileMap =new ConcurrentHashMap<>();

    public static TTFFile getTTFFile(String fileName) {
        if (ttfFileMap.containsKey(fileName)){
            return ttfFileMap.get(fileName);
        }
        TTFReader app = new TTFReader();
        TTFFile ttf =null;
        try {
            ttf = loadTTF(app,fileName);
            ttfFileMap.put(fileName,ttf);
            return ttf;
        } catch (IOException e) {
            throw new ExceptionConverter(e);
        }
    }

    private static TTFFile loadTTF(TTFReader app, String fileName) throws IOException{
        try {
            return app.loadTTF(fileName, null, true, true);
        } catch (IOException e) {
            TTFFile ttfFile = new TTFFile(true, true);
            InputStream stream = BaseFont.getResourceStream(fileName, null);
            try {
                FontFileReader reader = new FontFileReader(stream);
                String header = OFFontLoader.readHeader(reader);
                String fontName = null;
                boolean supported = ttfFile.readFont(reader, header, fontName);
                if (!supported) {
                    return null;
                }
            } finally {
                stream.close();
            }
            if (ttfFile.isCFF()) {
                throw new UnsupportedOperationException(
                        "OpenType fonts with CFF data are not supported, yet");
            }
            return ttfFile;
        }
    }
}
