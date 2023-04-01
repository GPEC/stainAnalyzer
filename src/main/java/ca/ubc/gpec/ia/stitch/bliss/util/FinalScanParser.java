package ca.ubc.gpec.ia.stitch.bliss.util;

import ca.ubc.gpec.ia.stitch.bliss.util.StitchedCoordinates;
import ca.ubc.gpec.ia.stitch.bliss.util.Da;
import ij.IJ;
import ij.process.ImageProcessor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

/**
 * This class is responsible for parsing the "FinalScan.ini" file Bliss
 * coordinates: bottom left = max X & min Y; upper right = min x & max Y Pixel
 * coordinates: bottom left = min X & max Y; upper right = max X & min Y; min
 * X,Y defined as 0,0 i.e. Pixel coordinates represents distance FROM (top left)
 * Bliss coordinates max X & max Y assume Bliss coordinates represents top left
 * corner of tile image.
 * 
 * seems bliss coordinates 0,0 is bottom right 
 * i.e. increasing X = from right to left
 *      increasing Y = from bottom to top
 * 
 * @author sleung
 * 
 */
public class FinalScanParser {

	private static final String DA_FILENAME_PREFIX = "Da";
	private static final String LX_STEP_SIZE = "lXStepSize";
	private static final String LY_STEP_SIZE = "lYStepSize";
	
	private int filePixelHeight;
	private int filePixelWidth;
	private int daFileHeight; // da file height in pixel
	private int daFileWidth;  // da file width in pixel
	private int blissHeight; // da file height in bliss coordinates
	private int blissWidth; // da file width in bliss coordinates
	private int blissOriginX; 
	private int blissOriginY;
	private double blissPixelConversionX;
	private double blissPixelConversionY;
	private int correctionFactorX; // for some reason tiles overlap by correctionFactorX, in pixel coordinates
	private int correctionFactorY; // for some reason tiles overlap by correctionFactorY, in pixel coordinates
	private boolean isTMA;
	
	private Hashtable<StitchedCoordinates, Da> daLookupTable;

	/**
	 * Class to store raw Da information
	 * 
	 * @author sleung
	 * 
	 */
	class DaRaw {
		private int x;
		private int y;
		private String name; // expect full path

		DaRaw(String name, int x, int y) {
			this.name = name;
			this.x = x;
			this.y = y;
		}

		String getName() {
			return name;
		}

		int getX() {
			return x;
		}

		int getY() {
			return y;
		}

	}

	/**
	 * constructor
	 * @param finalScanFileName
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public FinalScanParser(String finalScanFileName, boolean isTMA)
			throws FileNotFoundException, IOException {
		daLookupTable = new Hashtable<StitchedCoordinates, Da>(); // initialize daLookupTable
		this.isTMA = isTMA;
		parse(finalScanFileName);
	}

	/**
	 * return daLookupTable
	 * @return
	 */
	public Hashtable<StitchedCoordinates, Da> getDaLookupTable() {return daLookupTable;}
	
	public int getDaFileWidth() {return daFileWidth-correctionFactorX;}
	public int getDaFileHeight() {return daFileHeight-correctionFactorY;}
	
	public int getFileWidth() {return filePixelWidth;}
	public int getFileHeight() {return filePixelHeight;}
	
	/**
	 * return the directory name of filename
	 * 
	 * @param filename
	 * @return
	 */
	private String getDirectory(String filename) {
		File f = new File(filename);
		return f.getParent();
	}

