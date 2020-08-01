module com.github.librepdf.pdfToolbox {
    requires com.github.librepdf.openpdf;
    requires jfreechart;
    requires jcommon;

    exports com.lowagie.toolbox;
    exports com.lowagie.toolbox.arguments;
    exports com.lowagie.toolbox.arguments.filters;
    exports com.lowagie.toolbox.plugins;
    exports com.lowagie.toolbox.plugins.watermarker;
    exports com.lowagie.toolbox.swing;
    exports com.lowagie.tools;
}
