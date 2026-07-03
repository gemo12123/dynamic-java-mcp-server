package org.mytest.test.common.definition;

import lombok.Data;
import org.mytest.test.common.utils.ParsingUtils;

/**
 * @author gemo
 * @date 2025/11/28 16:41
 */
@Data
public class DefaultToolDefinition implements ToolDefinition{
    private String name;
    private String description;
    private String inputSchema;

    public DefaultToolDefinition() {
    }

    public DefaultToolDefinition(String name, String description, String inputSchema) {
        this.name = name;
        this.description = description;
        this.inputSchema = inputSchema;
    }


    public static Builder builder() {
        return new Builder();
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String description() {
        return description;
    }

    @Override
    public String inputSchema() {
        return inputSchema;
    }

    public static final class Builder {

        private String name;

        private String description;

        private String inputSchema;

        private Builder() {
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder inputSchema(String inputSchema) {
            this.inputSchema = inputSchema;
            return this;
        }

        public DefaultToolDefinition build() {
            if (this.description != null && !this.description.isEmpty()) {
                this.description = ParsingUtils.reConcatenateCamelCase(this.name, " ");
            }
            return new DefaultToolDefinition(this.name, this.description, this.inputSchema);
        }

    }
}
