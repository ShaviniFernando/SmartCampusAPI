package com.smartcampus.resource;

import com.smartcampus.config.DataStore;
import com.smartcampus.model.Reading;
import com.smartcampus.model.Sensor;
import jakarta.ws.rs.Consumes;
import com.smartcampus.exception.ForbiddenException;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.util.List;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ReadingResource {

    private final String sensorId;

    public ReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public List<Reading> getReadings() {
        return DataStore.getSensorReadings(sensorId);
    }

    @POST
    public Response addReading(Reading reading, @Context UriInfo uriInfo) {
        Sensor sensor = DataStore.getSensorById(sensorId);
        
        if ("MAINTENANCE".equalsIgnoreCase(sensor.getStatus())) {
            throw new ForbiddenException("Cannot add reading: Sensor " + sensorId + " is in MAINTENANCE mode");
        }
        
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }
        
        DataStore.addReadingToSensor(sensorId, reading);
        
        return Response.status(Response.Status.CREATED)
                .entity(reading)
                .build();
    }
}
