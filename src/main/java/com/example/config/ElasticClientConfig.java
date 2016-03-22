package com.example.config;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;

@Configuration
public class ElasticClientConfig {

    public static final String INDEX_NAME = "integration";
    public static final String TYPE_NAME = "documents";

    @Bean(destroyMethod = "close")
    public Client elasticClient(ElasticConfig config) throws UnknownHostException {
        ArrayList<TransportAddress> transportAddresses = new ArrayList<>();
        for (String node: config.getNodes()) {
            transportAddresses.add(new InetSocketTransportAddress(InetAddress.getByName(node), config.getPort()));
        }
        TransportAddress[] addresses = transportAddresses.toArray(new TransportAddress[transportAddresses.size()]);
        return TransportClient.builder().settings(Settings.settingsBuilder().put("cluster.name", config.getName()).build()).build().addTransportAddresses(addresses);
    }
}
