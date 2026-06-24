package org.mytest.test.coordinator;

import io.modelcontextprotocol.spec.McpServerTransportProvider;
import org.mytest.test.common.definition.ModuleDefinition;
import org.mytest.test.common.definition.ServiceInstance;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.util.List;
import java.util.Map;

/**
 * @author gemo
 */
public interface McpRegisterCoordinator<T> {

    /**
     * 注册模块
     *
     * @param moduleDefinitionMap
     * @param serviceInstanceMap
     */
    void register(Map<String, ModuleDefinition> moduleDefinitionMap, Map<String, ServiceInstance> serviceInstanceMap);

    /**
     * 初始化
     */
    void initialize();

    /**
     * 注销模块
     *
     * @param moduleId
     */
    void invalidate(String moduleId);

    /**
     * 获取全部 McpServer
     *
     * @return
     */
    List<T> getMcpServers();

    /**
     * 获取全部 McpServerTransportProvider
     *
     * @return
     */
    List<McpServerTransportProvider> getMcpServerTransportProviders();

    interface ModuleServerRecord<T> {

        ModuleDefinition getModuleDefinition();

        void setModuleDefinition(ModuleDefinition definition);

        McpServerTransportProvider getTransportProvider();

        T getServer();

        RouterFunction<ServerResponse> getRouterFunction();
    }
}
