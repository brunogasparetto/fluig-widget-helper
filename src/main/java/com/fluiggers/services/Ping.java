package com.fluiggers.services;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

@Path("/ping")
public class Ping {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public Response ping() {
        return Response.ok("pong").build();
    }
}
