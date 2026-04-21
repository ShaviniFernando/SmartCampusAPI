package com.smartcampus.resource;

import com.smartcampus.config.DataStore;
import com.smartcampus.model.Sensor;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Collection;

@Path("/sensors")
public class SensorResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Sensor> getAllSensors() {
        return DataStore.getAllSensors().values();
    }
}
