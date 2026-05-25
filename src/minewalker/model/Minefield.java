package minewalker.model;

import java.util.ArrayList;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

public class Minefield {
    private static final double START_REVEAL_CHANCE = 0.72;

    private final GameSettings settings;
    private final List<List<Tile>> tiles;
    private final Player player;
    private final int totalSafeTiles;
    private int discoveredSafeTiles;
    private boolean ended;
    private boolean won;

    public Minefield(GameSettings settings) {
        this(settings, randomPosition(settings));
    }

    public Minefield(GameSettings settings, Position spawn) {
        this.settings = settings;
        Set<Position> protectedPositions = protectedStartArea(settings, spawn);
        Set<Position> minePositions = generateMinePositions(settings, protectedPositions);
        validateSafeTilesAreConnected(settings, spawn, minePositions);
        this.tiles = buildTiles(settings, minePositions);
        this.player = new Player(spawn);
        this.totalSafeTiles = settings.getRows() * settings.getColumns() - minePositions.size();
        revealStartArea(protectedPositions);
    }

    public MoveOutcome move(Direction direction) {
        if (ended) {
            return MoveOutcome.IGNORED;
        }

        Position target = player.getPosition().move(direction);
        if (!contains(target)) {
            return MoveOutcome.BLOCKED;
        }
        if (getTile(target.y(), target.x()).isFlagged()) {
            return MoveOutcome.BLOCKED;
        }

        player.moveTo(target);
        TileVisitResult result = visitCurrentTile();
        if (result == TileVisitResult.LOSE) {
            ended = true;
            won = false;
            revealAllMines();
            return MoveOutcome.LOST;
        }
        if (discoveredSafeTiles >= totalSafeTiles) {
            ended = true;
            won = true;
            return MoveOutcome.WON;
        }
        return MoveOutcome.MOVED;
    }

    public Tile getTile(int row, int column) {
        return tiles.get(row).get(column);
    }

    public boolean toggleFlag(Direction direction) {
        Position target = player.getPosition().move(direction);
        if (!contains(target)) {
            return false;
        }

        Tile tile = getTile(target.y(), target.x());
        if (tile.isRevealed()) {
            return false;
        }

        tile.toggleFlagged();
        return true;
    }

    public int countAdjacentMines(Position position) {
        int mines = 0;
        for (int y = position.y() - 1; y <= position.y() + 1; y++) {
            for (int x = position.x() - 1; x <= position.x() + 1; x++) {
                Position neighbor = new Position(x, y);
                if (!neighbor.equals(position) && contains(neighbor) && getTile(y, x) instanceof MineTile) {
                    mines++;
                }
            }
        }
        return mines;
    }

    public Player getPlayer() {
        return player;
    }

    public int getRows() {
        return settings.getRows();
    }

    public int getColumns() {
        return settings.getColumns();
    }

    public int getDiscoveredSafeTiles() {
        return discoveredSafeTiles;
    }

    public int getTotalSafeTiles() {
        return totalSafeTiles;
    }

    public boolean isWon() {
        return won;
    }

    public void explodeAllMines() {
        ended = true;
        won = false;
        revealAllMines();
    }

    private TileVisitResult visitCurrentTile() {
        Tile tile = getTile(player.getPosition().y(), player.getPosition().x());
        boolean alreadyRevealed = tile.isRevealed();
        TileVisitResult result = tile.visit(player);
        if (!alreadyRevealed && tile instanceof SafeTile) {
            discoveredSafeTiles++;
        }
        return result;
    }

    private boolean contains(Position position) {
        return position.x() >= 0 && position.x() < settings.getColumns()
                && position.y() >= 0 && position.y() < settings.getRows();
    }

    private void revealAllMines() {
        for (List<Tile> row : tiles) {
            for (Tile tile : row) {
                if (tile instanceof MineTile) {
                    tile.visit(player);
                }
            }
        }
    }

    private static Position randomPosition(GameSettings settings) {
        Random random = new Random();
        return new Position(random.nextInt(settings.getColumns()), random.nextInt(settings.getRows()));
    }

    private void revealStartArea(Set<Position> protectedPositions) {
        Random random = new Random();
        for (Position position : protectedPositions) {
            if (position.equals(player.getPosition()) || random.nextDouble() <= START_REVEAL_CHANCE) {
                Tile tile = getTile(position.y(), position.x());
                boolean alreadyRevealed = tile.isRevealed();
                TileVisitResult result = tile.visit(player);
                if (result == TileVisitResult.CONTINUE && !alreadyRevealed && tile instanceof SafeTile) {
                    discoveredSafeTiles++;
                }
            }
        }
    }

    private static Set<Position> generateMinePositions(GameSettings settings, Set<Position> protectedPositions) {
        int tileCount = settings.getRows() * settings.getColumns();
        int mineCount = Math.max(1, tileCount * settings.getMinePercentage() / 100);
        mineCount = Math.min(mineCount, tileCount - protectedPositions.size());
        int safeTileCount = tileCount - mineCount;
        Random random = new Random();

        Set<Position> safePositions = generateConnectedSafePositions(settings, protectedPositions, safeTileCount, random);
        return minePositionsFromSafeTiles(settings, safePositions);
    }

