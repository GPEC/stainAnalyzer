/*
 * responsibile for generating the score report
 */
package ca.ubc.gpec.ia.analyzer.report;

import ca.ubc.gpec.ia.analyzer.model.guidedManualScorer.GuidedManualScorer;
import ca.ubc.gpec.ia.analyzer.model.guidedManualScorer.NucleiSelections;
import ca.ubc.gpec.ia.analyzer.reader.VirtualSlideReaderException;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.exception.RegionNotPreprocessedException;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.exception.RegionNotSupportedException;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.model.Annotation;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.model.Region;
import ca.ubc.gpec.ia.analyzer.stats.BootstrapCI;
import ca.ubc.gpec.ia.analyzer.views.ViewConstants;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseField;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfBorderDictionary;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfFormField;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.RadioCheckField;
import com.itextpdf.text.pdf.draw.LineSeparator;
import java.awt.Color;

import java.awt.Toolkit;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Hashtable;
import loci.formats.FormatException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;

/**
 *
 * @author samuelc
 */
public class GuidedManualScorerReport {

    public enum TrialCentre {
        BCCA, OCOG, PMCC
    }

    public enum RescoreStatus {
        RESCORE, INITIAL, NOT_SET
    }

    public enum ScoreType {
        RANDOM, MANUAL
    }
    
    public static final String BOOTSTRAP_SAMPLE_FILENAME_SUFFIX = "_bootstrapSamples";

    private GuidedManualScorer guidedManualScorer; // guided manual scorer object
    private PdfWriter writer;
    private Document document;
    private TrialCentre trialCentre;
    private boolean rescore; // indicate whether this is an external rescore
    private int numExtraLines = 0; // number of extra lines to move the checkboxes down

    /**
     * generate appropriate fax info
     *
     * @param trialCentre
     * @param slideId
     * @return
     */
    public static String getTrialCentreFaxInfo(TrialCentre trialCentre, String slideId) {
        String faxInfo = "(Please fax results to OCOG within 5 working days of receipt, at 905-575-2639)";
        //2015-03-16: per email forward from Angela, all reports are to be fax to OCOG
        return faxInfo;
        /**
        if (trialCentre == TrialCentre.BCCA) {
            // need to figure out the contact name based on slide id ... leave blank if could not figure out
            if (slideId.startsWith("2007")) {
                faxInfo = "(Please fax results to Sunshine Purificacion within 5 working days of receipt,\nat 604-877-0505)";
            } else if (slideId.startsWith("2010")) {
                faxInfo = "(Please fax results to Michael Miller within 5 working days of receipt, at 250-519-2039)";
            } else if (slideId.startsWith("2044")) {
                faxInfo = "(Please fax results to Mark Barnes within 5 working days of receipt, at 250-645-7301)";
            } else {
                faxInfo = "(Please fax results to BCCA within 5 working days of receipt)";
            }
        } else if (trialCentre == TrialCentre.OCOG) {
            faxInfo = "(Please fax results to OCOG within 5 working days of receipt, at 905-575-2639)";
        } else if (trialCentre == TrialCentre.PMCC) {
            faxInfo = "(Please fax results to UHN/PMCC within 5 working days of receipt, at 416-946-2828)";
        }
        return faxInfo;
        */
    }

    /**
     * generate the appropriate trial centre contact info
     *
     * @param trialCentre
     * @return
     */
    public static String getTrialCentreContactInfo(TrialCentre trialCentre) {
        String contactInfo = null;
        if (trialCentre == TrialCentre.BCCA) {
            contactInfo = "Questions OR Concerns: Contact LUMINA Study Coordinator, Michael Miller at 250-519-5741";
        } else if (trialCentre == TrialCentre.OCOG) {
            contactInfo = "Questions OR Concerns: Contact LUMINA Study Coordinator, Lucy Spadafora at 905-527-2299 ext. 42655";
        } else if (trialCentre == TrialCentre.PMCC) {
            contactInfo = "Questions OR Concerns: Contact LUMINA Study Coordinator, Lea Dungao at 416-946-4501 ext. 2589";
        }
        return contactInfo;
    }

