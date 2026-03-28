/*
 * This code is part of the 'OpenPDF Tutorial'.
 * You can find the complete tutorial at the following address:
 * https://github.com/LibrePDF/OpenPDF/wiki/Tutorial
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * Volker Kunert 2026
 */
package org.openpdf.examples.glyphlayout;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Calls all glyph layout examples using GlyphLayoutManager in multiple threads
 *
 */
public class RunGlyphLayoutExamplesMultithreading {

    /**
     * Main method
     *
     * @param args -- not used
     */
    public static void main(String[] args) throws Exception {
        final int MAX = 42;

        long startTime = System.nanoTime();
        System.out.println("RunGlyphLayoutExamplesMultithreading started");
        java.util.concurrent.ExecutorService executorService = Executors.newCachedThreadPool();

        for (int i = 0; i < MAX; i++) {
            executorService.submit(new MyThread(String.format("GlyphLayoutBidi-%d.pdf", i), GlyphLayoutBidi::test));
            executorService.submit(
                    new MyThread(String.format("GlyphLayoutBidiPerFont-%d.pdf", i), GlyphLayoutBidiPerFont::test));
            executorService.submit(new MyThread(String.format("GlyphLayoutBidiRotated-%d.pdf", i),
                    GlyphLayoutBidiRotated::test));
            executorService.submit(
                    new MyThread(String.format("GlyphLayoutDin91379-%d.pdf", i), GlyphLayoutDin91379::test));
            executorService.submit(
                    new MyThread(String.format("GlyphLayoutFormDin91379-%d.pdf", i), GlyphLayoutFormDin91379::test));
            executorService.submit(
                    new MyThread(String.format("GlyphLayoutInputStream-%d.pdf", i), GlyphLayoutInputStream::test));
            executorService.submit(
                    new MyThread(String.format("GlyphLayoutKernLiga-%d.pdf", i), GlyphLayoutKernLiga::test));
            executorService.submit(new MyThread(String.format("GlyphLayoutKernLigaPerFont-%d.pdf", i),
                    GlyphLayoutKernLigaPerFont::test));
            executorService.submit(new MyThread(String.format("GlyphLayoutSMP-%d.pdf", i),
                    GlyphLayoutSMP::test));
            executorService.submit(new MyThread(String.format("GlyphLayoutWithImage-%d.pdf", i),
                    GlyphLayoutWithImage::test));
        }
        executorService.shutdown();
        executorService.awaitTermination(MAX, TimeUnit.SECONDS);
        long endTime = System.nanoTime();
        System.out.printf("RunGlyphLayoutExamplesMultithreading ended %g seconds",
                (endTime - startTime) * 1e-9);
    }

    public interface Example {

        void test(String name) throws Exception;
    }

    private static class MyThread extends Thread {

        private final Example example;

        MyThread(final String name, Example example) {
            super(name);
            this.example = example;
        }

        @Override
        public void run() {
            try {
                example.test(getName());
            } catch (Exception e) {
                System.err.println("---" + getName() + " " + e);
            }
        }
    }
}
