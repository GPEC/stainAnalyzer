/*
 * Aperio annotation XML file ... RegionAttributeHeaders element
 */
package ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.model;

import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.exception.RegionAttributeHeaderDescriptionNotFoundException;
import java.util.TreeSet;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author samuelc
 */
@XmlRootElement(name = "RegionAttributeHeaders")
public class RegionAttributeHeaders {

    TreeSet<AttributeHeader> attributeHeaders;

    /**
     * constructor
     */
    public RegionAttributeHeaders() {
        // do nothing
    }
    
    @Override
    public RegionAttributeHeaders clone() {
        RegionAttributeHeaders clone = new RegionAttributeHeaders();
        TreeSet<AttributeHeader> cloneAttributeHeaders = new TreeSet<>();
        for (AttributeHeader a:attributeHeaders) {
            cloneAttributeHeaders.add(a.clone());
        }
        clone.setAttributeHeaders(cloneAttributeHeaders);
        return clone;
    }

    @XmlElement(name = "AttributeHeader")
    public void setAttributeHeaders(TreeSet<AttributeHeader> attributeHeaders) {
        this.attributeHeaders = attributeHeaders;
    }

    public TreeSet<AttributeHeader> getAttributeHeaders() {
        return attributeHeaders;
    }
    
    /**
     * search through the AttributeHeader and find out id of description
     * 
     * return null if "Description" not found
     * 
     * @return 
     */
    public int getDescriptionId() throws RegionAttributeHeaderDescriptionNotFoundException {
        for (AttributeHeader a:attributeHeaders) {
            if(a.getName().equals("Description")) {
                return a.getId();
            }
        }
        throw new RegionAttributeHeaderDescriptionNotFoundException("");
    }
}
