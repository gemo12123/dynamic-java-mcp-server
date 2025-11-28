package org.mytest.test.config;

import org.mytest.test.properties.McpRegisterProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author gemo
 */
@EnableScheduling
@ConditionalOnWebApplication
@EnableConfigurationProperties(McpRegisterProperties.class)
@ConditionalOnProperty(name = "mcp.register.enabled", havingValue = "true", matchIfMissing = true)
public class McpRegisterConfiguration {
}
