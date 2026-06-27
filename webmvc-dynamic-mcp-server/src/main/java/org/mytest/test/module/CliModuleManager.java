package org.mytest.test.module;

import org.mytest.test.common.definition.ModuleDefinition;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author gemo
 * @date 2026/6/27 15:55
 */
public class CliModuleManager implements ModuleManager{
    private static final Map<String, ModuleDefinition> MODULE_CACHE = new ConcurrentHashMap<>();

    @Override
    public void register(ModuleDefinition definition) {
        MODULE_CACHE.put(definition.getModuleId(), definition);
    }

    @Override
    public boolean exists(String moduleId) {
        return MODULE_CACHE.containsKey(moduleId);
    }

    @Override
    public Collection<ModuleDefinition> findAllModuleDefinitions() {
        return MODULE_CACHE.values();
    }

    @Override
    public void invalidate(String moduleId) {
        MODULE_CACHE.remove(moduleId);
    }

    @Override
    public void upgrade(String moduleId, ModuleDefinition definition) {
        MODULE_CACHE.put(definition.getModuleId(), definition);
    }
}
