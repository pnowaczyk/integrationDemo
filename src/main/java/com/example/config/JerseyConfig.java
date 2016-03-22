package com.example.config;

import com.example.rest.EndpointImplementation;
import org.glassfish.jersey.server.ResourceConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.ApplicationPath;

@Component
@ApplicationPath("/")
public class JerseyConfig extends ResourceConfig {

    @Autowired
    public JerseyConfig(@EndpointImplementation Object[] endpointImplementations) {
        registerInstances(endpointImplementations);
    }
}
