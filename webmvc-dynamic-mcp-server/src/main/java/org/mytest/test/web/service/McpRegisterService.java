package org.mytest.test.web.service;

import jakarta.servlet.http.HttpServletRequest;
import org.mytest.test.common.definition.ModuleDefinition;
import org.mytest.test.common.definition.ServiceInstance;
import org.mytest.test.common.definition.ServiceReportInfo;
import org.mytest.test.coordinator.McpRegisterCoordinator;
import org.mytest.test.utils.IpUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

/**
 * @author gemo
 * @date 2026/6/11 16:33
 */
@Service
public class McpRegisterService {

    @Autowired
    private McpRegisterCoordinator mcpRegisterCoordinator;

    public void register(ServiceReportInfo serviceReportInfo, HttpServletRequest request) {
        String clientIp = IpUtils.getClientIp(request);

        List<ModuleDefinition> modules = serviceReportInfo.getModules();
        ServiceInstance si = serviceReportInfo.getServiceInstance();

        HashMap<String, ModuleDefinition> moduleDefinitionMap = new HashMap<>();
        HashMap<String, ServiceInstance> serviceInstanceMap = new HashMap<>();
        for (ModuleDefinition module : modules) {
            ServiceInstance serviceInstance = new ServiceInstance();
            serviceInstance.setModuleId(module.getModuleId());
            serviceInstance.setIp(clientIp);
            serviceInstance.setPort(si.getPort());
            serviceInstance.setContextPath(si.getContextPath());
            serviceInstanceMap.put(module.getModuleId(), serviceInstance);
            moduleDefinitionMap.put(module.getModuleId(), module);
        }

        mcpRegisterCoordinator.register(moduleDefinitionMap, serviceInstanceMap);
    }
}
