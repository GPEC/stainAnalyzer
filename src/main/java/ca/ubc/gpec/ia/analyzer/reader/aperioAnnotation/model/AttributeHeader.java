/*
 * Aperio annotation XML file ... AttributeHeader element
 */
package ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.model;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author samuelc
 */
@XmlRootElement(name = "AttributeHeader")
public class AttributeHeader implements Comparable<AttributeHeader> {

    private int id;
    private String name;
    private int columnWidth;

    /**
     * constructor
     */
    public AttributeHeader() {
        // do nothing
    }

    public AttributeHeader clone() {
        AttributeHeader clone = new AttributeHeader();
        clone.setId(this.id);
        clone.setName(this.name);
        clone.setColumnWidth(this.columnWidth);
        return clone;
    }
    
    /**
     * for comparable interface
     * @param other
     * @return 
     */
    @Override
    public int compareTo(AttributeHeader other) {
        // sort by id (descending)
        return other.id - id;
    }
    
    @XmlAttribute(name = "Id")
    public void setId(int id) {
        this.id = id;
    }
    
    public int getId() {return id;}

    @XmlAttribute(name = "Name")
    public void setName(String name) {
        this.name = name;
    }
    
    public String getName() {return name;}

    @XmlAttribute(name = "ColumnWidth")
    public void setColumnWidth(int columnWidth) {
        this.columnWidth = columnWidth;
    }
    
    public int getColumnWidth() {return columnWidth;}
}
