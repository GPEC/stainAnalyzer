/*
 * Ask user to specify image(s) for analysis
 */
package ca.ubc.gpec.ia.analyzer.gui;

import ca.ubc.gpec.ia.analyzer.model.IAO;
import ca.ubc.gpec.ia.analyzer.model.ImageDescriptor;
import ca.ubc.gpec.ia.analyzer.reader.VirtualSlideReader;
import ca.ubc.gpec.ia.analyzer.reader.VirtualSlideReaderException;
import java.awt.Component;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.TreeSet;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileSystemView;

/**
 *
 * @author samuelc
 */
public class ImageChooserDialog extends JFileChooser {

    public static final boolean DEBUG = true;
    private JFileChooser jFileChooser; // TODO: problem using super JFileChooser ... therefore need to use a private JFileChooser ... need to fix this
    private TreeSet<IAO> iaos; // one file per iao

    public ImageChooserDialog() {
        super();
        iaos = new TreeSet<IAO>();
    }

    public ImageChooserDialog(File currentDirectory) {
        jFileChooser = new JFileChooser(currentDirectory);
        //super(currentDirectory);
        iaos = new TreeSet<IAO>();
    }

    public ImageChooserDialog(String currentDirectoryPath) {
        jFileChooser = new JFileChooser(currentDirectoryPath);
        //super(currentDirectoryPath);
        iaos = new TreeSet<IAO>();
    }

    public ImageChooserDialog(File currentDirectory, FileSystemView fsv) {
        jFileChooser = new JFileChooser(currentDirectory, fsv);
        //super(currentDirectory, fsv);
        iaos = new TreeSet<IAO>();
    }

    public ImageChooserDialog(String currentDirectoryPath, FileSystemView fsv) {
        jFileChooser = new JFileChooser(currentDirectoryPath, fsv);
        //super(currentDirectoryPath, fsv);
        iaos = new TreeSet<IAO>();
    }

    /**
     * return the set of select image(s)
     */
    public TreeSet<IAO> getIaos() {
        return iaos;
    }

    /**
     * return an array of files represented by all the iaos - this is a "legacy"
     * method to interface for original GPEC ImageJ plugin codes
     */
    @Override
    public File[] getSelectedFiles() {
        if (iaos.isEmpty()) {
            return new File[0];
        } // iaos is empty!

        ArrayList<File> filesArrayList = new ArrayList<File>();
        for (IAO iao : iaos) {
            for (ImageDescriptor id : iao.getImageDescriptors()) {
                try {
                    if (DEBUG) {
                        System.out.println("file selected: " + id.getUrl());
                    }
                    filesArrayList.add(new File(new URI(id.getUrl())));
                } catch (URISyntaxException ex) {
                    // just ignore ... url from JFileChooser - must be valid 
                }
            }
        }
        File[] result = new File[filesArrayList.size()];
        int i = 0;
        for (File f : filesArrayList) {
            result[i] = f;
            i++;
        }
        return result;
    }

    /**
     * go through all files in the specified directory and return a list of
     * IAO's.
     *
     * if directory is a file, returns an empty TreeSet
     *
     * @param directory
     * @return
     */
    private TreeSet<IAO> scanDirectoryForImageFiles(File directory) throws MalformedURLException {
        TreeSet<IAO> result = new TreeSet<IAO>();
        if (directory.isFile()) {
            return result;
        }
        for (File f : directory.listFiles(new ImageFileAndDirectoryFilter())) {
            if (f.isDirectory()) {
                result.addAll(scanDirectoryForImageFiles(f));
            } else {
                IAO iao = new IAO();
                iao.addImageDescriptor(new ImageDescriptor(f.toURI().toURL().toExternalForm()));
                result.add(iao);
            }
        }
        return result;
    }

    /**
     * extract analyzable file(s) and generate IAO(s) - analyzable because
     * ImageJ cannot analyze e.g. Aperio svs files directly ... therefore,
     * currently can only extract and export regions from svs file to analyzable
     * formats e.g. jpeg, tiff
     *
     * @param f
     * @return
     */
    private TreeSet<IAO> extractIAOs(File f) throws VirtualSlideReaderException {
        VirtualSlideReader vsr = new VirtualSlideReader(f, true);
        TreeSet<IAO> result = new TreeSet<IAO>();
        for (File a : vsr.getAnalyzableFiles()) {
            try {
                // make an IAO out of this file
                IAO iao = new IAO();
                String url = a.toURI().toURL().toExternalForm().replace("file:", "file://");
                if (DEBUG) {
                    System.out.println("input file: " + a.toURI().toURL().toExternalForm());
                }
                // toURI().toURL().toExternalForm() gives invalid URL 
                // e.g. file:/c/... instead of file:///c/...
                // ... just manually do it!!!
                iao.addImageDescriptor(new ImageDescriptor(url));
                result.add(iao);
            } catch (MalformedURLException ex) {
                throw new VirtualSlideReaderException(ex.toString());
            }
        }
        return result;
    }

    /**
     * enable multi select
     *
     * @param b
     */
    @Override
    public void setMultiSelectionEnabled(boolean b) {
        jFileChooser.setMultiSelectionEnabled(b);
    }

    /**
     * same as JFileChooser setFileSelectionMode(int mode)
     *
     * @param mode
     */
    @Override
    public void setFileSelectionMode(int mode) {
        jFileChooser.setFileSelectionMode(mode);
    }

    /**
     * same as JFileChooser setDialogTitle
     *
     * @param dialogTitle
     */
    @Override
    public void setDialogTitle(String dialogTitle) {
        jFileChooser.setDialogTitle(dialogTitle);
    }

    /**
     * ask for input file/folder
     *
     */
    public int showOpenDialogWithException(Component parent) throws VirtualSlideReaderException {
        //int result = jFileChooser.showOpenDialog(parent);
        int result = jFileChooser.showOpenDialog(parent);

        File[] fs = jFileChooser.getSelectedFiles();
        //File[] fs = super.getSelectedFiles();

        for (File f : fs) {
            if (DEBUG) {
                System.out.println("reading " + f.getName());
            }
            try {
                if (f.isDirectory()) {
                    // if folder, recursively find all images
                    iaos.addAll(scanDirectoryForImageFiles(f));
                } else {
                    // since ImageJ analyzes common image types only (e.g. jpeg, bmp ...),
                    // if we see non-common file types e.g. svs, we need to export images to 
                    // common file first 
                    //
                    iaos.addAll(extractIAOs(f));
                }
            } catch (MalformedURLException ex) {
                throw new VirtualSlideReaderException("MalformedURLException: " + ex.toString());
            }
        }
        return result;
    }

    /**
     * ask for input file/folder
     *
     */
    @Override
    public int showOpenDialog(Component parent) {
        int result = 0;
        try {
            result = showOpenDialogWithException(parent);
        } catch (VirtualSlideReaderException ex) {
            // throw runtime exception
            throw new RuntimeException("VirtualSlideReaderException: " + ex.toString());
        }
        return result;
    }
}
