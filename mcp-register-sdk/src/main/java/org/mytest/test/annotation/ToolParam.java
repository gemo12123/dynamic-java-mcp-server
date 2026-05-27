package org.mytest.test.annotation;

import java.lang.annotation.*;

/**
 * @author gemo
 * @date 2025/11/28 16:17
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Documented
public @interface ToolParam {

    String description() default "";

    boolean required() default true;

    boolean ignore() default false;

    PathParam pathParam() default @PathParam;
}
