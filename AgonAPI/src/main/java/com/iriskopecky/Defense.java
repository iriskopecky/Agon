package com.iriskopecky;

public record Defense(DefenseType defense) {

    public enum DefenseType {
        DEFLECT,
        SHIELD,
        DODGE
    }
}
