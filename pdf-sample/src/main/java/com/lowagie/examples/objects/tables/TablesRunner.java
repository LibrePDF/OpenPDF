package com.lowagie.examples.objects.tables;

import com.lowagie.examples.AbstractRunner;
import com.lowagie.examples.objects.tables.alternatives.AlternativesRunner;
import com.lowagie.examples.objects.tables.pdfptable.PdfpTableRunner;

public class TablesRunner extends AbstractRunner {
    public static void main(String[] args) {
        String[] argsNew = getOutputDirectory(args, "tables");

        AddBigTable.main(argsNew);
        CellAlignment.main(argsNew);
        CellColors.main(argsNew);
        CellHeights.main(argsNew);
        CellPaddingLeading.main(argsNew);
        CellWidths.main(argsNew);
        DefaultCell.main(argsNew);
        ImageCell.main(argsNew);
        MyFirstTable.main(argsNew);
        NestedTables.main(argsNew);
        SplitRows.main(argsNew);
        TableBorders.main(argsNew);
        TableSpacing.main(argsNew);
        TableWidthAlignment.main(argsNew);

        AlternativesRunner.main(argsNew);
        PdfpTableRunner.main(argsNew);
    }
}
