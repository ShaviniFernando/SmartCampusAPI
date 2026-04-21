package com.smartcampus.model;

import java.util.ArrayList;
import java.util.List;

public class Sensor {

    private String id;
    private String type;
    private String status;
    private double currentValue;
    private String roomId;
    private List<SensorReading> readings;

    // Default constructor (required for JSON deserialization)
    public Sensor() {
        this.readings = new ArrayList<>();
    }

    // Parameterized constructor
    public Sensor(String id, String type, String status, double currentValue, String roomId) {
        this.id = id;
        this.type = type;
        this.status = status;
        this.currentValue = currentValue;
        this.roomId = roomId;
        this.readings = new ArrayList<>();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public double getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(double currentValue) {
        this.currentValue = currentValue;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public List<SensorReading> getReadings() {
        return readings;
    }

    public void setReadings(List<SensorReading> readings) {
        this.readings = readings;
    }

    @Override
    public String toString() {
        return "Sensor{id='" + id + "', type='" + type + "', status='" + status
                + "', currentValue=" + currentValue + ", roomId='" + roomId + "'}";
    }
}
