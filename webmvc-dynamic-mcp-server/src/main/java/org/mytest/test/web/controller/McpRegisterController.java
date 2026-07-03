package org.mytest.test.web.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.mytest.test.common.definition.ServiceReportInfo;
import org.mytest.test.web.service.McpRegisterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author gemo
 * @date 2026/6/11 16:33
 */
@RestController
@RequestMapping("/dynamic-mcp-server/mcp")
public class McpRegisterController {
    @Autowired
    private McpRegisterService mcpRegisterService;

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody ServiceReportInfo serviceReportInfo, HttpServletRequest request) {
        if (serviceReportInfo == null
                || serviceReportInfo.getModules() == null
                || serviceReportInfo.getModules().isEmpty()) {
            return Map.of("status", 500, "msg", "请求参数异常！");
        }

        // TODO 格式校验
        mcpRegisterService.register(serviceReportInfo, request);

        return Map.of("status", 200);
    }
}
