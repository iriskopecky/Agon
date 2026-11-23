package com.iriskopecky.agon.game;

import com.iriskopecky.agon.data.Teras;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Battle {

    private GameConfig config;
    private GameBoard board;
    private ArrayList<ServerTeras> players;
    private Random seededRandom;
    private ResourceGenerator generator = null;
    private ArrayList<GameBoard.Position> generatorTerritory = null;

    /**
     * Initialize the board to be ready for a fight.
     * @param config The game config for this battle.
     */
    public Battle(GameConfig config) {
        this.config = config;
        this.board = new GameBoard(config);
        this.seededRandom = new Random(config.gameSeed());
        generateTeras();
        spawnResourceGenerator(false);
    }

    /**
     * Generate teras on the game board
     */
    public void generateTeras() {
        players = new ArrayList<>();
        //for every teras type, generate serverTeras
        for (int terasIndex = 0; terasIndex < config.competitors().size(); terasIndex++) {
            Teras templateTeras = config.competitors().get(terasIndex);
            int numToGenerate = config.competitorCount().get(terasIndex);
            //generate every teras instance
            for (int i = 0; i < numToGenerate; i++) {
                GameBoard.Position pos = board.findRandomUnoccupiedPos(seededRandom);
                ServerTeras teras = new ServerTeras(config, templateTeras, pos.getCoords());
                players.add(teras);

            }
        }
    }

    /**
     * Spawns a new resource generator, clearing the old one.
     * @param random Whether to randomly pick a new location or to use the center of the game board.
     */
    public void spawnResourceGenerator(boolean random) {
        //resolve the spawn location
        int x;
        int y;
        if (random) {
            x = seededRandom.nextInt(config.width());
            y = seededRandom.nextInt(config.height());
        } else {
            x = config.width() / 2;
            y = config.height() / 2;
        }

        //clear the old generator positions
        if (generator != null) {
            generatorTerritory.forEach((pos) -> {
                pos.setIsResourceGeneratorTile(false);
            });
            generatorTerritory.clear();
        }

        //get root location
        GameBoard.Position rootPos = board.getGamePos(x, y);

        //build generator
        generator = new ResourceGenerator(rootPos, board, config);
        generatorTerritory = generator.resolveGeneratorTerritory(board);

        //update locations to be generators
        generatorTerritory.forEach((pos) -> {
            pos.setIsResourceGeneratorTile(true);
        });
    }
}
