/*
 * write image to file system
 */
package ca.ubc.gpec.ia.analyzer.writer;

import ca.ubc.gpec.ia.analyzer.model.ImageDescriptor;
import ca.ubc.gpec.ia.analyzer.util.MiscUtil;
import ij.ImagePlus;
import ij.io.FileSaver;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author samuelc
 */
public class ImageExporter {

    private ImageDescriptor imageDescriptor;
    private ImagePlus imp;

    /**
     * constructor
     *
     * @param imageDescriptor
     */
    public ImageExporter(ImageDescriptor imageDescriptor, ImagePlus imp) {
        this.imageDescriptor = imageDescriptor;
        this.imp = imp;
    }

    /**
     * check the directory containing "filename", if it does not exist, create 
     * it (as well as any non-existent parents)
     *
     * @param filename
     */
    private void checkDirectory(String filename) {
        File f = new File(FilenameUtils.getFullPath(filename));
        if (!f.exists()) {
            f.mkdirs();
        }
    }

    /**
     * write image to file system - file format based on extension e.g. .bmp,
     * available format (via ImageJ plugin): bmp, fits, gif, jpg, pqm (ppm),
     * png, raw, tif
     *
     * @throws ImageWriterUnsupportedFileFormatException
     */
    public void writeToFilesystem() throws ImageExporterUnsupportedFileFormatException, MalformedURLException {
        // extract file name extension
        String filename = MiscUtil.revertUrlSpecialCharacterEncoding((new URL(imageDescriptor.getUrl())).getFile());
        checkDirectory(filename); // check to see if containing directory exist first
        String extension = FilenameUtils.getExtension(filename);
        FileSaver sf = new FileSaver(imp);
        if (extension.equalsIgnoreCase("bmp")) {
            sf.saveAsBmp(filename);
        } else if (extension.equalsIgnoreCase("fits")) {
            sf.saveAsFits(filename);
        } else if (extension.equalsIgnoreCase("gif")) {
            sf.saveAsGif(filename);
        } else if (extension.equalsIgnoreCase("jpg") | extension.equalsIgnoreCase("jpeg")) {
            sf.saveAsJpeg(filename);
        } else if (extension.equalsIgnoreCase("pqm") | extension.equalsIgnoreCase("ppm")) {
            sf.saveAsPgm(filename);
        } else if (extension.equalsIgnoreCase("png")) {
            sf.saveAsPng(filename);
        } else if (extension.equalsIgnoreCase("raw")) {
            sf.saveAsRaw(filename);
        } else if (extension.equalsIgnoreCase("tif") | extension.equalsIgnoreCase("tiff")) {
            sf.saveAsTiff(filename);
        } else {
            throw new ImageExporterUnsupportedFileFormatException("try to write image to filesystem (" + imageDescriptor + ") ... unsupported format");
        }
    }
}
