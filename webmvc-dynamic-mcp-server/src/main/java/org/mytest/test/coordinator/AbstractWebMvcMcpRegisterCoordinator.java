package org.mytest.test.coordinator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.WebMvcSseServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpServerTransportProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.mytest.test.common.definition.DefaultToolDefinition;
import org.mytest.test.common.definition.ModuleDefinition;
import org.mytest.test.common.definition.ServiceInstance;
import org.mytest.test.common.definition.ToolDefinitionWrapper;
import org.mytest.test.executor.HttpExecutor;
import org.mytest.test.module.ModuleManager;
import org.mytest.test.router.McpRouter;
import org.mytest.test.service.ServiceManager;
import org.mytest.test.utils.ModuleVersionComparator;
import org.springframework.ai.mcp.server.autoconfigure.McpServerProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import java.lang.ref.SoftReference;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author gemo
 * @date 2026/6/24 17:36
 */
@Slf4j
public abstract class AbstractWebMvcMcpRegisterCoordinator implements McpRegisterCoordinator<McpSyncServer>{

    private static final String ROOT_CONTEXT_PATH = "/dynamic-mcp-server";

    private static final String SSE_ENDPOINT_TEMPLATE = "%s/sse/%s";
    private static final String SSE_MESSAGE_ENDPOINT_TEMPLATE = "%s/mcp/message/%s";

    protected static final Map<String, ModuleServerRecord<McpSyncServer>> MODULE_CACHE = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String, SoftReference<Object>> LOCKS = new ConcurrentHashMap<>();

    protected final ObjectProvider<ObjectMapper> objectMapperProvider;

    protected final ServiceManager serviceManager;

    protected final ModuleManager moduleManager;

    protected final McpRouter mcpRouter;

    public AbstractWebMvcMcpRegisterCoordinator(ObjectProvider<ObjectMapper> objectMapperProvider,
                                                ServiceManager serviceManager,
                                                ModuleManager moduleManager,
                                                McpRouter mcpRouter) {
        this.objectMapperProvider = objectMapperProvider;
        this.serviceManager = serviceManager;
        this.moduleManager = moduleManager;
        this.mcpRouter = mcpRouter;
    }

    @Override
    public void register(Map<String, ModuleDefinition> moduleDefinitionMap, Map<String, ServiceInstance> serviceInstanceMap) {
        for (Map.Entry<String, ModuleDefinition> entry : moduleDefinitionMap.entrySet()) {
            String moduleId = entry.getKey();
            synchronized (getLock(moduleId)) {
                ModuleDefinition definition = entry.getValue();

                ModuleServerRecord<McpSyncServer> currentModuleServerRecord = MODULE_CACHE.get(moduleId);
                // 新注册模块
                if (currentModuleServerRecord == null) {
                    serviceManager.register(moduleId, serviceInstanceMap.get(moduleId));
                    generateServer(definition);
                    moduleManager.register(definition);
                    mcpRouter.register(moduleId, MODULE_CACHE.get(moduleId).getRouterFunction());
                    log.info("模块注册成功！模块ID：{}, 模块名称：{}", moduleId, definition.getModuleName());
                    continue;
                }

                // 存量模块，版本更新
                String reportVersion = definition.getVersion();
                String currentVersion = currentModuleServerRecord.getModuleDefinition().getVersion();
                int compare = ModuleVersionComparator.versionCompare(reportVersion, currentVersion);
                // 低于当前版本，忽略
                if (compare < 0) {
                    log.info("模块版本低于当前版本，将忽略此次上报！模块ID：{}, 模块名称：{}, 上报版本：{}，当前最新版本：{}",
                            moduleId,
                            definition.getModuleName(),
                            reportVersion,
                            currentVersion);
                    continue;
                }

                // 高于当前版本
                if (compare > 0) {
                    moduleManager.upgrade(moduleId, definition);
                    serviceManager.invalidate(moduleId);
                    serviceManager.register(moduleId, serviceInstanceMap.get(moduleId));
                    upgradeServer(moduleId, definition);
                    log.info("模块升级！模块ID：{}, 模块名称：{}, 由 {} 版本升级至版本{}",
                            moduleId,
                            definition.getModuleName(),
                            reportVersion,
                            currentVersion);
                    continue;
                }
                // 心跳，版本号不变，更新末次心跳时间
                serviceManager.register(moduleId, serviceInstanceMap.get(moduleId));
            }
        }
    }

    @Override
    public void invalidate(String moduleId) {
        moduleManager.invalidate(moduleId);
        serviceManager.invalidate(moduleId);
        mcpRouter.invalidate(moduleId);
        MODULE_CACHE.remove(moduleId);
        log.info("{} 模块已被注销！", moduleId);
    }

    @Override
    public List<McpSyncServer> getMcpServers() {
        return MODULE_CACHE.values()
                .stream()
                .map(ModuleServerRecord::getServer)
                .toList();
    }

    @Override
    public List<McpServerTransportProvider> getMcpServerTransportProviders() {
        return MODULE_CACHE.values()
                .stream()
                .map(ModuleServerRecord::getTransportProvider)
                .toList();
    }


