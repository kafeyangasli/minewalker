package minewalker.model;

import java.awt.Color;

public class MineTile extends Tile {
    public MineTile(Position position) {
        super(position);
    }

    @Override
    public TileVisitResult visit(Player player) {
        reveal();
        return TileVisitResult.LOSE;
    }

    @Override
    public Color getDisplayColor(boolean playerHere) {
        if (playerHere) {
            return new Color(235, 82, 82);
        }
        return isRevealed() ? new Color(80, 20, 30) : Color.BLACK;
    }

    @Override
    public String getDisplayText(boolean playerHere) {
        if (isFlagged()) {
            return "F";
        }
        return playerHere ? "X" : isRevealed() ? "*" : "";
    }
}
