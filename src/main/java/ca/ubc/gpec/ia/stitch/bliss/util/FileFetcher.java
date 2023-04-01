package ca.ubc.gpec.ia.stitch.bliss.util;

import java.util.Hashtable;
import java.util.Vector;

import ij.IJ;
import ij.ImagePlus;
import ij.process.ImageProcessor;

/**
 * responsible for opening files using ImageJ
 * cache opened files and remove files from cache if there
 * are too many opened files
 * 
 * @author sleung
 *
 */
public class FileFetcher {

	private Hashtable<String,ImageProcessor> imageCache;
	private Vector<String> imageNamesInCache;
	private int cacheSize;
	
	/**
	 * constructor
	 */
	public FileFetcher(int cacheSize) {
		this.cacheSize = cacheSize;
		imageCache = new Hashtable<String,ImageProcessor>();
		imageNamesInCache = new Vector<String>();
	}

	/**
	 * create ImagePlus object from name
	 * store ImagePlus object in fileCache
	 * @param directory
	 * @param name
	 */
	private void addImageProcessor(String name) {
		if (imageCache.size() > cacheSize) {
			// cache is full.  need to clean up ... empty half the cache
			for (int i=0; i<(cacheSize/2); i++) {
				// remove reference to image processor, hopefully it will cause java to close the corresponding image file as well.
				imageCache.remove(imageNamesInCache.firstElement()); 
				imageNamesInCache.removeElementAt(0);
			}
			IJ.freeMemory();
		}
		// make sure image processor is RGB
		imageCache.put(name, IJ.openImage(name).getChannelProcessor().convertToRGB());
		imageNamesInCache.add(name);
	}
	
	/**
	 * get ImageProcessor object with name
	 * @param directory
	 * @param name
	 * @return
	 */
	public ImageProcessor getImageProcessor(String name) {
		
		ImageProcessor ip =  (ImageProcessor)imageCache.get(name);
		
		if (ip==null) {
			// need to create ImagePlus object
			addImageProcessor(name);
			ip = (ImageProcessor)imageCache.get(name);
		}
		
		return ip;
	}
	
	/**
	 * main method for process test
	 * @param args
	 */
	public static void main(String[] args) {
		//ImagePlus ip = new ImagePlus("C:/Documents and Settings/sleung/workspace-java/NOT_VERSION_CONTROLLED/temp/AB269129-2R_F480-Caltag/Da74.jpg");
		try {
	
	    		ImagePlus ip = IJ.openImage("C:/Documents and Settings/sleung/workspace-java/NOT_VERSION_CONTROLLED/temp/AB269129-2R_F480-Caltag/Da74.jpg");
	    		System.out.println("pixel="+ip.getChannelProcessor().getPixel(0,0,null)[0]);
	    		System.out.println("pixel="+ip.getChannelProcessor().getPixel(700,470,null)[0]);
	    		
	    } catch (Exception e) {
			System.err.println(e);
		}
	}
}
