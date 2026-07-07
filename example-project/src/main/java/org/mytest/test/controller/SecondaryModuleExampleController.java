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
@RequestMapping("/examples/secondary")
public class SecondaryModuleExampleController {

    @PostMapping("/workspaces/{workspaceId}/jobs/{jobId}/dispatch")
    @Tool(
            name = "dispatchSecondaryJob",
            description = "Combined example for a secondary module in the same service",
            module = "secondary",
            removeStructResponse = false)
    public StructuredApiResponse<CompositeSubmissionResult> dispatch(
            @PathVariable("workspaceId")
            @ToolParam(description = "Workspace id", pathParam = @PathParam(pathPlaceholder = "workspaceId")) String workspaceId,
            @PathVariable("jobId")
            @ToolParam(description = "Job id", pathParam = @PathParam(pathPlaceholder = "jobId")) String jobId,
            @RequestBody @ToolParam(description = "Secondary module dispatch request") CompositeSubmitRequest request) {
        CompositeSubmissionResult result = new CompositeSubmissionResult(
                workspaceId,
                jobId,
                request.getOperator(),
                request.getPriority(),
                "queued");
        return StructuredApiResponse.success("secondary accepted", result);
    }
}