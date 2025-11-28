package org.mytest.test.annotation;

import java.lang.annotation.*;

/**
 * @author gemo
 * @date 2025/11/28 16:11
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface Tool {

    String name() default "";

    String description() default "";
}
