package com.lowagie.examples.objects;

import com.lowagie.examples.AbstractRunner;
import com.lowagie.examples.objects.anchors.AnchorsRunner;
import com.lowagie.examples.objects.bookmarks.BookmarksRunner;
import com.lowagie.examples.objects.chunk.ChunkRunner;
import com.lowagie.examples.objects.columns.ColumnsRunner;
import com.lowagie.examples.objects.images.ImagesRunner;
import com.lowagie.examples.objects.tables.TablesRunner;

public class ObjectsRunner extends AbstractRunner {
    public static void main(String[] args) {
        String[] argsNew = getOutputDirectory(args, "objects");

        Chunks.main(argsNew);
        DifferentFonts.main(argsNew);
        FancyLists.main(argsNew);
        Lists.main(argsNew);
        NegativeLeading.main(argsNew);
        ParagraphAttributes.main(argsNew);
        Paragraphs.main(argsNew);
        Phrases.main(argsNew);
        SpaceWordRatio.main(argsNew);
        SymbolSubstitution.main(argsNew);

        AnchorsRunner.main(argsNew);
        BookmarksRunner.main(argsNew);
        ChunkRunner.main(argsNew);
        ColumnsRunner.main(argsNew);
        ImagesRunner.main(argsNew);
        TablesRunner.main(argsNew);
    }
}