    /**
     * generate the box for trial centre use only
     * 
     * @param trialCentre
     * @return
     * @throws BadElementException
     * @throws IOException 
     */
    public static Image getTrialCentreUseOnlyBox(TrialCentre trialCentre) throws BadElementException, IOException {
        Image bccaUseOnly = null;
        if (trialCentre == TrialCentre.BCCA) {
            bccaUseOnly = Image.getInstance(
                    Toolkit.getDefaultToolkit().getImage(ViewConstants.class.getResource(
                            ViewConstants.RESOURCE_SEPARATOR + "images" + ViewConstants.RESOURCE_SEPARATOR + ViewConstants.BCCA_USE_ONLY_FILENAME).getPath()), null);
        } else if (trialCentre == TrialCentre.OCOG) {
            bccaUseOnly = Image.getInstance(
                    Toolkit.getDefaultToolkit().getImage(ViewConstants.class.getResource(
                            ViewConstants.RESOURCE_SEPARATOR + "images" + ViewConstants.RESOURCE_SEPARATOR + ViewConstants.OCOG_USE_ONLY_FILENAME).getPath()), null);
        } else if (trialCentre == TrialCentre.PMCC) {
            bccaUseOnly = Image.getInstance(
                    Toolkit.getDefaultToolkit().getImage(ViewConstants.class.getResource(
                            ViewConstants.RESOURCE_SEPARATOR + "images" + ViewConstants.RESOURCE_SEPARATOR + ViewConstants.PMCC_USE_ONLY_FILENAME).getPath()), null);
        }
        return bccaUseOnly;
    }

    /**
     * constructor
     *
     * @param guidedManualScorer
     */
    public GuidedManualScorerReport(GuidedManualScorer guidedManualScorer) throws VirtualSlideReaderException {
        this.guidedManualScorer = guidedManualScorer;
        this.trialCentre = ViewConstants.TRIAL_CENTRE;
        rescore = guidedManualScorer.isRescore();
    }

    /**
     * generate report
     *
     * @param outputFilename
     * @param archiveAnnotationFilename
     * @throws FileNotFoundException
     * @throws DocumentException
     * @throws IOException
     */
    public void report(String outputFilename, String archiveAnnotationFilename) throws FileNotFoundException, DocumentException, IOException, VirtualSlideReaderException, BadElementException, FormatException, RegionNotPreprocessedException, RegionNotSupportedException {
        try (
                OutputStream file = new FileOutputStream(new File(outputFilename));
                OutputStream bootstrapSamplesfile = new FileOutputStream(new File(archiveAnnotationFilename + BOOTSTRAP_SAMPLE_FILENAME_SUFFIX));) {
            document = new Document();
            writer = PdfWriter.getInstance(document, file);
            writer.setCompressionLevel(ViewConstants.REPORT_COMPRESSION_LEVEL);
            writer.setPageEvent(new GuidedManualScorerReportHeaderAndFooter(
                    archiveAnnotationFilename,
                    guidedManualScorer.getVirtualSlideReader().getVirtualSlideFile().getAbsolutePath(),
                    guidedManualScorer.getVirtualSlideReader().getAnnotations().getMd5Sum()));

            document.open();

            // document info
            document.addCreationDate();
            document.addAuthor("Genetic Pathology Evaluation Centre (GPEC) / BC Cancer Agency");
            String title = "LUMINA Ki67 results form";
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

            addDetailReport(document); // add detail report to the next page

            document.close();

            // output bootstrap samples ...
            guidedManualScorer.getVirtualSlideReader().getAnnotations().getRandomVirtualTmaCoresAnnotation().getOverallPercentPositiveNucleiSelected().exportBootstrapSamples(bootstrapSamplesfile, ViewConstants.TAB);
        }
    }

