package org.openpdf.swing;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

public class NaiveUserAgentTest {
    private static final String CONTENT = "body { color: red; }";

    private final List<String> requestedPaths = new CopyOnWriteArrayList<>();
    private HttpServer server;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.createContext("/content", exchange -> respond(exchange, 200, null));
        server.start();
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    @Test
    void redirectToFileUrlIsNotFollowed(@TempDir Path tempDir) throws IOException {
        Path file = tempDir.resolve("local.css");
        Files.writeString(file, "content-of-a-local-file");
        redirect("/to-file", 302, file.toUri().toString());

        assertThat(read(new NaiveUserAgent(), "/to-file")).isNull();
    }

    @Test
    void redirectToAnotherHttpUrlIsFollowed() throws IOException {
        redirect("/absolute", 302, url("/content"));
        redirect("/relative", 301, "content");
        redirect("/temporary", 307, "/content");

        assertThat(read(new NaiveUserAgent(), "/absolute")).isEqualTo(CONTENT);
        assertThat(read(new NaiveUserAgent(), "/relative")).isEqualTo(CONTENT);
        assertThat(read(new NaiveUserAgent(), "/temporary")).isEqualTo(CONTENT);
    }

    @Test
    void numberOfRedirectsIsLimited() throws IOException {
        redirect("/hop1", 302, "/content");
        for (int i = 2; i <= 6; i++) {
            redirect("/hop" + i, 302, "/hop" + (i - 1));
        }
        redirect("/loop", 302, "/loop");

        assertThat(read(new NaiveUserAgent(), "/hop5")).isEqualTo(CONTENT);
        assertThat(read(new NaiveUserAgent(), "/hop6")).isNull();
        assertThat(read(new NaiveUserAgent(), "/loop")).isNull();
    }

    @Test
    void redirectCanBeRefusedByTheApplication() throws IOException {
        redirect("/refused", 302, "/content");
        NaiveUserAgent userAgent = new NaiveUserAgent() {
            @Override
            protected boolean isRedirectAllowed(URL from, URL to) {
                return !to.getPath().equals("/content");
            }
        };

        assertThat(read(userAgent, "/refused")).isNull();
        assertThat(requestedPaths).containsExactly("/refused");
    }

    private void redirect(String path, int status, String location) {
        server.createContext(path, exchange -> respond(exchange, status, location));
    }

    private void respond(HttpExchange exchange, int status, String location) throws IOException {
        requestedPaths.add(exchange.getRequestURI().getPath());
        byte[] body = location == null ? CONTENT.getBytes(UTF_8) : new byte[0];
        if (location != null) {
            exchange.getResponseHeaders().add("Location", location);
        }
        exchange.sendResponseHeaders(status, body.length == 0 ? -1 : body.length);
        exchange.getResponseBody().write(body);
        exchange.close();
    }

    private String url(String path) {
        return "http://" + server.getAddress().getHostString() + ":" + server.getAddress().getPort() + path;
    }

    private String read(NaiveUserAgent userAgent, String path) throws IOException {
        try (InputStream in = userAgent.resolveAndOpenStream(url(path))) {
            return in == null ? null : new String(in.readAllBytes(), UTF_8);
        }
    }

    private static String resolve(String baseUri, String uri) {
        NaiveUserAgent userAgent = new NaiveUserAgent();
        userAgent.setBaseURL(baseUri);
        return userAgent.resolveURI(uri);
    }

    @Test
    public void basicResolve() {
        // absolute uris should be unchanged
        assertThat(resolve(null, "http://www.example.com")).isEqualTo("http://www.example.com");
        assertThat(resolve("ftp://www.example.com/other", "http://www.example.com")).isEqualTo("http://www.example.com");

        // by default relative uris resolves as file
        assertThat(resolve(null, "www.example.com"))
                .isNotNull()
                .startsWith("file:");

        // relative uris without slash
        assertThat(resolve("ftp://www.example.com/other", "test")).isEqualTo("ftp://www.example.com/test");

        // relative uris with slash
        assertThat(resolve("ftp://www.example.com/other/", "test")).isEqualTo("ftp://www.example.com/other/test");
        assertThat(resolve("ftp://www.example.com/other/", "/test")).isEqualTo("ftp://www.example.com/test");
    }

    @Test
    public void customProtocolResolve() {
        // absolute uris should be unchanged
        assertThat(resolve(null, "custom://www.example.com")).isEqualTo("custom://www.example.com");
        assertThat(resolve("ftp://www.example.com/other", "custom://www.example.com")).isEqualTo("custom://www.example.com");

        // relative uris without slash
        assertThat(resolve("custom://www.example.com/other", "test")).isEqualTo("custom://www.example.com/test");

        // relative uris with slash
        assertThat(resolve("custom://www.example.com/other/", "test")).isEqualTo("custom://www.example.com/other/test");
        assertThat(resolve("custom://www.example.com/other/", "/test")).isEqualTo("custom://www.example.com/test");
    }

    /**
     * This reproduces <a href="https://code.google.com/archive/p/flying-saucer/issues/262">...</a>
     * <p>
     * Below test was green with 9.0.6 and turned red in 9.0.7
     */
    @Test
    public void jarFileUriResolve() {
        // absolute uris should be unchanged
        assertThat(resolve(null, "jar:file:/path/jarfile.jar!/foo/index.xhtml")).isEqualTo("jar:file:/path/jarfile.jar!/foo/index.xhtml");
        assertThat(resolve("ftp://www.example.com/other", "jar:file:/path/jarfile.jar!/foo/index.xhtml")).isEqualTo("jar:file:/path/jarfile.jar!/foo/index.xhtml");

        // relative uris without slash
        assertThat(resolve("jar:file:/path/jarfile.jar!/foo/index.xhtml", "other.xhtml")).isEqualTo("jar:file:/path/jarfile.jar!/foo/other.xhtml");

        // relative uris with slash
        assertThat(resolve("jar:file:/path/jarfile.jar!/foo/", "other.xhtml")).isEqualTo("jar:file:/path/jarfile.jar!/foo/other.xhtml");
        assertThat(resolve("jar:file:/path/jarfile.jar!/foo/", "/other.xhtml")).isEqualTo("jar:file:/path/jarfile.jar!/other.xhtml");
    }

}
