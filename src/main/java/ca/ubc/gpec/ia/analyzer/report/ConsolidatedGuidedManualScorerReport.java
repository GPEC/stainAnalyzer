/*
 * responsibile for generating the consolidated report
 */
package ca.ubc.gpec.ia.analyzer.report;

import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.model.Annotations;
import ca.ubc.gpec.ia.analyzer.report.GuidedManualScorerReport.RescoreStatus;
import static ca.ubc.gpec.ia.analyzer.report.GuidedManualScorerReport.getTrialCentreContactInfo;
import static ca.ubc.gpec.ia.analyzer.report.GuidedManualScorerReport.getTrialCentreFaxInfo;
import static ca.ubc.gpec.ia.analyzer.report.GuidedManualScorerReport.getTrialCentreUseOnlyBox;
import ca.ubc.gpec.ia.analyzer.report.exception.CannotFindInitialScoreException;
import ca.ubc.gpec.ia.analyzer.stats.BootstrapCI;
import ca.ubc.gpec.ia.analyzer.views.ViewConstants;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseField;
import com.itextpdf.text.pdf.PdfFormField;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.RadioCheckField;
import com.itextpdf.text.pdf.draw.LineSeparator;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

/**
 *
 * @author samuelc
 */
public class ConsolidatedGuidedManualScorerReport {

    private final HashMap<File, Annotations> annotationsTable;
    private Annotations initialAnnotations;
    private PdfWriter writer;
    private Document document;
    private GuidedManualScorerReport.TrialCentre trialCentre;
    private int numExtraLines = 0; // number of extra lines to move the checkboxes down

    /**
     * constructor
     *
     * @param guidedManualScorerStage
     * @param annotationsTable
     */
    public ConsolidatedGuidedManualScorerReport(HashMap<File, Annotations> annotationsTable) {
        this.annotationsTable = annotationsTable;
        this.trialCentre = ViewConstants.TRIAL_CENTRE;

        // find the initial annotations
        initialAnnotations = null;
        for (Annotations a : annotationsTable.values()) {
            if (a.getRescore() == RescoreStatus.INITIAL) {
                initialAnnotations = a;
            }
        }
        if (initialAnnotations == null) {
            // cannot find initial score
            throw new CannotFindInitialScoreException("expected but can not find any annotation with initial score");
        }
    }

    public void report(String outputFilename) throws FileNotFoundException, IOException, DocumentException {
        try (OutputStream file = new FileOutputStream(new File(outputFilename));) {
            document = new Document();
            writer = PdfWriter.getInstance(document, file);
            writer.setCompressionLevel(ViewConstants.REPORT_COMPRESSION_LEVEL);
            writer.setPageEvent(new ConsolidatedGuidedManualScorerReportHeaderAndFooter(annotationsTable));

            document.open();

            // document info
            document.addCreationDate();
            document.addAuthor("Genetic Pathology Evaluation Centre (GPEC) / BC Cancer Agency");
            String title = "LUMINA Ki67 results form (consolidated)";
            document.addTitle(title);
            document.addSubject(title);

            // first paragraph
            Paragraph firstParagraph = generateFirstParagraph();
            firstParagraph.setIndentationLeft(ViewConstants.DEFAULT_INDENT);
            document.add(firstParagraph);

            // second Paragraph
            Paragraph secondParagraph = generateSecondParagraph();
            secondParagraph.setIndentationLeft(ViewConstants.DEFAULT_INDENT);
            document.add(secondParagraph);

            // BCCA use only box ...
            Image bccaUseOnly = getTrialCentreUseOnlyBox(trialCentre);
            bccaUseOnly.scaleAbsolute(bccaUseOnly.getWidth() / 2.4f, bccaUseOnly.getHeight() / 2.4f);
            bccaUseOnly.setAlignment(Paragraph.ALIGN_CENTER);
            document.add(bccaUseOnly);

            document.close();
        }
    }

