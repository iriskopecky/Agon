package com.iriskopecky.agon.game;

import java.util.ArrayList;
import java.util.Random;

/**
 * A class representing the game board for one battle.
 */
public class GameBoard {

    private int width;
    private int height;
    private Position[][] board;

    /**
     * Generate a new empty game board.
     * @param config The current game config.
     */
    public GameBoard(GameConfig config) {
        this.width = config.width();
        this.height = config.height();
        //populate the board with empty game positions.
        this.board = new Position[width][height];
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                this.board[i][j] = new Position(i, j, config);
            }
        }
    }

    /**
     * Gets the game pos at the provided coords, normalizing them if necessary.
     * @param x The x coordinate.
     * @param y The y coordinate.
     * @return The pos on the game coord.
     */
    public Position getGamePos(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return board[Position.normalizeAxis(x, width)][Position.normalizeAxis(y, height)];
        }
        return board[x][y];
    }

    /**
     * Attempts to find a random position that is not already occupied.
     * @param seededRandom The game randomizer.
     * @return A position that was randomly found,
     * or null if no random unoccupied space was found in a certain number of random checks.
     */
    public Position findRandomUnoccupiedPos(Random seededRandom) {
        final int MAX_CHECKS = 25;
        for (int i = 0; i < MAX_CHECKS; i++) {
            int posX = seededRandom.nextInt(width);
            int posY = seededRandom.nextInt(height);
            Position pos = getGamePos(posX, posY);
            if (!pos.isPositionOccupied()) {
                return pos;
            }
        }

        return null;
    }


    /**
     * A class representing one coordinate on the board, keeping track of all the information associated with this coordinate.
     */
    public class Position {
        private Coord position;
        private ArrayList<ServerTeras> occupyingTeras;
        private boolean isResourceGeneratorTile;

        /**
         * Create a new gamePos representing one position on the board.
         * @param x The x position of the gamePos
         * @param y The y position of the gamePos
         * @param config The current battle config
         */
        public Position(int x, int y, GameConfig config) {
            occupyingTeras = new ArrayList<>();
            position = new Coord(x, y, config);
            isResourceGeneratorTile = false;
        }

        /**
         * Sets resource generator tile value to newVal
         */
        public void setIsResourceGeneratorTile(boolean newVal) {
            this.isResourceGeneratorTile = newVal;
        }

        /**
         * Adds the provided teras to this position.
         */
        public void addOccupyingTeras(ServerTeras teras) {
            occupyingTeras.add(teras);
        }

        /**
         * Returns whether this position is occupied by any Teras.
         */
        public boolean isPositionOccupied() {
            return !occupyingTeras.isEmpty();
        }

        /**
         * Finds the coord value normalized to the board axis max
         * @param coord The value of a coord on one axis.
         * @param max The maximum value of the board on that axis.
         * @return The coord normalized to fit within the board, wrapping around if necessary.
         */
        public static int normalizeAxis(int coord, int max) {
            //bound coord to board size
            coord = coord % max;
            //if coord was negative, wrap around
            if (coord < 0) {
                return max + coord;
            } else {
                return coord;
            }
        }

        /**
         * Get this game pos's coords.
         */
        public Coord getCoords() {
            return position;
        }
    }
}