	/**
	 * return list of available Da file names
	 * 
	 * @param directory
	 * @return
	 */
	private String[] getDaNames(String directory) {
		File f = new File(directory);

		// return list of file names start start with DA_FILENAME_PREFIX - i.e.
		// Da files ... e.g. Da34.jpg
		return f.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name.startsWith(DA_FILENAME_PREFIX)) {
					return true;
				} else {
					return false;
				}
			}
		});
	}

	/**
	 * if line (from FinalScan.ini) represents a Da file found in names (an
	 * array of Da files found in the directory) return name of Da file,
	 * otherwise return null
	 * 
	 * @param line
	 * @param names
	 * @return
	 */
	private String matchDaFilename(String line, String[] names) {
		for (String name : names) {
			if (line.trim().equals(
					"[" + name.substring(0, name.indexOf(".")) + "]")) {
				// file entry found in FinalScan.ini
				return name;
			}
		}
		return null;
	}

	/**
	 * get pixel x coordinate given bliss x coordinate
	 * 
	 * @param blissX
	 * @return
	 */
	private int getPixelX(int blissX) {
		int correctionX = (-1*blissX - blissOriginX)/blissWidth*correctionFactorX;
		return (int)Math.round((-1*blissX - blissOriginX) * blissPixelConversionX) - correctionX;
	}

	/**
	 * get pixel y coordinate given bliss y coordinate
	 * 
	 * @param blissY
	 * @return
	 */
	private int getPixelY(int blissY) {
		int correctionY = (-1*blissY - blissOriginY)/blissHeight*correctionFactorY;
		return (int)Math.round((-1*blissY - blissOriginY) * blissPixelConversionY) - correctionY; 
	}

	/**
	 * calculate bliss to pixel conversion factor
	 * 
	 * @param daRawList
	 */
	private void calculateConversionFactors(List<DaRaw> daRawList) {
		
		// first need to calculate pixel coordinates of each da files

		// figure out height and width of each tile (assume all tile images are
		// same size)
		ImageProcessor ip = IJ.openImage(daRawList.get(0).getName())
				.getChannelProcessor();
		daFileHeight = ip.getHeight();
		daFileWidth = ip.getWidth();
		
		// hashtable holds <interval,count>
		Hashtable<Integer, Integer> xIntervals = new Hashtable<Integer, Integer>();
		Hashtable<Integer, Integer> yIntervals = new Hashtable<Integer, Integer>();
		for (int i = 0; i < daRawList.size() - 1; i++) {
			int xInterval = Math.abs(daRawList.get(i).getX()
					- daRawList.get(i + 1).getX());
			int yInterval = Math.abs(daRawList.get(i).getY()
					- daRawList.get(i + 1).getY());
			int currCount = 0;
			if (xIntervals.get(xInterval) != null) {
				currCount = xIntervals.get(xInterval);
			}
			xIntervals.put(xInterval, currCount + 1);
			currCount = 0;
			if (yIntervals.get(yInterval) != null) {
				currCount = yIntervals.get(yInterval);
			}
			yIntervals.put(yInterval, currCount + 1);
		}
		// assume the most common interval (i.e. max count) and the interval is
		// > 0 would represent the tile width/height in bliss coordinates scale
		int xCount = 0;
		blissWidth = 0;
		int yCount = 0;
		blissHeight = 0;
		for (Enumeration e = xIntervals.keys(); e.hasMoreElements();) {
			int currValue = (Integer) e.nextElement();
			if (currValue > 0) {
				int currCount = xIntervals.get(currValue);
				if (currCount > xCount) {
					blissWidth = currValue;
					xCount = currCount;
				}
			}
		}
		for (Enumeration e = yIntervals.keys(); e.hasMoreElements();) {
			int currValue = (Integer) e.nextElement();
			if (currValue > 0) {
				int currCount = yIntervals.get(currValue);
				if (currCount > yCount) {
					blissHeight = currValue;
					yCount = currCount;
				}
			}
		}

		// conversion factor
		blissPixelConversionX = ((double)daFileWidth)/((double)blissWidth);
		blissPixelConversionY = ((double)daFileHeight)/((double)blissHeight);
		System.out.println(blissPixelConversionX+" "+blissPixelConversionY+" "+daFileWidth+" "+daFileHeight+" "+blissWidth+" "+blissHeight);
	}

	/**
	 * for similicity in the code without sacraficing much performance,
	 * parse FinalScan.ini file again just for the lXStepSize/lYStepSize
	 * @param finalScanFileName
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private int[] parseXYStepSize(String finalScanFileName) throws FileNotFoundException, IOException {
		// read file
		BufferedReader br = new BufferedReader(
				new FileReader(finalScanFileName));
		String line=br.readLine();
		int lXStepSize=0; // just ignore it if lXStepSize/lYStepSize not specified
		int lYStepSize=0; // just ignore it if lXStepSize/lYStepSize not specified
		while (line != null) {
			line = line.trim();
			if (line.trim().startsWith(LX_STEP_SIZE)) {
				lXStepSize = Integer.parseInt(line.substring(line.indexOf("=")+1));
			}
			if (line.trim().startsWith(LY_STEP_SIZE)) {
				lYStepSize = Integer.parseInt(line.substring(line.indexOf("=")+1));
			}
			line=br.readLine();
		}
		br.close();
		return new int[] {lXStepSize, lYStepSize};
	}
	
	/**
	 * parse FinalScan.ini file
	 * @param finalScanFileName
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	private void parse(String finalScanFileName) throws FileNotFoundException,
			IOException {

		// read file
		BufferedReader br = new BufferedReader(
				new FileReader(finalScanFileName));

		// assume FinalScan.ini is in the same directory as all the Da tile
		// files
		String dirName = getDirectory(finalScanFileName); // directory
															// containing the
															// file
		String[] availableDaFileNames = getDaNames(dirName);
		List<DaRaw> daRawList = new ArrayList<DaRaw>();
		int minBlissX = Integer.MAX_VALUE; // min x bliss coordinates value
		int maxBlissX = Integer.MIN_VALUE; // max x bliss coordinates value
		int minBlissY = Integer.MAX_VALUE;// min y bliss coordinates value
		int maxBlissY = Integer.MIN_VALUE; // max y bliss coordinates value

		String line = br.readLine().trim();
		boolean endOfFile = false; // a flag to break out of the main while loop
		while (line != null) {
			line = line.trim();
			String daFileName = matchDaFilename(line, availableDaFileNames);
			while (daFileName == null) {
				line = br.readLine();
				if (line == null) {
					break; // end of file reached ... get out of this while
							// loop!!!
				} else {
					line = line.trim(); // read until found Da file entry
				}
				daFileName = matchDaFilename(line, availableDaFileNames);
			}
			if (line == null) {
				break;
			} // end of file reached ... get out of this while loop!!!
			while (!(line.startsWith("x=") | line.startsWith("y="))) {
				line = br.readLine();
				if (line == null) {
					// it seems that there will be some more jpg images corresponding to snapshots taken
					// after the scan.  These images will not have corresponding x/y coordinates.
					// These are not part of the tile images.  therefore, we
					// can safely ignore them.
					endOfFile = true;
					break;
				}
				line = line.trim(); // read until reach x/y definition
			}
			if (endOfFile) {break;} // break out of main while loop
			// this is the start of Da file entry
			// get x/y coordinates
			int x = -1;
			int y = -1;
			while (line.startsWith("x=") | line.startsWith("y=")) {
				if (line.startsWith("x=")) {
					x = Integer.parseInt(line.substring(line.indexOf("=") + 1));
					if (x > maxBlissX) {
						maxBlissX = x;
					}
					if (x < minBlissX) {
						minBlissX = x;
					}
				} else {
					y = Integer.parseInt(line.substring(line.indexOf("=") + 1));
					if (y > maxBlissY) {
						maxBlissY = y;
					}
					if (y < minBlissY) {
						minBlissY = y;
					}
				}
				line = br.readLine();
				if (line == null) {
					break; // end of file reached ... get out of this while
							// loop!!!
				} else {
					line = line.trim();
				}
			}
			// we now with the x/y coordinates (bliss coordiantes NOT pixel
			// coordinates)
			// store them in the DaRaw object
			System.out.println(daFileName+" "+x+" "+y);
			daRawList
					.add(new DaRaw(dirName + File.separator + daFileName, x, y));

			line = br.readLine();

		}

		br.close();
		// finished parse file

		// first need to figure out where about is (0,0) in pixel coordinates
		// (i.e. top left corner)
		blissOriginX = -1*maxBlissX;
		blissOriginY = -1*maxBlissY; // bliss coordinates bottom to top; pixel coordinate bottom to top

		// need to figure out conversion factor from bliss coordinates to pixel
		// coordinates
		calculateConversionFactors(daRawList);

		// figure out how many tile is in row/column
		int numDaFilesPerRow = (int)Math.round((double)(maxBlissX - minBlissX)/blissWidth)+1;
		int numDaFilesPerColumn = (int)Math.round((double)(maxBlissY - minBlissY)/blissHeight)+1;
		int[] lXYStepSize = parseXYStepSize(finalScanFileName);
		correctionFactorX = (int)((double)(lXYStepSize[0]-blissWidth)*blissPixelConversionX);
		correctionFactorY = (int)((double)(lXYStepSize[1]-blissHeight)*blissPixelConversionY);
		int correctionX = numDaFilesPerRow*correctionFactorX; // correction for whole file width
		int correctionY = numDaFilesPerColumn*correctionFactorY; // correction for whole file height
		
		// calculate whole file width and height
		filePixelWidth = (int)(blissPixelConversionX * (double)(maxBlissX - minBlissX)+daFileWidth) - correctionX;
		if ((filePixelWidth % 2) == 1) {
			System.out.println("File width is not even: "+filePixelWidth);
			System.out.println("... for some reason, the program can't work with file with odd width");
			filePixelWidth--;
			System.out.println("... changing file width to: "+filePixelWidth);
		}
		filePixelHeight = (int)(blissPixelConversionY * (double)(maxBlissY - minBlissY)+daFileHeight) - correctionY;
		
		System.out.println("daFileWidth/daFileHeight = "+daFileWidth+"/"+daFileHeight);
		System.out.println("blissOriginX/Y = "+blissOriginX+"/"+blissOriginY);
		System.out.println("correctionFactorX/Y = "+correctionFactorX+" "+correctionFactorY);
		System.out.println("number of da file per row/col "+numDaFilesPerRow+" "+numDaFilesPerColumn);
		System.out.println("file width/height "+filePixelWidth+" "+filePixelHeight);
		
		// now we are ready to add the pixel coordinates into daLookupTable
		for (int i = 0; i < daRawList.size(); i++) {
			DaRaw daRaw = daRawList.get(i);
			//System.out.println(daRaw.getName()+
			//		" "+
			//		getPixelX(daRaw.getX())+
			//		"-"+
			//		(getPixelX(daRaw.getX())+daFileWidth-correctionFactorX-1)+
			//		" "+
			//		getPixelY(daRaw.getY())+
			//		"-"+
			//		(getPixelY(daRaw.getY())+daFileHeight-correctionFactorY-1));
			//System.out.println(getPixelY(daRaw.getY())+"-"+(getPixelY(daRaw.getY())+daFileHeight));
			StitchedCoordinates sc = new StitchedCoordinates(getPixelX(daRaw.getX()),
					getPixelX(daRaw.getX())+daFileWidth-correctionFactorX-1,
					getPixelY(daRaw.getY()),
					getPixelY(daRaw.getY())+daFileHeight-correctionFactorY-1,
					daFileWidth-correctionFactorX,
					daFileHeight-correctionFactorY);
			daLookupTable.put(sc, 
					new Da(daRaw.getName(),
							sc,
							correctionFactorX, 
							correctionFactorY));
		}
	}

	/**
	 * main method for processing testing.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		try {
			FinalScanParser fsp = new FinalScanParser(
					"C:/Documents and Settings/sleung/workspace-java/NOT_VERSION_CONTROLLED/temp/10-001_HER2-Site114_-1_v-_b-/FinalScan.ini",
					true);
		} catch (Exception e) {
			System.out.println(e);
		}

	}
}