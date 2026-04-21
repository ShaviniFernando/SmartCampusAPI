package com.smartcampus.resource;

import com.smartcampus.config.DataStore;
import com.smartcampus.model.Sensor;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.exception.InvalidInputException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
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

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response addSensor(Sensor sensor, @Context UriInfo uriInfo) {
        // Validation: room must exist
        if (DataStore.getRoomById(sensor.getRoomId()) == null) {
            throw new InvalidInputException("Cannot create sensor: Room " + sensor.getRoomId() + " does not exist");
        }
        
        DataStore.addSensor(sensor);
        URI uri = uriInfo.getAbsolutePathBuilder().path(sensor.getId()).build();
        return Response.created(uri)
                .entity(sensor)
                .build();
    }
}
