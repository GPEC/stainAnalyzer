/*
 * Image to be analyzed
 */
package ca.ubc.gpec.ia.analyzer.model;

import ij.ImagePlus;
import ij.io.Opener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author samuelc
 */
@XmlRootElement(name = "imageDescriptor")
// specify order the fields appears in xml file
//@XmlType(propOrder = {"url", "colorSpace"})
public class ImageDescriptor implements Comparable {
   
    private String url;
    
    /**
     * for Comparable interface
     *
     * @param other
     * @return
     */
    @Override
    public int compareTo(Object other) {
        return url.compareTo(((ImageDescriptor) other).getUrl());
    }

    @Override
    public ImageDescriptor clone() {
        ImageDescriptor clone = new ImageDescriptor(url);
        return clone;
    }
    /**
     * toString
     * @return 
     */
    @Override
    public String toString(){
        return url;
    }
    
    /**
     * define equals
     * NOTE: only look at URL ... this is NEEDED if we want to look for an image
     * and we only have the URL (i.e. we don't have any other info regarding
     * this image such as colorSpace)
     * 
     * @param other
     * @return 
     */
    @Override
    public boolean equals(Object other) {
        if (other instanceof ImageDescriptor) {
            return ((ImageDescriptor)other).getUrl().equals(this.url);
        }
        return false;
    }

    /**
     * hashcode - only look at url
     * @return 
     */
    @Override
    public int hashCode() {
        return this.url.hashCode();
    }

    
    /**
     * setter
     *
     * @param url
     */
    public void setUrl(String url) {
        this.url = url;
    }

    /**
     * getter
     *
     * @return
     */
    public String getUrl() {
        return url;
    }

    /**
     * get URL object
     * @return
     * @throws MalformedURLException 
     */
    public URL getURL() throws MalformedURLException {
        return new URL(url);
    }
    
    /**
     * public constructor
     *
     * @param url
     * @param colorChannel
     */
    public ImageDescriptor(String url) {
        this.url = url;
    }

    public ImageDescriptor() {
        this.url = null;
    }
    
    /**
     * check to see if the file this descriptor refers to actually exist
     * TODO: currently only check for local files
     * @return 
     */
    public boolean exists() throws MalformedURLException {
        File f = new File((new URL(url)).getFile());
        return f.exists();
    }
    
    /**
     * get parent directory of this file ... 
     * 
     * WARNING!!! only works if URL is pointing to a local file
     * 
     * @return 
     */
    public File getParentDirectory() throws MalformedURLException {
        File f = new File((new URL(url)).getFile());
        return f.getParentFile();
    }
    
    /**
     * return an ImagePlus object from the URL
     * @return 
     */
    public ImagePlus getImagePlus() throws URISyntaxException {
        File f = new File(new URI(url));
        return (new Opener()).openImage(f.getPath());
    }
    
    /**
     * return a BufferedImage object from the URL
     * @return
     * @throws URISyntaxException 
     */
    public BufferedImage getBufferedImage() throws URISyntaxException {
        return this.getImagePlus().getProcessor().getBufferedImage();
    }
    
}
