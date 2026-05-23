package minewalker.model;

public class Player extends Entity {
    public Player(Position position) {
        super(position);
    }

    public void moveTo(Position position) {
        setPosition(position);
    }
}
