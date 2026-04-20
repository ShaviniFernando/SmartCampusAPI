package com.smartcampus.config;

import com.smartcampus.model.Room;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DataStore {
    private static final Map<String, Room> rooms = new ConcurrentHashMap<>();

    public static Map<String, Room> getRooms() {
        return rooms;
    }
}
