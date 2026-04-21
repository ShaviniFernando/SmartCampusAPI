package com.smartcampus.model;

public class Reading {

    private String id;
    private long timestamp;
    private double value;

    // Default constructor (required for JSON deserialization)
    public Reading() {
    }

    // Parameterized constructor
    public Reading(String id, long timestamp, double value) {
        this.id = id;
        this.timestamp = timestamp;
        this.value = value;
    }

    // Getters and Setters
    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Reading{id='" + id + "', timestamp=" + timestamp + ", value=" + value + "}";
    }
}
