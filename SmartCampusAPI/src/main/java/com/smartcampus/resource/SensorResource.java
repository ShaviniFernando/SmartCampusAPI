package com.smartcampus.resource;

import com.smartcampus.config.DataStore;
import com.smartcampus.model.Sensor;
import com.smartcampus.exception.ResourceNotFoundException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import java.util.Collection;

@Path("/sensors")
public class SensorResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Sensor> getSensors(@QueryParam("type") String type) {
        if (type != null && !type.isEmpty()) {
            return DataStore.getSensorsByType(type);
        }
        return DataStore.getAllSensors().values();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Sensor getSensorById(@PathParam("id") String id) {
        Sensor sensor = DataStore.getSensorById(id);
        if (sensor == null) {
            throw new ResourceNotFoundException("Sensor with ID " + id + " not found");
        }
        return sensor;
    }
}
