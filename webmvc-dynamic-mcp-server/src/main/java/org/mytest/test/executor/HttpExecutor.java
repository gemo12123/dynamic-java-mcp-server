package org.mytest.test.executor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.mytest.test.common.definition.PathParamDefinition;
import org.mytest.test.common.definition.StructResponseDefinition;
import org.mytest.test.common.definition.ToolDefinitionWrapper;
import org.mytest.test.service.ServiceManager;
import org.springframework.http.HttpMethod;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * @author gemo
 */
@Slf4j
public class HttpExecutor implements BiFunction<McpSyncServerExchange, Map<String, Object>, McpSchema.CallToolResult> {

    private static final String HTTP_URL_TEMPLATE = "http://%s:%s%s%s";

    private final String moduleId;
    private final HttpMethod requestMethod;
    private final String requestPath;
    private final List<PathParamDefinition> pathParams;
    private final Set<String> requestParams;
    private final boolean removeStructResponse;
    private final StructResponseDefinition structResponseDefinition;

    private final ServiceManager serviceManager;

    public HttpExecutor(String moduleId, ObjectMapper objectMapper,
                        ToolDefinitionWrapper toolDefinitionWrapper,
                        ServiceManager serviceManager) {
        this.moduleId = moduleId;
        this.requestMethod = HttpMethod.valueOf(toolDefinitionWrapper.getRequestMethod());
        this.requestPath = toolDefinitionWrapper.getRequestPath();
        this.pathParams = Optional.ofNullable(toolDefinitionWrapper.getPathParams())
                .orElse(Collections.emptyList());
        this.removeStructResponse = Optional.ofNullable(toolDefinitionWrapper.getStructResponseDefinition())
                .filter(item -> Optional.ofNullable(item.getRemoveStructResponse()).orElse(false))
                .isPresent();
        this.structResponseDefinition = toolDefinitionWrapper.getStructResponseDefinition();
        try {
            McpSchema.JsonSchema jsonSchema = objectMapper.readValue(toolDefinitionWrapper.getToolDefinition().inputSchema(), McpSchema.JsonSchema.class);
            this.requestParams = Optional.ofNullable(jsonSchema.properties())
                    .map(Map::keySet)
                    .orElse(Set.of());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid schema!", e);
        }
        this.serviceManager = serviceManager;
    }

    @Override
    public McpSchema.CallToolResult apply(McpSyncServerExchange mcpSyncServerExchange, Map<String, Object> request) {
        String path = generateRequestPath(request);
        if (StringUtils.isEmpty(path)) {
            return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("未找到服务器地址！")), true);
        }

        long start = System.currentTimeMillis();
        Map<String, String> headers = null;
        McpSchema.CallToolResult result = doExecute(path, headers, request);
        log.info("调用{}请求耗时{}ms", path, System.currentTimeMillis() - start);
        return result;
    }

    private McpSchema.CallToolResult doExecute(String path, Map<String, String> headers, Map<String, Object> params) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpUriRequest request = generateRequest(path, headers, params);

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    log.warn("HTTP 请求异常！请求地址：{}，响应码：{}", path, statusCode);
                    return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("工具执行异常！")), true);
                }
                String responseContent = EntityUtils.toString(response.getEntity());
                return parseResponse(responseContent);
            }
        } catch (Exception e) {
            log.warn("HTTP 请求异常！请求地址：{}, 请求参数：{}", path, params, e);
        }
        return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("工具执行异常!")), true);
    }

    private McpSchema.CallToolResult parseResponse(String response) {
        if (!removeStructResponse) {
            return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent(response)), false);
        }

        JSONObject responseJson = JSON.parseObject(response);
        // 状态码校验
        if (StringUtils.isNotEmpty(structResponseDefinition.getStatusField())) {
            boolean isSuccess = Optional.ofNullable(responseJson.getString(structResponseDefinition.getStatusField()))
                    .filter(item -> structResponseDefinition.getStatusExpectValue().equals(item))
                    .isPresent();
            // 与预期不一致则直接返回异常
            if (!isSuccess) {
                return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent("工具执行异常！")), true);
            }
        }

        // 移除外层包装后返回
        if (StringUtils.isNotEmpty(structResponseDefinition.getDataField())) {
            return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent(JSON.toJSONString(responseJson.get(structResponseDefinition.getDataField())))), false);
        }
        return new McpSchema.CallToolResult(List.of(new McpSchema.TextContent(response)), false);
    }

    private HttpUriRequest generateRequest(String path, Map<String, String> headers, Map<String, Object> params) throws URISyntaxException {
        HttpUriRequest request;
        if (requestMethod.equals(HttpMethod.GET) || requestMethod.equals(HttpMethod.DELETE)) {
            URI uri = generateUri(path, params);
            request = requestMethod.equals(HttpMethod.GET) ? new HttpGet(uri) : new HttpDelete(uri);
        } else if (requestMethod.equals(HttpMethod.POST) || requestMethod.equals(HttpMethod.PUT)) {
            request = generateEntityRequest(path, params);
        } else {
            throw new UnsupportedOperationException("Unsupported http method: " + requestMethod.name());
        }
        return request;
    }

    private HttpUriRequest generateEntityRequest(String path, Map<String, Object> params) {
        Set<String> pathParamNames = pathParams.stream()
                .map(PathParamDefinition::getParamName)
                .collect(Collectors.toSet());
        Optional<String> param = requestParams.stream()
                .filter(item -> !pathParamNames.contains(item))
                .findFirst();
        HttpEntityEnclosingRequestBase request = requestMethod.equals(HttpMethod.POST) ? new HttpPost(path) : new HttpPut(path);
        if (param.isPresent()) {
            request.setHeader("Content-Type", "application/json; charset=utf-8");
            request.setEntity(new StringEntity(JSON.toJSONString(params.get(param.get())), ContentType.APPLICATION_JSON));
        }
        return request;
    }

    private URI generateUri(String path, Map<String, Object> params) throws URISyntaxException {
        URIBuilder uriBuilder = new URIBuilder(path);
        for (String requestParam : requestParams) {
            Object paramValue = params.get(requestParam);
            if (paramValue instanceof Map<?, ?> mapParamValue) {
                for (Map.Entry<?, ?> entry : mapParamValue.entrySet()) {
                    uriBuilder.addParameter(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
                }
                continue;
            }
            uriBuilder.addParameter(requestParam, String.valueOf(paramValue));
        }
        return uriBuilder.build();
    }

    private String generateRequestPath(Map<String, Object> request) {
        String path = serviceManager.getServiceInstance(this.moduleId)
                .map(item -> String.format(HTTP_URL_TEMPLATE,
                        item.getIp(),
                        item.getPort(),
                        Optional.ofNullable(item.getContextPath()).orElse(""),
                        this.requestPath))
                .orElseThrow(() -> new IllegalArgumentException("无法获取服务！"));
        for (PathParamDefinition pathParam : this.pathParams) {
            String pathPlaceHolder = pathParam.getPathPlaceHolder();
            Boolean isConstant = Optional.ofNullable(pathParam.getIsConstant()).orElse(false);
            String replaceValue = isConstant ? Optional.ofNullable(pathParam.getConstantValue())
                    .map(String::valueOf)
                    .orElseThrow(() -> new IllegalArgumentException("服务器异常，无法找到工具请求！"))
                    : Optional.ofNullable(request.get(pathParam.getParamName()))
                    .map(String::valueOf)
                    .orElseThrow(() -> new IllegalArgumentException(pathParam.getParamName() + "参数获取异常！"));

            path = path.replace("{" + pathPlaceHolder + "}", replaceValue);
        }
        return path;
    }

}
