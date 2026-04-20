package com.smartcampus.resource;

import com.smartcampus.config.DataStore;
import com.smartcampus.model.Room;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Collection;

@Path("/rooms")
public class RoomResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Room> getAllRooms() {
        return DataStore.getAllRooms().values();
    }
}
