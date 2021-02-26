package it.fitnesschallenge.model.room;

public enum WorkoutType {
    INDOOR("indoor"),
    OUTDOOR("outdoor");

    private String value;

    WorkoutType(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }
};