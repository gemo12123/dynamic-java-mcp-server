package org.mytest.test.annotation;

import java.lang.annotation.*;

/**
 * @author gemo
 * @date 2025/11/28 16:19
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@Documented
public @interface PathParam {
    String pathPlaceholder() default "";

    boolean isConstant() default false;

    String constantValue() default "";
}
