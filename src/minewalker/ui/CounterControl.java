package minewalker.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

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
        valueLabel.setBorder(BorderFactory.createLineBorder(ScreenStyles.GRID, 2));
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
        valueLabel.setBorder(BorderFactory.createLineBorder(selected ? ScreenStyles.ACCENT : ScreenStyles.GRID, 2));
    }

    private JButton arrowButton(String text) {
        JButton button = ScreenStyles.button(text);
        button.setFont(ScreenStyles.pixelFont(Font.BOLD, 16));
        button.setForeground(ScreenStyles.WHITE);
        button.setBackground(Color.BLACK);
        return button;
    }

    private int clamp(int value) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
