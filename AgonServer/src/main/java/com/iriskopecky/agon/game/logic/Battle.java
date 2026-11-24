package com.iriskopecky.agon.game.logic;

import com.iriskopecky.*;
import com.iriskopecky.agon.data.Teras;
import com.iriskopecky.agon.game.Pair;

import java.util.*;

import static com.iriskopecky.ActionPair.SecondaryAction.*;
import static com.iriskopecky.ActionPair.PrimaryAction.*;
import com.iriskopecky.agon.game.logic.GameBoard.Position;
import com.iriskopecky.Attack.AttackType;
import com.iriskopecky.Defense.DefenseType;
import org.springframework.security.core.parameters.P;

public class Battle {

    private GameConfig config;
    private GameBoard board;
    private ArrayList<ServerTeras> players;
    private Random seededRandom;
    private PortentGenerator generator = null;
    private ArrayList<GameBoard.Position> generatorTerritory = null;
    private Map<Long, TerasGlobalData> globalData;

    /**
     * Initialize the board to be ready for a fight.
     * @param config The game config for this battle.
     */
    public Battle(GameConfig config) {
        this.config = config;
        this.board = new GameBoard(config);
        this.seededRandom = new Random(config.gameSeed());
        generateTeras();
        spawnPortentGenerator(false);
    }

    /**
     * Run one game tick.
     */
    public void tick() {
        //distribute resources onto the map
        distributePortents();

        //pass sight info
        generateSightLines();

        //action stage
        ActionWrapper parsedActions = determineActions();

        //attack stage
        MeleeAttackReturn result = processMeleeAttacks(parsedActions);
        //notify teras of the results
        result.results.forEach((pending) -> {
            pending.teras.fightResult(pending.result);
        });
        //clear the killed
        players.removeAll(result.killed);
        parsedActions.terasPendingMove().removeAll(result.killed);
        parsedActions.terasPendingResourceCollection().removeAll(result.killed);
        parsedActions.terasPendingRegen().removeAll(result.killed);
        parsedActions.terasPendingScanRangeBoost().removeAll(result.killed);
        result.killed.forEach((killed) -> {
            board.getGamePos(killed.getPos().getX(), killed.getPos().getY()).getOccupyingTeras().remove(killed);
        });

        //process regens
        processRegens(parsedActions);
        //process resource collection
        processPortentCollection(parsedActions);
        //process scan range boost
        processScanRangeBoost(parsedActions);

        //move stage

        //reproduction stage
        //TODO ensure nothing killed does this.

        //spend resource check stage
        //TODO don't do this for killed things

        //tick down duration effects

        //notify of updated state (ie globaldata, portents, etc)
    }

    /**
     * Distribute portents onto the game map uniformly and then extra at hotspots.
     */
    private void distributePortents() {
        //spawn portents uniformly across the entire board
        for (int i = 0; i < config.generalPortentsPerTick(); i++) {
            Position pos = board.getRandomPos(seededRandom);
            pos.addPortent();
        }

        //spawn portents at the generator
        generator.generatePortents(generatorTerritory, config.generatedPortentsPerTick(), seededRandom);
    }

    /**
     * Generates and distributes sight lines to the terata.
     */
    private void generateSightLines() {
        //for each teras, generate the view and send it
        for (ServerTeras teras : players) {
            teras.scanResults(ViewWrapperUtils.buildScan(teras, board));
        }
    }

