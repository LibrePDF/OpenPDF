package org.openpdf.util;

import com.google.errorprone.annotations.CheckReturnValue;
import org.jspecify.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.logging.Level;

import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.newOutputStream;

/**
 * @author patrick
 */
public class IOUtil {
    public static void copyFile(File page, File outputDir) throws IOException {
        File outputFile = new File(outputDir, page.getName());

        try (OutputStream out = new BufferedOutputStream(newOutputStream(outputFile.toPath()))) {
            try (InputStream in = new BufferedInputStream(newInputStream(page.toPath()))) {
                copyBytes(in, out);
            }
            out.flush();
        }
    }

    private static void copyBytes(InputStream in, OutputStream out) throws IOException {
        byte[] buf = new byte[1024];
        int len;
        while ((len = in.read(buf)) >= 0) {
            out.write(buf, 0, len);
        }
    }

    public static void deleteAllFiles(final File dir) throws IOException {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (!file.delete()) {
                    throw new IOException("Cleanup directory " + dir + ", can't delete file " + file);
                }
            }
        }
    }

    /**
     * Attempts to open a connection, and a stream, to the URI provided. timeouts will be set for opening the connection
     * and reading from it. will return the stream, or null if unable to open or read or a timeout occurred. Does not
     * buffer the stream.
     */
    @Nullable
    public static InputStream openStreamAtUrl(String uri) {
        try {
            URLConnection uc = new URL(uri).openConnection();
            uc.setConnectTimeout(10 * 1000);
            uc.setReadTimeout(30 * 1000);
            uc.setRequestProperty("Accept", "*/*");
            uc.connect();

            return uc.getInputStream();
        } catch (MalformedURLException e) {
            XRLog.exception("bad URL given: " + uri, e);
        } catch (FileNotFoundException e) {
            XRLog.exception("item at URI " + uri + " not found (caused by: " + e + ")");
        } catch (IOException e) {
            XRLog.exception("IO problem for " + uri, e);
        }

        return null;
    }

    /**
     * Gets a Reader for the resource identified
     *
     * @return The stylesheet value
     */
    @Nullable
    @CheckReturnValue
    public static InputStream getInputStream(@Nullable String uri) {
        if (uri == null) return null;
        try {
            return new BufferedInputStream(new URL(uri).openStream());
        } catch (MalformedURLException e) {
            XRLog.exception("bad URL given: " + uri, e);
        } catch (FileNotFoundException e) {
            XRLog.exception("item at URI " + uri + " not found (caused by: " + e + ")");
        } catch (IOException e) {
            XRLog.exception("IO problem for " + uri, e);
        }
        return null;
    }

    @CheckReturnValue
    public static byte @Nullable [] readBytes(String uri) {
        try (InputStream is = getInputStream(uri)) {
            if (is == null) return null;
            return readBytes(is);
        } catch (IOException e) {
            XRLog.load(Level.WARNING, "Unable to read " + uri, e);
            return null;
        }
    }

    @CheckReturnValue
    public static byte[] readBytes(Path file) throws IOException {
        try (InputStream is = newInputStream(file)) {
            return readBytes(is);
        }
    }

    @CheckReturnValue
    public static byte[] readBytes(InputStream is) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream(is.available());
        copyBytes(is, result);
        return result.toByteArray();
    }

    /**
     * @deprecated Use try-with-resources idiom instead.
     */
    @Deprecated
    @SuppressWarnings("EmptyCatch")
    public static void close(@Nullable Closeable in) {
        if (in != null) {
            try {
                in.close();
            } catch (IOException ignore) {
            }
        }
    }
}
