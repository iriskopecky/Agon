package com.iriskopecky.agon.game.logic;

import com.iriskopecky.agon.data.Teras;
import com.iriskopecky.agon.game.runners.RunnerBridge;
import com.iriskopecky.util.*;
import tools.jackson.databind.json.JsonMapper;

import java.util.UUID;

/**
 * This class represents the server view (method of truth) of every PlayerTeras.
 */
public class ServerTeras {

    private RunnerBridge bridge;
    private String runnerId;
    private JsonMapper mapper;

    private int maxHealth;
    private int currentHealth;
    private Coord pos;
    private int currentViewDist;
    private int baseViewDist;
    private int boostedViewDistDuration;
    private String identifier;
    private int baseMeleeRange;
    private int currentMeleeRange;
    private int baseRegen;
    private int currentRegen;
    private long templateId;
    private int baseMeleeDamage;
    private int currentMeleeDamage;

    public ServerTeras(GameConfig config, Teras templateTeras, Coord startingPos, RunnerBridge bridge) {
        //handle messaging the runner
        this.bridge = bridge;
        this.runnerId = UUID.randomUUID().toString();
        mapper = JsonMapper.builder().build();
        bridge.sendMessage(new Message(runnerId, Message.Command.MAKE, null));

        //prep defaults
        this.maxHealth = config.startingHealth();
        this.currentHealth = maxHealth;
        this.pos = startingPos;
        this.baseViewDist = config.defaultViewDist();
        this.currentViewDist = baseViewDist;
        this.identifier = "" + templateTeras.getId(); //TODO implement user-customizable identifiers
        this.baseMeleeRange = config.defaultMeleeRange();
        this.currentMeleeRange = baseMeleeRange;
        this.baseRegen = config.baseRegen();
        this.currentRegen = baseRegen;
        this.templateId = templateTeras.getId();
        this.baseMeleeDamage = config.baseMeleeDamage();
        this.currentMeleeDamage = baseMeleeDamage;
    }

    public void initialize(InitialState initialState) {
        bridge.sendMessage(new Message(runnerId, Message.Command.INIT, initialState));
    }

    public void scanResults(Scan scan) {
        bridge.sendMessage(new Message(runnerId, Message.Command.SCAN, scan));
    }

    public ActionPair decideActions() {
        Response response = bridge.sendMessage(new Message(runnerId, Message.Command.DECIDE_ACTIONS, null));
        if (response == null || response.response() != Response.ResponseType.ACTION_DECISION) return null;
        return mapper.convertValue(response.data(), ActionPair.class);

    }

    public Attack attack(TerasView defender) {
        Response response = bridge.sendMessage(new Message(runnerId, Message.Command.ATTACK, defender));
        if (response == null || response.response() != Response.ResponseType.ATTACK) return null;
        return mapper.convertValue(response.data(), Attack.class);
    }

    public Defense defend(TerasView attacker) {
        Response response = bridge.sendMessage(new Message(runnerId, Message.Command.DEFEND, attacker));
        if (response == null || response.response() != Response.ResponseType.DEFENSE) return null;
        return mapper.convertValue(response.data(), Defense.class);
    }

    public void fightResult(FightResult result) {
        bridge.sendMessage(new Message(runnerId, Message.Command.FIGHT_RESULT, result));
    }

    public Purchase spendResources(UpgradeState upgradeState, PortentState portentState) {
        Response response = bridge.sendMessage(new Message(runnerId, Message.Command.SPEND_RESOURCES, new Pair<>(upgradeState, portentState)));
        if (response == null || response.response() != Response.ResponseType.PURCHASE) return null;
        return mapper.convertValue(response.data(), Purchase.class);
    }

    public boolean shouldMate(TerasView ally, PortentState portentState) {
        Response response = bridge.sendMessage(new Message(runnerId, Message.Command.SHOULD_MATE, new Pair<>(ally, portentState)));
        if (response == null || response.response() != Response.ResponseType.WANTS_TO_MATE) return false;
        return mapper.convertValue(response.data(), Boolean.class);
    }

    public Move move() {
        Response response = bridge.sendMessage(new Message(runnerId, Message.Command.MOVE, null));
        if (response == null || response.response() != Response.ResponseType.MOVE) return null;
        return mapper.convertValue(response.data(), Move.class);
    }

    public void tickEndState(TickEndState state) {
        bridge.sendMessage(new Message(runnerId, Message.Command.TICK_END_STATE, state));
    }

    public void processRegen() {
        currentHealth = Math.min(currentHealth + currentRegen, maxHealth);
        //TODO this should probably be communicated to the user.
    }

    public void boostScanRange(GameConfig config) {
        boostedViewDistDuration += config.expandedSightRangeTicks();
        //repeated boosts only extend duration, not dist even further.
        currentViewDist = baseViewDist + config.expandedSightRangeBoost();
    }

    /**
     * Applies damage to this teras
     * @param damage The damage to apply
     * @return True if the teras has died as a result. False, otherwise
     */
    public boolean applyDamage(int damage) {
        currentHealth -= damage;
        return currentHealth <= 0;
    }


    public void tickDownViewDist() {
        if (boostedViewDistDuration > 0) {
            boostedViewDistDuration--;
        }

        if (boostedViewDistDuration == 0) {
            currentViewDist = baseViewDist;
        }
    }







    public Coord getPos() {
        return pos;
    }

    public void setPos(Coord pos) {
        this.pos = pos;
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public void setCurrentHealth(int currentHealth) {
        this.currentHealth = currentHealth;
    }

    public int getCurrentViewDist() {
        return currentViewDist;
    }

    public void setCurrentViewDist(int currentViewDist) {
        this.currentViewDist = currentViewDist;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public int getCurrentMeleeRange() {
        return currentMeleeRange;
    }

    public void setCurrentMeleeRange(int currentMeleeRange) {
        this.currentMeleeRange = currentMeleeRange;
    }

    public long getTemplateId() {return templateId;}

    public int getCurrentMeleeDamage() {
        return currentMeleeDamage;
    }

    public int getMaxHealth() {return maxHealth;}

    public int getCurrentRegen() {return currentRegen;}
}
