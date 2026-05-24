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

    private final GameStorage storage;
    private final MusicManager musicManager;
    private final SpriteSlider sfxVolume;
    private final SpriteSlider soundtrackVolume;
    private GameSettings settings;

    public SettingsPanel(Runnable back, GameStorage storage, MusicManager musicManager) {
        this.storage = storage;
        this.musicManager = musicManager;
        this.settings = storage.loadSettings().orElseGet(GameSettings::defaultSettings);
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

        styleSlider(sfxVolume);
        styleSlider(soundtrackVolume);

        sfxVolume.addChangeListener(event -> musicManager.setSfxVolume(sfxVolume.getValue()));
        soundtrackVolume.addChangeListener(event -> musicManager.setSoundtrackVolume(soundtrackVolume.getValue()));

        fields.add(sfxLabel);
        fields.add(sfxVolume);
        fields.add(soundtrackLabel);
        fields.add(soundtrackVolume);
        add(fields, BorderLayout.CENTER);

        JPanel actions = new JPanel();
        actions.setOpaque(false);
        JButton defaults = ScreenStyles.button("DEFAULTS");
        JButton menu = ScreenStyles.button("BACK");

        Runnable defaultsAction = () -> {
            musicManager.playEffect("select");
            GameSettings defaultSettings = GameSettings.defaultSettings();
            sfxVolume.setValue(defaultSettings.getSfxVolume());
            soundtrackVolume.setValue(defaultSettings.getSoundtrackVolume());
            saveSettings();
        };
        Runnable backAction = () -> {
            musicManager.playEffect("select");
            saveSettings();
            back.run();
        };

        defaults.addActionListener(event -> defaultsAction.run());
        menu.addActionListener(event -> backAction.run());
        actions.add(defaults);
        actions.add(menu);
        add(actions, BorderLayout.SOUTH);

        KeyboardSelector selector = new KeyboardSelector();
        selector.add(selectableSlider(sfxLabel, sfxVolume, 5));
        selector.add(selectableSlider(soundtrackLabel, soundtrackVolume, 5));
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
            default:
                break;
        }
    }

    private void saveSettings() {
        settings = settings.withAudio(sfxVolume.getValue(), soundtrackVolume.getValue());
        musicManager.applySettings(settings);
        storage.saveSettings(settings);
    }

    private int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