    /**
     * generate first paragraph - Title - LUMINA ID - date specimen received -
     * date Ki67 result
     *
     * @return
     */
    private Paragraph generateFirstParagraph() {
        Paragraph firstParagraph = new Paragraph();

        // title
        Paragraph title = new Paragraph("LUMINA", FontFactory.getFont(ViewConstants.DEFAULT_FONT_NAME, ViewConstants.TITLE_FONT_SIZE, Font.NORMAL, BaseColor.CYAN));
        title.setAlignment(Paragraph.ALIGN_CENTER);
        title.setSpacingBefore(ViewConstants.DEFAULT_PARAGRAPH_SPACING);
        title.setSpacingAfter(ViewConstants.DEFAULT_PARAGRAPH_SPACING);

        Paragraph title2 = new Paragraph("Ki67 Result FAX Form", FontFactory.getFont(ViewConstants.DEFAULT_FONT_NAME, ViewConstants.DEFAULT_FONT_SIZE, Font.BOLD));
        title2.setAlignment(Paragraph.ALIGN_CENTER);
        title2.setSpacingAfter(ViewConstants.DEFAULT_PARAGRAPH_SPACING);

        String slideId = initialAnnotations.getSlideId();

        Paragraph comment = null;
        String faxInfo = getTrialCentreFaxInfo(trialCentre, slideId);
        //if (trialCentre == GuidedManualScorerReport.TrialCentre.BCCA && slideId.startsWith("2007")) {numExtraLines++;}
        comment = new Paragraph(faxInfo, FontFactory.getFont(ViewConstants.DEFAULT_FONT_NAME, ViewConstants.DEFAULT_FONT_SIZE, Font.BOLD));
        comment.setAlignment(Paragraph.ALIGN_CENTER);

        // LUMINA subject ID
        Paragraph subjectId = new Paragraph(
                "LUMINA Subject Identification Number: ",
                FontFactory.getFont(ViewConstants.DEFAULT_FONT_NAME, ViewConstants.DEFAULT_FONT_SIZE));
        subjectId.setAlignment(Paragraph.ALIGN_LEFT);
        subjectId.setSpacingBefore(ViewConstants.DEFAULT_PARAGRAPH_SPACING);
        subjectId.add(new Phrase(slideId, FontFactory.getFont(ViewConstants.DEFAULT_FONT_NAME, ViewConstants.DEFAULT_FONT_SIZE, Font.UNDERLINE)));

        // Date specimen received
        Paragraph dateSpecimenReceived = new Paragraph(
                "Date Specimen Received: " + ViewConstants.formatDate(initialAnnotations.getSpecimenReceivedDate()),
                FontFactory.getFont(ViewConstants.DEFAULT_FONT_NAME, ViewConstants.DEFAULT_FONT_SIZE));
        dateSpecimenReceived.setAlignment(Paragraph.ALIGN_LEFT);
        dateSpecimenReceived.setSpacingBefore(ViewConstants.DEFAULT_PARAGRAPH_SPACING);

        // Date of Ki67 Result - this would be date of the LAST nuclei selection (or removal of selection, whichever is later)
        Paragraph dateKi67Result = new Paragraph(
                "Date of Ki67 Result: " + ViewConstants.formatDate(initialAnnotations.getMaxScoredDate()),
                FontFactory.getFont(ViewConstants.DEFAULT_FONT_NAME, ViewConstants.DEFAULT_FONT_SIZE));
        dateKi67Result.setAlignment(Paragraph.ALIGN_LEFT);
        dateKi67Result.setSpacingBefore(ViewConstants.DEFAULT_PARAGRAPH_SPACING);

        // line separator
        Paragraph lineSeparator = new Paragraph();
        lineSeparator.add(new LineSeparator());
        lineSeparator.setSpacingAfter(ViewConstants.DEFAULT_PARAGRAPH_SPACING);

        firstParagraph.add(title);
        firstParagraph.add(title2);
        firstParagraph.add(comment);
        firstParagraph.add(subjectId);
        firstParagraph.add(dateSpecimenReceived);
        firstParagraph.add(dateKi67Result);
        firstParagraph.add(lineSeparator);
        return firstParagraph;
    }

