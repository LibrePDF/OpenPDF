package com.lowagie.text;

import com.lowagie.text.error_messages.MessageLocalization;
import com.lowagie.text.pdf.codec.wmf.InputMeta;
import java.io.IOException;
import java.io.InputStream;

public class WMFProcessor {
    public static WMFData processWMF(InputStream is, String errorID) throws IOException, BadElementException {
        InputMeta in = new InputMeta(is);
        if (in.readInt() != 0x9AC6CDD7) {
            throw new BadElementException(
                    MessageLocalization.getComposedMessage("1.is.not.a.valid.placeable.windows.metafile", errorID));
        }
        in.readWord();
        int left = in.readShort();
        int top = in.readShort();
        int right = in.readShort();
        int bottom = in.readShort();
        int inch = in.readWord();

        float dpiX = 72;
        float dpiY = 72;
        float scaledHeight = (float) (bottom - top) / inch * 72f;
        float scaledWidth = (float) (right - left) / inch * 72f;

        return new WMFData(scaledWidth, scaledHeight, dpiX, dpiY);
    }
}
