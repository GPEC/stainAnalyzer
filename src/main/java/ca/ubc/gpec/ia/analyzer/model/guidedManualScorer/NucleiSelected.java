/*
 * a nuclei being selected
 */
package ca.ubc.gpec.ia.analyzer.model.guidedManualScorer;

import javafx.geometry.Rectangle2D;
import jakarta.xml.bind.annotation.XmlAttribute;

/**
 *
 * @author samuelc
 */
public class NucleiSelected {

    public static final long NUCLEI_TIME_NOT_SET = -1;
    public static final int X_NOT_SET = -1;
    public static final int Y_NOT_SET = -1;
    private int x; // x coordinate
    private int y; // y coordinate
    private long time; // time in milliseconds when this nuclei is selected
    // NOTE: '-1' indicates NOT SET
    //       time in MILLIseconds even though on the www.gpecdata.med.ubc.ca/tmadb database
    //       time is only stored in seconds
    //       also, NucleiSelectionParamString, time is in seconds only
    private NucleiSelectedState state; // whether it is positive or negative

    /**
     * default constructor with no arguments
     * - needed for Unmarshaller
     */
    public NucleiSelected() {
        // do nothing
    }
    
    public NucleiSelected(int x, int y, long time, NucleiSelectedState state) {
        this.x = x;
        this.y = y;
        this.state = state;
        this.time = time;
    }

    /**
     * deep copy
     *
     * @return
     */
    @Override
    public NucleiSelected clone() {
        NucleiSelected clone = new NucleiSelected(
                this.x,
                this.y,
                this.time,
                this.state);
        return clone;
    }

    @XmlAttribute(name = "X")
    public void setX(int x) {
        this.x = x;
    }

    public int getX() {
        return x;
    }

    @XmlAttribute(name = "Y")
    public void setY(int y) {
        this.y = y;
    }

    public int getY() {
        return y;
    }

    @XmlAttribute(name = "State")
    public void setState(NucleiSelectedState state) {
        this.state = state;
    }

    public NucleiSelectedState getState() {
        return state;
    }

    @XmlAttribute(name = "Time")
    public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    /**
     * determine if this nuclei is in view i.e. do I need to draw it
     *
     * @param viewWindow
     * @return
     */
    public boolean inView(Rectangle2D viewport) {
        return (viewport.getMaxX() <= x
                & viewport.getMinX() >= x
                & viewport.getMaxY() <= y
                & viewport.getMinY() >= y);
    }

    /**
     * print content of this Nuclei
     *
     * @return
     */
    public String toString() {
        return "x=" + x + ",y=" + y + ",time=" + time + ",state=" + state;
    }
}
