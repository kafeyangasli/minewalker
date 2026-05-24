package minewalker.ui;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import minewalker.model.Direction;

final class TextureManager {
    private static final int SPRITE_SIZE = 32;
    private static final int SPRITE_STRIDE = 32;
    private static final int COLUMNS = 9;
    private static final int UNREVEALED = 0;
    private static final int FLAG = 1;
    private static final int ONE_BOMB = 2;
    private static final int BOMB_TILE = 8;
    private static final int STEPPED_TILE = 9;
    private static final int BUTTON_LEFT = 10;
    private static final int BUTTON_CENTER_LEFT = 11;
    private static final int BUTTON_CENTER_RIGHT = 12;
    private static final int BUTTON_RIGHT = 13;
    private static final int PLUS = 14;
    private static final int MINUS = 15;
    private static final int CHECKBOX_EMPTY = 16;
    private static final int CHECKBOX_CHECKED = 17;
    private static final int PLAYER_RIGHT = 18;
    private static final int PLAYER_LEFT = 19;
    private static final int PLAYER_UP = 20;
    private static final int PLAYER_DOWN = 21;
    private static final int PLAYER_DEATH = 22;
    private static final int SLIDER_LEFT = 23;
    private static final int SLIDER_CENTER_LEFT = 24;
    private static final int SLIDER_CENTER_RIGHT = 25;
    private static final int SLIDER_RIGHT = 26;
    private static final int PLAYER_HEAD = 27;
    private static final int SLIDER_POINTER = 28;
    private static final int LOGO_1_LEFT = 29;
    private static final int LOGO_1_RIGHT = 30;
    private static final int LOGO_2_LEFT = 31;
    private static final int LOGO_2_RIGHT = 32;
    private static final int LOGO_3_LEFT = 33;
    private static final int LOGO_3_RIGHT = 34;

    private static final TextureManager INSTANCE = new TextureManager();

    private final BufferedImage sheet;

    private TextureManager() {
        this.sheet = loadSheet();
    }

    static TextureManager get() {
        return INSTANCE;
    }

    Image hiddenTile() {
        return sprite(UNREVEALED);
    }

    Image revealedTile() {
        return sprite(STEPPED_TILE);
    }

    Image numberTile(int number) {
        if (number >= 1 && number <= 6) {
            return sprite(ONE_BOMB + number - 1);
        }
        return revealedTile();
    }

    Image flagTile() {
        return sprite(FLAG);
    }

    Image mineTile() {
        return sprite(BOMB_TILE);
    }

    Image buttonLeft() {
        return sprite(BUTTON_LEFT);
    }

    Image buttonCenterLeft() {
        return sprite(BUTTON_CENTER_LEFT);
    }

    Image buttonCenterRight() {
        return sprite(BUTTON_CENTER_RIGHT);
    }

    Image buttonRight() {
        return sprite(BUTTON_RIGHT);
    }

    Image plus() {
        return sprite(PLUS);
    }

    Image minus() {
        return sprite(MINUS);
    }

    Image checkboxEmpty() {
        return sprite(CHECKBOX_EMPTY);
    }

    Image checkboxChecked() {
        return sprite(CHECKBOX_CHECKED);
    }

    Image player(Direction dir) {
        switch (dir) {
            case Direction.RIGHT:
                return sprite(PLAYER_RIGHT);
            case Direction.LEFT:
                return sprite(PLAYER_LEFT);
            case Direction.UP:
                return sprite(PLAYER_UP);
            case Direction.DOWN:
                return sprite(PLAYER_DOWN);
        }
        return sheet;
    }

    Image player(String type) {
        switch (type) {
            case "death":
                return sprite(PLAYER_DEATH);
            case "spawn":
                return sprite(PLAYER_HEAD);
            default:
                return sprite(PLAYER_DEATH);
        }
    }

    Image sliderLeft() {
        return sprite(SLIDER_LEFT);
    }

    Image sliderCenterLeft() {
        return sprite(SLIDER_CENTER_LEFT);
    }

    Image sliderCenterRight() {
        return sprite(SLIDER_CENTER_RIGHT);
    }

    Image sliderRight() {
        return sprite(SLIDER_RIGHT);
    }

    Image sliderPointer()  {
        return sprite(SLIDER_POINTER);
    }

    Image logo1Left() {
        return sprite(LOGO_1_LEFT);
    }

    Image logo1Right() {
        return sprite(LOGO_1_RIGHT);
    }

    Image logo2Left() {
        return sprite(LOGO_2_LEFT);
    }

    Image logo2Right() {
        return sprite(LOGO_2_RIGHT);
    }

    Image logo3Left() {
        return sprite(LOGO_3_LEFT);
    }

    Image logo3Right() {
        return sprite(LOGO_3_RIGHT);
    }

    boolean hasTexture() {
        return sheet != null;
    }

    private Image sprite(int index) {
        if (sheet == null) {
            return null;
        }
        int x = (index % COLUMNS) * SPRITE_STRIDE;
        int y = (index / COLUMNS) * SPRITE_STRIDE;
        if (x + SPRITE_SIZE > sheet.getWidth() || y + SPRITE_SIZE > sheet.getHeight()) {
            return null;
        }
        return sheet.getSubimage(x, y, SPRITE_SIZE, SPRITE_SIZE);
    }

    private BufferedImage loadSheet() {
        try (InputStream stream = TextureManager.class.getResourceAsStream("/textures/main_texture.bmp")) {
            if (stream != null) {
                return ImageIO.read(stream);
            }
        } catch (IOException ignored) {
            // Flat colors remain available if the texture cannot be loaded.
        }
        return null;
    }
}
