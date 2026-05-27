package org.mytest.test.definition;

import org.mytest.test.utils.JsonSchemaGenerator;
import org.mytest.test.utils.ToolUtils;

import java.lang.reflect.Method;

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

    /**
     * Create a default {@link ToolDefinition} builder.
     */
    static DefaultToolDefinition.Builder builder() {
        return DefaultToolDefinition.builder();
    }
    static DefaultToolDefinition.Builder builder(Method method) {
        return DefaultToolDefinition.builder()
                .name(ToolUtils.getToolName(method))
                .description(ToolUtils.getToolDescription(method))
                .inputSchema(JsonSchemaGenerator.generateForMethodInput(method));
    }

    static DefaultToolDefinition from(Method method){
        return ToolDefinition.builder(method).build();
    }
}
