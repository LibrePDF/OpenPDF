package org.openpdf.css.style;

import org.openpdf.css.constants.IdentValue;

import java.util.Set;

class CssKnowledge {

    static final Set<IdentValue> MARGINS_NOT_ALLOWED = Set.of(
            IdentValue.TABLE_HEADER_GROUP, IdentValue.TABLE_ROW_GROUP, IdentValue.TABLE_FOOTER_GROUP,
            IdentValue.TABLE_ROW, IdentValue.TABLE_CELL
    );

    static final Set<IdentValue> BORDERS_NOT_ALLOWED = Set.of(
            IdentValue.TABLE_HEADER_GROUP, IdentValue.TABLE_ROW_GROUP, IdentValue.TABLE_FOOTER_GROUP,
            IdentValue.TABLE_ROW
    );

    static final Set<IdentValue> OVERFLOW_APPLICABLE = Set.of(
            IdentValue.BLOCK, IdentValue.LIST_ITEM,
            IdentValue.TABLE, IdentValue.INLINE_BLOCK, IdentValue.TABLE_CELL
    );

    static final Set<IdentValue> MAY_HAVE_FIRST_LINE = Set.of(
            IdentValue.BLOCK, IdentValue.LIST_ITEM, IdentValue.RUN_IN,
            IdentValue.TABLE, IdentValue.TABLE_CELL, IdentValue.TABLE_CAPTION,
            IdentValue.INLINE_BLOCK
    );

    static final Set<IdentValue> MAY_HAVE_FIRST_LETTER = Set.of(
            IdentValue.BLOCK, IdentValue.LIST_ITEM,
            IdentValue.TABLE_CELL, IdentValue.TABLE_CAPTION,
            IdentValue.INLINE_BLOCK
    );

    static final Set<IdentValue> BLOCK_EQUIVALENTS = Set.of(
            IdentValue.BLOCK, IdentValue.LIST_ITEM,
            IdentValue.RUN_IN, IdentValue.INLINE_BLOCK,
            IdentValue.TABLE, IdentValue.INLINE_TABLE
    );

    static final Set<IdentValue> LAID_OUT_IN_INLINE_CONTEXT = Set.of(
            IdentValue.INLINE, IdentValue.INLINE_BLOCK, IdentValue.INLINE_TABLE
    );

    static final Set<IdentValue> TABLE_SECTIONS = Set.of(
            IdentValue.TABLE_ROW_GROUP, IdentValue.TABLE_HEADER_GROUP, IdentValue.TABLE_FOOTER_GROUP
    );

    static final Set<IdentValue> UNDER_TABLE_LAYOUT = Set.of(
            IdentValue.TABLE_ROW_GROUP, IdentValue.TABLE_HEADER_GROUP, IdentValue.TABLE_FOOTER_GROUP,
            IdentValue.TABLE_ROW, IdentValue.TABLE_CELL,
            IdentValue.TABLE_CAPTION, IdentValue.TABLE_COLUMN, IdentValue.TABLE_COLUMN_GROUP
    );
}
