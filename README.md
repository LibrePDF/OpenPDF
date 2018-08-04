# OpenPDF is a Java PDF library, forked from iText #

OpenPDF is a Java library for creating and editing PDF files with a LGPL and MPL open source license. OpenPDF is based on a fork of iText 4. We welcome contributions from other developers. Please feel free to submit pull-requests and bugreports to this GitHub repository.

[![Join the chat at https://gitter.im/LibrePDF/OpenPDF](https://badges.gitter.im/LibrePDF/OpenPDF.svg)](https://gitter.im/LibrePDF/OpenPDF) [![Build Status](https://travis-ci.org/LibrePDF/OpenPDF.svg?branch=master)](https://travis-ci.org/LibrePDF/OpenPDF) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.librepdf/openpdf/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.librepdf/openpdf) [![License (LGPL version 3.0)](https://img.shields.io/badge/license-GNU%20LGPL%20version%203.0-blue.svg?style=flat-square)](http://opensource.org/licenses/LGPL-3.0) [![License (MPL)](https://img.shields.io/badge/license-Mozilla%20Public%20License-yellow.svg?style=flat-square)](http://opensource.org/licenses/MPL-2.0)

## OpenPDF version 1.1.0 released 2018-07-06 ##
Get version 1.1.0 here - https://github.com/LibrePDF/OpenPDF/releases/tag/1.1.0

### Previous Versions
- [Version 1.0.5](https://github.com/LibrePDF/OpenPDF/releases/tag/1.0.5) _released 2017-11-16_
- [Version 1.0.4](https://github.com/LibrePDF/OpenPDF/releases/tag/1.0.4) _released 2017-10-11_
- [Version 1.0.3](https://github.com/LibrePDF/OpenPDF/releases/tag/1.0.3) _released 2017-07-24_
- [Version 1.0.2](https://github.com/LibrePDF/OpenPDF/releases/tag/1.0.2) _released 2017-06-03_
- [Version 1.0.1](https://github.com/LibrePDF/OpenPDF/releases/tag/1.0.1) _released 2017-01-28_
- [Version 1.0](https://github.com/LibrePDF/OpenPDF/releases/tag/1.0) _released 2016-05-03_

## License ##

GNU General Lesser Public License (LGPL) version 3.0 - http://www.gnu.org/licenses/lgpl.html

Mozilla Public License Version 2.0 - http://www.mozilla.org/MPL/2.0/

We want OpenPDF to be consist of source code with is consistently licensed with the LGPL and MPL licences only.
Since this is a software fork where we maintain code from a legacy software library (iText) we are reviewing the
history and license of the code-base. The result of this work has been that we have replaced some source code which
had other incompatible open source licenses. This also means that any new contributions to the project must have a dual LGPL and MPL license only.


## Use OpenPDF as Maven dependency
Add this to your pom.xml file:

        <dependency>
            <groupId>com.github.librepdf</groupId>
            <artifactId>openpdf</artifactId>
            <version>1.1.0</version>
        </dependency>

## Docs ##
See [examples](pdf-toolbox/src/test/java/com/lowagie/examples/) and [JavaDoc](https://librepdf.github.io/OpenPDF/docs-1-1-0/).

## Background ##

OpenPDF is open source with software with a LGPL and MPL license. It is a fork of iText version 4, more specifically iText svn tag 4.2.0, which was hosted publicly on sourceforge with LGPL and MPL license headers in the source code, and lgpl and mpl license documents in the svn repository.
Beginning with version 5.0 of iText, the developers have moved to the AGPL to improve their ability to sell commercial licenses. 

## Contributing ##
Release the hounds!  Please send all pull requests.
Make sure that your contributions can be released with a dual LGPL and MPL license. In particular, pull requests to the OpenPDF project must only contain code that you have written yourself. GPL or AGPL licensed code will not be acceptable.

## Google Group for users and developers ##

https://groups.google.com/d/forum/openpdf

## Dependencies ##

### Required: ###

 - Apache Commons IO
 - Apache Commons Compress
 - Apache Commons Text
 - Sanselan (Apache Commons Imaging)
 - PDFRenderer
 - DOM4j

### Optional: ###

  - BouncyCastle 1.60
    - Provider
    - PKIX/CMS
 - JUnit 4 - for unit testing
 - JFreeChart - for testing graphical examples
   - JFreeChart
   - JCommon
   - Servlet
