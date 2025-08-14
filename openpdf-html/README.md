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
- Uses neko-htmlunit as HTML parser, which will enable HTML5 parsing: https://github.com/HtmlUnit/htmlunit-neko
- also considering using Jsoup for HTML parsing: https://jsoup.org/
- Improved CSS3 compatibility
- Seamless integration with OpenPDF
- Modular architecture for easier maintenance and extension
- API compatible with Flying saucer, except package names are org.openpdf instead of org.xhtmlrenderer.


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

OpenPDF-html is a fork of [Flying Saucer](https://github.com/flyingsaucerproject/flyingsaucer)  and is used in accordance with the license https://github.com/flyingsaucerproject/flyingsaucer/blob/main/LICENSE-LGPL-2.1.txt
