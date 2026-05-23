package minewalker.ui;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;

class KeyboardItem implements KeyboardSelector.SelectableItem {
    private final JLabel label;
    private final JButton button;
    private final Runnable action;

    KeyboardItem(JLabel label, Runnable action) {
        this.label = label;
        this.button = null;
        this.action = action;
    }

    KeyboardItem(JButton button, Runnable action) {
        this.label = null;
        this.button = button;
        this.action = action;
    }

    @Override
    public void setSelected(boolean selected) {
        if (label != null) {
            label.setForeground(selected ? ScreenStyles.ACCENT : ScreenStyles.WHITE);
        }
        if (button != null) {
            button.setBorder(BorderFactory.createLineBorder(selected ? ScreenStyles.WHITE : ScreenStyles.ACCENT, 2));
            button.setForeground(selected ? ScreenStyles.ACCENT : ScreenStyles.WHITE);
        }
    }

    @Override
    public void activate() {
        action.run();
    }
}
