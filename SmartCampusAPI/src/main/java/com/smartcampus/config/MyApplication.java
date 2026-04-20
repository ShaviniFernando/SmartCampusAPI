package com.smartcampus.config;

import org.glassfish.jersey.server.ResourceConfig;
import jakarta.ws.rs.ApplicationPath;

@ApplicationPath("/api/v1")
public class MyApplication extends ResourceConfig {
    public MyApplication() {
        // Register the package where resources are located
        packages("com.smartcampus.resource", "com.smartcampus.exception", "com.smartcampus.filter");
    }
}
