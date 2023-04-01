package ca.ubc.gpec.ia.stitch.bliss.util;

import ca.ubc.gpec.ia.stitch.bliss.util.Da;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Hashtable;

import ca.ubc.gpec.ia.stitch.bliss.util.TileLookup;



/**
 * This class is responsible for looking up pixel 
 * coordinates of Da files
 * 
 * @author samuelc
 *
 */
public class DaLookup implements TileLookup {

	private FinalScanParser fsp;
	private FileFetcher fileFetcher;
	
	public DaLookup (String finalScanFileName, boolean isTMA, int cacheSize) throws FileNotFoundException, IOException {
		fileFetcher = new FileFetcher(cacheSize);
				
		fsp = new FinalScanParser(finalScanFileName, isTMA);
		
	}
	
	/**
	 * return file width in pixel
	 */
	public int getFileWidth() {return fsp.getFileWidth();}

	/**
	 * return file height in pixel
	 */
	public int getFileHeight() {return fsp.getFileHeight();}
	
	/**
	 * given the stitched image x,y coordinates,
	 * identifies which Da files is needed,
	 * identifies x,y coordinates of Da files
	 * and return RGB values from Da files
	 */
	public int[] getRGB(int stitchedX, int stitchedY) {
		// find Da objects from daLookupTable
		//System.out.println("x="+stitchedX+" y="+stitchedY);
		Object da = fsp.getDaLookupTable().get(new StitchedCoordinates(stitchedX,stitchedX,stitchedY,stitchedY,fsp.getDaFileWidth(),fsp.getDaFileHeight()));
		if (da == null) {
			// tile not found.  just output default pixel value.
			//System.out.println("not found x/y: " + stitchedX + "/" + stitchedY);
			//System.exit(1);
			return Da.DEFAULT_PIXEL_RGB;
		} else {
			return ((Da)da).getRGB(stitchedX, stitchedY,fileFetcher);		
		}
	}
	
	
}