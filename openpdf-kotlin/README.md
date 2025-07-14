**OpenPDF Kotlin** is an *experimental* Kotlin extension module for [OpenPDF](https://github.com/LibrePDF/OpenPDF). 
It provides idiomatic Kotlin APIs, utilities, and builder-style functionality for working with PDF documents.

This submodule makes OpenPDF more convenient and expressive for Kotlin developers by offering:

- Extension functions for common PDF tasks
- A lightweight Kotlin-style `PdfBuilder` for creating PDF files in Kotlin.
- `HtmlPdfBuilder` for creating PDF files from HTML.
- DSL-inspired document generation syntax

Kotlin pull requests are welcome!

---

## Example Usage

```kotlin
import java.io.FileOutputStream
import com.github.librepdf.html.HtmlPdfBuilder
import com.lowagie.text.pdf.PdfWriter

val outputStream = FileOutputStream("output.pdf")

HtmlPdfBuilder(outputStream).apply {
    html(
        """
        <html>
          <head><title>Example</title></head>
          <body>
            <h1>Hello from HTML</h1>
            <p>This PDF was generated using openpdf-html and Kotlin.</p>
          </body>
        </html>
        """.trimIndent()
    )
    scaleToFit(true)
    pdfVersion(com.lowagie.text.pdf.PdfWriter.VERSION_1_7)
    build()
}

```

```kotlin
import com.github.librepdf.kotlin.PdfBuilder
import java.io.FileOutputStream

fun main() {
    FileOutputStream("example.pdf").use { output ->
        PdfBuilder(output).build {
            title("Hello from Kotlin")
            subtitle("Using OpenPDF Kotlin")
            paragraph("This is a paragraph in a PDF file.")
            image("logo.png", scalePercent = 50f)
        }
    }
}
```


## TODO

- [ ] Improve `PdfBuilder` DSL syntax and flexibility (e.g. nested tables, headers, footers)
- [ ] Add unit tests for all Kotlin utilities
- [ ] Add `PdfFormUtil` for working with AcroForms (fill/read fields)
- [ ] Create `PdfMetadataUtil` for reading document properties
- [ ] Write more Kotlin usage examples and tutorials
- [ ] Explore multiplatform or Android compatibility
- [ ] Set up `markdownlint` and Kotlin style linter
