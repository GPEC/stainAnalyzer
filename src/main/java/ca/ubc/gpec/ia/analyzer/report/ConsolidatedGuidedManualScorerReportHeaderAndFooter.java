/*
 * generate and add header/footer to consolidated report
 */
package ca.ubc.gpec.ia.analyzer.report;

import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.model.Annotations;
import ca.ubc.gpec.ia.analyzer.report.GuidedManualScorerReport.RescoreStatus;
import static ca.ubc.gpec.ia.analyzer.report.GuidedManualScorerReportHeaderAndFooter.FOOTER_FONT_SMALL_ITALIC;
import ca.ubc.gpec.ia.analyzer.views.ViewConstants;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

/**
 *
 * @author samuelc
 */
public class ConsolidatedGuidedManualScorerReportHeaderAndFooter extends PdfPageEventHelper {

    private final HashMap<File, Annotations> annotationsTable;

    /**
     * constructor
     *
     * @param annotationsTable
     */
    public ConsolidatedGuidedManualScorerReportHeaderAndFooter(HashMap<File, Annotations> annotationsTable) {
        this.annotationsTable = annotationsTable;
    }

    /**
     * footer
     *
     * @param writer
     * @param document
     */
    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        try {
            PdfContentByte cb = writer.getDirectContent();

            /*
             * Header
             */
            //ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, new Phrase(headerContent,headerFont), 
            //        document.leftMargin() - 1, document.top() + 30, 0);
            Image bccaLogo = Image.getInstance(ViewConstants.getTrialCentreLogo(), null);
            bccaLogo.scaleAbsolute(bccaLogo.getWidth() / 4, bccaLogo.getHeight() / 4);
            bccaLogo.setAbsolutePosition(document.left(), document.top() - 10);
            document.add(bccaLogo);


            /*
             * Foooter
             */
            int heightIncr = 6;
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, new Phrase("report generated on " + (new Date().toString()), GuidedManualScorerReportHeaderAndFooter.FOOTER_FONT_SMALL_ITALIC), document.left(), document.bottom() + heightIncr, 0);
            for (File f : annotationsTable.keySet()) {
                heightIncr = heightIncr - 8;
                String text = "archived annotation file" + (annotationsTable.get(f).getRescore() == RescoreStatus.INITIAL ? "(initial)" : "(rescore)") + ":" + f.getAbsolutePath();
                ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, new Phrase(text, FOOTER_FONT_SMALL_ITALIC), document.left(), document.bottom() + heightIncr, 0);
            }
        } catch (IOException | DocumentException ex) {
            System.err.println(ex);
        }

    }

}
