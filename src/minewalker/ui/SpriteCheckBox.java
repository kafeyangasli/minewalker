package minewalker.ui;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;

import javax.swing.JCheckBox;

class SpriteCheckBox extends JCheckBox {
    private static final long serialVersionUID = 1L;
    private static final int BOX_SIZE = 30;
    private final TextureManager textures = TextureManager.get();

    SpriteCheckBox(String text, boolean selected) {
        super(text, selected);
        setOpaque(false);
        setIcon(null);
        setSelectedIcon(null);
        setIconTextGap(12);
        setFont(ScreenStyles.pixelFont(Font.PLAIN, 18));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        Image box = isSelected() ? textures.checkboxChecked() : textures.checkboxEmpty();
        if (box != null) {
            int boxY = (getHeight() - BOX_SIZE) / 2;
            g.drawImage(box, 0, boxY, BOX_SIZE, BOX_SIZE, null);
            g.setFont(getFont());
            g.setColor(getForeground());
            FontMetrics metrics = g.getFontMetrics();
            int textY = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
            g.drawString(getText(), BOX_SIZE + getIconTextGap(), textY);
        } else {
            super.paintComponent(g);
        }
        g.dispose();
    }
}
