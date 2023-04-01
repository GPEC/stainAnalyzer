/*
 * generate and add header/footer to report
 */
package ca.ubc.gpec.ia.analyzer.report;

import ca.ubc.gpec.ia.analyzer.views.ViewConstants;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import java.io.IOException;
import java.util.Date;

/**
 *
 * @author samuelc
 */
public class GuidedManualScorerReportHeaderAndFooter extends PdfPageEventHelper {

    private final String archiveAnnotationFilename;
    private final String virtualSlideFilename;
    private final String virutalSlideMd5Sum;

    /*
     * Font for header and footer part.
     */
    public static final Font HEADER_FONT = new Font(Font.FontFamily.COURIER, 9f, Font.NORMAL);
    public static final Font FOOTER_FONT = new Font(Font.FontFamily.COURIER, 9f, Font.NORMAL);
    public static final Font FOOTER_FONT_SMALL_ITALIC = new Font(Font.FontFamily.COURIER, 5f, Font.ITALIC);

    /**
     * constructor
     *
     * @param archiveAnnotationFilename
     * @param virtualSlideFilename
     * @param virutalSlideMd5Sum
     */
    public GuidedManualScorerReportHeaderAndFooter(String archiveAnnotationFilename, String virtualSlideFilename, String virutalSlideMd5Sum) {
        super();
        this.archiveAnnotationFilename = archiveAnnotationFilename;
        this.virtualSlideFilename = virtualSlideFilename;
        this.virutalSlideMd5Sum = virutalSlideMd5Sum;
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
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, new Phrase("report generated on " + (new Date().toString()), FOOTER_FONT_SMALL_ITALIC), document.left(), document.bottom() + 6, 0);
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, new Phrase("archived annotation file: " + archiveAnnotationFilename, FOOTER_FONT_SMALL_ITALIC), document.left(), document.bottom() - 2, 0);
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, new Phrase("virtual slide file: " + virtualSlideFilename, FOOTER_FONT_SMALL_ITALIC), document.left(), document.bottom() - 10, 0);
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT, new Phrase("virtual slide file MD5 sum: " + virutalSlideMd5Sum, FOOTER_FONT_SMALL_ITALIC), document.left(), document.bottom() - 18, 0);
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, new Phrase(String.format(" %d ", writer.getPageNumber()), FOOTER_FONT), document.right() - 2, document.bottom() - 20, 0);
        } catch (IOException | DocumentException ex) {
            System.err.println(ex);
        }

    }
}
