/*
 * generate consolidated report
 */
package ca.ubc.gpec.ia.analyzer.controller.helper.guidedManualScorer;

import ca.ubc.gpec.ia.analyzer.reader.VirtualSlideReaderException;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.exception.RegionNotPreprocessedException;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.exception.RegionNotSupportedException;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.model.Annotations;
import ca.ubc.gpec.ia.analyzer.report.ConsolidatedGuidedManualScorerReport;
import ca.ubc.gpec.ia.analyzer.views.ViewConstants;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.DocumentException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import jakarta.xml.bind.JAXBException;
import loci.formats.FormatException;

/**
 *
 * @author samuelc
 */
public class GenerateConsolidatedReportHelperService extends Service<String> {

    private final HashMap<File, Annotations> annotationsTable;

    /**
     * constructor
     *
     * @param annotationsTable
     */
    public GenerateConsolidatedReportHelperService(HashMap<File, Annotations> annotationsTable) {
        this.annotationsTable = annotationsTable;
    }

    /**
     * do the work!!! - generate report
     *
     * @return report file name
     */
    @Override
    protected Task<String> createTask() {
        return new Task<String>() {
            /**
             * generate report
             *
             * @param reportFilename
             *
             * @throws FileNotFoundException
             * @throws DocumentException
             * @throws IOException
             * @throws VirtualSlideReaderException
             */
            private void doReport(String reportFilename) throws FileNotFoundException, DocumentException, IOException, VirtualSlideReaderException, JAXBException, BadElementException, FormatException, RegionNotPreprocessedException, RegionNotSupportedException {
                //////////////////////////////////////////////////////////////////////////////
                // remember ... this is not FX thread ... cannot show dialog from here!!!   //
                //////////////////////////////////////////////////////////////////////////////
                ConsolidatedGuidedManualScorerReport consolidatedGuidedManualScorerReport = new ConsolidatedGuidedManualScorerReport(annotationsTable);
                consolidatedGuidedManualScorerReport.report(reportFilename);
            }

            @Override
            protected String call() throws VirtualSlideReaderException, FileNotFoundException, DocumentException, IOException, JAXBException, BadElementException, FormatException, RegionNotPreprocessedException, RegionNotSupportedException {
                // generate report!!!
                String annotationFilename = annotationsTable.keySet().iterator().next().getAbsolutePath(); // all annotation files should start with virtual slide name
                String reportFilename = annotationFilename.substring(0, annotationFilename.length() - (annotationFilename.endsWith(".ndpi") ? 5 : 4 /* assume .svs */)) + ViewConstants.GUIDED_MANUAL_SCORER_KI67_CONSOLIDATED_SCORE_REPORT_SUFFIX;
                
                doReport(reportFilename); // do report will SAVE annotation first
                return reportFilename; // return report file name
            }
        };
    }
}
