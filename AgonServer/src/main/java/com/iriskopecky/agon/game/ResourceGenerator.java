package com.iriskopecky.agon.game;


import java.util.ArrayList;

public class ResourceGenerator {

    private GameBoard.Position position;
    private int ticksRemaining;
    private int radius;

    public ResourceGenerator(GameBoard.Position pos, GameBoard board, GameConfig config) {
        this.position = pos;
        this.radius = config.resourceGeneratorRadius();
        this.ticksRemaining = config.resourceGeneratorDuration();
    }

    /**
     * Resolve all territory in range of the resource generator
     * @param board The game board.
     * @return A list of territory positions within radius of the generator.
     */
    public ArrayList<GameBoard.Position> resolveGeneratorTerritory(GameBoard board) {
        ArrayList<GameBoard.Position> output = new ArrayList<>();

        //resolve all coords in radius
        int rootX = position.getCoords().getX();
        int rootY = position.getCoords().getY();
        int lowerX = rootX - (radius - 1);
        int lowerY = rootY - (radius - 1);
        int upperX = rootX + (radius - 1);
        int upperY = rootY + (radius - 1);

        for (int x = lowerX; x <= upperX; x++) {
            for (int y = lowerY; y <= upperY; y++) {
                GameBoard.Position pos = board.getGamePos(x, y);
                output.add(pos);
            }
        }

        return output;
    }
}
