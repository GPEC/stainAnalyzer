/*
 * indicate NO image transformation set
 */
package ca.ubc.gpec.ia.analyzer.transformation;

import ca.ubc.gpec.ia.analyzer.model.IAO;
import ca.ubc.gpec.ia.analyzer.model.ImageTransformation;

/**
 *
 * @author samuelc
 */
public class NullImageTransformation extends ImageTransformation {
    
    /**
     * constructor
     */
    public NullImageTransformation() {
        this.setName(this.getClass().getName());
    }
    
    /**
     * implementation of ImageTransformation abstract class
     * @param iao
     * @return 
     */
    public IAO apply(IAO iao) {
        return iao;
    }
}
