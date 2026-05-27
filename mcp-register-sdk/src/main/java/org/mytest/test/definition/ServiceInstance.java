package org.mytest.test.definition;

import lombok.Data;

/**
 * @author gemo
 */
@Data
public class ServiceInstance {
    private String moduleId;
    private String ip;
    private int port;
    private String contextPath;

}
