package minewalker.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLayeredPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import minewalker.audio.MusicManager;
import minewalker.model.Direction;
import minewalker.model.GameResult;
import minewalker.model.GameSettings;
import minewalker.model.Minefield;
import minewalker.model.MoveOutcome;
import minewalker.model.Position;
import minewalker.model.SafeTile;
import minewalker.model.Tile;

public class GamePanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private static final int MAX_TILE_SIZE = 48;
    private static final int MIN_TILE_SIZE = 12;

    private final GameSettings settings;
    private Minefield minefield;
    private final Consumer<GameResult> recordResult;
    private final MusicManager musicManager;
    private final JLabel status = new JLabel("", SwingConstants.CENTER);
    private final JLabel timerLabel = new JLabel("Time: 0s", SwingConstants.CENTER);
    private final JLabel titleLabel;
    private final JPanel board = new JPanel();
    private final JLayeredPane playLayer = new JLayeredPane();
    private final ResultOverlay resultOverlay;
    private final PauseOverlay pauseOverlay;
    private final Map<Position, TileView> tileLabels = new HashMap<>();
    private final TextureManager textures = TextureManager.get();
    private final Timer timer;
    private int elapsedSeconds;
    private int remainingSeconds;
    private boolean waitingForFlagDirection;
    private boolean paused;
    private boolean selectingSpawn = true;
    private boolean gameFinished;
    private boolean playerDead;
    private Direction facingDirection = Direction.DOWN;
    private Position spawnCursor;

    public GamePanel(GameSettings settings, Consumer<GameResult> recordResult,
            Runnable playAgain, Runnable menu, MusicManager musicManager) {
        this.settings = settings;
        this.recordResult = recordResult;
        this.musicManager = musicManager;
        this.resultOverlay = new ResultOverlay(playAgain, menu);
        this.pauseOverlay = new PauseOverlay(this::resumeGame, menu);
        this.titleLabel = new JLabel(settings.getRows() + "x" + settings.getColumns() + " map", SwingConstants.CENTER);
        this.spawnCursor = new Position(settings.getColumns() / 2, settings.getRows() / 2);
        this.timer = new Timer(1000, this::tick);

        setLayout(new BorderLayout(12, 12));
        setBackground(ScreenStyles.BLACK);
        setBorder(ScreenStyles.gameBorder());

        JPanel top = new JPanel(new GridLayout(1, 3));
        top.setOpaque(false);
        top.setBorder(BorderFactory.createEmptyBorder(0, 0, 32, 0));
        status.setForeground(ScreenStyles.WHITE);
        status.setFont(ScreenStyles.pixelFont(Font.BOLD, 18));
        titleLabel.setForeground(ScreenStyles.ACCENT);
        titleLabel.setFont(ScreenStyles.pixelFont(Font.BOLD, 18));
        timerLabel.setForeground(ScreenStyles.WHITE);
        timerLabel.setFont(ScreenStyles.pixelFont(Font.BOLD, 18));
        top.add(status);
        top.add(titleLabel);
        top.add(timerLabel);
        add(top, BorderLayout.NORTH);

        board.setFocusable(true);
        board.setBackground(ScreenStyles.BLACK);
        board.setLayout(new GridLayout(settings.getRows(), settings.getColumns(), 0, 0));
        hideCursorOverGameArea();
        playLayer.add(board, JLayeredPane.DEFAULT_LAYER);
        playLayer.add(resultOverlay, JLayeredPane.PALETTE_LAYER);
        playLayer.add(pauseOverlay, JLayeredPane.PALETTE_LAYER);
        add(playLayer, BorderLayout.CENTER);

        buildBoard();
        bindMovementKeys();
        refreshSpawnSelection();

        if (settings.isTimerEnabled()) {
            remainingSeconds = settings.getTimerLimitSeconds();
            timerLabel.setText("Left: " + formatTime(remainingSeconds));
        } else {
            timerLabel.setText("Timer: off");
        }
    }

    public void requestBoardFocus() {
        board.requestFocusInWindow();
    }

    private void buildBoard() {
        tileLabels.clear();
        board.removeAll();
        for (int row = 0; row < settings.getRows(); row++) {
            for (int column = 0; column < settings.getColumns(); column++) {
                TileView label = new TileView();
                label.setOpaque(false);
                label.setFont(ScreenStyles.pixelFont(Font.BOLD, 18));
                label.setForeground(ScreenStyles.WHITE);
                label.setBorder(BorderFactory.createLineBorder(ScreenStyles.WHITE));
                Position position = new Position(column, row);
                tileLabels.put(position, label);
                board.add(label);
            }
        }
    }

    private void bindMovementKeys() {
        bind("W", Direction.UP);
        bind("UP", Direction.UP);
        bind("S", Direction.DOWN);
        bind("DOWN", Direction.DOWN);
        bind("A", Direction.LEFT);
        bind("LEFT", Direction.LEFT);
        bind("D", Direction.RIGHT);
        bind("RIGHT", Direction.RIGHT);
        board.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(settings.getFlagKey()), "flag");
        board.getActionMap().put("flag", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent event) {
                if (selectingSpawn || gameFinished) {
                    return;
                }
                waitingForFlagDirection = !waitingForFlagDirection;
                musicManager.playEffect("select");
                if (waitingForFlagDirection) {
                    status.setText("Flag: Pilih tempat menaruh flag");
                } else {
                    refreshBoard();
                }
            }
        });
        board.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ESCAPE"), "escape");
        board.getActionMap().put("escape", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent event) {
                handleEscape();
            }
        });
        board.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("SPACE"), "space");
        board.getActionMap().put("space", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent event) {
                if (selectingSpawn) {
                    selectSpawn();
                } else if (gameFinished && resultOverlay.isVisible()) {
                    musicManager.playEffect("select");
                    resultOverlay.playAgain();
                } else if (paused) {
                    pauseOverlay.activate();
                }
            }
        });
        board.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("ENTER"), "confirm");
        board.getActionMap().put("confirm", new AbstractAction() {
            private static final long serialVersionUID = 1L;

            @Override
            public void actionPerformed(ActionEvent event) {
                if (selectingSpawn) {
                    selectSpawn();
                } else if (paused) {
                    pauseOverlay.activate();
                }
            }
        });
    }

    private void bind(String key, Direction direction) {
        board.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(key), key);
        board.getActionMap().put(key, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                handleDirection(direction);
            }
        });
    }

    private void handleDirection(Direction direction) {
        if (selectingSpawn) {
            moveSpawnCursor(direction);
            return;
        }
        if (gameFinished) {
            return;
        }
        if (paused) {
            pauseOverlay.moveSelection(direction);
            return;
        }
        if (waitingForFlagDirection) {
            toggleFlag(direction);
            return;
        }
        move(direction);
    }

    private void toggleFlag(Direction direction) {
        if (minefield == null) {
            return;
        }
        waitingForFlagDirection = false;
        boolean changed = minefield.toggleFlag(direction);
        musicManager.playEffect(changed ? "select" : "blocked");
        refreshBoard();
    }

    private void move(Direction direction) {
        if (paused || gameFinished || minefield == null) {
            return;
        }
        MoveOutcome outcome = minefield.move(direction);
        if (outcome == MoveOutcome.BLOCKED || outcome == MoveOutcome.IGNORED) {
            musicManager.playEffect("blocked");
            return;
        }

        if (outcome == MoveOutcome.MOVED) {
            facingDirection = direction;
            musicManager.playEffect("step");
        } else if (outcome == MoveOutcome.WON) {
            facingDirection = direction;
            musicManager.playEffect("completed");
            musicManager.stopSoundtrack();
        } else if (outcome == MoveOutcome.LOST) {
            facingDirection = direction;
            playerDead = true;
        }
        refreshBoard();

        if (outcome == MoveOutcome.WON) {
            timer.stop();
            gameFinished = true;
            Timer delay = new Timer(2000, event -> playWinOverlay());
            delay.setRepeats(false);
            delay.start();
        } else if (outcome == MoveOutcome.LOST) {
            timer.stop();
            musicManager.stopSoundtrack();
            musicManager.playEffect("explosion");
            gameFinished = true;
            Timer delay = new Timer(2000, event -> playResultOverlay());
            delay.setRepeats(false);
            delay.start();
        }
    }

    private void refreshBoard() {
        if (minefield == null) {
            refreshSpawnSelection();
            return;
        }
        Position playerPosition = minefield.getPlayer().getPosition();
        for (int row = 0; row < settings.getRows(); row++) {
            for (int column = 0; column < settings.getColumns(); column++) {
                Tile tile = minefield.getTile(row, column);
                boolean playerHere = tile.getPosition().equals(playerPosition);
                TileView label = tileLabels.get(tile.getPosition());
                label.setSprite(spriteFor(tile));
                label.setOverlaySprite(playerHere ? playerSprite() : null);
                label.setBackground(tile.getDisplayColor(playerHere));
                label.setText(displayTextFor(tile, playerHere));
            }
        }
        status.setText(minefield.getDiscoveredSafeTiles() + "/" + minefield.getTotalSafeTiles() + " safe tiles");
    }

    private void refreshSpawnSelection() {
        for (int row = 0; row < settings.getRows(); row++) {
            for (int column = 0; column < settings.getColumns(); column++) {
                Position position = new Position(column, row);
                TileView label = tileLabels.get(position);
                boolean cursorHere = position.equals(spawnCursor);
                label.setSprite(textures.hiddenTile());
                label.setOverlaySprite(cursorHere ? textures.player("spawn") : null);
            }
        }
        status.setText("Pilih tempat spawn");
    }

    private void moveSpawnCursor(Direction direction) {
        Position target = spawnCursor.move(direction);
        if (target.x() < 0 || target.x() >= settings.getColumns() || target.y() < 0 || target.y() >= settings.getRows()) {
            musicManager.playEffect("blocked");
            return;
        }
        spawnCursor = target;
        musicManager.playEffect("step");
        refreshSpawnSelection();
    }

    private void selectSpawn() {
        selectingSpawn = false;
        minefield = new Minefield(settings, spawnCursor);
        elapsedSeconds = 0;
        remainingSeconds = settings.getTimerLimitSeconds();
        if (settings.isTimerEnabled()) {
            timerLabel.setText("Left: " + formatTime(remainingSeconds));
            timer.start();
        }
        musicManager.playEffect("select");
        refreshBoard();
    }

    private String displayTextFor(Tile tile, boolean playerHere) {
        if (textures.hasTexture()) {
            int nearbyMines = tile.isRevealed() && tile instanceof SafeTile
                    ? minefield.countAdjacentMines(tile.getPosition())
                    : 0;
            if (tile.isFlagged() || !tile.isRevealed() || nearbyMines <= 6) {
                return "";
            }
        }
        if (tile.isFlagged()) {
            return "F";
        }
        if (!tile.isRevealed() || !(tile instanceof SafeTile)) {
            return tile.getDisplayText(playerHere);
        }

        int nearbyMines = minefield.countAdjacentMines(tile.getPosition());
        return nearbyMines > 0 ? Integer.toString(nearbyMines) : tile.getDisplayText(false);
    }

    private Image spriteFor(Tile tile) {
        if (tile.isFlagged()) {
            return textures.flagTile();
        }
        if (!tile.isRevealed()) {
            return textures.hiddenTile();
        }
        if (!(tile instanceof SafeTile)) {
            return textures.mineTile();
        }

        int nearbyMines = minefield.countAdjacentMines(tile.getPosition());
        if (nearbyMines > 0) {
            return textures.numberTile(nearbyMines);
        }
        return textures.revealedTile();
    }

    private Image playerSprite() {
        if (playerDead) {
            return textures.player("death");
        }
        return textures.player(facingDirection);
    }

    private void hideCursorOverGameArea() {
        Image cursorImage = new java.awt.image.BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Cursor hiddenCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImage, new Point(0, 0), "hidden");
        applyCursor(board, hiddenCursor);
        applyCursor(playLayer, hiddenCursor);
        applyCursor(resultOverlay, hiddenCursor);
        applyCursor(pauseOverlay, hiddenCursor);
    }

    private void applyCursor(Component component, Cursor cursor) {
        component.setCursor(cursor);
        if (component instanceof Container) {
            for (Component child : ((Container) component).getComponents()) {
                applyCursor(child, cursor);
            }
        }
    }

    private void playResultOverlay() {
        GameResult result = new GameResult(false, minefield.getDiscoveredSafeTiles(), minefield.getTotalSafeTiles(),
                elapsedSeconds, settings);
        showResultOverlay(result, "GAME OVER", "game-over");
    }

    private void playWinOverlay() {
        GameResult result = new GameResult(true, minefield.getDiscoveredSafeTiles(), minefield.getTotalSafeTiles(),
                elapsedSeconds, settings);
        showResultOverlay(result, "YOU WIN!", "you-won");
    }

    private void showResultOverlay(GameResult result, String title, String soundtrack) {
        recordResult.accept(result);
        resultOverlay.setTitle(title);
        resultOverlay.setStats("Explored " + result.getDiscoveredSafeTiles() + "/" + result.getTotalSafeTiles()
                + " safe tiles  |  Time " + result.getElapsedSeconds() + "s");
        resultOverlay.setVisible(true);
        resultOverlay.resetAnimation();
        musicManager.playSoundtrack(soundtrack);

        Timer animation = new Timer(16, null);
        animation.addActionListener(event -> {
            boolean finished = resultOverlay.slideDown();
            if (finished) {
                animation.stop();
            }
        });
        animation.start();
    }

    private void tick(ActionEvent event) {
        if (!paused && !gameFinished && !selectingSpawn) {
            elapsedSeconds++;
            if (settings.isTimerEnabled()) {
                remainingSeconds--;
                timerLabel.setText("Left: " + formatTime(remainingSeconds));
                if (remainingSeconds <= 0) {
                    triggerTimeoutLoss();
                }
            }
        }
    }

    private void triggerTimeoutLoss() {
        timer.stop();
        gameFinished = true;
        playerDead = true;
        if (minefield != null) {
            minefield.explodeAllMines();
            refreshBoard();
        }
        musicManager.stopSoundtrack();
        musicManager.playEffect("explosion");
        Timer delay = new Timer(2000, event -> playResultOverlay());
        delay.setRepeats(false);
        delay.start();
    }

    private String formatTime(int seconds) {
        int safeSeconds = Math.max(0, seconds);
        return String.format("%d:%02d", safeSeconds / 60, safeSeconds % 60);
    }

    @Override
    public void doLayout() {
        super.doLayout();
        Dimension size = playLayer.getSize();
        int tileSize = calculateTileSize(size);
        int boardWidth = tileSize * settings.getColumns();
        int boardHeight = tileSize * settings.getRows();
        board.setBounds((size.width - boardWidth) / 2, (size.height - boardHeight) / 2, boardWidth, boardHeight);
        resultOverlay.layoutFor(size);
        pauseOverlay.layoutFor(size);
    }

    private int calculateTileSize(Dimension availableSize) {
        int widthBasedSize = availableSize.width / settings.getColumns();
        int heightBasedSize = availableSize.height / settings.getRows();
        int tileSize = Math.min(MAX_TILE_SIZE, Math.min(widthBasedSize, heightBasedSize));
        return Math.max(MIN_TILE_SIZE, tileSize);
    }

    private void handleEscape() {
        if (gameFinished && resultOverlay.isVisible()) {
            musicManager.playEffect("select");
            resultOverlay.menu();
        } else if (paused) {
            resumeGame();
        } else {
            pauseGame();
        }
    }

    private void pauseGame() {
        if (gameFinished || selectingSpawn) {
            return;
        }
        paused = true;
        waitingForFlagDirection = false;
        pauseOverlay.setVisible(true);
        pauseOverlay.resetSelection();
        status.setText("Paused");
        musicManager.playEffect("select");
    }

    private void resumeGame() {
        paused = false;
        pauseOverlay.setVisible(false);
        refreshBoard();
        musicManager.playEffect("select");
    }

    private static class ResultOverlay extends JPanel {
        private static final long serialVersionUID = 1L;
        private static final int OVERLAY_WIDTH = 560;
        private static final int OVERLAY_HEIGHT = 280;
        private int y = -OVERLAY_HEIGHT;
        private int targetY;
        private int shadeAlpha;
        private final JLabel title = new JLabel("", SwingConstants.CENTER);
        private final JLabel statsLabel = new JLabel("", SwingConstants.CENTER);
        private final JLabel continueLabel = new JLabel("SPACE: PLAY AGAIN    ESC: MENU", SwingConstants.CENTER);
        private final Runnable playAgain;
        private final Runnable menu;

        ResultOverlay(Runnable playAgain, Runnable menu) {
            this.playAgain = playAgain;
            this.menu = menu;
            setVisible(false);
            setOpaque(false);
            setLayout(null);
            setBorder(BorderFactory.createEmptyBorder(34, 34, 34, 34));

            title.setForeground(ScreenStyles.WHITE);
            title.setFont(ScreenStyles.pixelFont(Font.BOLD, 44));
            statsLabel.setForeground(ScreenStyles.WHITE);
            statsLabel.setFont(ScreenStyles.pixelFont(Font.BOLD, 16));
            continueLabel.setForeground(ScreenStyles.ACCENT);
            continueLabel.setFont(ScreenStyles.pixelFont(Font.BOLD, 15));
            add(title);
            add(statsLabel);
            add(continueLabel);
        }

        void setStats(String stats) {
            statsLabel.setText(stats);
        }

        void setTitle(String text) {
            title.setText(text);
            title.setForeground(text.toLowerCase().contains("win") ? ScreenStyles.GREEN : ScreenStyles.RED);
        }

        void layoutFor(Dimension parentSize) {
            setBounds(0, 0, parentSize.width, parentSize.height);
            int width = Math.min(OVERLAY_WIDTH, Math.max(0, parentSize.width - 32));
            targetY = Math.max(0, (parentSize.height - OVERLAY_HEIGHT) / 2);
            if (!isVisible() && y < 0) {
                y = -OVERLAY_HEIGHT;
            }
            layoutContent(width);
        }

        boolean slideDown() {
            int width = Math.min(OVERLAY_WIDTH, Math.max(0, getWidth() - 32));
            y = Math.min(targetY, y + 10);
            shadeAlpha = Math.min(175, shadeAlpha + 5);
            layoutContent(width);
            repaint();
            return y >= targetY;
        }

        void resetAnimation() {
            y = -OVERLAY_HEIGHT;
            shadeAlpha = 0;
        }

        void playAgain() {
            playAgain.run();
        }

        void menu() {
            menu.run();
        }

        private void layoutContent(int width) {
            int x = Math.max(0, (getWidth() - width) / 2);
            title.setBounds(x + 24, y + 42, width - 48, 64);
            statsLabel.setBounds(x + 24, y + 118, width - 48, 36);
            continueLabel.setBounds(x + 24, y + 178, width - 48, 36);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(new Color(0, 0, 0, shadeAlpha));
            g.fillRect(0, 0, getWidth(), getHeight());
            int width = Math.min(OVERLAY_WIDTH, Math.max(0, getWidth() - 32));
            int x = Math.max(0, (getWidth() - width) / 2);
            g.setColor(ScreenStyles.PANEL);
            g.fillRect(x, y, width, OVERLAY_HEIGHT);
            g.setColor(ScreenStyles.WHITE);
            g.drawRect(x, y, width - 1, OVERLAY_HEIGHT - 1);
            g.drawRect(x + 6, y + 6, width - 13, OVERLAY_HEIGHT - 13);
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    private static class PauseOverlay extends JPanel {
        private static final long serialVersionUID = 1L;
        private final JLabel title = new JLabel("PAUSED", SwingConstants.CENTER);
        private final JButton resume = new SpriteButton("RESUME");
        private final JButton exit = new SpriteButton("EXIT TO MENU");
        private final Runnable resumeAction;
        private final Runnable exitAction;
        private int selected;

        PauseOverlay(Runnable resumeAction, Runnable exitAction) {
            this.resumeAction = resumeAction;
            this.exitAction = exitAction;
            setVisible(false);
            setOpaque(false);
            setLayout(new GridLayout(0, 1, 8, 8));
            setBorder(BorderFactory.createEmptyBorder(120, 180, 120, 180));

            title.setForeground(ScreenStyles.WHITE);
            title.setFont(ScreenStyles.pixelFont(Font.BOLD, 42));
            title.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
            resume.setFont(ScreenStyles.pixelFont(Font.BOLD, 22));
            exit.setFont(ScreenStyles.pixelFont(Font.BOLD, 22));
            add(title);
            add(resume);
            add(exit);
            refresh();
        }

        void layoutFor(Dimension size) {
            setBounds(0, 0, size.width, size.height);
        }

        void resetSelection() {
            selected = 0;
            refresh();
        }

        void moveSelection(Direction direction) {
            if (direction == Direction.UP || direction == Direction.DOWN) {
                selected = (selected + 1) % 2;
            }
            refresh();
        }

        void activate() {
            if (selected == 0) {
                resumeAction.run();
            } else {
                exitAction.run();
            }
        }

        private void refresh() {
            resume.setForeground(selected == 0 ? ScreenStyles.ACCENT : ScreenStyles.WHITE);
            exit.setForeground(selected == 1 ? ScreenStyles.ACCENT : ScreenStyles.WHITE);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setColor(new Color(0, 0, 0, 190));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.dispose();
            super.paintComponent(graphics);
        }
    }
}
