/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.ubc.gpec.ia.analyzer.views;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Rectangle2D;

/**
 *
 * @author samuelc
 */
public class ZoomPanImageViewChangeHeightListener implements ChangeListener<Number> {

    private ZoomPanImageView imageView;

    public ZoomPanImageViewChangeHeightListener(ZoomPanImageView imageView) {
        this.imageView = imageView;
    }

    @Override
    public void changed(ObservableValue<? extends Number> observable,
            Number oldValue,
            Number newValue) {
        if (!Double.isNaN(oldValue.doubleValue())) { // just ingore if oldValue not available
            // get dimension of the current viewport of imageView
            Rectangle2D currViewport = imageView.getViewport();
            // force imageView to try the new viewport
            double newViewportHeight = currViewport.getHeight() * newValue.doubleValue()/oldValue.doubleValue();

            imageView.zoom(0, new Rectangle2D(
                    currViewport.getMinX(),
                    currViewport.getMinY() - 0.5d * (newViewportHeight - currViewport.getHeight()),
                    currViewport.getWidth(),
                    newViewportHeight));

            imageView.setFitWidth(imageView.stage.getWidth() - ViewConstants.COUNTER_CARTOON_WIDTH);
        }
    }
}
