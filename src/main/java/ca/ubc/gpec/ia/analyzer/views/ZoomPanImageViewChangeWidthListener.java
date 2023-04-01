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
public class ZoomPanImageViewChangeWidthListener implements ChangeListener<Number> {

    private ZoomPanImageView imageView;

    public ZoomPanImageViewChangeWidthListener(ZoomPanImageView imageView) {
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
            double newViewportWidth = currViewport.getWidth() * (newValue.doubleValue()-ViewConstants.COUNTER_CARTOON_WIDTH) / (oldValue.doubleValue()-ViewConstants.COUNTER_CARTOON_WIDTH);

            imageView.setViewport(new Rectangle2D(
                    currViewport.getMinX() - 0.5d * (newViewportWidth - currViewport.getWidth()),
                    currViewport.getMinY(),
                    newViewportWidth,
                    currViewport.getHeight()));
            imageView.setFitWidth(newValue.doubleValue() - ViewConstants.COUNTER_CARTOON_WIDTH);
        }
    }
}
