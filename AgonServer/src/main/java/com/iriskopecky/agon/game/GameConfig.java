package com.iriskopecky.agon.game;

import com.iriskopecky.agon.data.Teras;

import java.util.List;
import java.util.Map;
import java.util.Random;

public record GameConfig(

        int width,
        int height,
        List<Teras> competitors, //must be index-paired with competitorCount
        List<Integer> competitorCount, //must be index-paired with competitors
        int startingHealth,
        long gameSeed,
        int resourceGeneratorRadius,
        int resourceGeneratorDuration
) {}
