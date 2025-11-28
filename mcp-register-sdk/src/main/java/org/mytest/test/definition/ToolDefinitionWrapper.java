package org.mytest.test.definition;

import lombok.Data;

import java.util.List;

/**
 * @author gemo
 */
@Data
public class ToolDefinitionWrapper {
    private String requestMethod;
    private String requestPath;
    private List<PathParamDefinition> pathParams;
    private DefaultToolDefinition toolDefinition;
}
