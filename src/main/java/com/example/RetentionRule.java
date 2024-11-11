package com.example;

class RetentionRule {
    private String type;
    private int everySeconds;
    private int shardGroupDurationSeconds;

    // Getters and Setters
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getEverySeconds() {
        return everySeconds;
    }

    public void setEverySeconds(int everySeconds) {
        this.everySeconds = everySeconds;
    }

    public int getShardGroupDurationSeconds() {
        return shardGroupDurationSeconds;
    }

    public void setShardGroupDurationSeconds(int shardGroupDurationSeconds) {
        this.shardGroupDurationSeconds = shardGroupDurationSeconds;
    }
}



