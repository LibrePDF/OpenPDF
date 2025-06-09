<!-- ...................................................................... -->
<!-- XHTML Link Element Module  ........................................... -->
<!-- file: xhtml-link-1.mod

     This is XHTML, a reformulation of HTML as a modular XML application.
     Copyright 1998-2001 W3C (MIT, INRIA, Keio), All Rights Reserved.

     This DTD module is identified by the PUBLIC and SYSTEM identifiers:

       PUBLIC "-//W3C//ELEMENTS XHTML Link Element 1.0//EN"
       SYSTEM "http://www.w3.org/TR/xhtml-modularization/DTD/xhtml-link-1.mod"

     Revisions:
     (none)
     ....................................................................... -->

<!-- Link element

        link

     This module declares the link element type and its attributes,
     which could (in principle) be used to define document-level links
     to external resources such as:

     a) for document specific toolbars/menus, e.g. start, contents,
        previous, next, index, end, help
     b) to link to a separate style sheet (rel="stylesheet")
     c) to make a link to a script (rel="script")
     d) by style sheets to control how collections of html nodes are
        rendered into printed documents
     e) to make a link to a printable version of this document
        e.g. a postscript or pdf version (rel="alternate" media="print")
-->

<!-- link: Media-Independent Link ...................... -->

<!ENTITY % link.element  "INCLUDE" >
<![%link.element;[
<!ENTITY % link.content  "EMPTY" >
<!ENTITY % link.qname  "link" >
<!ELEMENT %link.qname;  %link.content; >
<!-- end of link.element -->]]>

<!ENTITY % link.attlist  "INCLUDE" >
<![%link.attlist;[
<!ATTLIST %link.qname;
      %Common.attrib;
      charset      %Charset.datatype;       #IMPLIED
      href         %URI.datatype;           #IMPLIED
      hreflang     %LanguageCode.datatype;  #IMPLIED
      type         %ContentType.datatype;   #IMPLIED
      rel          %LinkTypes.datatype;     #IMPLIED
      rev          %LinkTypes.datatype;     #IMPLIED
      media        %MediaDesc.datatype;     #IMPLIED
>
<!-- end of link.attlist -->]]>

<!-- end of xhtml-link-1.mod -->
