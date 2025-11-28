package org.mytest.test.definition;

import lombok.Data;

/**
 * @author gemo
 * @date 2025/11/28 16:47
 */
@Data
public class PathParamDefinition {
    private String pathPlaceHolder;

    private String paramName;

    private Boolean isConstant;

    private String constantValue;
}
