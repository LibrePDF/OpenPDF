package com.lowagie.examples.objects.tables.pdfptable;

import com.lowagie.examples.AbstractRunner;

public class PdfpTableRunner extends AbstractRunner {
    public static void main(String[] args) {
        String[] argsNew = getOutputDirectory(args, "pdfptable");

        CellEvents.main(argsNew);
        FloatingBoxes.main(argsNew);
        FragmentTable.main(argsNew);
        NestedKeepItTogether.main(argsNew);
        SplitTable.main(argsNew);
        TableEvents1.main(argsNew);
        TableEvents2.main(argsNew);
        Tables.main(argsNew);
        VerticalTextInCells.main(argsNew);
        WriteSelectedRows.main(argsNew);
    }
}
