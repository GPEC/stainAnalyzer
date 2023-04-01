/*
 * store some info for previewing virtual slide and make sure
 * necessary info are on the annotation file
 */
package ca.ubc.gpec.ia.analyzer.controller.helper.guidedManualScorer;

import ca.ubc.gpec.ia.analyzer.reader.VirtualSlideReader;
import java.awt.image.BufferedImage;

/**
 *
 * @author samuelc
 */
public class PreviewVirtualSlideInfo {
    private BufferedImage label;
    private BufferedImage selectionThumbWithRoi;
    private VirtualSlideReader virtualSlideReader;

    public PreviewVirtualSlideInfo(
            BufferedImage label, 
            BufferedImage selectionThumbWithRoi, 
            VirtualSlideReader virtualSlideReader) {
        this.label = label;
        this.selectionThumbWithRoi = selectionThumbWithRoi;
        this.virtualSlideReader = virtualSlideReader;
    }
    
    public void setLabel(BufferedImage label) {
        this.label = label;
    }
    
    public BufferedImage getLabel() {
        return label;
    }
    
    public VirtualSlideReader getVirtualSlideReader() {
        return virtualSlideReader;
    }
    
    public void setSelectionThumbWithRoi(BufferedImage setSelectionThumbWithRoi) {
        this.selectionThumbWithRoi = setSelectionThumbWithRoi;
    }
    
    public BufferedImage getSetSelectionThumbWithRoi() {
        return selectionThumbWithRoi;
    }
        
}
