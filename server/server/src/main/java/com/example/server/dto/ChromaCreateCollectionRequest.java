package com.example.server.dto;

//request body for POST .../collections - creates a collection with the given name
public record ChromaCreateCollectionRequest(String name) {
}
