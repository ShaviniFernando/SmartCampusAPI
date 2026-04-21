package com.smartcampus.resource;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReadingResource {

    private final String sensorId;

    public ReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

}
