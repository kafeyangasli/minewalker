package minewalker.ui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import minewalker.audio.MusicManager;

public class SplashPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final int FADE_INTERVAL_MILLIS = 35;
    private static final float FADE_STEP = 0.015f;

    private float alpha;

    public SplashPanel(Runnable done, MusicManager musicManager) {
        setLayout(new BorderLayout());
        setBackground(ScreenStyles.BLACK);
        setBorder(ScreenStyles.pageBorder());
        setFocusable(true);

        LogoView title = new LogoView(3);
        title.setAlpha(0f);
        add(title, BorderLayout.CENTER);

        JLabel prompt = new FadingLabel("", SwingConstants.CENTER);
        prompt.setForeground(ScreenStyles.WHITE);
        prompt.setFont(ScreenStyles.pixelFont(Font.BOLD, 20));
        add(prompt, BorderLayout.SOUTH);

        Timer fadeTimer = new Timer(FADE_INTERVAL_MILLIS, event -> {
            alpha = Math.min(1f, alpha + FADE_STEP);
            title.setAlpha(alpha);
            prompt.repaint();
            if (alpha >= 1f) {
                ((Timer) event.getSource()).stop();
            }
        });
        fadeTimer.start();

        prompt.setText("Press SPACE or ENTER to continue");
        prompt.repaint();
        requestFocusInWindow();

        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("SPACE"), "continue");
        getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), "continue");
        for (char key = 'A'; key <= 'Z'; key++) {
            getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key), "continue");
        }
        getActionMap().put("continue", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent event) {
                musicManager.playEffect("select");
                done.run();
            }
        });
    }

    @Override
    public void addNotify() {
        super.addNotify();
        requestFocusInWindow();
    }

    private class FadingLabel extends JLabel {
        private static final long serialVersionUID = 1L;

        FadingLabel(String text, int alignment) {
            super(text, alignment);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D copy = (Graphics2D) graphics.create();
            copy.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            copy.setComposite(java.awt.AlphaComposite.getInstance(java.awt.AlphaComposite.SRC_OVER, alpha));
            super.paintComponent(copy);
            copy.dispose();
        }
    }
}
