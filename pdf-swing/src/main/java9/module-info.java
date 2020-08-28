module com.github.librepdf.pdfSwing {
    requires com.github.librepdf.openpdf;
    requires dom4j;
    requires pdf.renderer;

    exports com.lowagie.rups;
    exports com.lowagie.rups.controller;
    exports com.lowagie.rups.io;
    exports com.lowagie.rups.io.filters;
    exports com.lowagie.rups.model;
    exports com.lowagie.rups.view;
    exports com.lowagie.rups.view.icons;
    exports com.lowagie.rups.view.itext;
    exports com.lowagie.rups.view.itext.treenodes;
    exports com.lowagie.rups.view.models;
}
