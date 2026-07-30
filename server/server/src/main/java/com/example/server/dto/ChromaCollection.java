package com.example.server.dto;

//a single collection as returned by both the list-collections and create-collection endpoints
public record ChromaCollection(String id, String name) {
}
