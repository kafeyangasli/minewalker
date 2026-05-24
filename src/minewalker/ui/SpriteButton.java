package minewalker.ui;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Dimension;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.RoundRectangle2D;

import javax.swing.JButton;

class SpriteButton extends JButton {
    private static final long serialVersionUID = 1L;
    private static final int EXTRA_WIDTH = 88;
    private static final int EXTRA_HEIGHT = 34;
    private static final int MIN_WIDTH = 180;
    private static final int MIN_HEIGHT = 58;
    private final TextureManager textures = TextureManager.get();

    SpriteButton(String text) {
        super(text);
        setContentAreaFilled(false);
        setBorderPainted(false);
        setOpaque(false);
        setMargin(new Insets(18, 40, 18, 40));
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension base = super.getPreferredSize();
        int width = Math.max(MIN_WIDTH, base.width + EXTRA_WIDTH);
        int height = Math.max(MIN_HEIGHT, base.height + EXTRA_HEIGHT);
        return new Dimension(width, height);
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        Shape oldClip = g.getClip();
        g.setClip(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 14, 14));
        Image left = textures.buttonLeft();
        Image centerLeft = textures.buttonCenterLeft();
        Image centerRight = textures.buttonCenterRight();
        Image right = textures.buttonRight();
        int unit = Math.max(15, getHeight());
        if (left != null && centerLeft != null && centerRight != null && right != null) {
            g.drawImage(left, 0, 0, unit, getHeight(), null);
            int x = unit;
            boolean useLeftCenter = true;
            while (x < getWidth() - unit) {
                Image center = useLeftCenter ? centerLeft : centerRight;
                g.drawImage(center, x, 0, Math.min(unit, getWidth() - unit - x), getHeight(), null);
                x += unit;
                useLeftCenter = !useLeftCenter;
            }
            g.drawImage(right, getWidth() - unit, 0, unit, getHeight(), null);
        } else {
            g.setColor(ScreenStyles.PANEL);
            g.fillRect(0, 0, getWidth(), getHeight());
        }
        g.setClip(oldClip);
        g.dispose();
        super.paintComponent(graphics);
    }
}
