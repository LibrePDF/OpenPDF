/*
  Copyright 2014 by Tizra Inc.
  The contents of this file are subject to the Mozilla Public License Version 1.1
  (the "License"); you may not use this file except in compliance with the License.
  You may obtain a copy of the License at http://www.mozilla.org/MPL/
  <p>
  Software distributed under the License is distributed on an "AS IS" basis,
  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
  for the specific language governing rights and limitations under the License.
  <p>
  The Original Code is 'iText, a free JAVA-PDF library'.
  <p>
  The Initial Developer of the Original Code is Bruno Lowagie. Portions created by
  the Initial Developer are Copyright (C) 1999-2008 by Bruno Lowagie.
  All Rights Reserved.
  Co-Developer of the code is Paulo Soares. Portions created by the Co-Developer
  are Copyright (C) 2000-2008 by Paulo Soares. All Rights Reserved.
  <p>
  Contributor(s): all the names of the contributors are added in the source code
  where applicable.
  <p>
  Alternatively, the contents of this file may be used under the terms of the
  LGPL license (the "GNU LIBRARY GENERAL PUBLIC LICENSE"), in which case the
  provisions of LGPL are applicable instead of those above.  If you wish to
  allow use of your version of this file only under the terms of the LGPL
  License and not to allow others to use your version of this file under
  the MPL, indicate your decision by deleting the provisions above and
  replace them with the notice and other provisions required by the LGPL.
  If you do not delete the provisions above, a recipient may use your version
  of this file under either the MPL or the GNU LIBRARY GENERAL PUBLIC LICENSE.
  <p>
  This library is free software; you can redistribute it and/or modify it
  under the terms of the MPL as stated above or under the terms of the GNU
  Library General Public License as published by the Free Software Foundation;
  either version 2 of the License, or any later version.
  <p>
  This library is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  FOR A PARTICULAR PURPOSE. See the GNU Library general Public License for more
  details.
 */
package com.lowagie.text.pdf.parser;

/**
 *
 * process a series of objects and text fragments, assembling them into a one
 * final text object representing the whole content.
 *
 * @author dgd
 */
public interface TextAssembler {

    /**
     * @param completed
     *            process a complete chunk -- just add this subsection into the
     *            proper place.
     * @param contextName
     *            Name of the element context we are in. Null value if it's an
     *            Artifact.
     */
    void process(FinalText completed, String contextName);

    /**
     * @param completed
     *            process a complete chunk -- just add this subsection into the
     *            proper place.
     * @param contextName
     *            Name of the element context we are in. Null value if it's an
     *            Artifact.
     */
    void process(Word completed, String contextName);

    /**
     * @param parsed
     *            process one of a number of raw pdf text chunks, with
     *            placement, font, etc.
     * @param contextName
     *            Name of the element context we are in. Null value if it's an
     *            Artifact.
     */
    void process(ParsedText parsed, String contextName);

    /**
     * @param completed
     *            process a complete chunk -- just add this subsection into the
     *            proper place.
     */
    void renderText(FinalText completed);

    /**
     * @param parsed
     *            process one of a number of raw pdf text chunks, with
     *            placement, font, etc.
     */
    void renderText(ParsedTextImpl parsed);

    /**
     * @param containingElementName
     *            This is an element name to surround the extracted text
     * @return the final text for the set of fragments and fully parsed items we
     *         were passed during processing.
     */
    FinalText endParsingContext(String containingElementName);

    /**
     * assembler can calculate an identifier for each word on a page, for use in
     * markup.
     *
     * @return the new unique id.
     */
    String getWordId();

    /**
     * @param page number of the page we are assembling
     *
     */
    void setPage(int page);

    /**
     *
     */
    void reset();
}
