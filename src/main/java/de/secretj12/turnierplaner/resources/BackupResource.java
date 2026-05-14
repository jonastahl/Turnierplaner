package de.secretj12.turnierplaner.resources;

import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/backup")
@RolesAllowed("director")
public class BackupResource {

    @GET
    @Path("/download")
    @Produces(MediaType.APPLICATION_JSON)
    public Response download() {
        // TODO: Implement download logic
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }

    @POST
    @Path("/upload")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response upload(String data) {
        // TODO: Implement upload logic
        return Response.status(Response.Status.NOT_IMPLEMENTED).build();
    }
}
