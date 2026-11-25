package com.iriskopecky.util;

/**
 * Represent what two actions a teras wants to take this tick.
 * @param primary The teras's primary action.
 * @param secondary The teras's secondary action
 * @param primarySubstitute If primary == REPLACE_WITH_SECONDARY, this is the second secondary action to take. Else, nullable.
 * @param targetX The x coord of where any targetable actions are aimed.
 * @param targetY The y coord of where any targetable actions are aimed.
 */
public record ActionPair(PrimaryAction primary, SecondaryAction secondary,
                         SecondaryAction primarySubstitute, int targetX, int targetY) {

    public enum PrimaryAction {
        MELEE, //if this is selected, targetX and targetY must be within +/- 1 of the teras' current pos
        GATHER_PORTENT, //gather the portent on the current square (happens before move, so CAN be combined with move)
        REPLACE_WITH_SECONDARY //take an additional secondary action instead (primarySubstitute must be set)
    }

    public enum SecondaryAction {
        MOVE, //use your secondar action to move (the move itself will be handled later)
        EXPAND_SCAN_RANGE, //expand your scan range for a period of time
        REGEN //regenerate health
    }
}
