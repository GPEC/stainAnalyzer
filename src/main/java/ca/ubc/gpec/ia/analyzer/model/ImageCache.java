/*
 * put images to memory during analyses
 * - to save time loading/writing images
 * - to save memory by writing images to filesystem
 */
package ca.ubc.gpec.ia.analyzer.model;

import ca.ubc.gpec.ia.analyzer.writer.ImageExporter;
import ca.ubc.gpec.ia.analyzer.writer.ImageExporterUnsupportedFileFormatException;
import ij.ImagePlus;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 *
 * @author samuelc
 */
public class ImageCache {

    public static final String ACTION_REMOVE_AND_DELETE = "action: remove and delete";
    public static final String ACTION_REMOVE_AND_WRITE_TO_FILESYSTEM = "action: remove and write to file system";
    private HashMap<ImageDescriptor, ImagePlus> cache;

    /**
     * constructor
     */
    public ImageCache() {
        cache = new HashMap<ImageDescriptor, ImagePlus>();
    }

    /**
     * return image, null if not found
     *
     * @param imageDescriptor
     * @return
     */
    public ImagePlus getImagePlus(ImageDescriptor imageDescriptor) throws ImageCacheItemNotFoundException {
        if (!cache.containsKey(imageDescriptor)) {
            // try to see if it is written on filesystem
            try {
                if (imageDescriptor.exists()) {
                    // get image from file system and put in cache
                    ImagePlus imp = new ImagePlus((new URL(imageDescriptor.getUrl())).getFile());
                    cache.put(imageDescriptor,imp);
                    return imp;
                } else {
                    throw new ImageCacheItemNotFoundException("trying to retrieve non-existing image: " + imageDescriptor);
                }
            } catch (MalformedURLException m) {
                throw new ImageCacheItemNotFoundException("MalformedURLException (" + m + ") encountered when trying to retrieve image: " + imageDescriptor);
            }
        }
        return cache.get(imageDescriptor);
    }

    /**
     * put item in cache
     *
     * @param imageDescriptor
     * @param imp
     * @throws ImageCacheItemAlreadyInCacheException
     */
    public void put(ImageDescriptor imageDescriptor, ImagePlus imp) throws ImageCacheItemAlreadyInCacheException {
        if (cache.containsKey(imageDescriptor)) {
            throw new ImageCacheItemAlreadyInCacheException("try to put already-in-cache image in cache again: " + imageDescriptor);
        }
        cache.put(imageDescriptor, imp);
    }

    /**
     * release image from cache, performing action described in "action"
     *
     * @param imageDescriptor
     * @param action
     * @throws ImageCacheItemNotFoundException
     */
    public void remove(ImageDescriptor imageDescriptor, String action) throws ImageCacheItemNotFoundException, ImageExporterUnsupportedFileFormatException, MalformedURLException {
        if (!cache.containsKey(imageDescriptor)) {
            throw new ImageCacheItemNotFoundException("trying to remove from image cache non-existing image: " + imageDescriptor);
        }
        if (action.equals(ACTION_REMOVE_AND_WRITE_TO_FILESYSTEM)) {
            // need to write to filesystem
            (new ImageExporter(imageDescriptor, cache.get(imageDescriptor))).writeToFilesystem();
        }
        // remove from cache!!!
        cache.remove(imageDescriptor);
    }

    /**
     * empty cache, performing action described in "action"
     *
     * @param action
     * @throws ImageExporterUnsupportedFileFormatException
     * @throws MalformedURLException
     */
    public void clear(String action) throws ImageExporterUnsupportedFileFormatException, MalformedURLException {
        for (ImageDescriptor imageDescriptor : cache.keySet()) {
            if (action.equals(ACTION_REMOVE_AND_WRITE_TO_FILESYSTEM)) {
                // need to write to filesystem
                (new ImageExporter(imageDescriptor, cache.get(imageDescriptor))).writeToFilesystem();
            }
        }
        cache.clear(); // clear cache!!!!
    }
}
