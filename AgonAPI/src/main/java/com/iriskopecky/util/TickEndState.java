package com.iriskopecky.util;

public record TickEndState(
        int globalPortents,
        int health,
        int maxHealth,
        int viewDist,
        int meleeDamage,
        int meleeRange,
        int regenAmount
) {
}
