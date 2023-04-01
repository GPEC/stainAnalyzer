package ca.ubc.gpec.ia.analyzer.deconvolution;

/**
 * get colour deconvolution channel matrix values "from ROI"
 * 
 * this is almost an exact copy of the codes from G.Landini plugin (22/Jun/2010 v 1.5)
 * http://www.dentistry.bham.ac.uk/landinig/software/cdeconv/cdeconv.html
 *  
 * @author samuelc
 *
 */

import ij.*;
import ij.process.*;
import ij.gui.*;

import java.awt.*;

import javax.swing.JFileChooser;

import ca.ubc.gpec.ia.analyzer.gui.ImageFilter;

public class ColourDeconvolutionFromROI {
	
	private ImagePlus imp;
	private double[][] channelMatrix; // 3x3 matrix, row=channel
	
	/**
	 * constructor
	 */
	public ColourDeconvolutionFromROI(Component parent) {
		if (askUserForImage(parent)) {
			// ok to continue

			channelMatrix = new double[][] {
					{-1,-1,-1},
					{-1,-1,-1},
					{-1,-1,-1}
			};
			
			double [] rgbOD = new double[3];
			for (int i=0; i<3; i++){
				getmeanRGBODfromROI(i, rgbOD, imp);
				channelMatrix[i][0] = rgbOD[0]; //MODx[i]= rgbOD[0];
				channelMatrix[i][1] = rgbOD[1]; //MODy[i]= rgbOD[1];
				channelMatrix[i][2] = rgbOD[2]; //MODz[i]= rgbOD[2];
			}
			imp.close();
			IJ.showMessage("Channel matrix generated from ROI successfully.\nPlease run Setting Editor to use this matrix.");
		} else {
			IJ.showMessage("Sorry, something went wrong with selecting file.  Please try again.");
		}
	}
	
	/**
	 * ask user for image and put image into imp
	 */
	private boolean askUserForImage(Component parent) {
		
		// ask user for file
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Please select an image file for ROI");
		chooser.setFileFilter(new ImageFilter("*.gif, *.jpg/jpeg, *.tif/tiff, *.png"));
		chooser.setMultiSelectionEnabled(false);
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		int returnVal = chooser.showOpenDialog(parent);
				
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			// file chosen!!!
			imp = IJ.openImage(chooser.getSelectedFile().getAbsolutePath());
			imp.show();
			return true;
		}
		return false;
	}
	
	public double[][] getChannelMatrix() {return channelMatrix;}
	
	
	
	
	private void getmeanRGBODfromROI(int i, double [] rgbOD, ImagePlus imp){
		//get a ROI and its mean optical density. GL
		int [] xyzf = new int [4]; //[0]=x, [1]=y, [2]=z, [3]=flags
		int x1, y1, x2, y2, h=0, w=0, px=0, py=0, x, y,p;
		double log255=Math.log(255.0);
		ImageProcessor ip = imp.getProcessor();
		int mw = ip.getWidth()-1;
		int mh = ip.getHeight()-1;

		IJ.showMessage("Select ROI for Colour_"+(i+1)+".\n \n(Right-click to end)");
		getCursorLoc( xyzf, imp );
		while ((xyzf[3] & 4) !=0){  //trap until right released
			getCursorLoc( xyzf, imp );
			IJ.wait(20);
		}

		while (((xyzf[3] & 16) == 0) && ((xyzf[3] & 4) ==0)) { //trap until one is pressed
			getCursorLoc( xyzf, imp );
			IJ.wait(20);
		}

		rgbOD[0]=0;
		rgbOD[1]=0;
		rgbOD[2]=0;

		if ((xyzf[3] & 4) == 0){// right was not pressed, but left (ROI) was
			x1=xyzf[0];
			y1=xyzf[1];
			//IJ.write("first point x:" + x1 + "  y:" + y1);
			x2=x1;  y2=y1;
			while ((xyzf[3] & 4) == 0){//until right pressed
				getCursorLoc( xyzf, imp );
				if (xyzf[0]!=x2 || xyzf[1]!=y2) {
					if (xyzf[0]<0) xyzf[0]=0;
					if (xyzf[1]<0) xyzf[1]=0;
					if (xyzf[0]>mw) xyzf[0]=mw;
					if (xyzf[1]>mh) xyzf[1]=mh;
					x2=xyzf[0]; y2=xyzf[1];
					w=x2-x1+1;
					h=y2-y1+1;
					if (x2<x1) {px=x2;  w=(x1-x2)+1;} else px=x1;
					if (y2<y1) {py=y2;  h=(y1-y2)+1;} else py=y1;
					IJ.makeRectangle(px, py, w, h);
					//IJ.write("Box x:" + x2 +"  y:" + y2+" w:"+w+" h:"+h);
				}
				IJ.wait(20);
			}
			while ((xyzf[3] & 16) !=0){  //trap until left released
				getCursorLoc( xyzf, imp );
				IJ.wait(20);
			}

			for (x=px;x<(px+w);x++){
				for(y=py;y<(py+h);y++){
					p=ip.getPixel(x,y);
					// rescale to match original paper values
					rgbOD[0] = rgbOD[0]+ (-((255.0*Math.log(((double)((p & 0xff0000)>>16)+1)/255.0))/log255));
					rgbOD[1] = rgbOD[1]+ (-((255.0*Math.log(((double)((p & 0x00ff00)>> 8) +1)/255.0))/log255));
					rgbOD[2] = rgbOD[2]+ (-((255.0*Math.log(((double)((p & 0x0000ff))        +1)/255.0))/log255));
				}
			}
			rgbOD[0] = rgbOD[0] / (w*h);
			rgbOD[1] = rgbOD[1] / (w*h);
			rgbOD[2] = rgbOD[2] / (w*h);
		}
		IJ.run("Select None");
	}


	private void getCursorLoc(int [] xyzf, ImagePlus imp ) {
		ImageWindow win = imp.getWindow();
		ImageCanvas ic = win.getCanvas();
		Point p = ic.getCursorLoc();
		xyzf[0]=p.x;
		xyzf[1]=p.y;
		xyzf[2]=imp.getCurrentSlice()-1;
		xyzf[3]=ic.getModifiers();
	}

	
}
