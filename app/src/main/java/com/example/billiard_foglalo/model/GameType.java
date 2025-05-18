package com.example.billiard_foglalo.model;

public enum GameType {
    POOL("Pool"),
    SNOOKER("Snooker");

    private final String label;

    GameType(String label) {
        this.label = label;
    }
    public static GameType fromString(String type) {
        switch (type.toLowerCase()) {
            case "pool":
                return POOL;
            case "snooker":
                return SNOOKER;
            default:
                throw new IllegalArgumentException("Unknown game type: " + type);
        }
    }

    public String getLabel() {
        return label;
    }

    public static GameType fromLabel(String label) {
        for (GameType type : values()) {
            if (type.label.equalsIgnoreCase(label)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown game type: " + label);
    }
}