package com.iriskopecky.util;

public record Defense(DefenseType defense) {

    public enum DefenseType {
        DEFLECT,
        SHIELD,
        DODGE
    }
}
