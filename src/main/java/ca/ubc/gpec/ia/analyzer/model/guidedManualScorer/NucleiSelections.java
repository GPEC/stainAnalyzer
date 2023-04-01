/*
 * contain a list of nuclei selected
 */
package ca.ubc.gpec.ia.analyzer.model.guidedManualScorer;

import static ca.ubc.gpec.ia.analyzer.model.guidedManualScorer.NucleiSelectedState.NEGATIVE;
import static ca.ubc.gpec.ia.analyzer.model.guidedManualScorer.NucleiSelectedState.POSITIVE;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author samuelc
 */
@XmlRootElement(name = "NucleiSelection")
public class NucleiSelections {

    public static final int NUCLEI_SELECTION_RADIUS = 5;
    private ArrayList<NucleiSelected> nucleiSelected;
    private int updateCount;       // keep track of how many nuclei needs to be delete when doing an update
    private int numNucleiToRemove; // keep track of how many nuclei needs to be delete when doing an update
    private String comment; // any comments from user regarding this set of nuclei selections
    private long lastModified; // record date when the nuclei selections were last modified

    public NucleiSelections() {
        nucleiSelected = new ArrayList<>();
        updateCount = 0;    // initially no nuclei needs to be updated - because no nuclei has been deleted
        numNucleiToRemove = 0; // initially no nuclei needs to be updated - because no nuclei has been deleted
        lastModified = Calendar.getInstance().getTimeInMillis();
        comment = ""; // initialize to empty String
    }

    /**
     * deep copy
     *
     * @return
     */
    @Override
    public NucleiSelections clone() {
        NucleiSelections clone = new NucleiSelections();
        for (NucleiSelected n : nucleiSelected) {
            clone.addNuclei(n.getX(), n.getY(), n.getTime(), n.getState());
            // DO NOT USE addNuclei(n) here as this does NOT clone the NucleiSelected object
        }
        clone.setLastModified(this.lastModified); // OVERWRITE lastModified timestamp so that clone has same lastModified timestamp as original
        clone.setComment(this.comment);

        return clone;
    }

    private void incrementUpdateCount() {
        updateCount++;
    }

    private void decrementUpdateCount() {
        updateCount--;
        if (updateCount < numNucleiToRemove) {
            numNucleiToRemove = updateCount;
        }
    }

    /**
     * reset updateCount AND numNucleiToRemove - for initial parsing of
     * nucleiSelectionParamString in Applet - these nuclei were previous
     * selected and already recorded in database therefore need to make sure
     * don't recount them for update
     */
    public void resetUpdateCount() {
        updateCount = 0;
        numNucleiToRemove = 0;
    }

    /**
     * reset nuclei selection i.e. remove all selections!!!
     * - this method updates lastModified timestamp
     */
    public void reset() {
        while (getNumTotal() > 0) {
            this.removeNuclei();
        }
        lastModified = Calendar.getInstance().getTimeInMillis(); // record time
    }

    /**
     * add a selected nuclei to the list - creating NucleiSelected objects
     * - this method updates lastModified timestamp
     * @param x
     * @param y
     * @param time
     * @param state
     */
    public void addNuclei(int x, int y, long time, NucleiSelectedState state) {
        nucleiSelected.add(new NucleiSelected(x, y, time, state));
        lastModified = Calendar.getInstance().getTimeInMillis(); // record time
        incrementUpdateCount();
    }

    /**
     * add nuclei - does not create new NucleiSelected objects
     * - this method updates lastModified timestamp
     * @param nuclei
     */
    public void addNuclei(NucleiSelected nuclei) {
        nucleiSelected.add(nuclei);
        lastModified = Calendar.getInstance().getTimeInMillis(); // record time
        incrementUpdateCount();
    }

    /**
     * remove the last selected nuclei i.e. undo nuclei selection
     *  - this method updates lastModified timestamp
     */
    public void removeNuclei() {
        int size = nucleiSelected.size();
        if (size > 0) {
            nucleiSelected.remove(size - 1);
            lastModified = Calendar.getInstance().getTimeInMillis(); // record time
            decrementUpdateCount();
        }
    }

    @XmlElement(name = "Comment")
    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getComment() {
        return comment == null ? "" : comment;
    }

    /**
     * for reading/writing XML file ONLY
     * @param lastModified 
     */
    @XmlElement(name = "lastModified")
    public void setLastModified(long lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * for reading/writing XML file ONLY
     * @return 
     */
    public long getLastModified() {
        return lastModified;
    }

    /**
     * set the array of nucleiSelected
     * - this method DOES NOT updates lastModified timestamp
     *   This method is used by the parser when reading annotation file ... 
     *   therefore, if this method updates lastModified, it will interfere with the
     *   lastModified field.
     * - therefore DO NOT use this method without explicitly updating lastModified field
     * @param nucleiSelected
     */
    @XmlElement(name = "NucleiSelected")
    public void setNucleiSelected(ArrayList<NucleiSelected> nucleiSelected) {
        this.nucleiSelected = nucleiSelected;
    }

    /**
     * return a list of selected nuclei
     *
     * @return
     */
    public ArrayList<NucleiSelected> getNucleiSelected() {
        return nucleiSelected;
    }

    /**
     * get number of positive nuclei selected
     *
     * @return
     */
    public int getNumPositive() {
        int numPositive = 0;
        for (NucleiSelected n : nucleiSelected) {
            switch (n.getState()) {
                case POSITIVE:
                    numPositive++;
                    break;
                case NEGATIVE:
                    // do nothing
                    break;
            }
        }
        return numPositive;
    }

    /**
     * get number of negative nuclei selected
     *
     * @return
     */
    public int getNumNegative() {
        int numNegative = 0;
        for (NucleiSelected n : nucleiSelected) {
            switch (n.getState()) {
                case POSITIVE:
                    // do nothing
                    break;
                case NEGATIVE:
                    numNegative++;
                    break;
            }
        }
        return numNegative;
    }

    /**
     * return total number of nuclei selected
     *
     * @return
     */
    public int getNumTotal() {
        return nucleiSelected.size();
    }

    /**
     * return number of nuclei to remove on the server - return a positive
     * number (even though numNucleiToRemove is recorded as a negative number
     * here)
     *
     * @return
     */
    public int getNumNucleiToRemove() {
        return 0 - numNucleiToRemove;
    }

    /**
     * return the number of newly selected nuclei to be added to the server
     *
     * @return
     */
    public int getNumNucleiToAdd() {
        return updateCount - numNucleiToRemove;
    }

    /**
     * print content of Nuclei Selection
     *
     * @return
     */
    public String toString() {
        String result = "";
        Iterator<NucleiSelected> itr = nucleiSelected.iterator();
        while (itr.hasNext()) {
            result = result + itr.next().toString() + "\n";
        }
        return result;
    }
}
