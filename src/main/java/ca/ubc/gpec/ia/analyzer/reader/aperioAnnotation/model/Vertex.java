/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.model;

import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author samuelc
 */
@XmlRootElement(name = "Vertex")
public class Vertex implements Comparable {
    public static int counter=0;
    
    private float x;
    private float y;
    private int order; // the order which this vertex is read ... assume this is the order which it appears in the xml file

    /**
     * constructor
     */
    public Vertex() {
        // do nothing
    }
    
    /**
     * deep copy
     * @return 
     */
    @Override
    public Vertex clone() {
        Vertex clone = new Vertex();
        clone.setX(this.x);
        clone.setY(this.y);
        // no need set order ... order is set during call of setX()
        return clone;
    }
    
    @XmlAttribute(name = "X")
    public void setX(float X) {
        this.x = X;
        this.order = counter;
        counter++;
    }

    public float getX() {
        return x;
    }

    @XmlAttribute(name = "Y")
    public void setY(float Y) {
        this.y = Y;
    }

    public float getY() {
        return y;
    }
    
    /**
     * for Comparable interface
     *
     * compare by order
     *
     * @param other
     * @return
     */
    @Override
    public int compareTo(Object other) {
        Vertex otherVertex = (Vertex) other;
        return this.order - otherVertex.order;
    }
}
