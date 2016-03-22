package com.example.rest;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/")
public interface DocumentEndpoint {

    @GET
    @Path("/document")
    String createDocument() throws Exception;
}