    /**
     * second paragraph (Ki67) - score results from rescore(s) - score results
     * from initial score - pathologist signature line - trial centre contact
     * info - footnote (for explaining bootstrap)
     *
     *
     * @return
     */
    private Paragraph generateSecondParagraph() throws BadElementException, IOException, DocumentException {
        Paragraph secondParagraph = new Paragraph();

        // title
        Paragraph title = new Paragraph(
                "Please record actual value:",
                FontFactory.getFont(FontFactory.TIMES_BOLDITALIC, ViewConstants.DEFAULT_FONT_SIZE));
        title.setSpacingAfter(ViewConstants.DEFAULT_PARAGRAPH_SPACING);

        // Ki67 Result
        Paragraph ki67Result = new Paragraph(
                "Ki67 Initial Result: ",
                FontFactory.getFont(ViewConstants.DEFAULT_FONT_NAME, ViewConstants.DEFAULT_FONT_SIZE, Font.BOLD));
        DecimalFormat df = new DecimalFormat(ViewConstants.DEFAULT_PERCENT_FORMAT);
        BootstrapCI scoreWithCI = initialAnnotations.getRandomVirtualTmaCoresAnnotation().getOverallPercentPositiveNucleiSelected();

        ki67Result.add(
                new Phrase(
                        df.format(scoreWithCI.getObservedValue()),
                        FontFactory.getFont(ViewConstants.DEFAULT_FONT_NAME, ViewConstants.DEFAULT_FONT_SIZE, Font.UNDERLINE)));
        ki67Result.add(
                new Phrase(
                        "  (" + (Math.round(100d * (double) BootstrapCI.DEFAULT_UPPER_CI_LEVEL - 100d * (double) BootstrapCI.DEFAULT_LOWER_CI_LEVEL))
                        + "% confidence interval*: " + df.format(scoreWithCI.getLowerCI())
                        + "-" + df.format(scoreWithCI.getUpperCI()) + ")",
                        FontFactory.getFont(ViewConstants.DEFAULT_FONT_NAME, ViewConstants.DEFAULT_FONT_SIZE - 2, Font.ITALIC)));
        ki67Result.setAlignment(Paragraph.ALIGN_CENTER);

        ArrayList<Paragraph> rescoreKi67ResultList = new ArrayList();

        // add rescore results
        for (Annotations a : annotationsTable.values()) {
            if (a.getRescore() != RescoreStatus.INITIAL) {
                // only do for RESCORE
                Paragraph ki67RescoreResult = new Paragraph(
                        "Ki67 Rescore Result (" + a.getTrialCentre() + "): ",
                        FontFactory.getFont(ViewConstants.DEFAULT_FONT_NAME, ViewConstants.DEFAULT_FONT_SIZE, Font.BOLD));
                scoreWithCI = a.getRandomVirtualTmaCoresAnnotation().getOverallPercentPositiveNucleiSelected();

                ki67RescoreResult.add(
                        new Phrase(
                                df.format(scoreWithCI.getObservedValue()),
                                FontFactory.getFont(ViewConstants.DEFAULT_FONT_NAME, ViewConstants.DEFAULT_FONT_SIZE, Font.UNDERLINE)));
                ki67RescoreResult.add(
                        new Phrase(
                                "  (" + (Math.round(100d * (double) BootstrapCI.DEFAULT_UPPER_CI_LEVEL - 100d * (double) BootstrapCI.DEFAULT_LOWER_CI_LEVEL))
                                + "% confidence interval*: " + df.format(scoreWithCI.getLowerCI())
                                + "-" + df.format(scoreWithCI.getUpperCI()) + ")",
                                FontFactory.getFont(ViewConstants.DEFAULT_FONT_NAME, ViewConstants.DEFAULT_FONT_SIZE - 2, Font.ITALIC)));
                ki67RescoreResult.setAlignment(Paragraph.ALIGN_CENTER);
                rescoreKi67ResultList.add(ki67RescoreResult);
            }
        }

        // amount of y adjustment for checkboxes based on # of rescore
        int dBtY = (annotationsTable.values().size() - 1) * Math.round(1.5f * ViewConstants.DEFAULT_FONT_SIZE);
        // checkbox for patient eligible and patient ineligible
        // HARD CODE x/y coordinates of check box!!!
        int checkBoxHeight = 10;
        int btX = 265;
        //int btY = recommandExternalRescore ? 438 /*528*/ : 581;//458;//434;
        int btY = 527 - dBtY;
        btY = btY - Math.round(1.5f * numExtraLines * ViewConstants.DEFAULT_FONT_SIZE);
        int bt2X = btX;

        int bt2Y = btY - 18;
        RadioCheckField bt = GuidedManualScorerReport.generateRadioButton(writer, new Rectangle(btX, btY, btX + checkBoxHeight, btY + checkBoxHeight), "eligible", "eligible", false);
        RadioCheckField bt2 = GuidedManualScorerReport.generateRadioButton(writer, new Rectangle(bt2X, bt2Y, bt2X + checkBoxHeight, bt2Y + checkBoxHeight), "ineligible", "ineligible", false);
        PdfFormField f1 = bt.getRadioField();
        PdfFormField top = bt.getRadioGroup(false, false);
        PdfFormField f2 = bt2.getRadioField();
        bt.setBorderWidth(BaseField.BORDER_WIDTH_THICK);
        bt2.setBorderWidth(BaseField.BORDER_WIDTH_THICK);
        top.addKid(f1);
        top.addKid(f2);
        writer.addAnnotation(top);

        int indent = 200;

        // checkbox for patient eligible
        Paragraph patientEligible = new Paragraph();
        patientEligible.add(new Phrase("Patient Eligible", FontFactory.getFont(ViewConstants.DEFAULT_FONT_NAME, ViewConstants.DEFAULT_FONT_SIZE, Font.BOLD)));
        patientEligible.setAlignment(Paragraph.ALIGN_LEFT);
        patientEligible.setIndentationLeft(indent);

        // checkbox for patient ineligible
        Paragraph patientIneligible = new Paragraph("Patient Ineligible", FontFactory.getFont(ViewConstants.DEFAULT_FONT_NAME, ViewConstants.DEFAULT_FONT_SIZE, Font.BOLD));
        patientIneligible.setAlignment(Paragraph.ALIGN_LEFT);
        patientIneligible.setIndentationLeft(indent);

        // pathologist signature field
        String signatureLine = "________________________________________________";
        String pathologistName = initialAnnotations.getPathologistName();
        if (pathologistName != null) {
            signatureLine = signatureLine.substring(pathologistName.length() + 2) + "(" + pathologistName + ")";
        }
        Paragraph pathologistSignature = new Paragraph(
                "Pathologist Signature " + signatureLine + " " + ViewConstants.formatDate(Calendar.getInstance().getTime()),
                FontFactory.getFont(ViewConstants.DEFAULT_FONT_NAME, ViewConstants.DEFAULT_FONT_SIZE, Font.BOLD));
        pathologistSignature.setAlignment(Paragraph.ALIGN_CENTER);
        pathologistSignature.setSpacingBefore(3 * ViewConstants.DEFAULT_PARAGRAPH_SPACING);
        pathologistSignature.setSpacingAfter(2 * ViewConstants.DEFAULT_PARAGRAPH_SPACING);

        // comment field
        Paragraph comment = new Paragraph(
                getTrialCentreContactInfo(trialCentre),
                FontFactory.getFont(ViewConstants.DEFAULT_FONT_NAME, ViewConstants.DEFAULT_FONT_SIZE - 2, Font.BOLD));

        comment.setAlignment(Paragraph.ALIGN_CENTER);
        comment.setSpacingAfter(ViewConstants.DEFAULT_PARAGRAPH_SPACING * 0.5f);

        Paragraph footnote = new Paragraph(
                "* " + (Math.round(100d * (double) BootstrapCI.DEFAULT_UPPER_CI_LEVEL - 100d * (double) BootstrapCI.DEFAULT_LOWER_CI_LEVEL))
                + "% confidence internal based on " + BootstrapCI.DEFAULT_NUMBER_OF_BOOTSTRAP_ITERATIONS + " bootstrap samples.",
                FontFactory.getFont(ViewConstants.DEFAULT_FONT_NAME, ViewConstants.DEFAULT_FONT_SIZE - 2, Font.ITALIC));
        footnote.setAlignment(Paragraph.ALIGN_CENTER);
        footnote.setSpacingAfter(ViewConstants.DEFAULT_PARAGRAPH_SPACING * 0.5f);

        secondParagraph.add(title);
        secondParagraph.add(ki67Result);
        for (Paragraph ki67RescoreResult : rescoreKi67ResultList) {
            secondParagraph.add(ki67RescoreResult);
        }
        patientEligible.setSpacingBefore(2 * ViewConstants.DEFAULT_PARAGRAPH_SPACING);

        secondParagraph.add(patientEligible);
        secondParagraph.add(patientIneligible);

        secondParagraph.add(pathologistSignature);
        secondParagraph.add(comment);
        secondParagraph.add(footnote);

        return secondParagraph;
    }

}
