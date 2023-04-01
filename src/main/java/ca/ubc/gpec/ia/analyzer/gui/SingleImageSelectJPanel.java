/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.ubc.gpec.ia.analyzer.gui;

import ca.ubc.gpec.ia.analyzer.model.IAO;
import ca.ubc.gpec.ia.analyzer.model.ImageDescriptor;
import ij.ImagePlus;
import ij.io.Opener;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.swing.JFrame;
import loci.formats.FormatException;

/**
 *
 * @author samuelc
 */
public class SingleImageSelectJPanel extends javax.swing.JPanel implements Comparable<SingleImageSelectJPanel> {

    public static final int DEFAULT_OFFSET_X = 35;
    public static final int DEFAULT_OFFSET_Y = 10;
    public static final int DEFAULT_IMAGE_WIDTH = 110;
    public static final int DEFAULT_PREFERRED_HEIGHT = 160;
    // image analyses object to be selected
    private IAO iao;
    private BufferedImage image;

    /**
     * Creates new form SingleImageSelectJPanel
     */
    public SingleImageSelectJPanel() {
        initComponents();
        imageJPanel.setPreferredSize(new Dimension(DEFAULT_IMAGE_WIDTH, DEFAULT_IMAGE_WIDTH));
    }

    /**
     * for Comparable interface
     *
     * @param other
     * @return
     */
    @Override
    public int compareTo(SingleImageSelectJPanel other) {
        return this.iao.compareTo(other.getIAO());
    }

    /**
     * set IAO
     *
     * @param iao
     */
    public void setIAO(IAO iao) throws IOException, URISyntaxException {
        this.iao = iao;
        // get image
        System.out.println(iao.getImageDescriptors().first().getUrl());
        image = iao.getImageDescriptors().first().getBufferedImage();
        filenameJLabel.setText(Utils.extractFileName(Utils.extractFileName(iao.getImageDescriptors().first().getURL().getFile())));
        imageJPanel.setImage(image);
        this.setToolTipText(iao.getImageDescriptors().first().getURL().getFile());
        if (image == null) {
            System.out.println("image is null");
            System.out.println(iao.getImageDescriptors().first().getURL().getFile());
        } else {
            System.out.println("image NOT null");
        }
        this.setPreferredSize(
                new Dimension(
                (int) Math.round(1.5 * DEFAULT_OFFSET_X) + Math.max((int) filenameJLabel.getSize().getWidth(), DEFAULT_IMAGE_WIDTH),
                Math.max(DEFAULT_PREFERRED_HEIGHT, 2 * DEFAULT_OFFSET_Y + DEFAULT_IMAGE_WIDTH * image.getHeight() / image.getWidth())));
        this.repaint();
    }

    /**
     * get IAO
     *
     * @return
     */
    public IAO getIAO() {
        return iao;
    }

    /**
     * main method for process testing
     *
     * @param args
     */
    public static void main(String[] args) {
        JFrame.setDefaultLookAndFeelDecorated(true);
        JFrame frame = new JFrame();
        frame.setTitle("testing SingleImageSelectJPanel ...");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        SingleImageSelectJPanel singleImageSelectJPanel = new SingleImageSelectJPanel();
        frame.add(singleImageSelectJPanel);
        IAO iao = new IAO();
        iao.addImageDescriptor(new ImageDescriptor("file:/C:/Users/samuelc/Documents/GPEC/imagej/test ground/02-008_Her2-SP3_E12_v3_s10_062_r6c11.jpg"));
        try {
            singleImageSelectJPanel.setIAO(iao);
        } catch (Exception e) {
            System.err.println(e);
        }
        frame.pack();
        frame.setVisible(true);

    }

    /**
     * return if this image is selected
     * @return 
     */
    public boolean isSelected() {
        return imageSelectCheckBox.isSelected();
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        imageSelectCheckBox = new javax.swing.JCheckBox();
        filenameJLabel = new ca.ubc.gpec.ia.analyzer.gui.ResizeLabelFont();
        imageJPanel = new ca.ubc.gpec.ia.analyzer.gui.ImageJPanel();

        filenameJLabel.setText("resizeLabelFont1");

        javax.swing.GroupLayout imageJPanelLayout = new javax.swing.GroupLayout(imageJPanel);
        imageJPanel.setLayout(imageJPanelLayout);
        imageJPanelLayout.setHorizontalGroup(
            imageJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        imageJPanelLayout.setVerticalGroup(
            imageJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(imageSelectCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(imageJPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 7, Short.MAX_VALUE))
                    .addComponent(filenameJLabel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(imageSelectCheckBox)
                    .addComponent(imageJPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(filenameJLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 27, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private ca.ubc.gpec.ia.analyzer.gui.ResizeLabelFont filenameJLabel;
    private ca.ubc.gpec.ia.analyzer.gui.ImageJPanel imageJPanel;
    private javax.swing.JCheckBox imageSelectCheckBox;
    // End of variables declaration//GEN-END:variables
}
