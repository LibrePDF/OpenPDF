package com.github.librepdf.kotlin

import org.openpdf.pdf.ITextRenderer
import java.io.OutputStream

/**
 * A Kotlin DSL-style builder for creating PDFs from HTML using OpenPDF + Flying Saucer (openpdf-html).
 */
class HtmlPdfBuilder(private val outputStream: OutputStream) {
  private var htmlContent: String? = null
  private var baseUrl: String? = null
  private var scaleToFit: Boolean = false
  private var pdfVersion: String? = null

  /**
   * Set the HTML content to render.
   *
   * @param html HTML string
   * @param baseUrl Optional base URL for resolving relative paths (e.g., for images or CSS)
   */
  fun html(html: String, baseUrl: String? = null) {
    this.htmlContent = html
    this.baseUrl = baseUrl
  }

  /**
   * Set the PDF version to use (optional).
   *
   * @param version One of PdfWriter.VERSION_1_2 through VERSION_1_7
   */
  fun pdfVersion(version: String) {
    this.pdfVersion = version
  }

  /**
   * Enable or disable scale-to-fit behavior.
   */
  fun scaleToFit(enabled: Boolean = true) {
    this.scaleToFit = enabled
  }

  /**
   * Builds and writes the PDF to the output stream.
   */
  fun build() {
    val content = htmlContent
      ?: throw IllegalStateException("HTML content must be set before calling build()")

    val renderer = ITextRenderer()
    pdfVersion?.let { renderer.setPDFVersion(it) }
    renderer.setScaleToFit(scaleToFit)
    renderer.setDocumentFromString(content, baseUrl)
    renderer.layout()
    renderer.createPDF(outputStream)
  }
}
