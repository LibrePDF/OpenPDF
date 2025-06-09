package org.openpdf.util;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Level;

public class FontUtil {

    @CheckReturnValue
    public static boolean isEmbeddedBase64Font(@Nullable String uri) {
        return uri != null && uri.startsWith("data:font/");
    }

    @Nullable
    @CheckReturnValue
    public static InputStream getEmbeddedBase64Data(@Nullable String uri) {
        int b64Index = (uri!= null)? uri.indexOf("base64,") : -1;
        if (b64Index != -1) {
            String b64encoded = uri.substring(b64Index + "base64,".length());
            if (b64encoded.contains("%")) {
                b64encoded = URLDecoder.decode(b64encoded, StandardCharsets.US_ASCII);
            }
            return new ByteArrayInputStream( Base64.getDecoder().decode( b64encoded));
        } else {
            XRLog.load(Level.SEVERE, "Embedded css fonts must be encoded in base 64.");
            return null;
        }
    }
}
