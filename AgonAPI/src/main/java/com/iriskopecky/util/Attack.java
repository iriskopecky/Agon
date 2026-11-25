package com.iriskopecky.util;

public record Attack(AttackType attack) {

    public enum AttackType {
        PIERCE,
        SLASH,
        BLUDGEON
    }
}
