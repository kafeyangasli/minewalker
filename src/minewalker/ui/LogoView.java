package minewalker.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import javax.swing.JComponent;

class LogoView extends JComponent {
    private static final long serialVersionUID = 1L;
    private static final int SPRITE_SIZE = 31;
    private static final int COLUMNS = 6;
    private static final int ROWS = 1;

    private final TextureManager textures = TextureManager.get();
    private float alpha = 1f;

    LogoView(int scale) {
        int width = SPRITE_SIZE * COLUMNS * scale;
        int height = SPRITE_SIZE * ROWS * scale;
        Dimension size = new Dimension(width, height);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(size);
        setOpaque(false);
    }

    void setAlpha(float alpha) {
        this.alpha = Math.max(0f, Math.min(1f, alpha));
        repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        g.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, alpha));

        Image[] logo = {
            textures.logo1Left(), textures.logo1Right(),
            textures.logo2Left(), textures.logo2Right(),
            textures.logo3Left(), textures.logo3Right()
        };

        int cell = Math.min(getWidth() / COLUMNS, getHeight() / ROWS);
        int startX = (getWidth() - cell * COLUMNS) / 2;
        int startY = (getHeight() - cell * ROWS) / 2;
        for (int column = 0; column < COLUMNS; column++) {
            Image image = logo[column];
            if (image != null) {
                g.drawImage(image, startX + column * cell, startY, cell, cell, null);
            }
        }
        g.dispose();
    }
}