    /**
     * generate first paragraph - Title - LUMINA ID - date specimen received -
     * date Ki67 result
     *
     * @return
     */
    private Paragraph generateFirstParagraph() throws VirtualSlideReaderException {
        Paragraph firstParagraph = new Paragraph();

        // title
        Paragraph title = new Paragraph("LUMINA", FontFactory.getFont(ViewConstants.DEFAULT_FONT_NAME, ViewConstants.TITLE_FONT_SIZE, Font.NORMAL, BaseColor.CYAN));
        title.setAlignment(Paragraph.ALIGN_CENTER);
        title.setSpacingBefore(ViewConstants.DEFAULT_PARAGRAPH_SPACING);
        title.setSpacingAfter(ViewConstants.DEFAULT_PARAGRAPH_SPACING);

        Paragraph title2 = new Paragraph("Ki67 Result FAX Form", FontFactory.getFont(ViewConstants.DEFAULT_FONT_NAME, ViewConstants.DEFAULT_FONT_SIZE, Font.BOLD));
        title2.setAlignment(Paragraph.ALIGN_CENTER);
        title2.setSpacingAfter(ViewConstants.DEFAULT_PARAGRAPH_SPACING);

        String slideId = guidedManualScorer.getVirtualSlideReader().getAnnotations().getSlideId();

        Paragraph comment = null;
        //Paragraph comment2 = null;
        if (rescore) {
            comment = new Paragraph("(This is a rescore.  Please send/fax results back to the initial scoring institution.)",
                    FontFactory.getFont(ViewConstants.DEFAULT_FONT_NAME, ViewConstants.DEFAULT_FONT_SIZE, Font.BOLD));
        } else {
            // need to figure out the contact name based on slide id ... leave blank if could not figure out
            String faxInfo = getTrialCentreFaxInfo(trialCentre, slideId);
            //if (trialCentre == TrialCentre.BCCA && slideId.startsWith("2007")) {numExtraLines++;}
            //if (trialCentre != TrialCentre.OCOG) {numExtraLines++;}
            comment = new Paragraph(faxInfo, FontFactory.getFont(ViewConstants.DEFAULT_FONT_NAME, ViewConstants.DEFAULT_FONT_SIZE, Font.BOLD));
            // 2015-01-15: Christine ask to fax to OCOG as well for non-OCOG site
            //String faxInfo2 = "(Please fax results to OCOG Department of Oncology at 905-575-2639)";
            //comment2 = new Paragraph(faxInfo2, FontFactory.getFont(ViewConstants.DEFAULT_FONT_NAME, ViewConstants.DEFAULT_FONT_SIZE, Font.BOLDITALIC));
            
        }
        comment.setAlignment(Paragraph.ALIGN_CENTER);
        //if (comment2 != null) {comment2.setAlignment(Paragraph.ALIGN_CENTER);}
        
        // LUMINA subject ID
        Paragraph subjectId = new Paragraph(
                "LUMINA Subject Identification Number: ",
                FontFactory.getFont(ViewConstants.DEFAULT_FONT_NAME, ViewConstants.DEFAULT_FONT_SIZE));
        subjectId.setAlignment(Paragraph.ALIGN_LEFT);
        subjectId.setSpacingBefore(ViewConstants.DEFAULT_PARAGRAPH_SPACING);
        subjectId.add(new Phrase(slideId, FontFactory.getFont(ViewConstants.DEFAULT_FONT_NAME, ViewConstants.DEFAULT_FONT_SIZE, Font.UNDERLINE)));

        // Date specimen received
        Paragraph dateSpecimenReceived = new Paragraph(
                "Date Specimen Received: " + (rescore ? "N/A" : ViewConstants.formatDate(guidedManualScorer.getVirtualSlideReader().getAnnotations().getSpecimenReceivedDate())),
                FontFactory.getFont(ViewConstants.DEFAULT_FONT_NAME, ViewConstants.DEFAULT_FONT_SIZE));
        dateSpecimenReceived.setAlignment(Paragraph.ALIGN_LEFT);
        dateSpecimenReceived.setSpacingBefore(ViewConstants.DEFAULT_PARAGRAPH_SPACING);

        // Date of Ki67 Result - this would be date of the LAST nuclei selection (or removal of selection, whichever is later)
        Paragraph dateKi67Result = new Paragraph(
                "Date of Ki67 Result: " + ViewConstants.formatDate(guidedManualScorer.getVirtualSlideReader().getAnnotations().getRandomVirtualTmaCoresAnnotation().getLastNucleiSelectionDate()),
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
        //if (comment2 != null) {
        //    firstParagraph.add(comment2);
        //}
        firstParagraph.add(subjectId);
        firstParagraph.add(dateSpecimenReceived);
        firstParagraph.add(dateKi67Result);
        firstParagraph.add(lineSeparator);
        return firstParagraph;
    }

    /**
     * second paragraph (Ki67)
     *
     * @return
     */
    private Paragraph generateSecondParagraph() throws VirtualSlideReaderException, BadElementException, IOException, DocumentException {
        Paragraph secondParagraph = new Paragraph();

        // title
        Paragraph title = new Paragraph(
                "Please record actual value:",
                FontFactory.getFont(FontFactory.TIMES_BOLDITALIC, ViewConstants.DEFAULT_FONT_SIZE));
        title.setSpacingAfter(ViewConstants.DEFAULT_PARAGRAPH_SPACING);

        // Ki67 Result
        Paragraph ki67Result = new Paragraph(
                "Ki67 Result: ",
                FontFactory.getFont(ViewConstants.DEFAULT_FONT_NAME, ViewConstants.DEFAULT_FONT_SIZE, Font.BOLD));
        DecimalFormat df = new DecimalFormat(ViewConstants.DEFAULT_PERCENT_FORMAT);
        BootstrapCI scoreWithCI = guidedManualScorer.getVirtualSlideReader().getAnnotations().getRandomVirtualTmaCoresAnnotation().getOverallPercentPositiveNucleiSelected();
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
        ki67Result.setSpacingAfter(2 * ViewConstants.DEFAULT_PARAGRAPH_SPACING);
        ki67Result.setAlignment(Paragraph.ALIGN_CENTER);

        // check to see if rescore is recommended
        // 2014-06-25: DO NOT PRINT ANY comment for guidedManualScorer.shouldConsiderExternalRescore() ... confusing to user
        // 2014-06-27: no warning message if rescore
        boolean recommandExternalRescore = guidedManualScorer.recommandExternalRescore();

        if (!rescore) { // no need for eligible/ineligible checkboxes if rescore
            // checkbox for patient eligible and patient ineligible
            // HARD CODE x/y coordinates of check box!!!
            int checkBoxHeight = 10;
            int btX = 265;
            //int btY = recommandExternalRescore ? 438 /*528*/ : 581;//458;//434;
            int btY = 527;
            btY = btY - Math.round(1.5f * numExtraLines * ViewConstants.DEFAULT_FONT_SIZE);
            int bt2X = btX;
            if (recommandExternalRescore) {
                btY = btY - 90; // for rescore entry box
                btY = btY - Math.round(6.5f * (float)ViewConstants.DEFAULT_FONT_SIZE) - 12; // for warning message
            } //else if (guidedManualScorer.shouldConsiderExternalRescore()) { // 2014-07-02: take out "should considered rescore" ... either recommand or not recommand.
            //  btY = btY - 4 * ViewConstants.DEFAULT_FONT_SIZE - 6;
            //  bt2Y = bt2Y - 4 * ViewConstants.DEFAULT_FONT_SIZE - 6;
            //}
            int bt2Y = btY - 18;
            RadioCheckField bt = generateRadioButton(writer, new Rectangle(btX, btY, btX + checkBoxHeight, btY + checkBoxHeight), "eligible", "eligible", false);
            RadioCheckField bt2 = generateRadioButton(writer, new Rectangle(bt2X, bt2Y, bt2X + checkBoxHeight, bt2Y + checkBoxHeight), "ineligible", "ineligible", false);
            PdfFormField f1 = bt.getRadioField();
            PdfFormField top = bt.getRadioGroup(false, false);
            PdfFormField f2 = bt2.getRadioField();
            bt.setBorderWidth(BaseField.BORDER_WIDTH_THICK);
            bt2.setBorderWidth(BaseField.BORDER_WIDTH_THICK);
            top.addKid(f1);
            top.addKid(f2);
            writer.addAnnotation(top);
        }

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
        String pathologistName = guidedManualScorer.getVirtualSlideReader().getAnnotations().getPathologistName();
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

        if (!rescore) {
            if (recommandExternalRescore) {
                ki67Result.add("\n\n");
                Chunk warningMsg = new Chunk(
                        "WARNING: "
                        + ViewConstants.MSG_GUIDED_MANUAL_SCORER_EXTERNAL_RESCORE_RECOMMANDED.substring(0, 1).toLowerCase()
                        + ViewConstants.MSG_GUIDED_MANUAL_SCORER_EXTERNAL_RESCORE_RECOMMANDED.substring(1),
                        FontFactory.getFont(ViewConstants.DEFAULT_FONT_NAME, ViewConstants.DEFAULT_FONT_SIZE, Font.BOLD));
                warningMsg.setBackground(BaseColor.YELLOW);
                ki67Result.add(warningMsg);
                //} else if (guidedManualScorer.shouldConsiderExternalRescore()) {
                //    ki67Result.add("\n\n");
                //    Chunk warningMsg = new Chunk(
                //            "Please note: "
                //            + ViewConstants.MSG_GUIDED_MANUAL_SCORER_EXTERNAL_RESCORE_MAY_BE_GOOD_IDEA,
                //            FontFactory.getFont(ViewConstants.DEFAULT_FONT_NAME, ViewConstants.DEFAULT_FONT_SIZE - 2, Font.ITALIC));
                //    ki67Result.add(warningMsg);

                // draw box for user to enter external resore result(s)
                Image rescoreBox = Image.getInstance(
                        Toolkit.getDefaultToolkit().getImage(
                                this.getClass().getResource(
                                        ViewConstants.RESOURCE_SEPARATOR + "images" + ViewConstants.RESOURCE_SEPARATOR + ViewConstants.RESCORE_BOX_IMAGE_FILENAME).getPath()), null);
                rescoreBox.scaleAbsolute(rescoreBox.getWidth() / 4f, rescoreBox.getHeight() / 4f);
                rescoreBox.setAlignment(Paragraph.ALIGN_CENTER);
                //document.add(rescoreBox);
                secondParagraph.add(rescoreBox);
            }

            secondParagraph.add(patientEligible);
            secondParagraph.add(patientIneligible);
        }
        secondParagraph.add(pathologistSignature);
        secondParagraph.add(comment);
        secondParagraph.add(footnote);

        return secondParagraph;
    }

    /**
     * generate radio button field
     *
     * @param writer
     * @param box
     * @param fieldName
     * @param onValue
     * @param initalValue
     * @return
     */
    public static RadioCheckField generateRadioButton(PdfWriter writer, Rectangle box, String fieldName, String onValue, boolean initialValue) {
        RadioCheckField bt = new RadioCheckField(writer, box, fieldName, onValue);
        bt.setCheckType(RadioCheckField.TYPE_CHECK);
        bt.setBackgroundColor(BaseColor.WHITE);
        bt.setBorderStyle(PdfBorderDictionary.STYLE_SOLID);
        bt.setBorderColor(BaseColor.BLACK);
        bt.setTextColor(BaseColor.BLACK);
        bt.setBorderWidth(BaseField.BORDER_WIDTH_THIN);
        bt.setChecked(initialValue);
        return bt;
    }

    /**
     * add detail report i.e. images of selected cores and associated scores
     *
     * @param document
     *
     */
    private void addDetailReport(Document document) throws DocumentException, BadElementException, IOException, VirtualSlideReaderException, FormatException, RegionNotPreprocessedException, RegionNotSupportedException {
        final int imageHeightInReport = 300;
        document.newPage(); // add page break

        Paragraph title = new Paragraph("Details of Ki67 Result", FontFactory.getFont(ViewConstants.DEFAULT_FONT_NAME, ViewConstants.TITLE_FONT_SIZE, Font.BOLD));
        title.setSpacingBefore(ViewConstants.DEFAULT_PARAGRAPH_SPACING);
        title.setSpacingAfter(ViewConstants.DEFAULT_PARAGRAPH_SPACING);
        title.setAlignment(Paragraph.ALIGN_CENTER);
        title.setSpacingBefore(ViewConstants.DEFAULT_PARAGRAPH_SPACING);
        document.add(title);

        // print some summary statistics ...
        Phrase summaryTitle = new Phrase("Summary", FontFactory.getFont(ViewConstants.DEFAULT_FONT_NAME, ViewConstants.DEFAULT_FONT_SIZE, Font.BOLD));
        document.add(summaryTitle);
        document.add(Chunk.NEWLINE);
        document.add(showInRegularTextFont("Total number of nuclei counted: " + guidedManualScorer.getTotalNumNucleiSelected()));
        document.add(Chunk.NEWLINE);
        document.add(showInRegularTextFont("Total number of " + ViewConstants.RANDOM_VIRTUAL_TMA_CORE + " scored: " + guidedManualScorer.getTotalNumVirtualTmaCores()));
        document.add(Chunk.NEWLINE);

        // print out location of cores ...
        //  Annotation.ANNOTATION_TYPE_CONSOLIDATED_SELECTION
        Hashtable<String, Color> overviewImageSelectionColors = new Hashtable<>();
        overviewImageSelectionColors.put(Annotation.ANNOTATION_TYPE_CONSOLIDATED_SELECTION, Color.yellow);
        overviewImageSelectionColors.put(Annotation.ANNOTATION_TYPE_GENERATED_VIRTUAL_TMA_CORES, Color.red);
        Image overviewImage = Image.getInstance(writer, guidedManualScorer.getVirtualSlideReader().getThumbImageWithRoi(overviewImageSelectionColors), ViewConstants.REPORT_IMAGE_QUALITY);
        // determine ideal width/height of overview image
        // width must be < document.getPageSize().getWidth() - 2*ViewConstants.REPORT_PAGE_MARGIN_LEFT_RIGHT
        // height musth be < document.getPageSize().getHeight() - ViewConstants.REPORT_HEADER_HEIGHT - ViewConstants.REPORT_FOOTER_HEIGHT
        int overviewImageWidth = Math.round(document.getPageSize().getWidth() - 2 * ViewConstants.REPORT_PAGE_MARGIN_LEFT_RIGHT);
        int overviewImageHeight = Math.round(overviewImageWidth * overviewImage.getHeight() / overviewImage.getWidth());
        if (overviewImageHeight > (document.getPageSize().getHeight() - ViewConstants.REPORT_HEADER_HEIGHT - ViewConstants.REPORT_FOOTER_HEIGHT)) {
            overviewImageHeight = Math.round(document.getPageSize().getHeight()) - ViewConstants.REPORT_HEADER_HEIGHT - ViewConstants.REPORT_FOOTER_HEIGHT;
            overviewImageWidth = Math.round(overviewImageHeight * overviewImage.getWidth() / overviewImage.getHeight());
        }
        overviewImage.scaleToFit(overviewImageWidth, overviewImageHeight);
        document.add(overviewImage);
        document.newPage();
        int counter = 1;
        for (Region r : guidedManualScorer.getVirtualSlideReader().getAnnotations().getRandomVirtualTmaCoresAnnotation().getRegions().getRegions()) {
            Image i = Image.getInstance(writer, guidedManualScorer.getVirtualSlideReader().getRegionAsBufferedImage(r), ViewConstants.REPORT_IMAGE_QUALITY);
            int scaledImageWidth = Math.round(imageHeightInReport / i.getHeight() * i.getWidth());
            i.scaleToFit(scaledImageWidth, imageHeightInReport);
            if (counter % 2 == 1) {
                document.add(Chunk.NEWLINE); // give some space so the top of the core will not be chopped up by the header
            }
            document.add(i);
            int pageHeight = Math.round(document.getPageSize().getHeight());
            int x = scaledImageWidth + 50;
            int y = pageHeight - (counter % 2 == 0 ? (imageHeightInReport + ViewConstants.DEFAULT_FONT_SIZE) : 0) - 100;
            absText(ViewConstants.RANDOM_VIRTUAL_TMA_CORE + " #" + counter, x, y, true);
            y -= ViewConstants.DEFAULT_FONT_SIZE;
            NucleiSelections selections = r.getNucleiSelections();
            absText("total number of nuclei counted: " + selections.getNumTotal(), x, y, false);
            y -= ViewConstants.DEFAULT_FONT_SIZE;
            absText("number of negative nuclei counted: " + selections.getNumNegative(), x, y, false);
            y -= ViewConstants.DEFAULT_FONT_SIZE;
            absText("number of positive nuclei counted: " + selections.getNumPositive(), x, y, false);
            y -= ViewConstants.DEFAULT_FONT_SIZE;
            String comment = r.getNucleiSelections().getComment();
            comment = comment == null ? "" : comment;
            if (comment.length() > 0) {
                absText("pathologist's comment:", x, y, false);
                y -= ViewConstants.DEFAULT_FONT_SIZE;
                absText(comment, x + Math.round(ViewConstants.SMALL_INDENT), y, false);
                y -= ViewConstants.DEFAULT_FONT_SIZE;
            }
            counter++;
            if (counter % 2 == 1) {
                // new page every two core
                document.newPage();
            }

        }

        // histogram of ki67 scores from bootstrap samples
        document.newPage();
        Paragraph titleBootstrap = new Paragraph("Distribution of Ki67 Result Among Bootstrap Samples", FontFactory.getFont(ViewConstants.DEFAULT_FONT_NAME, ViewConstants.TITLE_FONT_SIZE, Font.BOLD));
        Paragraph descriptionBootstrap = new Paragraph(
                "The " + guidedManualScorer.getTotalNumVirtualTmaCores() + " selected " + ViewConstants.RANDOM_VIRTUAL_TMA_CORE + "s were sampled (with replacement) "
                + BootstrapCI.DEFAULT_NUMBER_OF_BOOTSTRAP_ITERATIONS
                + " times to generate the bootstrap samples.  Ki67 results (i.e. average Ki67 results between the " + ViewConstants.RANDOM_VIRTUAL_TMA_CORE + "s) were calculated for the "
                + BootstrapCI.DEFAULT_NUMBER_OF_BOOTSTRAP_ITERATIONS + " bootstrap samples.  The 2.5 and 97.5 percentile of the distribution of the Ki67 results from the bootstrap samples "
                + "were taken as the 95% confidence interval of the Ki67 result.",
                FontFactory.getFont(ViewConstants.DEFAULT_FONT_NAME,
                        ViewConstants.DEFAULT_FONT_SIZE, Font.NORMAL));
        document.add(titleBootstrap);
        document.add(Chunk.NEWLINE);
        document.add(descriptionBootstrap);
        // add histogram!!!
        HistogramDataset dataset = new HistogramDataset();
        dataset.setType(HistogramType.RELATIVE_FREQUENCY);
        double[] valuesInPercent = guidedManualScorer.getVirtualSlideReader().getAnnotations().getRandomVirtualTmaCoresAnnotation().getOverallPercentPositiveNucleiSelected().getBootstrapValues();
        for (int i = 0; i < valuesInPercent.length; i++) {
            valuesInPercent[i] = valuesInPercent[i] * 100d;
        }
        dataset.addSeries("Histogram",
                valuesInPercent,
                100 // bin
        );
        String plotTitle = "Ki67 results on bootstrap samples";
        String xaxis = plotTitle;
        String yaxis = "Frequencies";
        PlotOrientation orientation = PlotOrientation.VERTICAL;
        boolean show = false;
        boolean toolTips = false;
        boolean urls = false;
        JFreeChart chart = ChartFactory.createHistogram(plotTitle, xaxis, yaxis,
                dataset, orientation, show, toolTips, urls);
        document.add(Image.getInstance(writer, chart.createBufferedImage(300, 300), ViewConstants.REPORT_IMAGE_QUALITY));
    }

    /**
     * show text as a Phrase in regular text font
     *
     * @param input
     * @return
     */
    private Phrase showInRegularTextFont(String input) {
        return new Phrase(input, FontFactory.getFont(ViewConstants.DEFAULT_FONT_NAME, ViewConstants.DEFAULT_FONT_SIZE, Font.NORMAL));
    }

    /*
     * add text at absolute position
     * reference: http://stackoverflow.com/questions/1625455/itext-positioning-text-absolutely
     */
    private void absText(String text, int x, int y, boolean underline) {
        try {
            PdfContentByte cb = writer.getDirectContent();
            BaseFont bf = BaseFont.createFont(ViewConstants.DEFAULT_FONT_NAME, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            cb.saveState();
            cb.beginText();
            cb.moveText(x, y);
            cb.setFontAndSize(bf, ViewConstants.DEFAULT_FONT_SIZE);
            String wrappedString = "";
            while ((x + cb.getEffectiveStringWidth(text, false) + ViewConstants.REPORT_PAGE_MARGIN_LEFT_RIGHT) > document.getPageSize().getWidth()) {
                int index = text.length() - 1;
                wrappedString = text.substring(index) + wrappedString;
                text = text.substring(0, index);
            }
            if (!(wrappedString.startsWith(" ") || text.endsWith(" ")) && wrappedString.length() > 0) {
                // assume we a chopping a word ...
                text = text + "-";
            }
            wrappedString = wrappedString.trim();
            text = text.trim();
            cb.showText(text);
            cb.endText();
            if (underline) {
                // need to draw underline ...
                cb.moveTo(x, y - 2);
                cb.lineTo(x + cb.getEffectiveStringWidth(text, false), y - 2);
                cb.stroke();
            }
            cb.restoreState();
            if (wrappedString.length() > 0) {
                absText(wrappedString, x, y - ViewConstants.DEFAULT_FONT_SIZE, underline);
            }
        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
