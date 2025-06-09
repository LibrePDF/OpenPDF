<!-- ...................................................................... -->
<!-- XHTML Document Style Sheet Module  ................................... -->
<!-- file: xhtml-style-1.mod

     This is XHTML, a reformulation of HTML as a modular XML application.
     Copyright 1998-2001 W3C (MIT, INRIA, Keio), All Rights Reserved.

     This DTD module is identified by the PUBLIC and SYSTEM identifiers:

       PUBLIC "-//W3C//DTD XHTML Style Sheets 1.0//EN"
       SYSTEM "http://www.w3.org/TR/xhtml-modularization/DTD/xhtml-style-1.mod"

     Revisions:
     (none)
     ....................................................................... -->

<!-- Style Sheets

        style

     This module declares the style element type and its attributes,
     used to embed style sheet information in the document head element.
-->

<!-- style: Style Sheet Information .................... -->

<!ENTITY % style.element  "INCLUDE" >
<![%style.element;[
<!ENTITY % style.content  "( #PCDATA )" >
<!ENTITY % style.qname  "style" >
<!ELEMENT %style.qname;  %style.content; >
<!-- end of style.element -->]]>

<!ENTITY % style.attlist  "INCLUDE" >
<![%style.attlist;[
<!ATTLIST %style.qname;
      %XHTML.xmlns.attrib;
      %title.attrib;
      %I18n.attrib;
      type         %ContentType.datatype;   #REQUIRED
      media        %MediaDesc.datatype;     #IMPLIED
      xml:space    ( preserve )             #FIXED 'preserve'
>
<!-- end of style.attlist -->]]>

<!-- end of xhtml-style-1.mod -->
