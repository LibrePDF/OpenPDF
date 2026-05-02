package org.openpdf.html;

import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for Html2PdfBatchUtils to ensure it runs batch jobs on virtual threads.
 */
class HtmlToPdfBatchUtilsTest {

    @Test
    void testBatchHtmlStringsRunsOnVirtualThreads() throws Exception {
        String html = "<html><body><h1>Hello PDF Batch test</h1></body></html>";

        Path out1 = Files.createTempFile("vt-batch-1-", ".pdf");
        Path out2 = Files.createTempFile("vt-batch-2-", ".pdf");
        Path out3 = Files.createTempFile("vt-batch-3-", ".pdf");

        // Assert virtual-thread execution from inside each batch task.
        var vtAssertCustomizer = HtmlToPdfBatchUtils.setDpi(96).andThen(renderer ->
                assertTrue(Thread.currentThread().isVirtual(), "Batch task should run on a virtual thread")
        );

        var jobs = List.of(
                new HtmlToPdfBatchUtils.HtmlStringJob(
                        html, null, out1,
                        Optional.of(HtmlToPdfBatchUtils.CSS_A4_20MM),
                        Optional.of(vtAssertCustomizer)
                ),
                new HtmlToPdfBatchUtils.HtmlStringJob(
                        html, null, out2,
                        Optional.of(HtmlToPdfBatchUtils.CSS_A4_20MM),
                        Optional.of(vtAssertCustomizer)
                ),
                new HtmlToPdfBatchUtils.HtmlStringJob(
                        html, null, out3,
                        Optional.of(HtmlToPdfBatchUtils.CSS_A4_20MM),
                        Optional.of(vtAssertCustomizer)
                )
        );

        var result = HtmlToPdfBatchUtils.batchHtmlStrings(jobs, p -> {}, t -> fail(t));

        assertTrue(result.isAllSuccessful(), "All batch jobs should succeed");

        for (Path p : List.of(out1, out2, out3)) {
            assertTrue(Files.exists(p), "PDF should exist: " + p);
            assertTrue(Files.size(p) > 0, "PDF should be non-empty: " + p);
            Files.deleteIfExists(p);
        }
    }

    @Test
    void testRenderHtmlStringToOutputStream() throws Exception {
        String html = "<html><body><h1>OutputStream test</h1></body></html>";

        try (var baos = new java.io.ByteArrayOutputStream()) {
            HtmlToPdfBatchUtils.renderHtmlString(html, null, baos,
                    HtmlToPdfBatchUtils.CSS_A4_20MM, null);

            assertTrue(baos.size() > 0, "PDF output should be non-empty");
            // PDF files always start with the magic bytes "%PDF"
            byte[] bytes = baos.toByteArray();
            assertEquals("%PDF", new String(bytes, 0, 4), "Output should be a valid PDF");
        }
    }

    @Test
    void testRenderHtmlFileToOutputStream() throws Exception {
        Path htmlFile = Files.createTempFile("test-", ".html");
        Files.writeString(htmlFile, "<html><body><h1>File OutputStream test</h1></body></html>");

        try (var baos = new java.io.ByteArrayOutputStream()) {
            HtmlToPdfBatchUtils.renderHtmlFile(htmlFile, null, baos, null, null);

            assertTrue(baos.size() > 0, "PDF output should be non-empty");
            byte[] bytes = baos.toByteArray();
            assertEquals("%PDF", new String(bytes, 0, 4), "Output should be a valid PDF");
        } finally {
            Files.deleteIfExists(htmlFile);
        }
    }

    @Test
    void testBatchHtmlStringsToStreams() throws Exception {
        String html = "<html><body><h1>Batch OutputStream test</h1></body></html>";

        var baos1 = new java.io.ByteArrayOutputStream();
        var baos2 = new java.io.ByteArrayOutputStream();
        var baos3 = new java.io.ByteArrayOutputStream();

        var jobs = List.of(
                new HtmlToPdfBatchUtils.HtmlStringStreamJob(
                        html, null, baos1,
                        Optional.of(HtmlToPdfBatchUtils.CSS_A4_20MM),
                        Optional.empty()
                ),
                new HtmlToPdfBatchUtils.HtmlStringStreamJob(
                        html, null, baos2,
                        Optional.of(HtmlToPdfBatchUtils.CSS_A4_20MM),
                        Optional.empty()
                ),
                new HtmlToPdfBatchUtils.HtmlStringStreamJob(
                        html, null, baos3,
                        Optional.of(HtmlToPdfBatchUtils.CSS_A4_20MM),
                        Optional.empty()
                )
        );

        var result = HtmlToPdfBatchUtils.batchHtmlStringsToStreams(jobs, v -> {}, t -> fail(t));

        assertTrue(result.isAllSuccessful(), "All batch stream jobs should succeed");

        for (var baos : List.of(baos1, baos2, baos3)) {
            assertTrue(baos.size() > 0, "PDF output should be non-empty");
            assertEquals("%PDF", new String(baos.toByteArray(), 0, 4), "Output should be a valid PDF");
        }
    }

