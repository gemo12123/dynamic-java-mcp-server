package org.mytest.test.definition;

import lombok.Data;

import java.util.List;

/**
 * @author gemo
 */
@Data
public class ModuleDefinition {

    private String moduleId;

    private List<ServiceInstance> serviceInstances;

    private List<ToolDefinitionWrapper> tools;
}
