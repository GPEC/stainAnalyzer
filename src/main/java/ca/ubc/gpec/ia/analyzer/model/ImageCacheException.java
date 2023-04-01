/*
 * some generic (unspecified) error during operations with ImageCache
 */
package ca.ubc.gpec.ia.analyzer.model;

import ca.ubc.gpec.ia.analyzer.transformation.ImageTransformationException;

/**
 * note: this extends TransformationException so that it won't break the 
 * abstract method "apply" interface in ImageTransformation
 * @author samuelc
 * @author samuelc
 */
public class ImageCacheException extends ImageTransformationException {
    public ImageCacheException (String msg) {
        super(msg);
    }
    
}
