package minewalker.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Dimension;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

class CounterControl extends JPanel {
    private static final long serialVersionUID = 1L;

    private final int minimum;
    private final int maximum;
    private final int step;
    private final JLabel valueLabel = new JLabel("", SwingConstants.CENTER);
    private int value;

    CounterControl(int value, int minimum, int maximum, int step) {
        this.minimum = minimum;
        this.maximum = maximum;
        this.step = step;
        this.value = clamp(value);

        setLayout(new BorderLayout(8, 0));
        setOpaque(false);

        JButton down = arrowButton("-");
        JButton up = arrowButton("+");
        down.addActionListener(event -> setValue(this.value - this.step));
        up.addActionListener(event -> setValue(this.value + this.step));

        valueLabel.setOpaque(true);
        valueLabel.setForeground(ScreenStyles.WHITE);
        valueLabel.setBackground(ScreenStyles.PANEL);
        valueLabel.setFont(ScreenStyles.pixelFont(Font.BOLD, 18));
        valueLabel.setBorder(BorderFactory.createLineBorder(ScreenStyles.WHITE, 2));
        valueLabel.setText(Integer.toString(this.value));

        JPanel buttons = new JPanel(new GridLayout(1, 2, 4, 0));
        buttons.setOpaque(false);
        buttons.add(down);
        buttons.add(up);

        add(valueLabel, BorderLayout.CENTER);
        add(buttons, BorderLayout.EAST);
    }

    int getValue() {
        return value;
    }

    void increment() {
        setValue(value + step);
    }

    void decrement() {
        setValue(value - step);
    }

    void setValue(int value) {
        this.value = clamp(value);
        valueLabel.setText(Integer.toString(this.value));
    }

    void setSelected(boolean selected) {
        valueLabel.setBorder(BorderFactory.createLineBorder(selected ? ScreenStyles.ACCENT : ScreenStyles.WHITE, 2));
    }

    private JButton arrowButton(String text) {
        JButton button = new CounterButton(text);
        button.setFont(ScreenStyles.pixelFont(Font.BOLD, 16));
        button.setForeground(ScreenStyles.WHITE);
        button.setBackground(Color.BLACK);
        return button;
    }

    private int clamp(int value) {
        return Math.max(minimum, Math.min(maximum, value));
    }

    private static class CounterButton extends JButton {
        private static final long serialVersionUID = 1L;
        private final TextureManager textures = TextureManager.get();

        CounterButton(String text) {
            super(text);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            Dimension size = new Dimension(38, 38);
            setPreferredSize(size);
            setMinimumSize(size);
            setMaximumSize(size);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            Image sprite = "+".equals(getText()) ? textures.plus() : textures.minus();
            if (sprite != null) {
                int size = Math.min(getWidth(), getHeight());
                int x = (getWidth() - size) / 2;
                int y = (getHeight() - size) / 2;
                g.drawImage(sprite, x, y, size, size, null);
            }
            g.dispose();
            if (sprite == null) {
                super.paintComponent(graphics);
            }
        }
    }
}
