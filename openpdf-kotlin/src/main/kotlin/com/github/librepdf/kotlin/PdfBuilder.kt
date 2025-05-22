package com.github.librepdf.kotlin.com.github.librepdf.kotlin

import com.lowagie.text.Document
import com.lowagie.text.Element
import com.lowagie.text.Font
import com.lowagie.text.Image
import com.lowagie.text.Paragraph
import com.lowagie.text.pdf.PdfWriter
import java.io.OutputStream

/**
 * A simple Kotlin-style builder to create a PDF document.
 */
class PdfBuilder(private val outputStream: OutputStream) {
  private val document = Document()

  private var writer: PdfWriter? = null

  init {
    writer = PdfWriter.getInstance(document, outputStream)
  }

  fun build(content: PdfBuilder.() -> Unit) {
    document.open()
    this.content()
    document.close()
  }

  fun paragraph(text: String, alignment: Int = Element.ALIGN_LEFT, font: Font = Font(Font.HELVETICA, 12f)) {
    val para = Paragraph(text, font)
    para.alignment = alignment
    document.add(para)
  }

  fun image(path: String, scalePercent: Float? = null, alignment: Int = Element.ALIGN_CENTER) {
    val img = Image.getInstance(path)
    scalePercent?.let { img.scalePercent(it) }
    img.alignment = alignment
    document.add(img)
  }

  fun lineBreak(lines: Int = 1) {
    repeat(lines) {
      document.add(Paragraph(" "))
    }
  }

  fun title(text: String) {
    paragraph(text, alignment = Element.ALIGN_CENTER, font = Font(Font.HELVETICA, 16f, Font.BOLD))
  }

  fun subtitle(text: String) {
    paragraph(text, alignment = Element.ALIGN_CENTER, font = Font(Font.HELVETICA, 13f, Font.ITALIC))
  }

  fun writer(): PdfWriter? = writer
}