package org.mytest.test.definition;

import lombok.Data;
import org.springframework.ai.tool.definition.DefaultToolDefinition;

/**
 * @author gemo
 */
@Data
public class ToolDefinitionWrapper {
    private String requestMethod;
    private String requestPath;
    private DefaultToolDefinition toolDefinition;
}
