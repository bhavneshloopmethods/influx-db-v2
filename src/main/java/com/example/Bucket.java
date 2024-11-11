package com.example;

import java.util.List;

class Bucket {
    private String id;
    private String orgID;
    private String type;
    private String name;
    private List<RetentionRule> retentionRules;
    private String createdAt;
    private String updatedAt;
    private String description;
    private BucketLinks links;
    private List<String> labels;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOrgID() {
        return orgID;
    }

    public void setOrgID(String orgID) {
        this.orgID = orgID;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<RetentionRule> getRetentionRules() {
        return retentionRules;
    }

    public void setRetentionRules(List<RetentionRule> retentionRules) {
        this.retentionRules = retentionRules;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    public BucketLinks getLinks() {
        return links;
    }

    public void setLinks(BucketLinks links) {
        this.links = links;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }
}
