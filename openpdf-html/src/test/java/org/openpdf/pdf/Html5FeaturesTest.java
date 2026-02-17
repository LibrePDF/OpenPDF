/*
 * This file is part of the OpenPDF HTML module.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package org.openpdf.pdf;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test cases demonstrating modern HTML5 features support in openpdf-html.
 * <p>
 * This test validates the integration with htmlunit-neko HTML parser,
 * which provides HTML5-compliant parsing with error tolerance.
 * <p>
 * The htmlunit-neko parser (https://github.com/HtmlUnit/htmlunit-neko) provides:
 * <ul>
 *   <li>HTML5 compliant parsing</li>
 *   <li>Error tolerant parsing for malformed HTML</li>
 *   <li>Automatic tag balancing and fixing</li>
 *   <li>Support for modern HTML5 semantic elements</li>
 * </ul>
 */
public class Html5FeaturesTest {

    private static final Logger log = LoggerFactory.getLogger(Html5FeaturesTest.class);

    /**
     * Tests support for HTML5 semantic elements like header, footer, nav, article,
     * section, aside, main, figure, and figcaption.
     */
    @Test
    void html5SemanticElements(@TempDir Path tempDir) throws Exception {
        String html = """
            <!DOCTYPE html>
            <html lang="en">
              <head>
                <meta charset="UTF-8">
                <title>HTML5 Semantic Elements Test</title>
                <style>
                  @page { size: A4; margin: 2cm; }
                  body { font-family: Arial, sans-serif; font-size: 12pt; }
                  header { background: #f0f0f0; padding: 10px; margin-bottom: 10px; }
                  nav { background: #e0e0e0; padding: 10px; margin-bottom: 10px; }
                  nav ul { list-style: none; padding: 0; margin: 0; }
                  nav li { display: inline; margin-right: 10px; }
                  main { padding: 10px; }
                  article { border: 1px solid #ccc; padding: 10px; margin-bottom: 10px; }
                  section { margin-bottom: 10px; }
                  aside { background: #f8f8f8; padding: 10px; margin: 10px 0; }
                  footer { background: #333; color: white; padding: 10px; text-align: center; }
                  figure { margin: 10px; padding: 10px; border: 1px solid #ddd; }
                  figcaption { font-style: italic; text-align: center; }
                </style>
              </head>
              <body>
                <header>
                  <h1>HTML5 Semantic Elements</h1>
                  <p>Testing modern HTML5 structural elements</p>
                </header>
                
                <nav>
                  <ul>
                    <li><a href="#section1">Section 1</a></li>
                    <li><a href="#section2">Section 2</a></li>
                    <li><a href="#section3">Section 3</a></li>
                  </ul>
                </nav>
                
                <main>
                  <article>
                    <h2>Article Title</h2>
                    <section id="section1">
                      <h3>Section 1</h3>
                      <p>This is the first section of the article.</p>
                    </section>
                    <section id="section2">
                      <h3>Section 2</h3>
                      <p>This is the second section with some content.</p>
                    </section>
                  </article>
                  
                  <aside>
                    <h4>Related Information</h4>
                    <p>This is an aside element with supplementary content.</p>
                  </aside>
                  
                  <figure>
                    <div style="width: 100px; height: 60px; background: #4a90d9; margin: auto;"></div>
                    <figcaption>Figure 1: A placeholder image</figcaption>
                  </figure>
                </main>
                
                <footer>
                  <p>&copy; 2025 OpenPDF HTML Test</p>
                </footer>
              </body>
            </html>
            """;

        File pdfFile = tempDir.resolve("html5-semantic-elements.pdf").toFile();

        try (OutputStream os = new FileOutputStream(pdfFile)) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(os);
        }

