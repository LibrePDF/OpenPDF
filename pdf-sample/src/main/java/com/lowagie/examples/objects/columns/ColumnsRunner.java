package com.lowagie.examples.objects.columns;

import com.lowagie.examples.AbstractRunner;

public class ColumnsRunner extends AbstractRunner {
    public static void main(String[] args) {
        String[] argsNew = getOutputDirectory(args, "columns");

        Column.main(argsNew);
        ColumnIrregular.main(argsNew);
        ColumnObjects.main(argsNew);
        ColumnSimple.main(argsNew);
        MultiColumnIrregular.main(argsNew);
        MultiColumnR2L.main(argsNew);
        MultiColumnSimple.main(argsNew);
    }
}
