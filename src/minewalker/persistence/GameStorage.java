package minewalker.persistence;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import minewalker.model.GameSettings;

public class GameStorage {
    private final Path dataDirectory = Path.of(System.getProperty("user.home"), ".minewalker");
    private final Path settingsFile = dataDirectory.resolve("settings.properties");
    private final Path scoresFile = dataDirectory.resolve("scores.csv");

    public Optional<GameSettings> loadSettings() {
        if (!Files.exists(settingsFile)) {
            return Optional.empty();
        }

        Properties properties = new Properties();
        try (BufferedReader reader = Files.newBufferedReader(settingsFile)) {
            properties.load(reader);
            int rows = Integer.parseInt(properties.getProperty("rows", "10"));
            int columns = Integer.parseInt(properties.getProperty("columns", "10"));
            int mines = Integer.parseInt(properties.getProperty("minePercentage", "15"));
            boolean timer = Boolean.parseBoolean(properties.getProperty("timerEnabled", "true"));
            int timerLimitSeconds = Integer.parseInt(properties.getProperty("timerLimitSeconds", "180"));
            int sfxVolume = Integer.parseInt(properties.getProperty("sfxVolume", "80"));
            int soundtrackVolume = Integer.parseInt(properties.getProperty("soundtrackVolume", "70"));
            return Optional.of(new GameSettings(rows, columns, mines, timer, timerLimitSeconds, sfxVolume,
                    soundtrackVolume));
        } catch (IOException | NumberFormatException ex) {
            return Optional.empty();
        }
    }

    public void saveSettings(GameSettings settings) {
        ensureDataDirectory();
        Properties properties = new Properties();
        properties.setProperty("rows", Integer.toString(settings.getRows()));
        properties.setProperty("columns", Integer.toString(settings.getColumns()));
        properties.setProperty("minePercentage", Integer.toString(settings.getMinePercentage()));
        properties.setProperty("timerEnabled", Boolean.toString(settings.isTimerEnabled()));
        properties.setProperty("timerLimitSeconds", Integer.toString(settings.getTimerLimitSeconds()));
        properties.setProperty("sfxVolume", Integer.toString(settings.getSfxVolume()));
        properties.setProperty("soundtrackVolume", Integer.toString(settings.getSoundtrackVolume()));
        try (BufferedWriter writer = Files.newBufferedWriter(settingsFile)) {
            properties.store(writer, "Minewalker settings");
        } catch (IOException ignored) {
            // The game remains playable even if local settings cannot be saved.
        }
    }

    public void addScore(ScoreEntry score) {
        ensureDataDirectory();
        try (BufferedWriter writer = Files.newBufferedWriter(scoresFile,
                java.nio.file.StandardOpenOption.CREATE,
                java.nio.file.StandardOpenOption.APPEND)) {
            writer.write(score.toCsv());
            writer.newLine();
        } catch (IOException ignored) {
            // Score persistence is best-effort storage.
        }
    }

    public List<ScoreEntry> loadScores() {
        List<ScoreEntry> scores = new ArrayList<>();
        if (!Files.exists(scoresFile)) {
            return scores;
        }

        try (BufferedReader reader = Files.newBufferedReader(scoresFile)) {
            String line;
            while ((line = reader.readLine()) != null) {
                ScoreEntry.fromCsv(line).ifPresent(scores::add);
            }
        } catch (IOException ignored) {
            return scores;
        }

        scores.sort(Comparator.comparing(ScoreEntry::isWin).reversed()
                .thenComparingInt(ScoreEntry::getElapsedSeconds)
                .thenComparing(ScoreEntry::getPlayedAt, Comparator.reverseOrder()));
        return scores;
    }

    public List<ScoreEntry> topScores(int limit) {
        List<ScoreEntry> scores = loadScores();
        return scores.subList(0, Math.min(limit, scores.size()));
    }

    private void ensureDataDirectory() {
        try {
            Files.createDirectories(dataDirectory);
        } catch (IOException ignored) {
            // Later file operations will simply fail gracefully.
        }
    }
}
