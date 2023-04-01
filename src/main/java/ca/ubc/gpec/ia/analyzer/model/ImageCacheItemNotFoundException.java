/*
 * regarding operation with ImageCache, an item is not found
 */
package ca.ubc.gpec.ia.analyzer.model;

/**
 *
 * @author samuelc
 */
public class ImageCacheItemNotFoundException extends ImageCacheException {
    public ImageCacheItemNotFoundException(String msg){
        super(msg);
    }
}
