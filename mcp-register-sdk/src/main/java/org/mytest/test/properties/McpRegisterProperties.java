package org.mytest.test.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author gemo
 * @date 2025/11/28 16:20
 */
@Data
@ConfigurationProperties(prefix = "mcp.register")
public class McpRegisterProperties {
    private boolean enabled = true;

    private String moduleId;

    private String moduleName;

    private String moduleDescription;

    private String moduleVersion;


    public static class ReportConfiguration {
        private AddressSource addressSource = AddressSource.FIXED;

        private String fixedUrlPrefix;
    }

    public enum AddressSource{
        FIXED;
    }
}
