package org.mytest.test.component.provider;

import org.mytest.test.properties.DynamicMcpRegisterProperties;

import java.util.Optional;

/**
 * @author gemo
 * @date 2025/11/28 16:40
 */
public class FixedAddressProvider implements AddressProvider{

    private final DynamicMcpRegisterProperties properties;

    public FixedAddressProvider(DynamicMcpRegisterProperties properties) {
        this.properties = properties;
    }

    @Override
    public Optional<String> getIpAddress() {
        return Optional.ofNullable(properties.getReportConfiguration())
                .map(item -> item.getFixedUrlPrefix());
    }
}
