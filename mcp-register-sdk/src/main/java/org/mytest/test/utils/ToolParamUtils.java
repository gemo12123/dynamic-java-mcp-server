package org.mytest.test.utils;

import org.mytest.test.annotation.PathParam;
import org.mytest.test.annotation.ToolParam;
import org.mytest.test.definition.PathParamDefinition;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * @author gemo
 * @date 2026/5/27 13:49
 */
public class ToolParamUtils {
    public static List<PathParamDefinition> parsePathParam(Method method){
        List<PathParamDefinition> list = new ArrayList<>();
        for (int i = 0; i < method.getParameterCount(); i++) {
            Parameter parameter = method.getParameters()[i];
            ToolParam toolParamAnnotation = parameter.getAnnotation(ToolParam.class);
            if (toolParamAnnotation == null || toolParamAnnotation.ignore()) {
                continue;
            }
            PathParam pathParam = toolParamAnnotation.pathParam();
            if (pathParam == null || !StringUtils.hasText(pathParam.pathPlaceholder())) {
                continue;
            }
            PathParamDefinition pathParamDefinition = new PathParamDefinition();
            pathParamDefinition.setPathPlaceHolder(pathParam.pathPlaceholder());
            pathParamDefinition.setParamName(parameter.getName());
            pathParamDefinition.setIsConstant(pathParam.isConstant());
            pathParamDefinition.setConstantValue(pathParam.constantValue());
            list.add(pathParamDefinition);
        }
        return list;
    }
}
