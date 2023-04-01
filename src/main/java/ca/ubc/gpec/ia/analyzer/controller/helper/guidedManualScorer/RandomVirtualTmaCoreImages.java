/*
 * Store the generated random TMA core images as javafx.scene.image.Image
 */
package ca.ubc.gpec.ia.analyzer.controller.helper.guidedManualScorer;

import java.util.ArrayList;
import javafx.scene.image.Image;

/**
 *
 * @author samuelc
 */
public class RandomVirtualTmaCoreImages {
    private ArrayList<Image> images;
    
    /**
     * constructor
     */
    public RandomVirtualTmaCoreImages () {
        images = new ArrayList<>();
    }
    
    /**
     * add image
     * 
     * NOTE: order is IMPORTANT ... order is kept by ArrayList
     * 
     * @param image 
     */
    public void addImage(Image image) {
        images.add(image);
    }
    
    public ArrayList<Image> getImages() {
        return images;
    }
}
