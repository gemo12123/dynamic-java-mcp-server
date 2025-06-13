package org.mytest.test.service;

import org.mytest.test.definition.ServiceInstance;

import java.util.List;
import java.util.Optional;

/**
 * @author gemo
 */
public interface ServiceManager {

    /**
     * 注册服务
     *
     * @param moduleId
     * @param serviceInstances
     */
    void register(String moduleId, List<ServiceInstance> serviceInstances);

    /**
     * 注销指定模块
     *
     * @param moduleId
     */
    void invalidate(String moduleId);

    /**
     * 获取指定模块的服务
     *
     * @param moduleId
     * @return
     */
    Optional<ServiceInstance> getServiceInstance(String moduleId);
}
