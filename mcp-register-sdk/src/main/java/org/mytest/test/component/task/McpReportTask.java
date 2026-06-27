package org.mytest.test.component.task;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.mytest.test.component.capture.McpToolCapture;
import org.mytest.test.component.provider.AddressProvider;
import org.mytest.test.common.definition.ModuleDefinition;
import org.mytest.test.common.definition.ServiceInstance;
import org.mytest.test.common.definition.ServiceReportInfo;
import org.mytest.test.common.definition.ToolDefinitionWrapper;
import org.mytest.test.properties.DynamicMcpRegisterProperties;
import org.mytest.test.utils.JsonParser;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author gemo
 * @date 2025/11/28 16:16
 */
@Slf4j
public class McpReportTask implements ApplicationRunner {

    private static final String REQUEST_PATH = "/dynamic-mcp-server/mcp/register";

    private final AddressProvider addressProvider;

    private final McpToolCapture mcpToolCapture;

    private final DynamicMcpRegisterProperties properties;

    private final ServerProperties serverProperties;

    private volatile String reportContent;

    public McpReportTask(AddressProvider addressProvider, McpToolCapture mcpToolCapture, DynamicMcpRegisterProperties properties, ServerProperties serverProperties) {
        this.addressProvider = addressProvider;
        this.mcpToolCapture = mcpToolCapture;
        this.properties = properties;
        this.serverProperties = serverProperties;
    }


    @Override
    public void run(ApplicationArguments args) throws Exception {
        report();
    }

    @Scheduled(cron = "0 * * * * ?")
    public void report() {
        tryInit();
        if (!StringUtils.hasLength(reportContent)) {
            return;
        }
        Optional<String> ipAddress = addressProvider.getIpAddress();
        if (!ipAddress.isPresent()) {
            log.warn("{} 未获取到地址信息！", addressProvider.getClass().getSimpleName());
            return;
        }
        String address = ipAddress.get();
        String requestPath = formatRequestPath(address);
        doReport(requestPath);
    }

    private void doReport(String requestPath) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost postRequest = new HttpPost();
            postRequest.setHeader("Content-Type", "application/json; charset=utf-8");
            postRequest.setEntity(new StringEntity(this.reportContent, ContentType.APPLICATION_JSON));
            try (CloseableHttpResponse response = httpClient.execute(postRequest)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    log.warn("MCP注册响应异常！请求地址：{}，响应码：{}", requestPath, statusCode);
                    return;
                }
                HttpEntity entity = response.getEntity();
                String responseContent = EntityUtils.toString(entity);
                if (Optional.ofNullable(JsonParser.readTree(responseContent))
                        .map(item -> item.findValue("status"))
                        .map(item -> item.asInt(-1))
                        .orElse(-1) != 200) {
                    log.warn("MCP 注册响应异常！请求地址：{}，响应：{}", requestPath, responseContent);
                }

            }
        } catch (Exception e) {
            log.warn("MCP 注册异常！请求地址：{}", requestPath, e);
        }
    }

    private void tryInit() {
        if (this.reportContent == null) {
            synchronized (this) {
                if (this.reportContent == null) {
                    Map<String, List<ToolDefinitionWrapper>> moduleToolDefinitions = this.mcpToolCapture.getModuleToolDefinitions();
                    if (moduleToolDefinitions.isEmpty()) {
                        log.info("当前容器内未发现可用工具!");
                        return;
                    }
                    List<ModuleDefinition> list = new ArrayList<>();
                    for (Map.Entry<String, List<ToolDefinitionWrapper>> entry : moduleToolDefinitions.entrySet()) {
                        ModuleDefinition moduleDefinition = new ModuleDefinition();
                        moduleDefinition.setModuleId(findModuleId(entry.getKey()));
                        moduleDefinition.setModuleName(findModuleName(entry.getKey()));
                        moduleDefinition.setDescription(findModuleDescription(entry.getKey()));
                        moduleDefinition.setVersion(findModuleVersion(entry.getKey()));
                        moduleDefinition.setTools(entry.getValue());
                        list.add(moduleDefinition);
                    }

                    ServiceInstance serviceInstance = new ServiceInstance();
                    serviceInstance.setPort(Optional.ofNullable(properties.getReportConfiguration())
                            .map(DynamicMcpRegisterProperties.ReportConfiguration::getServicePort)
                            .orElseGet(() -> Optional.ofNullable(serverProperties.getPort()).orElse(8080)));
                    serviceInstance.setContextPath(Optional.ofNullable(serverProperties.getServlet())
                            .map(ServerProperties.Servlet::getContextPath)
                            .orElse(""));

                    ServiceReportInfo serviceReportInfo = new ServiceReportInfo();
                    serviceReportInfo.setModules(list);
                    serviceReportInfo.setServiceInstance(serviceInstance);
                    this.reportContent = JsonParser.toJson(serviceReportInfo);
                }
            }
        }
    }

    private String formatRequestPath(String address) {
        if (address.endsWith("/")) {
            address = address.substring(0, address.length() - 1);
        }
        return address + REQUEST_PATH;
    }

    private String findModuleId(String module) {
        if (StringUtils.hasText(module)) {
            return Optional.ofNullable(this.properties.getModuleConfiguration())
                    .map(item -> item.get(module))
                    .map(DynamicMcpRegisterProperties.ModuleInfo::getModuleId)
                    .orElseThrow(() -> new IllegalArgumentException("Cannot find " + module + " module define!"));
        }
        return this.properties.getModuleId();
    }

    private String findModuleName(String module) {
        if (StringUtils.hasText(module)) {
            return Optional.ofNullable(this.properties.getModuleConfiguration())
                    .map(item -> item.get(module))
                    .map(DynamicMcpRegisterProperties.ModuleInfo::getModuleName)
                    .orElseThrow(() -> new IllegalArgumentException("Cannot find " + module + " module define!"));
        }
        return this.properties.getModuleName();
    }

    private String findModuleDescription(String module) {
        if (StringUtils.hasText(module)) {
            return Optional.ofNullable(this.properties.getModuleConfiguration())
                    .map(item -> item.get(module))
                    .map(DynamicMcpRegisterProperties.ModuleInfo::getModuleDescription)
                    .orElse(null);
        }
        return this.properties.getModuleDescription();
    }

    private String findModuleVersion(String module) {
        if (StringUtils.hasText(module)) {
            return Optional.ofNullable(this.properties.getModuleConfiguration())
                    .map(item -> item.get(module))
                    .map(DynamicMcpRegisterProperties.ModuleInfo::getModuleVersion)
                    .orElse("1.0.0");
        }
        return Optional.ofNullable(this.properties.getModuleVersion())
                .orElse("1.0.0");
    }
}
