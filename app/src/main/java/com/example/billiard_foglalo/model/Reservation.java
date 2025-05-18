package com.example.billiard_foglalo.model;

public class Reservation {
    private String userId;
    private String name;
    private String date;         // e.g., "2025-05-16"
    private String timeSlot;     // e.g., "14:00 - 15:00"
    private GameType gameType;     // "Pool" or "Snooker"
    private int tableNumber;

    public Reservation() {
        // Default constructor required for Firebase
    }

    public Reservation(String userId, String name, String date, String timeSlot, GameType gameType, int tableNumber) {
        this.userId = userId;
        this.name = name;
        this.date = date;
        this.timeSlot = timeSlot;
        this.gameType = gameType;
        this.tableNumber = tableNumber;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTimeSlot(String timeSlot) {
        this.timeSlot = timeSlot;
    }

    public void setGameType(GameType gameType) {
        this.gameType = gameType;
    }

    public void setTableNumber(int tableNumber) {
        this.tableNumber = tableNumber;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getDate() {
        return date;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public GameType getGameType() {
        return gameType;
    }

    public int getTableNumber() {
        return tableNumber;
    }
}
