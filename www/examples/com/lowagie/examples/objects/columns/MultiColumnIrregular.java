/*
 * $Id: MultiColumnIrregular.java 3373 2008-05-12 16:21:24Z xlv $
 * 
 * This code is part of the 'iText Tutorial'.
 * You can find the complete tutorial at the following address:
 * http://itextdocs.lowagie.com/tutorial/
 * 
 * This code is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE.
 * 
 * itext-questions@lists.sourceforge.net
 */
package com.lowagie.examples.objects.columns;

import java.awt.Color;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.MultiColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfWriter;

/**
 * An example using MultiColumnText with irregular columns.
 */
public class MultiColumnIrregular {

    /**
     * An example using MultiColumnText with irregular columns.
     * @param args no arguments needed
     */
    public static void main(String[] args) {
        try {
        	// step 1
            Document document = new Document();
            OutputStream out = new FileOutputStream("multicolumnirregular.pdf");
            PdfWriter writer = PdfWriter.getInstance(document, out);
            document.open();

            // calculate diamond shaped hole
            float diamondHeight = 400;
            float diamondWidth = 400;
            float gutter = 10;
            float bodyHeight = document.top() - document.bottom();
            float colMaxWidth = (document.right() - document.left() - (gutter * 2)) / 2f;
            float diamondTop = document.top() - ((bodyHeight - diamondHeight) / 2f);
            float diamondInset = colMaxWidth - (diamondWidth / 2f);
            float centerX = (document.right() - document.left()) / 2 + document.left();
            // draw stuff
            PdfContentByte cb = writer.getDirectContentUnder();
            
            MultiColumnText mct = new MultiColumnText(document.top() - document.bottom());

            // setup column 1
            float[] left = {document.left(), document.top(), document.left(), document.bottom()};
            float[] right = {document.left() + colMaxWidth, document.top(),
                             document.left() + colMaxWidth, diamondTop,
                             document.left() + diamondInset, diamondTop - diamondHeight / 2,
                             document.left() + colMaxWidth, diamondTop - diamondHeight,
                             document.left() + colMaxWidth, document.bottom()
                            };
            mct.addColumn(left, right);

            // setup column 2
            left = new float[] { document.right() - colMaxWidth, document.top(),
                                  document.right() - colMaxWidth, diamondTop,
                                  document.right() - diamondInset, diamondTop - diamondHeight / 2,
                                  document.right() - colMaxWidth, diamondTop - diamondHeight,
                                  document.right() - colMaxWidth, document.bottom()
                                 };
            right = new float[] { document.right(), document.top(), document.right(), document.bottom() };
            mct.addColumn(left, right);

            // add text
            for (int i=0; i<8; i++) {
                mct.addElement(new Paragraph("GALLIA est omnis divisa in partes tres, quarum unam incolunt Belgae, aliam Aquitani, tertiam qui ipsorum lingua Celtae, nostra Galli appellantur.  Hi omnes lingua, institutis, legibus inter se differunt. Gallos ab Aquitanis Garumna flumen, a Belgis Matrona et Sequana dividit. Horum omnium fortissimi sunt Belgae, propterea quod a cultu atque humanitate provinciae longissime absunt, minimeque ad eos mercatores saepe commeant atque ea quae ad effeminandos animos pertinent important, proximique sunt Germanis, qui trans Rhenum incolunt, quibuscum continenter bellum gerunt.  Qua de causa Helvetii quoque reliquos Gallos virtute praecedunt, quod fere cotidianis proeliis cum Germanis contendunt, cum aut suis finibus eos prohibent aut ipsi in eorum finibus bellum gerunt.\n", FontFactory.getFont(FontFactory.HELVETICA, 12)));
                mct.addElement(new Paragraph("[Eorum una, pars, quam Gallos obtinere dictum est, initium capit a flumine Rhodano, continetur Garumna flumine, Oceano, finibus Belgarum, attingit etiam ab Sequanis et Helvetiis flumen Rhenum, vergit ad septentriones. Belgae ab extremis Galliae finibus oriuntur, pertinent ad inferiorem partem fluminis Rheni, spectant in septentrionem et orientem solem. Aquitania a Garumna flumine ad Pyrenaeos montes et eam partem Oceani quae est ad Hispaniam pertinet; spectat inter occasum solis et septentriones.]\n", FontFactory.getFont(FontFactory.HELVETICA, 12)));
                mct.addElement(new Paragraph("Apud Helvetios longe nobilissimus fuit et ditissimus Orgetorix.  Is M. Messala, [et P.] M.  Pisone consulibus regni cupiditate inductus coniurationem nobilitatis fecit et civitati persuasit ut de finibus suis cum omnibus copiis exirent:  perfacile esse, cum virtute omnibus praestarent, totius Galliae imperio potiri.  Id hoc facilius iis persuasit, quod undique loci natura Helvetii continentur:  una ex parte flumine Rheno latissimo atque altissimo, qui agrum Helvetium a Germanis dividit; altera ex parte monte Iura altissimo, qui est inter Sequanos et Helvetios; tertia lacu Lemanno et flumine Rhodano, qui provinciam nostram ab Helvetiis dividit.  His rebus fiebat ut et minus late vagarentur et minus facile finitimis bellum inferre possent; qua ex parte homines bellandi cupidi magno dolore adficiebantur.  Pro multitudine autem hominum et pro gloria belli atque fortitudinis angustos se fines habere arbitrabantur, qui in longitudinem milia passuum CCXL, in latitudinem CLXXX patebant.\n", FontFactory.getFont(FontFactory.HELVETICA, 12)));
                mct.addElement(new Paragraph("His rebus adducti et auctoritate Orgetorigis permoti constituerunt ea quae ad proficiscendum pertinerent comparare, iumentorum et carrorum quam maximum numerum coemere, sementes quam maximas facere, ut in itinere copia frumenti suppeteret, cum proximis civitatibus pacem et amicitiam confirmare.  Ad eas res conficiendas biennium sibi satis esse duxerunt; in tertium annum profectionem lege confirmant.  Ad eas res conficiendas Orgetorix deligitur.  Is sibi legationem ad civitates suscipit.  In eo itinere persuadet Castico, Catamantaloedis filio, Sequano, cuius pater regnum in Sequanis multos annos obtinuerat et a senatu populi Romani amicus appellatus erat, ut regnum in civitate sua occuparet, quod pater ante habuerit; itemque Dumnorigi Haeduo, fratri Diviciaci, qui eo tempore principatum in civitate obtinebat ac maxime plebi acceptus erat, ut idem conaretur persuadet eique filiam suam in matrimonium dat.  Perfacile factu esse illis probat conata perficere, propterea quod ipse suae civitatis imperium obtenturus esset:  non esse dubium quin totius Galliae plurimum Helvetii possent; se suis copiis suoque exercitu illis regna conciliaturum confirmat.  Hac oratione adducti inter se fidem et ius iurandum dant et regno occupato per tres potentissimos ac firmissimos populos totius Galliae sese potiri posse sperant.\n", FontFactory.getFont(FontFactory.HELVETICA, 12)));
                mct.addElement(new Paragraph("Ea res est Helvetiis per indicium enuntiata.  Moribus suis Orgetoricem ex vinculis causam dicere coegerunt; damnatum poenam sequi oportebat, ut igni cremaretur.  Die constituta causae dictionis Orgetorix ad iudicium omnem suam familiam, ad hominum milia decem, undique coegit, et omnes clientes obaeratosque suos, quorum magnum numerum habebat, eodem conduxit; per eos ne causam diceret se eripuit.  Cum civitas ob eam rem incitata armis ius suum exequi conaretur multitudinemque hominum ex agris magistratus cogerent, Orgetorix mortuus est; neque abest suspicio, ut Helvetii arbitrantur, quin ipse sibi mortem consciverit.", FontFactory.getFont(FontFactory.HELVETICA, 12)));
            }
           do {
                cb.setLineWidth(5);
                cb.setColorStroke(Color.GRAY);
                cb.moveTo(centerX , document.top());
                cb.lineTo(centerX, document.bottom());
                cb.stroke();
                cb.moveTo(centerX, diamondTop);
                cb.lineTo(centerX - (diamondWidth/2), diamondTop - (diamondHeight / 2));
                cb.lineTo(centerX, diamondTop - diamondHeight);
                cb.lineTo(centerX + (diamondWidth/2), diamondTop - (diamondHeight / 2));
                cb.lineTo(centerX, diamondTop);
                cb.setColorFill(Color.GRAY);
                cb.fill();
            	document.add(mct);
            	mct.nextColumn();
            } while (mct.isOverflow());
            document.close();
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

}