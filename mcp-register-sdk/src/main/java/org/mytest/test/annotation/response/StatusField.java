package org.mytest.test.annotation.response;

import java.lang.annotation.*;

/**
 * @author gemo
 * @date 2026/6/29 15:55
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
@Documented
public @interface StatusField {
    String expect();
}
