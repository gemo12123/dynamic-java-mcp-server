package org.mytest.test.utils;

import org.mytest.test.annotation.Tool;
import org.mytest.test.common.definition.DefaultToolDefinition;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author gemo
 * @date 2026/5/27 14:02
 */
public class ToolUtils {
    private static final List<Class<?>> WRAPPER_TYPES = Arrays.asList(
            Boolean.class,
            Character.class,
            Byte.class,
            Short.class,
            Integer.class,
            Long.class,
            Float.class,
            Double.class,
            Void.class,
            String.class
    );

    private ToolUtils() {
    }

    public static String getToolName(Method method) {
        Assert.notNull(method, "method cannot be null");
        Tool tool = method.getAnnotation(Tool.class);
        if (tool == null) {
            return method.getName();
        }

        return StringUtils.hasText(tool.name()) ? tool.name() : method.getName();
    }

    public static String getToolDescription(Method method) {
        Assert.notNull(method, "method cannot be null");
        Tool tool = method.getAnnotation(Tool.class);
        if (tool == null) {
            return org.mytest.test.common.utils.ParsingUtils.reConcatenateCamelCase(method.getName(), " ");
        }

        return StringUtils.hasText(tool.description()) ? tool.description() : method.getName();
    }

    public static String getToolDescriptionFromName(String toolName) {
        Assert.notNull(toolName, "toolName cannot be null");
        return org.mytest.test.common.utils.ParsingUtils.reConcatenateCamelCase(toolName, " ");
    }

    public static boolean isWrapperOrStringType(Class<?> clazz) {
        return WRAPPER_TYPES.contains(clazz);
    }

    public static boolean isCollectionOrMapType(Class<?> clazz) {
        return Collection.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz);
    }


    public static DefaultToolDefinition buildDefaultToolDefinitionFromMethod(Method method){
        return DefaultToolDefinition.builder()
                .name(ToolUtils.getToolName(method))
                .description(ToolUtils.getToolDescription(method))
                .inputSchema(JsonSchemaGenerator.generateForMethodInput(method))
                .build();
    }
}
