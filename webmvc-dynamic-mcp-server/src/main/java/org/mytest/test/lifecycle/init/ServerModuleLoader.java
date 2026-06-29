package org.mytest.test.lifecycle.init;

import org.mytest.test.coordinator.McpRegisterCoordinator;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @author gemo
 * @date 2026/6/29 15:30
 */
@Component
public class ServerModuleLoader implements ApplicationRunner {
    private final McpRegisterCoordinator mcpRegisterCoordinator;

    public ServerModuleLoader(McpRegisterCoordinator mcpRegisterCoordinator) {
        this.mcpRegisterCoordinator = mcpRegisterCoordinator;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        mcpRegisterCoordinator.initialize();
    }
}
