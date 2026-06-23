package com.companion.navis;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class GlassCard extends JPanel {
    private int radius;
    private Color backgroundColor;
    private boolean isHovered = false;

    public GlassCard(int radius) {
        super();
        this.radius = radius;
        // Default glass background: Surface container #122131 at ~50% opacity (130)
        this.backgroundColor = new Color(18, 33, 49, 130); 
        setOpaque(false);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHovered = true;
                repaint();
            }
            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                repaint();
            }
        });
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
        
        // Draw shadow/glow
        if (isHovered) {
            // Cyber Blue glow #38BDF8 at ~25% opacity
            g2.setColor(new Color(56, 189, 248, 60));
            for (int i = 0 ; i < shadowSize ; i++) {
                g2.drawRoundRect(i, i, getWidth() - (i * 2) - 1, getHeight() - (i * 2) - 1, radius, radius);
            }
        } else {
            g2.setColor(new Color(0, 0, 0, 40));
            for (int i = 0 ; i < shadowSize ; i++) {
                g2.drawRoundRect(i, i, getWidth() - (i * 2) - 1, getHeight() - (i * 2) - 1, radius, radius);
            }
        }

        // Fill background
        g2.setColor(backgroundColor);
        g2.fillRoundRect(shadowSize, shadowSize, getWidth() - (shadowSize * 2), getHeight() - (shadowSize * 2), radius, radius);

        // Draw border line (Solar border / outline)
        if (isHovered) {
            // Cyber Blue outline
            g2.setColor(new Color(56, 189, 248, 150));
        } else {
            // Subtly transparent white border line
            g2.setColor(new Color(255, 255, 255, 30));
        }
        g2.drawRoundRect(shadowSize, shadowSize, getWidth() - (shadowSize * 2) - 1, getHeight() - (shadowSize * 2) - 1, radius, radius);

        g2.dispose();
    }
}
