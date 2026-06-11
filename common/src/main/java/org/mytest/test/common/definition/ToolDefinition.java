package org.mytest.test.common.definition;

/**
 * @author gemo
 * @date 2025/11/28 16:40
 */
public interface ToolDefinition {

    /**
     * The tool name. Unique within the tool set provided to a model.
     */
    String name();

    /**
     * The tool description, used by the AI model to determine what the tool does.
     */
    String description();

    /**
     * The schema of the parameters used to call the tool.
     */
    String inputSchema();

}
