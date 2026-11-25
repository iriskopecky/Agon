package com.iriskopecky.agon.services;

import com.iriskopecky.agon.data.Teras;
import com.iriskopecky.agon.data.TerasRepository;
import com.iriskopecky.agon.game.logic.Battle;
import com.iriskopecky.agon.game.logic.GameConfig;
import com.iriskopecky.agon.game.runners.RunnerBridge;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class BattleService {

    /**
     * For testing purposes, run a set battle. This should be replaced with more sophisticated battle choice later.
     * @param competitors The competitors to be involved in this match.
     * @return True if the battle succeeded, false if an issue occured.
     */
    public boolean runTestingBattle(List<Teras> competitors) {
        //prep config
        long seed = 1;
        ArrayList<Integer> competitorCount = new ArrayList<>();
        for (Teras teras : competitors) {
            competitorCount.add(5);
        }
        GameConfig config = new GameConfig(400, 400, List.copyOf(competitors), competitorCount, 3,
                seed, 5, 100, 20, 10,
                2, 1, 5, 2, 1,
                1, 1.5f, 0.5f, 0);

        //prep the runner bridges
        ArrayList<RunnerBridge> bridges = new ArrayList<>();
        for (Teras teras : competitors) {
            RunnerBridge bridge = new RunnerBridge(teras);
            bridges.add(bridge);
            if (!bridge.start()) {
                bridges.forEach(RunnerBridge::stop);
                return false;
            }
        }

        //start the battle
        Battle battle = new Battle(config, bridges);

        int maxTicks = 500;
        for (int tick = 0; tick < maxTicks; tick++) {
            battle.tick();
        }

        //close all bridges.
        bridges.forEach(RunnerBridge::stop);

        return true;
    }
}
