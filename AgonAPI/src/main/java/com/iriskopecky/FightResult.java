package com.iriskopecky;

public record FightResult(
        boolean critHit,
        boolean critDefense,
        boolean killed,
        boolean wasDefense,
        int damageDealt
) {
}
