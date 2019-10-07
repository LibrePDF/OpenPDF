package com.lowagie.text.pdf;

import java.io.IOException;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.StandardFonts;
import org.junit.jupiter.api.Test;

class GreekAndCyrillicTest {

    private static final String INPUT = "Symbol: '\u25b2' Latin: 'äöüÄÖÜß'";

    @Test
    void testGreek() throws IOException {
        String greekText = "Some greek text: " +
                "Οἱ δὲ Φοίνιϰες οὗτοι οἱ σὺν Κάδμῳ ἀπιϰόμενοι.. ἐσήγαγον διδασϰάλια ἐς τοὺς ῞Ελληνας ϰαὶ δὴ ϰαὶ " +
                "γράμματα, οὐϰ ἐόντα πρὶν ῞Ελλησι ὡς ἐμοὶ δοϰέειν, πρῶτα μὲν τοῖσι ϰαὶ ἅπαντες χρέωνται " +
                "Φοίνιϰες· μετὰ δὲ χρόνου προβαίνοντος ἅμα τῇ ϕωνῇ μετέβαλον ϰαὶ τὸν ϱυϑμὸν τῶν γραμμάτων. " +
                "Περιοίϰεον δέ σϕεας τὰ πολλὰ τῶν χώρων τοῦτον τὸν χρόνον ῾Ελλήνων ῎Ιωνες· οἳ παραλαβόντες " +
                "διδαχῇ παρὰ τῶν Φοινίϰων τὰ γράμματα, μεταρρυϑμίσαντές σϕεων ὀλίγα ἐχρέωντο, χρεώμενοι δὲ " +
                "ἐϕάτισαν, ὥσπερ ϰαὶ τὸ δίϰαιον ἔϕερε ἐσαγαγόντων Φοινίϰων ἐς τὴν ῾Ελλάδα, ϕοινιϰήια " +
                "ϰεϰλῆσϑαι.";
        Document document = PdfTestBase.createPdf("target/greek.pdf");
        document.open();
        document.add(new Paragraph(greekText, StandardFonts.LIBERATION_SANS.create()));
        document.close();
    }

    @Test
    void testCyrillic() throws IOException {
        String cyrillicText = "Some cyrillic text: " +
                "Статья 1 Все люди рождаются свободными и равными в своем достоинстве и правах. Они " +
                "наделены разумом и совестью и должны поступать в отношении друг друга в духе братства.";
        Document document = PdfTestBase.createPdf("target/cyrillic.pdf");
        document.open();
        document.add(new Paragraph(cyrillicText, StandardFonts.LIBERATION_SANS.create()));
        document.close();
    }
}
