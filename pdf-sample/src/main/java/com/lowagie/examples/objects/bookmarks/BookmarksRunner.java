package com.lowagie.examples.objects.bookmarks;

import com.lowagie.examples.AbstractRunner;

public class BookmarksRunner extends AbstractRunner {
    public static void main(String[] args) {
        String[] argsNew = getOutputDirectory(args, "bookmarks");

        Bookmarks.main(argsNew);
        ChapterSection.main(argsNew);
        Destinations.main(argsNew);
        Layers.main(argsNew);
        OutlineActions.main(argsNew);
        PageLabels.main(argsNew);
        ViewerPreferences.main(argsNew);
    }
}
