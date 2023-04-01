/*
 * check the need for internal resocre
 */
package ca.ubc.gpec.ia.analyzer.controller.helper.guidedManualScorer;

import ca.ubc.gpec.ia.analyzer.model.guidedManualScorer.GuidedManualScorer;
import ca.ubc.gpec.ia.analyzer.reader.VirtualSlideReaderException;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 *
 * @author samuelc
 */
public class CheckNeedInternalRescoreHelperService extends Service<Boolean> {

    private GuidedManualScorer guidedManualScorer;

    /**
     * constructor
     *
     * @param guidedManualScorer
     */
    public CheckNeedInternalRescoreHelperService(GuidedManualScorer guidedManualScorer) {
        this.guidedManualScorer = guidedManualScorer;
    }

    /**
     * do the work!!! - check the need for internal rescore
     *
     * @return
     */
    @Override
    protected Task<Boolean> createTask() {
        return new Task<Boolean>() {
            @Override
            protected Boolean call() throws VirtualSlideReaderException {
                Boolean result = new Boolean(guidedManualScorer.needInternvalRescore());
                return result;
            }
        };
    }
}
