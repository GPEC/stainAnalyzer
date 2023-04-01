/*
 * reference: http://java-sl.com/tip_adapt_label_font_size.html
 */
package ca.ubc.gpec.ia.analyzer.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
 
public class ResizeLabelFont extends JLabel {
    public static final int MIN_FONT_SIZE=3;
    public static final int MAX_FONT_SIZE=240;
    Graphics g;
 
    public ResizeLabelFont() {
        init();
    }
    
    protected void init() {
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                adaptLabelFont(ResizeLabelFont.this);
            }
        });
    }
 
    protected void adaptLabelFont(JLabel l) {
        if (g==null) {
            return;
        }
        Rectangle r=l.getBounds();
        int fontSize=MIN_FONT_SIZE;
        Font f=l.getFont();
 
        Rectangle r1=new Rectangle();
        Rectangle r2=new Rectangle();
        while (fontSize<MAX_FONT_SIZE) {
            r1.setSize(getTextSize(l, f.deriveFont(f.getStyle(), fontSize)));
            r2.setSize(getTextSize(l, f.deriveFont(f.getStyle(),fontSize+1)));
            if (r.contains(r1) && ! r.contains(r2)) {
                break;
            }
            fontSize++;
        }
 
        setFont(f.deriveFont(f.getStyle(),fontSize));
        repaint();
    }
 
    private Dimension getTextSize(JLabel l, Font f) {
        Dimension size=new Dimension();
        g.setFont(f);
        FontMetrics fm=g.getFontMetrics(f);
        size.width=fm.stringWidth(l.getText());
        size.height=fm.getHeight();
 
        return size;
    }
 
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        this.g=g;
    }
 
    public static void main(String[] args) throws Exception {
        ResizeLabelFont label=new ResizeLabelFont();
        label.setText("Some text");
        JFrame frame=new JFrame("Resize label font");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        frame.getContentPane().add(label);
 
        frame.setSize(300,300);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
 
}
