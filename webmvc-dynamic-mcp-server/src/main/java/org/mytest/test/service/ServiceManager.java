package org.mytest.test.service;


import com.google.common.collect.Multimap;
import org.mytest.test.common.definition.ServiceInstance;

import java.util.List;
import java.util.Map;
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

    /**
     * 获取全部服务实例及其末次访问时间
     *
     * @return
     */
    Map<String, Map<ServiceInstance, Long>> getServiceInstancesWithLastVisitTime();

    void removeServiceInstances(Multimap<String, ServiceInstance> serviceInstances);
}
