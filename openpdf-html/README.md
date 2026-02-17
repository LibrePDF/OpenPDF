# OpenPDF-html

OpenPDF-html is an HTML-to-PDF rendering engine built on a modernized fork of Flying Saucer, designed to integrate seamlessly with the OpenPDF library and support HTML5 and CSS3 features.

OpenPDF-html is a fork of [Flying Saucer](https://github.com/flyingsaucerproject/flyingsaucer) circa v9.12.0, focused on bringing modern HTML and CSS rendering capabilities to the [OpenPDF](https://github.com/LibrePDF/OpenPDF) library.

## Purpose

The goal of this project is to upgrade and maintain the HTML-to-PDF rendering engine, ensuring support for modern web standards. This enables developers to generate high-quality PDF documents from HTML5 and CSS3 content using OpenPDF.


## License

This project is licensed under the **GNU Lesser General Public License (LGPL)**.  

[![License (LGPL version 2.1)](https://img.shields.io/badge/license-GNU%20LGPL%20version%202.1-blue.svg?style=flat-square)](http://opensource.org/licenses/LGPL-2.1)



## Features

- Modern HTML5 support (in progress)
- Uses [htmlunit-neko](https://github.com/HtmlUnit/htmlunit-neko) as HTML parser, enabling HTML5-compliant parsing with error tolerance
- Improved CSS3 compatibility
- Seamless integration with OpenPDF
- Modular architecture for easier maintenance and extension
- API compatible with Flying saucer, except package names are org.openpdf instead of org.xhtmlrenderer.

### htmlunit-neko Parser Integration

OpenPDF-html leverages the htmlunit-neko parser for HTML5-compliant parsing with the following features:

- **Error Tolerant**: Handles malformed HTML gracefully by automatically fixing common mistakes
- **HTML5 Compliant**: Supports modern HTML5 semantic elements (`header`, `footer`, `nav`, `article`, `section`, `aside`, `main`, `figure`, etc.)
- **Data Attributes**: Full support for HTML5 `data-*` attributes
- **Void Elements**: Proper handling of HTML5 void elements (`br`, `hr`, `img`, `input`, etc.)
- **Configurable**: Extensive configuration options for parsing behavior

#### Using HtmlResource for Direct DOM Parsing

For direct HTML parsing using htmlunit-neko's DOMParser:

```java
import org.openpdf.resource.HtmlResource;
import org.openpdf.resource.HtmlParserConfig;
import org.w3c.dom.Document;

// Parse HTML string with default settings
HtmlResource resource = HtmlResource.load("<html><body><h1>Hello</h1></body></html>");
Document doc = resource.getDocument();

// Parse with custom configuration
HtmlParserConfig config = HtmlParserConfig.builder()
    .reportErrors(true)
    .allowSelfClosingTags(true)
    .encoding("UTF-8")
    .build();
HtmlResource resource = HtmlResource.load(html, config);
```

#### Available Configuration Options

| Option | Default | Description |
|--------|---------|-------------|
| `reportErrors` | false | Enable detailed error reporting during parsing |
| `allowSelfClosingTags` | false | Allow XHTML-style self-closing tags (`<div/>`) |
| `allowSelfClosingIframe` | false | Allow self-closing iframe tags |
| `parseNoScriptContent` | true | Parse content within `<noscript>` as HTML |
| `scriptStripCommentDelims` | false | Strip HTML comment delimiters from scripts |
| `styleStripCommentDelims` | false | Strip HTML comment delimiters from styles |
| `elementNameCase` | default | Element name handling: "upper", "lower", "default" |
| `attributeNameCase` | default | Attribute name handling: "upper", "lower", "default" |
| `encoding` | auto | Character encoding (e.g., "UTF-8") |


## Example

You can find a simple working example here:  [HelloWorldPdf.java](https://github.com/LibrePDF/OpenPDF/blob/master/openpdf-html/src/test/java/org/openpdf/pdf/HelloWorldPdf.java)

```java
import org.openpdf.pdf.ITextRenderer;
import java.io.FileOutputStream;

public class HelloWorldPdf {
    public static void main(String[] args) throws Exception {
        String html = """
            <html>
              <head>
                <style>
                  body { font-family: sans-serif; }
                  h1 { color: navy; }
                </style>
              </head>
              <body>
                <h1>Hello, World!</h1>
                <p>This PDF was generated using openpdf-html.</p>
              </body>
            </html>
            """;

        try (FileOutputStream outputStream = new FileOutputStream("openpdf-html-hello.pdf")) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(outputStream);
        }

        System.out.println("PDF created: flying-saucer-hello.pdf");
    }
}
```



## Related Projects

- [Flying Saucer (original project)](https://github.com/flyingsaucerproject/flyingsaucer)
- [OpenPDF](https://github.com/LibrePDF/OpenPDF)
- [htmlunit-neko](https://github.com/HtmlUnit/htmlunit-neko)

## Roadmap

- Refactor architecture for better modularity
- Add support for HTML5 tags and CSS3 properties
- Add comprehensive tests and sample HTML templates
- JavaScript

## Contributions

Contributions are welcome! Feel free to submit issues, pull requests, or ideas to help improve HTML and CSS support in OpenPDF.

## History

OpenPDF-html is a fork of [Flying Saucer](https://github.com/flyingsaucerproject/flyingsaucer) forked in june 2025 and is used in accordance with the license https://github.com/flyingsaucerproject/flyingsaucer/blob/main/LICENSE-LGPL-2.1.txt

## Flying Saucer – Origins

- **Founded**: 2004  
- **Founder**: Josh Marinacci  
- **Where**: Launched on Java.net as an open-source project  
- **Why**: To build a **pure-Java XHTML + CSS 2.1 renderer**—simpler than full browser engines like Gecko/WebKit.  
- **Goal**: Standards-compliant rendering for embedding in Java apps or generating PDFs (with iText), without scripting or heavy browser features.  
- **Notable**: Sun Microsystems once considered bundling it with F3 (early JavaFX Script), but it stayed independent.  
- **License**: LGPL



