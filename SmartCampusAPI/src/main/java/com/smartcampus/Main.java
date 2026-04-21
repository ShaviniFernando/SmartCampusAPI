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

public class Main {
    // Base URI the Grizzly HTTP server will listen on
    public static final String BASE_URI = "http://0.0.0.0:8080/";

    public static HttpServer startServer() {
        // Initialize mock data
        seedMockData();

        // Create a resource config that scans for JAX-RS resources and providers
        final MyApplication config = new MyApplication();

        // Create and start a new instance of grizzly http server
        // exposing the Jersey application at BASE_URI
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), config);
    }

    private static void seedMockData() {
        System.out.println("Seeding mock data...");
        Room room1 = new Room("LAB-101", "Computer Lab", 40);
        Room room2 = new Room("LECT-202", "Lecture Hall", 150);
        DataStore.addRoom(room1);
        DataStore.addRoom(room2);

        DataStore.addSensor(new Sensor("TEMP-001", "Temperature", "ACTIVE", 22.5, "LAB-101"));
        DataStore.addSensor(new Sensor("CO2-001", "CO2", "ACTIVE", 400.0, "LECT-202"));
    }

    public static void main(String[] args) {
        try {
            final HttpServer server = startServer();
            System.out.println(String.format("Smart Campus API started at %sapi/v1/\nHit enter to stop it...", BASE_URI));
            System.in.read();
            server.shutdownNow();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
