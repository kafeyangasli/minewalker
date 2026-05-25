package minewalker.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;

import minewalker.audio.MusicManager;
import minewalker.persistence.GameStorage;

public class MenuPanel extends JPanel {
    private static final long serialVersionUID = 1L;

public MenuPanel(Runnable playSettings, Runnable settings, Runnable guide, Runnable quit,
        GameStorage storage, MusicManager musicManager) {
        setLayout(new BorderLayout(20, 20));
        setBackground(ScreenStyles.BLACK);
        setBorder(ScreenStyles.pageBorder());
        setFocusable(true);

        LogoView title = new LogoView(2);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 32, 0));
        add(title, BorderLayout.NORTH);

        JPanel actions = new JPanel(new GridLayout(0, 1, 14, 14));
        actions.setOpaque(false);
        JButton play = ScreenStyles.button("PLAY");
        JButton settingsButton = ScreenStyles.button("SETTINGS");
        JButton guideButton = ScreenStyles.button("GUIDE");
        JButton quitButton = ScreenStyles.button("QUIT");

        Runnable playAction = () -> {
            musicManager.playEffect("select");
            playSettings.run();
        };
        Runnable guideAction = () -> {
            musicManager.playEffect("select");
            guide.run();
        };
        Runnable settingsAction = () -> {
            musicManager.playEffect("select");
            settings.run();
        };
        Runnable quitAction = () -> {
            musicManager.playEffect("select");
            quit.run();
        };

        play.addActionListener(event -> playAction.run());
        settingsButton.addActionListener(event -> settingsAction.run());
        guideButton.addActionListener(event -> guideAction.run());
        quitButton.addActionListener(event -> quitAction.run());
        actions.add(play);
        actions.add(settingsButton);
        actions.add(guideButton);
        actions.add(quitButton);
        add(actions, BorderLayout.CENTER);

        KeyboardSelector selector = new KeyboardSelector();
        selector.add(new KeyboardItem(play, playAction));
        selector.add(new KeyboardItem(settingsButton, settingsAction));
        selector.add(new KeyboardItem(guideButton, guideAction));
        selector.add(new KeyboardItem(quitButton, quitAction));
        selector.bindTo(this);

        JPanel recent = new JPanel(new GridLayout(0, 1, 4, 4));
        recent.setOpaque(false);
        recent.add(ScreenStyles.label("RECENT RESULTS", 16));
        for (var score : storage.topScores(3)) {
            recent.add(ScreenStyles.label(score.describe(), 13));
        }
        add(recent, BorderLayout.SOUTH);
    }
}
