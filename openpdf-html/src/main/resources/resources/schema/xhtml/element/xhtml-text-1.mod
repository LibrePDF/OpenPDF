<!-- ...................................................................... -->
<!-- XHTML Text Module  ................................................... -->
<!-- file: xhtml-text-1.mod

     This is XHTML, a reformulation of HTML as a modular XML application.
     Copyright 1998-2001 W3C (MIT, INRIA, Keio), All Rights Reserved.

     This DTD module is identified by the PUBLIC and SYSTEM identifiers:

       PUBLIC "-//W3C//ELEMENTS XHTML Text 1.0//EN"
       SYSTEM "http://www.w3.org/TR/xhtml-modularization/DTD/xhtml-text-1.mod"

     Revisions:
     (none)
     ....................................................................... -->

<!-- Textual Content

     The Text module includes declarations for all core
     text container elements and their attributes.
-->

<!ENTITY % xhtml-inlstruct.module "INCLUDE" >
<![%xhtml-inlstruct.module;[
<!ENTITY % xhtml-inlstruct.mod
     PUBLIC "-//W3C//ELEMENTS XHTML Inline Structural 1.0//EN"
            "xhtml-inlstruct-1.mod" >
%xhtml-inlstruct.mod;]]>

<!ENTITY % xhtml-inlphras.module "INCLUDE" >
<![%xhtml-inlphras.module;[
<!ENTITY % xhtml-inlphras.mod
     PUBLIC "-//W3C//ELEMENTS XHTML Inline Phrasal 1.0//EN"
            "xhtml-inlphras-1.mod" >
%xhtml-inlphras.mod;]]>

<!ENTITY % xhtml-blkstruct.module "INCLUDE" >
<![%xhtml-blkstruct.module;[
<!ENTITY % xhtml-blkstruct.mod
     PUBLIC "-//W3C//ELEMENTS XHTML Block Structural 1.0//EN"
            "xhtml-blkstruct-1.mod" >
%xhtml-blkstruct.mod;]]>

<!ENTITY % xhtml-blkphras.module "INCLUDE" >
<![%xhtml-blkphras.module;[
<!ENTITY % xhtml-blkphras.mod
     PUBLIC "-//W3C//ELEMENTS XHTML Block Phrasal 1.0//EN"
            "xhtml-blkphras-1.mod" >
%xhtml-blkphras.mod;]]>

<!-- end of xhtml-text-1.mod -->
