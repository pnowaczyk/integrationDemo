package com.example.rest;

import javax.ws.rs.POST;
import javax.ws.rs.Path;

@Path("/")
public interface DocumentEndpoint {

    @POST
    @Path("/document")
    String createDocument() throws Exception;
}
