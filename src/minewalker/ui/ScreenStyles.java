package minewalker.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.LineBorder;

final class ScreenStyles {
    static final Color BLACK = Color.BLACK;
    static final Color GREY = new Color(135, 136,  137);
    static final Color WHITE = Color.WHITE;
    static final Color PANEL = new Color(10, 10, 12);
    static final Color GRID = new Color(64, 70, 82);
    static final Color PLAYER = new Color(66, 227, 158);
    static final Color REVEALED = new Color(26, 34, 42);
    static final Color DANGER = new Color(235, 82, 82);
    static final Color ACCENT = new Color(112, 174, 255);
    static final Color GREEN = new Color(30, 140, 60);
    static final Color RED = new Color(140, 30, 30);

    private static final Map<String, Font> FONT_CACHE = new HashMap<>();
    private static final Font MONOCRAFT = loadMonocraft();

    private ScreenStyles() {
    }

    static Font pixelFont(int style, int size) {
        String key = style + ":" + size;
        return FONT_CACHE.computeIfAbsent(key, ignored -> MONOCRAFT.deriveFont(style, (float) size));
    }

    static Border pageBorder() {
        return BorderFactory.createEmptyBorder(144, 192, 144, 192);
    }

    static Border gameBorder() {
        return BorderFactory.createEmptyBorder(36, 48, 36, 48);
    }

    static JButton button(String text) {
        JButton button = new SpriteButton(text);
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setFont(pixelFont(Font.BOLD, 18));
        button.setForeground(WHITE);
        button.setBackground(PANEL);
        button.setBorder(new CompoundBorder(new LineBorder(ACCENT, 2, true),
                BorderFactory.createEmptyBorder(14, 34, 14, 34)));
        button.setMargin(new Insets(18, 40, 18, 40));
        return button;
    }

    static JLabel label(String text, int size) {
        JLabel label = new JLabel(text);
        label.setForeground(WHITE);
        label.setFont(pixelFont(Font.PLAIN, size));
        return label;
    }

    private static Font loadMonocraft() {
        try (InputStream stream = ScreenStyles.class.getResourceAsStream("/fonts/Monocraft.ttc")) {
            if (stream != null) {
                Font font = Font.createFont(Font.TRUETYPE_FONT, stream);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
                return font;
            }
        } catch (FontFormatException | IOException ignored) {
            // Fall back to the platform monospace font if the TTC cannot be parsed.
        }
        return new Font(Font.MONOSPACED, Font.PLAIN, 12);
    }
}
