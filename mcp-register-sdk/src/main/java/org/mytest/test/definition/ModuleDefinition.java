package org.mytest.test.definition;

import lombok.Data;

import java.util.List;

/**
 * @author gemo
 * @date 2025/11/28 16:51
 */
@Data
public class ModuleDefinition {
    private String moduleId;

    private String moduleName;

    private String description;

    private String version;

    private List<ToolDefinitionWrapper> tools;

}
