/*
 * vertex calcuations e.g. distance between two vertices
 */
package ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.util;

import ca.ubc.gpec.ia.analyzer.reader.aperioAnnotation.model.Vertex;

/**
 *
 * @author samuelc
 */
public class VertexArithmetic {

    /**
     * calculate euclideanDistance between two vertices
     *
     * @param v1
     * @param v2
     * @return
     */
    public static int euclideanDistance(Vertex v1, Vertex v2) {
        return (int) Math.round(Math.sqrt(
                (v1.getX() - v2.getX()) * (v1.getX() - v2.getX())
                + (v1.getY() - v2.getY()) * (v1.getY() - v2.getY())));
    }
}
