/*
 * Aperio annotation XML file ... Vertices element
 */
package ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.model;

import java.util.TreeSet;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author samuelc
 */
@XmlRootElement(name = "Vertices")
public class Vertices {

    TreeSet<Vertex> vertices;

    /**
     * constructor
     */
    public Vertices() {
        vertices = new TreeSet<Vertex>();
    }

    /**
     * deep copy
     *
     * @return
     */
    @Override
    public Vertices clone() {
        TreeSet<Vertex> cloneVertices = new TreeSet<Vertex>();
        for (Vertex vertex : vertices) {
            cloneVertices.add(vertex.clone());
        }
        Vertices clone = new Vertices();
        clone.setVertices(cloneVertices);
        return clone;
    }

    @XmlElement(name = "Vertex")
    public void setVertices(TreeSet<Vertex> vertices) {
        this.vertices = vertices;
    }

    public TreeSet<Vertex> getVertices() {
        return vertices;
    }

    /**
     * return first vertex
     *
     * @return
     */
    public Vertex getFirstVertex() {
        return vertices.first();
    }

    /**
     * return last vertex
     *
     * @return
     */
    public Vertex getLastVertex() {
        return vertices.last();
    }

    /**
     * check to see if vertices is empty
     *
     * @return
     */
    public boolean isEmpty() {
        return vertices.isEmpty();
    }

    /**
     * remove last vertex
     */
    public void removeLastVertex() {
        vertices.remove(vertices.last());
    }

    /**
     * add vertex to END of vertices
     *
     * @param vertex
     */
    public void addVertex(Vertex vertex) {
        // clone() makes sure getX() is called, hence the order field is updated
        vertices.add(vertex.clone());
    }

    /**
     * add vertices to end of vertices
     *
     * @param inputVertices
     */
    public void appendVertices(Vertices inputVertices) {
        for (Vertex vertex : inputVertices.getVertices()) {
            // need to clone because want to update the "order" field
            // in vertex ... this is done in setX() in Vertex
            addVertex(vertex);
        }
    }

    /**
     * add vertices to end of vertices, reverse inputVertices
     *
     * @param inputVertices
     */
    public void appendVerticesReverse(Vertices inputVertices) {
        Vertices tempVertices = inputVertices.clone();
        while (!tempVertices.isEmpty()) {
            addVertex(tempVertices.getLastVertex());
            tempVertices.removeLastVertex();
        }
    }

    /**
     * add vertices to beginning (front) of vertices
     *
     * @param inputVertices
     */
    public void prependVertices(Vertices inputVertices) {
        Vertices newVertices = inputVertices.clone();
        for (Vertex vertex : vertices) {
            newVertices.addVertex(vertex);
        }
        vertices = newVertices.getVertices(); // change the pointer!!!
    }

    /**
     * add vertices to beginning (front) of vertices, reverse inputVertices
     *
     * @param inputVertices
     */
    public void prependVerticesReverse(Vertices inputVertices) {
        Vertices newVertices = new Vertices();
        Vertices tempVertices = inputVertices.clone();
        while (!tempVertices.isEmpty()) {
            newVertices.addVertex(tempVertices.getLastVertex());
            tempVertices.removeLastVertex();
        }
        for (Vertex vertex : vertices) {
            newVertices.addVertex(vertex);
        }
        vertices = newVertices.getVertices(); // change the pointer!!!
    }
}
