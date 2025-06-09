package org.openpdf.swing;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NaiveUserAgentTest {
    private static String resolve(String baseUri, String uri) {
        NaiveUserAgent userAgent=new NaiveUserAgent();
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
