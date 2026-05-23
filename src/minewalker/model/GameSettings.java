package minewalker.model;

public class GameSettings {
    private final int rows;
    private final int columns;
    private final int minePercentage;
    private final boolean timerEnabled;

    public GameSettings(int rows, int columns, int minePercentage, boolean timerEnabled) {
        this.rows = rows;
        this.columns = columns;
        this.minePercentage = minePercentage;
        this.timerEnabled = timerEnabled;
    }

    public static GameSettings defaultSettings() {
        return new GameSettings(10, 10, 15, true);
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public int getMinePercentage() {
        return minePercentage;
    }

    public boolean isTimerEnabled() {
        return timerEnabled;
    }
}
