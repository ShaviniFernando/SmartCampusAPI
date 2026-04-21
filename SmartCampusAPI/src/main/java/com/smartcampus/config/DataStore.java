package com.smartcampus.config;

import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import com.smartcampus.model.Reading;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {
    private static final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private static final Map<String, Sensor> sensors = new ConcurrentHashMap<>();

    // Room CRUD
    public static Map<String, Room> getAllRooms() {
        return rooms;
    }

    public static Room getRoomById(String id) {
        return rooms.get(id);
    }

    public static void addRoom(Room room) {
        rooms.put(room.getId(), room);
    }

    public static boolean deleteRoom(String id) {
        // Return false if sensors are attached to this room (safety check)
        boolean hasSensors = sensors.values().stream().anyMatch(s -> s.getRoomId().equals(id));
        if (hasSensors) {
            return false;
        }
        return rooms.remove(id) != null;
    }

    // Sensor CRUD
    public static Map<String, Sensor> getAllSensors() {
        return sensors;
    }

    public static Sensor getSensorById(String id) {
        return sensors.get(id);
    }

    public static void addSensor(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);
    }

    public static Collection<Sensor> getSensorsByType(String type) {
        return sensors.values().stream()
                .filter(s -> s.getType().equalsIgnoreCase(type))
                .collect(java.util.stream.Collectors.toList());
    }

    // Sub-resource (Reading) operations
    public static void addReadingToSensor(String sensorId, Reading reading) {
        Sensor sensor = getSensorById(sensorId);
        if (sensor != null) {
            sensor.getReadings().add(reading);
            // Also update current value
            sensor.setCurrentValue(reading.getValue());
        }
    }

    public static java.util.List<Reading> getSensorReadings(String sensorId) {
        Sensor sensor = getSensorById(sensorId);
        return sensor != null ? sensor.getReadings() : java.util.Collections.emptyList();
    }
}
