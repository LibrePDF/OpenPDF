<!-- ...................................................................... -->
<!-- XHTML Document Metainformation Module  ............................... -->
<!-- file: xhtml-meta-1.mod

     This is XHTML, a reformulation of HTML as a modular XML application.
     Copyright 1998-2001 W3C (MIT, INRIA, Keio), All Rights Reserved.

     This DTD module is identified by the PUBLIC and SYSTEM identifiers:

       PUBLIC "-//W3C//ELEMENTS XHTML Metainformation 1.0//EN"
       SYSTEM "http://www.w3.org/TR/xhtml-modularization/DTD/xhtml-meta-1.mod"

     Revisions:
     (none)
     ....................................................................... -->

<!-- Meta Information

        meta

     This module declares the meta element type and its attributes,
     used to provide declarative document metainformation.
-->

<!-- meta: Generic Metainformation ..................... -->

<!ENTITY % meta.element  "INCLUDE" >
<![%meta.element;[
<!ENTITY % meta.content  "EMPTY" >
<!ENTITY % meta.qname  "meta" >
<!ELEMENT %meta.qname;  %meta.content; >
<!-- end of meta.element -->]]>

<!ENTITY % meta.attlist  "INCLUDE" >
<![%meta.attlist;[
<!ATTLIST %meta.qname;
      %XHTML.xmlns.attrib;
      %I18n.attrib;
      http-equiv   NMTOKEN                  #IMPLIED
      name         NMTOKEN                  #IMPLIED
      content      CDATA                    #REQUIRED
      scheme       CDATA                    #IMPLIED
>
<!-- end of meta.attlist -->]]>

<!-- end of xhtml-meta-1.mod -->
