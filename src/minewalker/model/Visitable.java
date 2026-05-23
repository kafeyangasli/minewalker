package minewalker.model;

public interface Visitable<TActor, TResult> {
    TResult visit(TActor actor);
}
