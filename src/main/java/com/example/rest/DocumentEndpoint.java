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

	@POST
	@Path("/document2")
	String getUUIDAndCreateDocument() throws Exception;

	@GET
	@Path("/dbDocument/{uuid}")
	@Produces(MediaType.TEXT_HTML)
	String getDocumentFromDB(@PathParam("uuid") UUID uuid) throws Exception;

	@POST
	@Path("/createAndSave")
	String createAndSaveDocument() throws Exception;

	@GET
	@Path("/documents")
	String getAllDocuments();

}
