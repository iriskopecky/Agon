package com.iriskopecky.agon.game.logic;

import com.iriskopecky.*;
import com.iriskopecky.agon.data.Teras;

/**
 * This class represents the server view (method of truth) of every PlayerTeras.
 */
public class ServerTeras {

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

    public ServerTeras(GameConfig config, Teras templateTeras, Coord startingPos) {
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

    }

    public void scanResults(Scan scan) {

    }

    public ActionPair decideActions() {
        return null;
    }

    public Attack attack(TerasView defender) {
        return null;
    }

    public Defense defend(TerasView attacker) {
        return null;
    }

    public void fightResult(FightResult result) {

    }

    public Purchase spendResources(UpgradeState upgradeState, PortentState portentState) {
        return null;
    }

    public boolean shouldMate(TerasView ally, PortentState portentState) {
        return false;
    }

    public Move move() {
        return null;
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
}
