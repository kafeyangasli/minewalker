package minewalker.audio;

import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineEvent;

public class MusicManager {
    private final Map<String, Clip> clips = new HashMap<>();
    private Clip activeSoundtrack;
    private String activeSoundtrackName;
    private boolean muted;
    private int sfxVolume = 80;
    private int soundtrackVolume = 70;

    public void playSoundtrack(String name) {
        playSoundtrack(name, true);
    }

    public void playSoundtrackOnce(String name) {
        playSoundtrack(name, false);
    }

    private void playSoundtrack(String name, boolean loop) {
        if (muted) {
            return;
        }
        if (name.equals(activeSoundtrackName) && activeSoundtrack != null && activeSoundtrack.isRunning()) {
            return;
        }
        stopSoundtrack();
        Optional<Clip> clip = loadClip(name);
        if (clip.isPresent()) {
            activeSoundtrack = clip.get();
            activeSoundtrackName = name;
            applyVolume(activeSoundtrack, soundtrackVolume);
            activeSoundtrack.setFramePosition(0);
            if (loop) {
                activeSoundtrack.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                activeSoundtrack.start();
                activeSoundtrack.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP
                            && activeSoundtrack != null
                            && activeSoundtrack.getFramePosition() >= activeSoundtrack.getFrameLength()) {
                        activeSoundtrack = null;
                        activeSoundtrackName = null;
                    }
                });
            }
        }
    }

    public void playEffect(String name) {
        if (muted) {
            return;
        }
        Optional<Clip> clip = loadClip(name);
        if (clip.isPresent()) {
            Clip effect = clip.get();
            applyVolume(effect, sfxVolume);
            effect.setFramePosition(0);
            effect.start();
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    public void stopSoundtrack() {
        if (activeSoundtrack != null) {
            activeSoundtrack.stop();
            activeSoundtrack = null;
            activeSoundtrackName = null;
        }
    }

    public boolean isMuted() {
        return muted;
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
        if (muted) {
            stopSoundtrack();
        }
    }

    public int getSfxVolume() {
        return sfxVolume;
    }

    public void setSfxVolume(int sfxVolume) {
        this.sfxVolume = clampVolume(sfxVolume);
    }

    public int getSoundtrackVolume() {
        return soundtrackVolume;
    }

    public void setSoundtrackVolume(int soundtrackVolume) {
        this.soundtrackVolume = clampVolume(soundtrackVolume);
        if (activeSoundtrack != null) {
            applyVolume(activeSoundtrack, this.soundtrackVolume);
        }
    }

    public void applySettings(minewalker.model.GameSettings settings) {
        setSfxVolume(settings.getSfxVolume());
        setSoundtrackVolume(settings.getSoundtrackVolume());
    }

    private Optional<Clip> loadClip(String name) {
        if (clips.containsKey(name)) {
            return Optional.of(clips.get(name));
        }

        String path = "/audio/" + name + ".wav";
        try (InputStream raw = MusicManager.class.getResourceAsStream(path)) {
            if (raw == null) {
                return Optional.empty();
            }
            try (AudioInputStream stream = AudioSystem.getAudioInputStream(new BufferedInputStream(raw))) {
                Clip clip = AudioSystem.getClip();
                clip.open(stream);
                clips.put(name, clip);
                return Optional.of(clip);
            }
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private void applyVolume(Clip clip, int volume) {
        if (!clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
            return;
        }
        FloatControl control = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        if (volume <= 0) {
            control.setValue(control.getMinimum());
            return;
        }
        float normalized = volume / 100f;
        float decibels = (float) (20.0 * Math.log10(normalized));
        control.setValue(Math.max(control.getMinimum(), Math.min(control.getMaximum(), decibels)));
    }

    private int clampVolume(int volume) {
        return Math.max(0, Math.min(100, volume));
    }
}
