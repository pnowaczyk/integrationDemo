package com.example.rest;

import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.UUID;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

@EndpointImplementation
public class DocumentEndpointImpl implements DocumentEndpoint {

    private Client elasticClient;

    @Autowired
    public DocumentEndpointImpl(Client elasticClient) {
        this.elasticClient = elasticClient;
    }

    @Override
    public String createDocument() throws Exception {
        UUID uuid = UUID.randomUUID();
        IndexResponse response = elasticClient.prepareIndex("integration", "documents", uuid.toString())
                .setCreate(true)
                .setSource(jsonBuilder().startObject().field("uuid", uuid.toString()).field("date", Instant.now()).endObject())
                .get();
        response.isCreated();
        return uuid.toString();
    }
}
