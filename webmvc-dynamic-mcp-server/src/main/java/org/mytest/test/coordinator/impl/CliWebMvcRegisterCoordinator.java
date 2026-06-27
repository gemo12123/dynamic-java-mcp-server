package org.mytest.test.coordinator.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.mytest.test.coordinator.AbstractWebMvcMcpRegisterCoordinator;
import org.mytest.test.module.ModuleManager;
import org.mytest.test.router.McpRouter;
import org.mytest.test.service.ServiceManager;
import org.springframework.beans.factory.ObjectProvider;

/**
 * @author gemo
 * @date 2026/6/27 15:53
 */
public class CliWebMvcRegisterCoordinator extends AbstractWebMvcMcpRegisterCoordinator {

    public CliWebMvcRegisterCoordinator(ObjectProvider<ObjectMapper> objectMapperProvider, ServiceManager serviceManager, ModuleManager moduleManager, McpRouter mcpRouter) {
        super(objectMapperProvider, serviceManager, moduleManager, mcpRouter);
    }

    @Override
    public void initialize() {
        // CLI模式下无需初始化
    }
}
