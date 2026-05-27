package org.mytest.test.definition;

import lombok.Data;

import java.util.List;

/**
 * @author gemo
 * @date 2026/5/27 17:16
 */
@Data
public class ServiceReportInfo {
    private List<ModuleDefinition> modules;
    private ServiceInstance serviceInstance;
}
