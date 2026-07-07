package org.mytest.test.controller;

import org.mytest.test.annotation.Tool;
import org.mytest.test.annotation.ToolParam;
import org.mytest.test.model.EchoRequest;
import org.mytest.test.model.UpdateResourceRequest;
import org.mytest.test.model.UpdateResourceResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/examples/basic")
public class BasicHttpExampleController {

    @GetMapping("/hello")
    @Tool(name = "basicHello", description = "Basic GET example")
    public String hello() {
        return "hello dynamic mcp";
    }

    @PostMapping("/echo")
    @Tool(name = "basicEcho", description = "Basic POST request body example")
    public EchoRequest echo(@RequestBody @ToolParam(description = "Echo request body") EchoRequest request) {
        return request;
    }

    @PutMapping("/resources")
    @Tool(name = "updateBasicResource", description = "PUT example for updating a resource without path parameters")
    public UpdateResourceResult update(
            @RequestBody @ToolParam(description = "Resource update payload") UpdateResourceRequest request) {
        return new UpdateResourceResult(request.getResourceId(), request.getName(), request.getEnabled(), "updated");
    }

    @DeleteMapping("/resources")
    @Tool(name = "deleteBasicResource", description = "DELETE example for removing a resource without path parameters")
    public String delete(@RequestParam("resourceId") @ToolParam(description = "Resource id") String resourceId) {
        return "resource " + resourceId + " deleted";
    }
}