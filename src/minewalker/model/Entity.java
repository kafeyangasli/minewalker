package minewalker.model;

public abstract class Entity {
    private Position position;

    protected Entity(Position position) {
        this.position = position;
    }

    public Position getPosition() {
        return position;
    }

    protected void setPosition(Position position) {
        this.position = position;
    }
}
