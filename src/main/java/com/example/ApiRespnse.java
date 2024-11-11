package com.example;

import java.util.List;

class ApiResponse {
    private Links links;
    private List<Bucket> buckets;

    // Getters and Setters
    public Links getLinks() {
        return links;
    }

    public void setLinks(Links links) {
        this.links = links;
    }

    public List<Bucket> getBuckets() {
        return buckets;
    }

    public void setBuckets(List<Bucket> buckets) {
        this.buckets = buckets;
    }
}