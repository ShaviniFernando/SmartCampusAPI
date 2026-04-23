package com.smartcampus.resource;

import com.smartcampus.config.DataStore;
import com.smartcampus.exception.LinkedResourceNotFoundException;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.model.Sensor;
import jakarta.ws.rs.Consumes;
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

/**
 * JAX-RS resource for Sensor management.
 * Base path: /api/v1/sensors
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
public class SensorResource {

    /**
     * GET /api/v1/sensors
     * Returns all sensors, optionally filtered by ?type= query param.
     */
    @GET
    public Collection<Sensor> getSensors(@QueryParam("type") String type) {
        if (type != null && !type.isEmpty()) {
            return DataStore.getSensorsByType(type);
        }
        return DataStore.getAllSensors().values();
    }

    /**
     * GET /api/v1/sensors/{sensorId}
     * Returns a single sensor by ID, or 404 if not found.
     */
    @GET
    @Path("/{sensorId}")
    public Sensor getSensorById(@PathParam("sensorId") String sensorId) {
        Sensor sensor = DataStore.getSensorById(sensorId);
        if (sensor == null) {
            throw new ResourceNotFoundException("Sensor with ID '" + sensorId + "' not found.");
        }
        return sensor;
    }

    /**
     * POST /api/v1/sensors
     * Registers a new sensor.
     * Validates that the referenced roomId exists — throws 422 if not.
     * On success, adds the sensorId to the room's sensorIds list and returns 201 Created.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createSensor(Sensor sensor, @Context UriInfo uriInfo) {
        // Integrity check: roomId must reference an existing room
        if (sensor.getRoomId() == null || DataStore.getRoomById(sensor.getRoomId()) == null) {
            throw new LinkedResourceNotFoundException("Referenced roomId does not exist.");
        }

        // Persist sensor and update room's sensorIds list
        DataStore.addSensor(sensor);

        URI location = uriInfo.getAbsolutePathBuilder().path(sensor.getId()).build();
        return Response.created(location)
                .entity(sensor)
                .build();
    }

    /**
     * Sub-resource locator for /api/v1/sensors/{sensorId}/readings
     * Delegates to SensorReadingResource. No HTTP method annotation — this is intentional.
     */
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingResource(@PathParam("sensorId") String sensorId) {
        // Validate sensor exists before delegating to sub-resource
        if (DataStore.getSensorById(sensorId) == null) {
            throw new ResourceNotFoundException("Sensor with ID '" + sensorId + "' not found.");
        }
        return new SensorReadingResource(sensorId);
    }
}
