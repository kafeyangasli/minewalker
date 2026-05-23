package minewalker.model;

import java.awt.Color;

public abstract class Tile implements Visitable<Player, TileVisitResult> {
    private final Position position;
    private boolean revealed;
    private boolean flagged;

    protected Tile(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }

    public boolean isRevealed() {
        return revealed;
    }

    public boolean isFlagged() {
        return flagged;
    }

    public void setFlagged(boolean flagged) {
        if (!revealed) {
            this.flagged = flagged;
        }
    }

    public void toggleFlagged() {
        setFlagged(!flagged);
    }

    protected void reveal() {
        flagged = false;
        revealed = true;
    }

    public abstract Color getDisplayColor(boolean playerHere);

    public abstract String getDisplayText(boolean playerHere);
}
