package ca.ubc.gpec.ia.stitch.writer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.awt.Color;

/**
 * write PPM file
 * @author sleung
 *
 */
public class PPMWriter implements PixelWriter {
	
	private FileOutputStream fos; // file stream to write to
	
	private int numHexDigitPerColor; // number of hex digits to store a color component of a pixel
	
	private int width; // width of image in pixel
	private int height; // height of image in pixel
	
	private String buffer; // buffer to store bytes so that we don't write to file every byte
	private int bufferSize; // size of write buffer

	
	/**
	 * constructor
	 * @param filename
	 * @param width
	 * @param height
	 * @param numBitPerPixelColor
	 * @param bufferSize
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public PPMWriter (String filename, int width, int height, int numBitPerPixelColor, int bufferSize)
		throws FileNotFoundException, IOException {
		
		this.width = width;
		this.height = height;
		this.bufferSize = bufferSize;
		this.buffer = "";
		
		fos = new FileOutputStream(filename);
		
		// header
		String headerString = "P6 " + this.width + " " + this.height + " " + (int)(Math.pow(2, numBitPerPixelColor)-1)+ "\n";
		
		// write header to file
		fos.write(headerString.getBytes());
		
		if (numBitPerPixelColor > 8) {
			numHexDigitPerColor = 4;
		} else {
			numHexDigitPerColor = 2;
		}
	}
	
	/**
	 * write content of buffer to file and reset numBytesInBuffer
	 */
	private void flushBuffer() throws IOException {
		fos.write(toByteArray(buffer));
		buffer = ""; // reset buffer
	}
	
	/**
	 * write hex string to file
	 * 
	 * @param data
	 */
	private void writeHexString(String data) throws IOException {		
		// try to put byte to buffer first
		buffer = buffer+data;
		if (buffer.length() > bufferSize) {
			// buffer is full or almost full, flush buffer
			flushBuffer();
		}
	}

	/**
	 * write decimal value (int) to file, 0's padded to stringLength
	 * 
	 * @param dec
	 * @param stringLength
	 * @throws IOException
	 */
	private void writeInt(int dec, int stringLength) throws IOException {
		if (stringLength == 0) {
			return;
		} // do nothing if stringLength == 0
		writeHexString(toReverseHex(dec, stringLength));
	}

	/**
	 * @param dec
	 * @throws IOException
	 */
	public void writeRGB(int dec) throws IOException {
		// http://www.shodor.org/stella2java/rgbint.html
		// Java, will want an integer where bits 0-7 are the blue value, 8-15 the green, and 16-23 the red.
		writeHexString(toReverseHex(Integer.reverseBytes(dec), numHexDigitPerColor * 3));
	}
	
	public void writePixel(int red, int green, int blue) throws IOException {
		writeInt(red,2);
		writeInt(green,2);
		writeInt(blue,2);
	}

	/**
	 * close output stream
	 */
	public void close() throws IOException {
		flushBuffer();
		fos.close();
	}

	/**
	 * return reverse Hex string with 0's padding (up to strengLength) for
	 * writing to file e.g. 2835 (dec) = B13 (hex) = 130B (reverse hex with 0's
	 * padding) - assume stringLength is even and is >= length of hex
	 * 
	 * @param dec
	 * @param stringLength
	 * @return
	 */
	private String toReverseHex(int dec, int stringLength) {
		String hex = Integer.toHexString(dec);

		// pad with 0's
		while (hex.length() < stringLength) {
			hex = "0" + hex;
		}

		// reverse order
		String reverseHex = "";
		for (int i = 0; i < stringLength / numHexDigitPerColor; i++) {
			reverseHex = hex.substring(numHexDigitPerColor * i, numHexDigitPerColor * i + numHexDigitPerColor) + reverseHex;
		}

		return reverseHex;
	}

	/**
	 * return byte array to write to file
	 * 
	 * @param input
	 * @return
	 */
	private byte[] toByteArray(String data) {
		byte[] bts = new byte[data.length() / 2];
		for (int i = 0; i < bts.length; i++) {
			bts[i] = (byte) Integer.parseInt(data.substring(2 * i, 2 * i + 2),
					16);
		}
		return bts;
	}
	
	/**
	 * main function for process testing
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		try {

			PPMWriter wPPM = new PPMWriter(
					//"C:\\Documents and Settings\\sleung\\workspace-java\\NOT_VERSION_CONTROLLED\\temp\\test.ppm",
					"/home/samuelc/Desktop/test.ppm",
					200, 200, 8, 256);

			// NOTE: 0,0 is top left corner
			
			for (int i = 100; i > 0; i--) {
				for (int j = 100; j > 0; j--) {
					wPPM.writePixel(255, 0, 0);
					wPPM.writePixel(255, 255, 255);
					wPPM.writePixel(0, 0, 255);
					wPPM.writePixel(0, 255, 0);
					//wPPM.writeRGB(125);
					//wPPM.writeRGB(125);
					//wPPM.writeRGB(125);
					//wPPM.writeRGB(125);
				}
			}
			wPPM.close();

		} catch (FileNotFoundException fnfe) {
			System.err.println(fnfe);
		} catch (IOException ioe) {
			System.err.println(ioe);
		}

	}
}
