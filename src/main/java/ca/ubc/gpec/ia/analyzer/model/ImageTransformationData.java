/*
 * store any data that is a result of the image transformation
 */
package ca.ubc.gpec.ia.analyzer.model;

import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author samuelc
 */
@XmlRootElement(name = "imageTransformationData")
public class ImageTransformationData implements Comparable {

    private String name; // needs to be UNIQUE!!!
    private Object value;

    /**
     * constructor
     */
    public ImageTransformationData() {
        name = null;
        value = null;
    }

    public ImageTransformationData(String name, Object value) {
        this.name = name;
        this.value = value;
    }

    @Override
    public ImageTransformationData clone() {
        ImageTransformationData clone = new ImageTransformationData(name, value);
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
        return ((ImageTransformationData) other).name.compareTo(this.name);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Object getValue() {
        return value;
    }
}
