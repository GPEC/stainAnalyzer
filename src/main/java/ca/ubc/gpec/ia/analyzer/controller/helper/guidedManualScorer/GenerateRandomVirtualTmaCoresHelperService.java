/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.ubc.gpec.ia.analyzer.controller.helper.guidedManualScorer;

import ca.ubc.gpec.ia.analyzer.model.guidedManualScorer.GuidedManualScorer;
import ca.ubc.gpec.ia.analyzer.reader.VirtualSlideReader;
import ca.ubc.gpec.ia.analyzer.reader.VirtualSlideReaderException;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.exception.RegionGeneratorMaxTriesExceeded;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.exception.RegionNotPreprocessedException;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.exception.RegionNotSupportedException;
import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.model.Region;
import java.io.IOException;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import loci.formats.FormatException;

/**
 *
 * @author samuelc
 */
public class GenerateRandomVirtualTmaCoresHelperService extends Service<RandomVirtualTmaCoreImages> {

    private VirtualSlideReader virtualSlideReader;
    private int diameterInMicrometer;
    private boolean initial; // generate initial 5 TMA cores or not?

    /**
     * constructor
     *
     * @param regions
     * @param diameterInMicrometer
     * @param micronsPerPixel
     * @param initial - true: generate initial 5 (NO overlap) TMA cores; false:
     * generate ONE additional (can overlap) core
     */
    public GenerateRandomVirtualTmaCoresHelperService(VirtualSlideReader virtualSlideReader, int diameterInMicrometer, boolean initial) {
        this.virtualSlideReader = virtualSlideReader;
        this.diameterInMicrometer = diameterInMicrometer;
        this.initial = initial;
    }

    /**
     * do the work!!! - the generated random virtual TMA cores regions are
     * stored in virtualSlideReader object
     *
     * @return
     */
    @Override
    protected Task<RandomVirtualTmaCoreImages> createTask() {
        return new Task<RandomVirtualTmaCoreImages>() {
            @Override
            protected RandomVirtualTmaCoreImages call() throws RegionNotPreprocessedException, RegionNotSupportedException, RegionGeneratorMaxTriesExceeded, VirtualSlideReaderException, FormatException, IOException {
                RandomVirtualTmaCoreImages randomVirtualTmaCoreImages = new RandomVirtualTmaCoreImages();
                if (initial) {
                    virtualSlideReader.generateRandomVirtualTmaCores(
                            diameterInMicrometer,
                            GuidedManualScorer.GUIDED_MANUAL_SCORER_NUMBER_OF_RANDOM_TMA_CORES_TO_GENERATE);
                    for (Region region : virtualSlideReader.getAnnotations().getRandomVirtualTmaCoresAnnotation().getRegions().getRegions()) {
                        randomVirtualTmaCoreImages.addImage(SwingFXUtils.toFXImage(virtualSlideReader.getRegionAsBufferedImage(region), null));
                    }
                } else {
                    virtualSlideReader.generateAdditionalRandomVirtualTmaCore(
                            GuidedManualScorer.GUIDED_MANUAL_SCORER_RANDOM_TMA_CORE_DIAMETER_IN_MICROMETER,
                            // use default random seed
                            true // allow overlap
                            );
                    randomVirtualTmaCoreImages.addImage(
                            SwingFXUtils.toFXImage(
                            virtualSlideReader.getRegionAsBufferedImage(
                            virtualSlideReader.getAnnotations().getRandomVirtualTmaCoresAnnotation().getRegions().getRegions().last()),
                            null));
                }
                return randomVirtualTmaCoreImages;
            }
        };
    }
}
