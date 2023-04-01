package ca.ubc.gpec.ia.stitch.writer;

import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

/**
 * This file is responsible for writing stitched image to file in BMP format
 * 
 * @author sleung
 * 
 */
public class BMPWriter implements PixelWriter {

	private FileOutputStream fos; // file stream to write to
	private int pixelWritten; // keep track of how many pixel is written
	// already.
	private int width; // width of image in pixel
	private int height; // height of image in pixel
	private int padding; // number of 0's to pad for 4 byte alignment
	
	private String buffer; // buffer to store bytes so that we don't write to file every byte
	private int bufferSize; // size of write buffer

	public BMPWriter(String filename, int width, int height, int numBitPerPixel, int bufferSize)
			throws FileNotFoundException, IOException {

		this.width = width;
		this.height = height;
		this.pixelWritten = 0;
		this.bufferSize = bufferSize;
		this.buffer = "";
		
		padding = (width * 24) % 32 / 4;

		fos = new FileOutputStream(filename);

		// calculate bmp data size in bytes
		int paddingSize = width % 4;
		int bmpDataSize = width*height*3 + paddingSize;
		
		
		// reference: http://en.wikipedia.org/wiki/BMP_file_format
		String headerString = "424D" 
				+ toReverseHex(bmpDataSize+54,8) // BMP file size
				+ "0000" + "0000"
				+ "36000000" + "28000000" + toReverseHex(width, 8)
				+ // The width of the bitmap in pixels
				toReverseHex(height, 8)
				+ // The height of the bitmap in pixels
				"0100" + toReverseHex(numBitPerPixel, 4) + "00000000"
				+ toReverseHex(bmpDataSize, 8) + // The size (in
				// bytes) of the
				// raw BMP data
				// (after this
				// header)
				"130B0000" + "130B0000" + "00000000" + "00000000";
		// write header to file
		fos.write(toByteArray(headerString));
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

	public void writePixel(int red, int green, int blue) throws IOException {
		writeInt(blue, 2);
		writeInt(green, 2);
		writeInt(red, 2);
		pixelWritten++;

		// check to see if reached the end of line
		// if so, do some padding ...
		if (pixelWritten % width == 0) {
			// do padding
			writeInt(0, padding);
		}
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
		for (int i = 0; i < stringLength / 2; i++) {
			reverseHex = hex.substring(2 * i, 2 * i + 2) + reverseHex;
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

			BMPWriter wBMP = new BMPWriter(
					"C:\\Documents and Settings\\sleung\\workspace-java\\NOT_VERSION_CONTROLLED\\temp\\test.bmp",
					200, 200, 24,256);

			// NOTE: 0,0 is top left corner
			
			for (int i = 100; i > 0; i--) {
				for (int j = 100; j > 0; j--) {
					wBMP.writePixel(255, 0, 0);
					wBMP.writePixel(255, 255, 255);
					wBMP.writePixel(0, 0, 255);
					wBMP.writePixel(0, 255, 0);
				}
			}
			wBMP.close();

		} catch (FileNotFoundException fnfe) {
			System.err.println(fnfe);
		} catch (IOException ioe) {
			System.err.println(ioe);
		}

	}
}