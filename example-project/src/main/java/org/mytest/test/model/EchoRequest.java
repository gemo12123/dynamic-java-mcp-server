package org.mytest.test.model;

public class EchoRequest {

    private String message;

    private Integer priority;

    public EchoRequest() {
    }

    public EchoRequest(String message, Integer priority) {
        this.message = message;
        this.priority = priority;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}