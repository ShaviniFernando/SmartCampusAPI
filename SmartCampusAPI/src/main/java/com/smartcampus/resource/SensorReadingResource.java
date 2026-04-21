package com.smartcampus.resource;

import com.smartcampus.config.DataStore;
import com.smartcampus.exception.SensorUnavailableException;
import com.smartcampus.model.SensorReading;
import com.smartcampus.model.Sensor;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

/**
 * Sub-resource for Sensor Readings.
 * Accessed via the sub-resource locator in SensorResource:
 *   /api/v1/sensors/{sensorId}/readings
 *
 * Note: No @Path annotation here — the path is set by the locator.
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    /**
     * GET /api/v1/sensors/{sensorId}/readings
     * Returns the full historical reading list for this sensor.
     */
    @GET
    public List<SensorReading> getReadings() {
        return DataStore.getSensorReadings(sensorId);
    }

    @POST
    public Response addReading(SensorReading reading) {
        Sensor sensor = DataStore.getSensorById(sensorId);

        // Business rule: reject readings on sensors under maintenance
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                    "Sensor is under maintenance and cannot accept readings.");
        }

        // Auto-generate id if not provided
        if (reading.getId() == null || reading.getId().isEmpty()) {
            reading.setId("READ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        }

        // Auto-set timestamp if not provided
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        // Persist reading and update sensor's currentValue
        DataStore.addReadingToSensor(sensorId, reading);

        return Response.status(Response.Status.CREATED)
                .entity(reading)
                .build();
    }
}
