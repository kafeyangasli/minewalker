package minewalker.persistence;

import java.time.LocalDateTime;
import java.util.Optional;

public class ScoreEntry {
    private final LocalDateTime playedAt;
    private final boolean win;
    private final int elapsedSeconds;
    private final int rows;
    private final int columns;
    private final int minePercentage;
    private final int discoveredTiles;
    private final int totalSafeTiles;

    public ScoreEntry(LocalDateTime playedAt, boolean win, int elapsedSeconds, int rows, int columns,
            int minePercentage, int discoveredTiles, int totalSafeTiles) {
        this.playedAt = playedAt;
        this.win = win;
        this.elapsedSeconds = elapsedSeconds;
        this.rows = rows;
        this.columns = columns;
        this.minePercentage = minePercentage;
        this.discoveredTiles = discoveredTiles;
        this.totalSafeTiles = totalSafeTiles;
    }

    public LocalDateTime getPlayedAt() {
        return playedAt;
    }

    public boolean isWin() {
        return win;
    }

    public int getElapsedSeconds() {
        return elapsedSeconds;
    }

    public String toCsv() {
        return String.join(",",
                playedAt.toString(),
                Boolean.toString(win),
                Integer.toString(elapsedSeconds),
                Integer.toString(rows),
                Integer.toString(columns),
                Integer.toString(minePercentage),
                Integer.toString(discoveredTiles),
                Integer.toString(totalSafeTiles));
    }

    public static Optional<ScoreEntry> fromCsv(String line) {
        String[] parts = line.split(",");
        if (parts.length != 8) {
            return Optional.empty();
        }
        try {
            return Optional.of(new ScoreEntry(
                    LocalDateTime.parse(parts[0]),
                    Boolean.parseBoolean(parts[1]),
                    Integer.parseInt(parts[2]),
                    Integer.parseInt(parts[3]),
                    Integer.parseInt(parts[4]),
                    Integer.parseInt(parts[5]),
                    Integer.parseInt(parts[6]),
                    Integer.parseInt(parts[7])));
        } catch (RuntimeException ex) {
            return Optional.empty();
        }
    }

    public String describe() {
        String result = win ? "WIN" : "LOSS";
        return result + " | " + rows + "x" + columns + " | " + minePercentage + "% mines | "
                + discoveredTiles + "/" + totalSafeTiles + " safe | " + elapsedSeconds + "s";
    }
}
