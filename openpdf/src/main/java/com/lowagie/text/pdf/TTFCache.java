package com.lowagie.text.pdf;

import com.lowagie.text.ExceptionConverter;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.fop.fonts.apps.TTFReader;
import org.apache.fop.fonts.truetype.FontFileReader;
import org.apache.fop.fonts.truetype.TTFFile;

/**
 * @author Gajendra kumar (raaz2.gajendra@gmail.com)
 */
public class TTFCache {

    private static Map<String, TTFFile> ttfFileMap = new ConcurrentHashMap<>();

    public static TTFFile getTTFFile(String fileName, TrueTypeFontUnicode ttu) {

        if (ttfFileMap.containsKey(fileName)) {
            return ttfFileMap.get(fileName);
        }
        TTFReader app = new TTFReader();
        TTFFile ttf = null;
        try {
            ttf = loadTTF(app, fileName, ttu);
            ttfFileMap.put(fileName, ttf);
            return ttf;
        } catch (IOException e) {
            throw new ExceptionConverter(e);
        }
    }

    private static TTFFile loadTTF(TTFReader app, String fileName, TrueTypeFontUnicode ttu) throws IOException {

        try {
            return app.loadTTF(fileName, null, true, true);
        } catch (IOException e) {
            TTFFile ttfFile = new TTFFile(true, true);
            InputStream stream = BaseFont.getResourceStream(fileName, null);
            try {
                if (stream == null) {
                    stream = getStreamFromFont(ttu);
                }
                FontFileReader reader = new FontFileReader(stream);
                String fontName = null;
                ttfFile.readFont(reader, fontName);
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
            if (ttfFile.isCFF()) {
                throw new UnsupportedOperationException(
                        "OpenType fonts with CFF data are not supported, yet");
            }
            return ttfFile;
        }
    }

    private static InputStream getStreamFromFont(TrueTypeFontUnicode ttu) throws IOException {
        return new ByteArrayInputStream(ttu.getFullFont());
    }

}
