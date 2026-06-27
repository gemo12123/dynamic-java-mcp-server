package org.mytest.test.service;

import com.google.common.collect.Multimap;
import org.mytest.test.common.definition.ServiceInstance;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author gemo
 * @date 2026/6/27 16:00
 */
public class CliServiceManager implements ServiceManager {

    private static final Map<String, Map<ServiceInstance, Long>> SERVICE_CACHE = new ConcurrentHashMap<>();

    @Override
    public void register(String moduleId, ServiceInstance serviceInstance) {
        if (serviceInstance == null) {
            return;
        }
        Map<ServiceInstance, Long> services = SERVICE_CACHE.computeIfAbsent(moduleId, k -> new ConcurrentHashMap<>());
        services.put(serviceInstance, System.currentTimeMillis());
    }

    @Override
    public void invalidate(String moduleId) {
        SERVICE_CACHE.remove(moduleId);
    }

    @Override
    public Optional<ServiceInstance> getServiceInstance(String moduleId) {
        return Optional.ofNullable(SERVICE_CACHE.get(moduleId))
                .map(Map::keySet)
                .stream()
                .flatMap(Collection::stream)
                .findFirst();
    }

    @Override
    public Map<String, Map<ServiceInstance, Long>> getServiceInstancesWithLastVisitTime() {
        return SERVICE_CACHE;
    }

    @Override
    public void removeServiceInstances(Multimap<String, ServiceInstance> serviceInstances) {
        for (Map.Entry<String, Map<ServiceInstance, Long>> entry : SERVICE_CACHE.entrySet()) {
            Collection<ServiceInstance> collection = serviceInstances.get(entry.getKey());
            for (ServiceInstance serviceInstance : collection) {
                entry.getValue().remove(serviceInstance);
            }
        }
    }
}
