package org.mytest.test.model;

public class CompositeSubmissionResult {

    private String projectId;

    private String taskId;

    private String operator;

    private Integer priority;

    private String status;

    public CompositeSubmissionResult() {
    }

    public CompositeSubmissionResult(String projectId, String taskId, String operator, Integer priority, String status) {
        this.projectId = projectId;
        this.taskId = taskId;
        this.operator = operator;
        this.priority = priority;
        this.status = status;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}