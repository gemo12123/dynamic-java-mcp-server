//package org.mytest.test.coordinator;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import io.modelcontextprotocol.server.McpServer;
//import io.modelcontextprotocol.server.McpServerFeatures;
//import io.modelcontextprotocol.server.McpSyncServer;
//import io.modelcontextprotocol.server.transport.WebMvcSseServerTransportProvider;
//import io.modelcontextprotocol.spec.McpSchema;
//import org.apache.commons.lang3.tuple.Triple;
//import org.mytest.test.definition.ModuleDefinition;
//import org.mytest.test.definition.ToolDefinitionWrapper;
//import org.mytest.test.executor.HttpExecutor;
//import org.mytest.test.router.McpRouter;
//import org.mytest.test.service.ServiceManager;
//import org.springframework.ai.mcp.server.autoconfigure.McpServerProperties;
//import org.springframework.ai.tool.definition.DefaultToolDefinition;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.springframework.web.servlet.function.RouterFunction;
//import org.springframework.web.servlet.function.ServerResponse;
//
//import java.time.Duration;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Map;
//import java.util.concurrent.ConcurrentHashMap;
//
///**
// * @author gemo
// */
//@Component
//public class DefaultMcpRegisterCoordinator implements McpRegisterCoordinator {
//
//    private static final String ROOT_CONTEXT_PATH = "/dynamic-mcp";
//
//    private static final String SSE_ENDPOINT_TEMPLATE = "%s/sse/%s";
//    private static final String SSE_MESSAGE_ENDPOINT_TEMPLATE = "%s/mcp/message/%s";
//
//    private static final Map<String, Triple<List<ToolDefinitionWrapper>, McpSyncServer, RouterFunction<ServerResponse>>> MODULE_CACHE = new ConcurrentHashMap<>();
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @Autowired
//    private McpRouter mcpRouter;
//
//    @Autowired
//    private ServiceManager serviceManager;
//
//    @Override
//    public void register(ModuleDefinition moduleDefinition) {
//        String moduleId = moduleDefinition.getModuleId();
//        if (MODULE_CACHE.containsKey(moduleId)) {
//            return;
//        }
//        generateServer(moduleDefinition);
//        mcpRouter.register(moduleId, MODULE_CACHE.get(moduleId).getRight());
//        serviceManager.register(moduleId, moduleDefinition.getServiceInstances());
//    }
//
//    /**
//     * 基于内存实现，没有初始化逻辑
//     */
//    @Override
//    public void initialize() {
//    }
//
//    @Override
//    public synchronized void invalidate(String moduleId) {
//        MODULE_CACHE.remove(moduleId);
//        mcpRouter.invalidate(moduleId);
//        serviceManager.invalidate(moduleId);
//    }
//
//    protected void generateServer(ModuleDefinition moduleDefinition) {
//        String moduleId = moduleDefinition.getModuleId();
//        McpServerProperties serverProperties = generateMcpServerProperties(moduleId);
//        WebMvcSseServerTransportProvider transportProvider = new WebMvcSseServerTransportProvider(objectMapper,
//                serverProperties.getBaseUrl(),
//                serverProperties.getSseMessageEndpoint(),
//                serverProperties.getSseEndpoint());
//        McpSchema.Implementation serverInfo = new McpSchema.Implementation(serverProperties.getName(),
//                serverProperties.getVersion());
//        McpSyncServer mcpSyncServer = McpServer.sync(transportProvider)
//                .serverInfo(serverInfo)
//                .capabilities(McpSchema.ServerCapabilities.builder()
//                        .tools(true)
//                        .build())
//                .tools(generateSyncToolSpecifications(moduleDefinition))
//                .instructions(serverProperties.getInstructions())
//                .requestTimeout(serverProperties.getRequestTimeout())
//                .build();
//
//        RouterFunction<ServerResponse> routerFunction = transportProvider.getRouterFunction();
//
//        MODULE_CACHE.put(moduleId, Triple.of(moduleDefinition.getTools(), mcpSyncServer, routerFunction));
//    }
//
//    protected List<McpServerFeatures.SyncToolSpecification> generateSyncToolSpecifications(ModuleDefinition definition) {
//        List<ToolDefinitionWrapper> tools = definition.getTools();
//        List<McpServerFeatures.SyncToolSpecification> toolSpecifications = new ArrayList<>(tools.size());
//        for (ToolDefinitionWrapper wrapper : tools) {
//            DefaultToolDefinition toolDefinition = wrapper.getToolDefinition();
//            McpSchema.Tool tool = new McpSchema.Tool(toolDefinition.name(), toolDefinition.description(), toolDefinition.inputSchema());
//            McpServerFeatures.SyncToolSpecification syncToolSpecification
//                    = new McpServerFeatures.SyncToolSpecification(tool, new HttpExecutor(definition.getModuleId(), objectMapper, wrapper, serviceManager));
//            toolSpecifications.add(syncToolSpecification);
//        }
//        return toolSpecifications;
//    }
//
//    protected McpServerProperties generateMcpServerProperties(String moduleId) {
//        McpServerProperties serverProperties = new McpServerProperties();
//        serverProperties.setRequestTimeout(Duration.ofSeconds(30L));
//        serverProperties.setSseEndpoint(String.format(SSE_ENDPOINT_TEMPLATE, ROOT_CONTEXT_PATH, moduleId));
//        serverProperties.setSseMessageEndpoint(String.format(SSE_MESSAGE_ENDPOINT_TEMPLATE, ROOT_CONTEXT_PATH, moduleId));
//        return serverProperties;
//    }
//}
