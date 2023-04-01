/*
 * Aperio annotation XML file ... Attributes element
 */
package ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.model;

import java.util.TreeSet;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author samuelc
 */
@XmlRootElement(name = "Attributes")
public class Attributes {

    TreeSet<Attribute> attributes;

    /**
     * constructor
     */
    public Attributes() {
        attributes = new TreeSet<Attribute>();
    }
    
    /**
     * deep copy
     * @return 
     */
    @Override
    public Attributes clone(){
        TreeSet<Attribute> cloneAttributes = new TreeSet<Attribute>();
        for (Attribute attribute:attributes) {
            cloneAttributes.add(attribute.clone());
        }
        Attributes clone = new Attributes();
        clone.setAttributes(cloneAttributes);
        return clone;
    }
    
    @XmlElement(name = "Attribute")
    public void setAttributes(TreeSet<Attribute> attributes) {
        this.attributes = attributes;
    }

    public TreeSet<Attribute> getAttributes() {
        return attributes;
    }
}
