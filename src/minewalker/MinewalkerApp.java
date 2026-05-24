package minewalker;

import java.awt.CardLayout;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;

import minewalker.audio.MusicManager;
import minewalker.model.GameResult;
import minewalker.model.GameSettings;
import minewalker.persistence.GameStorage;
import minewalker.ui.ConfigureMinesPanel;
import minewalker.ui.GamePanel;
import minewalker.ui.GuidePanel;
import minewalker.ui.MenuPanel;
import minewalker.ui.SettingsPanel;
import minewalker.ui.SplashPanel;

public class MinewalkerApp {
    private static final String SPLASH = "splash";
    private static final String MENU = "menu";
    private static final String CONFMINES = "configure_mines";
    private static final String SETTINGS = "settings";
    private static final String GAME = "game";
    private static final String GUIDE = "guide";
    private static final int DEFAULT_WIDTH = 1080;
    private static final int DEFAULT_HEIGHT = 720;
    private static final int MIN_WIDTH = 800;
    private static final int MIN_HEIGHT = 600;

    private final JFrame frame = new JFrame("Minewalker");
    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private final GameStorage storage = new GameStorage();
    private final MusicManager musicManager = new MusicManager();

    private GameSettings currentSettings;

    public void start() {
        currentSettings = storage.loadSettings().orElseGet(GameSettings::defaultSettings);
        musicManager.applySettings(currentSettings);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setMinimumSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
        frame.setPreferredSize(new Dimension(DEFAULT_WIDTH, DEFAULT_HEIGHT));
        frame.setLocationByPlatform(true);
        frame.setResizable(false);

        root.add(new SplashPanel(this::showMenu, musicManager), SPLASH);
        root.add(new MenuPanel(this::showConfigureMines, this::showSettings, this::showGuide, storage, musicManager), MENU);
        root.add(new ConfigureMinesPanel(currentSettings, this::startGame, this::showMenu, musicManager), CONFMINES);
        root.add(new GuidePanel(this::showMenu, musicManager), GUIDE);
        root.add(new SettingsPanel(this::showMenu, storage, musicManager), SETTINGS);

        frame.setContentPane(root);
        frame.pack();
        frame.setVisible(true);
        showSplash();
    }

    public void showSplash() {
        musicManager.playSoundtrackOnce("splash");
        cards.show(root, SPLASH);
    }

    public void showMenu() {
        musicManager.playSoundtrack("menu");
        refreshMenu();
        cards.show(root, MENU);
    }

    public void showConfigureMines() {
        musicManager.playSoundtrack("menu");
        currentSettings = storage.loadSettings().orElse(currentSettings);
        root.add(new ConfigureMinesPanel(currentSettings, this::startGame, this::showMenu, musicManager),
                CONFMINES);
        cards.show(root, CONFMINES);
    }

    public void showSettings() {
        musicManager.playSoundtrack("menu");
        root.add(new SettingsPanel(this::showMenu, storage, musicManager), SETTINGS);
        cards.show(root, SETTINGS);
    }

    public void showGuide() {
        musicManager.playSoundtrack("menu");
        cards.show(root, GUIDE);
    }

    private void startGame(GameSettings settings) {
        currentSettings = settings;
        musicManager.applySettings(settings);
        frame.setTitle("walk " + settings.getRows() + "x" + settings.getColumns());
        storage.saveSettings(settings);
        GamePanel gamePanel = new GamePanel(settings, this::recordResult,
                () -> startGame(settings), this::showMenu, musicManager);
        root.add(gamePanel, GAME);
        musicManager.playSoundtrack("gameplay");
        cards.show(root, GAME);
        gamePanel.requestBoardFocus();
    }

    private void recordResult(GameResult result) {
        storage.addScore(result.toScoreEntry());
    }

    private void refreshMenu() {
        frame.setTitle("Minewalker");
        root.add(new MenuPanel(this::showConfigureMines, this::showSettings, this::showGuide, storage, musicManager), MENU);
    }
}
