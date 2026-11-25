package com.iriskopecky.agon.game.logic;

import java.util.ArrayList;
import java.util.Random;
import com.iriskopecky.util.Move.Direction;

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
     * Gets a random position on the board.
     * @param seededRandom The random to use for repeatability.
     * @return A random position on the board.
     */
    public Position getRandomPos(Random seededRandom) {
        int x = seededRandom.nextInt(width);
        int y = seededRandom.nextInt(height);
        return board[x][y];
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

    public int getWidth() {return width;}
    public int getHeight() {return height;}

    /**
     * A class representing one coordinate on the board, keeping track of all the information associated with this coordinate.
     */
    public class Position {
        private Coord position;
        private ArrayList<ServerTeras> occupyingTeras;
        private boolean isPortentGeneratorTile;
        private int portents;

        /**
         * Create a new gamePos representing one position on the board.
         * @param x The x position of the gamePos
         * @param y The y position of the gamePos
         * @param config The current battle config
         */
        public Position(int x, int y, GameConfig config) {
            occupyingTeras = new ArrayList<>();
            position = new Coord(x, y, config);
            isPortentGeneratorTile = false;
            portents = 0;
        }

        /**
         * Sets resource generator tile value to newVal
         */
        public void setIsPortentGeneratorTile(boolean newVal) {
            this.isPortentGeneratorTile = newVal;
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

        public int getPortents() {return portents;}

        public void addPortent() {portents++;}

        public void removePortent() {portents--;}

        public ArrayList<ServerTeras> getOccupyingTeras() {return occupyingTeras;}

        public int getDiffX(Position other, int max) {
            int oX = other.getCoords().getX();
            int tX = this.getCoords().getX();
            int formerX = Math.min(oX, tX);
            int latterX = Math.max(oX, tX);
            int directDist = Math.abs(oX - tX);
            int indirectDist = (max - latterX) + formerX;
            return Math.min(directDist, indirectDist);
        }

        public int getDiffY(Position other, int max) {
            int oY = other.getCoords().getY();
            int tY = this.getCoords().getY();
            int formerY = Math.min(oY, tY);
            int latterY = Math.max(oY, tY);
            int directDist = Math.abs(oY - tY);
            int indirectDist = (max - latterY) + formerY;
            return Math.min(directDist, indirectDist);
        }

        /**
         * Returns the maximum of the distances along each component axis, used in range calculations.
         * @param other The other coord to compare to.
         * @return The range distance between these coords.
         */
        public int getComponentDistance(Position other, int width, int height) {
            return Math.max(getDiffX(other, width), getDiffY(other, height));
        }

        public Position offset(Direction direction, int distance, GameBoard board) {
            int xOffset = 0;
            int yOffset = 0;
            if (direction == Direction.EAST || direction == Direction.SOUTHEAST || direction == Direction.NORTHEAST) {
                xOffset = distance;
            }
            if (direction == Direction.WEST || direction == Direction.SOUTHWEST || direction == Direction.NORTHWEST) {
                xOffset = -1 * distance;
            }
            if (direction == Direction.NORTH || direction == Direction.NORTHWEST || direction == Direction.NORTHEAST) {
                yOffset = -1 * distance;
            }
            if (direction == Direction.SOUTH || direction == Direction.SOUTHWEST || direction == Direction.SOUTHEAST) {
                yOffset = distance;
            }

            return board.getGamePos(getCoords().getX() + xOffset, getCoords().getY() + yOffset);
        }
    }
}