    /**
     * Determine a list of actions to be performed.
     */
    private ActionWrapper determineActions() {
        //lists to pass through
        ArrayList<Pair<ServerTeras, ActionPair>> validatedResult = new ArrayList<>();
        ArrayList<Pair<ServerTeras, ActionPair>> meleeActionPairs = new ArrayList<>();
        ArrayList<ServerTeras> terasPendingResourceCollection = new ArrayList<>();
        ArrayList<ServerTeras> terasPendingMove = new ArrayList<>();
        ArrayList<ServerTeras> terasPendingRegen = new ArrayList<>();
        ArrayList<ServerTeras> terasPendingScanRangeBoost = new ArrayList<>();

        //process every action pair
        for (ServerTeras teras : players) {
            ActionPair requestedActions = teras.decideActions();
            //ensure the requested actions are valid
            if (ViewWrapperUtils.validateActionPair(requestedActions, teras, board)) {
                validatedResult.add(new Pair<>(teras, requestedActions));
                ActionPair.PrimaryAction primary = requestedActions.primary();
                ActionPair.SecondaryAction secondary = requestedActions.secondary();
                ActionPair.SecondaryAction primarySubstitute = requestedActions.primarySubstitute();
                //mark melee actions for handling
                if (primary == MELEE) {
                    meleeActionPairs.add(new Pair<>(teras, requestedActions));
                }
                //mark resource collection for handling
                if (primary == GATHER_PORTENT) {
                    terasPendingResourceCollection.add(teras);
                }
                //mark move for handling, account for double-actions
                if (secondary == MOVE) {
                    terasPendingMove.add(teras);
                }
                if (primary == REPLACE_WITH_SECONDARY && primarySubstitute == MOVE) {
                    terasPendingMove.add(teras);
                }
                //mark regen for handling, account for double-actions
                if (secondary == REGEN) {
                    terasPendingRegen.add(teras);
                }
                if (primary == REPLACE_WITH_SECONDARY && primarySubstitute == REGEN) {
                    terasPendingRegen.add(teras);
                }
                //mark scan range boost for handling, account for double-actions
                if (secondary == EXPAND_SCAN_RANGE) {
                    terasPendingScanRangeBoost.add(teras);
                }
                if (primary == REPLACE_WITH_SECONDARY && primarySubstitute == EXPAND_SCAN_RANGE) {
                    terasPendingScanRangeBoost.add(teras);
                }
            }
        }

        //build the wrapper and return
        return new ActionWrapper(validatedResult, meleeActionPairs,
                terasPendingResourceCollection, terasPendingMove, terasPendingRegen, terasPendingScanRangeBoost);
    }

    /**
     * Process all teras seeking to regenerate this turn.
     * @param wrapper The wrapper for all actions to be completed this tick.
     */
    private void processRegens(ActionWrapper wrapper) {
        //for every teras pending regen
        for (ServerTeras teras : wrapper.terasPendingRegen()) {
            teras.processRegen();
        }
    }

    /**
     * Process all teras farming portents this tick.
     * @param wrapper The wrapper for all actions to be completed this tick.
     */
    private void processPortentCollection(ActionWrapper wrapper) {
        //for each teras spending its turn to farm portents
        for (ServerTeras teras : wrapper.terasPendingResourceCollection()) {
            Position position = board.getGamePos(teras.getPos().getX(), teras.getPos().getY());
            //if there is a portent to farm
            if (position.getPortents() > 0) {
                position.removePortent();
                globalData.get(teras.getTemplateId()).portents++;
            }
        }
    }

    /**
     * Process teras who decided to boost their scan range
     * @param wrapper The wrapper for all actions to be completed this tick.
     */
    private void processScanRangeBoost(ActionWrapper wrapper) {
        //for each teras spending its turn to boost scan range
        for (ServerTeras teras : wrapper.terasPendingScanRangeBoost()) {
            teras.boostScanRange(config);
        }
    }

