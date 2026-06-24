package org.mytest.test.module;

import org.mytest.test.common.definition.ModuleDefinition;

import java.util.Collection;

/**
 * @author gemo
 * @date 2026/6/24 17:40
 */
public interface ModuleManager {

    /**
     * 注册
     *
     * @param definition
     */
    void register(ModuleDefinition definition);

    /**
     * 判断是否存在
     *
     * @param moduleId
     * @return
     */
    boolean exists(String moduleId);

    /**
     * 查询全部模块定义
     *
     * @return
     */
    Collection<ModuleDefinition> findAllModuleDefinitions();

    /**
     * 注销模块
     *
     * @param moduleId
     */
    void invalidate(String moduleId);

    /**
     * 升级
     *
     * @param moduleId
     * @param definition
     */
    void upgrade(String moduleId, ModuleDefinition definition);
}
