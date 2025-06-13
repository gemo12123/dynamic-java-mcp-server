package org.mytest.test.router;

import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

/**
 * @author gemo
 */
public interface McpRouter extends RouterFunction<ServerResponse> {

    /**
     * 注册
     *
     * @param moduleId
     * @param routes
     */
    void register(String moduleId, RouterFunction<ServerResponse> routes);

    /**
     * 注销指定模块
     *
     * @param moduleId
     */
    void invalidate(String moduleId);

}
