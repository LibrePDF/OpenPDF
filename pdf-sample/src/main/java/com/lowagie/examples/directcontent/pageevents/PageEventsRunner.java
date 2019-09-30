package com.lowagie.examples.directcontent.pageevents;

import com.lowagie.examples.AbstractRunner;

public class PageEventsRunner extends AbstractRunner {
    public static void main(String[] args) {
        String[] argsNew = getOutputDirectory(args, "pageevents");

        Bookmarks.main(argsNew);
        EndPage.main(argsNew);
        Events.main(argsNew);
        PageNumbersWatermark.main(argsNew);
    }
}
