package minewalker.ui;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

class KeyboardSelector {
    private final List<SelectableItem> items = new ArrayList<>();
    private int selectedIndex;

    void add(SelectableItem item) {
        items.add(item);
        refresh();
    }

    void bindTo(JComponent component) {
        bind(component, "UP", this::previous);
        bind(component, "W", this::previous);
        bind(component, "DOWN", this::next);
        bind(component, "S", this::next);
        bind(component, "ENTER", this::activate);
        bind(component, "SPACE", this::activate);
    }

    void next() {
        if (items.isEmpty()) {
            return;
        }
        selectedIndex = (selectedIndex + 1) % items.size();
        refresh();
    }

    void previous() {
        if (items.isEmpty()) {
            return;
        }
        selectedIndex = (selectedIndex - 1 + items.size()) % items.size();
        refresh();
    }

    void activate() {
        if (!items.isEmpty()) {
            items.get(selectedIndex).activate();
        }
    }

    int selectedIndex() {
        return selectedIndex;
    }

    void refresh() {
        for (int index = 0; index < items.size(); index++) {
            items.get(index).setSelected(index == selectedIndex);
        }
    }

    private void bind(JComponent component, String key, Runnable action) {
        component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key), key);
        component.getActionMap().put(key, new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent event) {
                action.run();
            }
        });
    }

    interface SelectableItem {
        void setSelected(boolean selected);

        void activate();
    }
}
