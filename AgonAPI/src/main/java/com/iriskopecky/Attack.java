package com.iriskopecky;

public record Attack(AttackType attack) {

    public enum AttackType {
        PIERCE,
        SLASH,
        BLUDGEON
    }
}
