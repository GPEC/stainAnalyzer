package ca.ubc.gpec.ia.analyzer.util;

import ij.IJ;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;

/**
 * Because of the modifications I made which allow for the selection of subsets
 * of images, the background may not necessarily be visible in the newly cropped
 * images. This is a problem because we want to normalize our image using the
 * background intensities.
 *
 * This class implements a super simple database backend which allows the
 * storing of these intensity values. This will allow for pre-computation of the
 * intensity values
 *
 * @author Dustin Thomson <dustint@sfu.ca>
 *
 */
public class MeanBackgroundLevels {

    /**
     * The actual hashtable where we store the data
     */
    private Hashtable<String, Double> levels;
    /**
     * The location of the file representation of the background levels
     */
    private String path;
    /**
     * Flag to indicate if the backend has been loaded properly
     */
    private boolean loaded;
    /**
     * A flag to indicate that we have added data to the database, without
     * saving it
     */
    private boolean dirty;

    /**
     * The default constructor Sets the path of the file-side data. if the file
     * exists it will load it
     *
     * @param fullPath	- The Path to the file
     */
    public MeanBackgroundLevels(String fullPath) {
        this.levels = new Hashtable<String, Double>();
        this.path = fullPath;
        this.loaded = false;
    }

    /**
     * Returns the state of the file backend. If the file was loaded, will
     * return true, else returns false
     *
     * @return boolean
     */
    public boolean loaded() {
        return this.loaded;
    }

    /**
     * Loads the background levels from the disk If data was loaded from a file,
     * returns true, false otherwise Sets the loaded flag to true
     *
     * @return boolean
     */
    public boolean load() {
        File f = new File(this.path);

        //Check to make sure the file exists
        if (!f.exists()) {
            return false;
        }

        try {
            FileInputStream fIn = new FileInputStream(this.path);
            ObjectInputStream objIn = new ObjectInputStream(fIn);
            Object obj = objIn.readObject();

            objIn.close();
            fIn.close();

            if (obj instanceof Hashtable) {
                this.levels = (Hashtable) obj;
            } else {
                IJ.showMessage("Specified Data File Does Not Contain Data");
            }
        } catch (Exception e) {
            IJ.showMessage("Error Reading Database From File: " + e.toString());
            return false;
        }
        this.loaded = true;
        this.dirty = false;
        return true;
    }

    /**
     * Saves the background levels to the disk
     */
    public void save() {
        if (this.dirty) {
            try {
                FileOutputStream fOut = new FileOutputStream(this.path);
                ObjectOutputStream objOut = new ObjectOutputStream(fOut);
                objOut.writeObject(this.levels);
                objOut.close();
                fOut.close();
                this.dirty = false;
            } catch (Exception e) {
                IJ.showMessage("Error Writing Database To File: " + e.toString());
            }
        }
    }

    /**
     * Adds the background level information into the database Calling this
     * function will set the loaded flag to true, as the object is now
     * guarenteed to contain data
     *
     * @param imageName	- The name of the image
     * @param imageChannel	- The chanel of the image
     * @param data	- The float representation of the background
     */
    public void put(String imageName, String imageChannel, double data) {

        //if we were to insert a duplicate key, simply preform an update
        if (this.levels.containsKey(imageName + imageChannel)) {
            this.levels.remove(imageName + imageChannel);
        }
        this.levels.put(imageName + imageChannel, new Double(data));
        this.loaded = true;
        this.dirty = true;
    }

    /**
     * Gets the background level information from the database
     *
     * @param imageName	- The name of the image
     * @param imageChannel	- The chanel of the image
     * @param data	- The float representation of the background
     */
    public Double get(String imageName, String imageChannel) {
        return this.levels.get(imageName + imageChannel);
    }
}
