package com.iriskopecky.util;

public record Message(
        String target,
        Command command,
        Object data
) {

    public enum Command {
        MAKE,
        INIT,
        SCAN,
        DECIDE_ACTIONS,
        ATTACK,
        DEFEND,
        FIGHT_RESULT,
        SPEND_RESOURCES,
        SHOULD_MATE,
        MOVE,
        TICK_END_STATE
    }
}