        assertTrue(pdfFile.exists(), "PDF file should exist");
        assertTrue(pdfFile.length() > 1000, "PDF file should not be empty");
        log.info("Generated PDF with HTML5 semantic elements: {}", pdfFile.getAbsolutePath());
    }

    /**
     * Tests HTML5 data-* attributes which are commonly used for JavaScript
     * interaction but should be parsed correctly by the HTML parser.
     */
    @Test
    void html5DataAttributes(@TempDir Path tempDir) throws Exception {
        String html = """
            <!DOCTYPE html>
            <html lang="en">
              <head>
                <meta charset="UTF-8">
                <title>HTML5 Data Attributes Test</title>
                <style>
                  @page { size: A4; margin: 2cm; }
                  body { font-family: Arial, sans-serif; font-size: 12pt; }
                  .product { border: 1px solid #ccc; padding: 10px; margin: 10px 0; }
                  .product-name { font-weight: bold; }
                  .product-price { color: green; }
                </style>
              </head>
              <body>
                <h1>Products with Data Attributes</h1>
                
                <div class="product" data-product-id="12345" data-category="electronics" data-price="299.99">
                  <p class="product-name">Smartphone X</p>
                  <p class="product-price">$299.99</p>
                </div>
                
                <div class="product" data-product-id="67890" data-category="electronics" data-price="999.99">
                  <p class="product-name">Laptop Pro</p>
                  <p class="product-price">$999.99</p>
                </div>
                
                <div class="product" data-product-id="11111" data-category="accessories" data-price="49.99">
                  <p class="product-name">Wireless Mouse</p>
                  <p class="product-price">$49.99</p>
                </div>
              </body>
            </html>
            """;

        File pdfFile = tempDir.resolve("html5-data-attributes.pdf").toFile();

        try (OutputStream os = new FileOutputStream(pdfFile)) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(os);
        }

        assertTrue(pdfFile.exists(), "PDF file should exist");
        assertTrue(pdfFile.length() > 1000, "PDF file should not be empty");
        log.info("Generated PDF with HTML5 data attributes: {}", pdfFile.getAbsolutePath());
    }

    /**
     * Tests that the HTML parser can handle malformed HTML gracefully.
     * The htmlunit-neko parser is error-tolerant and can fix common mistakes.
     */
    @Test
    void malformedHtmlHandling(@TempDir Path tempDir) throws Exception {
        // This HTML has intentional issues:
        // - Missing closing tags
        // - Improperly nested elements
        // - Missing quotes around attributes
        String html = """
            <!DOCTYPE html>
            <html>
              <head>
                <title>Malformed HTML Test
                <style>
                  body { font-family: Arial; font-size: 12pt; }
                  .highlight { background: yellow; }
                </style>
              </head>
              <body>
                <h1>Malformed HTML Test</h1>
                <p>This paragraph is not properly closed
                <p>This is another paragraph with <b>bold and <i>italic</b> text</i>
                <div class=highlight>
                  Missing quotes around class attribute
                </div>
                <ul>
                  <li>Item 1
                  <li>Item 2
                  <li>Item 3
                </ul>
                <table>
                  <tr><td>Cell 1<td>Cell 2
                  <tr><td>Cell 3<td>Cell 4
                </table>
              </body>
            </html>
            """;

        File pdfFile = tempDir.resolve("malformed-html.pdf").toFile();

        try (OutputStream os = new FileOutputStream(pdfFile)) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(os);
        }

        assertTrue(pdfFile.exists(), "PDF file should exist");
        assertTrue(pdfFile.length() > 1000, "PDF file should not be empty");
        log.info("Generated PDF from malformed HTML: {}", pdfFile.getAbsolutePath());
    }

    /**
     * Tests HTML5 form elements including new input types.
     * Note: These are rendered as static content in PDF.
     */
    @Test
    void html5FormElements(@TempDir Path tempDir) throws Exception {
        String html = """
            <!DOCTYPE html>
            <html lang="en">
              <head>
                <meta charset="UTF-8">
                <title>HTML5 Form Elements Test</title>
                <style>
                  @page { size: A4; margin: 2cm; }
                  body { font-family: Arial, sans-serif; font-size: 12pt; }
                  form { max-width: 400px; }
                  label { display: block; margin-top: 10px; font-weight: bold; }
                  input, select, textarea { width: 100%; padding: 5px; margin-top: 5px; }
                  fieldset { border: 1px solid #ccc; padding: 10px; margin: 10px 0; }
                  legend { font-weight: bold; padding: 0 5px; }
                  .form-group { margin-bottom: 15px; }
                  output { display: block; padding: 5px; background: #f0f0f0; }
                  meter, progress { width: 100%; height: 20px; }
                  details { margin: 10px 0; }
                  summary { cursor: pointer; font-weight: bold; }
                </style>
              </head>
              <body>
                <h1>HTML5 Form Elements</h1>
                
                <form>
                  <fieldset>
                    <legend>Personal Information</legend>
                    
                    <div class="form-group">
                      <label for="name">Name:</label>
                      <input type="text" id="name" name="name" placeholder="Enter your name" required>
                    </div>
                    
                    <div class="form-group">
                      <label for="email">Email:</label>
                      <input type="email" id="email" name="email" placeholder="your@email.com">
                    </div>
                    
                    <div class="form-group">
                      <label for="phone">Phone:</label>
                      <input type="tel" id="phone" name="phone" placeholder="+1-234-567-8900">
                    </div>
                    
                    <div class="form-group">
                      <label for="dob">Date of Birth:</label>
                      <input type="date" id="dob" name="dob">
                    </div>
                  </fieldset>
                  
                  <fieldset>
                    <legend>Preferences</legend>
                    
                    <div class="form-group">
                      <label for="color">Favorite Color:</label>
                      <input type="color" id="color" name="color" value="#4a90d9">
                    </div>
                    
                    <div class="form-group">
                      <label for="range">Satisfaction (1-10):</label>
                      <input type="range" id="range" name="range" min="1" max="10" value="7">
                    </div>
                    
                    <div class="form-group">
                      <label for="search">Search:</label>
                      <input type="search" id="search" name="search" placeholder="Search...">
                    </div>
                  </fieldset>
                  
                  <fieldset>
                    <legend>HTML5 Elements</legend>
                    
                    <div class="form-group">
                      <label>Progress:</label>
                      <progress value="70" max="100">70%</progress>
                    </div>
                    
                    <div class="form-group">
                      <label>Meter:</label>
                      <meter value="0.6" min="0" max="1">60%</meter>
                    </div>
                    
                    <div class="form-group">
                      <label>Output:</label>
                      <output name="result">Calculated result: 42</output>
                    </div>
                  </fieldset>
                  
                  <details>
                    <summary>Additional Options</summary>
                    <p>These are additional options that can be expanded.</p>
                    <ul>
                      <li>Option A</li>
                      <li>Option B</li>
                      <li>Option C</li>
                    </ul>
                  </details>
                  
                  <datalist id="browsers">
                    <option value="Chrome">
                    <option value="Firefox">
                    <option value="Safari">
                    <option value="Edge">
                  </datalist>
                </form>
              </body>
            </html>
            """;

        File pdfFile = tempDir.resolve("html5-form-elements.pdf").toFile();

        try (OutputStream os = new FileOutputStream(pdfFile)) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(os);
        }

        assertTrue(pdfFile.exists(), "PDF file should exist");
        assertTrue(pdfFile.length() > 1000, "PDF file should not be empty");
        log.info("Generated PDF with HTML5 form elements: {}", pdfFile.getAbsolutePath());
    }

    /**
     * Tests CSS3 features including flexbox, grid layout concepts,
     * rounded corners, shadows, and gradients.
     */
    @Test
    void css3Features(@TempDir Path tempDir) throws Exception {
        String html = """
            <!DOCTYPE html>
            <html lang="en">
              <head>
                <meta charset="UTF-8">
                <title>CSS3 Features Test</title>
                <style>
                  @page { size: A4; margin: 2cm; }
                  body { font-family: Arial, sans-serif; font-size: 12pt; }
                  
                  /* Border radius */
                  .rounded { 
                    border: 2px solid #333; 
                    border-radius: 10px; 
                    padding: 15px; 
                    margin: 10px 0; 
                  }
                  
                  /* Box shadow */
                  .shadowed { 
                    box-shadow: 3px 3px 10px rgba(0,0,0,0.3); 
                    padding: 15px; 
                    margin: 10px 0;
                    background: white;
                  }
                  
                  /* Text shadow */
                  .text-shadow { 
                    text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
                    font-size: 18pt;
                    margin: 10px 0;
                  }
                  
                  /* Multiple columns */
                  .columns {
                    column-count: 2;
                    column-gap: 20px;
                    column-rule: 1px solid #ccc;
                    margin: 10px 0;
                  }
                  
                  /* Transforms */
                  .rotated {
                    transform: rotate(5deg);
                    display: inline-block;
                    background: #f0f0f0;
                    padding: 10px;
                    margin: 20px;
                  }
                  
                  /* Opacity */
                  .transparent {
                    opacity: 0.7;
                    background: #4a90d9;
                    color: white;
                    padding: 15px;
                    margin: 10px 0;
                  }
                  
                  /* Custom properties (CSS variables) - for future support */
                  :root {
                    --primary-color: #4a90d9;
                    --secondary-color: #2c5282;
                  }
                  
                  .card {
                    border: 1px solid #ddd;
                    border-radius: 8px;
                    padding: 20px;
                    margin: 10px 0;
                    background: #fafafa;
                  }
                  
                  .card-header {
                    font-size: 14pt;
                    font-weight: bold;
                    border-bottom: 1px solid #eee;
                    padding-bottom: 10px;
                    margin-bottom: 10px;
                  }
                </style>
              </head>
              <body>
                <h1>CSS3 Features Demonstration</h1>
                
                <h2>Border Radius</h2>
                <div class="rounded">
                  This box has rounded corners using CSS3 border-radius property.
                </div>
                
                <h2>Box Shadow</h2>
                <div class="shadowed">
                  This box has a shadow effect using CSS3 box-shadow property.
                </div>
                
                <h2>Text Shadow</h2>
                <p class="text-shadow">This text has a shadow effect.</p>
                
                <h2>Multiple Columns</h2>
                <div class="columns">
                  Lorem ipsum dolor sit amet, consectetur adipiscing elit. 
                  Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. 
                  Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris 
                  nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in 
                  reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur.
                </div>
                
                <h2>Opacity</h2>
                <div class="transparent">
                  This box has reduced opacity (70%).
                </div>
                
                <h2>Card Component</h2>
                <div class="card">
                  <div class="card-header">Card Title</div>
                  <p>This is a modern card component with rounded corners and subtle styling.</p>
                </div>
              </body>
            </html>
            """;

        File pdfFile = tempDir.resolve("css3-features.pdf").toFile();

        try (OutputStream os = new FileOutputStream(pdfFile)) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(os);
        }

        assertTrue(pdfFile.exists(), "PDF file should exist");
        assertTrue(pdfFile.length() > 1000, "PDF file should not be empty");
        log.info("Generated PDF with CSS3 features: {}", pdfFile.getAbsolutePath());
    }

    /**
     * Tests self-closing tags and void elements in HTML5.
     * Tests that the parser correctly handles both XML-style and HTML5-style void elements.
     */
    @Test
    void html5VoidElements(@TempDir Path tempDir) throws Exception {
        String html = """
            <!DOCTYPE html>
            <html lang="en">
              <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <meta name="description" content="Testing void elements">
                <link rel="stylesheet" href="nonexistent.css">
                <title>HTML5 Void Elements Test</title>
                <style>
                  @page { size: A4; margin: 2cm; }
                  body { font-family: Arial, sans-serif; font-size: 12pt; }
                  hr { border: none; border-top: 2px solid #333; margin: 20px 0; }
                  .image-placeholder { 
                    width: 100px; height: 60px; 
                    background: #ddd; display: inline-block;
                    margin: 5px;
                  }
                </style>
              </head>
              <body>
                <h1>HTML5 Void Elements</h1>
                
                <p>Testing various self-closing/void elements:</p>
                
                <h2>Line Breaks</h2>
                <p>Line 1<br>Line 2<br/>Line 3<br />Line 4</p>
                
                <h2>Horizontal Rule</h2>
                <hr>
                <p>Content after horizontal rule.</p>
                <hr/>
                <p>Content after self-closing horizontal rule.</p>
                
                <h2>Images (placeholders)</h2>
                <div class="image-placeholder"></div>
                <div class="image-placeholder"></div>
                
                <h2>Input Elements</h2>
                <p>Text input: <input type="text" value="sample text"></p>
                <p>Checkbox: <input type="checkbox" checked> Option 1</p>
                <p>Radio: <input type="radio" name="choice"> Choice A</p>
                
                <h2>Word Break Opportunity</h2>
                <p>Supercalifragilistic<wbr>expialidocious</p>
                
                <h2>Embedded Content</h2>
                <p>Source element (in audio/video):</p>
                <audio controls>
                  <source src="audio.mp3" type="audio/mpeg">
                  Your browser does not support the audio element.
                </audio>
                
                <p>Track element (for captions):</p>
                <video width="200" height="150" controls>
                  <track kind="captions" src="captions.vtt" srclang="en">
                  Your browser does not support the video element.
                </video>
              </body>
            </html>
            """;

        File pdfFile = tempDir.resolve("html5-void-elements.pdf").toFile();

        try (OutputStream os = new FileOutputStream(pdfFile)) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(os);
        }

        assertTrue(pdfFile.exists(), "PDF file should exist");
        assertTrue(pdfFile.length() > 1000, "PDF file should not be empty");
        log.info("Generated PDF with HTML5 void elements: {}", pdfFile.getAbsolutePath());
    }

    /**
     * Tests a comprehensive HTML5 document with multiple modern features combined.
     */
    @Test
    void comprehensiveHtml5Document(@TempDir Path tempDir) throws Exception {
        String html = """
            <!DOCTYPE html>
            <html lang="en">
              <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <meta name="author" content="OpenPDF Team">
                <meta name="description" content="Comprehensive HTML5 test document">
                <meta name="keywords" content="HTML5, CSS3, OpenPDF, PDF generation">
                <title>Comprehensive HTML5 Test Document</title>
                <style>
                  @page { 
                    size: A4; 
                    margin: 2cm;
                    @top-center { content: "OpenPDF HTML5 Test"; }
                    @bottom-center { content: "Page " counter(page) " of " counter(pages); }
                  }
                  
                  * { box-sizing: border-box; }
                  
                  body { 
                    font-family: 'Helvetica', 'Arial', sans-serif; 
                    font-size: 11pt;
                    line-height: 1.5;
                    color: #333;
                    margin: 0;
                    padding: 0;
                  }
                  
                  header {
                    background: linear-gradient(to right, #4a90d9, #2c5282);
                    color: white;
                    padding: 20px;
                    text-align: center;
                    margin-bottom: 20px;
                  }
                  
                  header h1 { margin: 0; font-size: 24pt; }
                  header p { margin: 5px 0 0 0; opacity: 0.9; }
                  
                  nav {
                    background: #f5f5f5;
                    padding: 10px 20px;
                    border-bottom: 1px solid #ddd;
                  }
                  
                  nav ul { 
                    list-style: none; 
                    margin: 0; 
                    padding: 0; 
                  }
                  
                  nav li { 
                    display: inline; 
                    margin-right: 20px; 
                  }
                  
                  nav a { 
                    color: #4a90d9; 
                    text-decoration: none; 
                  }
                  
                  main { padding: 20px; }
                  
                  section { margin-bottom: 30px; }
                  
                  h2 { 
                    color: #2c5282; 
                    border-bottom: 2px solid #4a90d9;
                    padding-bottom: 5px;
                  }
                  
                  .card-container {
                    display: block;
                  }
                  
                  .card {
                    border: 1px solid #ddd;
                    border-radius: 8px;
                    padding: 15px;
                    margin-bottom: 15px;
                    background: #fafafa;
                    box-shadow: 0 2px 4px rgba(0,0,0,0.1);
                  }
                  
                  .card h3 { 
                    margin: 0 0 10px 0; 
                    color: #333;
                  }
                  
                  .badge {
                    display: inline-block;
                    padding: 3px 8px;
                    background: #4a90d9;
                    color: white;
                    border-radius: 12px;
                    font-size: 9pt;
                    margin-right: 5px;
                  }
                  
                  .badge.success { background: #48bb78; }
                  .badge.warning { background: #ed8936; }
                  .badge.danger { background: #f56565; }
                  
                  table {
                    width: 100%;
                    border-collapse: collapse;
                    margin: 15px 0;
                  }
                  
                  th, td {
                    border: 1px solid #ddd;
                    padding: 10px;
                    text-align: left;
                  }
                  
                  th { background: #f5f5f5; font-weight: bold; }
                  
                  tr:nth-child(even) { background: #fafafa; }
                  
                  blockquote {
                    border-left: 4px solid #4a90d9;
                    margin: 20px 0;
                    padding: 10px 20px;
                    background: #f8f9fa;
                    font-style: italic;
                  }
                  
                  code {
                    background: #f4f4f4;
                    padding: 2px 6px;
                    border-radius: 4px;
                    font-family: 'Courier New', monospace;
                    font-size: 10pt;
                  }
                  
                  pre {
                    background: #282c34;
                    color: #abb2bf;
                    padding: 15px;
                    border-radius: 8px;
                    overflow-x: auto;
                    font-size: 10pt;
                  }
                  
                  .highlight { background: #fff3cd; padding: 2px 4px; }
                  
                  mark { background: #ffeaa7; padding: 1px 3px; }
                  
                  footer {
                    background: #333;
                    color: white;
                    padding: 20px;
                    text-align: center;
                    margin-top: 30px;
                  }
                  
                  footer a { color: #4a90d9; }
                </style>
              </head>
              <body>
                <header>
                  <h1>OpenPDF HTML5 Feature Test</h1>
                  <p>Demonstrating modern HTML5 and CSS3 support</p>
                </header>
                
                <nav>
                  <ul>
                    <li><a href="#features">Features</a></li>
                    <li><a href="#examples">Examples</a></li>
                    <li><a href="#code">Code</a></li>
                    <li><a href="#contact">Contact</a></li>
                  </ul>
                </nav>
                
                <main>
                  <section id="features">
                    <h2>Supported Features</h2>
                    
                    <div class="card-container">
                      <div class="card">
                        <h3>HTML5 Parser</h3>
                        <p>
                          <span class="badge success">Active</span>
                          <span class="badge">htmlunit-neko</span>
                        </p>
                        <p>The htmlunit-neko parser provides HTML5-compliant parsing with error tolerance.</p>
                      </div>
                      
                      <div class="card">
                        <h3>Semantic Elements</h3>
                        <p>
                          <span class="badge success">Supported</span>
                        </p>
                        <p>Full support for <code>&lt;header&gt;</code>, <code>&lt;footer&gt;</code>, 
                           <code>&lt;nav&gt;</code>, <code>&lt;article&gt;</code>, <code>&lt;section&gt;</code>, 
                           and more.</p>
                      </div>
                      
                      <div class="card">
                        <h3>CSS3 Styling</h3>
                        <p>
                          <span class="badge warning">Partial</span>
                        </p>
                        <p>Support for border-radius, box-shadow, and other CSS3 properties is ongoing.</p>
                      </div>
                    </div>
                  </section>
                  
                  <section id="examples">
                    <h2>Examples</h2>
                    
                    <h3>Text Formatting</h3>
                    <p>
                      Regular text with <strong>bold</strong>, <em>italic</em>, 
                      <mark>highlighted</mark>, <del>deleted</del>, and <ins>inserted</ins> content.
                    </p>
                    
                    <h3>Abbreviations and Definitions</h3>
                    <p>
                      <abbr title="HyperText Markup Language">HTML</abbr> is the standard markup language.
                      <dfn>CSS</dfn> stands for Cascading Style Sheets.
                    </p>
                    
                    <h3>Data Table</h3>
                    <table>
                      <thead>
                        <tr>
                          <th>Feature</th>
                          <th>Status</th>
                          <th>Priority</th>
                        </tr>
                      </thead>
                      <tbody>
                        <tr>
                          <td>HTML5 Parsing</td>
                          <td><span class="badge success">Done</span></td>
                          <td>High</td>
                        </tr>
                        <tr>
                          <td>CSS3 Support</td>
                          <td><span class="badge warning">In Progress</span></td>
                          <td>High</td>
                        </tr>
                        <tr>
                          <td>JavaScript</td>
                          <td><span class="badge danger">Planned</span></td>
                          <td>Low</td>
                        </tr>
                      </tbody>
                    </table>
                    
                    <h3>Quote</h3>
                    <blockquote>
                      <p>"The best way to predict the future is to invent it."</p>
                      <cite>— Alan Kay</cite>
                    </blockquote>
                  </section>
                  
                  <section id="code">
                    <h2>Code Example</h2>
                    <p>Here's a simple example of using <code>ITextRenderer</code>:</p>
                    <pre>ITextRenderer renderer = new ITextRenderer();
renderer.setDocumentFromString(html);
renderer.layout();
renderer.createPDF(outputStream);</pre>
                  </section>
                  
                  <section id="contact">
                    <h2>Contact Information</h2>
                    <address>
                      <strong>OpenPDF Project</strong><br>
                      GitHub: <a href="https://github.com/LibrePDF/OpenPDF">LibrePDF/OpenPDF</a><br>
                      License: LGPL 2.1
                    </address>
                  </section>
                </main>
                
                <footer>
                  <p>&copy; 2025 OpenPDF Project. Licensed under LGPL.</p>
                  <p>Built with <a href="https://github.com/HtmlUnit/htmlunit-neko">htmlunit-neko</a> HTML parser.</p>
                </footer>
              </body>
            </html>
            """;

        File pdfFile = tempDir.resolve("comprehensive-html5.pdf").toFile();

        try (OutputStream os = new FileOutputStream(pdfFile)) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(os);
        }

        assertTrue(pdfFile.exists(), "PDF file should exist");
        assertTrue(pdfFile.length() > 1000, "PDF file should not be empty");
        log.info("Generated comprehensive HTML5 PDF: {}", pdfFile.getAbsolutePath());
    }
}
