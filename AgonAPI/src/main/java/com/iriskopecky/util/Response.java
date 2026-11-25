package com.iriskopecky.util;

public record Response(String target, ResponseType response, Object data) {

    public enum ResponseType {
        OK,
        ERROR,
        ACTION_DECISION,
        ATTACK,
        DEFENSE,
        PURCHASE,
        WANTS_TO_MATE,
        MOVE
    }
}
