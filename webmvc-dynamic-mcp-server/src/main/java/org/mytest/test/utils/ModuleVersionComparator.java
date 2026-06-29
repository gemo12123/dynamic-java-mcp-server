package org.mytest.test.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author gemo
 * @date 2026/6/24 18:03
 */
@Slf4j
public class ModuleVersionComparator implements Comparator<String> {

    private static final Integer MAX_VERSION_NUM_SIZE = 3;

    public static final ModuleVersionComparator INSTANCE = new ModuleVersionComparator();

    private ModuleVersionComparator() {
    }

    public static int versionCompare(String v1, String v2) {
        return INSTANCE.compare(v1, v2);
    }

    @Override
    public int compare(String v1, String v2) {
        if (StringUtils.isEmpty(v1) || StringUtils.isEmpty(v2)) {
            throw new IllegalArgumentException("version 数据为空！v1:" + v1 + ", v2:" + v2);
        }
        try {
            // 分割为整数数组，允许更多段
            List<Integer> v1List = Arrays.stream(v1.split("\\."))
                    .map(Integer::parseInt)
                    .toList();
            List<Integer> v2List = Arrays.stream(v2.split("\\."))
                    .map(Integer::parseInt)
                    .toList();

            if (v1List.size() != MAX_VERSION_NUM_SIZE || v2List.size() != MAX_VERSION_NUM_SIZE) {
                throw new IllegalArgumentException("version 格式异常！v1:" + v1 + ", v2:" + v2);
            }

            for (int i = 0; i < MAX_VERSION_NUM_SIZE; i++) {
                int diff = v1List.get(i) - v2List.get(i);
                if (diff != 0) {
                    return diff > 0 ? 1 : -1;
                }
            }
            return 0;
        } catch (Exception e) {
            log.error("version 比较时出现异常！v1:{}, v2:{}", v1, v2);
            throw new RuntimeException(e);
        }
    }
}