    /**
     * Process melee attacks.
     * @param wrapper The wrapper for all actions to be completed this tick.
     * @return A list of teras marked for death.
     */
    private MeleeAttackReturn processMeleeAttacks(ActionWrapper wrapper) {
        Set<ServerTeras> markedForDeath = new HashSet<>();
        ArrayList<PendingFightResult> pendingFightResults = new ArrayList<>();

        //for every attack to handle
        for (Pair<ServerTeras, ActionPair> attackPair : wrapper.meleeActionPairs()) {
            //get the teras involved
            ServerTeras attacker = attackPair.first();
            ActionPair actionInfo = attackPair.second();
            Position attackPos = board.getGamePos(actionInfo.targetX(), actionInfo.targetY());
            if (!attackPos.isPositionOccupied()) {
                continue; //attack missed lol embarrassing they haven't even moved yet, skip it.
            }
            ServerTeras defender = attackPos.getOccupyingTeras().getFirst();
            //handle the crit mechanic
            boolean critHit = false;
            boolean critDefense = false;
            Attack attackWrapper = attacker.attack(ViewWrapperUtils.genTerasView(defender));
            Defense defenseWrapper = defender.defend(ViewWrapperUtils.genTerasView(attacker));
            AttackType attack = attackWrapper == null ? null : attackWrapper.attack();
            DefenseType defense = defenseWrapper == null ? null : defenseWrapper.defense();
            if (attack == null && defense == null) {
                continue;
            }

            if (attack == null || (attack == AttackType.PIERCE && defense == DefenseType.DEFLECT
                    || attack == AttackType.SLASH && defense == DefenseType.SHIELD
                    || attack == AttackType.BLUDGEON && defense == DefenseType.DODGE)) {
                critDefense = true;
            }
            if (defense == null || (attack == AttackType.BLUDGEON && defense == DefenseType.SHIELD
                    || attack == AttackType.SLASH && defense == DefenseType.DEFLECT
                    || attack == AttackType.PIERCE && defense == DefenseType.DODGE)) {
                critHit = true;
            }
            int baseDamage = attacker.getCurrentMeleeDamage();
            if (critHit) {
                baseDamage = Math.round(baseDamage * config.attackCritMult());
            }
            if (critDefense) {
                baseDamage = Math.round(baseDamage * config.defenseCritMult());
            }
            boolean killed = defender.applyDamage(baseDamage);
            if (killed) {
                markedForDeath.add(defender);
            }

            //notify participants of the result.
            FightResult attackerResult = new FightResult(critHit, critDefense, killed, false, baseDamage);
            FightResult defenderResult = new FightResult(critHit, critDefense, killed, true, baseDamage);
            pendingFightResults.add(new PendingFightResult(attacker, attackerResult));
            pendingFightResults.add(new PendingFightResult(defender, defenderResult));
        }

        return new MeleeAttackReturn(markedForDeath, pendingFightResults);
    }

    /**
     * Generate teras on the game board
     */
    private void generateTeras() {
        players = new ArrayList<>();
        //for every teras type, generate serverTeras
        for (int terasIndex = 0; terasIndex < config.competitors().size(); terasIndex++) {
            Teras templateTeras = config.competitors().get(terasIndex);
            int numToGenerate = config.competitorCount().get(terasIndex);
            //generate every teras instance
            for (int i = 0; i < numToGenerate; i++) {
                Position pos = board.findRandomUnoccupiedPos(seededRandom);
                if (pos != null) {
                    ServerTeras teras = new ServerTeras(config, templateTeras, pos.getCoords());
                    players.add(teras);
                    pos.getOccupyingTeras().add(teras);
                }
            }
        }

        globalData = new HashMap<>();
        //prep the global data map
        for (Teras teras : config.competitors()) {
            globalData.put(teras.getId(), new TerasGlobalData());
        }
    }

    /**
     * Spawns a new resource generator, clearing the old one.
     * @param random Whether to randomly pick a new location or to use the center of the game board.
     */
    private void spawnPortentGenerator(boolean random) {
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
                pos.setIsPortentGeneratorTile(false);
            });
            generatorTerritory.clear();
        }

        //get root location
        Position rootPos = board.getGamePos(x, y);

        //build generator
        generator = new PortentGenerator(rootPos, board, config);
        generatorTerritory = generator.resolveGeneratorTerritory(board);

        //update locations to be generators
        generatorTerritory.forEach((pos) -> {
            pos.setIsPortentGeneratorTile(true);
        });
    }

    /**
     * Convenience for passing all of this data through as the return of one method.
     */
    record ActionWrapper(
            ArrayList<Pair<ServerTeras, ActionPair>> allValidatedActionPairs,
            ArrayList<Pair<ServerTeras, ActionPair>> meleeActionPairs,
            ArrayList<ServerTeras> terasPendingResourceCollection,
            ArrayList<ServerTeras> terasPendingMove,
            ArrayList<ServerTeras> terasPendingRegen,
            ArrayList<ServerTeras> terasPendingScanRangeBoost
    ) {}

    record PendingFightResult(
            ServerTeras teras,
            FightResult result
    ) {}

    record MeleeAttackReturn(
            Set<ServerTeras> killed,
            List<PendingFightResult> results
    ) {}
}
