/*
 * Copyright © 2018 by the OpenPDF contributors.
 *
 * This file is a part of OpenPDF project.
 * This project is dual licensed under LGPL 3.0 and MPL 2.0 licenses.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.lowagie.text.pdf.core.document;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import com.lowagie.text.pdf.PdfWriter.PdfBody.PdfCrossReference;
import java.util.Arrays;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Tests for {@link PdfCrossReference}
 *
 * @author noavarice
 * @since 1.2.8
 */
class PdfCrossReferenceTest {

  /**
   * Tests comparison between different {@link PdfCrossReference cross-reference entries},
   * because such entries are ordered through according PDF objects offsets.
   */
  @TestFactory
  Iterable<DynamicTest> testComparison() {
    return Arrays.asList(
        // Testing {@link Comparable#compareTo(Object)} implementation
        dynamicTest("Test lesser", () -> {
          final PdfCrossReference entry1 = new PdfCrossReference(1, 0);
          final PdfCrossReference entry2 = new PdfCrossReference(2, 0);
          assertTrue(entry1.compareTo(entry2) < 0);
        }),
        dynamicTest("Test greater", () -> {
          final PdfCrossReference entry1 = new PdfCrossReference(2, 0);
          final PdfCrossReference entry2 = new PdfCrossReference(1, 0);
          assertTrue(entry1.compareTo(entry2) > 0);
        }),
        dynamicTest("Test equals", () -> {
          final PdfCrossReference entry1 = new PdfCrossReference(2, 0);
          final PdfCrossReference entry2 = new PdfCrossReference(2, 0);
          assertEquals(entry1.compareTo(entry2), 0);
        }),

        // Testing {@link Object#equals(Object)} implementation
        dynamicTest("Test lesser", () -> {
          final PdfCrossReference entry1 = new PdfCrossReference(1, 0);
          final PdfCrossReference entry2 = new PdfCrossReference(2, 0);
          assertNotEquals(entry1, entry2);
        }),
        dynamicTest("Test greater", () -> {
          final PdfCrossReference entry1 = new PdfCrossReference(2, 0);
          final PdfCrossReference entry2 = new PdfCrossReference(1, 0);
          assertNotEquals(entry1, entry2);
        }),
        dynamicTest("Test equals", () -> {
          final PdfCrossReference entry1 = new PdfCrossReference(2, 0);
          final PdfCrossReference entry2 = new PdfCrossReference(2, 0);
          assertEquals(entry1, entry2);
        }),

        // Testing {@link Object#hashCode()} implementation
        dynamicTest("Test hashcode", () -> {
          final int offset = 1000;
          assertEquals(new PdfCrossReference(offset, 0).hashCode(), offset);
        })
    );
  }
}
