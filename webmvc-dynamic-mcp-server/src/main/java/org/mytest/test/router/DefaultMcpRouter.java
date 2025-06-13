package org.mytest.test.router;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.*;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author gemo
 */
@Component
public class DefaultMcpRouter implements McpRouter {

    private static final RouterFunction<ServerResponse> DEFAULT_ROUTER = RouterFunctions.route()
            .GET("/ping", r -> ServerResponse.async("pong"))
            .build();

    private static final Map<String, RouterFunction<ServerResponse>> ROUTER_CACHE = new ConcurrentHashMap<>();

    private volatile RouterFunction<ServerResponse> route = DEFAULT_ROUTER;

    @Override
    public Optional<HandlerFunction<ServerResponse>> route(ServerRequest request) {
        return this.route.route(request);
    }

    @Override
    public synchronized void register(String moduleId, RouterFunction<ServerResponse> routes) {
        ROUTER_CACHE.put(moduleId, routes);
        redirect();
    }

    private void redirect() {
        this.route = ROUTER_CACHE.values()
                .stream()
                .reduce(RouterFunction::and)
                .orElse(DEFAULT_ROUTER);
    }

    @Override
    public void invalidate(String moduleId) {
        if (StringUtils.isNotEmpty(moduleId)) {
            ROUTER_CACHE.remove(moduleId);
        }

    }

}
