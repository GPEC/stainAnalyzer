/*
 * customized tooltip
 */
package ca.ubc.gpec.ia.analyzer.views;

import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

/**
 *
 * @author samuelc
 */
public class MyTooltip extends Tooltip {

    public MyTooltip(String text) {
        super(text);
        Image img = new Image(this.getClass().getResource(ViewConstants.RESOURCE_SEPARATOR + "images" + ViewConstants.RESOURCE_SEPARATOR + "dialog_notice.png").toExternalForm());
        ImageView imgV = new ImageView(img);
        imgV.setFitHeight(30);
        imgV.setFitWidth(30);
        setGraphic(imgV);
        getStyleClass().add(ViewConstants.DEFAULT_CSS_THEME_NAME);
        if (text.length() > 80) {
            setPrefWidth(ViewConstants.TOOLTIP_LONG_WIDTH); // setMaxWidth doesn't seems to have any effect !!!
        } else {
            setPrefWidth(ViewConstants.TOOLTIP_SHORT_WIDTH);
        }
        setWrapText(true);
    }
}
