/*
 * parameter for image transformation
 */
package ca.ubc.gpec.ia.analyzer.model;

import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author samuelc
 */
@XmlRootElement(name = "imageTransformationParameter")
public class ImageTransformationParameter implements Comparable {

    private String name; // needs to be UNIQUE!!!
    private String value;

    /**
     * constructor
     */
    public ImageTransformationParameter() {
        name = null;
        value = null;
    }

    public ImageTransformationParameter(String name, String value) {
        this.name = name;
        this.value = value;
    }

    @Override 
    public ImageTransformationParameter clone() {
        ImageTransformationParameter clone = new ImageTransformationParameter();
        clone.setName(name);
        clone.setValue(value);
        return clone;
    }
    
    /**
     * for Comparable interface
     *
     * @param other
     * @return
     */
    @Override
    public int compareTo(Object other) {
        return ((ImageTransformationParameter) other).name.compareTo(this.name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
