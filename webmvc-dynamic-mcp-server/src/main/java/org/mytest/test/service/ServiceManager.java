package org.mytest.test.service;


import org.mytest.test.common.definition.ServiceInstance;

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
     * @param serviceInstance
     */
    void register(String moduleId, ServiceInstance serviceInstance);

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
