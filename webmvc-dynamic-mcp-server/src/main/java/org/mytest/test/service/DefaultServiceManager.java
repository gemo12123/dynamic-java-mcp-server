package org.mytest.test.service;

import org.apache.commons.lang3.StringUtils;
import org.mytest.test.definition.ServiceInstance;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author gemo
 */
@Component
public class DefaultServiceManager implements ServiceManager {

    private static final Map<String, Map<ServiceInstance, Long>> SERVICE_INSTANCE_CACHE = new ConcurrentHashMap<>();

    @Override
    public void register(String moduleId, List<ServiceInstance> serviceInstances) {
        Map<ServiceInstance, Long> services = SERVICE_INSTANCE_CACHE.computeIfAbsent(moduleId, k -> new ConcurrentHashMap<>());
        for (ServiceInstance serviceInstance : serviceInstances) {
            services.put(serviceInstance, System.currentTimeMillis());
        }
    }

    @Override
    public void invalidate(String moduleId) {
        if (StringUtils.isNotEmpty(moduleId)) {
            SERVICE_INSTANCE_CACHE.remove(moduleId);
        }
    }

    @Override
    public Optional<ServiceInstance> getServiceInstance(String moduleId) {
        return Optional.ofNullable(SERVICE_INSTANCE_CACHE.get(moduleId))
                .map(Map::keySet)
                .stream()
                .flatMap(Collection::stream)
                .findFirst();
    }
}
