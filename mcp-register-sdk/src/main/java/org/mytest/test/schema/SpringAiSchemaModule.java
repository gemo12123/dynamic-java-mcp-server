package org.mytest.test.schema;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.victools.jsonschema.generator.*;
import com.github.victools.jsonschema.generator.Module;
import org.mytest.test.annotation.ToolParam;
import org.springframework.lang.Nullable;
import org.springframework.util.StringUtils;

import java.util.stream.Stream;

/**
 * @author gemo
 * @date 2026/5/27 14:30
 */
public class SpringAiSchemaModule implements Module {

    private final boolean requiredByDefault;

    public SpringAiSchemaModule(Option... options) {
        this.requiredByDefault = Stream.of(options)
                .noneMatch(option -> option == Option.PROPERTY_REQUIRED_FALSE_BY_DEFAULT);
    }

    @Override
    public void applyToConfigBuilder(SchemaGeneratorConfigBuilder builder) {
        this.applyToConfigBuilder(builder.forFields());
    }

    private void applyToConfigBuilder(SchemaGeneratorConfigPart<FieldScope> configPart) {
        configPart.withDescriptionResolver(this::resolveDescription);
        configPart.withRequiredCheck(this::checkRequired);
    }

    /**
     * Extract description from {@code @ToolParam(description = ...)} for the given field.
     */
    @Nullable
    private String resolveDescription(MemberScope<?, ?> member) {
        ToolParam toolParamAnnotation = member.getAnnotationConsideringFieldAndGetter(ToolParam.class);
        if (toolParamAnnotation != null && StringUtils.hasText(toolParamAnnotation.description())) {
            return toolParamAnnotation.description();
        }
        return null;
    }

    /**
     * Determines whether a property is required based on the presence of a series of
     * annotations.
     * <p>
     * <ul>
     * <li>{@code @ToolParam(required = ...)}</li>
     * <li>{@code @JsonProperty(required = ...)}</li>
     * <li>{@code @Schema(required = ...)}</li>
     * <li>{@code @Nullable}</li>
     * </ul>
     * <p>
     * If none of these annotations are present, the default behavior is to consider the
     * property as required, unless the {@link Option#PROPERTY_REQUIRED_FALSE_BY_DEFAULT}
     * option is set.
     */
    private boolean checkRequired(MemberScope<?, ?> member) {
        ToolParam toolParamAnnotation = member.getAnnotationConsideringFieldAndGetter(ToolParam.class);
        if (toolParamAnnotation != null) {
            return toolParamAnnotation.required();
        }

        JsonProperty propertyAnnotation = member.getAnnotationConsideringFieldAndGetter(JsonProperty.class);
        if (propertyAnnotation != null) {
            return propertyAnnotation.required();
        }


        Nullable nullableAnnotation = member.getAnnotationConsideringFieldAndGetter(Nullable.class);
        if (nullableAnnotation != null) {
            return false;
        }

        return this.requiredByDefault;
    }

    /**
     * Options for customizing the behavior of the module.
     */
    public enum Option {

        /**
         * Properties are only required if marked as such via one of the supported
         * annotations.
         */
        PROPERTY_REQUIRED_FALSE_BY_DEFAULT

    }

}
