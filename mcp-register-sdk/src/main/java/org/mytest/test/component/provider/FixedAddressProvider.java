package org.mytest.test.component.provider;

import java.util.Optional;

/**
 * @author gemo
 * @date 2025/11/28 16:40
 */
public class FixedAddressProvider implements AddressProvider{
    @Override
    public Optional<String> getIpAddress() {
        return Optional.empty();
    }
}
