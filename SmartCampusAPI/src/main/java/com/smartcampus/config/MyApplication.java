package com.smartcampus.config;

import org.glassfish.jersey.server.ResourceConfig;
import jakarta.ws.rs.ApplicationPath;

/**
 * JAX-RS Application configuration.
 * Base path: /api/v1
 * Registers all resource, exception mapper, and filter packages.
 */
@ApplicationPath("/api/v1")
public class MyApplication extends ResourceConfig {

    public MyApplication() {
        // Scan packages for @Path, @Provider annotated classes
        packages(
            "com.smartcampus.resource",
            "com.smartcampus.exception",
            "com.smartcampus.filter"
        );

        // Explicitly register GlobalExceptionMapper last to ensure
        // it acts as the true catch-all safety net for any Throwable
        register(com.smartcampus.exception.GlobalExceptionMapper.class);
    }
}
