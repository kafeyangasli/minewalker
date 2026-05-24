package minewalker.ui;

import java.awt.Font;
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
        setIconTextGap(12);
        setFont(ScreenStyles.pixelFont(Font.PLAIN, 18));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        Image box = isSelected() ? textures.checkboxChecked() : textures.checkboxEmpty();
        if (box != null) {
            g.drawImage(box, 0, (getHeight() - BOX_SIZE) / 2, BOX_SIZE, BOX_SIZE, null);
            g.translate(BOX_SIZE + getIconTextGap(), 0);
            super.paintComponent(g);
        } else {
            super.paintComponent(g);
        }
        g.dispose();
    }
}
