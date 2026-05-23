package minewalker.model;

import java.time.LocalDateTime;

import minewalker.persistence.ScoreEntry;

public class GameResult {
    private final boolean win;
    private final int discoveredSafeTiles;
    private final int totalSafeTiles;
    private final int elapsedSeconds;
    private final GameSettings settings;

    public GameResult(boolean win, int discoveredSafeTiles, int totalSafeTiles, int elapsedSeconds, GameSettings settings) {
        this.win = win;
        this.discoveredSafeTiles = discoveredSafeTiles;
        this.totalSafeTiles = totalSafeTiles;
        this.elapsedSeconds = elapsedSeconds;
        this.settings = settings;
    }

    public boolean isWin() {
        return win;
    }

    public int getDiscoveredSafeTiles() {
        return discoveredSafeTiles;
    }

    public int getTotalSafeTiles() {
        return totalSafeTiles;
    }

    public int getElapsedSeconds() {
        return elapsedSeconds;
    }

    public GameSettings getSettings() {
        return settings;
    }

    public ScoreEntry toScoreEntry() {
        return new ScoreEntry(LocalDateTime.now(), win, elapsedSeconds, settings.getRows(), settings.getColumns(),
                settings.getMinePercentage(), discoveredSafeTiles, totalSafeTiles);
    }
}
