/*
 * help functiton to show virtual slide preview images
 */
package ca.ubc.gpec.ia.analyzer.controller.helper.guidedManualScorer;

import ca.ubc.gpec.ia.analyzer.reader.VirtualSlideReader;
import ca.ubc.gpec.ia.analyzer.reader.VirtualSlideReaderException;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.exception.RegionNotPreprocessedException;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.exception.RegionNotSupportedException;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.model.Annotation;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import jakarta.xml.bind.JAXBException;
import loci.formats.FormatException;

/**
 *
 * @author samuelc
 */
public class PreviewVirtualSlideHelperService extends Service<PreviewVirtualSlideInfo> {

    private File svsFile;
    private File annotationFile;

    public PreviewVirtualSlideHelperService(File svsFile, File annotationFile) {
        this.svsFile = svsFile;
        this.annotationFile = annotationFile;
    }

    /**
     * do the work!!!
     *
     * @return
     */
    @Override
    protected Task<PreviewVirtualSlideInfo> createTask() {
        return new Task<PreviewVirtualSlideInfo>() {
            @Override
            protected PreviewVirtualSlideInfo call()
                    throws VirtualSlideReaderException, FormatException, IOException, RegionNotSupportedException, RegionNotPreprocessedException, JAXBException {
                VirtualSlideReader virtualSlideReader = new VirtualSlideReader(svsFile, false);
                virtualSlideReader.setAnnotationFile(annotationFile);
                virtualSlideReader.consolidateAnnotationSelections(); // consolidate but do not export
                BufferedImage label = virtualSlideReader.getLabel();
                BufferedImage selectionThumbWithRoi = virtualSlideReader.getThumbImageWithRoi(Annotation.ANNOTATION_TYPE_CONSOLIDATED_SELECTION);

                return new PreviewVirtualSlideInfo(
                        label,
                        selectionThumbWithRoi,
                        virtualSlideReader);
            }
        };
    }
}
