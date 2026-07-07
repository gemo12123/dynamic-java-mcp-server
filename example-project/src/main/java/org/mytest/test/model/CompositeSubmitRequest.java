package org.mytest.test.model;

public class CompositeSubmitRequest {

    private String operator;

    private Integer priority;

    public CompositeSubmitRequest() {
    }

    public CompositeSubmitRequest(String operator, Integer priority) {
        this.operator = operator;
        this.priority = priority;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }
}