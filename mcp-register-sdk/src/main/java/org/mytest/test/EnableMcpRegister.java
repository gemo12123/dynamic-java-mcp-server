package org.mytest.test;

import org.mytest.test.config.McpRegisterConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author gemo
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(McpRegisterConfiguration.class)
public @interface EnableMcpRegister {
}
