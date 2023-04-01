/*
 * reference: http://stackoverflow.com/questions/299495/java-swing-how-to-add-an-image-to-a-jpanel?rq=1
 * 
 */
package ca.ubc.gpec.ia.analyzer.gui;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;

public class ImageJPanel extends JPanel {

    private BufferedImage image;

    public ImageJPanel() {
        image = null;
    }

    public void setImage(BufferedImage image) {
        this.image = image;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int width = (int) getPreferredSize().getWidth();
        if (image != null) {
            g.drawImage(image, 0, 0, width, width * image.getHeight() / image.getWidth(), null);
        }
    }
}
