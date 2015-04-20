# iText is a Java PDF library originally dual licensed under MPL/LGPL #

## Background ##

Beginning with version 5.0 the developers have moved to the AGPL to improve their ability to sell commercial licenses. I fully respect the developers' wishes and rights. To assist those desiring to stick with the old license I'm making the final MPL/LGPL version more easily available.

## Changes ##
This repo has the following changes from the old "original" 4.2.0 version:
 - compilation now supports also java 8, but compatibility level is in maven pom set to java 7
 - in the case of unexpected end of PDF file the IOException is thrown (not PDFNull)
 - merged patch from Steven to fix NPE in XFA Form (escapeSom method)
 - merged UnembedFontPdfSmartCopy functionality from Vicente Alencar
 - merged RTF Footer functionality from ubermichael
 - compatibility fix to support the newest (1.52) bouncy castle libraries based/inspired by flex-developments fixes
 - some NPE fixes and tweaked maven support

## Contributing ##
iText has moved on to its new license, and it's not my intention to continue to develop this repo as a fork. I only 
sometimes fixing obvious bugs and compatibility problems with the newest libraries.
If you'd like to contribute to iText, that's awesome! I encourage you to!

Here are some options:
- If you are okay with the AGPL, send your contributions [upstream](http://itextpdf.com/).
- If you want to stay with LGPL/MPL then you can maintain your own fork of this repo, or start a brand new project starting from this code base.

Good luck!

## [Javadocs](http://kulatamicuda.github.com/iText-4.2.0/) ##

