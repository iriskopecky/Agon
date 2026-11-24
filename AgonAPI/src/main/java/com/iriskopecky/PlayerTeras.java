package com.iriskopecky;

/**
 * This class represents the contract of methods that all terata must implement,
 * the means by which the server communicates to the teras.
 */
public abstract class PlayerTeras {

    public abstract void initialize(InitialState initialState);

    public abstract void scanResults(Scan scan);

    public abstract ActionPair decideActions();

    public abstract Attack attack(TerasView defender);

    public abstract Defense defend(TerasView attacker);

    public abstract void fightResult(FightResult result);

    public abstract Purchase spendResources(UpgradeState upgradeState, PortentState portentState);

    public abstract boolean shouldMate(TerasView ally, PortentState portentState);

    public abstract Move move();
}
