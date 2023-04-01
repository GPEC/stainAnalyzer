package ca.ubc.gpec.ia.stitch.bliss.util;

/**
 * specifies coordinates of Da files - assume pixel index starts at 0
 *
 * @author sleung
 *
 */
public class StitchedCoordinates {

    private int minX; //min x (inclusive) coordinates of stitched file
    private int maxX; //max x (inclusive) coordinates of stitched file
    private int minY; //min Y (inclusive) coordinates of stitched file
    private int maxY; //max Y (inclusive) coordinates of stitched file
    private int xRange; // width of da file in pixel
    private int yRange; // height of da file in pixel 

    /**
     * constructor
     *
     * @param minX
     * @param maxX
     * @param minY
     * @param maxY
     */
    StitchedCoordinates(int minX, int maxX, int minY, int maxY, int xRange, int yRange) {
        this.minX = minX;
        this.maxX = maxX;
        this.minY = minY;
        this.maxY = maxY;
        this.xRange = xRange;
        this.yRange = yRange;
    }

    protected int getMinX() {
        return minX;
    }

    protected void setMinX(int minX) {
        this.minX = minX;
    }

    protected int getMaxX() {
        return maxX;
    }

    protected void setMaxX(int maxX) {
        this.maxX = maxX;
    }

    protected int getMinY() {
        return minY;
    }

    protected void setMinY(int minY) {
        this.minY = minY;
    }

    protected int getMaxY() {
        return maxY;
    }

    protected void setMaxY(int maxY) {
        this.maxY = maxY;
    }

    public String toString() {
        return "minX=" + minX + "|maxX=" + maxX + "|minY=" + minY + "|maxY=" + maxY + "|xRange=" + xRange + "|yRange=" + yRange;
    }

    /**
     * if other's x/y min/max coordinates is within this x/y min/max coordinates
     *
     * @param other
     * @return
     */
    protected boolean inRange(StitchedCoordinates other) {
        if (this.minX <= other.minX
                & this.maxX >= other.maxX
                & this.minY <= other.minY
                & this.maxY >= other.maxY) {
            return true;
        } else {
            //System.out.println(other+" compare to self: "+this);
            return false;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        StitchedCoordinates other = (StitchedCoordinates) obj;
        if (!inRange(other)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        //result = prime * result + ((minY - (minY % yRange))/yRange); // so that pixel in same da file will be have same hashcode
        //result = prime * result + ((minX - (minX % xRange))/xRange); // so that pixel in same da file will be have same hashcode
        return result;
    }
}