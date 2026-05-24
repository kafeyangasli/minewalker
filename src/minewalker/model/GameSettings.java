package minewalker.model;

public class GameSettings {
    private final int rows;
    private final int columns;
    private final int minePercentage;
    private final boolean timerEnabled;
    private final int timerLimitSeconds;
    private final int sfxVolume;
    private final int soundtrackVolume;
    private final String flagKey;

    public GameSettings(int rows, int columns, int minePercentage, boolean timerEnabled) {
        this(rows, columns, minePercentage, timerEnabled, 180, 80, 70, "F");
    }

    public GameSettings(int rows, int columns, int minePercentage, boolean timerEnabled, int timerLimitSeconds, int sfxVolume,
            int soundtrackVolume, String flagKey) {
        this.rows = rows;
        this.columns = columns;
        this.minePercentage = minePercentage;
        this.timerEnabled = timerEnabled;
        this.timerLimitSeconds = Math.max(60, Math.min(300, timerLimitSeconds));
        this.sfxVolume = clampVolume(sfxVolume);
        this.soundtrackVolume = clampVolume(soundtrackVolume);
        this.flagKey = flagKey == null || flagKey.isBlank() ? "F" : flagKey.toUpperCase();
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

    public int getTimerLimitSeconds() {
        return timerLimitSeconds;
    }

    public int getSfxVolume() {
        return sfxVolume;
    }

    public int getSoundtrackVolume() {
        return soundtrackVolume;
    }

    public String getFlagKey() {
        return flagKey;
    }

    public GameSettings withAudioAndFlag(int sfxVolume, int soundtrackVolume, String flagKey) {
        return new GameSettings(rows, columns, minePercentage, timerEnabled, timerLimitSeconds, sfxVolume,
                soundtrackVolume, flagKey);
    }

    public GameSettings withBoard(int rows, int columns, int minePercentage, boolean timerEnabled,
            int timerLimitSeconds) {
        return new GameSettings(rows, columns, minePercentage, timerEnabled, timerLimitSeconds, sfxVolume,
                soundtrackVolume, flagKey);
    }

    private static int clampVolume(int volume) {
        return Math.max(0, Math.min(100, volume));
    }
}