    private void upgradeServer(String moduleId, ModuleDefinition newDefinition) {
        ModuleServerRecord<McpSyncServer> existModuleServerRecord = MODULE_CACHE.get(moduleId);
        McpSyncServer server = existModuleServerRecord.getServer();

        HashSet<McpServerFeatures.SyncToolSpecification> existTools = Sets.newHashSet(generateSyncToolSpecifications(existModuleServerRecord.getModuleDefinition()));
        HashSet<McpServerFeatures.SyncToolSpecification> newTools = Sets.newHashSet(generateSyncToolSpecifications(newDefinition));

        // 待移除工具列表
        Sets.SetView<McpServerFeatures.SyncToolSpecification> removeSet = Sets.difference(existTools, newTools);
        // 移除旧工具
        for (McpServerFeatures.SyncToolSpecification tool : removeSet) {
            String name = tool.tool().name();
            server.removeTool(name);
            log.info("从 {} 模块移除工具：{}！",moduleId,name);
        }

        // 待新增工具
        Sets.SetView<McpServerFeatures.SyncToolSpecification> addSet = Sets.difference(newTools, existTools);
        for (McpServerFeatures.SyncToolSpecification syncToolSpecification : addSet) {
            server.addTool(syncToolSpecification);
            log.info("向 {} 模块移除工具：{}！",moduleId, syncToolSpecification.tool().name());
        }
    }

    protected void generateServer(ModuleDefinition moduleDefinition) {
        String moduleId = moduleDefinition.getModuleId();
        McpServerProperties serverProperties = generateMcpServerProperties(moduleId);
        WebMvcSseServerTransportProvider transportProvider = new WebMvcSseServerTransportProvider(objectMapperProvider.getObject(),
                serverProperties.getBaseUrl(),
                serverProperties.getSseMessageEndpoint(),
                serverProperties.getSseEndpoint());
        McpSchema.Implementation serverInfo = new McpSchema.Implementation(serverProperties.getName(),
                serverProperties.getVersion());
        McpSyncServer mcpSyncServer = McpServer.sync(transportProvider)
                .serverInfo(serverInfo)
                .capabilities(McpSchema.ServerCapabilities.builder()
                        .tools(true)
                        .build())
                .tools(generateSyncToolSpecifications(moduleDefinition))
                .instructions(serverProperties.getInstructions())
                .requestTimeout(serverProperties.getRequestTimeout())
                .build();

        RouterFunction<ServerResponse> routerFunction = transportProvider.getRouterFunction();

        MODULE_CACHE.put(moduleId, new ModuleWebMvcServerRecord(moduleDefinition, transportProvider, mcpSyncServer, routerFunction));
        log.info("动态MCP Server建立！模块ID：{}", moduleId);
    }

    protected List<McpServerFeatures.SyncToolSpecification> generateSyncToolSpecifications(ModuleDefinition definition) {
        List<ToolDefinitionWrapper> tools = Optional.ofNullable(definition.getTools())
                .orElse(Collections.emptyList());
        List<McpServerFeatures.SyncToolSpecification> toolSpecifications = new ArrayList<>(tools.size());
        for (ToolDefinitionWrapper wrapper : tools) {
            DefaultToolDefinition toolDefinition = wrapper.getToolDefinition();
            McpSchema.Tool tool = new McpSchema.Tool(toolDefinition.name(), toolDefinition.description(), toolDefinition.inputSchema());
            McpServerFeatures.SyncToolSpecification syncToolSpecification
                    = new McpServerFeatures.SyncToolSpecification(tool, new HttpExecutor(definition.getModuleId(),
                    objectMapperProvider.getObject(),
                    wrapper,
                    serviceManager));
            toolSpecifications.add(syncToolSpecification);
        }
        return toolSpecifications;
    }

    protected McpServerProperties generateMcpServerProperties(String moduleId) {
        McpServerProperties serverProperties = new McpServerProperties();
        serverProperties.setRequestTimeout(Duration.ofSeconds(30L));
        serverProperties.setSseEndpoint(String.format(SSE_ENDPOINT_TEMPLATE, ROOT_CONTEXT_PATH, moduleId));
        serverProperties.setSseMessageEndpoint(String.format(SSE_MESSAGE_ENDPOINT_TEMPLATE, ROOT_CONTEXT_PATH, moduleId));
        return serverProperties;
    }

    private Object getLock(String key) {
        Object lock = LOCKS.compute(key, (k, v) -> {
                    if (v == null || v.get() == null) {
                        return new SoftReference<>(new Object());
                    }
                    return v;
                })
                .get();
        return lock;
    }


    @Data
    @AllArgsConstructor
    static class ModuleWebMvcServerRecord implements ModuleServerRecord<McpSyncServer>{
        private ModuleDefinition moduleDefinition;
        private McpServerTransportProvider transportProvider;
        private McpSyncServer server;
        private RouterFunction<ServerResponse> routerFunction;
    }
}
