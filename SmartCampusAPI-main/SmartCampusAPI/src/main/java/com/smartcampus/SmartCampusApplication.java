package com.smartcampus;

import com.smartcampus.config.MyApplication;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;

import java.io.IOException;
import java.net.URI;

/**
 * Main application class to start the embedded Grizzly server.
 */
public class SmartCampusApplication {

    // Base URI for the Grizzly HTTP server
    // MyApplication's @ApplicationPath("/api/v1") will be appended to this
    public static final String BASE_URI = "http://localhost:8080/";

    /**
     * Starts Grizzly HTTP server exposing JAX-RS resources defined in this application.
     * @return Grizzly HTTP server.
     */
    public static HttpServer startServer() {
        // Use the existing MyApplication configuration which handles package scanning
        // and manual registration of catch-all mappers.
        final ResourceConfig rc = new MyApplication();

        // Use the GlassFish Grizzly Factory to create and start the server
        return GrizzlyHttpServerFactory.createHttpServer(URI.create(BASE_URI), rc);
    }

    /**
     * Main method to launch the standalone application.
     * @param args command line arguments
     * @throws IOException if there's an error starting the server
     * @throws InterruptedException if the process is interrupted
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        final HttpServer server = startServer();
        System.out.println(String.format("Jersey app started with endpoints available at "
                + "%sapi/v1%nHit Ctrl-C to stop it...", BASE_URI));
        
        // Keep the server running until manually stopped
        Thread.currentThread().join();
    }
}
