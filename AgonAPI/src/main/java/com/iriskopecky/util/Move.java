package com.iriskopecky.util;

public record Move(Direction moveDirection) {

    public enum Direction {
        NORTH,
        SOUTH,
        WEST,
        EAST,
        NORTHWEST,
        NORTHEAST,
        SOUTHWEST,
        SOUTHEAST
    }
}
