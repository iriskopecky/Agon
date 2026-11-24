package com.iriskopecky.agon.game.logic;

public class Coord {

    private int x;
    private int y;
    private int boardWidth;
    private int boardHeight;

    public Coord(int x, int y, GameConfig config) {
        this.x = x;
        this.y = y;
        this.boardWidth = config.width();
        this.boardHeight = config.height();
    }

    public int getX() {return x;}
    public int getY() {return y;}
}
