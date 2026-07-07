package org.mytest.test.controller;

import org.mytest.test.annotation.PathParam;
import org.mytest.test.annotation.Tool;
import org.mytest.test.annotation.ToolParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/examples/path")
public class PathParamExampleController {

    @GetMapping("/resources/{resourceId}")
    @Tool(name = "getResourceById", description = "Single path parameter example")
    public String getResourceById(
            @PathVariable("resourceId")
            @ToolParam(description = "Resource id", pathParam = @PathParam(pathPlaceholder = "resourceId")) String resourceId) {
        return "resource-" + resourceId;
    }

    @GetMapping("/teams/{teamId}/resources/{resourceId}")
    @Tool(name = "getTeamResource", description = "Multiple path segment example")
    public String getTeamResource(
            @PathVariable("teamId")
            @ToolParam(description = "Team id", pathParam = @PathParam(pathPlaceholder = "teamId")) String teamId,
            @PathVariable("resourceId")
            @ToolParam(description = "Resource id", pathParam = @PathParam(pathPlaceholder = "resourceId")) String resourceId) {
        return teamId + ":" + resourceId;
    }
}