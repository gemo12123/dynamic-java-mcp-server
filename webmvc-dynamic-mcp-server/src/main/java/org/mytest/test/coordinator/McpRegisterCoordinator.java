package org.mytest.test.coordinator;

import org.mytest.test.definition.ModuleDefinition;

/**
 * @author gemo
 */
public interface McpRegisterCoordinator {

    /**
     * 注册模块
     *
     * @param moduleDefinition
     */
    void register(ModuleDefinition moduleDefinition);

    /**
     * 初始化
     */
    void initialize();

    /**
     * 注销模块
     *
     * @param moduleId
     */
    void invalidate(String moduleId);
}
