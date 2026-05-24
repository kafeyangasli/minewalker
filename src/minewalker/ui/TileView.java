package minewalker.ui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import javax.swing.JLabel;
import javax.swing.SwingConstants;

class TileView extends JLabel {
    private static final long serialVersionUID = 1L;

    private Image sprite;
    private Image overlaySprite;

    TileView() {
        super("", SwingConstants.CENTER);
        setOpaque(false);
    }

    void setSprite(Image sprite) {
        this.sprite = sprite;
        repaint();
    }

    void setOverlaySprite(Image overlaySprite) {
        this.overlaySprite = overlaySprite;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        if (sprite != null) {
            g.drawImage(sprite, 0, 0, getWidth(), getHeight(), null);
        } else {
            g.setColor(getBackground());
            g.fillRect(0, 0, getWidth(), getHeight());
        }

        if (overlaySprite != null) {
            g.drawImage(overlaySprite, 0, 0, getWidth(), getHeight(), null);
        }

        g.dispose();
        super.paintComponent(graphics);
    }
}
