/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.ubc.gpec.ia.analyzer.gui;

import java.io.File;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author samuelc
 */
public class ImageFileAndDirectoryFilter extends javax.swing.filechooser.FileFilter implements java.io.FileFilter{
    
    public static final String[] IMAGE_FILE_EXTENSION = {"bmp","gif","png","jpg","tiff","tif"};
    
    /**
     * for FileFilter abstract method
     * - only check for extension
     * 
     * return true if its a directory
     * 
     * @param dir
     * @param name
     * @return 
     */
    public boolean accept(File f) {
        if (f.isDirectory()) {
            return true;
        }
        String extension = FilenameUtils.getExtension(f.getName());
        for (String validImageExtension:IMAGE_FILE_EXTENSION) {
            if (validImageExtension.equalsIgnoreCase(extension)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * for FileFilter abstract method
     * @return 
     */
    public String getDescription() {
        return "image files or directory";
    }    
}
