package org.mytest.test.common.definition;

import lombok.Data;

/**
 * @author gemo
 * @date 2026/6/29 15:58
 */
@Data
public class StructResponseDefinition {
    private Boolean removeStructResponse;

    private String statusField;

    private String statusExpectValue;

    private String dataField;
}
