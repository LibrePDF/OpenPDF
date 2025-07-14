package org.librepdf.openpdf.fonts;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class GreekAndCyrillicTest {

    @Test
    void testGreek() throws IOException {
        String greekText = "Some greek text: " +
                "Οἱ δὲ Φοίνιϰες οὗτοι οἱ σὺν Κάδμῳ ἀπιϰόμενοι.. ἐσήγαγον διδασϰάλια ἐς τοὺς ῞Ελληνας ϰαὶ δὴ ϰαὶ "
                +
                "γράμματα, οὐϰ ἐόντα πρὶν ῞Ελλησι ὡς ἐμοὶ δοϰέειν, πρῶτα μὲν τοῖσι ϰαὶ ἅπαντες χρέωνται "
                +
                "Φοίνιϰες· μετὰ δὲ χρόνου προβαίνοντος ἅμα τῇ ϕωνῇ μετέβαλον ϰαὶ τὸν ϱυϑμὸν τῶν γραμμάτων. "
                +
                "Περιοίϰεον δέ σϕεας τὰ πολλὰ τῶν χώρων τοῦτον τὸν χρόνον ῾Ελλήνων ῎Ιωνες· οἳ παραλαβόντες "
                +
                "διδαχῇ παρὰ τῶν Φοινίϰων τὰ γράμματα, μεταρρυϑμίσαντές σϕεων ὀλίγα ἐχρέωντο, χρεώμενοι δὲ "
                +
                "ἐϕάτισαν, ὥσπερ ϰαὶ τὸ δίϰαιον ἔϕερε ἐσαγαγόντων Φοινίϰων ἐς τὴν ῾Ελλάδα, ϕοινιϰήια "
                +
                "ϰεϰλῆσϑαι.";
        Document document = FontsTestUtil.createPdf("target/greek.pdf");
        document.open();
        document.add(new Paragraph(greekText, Liberation.SANS_BOLD.create()));
        document.close();
    }

    @Test
    void testCyrillic() throws IOException {
        String cyrillicText = "Some cyrillic text: " +
                "Статья 1 Все люди рождаются свободными и равными в своем достоинстве и правах. Они "
                +
                "наделены разумом и совестью и должны поступать в отношении друг друга в духе братства.";
        Document document = FontsTestUtil.createPdf("target/cyrillic.pdf");
        document.open();
        document.add(new Paragraph(cyrillicText, Liberation.SANS_ITALIC.create()));
        document.close();
    }

}
