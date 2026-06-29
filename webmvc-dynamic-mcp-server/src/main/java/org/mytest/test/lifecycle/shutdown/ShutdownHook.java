package org.mytest.test.lifecycle.shutdown;

import io.modelcontextprotocol.server.McpSyncServer;
import lombok.extern.slf4j.Slf4j;
import org.mytest.test.coordinator.McpRegisterCoordinator;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author gemo
 * @date 2026/6/29 15:33
 */
@Slf4j
@Component
public class ShutdownHook implements ApplicationListener<ContextClosedEvent> {

    private final McpRegisterCoordinator<McpSyncServer> mcpRegisterCoordinator;

    public ShutdownHook(McpRegisterCoordinator<McpSyncServer> mcpRegisterCoordinator) {
        this.mcpRegisterCoordinator = mcpRegisterCoordinator;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        List<McpSyncServer> mcpServers = mcpRegisterCoordinator.getMcpServers();
        for (McpSyncServer mcpServer : mcpServers) {
            try {
                mcpServer.close();
            } catch (Exception e) {
                log.error("Close mcp server error!", e);
            }
        }
    }
}
