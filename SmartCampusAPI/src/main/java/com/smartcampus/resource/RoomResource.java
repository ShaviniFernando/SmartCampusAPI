package com.smartcampus.resource;

import com.smartcampus.config.DataStore;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.exception.RoomNotEmptyException;
import com.smartcampus.model.Room;
import jakarta.ws.rs.Consumes;
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

/**
 * JAX-RS resource for Room management.
 * Base path: /api/v1/rooms
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
public class RoomResource {

    /**
     * GET /api/v1/rooms
     * Returns the list of all rooms.
     */
    @GET
    public Collection<Room> getAllRooms() {
        return DataStore.getAllRooms().values();
    }

    /**
     * GET /api/v1/rooms/{roomId}
     * Returns a single room by ID, or 404 if not found.
     */
    @GET
    @Path("/{roomId}")
    public Room getRoomById(@PathParam("roomId") String roomId) {
        Room room = DataStore.getRoomById(roomId);
        if (room == null) {
            throw new ResourceNotFoundException("Room with ID '" + roomId + "' not found.");
        }
        return room;
    }

    /**
     * POST /api/v1/rooms
     * Creates a new room. Returns 201 Created with Location header.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createRoom(Room room, @Context UriInfo uriInfo) {
        DataStore.addRoom(room);
        URI location = uriInfo.getAbsolutePathBuilder().path(room.getId()).build();
        return Response.created(location)
                .entity(room)
                .build();
    }

    /**
     * DELETE /api/v1/rooms/{roomId}
     * Deletes a room. Returns 409 Conflict if sensors are still assigned.
     */
    @DELETE
    @Path("/{roomId}")
    public Response deleteRoom(@PathParam("roomId") String roomId) {
        Room room = DataStore.getRoomById(roomId);
        if (room == null) {
            throw new ResourceNotFoundException("Room with ID '" + roomId + "' not found.");
        }

        boolean deleted = DataStore.deleteRoom(roomId);
        if (!deleted) {
            // Room still has sensors attached — throw 409
            throw new RoomNotEmptyException("Room still has active sensors assigned.");
        }

        return Response.noContent().build(); // 204 No Content
    }
}
