/*
 * trying to put an item into the cache, but item already in cache
 */
package ca.ubc.gpec.ia.analyzer.model;

/**
 *
 */
public class ImageCacheItemAlreadyInCacheException extends ImageCacheException {
    public ImageCacheItemAlreadyInCacheException(String msg) {
        super(msg);
    }
}
