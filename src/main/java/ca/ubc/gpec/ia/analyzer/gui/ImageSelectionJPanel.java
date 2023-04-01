/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.ubc.gpec.ia.analyzer.gui;

import ca.ubc.gpec.ia.analyzer.model.IAO;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.TreeSet;

/**
 *
 * @author samuelc
 */
public class ImageSelectionJPanel extends javax.swing.JPanel {

    /**
     * Creates new form ImageSelectionJPanel
     */
    public ImageSelectionJPanel() {
        initComponents();
    }
        
    /**
     * check stainAnalyzerController and update available IAOs
     * 
     * @param iaos
     * @throws IOException 
     */
    public void update(TreeSet<IAO> iaos) throws IOException, URISyntaxException {
        multiImageSelectJPanel.clearIAOs();
        for (IAO iao:iaos) {
            multiImageSelectJPanel.addIAO(iao);
        }
    }
    
    /**
     * get selected IAOs
     * @return 
     */
    public TreeSet<IAO> getSelectedIAOs() {
        return multiImageSelectJPanel.getSelectedIAOs();
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        imageSelectJScrollPane = new javax.swing.JScrollPane();
        multiImageSelectJPanel = new ca.ubc.gpec.ia.analyzer.gui.MultiImageSelectJPanel();

        jLabel1.setText("Available image(s) for analysis");

        javax.swing.GroupLayout multiImageSelectJPanelLayout = new javax.swing.GroupLayout(multiImageSelectJPanel);
        multiImageSelectJPanel.setLayout(multiImageSelectJPanelLayout);
        multiImageSelectJPanelLayout.setHorizontalGroup(
            multiImageSelectJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 378, Short.MAX_VALUE)
        );
        multiImageSelectJPanelLayout.setVerticalGroup(
            multiImageSelectJPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 256, Short.MAX_VALUE)
        );

        imageSelectJScrollPane.setViewportView(multiImageSelectJPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(imageSelectJScrollPane)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(0, 235, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(imageSelectJScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane imageSelectJScrollPane;
    private javax.swing.JLabel jLabel1;
    private ca.ubc.gpec.ia.analyzer.gui.MultiImageSelectJPanel multiImageSelectJPanel;
    // End of variables declaration//GEN-END:variables
}
