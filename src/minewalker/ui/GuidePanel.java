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
        text.add(ScreenStyles.label("Menus: W/S atau Up/Down untuk memilih menu, Enter atau Space untuk konfirmasi.", 15));
        text.add(ScreenStyles.label("Gameplay Configuration: W/S atau Up/Down untuk memilih, A/D atau Left/Right untuk mengatur value.", 15));
        text.add(ScreenStyles.label("Spawn: WASD atau tombol arah pada keyboard untuk menentukan titik spawn, lalu Enter atau Space.", 15));
        text.add(ScreenStyles.label("Gameplay: WASD atau tombol arah untuk bergerak.", 15));
        text.add(ScreenStyles.label("Flag: tekan F, lalu WASD atau tombol arah untuk menaruh flag.", 15));
        text.add(ScreenStyles.label("Pause: Esc, lalu W/S atau Up/Down, dan Enter atau Space untuk konfirmasi.", 15));
        text.add(ScreenStyles.label("Gameplay Finished: Space untuk memulai kembali, Esc untuk kembali ke menu.", 15));
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
