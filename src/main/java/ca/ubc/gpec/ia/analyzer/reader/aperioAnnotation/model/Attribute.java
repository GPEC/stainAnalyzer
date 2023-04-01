/*
 * Aperio annotation XML file ... Attribute element
 */
package ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.model;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author samuelc
 */
@XmlRootElement(name = "Attribute")
public class Attribute implements Comparable<Attribute> {

    private String name;
    private int id;
    private String value;

    /**
     * constructor
     */
    public Attribute() {
        // do nothing
    }
    
    /**
     * deep copy
     * @return 
     */
    @Override
    public Attribute clone() {
        Attribute clone = new Attribute();
        clone.setName(this.name);
        clone.setId(this.id);
        clone.setValue(this.value);
        return clone;
    }
    
    /**
     * for Comparable interface
     * WARNING: assume id is unique!!!
     * @param other
     * @return 
     */
    @Override
    public int compareTo(Attribute other) {
        return this.id - other.id;
    }
    
    @XmlAttribute(name = "Name")
    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @XmlAttribute(name = "Id")
    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    @XmlAttribute(name = "Value")
    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
