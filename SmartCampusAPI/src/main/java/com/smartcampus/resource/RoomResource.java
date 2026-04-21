package com.smartcampus.resource;

import com.smartcampus.config.DataStore;
import com.smartcampus.model.Room;
import com.smartcampus.exception.ResourceNotFoundException;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Collection;

@Path("/rooms")
public class RoomResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Collection<Room> getAllRooms() {
        return DataStore.getAllRooms().values();
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Room getRoomById(@PathParam("id") String id) {
        Room room = DataStore.getRoomById(id);
        if (room == null) {
            throw new ResourceNotFoundException("Room with ID " + id + " not found");
        }
        return room;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response addRoom(Room room, @Context UriInfo uriInfo) {
        DataStore.addRoom(room);
        URI uri = uriInfo.getAbsolutePathBuilder().path(room.getId()).build();
        return Response.created(uri)
                .entity(room)
                .build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteRoom(@PathParam("id") String id) {
        if (DataStore.getRoomById(id) == null) {
            throw new ResourceNotFoundException("Room with ID " + id + " not found");
        }
        
        boolean deleted = DataStore.deleteRoom(id);
        if (!deleted) {
            throw new com.smartcampus.exception.RoomNotEmptyException("Cannot delete room with active sensors");
        }
        return Response.noContent().build();
    }
}