    private static void validateSafeTilesAreConnected(GameSettings settings, Position spawn, Set<Position> minePositions) {
        int tileCount = settings.getRows() * settings.getColumns();
        int safeTileCount = tileCount - minePositions.size();
        int reachableSafeTileCount = countReachableSafeTiles(settings, spawn, minePositions);
        if (reachableSafeTileCount != safeTileCount) {
            throw new IllegalStateException("Generated minefield has unreachable safe tiles.");
        }
    }

    private static int countReachableSafeTiles(GameSettings settings, Position spawn, Set<Position> minePositions) {
        Set<Position> visited = new HashSet<>();
        Queue<Position> queue = new ArrayDeque<>();
        visited.add(spawn);
        queue.add(spawn);

        while (!queue.isEmpty()) {
            Position current = queue.remove();
            for (Direction direction : Direction.values()) {
                Position neighbor = current.move(direction);
                if (contains(settings, neighbor) && !minePositions.contains(neighbor) && !visited.contains(neighbor)) {
                    visited.add(neighbor);
                    queue.add(neighbor);
                }
            }
        }

        return visited.size();
    }

    private static Set<Position> generateConnectedSafePositions(GameSettings settings, Set<Position> protectedPositions,
            int safeTileCount, Random random) {
        Set<Position> safePositions = new HashSet<>(protectedPositions);
        List<Position> frontier = new ArrayList<>();
        Set<Position> frontierSet = new HashSet<>();

        for (Position position : safePositions) {
            addFrontierNeighbors(settings, position, safePositions, frontier, frontierSet);
        }

        while (safePositions.size() < safeTileCount) {
            if (frontier.isEmpty()) {
                throw new IllegalStateException("Could not expand connected safe tiles.");
            }

            int index = chooseFrontierIndex(settings, safePositions, frontier, random);
            Position next = frontier.remove(index);
            frontierSet.remove(next);
            if (!safePositions.add(next)) {
                continue;
            }
            addFrontierNeighbors(settings, next, safePositions, frontier, frontierSet);
        }

        return safePositions;
    }

    private static void addFrontierNeighbors(GameSettings settings, Position position, Set<Position> safePositions,
            List<Position> frontier, Set<Position> frontierSet) {
        for (Direction direction : Direction.values()) {
            Position neighbor = position.move(direction);
            if (contains(settings, neighbor) && !safePositions.contains(neighbor) && frontierSet.add(neighbor)) {
                frontier.add(neighbor);
            }
        }
    }

    private static int chooseFrontierIndex(GameSettings settings, Set<Position> safePositions, List<Position> frontier,
            Random random) {
        int totalWeight = 0;
        int[] weights = new int[frontier.size()];
        for (int index = 0; index < frontier.size(); index++) {
            int adjacentSafeTiles = countAdjacentSafeTiles(settings, safePositions, frontier.get(index));
            int weight = 1 + Math.max(0, 4 - adjacentSafeTiles) * 2;
            weights[index] = weight;
            totalWeight += weight;
        }

        int roll = random.nextInt(totalWeight);
        for (int index = 0; index < weights.length; index++) {
            roll -= weights[index];
            if (roll < 0) {
                return index;
            }
        }
        return frontier.size() - 1;
    }

    private static int countAdjacentSafeTiles(GameSettings settings, Set<Position> safePositions, Position position) {
        int count = 0;
        for (Direction direction : Direction.values()) {
            Position neighbor = position.move(direction);
            if (contains(settings, neighbor) && safePositions.contains(neighbor)) {
                count++;
            }
        }
        return count;
    }

    private static Set<Position> minePositionsFromSafeTiles(GameSettings settings, Set<Position> safePositions) {
        Set<Position> minePositions = new HashSet<>();
        for (int row = 0; row < settings.getRows(); row++) {
            for (int column = 0; column < settings.getColumns(); column++) {
                Position position = new Position(column, row);
                if (!safePositions.contains(position)) {
                    minePositions.add(position);
                }
            }
        }
        return minePositions;
    }

    private static boolean contains(GameSettings settings, Position position) {
        return position.x() >= 0 && position.x() < settings.getColumns()
                && position.y() >= 0 && position.y() < settings.getRows();
    }

    private static Set<Position> protectedStartArea(GameSettings settings, Position spawn) {
        Set<Position> positions = new HashSet<>();
        for (int y = spawn.y() - 1; y <= spawn.y() + 1; y++) {
            for (int x = spawn.x() - 1; x <= spawn.x() + 1; x++) {
                Position position = new Position(x, y);
                if (contains(settings, position)) {
                    positions.add(position);
                }
            }
        }
        return positions;
    }

    private static List<List<Tile>> buildTiles(GameSettings settings, Set<Position> minePositions) {
        List<List<Tile>> rows = new ArrayList<>();
        for (int row = 0; row < settings.getRows(); row++) {
            List<Tile> currentRow = new ArrayList<>();
            for (int column = 0; column < settings.getColumns(); column++) {
                Position position = new Position(column, row);
                currentRow.add(minePositions.contains(position) ? new MineTile(position) : new SafeTile(position));
            }
            rows.add(currentRow);
        }
        return rows;
    }
}
