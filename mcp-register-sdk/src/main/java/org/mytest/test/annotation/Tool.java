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

    /**
     * 工具名，如果不提供将使用方法名
     * @return
     */
    String name() default "";

    /**
     * 工具描述
     *
     * @return
     */
    String description() default "";

    /**
     * 所属模块
     *
     * @return
     */
    String module() default "";

    /**
     * 向MCP Client返回时，是否包含结构化输出，
     * 即返回值为{"code":200, data:"xxxx"}结构时，是否只返回data中的数据
     *
     * @return
     */
    boolean removeStructResponse() default true;
}
