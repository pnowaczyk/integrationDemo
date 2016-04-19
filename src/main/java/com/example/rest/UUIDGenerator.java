package com.example.rest;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class UUIDGenerator {

	String getExternalUUID() {

		Client restClient = Client.create();

		WebResource webResource = restClient.resource("http://localhost:8090/uuid");

		ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);

		String output = response.getEntity(String.class);
		return output;

	}
}
