package com.example.rest;

import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.Client;
import org.springframework.beans.factory.annotation.Autowired;

import javax.ws.rs.NotFoundException;
import java.time.Instant;
import java.util.UUID;

import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;

@EndpointImplementation
public class DocumentEndpointImpl implements DocumentEndpoint {

    private static final String INDEX_NAME = "integration";
    private static final String TYPE_NAME = "documents";

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

    @Override
    public String getDocument(UUID uuid) throws Exception {
        GetResponse getResponse = elasticClient.prepareGet(INDEX_NAME, TYPE_NAME, uuid.toString()).get();

        if (getResponse.isExists()) {
            return getResponse.getSourceAsString();
        } else {
            throw new NotFoundException();
        }
    }
}
