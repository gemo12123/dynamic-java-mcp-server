package org.mytest.test.controller;

import org.mytest.test.annotation.Tool;
import org.mytest.test.model.ResourceDetailResponse;
import org.mytest.test.model.StructuredApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/examples/struct")
public class StructResponseExampleController {

    @GetMapping("/unwrapped")
    @Tool(name = "getUnwrappedStructResponse", description = "Structured response example that can be unwrapped")
    public StructuredApiResponse<ResourceDetailResponse> getUnwrapped() {
        return StructuredApiResponse.success("success", new ResourceDetailResponse("struct-1", "unwrapped demo", true));
    }

    @GetMapping("/wrapped")
    @Tool(name = "getWrappedStructResponse", description = "Structured response example that keeps the outer wrapper", removeStructResponse = false)
    public StructuredApiResponse<ResourceDetailResponse> getWrapped() {
        return StructuredApiResponse.success("success", new ResourceDetailResponse("struct-2", "wrapped demo", false));
    }
}