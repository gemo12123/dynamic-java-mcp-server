package org.mytest.test.model;

public class UpdateResourceRequest {

    private String resourceId;

    private String name;

    private Boolean enabled;

    public UpdateResourceRequest() {
    }

    public UpdateResourceRequest(String resourceId, String name, Boolean enabled) {
        this.resourceId = resourceId;
        this.name = name;
        this.enabled = enabled;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }
}