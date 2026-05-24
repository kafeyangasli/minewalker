package minewalker.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import minewalker.audio.MusicManager;
import minewalker.model.GameSettings;
import minewalker.persistence.GameStorage;

public class ConfigureMinesPanel extends JPanel {
    public ConfigureMinesPanel(GameSettings initialSettings, Consumer<GameSettings> startGame, Runnable back,
            GameStorage storage, MusicManager musicManager) {
        setLayout(new BorderLayout(20, 20));
        setBackground(ScreenStyles.BLACK);
        setBorder(ScreenStyles.pageBorder());
        setFocusable(true);

        JLabel title = new JLabel("Configure Tiles", SwingConstants.CENTER);
        title.setForeground(ScreenStyles.WHITE);
        title.setFont(ScreenStyles.pixelFont(Font.BOLD, 46));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        add(title, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridLayout(0, 2, 14, 14));
        fields.setOpaque(false);

        CounterControl rows = new CounterControl(initialSettings.getRows(), 5, 24, 1);
        CounterControl columns = new CounterControl(initialSettings.getColumns(), 5, 30, 1);
        SpriteSlider mines = new SpriteSlider(5, 45, initialSettings.getMinePercentage());
        mines.setMajorTickSpacing(10);
        mines.setPaintTicks(true);
        mines.setPaintLabels(true);
        mines.setOpaque(false);
        mines.setForeground(ScreenStyles.WHITE);
        mines.setBackground(ScreenStyles.BLACK);
        SpriteCheckBox timer = new SpriteCheckBox("TIMER ENABLED", initialSettings.isTimerEnabled());
        timer.setOpaque(false);
        timer.setForeground(ScreenStyles.WHITE);
        timer.setBackground(ScreenStyles.BLACK);
        timer.setFont(ScreenStyles.pixelFont(Font.PLAIN, 18));

        JLabel rowsLabel = ScreenStyles.label("ROWS", 18);
        JLabel columnsLabel = ScreenStyles.label("COLUMNS", 18);
        JLabel minesLabel = ScreenStyles.label("BOMBS (%)", 18);
        JLabel timerLabel = ScreenStyles.label("TIMER", 18);

        fields.add(rowsLabel);
        fields.add(rows);
        fields.add(columnsLabel);
        fields.add(columns);
        fields.add(minesLabel);
        fields.add(mines);
        fields.add(timerLabel);
        fields.add(timer);
        add(fields, BorderLayout.CENTER);

        JPanel actions = new JPanel();
        actions.setOpaque(false);
        JButton start = ScreenStyles.button("START");
        JButton reset = ScreenStyles.button("DEFAULTS");
        JButton menu = ScreenStyles.button("BACK");
        Runnable startAction = () -> {
            musicManager.playEffect("select");
            GameSettings settings = new GameSettings(rows.getValue(), columns.getValue(), mines.getValue(),
                    timer.isSelected());
            startGame.accept(settings);
        };
        Runnable resetAction = () -> {
            musicManager.playEffect("select");
            GameSettings defaults = GameSettings.defaultSettings();
            rows.setValue(defaults.getRows());
            columns.setValue(defaults.getColumns());
            mines.setValue(defaults.getMinePercentage());
            timer.setSelected(defaults.isTimerEnabled());
        };
        Runnable menuAction = () -> {
            musicManager.playEffect("select");
            back.run();
        };
        start.addActionListener(event -> startAction.run());
        reset.addActionListener(event -> resetAction.run());
        menu.addActionListener(event -> menuAction.run());
        actions.add(start);
        actions.add(reset);
        actions.add(menu);
        add(actions, BorderLayout.SOUTH);

        KeyboardSelector selector = new KeyboardSelector();
        selector.add(new KeyboardSelector.SelectableItem() {
            @Override
            public void setSelected(boolean selected) {
                rowsLabel.setForeground(selected ? ScreenStyles.ACCENT : ScreenStyles.WHITE);
                rows.setSelected(selected);
            }

            @Override
            public void activate() {
                rows.increment();
            }
        });
        selector.add(new KeyboardSelector.SelectableItem() {
            @Override
            public void setSelected(boolean selected) {
                columnsLabel.setForeground(selected ? ScreenStyles.ACCENT : ScreenStyles.WHITE);
                columns.setSelected(selected);
            }

            @Override
            public void activate() {
                columns.increment();
            }
        });
        selector.add(new KeyboardSelector.SelectableItem() {
            @Override
            public void setSelected(boolean selected) {
                minesLabel.setForeground(selected ? ScreenStyles.ACCENT : ScreenStyles.WHITE);
                mines.setForeground(selected ? ScreenStyles.ACCENT : ScreenStyles.WHITE);
            }

            @Override
            public void activate() {
                mines.setValue(Math.min(mines.getMaximum(), mines.getValue() + 5));
            }
        });
        selector.add(new KeyboardSelector.SelectableItem() {
            @Override
            public void setSelected(boolean selected) {
                timerLabel.setForeground(selected ? ScreenStyles.ACCENT : ScreenStyles.WHITE);
                timer.setForeground(selected ? ScreenStyles.ACCENT : ScreenStyles.WHITE);
            }

            @Override
            public void activate() {
                timer.setSelected(!timer.isSelected());
            }
        });
        selector.add(new KeyboardItem(start, startAction));
        selector.add(new KeyboardItem(reset, resetAction));
        selector.add(new KeyboardItem(menu, menuAction));
        selector.bindTo(this);
        bindSettingAdjustments(rows, columns, mines, selector);
    }

    private void bindSettingAdjustments(CounterControl rows, CounterControl columns, JSlider mines,
            KeyboardSelector selector) {
        bindAdjustment("LEFT", () -> adjustSelected(rows, columns, mines, selector, -1));
        bindAdjustment("A", () -> adjustSelected(rows, columns, mines, selector, -1));
        bindAdjustment("RIGHT", () -> adjustSelected(rows, columns, mines, selector, 1));
        bindAdjustment("D", () -> adjustSelected(rows, columns, mines, selector, 1));
    }

    private void bindAdjustment(String key, Runnable action) {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key), key);
        getActionMap().put(key, new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent event) {
                action.run();
            }
        });
    }

    private void adjustSelected(CounterControl rows, CounterControl columns, JSlider mines, KeyboardSelector selector,
            int direction) {
        switch (selector.selectedIndex()) {
            case 0:
                if (direction < 0) {
                    rows.decrement();
                } else {
                    rows.increment();
                }
                break;
            case 1:
                if (direction < 0) {
                    columns.decrement();
                } else {
                    columns.increment();
                }
                break;
            case 2:
                mines.setValue(Math.max(mines.getMinimum(), Math.min(mines.getMaximum(), mines.getValue() + direction * 5)));
                break;
            default:
                break;
        }
    }
}
