package com.smartcampus.model;

public class Reading {

    private long timestamp;
    private double value;

    // Default constructor (required for JSON deserialization)
    public Reading() {
    }

    // Parameterized constructor
    public Reading(long timestamp, double value) {
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

    @Override
    public String toString() {
        return "Reading{timestamp=" + timestamp + ", value=" + value + "}";
    }
}
