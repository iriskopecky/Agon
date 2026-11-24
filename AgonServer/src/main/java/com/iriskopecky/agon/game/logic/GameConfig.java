package com.iriskopecky.agon.game.logic;

import com.iriskopecky.agon.data.Teras;

import java.util.List;

public record GameConfig(

        int width,
        int height,
        List<Teras> competitors, //must be index-paired with competitorCount
        List<Integer> competitorCount, //must be index-paired with competitors
        int startingHealth,
        long gameSeed,
        int portentGeneratorRadius,
        int portentGeneratorDuration,
        int generalPortentsPerTick,
        int generatedPortentsPerTick,
        int defaultViewDist,
        int defaultMeleeRange,
        int expandedSightRangeTicks,
        int expandedSightRangeBoost,
        int baseRegen,
        int baseMeleeDamage,
        float attackCritMult,
        float defenseCritMult
) {}
