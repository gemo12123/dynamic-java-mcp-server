package org.mytest.test.common.definition;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author gemo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolDefinitionWrapper {
    private String requestMethod;
    private String requestPath;
    private List<PathParamDefinition> pathParams;
    private DefaultToolDefinition toolDefinition;
}
