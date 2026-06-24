package org.mytest.test.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;

/**
 * @author gemo
 * @date 2026/6/24 17:20
 */
public class IpUtils {
    public static String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (isInvalidIp(ip)) {
            ip = request.getHeader("X-Real-IP");
        }

        if (isInvalidIp(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }

        if (isInvalidIp(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }

        if (isInvalidIp(ip)) {
            ip = request.getRemoteAddr();
        }

        // X-Forwarded-For 可能是：client, proxy1, proxy2
        // 一般第一个是原始客户端 IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        // 本机 IPv6 地址转 IPv4
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }

        return StringUtils.isNotBlank(ip) ? ip : request.getRemoteAddr();
    }

    private static boolean isInvalidIp(String ip) {
        return ip == null
                || ip.length() == 0
                || "unknown".equalsIgnoreCase(ip);
    }
}
