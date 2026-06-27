package org.mytest.test.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * @author gemo
 * @date 2025/11/28 16:20
 */
@Data
@ConfigurationProperties(prefix = "dynamic.mcp.register")
public class McpRegisterProperties {
    private boolean enabled = true;

    private String moduleId;

    private String moduleName;

    private String moduleDescription;

    private String moduleVersion;

    private ReportConfiguration reportConfiguration;

    private Map<String, ModuleInfo> moduleConfiguration;


    @Data
    public static class ReportConfiguration {
        private AddressSource addressSource = AddressSource.FIXED;

        private String fixedUrlPrefix;

        private Integer servicePort;
    }

    public enum AddressSource{
        FIXED;
    }

    @Data
    public static class ModuleInfo{
        private String moduleId;

        private String moduleName;

        private String moduleDescription;

        private String moduleVersion;
    }
}
