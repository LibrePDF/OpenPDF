package com.github.librepdf.kotlin

import org.openpdf.text.Document
import org.openpdf.text.Element
import org.openpdf.text.Font
import org.openpdf.text.Image
import org.openpdf.text.Paragraph
import org.openpdf.text.pdf.PdfWriter
import java.io.OutputStream

/**
 * A simple Kotlin-style builder to create a PDF document.
 * This class wraps OpenPDF functionality in a more idiomatic and readable way for Kotlin users.
 */
class PdfBuilder(private val outputStream: OutputStream) {
  private val document = Document()
  private var writer: PdfWriter? = null

  init {
    writer = PdfWriter.getInstance(document, outputStream)
  }

  /**
   * Starts building the PDF document by applying the specified content block.
   *
   * @param content A lambda that defines the contents of the PDF using PdfBuilder methods.
   */
  fun build(content: PdfBuilder.() -> Unit) {
    document.open()
    this.content()
    document.close()
  }

  /**
   * Adds a paragraph of text to the document.
   *
   * @param text The text content of the paragraph.
   * @param alignment The alignment of the paragraph (default: ALIGN_LEFT).
   * @param font The font to use for the text.
   */
  fun paragraph(text: String, alignment: Int = Element.ALIGN_LEFT, font: Font = Font(Font.HELVETICA, 12f)) {
    val para = Paragraph(text, font)
    para.alignment = alignment
    document.add(para)
  }

  /**
   * Adds an image to the document.
   *
   * @param path The file path to the image.
   * @param scalePercent Optional scale percentage to resize the image.
   * @param alignment The alignment of the image (default: ALIGN_CENTER).
   */
  fun image(path: String, scalePercent: Float? = null, alignment: Int = Element.ALIGN_CENTER) {
    val img = Image.getInstance(path)
    scalePercent?.let { img.scalePercent(it) }
    img.alignment = alignment
    document.add(img)
  }

  /**
   * Adds one or more blank lines (line breaks) to the document.
   *
   * @param lines The number of blank lines to insert.
   */
  fun lineBreak(lines: Int = 1) {
    repeat(lines) {
      document.add(Paragraph(" "))
    }
  }

  /**
   * Adds a centered title using a bold font style.
   *
   * @param text The title text.
   */
  fun title(text: String) {
    paragraph(text, alignment = Element.ALIGN_CENTER, font = Font(Font.HELVETICA, 16f, Font.BOLD))
  }

  /**
   * Adds a centered subtitle using an italic font style.
   *
   * @param text The subtitle text.
   */
  fun subtitle(text: String) {
    paragraph(text, alignment = Element.ALIGN_CENTER, font = Font(Font.HELVETICA, 13f, Font.ITALIC))
  }

  /**
   * Returns the underlying PdfWriter instance, or null if not initialized.
   */
  fun writer(): PdfWriter? = writer
}
