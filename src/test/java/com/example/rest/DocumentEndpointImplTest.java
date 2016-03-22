package com.example.rest;

import com.example.config.ElasticClientConfig;
import com.google.common.io.Files;
import org.elasticsearch.action.admin.indices.refresh.RefreshRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;
import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

public class DocumentEndpointImplTest {

    private File tmpDir;
    private Node node;
    private Client client;

    private DocumentEndpointImpl endpoint;

    @Before
    public void setUp() {
        tmpDir = Files.createTempDir();
        node = nodeBuilder()
                .local(true)
                .settings(Settings.builder()
                        .put("path.home", tmpDir.getAbsolutePath()))
                .clusterName("integrationCluster")
                .node();
        client = node.client();
        // documents for tests
        client.admin().indices().refresh(new RefreshRequest()).actionGet();

        endpoint = new DocumentEndpointImpl(client);
    }

    @After
    public void tearDown() throws Exception {
        client.admin().indices().delete(Requests.deleteIndexRequest(ElasticClientConfig.INDEX_NAME)).actionGet();
        node.close();
        tmpDir.delete();
    }

    @Test
    public void endpointCreatesDocuments() throws Exception {
        assertThat(endpoint.createDocument()).isNotEmpty();
    }
}