package minewalker.model;

import java.awt.Color;

public class SafeTile extends Tile {
    public SafeTile(Position position) {
        super(position);
    }

    @Override
    public TileVisitResult visit(Player player) {
        reveal();
        return TileVisitResult.CONTINUE;
    }

    @Override
    public Color getDisplayColor(boolean playerHere) {
        if (playerHere) {
            return new Color(66, 227, 158);
        }
        return isRevealed() ? new Color(26, 34, 42) : Color.BLACK;
    }

    @Override
    public String getDisplayText(boolean playerHere) {
        if (isFlagged()) {
            return "F";
        }
        return isRevealed() ? "." : "";
    }
}
