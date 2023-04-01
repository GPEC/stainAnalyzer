//package ij.measure; /// GPEC mod
package ca.ubc.gpec.ia.analyzer.measure;

public interface Measurements {
	public static final int AREA=1,MEAN=2,STD_DEV=4,MODE=8,MIN_MAX=16,
		CENTROID=32,CENTER_OF_MASS=64,PERIMETER=128, LIMIT = 256, RECT=512,
		LABELS=1024,ELLIPSE=2048,INVERT_Y=4096,CIRCULARITY=8192,FERET=16384,
		INTEGRATED_DENSITY=0x8000, MEDIAN=0x10000, 
		SKEWNESS=0x20000, KURTOSIS=0x40000, ROUNDNESS=0x80000;
	
        /// GPEC mod - added GLCM texture analysis measurements
        public static final int TEXTURE=0x100000;
        /*
        public static final int GLCM_ASM_H=0x80000,GLCM_CONTRAST_H=0x100000,GLCM_CORRELATION_H=0x200000,
            GLCM_IDM_H=0x400000,GLCM_ENTROPY_H=0x800000,GLCM_SUM_H=0x1000000,
            GLCM_ASM_V=0x2000000,GLCM_CONTRAST_V=0x4000000,GLCM_CORRELATION_V=0x8000000,
            GLCM_IDM_V=0x10000000,GLCM_ENTROPY_V=0x20000000,GLCM_SUM_V=0x40000000;
         **/
                
	/** Maximum number of calibration standard (20) */
	public static final int MAX_STANDARDS = 20;

}
