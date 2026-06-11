package org.mytest.test.component.capture;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.mytest.test.annotation.Tool;
import org.mytest.test.common.definition.DefaultToolDefinition;
import org.mytest.test.common.definition.PathParamDefinition;
import org.mytest.test.common.definition.ToolDefinition;
import org.mytest.test.common.definition.ToolDefinitionWrapper;
import org.mytest.test.utils.JsonParser;
import org.mytest.test.utils.ToolParamUtils;
import org.mytest.test.utils.ToolUtils;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * @author gemo
 * @date 2025/11/28 16:12
 */
@Slf4j
public class McpToolCapture implements SmartInitializingSingleton {

    @Getter
    private final Map<String, ToolDefinitionWrapper> toolDefinitions = new ConcurrentHashMap<>();
    @Getter
    private final Map<String, List<ToolDefinitionWrapper>> moduleToolDefinitions = new ConcurrentHashMap<>();

    private final ConfigurableApplicationContext context;

    public McpToolCapture(ConfigurableApplicationContext context) {
        this.context = context;
    }

    @Override
    public void afterSingletonsInstantiated() {
        ConfigurableListableBeanFactory beanFactory = context.getBeanFactory();
        String[] beanDefinitionNames = beanFactory.getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanDefinitionName);
            String beanClassName = beanDefinition.getBeanClassName();
            if (beanClassName == null) {
                continue;
            }
            try {
                Class<?> clazz = Class.forName(beanClassName);
                RequestMapping classRequestMapping = AnnotatedElementUtils.findMergedAnnotation(clazz, RequestMapping.class);
                String controllerPath = null;
                if (classRequestMapping != null && classRequestMapping.path().length > 0) {
                    controllerPath = classRequestMapping.path()[0];
                }
                controllerPath = formatPath(controllerPath);
                tryParse(clazz, controllerPath);
            } catch (ClassNotFoundException e) {
                if (log.isDebugEnabled()) {
                    log.debug("未找到Bean对应的类！", e);
                }
            } catch (Exception e) {
                log.warn("未知异常！", e);
            }
        }

        log.info("获取到{}个工具!\n{}", toolDefinitions.size(), formatToolDefinitions());
    }

    private void tryParse(Class<?> clazz, String controllerPath) {
        for (Method method : clazz.getDeclaredMethods()) {
            Tool tool = method.getAnnotation(Tool.class);
            if (tool == null) {
                continue;
            }

            RequestMapping requestMapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
            if (requestMapping == null) {
                log.warn("class:{}, method:{} 标记了@Tool注解，但是未检测到标注@RequestMapping，跳过该方法的注册！",
                        clazz.getSimpleName(),
                        method.getName());
                continue;
            }

            if (requestMapping.path().length == 0) {
                log.warn("class:{}, method:{} 未显式声明请求路径！",
                        clazz.getSimpleName(),
                        method.getName());
                continue;
            }

            String path = formatPath(requestMapping.path()[0]);
            if (!StringUtils.hasText(path)) {
                log.warn("class:{}, method:{} 请求路径参数未配置！",
                        clazz.getSimpleName(),
                        method.getName());
                continue;
            }

            String requestPath = mergePath(controllerPath, path);
            if (requestPath.contains("*")) {
                log.warn("class:{}, method:{} 请求路径参数配置异常：{}",
                        clazz.getSimpleName(),
                        method.getName(),
                        requestPath);
                continue;
            }

            List<PathParamDefinition> pathParamDefinitions = ToolParamUtils.parsePathParam(method);
            if (!CollectionUtils.isEmpty(pathParamDefinitions)) {
                String tempRequestPath = requestPath;
                for (PathParamDefinition pathParamDefinition : pathParamDefinitions) {
                    String pathPlaceHolder = pathParamDefinition.getPathPlaceHolder();
                    tempRequestPath = tempRequestPath.replace("{" + pathPlaceHolder + "}", pathPlaceHolder);
                }
                if (tempRequestPath.contains("{")) {
                    log.warn("class:{}, method:{} 请求路径参数配置异常!路径参数：{}，占位符：{}",
                            clazz.getSimpleName(),
                            method.getName(),
                            requestPath,
                            pathParamDefinitions);
                    continue;
                }
            }

            DefaultToolDefinition toolDefinition = ToolUtils.buildDefaultToolDefinitionFromMethod(method);
            RequestMethod requestMethod = requestMapping.method()[0];
            ToolDefinitionWrapper toolDefinitionWrapper = ToolDefinitionWrapper.builder()
                    .requestMethod(String.valueOf(requestMethod))
                    .requestPath(requestPath)
                    .pathParams(pathParamDefinitions)
                    .toolDefinition(toolDefinition)
                    .build();
            this.moduleToolDefinitions.computeIfAbsent(tool.module(), k -> new CopyOnWriteArrayList<>()).add(toolDefinitionWrapper);
            this.toolDefinitions.put(clazz.getName() + "#" + method.getName(), toolDefinitionWrapper);
        }
    }


    public String formatPath(String pattern) {
        if (!StringUtils.hasText(pattern)) {
            return "";
        }
        if (!pattern.startsWith("/")) {
            pattern = "/" + pattern;
        }
        if (pattern.endsWith("/")) {
            pattern = pattern.substring(0, pattern.length() - 1);
        }
        return pattern;
    }

    public String mergePath(String controllerPath, String requestPath) {
        return controllerPath + requestPath;
    }

    private Object formatToolDefinitions() {
        if (this.toolDefinitions.isEmpty()) {
            return "";
        }
        return "工具列表：\n" + this.toolDefinitions.entrySet()
                .stream()
                .map(entry -> String.format("\t- %s: %s", entry.getKey(),
                        JsonParser.toJson(entry.getValue())))
                .collect(Collectors.joining("\n"));
    }

}
