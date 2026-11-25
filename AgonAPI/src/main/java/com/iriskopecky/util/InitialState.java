package com.iriskopecky.util;

public record InitialState(
        int maxHealth,
        int meleeDamage,
        int meleeRange,
        int viewDist,
        int portents,
        int boardWidth,
        int boardHeight,
        int enemyTeams,
        int healthRegenOnRequest,
        float attackCritMult,
        float defenseCritMult
) {
}
