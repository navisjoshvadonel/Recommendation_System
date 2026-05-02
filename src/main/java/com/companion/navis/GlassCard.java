package com.companion.navis;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

public class GlassCard extends JPanel {
    private int radius;
    private Color backgroundColor;

    public GlassCard(int radius) {
        super();
        this.radius = radius;
        this.backgroundColor = new Color(255, 255, 255, 220); 
        setOpaque(false);
    }
    
    public void setBackgroundColor(Color color) {
        this.backgroundColor = color;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int shadowSize = 5;
        
        g2.setColor(new Color(0, 0, 0, 30));
        for (int i = 0 ; i < shadowSize ; i++) {
        	g2.drawRoundRect(i, i, getWidth() - (i * 2) - 1, getHeight() - (i * 2) - 1, radius, radius);
        }

        g2.setColor(backgroundColor);
        g2.fillRoundRect(shadowSize, shadowSize, getWidth() - (shadowSize * 2), getHeight() - (shadowSize * 2), radius, radius);

        g2.setColor(new Color(207, 250, 254, 150));
        g2.drawRoundRect(shadowSize, shadowSize, getWidth() - (shadowSize * 2) - 1, getHeight() - (shadowSize * 2) - 1, radius, radius);

        g2.dispose();
    }
}
