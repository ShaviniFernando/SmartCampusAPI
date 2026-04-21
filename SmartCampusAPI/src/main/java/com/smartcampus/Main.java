package com.smartcampus;

import com.smartcampus.config.DataStore;
import com.smartcampus.config.MyApplication;
import com.smartcampus.model.Room;
import com.smartcampus.model.Sensor;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Entry point for the Smart Campus API.
 * Starts an embedded Grizzly HTTP server at http://0.0.0.0:8080/api/v1/
 */
public class Main {

    public static final String BASE_URI = "http://0.0.0.0:8080/";

    public static HttpServer startServer() {
        seedMockData();
        final MyApplication config = new MyApplication();
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), config);
    }

    /**
     * Seeds initial demo data so the API is usable immediately after startup.
     * Rooms are created first, then sensors (which auto-register their IDs in the room).
     */
    private static void seedMockData() {
        // ── Rooms ─────────────────────────────────────────────────────────────
        Room lab   = new Room("LAB-101",  "Computer Lab",    40);
        Room hall  = new Room("LECT-202", "Lecture Hall",   150);
        Room lib   = new Room("LIB-301",  "Library",         80);
        DataStore.addRoom(lab);
        DataStore.addRoom(hall);
        DataStore.addRoom(lib);

        // ── Sensors ───────────────────────────────────────────────────────────
        // DataStore.addSensor automatically adds the sensorId to the room's list
        DataStore.addSensor(new Sensor("TEMP-001", "Temperature", "ACTIVE",     22.5, "LAB-101"));
        DataStore.addSensor(new Sensor("CO2-001",  "CO2",         "ACTIVE",    400.0, "LECT-202"));
        DataStore.addSensor(new Sensor("HUM-001",  "Humidity",    "ACTIVE",     55.0, "LIB-301"));
        DataStore.addSensor(new Sensor("TEMP-002", "Temperature", "MAINTENANCE", 0.0, "LECT-202"));

        System.out.println("[DataStore] Seeded 3 rooms and 4 sensors.");
    }

    public static void main(String[] args) {
        try {
            final HttpServer server = startServer();
            System.out.println("==============================================");
            System.out.println("  Smart Campus API started successfully!");
            System.out.println("  Base URL : http://localhost:8080/api/v1/");
            System.out.println("  Discovery: http://localhost:8080/api/v1/");
            System.out.println("  Rooms    : http://localhost:8080/api/v1/rooms");
            System.out.println("  Sensors  : http://localhost:8080/api/v1/sensors");
            System.out.println("  Press ENTER to stop the server.");
            System.out.println("==============================================");
            System.in.read();
            server.shutdownNow();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, "Server failed to start", ex);
        }
    }
}
