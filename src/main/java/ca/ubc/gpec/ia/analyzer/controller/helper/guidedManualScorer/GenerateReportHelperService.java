/*
 * generate report
 */
package ca.ubc.gpec.ia.analyzer.controller.helper.guidedManualScorer;

import ca.ubc.gpec.ia.analyzer.model.guidedManualScorer.GuidedManualScorer;
import ca.ubc.gpec.ia.analyzer.reader.VirtualSlideReaderException;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.exception.RegionNotPreprocessedException;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.exception.RegionNotSupportedException;
import ca.ubc.gpec.ia.analyzer.report.GuidedManualScorerReport;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.DocumentException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import jakarta.xml.bind.JAXBException;
import loci.formats.FormatException;

/**
 *
 * @author samuelc
 */
public class GenerateReportHelperService extends Service<String> {

    private final GuidedManualScorer guidedManualScorer;

    public GenerateReportHelperService(GuidedManualScorer guidedManualScorer) {
        this.guidedManualScorer = guidedManualScorer;
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
                // generate the archive annotation file name ...
                String archiveAnnotationFilename = guidedManualScorer.getDefaultArchiveAnnotationFilename();
                //////////////////////////////////////////////////////////////////////////////
                // remember ... this is not FX thread ... cannot show dialog from here!!!   //
                //////////////////////////////////////////////////////////////////////////////
                GuidedManualScorerReport guidedManualScorerReport = new GuidedManualScorerReport(guidedManualScorer);
                guidedManualScorerReport.report(reportFilename, archiveAnnotationFilename);
                // CHANGE SAVE ANNOTATION FILE NAME!!!!!
                // NOTE: guidedManualScorer.saveAnnotations() will return saved annotation filename
                (new File(guidedManualScorer.saveAnnotations())).renameTo(new File(archiveAnnotationFilename));
            }

            @Override
            protected String call() throws VirtualSlideReaderException, FileNotFoundException, DocumentException, IOException, JAXBException, BadElementException, FormatException, RegionNotPreprocessedException, RegionNotSupportedException {
                // generate report!!!
                String reportFilename = guidedManualScorer.getDefaultReportFilename(guidedManualScorer.getVirtualSlideReader().getVirtualSlideFile());
                doReport(reportFilename); // do report will SAVE annotation first
                return reportFilename; // return report file name
            }
        };
    }
}
