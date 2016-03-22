package com.example.rest;

@EndpointImplementation
public class DocumentEndpointImpl implements DocumentEndpoint {

    @Override
    public String createDocument() throws Exception {
        return "hello";
    }
}
