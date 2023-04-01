package ca.ubc.gpec.ia.stitch.writer;

import java.io.IOException;

/**
 * pixel writer
 * @author samuelc
 *
 */
public interface PixelWriter {

	public static final int PPM_NUM_BIT_PER_PIXEL_COLOR = 8;
	public static final int BMP_NUM_BIT_PER_PIXEL_COLOR = 24;
	
	/**
	 * write a pixel
	 * @param red
	 * @param green
	 * @param blue
	 */
	public void writePixel(int red, int green, int blue) throws IOException;

	/**
	 * close output stream
	 */
	public void close() throws IOException;

	
}
