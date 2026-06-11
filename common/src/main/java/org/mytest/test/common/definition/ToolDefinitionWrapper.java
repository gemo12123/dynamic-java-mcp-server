package org.mytest.test.common.definition;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * @author gemo
 */
@Data
@Builder
public class ToolDefinitionWrapper {
    private String requestMethod;
    private String requestPath;
    private List<PathParamDefinition> pathParams;
    private DefaultToolDefinition toolDefinition;
}
