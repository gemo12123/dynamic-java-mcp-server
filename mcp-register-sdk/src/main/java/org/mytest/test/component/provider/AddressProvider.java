package org.mytest.test.component.provider;

import java.util.Optional;

/**
 * @author gemo
 * @date 2025/11/28 16:12
 */
public interface AddressProvider {

    /**
     * 获取注册地址
     * @return
     */
    Optional<String> getIpAddress();
}
