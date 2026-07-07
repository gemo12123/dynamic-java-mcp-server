package org.mytest.test.controller;

import org.mytest.test.annotation.PathParam;
import org.mytest.test.annotation.Tool;
import org.mytest.test.annotation.ToolParam;
import org.mytest.test.model.CompositeSubmitRequest;
import org.mytest.test.model.CompositeSubmissionResult;
import org.mytest.test.model.StructuredApiResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/examples/comprehensive")
public class ComprehensiveExampleController {

    @PostMapping("/projects/{projectId}/tasks/{taskId}/submit")
    @Tool(name = "submitCompositeTask", description = "Combined path parameter, request body, and structured response example", removeStructResponse = false)
    public StructuredApiResponse<CompositeSubmissionResult> submit(
            @PathVariable("projectId")
            @ToolParam(description = "Project id", pathParam = @PathParam(pathPlaceholder = "projectId")) String projectId,
            @PathVariable("taskId")
            @ToolParam(description = "Task id", pathParam = @PathParam(pathPlaceholder = "taskId")) String taskId,
            @RequestBody @ToolParam(description = "Composite submission request") CompositeSubmitRequest request) {
        CompositeSubmissionResult result = new CompositeSubmissionResult(
                projectId,
                taskId,
                request.getOperator(),
                request.getPriority(),
                "submitted");
        return StructuredApiResponse.success("accepted", result);
    }
}