package com.iriskopecky.agon.game;

import com.iriskopecky.agon.data.Teras;

import java.util.List;

/**
 * This class represents the server view (method of truth) of every PlayerTeras.
 */
public class ServerTeras {

    private int maxHealth;
    private int currentHealth;

    public ServerTeras(GameConfig config, Teras templateTeras, Coord startingPos) {
        this.maxHealth = config.startingHealth();
        this.currentHealth = maxHealth;
    }
}
