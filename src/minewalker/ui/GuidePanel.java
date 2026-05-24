package minewalker.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import minewalker.audio.MusicManager;

public class GuidePanel extends JPanel {
    private static final long serialVersionUID = 1L;

    public GuidePanel(Runnable back, MusicManager musicManager) {
        setLayout(new BorderLayout(20, 20));
        setBackground(ScreenStyles.BLACK);
        setBorder(ScreenStyles.pageBorder());
        setFocusable(true);

        JLabel title = new JLabel("Guide", SwingConstants.CENTER);
        title.setFont(ScreenStyles.pixelFont(Font.BOLD, 46));
        title.setForeground(ScreenStyles.WHITE);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 32, 0));
        add(title, BorderLayout.NORTH);

        JPanel text = new JPanel(new GridLayout(0, 1, 8, 8));
        text.setOpaque(false);
        text.add(ScreenStyles.label("Menu: UP/DOWN atau W/S untuk memilih, ENTER/SPACE untuk konfirmasi.", 17));
        text.add(ScreenStyles.label("SPAWN: MOVE THE GREEN CURSOR, THEN PRESS ENTER OR SPACE.", 17));
        text.add(ScreenStyles.label("GOAL: REVEAL EVERY SAFE TILE WITHOUT STEPPING ON A MINE.", 17));
        text.add(ScreenStyles.label("MOVE: WASD OR ARROW KEYS.", 17));
        text.add(ScreenStyles.label("FLAG: PRESS F, THEN A DIRECTION KEY TO TOGGLE A FLAG.", 17));
        text.add(ScreenStyles.label("FLAGS BLOCK MOVEMENT AND HELP PREVENT ACCIDENTAL STEPS.", 17));
        text.add(ScreenStyles.label("NUMBERS SHOW HOW MANY MINES TOUCH A REVEALED TILE.", 17));
        text.add(ScreenStyles.label("PAUSE: ESC DURING GAMEPLAY.", 17));
        add(text, BorderLayout.CENTER);

        JButton backButton = ScreenStyles.button("BACK");
        Runnable backAction = () -> {
            musicManager.playEffect("select");
            back.run();
        };
        backButton.addActionListener(event -> backAction.run());
        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.add(backButton);
        add(bottom, BorderLayout.SOUTH);

        KeyboardSelector selector = new KeyboardSelector();
        selector.add(new KeyboardItem(backButton, backAction));
        selector.bindTo(this);
    }
}
