/*
 * $Id: ColumnIrregular.java 3373 2008-05-12 16:21:24Z xlv $
 *
 * This code is part of the 'iText Tutorial'.
 * You can find the complete tutorial at the following address:
 * http://itextdocs.lowagie.com/tutorial/
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * itext-questions@lists.sourceforge.net
 */
package com.lowagie.examples.objects.columns;

import java.io.FileOutputStream;
import java.io.IOException;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Image;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;

/**
 * Writes text in irregular columns that can be added at an absolute position.
 */
public class ColumnIrregular {
    
    /**
     * Demonstrates the use of ColumnText.
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        
        System.out.println("Irregular Columns");
        
        // step 1: creation of a document-object
        Document document = new Document();
        
        try {
            
            // step 2: creation of the writer
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream("columnirregular.pdf"));
            
            // step 3: we open the document
            document.open();
            
            // step 4:
            // we grab the contentbyte and do some stuff with it
            PdfContentByte cb = writer.getDirectContent();
            
            PdfTemplate t = cb.createTemplate(600, 800);
            Image caesar = Image.getInstance("caesar_coin.jpg");
            cb.addImage(caesar, 100, 0, 0, 100, 260, 595);
            t.setGrayFill(0.75f);
            t.moveTo(310, 112);
            t.lineTo(280, 60);
            t.lineTo(340, 60);
            t.closePath();
            t.moveTo(310, 790);
            t.lineTo(310, 710);
            t.moveTo(310, 580);
            t.lineTo(310, 122);
            t.stroke();
            cb.addTemplate(t, 0, 0);
            
            ColumnText ct = new ColumnText(cb);
            ct.addText(new Phrase("GALLIA est omnis divisa in partes tres, quarum unam incolunt Belgae, aliam Aquitani, tertiam qui ipsorum lingua Celtae, nostra Galli appellantur.  Hi omnes lingua, institutis, legibus inter se differunt. Gallos ab Aquitanis Garumna flumen, a Belgis Matrona et Sequana dividit. Horum omnium fortissimi sunt Belgae, propterea quod a cultu atque humanitate provinciae longissime absunt, minimeque ad eos mercatores saepe commeant atque ea quae ad effeminandos animos pertinent important, proximique sunt Germanis, qui trans Rhenum incolunt, quibuscum continenter bellum gerunt.  Qua de causa Helvetii quoque reliquos Gallos virtute praecedunt, quod fere cotidianis proeliis cum Germanis contendunt, cum aut suis finibus eos prohibent aut ipsi in eorum finibus bellum gerunt.\n", FontFactory.getFont(FontFactory.HELVETICA, 12)));
            ct.addText(new Phrase("[Eorum una, pars, quam Gallos obtinere dictum est, initium capit a flumine Rhodano, continetur Garumna flumine, Oceano, finibus Belgarum, attingit etiam ab Sequanis et Helvetiis flumen Rhenum, vergit ad septentriones. Belgae ab extremis Galliae finibus oriuntur, pertinent ad inferiorem partem fluminis Rheni, spectant in septentrionem et orientem solem. Aquitania a Garumna flumine ad Pyrenaeos montes et eam partem Oceani quae est ad Hispaniam pertinet; spectat inter occasum solis et septentriones.]\n", FontFactory.getFont(FontFactory.HELVETICA, 12)));
            ct.addText(new Phrase("Apud Helvetios longe nobilissimus fuit et ditissimus Orgetorix.  Is M. Messala, [et P.] M.  Pisone consulibus regni cupiditate inductus coniurationem nobilitatis fecit et civitati persuasit ut de finibus suis cum omnibus copiis exirent:  perfacile esse, cum virtute omnibus praestarent, totius Galliae imperio potiri.  Id hoc facilius iis persuasit, quod undique loci natura Helvetii continentur:  una ex parte flumine Rheno latissimo atque altissimo, qui agrum Helvetium a Germanis dividit; altera ex parte monte Iura altissimo, qui est inter Sequanos et Helvetios; tertia lacu Lemanno et flumine Rhodano, qui provinciam nostram ab Helvetiis dividit.  His rebus fiebat ut et minus late vagarentur et minus facile finitimis bellum inferre possent; qua ex parte homines bellandi cupidi magno dolore adficiebantur.  Pro multitudine autem hominum et pro gloria belli atque fortitudinis angustos se fines habere arbitrabantur, qui in longitudinem milia passuum CCXL, in latitudinem CLXXX patebant.\n", FontFactory.getFont(FontFactory.HELVETICA, 12)));
            ct.addText(new Phrase("His rebus adducti et auctoritate Orgetorigis permoti constituerunt ea quae ad proficiscendum pertinerent comparare, iumentorum et carrorum quam maximum numerum coemere, sementes quam maximas facere, ut in itinere copia frumenti suppeteret, cum proximis civitatibus pacem et amicitiam confirmare.  Ad eas res conficiendas biennium sibi satis esse duxerunt; in tertium annum profectionem lege confirmant.  Ad eas res conficiendas Orgetorix deligitur.  Is sibi legationem ad civitates suscipit.  In eo itinere persuadet Castico, Catamantaloedis filio, Sequano, cuius pater regnum in Sequanis multos annos obtinuerat et a senatu populi Romani amicus appellatus erat, ut regnum in civitate sua occuparet, quod pater ante habuerit; itemque Dumnorigi Haeduo, fratri Diviciaci, qui eo tempore principatum in civitate obtinebat ac maxime plebi acceptus erat, ut idem conaretur persuadet eique filiam suam in matrimonium dat.  Perfacile factu esse illis probat conata perficere, propterea quod ipse suae civitatis imperium obtenturus esset:  non esse dubium quin totius Galliae plurimum Helvetii possent; se suis copiis suoque exercitu illis regna conciliaturum confirmat.  Hac oratione adducti inter se fidem et ius iurandum dant et regno occupato per tres potentissimos ac firmissimos populos totius Galliae sese potiri posse sperant.\n", FontFactory.getFont(FontFactory.HELVETICA, 12)));
            ct.addText(new Phrase("Ea res est Helvetiis per indicium enuntiata.  Moribus suis Orgetoricem ex vinculis causam dicere coegerunt; damnatum poenam sequi oportebat, ut igni cremaretur.  Die constituta causae dictionis Orgetorix ad iudicium omnem suam familiam, ad hominum milia decem, undique coegit, et omnes clientes obaeratosque suos, quorum magnum numerum habebat, eodem conduxit; per eos ne causam diceret se eripuit.  Cum civitas ob eam rem incitata armis ius suum exequi conaretur multitudinemque hominum ex agris magistratus cogerent, Orgetorix mortuus est; neque abest suspicio, ut Helvetii arbitrantur, quin ipse sibi mortem consciverit.", FontFactory.getFont(FontFactory.HELVETICA, 12)));
            
            float[] left1  = {70,790, 70,60};
            float[] right1 = {300,790, 300,700, 240,700, 240,590, 300,590, 300,106, 270,60};
            float[] left2  = {320,790, 320,700, 380,700, 380,590, 320,590, 320,106, 350,60};
            float[] right2 = {550,790, 550,60};
            
            int status = 0;
            int column = 0;
            while ((status & ColumnText.NO_MORE_TEXT) == 0) {
                if (column == 0) {
                    ct.setColumns(left1, right1);
                    column = 1;
                }
                else {
                    ct.setColumns(left2, right2);
                    column = 0;
                }
                status = ct.go();
                ct.setYLine(790);
                ct.setAlignment(Element.ALIGN_JUSTIFIED);
                status = ct.go();
                if ((column == 0) && ((status & ColumnText.NO_MORE_COLUMN) != 0)) {
                    document.newPage();
                    cb.addTemplate(t, 0, 0);
                    cb.addImage(caesar, 100, 0, 0, 100, 260, 595);
                }
            }
        }
        catch(DocumentException de) {
            System.err.println(de.getMessage());
        }
        catch(IOException ioe) {
            System.err.println(ioe.getMessage());
        }
        
        // step 5: we close the document
        document.close();
    }
}
