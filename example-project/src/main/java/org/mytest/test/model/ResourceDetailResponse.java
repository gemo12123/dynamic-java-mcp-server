package org.mytest.test.model;

public class ResourceDetailResponse {

    private String id;

    private String name;

    private Boolean active;

    public ResourceDetailResponse() {
    }

    public ResourceDetailResponse(String id, String name, Boolean active) {
        this.id = id;
        this.name = name;
        this.active = active;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}