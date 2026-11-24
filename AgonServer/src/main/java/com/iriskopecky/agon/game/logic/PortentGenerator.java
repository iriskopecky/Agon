package com.iriskopecky.agon.game.logic;


import java.util.ArrayList;
import java.util.Random;

public class PortentGenerator {

    private GameBoard.Position position;
    private int ticksRemaining;
    private int radius;

    public PortentGenerator(GameBoard.Position pos, GameBoard board, GameConfig config) {
        this.position = pos;
        this.radius = config.portentGeneratorRadius();
        this.ticksRemaining = config.portentGeneratorDuration();
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

    /**
     * Randomly distribute portents to this generator's territory.
     * @param generatorTerritory The list of positions owned by this generator.
     * @param portents The number of portents to randomly assign.
     * @param seededRandom The random number generator to use.
     */
    public void generatePortents(ArrayList<GameBoard.Position> generatorTerritory, int portents, Random seededRandom) {
        for (int i = 0; i < portents; i++) {
            int index = seededRandom.nextInt(generatorTerritory.size());
            generatorTerritory.get(index).addPortent();
        }
    }
}
