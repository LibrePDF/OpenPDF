package com.lowagie.text.pdf;

import org.apache.fop.fonts.apps.TTFReader;
import org.apache.fop.fonts.truetype.TTFFile;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
            ttf = app.loadTTF(fileName, null, true, true);
            ttfFileMap.put(fileName,ttf);
            return ttf;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


}
