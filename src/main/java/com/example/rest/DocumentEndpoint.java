package com.example.rest;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.UUID;

@Path("/")
public interface DocumentEndpoint {

    @POST
    @Path("/document")
    String createDocument() throws Exception;

    @GET
    @Path("/document/{uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    String getDocument(@PathParam("uuid") UUID uuid) throws Exception;
}
