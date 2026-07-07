package org.mytest.test.model;

public class UpdateResourceResult {

    private String resourceId;

    private String name;

    private Boolean enabled;

    private String status;

    public UpdateResourceResult() {
    }

    public UpdateResourceResult(String resourceId, String name, Boolean enabled, String status) {
        this.resourceId = resourceId;
        this.name = name;
        this.enabled = enabled;
        this.status = status;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}