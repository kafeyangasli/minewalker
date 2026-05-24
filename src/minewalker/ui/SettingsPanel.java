package minewalker.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;

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

public class SettingsPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final String[] FLAG_KEYS = { "F", "E", "Q", "R", "T" };

    private final GameStorage storage;
    private final MusicManager musicManager;
    private final SpriteSlider sfxVolume;
    private final SpriteSlider soundtrackVolume;
    private final JLabel flagValue = ScreenStyles.label("", 18);
    private GameSettings settings;
    private int flagKeyIndex;

    public SettingsPanel(Runnable back, GameStorage storage, MusicManager musicManager) {
        this.storage = storage;
        this.musicManager = musicManager;
        this.settings = storage.loadSettings().orElseGet(GameSettings::defaultSettings);
        this.flagKeyIndex = findFlagKeyIndex(settings.getFlagKey());
        this.sfxVolume = new SpriteSlider(0, 100, settings.getSfxVolume());
        this.soundtrackVolume = new SpriteSlider(0, 100, settings.getSoundtrackVolume());

        setLayout(new BorderLayout(20, 20));
        setBackground(ScreenStyles.BLACK);
        setBorder(ScreenStyles.pageBorder());
        setFocusable(true);

        JLabel title = new JLabel("Settings", SwingConstants.CENTER);
        title.setForeground(ScreenStyles.WHITE);
        title.setFont(ScreenStyles.pixelFont(Font.BOLD, 46));
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 32, 0));
        add(title, BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridLayout(0, 2, 14, 14));
        fields.setOpaque(false);

        JLabel sfxLabel = ScreenStyles.label("SFX VOLUME", 18);
        JLabel soundtrackLabel = ScreenStyles.label("SOUNDTRACK VOLUME", 18);
        JLabel flagLabel = ScreenStyles.label("FLAG KEYBIND", 18);

        styleSlider(sfxVolume);
        styleSlider(soundtrackVolume);
        updateFlagLabel();

        sfxVolume.addChangeListener(event -> musicManager.setSfxVolume(sfxVolume.getValue()));
        soundtrackVolume.addChangeListener(event -> musicManager.setSoundtrackVolume(soundtrackVolume.getValue()));
        flagValue.setHorizontalAlignment(SwingConstants.CENTER);

        fields.add(sfxLabel);
        fields.add(sfxVolume);
        fields.add(soundtrackLabel);
        fields.add(soundtrackVolume);
        fields.add(flagLabel);
        fields.add(flagValue);
        add(fields, BorderLayout.CENTER);

        JPanel actions = new JPanel();
        actions.setOpaque(false);
        JButton apply = ScreenStyles.button("APPLY");
        JButton defaults = ScreenStyles.button("DEFAULTS");
        JButton menu = ScreenStyles.button("BACK");

        Runnable applyAction = () -> {
            musicManager.playEffect("select");
            saveSettings();
        };
        Runnable defaultsAction = () -> {
            musicManager.playEffect("select");
            GameSettings defaultSettings = GameSettings.defaultSettings();
            sfxVolume.setValue(defaultSettings.getSfxVolume());
            soundtrackVolume.setValue(defaultSettings.getSoundtrackVolume());
            flagKeyIndex = findFlagKeyIndex(defaultSettings.getFlagKey());
            updateFlagLabel();
            saveSettings();
        };
        Runnable backAction = () -> {
            musicManager.playEffect("select");
            saveSettings();
            back.run();
        };

        apply.addActionListener(event -> applyAction.run());
        defaults.addActionListener(event -> defaultsAction.run());
        menu.addActionListener(event -> backAction.run());
        actions.add(apply);
        actions.add(defaults);
        actions.add(menu);
        add(actions, BorderLayout.SOUTH);

        KeyboardSelector selector = new KeyboardSelector();
        selector.add(selectableSlider(sfxLabel, sfxVolume, 5));
        selector.add(selectableSlider(soundtrackLabel, soundtrackVolume, 5));
        selector.add(selectableFlag(flagLabel));
        selector.add(new KeyboardItem(apply, applyAction));
        selector.add(new KeyboardItem(defaults, defaultsAction));
        selector.add(new KeyboardItem(menu, backAction));
        selector.bindTo(this);
        bindSettingAdjustments(sfxVolume, soundtrackVolume, selector);
    }

    private void styleSlider(SpriteSlider slider) {
        slider.setMajorTickSpacing(25);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setOpaque(false);
        slider.setForeground(ScreenStyles.WHITE);
        slider.setBackground(ScreenStyles.BLACK);
    }

    private KeyboardSelector.SelectableItem selectableSlider(JLabel label, JSlider slider, int step) {
        return new KeyboardSelector.SelectableItem() {
            @Override
            public void setSelected(boolean selected) {
                label.setForeground(selected ? ScreenStyles.ACCENT : ScreenStyles.WHITE);
                slider.setForeground(selected ? ScreenStyles.ACCENT : ScreenStyles.WHITE);
            }

            @Override
            public void activate() {
                slider.setValue(Math.min(slider.getMaximum(), slider.getValue() + step));
            }
        };
    }

    private KeyboardSelector.SelectableItem selectableFlag(JLabel label) {
        return new KeyboardSelector.SelectableItem() {
            @Override
            public void setSelected(boolean selected) {
                label.setForeground(selected ? ScreenStyles.ACCENT : ScreenStyles.WHITE);
                flagValue.setForeground(selected ? ScreenStyles.ACCENT : ScreenStyles.WHITE);
            }

            @Override
            public void activate() {
                cycleFlagKey(1);
            }
        };
    }

    private void bindSettingAdjustments(JSlider sfx, JSlider soundtrack, KeyboardSelector selector) {
        bindAdjustment("LEFT", () -> adjustSelected(sfx, soundtrack, selector, -1));
        bindAdjustment("A", () -> adjustSelected(sfx, soundtrack, selector, -1));
        bindAdjustment("RIGHT", () -> adjustSelected(sfx, soundtrack, selector, 1));
        bindAdjustment("D", () -> adjustSelected(sfx, soundtrack, selector, 1));
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

    private void adjustSelected(JSlider sfx, JSlider soundtrack, KeyboardSelector selector, int direction) {
        switch (selector.selectedIndex()) {
            case 0:
                sfx.setValue(clamp(sfx.getValue() + direction * 5, sfx.getMinimum(), sfx.getMaximum()));
                break;
            case 1:
                soundtrack.setValue(clamp(soundtrack.getValue() + direction * 5, soundtrack.getMinimum(),
                        soundtrack.getMaximum()));
                break;
            case 2:
                cycleFlagKey(direction);
                break;
            default:
                break;
        }
    }

    private void cycleFlagKey(int direction) {
        flagKeyIndex = (flagKeyIndex + direction + FLAG_KEYS.length) % FLAG_KEYS.length;
        updateFlagLabel();
    }

    private void updateFlagLabel() {
        flagValue.setText(FLAG_KEYS[flagKeyIndex]);
    }

    private void saveSettings() {
        settings = settings.withAudioAndFlag(sfxVolume.getValue(), soundtrackVolume.getValue(), FLAG_KEYS[flagKeyIndex]);
        musicManager.applySettings(settings);
        storage.saveSettings(settings);
    }

    private int findFlagKeyIndex(String key) {
        for (int index = 0; index < FLAG_KEYS.length; index++) {
            if (FLAG_KEYS[index].equalsIgnoreCase(key)) {
                return index;
            }
        }
        return 0;
    }

    private int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
