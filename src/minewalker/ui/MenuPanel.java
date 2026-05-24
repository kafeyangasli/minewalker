package minewalker.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;

import minewalker.audio.MusicManager;
import minewalker.persistence.GameStorage;

public class MenuPanel extends JPanel {
    public MenuPanel(Runnable playSettings, Runnable guide, GameStorage storage, MusicManager musicManager) {
        setLayout(new BorderLayout(20, 20));
        setBackground(ScreenStyles.BLACK);
        setBorder(ScreenStyles.pageBorder());
        setFocusable(true);

        LogoView title = new LogoView(2);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 32, 0));
        add(title, BorderLayout.NORTH);

        JPanel actions = new JPanel(new GridLayout(0, 1, 14, 14));
        actions.setOpaque(false);
        JButton play = ScreenStyles.button("PLAY SETTINGS");
        JButton guideButton = ScreenStyles.button("GUIDE");
        JButton mute = ScreenStyles.button(musicManager.isMuted() ? "UNMUTE AUDIO" : "MUTE AUDIO");

        play.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        guideButton.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
        mute.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));

        Runnable playAction = () -> {
            musicManager.playEffect("select");
            playSettings.run();
        };
        Runnable guideAction = () -> {
            musicManager.playEffect("select");
            guide.run();
        };
        Runnable muteAction = () -> {
            musicManager.setMuted(!musicManager.isMuted());
            mute.setText(musicManager.isMuted() ? "UNMUTE AUDIO" : "MUTE AUDIO");
        };

        play.addActionListener(event -> playAction.run());
        guideButton.addActionListener(event -> guideAction.run());
        mute.addActionListener(event -> muteAction.run());

        actions.add(play);
        actions.add(guideButton);
        actions.add(mute);
        add(actions, BorderLayout.CENTER);

        KeyboardSelector selector = new KeyboardSelector();
        selector.add(new KeyboardItem(play, playAction));
        selector.add(new KeyboardItem(guideButton, guideAction));
        selector.add(new KeyboardItem(mute, muteAction));
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
