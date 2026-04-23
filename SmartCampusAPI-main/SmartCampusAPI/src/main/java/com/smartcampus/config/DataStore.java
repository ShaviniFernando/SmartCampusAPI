package com.smartcampus.config;

import com.smartcampus.model.SensorReading;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Singleton in-memory data store for Rooms, Sensors, and Readings.
 * Uses ConcurrentHashMap to be thread-safe across request-scoped resource instances.
 */
public class DataStore {

    private static final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private static final Map<String, Sensor> sensors = new ConcurrentHashMap<>();

    static {
        seedMockData();
    }

    /**
     * Seeds initial demo data so the API is usable immediately after startup.
     */
    private static void seedMockData() {
        // ── Rooms ─────────────────────────────────────────────────────────────
        addRoom(new Room("LAB-101",  "Computer Lab",    40));
        addRoom(new Room("LECT-202", "Lecture Hall",   150));
        addRoom(new Room("LIB-301",  "Library",         80));

        // ── Sensors ───────────────────────────────────────────────────────────
        addSensor(new Sensor("TEMP-001", "Temperature", "ACTIVE",     22.5, "LAB-101"));
        addSensor(new Sensor("CO2-001",  "CO2",         "ACTIVE",    400.0, "LECT-202"));
        addSensor(new Sensor("HUM-001",  "Humidity",    "ACTIVE",     55.0, "LIB-301"));
        addSensor(new Sensor("TEMP-002", "Temperature", "MAINTENANCE", 0.0, "LECT-202"));

        System.out.println("[DataStore] Seeded initial rooms and sensors.");
    }

    // ─── Room Operations ───────────────────────────────────────────────────────

    public static Map<String, Room> getAllRooms() {
        return rooms;
    }

    public static Room getRoomById(String id) {
        return rooms.get(id);
    }

    public static void addRoom(Room room) {
        rooms.put(room.getId(), room);
    }

    /**
     * Deletes a room only if its sensorIds list is empty.
     * Returns false (without deleting) if the room still has linked sensors.
     */
    public static boolean deleteRoom(String id) {
        Room room = rooms.get(id);
        if (room == null) {
            return false;
        }
        if (room.getSensorIds() != null && !room.getSensorIds().isEmpty()) {
            return false; // room still has sensors — caller must throw RoomNotEmptyException
        }
        rooms.remove(id);
        return true;
    }

    // ─── Sensor Operations ────────────────────────────────────────────────────

    public static Map<String, Sensor> getAllSensors() {
        return sensors;
    }

    public static Sensor getSensorById(String id) {
        return sensors.get(id);
    }

    /**
     * Adds a sensor and registers its ID into the parent room's sensorIds list.
     */
    public static void addSensor(Sensor sensor) {
        sensors.put(sensor.getId(), sensor);
        // Maintain bi-directional link: add sensorId to the room's sensorIds list
        Room room = rooms.get(sensor.getRoomId());
        if (room != null && !room.getSensorIds().contains(sensor.getId())) {
            room.getSensorIds().add(sensor.getId());
        }
    }

    public static Collection<Sensor> getSensorsByType(String type) {
        return sensors.values().stream()
                .filter(s -> s.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    /**
     * Appends a reading to the sensor's history and updates currentValue.
     */
    public static void addReadingToSensor(String sensorId, SensorReading reading) {
        Sensor sensor = getSensorById(sensorId);
        if (sensor != null) {
            sensor.getReadings().add(reading);
            sensor.setCurrentValue(reading.getValue());
        }
    }

    public static List<SensorReading> getSensorReadings(String sensorId) {
        Sensor sensor = getSensorById(sensorId);
        return sensor != null ? sensor.getReadings() : Collections.emptyList();
    }
}
