package org.mytest.test.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;

/**
 * @author gemo
 * @date 2026/6/24 18:03
 */
@Slf4j
public class ModuleVersionComparator implements Comparator<String> {

    public static final ModuleVersionComparator INSTANCE = new ModuleVersionComparator();

    private ModuleVersionComparator() {
    }

    public static int versionCompare(String v1, String v2){
        return INSTANCE.compare(v1, v2);
    }

    @Override
    public int compare(String o1, String o2) {
        // TODO
        return 0;
    }
}
