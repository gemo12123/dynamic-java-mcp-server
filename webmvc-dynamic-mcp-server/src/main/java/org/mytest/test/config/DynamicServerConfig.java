package org.mytest.test.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpSyncServer;
import org.mytest.test.coordinator.McpRegisterCoordinator;
import org.mytest.test.coordinator.impl.CliWebMvcRegisterCoordinator;
import org.mytest.test.module.CliModuleManager;
import org.mytest.test.module.ModuleManager;
import org.mytest.test.router.DefaultMcpRouter;
import org.mytest.test.router.McpRouter;
import org.mytest.test.service.CliServiceManager;
import org.mytest.test.service.ServiceManager;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

/**
 * @author gemo
 * @date 2026/6/27 16:10
 */
@Configuration
public class DynamicServerConfig {
    @Bean
    public McpRouter mcpRouter() {
        return new DefaultMcpRouter();
    }

    @Configuration
    @ConditionalOnProperty(name = "dynamic-mcp-server.mode", havingValue = "cli")
//    @EnableAutoConfiguration(exclude = {EurekaClientAutoConfiguration.class,EurekaDiscoveryClientConfiguration.class})
    protected static class McpLocalConfiguration {
        @Bean
        public ServiceManager serviceManager() {
            return new CliServiceManager();
        }

        @Bean
        public ModuleManager moduleManager() {
            return new CliModuleManager();
        }

        @Bean
        public McpRegisterCoordinator<McpSyncServer> mcpRegisterCoordinator(ObjectProvider<ObjectMapper> objectMapperProvider,
                                                                            ServiceManager serviceManager,
                                                                            ModuleManager moduleManager,
                                                                            McpRouter mcpRouter) {
            return new CliWebMvcRegisterCoordinator(objectMapperProvider, serviceManager, moduleManager, mcpRouter);
        }

        @Bean
        public DataSource dataSource() {
            return new DataSource() {
                @Override
                public Connection getConnection() throws SQLException {
                    return null;
                }

                @Override
                public Connection getConnection(String username, String password) throws SQLException {
                    return null;
                }

                @Override
                public PrintWriter getLogWriter() throws SQLException {
                    return null;
                }

                @Override
                public void setLogWriter(PrintWriter out) throws SQLException {

                }

                @Override
                public void setLoginTimeout(int seconds) throws SQLException {

                }

                @Override
                public int getLoginTimeout() throws SQLException {
                    return 0;
                }

                @Override
                public <T> T unwrap(Class<T> iface) throws SQLException {
                    return null;
                }

                @Override
                public boolean isWrapperFor(Class<?> iface) throws SQLException {
                    return false;
                }

                @Override
                public Logger getParentLogger() throws SQLFeatureNotSupportedException {
                    return null;
                }
            };
        }
    }
}
