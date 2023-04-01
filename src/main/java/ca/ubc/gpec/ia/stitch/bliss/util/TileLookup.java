package ca.ubc.gpec.ia.stitch.bliss.util;

/**
 * define interface for mapping the pixel 
 * coordinates of the stitched file to the
 * coordinates of the tile files (and identifies
 * which tile files)
 * 
 * @author sleung
 *
 */

public interface TileLookup {
	
	/**
	 * return RGB pixel values
	 * from tile gives given the x,y 
	 * coordinates of the stitched image
	 * @param stitchedX
	 * @param stitchedY
	 * @return
	 */
	public int[] getRGB(int stitchedX, int stitchedY);
	
	/**
	 * return stitched file width in pixel
	 * @return
	 */
	public int getFileWidth();
	
	/**
	 * return stitched file height in pixel
	 * @return
	 */
	public int getFileHeight();

}