    @Test
    void testBatchHtmlFilesToStreams() throws Exception {
        Path htmlFile1 = Files.createTempFile("batch-file-1-", ".html");
        Path htmlFile2 = Files.createTempFile("batch-file-2-", ".html");
        Files.writeString(htmlFile1, "<html><body><h1>Batch file 1</h1></body></html>");
        Files.writeString(htmlFile2, "<html><body><h1>Batch file 2</h1></body></html>");

        var baos1 = new java.io.ByteArrayOutputStream();
        var baos2 = new java.io.ByteArrayOutputStream();

        var jobs = List.of(
                new HtmlToPdfBatchUtils.HtmlFileStreamJob(
                        htmlFile1, null, baos1,
                        Optional.empty(), Optional.empty()
                ),
                new HtmlToPdfBatchUtils.HtmlFileStreamJob(
                        htmlFile2, null, baos2,
                        Optional.empty(), Optional.empty()
                )
        );

        var result = HtmlToPdfBatchUtils.batchHtmlFilesToStreams(jobs, v -> {}, t -> fail(t));

        assertTrue(result.isAllSuccessful(), "All batch file stream jobs should succeed");

        for (var baos : List.of(baos1, baos2)) {
            assertTrue(baos.size() > 0, "PDF output should be non-empty");
            assertEquals("%PDF", new String(baos.toByteArray(), 0, 4), "Output should be a valid PDF");
        }

        Files.deleteIfExists(htmlFile1);
        Files.deleteIfExists(htmlFile2);
    }

    @Test
    void testBatchHtmlStringsToStreamsRunsOnVirtualThreads() throws Exception {
        String html = "<html><body><h1>Virtual thread stream test</h1></body></html>";

        var vtAssertCustomizer = HtmlToPdfBatchUtils.setDpi(96).andThen(renderer ->
                assertTrue(Thread.currentThread().isVirtual(), "Batch stream task should run on a virtual thread")
        );

        var baos1 = new java.io.ByteArrayOutputStream();
        var baos2 = new java.io.ByteArrayOutputStream();

        var jobs = List.of(
                new HtmlToPdfBatchUtils.HtmlStringStreamJob(
                        html, null, baos1,
                        Optional.of(HtmlToPdfBatchUtils.CSS_A4_20MM),
                        Optional.of(vtAssertCustomizer)
                ),
                new HtmlToPdfBatchUtils.HtmlStringStreamJob(
                        html, null, baos2,
                        Optional.of(HtmlToPdfBatchUtils.CSS_A4_20MM),
                        Optional.of(vtAssertCustomizer)
                )
        );

        var result = HtmlToPdfBatchUtils.batchHtmlStringsToStreams(jobs, v -> {}, t -> fail(t));

        assertTrue(result.isAllSuccessful(), "All batch stream jobs should succeed");
    }

    @Test
    void testRenderHtmlStringToOutputStreamWithCssInjection() throws Exception {
        // The CSS injection path is exercised separately to ensure it doesn't break OutputStream output
        String html = "<html><head></head><body><h1>CSS injection test</h1></body></html>";

        try (var baos = new java.io.ByteArrayOutputStream()) {
            HtmlToPdfBatchUtils.renderHtmlString(html, null, baos,
                    HtmlToPdfBatchUtils.CSS_LETTER_HALF_IN, null);

            assertTrue(baos.size() > 0, "PDF output should be non-empty");
            assertEquals("%PDF", new String(baos.toByteArray(), 0, 4), "Output should be a valid PDF");
        }
    }

    @Test
    void testRenderHtmlStringToOutputStreamRequiresNonNullHtml() {
        var baos = new java.io.ByteArrayOutputStream();
        assertThrows(NullPointerException.class, () ->
                HtmlToPdfBatchUtils.renderHtmlString(null, null, baos, null, null)
        );
    }

    @Test
    void testRenderHtmlStringToOutputStreamRequiresNonNullOutputStream() {
        assertThrows(NullPointerException.class, () ->
                HtmlToPdfBatchUtils.renderHtmlString("<html/>", null,
                        (java.io.OutputStream) null, null, null)
        );
    }
}
