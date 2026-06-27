package org.mytest.test.config;

import org.mytest.test.component.capture.McpToolCapture;
import org.mytest.test.component.provider.AddressProvider;
import org.mytest.test.component.provider.FixedAddressProvider;
import org.mytest.test.component.task.McpReportTask;
import org.mytest.test.properties.DynamicMcpRegisterProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author gemo
 */
@EnableScheduling
@ConditionalOnWebApplication
@EnableConfigurationProperties(DynamicMcpRegisterProperties.class)
@ConditionalOnProperty(name = "dynamic.mcp.register.enabled", havingValue = "true", matchIfMissing = true)
public class McpRegisterConfiguration {

    @Bean
    public McpToolCapture mcpToolCapture(ConfigurableApplicationContext context) {
        return new McpToolCapture(context);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(name = "dynamic.mcp.register.report-configuration.address-source", havingValue = "fixed")
    public AddressProvider fixedAddressProvider(DynamicMcpRegisterProperties properties) {
        return new FixedAddressProvider();
    }

    @Bean
    public McpReportTask mcpReportTask(
            AddressProvider addressProvider,
            McpToolCapture mcpToolCapture,
            DynamicMcpRegisterProperties properties,
            ServerProperties serverProperties) {
        return new McpReportTask(addressProvider, mcpToolCapture, properties, serverProperties);
    }
}
