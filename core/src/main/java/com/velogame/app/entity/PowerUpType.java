package com.velogame.app.entity;

public enum PowerUpType {
    MAGNET(8f, "MAGNET"),
    SHIELD(0f, "SHIELD"), // charge-based (1 hit)
    TURBO(5.5f, "TURBO");

    public final float duration;
    public final String label;

    PowerUpType(float duration, String label) {
        this.duration = duration;
        this.label = label;
    }
}
