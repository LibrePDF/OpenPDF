package com.lowagie.examples.objects.tables.alternatives;

import com.lowagie.examples.AbstractRunner;

public class AlternativesRunner extends AbstractRunner {
    public static void main(String[] args) {
        String[] argsNew = getOutputDirectory(args, "alternatives");

//        JTable2Pdf.main(argsNew);
        LargeCell.main(argsNew);
        MyFirstTable.main(argsNew);
        NestedTables.main(argsNew);
        OldTable.main(argsNew);
        PaddingBorders.main(argsNew);
        RepeatingTable.main(argsNew);
        SpecificCells.main(argsNew);
        TablePdfPTable.main(argsNew);
        TableWithImage.main(argsNew);
    }
}
