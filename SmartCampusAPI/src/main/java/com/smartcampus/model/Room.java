package com.smartcampus.model;

public class Room {

    private String id;
    private String name;
    private int capacity;
    private java.util.List<String> sensorIds = new java.util.ArrayList<>();

    // Default constructor (required for JSON deserialization)
    public Room() {
    }

    // Parameterized constructor
    public Room(String id, String name, int capacity) {
        this.id = id;
        this.name = name;
        this.capacity = capacity;
        this.sensorIds = new java.util.ArrayList<>();
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public java.util.List<String> getSensorIds() {
        return sensorIds;
    }

    public void setSensorIds(java.util.List<String> sensorIds) {
        this.sensorIds = sensorIds;
    }

    @Override
    public String toString() {
        return "Room{id='" + id + "', name='" + name + "', capacity=" + capacity + ", sensorIds=" + sensorIds + "}";
    }
}
