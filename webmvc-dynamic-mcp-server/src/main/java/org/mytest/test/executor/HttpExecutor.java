package org.mytest.test.executor;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.mytest.test.common.definition.ToolDefinitionWrapper;
import org.mytest.test.service.ServiceManager;
import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * @author gemo
 */
@Slf4j
public class HttpExecutor implements BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> {

    private final String moduleId;
    private final ObjectMapper objectMapper;
    private final HttpMethod requestMethod;
    private final String requestPath;
    private final Set<String> requestParams;
    private final ServiceManager serviceManager;

    public HttpExecutor(String moduleId, ObjectMapper objectMapper,
                        ToolDefinitionWrapper toolDefinitionWrapper,
                        ServiceManager serviceManager) {
        this.moduleId = moduleId;
        this.objectMapper = objectMapper;
        this.serviceManager = serviceManager;
        this.requestMethod = HttpMethod.valueOf(toolDefinitionWrapper.getRequestMethod());
        this.requestPath = toolDefinitionWrapper.getRequestPath();
        try {
            McpSchema.JsonSchema jsonSchema = objectMapper.readValue(toolDefinitionWrapper.getToolDefinition().inputSchema(), McpSchema.JsonSchema.class);
            this.requestParams = Optional.ofNullable(jsonSchema.properties())
                    .map(Map::keySet)
                    .orElse(Set.of());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public McpSchema.CallToolResult apply(McpSyncServerExchange mcpSyncServerExchange, Map<String, Object> request) {
        String requestPath = generateRequestPath();
        if (StringUtils.isEmpty(requestPath)) {
            return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("未找到服务器地址！")), true);
        }
        return HttpMethod.GET == requestMethod ? doGet(request) : doPost(request);
    }

    private String generateRequestPath() {
        return serviceManager.getServiceInstance(moduleId)
                .map(instance -> "http://" + instance.getIp() + ":" + instance.getPort() + requestPath)
                .orElse(null);
    }

    private McpSchema.CallToolResult doGet(Map<String, Object> request) {
        // 使用HTTPClient调用
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            URIBuilder uriBuilder = new URIBuilder(requestPath);
            for (String param : requestParams) {
                uriBuilder.addParameter(param, String.valueOf(request.get(param)));
            }
            HttpGet getRequest = new HttpGet(uriBuilder.build());
            try (CloseableHttpResponse response = httpClient.execute(getRequest)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    log.warn("HTTP GET请求异常！请求地址：{}，响应码：{}", requestPath, statusCode);
                    return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("HTTP GET请求异常！")), true);
                }
                String responseContent = EntityUtils.toString(response.getEntity());
                return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent(responseContent)), false);
            }
        } catch (Exception e) {
            log.error("执行异常！", e);
        }
        return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("执行异常！")), true);
    }

    private McpSchema.CallToolResult doPost(Map<String, Object> request) {

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost postRequest = new HttpPost(requestPath);
            Optional<String> param = requestParams.stream().findFirst();
            if (param.isPresent()) {
                postRequest.setHeader("Content-Type", "application/json");
                postRequest.setEntity(new StringEntity(objectMapper.writeValueAsString(param.get()), ContentType.APPLICATION_JSON));
            }
            try (CloseableHttpResponse response = httpClient.execute(postRequest)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    log.warn("HTTP POST请求异常！请求地址：{}，响应码：{}", requestPath, statusCode);
                    return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("HTTP POST请求异常！")), true);
                }
                String responseContent = EntityUtils.toString(response.getEntity());
                return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent(responseContent)), false);
            }

        } catch (Exception e) {
            log.error("执行异常！", e);
        }
        return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("执行异常！")), true);
    }

}
