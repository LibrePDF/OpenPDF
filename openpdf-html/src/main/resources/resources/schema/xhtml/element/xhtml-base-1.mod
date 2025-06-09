<!-- ...................................................................... -->
<!-- XHTML Base Element Module  ........................................... -->
<!-- file: xhtml-base-1.mod

     This is XHTML, a reformulation of HTML as a modular XML application.
     Copyright 1998-2001 W3C (MIT, INRIA, Keio), All Rights Reserved.

     This DTD module is identified by the PUBLIC and SYSTEM identifiers:

       PUBLIC "-//W3C//ELEMENTS XHTML Base Element 1.0//EN"
       SYSTEM "http://www.w3.org/TR/xhtml-modularization/DTD/xhtml-base-1.mod"

     Revisions:
     (none)
     ....................................................................... -->

<!-- Base element

        base

     This module declares the base element type and its attributes,
     used to define a base URI against which relative URIs in the
     document will be resolved.

     Note that this module also redeclares the content model for
     the head element to include the base element.
-->

<!-- base: Document Base URI ........................... -->

<!ENTITY % base.element  "INCLUDE" >
<![%base.element;[
<!ENTITY % base.content  "EMPTY" >
<!ENTITY % base.qname  "base" >
<!ELEMENT %base.qname;  %base.content; >
<!-- end of base.element -->]]>

<!ENTITY % base.attlist  "INCLUDE" >
<![%base.attlist;[
<!ATTLIST %base.qname;
      %XHTML.xmlns.attrib;
      href         %URI.datatype;           #REQUIRED
>
<!-- end of base.attlist -->]]>

<!ENTITY % head.content
    "( %HeadOpts.mix;,
     ( ( %title.qname;, %HeadOpts.mix;, ( %base.qname;, %HeadOpts.mix; )? )
     | ( %base.qname;, %HeadOpts.mix;, ( %title.qname;, %HeadOpts.mix; ))))"
>

<!-- end of xhtml-base-1.mod -->
