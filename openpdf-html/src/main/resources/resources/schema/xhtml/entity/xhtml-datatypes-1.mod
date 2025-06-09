<!-- ...................................................................... -->
<!-- XHTML Datatypes Module  .............................................. -->
<!-- file: xhtml-datatypes-1.mod

     This is XHTML, a reformulation of HTML as a modular XML application.
     Copyright 1998-2001 W3C (MIT, INRIA, Keio), All Rights Reserved.

     This DTD module is identified by the PUBLIC and SYSTEM identifiers:

       PUBLIC "-//W3C//ENTITIES XHTML Datatypes 1.0//EN"
       SYSTEM "http://www.w3.org/TR/xhtml-modularization/DTD/xhtml-datatypes-1.mod"

     Revisions:
     (none)
     ....................................................................... -->

<!-- Datatypes

     defines containers for the following datatypes, many of
     these imported from other specifications and standards.
-->

<!-- Length defined for cellpadding/cellspacing -->

<!-- nn for pixels or nn% for percentage length -->
<!ENTITY % Length.datatype "CDATA" >

<!-- space-separated list of link types -->
<!ENTITY % LinkTypes.datatype "NMTOKENS" >

<!-- single or comma-separated list of media descriptors -->
<!ENTITY % MediaDesc.datatype "CDATA" >

<!-- pixel, percentage, or relative -->
<!ENTITY % MultiLength.datatype "CDATA" >

<!-- one or more digits (NUMBER) -->
<!ENTITY % Number.datatype "CDATA" >

<!-- integer representing length in pixels -->
<!ENTITY % Pixels.datatype "CDATA" >

<!-- script expression -->
<!ENTITY % Script.datatype "CDATA" >

<!-- textual content -->
<!ENTITY % Text.datatype "CDATA" >

<!-- Imported Datatypes ................................ -->

<!-- a single character from [ISO10646] -->
<!ENTITY % Character.datatype "CDATA" >

<!-- a character encoding, as per [RFC2045] -->
<!ENTITY % Charset.datatype "CDATA" >

<!-- a space separated list of character encodings, as per [RFC2045] -->
<!ENTITY % Charsets.datatype "CDATA" >

<!-- Color specification using color name or sRGB (#RRGGBB) values -->
<!ENTITY % Color.datatype "CDATA" >

<!-- media type, as per [RFC2045] -->
<!ENTITY % ContentType.datatype "CDATA" >

<!-- comma-separated list of media types, as per [RFC2045] -->
<!ENTITY % ContentTypes.datatype "CDATA" >

<!-- date and time information. ISO date format -->
<!ENTITY % Datetime.datatype "CDATA" >

<!-- formal public identifier, as per [ISO8879] -->
<!ENTITY % FPI.datatype "CDATA" >

<!-- a language code, as per [RFC3066] -->
<!ENTITY % LanguageCode.datatype "NMTOKEN" >

<!-- a Uniform Resource Identifier, see [URI] -->
<!ENTITY % URI.datatype "CDATA" >

<!-- a space-separated list of Uniform Resource Identifiers, see [URI] -->
<!ENTITY % URIs.datatype "CDATA" >

<!-- end of xhtml-datatypes-1.mod -->
